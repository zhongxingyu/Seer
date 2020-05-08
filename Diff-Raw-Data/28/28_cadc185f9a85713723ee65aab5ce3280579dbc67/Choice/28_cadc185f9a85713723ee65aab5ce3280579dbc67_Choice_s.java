 package com.bunkerdev.chooser;
 
 import java.io.Serializable;
 
 public class Choice implements Serializable, Cloneable {
 
 	private static final long serialVersionUID = -2045169531698578334L;
 
 	public enum Weighing {NEVER, ALMOSTIMPOSSIBLE, NORMAL, ABOVENORMAL};
 	
 	private Weighing weight;
 	private String name;
 	private Integer rangeIni, rangeEnd;
 	private boolean range;
 	private Integer chosen;
 	
 	public Choice(String name) {
 		this.name = name;
 		range = false;
 		weight = Weighing.NORMAL;
 	}
 	
 	public Choice(String name, Integer ini, Integer end) {
 		this.name = name;
 		rangeIni = ini;
 		rangeEnd = end;
 		range = true;
 		weight = Weighing.NORMAL;
 	}
 
 	public Weighing getWeight() {
 		return weight;
 	}
 
 	public void setWeight(Weighing weight) {
 		this.weight = weight;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public boolean isRange() {
 		return range;
 	}
 
 	public void setRange(boolean range) {
 		this.range = range;
 	}
 
 	public Integer getRangeIni() {
 		return rangeIni;
 	}
 
 	public void setRangeIni(Integer rangeIni) {
 		this.rangeIni = rangeIni;
 	}
 
 	public Integer getRangeEnd() {
 		return rangeEnd;
 	}
 
 	public void setRangeEnd(Integer rangeEnd) {
 		this.rangeEnd = rangeEnd;
 	}
 	
 
 	public Integer getChosen() {
 		return chosen;
 	}
 
 	public void setChosen(Integer chosen) {
 		this.chosen = chosen;
 	}
 	
 	public String getRangeName(){
 		return rangeIni+" - "+rangeEnd;
 	}
 	
 	@Override
 	public String toString() {
 		if(!range)
 			return name;
 		else
 			return rangeIni+" - "+rangeEnd+": "+chosen; 
 	}
 
 	@Override
 	protected Object clone() {
 		Choice c = null;
 		try{
 			c = (Choice)super.clone();
 			
 		}catch(CloneNotSupportedException e){
 			e.printStackTrace();
 		}
 		return c;
 	}
 	
 	@Override
 	public boolean equals(Object o){
 		boolean res=false;
 		if (o instanceof Choice){
 			Choice c = (Choice)o;
 			if(range==c.isRange()){
 				if(range)
					res = rangeIni.equals(c.getRangeIni()) && rangeEnd.equals(c.getRangeEnd());
 				else
 					res = name.equals(c.getName());
 			}
 		}
 		return res;
 	}
 	
 }
