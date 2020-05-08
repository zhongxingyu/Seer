 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.samples.showcase.example.compat.dataTable;
 
 import java.io.Serializable;
 
 public class Car implements Serializable {
 	protected int id = -1;
 	protected String name;
 	protected String chassis;
 	protected int weight;
 	protected int acceleration;
 	protected double mpg;
 	protected double cost;
 	
 	public Car() {
 	}
         
 	public Car(int id,
 	           String name, String chassis,
 	           int weight, int acceleration, 
 	           double mpg, double cost) {
 		this.id = id;
 		this.name = name;
 		this.chassis = chassis;
 		this.weight = weight;
 		this.acceleration = acceleration;
 		this.mpg = mpg;
 		this.cost = cost;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getChassis() {
 		return chassis;
 	}
 	public void setChassis(String chassis) {
 		this.chassis = chassis;
 	}
 	public int getWeight() {
 		return weight;
 	}
 	public void setWeight(int weight) {
 		this.weight = weight;
 	}
 	public int getAcceleration() {
 		return acceleration;
 	}
 	public void setAcceleration(int acceleration) {
 		this.acceleration = acceleration;
 	}
 	public double getMpg() {
 		return mpg;
 	}
 	public void setMpg(double mpg) {
 		this.mpg = mpg;
 	}
 	public double getCost() {
 	    return cost;
 	}
 	public void setCost(double cost) {
 	    this.cost = cost;
 	}
 	
 	public void applyValues(Car parent) {
 	    setId(parent.getId());
 	    setName(parent.getName());
 	    setChassis(parent.getChassis());
 	    setWeight(parent.getWeight());
 	    setAcceleration(parent.getAcceleration());
 	    setMpg(parent.getMpg());
 	    setCost(parent.getCost());
 	}
 	
 	public String toString() {
 	    return getName();
 	}
 }
