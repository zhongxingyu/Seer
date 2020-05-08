 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pt.webdetails.cdb.connector;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import pt.webdetails.cda.connections.Connection;
 import pt.webdetails.cda.connections.mondrian.JndiConnection;
 import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
 import pt.webdetails.cda.dataaccess.DataAccess;
 import pt.webdetails.cda.dataaccess.MdxDataAccess;
 import pt.webdetails.cpf.repository.RepositoryUtils;
 
 /**
  *
  * @author pdpi
  */
 public class SaikuConnector implements Connector {
 
   private static final String path = "cdb/saiku";
 
   @Override
   public DataAccess exportCdaDataAccess(JSONObject query) {
     String id, name, queryContent;
     try {
       id = query.getString("name");
       JSONObject definition = new JSONObject(query.getString("definition"));
       name = id;
       queryContent = definition.getString("query");
       DataAccess dataAccess = new MdxDataAccess(id, name, id, queryContent);
       //dataAccess;
       return dataAccess;
     } catch (JSONException e) {
       return null;
     }
   }
 
   @Override
   public Connection exportCdaConnection(JSONObject query) {
     String jndi, catalog, cube, id;
     try {
       id = query.getString("name");
       JSONObject definition = new JSONObject(query.getString("definition"));
       jndi = definition.getString("jndi");
       cube = definition.getString("cube");
       catalog = definition.getString("catalog");
       MondrianJndiConnectionInfo cinfo = new MondrianJndiConnectionInfo(jndi, catalog, cube);
       Connection conn = new JndiConnection(id, cinfo);
       return conn;
     } catch (JSONException e) {
       return null;
     }
   }
 
   @Override
  public void copyQuery(String oldGroup, String oldName, String newGroup, String newName) {
    String newFileName = newGroup + "-" + newName + ".saiku",
            oldFileName = oldGroup + "-" + oldName + ".saiku";
     RepositoryUtils.copySolutionFile(path, oldFileName, path, newFileName);
   }
 
   @Override
  public void moveQuery(String oldGroup, String oldName, String newGroup, String newName) {
    String newFileName = newGroup + "-" + newName + ".saiku",
            oldFileName = oldGroup + "-" + oldName + ".saiku";
    RepositoryUtils.moveSolutionFile(path, oldFileName, path, newFileName);
  }

  @Override
   public void deleteQuery(String guid) {
     String fileName = guid + ".saiku";
     RepositoryUtils.deleteSolutionFile(path, fileName);
 
   }
 
  @Override
  public void deleteQuery(String group, String name) {
    String fileName = group + "-" + name + ".saiku";
    RepositoryUtils.deleteSolutionFile(path, fileName);

  }

 }
