 package org.javascool.gui;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import org.javascool.core.ProgletEngine;
 
 import org.javascool.widgets.ToolBar;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import org.javascool.Core;
 import org.javascool.builder.DialogFrame;
 import org.javascool.widgets.StartStopButton;
 import org.javascool.builder.ProgletsBuilder;
 import org.javascool.macros.Macros;
 
 /** La barre d'outils de Java's cool
  * Elle est placée en haut de l'interface. Elle contient les boutons de gestion
  * des fichiers, de compilation et d'éxecution.
  * @see org.javascool.widgets.StartStopButton
  * @see org.javascool.gui.JVSPanel
  */
 class JVSToolBar extends ToolBar {
   private static final long serialVersionUID = 1L;
   /** Boutons de l'interface. */
   private JButton compileButton;
   private StartStopButton runButton;
   /** Instance de la classe */
   private static JVSToolBar jvstb;
 
   public static JVSToolBar getInstance() {
     if(jvstb == null)
       jvstb = new JVSToolBar();
     return jvstb;
   }
   private JVSToolBar() {
     setName("Java's cool ToolBar");
     init();
   }
   /** Initialize la barre d'outils en créant les bouttons */
   private void init() {
     addTool("Nouvelle activité", "org/javascool/widgets/icons/new.png", new Runnable() {
               @Override
               public void run() {
                 JVSPanel.getInstance().closeProglet();
               }
             }
             );
     addTool("Nouveau fichier", "org/javascool/widgets/icons/new.png", new Runnable() {
               @Override
               public void run() {
                 JVSPanel.getInstance().newFile();
               }
             }
             );
     addTool("Ouvrir un fichier", "org/javascool/widgets/icons/open.png", new Runnable() {
               @Override
               public void run() {
                 JVSPanel.getInstance().openFile();
               }
             }
             );
     addTool("Sauver", "org/javascool/widgets/icons/save.png", new Runnable() {
               @Override
               public void run() {
                 JVSPanel.getInstance().saveFile();
               }
             }
             );
     addTool("Fermer le fichier", "org/javascool/widgets/icons/remove.png", new Runnable() {
               @Override
               public void run() {
                 JVSPanel.getInstance().closeFile();
               }
             }
             );
 
     compileButton = addTool("Compiler", "org/javascool/widgets/icons/compile.png", new Runnable() {
                               @Override
                               public void run() {
                                 JVSPanel.getInstance().compileFile();
                               }
                             }
                             );
 
     addTool("Executer", runButton = new StartStopButton() {
               private static final long serialVersionUID = 1L;
 
               @Override
               public void start() {
                 ProgletEngine.getInstance().doRun();
               }
 
               @Override
               public void stop() {
                 ProgletEngine.getInstance().doStop();
               }
 
               @Override
               public boolean isRunning() {
                 return ProgletEngine.getInstance().isRunning();
               }
             }
             );
 
     runButton.setVisible(false);
     // Crée le menu de construction de proglets si pertinent
     if(ProgletsBuilder.canBuildProglets()) {
       pbutton = addRightTool("Proglet Builder", new Runnable() {
                                @Override
                                public void run() {
                                  DialogFrame.startFrame();
                                }
                              }
                              );
     }
     JLabel logoLabel = new JLabel(Macros.getIcon("org/javascool/widgets/icons/logo32.png"));
     logoLabel.addMouseListener(new MouseListener() {
                                  @Override
                                  public void mouseClicked(MouseEvent e) {
                                    Core.showAboutMessage();
                                  }
 
                                  @Override
                                  public void mousePressed(MouseEvent e) {}
 
                                  @Override
                                  public void mouseReleased(MouseEvent e) {}
 
                                  @Override
                                  public void mouseEntered(MouseEvent e) {}
 
                                  @Override
                                  public void mouseExited(MouseEvent e) {}
                                }
                                );
     this.add(logoLabel, 0);
   }
   // @ inner-class-variable
   private JButton pbutton;
 
   public void enableCompileButton() {
     compileButton.setVisible(true);
     revalidate();
   }
   public void disableCompileButton() {
     compileButton.setVisible(false);
     revalidate();
   }
   public void enableStartStopButton() {
     runButton.setVisible(true);
     revalidate();
   }
   public void disableStartStopButton() {
     runButton.setVisible(false);
     revalidate();
   }
 }
