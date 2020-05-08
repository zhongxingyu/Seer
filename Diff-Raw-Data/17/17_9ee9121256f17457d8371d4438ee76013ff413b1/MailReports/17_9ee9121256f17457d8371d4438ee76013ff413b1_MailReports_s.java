 /*
  * Copyright 2011 Damien Bourdette
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.dbourdette.otto.source;
 
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 import java.util.Date;
 
import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.mail.MessagingException;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.quartz.CronScheduleBuilder;
 import org.quartz.JobBuilder;
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.JobKey;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.quartz.TriggerBuilder;
 import org.quartz.TriggerKey;
 import org.springframework.stereotype.Component;
 
 import com.github.dbourdette.otto.quartz.SendReportJob;
 import com.github.dbourdette.otto.report.Report;
 import com.github.dbourdette.otto.report.ReportPeriod;
 import com.github.dbourdette.otto.service.mail.Mail;
 import com.github.dbourdette.otto.service.mail.Mailer;
 import com.github.dbourdette.otto.source.config.MailReportConfig;
 import com.github.dbourdette.otto.source.config.ReportConfig;
 import com.github.dbourdette.otto.web.util.Pair;
 
 /**
  * Helper class for mail reports
  *
  * @author damien bourdette
  */
 @Component
 public class MailReports {
     private static final String JOB_NAME = "SendReportJob";
 
     private static final String JOB_GROUP = "DEFAULT";
 
     public static final String SOURCE_NAME = "sourceName";
 
     public static final String REPORT_ID = "reportId";
 
     @Inject
     private Scheduler quartzScheduler;
 
     @Inject
     private Mailer mailer;
 
    @PostConstruct
     public void initScheduler() throws SchedulerException, ParseException {
         JobDetail jobDetail = JobBuilder.newJob(SendReportJob.class)
                 .withIdentity(new JobKey(JOB_NAME, JOB_GROUP))
                 .storeDurably()
                 .build();
 
         quartzScheduler.addJob(jobDetail, false);
 
         for (Source source : Source.findAll()) {
             for (MailReportConfig mailReportConfig : source.getMailReports()) {
                 schedule(mailReportConfig);
             }
         }
     }
 
     public void onReportChange(MailReportConfig mailReportConfig) throws ParseException, SchedulerException {
         schedule(mailReportConfig);
     }
 
     public void onReportDeleted(MailReportConfig mailReportConfig) throws SchedulerException {
         unschedule(mailReportConfig);
     }
 
     public void sendReport(MailReportConfig mailReport) throws MessagingException, UnsupportedEncodingException {
         Source source = Source.findByName(mailReport.getSourceName());
 
         Mail mail = new Mail();
         mail.setTo(mailReport.getTo());
         mail.setSubject(mailReport.getTitle() + " sent at " + new Date() + " for period " + mailReport.getPeriod());
         mail.setHtml(buildHtml(source, mailReport));
 
         mailer.send(mail);
     }
 
     public Date previousFiretime(MailReportConfig mailReportConfig) throws SchedulerException {
         Trigger trigger = quartzScheduler.getTrigger(buildTriggerKey(mailReportConfig));
 
         if (trigger == null) {
             return null;
         }
 
         return trigger.getPreviousFireTime();
     }
 
     public Date nextFiretime(MailReportConfig mailReportConfig) throws SchedulerException {
         Trigger trigger = quartzScheduler.getTrigger(buildTriggerKey(mailReportConfig));
 
         if (trigger == null) {
             return null;
         }
 
         return trigger.getNextFireTime();
     }
 
     private boolean isScheduled(MailReportConfig mailReportConfig) throws SchedulerException {
         Trigger trigger = quartzScheduler.getTrigger(buildTriggerKey(mailReportConfig));
 
         return trigger != null;
     }
 
     private void unschedule(MailReportConfig mailReportConfig) throws SchedulerException {
         if (isScheduled(mailReportConfig)) {
             quartzScheduler.unscheduleJob(buildTriggerKey(mailReportConfig));
         }
     }
 
     private void schedule(MailReportConfig mailReportConfig) throws ParseException, SchedulerException {
         if (isScheduled(mailReportConfig) && StringUtils.isNotEmpty(mailReportConfig.getCronExpression())) {
             quartzScheduler.rescheduleJob(buildTriggerKey(mailReportConfig), buildTrigger(mailReportConfig));
         } else if (isScheduled(mailReportConfig)) {
             unschedule(mailReportConfig);
         } else if (StringUtils.isNotEmpty(mailReportConfig.getCronExpression())) {
             quartzScheduler.scheduleJob(buildTrigger(mailReportConfig));
         }
     }
 
     private Trigger buildTrigger(MailReportConfig mailReportConfig) throws ParseException {
         JobDataMap jobDataMap = new JobDataMap();
         jobDataMap.put(REPORT_ID, mailReportConfig.getId());
         jobDataMap.put(SOURCE_NAME, mailReportConfig.getSourceName());
 
         return TriggerBuilder.newTrigger().forJob(JOB_NAME, JOB_GROUP).withIdentity(buildTriggerKey(mailReportConfig))
                 .withSchedule(CronScheduleBuilder.cronSchedule(mailReportConfig.getCronExpression()))
                 .startAt(new DateTime().plusSeconds(1).toDate())
                 .usingJobData(jobDataMap)
                 .build();
     }
 
     private TriggerKey buildTriggerKey(MailReportConfig mailReportConfig) {
         return new TriggerKey(mailReportConfig.getId(), JOB_GROUP);
     }
 
     private String buildHtml(Source source, MailReportConfig mailReport) {
         String html = "";
 
         ReportPeriod period = mailReport.getPeriod();
 
         ReportConfig config = source.getReportConfigByTitle(mailReport.getReportTitle());
 
         if (config == null) {
             config = new ReportConfig();
         }
 
         Report report = source.buildReport(config, period);
 
         html += config.getTitle() + " :<br>";
 
         html += "<table>";
 
         int count = 0;
 
         for (Pair pair : report.getSums()) {
             count += pair.getValue();
         }
 
         html += "<tr>";
         html += "<td>total count</td>";
         html += "<td>";
         html += count;
         html += "</td>";
         html += "</tr>";
 
         for (Pair pair : report.getSums()) {
             html += "<tr>";
             html += "<td>";
             html += pair.getName();
             html += "</td>";
             html += "<td>";
             html += pair.getValue();
             html += "</td>";
             html += "</tr>";
         }
 
         html += "</table>";
 
         return html;
     }
 
 }
