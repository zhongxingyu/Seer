 public class AdminRequestListenerThread extends Thread {
   public AdminRequestListenerThread() throws IOException {
     // ...
     HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
     // ...
     reqistry.register("/gui/", new IndexPage());
     reqistry.register("/gui/backup/", new BackupIndexPageRH());
     reqistry.register("/gui/config/", new ConfigIndexRH());
     // ...
   }
 }
