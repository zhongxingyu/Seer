     /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package UserInterface;
 
 import DBDataStructures.Cube;
 import DBDataStructures.DBTable;
 import DBDataStructures.Dimension;
 import DBDataStructures.Measure;
 import com.google.gson.Gson;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Joseph
  */
 public class DBInfo extends HttpServlet
 {
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try
         {
             String dbName = (String) request.getSession().getAttribute("dbname");
             String dbAddr = (String) request.getSession().getAttribute("addr");
             String dbUser = (String) request.getSession().getAttribute("user");
             String dbPW = (String) request.getSession().getAttribute("pw");
 
             Connection conn = null;
             try
             {
                 String userConnect = "jdbc:mysql://"
                         + dbAddr + "/" + dbName + "?user=" + dbUser
                         + "&password=" + dbPW;
 
                 //String cop5725Connect = "jdbc:mysql://localhost/cop5725?user=test&password=test";
                 //String cop5725Connect = "jdbc:mysql://localhost/cop5725?user=root&password=control";
                String cop5725Connect = "jdbc:mysql://172.23.19.231:8080/cop5725?user=root&password=control";
 
                 Class.forName("com.mysql.jdbc.Driver").newInstance();
                 conn = DriverManager.getConnection(userConnect);
 
                 java.sql.PreparedStatement statement = conn.prepareStatement(
                         "select table_name, table_type from information_schema.tables "
                         + "where table_schema = ?");
                 statement.setString(1, dbName);
 
                 ResultSet rs = statement.executeQuery();
 
                 List<TreeData> tree = new ArrayList<TreeData>();
                 TreeData tablesRoot = new TreeData("Tables", "");
                 TreeData viewsRoot = new TreeData("Views", "");
                 while(rs.next())
                 {
                     String tableName = rs.getString(1);
                     String type = rs.getString(2);
 
                     TreeData child = new TreeData(tableName, "");
                     if(DBTable.getTableType(type) == DBTable.TableType.TABLE)
                         tablesRoot.addChild(child);
                     else if(DBTable.getTableType(type) == DBTable.TableType.VIEW)
                         viewsRoot.addChild(child);
                 }
 
                 //load tables and views with appropriate data
                 loadData(tablesRoot, conn, dbName);
                 loadData(viewsRoot, conn, dbName);
 
                 //close connection to given database
                 conn.close();
 
                 //load up cubes from our database
                 conn = DriverManager.getConnection(cop5725Connect);
                 ArrayList<Cube> cubes = getCubes(conn, dbName);
 
                 //build treedata with all cubes information
                 TreeData cubesRoot = new TreeData("Cubes", "");
                 for(Cube c : cubes)
                     cubesRoot.addChild(buildCubeTreeData(c));
 
                 TreeData dbRoot = new TreeData("Data Source - " + dbName, "");
                 dbRoot.addChild(tablesRoot);
                 dbRoot.addChild(viewsRoot);
                 dbRoot.addChild(cubesRoot);
                 tree.add(dbRoot);
                 String json = new Gson().toJson(tree);
                 out.write(json);
             }
             catch(SQLException ex)
             {
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();
             }
             catch(ClassNotFoundException ex)
             {
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();
             }
             catch(Exception ex)
             {
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();
             }
             finally
             {
                 if(conn != null)
                 {
                     try
                     {
                         conn.close();
                     }
                     catch(Exception ex)
                     {
                         System.out.println(ex.getMessage());
                         ex.printStackTrace();
                     }
                 }
             }
         }
         finally
         {
             out.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         processRequest(request, response);
     }
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo()
     {
         return "Short description";
     }// </editor-fold>
 
     private void loadData(TreeData data, java.sql.Connection conn, String schemaName) throws SQLException
     {
         if(data.getChildren() == null)
             return;
         for(TreeData td : data.getChildren())
         {
             java.sql.PreparedStatement getCols = conn.prepareStatement(
                     "select column_name, data_type from information_schema.columns "
                     + "where table_name=? and table_schema=?");
             getCols.setString(1, td.getLabel());
             getCols.setString(2, schemaName);
 
             ResultSet cols = getCols.executeQuery();
 
             while(cols.next())
             {
                 String colName = cols.getString(1);
                 String dType = cols.getString(2);
 
                 TreeData col = new TreeData(colName.toUpperCase(), dType.toUpperCase());
                 td.addChild(col);
             }
         }
     }
 
     private ArrayList<Cube> getCubes(Connection conn, String dbName) throws SQLException
     {
         ArrayList<Cube> cubes = new ArrayList<Cube>();
         java.sql.PreparedStatement statement = conn.prepareStatement("SELECT * FROM cube where dbname=?");
         statement.setString(1, dbName);
 
         ResultSet rs = statement.executeQuery();
         int cubeID = -1;
         //get cube info
         while(rs.next())
         {
             cubeID = rs.getInt(1);
             if(cubeID < 0)
                 throw new SQLException("Failed to retrieve cube!");
 
             Cube cube = new Cube();
             cube.id = cubeID;
             cube.setDbName(dbName);
             cube.setName(rs.getString(2));
             cube.setTable(rs.getString(4));
 
             cubes.add(cube);
         }
 
         //get all cube data
         statement = conn.prepareStatement("SELECT iddimension, name FROM dimension where cube_idcube=?");
         for(int i = 0; i < cubes.size(); i++)
         {
             statement.clearParameters();
             statement.setInt(1, cubes.get(i).id);
 
             rs = statement.executeQuery();
             int dimeID = -1;
             ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
             while(rs.next())
             {
                 dimeID = rs.getInt(1);
                 if(dimeID < 0)
                     throw new SQLException("Failed to retrieve dimension!");
                 Dimension dime = new Dimension();
                 dime.id = dimeID;
                 dime.setName(rs.getString(2));
 
                 ArrayList<String> granules = new ArrayList<String>();
                 java.sql.PreparedStatement grans = conn.prepareStatement("SELECT name from granularity where dimension_iddimension=?");
                 grans.setInt(1, dime.id);
                 ResultSet dimeGrans = grans.executeQuery();
                 while(dimeGrans.next())
                     granules.add(dimeGrans.getString(1));
                 dime.setGranules(granules);
                 dimensions.add(dime);
             }
             cubes.get(i).setDimensions(dimensions);
 
             ArrayList<Measure> measures = new ArrayList<Measure>();
             java.sql.PreparedStatement getMeasures = conn.prepareStatement("SELECT type, columnname from measure where cube_idcube=?");
             getMeasures.setInt(1, cubes.get(i).id);
             ResultSet cubeMs = getMeasures.executeQuery();
             while(cubeMs.next())
             {
                 Measure m = new Measure();
                 m.setType(cubeMs.getString(1));
                 m.setColumnName(cubeMs.getString(2));
                 measures.add(m);
             }
             cubes.get(i).setMeasures(measures);
         }
 
         return cubes;
     }
 
     private TreeData buildCubeTreeData(Cube c)
     {
         TreeData child = new TreeData(c.getName(), "");
 
         TreeData dimeRoot = new TreeData("Dimensions", "");
         TreeData msRoot = new TreeData("Measures", "");
         for(Dimension d : c.getDimensions())
         {
             TreeData dChild = new TreeData(d.getName().toUpperCase(), "ALL");
             dimeRoot.addChild(dChild);
             TreeData gChild = null;
             TreeData tmp = null;
             for(String g : d.getGranules())
             {
                 TreeData granule = new TreeData(g.toUpperCase(), "");
                 if(tmp == null)
                     gChild = tmp = granule;
                 else
                 {
                     tmp.addChild(granule);
                     tmp = gChild.getChildren().get(0);
                 }
             }
             dChild.addChild(gChild);
         }
         for(Measure m : c.getMeasures())
         {
             String tmp = m.getType() + "(" + m.getColumnName() + ")";
             TreeData mChild = new TreeData(tmp.toUpperCase(), "");
             msRoot.addChild(mChild);
         }
 
         child.addChild(dimeRoot);
         child.addChild(msRoot);
 
         return child;
     }
 }
