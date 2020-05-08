 package Login;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import java.awt.Desktop;
 import java.io.*;
 import java.net.*;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import net.lingala.zip4j.core.ZipFile;
 /**
  *
  * @author Reed
  */
 public class Updater extends Thread{
     private boolean data, error = false;
     private String link;
     private String path;
     private String name = "Update.zip";;
     public Installer.Worker work;
     public JProgressBar prog;
     public JLabel label;
     public boolean init = false, started = false, finish = false;
     //Creamos el actualizador con el link de la nueva versión como parámetro
     public Updater (){
         super("Updater");
        path = Sources.path(Sources.Directory.DirData()) + File.separator + "Update";
     }
     public void init(String host, boolean isData){
         data = isData;
         link = host;
         init = true;
     }
     public void init (String host, JProgressBar bar, JLabel eti){
         init(host, true);
         prog = bar;
         label = eti;
     }
     
     private String getFileName(URL url) {
         String fileName = url.getFile();
         return fileName.substring(fileName.lastIndexOf('/') + 1);
     }
     private void per (int size, int downloaded){
         size = size/1048576;
         downloaded = downloaded/1048576;
         String temp = downloaded + "MBytes/" + size + "MBytes";
         prog.setString(temp);
     }
     private void descargar(JProgressBar bar){
         prog = bar;
         descargar();
     }
     //Método de descarga
     public void descargar(){
         RandomAccessFile file = null;
         InputStream stream = null;
         try {
             Thread.sleep(1000);
             System.out.print("Transfering...");
             URL url = new URL(link);
             // Abrimos la conexión a la URL
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             System.out.print("...");
             // Especificamos la porción que queremos descargar
             connection.setRequestProperty("Range", "bytes=" + 0 + "-");
             System.out.print("...");
             // Conectamos al servidor
             connection.connect();
             String path = Sources.Prop.getProperty("user.data") + File.separator + name;
             System.out.print("...");
             // Abrimos el archivo
             file = new RandomAccessFile(path, "rw");
             file.seek(0);
             System.out.print("...");
             //Obtenemos el stream de la URL
             stream = connection.getInputStream();
             int size = connection.getContentLength();
             prog.setMaximum(size);
             //Creamos un array de bytes
             byte[] buffer = new byte[size];
             //Indicamos cuantos se van a leer cada vez
             int read = stream.read(buffer);
             int offset = 0;
             System.out.print("...");
             while (read > 0) {
                 offset += read;
                 per(size, offset);
                 prog.setValue(offset);
                 // Escribimos los bytes en el fichero
                 file.write(buffer, 0, read);
                 read = stream.read(buffer);
             }
             //Cerramos los sockets
             stream.close();
             file.close();
             System.out.println("... OK");
         } catch (Exception e) {
             System.out.println("... FAILED");
             if (!data){
                 Sources.fatalException(e, "Download crashed!", 4);
             } else{
                 Sources.exception(e, e.getMessage());
                 Sources.Init.multiGUI.reInit();
             }
             error = true;
         }
     }
     //Método de descompresión
     private void descomprimir(){
         System.out.print("Decompressing...");
         //Creamos la carpeta donde van a ir los archivos
         String zipper = Sources.Prop.getProperty("user.data") + File.separator + name;
         File zip = new File(zipper);
         zip.deleteOnExit();
         File mine = new File(path);
         if (mine.exists()){
             Sources.IO.borrarFichero(mine);
         }
         System.out.print("...");
         mine.mkdirs();
         try {
             ZipFile file = new ZipFile(new File(zipper));
             file.extractAll(path);
             //Abrimos el comprimido
             /*ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(zipper)));
             ZipEntry entrada;
             System.out.print("...");
             //Vamos cogiendo cada vez la siguiente entrada
             while ((entrada = zip.getNextEntry()) != null){
                 System.out.print("...");
                 boolean direc = false;//Comprobamos si es un directorio o no
                 //Separamos los nombres por el separador "/"
                 StringTokenizer token = new StringTokenizer(entrada.getName(), "/");
                 //Creamos una lista con todo el path
                 List<String> lista = new ArrayList<String>();
                 //Separamos el path y lo añadimos a la lista
                 while (token.hasMoreTokens()){
                     String A = token.nextToken();
                     if (A != null){
                         lista.add(A);
                     }
                 }
                 //Comprobamos si es un fichero o un directorio
                 if (entrada.getName().endsWith("/")){
                     direc = true;
                 }
                 //Cambiamos el tipo de separación de carpetas
                 StringBuilder build = new StringBuilder(path);
                 for (int i = 0; i < lista.size(); i++){
                     build.append(File.separator).append(lista.get(i));
                 }
                 String filero = build.toString();
                 File fich = new File(filero);
                 //Si es un directorio, creamos la carpeta
                 if (direc){
                     fich.mkdirs();
                 } else{//Sino, traspasamos el archivo a su destino
                     FileOutputStream salida = new FileOutputStream(fich);
                     int leido;
                     byte [] buffer = new byte[4096];
                     while ((leido = zip.read(buffer)) > 0){
                         salida.write(buffer, 0, leido);
                     }
                     //Cerramos todos los escuchadores
                     salida.close();
                 }
                 zip.closeEntry();
             }
             zip.close();*/
             System.out.println("... OK");
         } catch (Exception ex) {
             System.out.println("... FAILED");
             if (!data){
                 Sources.fatalException(ex, "Error al desencriptar la actualización.", 4);
             } else{
                 Sources.exception(ex, ex.getMessage());
             }
             error = true;
         }
         zip.delete();
     }
     //Método de ejecución de Main Instalador
     private void exec(){
         System.out.print("Openning new filesystem... ");
             //Por último ejecutamos el nuevo login
         if (!data){
             File old = new File(Sources.Prop.getProperty("user.data") + File.separator + "Update" 
                     + File.separator + Sources.Files.jar(false));
             File next = new File(Sources.Prop.getProperty("user.data") + File.separator + "minecraft.jar");
             if (next.exists()){
                 next.delete();
             }
             try{
                 Sources.IO.copy(old, next);
                 old.delete();
             } catch (Exception ex){
                 Sources.Init.error.setError(ex);
             }
             System.out.println("OK");
             temp();
             return;
         }
         System.out.println("OK");
         prog.setVisible(true);
         prog.setString("Aplicando instalación...");
         prog.setMaximum(100);
         prog.setMinimum(0);
         prog.setValue(0);
         System.out.println("Applying installation...");
         String instance = Sources.checkInstance("Default");
         work = Sources.Init.work;
         work.init(prog, instance, label);
         work.execute();
         File dst = new File(Sources.Prop.getProperty("user.instance") + File.separator + instance + 
                 File.separator + ".minecraft");
         Executer exe = new Executer(dst);
         exe.setDaemon(true);
         Sources.Init.hilos.put("Installer", exe);
         while(!work.isDone() && !work.isCancelled()){
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {
                 Sources.Init.error.setError(ex);
             }
         }
         exe.out();
         File tmp = new File(path);
         Sources.IO.borrarFichero(tmp);
         tmp.delete();
     }
     public void temp(){
         try {
             System.out.println("[>Executing new process and exiting<]");
             Desktop d = Desktop.getDesktop();
             d.open(new File(Sources.Prop.getProperty("user.data") + File.separator + 
                     Sources.Directory.Dirfiles + File.separator + "Temporal.jar"));
         } catch (IOException ex) {
             Sources.Init.error.setError(ex);
         } finally{
             System.exit(0);
         }
     }
     //Método de ejecución
     @Override
     public void run(){
         started = true;
         if (prog == null){
             descargar(Vista2.jProgressBar1);//Descargamos los archivos necesarios
         } else{
             descargar();
         }
         if (!error){
             descomprimir();
         }//Los descomprimimos
         if (!error){
             exec();
         }//Ejecutamos el main
         finish = true;
     }
 }
