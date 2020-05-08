 package Vue;
 
 import Controller.Action.ActionCarre;
 import Controller.Action.ActionCerfVolant;
 import Controller.Action.ActionChargement;
 import Controller.Action.ActionCouleur;
 import Controller.Action.ActionLosange;
 import Controller.Action.ActionParallelogramme;
 import Controller.Action.ActionRectangle;
 import Controller.Action.ActionSauvegarde;
 import Controller.Action.ActionTrapeze;
 import Controller.Main;
 import Model.listSelectionModel;
 import Vue.Listener.SharedListSelectionHandler;
 import Vue.Listener.SimpleMouseListener;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 /**
  *
  * @author Yohan Sanchez et Forian Haller-Cassandre
  */
 public class FenetrePincipale extends JFrame {
 
     Main monProgramme;
     JPanel east;
     JPanel west;
     JPanel south;
     JPanel center;
     JLabel monTexte;
     JTextField jTF_Point1;
     JTextField jTF_Point2;
     JTextField jTF_Point3;
     JTextField jTF_Point4;
     JLabel jL_Point1;
     JLabel jL_Point2;
     JLabel jL_Point3;
     JLabel jL_Point4;
     JList<String> jl_Polygones;
 
     public JList<String> getJl_Polygones() {
         return jl_Polygones;
     }
     ActionCarre monActionCarre;
     ActionRectangle monActionRectangle;
     ActionCerfVolant monActionCerfVolant;
     ActionTrapeze monActionTrapeze;
     ActionLosange monActionLosange;
     ActionParallelogramme monActionParallelogramme;
     JTextArea maZoneDeTexte;
     ListSelectionModel listSelectionModel;
     JCanvas monJCanvas;
 
     public JCanvas getMonJCanvas() {
         return monJCanvas;
     }
 
     public FenetrePincipale(Main monProgramme) {
         this.monProgramme = monProgramme;
 
         JPanel content = new JPanel();
         content.setLayout(new BorderLayout());
 
         //CENTER
         this.center = new JPanel();
         center.setLayout(new BorderLayout());
         this.monJCanvas = new JCanvas(this.monProgramme);
         this.monJCanvas.setBackground(Color.WHITE);
         new SimpleMouseListener(this.monJCanvas);
         center.add(this.monJCanvas, BorderLayout.CENTER);
 
         JPanel pointDefinition = new JPanel();
         pointDefinition.setLayout(new BoxLayout(pointDefinition, BoxLayout.X_AXIS));
         this.jL_Point1 = new JLabel("Point 1 :");
         this.jL_Point2 = new JLabel("Point 2 :");
         this.jL_Point3 = new JLabel("Point 3 :");
         this.jL_Point4 = new JLabel("Point 4 :");
         this.jTF_Point1 = new JTextField(4);
         this.jTF_Point2 = new JTextField(4);
         this.jTF_Point3 = new JTextField(4);
         this.jTF_Point4 = new JTextField(4);
         pointDefinition.add(this.jL_Point1);
         pointDefinition.add(this.jTF_Point1);
         pointDefinition.add(this.jL_Point2);
         pointDefinition.add(this.jTF_Point2);
         pointDefinition.add(this.jL_Point3);
         pointDefinition.add(this.jTF_Point3);
         pointDefinition.add(this.jL_Point4);
         pointDefinition.add(this.jTF_Point4);
         JButton bt_valid = new JButton("Valider");
         pointDefinition.add(bt_valid);
         center.add(pointDefinition, BorderLayout.NORTH);
 
         content.add(center, BorderLayout.CENTER);
         //
 
         //EAST
         this.east = new JPanel();
         //String subject[] = {"Math", "Computer", "Phisics", "Chemestry"};
         east.setLayout(new BorderLayout());
         jl_Polygones = new JList<String>();
         jl_Polygones.setPreferredSize(new Dimension(120, 400));
         jl_Polygones.setBackground(Color.lightGray);
         jl_Polygones.setForeground(Color.BLACK);
         jl_Polygones.setSelectionModel(new listSelectionModel(this.monJCanvas));
         ListSelectionModel listSelectionModel = jl_Polygones.getSelectionModel();
         listSelectionModel.addListSelectionListener(new SharedListSelectionHandler(this.monJCanvas));
         east.add(jl_Polygones, BorderLayout.CENTER);
         east.add(jl_Polygones);
         ActionSauvegarde ac_sauvegarder = new ActionSauvegarde("Sauvegarder", this.monProgramme);
         JButton bt_sauvegarder = new JButton(ac_sauvegarder);
 
         ActionChargement ac_charger = new ActionChargement("Charger", this.monProgramme);
         JButton bt_charger = new JButton(ac_charger);
 
         JPanel boutons = new JPanel();
         boutons.add(bt_sauvegarder);
         boutons.add(bt_charger);
 
         east.add(boutons, BorderLayout.SOUTH);
         content.add(east, BorderLayout.EAST);
         //
 
         //WEST
         this.west = new JPanel();
         west.setLayout(new BorderLayout());
 
         this.monActionCarre = new ActionCarre("Créer un carré", monProgramme);
         JButton carre = new JButton(this.monActionCarre);
 
         this.monActionRectangle = new ActionRectangle("Créer un rectangle", monProgramme);
         JButton rectangle = new JButton(this.monActionRectangle);
 
         this.monActionCerfVolant = new ActionCerfVolant("Créer un Cerf Volant", monProgramme);
         JButton cerfVolant = new JButton(this.monActionCerfVolant);
 
         this.monActionLosange = new ActionLosange("Créer un losange", monProgramme);
         JButton losange = new JButton(this.monActionLosange);
 
         this.monActionTrapeze = new ActionTrapeze("Créer un trapèze", monProgramme);
         JButton trapeze = new JButton(this.monActionTrapeze);
 
         this.monActionParallelogramme = new ActionParallelogramme("Créer un parallelogramme", monProgramme);
         JButton parallelogramme = new JButton(this.monActionParallelogramme);
 
         JPanel boutons_polygones = new JPanel();
         boutons_polygones.setLayout(new BoxLayout(boutons_polygones, BoxLayout.Y_AXIS));
         boutons_polygones.add(carre);
         boutons_polygones.add(rectangle);
         boutons_polygones.add(cerfVolant);
         boutons_polygones.add(losange);
         boutons_polygones.add(trapeze);
         boutons_polygones.add(parallelogramme);
         west.add(boutons_polygones, BorderLayout.CENTER);
 
         JPanel boutons_couleurs = new JPanel();
 
         ActionCouleur ac = new ActionCouleur("", Color.RED, this);
         JButton rouge = new JButton(ac);
         rouge.setBackground(Color.RED);
         rouge.setPreferredSize(new Dimension(25, 25));
        JButton bleu = new JButton();
        bleu.setBackground(Color.BLUE);
        bleu.setPreferredSize(new Dimension(25, 25));
        JButton jaune = new JButton();
         jaune.setBackground(Color.YELLOW);
         jaune.setPreferredSize(new Dimension(25, 25));
        JButton vert = new JButton();
         vert.setBackground(Color.GREEN);
         vert.setPreferredSize(new Dimension(25, 25));
        JButton orange = new JButton();
         orange.setBackground(Color.ORANGE);
         orange.setPreferredSize(new Dimension(25, 25));
        JButton gris = new JButton();
         gris.setBackground(Color.GRAY);
         gris.setPreferredSize(new Dimension(25, 25));
        JButton noir = new JButton();
         noir.setBackground(Color.BLACK);
         noir.setPreferredSize(new Dimension(25, 25));
 
         boutons_couleurs.add(rouge);
        boutons_couleurs.add(bleu);
         boutons_couleurs.add(jaune);
         boutons_couleurs.add(vert);
         boutons_couleurs.add(orange);
         boutons_couleurs.add(gris);
         boutons_couleurs.add(noir);
 
         west.add(boutons_couleurs, BorderLayout.SOUTH);
 
         content.add(west, BorderLayout.WEST);
         //
 
         //SOUTH
         this.south = new JPanel();
         this.maZoneDeTexte = new JTextArea(15, 80);
         south.add(this.maZoneDeTexte);
         content.add(south, BorderLayout.SOUTH);
         //
 
         //... Set window characteristics.
         setContentPane(content);
         setTitle("BorderTest");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         pack();
         this.setResizable(false);
         setVisible(true);
     }
 
     /**
      * Affiche le texte en parametre dans la zone de texte
      *
      * @param toString
      */
     public void affiche(String toString) {
         this.maZoneDeTexte.setText(toString);
     }
 
     /**
      * Affiche le texte dans la zone de texte ou cache la textbox et son label
      *
      * @param texte
      */
     public void setjTF_Point1_Text(String texte) {
         if (null == texte) {
             this.jTF_Point1.hide();
             this.jL_Point1.hide();
         } else {
             this.jTF_Point1.show();
             this.jL_Point1.show();
             this.jTF_Point1.setText(texte);
         }
     }
 
     /**
      * Affiche le texte dans la zone de texte ou cache la textbox et son label
      *
      * @param texte
      */
     public void setjTF_Point2_Text(String texte) {
         if (null == texte) {
             this.jTF_Point2.hide();
             this.jL_Point2.hide();
         } else {
             this.jTF_Point2.show();
             this.jL_Point2.show();
             this.jTF_Point2.setText(texte);
         }
     }
 
     /**
      * Affiche le texte dans la zone de texte ou cache la textbox et son label
      *
      * @param texte
      */
     public void setjTF_Point3_Text(String texte) {
         if (null == texte) {
             this.jTF_Point3.hide();
             this.jL_Point3.hide();
         } else {
             this.jTF_Point3.show();
             this.jL_Point3.show();
             this.jTF_Point3.setText(texte);
         }
     }
 
     /**
      * Affiche le texte dans la zone de texte ou cache la textbox et son label
      *
      * @param texte
      */
     public void setjTF_Point4_Text(String texte) {
         if (null == texte) {
             this.jTF_Point4.hide();
             this.jL_Point4.hide();
         } else {
             this.jTF_Point4.show();
             this.jL_Point4.show();
             this.jTF_Point4.setText(texte);
         }
     }
 
     /**
      *Ajoute le model dans la jlist pour lui permetre d'avoir des éléments
      * @param model
      */
     public void jl_PolygonesSetModel(DefaultListModel model) {
         this.jl_Polygones.setModel(model);
     }
 }
