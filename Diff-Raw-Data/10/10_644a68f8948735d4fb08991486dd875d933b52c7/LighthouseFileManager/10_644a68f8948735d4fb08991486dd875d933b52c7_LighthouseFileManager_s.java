 package edu.uci.lighthouse.model;
 
 public class LighthouseFileManager {
 	
 	private LighthouseFile lighthouseFile;
 	private LighthouseModel lighthouseModel = LighthouseModel.getInstance();
 
 	public LighthouseFileManager(LighthouseFile lighthouseFileModel) {
 		this.lighthouseFile = lighthouseFileModel; 
 	}
 
 	public LighthouseEntity addEntity(LighthouseEntity newEntity){
 		LighthouseEntity entity = lighthouseModel.getEntity(newEntity.getFullyQualifiedName());
 		if (entity==null) {
 			entity = newEntity;
 		}
 		lighthouseFile.addEntity(entity);
 		return entity;
 	}
 	
 	public LighthouseRelationship addRelationship(LighthouseRelationship newRelationship){
 		LighthouseRelationship relationship = lighthouseModel.getRelationship(newRelationship);
		if (relationship != null) {
			return relationship;
		} else {
			lighthouseFile.addRelationship(newRelationship);
			return newRelationship;
 		}
 	}
 	
 }
