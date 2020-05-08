 package eionet.eunis.scripts;
 
 import java.util.ResourceBundle;
 
 import ro.finsiel.eunis.dataimport.TabScripts;
 import ro.finsiel.eunis.utilities.SQLUtilities;
 
 public class PostImportScriptsCmd {
 
     /**
      * @param args
      */
     public static void main(String[] args) {

         if (args.length == 0) {
             System.out.println("Missing argument!");
             System.out
             .println("Possible first arguments are: sites, species_tab, sites_tab, habitats_tab, linkeddata_tab, taxonomy_tree");
         } else {
             try {
                 ResourceBundle props = ResourceBundle.getBundle("jrf");
                 String dbDriver = props.getString("mysql.driver");
                 String dbUrl = props.getString("mysql.url");
                 String dbUser = props.getString("mysql.user");
                 String dbPass = props.getString("mysql.password");
 
                 SQLUtilities sql = new SQLUtilities();
                 sql.Init(dbDriver, dbUrl, dbUser, dbPass);
 
                 System.out.println(args[0] + " command started!");
 
                 if (args[0].equals("sites")) {
                     sql.runPostImportSitesScript(true);
                 } else if (args[0].equals("taxonomy_tree")) {
                     sql.reconstructTaxonomyTree();
                 } else {
                     TabScripts scripts = new TabScripts();
                     scripts.Init(dbDriver, dbUrl, dbUser, dbPass, true);
                     if (args[0].equals("species_tab")) {
                         scripts.setTabSpecies();
                     } else if (args[0].equals("sites_tab")) {
                         scripts.setTabSites();
                     } else if (args[0].equals("habitats_tab")) {
                         scripts.setTabHabitats();
                     } else if (args[0].equals("linkeddata_tab")) {
                         scripts.setSpeciesLinkedDataTab();
                     }
                 }
 
                 System.out.println(args[0] + " command successfully finished!");
 
             } catch (Exception e) {
                 System.out.println("Error occured: " + e.getMessage());
                 e.printStackTrace();
             }
         }
     }
 }
