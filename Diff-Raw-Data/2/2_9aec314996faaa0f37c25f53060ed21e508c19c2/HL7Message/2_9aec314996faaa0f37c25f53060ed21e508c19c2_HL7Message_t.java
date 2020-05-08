 package com.janpix.hl7dto.hl7.v3.messages;
 
 import javax.xml.bind.annotation.XmlAttribute;
 
 public class HL7Message {
 	@XmlAttribute(name = "ITSVersion", required = true)
	public String itsVersion = "XML_1.0";
 
 	public HL7Message() {
 		super();
 	}
 
 }
