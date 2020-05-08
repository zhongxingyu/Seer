 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package Org.MrReporting.Shamik.Login;
 
 import DatabaseConnection.*;
 import Org.MrReporting.Shamik.BeanClass.*;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.*;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.HashSet;
 
 /**
  *
  * @author shamik
  */
 public class login extends HttpServlet {
    
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
          String loginid =request.getParameter("loginid");
          String pass1 =request.getParameter("pass");
         // String password=request.getParameter("password");
          /* Other details will follow
           *
           *
           *
           */
 
         try {
             DbConnection db = new DbConnection();
             Connection c=db.createConnection();
             Statement s =c.createStatement();
                    /*Here we need to check if password and other things are correct or not
                     * Then modelling has to be done and we have to set the session
                      and application objects after this.
                     *
                     */
                    
                  //  s.execute("insert into mr (name) values ('"+name+"')"); Just to see if the db connection is working or not
                    String query="select * from USERMASTER where LOGINID='"+loginid+"'";
                    ResultSet rs=s.executeQuery(query);
                    rs.next();
                   String pass=rs.getString("LOGINPASSWORD");
                    if(!pass.equals(pass1)){
                         response.sendRedirect("/index.jsp?message=User/password doesnt match !");
                    }
                    HashSet set=(HashSet) getServletContext().getAttribute("OnlineList");
                    
                    
             if(!set.add(loginid)){
 
                
                 
                request.getSession().invalidate();
                response.sendRedirect("/index.jsp?message=User allready logged in !");
                
                 
 
 
                
             }
             else {
                    out.println("The name doesnt exist");
                    Mr user=new Mr();
                    user.setLoginId(loginid);
                    user.setFirstName(rs.getString("FIRSTNAME"));
                    user.setGroupName(rs.getString("GROUPNAME"));
                    user.setLastName(rs.getString("LASTNAME"));
                   user.setHqName(rs.getString("HQNAME"));
                   user.setStateName(rs.getString("STATENAME"));
                   user.setDob(rs.getDate("DOB"));
                   user.setDoa(rs.getDate("DOA"));
                    request.getSession().setAttribute("UserInfo",user);
                    if(rs.getString("GROUPNAME").equals("MR")){
                        out.println("send him to MR page");
                    }
                    else{
                            out.println("send him to admin page"); ///SUDIP HERE WE WILL DECIDE WHERE TO SEND HIM !!!!!
                             }
                    out.println("Succesfull");
 
             }
         }
         catch(Exception e ){
             e.printStackTrace();
         }
 
         finally {
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
     throws ServletException, IOException {
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
     throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
 }
