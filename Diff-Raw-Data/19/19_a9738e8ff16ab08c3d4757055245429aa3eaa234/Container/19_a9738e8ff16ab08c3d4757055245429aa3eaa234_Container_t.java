 package edu.neumont.java3.factor.service;
 
 import java.util.ArrayList;
 
 import javax.xml.bind.annotation.*;
 
 @XmlRootElement(name="container")
 public class Container {
	public Long id;
 	public ArrayList<Long> yourlist;
 
 }
