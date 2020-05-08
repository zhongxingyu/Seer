 package com.github.thebiologist13.attributelib;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Attribute implements Serializable {
 
 	private static final long serialVersionUID = 7131150007489438852L;
 	
 	private VanillaAttribute attribute;
 	private double base;
 	private List<Modifier> modifiers = new ArrayList<Modifier>();
 	
 	public Attribute(VanillaAttribute attribute) {
 		this.attribute = attribute;
 		this.base = attribute.getDefaultBase();
 	}
 
 	public void addModifier(Modifier modifier) {
 		modifiers.add(modifier);
 	}
 	
 	public VanillaAttribute getAttribute() {
 		return attribute;
 	}
 
 	public double getBase() {
 		return base;
 	}
 	
 	public List<Modifier> getModifiers() {
 		return modifiers;
 	}
 
 	public void removeModifier(Modifier modifier) {
 		modifiers.remove(modifier);
 	}
 
 	public void setAttribute(VanillaAttribute attribute) {
 		this.attribute = attribute;
 	}
 
 	public void setBase(double base) {
 		double min = attribute.getMinimum();
 		double max = attribute.getMaximum();
 		if(base < min)
 			base = min;
		else if(base > max && max != -1)
 			base = max;
 		this.base = base;
 	}
 
 	public void setModifiers(List<Modifier> modifiers) {
 		this.modifiers = modifiers;
 	}
 
 }
