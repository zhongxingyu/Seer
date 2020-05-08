 package com.eps_hioa_2013.JointAttentionResearchApp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Module{
 	//private File Settingsfile; //should be the txt file, where the settings of the module are saved //not used anymore
 
	private int number; //number of the module
	private String numberString;
 	private String name;
 	private String description;
 	private List<Element> myPreactions = new ArrayList<Element>();
 	private List<Element> mySignals = new ArrayList<Element>();
 	private List<Element> myActions = new ArrayList<Element>();
 	private List<Element> myRewards = new ArrayList<Element>();
 
 
 	public Module()
 	{}
 	
 	public Module(String myNumber)
 	{
 		numberString = myNumber;
 	}
 	
 	public Module(int myNumber, String myName, String myDescription)
 	{
 		number = myNumber;
 		name = myName;
 		description = myDescription;
 
 	}
 	
 	public String getNumberString()
 	{
 		return numberString;
 	}
 
 	public Element getRandomRewardElement()
 	{
 		Element element = null;
 		if(!myRewards.isEmpty())
 		{
 			element = myRewards.get((int)(Math.random() * (myRewards.size())));
 		}
 		return element;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public String getDescription()
 	{
 		return description;
 	}
 
	public int getNumber() {
 		return number;
 	}
 
	public void setNumber(int number) {
		this.number = number;
 	}
 
 	public void addElement(int stage, Element gameElement)
 	{
 		switch (stage) {					
 		case 0:
 			myPreactions.add(gameElement);
 			break;
 		case 1:
 			mySignals.add(gameElement);
 			break;
 		case 2:
 			myActions.add(gameElement);
 			break;
 		case 3:
 			myRewards.add(gameElement);
 			break;
 		default:
 			//Shouldn't get here stage is bigger than 3 or lower than 0
 			break;
 		}
 	}
 	
 	public List<Element> getPreactions()
 	{
 		return myPreactions;
 	}
 
 	public List<Element> getActions()
 	{
 		return myActions;
 	}
 
 	public List<Element> getSignals()
 	{
 		return mySignals;
 	}
 
 	public List<Element> getRewards()
 	{
 		return myRewards;
 	}
 
 	public Element getRandomSignalElement() {
 		Element element = null;
 		if(!mySignals.isEmpty())
 		{
 			element = mySignals.get((int)(Math.random() * (mySignals.size())));
 		}
 		return element;
 	}
 
 }
