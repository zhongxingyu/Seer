 package com.operativus.senacrs.audit.model;
 
import java.util.Collection;
 import java.util.List;
import java.util.TreeSet;
 
 public class Form {
 
 	private Identification id;
 	private SkillSet skillSet;
	private Collection<EvaluationActivity> activities = new TreeSet<EvaluationActivity>();
	private Collection<StudentEvaluation> evaluations = new TreeSet<StudentEvaluation>();
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
 
	public Collection<EvaluationActivity> getActivities() {
 
 		return this.activities;
 	}
 
 	public void setActivities(final List<EvaluationActivity> activities) {
 
		this.activities.clear();
		this.activities.addAll(activities);
 	}
 
	public Collection<StudentEvaluation> getEvaluations() {
 
 		return this.evaluations;
 	}
 
 	public void setEvaluations(final List<StudentEvaluation> evaluations) {
 
		this.evaluations.clear();
		this.evaluations.addAll(evaluations);
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
