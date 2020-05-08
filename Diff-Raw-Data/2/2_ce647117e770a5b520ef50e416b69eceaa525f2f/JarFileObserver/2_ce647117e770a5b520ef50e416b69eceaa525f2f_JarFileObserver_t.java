 package com.epita.mti.plic.opensource.controlibserversample.observer;
 
 import com.epita.mti.plic.opensource.controlibserver.jarloader.JarClassLoader;
 import com.epita.mti.plic.opensource.controlibutility.beans.CLJarFile;
 import com.epita.mti.plic.opensource.controlibutility.plugins.CLObserver;
 import com.epita.mti.plic.opensource.controlibutility.serialization.CLSerializable;
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.iharder.Base64;
 
 /**
  *
  * @author Benoit "KIDdAe" Vasseur
  * This a the observer used to handle plugins reception.
  * The jar content is taken from the stream and put in a jar file in a local
  * directory.
  */
 public class JarFileObserver implements CLObserver
 {
   private JarClassLoader classLoader;
   private int index;
   
   @Override
   public void update(Observable o, Object arg)
   {
     if (((CLSerializable) arg).getType().equals("jarFile"))
     {
       String fileContent = ((CLJarFile) arg).getFile();
       String fileName = ((CLJarFile) arg).getFileName();
 
       try
       {
         //System.out.println("Been there");
         FileOutputStream tmpFos = new FileOutputStream("plugins/tmp/" + fileName);
         BufferedOutputStream tmpBos = new BufferedOutputStream(tmpFos);
         tmpBos.write(Base64.decode(fileContent));
         tmpBos.flush();
         tmpBos.close();
         
         
         try {
          if (classLoader.testPlugins("plugins/tmp/" + fileName, index))
           {
             FileOutputStream fos = new FileOutputStream("plugins/" + fileName);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
 
             bos.write(Base64.decode(fileContent));
             bos.flush();
             fos.close();
             classLoader.addPlugins("plugins/" + fileName, index);
           }
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
 
   public int getIndex()
   {
     return index;
   }
 
   public void setIndex(int index)
   {
     this.index = index;
   }
 
     @Override
     public String getVersion() {
         return "1.0";
     }
   
 }
 
 
