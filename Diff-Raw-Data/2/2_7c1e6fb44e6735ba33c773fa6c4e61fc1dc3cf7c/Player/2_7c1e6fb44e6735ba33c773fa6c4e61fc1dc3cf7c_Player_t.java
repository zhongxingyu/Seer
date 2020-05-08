 package com.admteal.dndhp;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import android.util.Log;
 
 /* 
  * Note: class uses int division because we round down in D&D.
  * 32 should equal 1.
  */
 
 public class Player implements Serializable {
 	private static final long serialVersionUID = -736811401990731225L; //generated
 	
 	private String playerName = "The Only Player";
 	
 	private boolean blinded, dazed, deafened, dominated, dying, grabbed, 
 		helpless, immobile, marked, petrified, prone, restrained, 
 		stunned, unconscious, weakened, usingDefaultPlayer;
 	
 	private int maxHP, currentHP;
 	private int maxSurges, currentSurges;
 	private int currentTHP, currentHS, currentOngo, currentDeathSaves;
 	
     private ArrayList<Integer> changeHistory = new ArrayList<Integer>();
     private ArrayList<Integer> HPHistory = new ArrayList<Integer>();
     
     // Default constructor for if we aren't passing a real max hp
     public Player() {
     	this.maxHP = 999;
     	this.currentHP = 0;
     	this.currentTHP = 0;
     	this.currentOngo = 0;
     	this.maxSurges = 99;
     	this.currentSurges = 0;
     	this.currentHS = 0;
     	this.currentDeathSaves = 3;
      	clearToggles();
      	this.usingDefaultPlayer = true;
     }
     
     // Create a new player at full HP
     public Player(int maxHP, int maxSurges, int currentHS) {
         this.maxHP = maxHP;
     	this.currentHP = maxHP;
     	this.currentTHP = 0;
         this.maxSurges = maxSurges;
         this.currentSurges = maxSurges;
         this.currentHS = currentHS;
      	this.currentDeathSaves = 3;
         clearToggles();
      	this.usingDefaultPlayer = false;
     }
     
     // Create a new player at partial HP
     public Player(int maxHP, int currentHP,
     		int maxSurges, int currentSurges, int currentHS) {
     	this.maxHP = maxHP;
     	this.currentHP = currentHP;
     	this.currentTHP = 0;
         this.maxSurges = maxSurges;
         this.currentSurges = currentSurges;
         this.currentHS = currentHS;
      	this.currentDeathSaves = 3;
         clearToggles();
      	this.usingDefaultPlayer = false;
     }
     
     public void heal(int healBy) {
     	dying = false;
     	if (healBy > 0 && currentHP < 0) {
     		currentHP = 0;
     	}
     	if (currentHP + healBy > maxHP) { //No going above max.
     		changeHistory.add(maxHP-currentHP);
     		currentHP = maxHP;
     	} else {
         	currentHP += healBy;
         	changeHistory.add(healBy);
     	}
     	unconscious = false;
     	HPHistory.add(currentHP);
     }
     
 	public void injure(int injureBy) {
 		int negBloodied = -maxHP / 2;
 		if (injureBy < 0) {
 			// Allows injure() to accept a negative int and behave expectedly
 			injureBy = -injureBy;
 		}
 		// No going below negative bloodied (you're dead)
 		if (currentHP + currentTHP - injureBy <= negBloodied) {
 			currentTHP = 0;
 			changeHistory.add(negBloodied - currentHP);
 			currentHP = negBloodied;
 		} else {
 			// Negative to make it clear this was an injury in the history
 			changeHistory.add(-injureBy);
 			/*
 			 * Consume THP first, before affecting actual HP. We do not want
 			 * either currentTHP or injureBy to drop below 0 at any point
 			 */
 			if (injureBy > currentTHP) {
 				injureBy -= currentTHP;
 				currentTHP = 0;
 			} else if (currentTHP > injureBy) {
 				currentTHP -= injureBy;
 				injureBy = 0;
 			} else {
 				currentTHP = 0;
 				injureBy = 0;
 			}
 
 			currentHP -= injureBy;
 		}
 		HPHistory.add(currentHP);
 		if (currentHP <= 0) {
 			dying = true;
 		}
 	}
 
 	// PLAYER NAME
 	
 	public String getName() {
 		return playerName;
 	}
 	
 	public void setName(String playerName) {
 		this.playerName = playerName;
 	}
 	
     // HP VALUE
     public int getHP() {
         return currentHP;
     }
 
     public void setHP(int currentHP) {
         this.currentHP = currentHP;
     }
 
     // MAX HP VALUE
     public int getMaxHP() {
         return maxHP;
     }
 
     public void setMaxHP(int maxHP) {
         this.maxHP = maxHP;
     }
     	
     // BLOODIED VALUE
     public boolean isBloodied() {
     	if (currentHP <= maxHP/2) {
     		return true;
     	} else {
     		return false;
     	}
     }
 
     // TEMPORARY HP
     public int getTHP() {
     	return currentTHP;
     }
     
     // Only the largest THP number offered applies.
     public void addTHP(int newTHP) {
     	if (newTHP > currentTHP) {
     		currentTHP = newTHP;
     	}
     }
     
     // SURGES COUNT
     public int getSurges() {
         return currentSurges;
     }
 
     public void setSurges(int currentSurges) {
     	if (currentSurges >= 0) {
             this.currentSurges = currentSurges;
     	}
     }
     
     public void addSurge() {
     	currentSurges++;
     }
     
     public void remSurge() {
     	if (currentSurges == 0) {
     		return;
     	}
     	currentSurges--;
     }
     
     // SURGE VALUE
     public void setHS(int currentHS) {
     	this.currentHS = currentHS;
     }
     
     public int getHS() {
     	return currentHS;
     }
     
     // ONGOING VALUE
     public void setOngo(int currentOngo) {
     	this.currentOngo = currentOngo;
     }
     
     public int getOngo() {
     	return currentOngo;
     }
     	
     // Regen is the opposite of ongoing damage
     public int getRegen() { 
         return currentOngo * -1;
     }
 	    
     public void addOngo() {
     	currentOngo++;
     }
     
     public void remOngo() {
     	currentOngo--;
     }
     
     // DEATH SAVES COUNT
     public void setDeathSaves(int currentDeathSaves) {
     	this.currentDeathSaves = currentDeathSaves;
     }
     
     public int getDeathSaves() {
     	return currentDeathSaves;
     }
     
     public void addDeathSave() {
     	currentDeathSaves++;
     }
     
     public void remDeathSave() {
     	currentDeathSaves--;
     }
     
     // STATUS EFFECTS    
 	public boolean isBlinded() {return blinded;}
 	public void blind() {
 		blinded = true;
 	}
 	public void unblind() {
 		blinded = false;
 	}
 	
 	public boolean isDazed() {return dazed;}
 	public void daze() {
 		dazed = true;
 	}
 	
 	public void undaze() {
 		dazed = false;
 	}
 	
 	public boolean isDeafened() {return deafened;}
 	public void deafen() {
 		deafened = true;
 	}
 	public void undeafen() {
 		deafened = false;
 	}
 	
 	public boolean isDominated() {return dominated;}
 	public void dominate() {
 		dominated = true;
 	}
 	public void undominate() {
 		dominated = false;
 	}
 	
 	public boolean isDying() {return dying;}
 	public void kill() {
 		dying = true;
 	}
 	public void unkill() {
 		dying = false;
 	}
 	
 	public boolean isGrabbed() {return grabbed;}
 	public void grab() {
 		grabbed = true;
 	}
 	public void ungrab() {
 		grabbed = false;
 	}
 	
 	public boolean isHelpless() {return helpless;}
 	public void incapacitate() {
 		helpless = true;
 	}
 	public void unincapacitate() {
 		helpless = false;
 	}
 	
 	public boolean isImmobile() {return immobile;}
 	public void immobilize() {
 		immobile = true;
 	}
 	public void unimmobilize() {
 		immobile = false;
 	}
 	
 	public boolean isMarked() {return marked;}
 	public void mark() {
 		marked = true;
 	}
 	public void unmark() {
 		marked = false;
 	}
 	
 	public boolean isPetrified() {return petrified;}
 	public void petrify() {
 		petrified = true;
 	}
 	public void unpetrify() {
 		petrified = false;
 	}
 	
 	public boolean isProne() {return prone;}
 	public void knockProne() {
 		prone = true;
 	}
 	public void getUp() {  // NOTE THIS BREAKS THE FORM BECAUSE unknockProne SOUNDS STUPID
 		prone = false;
 	}
 	
 	public boolean isRestrained() {return restrained;}
 	public void restrain() {
 		restrained = true;
 	}
 	public void unrestrain() {
 		restrained = false;
 	}
 	
 	public boolean isStunned() {return stunned;}
 	public void stun() {
 		stunned = true;
 	}
 	public void unstun() {
 		stunned = false;
 	}
 	
 	public boolean isUnconscious() {return unconscious;}
 	public void knockOut() {
 		unconscious = true;
 	}
 	public void wakeUp() {  // NOTE THIS BREAKS THE FORM BECAUSE unknockOut SOUNDS STUPID
 		unconscious = false;
 	}
 	
 	public boolean isWeakened() {return weakened;}
 	public void weaken() {
 		weakened = true;
 	}
 	public void unweaken() {
 		weakened = false;
 	}
 	
 	// Clears all the toggles.  Private because it should only be called by making a new Player
 	private void clearToggles() {
 		blinded = false;
 		dazed = false;
 		deafened = false;
 		dominated = false;
 		dying = false;
 		grabbed = false;
 		helpless = false;
 		immobile = false;
 		marked = false;
 		petrified = false;
 		prone = false;
 		restrained = false;
 		stunned = false;
 		unconscious = false;
 		weakened = false;	
 	}
 	
 	public void extendedRest() {
 		clearToggles();
 		currentOngo = 0;
 		currentTHP = 0;
 		currentDeathSaves = 0;
 		if (usingDefaultPlayer) {
 			currentHP = 0;
 			currentSurges = 0;
 			currentOngo = 0;
 		} else {
 			currentHP = maxHP;
 			currentSurges = maxSurges;
 		}
 	}
 	
 	public boolean isDefaultPlayer() {
 		return usingDefaultPlayer;
 	}
 	
 	public void undoLast() {
 		undoNum(1);
 	}
 	
 	public void undoNum(int distance) {
		if (changeHistory.size() <= 1 || HPHistory.size() <= 1) {
 			changeHistory.clear();
 			HPHistory.clear();
 			currentHP = 0;
 			
 		} else {
 			for (int i = 1; i <= distance; i++) {
 				changeHistory.remove(changeHistory.size()-1);		
 				HPHistory.remove(HPHistory.size()-1);
 				currentHP = HPHistory.get(HPHistory.size()-1);
 			}
 		}
 	}
     
 	// History trackers.  These must be the same size or it will cause problems.
     public ArrayList<Integer> getChangeHistory() {
     	if (changeHistory.size() != HPHistory.size()) {
     		return null;
     	} else {
     		return changeHistory;
     	}
     }
     
     public ArrayList<Integer> getHPHistory() {
     	if (changeHistory.size() != HPHistory.size()) {
     		return null;
     	} else {
     		return HPHistory;
     	}
     }
     
     // From johndev.net tutorials for manual serialization
     
     public static byte[] serliazeObject(Player player) { 
     	ByteArrayOutputStream bos = new ByteArrayOutputStream();
     	
     	try {
     		ObjectOutput out = new ObjectOutputStream(bos);
     		out.writeObject(player);
     		out.close();
     		
     		// now get the bytes
     		byte[] buf = bos.toByteArray();
     		return buf;
     	} catch (IOException ioe) {
     	      Log.e("serializeObject", "error", ioe); 
     	      return null;
     	}
     }
     
     public static Object deseriaizeObject(byte[] b) {
     	try {
     		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
     		Object player = in.readObject();
     		in.close();
     		
     		return player;
     	} catch(ClassNotFoundException cnfe) { 
     	      Log.e("deserializeObject", "class not found error", cnfe); 
     	      
     	      return null; 
     	    } catch(IOException ioe) { 
     	      Log.e("deserializeObject", "io error", ioe); 
     	 
     	      return null; 
     	    } 
     }
 
 }
