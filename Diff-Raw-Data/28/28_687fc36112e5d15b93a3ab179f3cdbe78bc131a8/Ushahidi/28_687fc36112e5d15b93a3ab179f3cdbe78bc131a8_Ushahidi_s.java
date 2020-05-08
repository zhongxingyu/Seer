 package services;
 
 import com.sun.jersey.api.client.AsyncWebResource;
 import com.sun.jersey.api.client.AsyncWebResource.Builder;
 import com.sun.jersey.api.client.Client;
 import java.net.URLEncoder;
 import java.util.concurrent.Future;
 import models.Incident;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Service class to interact with Ushahidi.
  *
  * <p>
  * The information contained herein is the property of OZ Communications Inc. and is strictly proprietary and
  * confidential. Except as expressly authorized in writing by OZ Communications Inc., you do not have the right
  * to use the information contained herein and you shall not disclose it to any third party.</p>
  *
  * <br>Copyright 2011 Nokia Inc. All Rights Reserved.<br>
  *
  * @author Alexandre Normand (AlexandreN)
  * @since 12/3/11
  */
 public class Ushahidi
 {
     private static Logger LOGGER = LoggerFactory.getLogger(Ushahidi.class);
 
     private String url;
     private String username;
     private String password;
 
     public Ushahidi(String url, String username, String password)
     {
 
         this.url = url;
         this.username = username;
         this.password = password;
     }
 
     public void storeIncident(Incident report)
     {
         try
         {
             Client client = Client.create();
             //client.addFilter(new HTTPBasicAuthFilter(username, password));
             AsyncWebResource resource = client.asyncResource(url);
 
             StringBuilder contentBuilder = new StringBuilder();
             contentBuilder.append("task=report");
             addParameter(contentBuilder, "incident_title", report.incidentTitle);
             addParameter(contentBuilder, "incident_description", report.description);
             addParameter(contentBuilder, "incident_category", report.incidentCategory.getName());
            addParameter(contentBuilder, "incident_date",
                DateFormatUtils.format(report.incidentDate, "M/dd/yyyy"));
             addParameter(contentBuilder, "incident_hour", DateFormatUtils.format(report.incidentDate, "hh"));
             addParameter(contentBuilder, "incident_minute", DateFormatUtils.format(report.incidentDate, "mm"));
             addParameter(contentBuilder, "incident_ampm", DateFormatUtils.format(report.incidentDate, "a").toLowerCase());
             addParameter(contentBuilder, "latitude", String.valueOf(report.location.getLat()));
             addParameter(contentBuilder, "longitude", String.valueOf(report.location.getLong()));
             addParameter(contentBuilder, "location_name", report.location.getName());
             addParameter(contentBuilder, "person_first", report.User.getFirstName());
             addParameter(contentBuilder, "person_last", report.User.getLastName());
             //addParameter(contentBuilder, "person_email", );
             addParameter(contentBuilder, "resp", "json");
 
             String content = formEncode(contentBuilder.toString());
 
             LOGGER.info("Sending: " + content);
 
             Builder builder = resource.type("application/x-www-form-urlencoded");
             Future<String> future = builder.post(String.class, content);
             String response = future.get();
             LOGGER.info(response);
         }
         catch (Exception e)
         {
             String errorMessage = generateErrorMessage();
             throw new RuntimeException(errorMessage, e);
         }
     }
 
     private String formEncode(String aData)
     {
         // Replace any white spaces with the '+' character
         String data = StringUtils.replace(aData, " ", "+");
 
         // Remove white spaces
         return data.trim();
     }
 
     private void addParameter(StringBuilder aContentBuilder, String name, String value)
     {
         aContentBuilder.append("&");
         aContentBuilder.append(name);
         aContentBuilder.append("=");
         aContentBuilder.append(value);
     }
 
     private String generateErrorMessage()
     {
         return String.format("Error sending request to %1$s", url);
     }
 }
