 /*
  * Copyright © 2012, Source Tree, All Rights Reserved
  * 
  * JobsInitializerBean.java
  * Modification History
  * *************************************************************
  * Date				Author						Comment
  * Dec 02,2012		Venkaiah Chowdary Koneru	Created
  * *************************************************************
  */
 package org.sourcetree.interview.support.schedulers;
 
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.impl.StdSchedulerFactory;
 import org.sourcetree.interview.support.EmailUtil;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 /**
  * All the Scheduler jobs initializes must be done here.
  * 
  * @author Venkaiah Chowdary Koneru
  */
 @Component
 public class JobsInitializerBean
 {
 	private static final Log LOG = LogFactory.getLog(JobsInitializerBean.class);
 
 	@Value("#{'emailjob.cron.expression'}")
 	private String emailJobCronExpression;
 
 	@Value("#{'email.server'}")
 	private String emailServer;
 
	@Value("#{'email.port'}")
 	private String emailServerPort;
 
 	@Value("#{'email.login.id'}")
 	private String emailLoginId;
 
 	@Value("#{'email.login.password'}")
 	private String emailLoginPassword;
 
 	@Value("#{'email.starttls'}")
 	private String emailStartTls;
 
 	@Value("#{'email.origin'}")
 	private String emailOrigin;
 
 	@Value("#{'email.origin.name'}")
 	private String emailOriginName;
 
 	@Value("#{'admin.email.enabled'}")
 	private String adminEmailEnabled;
 
 	@Value("#{'admin.email'}")
 	private String adminEmail;
 
 	@Value("#{'admin.email.prefix'}")
 	private String adminEmailPrefix;
 
 	@Value("#{'email.signature'}")
 	private String emailSignature;
 
 	private Scheduler scheduler;
 
 	private Map<String, String> emailConfig;
 
 	/**
 	 * after properties set method which executes the actual schedulers
 	 * initialization.
 	 * 
 	 * @throws SchedulerException
 	 */
 	@PostConstruct
 	public void initializeJobs() throws SchedulerException
 	{
 		scheduler = StdSchedulerFactory.getDefaultScheduler();
 		scheduler.start();
 
 		try
 		{
 			// Email Job
 			if (!StringUtils.isBlank(emailJobCronExpression))
 			{
 				emailConfig = new HashMap<String, String>();
 				emailConfig.put(EmailUtil.EMAIL_HOST, emailServer);
 				emailConfig.put(EmailUtil.EMAIL_SERVER_PORT, emailServerPort);
 				emailConfig.put(EmailUtil.EMAIL_ID, emailLoginId);
 				emailConfig.put(EmailUtil.EMAIL_PASSWORD, emailLoginPassword);
 				emailConfig.put(EmailUtil.EMAIL_STARTTLS, emailStartTls);
 
 				emailConfig.put(EmailUtil.EMAIL_ORIGIN, emailOrigin);
 
 				if (!StringUtils.isBlank(emailOriginName))
 				{
 					emailConfig.put(EmailUtil.EMAIL_ORIGIN_NAME,
 							emailOriginName);
 				}
 
 				emailConfig.put(EmailUtil.ADMIN_EMAIL, adminEmail);
 				emailConfig.put(EmailUtil.ADMIN_EMAIL_ENABLED,
 						adminEmailEnabled);
 
 				if (!StringUtils.isBlank(adminEmailPrefix))
 				{
 					emailConfig.put(EmailUtil.ADMIN_EMAIL_PREFIX,
 							adminEmailPrefix);
 				}
 
 				if (!StringUtils.isBlank(emailSignature))
 				{
 					emailConfig.put(EmailUtil.EMAIL_SIGNATURE, emailSignature);
 				}
 
 				Map<String, Object> beanMap = new HashMap<String, Object>();
 				beanMap.put(JobsInitializerBean.class.getSimpleName(), this);
 
 				SchedulerJobsUtil.scheduleCronJob(scheduler,
 						EmailJob.class.getSimpleName(), EmailJob.class,
 						beanMap, emailJobCronExpression);
 			}
 		}
 		catch (ParseException e)
 		{
 			LOG.error("Error scheduling Email Job.", e);
 		}
 	}
 
 	/**
 	 * destroys the schedulers before shutdown.
 	 * 
 	 * @throws SchedulerException
 	 */
 	@PreDestroy
 	public void beforeDestroy() throws SchedulerException
 	{
 		scheduler.shutdown(true);
 	}
 
 	/**
 	 * @return scheduler
 	 */
 	public Scheduler getScheduler()
 	{
 		return scheduler;
 	}
 
 	/**
 	 * @return emailConfig
 	 */
 	public Map<String, String> getEmailConfig()
 	{
 		return emailConfig;
 	}
 }
