 package fr.ujm.tse.info4.pgammon.controleur;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 import fr.ujm.tse.info4.pgammon.exeption.TourNonJouableException;
 import fr.ujm.tse.info4.pgammon.gui.CaseButton;
 import fr.ujm.tse.info4.pgammon.models.Case;
 import fr.ujm.tse.info4.pgammon.models.DeSixFaces;
 import fr.ujm.tse.info4.pgammon.models.EtatSession;
 import fr.ujm.tse.info4.pgammon.models.NiveauAssistant;
 import fr.ujm.tse.info4.pgammon.models.Partie;
 import fr.ujm.tse.info4.pgammon.models.Tablier;
 import fr.ujm.tse.info4.pgammon.vues.VuePartie;
 import fr.ujm.tse.info4.pgammon.vues.VueTablier;
 
 public class ControleurTablier implements Controleur{
 	private Tablier tablier;
 	private Partie partie;
 	private VueTablier vueTablier;
 	private VuePartie vuePartie;
 	private Timer timer;
 	private ControleurPartie controleurPartie;
 	private JFrame frame;
 
 
 
 	public ControleurTablier(Partie partie,VuePartie vuePartie,ControleurPartie controleurPartie)
 	{
 
 		this.partie = partie;
 		this.tablier = partie.getTablier();
 		this.vuePartie = vuePartie;
 		this.vueTablier = vuePartie.getVueTablier();
 		this.controleurPartie = controleurPartie;
 	
 		build();
 		vueTablier.updateDes();
 		vuePartie.afficherTransition(partie.getParametreJeu().getJoueur(partie.getJoueurEnCour()).getPseudo(), "Joueur" + partie.getJoueurEnCour().toString());
 		
 	}
 
 
 	private void build() {
 			
 		buildTimer();
 		ListenerCaseButton();
 		
 	}
 	
 	private void ListenerCaseButton()
 	{
 		Collection<CaseButton> lCase = vueTablier.getCasesButtons();
 		for (CaseButton caseButton : lCase) {
 			caseButton.addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					CaseButton caseButton = (CaseButton) e.getSource();
					if (!partie.isTourFini())
 						if(vueTablier.getCandidat() == null 
 								&& partie.getJoueurEnCour() == caseButton.getCase().getCouleurDame())
 						{
 							if (caseButton.getCase().getNbDame() != 0 
 									&& (!tablier.isDameDansCaseBarre(partie.getJoueurEnCour()) 
 											|| caseButton.getCase().isCaseBarre())
 											&& caseButton.getCase().getCouleurDame() == partie.getJoueurEnCour())
 								{
 									vueTablier.setCandidat(caseButton);
 									if(partie.getParametreJeu().getJoueur(partie.getJoueurEnCour()).getNiveauAssistant() == NiveauAssistant.SIMPLE
 											|| partie.getParametreJeu().getJoueur(partie.getJoueurEnCour()).getNiveauAssistant() == NiveauAssistant.COMPLET)
 										vueTablier.setPossibles(partie.getCoupsPossibles(caseButton.getCase()));
 								}
 						}
 						else if (vueTablier.getCandidat() != null)
 						{
 							if (partie.jouerCoup(vueTablier.getCandidat().getCase(), caseButton.getCase()))
 							{
 								vueTablier.uncandidateAll();
 								vueTablier.setPossibles((List)(new ArrayList<Case>()));
 								
 								if(partie.isPartieFini())
 								{
 									controleurPartie.finPartie();
 								}
 								
 								if (partie.siDesUtilises())
 								{	
 									//TODO affichage changement de Tour
 									changerTour();	
 									
 								}
 								else if(!partie.hasCoupPossible())
 								{
 									changerTour();
 									partie.lancerDes();
 									if(!partie.hasCoupPossible())
 									{
 										//TODO affichage plus de coup possible
 										changerTour();
 									}
 								}
 										
 							}
 							else
 							{
 								vueTablier.uncandidateAll();
 								vueTablier.setPossibles((List)(new ArrayList<Case>()));
 							}
 							if (timer!= null)
 								timer.restart();
 						}
 
 					vueTablier.updateUI();
 					vueTablier.updateDes();
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {}
 				@Override
 				public void mouseExited(MouseEvent e) {}
 				@Override
 				public void mousePressed(MouseEvent e) {}
 				@Override
 				public void mouseReleased(MouseEvent e) {}
 			});
 			
 		}
 	}
 	
 	
 	/*public void nouvellePartie(Partie partie)
 	{
 		this.partie = partie;
 		vueTablier.setPartie(partie);
 		vueTablier.updateUI();
 		vueTablier.updateDes();
 	}*/
 	
 	public void changerTour() 
 	{
 		partie.changerTour();
 		vuePartie.afficherTransition(partie.getParametreJeu().getJoueur(partie.getJoueurEnCour()).getPseudo(),"Joueur" + partie.getJoueurEnCour().toString());
 	}
 	
 	private void buildTimer(){
 		
 		if (partie.getParametreJeu().getSecondesParTour() != 0)
 		{
 			timer = new Timer(partie.getParametreJeu().getSecondesParTour(), new ActionListener() {
 				  public void actionPerformed(ActionEvent ae) {
 					  
 					  try {
 						  //Deplacement aleatoire du nombre de de restant
 						  int nbDeNonUtiliser=0;
 						  for (DeSixFaces de : partie.getDeSixFaces()) {
 							if(!de.isUtilise())
 								nbDeNonUtiliser++;
 						  }
 						  for(int i=0;i<nbDeNonUtiliser;i++)
 						  {
 							  partie.deplacementAleatoire();
 						  }
 
 					} catch (TourNonJouableException e) {
 						// TODO affichage ecran
 						changerTour();
 					}
 					  vueTablier.uncandidateAll();
 					  vueTablier.setPossibles((List)(new ArrayList<Case>()));
 					  
 					  if (partie.siDesUtilises())
 						{	
 							//TODO affichage changement de Tour
 							changerTour();			
 						}
 						else if(!partie.hasCoupPossible())
 						{
 							//TODO affichage plus de coup possible
 							if (timer!= null)
 								timer.stop();
 							changerTour();
 						}	
 					  
 					  vueTablier.updateUI();
 					  vueTablier.updateDes();
 					  
 				  }
 				});
 			
 			timer.setRepeats(false);
 			
 		}
 		else
 		{
 			timer =null;
 		}
 	}
 	
 	public Timer getTimer() {
 		return timer;
 	}
 
 
 	@Override
 	public Controleur getControleur() {
 		return this;
 	}
 
 
 	@Override
 	public JFrame getFrame() {
 		return frame;
 	}
 
 
 	@Override
 	public void retour() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
