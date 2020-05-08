 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.yuukou.common;
 
 import com.yuukou.data.TimeTable;
 import com.yuukou.data.Location;
 import com.yuukou.data.Room;
 import com.yuukou.data.UserList;
 import com.yuukou.data.Computer;
 import com.yuukou.data.LocationList;
 import com.yuukou.data.RoomList;
 import com.yuukou.data.User;
 import com.yuukou.connection.Connection;
 import com.yuukou.data.*;
 import com.yuukou.stats.Graph;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 /**
  *
  * @author Yacine
  */
 public class YuukouServlet extends HttpServlet {
 
     private byte[] tabImageByte;
 
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
     //@WebServiceRef(wsdlLocation = "WEB-INF/wsdl/yuukou2.wmin.ac.uk_8181/YuukouServerService/YuukouServerService.wsdl")
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         String type = request.getParameter("choice");
         if (type == null || type.isEmpty()) {
         }
 
         RoomList rl = new RoomList();
         Room r = new Room();
         Graph g = new Graph();
         UserList ul = new UserList();
         User u = new User();
         LocationList locationList = new LocationList();
         Computer cp = new Computer();
 
 
         String url = null;
 
         if (type.equals("find")) {
             url = "/roomsDisplay.jsp";
             roomsStatus(rl);
             roomLocation(rl);
             request.setAttribute("roomList", rl);
         } else if (type.equals("stats")) {
             url = "/roomStatistics.jsp";
             roomsStatus(rl);
             roomLocation(rl);
             //graphRequest(g, "2012-04-16 00:00:00", "2012-04-17 00:00:00", "0");
             request.setAttribute("roomList", rl);
         } else if (type.equals("Room")) {
             url = "/roomInfos.jsp";
             String id = request.getParameter("id");
             roomStatus(r, id, cp);
             getLocation(locationList);
             roomLocation(r, locationList);
             parseWho(ul, r);
             request.setAttribute("userlist", ul);
             request.setAttribute("room", r);
 
         } else if (type.equals("stattest")) {
             url = "/roomStatisticsAdmin.jsp";
             roomsStatus(rl);
             roomLocation(rl);
             graphRequest(g, "2012-05-01 00:00:00", "2012-05-10 00:00:00", 0);
             request.setAttribute("roomList", rl);
             request.setAttribute("graph", g);
         } else if (type.equals("adminView")) {
             url = "/adminView.jsp";
             roomsStatus(rl);
             roomLocation(rl);
             getLocation(locationList);
             request.setAttribute("locationList", locationList);
             request.setAttribute("roomList", rl);
 
         } else if (type.equals("findUser")) {
             url = "/userInfos.jsp";
             String idUser = request.getParameter("idUser");
             searchHistoryUser(u, idUser);
             request.setAttribute("user", u);
         } else if (type.equals("computerInfo")) {
             url = "/computerInfos.jsp";
             String idPc = request.getParameter("idPc");
             searchHistoryResource(cp, idPc);
             request.setAttribute("computer", cp);
         }
 
 
 
         ServletContext sc = getServletContext();
 
         RequestDispatcher rd = sc.getRequestDispatcher(url);
 
         rd.forward(request, response);
 
 
 //        response.setContentType("text/html;charset=UTF-8");
 //        PrintWriter out = response.getWriter();
 //        
 //        try {
 //            /*
 //             * TODO output your page here. You may use following sample code.
 //             */
 //            out.println("<html>");
 //            out.println("<head>");
 //            out.println("<title>Servlet YuukouServlet</title>");
 //            out.println("</head>");
 //
 //            out.println("<body>");
 //            out.println("<h1>Servlet YuukouServlet at " + request.getContextPath() + "</h1>");
 //            out.println("</body>");
 //            out.println("</html>");
 //
 //        } finally {
 //            out.close();
 //        }
     }
 
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
 
 
         processRequest(request, response);
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
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }
 
     public void roomsStatus(RoomList rl) throws IOException {
         JSONParser jp = new JSONParser();
         Object obj;
 
         Connection c = new Connection();
         String responsehealthForAllRooms = c.conhealthForAllRooms();
 
 
         try {
             obj = jp.parse(responsehealthForAllRooms);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
 
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             rl.setJSONstate("OK");
             rl.setJSONlastCycle(jo.get("JSONLastCycle").toString());
             rl.setJSONmaintenance(jo.get("JSONMaintenance").toString());
             JSONArray joo = (JSONArray) jo.get("JSONContents");
 
             for (Iterator it = joo.iterator(); it.hasNext();) {
                 Room r = new Room();
                 JSONObject jso = (JSONObject) it.next();
                 r.setIdRoom(jso.get("Room").toString());
                 r.setStatus(jso.get("State").toString());
                 r.setTypeResource(jso.get("TypeResources").toString());
 
                 //a finir ajouter une listRooms pour l'
                 r.setHealthRoom(jso.get("Health").toString());
                 r.setAvailability(jso.get("Availability").toString());
                 r.setPcAvailable(jso.get("Available").toString());
                 r.setPcDown(jso.get("Down").toString());
                 r.setResources(jso.get("Resources").toString());
                 r.setBusy(jso.get("Busy").toString());
                 r.setPcTotal(Integer.toString(Integer.parseInt(jso.get("Busy").toString())
                         + Integer.parseInt(jso.get("Available").toString())
                         + Integer.parseInt(jso.get("Down").toString())));
                 r.setRestriction(jso.get("Restriction").toString());
 
                 if (jso.get("State").equals("Busy")) {
                     r.setStartTime(jso.get("StartTime").toString().replace(".0", ""));
                     r.setEndTime(jso.get("EndTime").toString().replace(".0", ""));
                     r.setEventType(jso.get("EventType").toString());
                 }
                 rl.addRoom(r);
 
             }
 
         } else {
             rl.setJSONstate("KO");
             rl.setJSONReason(jo.get("JSONReason").toString());
             rl.setJSONmaintenance(responsehealthForAllRooms);
 
 
         }
     }
 
     public void roomLocation(RoomList rl) {
         JSONParser jp = new JSONParser();
         Object obj;
         Connection c = new Connection();
         String responsegetSitesInformation = c.congetSitesInformations();
         int i = 0;
 
         try {
             obj = jp.parse(responsegetSitesInformation);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
 
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             rl.setJSONstate("OK");
             rl.setJSONlastCycle(jo.get("JSONLastCycle").toString());
             rl.setJSONmaintenance(jo.get("JSONMaintenance").toString());
             JSONArray joo = (JSONArray) jo.get("JSONContents");
 
             for (i = 0; i < rl.getSize(); i++) {
 
                 Iterator it = joo.iterator();
                 boolean loop = false;
                 while (it.hasNext() && !loop) {
 
                     Room r = rl.getRoom2(i);
                     JSONObject jso = (JSONObject) it.next();
 
                     if (r.getIdRoom().startsWith(jso.get("Location").toString())) {
 
                         r.setShortLocation(jso.get("ShortLocation").toString());
                         r.setLongLocation(jso.get("LongLocation").toString());
 
                         r.setLetterDescription(jso.get("Location").toString());
 
                         loop = true;
                     }
                 }
 
 
             }
         } else {
 
             rl.setJSONstate("KO");
             rl.setJSONReason(jo.get("JSONReason").toString());
             rl.setJSONmaintenance(responsegetSitesInformation);
 
 
         }
     }
 
     private void roomLocation(Room r, LocationList locationList) {
         String id = r.getIdRoom().split("-")[0];
         if (locationList.contains(id)) {
             Location l = locationList.get(id);
             r.setLetterDescription(l.getId());
             r.setShortLocation(l.getShortLocation());
             r.setLongLocation(l.getLongLocation());
         }
     }
 
     private void getLocation(LocationList locationList) {
         JSONParser jp = new JSONParser();
         Object obj;
         Connection c = new Connection();
         String responsegetSitesInformation = c.congetSitesInformations();
 
         try {
             obj = jp.parse(responsegetSitesInformation);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
 
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             JSONArray joo = (JSONArray) jo.get("JSONContents");
             Iterator it = joo.iterator();
 
             while (it.hasNext()) {
                 JSONObject jso = (JSONObject) it.next();
 
                 String id = jso.get("Location").toString();
                 String shortL = (String) jso.get("ShortLocation");
                 String longL = (String) jso.get("LongLocation");
 
                 locationList.add(id, new Location(id, shortL, longL));
             }
         } else {
             //TODO
         }
     }
 
     public void roomStatus(Room r, String idRoom, Computer cp) throws IOException {
         JSONParser jp = new JSONParser();
         Object obj;
         int i, j, k;
         Connection c = new Connection();
         String responsehealthForRoom = c.healthForRoom(idRoom);
 
 
         try {
             obj = jp.parse(responsehealthForRoom);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
 
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             r.setJSONstate("OK");
             r.setJSONlastCycle(jo.get("JSONLastCycle").toString());
             r.setJSONmaintenance(jo.get("JSONMaintenance").toString());
 
 
             JSONObject jso = (JSONObject) jo.get("JSONContents");
             r.setIdRoom(jso.get("Room").toString());
             r.setRoomDescription(jso.get("Description").toString());
             r.setRoomUrl(jso.get("Url").toString());
             r.setStatus(jso.get("State").toString());
             r.setTypeResource(jso.get("TypeResources").toString());
             r.setHealthRoom(jso.get("Health").toString());
             r.setAvailability(jso.get("Availability").toString());
             r.setPcAvailable(jso.get("Available").toString());
             r.setPcDown(jso.get("Down").toString());
             r.setResources(jso.get("Resources").toString());
             r.setBusy(jso.get("Busy").toString());
             r.setRoomUrl(jso.get("Url").toString());
             r.setRestriction(jso.get("Restriction").toString());
             r.setHasSoftware(jso.get("HasSoftware").toString());
             if (jso.get("State").toString().equals("Busy")) {
                 r.setActualEventId(jso.get("EventId").toString());
             }
             healthResourceForRoom(r);
 
             if (jso.get("HasGroups").toString().equals("YES")) {
                 r.setHasGroups(jso.get("HasGroups").toString());
                 JSONArray joo = (JSONArray) jso.get("GroupsContents");
                 ArrayList<GroupSoftwares> groupSwap = new ArrayList<GroupSoftwares>();
                 for (j = 0; j < joo.size(); j++) {
                     JSONObject joGroup = (JSONObject) joo.get(j);
                     GroupSoftwares gs = new GroupSoftwares();
                     gs.setIdGroup(joGroup.get("Group").toString());
                     gs.setDescriptionGroup(joGroup.get("Description").toString());
                     gs.setHasSoftwareContents(joGroup.get("HasSoftware").toString());
                     ArrayList<Software> softSwap = new ArrayList<Software>();
                     if (gs.getHasSoftwareContents().equals("YES")) {
 
                         JSONArray joo2 = (JSONArray) joGroup.get("SoftwareContents");
                         for (k = 0; k < joo2.size(); k++) {
                             JSONObject joSoft = (JSONObject) joo2.get(k);
                             String swap1 = joSoft.get("Software").toString();
                             String swap2 = joSoft.get("Description").toString();
                             Software st = new Software(swap1, swap2);
                             softSwap.add(st);
                         }
                         gs.setSoftwareContents(softSwap);
 
                     } else {
                         gs.setHasSoftwareContents("NO");
                     }
                     groupSwap.add(gs);
 
                 }
                 r.setGroupsSoftwareList(groupSwap);
 
 
             } else {
                 r.setHasGroups("NO");
             }
 
             if (jso.get("State").equals("Busy")) {
                 if (jso.get("HasTimeTable").equals("YES")) {
 
                     r.setHasTimeTable(true);
 
                     JSONArray joo = (JSONArray) jso.get("TimeTable");
                     TimeTable[] tabTimeTable = new TimeTable[joo.size()];
 
                     for (i = 0; i < joo.size(); i++) {
                         JSONObject joTimeTable = (JSONObject) joo.get(i);
                         String swap0 = (String) joTimeTable.get("EventId");
                         String swap1 = (String) joTimeTable.get("StartTime").toString().replace(".0", "");
                         String swap2 = (String) joTimeTable.get("EndTime").toString().replace(".0", "");
                         String swap3 = (String) joTimeTable.get("EventType");
                         String swap4 = (String) joTimeTable.get("EventDescription");
                         TimeTable tb = new TimeTable(swap1, swap2, swap3, swap4, swap0);
 
                         tabTimeTable[i] = tb;
 
                     }
                     r.setTimeTable(tabTimeTable);
 
                 }
 
             }
             if (jso.get("HasImage").equals("YES")) {
                 r.setHasImage(true);
 
                 JSONArray jab = (JSONArray) jso.get("Image");
                 byte[] tab = new byte[jab.size()];
                 for (i = 0; i < jab.size(); i++) {
                     Long ll = (Long) jab.get(i);
                     String test = String.valueOf(ll);
 
                     tab[i] = Byte.parseByte(test);
                 }
                 r.setImage(convertByteToImage(tab, r.getIdRoom(), "jpg"));
             }
 
         } else {
             r.setJSONstate("KO");
             r.setJSONReason(jo.get("JSONReason").toString());
 
         }
 
     }
 
     public void healthResourceForRoom(Room r) {
         Connection c = new Connection();
         String responseHealthResourceForRoom = c.healthResourcesReportForRoom(r.getIdRoom());
         JSONParser jp = new JSONParser();
         Object obj = null;
 
 
         try {
             obj = jp.parse(responseHealthResourceForRoom);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             JSONObject jso = (JSONObject) jo.get("JSONContents");
 
             if (jso.get("HasContents").toString().equals("YES")) {
                 JSONArray joo = (JSONArray) jso.get("Contents");
                 Computer[] tabComputer = new Computer[joo.size()];
                 String swap4 = (String) jso.get("Room").toString();
                 for (int i = 0; i < joo.size(); i++) {
                     JSONObject joComputers = (JSONObject) joo.get(i);
 
                     String swap1 = (String) joComputers.get("Status").toString();
                     String swap2 = (String) joComputers.get("Resource").toString();
                     String swap3 = (String) joComputers.get("LastTimeSeen").toString();
 
                     /*
                      * cp.setRessourceStatus(swap1); cp.setLastTimeSeen(swap3);
                      * cp.setRessourceName(swap2);
                      */
                     Computer cpt2 = new Computer(swap4, swap2, swap3, swap1);
                     tabComputer[i] = cpt2;
                     r.setHasComputersDown(true);
                 }
 
                 r.setComputerList(tabComputer);
             }
         }
     }
 
     public void parseWho(UserList ul, Room r) {
         JSONParser jp = new JSONParser();
         Object obj;
 
         Connection c = new Connection();
         String responseWho = c.who();
         String startRoom = r.getIdRoom();
 
         try {
             obj = jp.parse(responseWho);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
 
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             ul.setJSONstate("OK");
             ul.setJSONlastCycle(jo.get("JSONLastCycle").toString());
             ul.setJSONmaintenance(jo.get("JSONMaintenance").toString());
 
 
             JSONArray joo = (JSONArray) jo.get("JSONContents");
             for (Iterator it = joo.iterator(); it.hasNext();) {
                 JSONObject jso = (JSONObject) it.next();
 
 
                 if (((String) jso.get("Resource")).startsWith(startRoom)) {
                     User u = new User();
                     u.setIdUser(jso.get("User").toString());
                     u.setResourceUsedByUser(jso.get("Resource").toString());
                     u.setStartTimeSession(jso.get("StartTimeSession").toString().replace(".0", ""));
                     if (!jso.get("IdPicture").toString().isEmpty() || jso.get("IdPicture").toString() != null) {
                         String swap = jso.get("IdPicture").toString().replace("'\'", "");
                         u.setIdPicture(swap);
                     }
                     //searchHistoryUser(u);
                     ul.addUser(u);
                     r.setHasUserOnline(true);
                 }
             }
 
         } else {
 
             ul.setJSONstate("KO");
             ul.setJSONReason(jo.get("JSONReason").toString());
             ul.setJSONmaintenance(responseWho);
 
 
         }
     }
 
     public void searchHistoryUser(User u, String idUser) {
         JSONParser jp = new JSONParser();
         Object obj = null;
         String Duree = null;
         Connection c = new Connection();
         String responseSearchHistoryUser = c.searchHistoryUser(idUser, true, true, 5);
 
 
         System.out.println("Rentre dans la fonction SearchHistoryUser");
         try {
             obj = jp.parse(responseSearchHistoryUser);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null,
                     ex);
         }
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             ArrayList<History> historySwap = new ArrayList<History>();
             u.setJSONState(jo.get("JSONState").toString());
             JSONObject joo = (JSONObject) jo.get("JSONContents");
             u.setIdUser(joo.get("User").toString());
             u.setNameUser(joo.get("Fullname").toString());
             if (!joo.get("IdPicture").toString().isEmpty() || joo.get("IdPicture").toString() != null) {
                 String swap = joo.get("IdPicture").toString().replace("'\'", "");
                 u.setIdPicture(swap);
             }
 
             if (joo.get("HasLast").equals("YES")) {
                 u.setHasHistory(true);
                 JSONArray joo2 = (JSONArray) joo.get("ContentsLast");
 
                 for (Iterator it2 = joo2.iterator(); it2.hasNext();) {
                     JSONObject jso2 = (JSONObject) it2.next();
                     String swap1 = jso2.get("Resource").toString();
 
                     String swap2 = jso2.get("State").toString();
                     String swap3 = jso2.get("StartTimeSession").toString().replace(".0", "");
                     String swap4 = jso2.get("EndTimeSession").toString().replace(".0", "");

                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     try {
 
                         Date fin = sdf.parse(swap3);
                         Date deb = sdf.parse(swap4);
                         long diff = deb.getTime() - fin.getTime();
 
                         float nbHeuresSwap = diff / 3600000.0f;
                         float nbMinPrcent = nbHeuresSwap % 3600000.0f;
                         //String decimalHeures = Float.valueOf(nbMinPrcent).toString().substring(1);

 
 
                         String s = Float.valueOf(nbHeuresSwap).toString();
                         String sf = s.substring(0, s.indexOf("."));
                         float nbHeures = Float.parseFloat(sf);
 
                        float nbMin = (nbMinPrcent * 60);
                        if (nbMin >= 60) {
                            nbMin = (nbMinPrcent * 60) - nbHeures * 60;
                        }
                      
                        float nbMin1 = Math.round(nbMin);
                         System.out.println("Nbheures : " + nbHeures);
                        System.out.println("nbMin : " + nbMin1);
                        
 
                         if (nbHeures == 0) {
                            Duree = nbMin1 + " minutes";
                         } else if (nbHeures == 1) {
                            Duree = nbHeures + " hour " + nbMin1 + " minutes";
                            Duree = Duree.toString().replace(".0", "");
                         } else {
                            Duree = nbHeures + " hours " + nbMin1 + " minutes";
                            Duree = Duree.toString().replace(".0", "");
                         }
 
                     } catch (Exception e) {
                         System.out.println("ERROR :" + e.getMessage());
                     }
 
                     History h = new History(swap1, swap2, swap3, swap4, Duree);
 
                     historySwap.add(h);
                 }
 
             } else {
                 u.setHasHistory(false);
             }
             if (joo.get("HasWho").equals("YES")) {
                 u.setOnline(true);
                 //JSONObject job2 = (JSONObject) jo.get("ContentsWho");
                 JSONArray joo3 = (JSONArray) joo.get("ContentsWho");
                 for (Iterator it3 = joo3.iterator(); it3.hasNext();) {
                     JSONObject jso3 = (JSONObject) it3.next();
                     u.setResourceUsedByUser(jso3.get("Resource").toString());
                     u.setActualState(jso3.get("State").toString());
                     u.setActualSession(jso3.get("StartTimeSession").toString().replace(".0", ""));
                 }
             } else {
                 u.setOnline(false);
             }
             u.setAllHistoryUser(historySwap);
             System.out.println(u.getAllHistoryUser().size());
         } else {
             u.setJSONState(jo.get("JSONState").toString());
         }
     }
 
     public File convertByteToImage(byte[] tab, String idRoom, String extension) throws IOException {
 
         InputStream in = new ByteArrayInputStream(tab);
         BufferedImage bImageFromConvert = ImageIO.read(in);
         File fi = new File(getServletContext().getRealPath("/") + "/images/" + idRoom + "." + extension);
         ImageIO.write(bImageFromConvert, "jpg", fi);
         return fi;
     }
 
     public void graphRequest(Graph g, String timeStart, String timeEnd, int factorStr) throws IOException {
         Connection c = new Connection();
 
         int factor = factorStr;
 
         JSONParser jp = new JSONParser();
         Object obj;
         int i;
 
         String responseGetGraph = c.getGraphWithRequestUsingJson("start_time_session", "label", timeStart, timeEnd, null, factor);
 
         try {
             obj = jp.parse(responseGetGraph);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null,
                     ex);
             return;
         }
 
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             g.setJSONstate("OK");
             g.setJSONlastCycle(jo.get("JSONLastCycle").toString());
             g.setJSONmaintenance(jo.get("JSONMaintenance").toString());
 
 
             JSONObject jso = (JSONObject) jo.get("JSONContents");
             g.setHasData(jso.get("HasData").toString());
             g.setImageType(jso.get("ImageType").toString());
 
             JSONArray joo = (JSONArray) jso.get("ContentsValues");
 
             String[] tabContentsValues = new String[joo.size()];
 
             for (i = 0; i < joo.size(); i++) {
                 tabContentsValues[i] = (String) joo.get(i).toString();
             }
             g.setContentsValues(tabContentsValues);
 
 
             JSONArray joo2 = (JSONArray) jso.get("ContentsDates");
             String[] tabContentsDates = new String[joo2.size()];
             for (i = 0; i < joo2.size();
                     i++) {
                 tabContentsDates[i] = (String) joo2.get(i).toString();
             }
             g.setContentsDates(tabContentsDates);
 
             JSONArray joo3 = (JSONArray) jso.get("Image");
             //String[] tabImages = new String[joo3.size()];
 
             byte[] tabImageByte = new byte[joo3.size()];
 
             for (i = 0; i < joo3.size(); i++) {
 
                 Long ll = (Long) joo3.get(i);
                 String test = String.valueOf(ll);
                 tabImageByte[i] = Byte.parseByte(test);
 
                 //tabImages[i] =(String) joo3.get(i).toString();
             }
             g.setImageFile(convertByteToImage(tabImageByte, "test", g.getImageType()));
         } else {
             g.setJSONstate("KO");
             g.setJSONReason(jo.get("JSONReason").toString());
         }
     }
 
     public void searchHistoryResource(Computer cp, String idPc) {
         JSONParser jp = new JSONParser();
         Object obj = null;
         String swapPict = null;
         String Duree = null;
         Connection c = new Connection();
         String responseSearchHistoryPc = c.searchHistoryResource(idPc, true, true, 5);
 
 
         System.out.println("Rentre dans la fonction SearchHistoryPC");
         try {
             obj = jp.parse(responseSearchHistoryPc);
 
         } catch (ParseException ex) {
             Logger.getLogger(YuukouServlet.class.getName()).log(Level.SEVERE, null,
                     ex);
         }
         JSONObject jo = (JSONObject) obj;
 
         if (jo.get("JSONState").equals("OK")) {
             ArrayList<User> PreviousSwap = new ArrayList<User>();
 
             JSONObject joo = (JSONObject) jo.get("JSONContents");
             cp.setRessourceName(idPc);
             cp.setRessourceStatus(joo.get("Status").toString());
             cp.setLastTimeSeen(joo.get("LastTimeSeen").toString());
 
             if (joo.get("HasLast").equals("YES")) {
                 cp.setHasPreviousUser(true);
                 JSONArray joo2 = (JSONArray) joo.get("ContentsLast");
 
                 for (Iterator it2 = joo2.iterator(); it2.hasNext();) {
                     JSONObject jso2 = (JSONObject) it2.next();
                     String swap1 = jso2.get("Fullname").toString();
                     String swap5 = jso2.get("User").toString();
                     String swap2 = jso2.get("State").toString();
                     String swap3 = jso2.get("StartTimeSession").toString().replace(".0", "");
                     String swap4 = jso2.get("EndTimeSession").toString().replace(".0", "");
                     if (!jso2.get("IdPicture").toString().isEmpty() || jso2.get("IdPicture").toString() != null) {
                         swapPict = jso2.get("IdPicture").toString().replace("'\'", "");
 
                     }
                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     try {
 
                         Date deb = sdf.parse(swap4);
                         Date fin = sdf.parse(swap3);
                         long diff = deb.getTime() - fin.getTime();
 
                         float nbHeuresSwap = diff / 3600000.0f;
                         float nbMinPrcent = nbHeuresSwap % 3600000.0f;
                         //String decimalHeures = Float.valueOf(nbMinPrcent).toString().substring(1);

                         float nbMin = (nbMinPrcent * 60);
 
 
                         String s = Float.valueOf(nbHeuresSwap).toString();
                         String sf = s.substring(0, s.indexOf("."));
                         float nbHeures = Float.parseFloat(sf);
 
                         System.out.println("Nbheures : " + nbHeures);
                         System.out.println("nbMin : " + nbMin);
 
                         if (nbHeures == 0) {
                             Duree = nbMin + " minutes";
                         } else if (nbHeures == 1) {
                             Duree = nbHeures + " hour " + nbMin + " minutes";
                         } else {
                             Duree = nbHeures + " hours " + nbMin + " minutes";
                         }
 
                     } catch (Exception e) {
                         System.out.println("ERROR :" + e.getMessage());
                     }
 
                     User u = new User(swap5, swap1, swap3, swapPict, swap4, swap2, Duree);
                     PreviousSwap.add(u);
                 }
 
             } else {
                 cp.setHasPreviousUser(false);
             }
 
             cp.setPreviousUsers(PreviousSwap);
         }
     }
 }
