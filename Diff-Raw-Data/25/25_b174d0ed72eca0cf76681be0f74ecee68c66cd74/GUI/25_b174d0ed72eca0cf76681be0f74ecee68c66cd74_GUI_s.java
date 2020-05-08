 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package java_backend;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 
 /**
  * @author Jelle
  * @author Daniel
  */
 public class GUI extends JFrame implements ActionListener {
     private JLabel  card1logo, lWachtwoord, lEmailadres;
     private JButton btNieuw, btLogin, btNorth;
     private CardLayout cl = new CardLayout();
     private JPanel cardHolder = new JPanel(cl);
     private JPanel login, logincenter;
     private JTextField tfEmailadres;
     private JPasswordField pfWachtwoord;
     private Persoon persoon;
     //private JPanel jInputfields, jNorth, jWest, jEast, jSouth, jFieldPanel;
     
     public GUI() {
         //Standaard instellingen hoofdscherm
         super();
         this.setTitle("TZT Post");
         this.setLayout(new FlowLayout());
         this.setDefaultCloseOperation(EXIT_ON_CLOSE);
         this.setResizable(false);
         this.setSize(800, 600);        
         //set persoon op null om aan te tonen dat er geen ingelogde is.
         this.persoon = null;
         
         //Instellingen eerste pagina.
         JPanel Card1 = new JPanel();
         Card1.setLayout(new BorderLayout());
         //Card1.add();
         this.card1logo = new JLabel();
         
         //nog even kijken naar logo bovenaan de pagina met laden afbeelding van internet.
         //this.card1logo.setIcon(new javax.swing.ImageIcon("http://www.tztpost.nl/tztklein.png"));
         ////this.card1logo.setIcon(new javax.swing.ImageIcon("C:\\Users\\Jelle\\Pictures\\zoidberg.png"));
 
         
         //loginscherm voor card1
         this.login = new JPanel();
         this.logincenter = new JPanel();
         //kies borderlayout
         this.login.setLayout(new BorderLayout());
         this.logincenter.setLayout(new GridLayout(3, 2));
         //instancieer buttons en velden.
         this.btLogin = new JButton("Log in");
         this.tfEmailadres = new JTextField(20);
         this.pfWachtwoord = new JPasswordField(10);
         
         this.lWachtwoord = new JLabel("Wachtwoord:");
         this.lEmailadres = new JLabel("E-mailadres:");
         
         
         //voeg velden toe aan login center
         this.logincenter.add(lEmailadres);
         this.logincenter.add(tfEmailadres);
         this.logincenter.add(lWachtwoord);
         this.logincenter.add(pfWachtwoord);
         this.logincenter.add(btLogin);
                 
         //voeg velden toe aan loginpanel
         this.login.add(this.logincenter);
         
         //voeg logo toe aan loginpanel
         Card1.add(this.card1logo, BorderLayout.NORTH);
         //voeg panel toe aan loginscherm.
         Card1.add(this.login, BorderLayout.CENTER);
         
         //instellingen tweede pagina/card.
         JPanel Card2 = new AccountsBeherenTabel();
         cardHolder.add(Card1);
         cardHolder.add(Card2);
         this.add(cardHolder);
 
         //Actionlistener toevoegen voor btLogin.
         btLogin.addActionListener(this);
                    this.cl.next(this.cardHolder);
    
         
         this.setVisible(true);
     }
     
     public void actionPerformed(ActionEvent ae) {
             //Acties voor login button
             if (ae.getSource() == btLogin) {
                 //instancieer databaseconnectie
                 DbConnect a = new DbConnect();
             try {
                 //controleer de gegevens. allebei niet leeg.
                 if(tfEmailadres.getText().equals("") || pfWachtwoord.getText().equals("")){
                     //show message dialog met foutmelding.
                     JOptionPane.showMessageDialog(rootPane, "Niet alle verplichte velden zijn ingevuld.", "Waarschuwing", 2);
                 }else{
                     //maak Persoon aan aan de hand van inloggegevens en methode in inlogdata
                     this.persoon = a.getLoginData(tfEmailadres.getText(), pfWachtwoord.getText(), true);
                     if(this.persoon != null){
                         //succesvol ingelogd
                         
                         //alleen bij succesvolle inlog mag iemand door naar de volgende card.
                         this.cl.next(this.cardHolder);
                     } else{
                         //show error dialog, inloggen is niet gelukt, object is leeg (controle en afhandeling in methode getLoginData
                         JOptionPane.showMessageDialog(rootPane, "Het inloggen is niet gelukt!", "Waarschuwing", 2);
                         //zet het formulier weer op leeg om opnieuw in te kunnen loggen.
                         tfEmailadres.setText("");
                         pfWachtwoord.setText("");    
                         //mag niet door naar de volgende card.
                     }
                 }   
             } catch (SQLException ex) {
                 Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
             }   
         
             }                
         }
 }
