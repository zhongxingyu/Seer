 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.univnantes.atal.web.trashnao;
 
 import au.com.bytecode.opencsv.CSVReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mog
  */
 public class Fetch extends HttpServlet {
 
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
         URL url = new URL("http://data.nantes.fr/api/publication/"
                 + "JOURS_COLLECTE_DECHETS_VDN/JOURS_COLLECTE_DECHETS_VDN_STBL/"
                 + "content/?format=csv");
        CSVReader reader = new CSVReader(new InputStreamReader(url.openStream(), "UTF-8"));
         String[] nextLine;
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         out.println("<html>");
         out.println(" <head>");
         out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
         out.println("  <title>JSP Page</title>");
         out.println(" </head>");
         out.println(" <body>");
         StringBuilder correct = new StringBuilder("<table>")
                 .append("<tr>")
                 .append("<td>Rue</td>")
                 .append("<td>Type</td>")
                 .append("<td>Multiple</td>")
                 .append("<td>Obs</td>")
                 .append("<td>Bleu</td>")
                 .append("<td>Jaune</td>")
                 .append("<td>Obs</td>")
                 .append("</tr>");
         StringBuilder incorrect = new StringBuilder(correct);
         Pattern triSac = Pattern.compile("(?ui)tri'sac|centre-ville");
         while ((nextLine = reader.readNext()) != null) {
             if (nextLine.length < 13) {
                 incorrect.append("trop court!!");
             } else {
                 String street = nextLine[1],
                         type = nextLine[7],
                         multiple = nextLine[8],
                         obs_type = nextLine[9],
                         blue = nextLine[10],
                         yellow = nextLine[11],
                         obs_day = nextLine[12];
                 Boolean blueAndYellows = true,
                         singleCollect = true;
                 Matcher triSacMatcher = triSac.matcher(type);
                 if (triSacMatcher.find()) {
                     blueAndYellows = true;
                     singleCollect = true;
                 } else if (type.equals("bacs bleus et bacs jaunes")
                         || type.equals("zone_transition")) {
                     blueAndYellows = true;
                     singleCollect = false;
                 } else if (type.equals("bacs bleus")) {
                     blueAndYellows = false;
                     singleCollect = true;
                 } else {
                     blueAndYellows = false;
                     singleCollect = false;
                 }
                 if (!blueAndYellows && !yellow.isEmpty()
                         || !singleCollect && (yellow.isEmpty() || blue.isEmpty())
                         || !blueAndYellows && blue.isEmpty()
                         || !blueAndYellows && !singleCollect) {
                     incorrect.append("<tr>")
                             .append("<td>").append(street).append("</td>")
                             .append("<td>").append(type).append("</td>")
                             .append("<td>").append(multiple).append("</td>")
                             .append("<td>").append(obs_type).append("</td>")
                             .append("<td>").append(blue).append("</td>")
                             .append("<td>").append(yellow).append("</td>")
                             .append("<td>").append(obs_day).append("</td>")
                             .append("</tr>");
                 } else {
                     correct.append("<tr>")
                             .append("<td>").append(street).append("</td>")
                             .append("<td>").append(type).append("</td>")
                             .append("<td>").append(multiple).append("</td>")
                             .append("<td>").append(obs_type).append("</td>")
                             .append("<td>").append(blue).append("</td>")
                             .append("<td>").append(yellow).append("</td>")
                             .append("<td>").append(obs_day).append("</td>")
                             .append("</tr>");
                 }
             }
         }
         correct.append("</table>");
         incorrect.append("</table>");
         out.println("  <div style=\"background-color: green\">");
         out.println("   Corrects:");
         out.println(correct.toString());
         out.println("  </div>");
         out.println("  <div style=\"background-color: red\">");
         out.println("   Incorrects:");
         out.println(incorrect.toString());
         out.println("  </div>");
         out.println(" </body>");
         out.println("</html>");
     }
 }
