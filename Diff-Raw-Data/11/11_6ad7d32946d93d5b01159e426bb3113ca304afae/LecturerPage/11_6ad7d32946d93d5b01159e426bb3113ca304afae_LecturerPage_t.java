 package web;
 
 import java.sql.ResultSet;
 import log.ErrorLogger;
 import sql.Query;
 
 /**
  * Class for making a web page for displaying a lecturer from the database.
  * 
  * @author Stephen Fahy
  */
 public class LecturerPage {
     private static final String NAME_REPLACE = "#[[name]]#";
     private static final String DESCRIPTION_REPLACE = "#[[desc]]#";
     
     private static final String TITLE_TEMPLATE = 
             "<title>Lecturer Details: " + NAME_REPLACE + "</title>";
     private static final String BODY_TEMPLATE = 
             "<h1>" + NAME_REPLACE + "</h1>" + 
             "<div>" + DESCRIPTION_REPLACE + "</div>";
     
     private String title;
     private String body;
     
     /**
      * Constructor.
      * 
      * @param name The full name of the lecturer.
      */
     public LecturerPage(String name) {
         ResultSet results = Query.query("SELECT * FROM Lecturers WHERE " +
                 "name = '" + name + "';");
         
         try {
             results.next();
             String description = HTMLTransformer.toHTML(results.getString(2));
             
            title = TITLE_TEMPLATE.replace(NAME_REPLACE, name);
            body = BODY_TEMPLATE.replace(NAME_REPLACE, name);
            body = body.replace(DESCRIPTION_REPLACE, description);
         }
         catch(Exception e) {
             ErrorLogger.get().log(e.toString());
             e.printStackTrace();
             
             title = "<title>Database Error</title>";
             body = "<h1>Database Error</h1>";
         }
     }
     
     //getters
     public String getBody() {
         return body;
     }
     public String getTitle() {
         return title;
     }
 }
