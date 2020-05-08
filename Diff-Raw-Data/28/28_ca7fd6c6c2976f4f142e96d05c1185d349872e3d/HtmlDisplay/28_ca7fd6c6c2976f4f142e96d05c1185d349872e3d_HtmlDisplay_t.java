 /*********************************************************************************
 * Philippe.Vienne@sophia.inria.fr, Copyright (C) 2011.  All rights reserved.    *
 * Guillaume.Matheron@sophia.inria.fr, Copyright (C) 2011.  All rights reserved. *
 * Thierry.Vieville@sophia.inria.fr, Copyright (C) 2004.  All rights reserved.   *
 *********************************************************************************/
 package org.javascool.widgets;
 
 // Used to build the gui
 import java.net.MalformedURLException;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.JEditorPane;
 
 // Used to manage links
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.net.URLDecoder;
 import javax.swing.text.Document;
 import org.javascool.macros.Macros;
 
 // Used to manage keystroke
 import java.util.ArrayList;
 
 /** Definit un visualisateur de pages HTML3.
  * <p><i>Note:</i> L'implémentation disponible ne rend utilisable que le "vieux" HTML-3.</p>
  * <p><i>Conseil:</i> Ecrire les pages en <a href="http://javascool.gforge.inria.fr/v4/index.php?page=developers&action=doc-xml">HML</a> (en XHTML simplifié),
  * la maintenance et polyvalence des pages en sera renforcée.</p>
  * <div id="URLs"><p><b>Mécanismes d'affichage des contenus:</b><ul>
  * <li>Les pages locales d'extension <tt>*.htm</tt> sont réputées être du HTML3 et sont affichées ici.</li>
  * <li>Les autres pages <tt>http://</tt>, <tt>file://</tt>, etc.. sont visualiées dans le navigateur du système, extérieur à javascool.</li>
  * <li>Il est possible d'ouvrir des pages dans une cible autre que ce visualisateur: <ul>
  *   <li>Les liens de la forme <tt>http://editor/<i>location</i></tt> ouvrent le document dans l'éditeur de JavaScool. <br>
  *     Il sont générés par un tag de la form <tt>&lt;l class="editor" ..</tt></li>
  *   <li>Les liens de la forme <tt>http://newtab/<i>location</i></tt> ouvrent le document dans un autre onglet de JavaScool.<br>
  *     Il sont générés par un tag de la form <tt>&lt;l class="newtab" ..</tt></li> </li>
  * </ul> Il sont produits par les tags <tt>&lt;a target="editor" . . </tt> du XML.
  * En cas d'échec les contenus sont dirigés vers le navigateur du système, extérieur à javascool.</li>
  * <li>Les liens de la forme <tt>string://?value="text"</tt> permettent d'afficher directement du texte HTML3.</li>
  * <li>Les autres liens font l'objet d'un appel à la méthode <tt>doBrowse()</tt> ce qui permet de définir des URI dépendant de l'application.</li>
  * </ul></div>
  *
  * @see <a href="HtmlDisplay.java.html">source code</a>
  * @serial exclude
  */
 public class HtmlDisplay extends JPanel {
   private static final long serialVersionUID = 1L;
 
   /** Le panneau d'affichage du texte. */
   private JEditorPane pane;
   /** Les bottons de navigation. */
   private JButton home, prev, next;
 
   /** Définit le préfix pour une chaîne. */
   static private final String stringPrefix = "http://string?";
   /** Définit le préfix pour une ouverture dans l'éditeur. */
   static private final String editorPrefix = "http://editor?";
   /** Définit le préfix pour une ouverture dans un onglet. */
   static private final String newtabPrefix = "http://newtab?";
 
   // @bean
   public HtmlDisplay() {
     setLayout(new BorderLayout());
     {
       ToolBar bar = new ToolBar();
       home = bar.addTool("Page initiale", "org/javascool/widgets/icons/refresh.png", new Runnable() {
                            @Override
                            public void run() {
                              if(urls.hasHome())
                                update(urls.getHome(), false);
                            }
                          }
                          );
       prev = bar.addTool("Page précédente", "org/javascool/widgets/icons/prev.png", new Runnable() {
                            @Override
                            public void run() {
                              if(urls.hasPrev())
                                update(urls.getPrev(), false);
                            }
                          }
                          );
       next = bar.addTool("Page suivante", "org/javascool/widgets/icons/next.png", new Runnable() {
                            @Override
                            public void run() {
                              if(urls.hasNext())
                                update(urls.getNext(), false);
                            }
                          }
                          );
       add(bar, BorderLayout.NORTH);
     }
     {
       pane = new JEditorPane();
       pane.setBackground(Color.WHITE);
       pane.setEditable(false);
       pane.setContentType("text/html; charset=utf-8");
       pane.addHyperlinkListener(new HyperlinkListener() {
                                   @Override
                                   public void hyperlinkUpdate(HyperlinkEvent e) {
                                     if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                                       update(e.getDescription(), true);
                                   }
                                 }
                                 );
       JScrollPane spane = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       add(spane, BorderLayout.CENTER);
     }
   }
   /** Affiche une page de texte HTML3 dans le visualisateur.
    * @param location L'URL de la page à afficher.
    * @return Cet objet, permettant de définir la construction <tt>new HtmlDisplay().setPage(..)</tt>.
    */
   public HtmlDisplay setPage(String location) {
     update(location, true);
     return this;
   }
   public HtmlDisplay setPage(URL location) {
     update(location, true);
     return this;
   }
   /** Affiche un texte HTML3 dans le visualisateur.
    * @param text Le texte à afficher.
    * @return Cet objet, permettant de définir la construction <tt>new HtmlDisplay().setText(..)</tt>.
    */
   public HtmlDisplay setText(String text) {
     try {
       return setPage(stringPrefix + URLEncoder.encode(text, "UTF-8"));
     } catch(java.io.UnsupportedEncodingException e) { throw new IllegalStateException("UTF-8 n'est pas reconnu comme encodage: (" + e + ") c'est un bug Java !");
     }
   }
   /** Met à jour les boutons selon l'état de la pile. */
   private void updateButtons() {
     home.setEnabled(urls.hasHome());
     prev.setEnabled(urls.hasPrev());
     next.setEnabled(urls.hasNext());
   }
   /** Définit une pile d'URL avec le mécanisme de home/prev/next. */
   private class URLStack extends ArrayList<Object>{
     private static final long serialVersionUID = 1L;
     /** Index courant dans la pile. */
     private int current = -1;
     /** Ajoute un élément dans la pile. */
     public void push(Object url) {
       current++;
       while(current < size())
         remove(current);
       add(url);
     }
     public Object getCurrent() {
       return current >= 0 ? get(current) : null;
     }
     public boolean hasHome() {
       return current >= 0;
     }
     public Object getHome() {
       if(hasHome())
         current = 0;
       return getCurrent();
     }
     public boolean hasPrev() {
       return current > 0;
     }
     public Object getPrev() {
       if(hasPrev())
         current--;
       return getCurrent();
     }
     public boolean hasNext() {
       return current < size() - 1;
     }
     public Object getNext() {
       if(hasNext())
         current++;
       return getCurrent();
     }
     @Override
     public String toString() {
       String s = "";
       for(int i = size() - 1; i >= 0; i--)
         s += (i == current ? " * " : "   ") + get(i) + "\n";
       return s;
     }
   }
   private URLStack urls = new URLStack();
 
   /** Implémentation du mécanisme de gestion des URL spécifiques.
    * <p>Cette routine est appelée pour gérer des URL specifiques d'une application donnée.</p>
    * @param location L'URL à traiter.
    * @return Cette méthode doit retourner true si l'URL à été traité et false si l'URL n'a pas été reconnu ou traité.
    */
   public boolean doBrowse(String location) {
     return false;
   }
   /** Gestion des URLs externes par le navigateur du système. */
   private void browse(String location) {
     try {
       java.awt.Desktop.getDesktop().browse(new java.net.URI(location));
     } catch(Exception e) {
       setText("Cette page est à l'adresse internet: <tt>«" + location.replaceFirst("^(mailto):.*", "$1: ...") + "»</tt> (non accessible ici).");
     }
   }
   /** Mécanisme de gestion des URL. */
   private void update(String location, boolean push) {
     // Gestion des contenus textuels
     if(location.startsWith(stringPrefix)) { // Affichage de texte
       try {
         if(push)
           urls.push(location);
         pane.setText(URLDecoder.decode(location.substring(stringPrefix.length()), "UTF-8"));
       } catch(java.io.UnsupportedEncodingException e) { throw new IllegalStateException("UTF-8 n'est pas reconnu comme encodage: (" + e + ") c'est un bug Java !");
       }
     } else if(location.startsWith(editorPrefix)) {  // Affichage dans editeur JavaScool
      org.javascool.gui.Desktop.getInstance().openFile(toURL(location.substring(editorPrefix.length())));
     } else if(location.startsWith(newtabPrefix)) {    // Affichage dans browser JavaScool
       URL url = toURL(location.substring(newtabPrefix.length()));
       String name = new File(url.getPath()).getName().replaceFirst("\\.[^\\.]*$", "").replace('_', '.');
       org.javascool.gui.Desktop.getInstance().openBrowserTab(url.toString(), name.substring(0, 1).toUpperCase()+name.substring(1));
     } else if(location.matches("^(http|https|rtsp|mailto):.*$"))     // Gestion des URL externes
       browse(location);
     else if(location.matches(".*\\.htm$") || location.matches("^#.*")) {   // Gestion des URLs en HTML3 et des ancres
       update(toURL(location), push);
     }    else if(!doBrowse(location))     // Délégation au client
       setText("Le lien : <tt>«" + location + "»</tt> n'a pas pu être affiché");
   }
   private void update(URL url, boolean push) {
     if(push)
       urls.push(url);
     pane.getDocument().putProperty(Document.StreamDescriptionProperty, null);
     
     try {
       pane.setPage(url);
     } catch(IOException e) {
       setText("Le lien : <tt>«" + url + "»</tt> génère une erreur \"" + e.toString() + "\"");
     }
     updateButtons();
   }
   private void update(Object link, boolean push) {
     if(link instanceof URL)
       update((URL) link, push);
     else
       update(link.toString(), push);
   }
   private URL toURL(String location) {
     try {
       return urls.isEmpty() ? Macros.getResourceURL(location) :
 	urls.getCurrent() instanceof URL ? new URL((URL) urls.getCurrent(), location) :
 	new URL(location);
     } catch(MalformedURLException e) {
       try {
       return new URL(stringPrefix+"Le lien : <tt>«" + location + "»</tt> est mal formé");
       } catch(MalformedURLException ex) {
           throw new IllegalStateException(ex);
       }
     }
   }
 }
 
