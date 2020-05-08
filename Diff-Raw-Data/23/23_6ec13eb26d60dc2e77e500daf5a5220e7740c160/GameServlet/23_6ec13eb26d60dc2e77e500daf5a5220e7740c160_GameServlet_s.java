 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import model.Cube;
 import model.Field;
 import model.GameInformation;
 import model.Player;
 import model.FieldMap;
 
 /**
  *
  * @author rainer
  */
 public class GameServlet extends HttpServlet {
     private GameInformation gameInfo = null;
     private Player player1 = null;
     private Player player2 = null;
     private FieldMap fieldMap = null;
     private Random generator = null;
     private String computerCube = "";
 
     @Override
     public void init() throws ServletException {
         super.init();
         initGame();
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
         String action = request.getParameter("action");
         
         if (action == null) {
             return;
         }
         
         if (action.equals("wuerfeln")) {                           
             HttpSession session = request.getSession(true);
             this.gameInfo.incrementRound();
             
             // Spieler1 würfelt zuerst
             wuerfeln(this.player1);
             
             // Spieler1 würfelt eine 6
             if (this.player1.getCube().getNumber() == 6) {
                 // Spieler1 ist noch im Starthaus
                 if (this.player1.getRunning() == false) {
                     // Spieler1 auf erstes Feld setzen
                     this.player1.setRunning(true);
                     this.player1.setActPosition(1);
                     
                     Field f = new Field();
                     f.setId("field" + player1.getActPosition());
                     f.setAlt("");
                     f.setTitle("");
                     f.setSrc("img/field_yellow_player_yellow.png");
                     this.fieldMap.getFieldMap().put(new Integer(1), f);
                     
                     // Spieler1 Starthaus leeren
                    /*f.setId("field" + 41);
                    f.setAlt("");
                    f.setTitle("");
                    f.setSrc("img/field1.png");
                    this.player1.getHomeMap().getFieldMap().put(new Integer(41), f);*/
                     
                     session.setAttribute("wuerfel", player1.getCube());
                     session.setAttribute("fieldMap", this.fieldMap);
                     session.setAttribute("gameInfo", gameInfo);
                     RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
                     dispatcher.forward(request, response);
                 } else {
                     // Spieler1 ist bereits im Feld
                     move(this.player1);
                     // Spieler1 darf nocheinmal würfeln
                     session.setAttribute("gameInfo", gameInfo);
                     RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
                     dispatcher.forward(request, response);
                 }
             } else {
                 // Spieler1 würfelt keine 6
                 // Überprüfen ob noch im Starthaus
                 if (this.player1.getRunning() == false) {
                     // Spieler1 ist im Starthaus
                     session.setAttribute("wuerfel", player1.getCube());
                     session.setAttribute("fieldMap", this.fieldMap);
                     session.setAttribute("gameInfo", gameInfo);
                     RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
                     dispatcher.forward(request, response);
                 } else {
                     // Spieler1 ist bereits im Feld
                     move(this.player1);
                     
                     session.setAttribute("fieldMap", this.fieldMap);
                     RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
                     dispatcher.forward(request, response);
                 }
                 
                 // Spieler2 würfelt
                 //wuerfeln(this.player2);
                 
                 // Spieler2 würfelt eine 6
             }
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
         //TODO
         // palce restart game here
         HttpSession session = request.getSession(true);         
         
         gameInfo = new GameInformation();
         player1 = new Player();
         player2 = new Player();
         fieldMap = new FieldMap();
         generator = new Random();
         
         //Start-Position
         player1.setActPosition(1);
         player1.setAlt("Feld 43: Startfeld Spieler 1: Spieler 1");
         player1.setCube(new Cube());
         //player1.setFieldList(initPlayerFields());
         player1.setHomeMap(initHomeFields(1));
         player1.setGoalMap(initGoalFields(1));
         player1.setId("player1");
         player1.setSrc("img/field1_player1.png");
         player1.setName("Super Mario");
         player1.setTitle("Feld 43: Startfeld Spieler 1: Spieler 1");
         player1.setStart(1);
         player1.setStop(40);
         
         //Start-Position
         player2.setActPosition(11);
         player2.setAlt("Feld 45: Startfeld Spieler 2: Spieler 2");
         player2.setCube(new Cube());
         //player2.setFieldList(initPlayerFields());
         player2.setHomeMap(initHomeFields(2));
         player2.setGoalMap(initGoalFields(2));
         player2.setId("player2");
         player2.setSrc("img/field2_player2.png");
         player2.setName("Computer");
         player2.setTitle("Feld 45: Startfeld Spieler 2: Spieler 2");
         player2.setStart(11);
         player2.setStop(10);
         
         gameInfo.addPlayer("Spieler 1", player1);
         gameInfo.addPlayer("Spieler 2", player2);
         this.computerCube = "";
         
         for (int i = 1; i <= 40; i++) {
             Field f = new Field();
             f.setId("field" + i);
             if (i == 1)
                 f.setSrc("img/field1.png");
             else if (i == 11)
                 f.setSrc("img/field2.png");
             else if (i == 21)
                 f.setSrc("img/field3.png");
             else if (i == 31)
                 f.setSrc("img/field4.png");
             else
                 f.setSrc("img/field.png");
             //TODO richtige Werte
             f.setAlt("");
             f.setTitle("");
             fieldMap.getFieldMap().put(new Integer(i), f);
         }  
         
         player1.getCube().setTitle("W&uuml;rfel Zahl 1");
         player1.getCube().setSrc("img/wuerfel1.png");
         player1.getCube().setAlt("W&uuml;rfel Zahl 1");
         
         Field f;
         f = new Field();
         f.setId("field" + 43);
         f.setSrc("img/field_yellow_player_yellow.png");
         f.setAlt("");
         f.setTitle("");
         
         player1.getHomeMap().getFieldMap().put(new Integer(43), f);
         
         f = new Field();
         f.setId("field" + 47);
         f.setSrc("img/field_green_player_green.png");
         f.setAlt("");
         f.setTitle("");
         
         player2.getHomeMap().getFieldMap().put(new Integer(47), f);
         
         session.setAttribute("wuerfel", player1.getCube());
         session.setAttribute("fieldMap", this.fieldMap);
         session.setAttribute("gameInfo", gameInfo);
         
         RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
         dispatcher.forward(request, response);
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
     
     private void initGame() {
         gameInfo = new GameInformation();
         player1 = new Player();
         player2 = new Player();
         fieldMap = new FieldMap();
         generator = new Random();
         
         //Start-Position
         player1.setActPosition(1);
         player1.setAlt("Feld 43: Startfeld Spieler 1: Spieler 1");
         player1.setCube(new Cube());
         //player1.setFieldList(initPlayerFields());
         player1.setHomeMap(initHomeFields(1));
         player1.setGoalMap(initGoalFields(1));
         player1.setId("player1");
         player1.setSrc("img/field1_player1.png");
         player1.setName("Super Mario");
         player1.setTitle("Feld 43: Startfeld Spieler 1: Spieler 1");
         player1.setStart(1);
         player1.setStop(40);
         
         //Start-Position
         player2.setActPosition(11);
         player2.setAlt("Feld 45: Startfeld Spieler 2: Spieler 2");
         player2.setCube(new Cube());
         //player2.setFieldList(initPlayerFields());
         player2.setHomeMap(initHomeFields(2));
         player2.setGoalMap(initGoalFields(2));
         player2.setId("player2");
         player2.setSrc("img/field2_player2.png");
         player2.setName("Computer");
         player2.setTitle("Feld 45: Startfeld Spieler 2: Spieler 2");
         player2.setStart(11);
         player2.setStop(10);
         
         gameInfo.addPlayer("Spieler 1", player1);
         gameInfo.addPlayer("Spieler 2", player2);
         
         for (int i = 1; i <= 40; i++) {
             Field f = new Field();
             f.setId("field" + i);
             if (i == 1)
                 f.setSrc("img/field1.png");
             else if (i == 11)
                 f.setSrc("img/field2.png");
             else if (i == 21)
                 f.setSrc("img/field3.png");
             else if (i == 31)
                 f.setSrc("img/field4.png");
             else
                 f.setSrc("img/field.png");
             //TODO richtige Werte
             f.setAlt("");
             f.setTitle("");
             fieldMap.getFieldMap().put(new Integer(i), f);
         }  
     }
     
     private FieldMap initPlayerFields() {
         FieldMap lst = new FieldMap();
         
         // haben 1-40 Felder + 4 Zielfelder
         for (int i = 1; i <= 44; i++) {
             Field f = new Field();
             f.setId("field" + i);
             f.setSrc("img/field.png");
             //TODO richtige Werte
             f.setAlt("");
             f.setTitle("");
             lst.getFieldMap().put(new Integer(i), f);
         }
         
         return lst;
     }
     
     private FieldMap initHomeFields(int playerId) {
         FieldMap lst = new FieldMap();
         int start = 0;
         Field f = new Field();
         
         if (playerId == 1) {
             start = 41;
             f.setSrc("img/field_yellow_player_yellow.png");
         }
         
         if (playerId == 2) {
             start = 45;
             f.setSrc("img/field_green_player_green.png");
         }
         
         f.setId("field" + start);
         //TODO richtige Werte
         f.setAlt("");
         f.setTitle("");
         lst.getFieldMap().put(new Integer(start), f);
         
         for (int i = start + 1; i <= start + 4; i++) {
             f = new Field();
             f.setId("field" + i);
             f.setSrc("img/field" + playerId + ".png");
             //TODO richtige Werte
             f.setAlt("");
             f.setTitle("");
             lst.getFieldMap().put(new Integer(i), f);
         }
         
         return lst;
     }
     
     private FieldMap initGoalFields(int playerId) {
         FieldMap lst = new FieldMap();
         int start = 0;
         
         if (playerId == 1)
             start = 57;
         if (playerId == 2)
             start = 61;
         
         for (int i = start; i <= start + 4; i++) {
             Field f = new Field();
             f.setId("field" + i);
             f.setSrc("img/field" + playerId + ".png");
             //TODO richtige Werte
             f.setAlt("");
             f.setTitle("");
             lst.getFieldMap().put(new Integer(i), f);
         }
         
         return lst;
     }
     
     private void wuerfeln(Player player) {
         int number = generator.nextInt(6) + 1;
         player.getCube().setNumber(number);
         
         if (number == 1) {
             player.getCube().setTitle("W&uuml;rfel Zahl 1");
             player.getCube().setSrc("img/wuerfel1.png");
             player.getCube().setAlt("W&uuml;rfel Zahl 1");
         }
         if (number == 2) {
             player.getCube().setTitle("W&uuml;rfel Zahl 2");
             player.getCube().setSrc("img/wuerfel2.png");
             player.getCube().setAlt("W&uuml;rfel Zahl 2");
         }
         if (number == 3) {
             player.getCube().setTitle("W&uuml;rfel Zahl 3");
             player.getCube().setSrc("img/wuerfel3.png");
             player.getCube().setAlt("W&uuml;rfel Zahl 3");
         }
         if (number == 4) {
             player.getCube().setTitle("W&uuml;rfel Zahl 4");
             player.getCube().setSrc("img/wuerfel4.png");
             player.getCube().setAlt("W&uuml;rfel Zahl 4");
         }
         if (number == 5) {
             player.getCube().setTitle("W&uuml;rfel Zahl 5");
             player.getCube().setSrc("img/wuerfel5.png");
             player.getCube().setAlt("W&uuml;rfel Zahl 5");
         }
         if (number == 6) {
             player.getCube().setTitle("W&uuml;rfel Zahl 6");
             player.getCube().setSrc("img/wuerfel6.png");
             player.getCube().setAlt("W&uuml;rfel Zahl 6");
         }
     }
     
     private void move(Player player) {
         HashMap<Integer, Field> map = this.fieldMap.getFieldMap();
         int old = player.getActPosition();
         int tmp = player.getActPosition();
         
         if ((old + player.getCube().getNumber()) > 40) {
             //Spieler erreicht vl das Ziel
             Field f = new Field();
             
             if ("player1".equals(player.getId())) {
                 if ((old + player.getCube().getNumber() + 16) <= 60) {
                     // altes Feld löschen
                    f.setId("field" + old);
                    f.setAlt("");
                    f.setTitle("");
                    f.setSrc("img/field.png");
                    map.put(new Integer(tmp), f);
                     
                     // Zielfeld setzen
                     player.setActPosition(old + player.getCube().getNumber() + 16);
                     f.setId("field" + player.getActPosition());
                     f.setAlt("");
                     f.setTitle("");
                     f.setSrc("img/field_yellow_player_yellow.png");
                     player.getGoalMap().getFieldMap().put(new Integer(player.getActPosition()), f);
                     
                     // Spiel auf beendet setzen
                     this.gameInfo.setFinished(true);
                 } else {
                     //bleibt vor dem Ziel stehen
                     player.setActPosition(40);
                     f.setId("field" + 40);
                     f.setAlt("");
                     f.setTitle("");
                     f.setSrc("img/field_yellow_player_yellow.png");
                     player.getGoalMap().getFieldMap().put(new Integer(40), f);
                 }
                 
             }
             
             if ("player2".equals(player.getId())) {
                 //hardcoded 61
                 f.setId("field" + 61);
                 f.setAlt("");
                 f.setTitle("");
                 f.setSrc("img/field_green_player_green.png");
                 player.getGoalMap().getFieldMap().put(new Integer(61), f);
             }
         } else {
         
             int modulo = ((old + player.getCube().getNumber()) % 40);
             old = (modulo != 0) ? modulo : 40;
 
             player.setActPosition(old);
 
             player.setSrc("img/field_" + player.getId() + ".png");
 
             Field f = new Field();
             f.setId("field" + tmp);
             f.setAlt("");
             f.setTitle("");
 
             if (tmp == 1) {
                 f.setSrc("img/field1.png");
             } else if (tmp == 11) {
                 f.setSrc("img/field2.png");
             } else if (tmp == 21) {
                 f.setSrc("img/field3.png");
             } else if (tmp == 31) {
                 f.setSrc("img/field4.png");
             } else {
                 f.setSrc("img/field.png");
             }
 
             map.put(new Integer(tmp), f);
 
             f = new Field();
             f.setId("field" + player.getActPosition());
             f.setAlt("");
             f.setTitle("");
 
             if (player.getId() == "player1") {
                 if (player.getActPosition() == 1) {
                     f.setSrc("img/field_yellow_player_yellow.png");
                 } else if (player.getActPosition() == 11) {
                     f.setSrc("img/field_green_player_yellow.png");
                 } else if (player.getActPosition() == 21) {
                     f.setSrc("img/field_red_player_yellow.png");
                 } else if (player.getActPosition() == 31) {
                     f.setSrc("img/field_blue_player_yellow.png");
                 } else {
                     f.setSrc(player.getSrc());
                 }
             }
 
             if (player.getId() == "player2") {
                 if (player.getActPosition() == 1) {
                     f.setSrc("img/field_yellow_player_green.png");
                 } else if (player.getActPosition() == 11) {
                     f.setSrc("img/field_green_player_green.png");
                 } else if (player.getActPosition() == 21) {
                     f.setSrc("img/field_red_player_green.png");
                 } else if (player.getActPosition() == 31) {
                     f.setSrc("img/field_blue_player_green.png");
                 } else {
                     f.setSrc(player.getSrc());
                 }
             }
 
             map.put(new Integer(player.getActPosition()), f);
         }
     }
     
     private void restartGame() {
         
     }
 }
