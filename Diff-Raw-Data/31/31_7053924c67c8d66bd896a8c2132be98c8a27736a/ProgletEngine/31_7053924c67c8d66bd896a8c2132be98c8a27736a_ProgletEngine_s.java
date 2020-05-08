 /*********************************************************************************
  * Philippe.Vienne@sophia.inria.fr, Copyright (C) 2011.  All rights reserved.    *
  * Guillaume.Matheron@sophia.inria.fr, Copyright (C) 2011.  All rights reserved. *
  * Thierry.Vieville@sophia.inria.fr, Copyright (C) 2009.  All rights reserved.   *
  *********************************************************************************/
 package org.javascool.core;
 
 import java.applet.Applet;
 import java.awt.Component;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import org.javascool.macros.Macros;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.Comparator;
 import javax.swing.JFrame;
 import org.javascool.Core;
 import org.javascool.tools.FileManager;
 import org.javascool.tools.Pml;
 import org.javascool.tools.Invoke;
 import org.javascool.widgets.MainFrame;
 
 /** Définit les mécanismes de compilation, exécution, gestion de proglet.
  *
  * @see <a href="ProgletEngine.java.html">code source</a>
  * @serial exclude
  */
 public class ProgletEngine {
 
     /** Tables des proglets. */
     private ArrayList<Proglet> proglets;
 
     // @static-instance
     /** Crée et/ou renvoie l'unique instance de l'engine.
      * <p>Une application ne peut définir qu'un seul engine.</p>
      */
     public static ProgletEngine getInstance() {
         if (engine == null) {
             engine = new ProgletEngine();
         }
         return engine;
     }
     private static ProgletEngine engine = null;
 
     private ProgletEngine() {
         // Détection des proglets présentes dans le jar
         {
             proglets = new ArrayList<Proglet>();
             String javascoolJar = Core.javascoolJar();
             for (String dir : FileManager.list(javascoolJar, "org.javascool.proglets.[^\\.]+.proglet.pml")) {
                 Proglet proglet = new Proglet().load(dir.replaceFirst("jar:[^!]*!(.*)proglet.pml", "$1"));
                 proglets.add(proglet);
             }
             Collections.sort(proglets, new Comparator<Proglet>() {
                 @Override
                  public int compare(Proglet p1, Proglet p2) {
                      return p1.getName().compareTo(p2.getName());
             }});
         }
         // Définit une proglet "vide" pour lancer l'interface
         if (proglets.isEmpty()) {

             for (int i = 0; i < 1; i++) {
                 Proglet p = new Proglet();
                 p.pml.set("name", "Interface");
                 p.pml.set("icon-location", "org/javascool/widgets/icons/scripts.png");
                 p.pml.set("help-location", "org/javascool/macros/memo-macros.htm");
                 proglets.add(p);
             }
         }
     }
     //
     // [1] Mécanisme de compilation/exécution
     //
 
     /** Mécanisme de compilation du fichier Jvs.
      * @param program Nom du programme à compiler.
      * @return La valeur true si la compilation est ok, false sinon.
      */
     public boolean doCompile(String program) {
         doStop();
         // Traduction Jvs -> Java puis Java -> Class et chargement de la classe si succès
         Jvs2Java jvs2java = new Jvs2Java();
         if (getProglet() != null) {
             jvs2java.setProgletTranslator(getProglet().getTranslator());
             jvs2java.setProgletPackageName(getProglet().hasFunctions() ? "org.javascool.proglets." + getProglet().getName() : null);
         }
         String javaCode = jvs2java.translate(program);
         // Creation d'un répertoire temporaire
         String javaFile;
         try {
             File buildDir = FileManager.createTempDir("javac");
             javaFile = buildDir + File.separator + jvs2java.getClassName() + ".java";
             FileManager.save(javaFile, javaCode);
             // Si il y a un problème avec le répertoire temporaire on se rabat sur le répertoire local
         } catch(Exception e) {
             javaFile = new File(jvs2java.getClassName() + ".java").getAbsolutePath();
             System.err.println("Sauvegarde locale du fichier : "+ javaFile);
             FileManager.save(javaFile, javaCode);
         }
         if (Java2Class.compile(javaFile)) {
             runnable = Java2Class.load(javaFile);
             return true;
         } else {
             runnable = null;
             return false;
         }
     }
 
     /** Mécanisme de lancement du programme compilé. */
     public void doRun() {
         doStop();
         // Lancement du runnable dans un thread
         if (runnable != null) {
             (thread = new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         runnable.run();
                         thread = null;
                     } catch (Throwable e) {
                         System.out.println("Erreur à l'exécution: " + e);
                     }
                 }
             })).start();
         }
     }
 
     /** Mécanisme d'arrêt du programme compilé. 
      * @param message Message d'erreur affiché à la console. Si null (par défaut) pas de message.
      */
     public void doStop(String message) {
         if (thread != null) {
             thread.interrupt();
             thread = null;
         }
         if (message != null) {
             System.out.println("Cause de l'interruption : " + message);
         }
     }
 
     /**
      * @see #doStop(String)
      */
     public void doStop() {
         doStop(null);
     }
 
     /** Renvoie true si le programme est en cours. */
     public boolean isRunning() {
         return thread != null;
     }
     private Thread thread = null;
     private Runnable runnable = null;
 
     /** Renvoie le runnable correspondant au programme utilisateur en cours.
      * @return Le runnable correspondant au programme démarré par doRun() ou null si il n'y en a pas.
      */
     public Runnable getProgletRunnable() {
         return runnable;
     }
     //
     // Mécanisme de chargement d'une proglet
     //
 
     /** Mécanisme de chargement d'une proglet.
      * @param proglet Le nom de la proglet.
      * @return La proglet en fonctionnement ou null si la proglet n'existe pas.
      * @throws IllegalArgumentException Si il y a tentative d'utilisation d'une proglet indéfinie
      */
     public Proglet setProglet(String proglet) {
         if (currentProglet != null)
         currentProglet.stop();
         currentProglet = null;
         for (Proglet p : getProglets()) {
             if (p.getName().equals(proglet)) {
                 currentProglet = p;
             }
         }
         if (currentProglet == null)
             throw new IllegalArgumentException("Tentative d'utilisation d'une proglet indéfinie : "+proglet);
         currentProglet.start();
         return currentProglet;
     }
 
     /** Renvoie la proglet demandé.
      * @return la proglet ou null sinon.
      */
     public Proglet getProglet(String proglet) {
         for (Proglet p : getProglets()) {
             if (p.getName().equals(proglet)) {
                 return p;
             }
         }
         return null;
     }
 
     /** Renvoie la proglet courante.
      * * @return la proglet courante ou null sinon.
      */
     public Proglet getProglet() {
         return currentProglet;
     }
     private Proglet currentProglet = null;
 
     /** Renvoie toutes les proglets actuellement disponibles.
      * @return Un objet utilisable à travers la construction <tt>for(Proglet proglet: getProglets()) { .. / .. }</tt>.
      */
     public Iterable<Proglet> getProglets() {
         return proglets;
     }
 
     public class Proglet {
 
         /** Méta-données de la proglet. */
         public Pml pml = new Pml();
 
         /** Définit une proglet à partir d'un répertoire donné.
          * @param location L'URL (Universal Resource Location) où se trouve la proglet.
          * @throws IllegalArgumentException Si l'URL est mal formée.
          * @return Cet objet, permettant de définir la construction <tt>new Proglet().load(..)</tt>.
          */
         public Proglet load(String location) {
             // Définit les méta-données de la proglet.
             pml.load(location + "proglet.pml");
             pml.set("location", location);
             try {
                 pml.set("name", new File(location).getName());
             } catch (Exception e) {
                 throw new IllegalArgumentException(e + " : " + location + " is a malformed URL");
             }
             if (FileManager.exists(Macros.getResourceURL(location + "completion.xml"))) {
                 pml.set("completion", location + "completion.xml");
             }
             if (pml.isDefined("icon")
                     && FileManager.exists(Macros.getResourceURL(location + pml.getString("icon")))) {
                 pml.set("icon-location", location + pml.getString("icon"));
             } else {
                 pml.set("icon-location", "org/javascool/widgets/icons/scripts.png");
             }
             try {
                 Class.forName("org.javascool.proglets." + pml.getString("name") + ".Functions");
                 pml.set("has-functions", true);
             } catch (Throwable e) {
                 pml.set("has-functions", false);
             }
             if (!pml.isDefined("help-location")) {
                 pml.set("help-location", pml.getString("location") + "help.htm");
             }
             try {
                 pml.set("jvs-translator", (Translator) Class.forName("org.javascool.proglets." + pml.getString("name") + ".Translator").newInstance());
             } catch (Throwable e) {
             }
             return this;
         }
 
         @Override
         public String toString() {
             return pml.toString();
         }
 
         /** Renvoie le nom de la proglet.
          * @return Le nom de la proglet.
          */
         public String getName() {
             return pml.getString("name");
         }
 
         /** Renvoie le titre de la proglet.
          * @return Le titre de la proglet.
          */
         public String getTitle() {
             return pml.getString("title");
         }
 
         /** Renvoie l'icone de la proglet.
          * @return Le nom de l'URL de l'icone de la proglet, ou l'icone par defaut sinon.
          */
         public String getIcon() {
             return pml.getString("icon-location");
         }
 
         /** Renvoie la documentation de la proglet.
          * @return L'URL de la documentation de la proglet.
          */
         public String getHelp() {
             return pml.getString("help-location");
         }
 
         /** Renvoie l'url du fichier de completion de la proglet.
          * @return L'URL de l'xml de completion de la proglet.
          */
         public String getCompletion() {
             return pml.getString("completion", "");
         }
 
         /** Indique si la proglet définit des fonctions statiques pour l'utilisateur.
          */
         public boolean hasFunctions() {
             return pml.getBoolean("has-functions");
         }
 
         /** Renvoie, si il existe, le panneau graphique à insérer dans javascool.
          * @return Le panneau graphique de la proglet si il existe, sinon null.
          */
         public Component getPane() {
            setPane();
             return (Component) pml.getObject("java-pane");
         }
         /** Renvoie, si il existe, le panneau graphique de la proglet.
          * @return Le panneau graphique de la proglet si il existe, sinon null.
          */
         public Component getProgletPane() {
            setPane();
            return (Component) pml.getObject("java-proglet-pane");
         }
         private void setPane() {
           if (!pml.isDefined("pane-defined")) {
               pml.set("pane-defined", true);
             if (this.isProcessing()) {
                 boolean popup = true;
                 try {
 		  int width = pml.getInteger("width", 500), height = pml.getInteger("height", 500);
                     Applet applet = (Applet) Class.forName("" + pml.getString("name") + "").newInstance();
                     applet.init();
                     applet.setMinimumSize(new Dimension(width, height));
                     applet.setMaximumSize(new Dimension(width, height));
                     if (popup) {
                         popupframe = (new MainFrame() {
                                 @Override
                             public boolean isClosable() { return false; }
                         }).reset(getName(), getIcon(), width, height, applet);
                         pml.set("java-pane", null);
                     }  else {
                         pml.set("java-pane", applet);
                     }
                     pml.set("java-proglet-pane", applet);
                 } catch (Throwable e) {
                     e.printStackTrace();
                    System.err.println("Upps erreur de chargement d'une proglet processing : "+e);
                 }
             } else {
                 try {
                     Component pane = (Component) Class.forName("org.javascool.proglets." + pml.getString("name") + ".Panel").newInstance();
                     if (pane instanceof JFrame) {
                      ((JFrame) pane).setVisible(true);
                      pml.set("java-pane", null);
                     } else
                         pml.set("java-pane", pane);
                     pml.set("java-proglet-pane", pane);
                 } catch (Throwable e) {
                 }
             }
             }
         }
         private MainFrame popupframe = null;
         /** Renvoie, si il existe, le translateur de code de la proglet.
          * @return Le translateur de code de la proglet si il existe, sinon null.
          */
         public Translator getTranslator() {
             return (Translator) pml.getObject("jvs-translator");
         }
 
         /** Indique si la proglet a une démo pour l'utilisateur.
          */
         public boolean hasDemo() {
             return getPane() != null && Invoke.run(getPane(), "start", false);
         }
 
         /** Lance la démo de la proglet.
          * @throws RuntimeException si la méthode génère une exception lors de son appel.
          */
         public void doDemo() {
             if (hasDemo()) {
                 (new Thread() {
 
                     @Override
                     public void run() {
                         Invoke.run(getPane(), "start");
                     }
                 }).start();
             }
         }
 
         /**  Indique si la proglet est une proglet processing.
          * @return La valeur true si cette applet est développée en processing.
          */
         public boolean isProcessing() {
             return pml.getBoolean("processing");
         }
         /** Démarre la proglet. */
         public void start() {
         if (popupframe != null)
             popupframe.setVisible(true);
        try {
         if (getPane() != null && getPane() instanceof Applet)
             ((Applet) getPane()).start();
         } catch(Throwable e) {
             System.err.println("Erreur au démarrage de l'applet/proglet");
         }
         }
         /** Arrête la proglet. */
         public void stop() {
         try {
         if (getPane() != null && getPane() instanceof Applet)
             ((Applet) getPane()).stop();
         } catch(Throwable e) {
             System.err.println("Erreur à l'arrêt de l'applet/proglet");
         }
         if (popupframe != null)
             popupframe.setVisible(false);
         }
     }
 }
