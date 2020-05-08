 package org.promasi.game.singleplayer;
 
 import java.beans.XMLDecoder;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.regex.Pattern;
 
 import org.promasi.game.GameException;
 import org.promasi.game.GameModel;
 import org.promasi.game.company.Company;
 import org.promasi.game.company.MarketPlace;
 import org.promasi.game.company.CompanyMemento;
 import org.promasi.game.company.MarketPlaceMemento;
 import org.promasi.game.project.Project;
 import org.promasi.game.project.ProjectMemento;
 import org.promasi.utilities.file.RootDirectory;
 import org.promasi.utilities.logger.ILogger;
 import org.promasi.utilities.logger.LoggerFactory;
 import org.promasi.utilities.serialization.SerializationException;
 
 /**
  * 
  * @author m1cRo
  *
  */
 public class SinglePlayerGameFolder
 {
 	/**
 	 * Logger
 	 */
 	private static final ILogger CONST_LOGGER = LoggerFactory.getInstance(SinglePlayerGameFolder.class);
 	
 	/**
 	 * 
 	 */
 	public static final String CONST_SINGLEPLAYER_GAMES_FOLDER="SinglePlayer";
 	
 	/**
 	 * 
 	 */
 	public static final String CONST_PROJECTS_DIR_NAME="Projects";
 	
 	/**
 	 * 
 	 */
 	public static final String CONST_COMPANY_FILE_NAME="Company";
 	
 	/**
 	 * 
 	 */
 	public static final String CONST_MARKETPLACE_FILE_NAME="MarketPlace";
 	
 	/**
 	 * 
 	 */
 	public static final String CONST_GAME_DESCRIPTION_FILE_NAME="GameInfo";
 	
 	/**
 	 * 
 	 */
 	private GameModel _game;
 	
 	/**
 	 * 
 	 * @param gameFolderPath
 	 * @param playerName
 	 * @throws GameException
 	 */
 	public SinglePlayerGameFolder(final String gameFolderPath, String playerName)throws GameException{
 		CONST_LOGGER.info("Start reading game from : " + gameFolderPath);
 		if(gameFolderPath==null){
 			throw new GameException("Wrong argument gameFolderPath==null");
 		}
 		
 		String separator;
 		try {
 			separator = RootDirectory.getInstance().getSeparator();
 		} catch (IOException e) {
 			throw new GameException(e.toString());
 		}
 		
 		File gameFile=new File(gameFolderPath);
 		if(!gameFile.isDirectory()){
 			throw new IllegalArgumentException("Wrong argument gameFolder is not an Directory");
 		}
 		
 		String tokens[]=gameFolderPath.split(Pattern.quote(separator));
 		if(tokens.length==0){
 			throw new IllegalArgumentException("Wrong gameFolderPath");
 		}
 		
 		String gameName=tokens[tokens.length-1];
 		
 		Company company=null;
 		MarketPlace marketPlace=null;
 		Queue<Project> projects=null;
 		String gameInfo=null;
 		
 		String projectFiles[]= gameFile.list();
 		
 		try{
 			for(int i=0;i<projectFiles.length;i++){
 				if( projectFiles[i].toLowerCase().equals(CONST_COMPANY_FILE_NAME.toLowerCase()) ){
 					company=makeCompany(gameFolderPath+separator+projectFiles[i]);
 				}else if( projectFiles[i].toLowerCase().equals(CONST_MARKETPLACE_FILE_NAME.toLowerCase()) ){
 					marketPlace=makeMarketPlace(gameFolderPath+separator+projectFiles[i]);
 				}else if( projectFiles[i].toLowerCase().equals(CONST_GAME_DESCRIPTION_FILE_NAME.toLowerCase()) ){
 					gameInfo=makeGameDescription(gameFolderPath+separator+projectFiles[i]);
 				}else if( projectFiles[i].toLowerCase().equals(CONST_PROJECTS_DIR_NAME.toLowerCase()) ){
 					projects=makeProjects(gameFolderPath+separator+projectFiles[i]);
 				}
 			}
 		}catch(SerializationException e){
 			throw new GameException("Wrong gameFolderPath");
 		}catch(IllegalArgumentException e){
 			throw new GameException("Wrong gameFolderPath");
 		}catch(FileNotFoundException e){
 			throw new GameException("Wrong gameFolderPath");
 		} catch (IOException e) {
 			throw new GameException("Wrong gameFolderPath");
 		}
 
 		
 		if(company==null){
 			CONST_LOGGER.warn("unable to read company");
 			throw new GameException("Wrong gameFolderPath");
 		}
 		
 		if(marketPlace==null){
 			CONST_LOGGER.warn("unable to read marketplace");
 			throw new GameException("Wrong gameFolderPath");
 		}
 		
 		if(projects==null || projects.size()==0){
 			CONST_LOGGER.warn("unable to read projects");
 			throw new GameException("Wrong gameFolderPath");
 		}
 		
 		if(gameInfo==null){
 			CONST_LOGGER.warn("unable to read gameinfo");
 			throw new GameException("Wrong gameFolderPath");
 		}
 		
 		try {
 			_game=new GameModel(gameName, gameInfo, marketPlace, company, projects);
 		} catch (GameException e) {
 			CONST_LOGGER.warn("unable to make game");
 			throw new GameException("Wrong gameFolderPath");
 		}
 		
 		company.setOwner(playerName);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public GameModel readGame(){
 		return _game;
 	}
 	
 	/**
 	 * 
 	 * @param filePath
 	 * @return
 	 * @throws FileNotFoundException 
 	 * @throws SerializationException 
 	 */
 	private Company makeCompany(final String filePath) throws FileNotFoundException, SerializationException{
 		File companyFile=new File(filePath);
 		FileInputStream fileInputStream=new FileInputStream(companyFile);
 		XMLDecoder xmlDecoder=new XMLDecoder(fileInputStream);
 		Object object=xmlDecoder.readObject();
 		if(object instanceof CompanyMemento){
 			CompanyMemento sCompany=(CompanyMemento)object;
 			Company company=sCompany.getCompany();
 			return company;
 		}
 		
 		throw new FileNotFoundException("Company file not found");
 	}
 	
 	/**
 	 * 
 	 * @param filePath
 	 * @return
 	 * @throws FileNotFoundException 
 	 * @throws SerializationException 
 	 */
 	private MarketPlace makeMarketPlace(final String filePath) throws FileNotFoundException, SerializationException{
 		File marketPlaceFile=new File(filePath);
 		FileInputStream fileInputStream=new FileInputStream(marketPlaceFile);
 		XMLDecoder xmlDecoder=new XMLDecoder(fileInputStream);
 		Object object=xmlDecoder.readObject();
 		if(object instanceof MarketPlaceMemento){
 			MarketPlaceMemento sMarketPlace=(MarketPlaceMemento)object;
 			MarketPlace marketPlace=sMarketPlace.getMemento();
 			return marketPlace;
 		}
 		
 		throw new FileNotFoundException("Company file not found");
 	}
 	
 	/**
 	 * 
 	 * @param filePath
 	 * @return
 	 * @throws IOException 
 	 */
 	private String makeGameDescription(final String filePath) throws IOException{
 		File file=new File(filePath);
 		if(!file.exists() || file.isDirectory()){
 			throw new FileNotFoundException("File not found");
 		}
 		
 		String gameInfo="";
 		FileReader fReader=new FileReader(file);
 		BufferedReader bufferedReader=new BufferedReader(fReader);
 		String line=null;
 
 	    do{
 	    	line=bufferedReader.readLine();
 	    	if(line!=null){
 	    		gameInfo=gameInfo+line;
 	    	}
 	    }while(line!=null);
 	 
 		return gameInfo;
 	}
 	
 	/**
 	 * 
 	 * @param filePath
 	 * @return
 	 * @throws IOException 
 	 */
 	private Queue<Project> makeProjects(final String filePath) throws IOException{
 		Queue<Project> projects=new LinkedList<Project>();
 		
 		File file=new File(filePath);
 		if(!file.isDirectory()){
 			throw new FileNotFoundException("File not found");
 		}
 		
 		String files[]=file.list();
 		if(files.length==0){
 			throw new FileNotFoundException("File not found");
 		}
 		
 		for(int i=0;i<files.length;i++){
 			try{
 				File projectFile=new File(filePath+RootDirectory.getInstance().getSeparator()+files[i]);
 				FileInputStream fileInputStream=new FileInputStream(projectFile);
 				XMLDecoder xmlDecoder=new XMLDecoder(fileInputStream);
 				Object object=xmlDecoder.readObject();
 				if(object instanceof ProjectMemento){
 					ProjectMemento sProject=(ProjectMemento)object;
 					Project project;
 					try {
 						project = sProject.getProject();
 						projects.add(project);
 					} catch (SerializationException e) {
 						e.printStackTrace();
 					}
 				}
 			}catch(FileNotFoundException e){
 				//Log wrong file
 			}catch( Exception e ){
 				//Log wrong file
 			}
 		}
 		
 		return projects;
 	}
 }
