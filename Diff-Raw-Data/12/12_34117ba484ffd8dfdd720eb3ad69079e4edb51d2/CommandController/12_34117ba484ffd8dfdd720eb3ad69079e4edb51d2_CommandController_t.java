 package org.makumba.parade.controller;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.managers.CVSManager;
 import org.makumba.parade.model.managers.FileManager;
 import org.makumba.parade.view.managers.CommandViewManager;
 import org.makumba.parade.view.managers.FileDisplay;
 
 
 /**
  * 
  * @author manu
  * @version $id
  */
 
 public class CommandController {
 
     public static Object[] onNewFile(String context, String[] params) {
 
         Object[] result = fileHandler(context, params, "newFile");
         return result;
     }
 
     public static Object[] onNewDir(String context, String[] params) {
 
         Object[] result = fileHandler(context, params, "newDir");
         return result;
     }
 
     public static Object[] onDeleteFile(String context, String[] params) {
         Object[] result = fileHandler(context, params, "deleteFile");
 
         return result;
     }
 
     private static Object[] fileHandler(String context, String[] params, String action) {
         Object[] obj = new Object[2];
         String result = new String();
         String filename = params[0];
         String absolutePath = params[1];
         
         FileManager fileMgr = new FileManager();
 
         Session s = InitServlet.getSessionFactory().openSession();
         Transaction tx = s.beginTransaction();
 
         Parade p = (Parade) s.get(Parade.class, new Long(1));
 
         Row row = (Row) p.getRows().get(context);
         if (row == null) {
             tx.commit();
             s.close();
             return res("Unknown context " + context, false);
         }
         
         String filepath = absolutePath + java.io.File.separator + filename;
         String relativepath = absolutePath.substring(row.getRowpath().length());
         
         if (action.equals("newFile"))
             result = fileMgr.newFile(row, absolutePath, filename);
         else if (action.equals("newDir"))
             result = fileMgr.newDir(row, absolutePath, filename);
         else if (action.equals("deleteFile"))
             result = fileMgr.deleteFile(row, filepath);
         
         //updates the caches
         //TODO the same for the other caches
         CVSManager.updateCvsCache(context, absolutePath);
  
         tx.commit();
         s.close();
 
         boolean success = result.startsWith("OK");
         if (success) {
             if (action.equals("newFile"))
                return res(FileDisplay.creationFileOK(row.getRowname(), relativepath, filename), success);
             if (action.equals("newDir"))
                 return res(FileDisplay.creationDirOK(filename), success);
             if (action.equals("deleteFile"))
                 return res(FileDisplay.deletionFileOK(result.substring(result.indexOf("#") + 1)), success);
         } else {
             if (action.equals("newFile"))
                 return res(result, success);
             if (action.equals("newDir"))
                 return res(result, success);
             if (action.equals("deleteFile"))
                 return res(result, success);
         }
 
         return obj;
     }
 
     private static Object[] res(String message, boolean status) {
         Object obj[] = new Object[2];
         obj[0] = message;
         obj[1] = new Boolean(status);
         return obj;
     }
 
     public static Object[] uploadFile(String context, String path, String fileName, String contentType, int fileSize, byte[] fileData) {
         
         boolean success = true;
         String result = new String();
         
         String saveFilePath = Parade.constructAbsolutePath(context, path) + File.separator + fileName;
 
         FileOutputStream fileOut;
         try {
             fileOut = new FileOutputStream(saveFilePath);
             fileOut.write(fileData);
             fileOut.flush();
             fileOut.close();
         } catch (FileNotFoundException e) {
             success = false;
             result = ("Error writing file: " + e);
 
         } catch (IOException e) {
             success = false;
             result = ("Error writing file: " + e);
         }
         
         if (success) {
 
             // updating the cache
             
             Session s = InitServlet.getSessionFactory().openSession();
             Transaction tx = s.beginTransaction();
 
             Parade p = (Parade) s.get(Parade.class, new Long(1));
             FileManager fileMgr = new FileManager();
             fileMgr.uploadFile(p, saveFilePath, context);
 
             tx.commit();
             s.close();
             
             // generating result view
             result = CommandViewManager.getUploadResponseView(context, path, fileName, contentType, fileSize, saveFilePath);
         }
         
         Object obj[] = new Object[2];
         obj[0] = result;
         obj[1] = new Boolean(success);
         
         return obj;
     }
 
 }
