 package org.ebayopensource.turmeric.tools.annoparser.outputgenerator.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class IndexerDataObject {
 	private List<IndexerBaseDataObject> dataObjects=new ArrayList<IndexerBaseDataObject>();
 
 	public List<IndexerBaseDataObject> getDataObjects() {
 		return dataObjects;
 	}
 	public void addDataObjects(IndexerBaseDataObject dataObject) {
 		dataObjects.add(dataObject);
 	}
 	public void setDataObjects(List<IndexerBaseDataObject> dataObjects) {
 		this.dataObjects = dataObjects;
 	}
 	
 }
