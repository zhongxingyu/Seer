 /**
  * 
  */
 package aeroport.sgbag.views;
 
 import lombok.Getter;
 
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 
 import aeroport.sgbag.controler.Simulation;
 import aeroport.sgbag.controler.Simulation.Etat;
 
 /**
  * @author Arnaud Lahache
  * 
  */
 public class TraitementClic extends MouseAdapter {
 
 	@Getter
 	private VueHall vueHall;
 
 	public TraitementClic(VueHall vueHall) {
 		this.vueHall = vueHall;
 	}
 
 	/**
 	 * Implémentation de l'évènement du clic dans un Hall.
 	 * 
 	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
 	 */
 	@Override
 	public void mouseUp(MouseEvent mouse) {
 		Simulation s = vueHall.getSimulation();
 		Viewable clickedView = vueHall.getClickedView(mouse.x, mouse.y);
 		
 		if(clickedView == vueHall) {
 			s.setEtat(Etat.NORMAL);
 			s.setSelectedElem(null);
 		} else {
 			VueElem elem = (VueElem) clickedView;
 			
 			if(s.getEtat() == Etat.CHOIX_DESTINATION) {
 				s.createBagage(s.getSelectedElem(), elem);
 			}
 			
 			s.setSelectedElem(elem);
 			s.setEtat(Etat.SELECTION);
 		}
 	}
 
 }
