 /*
  * Solar power calculator
  * 
  * Copyright (C) 2012, ORANGE group.
  * See LICENSE.txt for license details.
  */
 
 package com.qut.spc.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.qut.spc.api.InverterFilterAPI;
 
 
 /**
  * Wrapper class for list of Inverters
  * @author QuocViet
  */
@XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement(name="inverters")
 public class InverterContainer extends ComponentContainer
 		implements InverterFilterAPI {
 	
 	@XmlElement(name="inverter")
 	private List<Inverter> list;
 	
 	public InverterContainer() {
 		list = new ArrayList<Inverter>();
 	}
 	
 	public List<Inverter> getList() {
 		return list;
 	}
 	

 
 	@Override
 	public List<Inverter> search() throws IllegalArgumentException {
 		list = fetchComponents(Inverter.class.getName());
 		return list;
 	}
 }
