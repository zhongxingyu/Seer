 package com.qut.spc.model.converter;
 
 import javax.xml.bind.annotation.XmlElement;
 
 import com.qut.spc.model.SolarPanel;
 public class SolarPanelConverter extends SolarComponentConverter{
 	
 	public SolarPanelConverter(){
 		super(new SolarPanel());
 	}
 	
 	public SolarPanelConverter(SolarPanel panel) {
 		super(panel);	
 	}
 	
 	@XmlElement
	public float getOutputEnergy(){
 		return ((SolarPanel)panel).getOutputEnergy();
 	}
 
 }
