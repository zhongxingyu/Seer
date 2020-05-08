 package info.huggard.charlie.ews;
 
 import info.huggard.charlie.ews.Configuration.Section;
 import info.huggard.charlie.ews.Configuration.Values;
 import info.huggard.charlie.ews.config.PropertiesFileConfig;
 import info.huggard.charlie.ews.util.EWSUtil;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import microsoft.exchange.webservices.data.ExchangeService;
 import microsoft.exchange.webservices.data.ExchangeVersion;
import microsoft.exchange.webservices.data.TrustingWebCredentials;
 
 /**
  * Main method for invoking email cleanup methods.
  * @author Charlie Huggard
  */
 @SuppressWarnings("nls")
 public class EmailCleanup {
 
     private static String getFileName(final String... args) {
         return (args.length == 1) ? args[0] : "ews.properties";
     }
 
     public static void main(final String... args) throws Exception {
         final Configuration config = new PropertiesFileConfig(getFileName(args));
         TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // Solve a bug handling date time stamps.
         final ExchangeService service = getService(config.getConnectionSettings());
         final EWSUtil serviceUtil = new EWSUtil(service);
         final List<Section> sections = config.getCleanupMethodSettings();
         for (final CleanupMethod method : getCleanupMethods(sections)) {
             System.out.printf("%s - Executing Class: %s\n", new Date(), method.getClass().getCanonicalName());
             method.execute(serviceUtil);
         }
 
     }
 
     private static ExchangeService getService(final Values conn) throws Exception {
         final ExchangeVersion version = ExchangeVersion.valueOf(conn.getValue("version"));
         final ExchangeService service = new ExchangeService(version);
 
         final String username = conn.getValue("user");
         final String password = conn.getValue("password");
         final String domain = conn.getValue("domain");
         service.setCredentials(new WebCredentials(username, password, domain));
 
         final String uri = conn.getValue("uri");
         if (uri == null) {
             service.autodiscoverUrl(conn.getValue("mailbox"));
         } else {
             service.setUrl(URI.create(uri));
         }
 
         return service;
     }
 
     private static List<CleanupMethod> getCleanupMethods(final List<Section> configs) throws Exception {
         final List<CleanupMethod> toReturn = new ArrayList<CleanupMethod>(configs.size());
         for (final Section config : configs) {
             final CleanupMethod method = Class.forName(config.getValue("class")).asSubclass(CleanupMethod.class)
                     .newInstance();
             method.setConfig(config);
             toReturn.add(method);
         }
         return toReturn;
     }
 }
