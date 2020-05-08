 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import model.Field;
 import model.GameInformation;
 import model.Player;
 import model.Wuerfel;
 
 /**
  *
  * @author rainer
  */
 public class GameServlet extends HttpServlet {
     private GameInformation gameInfo;
     private Player pl1, pl2;
     private Wuerfel wuerfel;
     private List<Field> fields;
     private Random generator = null;
     private String computerCube = "";
 
     @Override
     public void init() throws ServletException {
         super.init();
         gameInfo = new GameInformation();
         wuerfel = new Wuerfel();
         fields = new ArrayList<Field>();
         this.generator = new Random();
         
         pl1 = new Player();
         pl1.setName("Super Mario");
         pl1.setActPosition(43);
         
         pl2 = new Player();
         pl2.setName("Computer");
         pl2.setActPosition(45);
         
         gameInfo.addPlayer("Spieler 1", pl1);
         gameInfo.addPlayer("Spieler 2", pl2);
         
     }
     
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             /*
              * TODO output your page here. You may use following sample code.
              */
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Servlet GameServlet</title>");            
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Servlet GameServlet at " + request.getContextPath() + "</h1>");
             out.println("</body>");
             out.println("</html>");
         } finally {            
             out.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {        
         //processRequest(request, response);
         String action = request.getParameter("action");
         
         if (action == null) {
             return;
         }
         
         if (action.equals("wuerfeln")) {                           
             HttpSession session = request.getSession(true);
             this.gameInfo.incrementRound();
             
             //player to cube
             wuerfeln(this.pl1);
             
             session.setAttribute("wuerfel", pl1.getWuerfel());
             
             // player got a 6
             if (this.pl1.getWuerfel().getNumber() == 6) {
                 // cube again
                 session.setAttribute("gameInfo", gameInfo);
                 RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
                 dispatcher.forward(request, response);
             } else {
                 // computer to cube
                 wuerfeln(this.pl2);
                 
                 // computer got a 6
                 if (this.pl2.getWuerfel().getNumber() == 6) {
                    
                    this.computerCube = "6";
                    
                    while (this.pl2.getWuerfel().getNumber() == 6) {
                        wuerfeln(this.pl2);
                        computerCube += " - " + pl2.getWuerfel().getNumber();
                        gameInfo.setCubeComputer(computerCube);
                    }
                     session.setAttribute("gameInfo", gameInfo);
             
                     RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
                     dispatcher.forward(request, response);
                 } else {
                     computerCube = "";
                     gameInfo.setCubeComputer(computerCube + pl2.getWuerfel().getNumber());
                     session.setAttribute("gameInfo", gameInfo);
             
                     RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
                     dispatcher.forward(request, response);
                 }
             }
         } else {
             
         }  
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
         //TODO
         // palce restart game here
         /*
          * Beispiel Code vom RegistrationMVC aus der LVA
          * 
          * HttpSession session = request.getSession(true);         
         User user =(User)session.getAttribute("user");
         boolean newuser = false;
         if(user == null) {
             user = new User();            
             newuser = true;
             user.setUsername(request.getParameter("username"));
         } else {
             user = userpool.getUser(user.getUsername());
         }
         user.setFirstname(request.getParameter("firstname"));
         user.setLastname(request.getParameter("lastname"));        
         user.setPassword(request.getParameter("password"));
 
         String[] inter = request.getParameterValues("interests");
         if(!newuser) {
             user.clearInterests();
         }
         if(inter != null ) {
             List<String> interests = Arrays.asList(inter);
             if(interests.contains("webEngineering")) {
                 user.addInterest(Interest.WEBENINEERING);                
             }
             if(interests.contains("modelEngineering")) {
                 user.addInterest(Interest.MODELENGINEERING);                
             }
             if(interests.contains("semanticWeb")) {
                 user.addInterest(Interest.SEMANTICWEB);                
             }
             if(interests.contains("objectOrientedModeling")) {
                 user.addInterest(Interest.OBJECTORIENTEDMODELING);                
             }
             if(interests.contains("businessInformatics")) {
                 user.addInterest(Interest.BUSINESSINFORMATICS);                
             }         
         } 
         
         session.setAttribute("user", user);
         
         if(newuser) {
             userpool.registerUser(user);
         }           
         
         RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/userpage.jsp");
         dispatcher.forward(request, response);
          * 
          */
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
     
     private void wuerfeln(Player player) {
         int number = generator.nextInt(6) + 1;
         player.getWuerfel().setNumber(number);
         
         if (number == 1) {
             player.getWuerfel().setTitle("W&uuml;rfel Zahl 1");
             player.getWuerfel().setImage("img/wuerfel1.png");
             player.getWuerfel().setAlt("W&uuml;rfel Zahl 1");
         }
         if (number == 2) {
             player.getWuerfel().setTitle("W&uuml;rfel Zahl 2");
             player.getWuerfel().setImage("img/wuerfel2.png");
             player.getWuerfel().setAlt("W&uuml;rfel Zahl 2");
         }
         if (number == 3) {
             player.getWuerfel().setTitle("W&uuml;rfel Zahl 3");
             player.getWuerfel().setImage("img/wuerfel3.png");
             player.getWuerfel().setAlt("W&uuml;rfel Zahl 3");
         }
         if (number == 4) {
             player.getWuerfel().setTitle("W&uuml;rfel Zahl 4");
             player.getWuerfel().setImage("img/wuerfel4.png");
             player.getWuerfel().setAlt("W&uuml;rfel Zahl 4");
         }
         if (number == 5) {
             player.getWuerfel().setTitle("W&uuml;rfel Zahl 5");
             player.getWuerfel().setImage("img/wuerfel5.png");
             player.getWuerfel().setAlt("W&uuml;rfel Zahl 5");
         }
         if (number == 6) {
             player.getWuerfel().setTitle("W&uuml;rfel Zahl 6");
             player.getWuerfel().setImage("img/wuerfel6.png");
             player.getWuerfel().setAlt("W&uuml;rfel Zahl 6");
         }
     }
 }
