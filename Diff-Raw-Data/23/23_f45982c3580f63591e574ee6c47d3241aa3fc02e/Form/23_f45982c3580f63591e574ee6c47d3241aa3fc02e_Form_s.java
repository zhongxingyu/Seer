 package com.operativus.senacrs.audit.model;
 
import java.util.LinkedList;
 import java.util.List;
 
 public class Form {
 
 	private Identification id;
 	private SkillSet skillSet;
	private List<EvaluationActivity> activities = new LinkedList<EvaluationActivity>();
	private List<StudentEvaluation> evaluations = new LinkedList<StudentEvaluation>();
 	private String notes;
 
 	public Identification getId() {
 
 		return this.id;
 	}
 
 	public void setId(final Identification id) {
 
 		this.id = id;
 	}
 
 	public SkillSet getSkillSet() {
 
 		return this.skillSet;
 	}
 
 	public void setSkillSet(final SkillSet skillSet) {
 
 		this.skillSet = skillSet;
 	}
 
	public List<EvaluationActivity> getActivities() {
 
 		return this.activities;
 	}
 
 	public void setActivities(final List<EvaluationActivity> activities) {
 
		this.activities = activities;
 	}
 
	public List<StudentEvaluation> getEvaluations() {
 
 		return this.evaluations;
 	}
 
 	public void setEvaluations(final List<StudentEvaluation> evaluations) {
 
		this.evaluations = evaluations;
 	}
 
 	public String getNotes() {
 
 		return this.notes;
 	}
 
 	public void setNotes(final String notes) {
 
 		this.notes = notes;
 	}
 
 	@Override
 	public String toString() {
 		
 		StringBuilder result = null;
 		String nl = System.getProperty("line.separator");
 		
 		result = new StringBuilder();
 		result.append(this.id.toString());
 		result.append(nl);
 		result.append(this.skillSet.toString());
 		result.append(nl);
 		result.append(this.evaluations.toString());
 		result.append(nl);
 		result.append(this.notes);
 	
 		return result.toString();
 	}
 }
