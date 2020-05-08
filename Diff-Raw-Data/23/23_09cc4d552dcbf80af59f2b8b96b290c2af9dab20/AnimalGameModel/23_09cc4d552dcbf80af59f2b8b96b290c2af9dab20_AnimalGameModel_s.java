 /*
  * AnimalGameModel.java
  * Matt Dickenson
  * 
  * Implementation of IAnimalGameModel.
  */
 import java.io.*;
 import java.util.*;
 
 public class AnimalGameModel implements IAnimalModel {
 	
 	// In Model-View systems, the Model & View must be able to communicate;
 	// this pointer to the view lets you tell the view to do things (show messages,
 	// for example).
 	private AnimalGameViewer myView;
 	private AnimalNode myRoot;
 	private AnimalNode myCurrent; 
 	private StringBuilder myPath; 
     
 	@Override
 	public void addNewKnowledge(String noResponse) {
		// TODO Auto-generated method stub
 		myCurrent = myCurrent.getNo();
 		AnimalNode newYes = new AnimalNode(noResponse, null, null);
 		myCurrent.setYes(newYes);
 	}
 
 	@Override
 	public void addNewQuestion(String question) {
 		question = question.replace("?", "");
 		AnimalNode newQuestion = new AnimalNode(question, null, null);
 		myCurrent.setNo(newQuestion);
		myView.getDifferentiator();
 	}
 
 	@Override
 	public void initialize(Scanner s) {
 		myRoot = readHelper(s); 	
 		myView.setEnabled(true);
 		newGame();
 	}
 	
 	private AnimalNode readHelper(Scanner s){
 		String line = s.nextLine();
 		if( isLeaf(line) ){
 			AnimalNode leafNode = new AnimalNode(line, null, null);
 			return leafNode; 
 		}
 		line = line.substring(3);
 				
 		// Make a recursive call to read left subtree
 		AnimalNode leftNode = readHelper(s); 
 		// Make a recursive call to read right subtree
 		AnimalNode rightNode = readHelper(s); 
 		// Construct resulting AnimalNode and return it 
 		AnimalNode newNode = new AnimalNode(line, leftNode, rightNode);
 		return newNode; 
 	}
 	
 	private boolean isLeaf(String line){
 		if( line.startsWith("#Q:")){ return false; }
 		else { return true; } 
 	}
 
 	@Override
 	public void newGame() {
 		myPath = new StringBuilder(); 
 		myPath.append("Please enter a question for which the\n");
 		myPath.append("answer is no for what I said and yes for");
 		myPath.append("the word you were thinking of. Then click OK.");
 		myPath.append("Please phrase as a question, e.g.,\n");
 		myPath.append("Are you a rhinoceros?\n");
 		myPath.append("Are you 'The Grapes of Wrath'?\n");
 		myPath.append("Your path below is a reminder.\n");
 		myPath.append("\n");
 		myPath.append("Then enter the word you were thinking of.\n");
 		myPath.append("\n");
 		myPath.append("\nYour path so far:\n");
 		
		
 		myCurrent = myRoot; 
 		askQuestion(myCurrent);	
 	}
 	
 	private void askQuestion(AnimalNode node){
 		String data = node.toString(); 
 		if(data.startsWith("#Q:")){
 			data = data.substring(0, 3);
 		}
 		data = data + "?\n";
 		StringBuilder sb = new StringBuilder();
 		sb.append(data);
 		myView.update(sb.toString());
 	}
 
 	@Override
 	public void processYesNo(boolean yes) {
 		AnimalNode node;
 		String youSaid; 
 		String question = myCurrent.toString();
 		
 		if(yes){ 
 			node = myCurrent.getYes(); 
 			youSaid = "You answered YES to ";
 		}
 		else{ 
 			node = myCurrent.getNo();	
 			youSaid = "You answered NO to ";
 		}
 		
 		youSaid = youSaid + question + "\n"; 
 		myPath.append(youSaid);
 		
 		if(node==null && yes){ 
 			myView.showDialog("I won!"); 
 		}
 		else if(node==null && !yes){ 
 			handleLoss(); 
 		}
 		else{
 			myCurrent = node;
 			askQuestion(myCurrent);
 		}	
 	}
 	
 	public void handleLoss(){
 		myView.update(myPath.toString());
		myView.getNewInfoLeaf(); 
 	}
 
 	@Override
 	public void setView(AnimalGameViewer view) {
 		myView = view;
 	}
 
 	@Override
 	public void write(FileWriter writer) {
 		writeHelper(myRoot, writer);
 		try {
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void writeHelper(AnimalNode n, FileWriter w){
 		String text; 
 		
 		// check whether node is a leaf
 		if((n.getYes() == null) && (n.getNo() == null)){
 			text = n.toString() + "\n"; 
 			try {
 				w.write(text);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else{
 			text = "#Q:" + n.toString() + "\n"; 
 			try {
 				w.write(text);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} 
 
 			if(n.getYes() != null){
 				writeHelper(n.getYes(), w); 
 			}
 			if(n.getNo() != null){
 				writeHelper(n.getNo(), w); 
 			}
 		}
 	}
 	
 }
