 package com.epita.mti.plic.opensource.controlibserversample;
 
 import com.epita.mti.plic.opensource.controlibserver.connection.ConnectionManager;
 import com.epita.mti.plic.opensource.controlibserversample.jarloader.JarClassLoader;
 import com.epita.mti.plic.opensource.controlibserversample.observer.JarFileObserver;
 import com.epita.mti.plic.opensource.controlibserversample.view.ServerView;
 import com.epita.mti.plic.opensource.controlibutility.serialization.ObjectReceiver;
 import com.epita.mti.plic.opensource.controlibutility.serialization.ObjectSender;
 import java.awt.AWTException;
 import java.awt.Frame;
 import java.awt.SystemTray;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Observer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Julien "Roulyo" Fraisse
  */
 public class ServerSample
 {
 
   public final static int IN_PORT = 4200;
   public final static int OUT_PORT = 4201;
   private static ConnectionManager connectionManager = new ConnectionManager();
   private static ServerView serverView = new ServerView();
   private static Frame qrcodeView = null;
   private static JarClassLoader classLoader = new JarClassLoader();
   private static ObjectReceiver receiver;
   private static ObjectSender sender;
 
   public static void main(String[] args)
   {
     try
     {
       final SystemTray tray = SystemTray.getSystemTray();
       connectionManager.openConnection(IN_PORT, OUT_PORT);
 
       tray.add(serverView.getTrayIcon());
 
       while (true)
       {
         try
         {
           classLoader.initializeLoader();
         }
         catch (Exception ex)
         {
           Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
         }
         JarFileObserver jarFileObserver = new JarFileObserver();
        System.out.println("WAITING CONNECTION");
         Socket inputSocket = connectionManager.getInputSocket().accept();
        //Socket outputSocket = connectionManager.getOutputSocket().accept();
        System.out.println("CONNECTED");
        
         closeQrcodeView();
        
         jarFileObserver.setClassLoader(classLoader);
         receiver = new ObjectReceiver(inputSocket, jarFileObserver);
        //sender = new ObjectSender(outputSocket.getOutputStream());
         new Thread(receiver).start();
       }
     }
     catch (IOException ex)
     {
       Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
     }
     catch (AWTException ex)
     {
       Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
     }
   }
 
   public static ConnectionManager getConnectionManager()
   {
     return connectionManager;
   }
 
   public static void setQrcodeView(Frame f)
   {
     qrcodeView = f;
   }
 
   public static void closeQrcodeView()
   {
     if (qrcodeView != null)
     {
       qrcodeView.dispose();
       qrcodeView = null;
     }
   }
 
   public static void updatePlugins()
   {
     ArrayList<Class<?>> plugins = classLoader.getPlugins();
     for (Class<?> plugin : plugins)
     {
       try
       {
         Constructor<?> constructor = plugin.getConstructor();
         Observer observer = (Observer) constructor.newInstance();
         receiver.addObserver(observer);
       }
       catch (InstantiationException ex)
       {
         Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
       }
       catch (IllegalAccessException ex)
       {
         Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
       }
       catch (IllegalArgumentException ex)
       {
         Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
       }
       catch (InvocationTargetException ex)
       {
         Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
       }
       catch (NoSuchMethodException ex)
       {
         Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
       }
       catch (SecurityException ex)
       {
         Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     classLoader.getPlugins().clear();
   }
 }
