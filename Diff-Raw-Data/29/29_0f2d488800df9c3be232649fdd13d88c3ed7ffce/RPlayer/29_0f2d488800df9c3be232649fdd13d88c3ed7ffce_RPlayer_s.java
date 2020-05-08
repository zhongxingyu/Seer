 package net.croxis.plugins.research;
 
 import java.util.HashSet;
 
 public class RPlayer {
 	public String name = "";
 	//public String cantplace = "";
 	//public String cantbuild = "";
 	//public String cantcraft = "";
 	//public HashSet<Integer> cantPlace = new HashSet<Integer>();
	public HashSet<Integer> cantPlace = Research.validIds;
	public HashSet<Integer> cantBreak = Research.validIds;
	public HashSet<Integer> cantCraft = Research.validIds;
	public HashSet<Integer> cantUse = Research.validIds;
 	public HashSet<String> permissions = new HashSet<String>();
 }
