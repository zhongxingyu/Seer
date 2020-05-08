 package org.motechproject.ananya.referencedata.web.diagnostic;
 
 import org.motechproject.diagnostics.annotation.Diagnostic;
 import org.motechproject.diagnostics.diagnostics.DiagnosticLog;
 import org.motechproject.diagnostics.response.DiagnosticsResult;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Properties;
 
 @Component
 public class HttpConnectionDiagnostic {
 
     private Properties properties;
 
     @Autowired(required = false)
     public void setProperties(@Qualifier("referencedataProperties") Properties properties) {
         this.properties = properties;
     }
 
     @Diagnostic(name = "HttpConnection")
     public DiagnosticsResult performDiagnosis() {
         HttpURLConnection connection;
        DiagnosticLog diagnosticLog = new DiagnosticLog();
         diagnosticLog.add("Opening http session with ananya");
 
         if (properties == null) return null;
         if (properties.getProperty("ananya.bbc.url") == null) {
             diagnosticLog.add("Property ananya.bbc.url does not exist.");
             return new DiagnosticsResult(false, diagnosticLog.toString());
         }
         boolean httpConnectionStatus = false;
         try {
             connection = getConnection();
             connection.connect();
             int responseCode = connection.getResponseCode();
             String responseMessage = connection.getResponseMessage();
             if (responseCode == HttpURLConnection.HTTP_OK) {
                 httpConnectionStatus = true;
                 diagnosticLog.add(responseMessage);
             } else
                 diagnosticLog.add("Status: " + responseCode + "\nMessage :" + responseMessage);
 
         } catch (Exception e) {
             diagnosticLog.add("Connection Failed. " + e.getMessage());
         }
         return new DiagnosticsResult(httpConnectionStatus, diagnosticLog.toString());
     }
 
     protected HttpURLConnection getConnection() throws IOException {
         HttpURLConnection connection;
         URL url = new URL(properties.getProperty("ananya.bbc.url"));
         connection = (HttpURLConnection) url.openConnection();
         return connection;
     }
 }
