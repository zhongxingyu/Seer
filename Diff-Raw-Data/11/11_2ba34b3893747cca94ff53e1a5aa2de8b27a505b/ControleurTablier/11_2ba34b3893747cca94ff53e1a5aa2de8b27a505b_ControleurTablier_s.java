 package fr.ujm.tse.info4.pgammon.controleur;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.Timer;
 
 import fr.ujm.tse.info4.pgammon.exeption.TourNonJouableException;
 import fr.ujm.tse.info4.pgammon.gui.CaseButton;
 import fr.ujm.tse.info4.pgammon.models.Case;
 import fr.ujm.tse.info4.pgammon.models.NiveauAssistant;
 import fr.ujm.tse.info4.pgammon.models.Partie;
 import fr.ujm.tse.info4.pgammon.models.Tablier;
 import fr.ujm.tse.info4.pgammon.vues.VueTablier;
 
 public class ControleurTablier {
 	private Tablier tablier;
 	private Partie partie;
 	private VueTablier vueTablier;
 	private Timer timer;
 	
 	public ControleurTablier(Partie partie,VueTablier vueTablier)
 	{
 		
 		
 		this.partie = partie;
 		this.tablier = partie.getTablier();
 		this.vueTablier = vueTablier;
 		
 	
 		build();
 	}
 
 
 	private void build() {
 			
 		buildTimer();
 		
 		Collection<CaseButton> lCase = vueTablier.getCasesButtons();
 		for (CaseButton caseButton : lCase) {
 			caseButton.addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					CaseButton caseButton = (CaseButton) e.getSource();
 					
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
 							if (partie.siDesUtilises())
 							{	
 								//TODO affichage changement de Tour
 								changerTour();			
 							}
 							else if(!partie.hasCoupPossible())
 							{
 								//TODO affichage plus de coup possible
 								changerTour();
 							}		
 						}
 						else
 						{
 							vueTablier.uncandidateAll();
 							vueTablier.setPossibles((List)(new ArrayList<Case>()));
 						}
 						
 					}
 					if (timer!= null)
 						timer.restart();
 					
 					vueTablier.updateUI();
 					vueTablier.updateDes();
 				}
 				
 
 				
 				@Override
 				public void mouseEntered(MouseEvent e) {
 					// TODO Auto-generated method stub
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 					// TODO Auto-generated method stub
 				}
 
 				@Override
 				public void mousePressed(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseReleased(MouseEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 			});
 			
 		}
 	}
 	
 	private void changerTour() 
 	{
 		try {
 			partie.changerTour();
 		} catch (TourNonJouableException e1) {
 			// TODO Auto-generated catch block
 			changerTour();
 		}	
 		
 		vueTablier.uncandidateAll();
 	}
 	
 	private void buildTimer(){
 		
 		if (partie.getParametreJeu().getSecondesParTour() != 0)
 		{
 			timer = new Timer(1000, new ActionListener() {
 				  public void actionPerformed(ActionEvent ae) {
 					  partie.deplacementAleatoire();
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
 							changerTour();
 						}	
 					  
 					  vueTablier.updateUI();
 					  vueTablier.updateDes();
 					  timer.restart();
 				  }
 				});
 			timer.setRepeats(false);
 			timer.start();
 		}
 		else
 		{
 			timer =null;
 		}
 	}
 	
 }
