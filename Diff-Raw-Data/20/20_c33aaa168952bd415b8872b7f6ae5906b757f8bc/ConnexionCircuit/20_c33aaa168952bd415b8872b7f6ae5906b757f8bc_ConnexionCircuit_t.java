 package aeroport.sgbag.kernel;
 
 import aeroport.sgbag.utils.UtilsCircuit;
 import lombok.Getter;
 import lombok.Setter;
 import lombok.extern.log4j.Log4j;
 
 @Log4j
 public class ConnexionCircuit extends Noeud {
 
 	@Getter
 	@Setter
 	private FileBagage fileBagage;
 
 	public ConnexionCircuit(FileBagage f) {
 		this(f, null);
 	}
 
 	public ConnexionCircuit(FileBagage f, Circuit parent) {
 		super(parent);
 		this.fileBagage = f;
 		if (f != null)
 			f.setConnexionCircuit(this);
 	}
 
 	public Boolean update() {
 		log.trace("Update de la ConnexionCircuit " + this);
 		if (hasChariot() && ++ticksToUpdate >= tickThresholdToUpdate) {
 			Chariot chariot = this.getListeChariot().getFirst();
 			if (chariot.getDestination().getId() == this.getId()) {
 
 				// Le chariot est arrivé à destination
 				log.debug("Chariot arrivé à destination "
 						+ getListeChariot().getFirst());
 
 				if (fileBagage instanceof TapisRoulant) {
 
 					if (((TapisRoulant) fileBagage).hasReadyBagage()
 							&& getListeChariot().getFirst().getBagage() == null) {
 
 						chariot.setBagage(((TapisRoulant) fileBagage)
 								.getBagageIfReady());
 						chariot.getBagage().setParent(
 								getListeChariot().getFirst());
 
 						log.debug("Bagage du chariot : " + chariot.getBagage()
 
 						+ " destination : "
 								+ chariot.getBagage().getDestination());
 						chariot.setDestination(chariot.getBagage()
 								.getDestination());
 
 						// Mode automatique
 						if (getParent().getParent().isAutomatique()) {
 							chariot.setCheminPrevu(this.getParent()
 									.calculChemin(
 											this,
 											chariot.getBagage()
 													.getDestination()));
 
 							String logstr = "Chemin prévu pour aller à "
 									+ chariot.getBagage().getDestination()
 									+ " :";
 							for (int i = 0; i < chariot.getCheminPrevu().size(); i++) {
 								logstr += chariot.getCheminPrevu().get(i) + " ";
 							}
 							log.debug(logstr);
 						}
 
						if(moveToNextRail()){
							ticksToUpdate = 0;
						}
						
 					}
 				} else if (getListeChariot().getFirst().hasBagage()) {
 
 					// On retire le bagage
 					chariot.moveBagageToFile(fileBagage);
 
 					// Mode automatique
 					if (getParent().getParent().isAutomatique()) {
 						Noeud nouvelleDestination = UtilsCircuit
 								.getUtilsCircuit().getTapisRoulantRandom()
 								.getConnexionCircuit();
 						chariot.setCheminPrevu(this.getParent().calculChemin(
 								this, nouvelleDestination));
 						chariot.setDestination(nouvelleDestination);
 					}
 
					if(moveToNextRail()){
						ticksToUpdate = 0;
					}
 				} else {
 
 					// Le chariot est arrivé à destination mais n'a pas de
 					// bagage à retirer
 
					if(moveToNextRail()){
						ticksToUpdate = 0;
					}
 
 				}
 			} else {
				if(moveToNextRail()){
					ticksToUpdate = 0;
				}
 			}
 		} else if (!hasChariot()) {
 			ticksToUpdate = 0;
 		}
 		return true;
 	}
 
 }
