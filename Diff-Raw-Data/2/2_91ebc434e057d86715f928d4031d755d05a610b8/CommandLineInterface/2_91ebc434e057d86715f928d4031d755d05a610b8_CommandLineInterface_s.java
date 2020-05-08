 package gd.app.cli;
 
 import java.awt.Dimension;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 
 import att.grappa.Graph;
 import att.grappa.GrappaPanel;
 import att.grappa.Parser;
 
 import gd.app.model.Table;
 import gd.util.ParamManager;
 import gd.app.util.GraphvizCmd;
 import gd.app.util.GrappaFrame;
 import gd.app.util.ToDotUtil;
 import gd.app.util.ToDotUtilException;
 import gd.hibernate.util.HibernateUtil;
 
 /**
  * Command Line Interface class, this class contains the main function to
  * compile the command line program.
  * 
  * @author Clément Sipieter <csipieter@gmail.com>
  * @version 0.1
  */
 public class CommandLineInterface {
 
     private static final String TITLE = "db2graph";
 
     private static String username = "";
     private static String password = "";
     private static String db_name = null;
     private static String sgbd_type = null;
     private static String host = "";
     private static String port = null;
     private static String output = null;
     private static boolean opt_show = false;
     private static GraphvizCmd gv_cmd = GraphvizCmd.DOT;
 
     /**
      * Main
      * 
      * @param args
      *            see --help.
      */
     public static void main(String[] args) {
 
         Session session = null;
         String dot = "";
         String dir = System.getProperty("user.dir") + "/";
 
         manageParams(args);
 
         try {
             session = HibernateUtil.openSession(sgbd_type, host, db_name,
                     username, password, port);
 
             Criteria c = session.createCriteria(Table.class);
             dot = ToDotUtil.convertToDot(c.list(), db_name);
 
             if (output == null && !opt_show) {
                 System.out.println(dot);
             } else {
                 String racine_file = (output == null) ? "out" : output
                         .substring(0, output.lastIndexOf('.'));
                 String url_dot_file = dir + racine_file + ".dot";
                 String url_dot_pos_file = dir + racine_file + "_pos.dot";
 
                 // Write a dot file
                 BufferedWriter bw = new BufferedWriter(new FileWriter(
                         url_dot_file));
                 bw.write(dot);
                 bw.close();
 
                 if (opt_show) {
                     // generate position with neato
                    int val = gv_cmd.exec("tt.dot", url_dot_pos_file);
                     if (val == 0) {
                         // affichage graphique
                         GrappaFrame frame = new GrappaFrame(new File(
                                 url_dot_pos_file), TITLE);
                         frame.setVisible(true);
                     }
                 } else if (output != null) {
                     // Generate a png image from the dot file
                     // @todo treat other image format
                     String url_image = dir + output;
 
                     // System call to graphviz
                     // @todo externalise next command
                     String neato_cmd = gv_cmd.toString() + " " + url_dot_file
                             + " -Tpng -o " + url_image;
                     Process process = Runtime.getRuntime().exec(neato_cmd);
                     if (process.waitFor() != 0)
                         System.err.println("Command neato not found or fail : "
                                 + neato_cmd);
 
                 }
 
             }
 
         } catch (IOException | HibernateException | InterruptedException
                 | ToDotUtilException e) {
             System.err.println(e.getMessage());
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             session.close();
         }
 
     }
 
     private static void manageParams(String args[]) {
         ParamManager param_manager = new ParamManager(args);
         String arg;
 
         try {
             while ((arg = param_manager.getNextParam()) != null) {
                 if (!ParamManager.isAnOptionName(arg)) {
                     if (sgbd_type == null) {
                         sgbd_type = ParamManager.getOptionName(arg);
                     } else {
                         db_name = ParamManager.getOptionName(arg);
                     }
                 } else {
                     String param = ParamManager.getOptionName(arg);
                     // @todo manage this next line width ParamManager
                     String value = "";
                     System.out.println("-" + param + "-");
 
                     if (!param.equals("show") && !param.equals("help")) {
                         value = param_manager.getNextParam();
                         if (value == null)
                             throw new Exception("wrong option : " + param);
                         else if (ParamManager.isAnOptionName(value))
                             throw new Exception("wrong value for " + param
                                     + " option");
                     }
 
                     switch (param) {
                         case "host":
                         case "h":
                             host = value;
                             break;
                         case "user":
                         case "u":
                             username = value;
                             break;
                         case "password":
                         case "p":
                             password = value;
                             break;
                         case "dbname":
                             db_name = value;
                             break;
                         case "sgbd_type":
 
                             break;
                         case "port":
                             port = value;
                             break;
                         case "cmd":
                         case "c":
                             gv_cmd = GraphvizCmd.getInstance(value);
                             break;
                         case "output":
                         case "o":
                             output = value;
                             break;
                         case "show":
                             opt_show = true;
                             break;
                         case "help":
                             printHelp();
                             System.exit(0);
                         default:
                             throw new Exception("wrong option : " + arg);
                     }
 
                 }
             }
 
             if (sgbd_type == null || db_name == null)
                 throw new Exception("Bad usage");
 
         } catch (Exception e) {
             System.err.println(e.getMessage());
             printHelp();
             System.exit(1);
         }
     }
 
     private static void printHelp() {
         System.out.println("Usage:\n"
                 + "gd [OPTION...] SGBD_NAME DATABASE_NAME\n" + "\n"
                 + "Options:\n" + "    -u, --user <USERNAME>\n"
                 + "        use this username.\n"
                 + "    -p, --password [PASSWORD]\n"
                 + "        use this password.\n" + "    -h, --host <HOST>\n"
                 + "        use this host.\n" + "    --port <PORT>\n"
                 + "        use this port.\n" + "    -o, --output <FILE_NAME>\n"
                 + "        generate an png image width graphviz\n"
                 + "    -c, --cmd <GRAPHVIZ_CMD>\n"
                 + "        choose your graphviz command (man graphviz).\n"
                 + "    --show \n"
                 + "        open a window with graph representation.\n"
                 + "    --help\n" + "        print this message.\n" + "\n");
     }
 
 }
