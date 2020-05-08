 import model.DAO;
 import model.request.Response;
 import model.request.ClauseFrom;
 import model.request.ClauseSelect;
 import model.request.ClauseWhere;
 import model.request.Request;
 import session.SessionHandler;
 import xls.XLSParser;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Vector;
 
 /**
  * Classe principale : servlet en lui meme
  */
 
 public class AirLine extends HttpServlet {
     /**
      * @see javax.servlet.http.HttpServlet#doPost
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         if (request.getParameter("page") == null && request.getParameter("action") == null) {
             RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.jsp");
             rd.forward(request, response);
         } else {
             String page = request.getParameter("page");
             if (page == null) page = "";
             String action = request.getParameter("action");
             if (action == null) action = "";
             HttpSession session = request.getSession();
             if (page.equals("query") &&
                     request.getParameter("query_updating") != null) {
                 Request req = buildQueryFromPost(request);
                 req.cleanRequest();
                 request.setAttribute("Requete", req);
                 request.setAttribute("query_updating", request.getParameter("query_updating"));
                 if (request.getParameter("query_updating").equals("0")) {
                     Response resp = req.processRequest();
                     if (resp != null) {
                         request.setAttribute("Response", resp);
                     }
                 }
                 RequestDispatcher rd = getServletContext().getRequestDispatcher("/dynamicQuery.jsp");
                 rd.forward(request, response);
             } else if (action.equals("login") &&
                     request.getParameter("login") != null &&
                     request.getParameter("login").equals(SessionHandler.ADMIN_LOGIN)) {
                 if (request.getParameter("password") != null &&
                         request.getParameter("password").equals(SessionHandler.ADMIN_PASSWORD)) {
                     session.putValue("admin", "true");
                     RequestDispatcher rd = getServletContext().getRequestDispatcher("/Control.jsp");
                     rd.forward(request, response);
                 } else {
                     RequestDispatcher rd = getServletContext().getRequestDispatcher("/Connection.jsp");
                     rd.forward(request, response);
                 }
             } else if (request.getParameter("page").equals("control")) {
                 if (session.getValue("admin") == null || !session.getValue("admin").equals("true")) {
                     RequestDispatcher rd = getServletContext().getRequestDispatcher("/Connection.jsp");
                     rd.forward(request, response);
                     return;
                 } else if (action.equals("create_table") && request.getParameter("createtablename") != null) {
                     try {
                         DAO.getInstance().processUpdate("CREATE TABLE " + request.getParameter("createtablename"));
                     } catch (SQLException e) {
                         request.setAttribute("shit_happens", "1");
                     }
                 } else if (action.equals("drop_table") && request.getParameter("droptableid") != null) {
                     try {
                         DAO.getInstance().processUpdate("DROP TABLE " + ((Vector<String>) DAO.getInstance().retrieveTablesNames()).get(Integer.parseInt(request.getParameter("droptableid"))));
                     } catch (SQLException e) {
                         request.setAttribute("shit_happens", "1");
                     }
                     catch (NumberFormatException e) {
                         request.setAttribute("shit_happens", "1");
                     }
                 } else if (action.equals("create_field") && request.getParameter("createfieldintableid") != null) {
                     request.setAttribute("createfieldintableid", request.getParameter("createfieldintableid"));
                     if (request.getParameter("createfieldname") != null) {
                         try {
                             DAO.getInstance().processUpdate("ALTER TABLE " +
                                     ((Vector<String>) DAO.getInstance().retrieveTablesNames()).get(Integer.parseInt(request.getParameter("createfieldintableid"))) +
                                     " ADD COLUMN " +
                                     request.getParameter("createfieldname") +
                                     " " +
                                     request.getParameter("createfieldtype") +
                                     " )");
 
                         } catch (SQLException e) {
                             request.setAttribute("shit_happens", "1");
                         }
                         catch (NumberFormatException e) {
                             request.setAttribute("shit_happens", "1");
                         }
                     }
                 } else if (action.equals("drop_field") && request.getParameter("dropfieldintableid") != null) {
                     request.setAttribute("dropfieldintableid", request.getParameter("dropfieldintableid"));
                     if (request.getParameter("dropfieldid") != null) {
                         try {
                             String tableName = ((Vector<String>) DAO.getInstance().retrieveTablesNames()).get(Integer.parseInt(request.getParameter("dropfieldintableid")));
                             String columnName = ((Vector<String>) DAO.getInstance().retrieveColumnsNames(tableName)).get(Integer.parseInt(request.getParameter("dropfieldid")));
                             DAO.getInstance().processUpdate("ALTER TABLE " +
                                     tableName +
                                     " DROP COLUMN " +
                                     columnName +
                                     " )");
 
                         } catch (SQLException e) {
                             request.setAttribute("shit_happens", "1");
                         }
                         catch (NumberFormatException e) {
                             request.setAttribute("shit_happens", "1");
                         }
                     }
                 } else if (action.equals("load_from_file")) {
                     XLSParser parser = new XLSParser();
                     //System.out.println(new File("AirLineData.xls").getAbsolutePath());
                     parser.parse("AirLineData.xls");
                    if(parser.isNyah()){
                        request.setAttribute("shit_happens", "1");
                    }
                 }
                 RequestDispatcher rd = getServletContext().getRequestDispatcher("/Control.jsp");
                 rd.forward(request, response);
             } else {
                 RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.jsp");
                 rd.forward(request, response);
             }
         }
     }
 
     private Request buildQueryFromPost(HttpServletRequest request) {
         Request req = new Request();
         int nb_selects, nb_froms, nb_wheres;
 
         try {
             nb_selects = Integer.parseInt(request.getParameter("nb_selects"));
             nb_froms = Integer.parseInt(request.getParameter("nb_froms"));
             nb_wheres = Integer.parseInt(request.getParameter("nb_wheres"));
 
             for (int i = 0; i <= nb_selects; ++i) {
 
                 int tableIndex = Integer.parseInt(request.getParameter("select_" + i));
                 if (tableIndex >= 0) {
                     int columnIndex = 0;
                     if (request.getParameter("select_" + i + "_2") != null) {
                         columnIndex = Integer.parseInt(request.getParameter("select_" + i + "_2"));
                     }
                     req.addClauseSelect(new ClauseSelect(tableIndex, columnIndex));
                 }
             }
 
             for (int i = 0; i <= nb_froms; ++i) {
                 int tableIndex = Integer.parseInt(request.getParameter("from_" + i));
                 if (tableIndex >= 0) {
                     req.addClauseFrom(new ClauseFrom(tableIndex));
                 }
             }
 
             for (int i = 0; i <= nb_wheres; ++i) {
                 int comparatif = 0;
                 try {
                     comparatif = Integer.parseInt(request.getParameter("where_" + i + "_comp"));
                 } catch (NumberFormatException e) {
                 }
                 String firstField = request.getParameter("where_" + i + "_first");
                 if (firstField == null) {
                     firstField = "";
                 }
                 String secondField = request.getParameter("where_" + i + "_second");
                 if (secondField == null) {
                     secondField = "";
                 }
                 int operator = Integer.parseInt(request.getParameter("where_" + i + "_op"));
                 if (operator >= 0) {
                     req.addClauseWhere(new ClauseWhere(comparatif, firstField, secondField, operator));
                 }
             }
         } catch (NumberFormatException e) {
             return new Request();
         }
 
         return req;
     }
 
     /**
      * @see javax.servlet.http.HttpServlet#doGet
      */
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession();
         RequestDispatcher rd = null;
         String page = request.getParameter("page");
 
         if (page == null) {
             rd = getServletContext().getRequestDispatcher("/index.jsp");
             rd.forward(request, response);
         } else if (page.equals("query")) {
             request.setAttribute("Requete", new Request());
             rd = getServletContext().getRequestDispatcher("/dynamicQuery.jsp");
             rd.forward(request, response);
         } else if (page.equals("login")) {
             rd = getServletContext().getRequestDispatcher("/Connection.jsp");
             rd.forward(request, response);
         } else if (page.equals("control")) {
             rd = getServletContext().getRequestDispatcher("/Control.jsp");
             rd.forward(request, response);
         } else {
             rd = getServletContext().getRequestDispatcher("/index.jsp");
             rd.forward(request, response);
         }
     }
 
     void trash(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 ////    	Connector connector = Connector.getInstance();
 //
 //        Statement question;
 //        String query;
 //        ResultSet answer;
 //        try {
 //            List<Pilot> pilots = DAO.getInstance().retrievePilotsFromCity(request.getParameter("city"));
 //            query = "SELECT * FROM PILOT WHERE Address ='" + request.getParameter("city") + "'";
 //            PrintWriter pen;
 //            Response.setContentType("text/html");
 //            pen = Response.getWriter();
 //            pen.println("<HTML>");
 //            pen.println("<HEAD> <TITLE> Answer </TITLE> </HEAD>");
 //            pen.println("<BODY>");
 //            Collection<String> mougnou = DAO.getInstance().retrieveTablesNames();
 //            for (Pilot pilot : pilots) {
 //                pen.println("<P><B> Pilot : </B>" + pilot.getLastName() + " " + pilot.getFirstName());
 //
 ////					pen.println("<P><B> ---Reference : </B>" + pilotNumber);
 ////					pen.println("<P><B> ---Address : </B>" + address);
 ////					pen.println("<P><B> ---Salary : </B>" + salary);
 ////					pen.println("<P><B> ---since : </B>" + hiringDate);
 ////					if (premium > 0 ) pen.println("<P><B> ---Premium : </B>" + premium);
 ////					else pen.println("<P><B> ---No premium </B>");
 //            }
 //            for (String sdsd : mougnou) {
 //                pen.println("<P><B> TableName : </B>" + sdsd);
 //            }
 //            pen.println("</BODY>");
 //            pen.println("</HTML>");
 ////			answer.close();
 ////			question.close();
 ////			link.close();
 //        } catch (SQLException e) {
 //            System.out.println("Connection error: " + e.getMessage());
 //        }
     }
 
     public void destroy() {
         // la destruction de l'objet singleton Connector entraine la fermeture de la connection a la BDD
         super.destroy();
     }
 }
