 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import processing.core.PApplet;
 import processing.core.PImage;
 
 //Character object
 //      Stores data for a specific character.
 public class Character {
 
         PApplet processing;
         private String name;                       //Character name (all lowercase by convention)
         private PImage icon;                       //Character icon
         private ArrayList<Integer> episodes_s1;    //List of episode numbers character appears (season 1)
         private ArrayList<Integer> episodes_s2;    //List of episode numbers character appears (season 2)
         private ArrayList<Integer> episodes_s3;    //List of episode numbers character appears (season 3)
         private ArrayList<Integer> episodes_s4;    //List of episode numbers character appears (season 4)
         private ArrayList<Integer> episodes_s5;    //List of episode numbers character appears (season 5)
         private ArrayList<Integer> episodes_s6;    //List of episode numbers character appears (season 6)
         
         private ArrayList<Episode> episodes;       	//List of all episodes character appears (among all seasons)
         private ArrayList<Catchphrase> phrases;		//List of catchphrases the Character is known to say.
         
         //Lists of 'important' words and how often they appear. (per season)
        private ArrayList<Word> wordWeights  = new ArrayList<Word>();
         
         public Character (String n){
 	        name = n;
 	        episodes_s1 = new ArrayList<Integer>();
 	        episodes_s2 = new ArrayList<Integer>();
 	        episodes_s3 = new ArrayList<Integer>();
 	        episodes_s4 = new ArrayList<Integer>();
 	        episodes_s5 = new ArrayList<Integer>();
 	        episodes_s6 = new ArrayList<Integer>();
 
 	        episodes = new ArrayList<Episode>();
         }
        
         //Add episode # to list of episodes character appears in
         public void addEpisode(int s, int e) {
                 //Convert int to Integer
                 Integer e2 = new Integer(e);
                 
                 for (int i=0; i< Parser.LIST_ALL.size(); i++)
                 	if( Parser.LIST_ALL.get(i).getSeason() == s &&  Parser.LIST_ALL.get(i).getEpisode() == e) {
                 		episodes.add(Parser.LIST_ALL.get(i));
                 	}
                 		
                
                 switch(s) {
                 case 1: //If season 1
                 		if(!episodes_s1.contains(e2)) { //Does not add episode to list if episode already exists in list.
                 			episodes_s1.add(e2);
                 		} break;
                 case 2: //If season 2
             		if(!episodes_s2.contains(e2)) { 
             			episodes_s2.add(e2);
             		} break;
                 case 3: //If season 3
             		if(!episodes_s3.contains(e2)) { 
             			episodes_s3.add(e2);
             		} break;
                 case 4: //If season 4
             		if(!episodes_s4.contains(e2)) { 
             			episodes_s4.add(e2);
             		} break;
                 case 5: //If season 5
             		if(!episodes_s5.contains(e2)) { 
             			episodes_s5.add(e2);
             		} break;
                 case 6: //If season 6
             		if(!episodes_s6.contains(e2)) {
             			episodes_s6.add(e2);
             		} break;
                 
                 }
                 
         }
         
       //Return number of episodes character appears in
         public int getTotalEpisodes(){
     		return episodes_s1.size() + episodes_s2.size() + episodes_s3.size()
         			+ episodes_s4.size() + episodes_s5.size() + episodes_s6.size();
         }
        
         //Return number of episodes character appears in by season
         public int getTotalEpisodesBySeason(int s){
         	switch(s) {
         	case 0: // 0 means all seasons
         		return getTotalEpisodes();
         	case 1:
         		return episodes_s1.size();
         	case 2:
         		return episodes_s2.size();
         	case 3:
         		return episodes_s3.size();
         	case 4:
         		return episodes_s4.size();
         	case 5:
         		return episodes_s5.size();
         	case 6:
         		return episodes_s6.size();
         	}
         	return -1;
         }
         
         //Return the list of episodes character appears
         public ArrayList<Episode> getEpisodes() {
         	return episodes;
         }
        
         //Return character name
         public String getName() {
                 return name;
         }
         
         //Add a new catchphrase to phrases arraylist
         public void addPhrase(String p, String r) {
         	if(phrases == null) {
         		phrases = new ArrayList<Catchphrase>();
         	}
         	phrases.add(new Catchphrase(p, r));
         }
         
         //add a new occurrence of a catchphrase (which may or may not already be in the list)
         public void addPhraseOccurrence(String p, String r, int s, int e) {
         	int i = getPhraseIndex(p);
         	if(i == -1) {
         		addPhrase(p, r);
         		i = phrases.size()-1;
         	}
         	
         	phrases.get(i).addToTotals(s, e, 1);
         }
         
         //get index of phrase p
         private int getPhraseIndex(String p) {
         	for(int x=0; x<phrases.size(); ++x) {
         		if(phrases.get(x).getPhrase().equals(p)) {
         			return x;
         		}
         	}
 
         	return -1;
         }
         
         //Return total number of phrases character has
         public int getTotalPhrases() {
         	if(phrases == null) { return -1; }
         	return phrases.size();
         }
         
         //Return a phrase bsed on index number
         public Catchphrase getPhrase(int i) {
         	return phrases.get(i);
         }
         
         //FOR TESTING:: -------
         public void printPhrases() {
         	System.out.println(name.toUpperCase());
         	for(int x=0; x<getTotalPhrases(); ++x) {
         		System.out.println("\t" + phrases.get(x).getTotal() + "\t" + phrases.get(x).getPhrase());
         	}
         }//--------------------
         
         public void setIcon() {
         	icon = GLOBAL.processing.loadImage("images/" +name+ ".jpg");
         }
         
         public PImage getIcon() {
         	return icon;
         }
         
         public ArrayList<Catchphrase> getAllPhrases() {
         	return phrases;
         }        
         
         //METHODS FOR WORD MAPPING ---------------------------
         //----------------------------------------------------
         
         public ArrayList<Word> getWordRange(int s1, int e1, int s2, int e2) {
         	ArrayList<Word> a1 = getWords(s1, e1);	//Get first episode from the season s1
         	ArrayList<Word> a2;	//Used as temporary array to merge into a1
         	
         	//Range is only over 1 season
         	if(s1 == s2) {	
         		for(int x=(e1+1); x<=e2; ++x) {
         			a2 = getWords(s1, x);	//Get next episode
         			a1 = mergeWordArrays(a1, a2);	//Merge current arrays
         		}
         	}
         	
         	//Range is over multiple seasons
         	else {
         		//Parse first season given (seperate b/c first episode may not = 1)
         		for(int x=(e1+1); x<=getSeasonTotal(s1); ++x) {
         			a2 = getWords(s1, x);	//Get next episode
         			a1 = mergeWordArrays(a1, a2);	//Merge current arrays
         		}
         		
         		//Parse all other seasons up to (not including) s2 (seperate b/c includes all episodes)
         		for(int y=s1+1; y<s2; ++y) {
         			for(int z=1; z<=getSeasonTotal(y); ++z) {
         				a2 = getWords(y, z);	//Get next episode
             			a1 = mergeWordArrays(a1, a2);	//Merge current arrays
         			}
         		}
         		
         		//Parse last season in range (s2) 
         		for(int a=1; a<=e2; ++a) {
         			a2 = getWords(s2, a);	//Get next episode
         			a1 = mergeWordArrays(a1, a2);	//Merge current arrays
         		}
         	}
         	
         	//sort array by frequency
         	Collections.sort(a1, new Comparator<Object>(){    		 
                 public int compare(Object o1, Object o2) {
                     Word w1 = (Word) o1;
                     Word w2 = (Word) o2;
                    return w2.getFreq()-w1.getFreq();
                 }
             });
         	
         	return a1;
         }
         
         public ArrayList<Word> mergeWordArrays(ArrayList<Word> a1, ArrayList<Word> a2) {
         	ArrayList<Word> temp = new ArrayList<Word>();
         	ArrayList<Integer> ignore = new ArrayList<Integer>();
         	int i = -1;
         	
         	//No need to merge anything if any of the arrays are empty...
         	if(a1.isEmpty()) { return a2; }
         	if(a2.isEmpty()) { return a1; }
         	
         	//Parse first list
         	for(int x=0; x<a1.size(); ++x) {
         		i = getWordIndex(a1.get(x).getWord(), a2);	//Check if word from a1 exists in a2
         		
         		if(i >= 0) {	//Word exists in both arrays
         			int f = a1.get(x).getFreq() + a2.get(i).getFreq();
         			temp.add(new Word(a1.get(x).getWord(), 0, 0, f));
         			ignore.add(i);
         		}
         		else {
         			temp.add(a1.get(x));
         		}
         	}
         	
         	//Add remaining words from second array
         	for(int y=0; y<a2.size(); ++y) {
         		if(!ignore.contains(y)) {
         			temp.add(a2.get(y));
         		}
         	}
         	
         	return temp;
         }
  
         
         //Return arrayList of all words character says in season s, episode e
         public ArrayList<Word> getWords(int s, int e) {
         	ArrayList<Word> temp = new ArrayList<Word>();	//to be converted to array
         	
         	//Parse wordWeights array list
         	for(int x=0; x<wordWeights.size(); ++x) {
         		if(wordWeights.get(x).getSeason() > s) {
         			break;
         		}
         		if(wordWeights.get(x).getSeason() == s && wordWeights.get(x).getEpisode() == e) {
         			temp.add(wordWeights.get(x));
         		}
         	}
         	
         	return temp;
         }
         
         //Return arrayList of all weights (of words) for season s, episode s
         public ArrayList<Integer> getWeights(int s, int e) {
         	ArrayList<Integer> temp = new ArrayList<Integer>();	//to be converted to array
         	
         	//Parse wordWeights array list
         	for(int x=0; x<wordWeights.size(); ++x) {
         		if(wordWeights.get(x).getSeason() > s) {
         			break;
         		}
         		if(wordWeights.get(x).getSeason() == s && wordWeights.get(x).getEpisode() == e) {
         			temp.add(wordWeights.get(x).getFreq());
         		}
         	}
         	
         	return temp;
         }
         
         //Get index (wordWeights) of word w from season s, episode e
         //	returns -1 if word does not exist in wordWeights
         public int getWordIndex(String w, int s, int e) {
         	for(int x=0; x<wordWeights.size(); ++x) {	//Parse wordWeights
         		if(wordWeights.get(x).getSeason() > s) {	//Word does not exist if we reached a further season
         			break;
         		}
         		else if(wordWeights.get(x).getSeason() == s && wordWeights.get(x).getEpisode() == e && wordWeights.get(x).getWord().equals(w)) { //Found word
         			return x;
         		}
         	}
         	
         	return -1;
         }
         
         //Get index (in any ArrayList<Word> = a) of word w
         //	returns -1 if word does not exist in a
         public int getWordIndex(String w, ArrayList<Word> a) {
         	for(int x=0; x<a.size(); ++x) {	//Parse wordWeights
         		if(a.get(x).getWord().equals(w)) { //Found word
         			return x;
         		}
         	}
         	return -1;
         }
         
         public void addWord(String w, int s, int e) {
         	w = w.toLowerCase();
         	
         	//if(w.equals("michelle")) { 
         	//System.out.println("Mmmm ... \t" + s + ", " + e); }
         	
         	int i = getWordIndex(w,s,e);	//See if word already exists for this episode.
         	if(i == -1) { //Word does not exist
         		wordWeights.add(new Word(w, s, e));
         		return;
         	}
 
         	//Word exists already...
         	wordWeights.get(i).addOne();	//Add one to the weight/frequency of this word
         }
         
         //For Testing...
         public void printWords(int s, int e) {
         	ArrayList<Word> temp = getWords(s, e);
         	
         	for(int x=0; x<temp.size(); ++x) {
         		System.out.println(temp.get(x).getWord() + "\t" + temp.get(x).getFreq());
         	}
         }
         
         //return total number of episodes in season s
         private int getSeasonTotal(int s) {
         	switch(s) {
         	case 1:	return 13;
         	case 2:	return 19;
         	case 3:	return 22;
         	case 4: return 18;
         	case 5:	return 16;
         	case 6:	return 26;
         	default: return 0;
         	}
         }
         
 }
