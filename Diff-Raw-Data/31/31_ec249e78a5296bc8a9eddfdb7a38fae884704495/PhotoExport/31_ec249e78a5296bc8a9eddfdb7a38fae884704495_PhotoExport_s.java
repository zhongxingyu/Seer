 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package netcracker.service;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import netcracker.dao.DAOConstants;
 import netcracker.dao.DAOFactory;
 
 /**
  *
  * @author lastride
  */
 public class PhotoExport {
 
     /**
      * Exports photo to .zip archive
      *
      * @return
      */
    public static boolean exportPhoto() {
         ZipOutputStream zip = null;
         ZipEntry entry = null;
         PreparedStatement stmt = null;
         PreparedStatement stmtCount = null;
         ResultSet result = null;
         ResultSet resultCount = null;
         Connection conn = DAOFactory.createConnection();
         StringBuffer sbPhoto = null;
         try {
             sbPhoto = new StringBuffer();
             sbPhoto.append("SELECT photo, last_name, first_name, email1 FROM ");
             sbPhoto.append(DAOConstants.StudentsTableName);
            zip = new ZipOutputStream(new FileOutputStream("/home/lastride/photos.zip"));
             try {
                 stmt = conn.prepareStatement(sbPhoto.toString());
                 result = stmt.executeQuery();
                 while (result.next()) {
                     StringBuilder sbStudentsCount = new StringBuilder();
                     sbStudentsCount.append("SELECT count(id_student) FROM ");
                     sbStudentsCount.append(DAOConstants.StudentsTableName);
                     sbStudentsCount.append(" WHERE first_name like '%").append(result.getString("first_name")).append("%'");
                     sbStudentsCount.append(" AND last_name like '%").append(result.getString("last_name")).append("%'");
                     stmtCount = conn.prepareStatement(sbStudentsCount.toString());
                     resultCount = stmtCount.executeQuery();
                     while (resultCount.next()) {
                         if (resultCount.getInt(1) > 1) {
                             entry = new ZipEntry(result.getString("last_name")
                                     + "_" + result.getString("first_name") + "_"
                                     + result.getString("email1") + ".jpeg");
                         } else {
                             entry = new ZipEntry(result.getString("last_name")
                                     + "_" + result.getString("first_name") + ".jpeg");
                         }
                     }
                     zip.putNextEntry(entry);
                     zip.write((byte[]) result.getObject("photo"));
                 }
             } catch (IOException ex) {
                 Logger.getLogger(PhotoExport.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SQLException ex) {
                 Logger.getLogger(PhotoExport.class.getName()).log(Level.SEVERE, null, ex);
             } finally {
                 DAOFactory.closeConnection(conn);
                 DAOFactory.closePreparedStatement(stmt);
                 DAOFactory.closePreparedStatement(stmtCount);
             }
         } catch (FileNotFoundException ex) {
             Logger.getLogger(PhotoExport.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 zip.close();
             } catch (IOException ex) {
                 Logger.getLogger(PhotoExport.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
        return true;
     }
 }
