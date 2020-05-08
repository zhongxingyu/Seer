 package pkg_mvc;
 
 import java.awt.Insets;
 import java.awt.Dimension;
 import java.awt.Container;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import java.net.URL;
 
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextArea;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.DefaultListModel;
 
 
 /**
  * Programme <b>Who is Jack ?</b><br>
  * Class GameView - Une inteface graphique pour le jeu "Who is jack ?"<br><br>
  * 
  * Cette classe fait parti du jeu "Who is Jack ?"<br>
  * 
  * Elle correspond a la partie "Vue" du pattern MVC<br>
  * Elle génère une interface graphique utilisable pour le jeu "Who is Jack ?"<br>
  *  
  * @author TRAN Anthony - RAVELONANOSY Lova - LE STUM Sébastien - PEYTOUREAU Julie
  * @version 2011.11.28 Version finale
  */
 
 public class GameView implements ActionListener, Observer, ListSelectionListener
 {
     //Attributs
     private GameModel gameModel;
     private Container aCont;
     private JFrame myFrame;
     
     private JButton n;
     private JButton s;
     private JButton e;
     private JButton w;
     private JButton nw;
     private JButton ne;
     private JButton se;
     private JButton sw;
     private JButton up;
     private JButton dn;
     private JButton take;
     private JButton drop;
     private JButton use;
     private JButton quit;
     private JButton look;
     private JLabel image;
     private JLabel nomSalle = new JLabel();
     private JLabel nomListe1 = new JLabel();
     private JLabel nomListe2 = new JLabel();
     private JLabel poidsInventaire = new JLabel();
     
     private JTextField entryField;
     private JTextArea log;
     
     private JList liste1;
     private JList liste2;
     private JScrollPane liste1Scroll;
     private JScrollPane liste2Scroll;
 
     private DefaultListModel listObjectRoom;
     private DefaultListModel listPlayer;
     
     private String selectL1;
     private String selectL2;
     
      /**
      * Construit une interface graphique en lien avec le gameModel du jeu
      * 
      * @param gameModel Le gameModel implémentant la logique du jeu
      */
     public GameView(GameModel gameModel,Container Cont)
     {
         this.gameModel = gameModel;
         this.aCont = Cont;
         createGUI();
     }//GameView()
     
     /**
      * Affiche des informations dans la zone de texte
      */
     public void print(String text)
     {
         log.append(text);
         log.setCaretPosition(log.getDocument().getLength());
     }//print()
     
     /**
      * Affiche des informations dans la zone de texte, suivi d'un saut de ligne.
      */
     public void println(String text)
     {
         log.append(text + "\n");
         log.setCaretPosition(log.getDocument().getLength());
     }//println()
     
     /**
      * Affiche une image dans l'interface graphique
      */
     public void showImage(String imageName)
     {
         ImageIcon icon;
         URL imageURL = this.getClass().getClassLoader().getResource(imageName);
         if(imageURL == null){
             System.out.println("ATTENTION : Image introuvable !");
             icon = new ImageIcon("img/blank.jpg");
         } else {
             icon = new ImageIcon(imageURL);
         }//else
         image.setIcon(icon);
     }//showImage()
     
     /**
      * Active ou désactive la possibilité de saisir
      */
     public void enable(boolean on)
     {
         entryField.setEditable(on);
         if(!on)
             entryField.getCaret().setBlinkRate(0);
     }//enable()
     
     /**
      * Procédure qui initialise les deux listes utilisées pour gérer les objets
      */
     private void initLists(){
         listObjectRoom = new DefaultListModel();
         listPlayer = new DefaultListModel();
         for(String element : gameModel.getCurrentRoom().getObjectRoom().getItemList().keySet()){
             listObjectRoom.addElement(element);
         }
         
         for(String element : gameModel.getPlayer().getObjectPlayer().getItemList().keySet()){
             listPlayer.addElement(element);
         }
         
         liste1 = new JList(listObjectRoom);
         liste2 = new JList(listPlayer);
         liste1Scroll = new JScrollPane(liste1);
         liste1Scroll.setPreferredSize(new Dimension(80, 70));
         liste2Scroll = new JScrollPane(liste2);
         liste2Scroll.setPreferredSize(new Dimension(80, 70));
     }
     
     /**
      * Procédure qui met a jour les deux listes en fonction des actions (Ramassage/dépot)
      */
     public void update_lists_labels(){
         listObjectRoom.removeAllElements();
         listPlayer.removeAllElements();
         for(String element : gameModel.getCurrentRoom().getObjectRoom().getItemList().keySet()){
             listObjectRoom.addElement(element);
         }
         
         for(String element : gameModel.getPlayer().getObjectPlayer().getItemList().keySet()){
             listPlayer.addElement(element);
         }
         nomSalle.setText(GameControl.getGameModel().getCurrentRoom().getName());
         poidsInventaire.setText("Poids inventaire : "+GameControl.getGameModel().getPlayer().getObjectPlayer().getPoidsCourant()+
                                              "/"+GameControl.getGameModel().getPlayer().getObjectPlayer().getPoidsMax());
     }
     
     /**
      * Ajoute les différents composants a la fenetre dont le container est pane
      * @param pane Le container de la fenetre a charger
      */
     public void addComponentsToPane(Container pane) {  
         
         pane.setLayout(null);          
         image = new JLabel();
         n = new JButton("N");
         s = new JButton("S");
         e = new JButton("E");
         w = new JButton("O");
         ne = new JButton("N-E");
         nw = new JButton("N-O");
         se = new JButton("S-E");
         sw = new JButton("S-O");
         up = new JButton("Haut");
         dn = new JButton("Bas");
         take = new JButton("Prendre");
         drop = new JButton("Deposer");
         use = new JButton("Utiliser");
         quit = new JButton("Quitter");
         look = new JButton("Regarder");
         
         entryField = new JTextField(34);
         nomSalle.setText(GameControl.getGameModel().getCurrentRoom().getName());
         poidsInventaire.setText("Poids inventaire : "+GameControl.getGameModel().getPlayer().getObjectPlayer().getPoidsCourant()+
                                              "/"+GameControl.getGameModel().getPlayer().getObjectPlayer().getPoidsMax());
         nomListe1.setText("Objets de la salle :");
         nomListe2.setText("Objets du joueur :");
         log = new JTextArea();
         log.setEditable(false);
         JScrollPane listScroller = new JScrollPane(log);
         listScroller.setPreferredSize(new Dimension(200, 200));
         listScroller.setMinimumSize(new Dimension(100,100));
         
         pane.add(look);
         pane.add(nomSalle);
         pane.add(nomListe1);
         pane.add(nomListe2);
         pane.add(poidsInventaire);
         pane.add(listScroller);         
         pane.add(image);         
         pane.add(entryField);
         pane.add(n);
         pane.add(s);
         pane.add(e);
         pane.add(w);
         pane.add(ne);
         pane.add(nw);
         pane.add(se);
         pane.add(sw);
         pane.add(up);
         pane.add(dn);
         pane.add(liste1Scroll);
         pane.add(liste2Scroll);
         pane.add(take);
         pane.add(drop);
         pane.add(use);
         pane.add(quit);
         
         Insets insets = pane.getInsets();
         nomListe1.setBounds(10+insets.left,300+insets.top,250,20);
         nomListe2.setBounds(220+insets.left,300+insets.top,250,20);
         poidsInventaire.setBounds(220+insets.left, 420 + insets.top, 200,20); 
         nomSalle.setBounds(10+ insets.left, 5 + insets.top,400,20);
         image.setBounds(10+ insets.left, 10 + insets.top,400,300);   
         listScroller.setBounds(420 + insets.left, 10 + insets.top, 510-insets.left, 260);
         entryField.setBounds(420+ insets.left, 280 + insets.top, 510-insets.left,30); 
         n.setBounds((1340-insets.left)/2-35,325+insets.top,65,20);
         nw.setBounds((1190-insets.left)/2-35,325+insets.top,65,20);
         ne.setBounds((1495-insets.left)/2-35,325+insets.top,65,20);
         s.setBounds((1340-insets.left)/2-35,385+insets.top,65,20);
         sw.setBounds((1190-insets.left)/2-35,385+insets.top,65,20);
         se.setBounds((1495-insets.left)/2-35,385+insets.top,65,20);
         w.setBounds((1190-insets.left)/2-35,355+insets.top,65,20);
         e.setBounds((1495-insets.left)/2-35,355+insets.top, 65,20);
         up.setBounds((1675-insets.left)/2-35,325+insets.top,65,20);
         dn.setBounds((1675-insets.left)/2-35,385+insets.top,65,20);
         liste1Scroll.setBounds(10 + insets.left,325+insets.top,190,80);
         liste2Scroll.setBounds(220 + insets.left,325+insets.top,190,80);
         
         take.setBounds((980-insets.left)/2-35,325+insets.top,100,20);
         drop.setBounds((980-insets.left)/2-35,355+insets.top,100,20);
         use.setBounds((980-insets.left)/2-35,385+insets.top,100,20);
         look.setBounds((980-insets.left)/2-35,415+insets.top,100,20);
         quit.setBounds(10 + insets.left,420,105,20);
     }   
     
     /**
      * Etablit l'interface utilisateur
      */
     private void createGUI()
     {
         initLists();
                 
         if(aCont == null){
             myFrame = new JFrame();
             addComponentsToPane(myFrame.getContentPane()); 
             myFrame.setSize(960,500);  
             myFrame.addWindowListener(new myFrameCloser());
             myFrame.setTitle("Who is Jack ?");
             myFrame.setLocationRelativeTo(null);
             myFrame.setVisible(true);
         } else {
             addComponentsToPane(aCont);
         }
         
         n.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("aller Nord"); 
                                     
                                 }
                             });
         s.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("aller Sud");
                                     
                                 }
                             });
         e.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("aller Est"); 
                                     
                                 }
                             });
         w.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                   GameControl.play("aller Ouest");
                                  
                                 }
                             });
         ne.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                    GameControl.play("aller Nord-Est"); 
                                    
                                 }
                             });
         nw.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                    GameControl.play("aller Nord-Ouest"); 
                                    
                                 }
                             });
         se.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("aller Sud-Est");
                                     
                                 }
                             });
         sw.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("aller Sud-Ouest"); 
                                     
                                 }
                             });
         up.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("aller en-haut"); 
                                    
                                 }
                             });
         dn.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("aller en-bas"); 
                                    
                                 }
                             });
         use.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("utiliser "+selectL2);
                                     
                                 }
                             });
         look.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("regarder "+selectL2);
                                     
                                 }
                             });
                             
         drop.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("deposer "+selectL2);
                                    
                                                                     }
                             });
         take.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     GameControl.play("prendre "+selectL1);
                                     
                                 }
                             });
         quit.addActionListener(new ActionListener() 
                             {
                                 public void actionPerformed(ActionEvent e)
                                 {
                                     killFrame();
                                     
                                 }
                             });
         entryField.addActionListener(this);
         liste1.addListSelectionListener(this);
         liste2.addListSelectionListener(this);
     }//createGUI()
 
     /**
      * ActionListener pour une action effectué sur un objet de l'interface
      */
     public void actionPerformed(ActionEvent e) 
     {
         processCommand();
     } 
         
     public void valueChanged(ListSelectionEvent evt)
     {
        selectL1 = (String) liste1.getSelectedValue(); 
        selectL2 = (String) liste2.getSelectedValue();
     }
     
     /**
      * Une commande a été saisie, récupere cette commande et la renvoie pour qu'elle soit interperer
      */
     private void processCommand()
     {
         String input = entryField.getText();
         entryField.setText("");
         GameControl.play(input);
     }//processCommand()
     
     /**
      * Affiche le message de bienvenue
      */
     public void printWelcome()
     {
         display_message("Bienvenue !","\n" + gameModel.getWelcomeString() + "\n",JOptionPane.INFORMATION_MESSAGE);
         printLocationInfo();
     }//printWelcome()
         
     /**
      * Affiche des informations sur la pièce courante
      */
     public void printLocationInfo()
     {
         println(gameModel.getLongDescription());
     }//printLocationInfo()
         
     /**
      * Affiche un message de remerciement
      */
     public void printGoodBye() 
     {
         display_message("Au revoir !",gameModel.getGoodByeString(),JOptionPane.INFORMATION_MESSAGE);
     }//printGoodBye()
    
     /**
      * Fonction de mise a jour de l'observateur lorsque qu'un signal notifyObserver() est reconnu
      */
     public void update(Observable o, Object arg)
     {
         printLocationInfo();
         if(gameModel.getCurrentRoom().getImageName() != null){
                showImage(gameModel.getCurrentRoom().getImageName());
         }
     }//update()
     
     /**
      * Procédure qui ferme la fenetre en cours en gérant le cas Applet/Execution Classique
      */
     public void killFrame()
     {
       printGoodBye();
       if(aCont ==null){
         myFrame.dispose();
       }
       else{
         aCont.setVisible(false);  
       }
     }
     
     /**
      * Affiche un message dans une boite de dialogue 
      */
     public void display_message(final String titre, final String message, final int optionType)
     {
         if(!(message.equals("vide") || message.equals("")))
         JOptionPane.showMessageDialog(null,message,titre,optionType);
     }
     
     /**
      * ???????????????????????,,
      */
     class myFrameCloser extends WindowAdapter
     {
         @Override      
         public void windowClosing(WindowEvent e) 
         {        
             killFrame();
         }
     }
 }//GameView
