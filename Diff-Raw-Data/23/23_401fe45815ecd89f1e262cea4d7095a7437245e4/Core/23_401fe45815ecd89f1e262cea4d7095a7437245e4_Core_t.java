 /*******************************************************************************
 *           Philippe.Vienne, Copyright (C) 2011.  All rights reserved.         *
 *******************************************************************************/
 
 package org.javascool;
 
import javax.swing.UIManager;
 import org.javascool.gui.Desktop;
 import org.javascool.macros.Macros;
 import org.javascool.tools.ErrorCatcher;
 
 /** Lanceur de l'application "apprenant" qui permet de manipuler des «proglets».  *
  *
  * @see <a href="Core.java.html">source code</a>
  * @serial exclude
  */
 public class Core {
   /** Aide de JVS */
   public static final String help = "org/javascool/macros/memo-macros.htm";
   /** Mets en place le système d'alerte en cas d'erreur non gérée. */
   static void setUncaughtExceptionAlert() {
     ErrorCatcher.setUncaughtExceptionAlert("<h1>Détection d'une anomalie liée à Java:</h1>\n" +
                                            "Il y a un problème de compatibilité avec votre système, nous allons vous aider:<ul>\n" +
                                            "  <li>Copier/Coller tous les éléments de cette fenêtre et</li>\n" +
                                            "  <li>Envoyez les par mail à <b>javascool@googlegroups.com</b> avec toute information utile.</li>" +
                                            " </ul>",
                                            About.revision);
   }
   /** Retrouve le chemin du jar courant.
    * @return Le chemin du jar
    * @throws RuntimeException lorsque l'application n'a pas été démarré depuis un jar
    */
   public static String javascoolJar()  {
     String jar = "";
     jar = Macros.getDecoded(Macros.getResourceURL("org/javascool/Core.class")).replaceFirst("jar:file:([^!]*)!.*", "$1");
     System.err.println("Notice: the javascool jar is "+jar);
     if(jar.endsWith(".jar"))
       return jar;
     else
      throw new RuntimeException("Java's cool n'a pas été démarré depuis un Jar");
   }
   /** Lanceur de l'application.
    * @param usage <tt>java -jar javascool.jar</tt>
    */
   public static void main(String[] usage) {
     if((usage.length > 0) && (usage[0].equals("-h") || usage[0].equals("-help") || usage[0].equals("--help"))) {
       System.out.println("Java's Cool Core - lance l'interface pour travailler avec les proglets");
       System.out.println("Usage : java -jar javascool.jar");
       System.exit(0);
     }
    // Empeche de pouvoir renommer itempestivement des folder
    UIManager.put("FileChooser.readOnly", Boolean.TRUE);
     System.err.println("" + About.title + " is starting ...");
     ErrorCatcher.checkJavaVersion(6);
     setUncaughtExceptionAlert();
     Desktop.getInstance().getFrame();
   }
 }
