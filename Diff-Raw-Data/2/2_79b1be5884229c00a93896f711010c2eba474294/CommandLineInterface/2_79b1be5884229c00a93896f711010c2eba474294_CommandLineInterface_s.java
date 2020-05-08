 package gd.app.cli;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 
 import gd.app.model.Table;
 import gd.util.ImageFrame;
 import gd.util.ArgsManager;
 import gd.app.util.todot.GraphvizCmd;
 import gd.app.util.todot.ToDotUtil;
 import gd.app.util.todot.ToDotUtilException;
 import gd.hibernate.util.HibernateUtil;
 
 /**
  * Command Line Interface class, this class contains the main function to
  * compile the command line program.
  * 
  * @author Clément Sipieter <csipieter@gmail.com>
  * @version 0.1
  */
 public class CommandLineInterface
 {
 
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
    *          see --help.
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
 
                 // Write a dot file
                 BufferedWriter bw = new BufferedWriter(new FileWriter(
                         url_dot_file));
                 bw.write(dot);
                 bw.close();
 
                 // Generate a png image from the dot file
                 // @todo treat other image format
                 String url_image = dir + output;
                 if (output != null) {
 
                     // System call to graphviz
                     if (ToDotUtil.dotToSvg(gv_cmd, url_dot_file, url_image) != 0)
                         System.err
                                 .println("Erreur lors de la génération du svg.");
 
                 }
                 if (opt_show) {
                     String png_image = dir + racine_file + ".png";
                     // System call to graphviz
                     if (ToDotUtil.dotToPng(gv_cmd, url_dot_file, png_image) != 0)
                         System.err
                                 .println("Erreur lors de la génération du png.");
 
                     new ImageFrame(png_image, TITLE);
                 }
 
             }
 
             session.close();
 
         } catch (Exception e) {
           System.err.println(e.getMessage());
         } finally {
         }
 
     }
 
   private static void manageParams(String args[])
   {
     ArgsManager param_manager = new ArgsManager(args);
     String arg;
 
     try
       {
         while ((arg = param_manager.getNextParam()) != null)
           {
             if (!ArgsManager.isAnOptionName(arg))
               {
                 if (sgbd_type == null)
                   {
                     sgbd_type = ArgsManager.getOptionName(arg);
                   }
                 else
                   {
                     db_name = ArgsManager.getOptionName(arg);
                   }
               }
             else
               {
                 String param = ArgsManager.getOptionName(arg);
                 // @todo manage this next line width ArgsManager
                 String value = "";
 
                 if (!param.equals("show") && !param.equals("help"))
                   {
                     value = param_manager.getNextParam();
                     if (value == null)
                       throw new Exception("wrong option : " + param);
                     else if (ArgsManager.isAnOptionName(value))
                       throw new Exception("wrong value for " + param
                           + " option");
                   }
 
                 if (param.equals("host") || param.equals("h"))
                   host = value;
                 else if (param.equals("user") || param.equals("u"))
                   username = value;
                 else if (param.equals("password") || param.equals("p"))
                   password = value;
                 else if (param.equals("dbname"))
                   db_name = value;
                 else if (param.equals("sgbd_type"))
                   {
                   }
                 else if (param.equals("port"))
                   port = value;
                 else if (param.equals("cmd") || param.equals("c"))
                   gv_cmd = GraphvizCmd.getInstance(value);
                else if (param.equals("output"))
                   output = value;
                 else if (param.equals("show"))
                   opt_show = true;
                 else if (param.equals("help"))
                   {
                     printHelp();
                     System.exit(0);
                   }
                 else
                   throw new Exception("wrong option : " + arg);
 
               }
           }
 
         if (sgbd_type == null || db_name == null)
           throw new Exception("Bad usage");
 
       }
     catch (Exception e)
       {
         System.err.println(e.getMessage());
         printHelp();
         System.exit(1);
       }
   }
 
   private static void printHelp()
   {
     System.out.println("Usage:\n" + "gd [OPTION...] SGBD_NAME DATABASE_NAME\n"
         + "\n" + "Options:\n" + "    -u, --user <USERNAME>\n"
         + "        use this username.\n" + "    -p, --password [PASSWORD]\n"
         + "        use this password.\n" + "    -h, --host <HOST>\n"
         + "        use this host.\n" + "    --port <PORT>\n"
         + "        use this port.\n" + "    -o, --output <FILE_NAME>\n"
         + "        generate an png image width graphviz\n"
         + "    -c, --cmd <GRAPHVIZ_CMD>\n"
         + "        choose your graphviz command (man graphviz).\n"
         + "    --show \n"
         + "        open a window with graph representation.\n" + "    --help\n"
         + "        print this message.\n" + "\n");
   }
 
 }
