 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import java.awt.Font;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 import java.awt.Color;
 import javax.swing.JScrollPane;
 
 public class ModifierCompte extends JPanel implements MouseListener
 {
     
     private SqlData info = Client.getInstance().getConnect().request("get_infoMe");
     private SqlData paye = Client.getInstance().getConnect().request("get_paiementMe");
 
     JPanel header = new JPanel();
     JScrollPane centre = new JScrollPane();
     JPanel sud    = new JPanel();
     JButton annuler = new JButton("Annuler");
     JButton ok = new JButton("Ok");
     JButton accueil = new JButton("Accueil");
     JButton deconnexion = new JButton("Déconnexion");
 
     JTextField new_mail = new JTextField(10);
     JTextField new_mdp = new JTextField(10);
     JTextField conf_mail = new JTextField(10);
     JTextField conf_mdp = new JTextField(10);
 
     public ModifierCompte()
     {
 	setSize(779, 456);
         setLayout(new BorderLayout());
         addheader();
         addcentre();
         addsud();
         add("North", header);
         add("Center", centre);
         add("South", sud);
 	setVisible(true);
     }
     
     public void addheader()
     {
         header.setLayout(new GridLayout(1, 3));
         JPanel content_acc = new JPanel();
         JPanel content_text = new JPanel();
         JPanel content_deco = new JPanel();
         content_acc.add(accueil);
         JTextField recherche = new JTextField(20);
         recherche.addKeyListener(new ClavierListener());
         content_text.add(recherche);
         content_deco.add(deconnexion);
         header.add(content_deco);
         header.add(content_text);
         header.add(content_acc);
         accueil.addMouseListener(this);
         deconnexion.addMouseListener(this);
     }
 
     public void addcentre()
     {
 	JPanel grille = new JPanel();
 	JPanel content_mail = new JPanel();
 	JPanel content_mdp = new JPanel();
 	JPanel content_payement = new JPanel();
 	JPanel content_new_mail = new JPanel();
 	JPanel content_conf_mail = new JPanel();
 	JPanel content_new_mdp = new JPanel();
 	JPanel content_conf_mdp = new JPanel();
 	JPanel content_champs_mail = new JPanel();
 	JPanel content_champs_mdp = new JPanel();
 	
 	grille.setLayout(new GridLayout(4, 1));
 
 	grille.add(new JLabel("Ne mettez rien dans les champs de mots de passe ou d'email si vous ne souhaitez pas les changer"));
 	
 	// adresse mail
 	content_mail.setLayout(new BorderLayout());
 
 	content_new_mail.setLayout(new BorderLayout());
 	content_new_mail.add("North", new JLabel("Nouvelle adresse:"));
 	content_new_mail.add("Center", new_mail);
 
 	content_conf_mail.setLayout(new BorderLayout());
 	content_conf_mail.add("North", new JLabel("Confirmer:"));
 	content_conf_mail.add("Center", conf_mail);
 	
 	content_champs_mail.setLayout(new GridLayout(1, 2));
 	content_champs_mail.add(content_new_mail);
 	content_champs_mail.add(content_conf_mail);	
 	
 	content_mail.add("North", new JLabel("Changer d'adresse e-mail"));
 	content_mail.add("Center", content_champs_mail);
 	
 	grille.add(content_mail);	
 
 	// mot de passe 
 	content_mdp.setLayout(new BorderLayout());
 
 	content_new_mdp.setLayout(new BorderLayout());
 	content_new_mdp.add("North", new JLabel("Nouveau mot de passe:"));
 	content_new_mdp.add("Center", new_mdp);
 
 	content_conf_mdp.setLayout(new BorderLayout());
 	content_conf_mdp.add("North", new JLabel("Confirmer:"));
 	content_conf_mdp.add("Center", conf_mdp);
 	
 	content_champs_mdp.setLayout(new GridLayout(1, 2));
 	content_champs_mdp.add(content_new_mdp);
 	content_champs_mdp.add(content_conf_mdp);	
 	
 	content_mdp.add("North", new JLabel("Changer de mot de passe"));
 	content_mdp.add("Center", content_champs_mdp);
 
 	grille.add(content_mdp);
 
 	//paiement
 	content_payement.setLayout(new GridLayout(paye.getNbLigne(),1));
 	JPanel[] payement_grille = new JPanel[paye.getNbLigne()];
 	for(int i = 0 ; i < paye.getNbLigne(); i++)
 	    {
 		payement_grille[i] = new JPanel();
 		payement_grille[i].setLayout(new BorderLayout());
 		payement_grille[i].add("North", new JLabel((paye.data[i][1].equals("1")? "Carte Bleue" : "Paypal")));
 		payement_grille[i].add("Center", new JLabel(paye.data[i][2]));
 		if(paye.data[i][1].equals("1"))
 		    payement_grille[i].add("South", new JLabel(paye.data[i][2]));
 		content_payement.add(payement_grille[i]);
 	    }
 	
 	grille.add(content_payement);
 	
 	centre = new JScrollPane(grille);
 	
     }
     
     public void addsud()
     {
 	sud.setLayout(new GridLayout(1, 2));
 	JPanel content_ok = new JPanel();
 	JPanel content_ann = new JPanel();
 	content_ok.add(ok);
 	content_ann.add(annuler);
 	sud.add(content_ok);
 	sud.add(content_ann);
     }
     
     public void mouseClicked(MouseEvent e)
     {
 	if(e.getSource() == accueil)
 	    {
                 Client.getInstance().getFen().setContentPane(new Accueil());
 	    }
 	if(e.getSource() == deconnexion)
 	    {
 		Client.getInstance().getConnect().dialog("DISCONNECT");
                 Client.getInstance().getFen().setContentPane(new MenuConnexion("GoldenStore - Connexion"));
 	    }
 	if(e.getSource() == ok)
 	    {
		String mail = new_mail.getText();
		if(mail.length() > 0 && mail.equals(conf_mail.getText()))
		   {
		       Client.getInstance().getConnect().request("set_mailMe", mail );
		   }
		String mdp = new_mdp.getText();
		if(mdp.length() > 0 && mail.equals(conf_mdp.getText()))
		    {
		     Client.getInstance().getConnect().request("set_mdpMe", mail );
		    }
		Client.getInstance().getFen().setContentPane(new Accueil());
 	    }
 	if(e.getSource() == annuler)
 	    {
 		Client.getInstance().getFen().setContentPane(new Accueil());
 	    }
     }
     public void mouseEntered(MouseEvent e){}
     public void mouseExited(MouseEvent e){}
     public void mousePressed(MouseEvent e){}
     public void mouseReleased(MouseEvent e){}
 }
