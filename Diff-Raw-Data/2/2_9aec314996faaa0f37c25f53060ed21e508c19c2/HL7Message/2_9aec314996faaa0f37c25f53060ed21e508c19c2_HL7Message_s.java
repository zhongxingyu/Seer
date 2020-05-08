 package com.janpix.hl7dto.hl7.v3.messages;
 
 import javax.xml.bind.annotation.XmlAttribute;
 
 public class HL7Message {
 	@XmlAttribute(name = "ITSVersion", required = true)
	public String itsVersion;
 
 	public HL7Message() {
 		super();
 	}
 
 }
