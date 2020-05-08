 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package plantgame.controllers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import plantgame.models.Fruits;
 import plantgame.models.User;
 import plantgame.models.Plot;
 import plantgame.models.UserItem;
 import plantgame.utils.Constants;
 import plantgame.utils.GameItemsEnum;
 import plantgame.utils.FruitsEnum;
 
 /**
  *
  * @author minh
  */
 public class GardenServlet extends HttpServlet {
 
     /**
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
      * methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
       
         //First get the user object from the session
         HttpSession session = request.getSession();
         User user = null;
         //TODO: forward to some error page or something
         if (session == null){
             System.out.println("Garden Servlet processRequest session was null");
             log("Garden Servlet processRequest session was null");
         }
         else{
             //Get the user object from the session
             user = (User)session.getAttribute(Constants.USER);
             if (user == null){
                 System.out.println("Garden Servlet processRequest user was null");
                 log("Garden Servlet processRequest user was null");
             }
             else {
                 ArrayList<String> errors = new ArrayList<String>();
                 System.out.println(user.getFirstName());
                 Enumeration paramNames = request.getParameterNames();
                 while(paramNames.hasMoreElements()) {
                     String paramName = (String)paramNames.nextElement();
                     System.out.println(
                     paramName + " = " + request.getParameter(paramName));
                     
                     if (paramName.equalsIgnoreCase("actionPlow")) {
                         int plotNumber = 
                             Integer.parseInt(request.getParameter(paramName));
                         // Allow users to dig
                         user.getGarden().getPlots().get(plotNumber).setIsPlowed(true);
                         user.getGarden().getPlots().get(plotNumber).setPlotStatus(Constants.PLOT_STATUS_NEED_SEED);
                     }
                     
                     // Allow users to plant
                     if (paramName.equalsIgnoreCase("actionPlant")) {
                         int plotNumber = 
                             Integer.parseInt(request.getParameter(paramName));
                         if (!(user.getGarden().getPlots().get(plotNumber).getPlotStatus().equalsIgnoreCase(Constants.PLOT_STATUS_NEED_SEED))) {
                             // this plot has not been plowed
                             errors.add(Constants.ERROR_PLOT_NOT_PLOWED);
                         }
                         else {
                             String seedToPlant =
                                 request.getParameter("seedToPlant");
                             System.out.println("seedToPlant: " + seedToPlant);
                             if (seedToPlant == null) {
                                 errors.add(Constants.ERROR_SEED_NOT_SELECTED);
                             }
                             else {
                                 for (GameItemsEnum item:GameItemsEnum.values()){
                                     if (item.getName().equalsIgnoreCase(seedToPlant)) {
                                         // update the number of this seed
                                         HashMap<String, UserItem> userItems = user.getItems();
                                         UserItem userItem = userItems.get(item.getName());
                                         if (userItem.getNumberOfItem() == 0) {
                                             //users can't plant any seed of this type because they have 0 seed of this type
                                             errors.add(Constants.ERROR_SEED_NOT_AVAILABLE);
                                         }
                                         else {
                                             user.getGarden().getPlots().get(plotNumber).setPlotStatus(Constants.PLOT_STATUS_HAS_SEED);
                                             userItem.setNumberOfItem(userItem.getNumberOfItem()-1);
                                             userItems.put(item.getName(), userItem);
                                             user.setItems(userItems);
                                             // update the fruit in this plot
                                             String fruitType = seedToPlant.replace(" Seed", "");
                                             for (FruitsEnum fruitsEnumItem:FruitsEnum.values()){
                                                 if (fruitsEnumItem.getName().equalsIgnoreCase(fruitType)) {
                                                     // Fruits.java: Fruits() constructor: what id is for?
                                                     // set id = 1 for all fruits for now
                                                     Fruits fruit = new Fruits(fruitsEnumItem, 1);
                                                     user.getGarden().getPlots().get(plotNumber).setFruit(fruit);
                                                     user.getGarden().getPlots().get(plotNumber).getFruit().startGrowing();
                                                    
                                                 }
                                             }
                                         }
                                     }  
                                 }//for (GameItemsEnum item:GameItemsEnum.values())   
                             }
                         }
                     }//if (paramName.equalsIgnoreCase("actionPlant"))
                     // Allow users to water
                     if (paramName.equalsIgnoreCase("actionWater")) {
                         int plotNumber = 
                             Integer.parseInt(request.getParameter(paramName));
                         if (!(user.getGarden().getPlots().get(plotNumber).getPlotStatus().equalsIgnoreCase(Constants.PLOT_STATUS_HAS_SEED))) {
                             // no tree has been planted in this plot
                             errors.add(Constants.ERROR_PLOT_NO_SEED);
                         }
                         else {
                             for (GameItemsEnum item:GameItemsEnum.values()){
                                 if (item.getName().equalsIgnoreCase("Water")) {
                                     // update the number of this seed
                                     HashMap<String, UserItem> userItems = user.getItems();
                                     UserItem userItem = userItems.get(item.getName());
                                     if (userItem.getNumberOfItem() == 0) {
                                         // no more water
                                         errors.add(Constants.ERROR_NO_WATER_OR_FERTILIZER);
                                     }
                                     else {
                                         user.getGarden().getPlots().get(plotNumber).getFruit().setNumberOfTimesWater(user.getGarden().getPlots().get(plotNumber).getFruit().getNumberOfTimesWater()+1);                                 
                                         System.out.println("number of times watering: " + user.getGarden().getPlots().get(plotNumber).getFruit().getNumberOfTimesWater());
                                         // update the amount of water
                                         userItem.setNumberOfItem(userItem.getNumberOfItem()-1);
                                         userItems.put(item.getName(), userItem);
                                         user.setItems(userItems);
                                     }
                                 }
                             }
                         }
                     }//if (paramName.equalsIgnoreCase("actionWater"))
                     // Allow users to fertilize
                     if (paramName.equalsIgnoreCase("actionFertilize")) {
                         int plotNumber = 
                             Integer.parseInt(request.getParameter(paramName));
                         if (!(user.getGarden().getPlots().get(plotNumber).getPlotStatus().equalsIgnoreCase(Constants.PLOT_STATUS_HAS_SEED))) {
                             // no tree has been planted in this plot
                             errors.add(Constants.ERROR_PLOT_NO_SEED);
                         }
                         else {
                             for (GameItemsEnum item:GameItemsEnum.values()){
                                 if (item.getName().equalsIgnoreCase("Fertilizer")) {
                                     // update the number of this seed
                                     HashMap<String, UserItem> userItems = user.getItems();
                                     UserItem userItem = userItems.get(item.getName());
                                     if (userItem.getNumberOfItem() == 0) {
                                         // no more fertilizer
                                         errors.add(Constants.ERROR_NO_WATER_OR_FERTILIZER);
                                     }
                                     else {
                                         user.getGarden().getPlots().get(plotNumber).getFruit().setNumberOfTimesFertilize(user.getGarden().getPlots().get(plotNumber).getFruit().getNumberOfTimesFertilize()+1);
                                         System.out.println("number of times fertilizing: " + user.getGarden().getPlots().get(plotNumber).getFruit().getNumberOfTimesFertilize());
                                         // update the amount of fertilizer
                                         userItem.setNumberOfItem(userItem.getNumberOfItem()-1);
                                         userItems.put(item.getName(), userItem);
                                         user.setItems(userItems);
                                     }
                                 }
                             }
                         }
                     }//if (paramName.equalsIgnoreCase("actionFertilize"))
                 }//while(paramNames.hasMoreElements())
                 request.setAttribute(Constants.USER, user);
                 RequestDispatcher rDispatcher;
                 String message = "";
                 if (errors.size() > 0) {
                     // concatenating all error messages
                     String last_message = "";
                     for (int i = 0; i < errors.size(); i++) {
                         String this_error = errors.get(i).concat("<br>");
                         message = last_message.concat(this_error);
                         last_message = message;
                     }
                     request.setAttribute(Constants.ERROR_MESSAGE, message);
                         //DEBUG
                     System.out.println("Garden Servlet forwarding request to " + Constants.ERROR_JSP);
                     log("Garden Servlet forwarding request to " + Constants.ERROR_JSP);
                     //Forward the request to the Store.jsp 
                     rDispatcher = getServletContext().getRequestDispatcher(Constants.ERROR_JSP);
                 }
                 else {
                     //Add user to the request
                     request.setAttribute(Constants.USER, user);
                     //DEBUG
                     System.out.println("Garden Servlet forwarding request to " + Constants.GARDEN_JSP);
                     log("Garden Servlet forwarding request to " + Constants.GARDEN_JSP);
                     //Forward the request to the Store.jsp 
                     rDispatcher = getServletContext().getRequestDispatcher(Constants.GARDEN_JSP);
                 }
                 rDispatcher.forward(request, response);
             }
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP <code>GET</code> method.
      *
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
 
 }
