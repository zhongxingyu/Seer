 package com.madalla.util.jcr.model;
 
 import java.util.Map;
 
 import org.apache.wicket.model.IModel;
 
 public interface IPluginModel extends IModel {
	public Map<String, Object> getMapRepresentation();
 }
