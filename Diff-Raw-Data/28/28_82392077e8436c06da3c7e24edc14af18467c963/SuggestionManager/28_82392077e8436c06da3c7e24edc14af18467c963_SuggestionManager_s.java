 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 23, 2012
  * Author(s): bene
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.course.suggestions;
 
 import java.io.EOFException;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.LinkedList;
 import java.util.Random;
 
 import org.apache.log4j.Logger;
 
 import com.thoughtworks.xstream.XStream;
 
 import edu.dhbw.sos.helper.XMLParam;
 
 
 /**
  * This class manages available and displayed suggestions. It also allows removing executed Suggestions and offers the
  * corresponding influence vectors to the simulation.
  * 
  * @author bene
  * 
  */
 public class SuggestionManager implements ISuggestionsObserver {
 	private static final Logger		logger				= Logger.getLogger(SuggestionManager.class);
 
 
 	private static final String		SUGGESTION_FILE	= System.getProperty("user.home") + "/.sos/suggestions.xml";
 
 	private LinkedList<Suggestion>	availableSuggestions;
 	private LinkedList<Suggestion>	currentSuggestions;
 	private LinkedList<String>			courseParams;
 	private int								paramCount;
 	private XStream						xs;
 	
 	
 	public SuggestionManager(LinkedList<String> params) {
 		this.courseParams = params;
 		this.paramCount = params.size();
 		availableSuggestions = new LinkedList<Suggestion>();
 		currentSuggestions = new LinkedList<Suggestion>();
 		
 		// init xml writer/reader
 		xs = new XStream();
 		xs.alias("parameters", XMLParam[].class);
 		xs.alias("param", XMLParam.class);
 		xs.alias("suggestion", Suggestion.class);
 		
 		// try loading the suggestions from file
 		int retCode = loadSuggestionsFromFile();
 		// no file was found => create file with dummy data and try loading again
 		if (retCode == 0) {
 			if (writeDummySuggestions()) {
 				if (loadSuggestionsFromFile() != 1) {
 					logger.error("Cannot create a suggestions.xml file. Please check the permission of \"" + SUGGESTION_FILE
 							+ "\".");
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Removes the Suggestion object s from the list of displayed suggestions.
 	 * 
 	 * @param s Suggestion object to be removed from displayed list.
 	 * @return true if the Suggestion could be removed or false if not.
 	 * @author bene
 	 */
 	public boolean removeSuggestion(Suggestion s) {
 		return true;
 	}
 	
 	
 	@Override
 	public void updateSuggestions() {
 		// TODO bene Auto-generated method stub
 		
 	}
 	
 	
 	/**
 	 * Loads the suggestions from the .xml file and add the ones that are needed to the availableSuggestions list.
 	 * 
 	 * @return 0 if the suggestions.xml file was not found, -1 if there was an error with the suggestions.xml file and 1
 	 *         if everything worked as it should.
 	 * @author bene
 	 */
 	private int loadSuggestionsFromFile() {
 		try {
 			ObjectInputStream in = xs.createObjectInputStream(new FileReader(System.getProperty("user.home")
 					+ "/.SoS/suggestions.xml"));
 			while (true) {
 				Suggestion s = (Suggestion) in.readObject();
 				if (haveToAddSuggestion(s)) {
					s.removeUnusedParameters((String[]) courseParams.toArray());
 					availableSuggestions.add(s);
 				}
 			}
 		} catch (FileNotFoundException err) {
 			logger.info("The suggestion file could not be found");
 			return 0;
 		} catch (ClassNotFoundException err) {
 			logger.error("There are errors in the suggestions.xml file.");
 			return -1;
 		} catch (IOException err) {
 			if (err.getClass().equals(EOFException.class)) {
 				return 1;
 			} else {
 				logger.error("There are errors in the suggestions.xml file.");
 				return -1;
 			}
 		}
 	}
 	
 	
 	// generates 4 random suggestions with the current course parameters.
 	private boolean writeDummySuggestions() {
 		Suggestion[] sugArray = new Suggestion[4];
 		Random r = new Random();
 		for (int i = 0; i < 4; i++) {
 			float[][] range = new float[courseParams.size()][2];
 			float[] influence = new float[courseParams.size()];
 			for (int j = 0; j < courseParams.size(); j++) {
 				range[j][0] = r.nextInt(40);
 				range[j][1] = r.nextInt(60);
 				influence[j] = r.nextInt(50);
 			}
 			sugArray[i] = new Suggestion(range, "Vorschlag " + i, r.nextInt(5), influence, courseParams);
 		}
 		try {
 			ObjectOutputStream out = xs.createObjectOutputStream(new FileWriter(SUGGESTION_FILE), "suggestionList");
 			
 			for (int i = 0; i < 4; i++) {
 				out.writeObject(sugArray[i]);
 			}
 			
 			out.close();
 			return true;
 		} catch (IOException err) {
 			logger.error("IO Error on writing dummy suggestions.xml file.");
 			return false;
 		}
 	}
 	
 	
 	private boolean haveToAddSuggestion(Suggestion s) {
 		String[] suggestionParamNames = s.getParamNames();
 		// suggestion contains less parameters than the course and can therefore not be used
 		if (suggestionParamNames.length < courseParams.size()) {
 			return false;
 		}
 		boolean result = true;
 		for (int i = 0; i < courseParams.size(); i++) {
 			String currentParamName = courseParams.get(i);
 			boolean isInSuggestion = false;
 			for (String suggestionParamName : suggestionParamNames) {
 				if (currentParamName.compareTo(suggestionParamName) == 0) {
 					isInSuggestion = true;
 					break;
 				}
 			}
 			result = result && isInSuggestion;
 		}
 		return result;
 	}
 }
