 package Wrapper;
 
 import QueryHandlers.QueryHandler;
 import java.util.ArrayList;
 import java.util.Calendar;
 import javax.ejb.Stateless;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 /**
  * This class hosts the restfull web services that is used to connect to the
  * database
  *
  * @author NightFiyah
  * @version 1.0
  * @since 2013-07-26
  */
 @Stateless
 @Path("/Wrapper")
 public class Wrapper {
 
     QueryHandler handler = new QueryHandler(0);
     Converter converter = new Converter();
 
     public Wrapper() {
     }
 
     @GET
     @Path("StatusReport/{PropertyId}")
     @Produces("text/html")
     public String StatusReport(@PathParam("PropertyId") int PropertyId) {
 
         ArrayList<String> details = handler.getPlotQH().retrievePlotDetails(PropertyId);
 
         StringBuilder html = new StringBuilder();
         html.append("<html>");
         html.append("<head>");
 
         /*
          * CSS for page
          */
         html.append("<style type=\"text/css\">");
         html.append("body{"
                 + "font-family: \"century gothic\";"
                 + "background-color: white;"
                 + "border-right-width: 2px;"
                 + "border-bottom-width: 2px;"
                 + "border-left-width: 2px;"
                 + "border-top-width: 2px;"
                 + "border-top-style: solid;"
                 + "border-right-style: solid;"
                 + "border-bottom-style: solid;"
                 + "border-left-style: solid;"
                 + "border-top-color: #FF0000;"
                 + "border-right-color: #FF0000;"
                 + "border-bottom-color: #FF0000;"
                 + "border-left-color: #FF0000;"
                 + "}");
         html.append("h1{"
                 + "text-align: center;"
                 + "}");
         html.append("table{"
                 + "width: 90%;"
                 + "}");
         html.append("td{"
                 + "width: 50%;"
                 + "}");
         html.append("hr{"
                 + ""
                 + "}");
         html.append(".hilight{"
                 + "font-size: 12px;"
                 + "}");
         html.append(".sheading{"
                 + "font-size: 14px;"
                 + "font-weight: bold;"
                 + "}");
         html.append(".ssheading{"
                 + "font-size: 10px;"
                 + "font-weight: bold;"
                 + "}");
         html.append("</style>");
         html.append("</head>");
 
         html.append("<body>");
         html.append("<h1>Status report</h1>");
 
         html.append("<table>");
         html.append("<tr>");
         try {
             details.get(1).substring(0, details.get(1).indexOf("&*&"));
             html.append("<td class=\"hilight\">Character Name:</td><td>").append(details.get(1).substring(0, details.get(1).indexOf("&*&"))).append("</td>");
         } catch (Exception e) {
             html.append("<td class=\"hilight\">Character Name:</td><td>").append(details.get(1)).append("</td>");
         }
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td class=\"hilight\">Estate Number:</td><td>").append(details.get(0)).append("</td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td class=\"hilight\">Estate Name:</td><td>").append(details.get(18).replaceAll("\\.", " ")).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td class=\"hilight\">Duchy:</td><td>").append(details.get(3)).append("</td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td class=\"hilight\">County:</td><td>").append(details.get(19)).append("</td>");
         html.append("</tr>");
         html.append("</table>");
 
         html.append("<br/>");
 
         if (!"".equals(handler.getPlotQH().getDescription(PropertyId))) {
             html.append("<p class=\"sheading\">Plot Description</p>");
             html.append(handler.getPlotQH().getDescription(PropertyId).replaceAll("~", " "));
             html.append("</p>");
             html.append("<br/>");
         }
         html.append("<table>");
         html.append("<tr>");
         html.append("<td class=\"sheading\">Acres</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Poor:</td><td>").append(details.get(15)).append("/").append(details.get(16)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Fine:</td><td>").append(details.get(13)).append("/").append(details.get(14)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Exquisite:</td><td>").append(details.get(11)).append("/").append(details.get(12)).append("</td>");
         html.append("</tr>");
         html.append("</table>");
 
         html.append("<br/>");
 
         html.append("<table>");
         html.append("<tr>");
         html.append("<td class=\"sheading\">Happiness and Workers</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Happiness:</td><td>").append(details.get(7)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Workers employed:</td><td>").append(details.get(9)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Maximum workers:</td><td>").append(details.get(10)).append("</td>");
         html.append("</tr>");
         html.append("</table>");
 
         html.append("<br/>");
 
         ArrayList<String> money = handler.getPlotQH().getCurrentAmount(PropertyId);
         html.append("<table>");
         html.append("<tr>");
         html.append("<td class=\"sheading\">Estate funds</td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td>Income:</td><td>").append(details.get(8)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Platinum thrones:</td><td>").append(money.get(0)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Gold crowns:</td><td>").append(money.get(1)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Silver shields:</td><td>").append(money.get(2)).append("</td>");
         html.append("</tr>");
         html.append("</table>");
 
         html.append("<br/>");
 
         Calendar cal = Calendar.getInstance();
         int month = cal.get(cal.MONTH) + 1; //zero-based
 
         ArrayList<String[]> event = handler.getEventQH().getEvent(month, PropertyId);
 
         if (event != null) {
             if (event.size() > 0) {
                 html.append("<p class=\"sheading\">Events</p>");
                 html.append("<br/><hr/>");
 
                 for (int i = 0; i < event.size(); i++) {
                     html.append("<table>");
                     html.append("<tr><td class=\"ssheading\">Name:</td><td>").append(event.get(i)[2]).append("</td></tr>");
                     html.append("<tr><td class=\"ssheading\">Description:</td><td>").append(event.get(i)[3].replace("~", " ")).append("</td></tr>");
 
                     html.append("<tr>");
                     html.append("<td>Platinum Effect:</td><td>").append(event.get(i)[5]).append("</td>");
                     html.append("</tr>");
 
                     html.append("<tr>");
                     html.append("<td>Gold Effect:</td><td>").append(event.get(i)[6]).append("</td>");
                     html.append("</tr>");
 
                     html.append("<tr>");
                     html.append("<td>Silver Effect:</td><td>").append(event.get(i)[7]).append("</td>");
                     html.append("</tr>");
 
                     html.append("<tr>");
                     html.append("<td>Happiness Effect:</td><td>").append(event.get(i)[8]).append("</td>");
                     html.append("</tr>");
 
                     html.append("<tr>");
                     html.append("<td>Income Effect:</td><td>").append(event.get(i)[9]).append("%</td>");
                     html.append("</tr>");
                     html.append("</table>");
                     html.append("<hr/>");
                 }
             }
         }
 
         html.append("</body>");
         html.append("</html>");
 
         return html.toString();
     }
 
     @GET
     @Path("SuperStatusReport/{PropertyId}")
     @Produces("text/html")
     public String SuperStatusReport(@PathParam("PropertyId") int PropertyId) {
 
         ArrayList<String> details = handler.getPlotQH().retrievePlotDetails(PropertyId);
 
        System.out.println("DETAILS HERER ");
        System.out.println(details.get(0));
        System.out.println(details.get(1));
         StringBuilder html = new StringBuilder();
         html.append("<html>");
         html.append("<head>");
 
         /*
          * CSS for page
          */
         html.append("<style type=\"text/css\">");
         html.append("body{"
                 + "font-family: \"century gothic\";"
                 + "background-color: white;"
                 + "border-right-width: 2px;"
                 + "border-bottom-width: 2px;"
                 + "border-left-width: 2px;"
                 + "border-top-width: 2px;"
                 + "border-top-style: solid;"
                 + "border-right-style: solid;"
                 + "border-bottom-style: solid;"
                 + "border-left-style: solid;"
                 + "border-top-color: #FF0000;"
                 + "border-right-color: #FF0000;"
                 + "border-bottom-color: #FF0000;"
                 + "border-left-color: #FF0000;"
                 + "}");
         html.append("h1{"
                 + "text-align: center;"
                 + "}");
         html.append("table{"
                 + "width: 90%;"
                 + "}");
         html.append("td{"
                 + "width: 50%;"
                 + "}");
         html.append("th{"
                 + "text-align: left;"
                 + "}");
         html.append(".hilight{"
                 + "font-size: 12px;"
                 + "}");
         html.append(".sheading{"
                 + "font-size: 14px;"
                 + "font-weight: bold;"
                 + "}");
         html.append(".ssheading{"
                 + "font-size: 10px;"
                 + "font-weight: bold;"
                 + "}");
         html.append("</style>");
         html.append("</head>");
 
         html.append("<body>");
 
         html.append("<h1>Status report</h1>");
 
         html.append("<table>");
         html.append("<tr>");
         try {
             details.get(1).substring(0, details.get(1).indexOf("&*&"));
             html.append("<td class=\"hilight\">Character Name:</td><td>").append(details.get(1).substring(0, details.get(1).indexOf("&*&"))).append("</td>");
         } catch (Exception e) {
             html.append("<td class=\"hilight\">Character Name:</td><td>").append(details.get(1)).append("</td>");
         }
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td class=\"hilight\">Estate Number:</td><td>").append(details.get(0)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td class=\"hilight\">Estate Name:</td><td>").append(details.get(18)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td class=\"hilight\">Duchy:</td><td>").append(details.get(3)).append("</td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td class=\"hilight\">Countie:</td><td>").append(details.get(19)).append("</td>");
         html.append("</tr>");
         html.append("</table>");
 
         html.append("<br/>");
         try {
             String replaceAll = handler.getPlotQH().getDescription(PropertyId).replaceAll("~", " ");
             if (!"".equals(handler.getPlotQH().getDescription(PropertyId))) {
                 html.append("<p class=\"sheading\">Plot Description</p>");
                 html.append(handler.getPlotQH().getDescription(PropertyId).replaceAll("~", " "));
                 html.append("</p>");
                 html.append("<br/>");
             }
         } catch (Exception e) {
         }
 
         html.append("<table>");
         html.append("<tr>");
         html.append("<td class=\"sheading\">Acres</td><td></td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Poor:</td><td>").append(details.get(15)).append("/").append(details.get(16)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Fine:</td><td>").append(details.get(13)).append("/").append(details.get(14)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Exquisite:</td><td>").append(details.get(11)).append("/").append(details.get(12)).append("</td>");
         html.append("</tr>");
         html.append("</table>");
 
         html.append("<br/>");
 
         html.append("<table>");
         html.append("<tr><td class=\"sheading\">Happiness and workers</td></tr>");
         html.append("<tr>");
         html.append("<td>Happiness:</td><td>").append(details.get(7)).append("</td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td>Workers employed:</td><td>").append(details.get(9)).append("</td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td>Maximum workers:</td><td>").append(details.get(10)).append("</td>");
         html.append("</tr>");
         html.append("</table>");
 
         html.append("<br/>");
 
         ArrayList<String> money = handler.getPlotQH().getCurrentAmount(PropertyId);
 
         html.append("<table>");
         html.append("<tr>");
         html.append("<td class=\"sheading\">Estate funds</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Platinum thrones:</td><td>").append(money.get(0)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Gold crowns:</td><td>").append(money.get(1)).append("</td>");
         html.append("</tr>");
 
         html.append("<tr>");
         html.append("<td>Silver shields:</td><td>").append(money.get(2)).append("</td>");
         html.append("</tr>");
         html.append("</table>");
 
         html.append("<br/>");
 
         ArrayList<String[]> retrieveAllBuildingsOwnedByCharacter = handler.getBuildingQH().retrieveAllBuildingsOwnedByCharacter(PropertyId);
         ArrayList<String[]> tempresult;
 
         html.append("<table>");
         html.append("<tr><td class=\"sheading\">Estate buildings</td></tr>");
 
         if (retrieveAllBuildingsOwnedByCharacter.size() == 0) {
             html.append("<p>There are currently no buildings bought for this estate.</p>");
         } else {
             html.append("<th>Building ID</th>");
             html.append("<th>Building name </th>");
             html.append("<th>Income</th>");
             html.append("<th>Happiness</th>");
 
             for (int a = 0; a < retrieveAllBuildingsOwnedByCharacter.size(); a++) {
                 tempresult = handler.getBuildingQH().retrieveBuildingDetailsById(Integer.parseInt(retrieveAllBuildingsOwnedByCharacter.get(a)[0]));
                 html.append("<tr>");
                 html.append("<td>").append(retrieveAllBuildingsOwnedByCharacter.get(a)[0]).append("</td>");
                 html.append("<td>").append(tempresult.get(0)[1]).append("</td>");
                 html.append("<td>").append(tempresult.get(0)[6]).append("</td>");
                 html.append("<td>").append(tempresult.get(0)[10]).append("</td>");
                 html.append("</tr>");
             }
         }
 
         html.append("</table>");
 
         Calendar cal = Calendar.getInstance();
         int month = cal.get(cal.MONTH) + 1; //zero-based
 
         ArrayList<String[]> event = handler.getEventQH().getEvent(month, PropertyId);
 
         if (event != null) {
             if (event.size() > 0) {
                 html.append("<p class=\"sheading\">Events<p>");
                 html.append("<br/><hr/>");
 
                 for (int i = 0; i < event.size(); i++) {
                     html.append("<table>");
                     html.append("<tr><td class=\"ssheading\">Name:</td><td>").append(event.get(i)[2]).append("</td></tr>");
                     html.append("<tr><td class=\"ssheading\">Description:</td><td>").append(event.get(i)[3].replace("~", " ")).append("</td></tr>");
 
                     html.append("<tr>");
                     html.append("<td>Platinum Effect:</td><td>").append(event.get(i)[5]).append("</td>");
                     html.append("</tr>");
 
                     html.append("<tr>");
                     html.append("<td>Gold Effect:</td><td>").append(event.get(i)[6]).append("</td>");
                     html.append("</tr>");
 
                     html.append("<tr>");
                     html.append("<td>Silver Effect:</td><td>").append(event.get(i)[7]).append("</td>");
                     html.append("</tr>");
 
                     html.append("<tr>");
                     html.append("<td>Happiness Effect:</td><td>").append(event.get(i)[8]).append("</td>");
                     html.append("</tr>");
 
                     html.append("<tr>");
                     html.append("<td>Income Effect:</td><td>").append(event.get(i)[9]).append("%</td>");
                     html.append("</tr>");
                     html.append("</table>");
                     html.append("<hr/>");
                 }
             }
         }
 
         html.append("</body>");
         html.append("</html>");
 
         return html.toString();
     }
 
     @GET
     @Path("GlobalStatus")
     @Produces("text/html")
     public String GlobalStatus() {
         StringBuilder html = new StringBuilder();
         //retrieve all plots
         ArrayList<String[]> retrieveAllPlots = handler.getPlotQH().retrieveAllPlots();
         html.append("<html>");
         html.append("<head>");
 
         /*
          * CSS for page
          */
         html.append("<style type=\"text/css\">");
         html.append("body{"
                 + "font-family: \"century gothic\";"
                 + "background-color: white;"
                 + "border-right-width: 2px;"
                 + "border-bottom-width: 2px;"
                 + "border-left-width: 2px;"
                 + "border-top-width: 2px;"
                 + "border-top-style: solid;"
                 + "border-right-style: solid;"
                 + "border-bottom-style: solid;"
                 + "border-left-style: solid;"
                 + "border-top-color: #FF0000;"
                 + "border-right-color: #FF0000;"
                 + "border-bottom-color: #FF0000;"
                 + "border-left-color: #FF0000;"
                 + "}");
         html.append("h1{"
                 + "text-align: center;"
                 + "}");
         html.append("table{"
                 + "width: 90%;"
                 + "}");
         html.append("td{"
                 + "width: 50%;"
                 + "font-size: 10px;"
                 + "}");
         html.append("th{"
                 + "text-align: left;"
                 + "}");
         html.append(".ssheading{"
                 + "font-size: 12px;"
                 + "font-weight: bold;"
                 + "}");
         html.append(".NegIn{"
                 + "font-size: 12px;"
                 + "font-weight: bold;"
                 + "color: red;"
                 + "}");
         html.append("</style>");
         html.append("</head>");
 
         html.append("<body>");
         html.append("<h1>Global Status</h1>");
 
         for (int i = 0; i < retrieveAllPlots.size(); i++) {
             html.append("<table>");
             try {
                 retrieveAllPlots.get(i)[1].substring(0, retrieveAllPlots.get(i)[1].indexOf("&*&"));
                 html.append("<tr><td class=\"ssheading\">Owner:</td><td>").append(retrieveAllPlots.get(i)[1].substring(0, retrieveAllPlots.get(i)[1].indexOf("&*&"))).append("</td></tr>");
             } catch (Exception ex) {
                 html.append("<tr><td class=\"ssheading\">Owner:</td><td>").append(retrieveAllPlots.get(i)[1]).append("</td></tr>");
             }
             html.append("<tr><td>Plot Name:</td><td>").append(retrieveAllPlots.get(i)[18]).append("</td></tr>");
             if (Double.parseDouble(retrieveAllPlots.get(i)[8]) < 0) {
                 html.append("<tr><td>Plot Income:</td><td class=\"NegIn\">").append(retrieveAllPlots.get(i)[8]).append("</td></tr>");
             } else {
                 html.append("<tr><td>Plot Income:</td><td>").append(retrieveAllPlots.get(i)[8]).append("</td></tr>");
             }
             html.append("</table>");
 
             if (i < (retrieveAllPlots.size() - 1)) {
                 html.append("<hr/>");
             }
         }
 
         html.append("</body>");
         html.append("</html>");
 
         return html.toString();
     }
 }
