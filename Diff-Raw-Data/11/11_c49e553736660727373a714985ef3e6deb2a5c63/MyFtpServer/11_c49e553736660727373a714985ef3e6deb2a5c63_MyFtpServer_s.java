 package Ftp;
 import java.io.*;
 import java.util.*;
 
 import org.apache.ftpserver.FtpServer;
 import org.apache.ftpserver.ftplet.*;
 import org.apache.ftpserver.usermanager.*;
 import org.apache.ftpserver.usermanager.impl.*;
 import org.apache.ftpserver.FtpServerFactory;
 import org.apache.ftpserver.listener.ListenerFactory;
 import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
 
 public class MyFtpServer 
 {
 private final FtpServer server;
 private UserFactory userFactory;
 private UserManager userManager;
private final String rootPath = "/home/jack1/eclipse_workspace/exp1/exp1server/ftp/room/";
private final String imgPath = "/home/jack1/eclipse_workspace/exp1/exp1server/ftp/img/";
 
 public MyFtpServer() throws Exception 
 {
    FtpServerFactory serverFactory = new FtpServerFactory();
         
    ListenerFactory factory = new ListenerFactory();     
    factory.setPort(2221);
 
    serverFactory.addListener("default", factory.createListener());
         
    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    userManagerFactory.setFile(new File("src/Ftp/myusers.properties"));
    userManagerFactory.setPasswordEncryptor(new PasswordEncryptor()
    {
       @Override
       public String encrypt(String password) 
       {
          return password;
       }
       @Override
       public boolean matches(String passwordToCheck, String storedPassword) 
       {
          return passwordToCheck.equals(storedPassword);
       }
    });
         
    List<Authority> authorities = new ArrayList<Authority>();
    authorities.add(new WritePermission());
    authorities.add(new ConcurrentLoginPermission(50, 5));
    //authorities.add(new TransferRatePermission(200*1024, 100*1024));
 
    userFactory = new UserFactory();
    userFactory.setAuthorities(authorities);
    userFactory.setMaxIdleTime(60);
    userManager = userManagerFactory.createUserManager();
    userFactory.setName("IMG");
    userFactory.setPassword("IMG");
    userFactory.setHomeDirectory(imgPath);
    userManager.save(userFactory.createUser());
 
    Map<String, Ftplet> map = new HashMap<String, Ftplet>();
    map.put("MyFtplet", new MyFtplet());
    serverFactory.setFtplets(map);
    serverFactory.setUserManager(userManager);
         
    server = serverFactory.createServer(); 
    System.out.println("Ftp Server ok");
 }
 
 public void start() throws FtpException
 {
    server.start();
 }
 
 public void stop() throws FtpException
 {
    server.stop();
 }
 
 public void addUser(String username) throws FtpException, IOException
 {
    if(!userManager.doesExist(username))
    {
       File dir = new File(rootPath+username);
       System.err.println("Create dir: "+rootPath+username+" "+(dir.mkdir()?"succ":"fail"));
       userFactory.setName(username);
       userFactory.setPassword(username);
       userFactory.setHomeDirectory(rootPath+username);
       userManager.save(userFactory.createUser());
    }
 }
 
 public void deleteUser(String username) throws FtpException, IOException
 {
    if(userManager.doesExist(username))
    {
       userManager.delete(username);
       File dir = new File(rootPath+username);
       File[] files = dir.listFiles();
       for(File f : files)
       {
          f.delete();
       }
       System.err.println("Delete dir: "+rootPath+username+" "+(dir.delete()?"succ":"fail"));
    }
 }
 
 public static void main(String[] args)
 {
    try
    {
       MyFtpServer sv = new MyFtpServer();
       sv.start();
       System.err.println("started");
       sv.deleteUser("test");
       sv.addUser("test");
    }
    catch(Exception e)
    {
    }
 }
 }
 
