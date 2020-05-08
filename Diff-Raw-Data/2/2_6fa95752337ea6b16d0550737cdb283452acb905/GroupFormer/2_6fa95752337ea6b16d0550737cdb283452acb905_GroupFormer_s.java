 package friendzone.elec3609.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import org.springframework.stereotype.Service;
 
 import friendzone.elec3609.model.ProgrammingLanguage;
 import friendzone.elec3609.model.Project;
 import friendzone.elec3609.model.Student;
 import friendzone.elec3609.model.Team;
 import friendzone.elec3609.model.UnitOfStudy;
 
 @Service
 public class GroupFormer{
 	
 	// The following list are the weightings used to determine the compatibility of two students
 	final static int SAME_TUTORIAL_WEIGHT = 5; // added once if both students are in the same tute
 	final static int SAME_PREFERRED_ROLE_WEIGHT = -1000; // subtracted once if both students have the same preferred role. This is done to increase the chance of a student obtaining their preferred role once they are in the same
 	final static int SAME_ESL_WEIGHT = 50; // if both students have english as a second language, they are more likely to want to be in a group with other ESL students. Similarly, fluent English speakers are more likely to want to work with other fluent English speakers.
 	final static int SAME_CONTACT_PREFERENCE_WEIGHT = 40; // added once if both students have the same preferred contact method
 	final static int SAME_STUDY_LEVEL_WEIGHT = 5; // added once if both students have the same study level
 	final static int SHARED_LANGUAGE_WEIGHT = 100; // added for each language that both students share
 	final static int PERCENTAGE_COMPATIBLE_LANGUAGE_WEIGHT = 200; // this score is multiplied by the percentage of compatible languages ( / union of first and seconds languages
 	/**
 	 * This class is used to run the group forming algorithm. As a <code>Runnable</code> object, it can run in it's own <code>Thread</code> and will update the database when it completes.
 	 * Since students access their teams through a database query, they will receive the updates next time they load any view with team-relevant info.
 	 * @author CJR
 	 */
 	private class GroupFormingAlgorithm implements Runnable{
 
 		int projectID;
 		int minTeamSize;
 		int maxTeamSize;
 		String unitCode;	
 		
 		/**
 		 * Node class used by the algorithm, contains an SID of the student it represents and a compatibility score with every other node  (null for dummy nodes) 
 		 * @author CJR
 		 */
 		private class Node{
 			Student student;
 			ArrayList<Link> links; // Links to all other students in the UoS
 			Link[] groupLinks = new Link[maxTeamSize]; //group links is a subset of links that connect this node to the other nodes in its current group
 			
 			public Node(Student student){
 				this.student = student;	
 				links = new ArrayList<Link>();
 			}
 			
 			public Student getStudent(){
 				return student;
 			}
 			public ArrayList<Link> getLinks(){
 				return links;
 			}
 			public Link[] getGroupLinks(){
 				return groupLinks;
 			}
 		}
 		
 		/**
 		 * Links refer to the edges, which connect the Nodes to eachother with a value.
 		 * This value refers to the compatibility of the two students, and boolean is used to ensure we don't try to switch students in the same group, or calculate the viability of a switch that we've already considered
 		 * @author CJR
 		 */
 		private class Link{
 			boolean considered; //links are marked as considered if the nodes are in the same group, if the result of a switch has been examined, or if we just finished switching these nodes
 			Node start;
 			Node end;
 			Link complement; //the link that goes in the other direction (start = end and end = start)
 			int value; // value is the compatibility strength between these two students - it is based on a variety of factors such as acceptance of invitation, languages known and tutorial number and is set when the Link is created
 			
 			public Link(Node start, Node end, Link complement){
 				this.start = start;
 				this.end = end;
 				
 				if (complement == null){ //if we don't already have a complement link, we need to calculate the value of the link and then MAKE a complement link
 					if (end == null){ // if the student is null, we have a dummy node, and all nodes have compatibility 0 with dummy nodes
 						value = 0;
 					}
 					else{ //calculate the compatibility between these two students 
 						Student first = start.getStudent();
 						Student second = end.getStudent();
 						
 						//run through all the cases that add to or subtract from compatibility
 						if (first.getESL() == second.getESL())
 							value += SAME_ESL_WEIGHT;
 						
 						if (first.getPreferredRole() == second.getPreferredRole()){
 							value += SAME_PREFERRED_ROLE_WEIGHT;
 						}
 						
 						if (first.getTutorialNum(unitCode) == second.getTutorialNum(unitCode)){
 							value += SAME_TUTORIAL_WEIGHT;
 						}
 						
 						HashSet<ProgrammingLanguage> combinedLanguages = new HashSet<ProgrammingLanguage>();
 						for (ProgrammingLanguage firstsLanguage : first.getLanguages()){
 							combinedLanguages.add(firstsLanguage);
 							for (ProgrammingLanguage secondsLanguage : second.getLanguages()){
 								combinedLanguages.add(secondsLanguage);
 								if (firstsLanguage == secondsLanguage){
 									value += SHARED_LANGUAGE_WEIGHT;
 								}
 							}
 						}
						value += (combinedLanguages.size() * );
 						
 					}
 					this.complement = new Link(end, start, this);
 				} else {
 					this.complement = complement;
 					this.value = complement.getValue();
 				}
 			
 			}
 			public boolean considered(){
 				return considered;
 			}
 			
 			public void setConsidered(boolean considered){
 				this.considered = considered;
 			}
 			
 			public void setComplement(Link complement){
 				this.complement = complement;
 			}
 			public Link getComplement(){
 				return complement;
 			}
 			public boolean connects(Node a, Node b){
 				return (start == a && end == b) || (start == b || end == a);
 			}
 			public int getValue(){
 				return value;
 			}
 		}
 					
 		ArrayList<ArrayList<Node>> groups; //we have a list of groups, and each group has a list of nodes (its current members) 
 		HashMap<Link, ArrayList<Link>> teamLinks; //given a Link object, this will return all links (link.start, u) where u is a member of link.ends group 
 		
 		public GroupFormingAlgorithm(int projectID){
 			this.projectID = projectID;
 			Project project = dbHandler.getProject(projectID);
 			UnitOfStudy parent = project.getParent();
 			this.unitCode = unitCode;
 			this.minTeamSize = project.getMinTeamSize();
 			this.maxTeamSize = project.getMaxTeamSize();
 		
 			//build the data structure
 			List<Student> studentList = project.getParent().getStudents();
 			int numGroups = (studentList.size() / maxTeamSize) + (studentList.size() % maxTeamSize > 0? 1 : 0);
 			int numNodes = (numGroups * maxTeamSize); // there needs to be dummy nodes to fill the gaps, so the number of nodes >= the number of students to make all groups have maxTeamSize nodes 
 			Node[] nodeList = new Node[numNodes];
 			
 			for (int i = 0; i != (numGroups * maxTeamSize); i++){		
 				nodeList[i] = new Node((i < studentList.size()? studentList.get(i) : null));
 			}
 			
 			ArrayList<Link> allLinks = new ArrayList<Link>();
 			//every pairing has a unique link, so we can discard link where i==j (can't link to self) and i<j (Link(i,j) == Link(j,i), so just make Link(j,i))
 			for (int i = 0; i < nodeList.length; ++i){ 
 				for (int j = i+1; j < nodeList.length; ++j){
 					Link newLink = new Link(nodeList[i], nodeList[j], null);
 					
 				}
 			}
 			
 			for (int i = 0; i < numGroups; ++i){
 		//		groups.set(i, new HashSet<Node>());
 				for (int j = 0; j < maxTeamSize; ++j){
 					groups.get(i).add(nodeList[(i * maxTeamSize) + j]);
 				}
 			}
 			
 		}
 		
 		
 		public void run() {
 			System.out.println("Starting group forming for " + projectID);
 			
 			//TODO: group forming algorithm runs here
 			//Link currentBestSwitch = null;
 				//while exists links that have not yet been considered:
 					//if nodes in same group, mark as considered and move on
 					//else calculate the profit that this switch would provide:
 						//for each other link:
 							//currentFirstScore = 0 -> find each node u in the same group as link.start, add value of Link(u, link.start) to currentFirstScore
 							//currentSecondScore = 0 -> find each node v in the same group as link.end, add value of Link(v, link.end) to currentSecondsScore
 							//resultantFirstScore = 0 -> find each node v in the same group as link.end, add value of Link(v, link.start) to resultantFirstScore
 							//resultantSecondScore = 0 -> find each node v in the same group as link.start, add value of Link(u, link.end) to resultantFirstScore
 							// calculate profit as (resultantFirstScore - currentFirstScore) + (resultantSecondScore - resultantFirstScore)
 							// if profit < 0 || profit < maxProfit, mark as Link(link.start, link.end) as considered and move on
 							// else, set currentBestSwitch as Link(link.start, link.end), mark as considered and move on
 						// <END CONDITION> if best switch = null, there were no links that provide a profit > 0, so we end the algorithm
 						// else
 							// perform the switch (link.start, link.end)
 							// set all links back to !considered except for the following (note that this keeps the algorithm running in the while loop)
 							//		- (link.start, link.end) (we know the switch gave profit, so switching back would be loss)
 							//		- all links (u, link.start) and (v, link.end) where u is in the same group as link.start or v is in the same group as link.end
 			
 			
 			//could be sped up if we had:
 			//	- we have a stack/queue for all links not yet considered, which we pop from until empty, then push to until we hit the end condition
 			//	- link(u,v) == link(v,u)
 			//	- we have a way to instantly access the set of links between a node and a group with some sort of mapping (node, group) -> ArrayList<Link>
 			//	-	HashMap<Link, Set<Link>> returns links between given links start and all nodes in link.end's group?
  			//add the result to the database by using the setter methods
 			
 			HashSet<Node>[] finalGroups = (HashSet<Node>[]) groups.toArray();
 			for (int i = 0; i != finalGroups.length; ++i){
 				Team newTeam = new Team(projectID, projectID + " group " + i);
 				for (Node studentNode : finalGroups[i]){ 
 					studentNode.getStudent().joinTeam(newTeam.getID());
 				}
 			}
 			
 			//TODO: delete all invitations referencing this project
 			
 			System.out.println("Group forming for " + projectID + " completed!");
 		}
 	}
 	
 	final static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
 	
 	
 	/**
 	 * Creates a new <code>Thread</code> that runs the <code>GroupFormingAlgorithm</code>. The method does not return a value because it runs in parallel, and instead it writes the changes to the database where they can be retrieved.
 	 * @param projectID The project id corresponding to the <code>Project</code> that needs <code>Team</code>s formed.
 	 */
 	public void createGroups(int projectID) {
 		Thread thread = new Thread(new GroupFormingAlgorithm(projectID));
 		thread.run();
 		return;
 	}
 	
 	
 }
