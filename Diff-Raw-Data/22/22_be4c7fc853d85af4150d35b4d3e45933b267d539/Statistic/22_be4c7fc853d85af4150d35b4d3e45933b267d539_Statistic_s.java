 package site;
 
 /**Because Innerclasses are annoying. Statistic encompasses a public stat int field and String description field*/
 public class Statistic extends Object{
 	public final int stat;
 	public final String description;
 	
 	public Statistic(String desc, int stat){
 		this.stat = stat;
 		this.description = desc;
 	}
 }
