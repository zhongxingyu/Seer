 // 
 //
 //  @ Projet : Project Gammon
 //  @ Fichier : ControleurPrincipal.java
 //  @ Date : 12/12/2012
 //  @ Auteurs : DONG Chuan, BONNETTO Benjamin, FRANCON Adrien, POTHELUNE Jean-Michel
 //
 //
 
 package fr.ujm.tse.info4.pgammon.controleur;
 
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JFrame;
 
 import fr.ujm.tse.info4.pgammon.models.Master;
 import fr.ujm.tse.info4.pgammon.models.ParametreJeu;
 import fr.ujm.tse.info4.pgammon.models.Session;
 import fr.ujm.tse.info4.pgammon.vues.VueIntermediairePartie;
 import fr.ujm.tse.info4.pgammon.vues.VueMenu;
 
 public class ControleurPrincipal implements Controleur{
 
 	private VueMenu vueMenu;
 	private Master master;
 	private JFrame frame;
 	private VueIntermediairePartie creationPartie;
 	private ControleurPrincipal controleurPrincipal;
 	
 	public ControleurPrincipal(Master master) {
 		this.master = master;
 		controleurPrincipal= this;
 		frame = new JFrame("jeu de backgammon");
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(816, 638);
 		Container panel = frame.getContentPane();
 		panel.setLayout(new FlowLayout());
 
 		vueMenu = new VueMenu();
 		frame.setContentPane(vueMenu);
 	
 		frame.setVisible(true);
 
 		build();
 	}
 
 	public void build() {
 		listenerButtonQuitter();
 		listenerButtonNouvellePartie();
 		listenerButtonReprendrePartie();
 		listenerButtonAjouter();
 	}
 
 	/**
 	 * 
 	 * listenerButtonNouvellePartie
 	 * 
 	 */	
 	private void listenerButtonNouvellePartie()
 	{
 		vueMenu.getBoutonNouvellePartie().addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}		
 			@Override
 			public void mouseExited(MouseEvent e) {}			
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				vueMenu.setVisible(false);
 				ControleurIntermediairePartie controleurIntermediairePartie = new ControleurIntermediairePartie(true,controleurPrincipal);	
 			}
 		});
 	}
 	
 	private void listenerButtonReprendrePartie()
 	{
 		vueMenu.getBoutonReprendrePartie().addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}		
 			@Override
 			public void mouseExited(MouseEvent e) {}			
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				vueMenu.setVisible(false);
 				ControleurIntermediairePartie	controleurIntermediairePartie = new ControleurIntermediairePartie(false,controleurPrincipal);
 			}
 		});
 	}
 	
 	
 	private void listenerButtonAjouter()
 	{
 		vueMenu.getBoutonAjouter().addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}		
 			@Override
 			public void mouseExited(MouseEvent e) {}			
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				vueMenu.setVisible(false);
 				ControleurListeJoueur controleurListeJoueur = new ControleurListeJoueur(false,controleurPrincipal);	
 			}
 		});
 	}
 	
 	
 	/**
 	 * 
 	 * listenerButtonQuitter
 	 * 
 	 */
 	private void listenerButtonQuitter() {
 		vueMenu.getBoutonQuitter().addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseReleased(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}
 			@Override
 			public void mouseExited(MouseEvent e) {}
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				frame.dispose();
 			}
 		});
 	}
 
 	public JFrame getFrame() {
 		return frame;
 	}
 	@Override
 	public void retour()
 	{
 		frame.setContentPane(vueMenu);
 		vueMenu.setVisible(true);
 	}
 	
 	public void nouvelleSession(ParametreJeu parametreJeu)
 	{
 		master.lancerSession(parametreJeu);
 		Session s1 = master.getSession();
 		s1.LancerPartie();
 		ControleurPartie controleurPartie = new ControleurPartie(s1,this);
 		
 		frame.setContentPane(controleurPartie.getVuePartie());
 		
 	}
 	
 	public void chargerSession(Session session)
 	{
 		master.lancerSession(session.getParametreSession());
 		Session s1 = master.getSession();
 		s1.LancerPartie();
 		ControleurPartie controleurPartie = new ControleurPartie(s1,this);
 		
 		frame.setContentPane(controleurPartie.getVuePartie());
 		
 	}
 
 	@Override
 	public Controleur getControleur() {
 		return this;
 	}
 }
