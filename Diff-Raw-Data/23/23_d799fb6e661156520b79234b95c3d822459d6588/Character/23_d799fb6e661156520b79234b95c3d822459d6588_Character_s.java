 package net.jnickg.dnd;
 
 import java.util.*;
 
 /** Manages subordinate classes of information, and compiles it to local data
  */
 public class Character {
 	
 /* Variable Declarations */
 	private				String		charName;	//The character's name
 	private				String		charAlignment, charDeity, charGender, charEyes, charHair, charSkin;	//Aesthetic character details
 	private				int			charAge, charWeight, charHeight;	//Aesthetic numerical character details
 	private				int			charExperience, charLevel, HitDie;	//Character Levelrelevant statistics
 	private				int			charSTR, charDEX, charCON, charINT, charWIS, charCHA;	//Character ability scores
 	private				int			charHP, charDMG, charNLDMG;	//Character vitals
 	private				int			charBAB, charSpeed;	//Combat-relevant statistics
 	private				List<PlayerClass>	playclass;	//a players class
 	
 	
 	//null constructor
 	public Character(){
 		this.charName = null;
 		this.charAlignment = null;
 		this.charDeity = null;
 		this.charGender = null;
 		this.charEyes = null;
 		this.charHair = null;
 		this.charSkin = null;
 		this.charAge = 0;
 		this.charWeight = 0;
 		this.charHeight = 0;
 		this.charExperience = 0;
 		this.charLevel = 0;
 		this.charSTR = 0;
 		this.charDEX = 0;
 		this.charCON = 0;
 		this.charINT = 0;
 		this.charWIS = 0;
 		this.charCHA = 0;
 		this.charHP = 0;
 		this.charDMG = 0;
 		this.charNLDMG = 0;
 		this.charBAB = 0;
 		this.charSpeed = 0;
 		this.HitDie = 6;
 		playclass=new ArrayList<>();
  
 	}
 	
 	/* Constructor */
 	public Character(String charName, String charAlignment, String charDeity,
 			String charGender, String charEyes, String charHair,
 			String charSkin, int charAge, int charWeight, int charHeight,
 			int charExperience, int charLevel, int charSTR, int charDEX,
 			int charCON, int charINT, int charWIS, int charCHA, int charHP,
 			int charDMG, int charNLDMG, int charBAB, int charSpeed, int HitDie) {
 		this.charName = charName;
 		this.charAlignment = charAlignment;
 		this.charDeity = charDeity;
 		this.charGender = charGender;
 		this.charEyes = charEyes;
 		this.charHair = charHair;
 		this.charSkin = charSkin;
 		this.charAge = charAge;
 		this.charWeight = charWeight;
 		this.charHeight = charHeight;
 		this.charExperience = charExperience;
 		this.charLevel = charLevel;
 		this.charSTR = charSTR;
 		this.charDEX = charDEX;
 		this.charCON = charCON;
 		this.charINT = charINT;
 		this.charWIS = charWIS;
 		this.charCHA = charCHA;
 		this.charHP = charHP;
 		this.charDMG = charDMG;
 		this.charNLDMG = charNLDMG;
 		this.charBAB = charBAB;
 		this.charSpeed = charSpeed;
 		this.HitDie = HitDie;
 		playclass = new ArrayList<>(); //to allow for multiclassing
 	}
 
 
 	/* Getters */
 	public String getCharName() {
 		return charName;
 	}
 
 	public String getCharAlignment() {
 		return charAlignment;
 	}
 	
 	public int getClassNum(){
 		return playclass.size();
 	}
 
 	public String getCharDeity() {
 		return charDeity;
 	}
 	
 	public Integer getHitDie() {
 		return HitDie;
 	}
 
 	public String getCharGender() {
 		return charGender;
 	}
 
 	public String getCharEyes() {
 		return charEyes;
 	}
 
 	public String getCharHair() {
 		return charHair;
 	}
 
 	public String getCharSkin() {
 		return charSkin;
 	}
 
 	public int getCharAge() {
 		return charAge;
 	}
 
 	public int getCharWeight() {
 		return charWeight;
 	}
 
 	public int getCharHeight() {
 		return charHeight;
 	}
 
 	public int getCharExperience() {
 		return charExperience;
 	}
 
 /* Incomplete */
 	public PlayerClass getCharClass(int whichclass){
 		return playclass.get(whichclass);
 	}
 	
 	public String getCharClass(){
 		StringBuilder st=new StringBuilder();
 		for(PlayerClass pc:playclass){
 			st.append(pc.getName() + "\t");
 		}
 		return st.toString();
 	}
 	
 	public int getTotalXP(){
 		int txp = 0;
 		for(int p = 0;p<playclass.size();p++){
 			txp += playclass.get(p).getXP();
 		}
 		return txp;
 	}
 
 	public int getCharLevel() {
 		int y=this.getTotalXP();
 		this.charLevel = 1;
 		if(y<1000) return this.charLevel;
		while(true){
			if(this.charLevel*(this.charLevel-1)*500<y){ this.charLevel++; continue;}
			else return this.charLevel;
 		}
 	}
 
 	public int getCharSTR() {
 		return charSTR;
 	}
 
 	public int getCharDEX() {
 		return charDEX;
 	}
 
 	public int getCharCON() {
 		return charCON;
 	}
 
 	public int getCharINT() {
 		return charINT;
 	}
 
 	public int getCharWIS() {
 		return charWIS;
 	}
 
 	public int getCharCHA() {
 		return charCHA;
 	}
 
 	public int getCharHP() {
 		return charHP;
 	}
 
 	public int getCharDMG() {
 		return charDMG;
 	}
 
 	public int getCharNLDMG() {
 		return charNLDMG;
 	}
 
 	public int getCharBAB() {
 		return charBAB;
 	}
 
 	public int getCharSpeed() {
 		return charSpeed;
 	}
 
 /* Setters */
 	public void setCharName(String charName) {
 		this.charName = charName;
 	}
 
 	public void setCharAlignment(String charAlignment) {
 		this.charAlignment = charAlignment;
 	}
 
 	public void setCharDeity(String charDeity) {
 		this.charDeity = charDeity;
 	}
 	
 	public void setCharClass(String pcl){
 		PlayerClass pc = new PlayerClass(pcl);
 		playclass.add(pc);
 		
 	}
 
 	public void setCharGender(String charGender) {
 		this.charGender = charGender;
 	}
 	
 	public void setCharDie(int whichclass) {
 		this.HitDie = playclass.get(whichclass).getDie();
 	}
 
 	public void setCharEyes(String charEyes) {
 		this.charEyes = charEyes;
 	}
 
 	public void setCharHair(String charHair) {
 		this.charHair = charHair;
 	}
 
 	public void setCharSkin(String charSkin) {
 		this.charSkin = charSkin;
 	}
 
 	public void setCharAge(int charAge) {
 		this.charAge = charAge;
 	}
 
 	public void setCharWeight(int charWeight) {
 		this.charWeight = charWeight;
 	}
 
 	public void setCharHeight(int charHeight) {
 		this.charHeight = charHeight;
 	}
 
 	public void setCharSTR(int charSTR) {
 		this.charSTR = charSTR;
 	}
 
 	public void setCharDEX(int charDEX) {
 		this.charDEX = charDEX;
 	}
 
 	public void setCharCON(int charCON) {
 		this.charCON = charCON;
 	}
 
 	public void setCharINT(int charINT) {
 		this.charINT = charINT;
 	}
 
 	public void setCharWIS(int charWIS) {
 		this.charWIS = charWIS;
 	}
 
 	public void setCharCHA(int charCHA) {
 		this.charCHA = charCHA;
 	}
 
 	public void setCharHP(int charHP) {
 		this.charHP = charHP;
 	}
 
 	public void setCharDMG(int charDMG) {
 		this.charDMG = charDMG;
 	}
 
 	public void setCharNLDMG(int charNLDMG) {
 		this.charNLDMG = charNLDMG;
 	}
 
 	public void setCharBAB(int charBAB) {
 		this.charBAB = charBAB;
 	}
 
 	public void setCharSpeed(int charSpeed) {
 		this.charSpeed = charSpeed;
 	}
 	
 }
