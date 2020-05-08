 /*
 Andrew Darwin
 CSC 420: Graphical User Interfaces
 Fall 2012
 */
 
 package csc420.hw1;
 
 import javax.swing.SwingWorker;
 import java.io.File;
import java.io.FileFilter;
//import javax.swing.filechooser.FileFilter;
 
 public class Worker extends SwingWorker<File[], Integer>
 {
   File[] countryFiles;
   public Worker(File[] fileList)
   {
     super();
     countryFiles = fileList;
   }
   @Override
   protected File[] doInBackground() throws Exception
   {
     //Load images
     System.out.println("Begin loading images in worker thread.");
     String path = "csc420/hw1/img";
     File directory = new File(path);
     if (directory != null)
     {
       File[] fileList = directory.listFiles(new FileFilter()
       {
         public boolean accept(File pathname)
         {
           if (pathname.getName().endsWith(".gif"))
           {
             return true;
           }
           else
           {
             return false;
           }
         }
         public String getDescription()
         {
           return "SomeString";
         }
       });
       if (fileList != null)
       {
         countryFiles = new File[fileList.length];
         Object[] values = new Object[fileList.length];
         for (int i = 0; i < fileList.length; i++)
         {
           countryFiles[i] = fileList[i];
           String name = fileList[i].getName();
           int extensionIndex = name.lastIndexOf('.');
           name = name.substring(0, extensionIndex);
           values[i] = name;
           //list.addItem(name);
         }
         //list.setListData(values);
       }
     }
     else
     {
       throw new Exception("null directory");
     }
     return new File[7];
   }
 
   @Override
   public void done()
   {
     System.out.println("All done. :-)");
   }
 }
