 package davmail;
 
 import davmail.caldav.CaldavServer;
 import davmail.exception.DavMailException;
 import davmail.exchange.ExchangeSessionFactory;
 import davmail.http.DavGatewayHttpClientFacade;
 import davmail.http.DavGatewaySSLProtocolSocketFactory;
 import davmail.imap.ImapServer;
 import davmail.ldap.LdapServer;
 import davmail.pop.PopServer;
 import davmail.smtp.SmtpServer;
 import davmail.ui.tray.DavGatewayTray;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 /**
  * DavGateway main class
  */
 public class DavGateway {
     private static final String HTTP_DAVMAIL_SOURCEFORGE_NET_VERSION_TXT = "http://davmail.sourceforge.net/version.txt";
 
    private static boolean stopped;

     private DavGateway() {
     }
 
     private static final ArrayList<AbstractServer> serverList = new ArrayList<AbstractServer>();
 
     /**
      * Start the gateway, listen on spcified smtp and pop3 ports
      *
      * @param args command line parameter config file path
      */
     public static void main(String[] args) {
 
         if (args.length >= 1) {
             Settings.setConfigFilePath(args[0]);
         }
 
         Settings.load();
         DavGatewayTray.init();
 
         start();

        // server mode: all threads are daemon threads, do not let main stop
        if (Settings.getBooleanProperty("davmail.server")) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    DavGatewayTray.debug(new BundleMessage("LOG_GATEWAY_INTERRUPTED"));
                    DavGateway.stop();
                    DavGatewayTray.debug(new BundleMessage("LOG_GATEWAY_STOP"));
                    stopped = true;
                }
            });

            try {
                while (!stopped) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                DavGatewayTray.debug(new BundleMessage("LOG_GATEWAY_INTERRUPTED"));
                stop();
                DavGatewayTray.debug(new BundleMessage("LOG_GATEWAY_STOP"));
            }

        }
     }
 
     public static void start() {
         // register custom SSL Socket factory
         DavGatewaySSLProtocolSocketFactory.register();
 
         // prepare HTTP connection pool
         DavGatewayHttpClientFacade.start();
 
         serverList.clear();
 
         int smtpPort = Settings.getIntProperty("davmail.smtpPort");
         if (smtpPort != 0) {
             serverList.add(new SmtpServer(smtpPort));
         }
         int popPort = Settings.getIntProperty("davmail.popPort");
         if (popPort != 0) {
             serverList.add(new PopServer(popPort));
         }
         int imapPort = Settings.getIntProperty("davmail.imapPort");
         if (imapPort != 0) {
             serverList.add(new ImapServer(imapPort));
         }
         int caldavPort = Settings.getIntProperty("davmail.caldavPort");
         if (caldavPort != 0) {
             serverList.add(new CaldavServer(caldavPort));
         }
         int ldapPort = Settings.getIntProperty("davmail.ldapPort");
         if (ldapPort != 0) {
             serverList.add(new LdapServer(ldapPort));
         }
 
         BundleMessage.BundleMessageList messages = new BundleMessage.BundleMessageList();
         BundleMessage.BundleMessageList errorMessages = new BundleMessage.BundleMessageList();
         for (AbstractServer server : serverList) {
             try {
                 server.bind();
                 server.start();
                 messages.add(new BundleMessage("LOG_PROTOCOL_PORT", server.getProtocolName(), server.getPort()));
             } catch (DavMailException e) {
                 errorMessages.add(e.getBundleMessage());
             } catch (IOException e) {
                 errorMessages.add(new BundleMessage("LOG_SOCKET_BIND_FAILED", server.getProtocolName(), server.getPort()));
             }
         }
 
         String currentVersion = getCurrentVersion();
         DavGatewayTray.info(new BundleMessage("LOG_DAVMAIL_GATEWAY_LISTENING",
                 currentVersion == null ? "" : currentVersion, messages));
         if (!errorMessages.isEmpty()) {
             DavGatewayTray.error(new BundleMessage("LOG_MESSAGE", errorMessages));
         }
 
         // check for new version
         String releasedVersion = getReleasedVersion();
         if (currentVersion != null && releasedVersion != null && currentVersion.compareTo(releasedVersion) < 0) {
             DavGatewayTray.info(new BundleMessage("LOG_NEW_VERSION_AVAILABLE", releasedVersion));
         }
 
     }
 
     public static void stop() {
         for (AbstractServer server : serverList) {
             server.close();
             try {
                 server.join();
             } catch (InterruptedException e) {
                 DavGatewayTray.warn(new BundleMessage("LOG_EXCEPTION_WAITING_SERVER_THREAD_DIE"), e);
             }
         }
         // close pooled connections
         DavGatewayHttpClientFacade.stop();
         // clear session cache
         ExchangeSessionFactory.reset();
     }
 
     public static String getCurrentVersion() {
         Package davmailPackage = DavGateway.class.getPackage();
         return davmailPackage.getImplementationVersion();
     }
 
     public static String getReleasedVersion() {
         String version = null;
         if (!Settings.getBooleanProperty("davmail.disableUpdateCheck")) {
             BufferedReader versionReader = null;
             HttpClient httpClient = DavGatewayHttpClientFacade.getInstance();
             GetMethod getMethod = new GetMethod(HTTP_DAVMAIL_SOURCEFORGE_NET_VERSION_TXT);
             try {
                 httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(1000);
                 int status = httpClient.executeMethod(getMethod);
                 if (status == HttpStatus.SC_OK) {
                     versionReader = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));
                     version = versionReader.readLine();
                 }
             } catch (IOException e) {
                 DavGatewayTray.debug(new BundleMessage("LOG_UNABLE_TO_GET_RELEASED_VERSION"));
             } finally {
                 if (versionReader != null) {
                     try {
                         versionReader.close();
                     } catch (IOException e) {
                         // ignore
                     }
                 }
                 getMethod.releaseConnection();
             }
         }
         return version;
     }
 }
