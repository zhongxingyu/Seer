 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author
  * Pulkit
  */
 @WebServlet(name = "showPrescription", urlPatterns = {"/showPrescription"})
 public class showPrescription extends HttpServlet {
 
     /**
      * Processes
      * requests
      * for
      * both
      * HTTP
      * <code>GET</code>
      * and
      * <code>POST</code>
      * methods.
      *
      * @param
      * request
      * servlet
      * request
      * @param
      * response
      * servlet
      * response
      * @throws
      * ServletException
      * if
      * a
      * servlet-specific
      * error
      * occurs
      * @throws
      * IOException
      * if
      * an
      * I/O
      * error
      * occurs
      */
     private static ConfigFetcher fetcher = new ConfigFetcher();
     private static final String DBNAME = fetcher.fetchDBNAME();
     private static final String DB_USERNAME = fetcher.fetchDBUSER();
     private static final String DB_PASSWORD = fetcher.fetchDBPASS();
     private static final String DBSERVER = fetcher.fetchDBSERVER();
     private static final String PRESCRIPTION_QUERY="SELECT * from prescription where prescription_id=?;";
     private static final String DOC_NAME_QUERY="SELECT name from doctor where doc_id=?;";
     private static final String PATIENT_NAME_QUERY="(SELECT name from student where roll_no=?) union (SELECT name from staff where staff_id=?);";
     private static final String DRUG_NAME_QUERY="SELECT name from drugs where drug_id=?;";
     private static final String DRUG_LIST_QUERY="SELECT * from prescription_has_drugs where prescription_id=?;";
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException, Exception {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         HttpSession session = request.getSession();
         Connection con=null;
                 String username=(String)session.getAttribute("username");
                 String password=(String)session.getAttribute("password");
                 checkLoginObj clo = new checkLoginObj();
                 int isLoggedIn=clo.isLoggedIn(username, password);
                 if(isLoggedIn==0)
                 {
                     response.sendRedirect("interfaces/index.html");
                 }
                 else{
         
         //st.nextToken();
         
             /* TODO output your page here. You may use following sample code. */
             /*String druglist=request.getParameter("drugs-list").toString();
             String patient_id=request.getParameter("patient_id").toString();
             String refer_to=request.getParameter("refer_to").toString();
             String refer_from=request.getParameter("refer_from").toString();
             String extra_notes=request.getParameter("extra_notes").toString();
             StringTokenizer st=new StringTokenizer(druglist,",");*/
             String prescription_id=request.getParameter("prescription_id").toString();        
             String drug_id,quantity;
             //    out.println(druglist);
             String druglist="";
             con = connect();
             PreparedStatement prepStmt = con.prepareStatement(PRESCRIPTION_QUERY);
             prepStmt.setString(1, prescription_id);
             ResultSet rs = prepStmt.executeQuery();
             rs.next();
             session.setAttribute("patient_id", rs.getString("patient_id"));
             session.setAttribute("prescription_id", rs.getString("prescription_id"));
             
             session.setAttribute("extra_notes", rs.getString("extra_notice"));
             PreparedStatement prepStmt2 = con.prepareStatement(PATIENT_NAME_QUERY);
             prepStmt2.setString(1, rs.getString("patient_id"));
             prepStmt2.setString(2, rs.getString("patient_id"));
             ResultSet rs2 = prepStmt2.executeQuery();
             rs2.next();
             session.setAttribute("patient_name", rs2.getString("name"));
             PreparedStatement prepStmt3 = con.prepareStatement(DOC_NAME_QUERY);
             prepStmt3.setString(1, rs.getString("doc_id"));
             ResultSet rs3 = prepStmt3.executeQuery();
             rs3.next();
             session.setAttribute("doc_name", rs3.getString("name"));
             prepStmt3.setString(1,rs.getString("refer_from"));
             rs3=prepStmt3.executeQuery();
             rs3.next();
             session.setAttribute("refer_from", rs3.getString("name"));
             PreparedStatement prepStmt4 = con.prepareStatement(DRUG_LIST_QUERY);
             prepStmt4.setString(1, prescription_id);
             ResultSet rs4 = prepStmt4.executeQuery();
             while(rs4.next())
             {
                  drug_id=rs4.getString("drug_id");
                  PreparedStatement prepStmt5 = con.prepareStatement(DRUG_NAME_QUERY);
                  prepStmt5.setString(1, drug_id);
                  ResultSet rs5 = prepStmt5.executeQuery();
                  rs5.next();
                  druglist=druglist + "," + rs5.getString("name") + ","+rs4.getString("quantity");
                  
             }
                     session.setAttribute("druglist", druglist);
             /*System.out.println(patient_id);
             System.out.println(refer_to);
             System.out.println(refer_from);
             System.out.println(druglist);
             System.out.println(extra_notes);
             System.out.println(rs4.getString("name"));
             System.out.println(rs5.getString("name"));*/
             response.sendRedirect("interfaces/printPrescription.jsp");
                 }
     }
 
     
     Connection connect() throws Exception
     {
         Connection con=null;
         try
         {
            String url = "jdbc:mysql://10.3.113.29:3306/"+DBNAME+"?user="+DB_USERNAME+"&password="+DB_PASSWORD;
             Class.forName("com.mysql.jdbc.Driver");
             con = DriverManager.getConnection(url);
         } 
         catch (SQLException sqle) 
         {
             System.out.println("SQLException: Unable to open connection to db: "+sqle.getMessage());
             throw sqle;
         }
          catch(Exception e)
         {
             System.out.println("Exception: Unable to open connection to db: "+e.getMessage());
             throw e;
         }
         
         return con;
         
         
     }
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles
      * the
      * HTTP
      * <code>GET</code>
      * method.
      *
      * @param
      * request
      * servlet
      * request
      * @param
      * response
      * servlet
      * response
      * @throws
      * ServletException
      * if
      * a
      * servlet-specific
      * error
      * occurs
      * @throws
      * IOException
      * if
      * an
      * I/O
      * error
      * occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         try {
             processRequest(request, response);
         } catch (Exception ex) {
             Logger.getLogger(showPrescription.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Handles
      * the
      * HTTP
      * <code>POST</code>
      * method.
      *
      * @param
      * request
      * servlet
      * request
      * @param
      * response
      * servlet
      * response
      * @throws
      * ServletException
      * if
      * a
      * servlet-specific
      * error
      * occurs
      * @throws
      * IOException
      * if
      * an
      * I/O
      * error
      * occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         try {
             processRequest(request, response);
         } catch (Exception ex) {
             Logger.getLogger(showPrescription.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Returns
      * a
      * short
      * description
      * of
      * the
      * servlet.
      *
      * @return
      * a
      * String
      * containing
      * servlet
      * description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
