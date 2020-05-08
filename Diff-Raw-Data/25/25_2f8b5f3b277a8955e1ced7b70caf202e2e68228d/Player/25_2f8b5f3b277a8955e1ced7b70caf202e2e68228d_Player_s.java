 package com.thisisdinosaur.protosaurus.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.thisisdinosaur.protosaurus.shared.Dinosaur;
 
 public class Player {
 
 	private List<Dinosaur> controlledEntities;
 	private List<Dinosaur> dinosaurs;
 	
 	private Dinosaur selectedEntity;
 	
 	public Player(){
 		this.controlledEntities = new ArrayList<Dinosaur>();
 		this.dinosaurs = new ArrayList<Dinosaur>();
 	}
 	
	public void addDinosaur(Dinosaur entity) {
		this.controlledEntities.add(entity);
 	}
 	
	public void removeDinosaur(Dinosaur entity) {
		this.controlledEntities.remove(entity);
 	}
 	
 	// Will be changed to some other list when resource nodes are added
 	public List<Dinosaur> getLOSEntities() {
 		return this.controlledEntities;
 	}
 	
 	public List<Dinosaur> getDinosaurs() {
 		return this.dinosaurs;
 	}
 
 	public Dinosaur getSelectedEntity() {
 		return selectedEntity;
 	}
 
 	public void setSelectedEntity(Dinosaur selectedEntity) {
 		this.selectedEntity = selectedEntity;
 	}
 
 }
