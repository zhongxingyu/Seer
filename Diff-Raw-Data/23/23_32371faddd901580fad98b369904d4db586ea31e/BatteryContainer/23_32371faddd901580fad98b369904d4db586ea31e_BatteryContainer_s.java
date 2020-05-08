 /*
  * Solar power calculator
  * 
  * Copyright (C) 2012, ORANGE group.
  * See LICENSE.txt for license details.
  */
 
 package com.qut.spc.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.qut.spc.api.BatteryFilterAPI;
 
 
 /**
  * Wrapper class for list of Battery
  * @author QuocViet
  */
 @XmlRootElement(name="batteries")
 public class BatteryContainer extends ComponentContainer
 		implements BatteryFilterAPI {
 	
 	@XmlElement(name="battery")
 	private List<Battery> list;
 	
 	public BatteryContainer() {
 		list = new ArrayList<Battery>();
 	}
 	
 	public BatteryContainer(List<Battery> list) {
 		this.list = list;
 	}
 	
 	public List<Battery> getList() {
 		return list;
 	}
 	
 	public void setList(List<Battery> list) {
 		this.list = list;
 	}
 
 	@Override
 	public List<Battery> search() throws IllegalArgumentException {
 		list = fetchComponents(Battery.class.getName());
 		return list;
 	}
 }
