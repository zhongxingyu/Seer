 package com.kdcloud.lib.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import weka.core.Attribute;
 import weka.core.Instances;
 
 @XmlAccessorType(XmlAccessType.NONE)
 @XmlRootElement
 public class DataSpecification implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@XmlAccessorType(XmlAccessType.FIELD)
 	static class Column implements Serializable {
 		private static final long serialVersionUID = 1L;
 		
 		String name;
 		DataType type;
 		InputSource source;
 	}
 	
 	public enum DataType {
 		DOUBLE, TIMESTAMP, INTEGER
 	}
 	
 	public enum InputSource {
 		HEARTBEAT, CLOCK
 	}
 	
 	@XmlElement(name="column")
 	List<Column> columns;
 	
 	@XmlElement
 	String view;
 	
 	public Instances newInstances(String relationalName) {
 		return new Instances(relationalName, getAttrInfo(), 1000);
 	}
 	
 	public boolean matchingSpecification(Instances instances) {
 		return new Instances("", getAttrInfo(), 0).equalHeaders(instances);
 	}
 	
 	public ArrayList<Attribute> getAttrInfo() {
 		ArrayList<Attribute> info = new ArrayList<Attribute>(columns.size());
 		for (Column c : columns) {
 			info.add(new Attribute(c.name));
 		}
 		return info;
 	}
 
 	public String getView() {
 		return view;
 	}
 
 	public static Instances newInstances(String name, int columns) {
 		ArrayList<Attribute> info = new ArrayList<Attribute>(columns);
 		for (int i = 0; i < columns; i++) {
 			info.add(new Attribute("attr" + i));
 		}
 		return new Instances(name, info, 0);
 	}
 
 	public List<InputSource> sources() {
 		LinkedList<InputSource> list = new LinkedList<DataSpecification.InputSource>();
 		for (Column c : columns)
 			list.add(c.source);
 		return list;
 	}
 
 }
