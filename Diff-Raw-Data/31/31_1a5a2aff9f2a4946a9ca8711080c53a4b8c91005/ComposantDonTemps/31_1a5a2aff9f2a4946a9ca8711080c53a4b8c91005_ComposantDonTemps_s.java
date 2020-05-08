 package fr.mercredymurderparty.ihm.composants;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import fr.mercredymurderparty.client.CoeurClient;
 import fr.mercredymurderparty.ihm.fenetres.FenetreJoueur;
 import fr.mercredymurderparty.outil.BaseDeDonnees;
 import fr.mercredymurderparty.outil.Message;
 import fr.mercredymurderparty.outil.StructureDonTemps;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 
 @SuppressWarnings("serial")
 public class ComposantDonTemps extends JPanel
 {
 // ----- ATTRIBUTS ----- //
 	private JPanel panelDonTemps;
 	private JLabel labelDonTemps;
 	private JLabel labelDonTempsJoueur;
 	private JLabel labelDonTempsDuree;
 	private JButton boutonDonTemps;
 	private JTextField champsDonTempsJoueur;
 	private JSpinner champsDonTempsDuree;
 	
 	private CoeurClient joueur;
 
 // ----- CONSTRUCTEUR ----- //
 	/**
 	 * 
 	 */
 	public ComposantDonTemps(FenetreJoueur _fenetre, final SpringLayout _springLayout, JButton _bouton, CoeurClient _coeurClient) 
 	{
 		joueur = _coeurClient;
 		
 		panelDonTemps = new JPanel();
 		panelDonTemps.setOpaque(false);
 		SpringLayout slPanelDonTemps = new SpringLayout();
 		panelDonTemps.setLayout(slPanelDonTemps);
 		_springLayout.putConstraint(SpringLayout.NORTH, panelDonTemps, 10, SpringLayout.SOUTH, _bouton);
 		_springLayout.putConstraint(SpringLayout.SOUTH, panelDonTemps, 200, SpringLayout.SOUTH, _bouton);
 		_springLayout.putConstraint(SpringLayout.WEST, panelDonTemps, 0, SpringLayout.WEST, _bouton);
 		_springLayout.putConstraint(SpringLayout.EAST, panelDonTemps, 250, SpringLayout.WEST, _bouton);
 		_fenetre.getContentPane().add(panelDonTemps);
 		
 		labelDonTemps = new JLabel("Donner du temps  un joueur :");
 		slPanelDonTemps.putConstraint(SpringLayout.NORTH, labelDonTemps, 10, SpringLayout.NORTH, panelDonTemps);
 		slPanelDonTemps.putConstraint(SpringLayout.WEST, labelDonTemps, 10, SpringLayout.WEST, panelDonTemps);
 		panelDonTemps.add(labelDonTemps);
 		
 		labelDonTempsJoueur = new JLabel("Joueur :");
 		slPanelDonTemps.putConstraint(SpringLayout.NORTH, labelDonTempsJoueur, 10, SpringLayout.SOUTH, labelDonTemps);
 		slPanelDonTemps.putConstraint(SpringLayout.WEST, labelDonTempsJoueur, 10, SpringLayout.WEST, panelDonTemps);
 		panelDonTemps.add(labelDonTempsJoueur);
 		
 		labelDonTempsDuree = new JLabel("Dure :");
 		slPanelDonTemps.putConstraint(SpringLayout.NORTH, labelDonTempsDuree, 10, SpringLayout.SOUTH, labelDonTempsJoueur);
 		slPanelDonTemps.putConstraint(SpringLayout.WEST, labelDonTempsDuree, 10, SpringLayout.WEST, panelDonTemps);
 		panelDonTemps.add(labelDonTempsDuree);
 		
 		champsDonTempsJoueur = new JTextField();
 		slPanelDonTemps.putConstraint(SpringLayout.NORTH, champsDonTempsJoueur, 0, SpringLayout.NORTH, labelDonTempsJoueur);
 		slPanelDonTemps.putConstraint(SpringLayout.WEST, champsDonTempsJoueur, 10, SpringLayout.EAST, labelDonTempsJoueur);
 		slPanelDonTemps.putConstraint(SpringLayout.EAST, champsDonTempsJoueur, 100, SpringLayout.WEST, champsDonTempsJoueur);
 		panelDonTemps.add(champsDonTempsJoueur);
 		
 		champsDonTempsDuree = new JSpinner();
 		slPanelDonTemps.putConstraint(SpringLayout.NORTH, champsDonTempsDuree, 0, SpringLayout.NORTH, labelDonTempsDuree);
 		slPanelDonTemps.putConstraint(SpringLayout.WEST, champsDonTempsDuree, 0, SpringLayout.WEST, champsDonTempsJoueur);
 		slPanelDonTemps.putConstraint(SpringLayout.EAST, champsDonTempsDuree, 100, SpringLayout.WEST, champsDonTempsDuree);
 		panelDonTemps.add(champsDonTempsDuree);
 		
 		boutonDonTemps = new JButton("Donner");
 		boutonDonTemps.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				donnerTemps();
 			}
 		});
 		slPanelDonTemps.putConstraint(SpringLayout.NORTH, boutonDonTemps, 10, SpringLayout.SOUTH, labelDonTempsDuree);
 		slPanelDonTemps.putConstraint(SpringLayout.SOUTH, boutonDonTemps, 40, SpringLayout.SOUTH, labelDonTempsDuree);
 		slPanelDonTemps.putConstraint(SpringLayout.WEST, boutonDonTemps, 10, SpringLayout.WEST, panelDonTemps);
 		slPanelDonTemps.putConstraint(SpringLayout.EAST, boutonDonTemps, 160, SpringLayout.WEST, panelDonTemps);
 		panelDonTemps.add(boutonDonTemps);
 	}
 	
 	
 // ----- METHODES ----- //
 	
 	/**
 	 * rend visible le composant ou pas
 	 * @param : true : rend visible le composant
 	 * 	   <br/>false : masque le composant
 	 */
 	final public void estVisible(boolean _valeur)
 	{
 		panelDonTemps.setVisible(_valeur);
 	}
 	
 	/**
 	 * mthode qui permet d'changer du temps entre joueur
 	 */
 	final public void donnerTemps()
 	{
 		// On teste l'existance du joueur et si le joueur rentr est diffrent du joueur connect
 		BaseDeDonnees.envoyerRequete("SELECT id FROM personnage WHERE login = '" + champsDonTempsJoueur.getText() + "'", joueur, "id");
 		Integer idPerso = (Integer)joueur.getResultatSQL();
 		
 		if( idPerso != null && !(champsDonTempsJoueur.getText().equals(joueur.getLogin())))
 		{
 			// On teste la dure pour savoir si c'est un entier valide suprieur  0
 			if( (champsDonTempsDuree.getValue().toString()).matches("[0-9]+") )
 			{
 				// On rcupre le temps du joueur RECEVEUR
 				BaseDeDonnees.envoyerRequete("SELECT temps FROM personnage WHERE login = '" + champsDonTempsJoueur.getText() + "'", joueur, "temps");
 				Integer tempsDonne = (Integer)joueur.getResultatSQL() + (Integer)champsDonTempsDuree.getValue();
 				System.out.println("Temps Donneur avant modif : ");
 				// On rcupre le temps du joueur DONNEUR
 				BaseDeDonnees.envoyerRequete("SELECT temps FROM personnage WHERE login = '" + joueur.getLogin() + "'", joueur, "temps");
 				Integer tempsPris = (Integer)joueur.getResultatSQL() - (Integer)champsDonTempsDuree.getValue();
 				
 				// On teste si le joueur a suffisamment de temps en rserve
 				if( (Integer)champsDonTempsDuree.getValue() < (Integer)joueur.getResultatSQL() )
 				{
 					StructureDonTemps strucDonTemps = new StructureDonTemps (champsDonTempsJoueur.getText(), joueur.getLogin(), (Integer)champsDonTempsDuree.getValue());
 					try 
 					{
 						joueur.getClient().getSortie().writeObject(new Message(joueur.getIdClient(), Message.DEMANDE_DON_TEMPS, strucDonTemps));
 					} 
 					catch (IOException e) 
 					{
 						e.printStackTrace();
 					}	
 					labelDonTemps.setText("<html>Succs ! <br/>Le matre du jeu validera<br/>ou non cette action.</html>");
 				}
 				else
 				{
 					labelDonTemps.setText("Pas assez de temps disponible !");
 				}
 			}
 			else
 			{
 				labelDonTemps.setText("Il faut rentrer un chiffre valide !");
 			}
 		}
 		else
 		{
 			labelDonTemps.setText("<html>Le joueur n'existe pas<br/>ou vous avez rentr votre pseudo !</html>");
 		}
 	}
 	
 	
 // ----- Getters & Setters ----- //
 	
 	/**
 	 * Getter du coeur client joueur
 	 * @return CoeurClient
 	 */
 	public CoeurClient getJoueur() 
 	{
 		return joueur;
 	}
 
 	/**
 	 * Setter du coeur client joueur
 	 * @param CoeurClient avec le joueur
 	 */
 	public void setJoueur(CoeurClient joueur) 
 	{
 		this.joueur = joueur;
 	}
 }
