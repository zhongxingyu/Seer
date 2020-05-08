 import java.io.File;
 import java.util.Scanner;
 
 public class CMD {
     private static final int EXIT_NOW = 13;
     private static Scanner sc;
     private static File currentDir;
 
     public static void main(String[] args) {
         sc = new Scanner(System.in);
         int result = 0;
         cd(System.getProperty("user.dir"));
         do{
             showCurrentPath();
             String cmd = readCommand();
             result = executeCommand(cmd);
         }while(result != CMD.EXIT_NOW);
     }
 
     private static String readCommand() {
         return sc.next();
     }
 
     private static int executeCommand(String cmd) {
         String cad = cmd.toLowerCase();
         
         if(cad.equals("exit")){
             System.out.println("Saliendo...");
             return EXIT_NOW;
         }
         else if( cad.equals("mkdir")) mkdir();
         else if( cad.equals("newfile")) newFile(); 
         else if( cad.equals("cd")) cd();
        else if( cad.equals("cd..")) cd(currentDir.getParent());
         else if( cad.equals("dir")) dir();
         else if( cad.equals("delete")) delete();
         else if( cad.equals("move")) move();
         else if( cad.equals("rename")) rename();
         else System.out.print("\n'"+ cmd + "' no se reconoce como un comando valido.\n");
         
         sc.nextLine();
         return 0;
     }
 
     private static void showCurrentPath() {
         System.out.print(currentDir.getAbsolutePath() + " ");
     }
 
     private static void cd(String dir) {
         File nd = new File(dir);
         if( nd.exists() && nd.isDirectory())
             currentDir = nd;
     }
 
     private static void mkdir() {
         if(!sc.hasNext()){
             System.out.println("\nFaltan parametros.");
         }else{
             File f = new File(currentDir.getPath() + "/" + sc.next());
             try{
                 if( f.mkdir() ){
                     System.out.println("\nSE HA CREADO SATISTFACTORIAMENTE.\n");
                 }else{
                     System.out.println("No se creo. Ya existia?");
                 }
             }catch(Exception ex){
                 System.out.println("Error: " + ex.getMessage());
             }
         }
     }
 
     private static void newFile() {
         if(!sc.hasNext()){
             System.out.println("\nFaltan parametros.");
         }else{
             String name = sc.next();
             if(!name.contains(".")){
                 System.out.println("No escribio la extension.");
                 return;
             }
             File f = new File(currentDir.getPath() + "/" + name);
             try{
                 if( f.createNewFile() ){
                     System.out.println("\nSE HA CREADO SATISTFACTORIAMENTE.\n");
                 }else{
                     System.out.println("No se creo. Ya existia?");
                 }
             }catch(Exception ex){
                 System.out.println("Error: " + ex.getMessage());
             }
         }
     }
 
     private static void dir() {
         try{
             File files[] = currentDir.listFiles();
             for(File f: files){
                 String cont = "\t-" + f.getName();
                 if(f.isDirectory()){
                     System.out.printf("%-20s %s\n",cont, "[Directorio]");
                 }
                 if(f.isFile()){
                     System.out.printf("%-20s %-11s %d %s\n", cont, "[ARCHIVO]", f.length() , "bytes");
                 }
             }
         }catch(Exception ex){
             System.out.println("Error: " + ex);
         }
     }
     
     private static String getNextParametro(){
         if( sc.hasNext())
             return sc.next();
         System.out.println("\nFaltan Parametros.");
         return null;
     }
 
     private static void cd() {
         if(!sc.hasNext()){
             System.out.println("\nFaltan parametros.");
         }else{
             String path = sc.next();
             
             File f = new File(path);
             try{
                if( f.exists() ){
                   if(f.isDirectory() && f.isAbsolute()){
                       cd(path);
                       return;
                   }
                   System.out.println("\nDirectorio invalido\n");
                   return;
                 }
                 System.out.println("\nEl directorio no existe\n");               
             }catch(NullPointerException ex){
                 if(path.equals(".."))
                     System.out.println("\nYa esta en el root\n");
             }catch(Exception ex){
                 System.out.println("\nError: " + ex.getMessage() + "\n");
             }
         }
     }
 
     private static void delete() {
         String par = getNextParametro();
         if(par != null){
             File file = new File(currentDir.getPath() + "/" + par);
             if( file.exists()){
                 try{
                     if(file.delete()){
                         System.out.println("\n--SE BORRO SATISFACTORIAMENTE--\n");
                     }else{
                         System.out.println("\nNo se permite borrar este archivo o directorio\n");
                     }
                 }catch(Exception ex){
                     System.out.println("\nError: " + ex);
                 }
             }else{
                 System.out.println("\nNO EXISTE, NO SE PUEDE BORRAR\n");
             }
         }
     }
 
     private static void move() {
         String fileP = getNextParametro();
         String newDir = getNextParametro();
         if(fileP != null && newDir != null){
             File f = new File(currentDir.getPath() + "/" + fileP);
             File dir = new File(newDir);
             if(f.exists()){
                 if(dir.exists()){
                     File dest = new File(dir.getAbsoluteFile() + "/" + fileP);
                     if(f.renameTo(dest)){
                         System.out.println("\n--SE MOVIO EXITOSAMENTE--\n");
                     }else{
                         System.out.println("\n--NO SE PUDO MOVER--\n");
                     }
                 }else{
                     System.out.println("\nEl directorio '" + newDir + "' no existe.\n");
                 }
             }else{
                 System.out.println("\nEl archivo '" + fileP + "' no existe.\n");
             }
         }
     }
 
     private static void rename() {
         String par = getNextParametro();
         if( par != null){
             File file = new File(currentDir.getPath() + "/" + par);
             if( file.exists()){
                 System.out.print("\nNuevo Nombre:");
                 String newName = sc.next();
                 if( file.renameTo(new File(currentDir.getPath() + "/" + newName))){
                     System.out.println("\n--SE RENOMBRO SATISFACTORIAMENTE--\n");
                 }else{
                     System.out.println("\n--NO SE PUDO RENOMBRAR EL ARCHIVO '" + par + "' a '" + newName + "'.\n");
                 }
             }else{
                 System.out.println("\nEl archivo '" + par + "' no existe.\n");
             }
         }
     }
 }
