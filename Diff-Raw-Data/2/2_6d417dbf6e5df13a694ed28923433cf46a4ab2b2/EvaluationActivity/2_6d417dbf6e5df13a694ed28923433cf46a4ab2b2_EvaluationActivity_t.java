 package com.operativus.senacrs.audit.model;
 
 public class EvaluationActivity
 		extends
 		AbstractSequenceStringFieldComparable {
 
 	private final EvaluationType type;
 	String name = null;
 	private String description = null;
 
 	public EvaluationActivity(final int sequence, final EvaluationType type, final String name, final String description) {
 
 		super(sequence);
 		this.type = type;
 		this.name = name;
 		this.description = description;
 	}
 
 	public EvaluationType getType() {
 
 		return this.type;
 	}
 
 	public String getName() {
 
 		return this.name;
 	}
 
 	public void setName(final String name) {
 
 		this.name = name;
 	}
 
 	public String getDescription() {
 
 		return this.description;
 	}
 
 	public void setDescription(final String description) {
 
 		this.description = description;
 	}
 
 	@Override
 	protected String getComparisonStringField() {
 
 		return this.getName();
 	}
 	
 	@Override
 	public String toString() {
 	
		return super.toString() + ":" + this.description + " = " + this.type;
 	}
 
 }
