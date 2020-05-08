 package edu.mayo.phenoportal.server.upload;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import edu.mayo.phenoportal.client.upload.ClientUploadItems;
 import edu.mayo.phenoportal.client.upload.FileService;
 import edu.mayo.phenoportal.server.database.DBConnection;
 import edu.mayo.phenoportal.shared.AlgorithmType;
 import edu.mayo.phenoportal.utils.SQLStatements;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class FileServiceImpl extends RemoteServiceServlet implements FileService {
 
    private static final long serialVersionUID = 3L;
     static final Logger lgr = Logger.getLogger(FileServiceImpl.class.getName());
 
     @Override
     public ClientUploadItems retrieveUploadMetadata(int id) {
 
        Connection conn = DBConnection.getDBConnection();
         PreparedStatement st = null;
         ResultSet rs = null;
         ClientUploadItems clientUploadItems = new ClientUploadItems();
 
         if (conn != null) {
             try {
 
                 st = conn.prepareStatement(SQLStatements.selectUploadStatement(id));
 
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     clientUploadItems.setId(rs.getInt("id"));
                     clientUploadItems.setParentId(rs.getString("parentId"));
                     clientUploadItems.setName(rs.getString("name"));
                     clientUploadItems.setUser(rs.getString("user"));
                     clientUploadItems.setVersion(rs.getString("version"));
                     clientUploadItems.setDescription(rs.getString("description"));
                     clientUploadItems.setInstitution(rs.getString("institution"));
                     clientUploadItems.setCreateDate(rs.getString("creationDate"));
                     clientUploadItems.setComment(rs.getString("comments"));
                     clientUploadItems.setStatus(rs.getString("status"));
                     clientUploadItems.setAssocLink(rs.getString("assocLink"));
                     clientUploadItems.setAssocName(rs.getString("assocName"));
                     clientUploadItems.setZipFile(rs.getString("zipFile"));
                     clientUploadItems.setHtmlFile(rs.getString("htmlFile"));
                     clientUploadItems.setXmlFile(rs.getString("xmlFile"));
                     clientUploadItems.setXlsFile(rs.getString("xlsFile"));
                     clientUploadItems.setDocFile(rs.getString("wordFile"));
 	                clientUploadItems.setType(AlgorithmType.valueOf(rs.getString("type")));
                 }
 
             } catch (Exception ex) {
 
                 lgr.log(Level.SEVERE,
                        "Failed to retrieve meta data information" + ex.getMessage(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
         return clientUploadItems;
     }
 
 }
