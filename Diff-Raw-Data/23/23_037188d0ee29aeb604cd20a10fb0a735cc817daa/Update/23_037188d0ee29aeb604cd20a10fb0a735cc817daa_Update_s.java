 package gutenberg.collect;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Locale;
 
 public class Update extends Task {
 
     public Update() {
         this.filter = DETECTED;
     }
     
     @Override
     protected void init() throws Exception {
         this.todaysFolder = getTodaysFolder();
     }
 
     @Override
     protected void execute(Path file) throws Exception {
         
         if (!Files.exists(todaysFolder)) 
             Files.createDirectory(todaysFolder);
         
         Path target = null;
        String scanId = null, scanType = null;
        String name = file.getFileName().toString().split("\\.")[0];
        if (name.startsWith(GRADED_RESPONSE_ID)) {
            scanId = name.split("_")[1];
            target = todaysFolder.resolve(name.split("_")[2]);
             scanType = "plainpaper";
         } else {
            scanId = name;
             target = todaysFolder.resolve(scanId);
             scanType = "worksheet";
         }
                 
         if (Files.exists(target))
             Files.delete(file);
        else if (updateScanId(bankroot.resolve(LOCKER).relativize(target).toString(), scanType))
             Files.move(file, target);
         else
             Files.delete(file);
     }        
     
     @Override
     protected void cleanup() throws Exception {
         if (conn != null) conn.disconnect();
     }
 
    private boolean updateScanId(String scanId, String scanType) throws Exception {        
         String charset = Charset.defaultCharset().name();
         
         String hostport = System.getProperty("user.name").equals("gutenberg")?
             "www.gradians.com": "localhost:3000";        
         URL updateScan = new URL(String.format("http://%s/update_scan_id", hostport));
        String params = String.format("id=%s&type=%s",
            URLEncoder.encode(scanId, charset), scanType);
         conn = (HttpURLConnection)updateScan.openConnection();
         conn.setDoOutput(true); // Triggers HTTP POST
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
         conn.setRequestProperty("Cache-Control", "no-cache");
         conn.getOutputStream().write(params.getBytes(charset));
         conn.getOutputStream().close();
 
         if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
             byte[] goodReturn = "{\"status\":\"ok\"}".getBytes();
             byte[] actualReturn = new byte[goodReturn.length];            
             while (conn.getInputStream().read(actualReturn) != -1) { }            
             conn.getInputStream().close();            
             return Arrays.equals(goodReturn, actualReturn);
         } else {
             return false;    
         }        
     }    
 
     private Path getTodaysFolder() {
         Calendar rightNow = Calendar.getInstance();
         return bankroot.resolve(LOCKER).resolve(String.format("%s.%s.%s", 
                 rightNow.get(Calendar.DAY_OF_MONTH),
                 rightNow.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH),
                 rightNow.get(Calendar.YEAR)));
     }
     
     private HttpURLConnection conn;
     private Path todaysFolder;
 
 }
