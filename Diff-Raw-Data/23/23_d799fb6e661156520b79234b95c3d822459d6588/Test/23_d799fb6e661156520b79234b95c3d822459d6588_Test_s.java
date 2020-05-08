 package net.jnickg.dnd;
 
 //tests getters and setters
 public class Test {
 
 	public static void main(String[] args) {
 		
 		//create a character object
 		Character guy = new Character();
 		
 		//welcome user
 		System.out.println("Welcome to a test for DnD app By JnickG and Castlez! (Version 1.0)");
 		System.out.print("were going to make a character. then print it out!");
 		
 		//build guy
 		guy.setCharAge(69);
 		guy.setCharAlignment("Lawful Evil");
 		guy.setCharClass("BARBARIAN");
		guy.getCharClass(0).addXP(2000);
 		guy.setCharClass("WIZARD");
		guy.getCharClass(1).addXP(1000);
 		guy.setCharDie(0);
 		guy.setCharBAB(3);
 		guy.setCharCHA(13);
 		guy.setCharCON(13);
 		guy.setCharDeity("Satan");
 		guy.setCharDEX(13);
 		guy.setCharEyes("Blue");
 		guy.setCharGender("male");
 		guy.setCharHair("Blond");
 		guy.setCharHeight(6);
 		guy.setCharHP(12);
 		guy.setCharDMG(6);
 		guy.setCharINT(13);
 		guy.setCharName("Dude Man");
 		guy.setCharNLDMG(0);
 		guy.setCharSkin("Whitey");
 		guy.setCharSpeed(30);
 		guy.setCharSTR(13);
 		guy.setCharWeight(150);
 		guy.setCharWIS(13);
 			
 		//print guy
 		System.out.println("\nPrinting your guy:\n\n");
 		
 		//Personal Info
 		System.out.println("Personal Info\n");
 		for(int p = 0;p<guy.getClassNum();p++){
 		System.out.println("Class " + (p+1) + ": " + guy.getCharClass(p).getName());
 		}
 		System.out.println("Name: " + guy.getCharName());
 		System.out.println("Age: " + guy.getCharAge());
 		System.out.println("Gender: " + guy.getCharGender());
 		System.out.println("Height: " + guy.getCharHeight());
 		System.out.println("Weight: " + guy.getCharWeight());
 		System.out.println("Hair color: " + guy.getCharHair());
 		System.out.println("Eye Color: " + guy.getCharEyes());
 		System.out.println("Skin Color: " + guy.getCharSkin());
 		System.out.println("Diety: " + guy.getCharDeity());
 		
 		//Abilities
 		System.out.println("\nAbilities\n");
 		System.out.println("Str: " + guy.getCharSTR());
 		System.out.println("Dex: " + guy.getCharDEX());
 		System.out.println("Con: " + guy.getCharCON());
 		System.out.println("Int: " + guy.getCharINT());
 		System.out.println("Wis: " + guy.getCharWIS());
 		System.out.println("Cha: " + guy.getCharCHA());
 		
 		//Vitals
 		System.out.println("\nVitals\n");
 		System.out.println("Health (max/current): " + guy.getCharHP() + "/" + (guy.getCharHP()-guy.getCharDMG()));
 		System.out.println("Non-Lethal Damage: " + guy.getCharNLDMG());
 		System.out.println("Hit Die: " + guy.getHitDie());
 		
 		//Experience
 		System.out.println("\nCurrent Level: " + guy.getCharLevel());
 		for(int e = 0;e<guy.getClassNum();e++){
 		System.out.println("Class " + (e+1) +" Experience: " + guy.getCharClass(e).getXP());
 		}
 		System.out.println("Total: " + guy.getTotalXP());
 		
 		//Combat
 		System.out.println("\nCombat\n");
 		System.out.println("BAB: " + guy.getCharBAB());
 		System.out.println("Speed: " + guy.getCharSpeed());
 		
 	}
 
 }
