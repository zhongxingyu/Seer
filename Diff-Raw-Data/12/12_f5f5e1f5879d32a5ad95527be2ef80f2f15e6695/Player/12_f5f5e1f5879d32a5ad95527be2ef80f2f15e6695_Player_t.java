 package projectubernahme;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 import projectubernahme.lifeforms.Lifeform;
 import projectubernahme.simulator.MainSimulator;
 
 
 /** a physical player of the game, controlling a heap of different lifeforms */
 public class Player {
 	private ArrayList<Lifeform> controlledLifeforms;
 
 	MainSimulator sim;
 
 	private Lifeform selectedLifeform;
 	private Lifeform secondarySelectedLifeform;
 
 
 	/** initializes the list of controlledLifeforms, add a starting one */
 	public Player (MainSimulator sim) {
 		this.sim = sim;
 		setControlledLifeforms(new ArrayList<Lifeform>());
 		getControlledLifeforms().add(sim.giveLifeform(this));
 	}
 
 	public void takeControlOver (Lifeform l) {
 		getControlledLifeforms().add(l);
 		l.setControlled(this);
 	}
 
 	/** checks whether the player is in control of at least one lifeform */
 	public boolean hasSomeControl() {
 		return getControlledLifeforms().size() > 0;
 	}
 
 	public void handleKeyPressed(KeyEvent e) {
 		if (e.getKeyChar() == 't') {
			if(secondarySelectedLifeform != null) {
				takeover(selectedLifeform, secondarySelectedLifeform);
				secondarySelectedLifeform = null;
			}
 		}
 		else if (e.getKeyChar() == 'f') {
			if(secondarySelectedLifeform != null) {
				ingest(selectedLifeform, secondarySelectedLifeform);
				secondarySelectedLifeform = null;
			}
			
 		}
 		if (getSelectedLifeform() != null)
 			getSelectedLifeform().handleKeyPressed(e);
 	}
 
 	public void handleKeyReleased(KeyEvent e) {
 		if (getSelectedLifeform() != null)
 			getSelectedLifeform().handleKeyReleased(e);
 	}
 
 	public void setControlledLifeforms(ArrayList<Lifeform> controlledLifeforms) {
 		this.controlledLifeforms = controlledLifeforms;
 	}
 
 	public ArrayList<Lifeform> getControlledLifeforms() {
 		return controlledLifeforms;
 	}
 
 	public void setSelectedLifeform(Lifeform selectedLifeform) {
 		if (selectedLifeform != this.selectedLifeform) {
 			secondarySelectedLifeform = null;
 		}
 		this.selectedLifeform = selectedLifeform;
 	}
 
 	public Lifeform getSelectedLifeform() {
 		if (selectedLifeform == null)
 			selectedLifeform = controlledLifeforms.get(0);
 		return selectedLifeform;
 	}
 
 	public Lifeform getSecondarySelectedLifeform() {
 		return secondarySelectedLifeform;
 	}
 
 	public void setSecondarySelectedLifeform(Lifeform secondarySelectedLifeform) {
 		if (secondarySelectedLifeform == selectedLifeform) {
 			this.secondarySelectedLifeform = null;
 		}
 		else {
 			this.secondarySelectedLifeform = secondarySelectedLifeform;
 		}
 	}
 
 	public void takeover(int who, int whom) {
 		Lifeform whoL = controlledLifeforms.get(who);
 		Lifeform whomL = controlledLifeforms.get(who).getNeighbors().get(whom);
 		if (whoL != null && whomL != null) {
 			takeover(whoL, whomL);
 		}
 	}
 
 	private void takeover(Lifeform who, Lifeform whom) {
 		if (!controlledLifeforms.contains(whom)) {
 			if (who.takeover(whom))
 				controlledLifeforms.add(whom);
 		}
 	}
 
 	public void ingest(int who, int whom) {
 		Lifeform a = controlledLifeforms.get(who);
 		Lifeform b = controlledLifeforms.get(who).getNeighbors().get(whom);
 		
 		if (a != null && b != null) {
 			ingest(a, b);
 		}
 	}
 
 	private void ingest(Lifeform who, Lifeform whom) {
 		who.ingest(whom);
 	}
 
 	/** determined whether the player can see a certain lifeform */
 	public boolean canSee(Lifeform l) {
 		if (controlledLifeforms.contains(l)) {
 			return true;
 		}
 		for (Lifeform lifeform : controlledLifeforms) {
 			if (lifeform.getNeighbors().contains(l)) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
