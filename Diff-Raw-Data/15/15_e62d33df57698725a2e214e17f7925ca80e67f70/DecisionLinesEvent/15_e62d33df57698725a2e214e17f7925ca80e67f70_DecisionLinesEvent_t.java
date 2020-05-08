 package model;
 
 import java.util.ArrayList;
 
 /**
  * This class stores all the information needed
  * by a Decision Line Event
  * @author Trevor Hodde
  */
 public class DecisionLinesEvent {
 	public String question;
 	public String mode;
 	public String type;
 	public int rounds;
 	public int position;
 	public String eventId;
 	private Line[] choices;
 	public String username;
 	public String password;
 	private ArrayList<Edge> edges;
 	static DecisionLinesEvent localInstance = null;
 	
 	/**
 	 * This constructor sets default values to 
 	 * all the data used by the Decision Line Event
 	 */
 	DecisionLinesEvent() {
 	     question = "";
 	     eventId = "";
 	     position = 0;
 	     type = "";
 	     mode = "";
 	     rounds = 0;
 	     eventId = "";
 	     username = "";
 	     password = "";
 	     edges = new ArrayList<Edge>();
 	 }
 	
 	public static DecisionLinesEvent getInstance() {
 		if (localInstance == null)
 			localInstance = new DecisionLinesEvent();
 		
 		return localInstance;
 	}
 	
 	public ArrayList<Edge> getEdges() {
 		if (edges == null)
 			edges = new ArrayList<Edge>();
 		return edges;
 	}
 
     public String getMode() {
 		return mode;
 	}
 	
 	public void setMode(String mode) {
 		this.mode = mode;
 	}
     
 	 public int getRounds() {
 		return rounds;
 	}
 		
 	public void setRounds(int rounds) {
 		this.rounds = rounds;
 	}
 	
 	public int getPostion() {
 		return position;
 	}
 			
 	public void setPosition(int position) {
 		this.position = position;
 	}
 	
 	public String getType() {
 		return type;
 	}
 		
 	public void setType(String type) {
 		this.type = type;
 	}
 		
 	public String getQuestion() {
 		return question;
 	}
 		
 	public void setQuestion(String question) {
 		this.question = question;
 	}
 
 	public String getEventID() {
 		return eventId;
 	}
 	
 	public void setEventID(String eventId) {
 		this.eventId = eventId;
 	}
 
 	public Line getChoice(int i) {
 		if (i < 0 && i >= choices.length)
 			return null;
 		
 		return choices[i];
 	}
 	
 	public void setChoice(Line choice) {
 		if (choice.getLinePosition() < 0 && choice.getLinePosition() >= choices.length)
 			return;
 		
 		choices[choice.getLinePosition()] = choice;
 		
 		if (type.equals("open")) {
 			boolean allChoicesSet = true;
 			for (int x = 0; x < choices.length; x++) 
 				if (choices[x] == null)
 					allChoicesSet = false;
 			
 			if (allChoicesSet) {
 				type = "closed";
 			}
 		}
 	}
 
 	public void setNumChoices(int numChoices) {
 		choices = new Line[numChoices];
 	}
 	
 	public int getNumChoices() {
 		return choices.length;
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	/**
 	 * This method sets the final choice of the decision line event
 	 */
 	public void determineFinalOrder() {
 		//make an array to store the current edge in one spot and its height in the other
 		int heights[] = new int[2];
 		//make sure we start at the top
 		int currentHeight = 0; 
 		int position = 0;
 		int currentPosition = 0;
 		
 		// Calculate each Choice
 		for(int i = 0; i < choices.length; i++) {
 			//position = choices[i].getLinePosition();
 			//heights = getNextEdge(position, 65000);
 			currentPosition = choices[i].getLinePosition();
 			currentHeight = 65000;
 
 			while(currentHeight > 0) {
 				position = currentPosition;
 				heights = getNextEdge(position, currentHeight);
 				currentPosition = heights[0];
 				currentHeight = heights[1];
 			}
 			choices[i].setFinalOrder(currentPosition);
 		}
 	}
 
 	/**
 	 * This method gets the closest edge to the current position on the line
 	 * @param choice int: the choice that we are currently on
 	 * @param height: int the height on the decision line
 	 * @return int[] the next edge on the line
 	 */
 	private int[] getNextEdge(int choice, int height) {	
 		//make sure there are edges stored, otherwise return default values
 		if(edges.size() == 0) {
 			return new int[]{choice, 0};
 		}
 		
 		//grab the first edge
 		Edge currentEdge = edges.get(0);
		int results[] = new int[] { choice, 0 };
 		
 		// Go through the each Edge on the lines
 		for(int i = 0; i < edges.size(); i++) {
 			currentEdge = edges.get(i);
 			//make sure the next edge is below the current height
			if(currentEdge.getHeight() < height && (currentEdge.getLeft() == results[0] || currentEdge.getRight() == results[0])) {
 				//we are already on the right, we have to move left
 				if(choice != currentEdge.getLeft()) {
 					results[0] = currentEdge.getLeft();
 				}
 				else { //if we are already on the left, we have to go right
 					results[0] = currentEdge.getRight();
 				}
 				//store the height of the current edge
 				results[1] = currentEdge.getHeight();
 			}
 		}
 		return results;
 	}
 	
 	/**
 	 * This method allows us to get the final ordering at the end of the event
 	 * and display it for all users.
 	 * @param position int the position in the ordering of the current choice
 	 * @return String the name of the choice at the current position
 	 */
 	public String getChoiceOrderPosition(int position) {
 		String choiceName = "";
 		for(int i = 0; i < choices.length; i++) {
 			if(choices[i].getFinalOrder() == position) {
				choiceName = choices[i].getChoice();
 			}
 		}
 		return choiceName;
 	}
 }
