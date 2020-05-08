 import javax.swing.*;
 import java.awt.Dimension;
 import java.awt.Color; //TEMPORAIRE, juste pour le positionnement
 import java.awt.BorderLayout;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 @SuppressWarnings("serial")
 public class Terminal extends JPanel implements KeyListener{
 
     private static JTextArea historique = new JTextArea("Bienvenue sur Carapuce ! Le logiciel fait pour les tortues !\n");
     private static JScrollPane scroll_pane = new JScrollPane(historique, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
     
     private Controleur controleur;
     private int compteur_commandes = -1;
     private String message_erreur = "";
     private JTextField champ_de_commande;
 
     /**
      *  Constructeur du Terminal
      */
 	Terminal()
     {
 
 		this.setBackground(Color.BLACK);//TEMPORAIRE, juste pour le positionnement
         addTerminal();
         this.setLayout(new BorderLayout());
         
         this.add(this.champ_de_commande, BorderLayout.SOUTH);
         this.add(Terminal.scroll_pane, BorderLayout.CENTER);
         
         this.champ_de_commande.addKeyListener(this);
 
     }
     
 
     /**
      *  Evenement lors de la pression des touches
      *  @param keyEvent
      */
     public void keyPressed(KeyEvent keyEvent)
     {
         if ( keyEvent.getKeyCode() == KeyEvent.VK_ENTER
                 && !this.champ_de_commande.getText().equals(""))
         {
            Terminal.historique.append("\n > "+this.champ_de_commande.getText().trim());
             controleur.commande(this.champ_de_commande.getText());
             
             Terminal.historique.setCaretPosition(Terminal.historique.getDocument().getLength());
             
             this.champ_de_commande.setText("");
             this.compteur_commandes = StockageDonnee.getSize_LCEG();
         }
 
         else if ( keyEvent.getKeyCode() == KeyEvent.VK_UP)
         {
             
             if ( compteur_commandes - 1 >= 0 )
             {
                 compteur_commandes--;
                 this.champ_de_commande.setText(StockageDonnee.getLCEG(compteur_commandes));
             }        
             
         }
 
         else if ( keyEvent.getKeyCode() == KeyEvent.VK_DOWN )
         {
 
             if ( compteur_commandes + 1 < StockageDonnee.getSize_LCEG() )
             {
                 compteur_commandes++;
                 this.champ_de_commande.setText(StockageDonnee.getLCEG(compteur_commandes));
             }
             else
             {
                 if ( compteur_commandes + 1 < StockageDonnee.getSize_LCEG()+1 )
                 {
                     compteur_commandes++;
                 }
                 this.champ_de_commande.setText("");
             }
 
         }
 
         else if ( keyEvent.getKeyCode() == KeyEvent.VK_TAB 
                     && !this.champ_de_commande.getText().equals("") )
         {
 
             ArrayList<String> proposition_completion = auto_completion( this.champ_de_commande.getText().trim() );
 
             if ( proposition_completion.size() == 1 )
                 this.champ_de_commande.setText(proposition_completion.get(0));
             else if ( proposition_completion.size() > 1 )
             {
                 String display_proposition = "\n > " + this.champ_de_commande.getText() + "\n";
                 for ( int i = 0; i < proposition_completion.size(); i++)
                     display_proposition += "  " + proposition_completion.get(i);
                 Terminal.historique.append(display_proposition);
             }
             else;
 
         }
 
         else;
 
     }
 
     /**
      *  (IGNORE)
      *  @param keyEvent
      */
     public void keyReleased(KeyEvent keyEvent)
     {
         /*  IGNORE  */
     }
 
     /**
      *  (IGNORE)
      *  @param keyEvent
      */
     public void keyTyped(KeyEvent keyEvent)
     {
         /*  IGNORE  */
     }
 
     /**
      *  Ajoute le terminal, et ajoute les valeurs par défaut
      */
     private void addTerminal()
     {
 
         this.champ_de_commande = new JTextField(50);
         this.champ_de_commande.setBackground(Color.black);
         this.champ_de_commande.setForeground(Color.white);
         this.champ_de_commande.setSize( this.getWidth(), 20 );
         this.champ_de_commande.setFocusable(true);
         this.champ_de_commande.requestFocus();
         this.champ_de_commande.setFocusTraversalKeysEnabled(false);
 
         Terminal.historique.setLineWrap(true);
         Terminal.historique.setWrapStyleWord(true);
         Terminal.historique.setBackground(Color.black);
         Terminal.historique.setForeground(Color.white);
         Terminal.historique.setEnabled(false);
 
 
     }
 
     /**
      *  Fonction d'auto completion
      *  @param s Debut de commande entré par l'utilisateur
      *  @return ArrayList de tous les choix possibles [ de 0 jusqu'à n choix ]
      */
     public ArrayList<String> auto_completion(String s)
     {
 
         ArrayList<String> proposition = new ArrayList<String>();
         Enumeration<String> commandes = StockageDonnee.liste_des_commandes.keys();
 
         while ( commandes.hasMoreElements() )
         {
             String commande_courante = commandes.nextElement();
             if ( (s.length() < commande_courante.length()) 
                     && s.equals(commande_courante.substring(0, s.length())) )
             {
                 proposition.add( commande_courante );
             }
         }
 
         return proposition;
 
     }
 
     /**
      *  Modifie le controleur
      *  @param c nouveau controleur
      */
     public void setControleur(Controleur c)
     {
         this.controleur = c;
     }
 
     /**
      *  Ajoute un message sur l'historique
      *  @param message
      */
     public void addMessage(String message)
     {
         this.historique.append("\n" + message);
     }
 
     /**
      *  Supprime l'affichage courant de l'historique (mais garde en
      *  mémoire les commandes.
      */
     public void clear()
     {
         Terminal.historique.setText("");
     }
 
 }
