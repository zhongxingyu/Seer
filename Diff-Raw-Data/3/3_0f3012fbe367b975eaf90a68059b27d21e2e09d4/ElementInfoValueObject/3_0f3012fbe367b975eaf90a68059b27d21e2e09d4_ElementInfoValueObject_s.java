 package org.lh.dmlj.schema.editor.property;
 
 public class ElementInfoValueObject {
 	
 	private String description;
 	private String elementName;		
 	private String levelAndElementName;
 	private String pictureAndUsage;
 	private int	   seqNo;
 	
 	public ElementInfoValueObject(int seqNo) {
		super();		
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getElementName() {
 		return elementName;
 	}
 
 	public String getLevelAndElementName() {
 		return levelAndElementName;
 	}
 
 	public String getPictureAndUsage() {
 		return pictureAndUsage;
 	}
 	
 	public int getSeqNo() {
 		return seqNo;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public void setElementName(String elementName) {
 		this.elementName = elementName;
 	}
 
 	public void setLevelAndElementName(String levelAndElementName) {
 		this.levelAndElementName = levelAndElementName;
 	}
 
 	public void setPictureAndUsage(String pictureAndUsage) {
 		this.pictureAndUsage = pictureAndUsage;
 	}	
 	
 }
