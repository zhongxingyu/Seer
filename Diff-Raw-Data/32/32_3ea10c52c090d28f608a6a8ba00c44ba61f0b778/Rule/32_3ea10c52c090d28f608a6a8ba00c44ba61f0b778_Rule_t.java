 package ll1;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Rule
 {
     private String lhs ;
     private ArrayList<ArrayList<String>> rhs ;
     private ArrayList<String> firstSet;
     private ArrayList<String> followSet;
     private boolean start;
     
     public static HashMap<String, String> tokenToRegex = new HashMap<String,String>(){
         private static final long serialVersionUID = -3387608531616831640L;
     {
     this.put("$BEGIN","begin");
     this.put("$END", "end");
     this.put("$EQ", "=");
     this.put("$REPLACE", "replace");
     this.put("$WITH", "with");
     this.put("$IN", "in");
     this.put("$SEMICOLON", ";");
     this.put("$RECREP", "recursivereplace");
     this.put("$GRTNOT", ">!");
     this.put("$PRINT", "print");
     this.put("$OPENPARENS", "("); // \(
     this.put("$CLOSEPARENS", ")"); // \)
     this.put("$COMMA", ",");
     this.put("$HASH", "#");
     this.put("$FIND", "find");
     this.put("$DIFF", "diff");
     this.put("$UNION", "union");
     this.put("$INTERS", "inters");
     this.put("$MAXFREQ", "maxfreqstring");
     }};
     
     public static HashMap<String, String> regexToToken = new HashMap<String,String>(){
         private static final long serialVersionUID = 1610564828203122262L;
     {
     this.put("begin","$BEGIN");
     this.put("end", "$END");
     this.put("=", "$EQ");
    this.put("replace","$REPLACE");
    this.put("with","$WITH");
    this.put("in","$IN");
    this.put(";","$SEMICOLON");
    this.put("recursivereplace","$RECREP");
    this.put(">!","$GRTNOT");
    this.put("print","$PRINT");
    this.put("(","$OPENPARENS"); // \(
    this.put(")","$CLOSEPARENS"); // \)
    this.put(",","$COMMA");
    this.put("#","$HASH");
    this.put("find","$FIND");
    this.put("diff","$DIFF");
    this.put("union","$UNION");
    this.put("inters","$INTERS");
    this.put("maxfreqstring","$MAXFREQ");
     }};
     
     public Rule(){
         this.lhs = "";
         this.rhs = new ArrayList<ArrayList<String>>();
         this.firstSet = new ArrayList<String>();
         this.followSet = new ArrayList<String>();
         this.start = false;
     }
     
     
     public boolean isStart() {
 		return start;
 	}
 
 
 	public void setStart(boolean start) {
 		this.start = start;
 	}
 
 
 	public String getLHS(){
         return this.lhs;
     }
     public ArrayList<ArrayList<String>> getRHS(){
         return this.rhs;
     }
     
     public void init(String line){
         int coloncolonequals = line.indexOf("::=");
         if(coloncolonequals < 0){
             System.err.println("The line: " + line + " failed.");
             System.exit(0);
         }
         int rhsIndex = 0;
         String lhs = line.substring(0, coloncolonequals).trim();
         String rhs = line.substring(coloncolonequals+3);
         StringBuilder sb = new StringBuilder();
         this.rhs.add(new ArrayList<String>());
         for(Character c : rhs.toCharArray()){
             if(c == ' ' || (c == '>') && (sb.length() > 0 && sb.charAt(0)=='<')){
                 if(c == '>')
                     sb.append(c);
                 if(sb.toString().length() > 0){
                     this.rhs.get(rhsIndex).add(sb.toString());
                     sb.delete(0, sb.length());
                 }
             } else if(c == '|'){
                 this.rhs.add(new ArrayList<String>());
                 rhsIndex += 1;
                 sb.delete(0, sb.length());
             } else {
                 sb.append(c);
             }
         }
         if(sb.toString().length() > 0)
             this.rhs.get(rhsIndex).add(sb.toString());
         this.lhs = lhs;
     }
     
     public void merge(Rule other){
         assert(this.lhs.equals(other.lhs));
         for(ArrayList<String> als : other.rhs)
             this.rhs.add(als);
     }
     
     public String toString(){
         StringBuilder sb = new StringBuilder();
         sb.append(this.lhs + " ::=\n");
         for(ArrayList<String> onerhs : this.rhs){
             sb.append("\t[");
             for(int i = 0;  i < onerhs.size(); i++){
                 sb.append(onerhs.get(i));
                 if(i != onerhs.size()-1)
                     sb.append(", ");
             }
             sb.append("]\n");
         }
         return sb.toString();
     }
     
     public static boolean isRule(String token){
         if(token.equals("<epsilon>"))
             return false;
         return token.charAt(0) == '<' && token.charAt(token.length()-1) == '>';
     }
     
     public void addToFirst(String input){
     	if(!this.firstSet.contains(input)){
     	 	this.firstSet.add(input);
     	}
     }
     
     public void addToFirst(ArrayList<String> input){
     	for(String i : input){
     		if(!this.firstSet.contains(i)){
     			this.firstSet.add(i);
     		}
     	}
     }
     
     public void addToFollow(String input){
     	if(!this.followSet.contains(input)){
     	 	this.followSet.add(input);
     	}
     }
     
     public void addToFollow(ArrayList<String> input){
     	for(String i : input){
     		if(!this.followSet.contains(i)){
     			this.followSet.add(i);
     		}
     	}
     }
     
     public ArrayList<String> getFirstSet(){
     	return this.firstSet;
     }
     
     public ArrayList<String> getFollowSet(){
     	return this.followSet;
     }
     
     public void printFirstSet(){
     	System.out.print(this.lhs + " : ");
     	System.out.print("{");
     	for(String s : this.firstSet){
     		System.out.print(" "+s+" ");
     	}
     	System.out.println("}");
     }
     
     public void printFollowSet(){
     	System.out.print(this.lhs + " : ");
     	System.out.print("{");
     	for(String s : this.followSet){
     		System.out.print(" "+s+" ");
     	}
     	System.out.println("}");
     }
     
   
    
     
     
 }
