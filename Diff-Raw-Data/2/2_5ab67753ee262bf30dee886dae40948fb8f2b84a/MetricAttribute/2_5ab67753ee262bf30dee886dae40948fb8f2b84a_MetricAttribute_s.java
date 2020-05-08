 /*
  * MetricAttribute.java (JMetricVis)
  * Copyright 2011 Luke Plaster. All rights reserved.
  */
 package org.lukep.javavis.metrics;
 
 import java.util.List;
 
 public class MetricAttribute {
 	
 	private org.lukep.javavis.generated.jaxb.Metrics.Metric source;
 	
 	private String name;
 	private String nameInternal;
 	private MetricType type;
 	private List<String> appliesTo;
 	private Class<IMeasurableVisitor> visitor;
 	private String argument;
 	private double cold;
 	private double hot;
 	
 	public MetricAttribute(String name, String nameInternal, MetricType type, List<String> appliesTo) {
 		this.name = name;
 		this.nameInternal = nameInternal;
 		this.type = type;
 		this.appliesTo = appliesTo;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public MetricAttribute(org.lukep.javavis.generated.jaxb.Metrics.Metric source, 
 			MetricRegistry registry) throws ClassNotFoundException {
 		// set the fields in our new MetricAttribute object from the data source object
 		this(source.getName(), 
 				source.getInternalName(), 
 				registry.getOrSetMetricType(source.getType()), 
 				source.getAppliesTo().getMeasurable());
 		
 		this.source = source;
 		
 		// set static metric specific fields
 		visitor = (Class<IMeasurableVisitor>) Class.forName(source.getVisitor());
 		argument = source.getArgument();
 		cold = source.getCold();
 		hot = source.getHot();
 	}
 	
 	public org.lukep.javavis.generated.jaxb.Metrics.Metric getSource() {
 		return source;
 	}
 
 	public MetricMeasurement measureTarget(IMeasurableNode target) {
 		// if this metric applies to the target's type - run it!
 		if (testAppliesTo(target.getModelTypeName()))
 			return target.accept( this, 
 					MeasurableVisitorPool.getInstance().getPooledVisitor(visitor) );
 		return null;
 	}
 	
 	public MetricMeasurement measureTargetCached(IMeasurableNode target) {
 		return MetricRegistry.getInstance().getCachedMeasurement(target, this);
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public String getInternalName() {
 		return nameInternal;
 	}
 
 	public MetricType getType() {
 		return type;
 	}
 
 	public List<String> getAppliesTo() {
 		return appliesTo;
 	}
 
 	public boolean testAppliesTo(String measurableName) {
 		return appliesTo.contains(measurableName);
 	}
 
 	public String getArgument() {
 		return argument;
 	}
 	
 	public boolean isArgumentSet(String argument) {
 		if (argument != null)
			return argument.contains(argument);
 		return false;
 	}
 
 	public double getCold() {
 		return cold;
 	}
 
 	public double getHot() {
 		return hot;
 	}
 
 	@Override
 	public String toString() {
 		return name;// + " " + appliesTo;
 	}
 	
 }
