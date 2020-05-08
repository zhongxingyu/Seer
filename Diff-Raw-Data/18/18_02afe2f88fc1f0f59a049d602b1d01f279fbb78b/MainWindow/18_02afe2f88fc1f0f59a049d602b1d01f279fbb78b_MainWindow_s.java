 package game;
 
 import element.XMLLoadableElement;
 import element.plant.Plant;
 import element.creature.Creature;
 import element.mission.Mission;
 import game.CreaturePool;
 import xml.GlobalProperties;
 
 import t2s.SIVOXDevint;
 
 import javax.swing.Timer;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.ImageIcon;
 import javax.swing.DefaultListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JOptionPane;
 
 import java.awt.Font;
 import java.awt.Dimension;
 import java.awt.Component;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowAdapter;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusAdapter;
 import java.awt.image.BufferedImage;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 /*
  * Classe gérant la fenêtre principale
  *  ainsi que les évènements
  *
  */
 public class MainWindow extends JFrame {
 	// listes de toutes les missions
     protected List<Mission>   missions     = new LinkedList<Mission>();
     // plantes disponibles pour la mission courante
     private List<Plant>       plants       = new LinkedList<Plant>();
     // insectes nécessaires pour valider la mission courante
     private Map<String,Integer> insects    = new HashMap<String,Integer>();
     // nombres de graînes utilisées
     private Map<String,Integer> seedsUsed  = new HashMap<String,Integer>();
     // plantes en terre
     private List<Plant> plantedPlants    = new ArrayList<Plant>();
     // mission courante
     private Mission          currentMission;
     // police par défaut pour tous les textes du jeu
     private Font             defaultFont = new Font(null, Font.BOLD, GlobalProperties.getInteger("font_size"));
     // timer pour la boucle du temps
     private Timer            timer;
     // temps entre chaque ré-affichage
     // boucle principale (en ms)
     private final static int DELAY       = 100;
 
     private JLabel           statusBar     = new JLabel("Initialisation");
     private DefaultListModel goalList      = new DefaultListModel();
     private JList            goalListView  = new JList(goalList);
     private JProgressBar     levelBar      = new JProgressBar(JProgressBar.VERTICAL, 0, 100);
     private DefaultListModel seedList      = new DefaultListModel();
     private JList            seedListView  = new JList(seedList);
     private GameView         gameView      = new GameView(plantedPlants);
     private SIVOXDevint      player        = new SIVOXDevint();
 
     // créatures apparus lors de la pousse des plantes
     private List<Creature>   insectsOnGame = gameView.getCreaturesOnGame();
 
     public MainWindow() {
         // chargement de toutes les missions
         loadMissions();
 
         // changement du type de rendu de la liste pour l'affichage des images
         seedListView.setCellRenderer(new PlantCellRenderer());
         seedListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         // autoriser l'affichage d'un message dans la progress bar
         //  augmentation de la taille du texte
         levelBar.setStringPainted(true);
         levelBar.setFont(defaultFont);
 
         goalListView.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         goalListView.setVisibleRowCount(1);
         goalListView.setCellRenderer(new CreatureCellRenderer());
         goalListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         goalListView.addFocusListener(new FocusAdapter() {
             public void focusGained(FocusEvent e) {
                 seedListView.grabFocus();
             }
         });
 
         // augmentation de la taille du texte de la barre de status
         statusBar.setFont(defaultFont);
 
         // gestion des événements
         seedListView.addKeyListener(new KeyAdapter() {
             public void keyPressed(KeyEvent e) {
                 // espace => ajouter de l'eau
                 if (KeyEvent.VK_SPACE == e.getKeyCode()) {
                     int i = gameView.getSelectedHoleIndex();
                     if (plantedPlants.get(i) != null)
                    		plantedPlants.get(i).incrWater();
                 // entrée => planter la plante
                 } else if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                     int i = gameView.getSelectedHoleIndex();
                     if (plantedPlants.get(i) == null) {
                         try {
                             // récupère la plante dans la liste des graînes
                             // et la clone pour en faire un nouvel objet
                             Plant newPlant = ((Plant) seedListView.getSelectedValue()).clone();
                             if (seedsUsed.containsKey(newPlant.ID())) {
                                 if (seedsUsed.get(newPlant.ID()) < currentMission.plants().get(newPlant.ID())) {
                                     seedsUsed.put(newPlant.ID(), seedsUsed.get(newPlant.ID()) + 1);
                                     plantedPlants.set(i, newPlant);
                                     gameView.updatePlantedPlants();
                                 } else {
                                     play("Tu n'as plus de graine de "+newPlant.name());
                                 }
                             } else {
                                 seedsUsed.put(newPlant.ID(), 1);
                                 plantedPlants.set(i, newPlant);
                                 gameView.updatePlantedPlants();
                             }
                             if (seedsUsed.get(newPlant.ID()) >= currentMission.plants().get(newPlant.ID())) {
                                 for (int j = 0; j < seedList.getSize(); ++j)
                                     if (((Plant) seedList.get(j)).ID() == newPlant.ID())
                                         seedList.remove(j);
                                 seedListView.setSelectedIndex(0);
                             }
                         } catch(CloneNotSupportedException ex) {
                             System.err.println("[erreur] Impossible de cloner l'objet plante : " + ex);
                         }
                     }
                     else
                         play("Il ya déjà une plante dans ce trou.", "Il y a déjà une plante dans ce trou.");
                 // droite => sélectionner le trou de droite
                 } else if (KeyEvent.VK_RIGHT == e.getKeyCode()) {
                     gameView.selectNextHole();
                 // gauche => sélectionner le trou de gauche
                 } else if (KeyEvent.VK_LEFT == e.getKeyCode()) {
                     gameView.selectPreviousHole();
                 // échap => quitter le jeu
                 } else if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
                     statusBar.setText("Au revoir !");
                     timer.stop();
                     dispose();
 				// F1 ou HELP => lit l'aide. TODO : mapper aussi le point d'interrogation.
 				} else if (KeyEvent.VK_F1 == e.getKeyCode() || KeyEvent.VK_HELP == e.getKeyCode()) {
 					play("Pour lire un indice, appuie sur F 2. Pour passer à un indice plus précis, appuie sur F 3. Pour lire la description de la mission, appuie sur la touche F ou la touche D. Pour sélectionner une graine, utilise les flèches haut et bas. Appuie sur J pour avoir une description de la graine. Pour choisir un trou où planter la graine, utilise les flèches droite et gauche. Pour planter la graine, appuie sur la touche entrée. Pour arroser la plante, laisse la touche espace enfoncée. Pour quitter, appuie sur la touche échap. Si tu es perdu, commence par faire le tutoriel du jeu !");
                 // f2 => lecture de l'indice courant
 				} else if (KeyEvent.VK_F2 == e.getKeyCode()) {
 					play(currentMission.hints().peek());
                 // f3 => passe à l'indice suivant et le lit
 				} else if (KeyEvent.VK_F3 == e.getKeyCode()) {
 					java.util.concurrent.LinkedBlockingQueue<String> hints = currentMission.hints();
 					if (hints.size() > 1) hints.poll();
 					play(hints.peek());
 				// F => lit la description de la mission
 				} else if (KeyEvent.VK_F == e.getKeyCode() || KeyEvent.VK_D == e.getKeyCode()) {
 					play(currentMission.description());
 				// J => lit la description de la plante
 				} else if (KeyEvent.VK_J == e.getKeyCode()) {
 					play(((Plant) seedListView.getSelectedValue()).description());
				}
             }
        });

        // pronociation du nom de la plante dont la graine est sélectionnée
        seedListView.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Plant p = (Plant) seedListView.getSelectedValue();
                if (p != null)
                    play(p.name());
             }
         });
 
         // scroll pour l'affichage de la liste
         JScrollPane seedScroll = new JScrollPane(seedListView);
         seedScroll.setPreferredSize(new Dimension(200, 400));
 
         // scroll pour l'affichage de la barre de status
         JScrollPane statusScroll = new JScrollPane(statusBar);
         statusScroll.setPreferredSize(new Dimension(200, statusBar.getFontMetrics(statusBar.getFont()).getHeight() + 20));
 
         // scroll pour l'affichage de la barre de progression
         JScrollPane levelScroll = new JScrollPane(levelBar);
         levelScroll.setPreferredSize(new Dimension(110, 400));
 
         // scroll pour l'affichage de la barre d'avancement
         JScrollPane goalScroll = new JScrollPane(goalListView);
         goalScroll.setPreferredSize(new Dimension(100, 110));
 
         getContentPane().add(gameView,     BorderLayout.CENTER);
         getContentPane().add(seedScroll,   BorderLayout.EAST);
         //getContentPane().add(levelScroll,  BorderLayout.WEST);
         getContentPane().add(goalScroll,   BorderLayout.NORTH);
         getContentPane().add(statusScroll, BorderLayout.SOUTH);
 
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         setFullScreen();
         //setSize(700,700);
         setVisible(true);
         addWindowListener(new WindowAdapter() {
             public void windowClosed(WindowEvent e) {
                 timer.stop();
             }
         });
 
         // lancement du jeu
         run();
     }
 
     // vérifie si les objectifs sont atteints
     private boolean checkInsects() {
         // construit la liste des insectes <id, nombre>
         // présent dans le jeu
         Map<String,Integer> ig = new HashMap<String,Integer>();
         for (Creature c : insectsOnGame) {
             if (c.isOnScreen()) {
                 if (ig.containsKey(c.ID())) {
                     ig.put(c.ID(), ig.get(c.ID()) + 1);
                 } else {
                     ig.put(c.ID(), 1);
                 }
             }
         }
 
         // compare la liste des insectes présents
         // avec celle des objectifs
         int nbInsects = 0;
         boolean goalsOk = true;
         DefaultListModel newList = new DefaultListModel();
         for (Map.Entry<String, Integer> e : insects.entrySet()) {
             if (ig.containsKey(e.getKey())) {
                 nbInsects += ig.get(e.getKey());
                 if (ig.get(e.getKey()) < e.getValue()) {
                     goalsOk = false;
                     for (int i = 0; i < e.getValue() - ig.get(e.getKey()); ++i)
                         newList.addElement(CreaturePool.getCreature(e.getKey()));
                 }
             } else  {
                 for (int i = 0; i < e.getValue(); ++i)
                     newList.addElement(CreaturePool.getCreature(e.getKey()));
                 goalsOk = false;
             }
         }
 
         goalListView.setModel(newList);
 
         // met à jour la barre de progression
         levelBar.setValue(nbInsects);
 
         return goalsOk;
     }
 
     // démarre le jeu
     private void run() {
         // chargement de la première mission
         loadMission(0);
 
         ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 // fait pousser les plantes
                 for (Plant p : plantedPlants)
                     if (p != null && !p.isAdult())
                         p.grow();
                 
                 // repeint le plateau de jeu
                 gameView.repaint();
 
                 // vérifie si les objectifs sont atteints
                 if (checkInsects()) {
                     timer.stop();
                     play("Tu as gagné cette mission !", true);
 
                     int newMission = missions.indexOf(currentMission) + 1;
                     if (newMission >= missions.size()) {
                         play("Tu as fini de jouer. Il n'y a plus de niveau disponible.", true);
                         setVisible(false);
                         dispose();
                     } else {
                         play("Niveau suivant");
                         loadMission(newMission);
                         timer.start();
                     }
                 }
             }
         };
         timer = new Timer(DELAY, taskPerformer);
         timer.start();
     }
 
     // prononce le texte et l'affiche dans la barre de status
     // {
     private void play(String readText, String statusText, boolean widthDialog) {
         player.stop();
         player.playText(readText);
         statusBar.setText(statusText);
         if (widthDialog)
             JOptionPane.showMessageDialog(null, statusText, "Information", JOptionPane.INFORMATION_MESSAGE);
     }
 
     private void play(String readText) {
         play(readText, readText, false);
     }
 
     private void play(String readText, boolean widthDialog) {
         play(readText, readText, widthDialog);
     }
 
     private void play(String readText, String statusText) {
         play(readText, statusText, false);
     }
     // }
 
     // charge toutes les missions en listant le répertoire des ressources
     protected void loadMissions() {
         File rep = new File("../ressources/elements/");
         File[] listMissions = rep.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.startsWith("mission_");
             }
         });
 
         Arrays.sort(listMissions);
 
         for (File f : listMissions)
             missions.add(new Mission(f.getName()));
     }
 
     // charge une nouvelle mission
     private void loadMission(int index) {
         currentMission = missions.get(index);
         gameView.setMission(currentMission);
         plantedPlants.clear();
         seedsUsed.clear();
 
         for (int i = 0; i < currentMission.holes(); ++i)
             plantedPlants.add(null);
 
         try {
 	        loadCurrentPlants();
 	        loadCurrentGoals();
         } catch (RuntimeException e) {
         	play("Impossible de charger la mission suivante.", true);
         }
 
         seedListView.requestFocus();
         seedListView.setSelectedIndex(0);
         ((DefaultListModel) goalListView.getModel()).clear();
 
         gameView.repaint();
 
         play(currentMission.description());
     }
 
     // charge la liste des plantes disponibles
     //  pour la mission courante
     private void loadCurrentPlants() {
         plants.clear();
         seedList.clear();
 
         for (Map.Entry<String, Integer> i : currentMission.plants().entrySet()) {
             Plant p = new Plant(i.getKey());
             plants.add(p);
             seedList.addElement(p);
         }
     }
 
     // charge la liste des objectifs
     //  pour la mission courante
     private void loadCurrentGoals() {
         insects.clear();
 
         String msg = "";
         int value  = 0;
         for (Map.Entry<String, Integer> i : currentMission.goal().entrySet()) {
             Creature c = CreaturePool.getCreature(i.getKey());
 
             value += i.getValue();
             msg   += i.getValue() + " " + c.name() + (i.getValue() > 1 ? "s, " : ", ");
 
             insects.put(i.getKey(), i.getValue());
 
             for (int j = 0; j < i.getValue(); ++j) {
                 try { goalList.addElement(c.clone()); }
                 catch(Exception e) {}
             }
         }
 
         // adapte la barre de progression en fonction de la mission
         levelBar.setValue(0);
         levelBar.setMaximum(value);
         levelBar.setString(msg.substring(0, msg.length() - 2));
     }
 
     // bascule en plein écran
     private void setFullScreen() {
         setUndecorated(true);
         setSize(getToolkit().getScreenSize());
         setLocationRelativeTo(null);
         validate();
     }
 
     // classe permettant de modifier l'affichage
     //  de la liste des graînes
     class PlantCellRenderer extends DefaultListCellRenderer {
         public Component getListCellRendererComponent(
             JList list,
             Object value,   // value to display
             int index,      // cell index
             boolean iss,    // is the cell selected
             boolean chf)    // the list and the cell have the focus
         {
             super.getListCellRendererComponent(list, value, index, iss, chf);
 
             setIcon(new ImageIcon(((Plant) value).seedImages().get(0)));
             setText("");
 
             return this;
         }
     }
 
     // classe permettant de modifier l'affichage
     //  de la liste des buts à atteindre
     class CreatureCellRenderer extends DefaultListCellRenderer {
         public Component getListCellRendererComponent(
             JList list,
             Object value,   // value to display
             int index,      // cell index
             boolean iss,    // is the cell selected
             boolean chf)    // the list and the cell have the focus
         {
             super.getListCellRendererComponent(list, value, index, iss, chf);
 
 
             BufferedImage image = ((Creature) value).stillImages().get(0);
             Dimension dimensions = new Dimension(100, 100);
 
             java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
             java.awt.GraphicsDevice gs = ge.getDefaultScreenDevice();
             java.awt.GraphicsConfiguration gc = gs.getDefaultConfiguration();
             
     //		double ratio = image.getWidth() / image.getHeight();
             BufferedImage result = gc.createCompatibleImage((int) (dimensions.getWidth()), (int) (dimensions.getHeight()), java.awt.Transparency.TRANSLUCENT);
             java.awt.Graphics2D g = result.createGraphics();
             g.drawImage(image, 0, 0, (int) (dimensions.getWidth()), (int) (dimensions.getHeight()), null);
             g.dispose();
             
             setIcon(new ImageIcon(result));
             setText("");
 
             return this;
         }
     }
 }
