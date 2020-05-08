 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 // Class for managing the parsing of the text file
 public class Parser {
        
         //HashMap for storing all dialog.
         //      Key:    (String) S# + E# + CharacterName
         //      Value:  (String) Dialog
         //public static HashMap<String, String> ALL_DATA = new HashMap<String, String>();
        
         //ArrayList for storing all characters.
         public static ArrayList<Character> ALL_CHARACTERS = new ArrayList<Character>();
        
         //6 ArrayLists (1 Per Season)
         //Each hold episode information via Episode objects.
         public static ArrayList<Episode> LIST_S1 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S2 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S3 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S4 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S5 = new ArrayList<Episode>();
         public static ArrayList<Episode> LIST_S6 = new ArrayList<Episode>(); 
         
         //1 Arraylist that contains all the season
         public static ArrayList<Episode> LIST_ALL = new ArrayList<Episode>(); 
         
         //Array that lists characters who have catchphrases
         String[] phraseChars = {"fry", "bender", "leela", "farnsworth", "zoidberg", "hermes", "amy", "zapp brannigan", 
         						"lrrr", "kif", "scruffy", "dwight", "terry", "url", "elzar", "vernon", "santa",
         						 "labarbara", "cubert", "sal", "nixon", "nd-nd", "hattie"};
        
         int episode;    //Current episode number
         int season;		//Current season number
         //String hashKey; //HashMap key based on episode and season
         
         Episode ep;		//Current object to be added to arraylist
        
         int lastCharacter;	//Index in ALL_CHARACTERS of the character who last spoke
        
         //Character names to ignore
         ArrayList<String> backgroundChars = new ArrayList<String>();
 
         public void parseBackgroundChars(){
         	//Constructor simply loads unacceptable character names into backgroundChars from file names.txt
 //        	Scanner scan;    //Scanner for reading file
 //        	
 //        	try {
 //        		scan = new Scanner(new FileReader("data/names.txt"));   //Initialize scanner with file.
 //	        } catch (FileNotFoundException e) {
 //	                e.printStackTrace();
 //	                return;
 //	        }
 //
 //	        //Scan all dialog
 //	        while (scan.hasNextLine()) {    //Executes if a line of text exists
 //	                backgroundChars.add(scan.nextLine());
 //	        }
 //	        
 //	        scan.close();
 //	        
             String[] lines = GLOBAL.processing.loadStrings("names.txt");
             
             for(int j=0; j<lines.length ; j++) {
             	backgroundChars.add(lines[j]);
             }
 	        
         }
 
         public void parseFile(String fileName) {
         	if(GLOBAL.parseForWordMap) {	//If we are parsing word map transcripts
         		//remove last four characters from filename (".txt")
         		int i = fileName.indexOf(".txt");
         		fileName = fileName.substring(0, i);
         		//add underscore & re-insert ".txt"
         		fileName += "_.txt";
         	}
         	
         	parseFile(fileName, GLOBAL.parseForWordMap);
         }
         
         //Parses transcripts (wordsFile = false)
         //OR word transcripts (have an added underscore to filename, and are used for word mapping) (wordsFile = true)
         public void parseFile(String fileName, boolean wordsFile) {
 //            Scanner scan = null;    //Scanner for reading file
 //            ep = null;
 //           
 //            try {
 //                    scan = new Scanner(new FileReader(fileName));   //Initialize scanner with file.
 //            } catch (FileNotFoundException e) {
 //                    e.printStackTrace();
 //                    return;
 //            }
 //           
 //            //Scan first line
 //            storeLineTitle(scan.nextLine());        //Store title of episode
 //           
 //            //Scan all dialog
 //            while (scan.hasNextLine()) {    //Executes if a line of text exists
 //            	if(wordsFile) { storeLineWords(scan.nextLine()); }
 //            	else { storeLine(scan.nextLine()); }    //Sends line to method storeLine()
 //            }
 //           
 //            scan.close();
             
             String[] lines = GLOBAL.processing.loadStrings(fileName);
             
             storeLineTitle(lines[0]);
             
             for(int j = 1; j < lines.length; j++) {
             	if(wordsFile) { storeLineWords(lines[j]); }
             	else { storeLine(lines[j]); }    //Sends line to method storeLine()
             }
             
             if(!wordsFile) { //Only do the following when parsing initial transcripts
             	updateCharacterEpisodes();      //Add episode to appropriate Character objects.
             	updateArray();  //Add episode to appropriate array list
             }
         }
        
         //Sends all Futurama transcripts through the method parseFile()
         public void parseAllTranscripts() {
         	String path;
         	
             //SEASON 1  
             parseFile("transcripts/SEASON1/S1E1.txt"); //Series 1
             parseFile("transcripts/SEASON1/S1E2.txt");
             parseFile("transcripts/SEASON1/S1E3.txt");
             parseFile("transcripts/SEASON1/S1E4.txt");
             parseFile("transcripts/SEASON1/S1E5.txt");
             parseFile("transcripts/SEASON1/S1E6.txt");
             parseFile("transcripts/SEASON1/S1E7.txt");
             parseFile("transcripts/SEASON1/S1E8.txt");
             parseFile("transcripts/SEASON1/S1E9.txt");
             parseFile("transcripts/SEASON1/S2E1.txt");	//Series 2
             parseFile("transcripts/SEASON1/S2E2.txt");
             parseFile("transcripts/SEASON1/S2E3.txt");
             parseFile("transcripts/SEASON1/S2E4.txt");
            
             //SEASON 2
             path = "transcripts/SEASON2/S2E";	//Series 2, continued
             for(int x=5; x<10; ++x) { //Build path name dynamically.
             	parseFile(path + x + ".txt");	
             }
             parseFile("transcripts/SEASON2/S2E11.txt");
             parseFile("transcripts/SEASON2/S2E10.txt");
             parseFile("transcripts/SEASON2/S2E12.txt");
             parseFile("transcripts/SEASON2/S2E13.txt");
             parseFile("transcripts/SEASON2/S2E15.txt");
             parseFile("transcripts/SEASON2/S2E14.txt");
             parseFile("transcripts/SEASON2/S2E16.txt");
             parseFile("transcripts/SEASON2/S2E17.txt");
             parseFile("transcripts/SEASON2/S2E19.txt");
             parseFile("transcripts/SEASON2/S2E18.txt");
             parseFile("transcripts/SEASON2/S2E20.txt");
             
             parseFile("transcripts/SEASON2/S3E2.txt"); //Series 3
             parseFile("transcripts/SEASON2/S3E1.txt");
             parseFile("transcripts/SEASON2/S3E3.txt");
             
             //SEASON 3
             parseFile("transcripts/SEASON3/S3E5.txt");
             parseFile("transcripts/SEASON3/S3E4.txt");
             
             parseFile("transcripts/SEASON3/S4E2.txt");
             
             parseFile("transcripts/SEASON3/S3E10.txt");
             parseFile("transcripts/SEASON3/S3E9.txt");
             parseFile("transcripts/SEASON3/S3E6.txt"); 
             parseFile("transcripts/SEASON3/S3E7.txt");
             parseFile("transcripts/SEASON3/S3E8.txt");
             parseFile("transcripts/SEASON3/S3E11.txt");
             
             parseFile("transcripts/SEASON3/S4E6.txt");
             parseFile("transcripts/SEASON3/S3E12.txt");
             parseFile("transcripts/SEASON3/S5E3.txt");
             
             parseFile("transcripts/SEASON3/S3E13.txt");
             parseFile("transcripts/SEASON3/S3E14.txt");
             parseFile("transcripts/SEASON3/S3E15.txt");
             
             parseFile("transcripts/SEASON3/S4E10.txt");
             parseFile("transcripts/SEASON3/S4E7.txt");
             parseFile("transcripts/SEASON3/S4E3.txt");
             parseFile("transcripts/SEASON3/S4E1.txt");
             parseFile("transcripts/SEASON3/S4E8.txt");
             parseFile("transcripts/SEASON3/S4E9.txt");
             parseFile("transcripts/SEASON3/S4E11.txt");
             
             //SEASON 4
             parseFile("transcripts/SEASON4/S5E5.txt");
             parseFile("transcripts/SEASON4/S4E5.txt");
             parseFile("transcripts/SEASON4/S4E4.txt");
             
             parseFile("transcripts/SEASON4/S5E6.txt");
             parseFile("transcripts/SEASON4/S5E4.txt");
             parseFile("transcripts/SEASON4/S5E15.txt");
             parseFile("transcripts/SEASON4/S5E2.txt");
             parseFile("transcripts/SEASON4/S5E1.txt");
             parseFile("transcripts/SEASON4/S5E7.txt");
             parseFile("transcripts/SEASON4/S5E8.txt");
             
             parseFile("transcripts/SEASON4/S4E12.txt");
             
             parseFile("transcripts/SEASON4/S5E9.txt");
             parseFile("transcripts/SEASON4/S5E13.txt");
             parseFile("transcripts/SEASON4/S5E14.txt");
             parseFile("transcripts/SEASON4/S5E10.txt");
             parseFile("transcripts/SEASON4/S5E11.txt");
             parseFile("transcripts/SEASON4/S5E12.txt");
             parseFile("transcripts/SEASON4/S5E16.txt");
             
             //SEASON 5
             path = "transcripts/SEASON5/S6E";
             for(int b=1; b<17; ++b) {
             	parseFile(path + b + ".txt");
             }
             
             //SEASON 6
             path = "transcripts/SEASON6/S7E";
             for(int c=1; c<14; ++c) {
             	parseFile(path + c + ".txt");
             }
             
             parseFile("transcripts/SEASON6/S8E5.txt");
             parseFile("transcripts/SEASON6/S8E8.txt");
             parseFile("transcripts/SEASON6/S8E4.txt");
             parseFile("transcripts/SEASON6/S8E2.txt");
             parseFile("transcripts/SEASON6/S8E10.txt");
             parseFile("transcripts/SEASON6/S8E3.txt");
             parseFile("transcripts/SEASON6/S8E1.txt");
             parseFile("transcripts/SEASON6/S8E6.txt");
             parseFile("transcripts/SEASON6/S8E9.txt");
             parseFile("transcripts/SEASON6/S8E7.txt");
             parseFile("transcripts/SEASON6/S8E11.txt");
             parseFile("transcripts/SEASON6/S8E12.txt");
             parseFile("transcripts/SEASON6/S8E13.txt");
             
             if(!GLOBAL.parseForWordMap) {
 	            //Aggregate all seasons
 	            LIST_ALL.addAll(LIST_S1);
 	            LIST_ALL.addAll(LIST_S2);
 	            LIST_ALL.addAll(LIST_S3);
 	            LIST_ALL.addAll(LIST_S4);
 	            LIST_ALL.addAll(LIST_S5);
 	            LIST_ALL.addAll(LIST_S6);
             
 	            /*for(int n=0; n<LIST_ALL.size(); ++n) {
 	            	Episode temp = LIST_ALL.get(n);
 	            	System.out.println( temp.getSeason() + ": " + temp.getEpisode());
 	            }*/
             }
         }
        
         //Add episode to appropriate ArrayList
         private void updateArray() {
             switch(season) {
             case 1: LIST_S1.add(ep); return;
             case 2: LIST_S2.add(ep); return;
             case 3: LIST_S3.add(ep); return;
             case 4: LIST_S4.add(ep); return;
             case 5: LIST_S5.add(ep); return;
             case 6: LIST_S6.add(ep); return;
             }
         }
        
         //Store Line of dialog
         private void storeLine(String line) {
                 //Split line so character name and dialog are seperated.
                 String[] splitLine = line.split("\t", 2);
                 
                 if(splitLine.length<2) { return; }
                
                 String character = splitLine[0].toLowerCase();  //Store character name
                 character = updateName(character);	//Check to see if name needs to be changed (eg. "inez" is also = "mrs. wong")
                 
                 //Add character to episode (unless is a background character)
                 if(!isBackgroundCharacter(character)){
                     
                     int i  = findCharacter(character);	
                     
                     if(i < 0){	//If chracter does not exist
                     		Character chr = new Character(character, GLOBAL.COLORS.getNextColor());	//make a new Character object
                     		chr.addAppearence(ALL_CHARACTERS.get(lastCharacter).getName());	//Add the previous Character to its HashMap
                     		
                             ALL_CHARACTERS.add(chr);	//Add Character object to ArrayList
                             ep.addCharacter(chr);		//Add Character object to Episode
                             
                             lastCharacter = ALL_CHARACTERS.size()-1;	//Store index of this Character
                     }
                     else {	//Add Character object to Episode
                     	ALL_CHARACTERS.get(i).addAppearence(ALL_CHARACTERS.get(lastCharacter).getName());
                     	ep.addCharacter(ALL_CHARACTERS.get(i));
                     	lastCharacter = i;	//Store index of this Character
                     }
                     
                     //Determine if current line is a catchphrase of character
                     findCatchphrase(character, splitLine[1]);
                 }
                
         }
        
         //Store Episode Title
         //Called only for first line when .txt file is opened
         private void storeLineTitle(String line) {
                 //Split line so season #, episode #, episode title are seperated
                 String[] splitLine = line.split("\t", 8);
                
                 season = Integer.parseInt(splitLine[0]);        //Store season #
                 episode = Integer.parseInt(splitLine[1]);       //Store episode #
 
                 if(!GLOBAL.parseForWordMap) {
                 	//"19990328" : format of date in text file
                     //"03/28/1990" : format of String airdate below...
 //                    String airdate = splitLine[5].substring(4,6) + "/" +
 //                    					splitLine[5].substring(6) + "/" +
 //                    						splitLine[5].substring(0,4);
                     
                     // DANI mod
                     DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                     DateFormat niceFormatter = new SimpleDateFormat("dd MMM yyyy");
                     
                     try {
 						Date airdate= formatter.parse(splitLine[5]);
 					
                 	
                 	ep = new Episode(episode, season, splitLine[2], niceFormatter.format(airdate));
                 	//hashKey = "S" + season + "E" + episode;         //Store current hash map key prefix
                 	
                     } catch (Exception e) {
 						e.printStackTrace();
 					}
                 }
         }
         
         //Store Line of words (as opposed to dialog, for only the 'main' words are included in the file)
         private void storeLineWords(String line) {
         	//Split line so character name and dialog are seperated.
             String[] splitLine = line.split("\t", 2);
             
             if(splitLine.length<2) { return; }
            
             String character = splitLine[0].toLowerCase();  //get character name
             
             if(character.length() < 1) { return; }
             
             character = updateName(character);	//Check to see if name needs to be changed (eg. "inez" is also = "mrs. wong")
 
             int i = findCharacter(character);	//Find index of character in the array ALL_CHARACTERS
             
             if(i == -1) { return; }	//Ignore characters not part of list
             
             String[] splitWords = splitLine[1].split(",", 0);
             
             if(splitWords == null) { return; }
             
             for(int x=0; x<splitWords.length; ++x){
             	if(!splitWords[x].equals("") && !splitWords[x].contains("'")) {
             		ALL_CHARACTERS.get(i).addWord(splitWords[x], season, episode);
             	}
             }
             
         }
        
         //Returns true if character (name String c) is not a reoccurring character.
     //  This does not find all of the non-reocurring characters, but gets rid of the obvious ones.
     private boolean isBackgroundCharacter(String c) {
        
         //Parse every background character name
         for(int x=0; x<backgroundChars.size(); ++x){
             if(c.startsWith(backgroundChars.get(x))){
                     return true;
             }              
         }
        
         return false;
     }
        
     //Returns true if character (name String c) is already in ArrayList ALL_CHARATCTERS
     private boolean isInAllCharacters(String c) {
         for(int x=0; x<ALL_CHARACTERS.size(); ++x){
             if(c.equals(ALL_CHARACTERS.get(x).getName())){
                     return true;
             }
         }
         return false;
     }
    
     //Add current episode to Character objects' episode lists
     private void updateCharacterEpisodes() {
         //Retrieve all chracters added to the current episode
         ArrayList<Character> chars = ep.getChars();        
        
         //Parse characters from current episode
         for(int x=0; x<chars.size(); ++x) {
             for(int y=0; y<ALL_CHARACTERS.size(); ++y) {
                 if(chars.get(x).getName().equals(ALL_CHARACTERS.get(y).getName())) {
                         ALL_CHARACTERS.get(y).addEpisode(season, episode);
                         //System.out.println(chars.get(x) + "\tep# " + episode);
                 }
             } 
         }   
     }
    
     //Parse entire list of Characters (ALL_CHARACTERS).
     //Removes any Characters only in 1 episode.
     //  Note: Meant to be run only ONCE after all episode transcripts are parsed.
     public void filterCharacters() {
         for(int x=0; x<ALL_CHARACTERS.size(); ++x) {
                 if(ALL_CHARACTERS.get(x).getTotalEpisodes() < 2) {
                         ALL_CHARACTERS.remove(x);
                         --x;    //prevents x from incrementing since items have been shifted to the left
                 }              
         }      
     }
     
     //Create characters that have catchprases
     //	This is opposed to all other characters created dynamically (when parsing transcripts).
     //		& allows for phrases.txt to be parsed before the transcripts.
     private void createCatchphraseCharacters() {
     	for(int x=0; x<phraseChars.length; ++x) {
     		ALL_CHARACTERS.add(new Character(phraseChars[x], GLOBAL.COLORS.getNextColor())); 
     	}	
     }
    
     //Returns character name with changes, if needed.
     //Allows for one Character object per Character, 
     //	even if they go by multiple names
    public String updateName(String c) {
 	   if(c.charAt(c.length()-1) == ' ') {
 		   c = c.substring(0,c.length()-1);	//Remove extra whitespace character if needed.  // DANI : you can use global.processing.trim(String) ;)
 	   }
 	   
 	   if(c.equals("professor farnsworth") || c.equals("prof. farnsworth")) {	//ensures correct stats on farnsworth
 		   return "farnsworth";
 	   }
 	   
 	   if(c.equals("inez")){ //Inez is Mrs. Wong's first name
 		   return "mrs. wong";
 	   }
 	   
 	   if(c.equals("leo")){ //Leo is Mr. Wong's first name
 		   return "mr. wong";
 	   }
 	   
 	   if(c.equals("zapp")) {	//Changing Zapp to his full name
 		   return "zapp brannigan";
 	   }
 	   
 	   if(c.equals("farsnworth")){	//This one was likely a typo
 		   return "farnsworth";
 	   }
 	   
 	   if(c.equals("richard nixon's head")) {	//Switch these two arround if you'd rather it the other way, 
 		   return "nixon";									//but I had to pick one :)
 	   }
 	   
 	   if(c.equals("joey")) {
 		   return "joey mouspad";
 	   }
 	   
 	   if(c.equals("mr. fry")) {
 		   return "yancy";
 	   }
 	   
 	   if(c.equals("stephen hawking's head")) {
 		   return "stephen hawking";
 	   }
 	   
 	   if(c.equals("slim")) {
 		   return "barbados slim";
 	   }
 	   
 	   if(c.equals("h.g. blob") || c.equals("horrible gelatinous blob")) {
 		   return "horrible gelatinous blob";
 	   }
 	   
 	   if(c.equals("joey mouspad")) {
 		   return "joey mousepad";
 	   }
 	   
 	   if(c.equals("inger")) {
 		   return "igner";
 	   }
 	   
 	   if(c.equals("turanga morris")) {
 		   return "morris";
 	   }
 	   
 	   if(c.equals("ben beeler")) {
 		   return "beeler";
 	   }
 	   
 	   if(c.equals("turanga munda")) {
 		   return "munda";
 	   }
 	   
 	   if(c.equals("vogel")) {
 		   return "warden vogel";
 	   }
 	   
 	   if(c.equals("mayor poopenmeyer")) {
 		   return "poopenmeyer";
 	   }
 	   
 		//c = c.replaceAll("\\s", "\n");
 	   
 	   return c;
    }
    
    //Parse file phrases.txt for list of catchphrases
    public void parseCatchphrases(){
 	   createCatchphraseCharacters();	//Create characters needed for this method.
 	   
 //	   Scanner scan;    //Scanner for reading file
 //   	
 //   		try {
 //	   		scan = new Scanner(new FileReader("data/phrases.txt"));   //Initialize scanner with file.
 //	    } catch (FileNotFoundException e) {
 //               e.printStackTrace();
 //               return;
 //	    }
 //	
 //       //Scan all dialog
 //       while (scan.hasNextLine()) {    //Executes if a line of text exists
 //               storeCatchphrase(scan.nextLine());
 //       }
        
        String[] lines = GLOBAL.processing.loadStrings("phrases.txt");
        
        for(int j = 0; j < lines.length; j++) {
     	   storeCatchphrase(lines[j]);
        }
    
 //       scan.close();
    }
    
    public void storeCatchphrase(String line){
 	   //Split line into character + catchphrase (regex) + catchphrase
 	   String[] splitLine = line.split("\t", 3);
        
        if(splitLine.length<3) { return; }		//Ignore invalid lines of text
        
        //splitLine[0] = Character name.
        //splitLine[1] = Phrase in readable form.
        //splitLine[2] = Phrase in regex form.   
 
        int index = findCharacter(splitLine[0]);	//Find index of character in ALL_CHARACTERS
       if(index == -1) { return; }
        //Add current catchphrase to the character's list of catchphrases.
        ALL_CHARACTERS.get(index).addPhrase(splitLine[1], splitLine[2]);
    }
    
    //Determine if character 'c' is a character with catchphrases
    //	Then, determine if 'line' is one of their catchphrases.
    public void findCatchphrase(String c, String line) {
 	   //Do nothing if character does not have catchphrases...
 	   if(!isphraseChars(c)) { return; }
 	   
 	   line = line.toLowerCase();	//convert line to lower case for simplicity.
 	   
 	   //For testing...
 	   //int abc = 0;
 	   /*if(c.equalsIgnoreCase("kif") && line.contains("sigh")) { System.out.println(line); abc++;}*/
 	   
 	   //Find character in ALL_CHARACTERS array
 	   int index = findCharacter(c);
 	   
 	   for(int x=0; x<ALL_CHARACTERS.get(index).getTotalPhrases(); ++x) {
 		   if(line.matches(ALL_CHARACTERS.get(index).getPhrase(x).getRegex())) {
 			   ALL_CHARACTERS.get(index).getPhrase(x).addToTotals(season, episode, 1);
 			   //if(abc > 0) { System.out.println("\tFOUND"); } 	(for testing)
 		   }
 	   }
    }
    
    //Find index number of character c in ALL_CHARACTERS
    public static int findCharacter(String c){
 	   for(int x=0; x<ALL_CHARACTERS.size(); ++x) {
 		   if(c.equals(ALL_CHARACTERS.get(x).getName())) {
 			   return x;
 		   }
 	   }
 	   
 	   return -1;
    }
    
    //determine if String c is in the Array phraseChars
    public boolean isphraseChars(String c) {
 	   for(int x=0; x<phraseChars.length; ++x) {
 		   if(phraseChars[x].equals(c)) {
 			   return true;
 		   }
 	   }
 	   return false;
    }
    
    public void parseWordFiles() {
 //	   	Scanner scan;    //Scanner for reading file
 //	   	
 //  		try {
 //	   		scan = new Scanner(new FileReader("data/phrases.txt"));   //Initialize scanner with file.
 //	    } catch (FileNotFoundException e) {
 //              e.printStackTrace();
 //              return;
 //	    }
 //	
 //      //Scan all dialog
 //      while (scan.hasNextLine()) {    //Executes if a line of text exists
 //              storeCatchphrase(scan.nextLine());
 //      }
 //  
 //      scan.close();
       
       String[] lines = GLOBAL.processing.loadStrings("phrases.txt");
       
       for(int j = 0; j < lines.length; j++) {
    	   storeCatchphrase(lines[j]);
       }
       
       
    }
 }
 
