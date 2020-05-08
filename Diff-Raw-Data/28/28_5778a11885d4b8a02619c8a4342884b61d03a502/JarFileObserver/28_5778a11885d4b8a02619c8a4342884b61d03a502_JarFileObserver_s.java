 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.epita.mti.plic.opensource.controlibserversample.observer;
 
 import com.epita.mti.plic.opensource.controlibserversample.jarloader.JarClassLoader;
 import com.epita.mti.plic.opensource.controlibutility.beans.CLJarFile;
 import com.epita.mti.plic.opensource.controlibutility.serialization.CLSerializable;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Benoit "KIDdAe" Vasseur
  */
 public class JarFileObserver implements Observer
 {
 
   private JarClassLoader classLoader;
 
   @Override
   public void update(Observable o, Object arg)
   {
     if (((CLSerializable) arg).getType().equals("jarFile"))
     {
       String fileContent = ((CLJarFile) arg).getFile();
       String fileName = ((CLJarFile) arg).getFileName();
 
       try
       {
         FileOutputStream fos = new FileOutputStream(fileName);
         BufferedOutputStream bos = new BufferedOutputStream(fos);
 
         bos.write(Base64.decode(fileContent));
         bos.flush();
         fos.close();
         try {
           classLoader.addPlugins(fileName);
         } catch (Exception ex) {
           Logger.getLogger(JarFileObserver.class.getName()).log(Level.SEVERE, null, ex);
         }
       }
       catch (IOException ex)
       {
         Logger.getLogger(JarFileObserver.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
   }
 
   public JarClassLoader getClassLoader()
   {
     return classLoader;
   }
 
   public void setClassLoader(JarClassLoader classLoader)
   {
     this.classLoader = classLoader;
   }
 }
