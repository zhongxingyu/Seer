 /**
  * 
  */
 package model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 
 import model.Exercise.ExerciseCategory;
 import utils.DateGC;
 
 /**
  * 
  * @author Lieven
  * @author Emin
  * @version 10/10/2013
  *
  */
 public class ExerciseCatalog implements Comparable<ExerciseCatalog>, Cloneable{
 	
 	private List<Exercise> exercises;
 	
 	/**
 	 * Default constructor
 	 * 
 	 * @throws IllegalArgumentException
 	 * @throws CloneNotSupportedException 
 	 */
 	public ExerciseCatalog() throws IllegalArgumentException{
 		this.setExercises(new ArrayList<Exercise>());
 	}
 	
 	/**
 	 * Constructor with 1 param
 	 * 
 	 * @param opdrachten
 	 * @throws IllegalArgumentException
 	 * @throws CloneNotSupportedException 
 	 */
 	public ExerciseCatalog(List<Exercise> exercises) throws IllegalArgumentException{
 		this.setExercises(exercises);
 	}
 
 	// Selectors
 	
 	/**
 	 * @return
 	 */
 	public List<Exercise> getExercises() {
 		return exercises;
 	}
 
 	// Modifiers
 	
 	/**
 	 * Set exercises
 	 * 
 	 * @param exercises
 	 * @throws IllegalArgumentException
 	 * @throws CloneNotSupportedException 
 	 */
 	public void setExercises(List<Exercise> exercises) throws IllegalArgumentException{
 		if (exercises == null)throw new IllegalArgumentException("Opdrachten verzameling is null");
 		this.exercises = exercises;
 	}
 	
 	/**
 	 * Add exercise to list
 	 * 
 	 * @param exercise
 	 * @throws IllegalArgumentException
 	 */
 	public void addExercise(Exercise exercise) throws IllegalArgumentException{
 		if (exercise == null)throw new IllegalArgumentException("Opdracht is null!");
 		for (Exercise eCheck : exercises) {
 			if (eCheck.equals(exercise))throw new IllegalArgumentException("Opdracht bestaat al!");
 		}
 		// Set exerciseId
 		if (exercises.size() >0)
 		{
 			Exercise tempEx = exercises.get(exercises.size()-1);
 			exercise.setExerciseId(tempEx.getExerciseId()+2);
 		}
 		else{
 			exercise.setExerciseId(1);
 		}
 		
 		exercises.add(exercise);
 	}
 	
 	/**
 	 * Remove exercise from list
 	 * 
 	 * @param exercise
 	 * @throws IllegalArgumentException
 	 */
 	public void removeExercise(Exercise exercise) throws IllegalArgumentException{
 		if (exercise == null)throw new IllegalArgumentException("Opdracht is null!");
 		exercises.remove(exercise);
 	}
 	
 	/**
 	 * Update exercise from list
 	 * 
 	 * @param oldOpdracht
 	 * @param newOpdracht
 	 * @throws IllegalArgumentException
 	 */
 	public void updateExercise(Exercise oldExercise, Exercise newExercise) throws IllegalArgumentException{
 		if (oldExercise == null)throw new IllegalArgumentException("oldOpdracht is null!");
 		if (newExercise == null)throw new IllegalArgumentException("newOpdracht is null!");
 		int index = exercises.lastIndexOf(oldExercise);
 		exercises.set(index, newExercise);
 	}
 	
 	/**
 	 * Create a new file named exercises.txt and adds all exercises from exercises list
 	 */
 	public void writeExercisesToFile(){
 		// Create new file
 		File file = new File("src" + File.separator + "files" + File.separator + "exercises.txt");
 		
 		try {
 			if (exercises == null)throw new IllegalArgumentException("Exercises lijst is leeg, er is niets om op te slaan!");
 			
 			// Create new writer
 			PrintWriter writer = new PrintWriter(file);
 			
 			// Loop through exercises
 			for (int i = 0;i < exercises.size();i++){
 				Exercise exercise = exercises.get(i);
 				
 				// Line that will be saved in the file per(per exercises)
 				String line = 
 						exercise.getDiscriminator() + " > " + exercise.getQuestion() +
 						" > " + exercise.getCorrectAnswer() + " > " + Arrays.toString(exercise.getAnswerHints()) + " > " + exercise.getAuthor() +
 						" > " + exercise.getCategory() + " > " + exercise.getMaxAnswerTime() + " > " + exercise.getMaxNumberOfAttempts() +
 						" > " + exercise.getDateRegistration() + " > ";
 				
 				if (exercise.getQuizExercises() != null){
 					for (int j = 0; j < exercise.getQuizExercises().size(); j++) {
 						line += exercise.getQuizExercises().get(j).getMaxScore() +
 								" , " + exercise.getQuizExercises().get(j).getQuiz().getQuizId() +
 								" , " + exercise.getQuizExercises().get(j).getExercise().getExerciseId() + " > ";
 					}
 				}
 				
 				// Check type of exercises with discriminator
 				switch (exercise.getDiscriminator()){
 					case 'S':
 						writer.println(line);
 						break;
 					case 'E':
 						EnumerationExercise eExercise = (EnumerationExercise)exercise;
 						line += " > " + eExercise.getInCorrectOrder();
 						writer.println(line);
 						break;
 					case 'M':
 						MultipleChoiceExercise mExercise = (MultipleChoiceExercise)exercise;
 						line += " > " + mExercise.getMultipleChoice();
 						writer.println(line);
 						break;
 					}
 				
 				// Reset line
 				line ="";
 			}
 			
 			// Clone writer
 			if (writer !=null)
 				writer.close();
 		
 		} catch (FileNotFoundException e){
 			System.out.println(e.getMessage());
 		} catch (Exception e){
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Read exercises from file and put them in exercises list
 	 */
 	public void readExercisesFromFile(){
 		// Get exercises.txt file
 		File file = new File("src" + File.separator + "files" + File.separator + "exercises.txt");
 	 
 		try{
 			// Scan through file
 			Scanner scanner = new Scanner(file);
 			
 			List<String> tempExercises = new ArrayList<String>();
 			
 			// Add each line as String object to tempExercises list
 			while (scanner.hasNextLine()){
 			  tempExercises.add(scanner.nextLine());
 			}
 
 			if (scanner!=null){
 			  scanner.close();
 			}
 			
 			// Counter to assign exerciseId
 			int count = 1;
 			
 			// Loop through each String object in tempExercises
 			for (int i = 0; i < tempExercises.size(); i++) {
 				Scanner scanner2 = new Scanner(tempExercises.get(i));
 				scanner2.useDelimiter("\\s*>\\s*");
 				
 				String descriminator = scanner2.next();
 				
 				// Create corresponding exercise based on discriminator
 				Exercise ex = (descriminator.equals("S")) ? new SimpleExercise() :
 					(descriminator.equals("M")) ? new MultipleChoiceExercise() : new EnumerationExercise();
 				
 				// Add parameters
 				ex.setExerciseId(count);
 				ex.setDiscriminator(descriminator.charAt(0));
 				ex.setQuestion(scanner2.next());
 				ex.setCorrectAnswer(scanner2.next());
 				
 				// Add anwserHints from scanner2.next to tempS
 				String tempS = scanner2.next();
 				// Remove square brackets
 				tempS = tempS.replaceAll("\\[", "").replaceAll("\\]","");
 				// Scan through
 				Scanner scanner3 = new Scanner(tempS);
 			    scanner3.useDelimiter("\\s*,\\s*");
 				// Temporary answerHints list 
 				List<String> tempHints = new ArrayList<String>();
 				// Add each String to temporary answerHints list
 				while (scanner3.hasNext()){
 					tempHints.add(scanner3.next());
 				}
 				if (scanner3!=null){
 					scanner3.close();
 				}
 				// Add result to answerHints parameter
 				ex.setAnswerHints(tempHints.toArray(new String[tempHints.size()]));
 				
 				ex.setAuthor(Teacher.valueOf(scanner2.next().toUpperCase()));
 				ex.setCategory(ExerciseCategory.valueOf(scanner2.next().toUpperCase()));
 				ex.setMaxAnswerTime(scanner2.nextInt());
 				ex.setMaxNumberOfAttempts(scanner2.nextInt());
 				
 				String[] monthNames = {"januari", "februari", "maart", "april", "mei", "juni",
 						"juli", "augustus", "september", "oktober", "november", "december"};
 				// Scan through scanner2.next() which is date
 				Scanner scannerDate = new Scanner(scanner2.next());
 				int counter = 0;
 				int day = scannerDate.nextInt();
 				String monthS = scannerDate.next();
 				int month = 0;
 				// Loop through month list
 				for (String temp : monthNames){
 					counter++;
 					if (monthS.equals(temp)){
 						month = counter;
 					}
 				}
 				int year = scannerDate.nextInt();
 				if (scannerDate!=null){
 					scannerDate.close();
 				}
 				// Add result to dateRegistration parameter
 				ex.setDateRegistration(new DateGC(year, month, day));
 				
 				// Add to exercises based on corresponding subclass
 				if (descriminator.equals("S")){
 					SimpleExercise exS = (SimpleExercise)ex;
 					exercises.add(exS);
 				}
 				if (descriminator.equals("M")){
 					MultipleChoiceExercise exM = (MultipleChoiceExercise)ex;
 					exM.setMultipleChoice(scanner2.next());
 					exercises.add(exM);
 				}
 				if (descriminator.equals("E")){
 					EnumerationExercise exE = (EnumerationExercise)ex;
 					exE.setInCorrectOrder(false);
 					exercises.add(exE);
 				}
 				if (scanner2!=null){
 					scanner2.close();
 				}
 				count++;
 			}
 		} catch(FileNotFoundException ex){
 			System.out.println("Bestand niet gevonden!");
 		} catch(Exception ex){
 			System.out.println(ex.getMessage());
 		}
 	}
 	
 	public void createQuizExercises(List<Exercise> exercises, List<Quiz> quizzes){
 		File file = new File("src" + File.separator + "files" + File.separator + "exercises.txt");
 		
 		try{
 			if (exercises == null)throw new IllegalArgumentException("exercises lijst is null!");
 			if (exercises.size() == 0)throw new IllegalArgumentException("exercises lijst is leeg!");
 			if (quizzes == null)throw new IllegalArgumentException("quizzes lijst is null!");
 			if (quizzes.size() == 0)throw new IllegalArgumentException("quizzes lijst is leeg!");
 			
 			// Scan through file
 			Scanner scanner = new Scanner(file);
 			
 			List<String> tempExercises = new ArrayList<String>();
 			
 			// Add each line as String object to tempExercises list
 			while (scanner.hasNextLine()){
 			  tempExercises.add(scanner.nextLine());
 			}
 	
 			if (scanner!=null){
 			  scanner.close();
 			}
 			// Loop through each String object in tempExercises
 			for (int i = 0; i < tempExercises.size(); i++) {
 				Scanner scanner2 = new Scanner(tempExercises.get(i));
 				scanner2.useDelimiter("\\s*>\\s*");
 				
 				// Skip unused parameters
 				for (int j = 0; j < 9; j++) {
 					scanner2.next();
 				}
 				
 				while (scanner2.hasNext()){
 					Scanner scanner3 = new Scanner(scanner2.next());
					if (scanner2.hasNext("\\s*,\\s*")){
 					scanner3.useDelimiter("\\s*,\\s*");
 					
 					int tempScore = scanner3.nextInt();
 					int tempQuizId = scanner3.nextInt();
 					int tempExerciseID= scanner3.nextInt();
 					QuizExercise qe = new QuizExercise(tempScore, quizzes.get(tempQuizId - 1), exercises.get(tempExerciseID - 1));
 					
 					quizzes.get(tempQuizId - 1).addQuizExercise(qe);
 					exercises.get(tempExerciseID - 1).addQuizExercise(qe);
					}
 					if (scanner2!=null){
 						scanner3.close();
 					}
 				}
 				
 				if (scanner2!=null){
 					scanner2.close();
 				}
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println(e.getMessage());
 		}
 		catch(IllegalArgumentException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	@SuppressWarnings("resource")
 	public static void main(String[] args) throws Exception {
 //		File file = new File("src" + File.separator + "files" + File.separator + "exercises.txt");
 		try {
 //			
 //			Quiz quiz1 = new Quiz();
 //			Quiz quiz2 = new Quiz("testsubject");
 //			
 //			Exercise exercise1 = new SimpleExercise("Wat is mijn Voornaam","Emin",new String[]{"kort","4"},2,30,
 //					Exercise.ExerciseCategory.AARDRIJKSKUNDE,Teacher.BAKKER,
 //					new DateGC(2013,10,1), 'S');
 //			Exercise exercise2 = new SimpleExercise("Wat is mijn Naam","Iandyrhanov",new String[]{"kort","4"},2,30,
 //					Exercise.ExerciseCategory.AARDRIJKSKUNDE,Teacher.BAKKER,
 //					new DateGC(2013,10,1), 'S');
 //			Exercise exercise3 = new SimpleExercise("Hoofdstad van België?","Brussel",new String[]{"kort","4"},2,30,
 //					Exercise.ExerciseCategory.AARDRIJKSKUNDE,Teacher.BAKKER,
 //					new DateGC(2013,10,1), 'S');
 //			
 //			QuizExercise qe = new QuizExercise(1, quiz1, exercise1);
 //			QuizExercise qe2 = new QuizExercise(2, quiz2, exercise1);
 //			
 //			exercise1.addExercise(qe);
 //			exercise1.addExercise(qe2);
 //			
 //			
 			ExerciseCatalog ec = new ExerciseCatalog();
 			QuizCatalog qc = new QuizCatalog();
 //			ec.addExercise(exercise1);
 //			ec.addExercise(exercise2);
 //			ec.addExercise(exercise3);
 //			
 //			ec.writeExercisesToFile();
 			ec.readExercisesFromFile();
 			qc.readQuizzesFromFile();
 			//System.out.println(ec.exercises.size() + "  " + qc.getQuizCatalogs().size());
 			ec.createQuizExercises(ec.getExercises(), qc.getQuizCatalogs());
 			
 			System.out.println(ec.exercises.get(0).getQuizExercises().get(0).getMaxScore());
 			
 			
 			
 			//System.out.println(ec.exercises.get(2));
 			
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	// Comparisons
 	
 	/**
 	 * Comparable
 	 * 
 	 * @param exerciseCatalog
 	 * @return
 	 */
 	public int compareTo(ExerciseCatalog exerciseCatalog){
 		return this.getExercises().size() - exerciseCatalog.getExercises().size();
 	}
 	
 	// Cloneable
 	
 	/**
 	 * Method to clone this object
 	 * 
 	 * @return
 	 */
 	@Override
 	public ExerciseCatalog clone() throws CloneNotSupportedException{
 		List<Exercise> ex = new ArrayList<Exercise>();
 		for (Exercise e : exercises) {
 			ex.add(e);
 		}
 		
 		ExerciseCatalog ec = new ExerciseCatalog(ex);
 		
 		return ec;
 	}
 	
 	// Overrides
 	
 	@Override
 	public String toString() {
 		return "ExerciseCatalog [exercises=" + exercises + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((exercises == null) ? 0 : exercises.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ExerciseCatalog other = (ExerciseCatalog) obj;
 		if (exercises == null) {
 			if (other.exercises != null)
 				return false;
 		} else if (!exercises.equals(other.exercises))
 			return false;
 		return true;
 	}
 }
