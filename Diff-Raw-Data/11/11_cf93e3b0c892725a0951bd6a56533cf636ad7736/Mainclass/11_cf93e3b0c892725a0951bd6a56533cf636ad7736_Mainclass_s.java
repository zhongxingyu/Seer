 package Login;
 
 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 /**
  *
  * @author Reed
  */
 public class Mainclass {
     //Versión
     public final static String title = "MineLogin";
    public final static String version = "V4.1.0";
     public static Map<String, Thread> hilos;
     public static Splash init;
     public static PrintStream err;
     public static ErrStream error;
     
     public static void setThreadError(){
         error = new ErrStream("Errores");
         error.start();
         hilos.put("Errores", error);
     }
     
     public static void suspend(Vista2 see){
         Iterator<String> it = hilos.keySet().iterator();
         String temp;
         while(it.hasNext()){
             temp = it.next();
             if (!temp.equals("Systray")){
                 Thread thread = hilos.get(temp);
                 try {
                     if (thread.isAlive()){
                         thread.wait();
                     } else{
                         System.out.println("Thread " + thread.getName() + " is not active!");
                     }
                 } catch (InterruptedException ex) {
                     ex.printStackTrace(err);
                 }
             }
         }
     }
     public static void threads(){
         System.out.println("Name  IsActive IsInterruped IsDaemon");
         Iterator<String> it = hilos.keySet().iterator();
         String temp;
         while(it.hasNext()){
             temp = it.next();
             Thread thread = hilos.get(temp);
             System.out.println(thread.getName() + "  " + thread.isAlive() + "  "
                     + thread.isInterrupted() + "  " + thread.isDaemon());
         }
         System.out.println(Thread.currentThread().getName() + "  " + Thread.currentThread().isAlive()
                  + "  " + Thread.currentThread().isInterrupted() + "  " + Thread.currentThread().isDaemon());
     }
     /**
      * Method to synchronize the local files with the server. ALWAYS the server files have priority.
      */
     public static void synchAllFiles(){
         File base = new File(Sources.path(Sources.DirData() + Sources.sep() + Sources.DirNM));
         File tmpDir = new File(base.getAbsolutePath() + Sources.sep() + "TMP");
         if (!base.exists()){
             System.err.println("Syncronization is disabled!");
             return;
         }
         if (!tmpDir.exists()){
             System.out.println("Creating temporally directory");
             tmpDir.mkdirs();
         }
         List <File> names = new ArrayList<File>();
         List <File> tmp = new ArrayList<File>();
         File[] ficheros = base.listFiles();
         for (int x = 0; x < ficheros.length; x++){
             if (!ficheros[x].isDirectory() && !ficheros[x].getName().equals(Sources.lsNM)){
                 names.add(new File(base.getAbsolutePath() + Sources.sep() + ficheros[x].getName()));
             }
         }
         if (names.isEmpty()){
             System.out.println("No base files to synchronize!");
             return;
         }
         for (int i = 0; i < names.size(); i++){
             tmp.add(new File(tmpDir.getAbsolutePath() + Sources.sep() + nameFiles(names.get(i).getName())));
             if (!Sources.download(tmpDir.getAbsolutePath() + Sources.sep() + nameFiles(names.get(i).getName())
                     , nameFiles(names.get(i).getName()))){
                 System.err.println("[ERROR]Sync interrupted!");
             }
         }
         BufferedReader bf = null;
         PrintWriter pw = null;
         for (int i = 0; i < names.size(); i++){
              try{
                  bf = new BufferedReader(new FileReader(tmp.get(i)));
                  pw = new PrintWriter(names.get(i));
                  String temp;
                  while((temp = bf.readLine()) != null){
                      pw.println(temp);
                  }
                  bf.close();
                  pw.close();
                  tmp.get(i).delete();
              } catch (IOException ex){
                 ex.printStackTrace(err);
              }
         }
         tmp.clear();
         names.clear();
         tmp = null;
         names = null;
         System.gc();
     }
     /**
      * Method to synchronize one file with the server. This override the server file.
      * @param sync The local file to synchronize.
      */
     public static void synch(File sync){
         File tmpDir = new File(Sources.path(Sources.DirData() + Sources.sep() + Sources.DirNM + Sources.sep() 
                 + Sources.DirTMP));
         if (!tmpDir.exists()){
             System.out.println("Creating temporally directory");
         }
         File tmp = new File(tmpDir.getAbsolutePath() + Sources.sep() + nameFiles(sync.getName()));
         System.out.print("Creating temp file to synchronize... ");
         try{
             tmp.createNewFile();
             PrintWriter pw = new PrintWriter(tmp);
             BufferedReader bf = new BufferedReader(new FileReader(sync));
             String temp = null;
             while((temp = bf.readLine()) != null){
                 pw.println(temp);
             }
             pw.close();
             bf.close();
             System.out.println("OK");
         } catch (IOException ex){
             System.out.println("FAILED");
             ex.printStackTrace(err);
             System.err.println("[ERROR]Failed to synchronize with the server!");
             tmp.delete();
             JOptionPane.showMessageDialog(null, "Error al sincronizarse con el servidor. Comprueba que "
                     + "estás conectado a internet");
             return;
         }
         if (!Sources.upload(tmp.getAbsolutePath(), tmp.getName())){
             JOptionPane.showMessageDialog(null, "Error al conectar con el servidor");
             System.err.println("[ERROR]Failed to synchronize with the server!");
         }
         tmp.delete();
     }
     private static String nameFiles(String name){
         StringTokenizer token = new StringTokenizer(name, "_");
         StringBuilder str = new StringBuilder();
         String tmp = null;
         while(token.hasMoreTokens()){
             tmp = token.nextToken();
             if (!tmp.contains(".")){
                 int A = Integer.parseInt(tmp);
                 char B = (char) A;
                 str.append(B);
             } else{
                 str.append(tmp);
             }
         }
         return str.toString();
     }
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
         init = new Splash();
         Thread t = new Thread(init, "Splash");
         t.start();
         System.out.println("Loading files..."); //Se crea el Splash
         hilos = new HashMap<String, Thread>();
         hilos.put("Splash", t);
         System.out.print("Setting new ErrorStream..."); //Se crea el nuevo hilo de errores
         setThreadError();
         System.out.println("OK");
         System.out.print("Setting OS... "); //Se implementa el sistema operativo
         Sources.setOS();
         System.out.println("OK");
         System.out.println("OS: " + Sources.OS);
         try {
             Thread.sleep(500);
         } catch (InterruptedException ex) {
             ex.printStackTrace(err);
         }
         if (!Sources.OS.equals("windows") && !Sources.OS.equals("linux")){ //Si el OS no es soportado lanzamos error
             System.err.println("[ERROR]Found operative system not supported! Exiting system!");
             JOptionPane.showMessageDialog(null, "Your operative system is not supported! Only are supported"
                     + " Windows and Linux.\nOperative system in this computer: " + Sources.OS + "\nIf you are running one "
                     + "of these operative systems, contact with me: Infernage");
             Debug de = new Debug(null, true);
             de.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
             de.setLocationRelativeTo(null);
             de.setVisible(true);
             System.exit(9);
         }
         System.out.println("Checking for outdated config versions..............");
         Outdated.checkAll();
         File mine = new File(Sources.path(Sources.DirMC));
         System.out.print("Checking minecraft state... ");
         if (mine.exists()){
             File[] mines = mine.listFiles();
             File infer = new File(mine.getAbsolutePath() + Sources.sep() + Sources.infernage());
             if ((mines.length < 7) || !infer.exists()){ /*Si la instalación del minecraft está fallida
              * o si no es la instalación de este login
              */
                 System.out.println("FAILED");
                 System.out.println("[Openning Installer]");
                 Installer.Vista.main(args);
                 return;
             }
         } else{ //Si el minecraft no está instalado
             System.out.println("FAILED");
             System.out.println("[Openning Installer]");
             Installer.Vista.main(args);
             return;
         }
         File dat = new File(Sources.path(Sources.DirData()));
         if (!dat.exists()){ //Si los datos no existen
             System.out.println("FAILED");
             System.out.println("[Openning Installer]");
             Installer.Vista.main(args);
             return;
         } else{
             File[] datas = dat.listFiles();
             if (datas.length < 1){ //Si los datos creados están corruptos
                 System.out.println("FAILED");
                 System.out.println("[Openning Installer]");
                 Installer.Vista.main(args);
                 return;
             }
         }
         //Comprobamos el estado de los jars
         System.out.println("OK");
         System.out.print("Checking source files... ");
         File runner = new File(Sources.path(Sources.DirData() + Sources.sep() + Sources.Dirfiles 
                 + Sources.sep() + Sources.jar));
         File runnerlib = new File(Sources.path(Sources.DirData() + Sources.sep() + Sources.Dirfiles 
                 + Sources.sep() + Sources.Dirlibs));
         File actual = new File(Sources.path(Sources.DirData() + Sources.sep() + Sources.Dirfiles 
                 + Sources.sep() + Sources.jar));
         File actuallib = new File(Sources.path(Sources.DirData() + Sources.sep() + Sources.Dirfiles 
                 + Sources.sep() + Sources.Dirlibs));
         if (!runner.exists() && !runnerlib.exists()){
             System.out.print("FAILED\nExporting source files... ");
             File dir = new File(Sources.path(Sources.DirData() + Sources.sep() + Sources.Dirfiles));
             dir.mkdirs();
             try{
                 copy(actual, runner);
                 copyDirectory(actuallib, runnerlib);
                 System.out.println("OK");
             } catch (IOException ex){
                 System.out.println("FAILED");
                 ex.printStackTrace(err);
             }
         } else if (runner.exists() && runnerlib.exists()){
             System.out.println("OK");
         } else if (!runner.exists() && runnerlib.exists()){
             System.out.print("FAILED\nExporting source files... ");
             try{
                 copy(actual, runner);
                 System.out.println("OK");
             } catch (IOException ex){
                 System.out.println("FAILED");
                 ex.printStackTrace(err);
             }
         } else if (runner.exists() && !runnerlib.exists()){
             System.out.print("FAILED\nExporting source files...");
             try{
                 copyDirectory(actuallib, runnerlib);
                 System.out.println("OK");
             } catch (IOException ex){
                 System.out.println("FAILED");
                 ex.printStackTrace(err);
             }
         }
         File fich = new File(Sources.path(Sources.DirData()));
         fich.mkdirs();
         File fichero = new File (Sources.path(Sources.DirData() + Sources.sep() + Sources.bool));
         //Controlamos si es la primera vez que se ejecuta y si hay registro o no
         String boo = "";
         System.out.println("Checking files... OK");
         if (fichero.exists()){
             try{
                 BufferedReader bf = new BufferedReader (new FileReader (fichero));
                 boo = bf.readLine();
             } catch (IOException ex){
                 ex.printStackTrace(err);
             }
             if (boo.equals("true")){
                 //Si en el fichero hay un true, significa que ya se ha registrado y abrimos la Vista2
                 System.out.println("[Openning Login file]");
                 Vista2 vista = new Vista2();
                 vista.setIconImage(new ImageIcon(Sources.path(Sources.DirMC + Sources.sep() + "5547.png")).getImage());
                 vista.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 vista.setTitle(title + " " + version);
                 vista.setLocationRelativeTo(null);
                 vista.setVisible(true);
                 vista.pack();
             } else{
                 //Sino, no se ha registrado, y abrimos la Vista
                 System.out.println("[Openning Register file]");
                 Vista.main(Sources.path(Sources.DirData() + Sources.sep()), fichero.getAbsolutePath(), false);
             }
         } else{
             //Si no existe el fichero, lo creamos y escribimos en él false
             try{
                 fichero.createNewFile();
                 PrintWriter pw = new PrintWriter(fichero);
                 pw.print("false");
                 pw.close();
             } catch (IOException ex){
                 ex.printStackTrace(err);
             }
             System.out.println("[Openning Register file]");
             Vista.main(Sources.path(Sources.DirData()), fichero.getAbsolutePath(), false);
         }
     }
     //Borrar fichero o directorio
     public static void borrarFichero (File fich){
         File[] ficheros = fich.listFiles();
         for (int x = 0; x < ficheros.length; x++){
             if (ficheros[x].isDirectory()){
                 borrarFichero(ficheros[x]);
             }
             ficheros[x].delete();
         }
     }
     //Copiar directorio de un sitio a otro
     public static void copyDirectory(File srcDir, File dstDir) throws IOException {
         if (srcDir.isDirectory()) { 
             if (!dstDir.exists()) { 
                 dstDir.mkdir(); 
             }
              
             String[] children = srcDir.list(); 
             for (int i=0; i<children.length; i++) {
                 copyDirectory(new File(srcDir, children[i]), 
                     new File(dstDir, children[i])); 
             } 
         } else { 
             copy(srcDir, dstDir); 
         } 
     }
     //Copiar fichero de un sitio a otro
     public static void copy(File src, File dst) throws IOException { 
         InputStream in = new FileInputStream(src); 
         OutputStream out = new FileOutputStream(dst);
         byte[] buf = new byte[4096]; 
         int len; 
         while ((len = in.read(buf)) > 0) { 
             out.write(buf, 0, len); 
         } 
         in.close(); 
         out.close(); 
     }
 }
