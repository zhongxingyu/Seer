 import model.spielautomatEvent;
 import model.spielautomatListener;
 
 
 public class mySpielautomatListener implements spielautomatListener {
 	Playfield playfield;
 	
 	
 	public mySpielautomatListener(Playfield _playfield) {
 		this.playfield = _playfield;
 	
 	}
 
 	@Override
 	public void ModelChanged(spielautomatEvent arg0) {
		// TODO ModelChanged isnt used - triggered indirect over getZiffern()?!
 		this.playfield.printRollingNumerics();
 		System.out.println("ModelChanged");
 		
 	}
 
 	@Override
 	public void StatusChanged(spielautomatEvent arg0) {
 		System.out.println("StatusChanged");
 		this.playfield.updateCreditPrize();
 		
 	}
 
 }
