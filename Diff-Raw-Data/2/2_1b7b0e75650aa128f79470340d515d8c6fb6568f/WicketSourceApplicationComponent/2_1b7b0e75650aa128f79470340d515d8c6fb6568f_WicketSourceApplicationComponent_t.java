 /*
  *  Copyright 2012 George Armhold
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.armhold.wicketsource;
 
 import com.intellij.ide.util.PropertiesComponent;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.components.ApplicationComponent;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.options.Configurable;
 import com.intellij.openapi.options.ConfigurationException;
 import com.intellij.openapi.ui.Messages;
 import org.jetbrains.annotations.Nls;
 import org.jetbrains.annotations.NotNull;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 
 /**
  * main entrypoint for the plugin
  *
  * NB: This code was heavily inspired by the RemoteCall plugin by Alexander Zolotov
  * @see https://github.com/zolotov/RemoteCall
  */
 public class WicketSourceApplicationComponent implements ApplicationComponent, Configurable
 {
     private static final Logger log = Logger.getInstance(WicketSourceApplicationComponent.class);
     public static final String PORT_KEY = "com.armhold.wicketsource.port";
     public static final String PASSWORD_KEY = "com.armhold.wicketsource.password";
     public static final int DEFAULT_PORT = 9123;
 
     private static ServerSocket serverSocket;
     private static Thread listener;
 
     private JComponent myComponent;
     private JPanel myPanel;
     private JTextField portNumberField;
     private JTextField passwordField;
 
     public WicketSourceApplicationComponent()
     {
         log.info("starting");
     }
 
     public void initComponent()
     {
         initListenerAndSocket();
     }
     
     private void initListenerAndSocket()
     {
         final int port = getPort();
 
         try
         {
             serverSocket = new ServerSocket();
             serverSocket.bind(new InetSocketAddress("localhost", port));
             log.info("WicketSource plugin listening on port: " + port);
         }
         catch (IOException e)
         {
             ApplicationManager.getApplication().invokeLater(new Runnable()
             {
                 public void run()
                 {
                     Messages.showMessageDialog("WicketSource plugin: can't bind to port: " + port, "WicketSource Plugin Error", Messages.getErrorIcon());
                 }
             });
 
             return;
         }
 
         Listener messageNotifier = new Listener(serverSocket);
         listener = new Thread(messageNotifier);
         listener.start();
     }
     
 
     private void disposeListenerAndSocket()
     {
         try
         {
             if (serverSocket != null)
             {
                 serverSocket.close();
             }
 
             if (listener != null)
             {
                 listener.interrupt();
             }
         }
         catch (IOException e)
         {
             throw new RuntimeException(e);
         }
     }
     
     
     public void disposeComponent()
     {
         disposeListenerAndSocket();
     }
 
     @NotNull
     public String getComponentName()
     {
         return "WicketSource";
     }
 
     @Nls
     @Override
     public String getDisplayName()
     {
         return "WicketSource";
     }
 
     @Override
     public Icon getIcon()
     {
         return null;
     }
 
     @Override
     public String getHelpTopic()
     {
        return null;
     }
 
     @Override
     public JComponent createComponent()
     {
         // Define a set of possible values for combo boxes.
         PropertiesComponent.getInstance().getValue("", "");
         
         portNumberField.setText(Integer.toString(getPort()));
         passwordField.setText(getPassword());
 
         myComponent = myPanel;
         return myComponent;
     }
     
     public static int getPort()
     {
         String result = PropertiesComponent.getInstance().getValue(PORT_KEY, Integer.toString(DEFAULT_PORT));
         return Integer.parseInt(result);
     }
 
     public static String getPassword()
     {
         return PropertiesComponent.getInstance().getValue(PASSWORD_KEY, "");
     }
 
     @Override
     public boolean isModified()
     {
         return true;
     }
 
     @Override
     public void apply() throws ConfigurationException
     {
         String port = portNumberField.getText();
         String password = passwordField.getText();
 
         PropertiesComponent props = PropertiesComponent.getInstance();
         props.setValue(PORT_KEY, port);
         props.setValue(PASSWORD_KEY, password);
 
         disposeListenerAndSocket();
         initListenerAndSocket();
     }
 
     @Override
     public void reset()
     {
 
     }
 
     @Override
     public void disposeUIResources()
     {
 
     }
 }
