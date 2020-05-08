 package parsing;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.HashMap;
 import utils.Configuration;
 
 
 /**
  * TODO - add description 
  * 
  **/
 public class IMDBParser {
 	/*Class constants*/
 	private final String GENRE_FILE = "genres.list";
 	private final String GENRE_LIST = "8: THE GENRES LIST";
 	private final String PLOT_FILE = "plot.list";
 	private final String PLOT_LIST = "PLOT SUMMARIES LIST";
 	private final String LANGUAGE_FILE = "language.list";
 	private final String LANGUAGE_LIST = "LANGUAGE LIST";
 	/*Current user directory*/
 	private String currentDir;
 	/*Setting from configuration file*/
 	private Configuration settings; 
 	/*Internal helper movie table */
 	private HashMap<String, Movie> IMDBMoviesTable;
 
 
 	/**
 	 * Default Constructor.
 	 **/
 	public IMDBParser(HashMap<String,Movie> moviesTable){
 		currentDir = System.getProperty("user.dir");
 		settings = new Configuration();
 		IMDBMoviesTable = new HashMap<String,Movie>();
 		//Building the IMDBMoviesTable
 		for (Movie m : moviesTable.values()) {
 			//if the year for this movie is known
 			if(m.getDateCreated() != null){
 				IMDBMoviesTable.put(m.getName()+" ("+m.getDateCreated()+")", m);
 				IMDBMoviesTable.put(m.getName()+" ("+m.getDateCreated()+"/I)", m);
 				IMDBMoviesTable.put(m.getName()+" ("+m.getDateCreated()+"/II)", m);
 				IMDBMoviesTable.put(m.getName()+" ("+m.getDateCreated()+"/III)", m);
 			}
 		}
 		parseGenre();
 		parsePlot();
 		parseLanguage();
 	}
 
 
 	/** Parses the IMDB genres file and updates the movie table accordingly */
 	private void parseGenre(){		
 		String filePath;
 
 		// set the file path- if exist in the configuration= take it from it   
		if (settings.getImdbGenres().isEmpty() || settings.getImdbGenres() == null)
 			filePath = currentDir + "\\" + GENRE_FILE;
 		else
 			// take it from the current directory
 			filePath = settings.getImdbGenres();
 
 		try{
 			BufferedReader br = new BufferedReader(new FileReader(filePath));	
 			boolean listStartReached = false;
 			String[] strArr;
 			Movie m;
 			String line , movieName, genre;
 			genre = "";
 			movieName = "";
 			//read each line
 			while((line = br.readLine()) != null){
 				//if we got the the place in the file where the genres list starts
 				if (!listStartReached && line.contains(GENRE_LIST) && br.readLine().contains("=")){
 					listStartReached = true;
 					continue;
 				}
 				//if this line does not contain a valid movie title
 				if(!line.contains(" (")){
 					continue;
 				}
 				//split by tab and get the movie name and genre
 				strArr = line.split("\\t");
 				movieName = strArr[0];
 				genre = strArr[strArr.length-1];
 				//adding genre to movie table
 				if((m = IMDBMoviesTable.get(movieName)) != null){
 					m.addGenre(genre);
 				}
 			}
 			br.close();
 		}
 		catch(Exception ex){
 			System.out.println(ex.getMessage());
 		}
 	}
 
 
 	/** Parses the IMDB plot file and updates the movie table accordingly */
 	private void parsePlot(){
 		//set the file path
 		String filePath = currentDir+"\\"+PLOT_FILE;
 		try{
 			BufferedReader br = new BufferedReader(new FileReader(filePath));	
 			boolean listStartReached = false;
 			Movie m;
 			String line , movieName, plot, comments;
 			movieName = "";
 			//read each line
 			while((line = br.readLine()) != null){
 				//if we got the the place in the file where the plot list starts
 				if (!listStartReached && line.contains(PLOT_LIST) && br.readLine().contains("=")){
 					listStartReached = true;
 					continue;
 				}
 				//if valid movie plot starting line
 				if(line.contains("MV:")){
 					plot = "";
 					//parsing movie name from line
 					movieName = line.replace("MV: ", "");
 					movieName = movieName.replaceAll("\"", "");
 					movieName = movieName.replaceAll("$#*! ", "");
 					comments = movieName.substring(movieName.indexOf(")")+1);
 					movieName = movieName.replace(comments, "");
 
 					line = br.readLine();
 					//concatenating the plot lines
 					while((line = br.readLine()).contains("PL:")){
 						plot+= line;
 					}
 					//adding plot to movie table
 					if((m = IMDBMoviesTable.get(movieName)) != null){
 						plot = plot.replaceAll("PL:", "");
 						m.setPlot(plot);
 					}
 				}
 			}
 			br.close();
 		}
 		catch(Exception ex){
 			System.out.println(ex.getMessage());
 		}
 	}
 
 
 	/** Parses the IMDB language file and updates the movie table accordingly */
 	private void parseLanguage(){
 		//set the file path
 		String filePath = currentDir+"\\"+LANGUAGE_FILE;
 		try{
 			BufferedReader br = new BufferedReader(new FileReader(filePath));
 			boolean listStartReached = false;
 			String[] strArr;
 			Movie m;
 			String line , movieName, language, comments;
 			movieName = "";
 			//read each line
 			while((line = br.readLine()) != null){
 				//if we got the the place in the file where the language list starts
 				if (!listStartReached && line.contains(LANGUAGE_LIST) && br.readLine().contains("=")){
 					listStartReached = true;
 					continue;
 				}
 				//if this line does not contain a valid movie title
 				if(!line.contains(" (")){
 					continue;
 				}
 				
 				//split by tab and get the movie name and genre
 				strArr = line.split("\\t");
 				movieName = strArr[0];
 				language = strArr[strArr.length-1];
 				
 				//parsing movie name from line
 				movieName = movieName.replaceAll("\"", "");
 				movieName = movieName.replaceAll("$#*! ", "");
 				comments = movieName.substring(movieName.indexOf(")")+1);
 				movieName = movieName.replace(comments, "");
 				//adding language to movie table
 				if((m = IMDBMoviesTable.get(movieName)) != null){
 					m.setLanguage(language);
 				}
 			}
 			br.close();
 		}
 		catch(Exception ex){
 			System.out.println(ex.getMessage());
 		}
 	}
 }
