 package parsing;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.HashMap;
 
 import utils.Configuration;
 
 
 
 public class YagoParser extends Parser{
 
 	final static String YAGO_TYPES_FILE = "yagoSimpleTypes.ttl";
 	final static String YAGO_FACTS_FILE = "yagoFacts.ttl";
 	final static String YAGO_LITERAL_FACTS_FILE = "yagoLiteralFacts.ttl";
 	final static String YAGO_LABELS_FILE = "yagoLabels.ttl";
 	final static String YAGO_WIKIPEDIA_FILE = "yagoWikipediaInfo.ttl";
 	final static String ACTED_IN = "actedIn";
 	final static String DIRECTED = "directed";
 	final static String CREATED_ON = "wasCreatedOnDate";
 	final static String DURATION = "hasDuration";
 	final static String LABEL = "rdfs:label";
 	final static String FAMILY_NAME = "hasFamilyName";
 	final static String FIRST_NAME = "hasGivenName";
 	final static String WIKI = "hasWikipediaUrl";
 	final static String PREFERRED_MEAN = "skos:prefLabel";
 	final static String MOVIE_ID = "106613686";
 	final static String ACTOR_ID = "109765278";
 	final static String DIRECTOR_ID = "110088200";
 
 	/*=============*/
 	/* constructor */
 	/*=============*/
 	
 	public YagoParser(){
 		setMoviesTable(new HashMap<String,Movie>());
 		setActorsTable(new HashMap<String,Person>());
 		setDirectorsTable(new HashMap<String,Person>());
 	}
 	
 
 	/*=================*/
 	/* public function */
 	/*=================*/
 		
 	@Override
 	public void parse(){
 		// get the configuration file
 		utils.Configuration settings = new Configuration();
 		
		//parse yago files 				
 		parseYagoTypes(settings.getYagoSimpleTypes());
 		parseYagoFacts(settings.getYagoFacts());
 		parseYagoLiteralFacts(settings.getYagoLiteralFacts());
 		parseYagoWikiInfo(settings.getYagoWikipediaInfo());
 	}
 	
 	
 	/*===================*/
 	/* private functions */
 	/*===================*/
 	
 	/** expect to get the file yagoSimpleTpyes.ttl and populate movieTable, actorTable, directorTable */
 	private void parseYagoTypes(String path){
 		//check that the path is correct (the correct yago file)
 		if (isFileCorrect(path, YAGO_TYPES_FILE)){			
 			try{
 				// create a buffered reader for the current file
 				BufferedReader br = new BufferedReader(new FileReader(path));					
 				String[] strArr;
 				//parse the next line by tabs to String[3]
 				while ((strArr = parseLine2Array(br)) != null){ 
 					
 					//in case of Movie Entity - add to moviesTable
 					if(strArr.length >= 3 && strArr[2].contains(MOVIE_ID)){
 						String movieName = pullEntityName(strArr[0]);
 						Movie m = new Movie(strArr[0]);
 						m.setName(movieName);
 						getMoviesTable().put(strArr[0], m);					
 						continue;
 					}
 					
 					//in case of Actor Entity - add to actorsTable
 					else if(strArr.length >= 3 && strArr[2].contains(ACTOR_ID)){
 						String actorName = pullEntityName(strArr[0]);
 						Person p = new Person(strArr[0]);
 						p.setName(actorName);
 						getActorsTable().put(strArr[0],p);				
 						continue;
 					}
 					
 					//in case of Director Entity - add to directorsTable	
 					else if(strArr.length >= 3 && strArr[2].contains(DIRECTOR_ID)){
 						String directorName = pullEntityName(strArr[0]);
 						Person p = new Person(strArr[0]);
 						p.setName(directorName);
 						getDirectorsTable().put(strArr[0], p);
 						continue;
 					}					
 				}
 				br.close();
 				return;
 			} 
 			catch(Exception ex){
 				//to-do
 			}			
 		}
 		else
 			System.out.println("Wrong Path. Please Provide a correct path");
 	}
 
 	/** expect to get the file yagoFacts.ttl and update the fats: "actedIn" and "directed" */
 	private void parseYagoFacts(String path){
 		//make sure the path is not null or empty and that it's the correct file
 		if (isFileCorrect(path,YAGO_FACTS_FILE)){			
 			try{
 				// create a buffered reader for the current file
 				BufferedReader br = new BufferedReader(new FileReader(path));
 				String[] strArr;	
 				//read line and parse it by tabs	
 				while((strArr = parseLine2Array(br)) != null){ 			
 					//if it contains "actedIn" or "directed" add that fact
 					if(strArr.length >= 3 && (strArr[1].contains(ACTED_IN) || strArr[1].contains(DIRECTED))){
 						addFact(strArr);
 					}
 				}
 				br.close();
 				return;
 			} 
 			catch(Exception ex){
 				System.out.println(ex.toString());
 			}	
 		}
 	}
 
 	/** expect the filename yagoLiteralFacts and it updates: "createdOnDate" and "duration" */
 	private void parseYagoLiteralFacts(String path){
 		if (isFileCorrect(path,YAGO_LITERAL_FACTS_FILE)){			
 			try{
 				// create a buffered reader for the current file
 				BufferedReader br = new BufferedReader(new FileReader(path));
 				String[] strArr;
 				while((strArr = parseLine2Array(br)) != null){
 					//add to the movie the literal if it is createdOnDate or Duration
 					if(strArr.length >= 3 && (strArr[1].contains(CREATED_ON) || strArr[1].contains(DURATION))){
 						addLiteral(strArr);
 					}
 				}
 				br.close();
 				return;
 			}
 			catch(Exception ex){
 				System.out.println(ex.toString());
 			}
 		}
 	}
 
 	/** expect the filename yagoWikiPediaInfo and update the wiki url for each movie */
 	private void parseYagoWikiInfo(String path){
 		if(isFileCorrect(path, YAGO_WIKIPEDIA_FILE)){
 			try{
 				// create a buffered reader for the current file
 				BufferedReader br = new BufferedReader(new FileReader(path));
 				String[] strArr;
 				while((strArr = parseLine2Array(br)) != null){
 					//add to the movie the literal if it is createdOnDate or Duration
 					if(strArr.length >= 3 && strArr[1].contains(WIKI)){
 						addWikiInfo(strArr);
 					}
 				}
 				br.close();
 				return;
 			}
 			catch(Exception ex){
 				System.out.println(ex.toString());
 			}	
 		}	
 	}
 
 	
 	/*==================*/
 	/* helper functions */
 	/*==================*/
 	
 	
 	/** check that the filePath is correct and it's the right fileName from yago*/
 	private boolean isFileCorrect(String path, String fileName2compare){
 		//make sure the path is not null or empty and that it's the correct file
 		if (path != null && !path.isEmpty() && new File(path).getName().compareTo(fileName2compare)== 0)
 			return true;
 		else{
 			System.out.println("Wrong path! you gave path = " +path + ". we expected = " + fileName2compare);
 			return false;
 		}
 	}
 
 	/**get a BufferedReader, read the next line and split line by tabs*/
 	private String[] parseLine2Array(BufferedReader br){		
 		String line;
 		String[] strArr = new String[3]; //array with 3 cells to parse each line
 		try{
 			if((line = br.readLine()) != null){ //read line	
 				strArr = line.split("\\t"); //split the line by tabs - we know there are 2 tabs in each line				
 				return strArr;
 			}
 			else
 				return null;
 		}
 		catch (Exception ex){
 			System.out.println("Could not read line");
 		}
 		return null;
 	}
 
 	/** add the fact: <actor> <actedIn> <movie> OR <director> <directed> <movie>*/
 	private void addFact(String[] strArr){		
 		//get the movie name from strArr[2]
 		String currentMovie = strArr[2].substring(0, strArr[2].length()-2);
 		//get the person name from strArr[0]
 		String personName = strArr[0];
 		//get the movie object and check it's not null
 		Movie movie = getMoviesTable().get(currentMovie);
 		if(movie == null){		
 			return;
 		}
 		//in case of an actor
 		if (strArr[1].contains(ACTED_IN)){
 			Person actor = getActorsTable().get(personName);
 			//add the Actor to the Movie actorsList
 			movie.addActor(actor);	
 		}
 		//in case of a director
 		else{
 			Person director = getDirectorsTable().get(personName);
 			movie.setDirector(director);	
 		}
 	}
 
 	/** add the literal: <movie> <wasCreatedOnDate> <date> OR <movie> <hasDuration> <duration> */
 	private void addLiteral(String[] strArr) {
 		//get the movie name from strArr[0]
 		String currentMovie = strArr[0];
 		//get the literal from strArr[2]
 		String literal = strArr[2].substring(0, strArr[2].length()-2);
 		//get the movie object and check it's not null
 		Movie movie = getMoviesTable().get(currentMovie);
 		if(movie == null){
 			return;
 		}
 		//in case of an createdOnDate
 		if (strArr[1].contains(CREATED_ON)){
 			//add the creation date of the Movie 
 			String year = pullYear(literal);
 			movie.setDateCreated(year);	
 		}
 		//in case of a Duration
 		else{		
 			String min = pullDuration(literal);
 			movie.setDuration(min);	
 		}		
 	}
 
 	/** add the wikiURL to the movie */
 	private void addWikiInfo(String[] strArr) {
 		//get the movie name from strArr[0]
 		String currentMovie = strArr[0];
 		//get the literal from strArr[2]
 		String wikiURL = strArr[2].substring(0, strArr[2].length()-2);
 		if(getMoviesTable().containsKey(currentMovie)){
 			Movie movie = getMoviesTable().get(currentMovie);
 			movie.setWikiURL(wikiURL);
 		}		
 	}
 
 	/** get rid of the '<' and '>' in the entity name */
 	private String pullEntityName(String entity){
 		if(entity == null || entity.length() < 2)
 			return null;
 		// remove all "(<something>)"
 		if(entity.contains("_(")){
 			String comments = entity.substring(entity.indexOf("_("),entity.lastIndexOf(")")+1);
 			entity = entity.replace(comments, "");
 		}
 		// remove "_"
 		entity = entity.replace("_", " ");
 		// remove "<" and ">"
 		String name = entity.substring(1, entity.length()-1);
 		return name;
 	}
 	
 	/** pull the year from the date format of yago */
 	private String pullYear(String date){
 		if(date == null || date.length() < 5)
 			return null;
 		String year = date.substring(1,5);
 		return year;
 	}
 	
 	/** pull the time in minutes from the duration format */
	private String pullDuration(String duration){	
		try {
			int i = duration.indexOf(".");
			String min = duration.substring(1, i);
			int sec = Integer.parseInt(min);
			sec = sec/60;
			min = String.valueOf(sec);
			return min;
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
 	}
 }
