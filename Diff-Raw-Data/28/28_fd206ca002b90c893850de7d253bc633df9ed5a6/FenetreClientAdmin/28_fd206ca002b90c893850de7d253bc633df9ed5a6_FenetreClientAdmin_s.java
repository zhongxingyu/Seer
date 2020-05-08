 package graphique.admin;
 
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 import logiqueMetier.Serveur;
 import logiqueMetier.ServeurV2;
 
 /**
  * Cette classe organise l'ensemble des composants de la partie administrative 
  * de l'interface graphique.
  * 
  * @author Fauvel-jaeger Olivier, Tanguy Arnaud, Ceschel Marvin, Kruck Nathan
  * @version 2012.01.29
  */
 
 public class FenetreClientAdmin extends JFrame {
     private static final long serialVersionUID = 1L;
 
     private Serveur serveur;
     JTabbedPane tabbedPane;
     GestionTrajetsPanel gestionTrajetsPanel;
 
     public FenetreClientAdmin() {
         super();
        serveur = new ServeurV2();      
        gestionTrajetsPanel = new GestionTrajetsPanel(serveur);
 
         try {
             serveur.charger();
         } catch (Exception e) {
             e.printStackTrace();
        }
         build();
     }
 
     private void build() {
         setTitle("Administration");
         setSize(800, 400);
         setLocationRelativeTo(null);
         setResizable(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setContentPane(buildContentPane());
     }
 
     /**
      * Construit les différents onglets de l'application
      */
     private JTabbedPane buildTabbedPane() {
         tabbedPane = new JTabbedPane();
 
         tabbedPane.add("Ville", buildVillePanel());
         tabbedPane.add("Transports", buildTransportsPanel());
         tabbedPane.add("Trajets", buildTrajetsPanel());
 
         return tabbedPane;
     }
 
     private JPanel buildVillePanel() {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
         panel.add(new AjoutVillePanel(serveur, gestionTrajetsPanel));
         return panel;
     }
 
     private JPanel buildTransportsPanel() {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
         panel.add(new TableTransportsPanel(serveur));
         return panel;
     }
 
     private JPanel buildTrajetsPanel() {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
         panel.add(gestionTrajetsPanel);
         return panel;
     }
 
     /**
      * Assemble les différents composants
      * @return le panel contenant tous les composants assemblés dans des layout managers
      */
     private JPanel buildContentPane() {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 
         panel.add(buildTabbedPane());
 
         JPanel bp = new JPanel();      
         bp.setLayout(new BoxLayout(bp, BoxLayout.LINE_AXIS));
         bp.add(new JButton(new SaveAction("Sauvegarder")));
         bp.add(new JButton(new QuitAndSaveAction("Sauvegarder et quitter")));
         bp.add(new JButton(new QuitAction("Quitter")));
 
         panel.add(bp);
 
         return panel;
     }
     /**
      * Demande au serveur d'enregistrer les modifications.
      */
     private class SaveAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public SaveAction(String texte) {
             super(texte);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             try {
                 serveur.sauvegarder();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     
     /**
      * Quitte et demande au serveur d'enregistrer.
      */
     private class QuitAndSaveAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public QuitAndSaveAction(String texte) {
             super(texte);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             try {
                 serveur.sauvegarder();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             dispose();
         }
     }
 
     /**
      * Quitte sans sauvegarder
      */
     private class QuitAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public QuitAction(String texte) {
             super(texte);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             dispose();
         }
     }
 }
