 package com.example.prototype.applet;
 
 import com.example.prototype.api.widget.WidgetFactory;
 
 import javax.swing.*;
 import java.applet.Applet;
 import java.applet.AppletContext;
 import java.applet.AppletStub;
 import java.awt.*;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.Properties;
 
 public class AppletHostFactory implements WidgetFactory {
     @Override
     public JComponent instantiate(Properties prp) {
         final AppletConfig appletConfig = new AppletConfig(prp);
         final JPanel panel = new JPanel(new BorderLayout());
         panel.setPreferredSize(appletConfig.dimension());
         final StringBuffer sb = new StringBuffer();
 
        ClassLoader classLoader;
         try {
             classLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction<Object>() {
                 @Override
                 public Object run() {
                     try {
                         final URL urls[] = {new URL(appletConfig.url())};
                         return new URLClassLoader(urls, this.getClass().getClassLoader());
                     } catch (Exception e) {
                         sb.append(e.getMessage());
                     }
                     return null;
                 }
             });
             Class<Applet> appletClass = (Class<Applet>) classLoader.loadClass(appletConfig.code());
            Applet applet = appletClass.newInstance();
             AppletContext appletContext = new BasicAppletContext();
             AppletStub appletStub = new BasicAppletStub(appletContext, prp);
             applet.setStub(appletStub);
             applet.setSize(appletConfig.dimension());
             applet.init();
             applet.start();
             panel.add(applet);
         } catch (Throwable tt) {
             JTextArea label = new JTextArea("Applet\n" +
                     appletConfig.url() + appletConfig.code() +
                     "\nfailed to load:\n" + sb.toString());
             label.setEditable(false);
             panel.add(label);
         }
 
         return panel;
     }
 }
