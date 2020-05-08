 import java.io.IOException;
 import java.util.Random;
 import java.util.Vector;
 
 import javax.microedition.lcdui.Image;
 
 
 public class ProblemSet {
 	
 	Random randomizer;
 
 	private Image[] molecules;
 	private int numMolecules;
 	private String[] reactions;
 	private int numReactions;
 
 	private int startingMaterial;
 	private int product;
 
 	private ReactionList[][] reactionArray;
 
 	private Vector studentsAnswer;
 
 	private boolean[][] successfullyCompletedProblemMatrix;
 	private int numSuccessfullyCompletedProblems = 0;
 	private int totalNumberOfProblems;
 
 
 	public ProblemSet() {
 
 		numMolecules = 4;
 		totalNumberOfProblems = (numMolecules * numMolecules) - numMolecules;
 		numReactions = 7;
 
 		//list of reactions in current solution attempt
 		studentsAnswer = new Vector();
 
 		randomizer = new Random();
 		
 		//load in images
 		molecules = new Image[4];
 		reactions = new String[] {"SOCl2",
 				"Oxidation",
 				"NH3",
 				"CH3OH",
 				"Reduction",
 				"H3O+",
 				"OH-",
 		"None of the above"};
 
 		try {
 			for (int i = 0; i < numMolecules; i++) {
 				String fileName = "/images/mol" + i + ".png";
 				molecules[i] = Image.createImage(fileName);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		//set up correct reaction array
 		reactionArray = new ReactionList[numMolecules][numMolecules];
 
 		//can't convert mol0 to mol0
 		for (int i = 0; i < numMolecules; i++) {
 			reactionArray[i][i] = null;
 		}
 
 		// row, column
 		reactionArray[0][1] = new ReactionList("1");
 		reactionArray[0][2] = new ReactionList("1,0,2");
 		reactionArray[0][3] = new ReactionList("1,3");
 
 		reactionArray[1][0] = new ReactionList("4");
 		reactionArray[1][2] = new ReactionList("0,2");
 		reactionArray[1][3] = new ReactionList("0,3");
 
 		reactionArray[2][0] = new ReactionList("5,4");
 		reactionArray[2][1] = new ReactionList("5");
 		reactionArray[2][3] = new ReactionList("5,0,3");
 
 		reactionArray[3][0] = new ReactionList("4");
 		reactionArray[3][1] = new ReactionList("6");
 		reactionArray[3][2] = new ReactionList("2");
 
 		//set up array of successfully-completed problems
 		successfullyCompletedProblemMatrix = new boolean[numMolecules][numMolecules];
 		for (int i = 0; i < numMolecules; i++) {
 			for (int j = 0; j < numMolecules; j++) {
 				if (i == j) {
 					successfullyCompletedProblemMatrix[i][j] = true;
 				} else {
 					successfullyCompletedProblemMatrix[i][j] = false;
 				}
 			}
 		}
 
 		newProblem();
 	}
 
 	public void newProblem() {
 		// see if all done
 		if (numSuccessfullyCompletedProblems == totalNumberOfProblems) {
 			System.out.println("Yahoo");
 		} else {
 			studentsAnswer.removeAllElements();
 			startingMaterial = 0;
 			product = 0;
 			while (isSuccessfullyCompleted(startingMaterial, product)) {
 				while (product == startingMaterial) {
 					startingMaterial = getRandomInt(0,3);
 					product = getRandomInt(0,3);
 				}
 			}
 		}
 	}
 
 	public Image getMoleculeImage(int i) {
 		if (i < molecules.length) {
 			return molecules[i];
 		} else {
 			return null;
 		}
 	}
 
 	public int getNumMolecules() {
 		return numMolecules;
 	}
 
 	public String getReactionDescription(int i) {
 		if (i < reactions.length) {
 			return reactions[i];
 		} else {
 			return "";
 		}
 	}
 
 	public String[] getAllReactions() {
 		return reactions;
 	}
 
 	public int getNumReactions() {
 		return numReactions;
 	}
 
 	public int getStartingMaterial() {
 		return startingMaterial;
 	}
 
 	public int getProduct() {
 		return product;
 	}
 
 	public ReactionList getCorrectAnswer(int startingMaterial, int product) {
 		if ((startingMaterial < numMolecules) &&
 				(product < numMolecules)) {
 			return reactionArray[startingMaterial][product];
 		} else {
 			return null;
 		}
 	}
 
 	public int getSizeOfStudentsAnswer() {
 		return studentsAnswer.size();
 	}
 	
 	public int[] getStudentsAnswer() {
 		int[] answer = new int[studentsAnswer.size()];
 		for (int i = 0; i < studentsAnswer.size(); i++){
 			answer[i] = ((Integer)studentsAnswer.elementAt(i)).intValue();
 		}
 		return answer;
 	}
 
 	//adds to end
 	public void addReactionToStudentsAnswer(int reaction) {
 		studentsAnswer.addElement(new Integer(reaction));
 	}
 
 	//adds before element at location
 	public void addReactionToStudentsAnswer(int reaction, int location){
 		if (location == -1) {
 			addReactionToStudentsAnswer(reaction);
 		} else {
 			studentsAnswer.insertElementAt(new Integer(reaction), location);
 		}
 	}
 
 	public void deleteReactionFromStudentsAnswer(int location) {
 		if (studentsAnswer.size() != 0) {
 			studentsAnswer.removeElementAt(location);
 		}
 	}
 
 	public boolean isCurrentListCorrect() {
 		int[] correctList = getCorrectAnswer(startingMaterial, product).getList();
		
		//debug code
//		System.out.println("******");
//		System.out.println("Checking answer");
//		System.out.print("Student(" + studentsAnswer.size() + "):");
//		for (int i = 0; i  < studentsAnswer.size(); i++){
//			System.out.print(studentsAnswer.elementAt(i));
//		}
//		System.out.print("\n");
//		System.out.print("Correct (" + correctList.length + "):");
//		for (int i = 0; i < correctList.length; i++) {
//			System.out.print(correctList[i]);
//		}
//		System.out.print("\n");
		
 		if (correctList.length != studentsAnswer.size()) {
//			System.out.println("Wrong answer length");
 			return false;
 		}
		
//		System.out.println("Comparing S,C:");
 		boolean isCorrect = true;
 		for (int i = 0; i < correctList.length; i++) {
//			System.out.println(studentsAnswer.elementAt(i) + "," + correctList[i]);
 			if (correctList[i] != ((Integer)studentsAnswer.elementAt(i)).intValue()){
 				isCorrect = false;
 			}
 		}
//		System.out.println("returning " + isCorrect);
//		System.out.println("***");
 		return isCorrect;
 	}
 
 	public void setSuccessfullyCompleted(int startingMaterial, int product) {
 		if ((startingMaterial < numMolecules) 
 				&& (product < numMolecules)) {
 			successfullyCompletedProblemMatrix[startingMaterial][product] = true;
 			numSuccessfullyCompletedProblems++;
 		}
 	}
 
 	public boolean isSuccessfullyCompleted(int startingMaterial, int product) {
 		return successfullyCompletedProblemMatrix[startingMaterial][product];
 	}
 
 	public int getNumSuccessfullyCompletedProblems() {
 		return numSuccessfullyCompletedProblems;
 	}
 
 	public int getTotalNumberOfProblems() {
 		return totalNumberOfProblems;
 	}
 	
 	public int getRandomInt(int min, int max){
 		int r = Math.abs(randomizer.nextInt());
 		return (r % (max - min)) + min;
 	}
 }
