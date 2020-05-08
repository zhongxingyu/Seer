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
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.EOFException;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.Random;
 
 import javax.swing.JLabel;
 
 import org.apache.log4j.Logger;
 
 import com.thoughtworks.xstream.XStream;
 
 import edu.dhbw.sos.course.Course;
 import edu.dhbw.sos.course.Courses;
import edu.dhbw.sos.course.ICurrentCourseObserver;
 import edu.dhbw.sos.helper.CalcVector;
 import edu.dhbw.sos.helper.XMLParam;
 
 
 /**
  * This class manages available and displayed suggestions. It also allows removing executed Suggestions and offers the
  * corresponding influence vectors to the simulation.
  * 
  * @author bene
  * 
  */
public class SuggestionManager implements ISuggestionsObserver, MouseListener, ICurrentCourseObserver {
 	private static final Logger		logger				= Logger.getLogger(SuggestionManager.class);
 
 
 	private static final String		SUGGESTION_FILE	= System.getProperty("user.home") + "/.sos/suggestions.xml";
 
 	private LinkedList<Suggestion>	availableSuggestions;
 	private LinkedList<Suggestion>	currentSuggestions;
 	private LinkedList<String>			courseParams;
 	private XStream						xs;
 	
 	
 	public SuggestionManager(Courses courses) {
 		this.courseParams = courses.getCurrentCourse().getProperties();
		courses.subscribeCurrentCourse(this);
 		availableSuggestions = new LinkedList<Suggestion>();
 		currentSuggestions = new LinkedList<Suggestion>();
 		
 
 		// init xml writer/reader
 		xs = new XStream();
 		// aliases are not required for functionality but for improving readability of the generated xml file.
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
 							+ "\" and the surrounding folder.");
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
 	private boolean removeSuggestion(Suggestion s) {
 		if (currentSuggestions.contains(s)) {
 			currentSuggestions.remove(s);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	
 	@Override
 	public void updateSuggestions() {
 		// TODO check which suggestions should be displayed based on average course parameters
 		// TODO update gui
 		
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
 			ObjectInputStream in = xs.createObjectInputStream(new FileReader(SUGGESTION_FILE));
 			while (true) {
 				Suggestion s = (Suggestion) in.readObject();
 				if (haveToAddSuggestion(s)) {
 					s.removeUnusedParameters(courseParams);
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
 		// OR course does not have any parameters (valid suggestions cannot be determined if there are no course
 		// parameters).
 		if (suggestionParamNames.length < courseParams.size() || courseParams.size() == 0) {
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
 	
 	
 	private Suggestion lookUpSuggestion(String text) {
 		for (Suggestion s : currentSuggestions) {
 			if (s.getMessage().compareTo(text) == 0) {
 				return s;
 			}
 		}
 		return null;
 	}
 	
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		String sugText = ((JLabel) e.getSource()).getText();
 		Suggestion clicked = this.lookUpSuggestion(sugText);
 		if (clicked != null) {
 			// TODO pass suggestions influence to simulation
 			this.removeSuggestion(clicked);
 			// DOES NOT UPDATE DIRECTLY, UPDATE OF GUI IS DONE WITH NEXT SIMULATIONSTEP!
 		}
 	}
 	
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// empty
 		
 	}
 	
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// empty
 		
 	}
 	
 	
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// empty
 	}
 	
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// empty
 	}
 	
 	
	@Override
	public void updateCurrentCourse(Course course) {
 		CalcVector averages = course.getStatState();
 		currentSuggestions.clear();
 		for (int i = 0; i < availableSuggestions.size(); i++) {
 			boolean addSuggestion = true;
 			for (int j = 0; j < courseParams.size(); j++) {
 				addSuggestion = addSuggestion && availableSuggestions.get(i).paramIsInRange(j, averages.getValueAt(j));
 			}
 			if (addSuggestion) {
 				currentSuggestions.add(availableSuggestions.get(i));
 			}
 		}
 		Collections.sort(currentSuggestions);
 	}
 }
