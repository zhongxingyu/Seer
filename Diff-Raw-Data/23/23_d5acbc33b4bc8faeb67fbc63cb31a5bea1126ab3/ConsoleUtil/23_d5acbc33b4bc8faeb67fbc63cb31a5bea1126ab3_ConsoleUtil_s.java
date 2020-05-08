 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.xhubacubi.jarhalla.client.cli.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.xhubacubi.alicante.core.SearchFilesUtil;
 import org.xhubacubi.alicante.core.jar.JarUtil;
 import org.xhubacubi.jarhalla.client.dao.bean.IArray;
 import org.xhubacubi.jarhalla.client.dao.bean.Repo;
 import org.xhubacubi.jarhalla.client.services.DemiurgoFacade;
 import org.xhubacubi.jarhalla.client.ui.JMain;
 import org.xhubacubi.jarhalla.client.util.FileUtil;
 import org.xhubacubi.jarhalla.client.util.StringUtil;
 
 /**
  *
  * @author rugi
  */
 public class ConsoleUtil {
 
     private static final int INIT_SIZE = 100;
     private int resultSize = INIT_SIZE;
     private boolean buscando = false;
 
     public void addRepo(String command) throws IOException {
         System.out.println("[addRepo]----------------------------------------------------");
         String[] tokens = command.split("\\s+");
         String dir = null;
         //si es un solo token mostramos ayuda
         if (tokens.length == 1) {
             System.out.println("[addRepo]addRepo:");
             System.out.println("[addRepo]           Agrega un repositorio");
             System.out.println("[addRepo]                  addRepo <ruta_directorio>");
             System.out.println("[addRepo]Donde: ");
             System.out.println("[addRepo]           <ruta_directorio> ruta completa al directorio que se quiere agregar como repositorio.");
             System.out.println("[addRepo]           Al terminar puedes validar la opeacion con: <<showRepos>>");
             System.out.println("[addRepo]----------------------------------------------------");
             return;
         }
         if (tokens.length > 2) {
             System.out.println("[addRepo]Se recibieron mas parametros de los necesarios. ");
             System.out.println("[addRepo]Se toma el requerido y se ignora el resto ");
         }
         dir = validateDir(tokens[1]);
         //Validamos que sea un path valido
         if (!new File(dir).isDirectory()) {
             System.out.println("[addRepo] " + dir + " No es un directorio valido.");
             System.out.println("[addRepo]----------------------------------------------------");
             return;
         }
         //Si ya existe, se avisa que debe eliminarlo primero.
         Repo r = DemiurgoFacade.getInstance().getService().getRepoByPath(dir);
         if (r != null) {
             System.out.println("[addRepo] " + dir + " Ya existe como repositorio");
             System.out.println("[addRepo]----------------------------------------------------");
             return;
         }
         System.out.println("[addRepo] Agregando: " + dir);
         buscando = true;
         //------------- Proceso de indexado
         SearchTaskThread st = new SearchTaskThread();
         (new Thread(st)).start();
         //Buscamos si existen jars
         SearchFilesUtil sfu = new SearchFilesUtil();
         List<String> jarsPath = sfu.getPathFilesInFolder(dir, "(.*?).jar");
         String idRepo;
         if (jarsPath.size() > 0) {
             idRepo = DemiurgoFacade.getInstance().getService().addRepo(dir);
         } else {
             System.out.println("[addRepo] No existen jars en esta ruta: " + dir);
            System.out.println("[addRepo]----------------------------------------------------");
             return;
         }
         int k = 0;
         Iterator<String> pathI = jarsPath.iterator();
         StringBuilder pathS = new StringBuilder();
         while (pathI.hasNext()) {
             k++;
             pathS.delete(0, pathS.length());
             pathS.append(pathI.next());
             System.out.println("[addRepo]Analizando " + k + " de " + jarsPath.size());
             System.out.println("[addRepo]\n");
             System.out.println("[addRepo]\t" + pathS.toString());
             System.out.println("[addRepo]\n");
 
             //TODO esto no me late, debo modificar la clase JarUtil.
             JarUtil ju = new JarUtil(pathS.toString());
 
             //se van grabando los jars
             String nameJar = new File(pathS.toString()).getName();
             String pathJar = pathS.toString().substring(0, pathS.toString().length() - nameJar.length());
             DemiurgoFacade.getInstance().getService().
                     addJar(idRepo,
                     pathJar,
                     nameJar,
                     ju.getSize(), ju.getLastModif());
             List<String> clazz = ju.getClassInside();
             System.out.println("[addRepo]\t\t Total de clases encontradas " + clazz.size());
             System.out.println("[addRepo]\n");
             // se graban las clases
             DemiurgoFacade.getInstance().getService().
                     addClass(idRepo,
                     pathJar,
                     nameJar,
                     clazz);
             ju = null;
         }
         System.out.println("[addRepo] ");
         System.out.println("[addRepo] RESUMEN:");
         System.out.println("[addRepo] La ruta: " + dir + " ha sido agregada como repositorio.");
         System.out.println("[addRepo] Total de JARs encontrados:"+jarsPath.size());
         System.out.println("[addRepo] Ahora puedes identificarla con el numero:" + idRepo);
         System.out.println("[addRepo] ");
         System.out.println("[addRepo] Puedes ver la lista completa de repositorios con <<showRepos>>");
         //Fin de proceso de indexado.
         buscando = false;
         System.out.println("[addRepo]----------------------------------------------------");
     }
 
     public void search(String command, String type) {
         System.out.println("[" + type + "]----------------------------------------------------");
         String[] tokens = command.split("\\s+");
         if (tokens.length == 1) {
             if (type.equals("searchJar")) {
                 showInfoSearch(type, "Jars", "axis2.jar, WSDL*.jar, *conn*, etc");
             }
             if (type.equals("searchClass")) {
                 showInfoSearch(type, "Class's", "org.apache*.xml, *xmlbeans* , etc");
             }
             return;
         }
         if (tokens.length > 1) {
             if (validaSintaxis(command)) {
                 //validamos repo
                 String query = tokens[1].trim();
                 String idRepo = tokens[3].trim();
                 Repo repo = DemiurgoFacade.getInstance().getService().getRepoById(idRepo);
                 if (repo == null) {
                     System.out.println("[" + type + "]   El repositorio que indicas "
                             + idRepo + ": No existe");
                     System.out.println("[" + type + "]");
                     System.out.println("[" + type + "]     Revisa los repositorios existentes con: showRepos");
                 } else {
                     System.out.println("[" + type + "] Buscando: " + query);
                     System.out.println("[" + type + "] En      : " + repo.getPath());
                     int k = 0;
                     Object[] r = null;
                     buscando = true;
                     SearchTaskThread st = new SearchTaskThread();
                     (new Thread(st)).start();
                     if (type.equals("searchJar")) {
                         r = DemiurgoFacade.getInstance().getService().
                                 getListJarByRepoAndLike(idRepo, StringUtil.generatePattern(query.trim())).toArray();
                     }
                     if (type.equals("searchClass")) {
                         r = DemiurgoFacade.getInstance().getService().
                                 getListClassByIdRepoAndLike(idRepo, StringUtil.generatePattern(query.trim())).toArray();
 
                     }
                     buscando = false;
                     System.out.println("[" + type + "]  Busqueda Terminada. Resultados: " + r.length);
                     if (r.length > 0) {
                         System.out.println("[" + type + "]  Se muestran unicamente los primeros " + resultSize + " resultados.");
                         int max = resultSize>r.length?r.length:resultSize;
                         for (int i = 0; i < max; i++) {
                             System.out.print("[" + type + "] {" + (i + 1) + "}");
                             Object[] o = ((IArray) r[i]).toArray();
                             System.out.println(o[0]);
                             System.out.println("\t\t" + o[1]);
                             System.out.println("\t\t\t" + o[2]);
                             o = null;
                         }
                     } else {
                         System.out.println("[" + type + "]  Intente mejorando el criterio de busqueda");
                     }
                 }
             } else {
                 System.out.println("[searchJar]     Sintaxis no reconocida.");
                 showInfoSearch("searchJar", "Jars", "axis2.jar, WSDL*.jar, *conn*, etc");
             }
         }
         //si llega aca es que tiene sintaxis
         System.out.println("[searchJar]----------------------------------------------------");
     }
 
     /**
      * No vale que inicien y terminen con *, eso lo hace el buscador.
      *
      * @param s
      * @return
      */
     private String validateString(String s) {
         String t1 = null;
         String t2 = null;
         if (s.indexOf("*") == 0) {
             t1 = s.substring(1);
         } else {
             t1 = s;
         }
         if (t1.endsWith("*")) {
             t2 = t1.substring(0, t1.length() - 1);
         } else {
             t2 = t1;
         }
         return t2;
     }
 
     private boolean validaSintaxis(String command) {
         boolean r = false;
         String[] tokens = command.split("\\s+");
         if ((tokens.length > 1 && tokens.length < 4) || (tokens.length > 4)) {
             r = false;
         } else {
             r = true;
         }//
         return r;
     }
 
     private void showInfoSearch(String command, String nameItem, String examples) {
         System.out.println("[" + command + "]" + command + ":");
         System.out.println("[" + command + "]           Realiza una busqueda de " + nameItem + " segun el criterio dado sobre el repositorio indicado.");
         System.out.println("[" + command + "]                  searchJar <criterio> in <idRepo>");
         System.out.println("[" + command + "]Donde: ");
         System.out.println("[" + command + "]           <criterio> Es el criterio de busqueda. Por ejemplo:");
         System.out.println("[" + command + "]                      " + examples);
         System.out.println("[" + command + "] ");
         System.out.println("[" + command + "]           <idRepo>   Identificador del repositorio sobre el cual se realiza la busqueda.");
         System.out.println("[" + command + "]                      Para ver los repositorios existentes: showRepos ");
         System.out.println("[" + command + "]----------------------------------------------------");
     }
 
     public void resultSize(String command) {
         System.out.println("[resultSize]----------------------------------------------------");
         String[] tokens = command.split("=");
         if (tokens.length == 2) {
             int v = StringUtil.toInt(tokens[1].trim(), INIT_SIZE);
             System.out.println("[resultSize] Estableciendo el valor a: " + v);
             resultSize = v;
         } else {
             if (tokens.length > 2) {
                 System.out.println("[resultSize]Demasiados parametros.  ");
                 System.out.println("[resultSize]    ");
             }
             if (tokens.length == 1) {
                 System.out.println("[resultSize]resultSize:");
                 System.out.println("[resultSize]             valor actual :" + resultSize);
                 System.out.println("[resultSize]");
                 System.out.println("[resultSize] Muestra o establece la cantidad de resultados que "
                         + "se mostrara en cada busqueda.");
                 System.out.println("[resultSize] resultSize=<valor>");
                 System.out.println("[resultSize]Donde: ");
                 System.out.println("[resultSize]     <valor> es una cantidad entera. ");
                 System.out.println("[resultSize]     Si <valor> no cumple con la condición, se usa el valor por default: " + INIT_SIZE);
             }
         }
         System.out.println("[resultSize]----------------------------------------------------");
     }
 
     public void delete(String command) {
         System.out.println("[deleteRepo]----------------------------------------------------");
         String[] tokens = command.split(" ");
         String idRepo = null;
         //si es un solo token mostramos ayuda
         if (tokens.length == 1) {
             System.out.println("[deleteRepo]deleteRepo:");
             System.out.println("[deleteRepo]           Elimina un repositorio.");
             System.out.println("[deleteRepo]                  deleteRepo <id_repo>");
             System.out.println("[deleteRepo]Donde: ");
             System.out.println("[deleteRepo]           <id_repo> es el identificador  del repositorio a borrar.");
             System.out.println("[deleteRepo]           Para ver los repositorios disponibles use el comando: <<showRepos>>");
             System.out.println("[deleteRepo]----------------------------------------------------");
             return;
         }
         if (tokens.length > 2) {
             System.out.println("[deleteRepo]Se recibieron mas parametros de los necesarios. ");
             System.out.println("[deleteRepo]Se toma el requerido y se ignora el resto ");
         }
         idRepo = tokens[1];
         Repo repo = DemiurgoFacade.getInstance().getService().getRepoById(idRepo);
         if (repo != null) {
             System.out.println("[deleteRepo]    Eliminando respositorio con identificador: " + idRepo);
             System.out.println("[deleteRepo]             " + repo.getPath());
             boolean res = DemiurgoFacade.getInstance().getService().deleteRepo(idRepo);
             if (res) {
                 System.out.println("[deleteRepo]    Repositorio eliminado: " + idRepo);
             } else {
             }
         } else {
             System.out.println("[deleteRepo]  El repositorio indicado no existe:" + idRepo);
         }
         System.out.println("[deleteRepo]----------------------------------------------------");
     }
 
     public void status() {
         System.out.println("[status]----------------------------------------------------");
         System.out.println("[status]Carpeta de almacenamiento (user.home)..:" + FileUtil.getWorkDirectory());
         System.out.println("[status]Total de Repositorios..................:"
                 + DemiurgoFacade.getInstance().getService().getListRepo().size());
         System.out.println("[status]----------------------------------------------------");
     }
 
     public void showRepos() {
         System.out.println("[showRepos]  --------------------------------------------------------------");
         int k = DemiurgoFacade.getInstance().getService().getListRepo().size();
 
         if (k == 0) {
             System.out.println("[showRepos] No existen repositorios registrados.");
         } else {
 
             System.out.println("[showRepos]  ==============================================================");
             System.out.println("[showRepos]    ID\t\tRuta");
             System.out.println("[showRepos]  ==============================================================");
             List<Repo> l = DemiurgoFacade.getInstance().getService().getListRepo();
             for (int i = 0; i < k; i++) {
                 System.out.println("[showRepos]    " + l.get(i).getId() + "\t\t" + l.get(i).getPath());
             }
             System.out.println("[showRepos]  ==============================================================");
         }
         if(FileUtil.existGradleDirectory()||FileUtil.existIvyDirectory()||FileUtil.existMaven2Directory()){
             System.out.println("[showRepos]  --------------------------------------------------------------");
             System.out.println("[showRepos]  Los siguientes directorios pueden tener jars:");
             if(FileUtil.existGradleDirectory()){
                 System.out.println("[showRepos] \t GRADLE.......:"+FileUtil.getGradleDirectory());
             }
             if(FileUtil.existIvyDirectory()){
                 System.out.println("[showRepos] \t IVY .........:"+FileUtil.getIvyDirectory());
             }   
             if(FileUtil.existMaven2Directory()){
                 System.out.println("[showRepos] \t MAVEN .......:"+FileUtil.getMaven2Directory());
             }            
         }
         System.out.println("[showRepos]  --------------------------------------------------------------");
     }
 
     private boolean validaSyntaxisSearch(String body) {
         return true;
     }
 
     private String validateDir(String dir) {
         String r;
         if (dir.endsWith(File.separator)) {
             r = dir.substring(0, dir.length() - 1);
         } else {
             r = dir;
         }
         return r;
     }
 
     //--- Clases
     class SearchTaskThread implements Runnable {
 
         @Override
         public void run() {
             while (buscando) {
                 try {
                     System.out.println("."); 
                     Thread.sleep(1000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(ConsoleUtil.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
     //---
 }
