 package fr.ujm.tse.info4.pgammon.vues;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Paint;
 import java.awt.RadialGradientPaint;
 import java.awt.geom.Point2D;
 import java.util.Set;
 import java.util.SortedSet;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import fr.ujm.tse.info4.pgammon.gui.ChangementTourAnimation;
 import fr.ujm.tse.info4.pgammon.gui.FenetreDemandeAnimationBase;
 import fr.ujm.tse.info4.pgammon.gui.IconMonochromeType;
 import fr.ujm.tse.info4.pgammon.gui.MonochromeButton;
 import fr.ujm.tse.info4.pgammon.gui.MonochromeIconButton;
 import fr.ujm.tse.info4.pgammon.gui.MonochromeLabel;
 import fr.ujm.tse.info4.pgammon.gui.PanelJoueur;
 import fr.ujm.tse.info4.pgammon.gui.TranstionAnimeeBase;
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 import fr.ujm.tse.info4.pgammon.models.EtatSession;
 import fr.ujm.tse.info4.pgammon.models.Partie;
 
 public class VuePartie extends JPanel{
 	private static final long serialVersionUID = 2417367501490643145L;
 	
 	//recuperation de l'image de fond
 	public static final ImageIcon img_fond = new ImageIcon("images/fond_partie.png");
 	
 	private Partie partie;
 	private VueTablier vueTablier;
 	private EtatSession etat;
 	
 	
 	
 	
 	private PanelTermineVueDroite paneldroitrevoir;
 	
 	private PanelEnCoursVueDroite paneldroitencours;
 	
 	private PanelEnCoursVueBas panelEnCoursVueBas;
 	
 	private PanelTermineVueBas panelTermineVueBas;
 	
 	private PanelJoueurVuePartie paneljoueur1;
 	private PanelJoueurVuePartie paneljoueur2;
 	private JPanel animationPanel;
 	
 	public VuePartie(Partie partie) {
 		this.partie = partie;
 		vueTablier = new VueTablier(partie);
 
 		setOpaque(false);
 		build();
 	}
 	
 	private void build() {
 		
 		animationPanel = new JPanel();
 		animationPanel.setLayout(null);
 		animationPanel.setBounds(0,0,800,600);
 		animationPanel.setOpaque(false);
 		add(animationPanel);
 		
 		setPreferredSize(new Dimension(800,600));
 		setOpaque(false);
 
 		setLayout(null);
 		vueTablier.setBounds(173, 30, 547, 446);
 		add(vueTablier);
 		
 		
 		
 		//
 		//
 		//
 		etat=EtatSession.EN_COURS;
 		
 		
 		//creation des panels de la partie
 		paneldroitencours = new PanelEnCoursVueDroite();
 		paneldroitencours.setBounds(720,0,80,476);
 		add(paneldroitencours);
 		
 		panelEnCoursVueBas = new PanelEnCoursVueBas();
 		panelEnCoursVueBas.setBounds(0,505,800,95);
 		add(panelEnCoursVueBas);
 		
 		paneldroitrevoir = new PanelTermineVueDroite();
 		paneldroitrevoir.setBounds(720,0,80,476);
 		add(paneldroitrevoir);
 		
 		paneljoueur1 = new PanelJoueurVuePartie(partie.getParametreJeu().getJoueurBlanc(), CouleurCase.BLANC);
 		paneljoueur1.setBounds(10, 30, 150, 210);
 		add(paneljoueur1);
 		
 		
 		paneljoueur2 = new PanelJoueurVuePartie(partie.getParametreJeu().getJoueurNoir(), CouleurCase.NOIR);
 		paneljoueur2.setBounds(10, 260, 150, 215);
 		add(paneljoueur2);
 		
 		
 		//changement des panels en fonctionde l'etat
 		if(etat.equals(EtatSession.EN_COURS)){
 			
 			paneldroitencours.setVisible(true);
 			paneldroitrevoir.setVisible(false);
 		
 		}
 		
 		else if(etat.equals(EtatSession.TERMINEE)){
 		
 			paneldroitencours.setVisible(false);
 			paneldroitrevoir.setVisible(true);
 			
 		}
 		
 etat=EtatSession.EN_COURS;
 		
 	
 		
 		
 		if(etat.equals(EtatSession.EN_COURS)){
 				
 			paneldroitencours.setVisible(true);
 			paneldroitrevoir.setVisible(false);
 		
 		}
 		
 		else if(etat.equals(EtatSession.TERMINEE)){
 		
 			paneldroitencours.setVisible(false);
 			paneldroitrevoir.setVisible(true);
 		}
 		
 	}
 	
 	
 	
 	
 
 
 	public Partie getPartie() {
 		return partie;
 	}
 
 	public EtatSession getEtat() {
 		return etat;
 	}
 
 
 	public VueTablier getVueTablier() {
 		return vueTablier;
 	}
 	
 	
 	
 	public PanelEnCoursVueBas getPanelEnCoursVueBas() {
 		return panelEnCoursVueBas;
 	}
 
 	public PanelEnCoursVueDroite getPaneldroitencours() {
 		return paneldroitencours;
 	}
 
 	public PanelTermineVueDroite getPaneldroitrevoir() {
 		return paneldroitrevoir;
 	}
 
 	public void setPaneldroitrevoir(PanelTermineVueDroite paneldroitrevoir) {
 		this.paneldroitrevoir = paneldroitrevoir;
 	}
 	
 	
 
 	public PanelTermineVueBas getPanelTermineVueBas() {
 		return panelTermineVueBas;
 	}
 
 	public PanelJoueurVuePartie getPaneljoueur1() {
 		return paneljoueur1;
 	}
 
 	public PanelJoueurVuePartie getPaneljoueur2() {
 		return paneljoueur2;
 	}
 
 	private void setAnimation(TranstionAnimeeBase animation){
 		animation.setBounds(animationPanel.getBounds());
 		animationPanel.removeAll();
 		animationPanel.add(animation);
 	}
 
 	public void afficherTransition(String titre, String text){
 		ChangementTourAnimation tourAnimation = new ChangementTourAnimation(titre, text);
 		tourAnimation.start();
 		setAnimation(tourAnimation);
 	}
 	
	public FenetreDemandeAnimationBase afficherFenetreDemande(String titre,String btnLabel,SortedSet<String> reponses){
		FenetreDemandeAnimationBase fenetreDemande = new FenetreDemandeAnimationBase(titre,btnLabel,reponses);
 		setAnimation(fenetreDemande);
 		fenetreDemande.start();
 		return fenetreDemande;
 		
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		
 		g.drawImage(img_fond.getImage(),0,0,this);
 		super.paintComponent(g);
 		
 	}
 	
 	
 }
