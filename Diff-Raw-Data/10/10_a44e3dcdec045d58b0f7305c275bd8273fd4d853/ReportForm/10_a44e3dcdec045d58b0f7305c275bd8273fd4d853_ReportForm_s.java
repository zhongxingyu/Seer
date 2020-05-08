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
 
 package com.github.dbourdette.otto.web.form;
 
 import com.github.dbourdette.otto.data.DataTablePeriod;
 import com.github.dbourdette.otto.data.SimpleDataTable;
 import com.github.dbourdette.otto.source.Source;
 import com.github.dbourdette.otto.source.config.DefaultGraphParameters;
 import com.github.dbourdette.otto.source.reports.ReportConfig;
 import com.github.dbourdette.otto.web.editor.DatePropertyEditor;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author damien bourdette
  * @version \$Revision$
  */
 public class ReportForm {
     private DataTablePeriod period;
 
     private boolean advanced;
 
     private String reportId;
 
     private Date from;
 
     private Date to;
 
     private List<ReportConfig> reportConfigs;
 
     public ReportForm() {
         period = DataTablePeriod.RECENT;
     }
 
     public Interval getInterval() {
         if (advanced) {
             fixAdvancedParams();
 
            return new Interval(from.getTime(), to.getTime());
         } else {
             return period.getInterval();
         }
     }
 
     public void fillWithDefault(DefaultGraphParameters defaultParameters, HttpServletRequest request) {
         Map<String, String> map = request.getParameterMap();
 
         if (!map.containsKey("period") && defaultParameters.getPeriod() != null) {
             setPeriod(defaultParameters.getPeriod());
         }
     }
 
     public void setReportConfigs(List<ReportConfig> reportConfigs) {
         this.reportConfigs = reportConfigs;
     }
 
     public SimpleDataTable buildTable(Source source) {
         if (advanced) {
             return source.buildTable(getReportConfig(), getInterval(), Duration.standardDays(1));
         } else {
             return source.buildTable(getReportConfig(), period);
         }
     }
 
     public DataTablePeriod[] getPeriods() {
         return DataTablePeriod.values();
     }
 
     public DataTablePeriod getPeriod() {
         return period;
     }
 
     public void setPeriod(DataTablePeriod period) {
         this.period = period;
     }
 
     public String getReportId() {
         return reportId;
     }
 
     public String getReportTitle() {
         for (ReportConfig reportConfig : reportConfigs) {
             if (reportConfig.getId().equals(reportId)) {
                 return reportConfig.getTitle();
             }
         }
 
         return "";
     }
 
     public void setReportId(String reportId) {
         this.reportId = reportId;
     }
 
     public List<ReportConfig> getReportConfigs() {
         return reportConfigs;
     }
 
     public boolean isAdvanced() {
         return advanced;
     }
 
     public void setAdvanced(boolean advanced) {
         this.advanced = advanced;
     }
 
     public Date getFrom() {
         return from;
     }
 
     public void setFrom(Date from) {
         this.from = from;
     }
 
     public String getFormattedFrom() {
         return DatePropertyEditor.format(from);
     }
 
     public String getFormattedTo() {
         return DatePropertyEditor.format(to);
     }
 
     public Date getTo() {
         return to;
     }
 
     public void setTo(Date to) {
         this.to = to;
     }
 
     public ReportConfig getReportConfig() {
         for (ReportConfig reportConfig : reportConfigs) {
             if (reportConfig.getId().equals(reportId)) {
                 return reportConfig;
             }
         }
 
         if (reportConfigs.size() > 0) {
             ReportConfig reportConfig = reportConfigs.get(0);
             reportId = reportConfig.getId();
 
             return reportConfig;
         }
 
         return new ReportConfig();
     }
 
     /**
      * Ensures that from and to attributes are valid
      */
     private void fixAdvancedParams() {
         if (from == null) {
            from = DataTablePeriod.TODAY.getInterval().getStart().toDate();
         }
 
         if (to == null) {
            to = DataTablePeriod.TODAY.getInterval().getEnd().toDate();
         }
     }
 }
