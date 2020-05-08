 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.opencit.ui.web.util;
 
 import java.util.List;
 
 import org.openengsb.domain.report.model.Report;
 import org.openengsb.domain.report.model.ReportPart;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 
 public final class StateUtil {
     private StateUtil() {
     }
 
     public static String getImage(Project project) {
         if (project.getState() == null) {
             return "images/traffic_light_none.png";
         }
         switch (project.getState()) {
             case OK:
                 return "images/traffic_light_green.png";
             case IN_PROGRESS:
                 return "images/traffic_light_yellow.png";
             case FAILURE:
                 return "images/traffic_light_red.png";
             default:
                 return "images/traffic_light_none.png";
         }
     }
 
     private static boolean isSuccessReport(Report report) {
         List<ReportPart> parts = report.getParts();
         for (ReportPart p : parts) {
            if (p.getPartName().contains("FailEvent")) {
                return false;
            }
         }
         return true;
     }
 
     public static String getImage(Report report) {
        if (isSuccessReport(report)) {
            return "images/traffic_light_green.png";
        }
         return "images/traffic_light_red.png";
     }
 }
