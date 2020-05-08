 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 package projectubernahme;
 import java.awt.event.KeyEvent;
 import java.util.Calendar;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import projectubernahme.interface2D.CircleMenu;
 import projectubernahme.lifeforms.Lifeform;
 import projectubernahme.simulator.MainSimulator;
 
 
 /** a physical player of the game, controlling a heap of different lifeforms */
 public class Player {
 	private CopyOnWriteArrayList<Lifeform> controlledLifeforms;
 
 	private Lifeform selectedLifeform;
 	private Lifeform secondarySelectedLifeform;
 	private double upgradePoints = 0;
 
 	/** initializes the list of controlledLifeforms, add a starting one */
 	public Player (MainSimulator sim) {
 		setControlledLifeforms(new CopyOnWriteArrayList<Lifeform>());
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
 		else if (e.getKeyChar() == 'u'){
 			upgradeIntelligence();
 		}
 		else if (getSelectedLifeform() != null)
 			getSelectedLifeform().handleKeyPressed(e);
 	}
 
 	private void upgradeIntelligence() {
 		if(upgradePoints > 1) {
 			--upgradePoints;
 			setIntFactor(getIntFactor() + 1);
 		}
 	}
 
 	public void handleKeyReleased(KeyEvent e) {
 		if (getSelectedLifeform() != null)
 			getSelectedLifeform().handleKeyReleased(e);
 	}
 
 	public void setControlledLifeforms(CopyOnWriteArrayList<Lifeform> copyOnWriteArrayList) {
 		this.controlledLifeforms = copyOnWriteArrayList;
 	}
 
 	public CopyOnWriteArrayList<Lifeform> getControlledLifeforms() {
 		return controlledLifeforms;
 	}
 
 	public void setSelectedLifeform(Lifeform selectedLifeform) {
 		if (selectedLifeform != this.selectedLifeform) {
 			secondarySelectedLifeform = null;
 		}
 		this.selectedLifeform = selectedLifeform;
 	}
 
 	public Lifeform getSelectedLifeform() {
 		if (selectedLifeform == null && controlledLifeforms != null && !controlledLifeforms.isEmpty()) {
 			selectedLifeform = controlledLifeforms.get(0);
 		}
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
 			who.takeover(whom);
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
 
 	public void addControlledLifeform(Lifeform prey) {
 		controlledLifeforms.add(prey);
 	}
 	
 	private CircleMenu circlemenu;
 	public void setMenu(CircleMenu circleMenu) {
 		this.setCirclemenu(circleMenu);		
 	}
 	
 	private double lastCalculatedTotalBiomass;
 	private long lastCalculatedTotalBiomassTime;
 	public double getTotalBiomass() {
 		Calendar cal = Calendar.getInstance();
 		if (lastCalculatedTotalBiomassTime + Long.parseLong(ProjectUbernahme.getConfigValue("TotalBiomassCalcInterval")) < cal.getTimeInMillis()) {
 			lastCalculatedTotalBiomassTime = cal.getTimeInMillis();
 			lastCalculatedTotalBiomass = 0.0;
 			for (Lifeform l : controlledLifeforms) {
 				lastCalculatedTotalBiomass += l.getBiomass();
 			}
 		}
 		return lastCalculatedTotalBiomass;
 	}
 
 	public void setIntFactor(double intFactor) {
 		this.intelligenceFactor = intFactor;
 	}
 
 	public double getIntFactor() {
 		return intelligenceFactor;
 	}
 
 	public void setCirclemenu(CircleMenu circlemenu) {
 		this.circlemenu = circlemenu;
 	}
 
 	public CircleMenu getCirclemenu() {
 		return circlemenu;
 	}
 }
