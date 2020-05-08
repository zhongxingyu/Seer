 package net.qty.db.mysql;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class MySQLInstance {
 
     static Log logger = LogFactory.getLog(MySQLInstance.class);
 
     private MySQLManager manager;
     private File datadir;
     private String password;
     private int port;
 
     public MySQLInstance(MySQLManager manager, File datadir) {
         this.manager = manager;
         this.datadir = datadir;
         port = availablePort();
     }
 
     private int availablePort() {
         int port = 1024 + (int) (Math.random() * 10000);
         if (!isInUsedPort(port)) {
             return port;
         }
         return availablePort();
     }
 
     private boolean isInUsedPort(int uncheckPort) {
         try {
             ServerSocket server = new ServerSocket(uncheckPort);
             server.close();
         } catch (Exception e) {
             return true;
         }
         return false;
     }
 
     protected String[] getLaunchArgs() {
         return new String[] { 
                 String.format("--datadir=%s", datadir.getAbsoluteFile()),
                 String.format("--port=%d", getPort()),
                 String.format("--socket=%s", getSockFile())
                 };
     }
 
     private int getPort() {
         return port;
     }
 
     private String getSockFile() {
         return new File(datadir, "my.sock").getAbsolutePath();
     }
     
     protected void waitForDatabaseReady() {
         try {
             waitingDatabaseOnLine();
             setUpPassword();
             createNewUser("root", password);
         } catch (Exception e) {
             throw new RuntimeException("Waiting for database timed out.", e);
         }
     }
 
     private void setUpPassword() {
         password = generatePassword();
         String[] args = {String.format("--socket=%s", getSockFile()), 
                 "-u", "root", "password", password};
         
         manager.invoker.invoke(manager.location(MySQLManager.MYSQL_MYSQLADMIN), args);
     }
     
     public void createNewUser(String account, String passwd) throws Exception {
         runSqlScript(String.format("GRANT ALL PRIVILEGES ON *.* TO '%s'@'%%' IDENTIFIED BY '%s';", account, passwd));
     }
     
     public void createDatabase(String name) throws Exception {
        runSqlScript(String.format("CREATE DATABASE %s DEFAULT CHARACTER SET utf8;", name));
     }
     
     private String generatePassword() {
 
         StringBuilder sb = new StringBuilder();
         char[] s = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
 
         int sum = 0;
         for (int i = 0; i < 8; i++) {
             sum += ((int) (Math.random() * 10));
             sb.append(s[sum % s.length]);
         }
 
         return sb.toString();
     }
 
     protected void waitingDatabaseOnLine() throws Exception {
         int count = 15;
         while (count-- > 0) {
             if (!isDatabaseNetworkOn()) {
                 Thread.sleep(1000);
             } else {
                 break;
             }
         }
         if (!isDatabaseNetworkOn()) {
             throw new RuntimeException("Can not connect to database.");
         }
     }
 
     protected boolean isDatabaseNetworkOn() throws IOException {
         try {
             Socket socket = new Socket();
             socket.connect(new InetSocketAddress(getPort()));
             socket.close();
             return true;
         } catch (Exception e) {
             return false;
         }
     }
     
     public void shutdown() {
         manager.invoker.invoke(manager.location(MySQLManager.MYSQL_MYSQLADMIN),
                 String.format("--socket=%s", getSockFile()),
                 "-p" + password,
                 "-u", "root", "shutdown");
     }
 
     public String getBaseConnectionUrl() {
         return String.format("jdbc:mysql://127.0.0.1:%d", getPort());
     }
     
     public void runSqlScript(String sql) throws Exception {
         String sqlPath = saveInTempFile(sql);
         String cmd = String.format("%s -uroot -p%s --socket=%s < %s",
                 manager.location(MySQLManager.MYSQL_MYSQL_CLIENT), password, getSockFile(), sqlPath);
         manager.invoker.delegateInvoke(cmd);
     }
 
     private String saveInTempFile(String sql) throws Exception {
         File file = File.createTempFile(MySQLInstance.class.getSimpleName(), ".tmp");
         FileWriter fw = new FileWriter(file);
         fw.write(sql);
         fw.close();
         return file.getAbsolutePath();
     }
 
     public String getBaseConnectionUrlWithRoot() {
         return String.format("%s?username=root&password=%s", getBaseConnectionUrl(), password);
     }
 
     public String getPassword() {
         return password;
     }
 
 }
