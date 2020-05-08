 /**
  * User: mischarohlederer
  * Date: 27.10.11
  * Time: 11:56
  */
 public class LBS_Roth {
     public static void main(String[] args) {
 // Standart Einstellungen
         String dbhost = "geo.informatik.fh-nuernberg.de";
         int dbport = 5432;
         String dbuser = "dbuser";
         String dbpasswd = "dbuser";
         String dbname = "deproDBMittelfranken";
 
         //System.out.println(ohm.roth.ohm.roth.lbs.distance.calcDist(new Position(-11.07043531536262,49.45043799363943), new Position(-11.0754185442121,49.45185742264561)));
 
        // ToDo: Verarbeiten von CLI Argumenten
        Application app = new Application(dbhost,dbport,dbuser,dbpasswd,dbname,"NBG",2);
         app.mainMenu();
     }
 
 }
