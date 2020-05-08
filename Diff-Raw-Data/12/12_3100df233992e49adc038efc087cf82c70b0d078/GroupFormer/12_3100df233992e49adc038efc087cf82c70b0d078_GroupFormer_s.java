 package friendzone.elec3609.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Stack;
 
 import org.springframework.stereotype.Service;
 
 import friendzone.elec3609.model.Invitation;
 import friendzone.elec3609.model.ProgrammingLanguage;
 import friendzone.elec3609.model.Project;
 import friendzone.elec3609.model.Student;
 import friendzone.elec3609.model.Team;
 import friendzone.elec3609.model.UnitOfStudy;
 
 @Service
 public class GroupFormer{
 	
 	final static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
 	
 	
 	// The following list are the weightings used to determine the compatibility of two students
 	final static int SAME_ESL_WEIGHT = 50; // if both students have english as a second language, they are more likely to want to be in a group with other ESL students. Similarly, fluent English speakers are more likely to want to work with other fluent English speakers.
 	final static int SAME_PREFERRED_ROLE_WEIGHT = -1000; // subtracted once if both students have the same preferred role. This is done to increase the chance of a student obtaining their preferred role once they are in the same
 	final static int SAME_TUTORIAL_WEIGHT = 5; // added once if both students are in the same tute
 	final static int SHARED_LANGUAGE_WEIGHT = 100; // added for each language that both students share. Unlike the next weighting, this aims to maximise the AMOUNT of options the group have in common.  
 	final static int PERCENTAGE_SHARED_LANGUAGE_WEIGHT = 200; // this score is multiplied by the percentage of compatible languages (size of intersection of first and seconds languages / size of union of first and seconds languages). This weighting aims to ensure that all members of the groups have a high percentage of the languages they are comfortable with that the others are also comfortable with
 	final static int SAME_CONTACT_PREFERENCE_WEIGHT = 40; // added once if both students have the same preferred contact method
 	final static int SAME_STUDY_LEVEL_WEIGHT = 5; // added once if both students have the same study level
 		
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
 		Stack<Link> stackedLinks;
 		List<Student> studentList;
 		ArrayList<Link> allLinks;
 		ArrayList<Group> groups; 
 		HashMap<Student, ArrayList<Student>> initialGroups;
 		
 		private class Group{
 			int currentSize; //refers to the number of students, not the number of nodes - a group could have 5 nodes, but all with more than one student!
 			HashSet<Node> nodes;
 			
 			public void addNode(Node node){
 				if (node.getStudents() != null){
 					currentSize += node.getStudents().size();
 				}
 				node.setGroup(this);
 				nodes.add(node);
 			}
 			
 			public int getCurrentSize(){
 				return currentSize;
 			}
 			
 			public void removeNode(Node node){
 				if (node.getStudents() != null){ //if this is a one-student node with no student object, its one of our dummy nodes
 					currentSize -= node.getStudents().size();
 				}
 				node.setGroup(null);
 				nodes.remove(node);
 			}
 			
 			public Node[] getMembers(){
 				return (Node[]) nodes.toArray();
 			}
 		}
 		
 		/**
 		 * Node class used by the algorithm, contains the Student and the id of the group it is currently in (which is used as an index in the groups array list)
 		 * @author CJR
 		 */
 		private class Node{
 			HashMap<Node, Link> links;
 			ArrayList<Student> students;
 			Group group;
 			
 			public Node(ArrayList<Student> students){
 				this.students = students;	
 				links = new HashMap<Node, Link>();
 			}
 			
 			public ArrayList<Student> getStudents(){
 				return students;
 			}
 			
 			public void setGroup(Group group){
 				this.group = group;
 			}
 			
 			public int getNumStudents(){
 				return students.size();
 			}
 			
 			public void setLinkTo(Node node, Link link){
 				links.put(node, link);
 			}
 			
 			public Link getLinkTo(Node node){
 				return links.get(node);
 			}
 			
 			public Group getGroup(){
 				return group;
 			}
 			
 			public void switchGroup(Node node){
 				Group thisGroup = group;
 				Group nodesGroup = node.getGroup();
 				
 				thisGroup.removeNode(this);
 				nodesGroup.removeNode(node);
 				thisGroup.addNode(node);
 				nodesGroup.addNode(this);
 			}
 		}
 		
 		/**
 		 * Links refer to the edges, which connect the Nodes to eachother with a value.
 		 * This value refers to the compatibility of the two students, and boolean is used to ensure we don't try to switch students in the same group, or calculate the viability of a switch that we've already considered
 		 * @author CJR
 		 */
 		private class Link{
 			Node start;
 			Node end;
 			int value; // value is the compatibility strength between these two students - it is based on a variety of factors such as acceptance of invitation, languages known and tutorial number and is set when the Link is created
 			
 			public Link(Node start, Node end){
 				this.start = start;
 				this.end = end;
 				
 				if (start.getStudents() == null || end.getStudents() == null){ // if the student is null, we have a dummy node, and all nodes have compatibility 0 with dummy nodes
 					value = 0;
 				}
 				else{
 					//calculate the compatibility between these two nodes - to do this, it compares all students in the node to all students in the other node  
 					for (Student first : start.getStudents()){
 						for (Student second : end.getStudents()){
 							//run through all the cases that add to or subtract from compatibility
 							if (first.getESL() == second.getESL())
 								value += SAME_ESL_WEIGHT;
 							
 							if (first.getPreferredRole() == second.getPreferredRole()){
 								value += SAME_PREFERRED_ROLE_WEIGHT;
 							}
 							
 							if (first.getTutorialNum(unitCode) == second.getTutorialNum(unitCode)){
 								value += SAME_TUTORIAL_WEIGHT;
 							}
 							
 							ArrayList<ProgrammingLanguage> intersection = new ArrayList<ProgrammingLanguage>();
 							HashSet<ProgrammingLanguage> union = new HashSet<ProgrammingLanguage>(); // provides the union of first and seconds languages
 							for (ProgrammingLanguage firstsLanguage : first.getLanguages()){
 								intersection.add(firstsLanguage);
 								for (ProgrammingLanguage secondsLanguage : second.getLanguages()){
 									intersection.add(secondsLanguage);
 									if (firstsLanguage == secondsLanguage){
 										value += SHARED_LANGUAGE_WEIGHT;
 										union.add(firstsLanguage);
 									}
 								}
 							}
 							value += (PERCENTAGE_SHARED_LANGUAGE_WEIGHT * (intersection.size() / union.size()));
 							
 							if (first.getPreferredContact() != null && first.getPrefferedContact() == second.getPreferredContact()){
 								value += SAME_CONTACT_PREFERENCE_WEIGHT;	
 							}
 							
 							if (first.getStudyLevel() == second.getStudyLevel()){
 								value +=  SAME_STUDY_LEVEL_WEIGHT; 
 							}			
 						}
 					}
 				}
 				//now create a link that goes the other direction - this new link will set this as its complement, and use the same value we just created
 				end.setLinkTo(start, this);
 				start.setLinkTo(end, this);
 			}
 			public int getValue(){
 				return value;
 			}
 			public Node getEnd() {
 				return end;
 			}
 			public Node getStart() {
 				return start;
 			}
 		}
 			
 		public GroupFormingAlgorithm(int projectID){
 			
 			//set the local variables
 			this.projectID = projectID;
 			Project project = dbHandler.getProject(projectID);
 			UnitOfStudy parent = project.getParent();
 			this.studentList = parent.getStudents();
 			this.unitCode = parent.getUnitCode();
 			this.minTeamSize = project.getMinTeamSize();
 			this.maxTeamSize = project.getMaxTeamSize();
 			stackedLinks = new Stack<Link>();
 			allLinks = new ArrayList<Link>();
 			groups = new ArrayList<Group>();
 			
 			//data structure could be built here, but since that's quite a time consuming process I decided to do that after the thread is spawned
 		}
 		
 		
 		public void run() {
 			System.out.println("Starting group forming for " + projectID);
 			
 			//get all accepted invitations make some starter groups
 			initialGroups = new HashMap<Student, ArrayList<Student>>();
 			List<Invitation> acceptedInvites = dbHandler.getInvitations(projectID, true);
 			
 			int numGroups = (studentList.size() / maxTeamSize) + (studentList.size() % maxTeamSize > 0? 1 : 0); 
 			
 			//for all accepted invitations, we form a node to force these students together (so if the algorithm switches the 
 			for (Invitation invite : acceptedInvites){
 				Student sender = invite.getSender();
 				Student recipient = invite.getRecipient();
 			
 				ArrayList<Student> sendersGroup;
 				ArrayList<Student> recipientsGroup;
 				if ((sendersGroup = initialGroups.get(sender)) == null){
 					if ((recipientsGroup = initialGroups.get(recipient)) == null){
 						studentList.remove(sender); // by removing the students from the list, we are marking them as already groups (
 						studentList.remove(recipient);
 						initialGroups.put(sender, new ArrayList<Student>());
 						initialGroups.put(recipient, sendersGroup);
 					}
 					else{
 						recipientsGroup.add(sender);
 						initialGroups.put(sender, recipientsGroup);
 						studentList.remove(sender);
 					}
 				}
 				else{
 					sendersGroup.add(recipient);
 					initialGroups.put(recipient, sendersGroup);
 					studentList.remove(recipient);
 				}
 			}
 			
 			ArrayList<Node> nodeList = new ArrayList<Node>();
 
 			//now merge these students so that they are treated as one node (unable to be seperated)
 			for (ArrayList<Student> group : initialGroups.values()){
 				Node groupNode = new Node(group);
 				nodeList.add(groupNode);
 			}
 			
 			//with the remaining students we build one-student nodes and dummy nodes to fill the gaps
 			for (int i = 0; i != (numGroups * maxTeamSize); i++){
 				if (i < studentList.size()){
 					Student student = studentList.get(i);
 					ArrayList<Student> oneStudentList = new ArrayList<Student>();
 					oneStudentList.add(student);
 					Node studentNode = new Node(oneStudentList);
 					nodeList.add(studentNode);
 				}
 				else{
 					//for dummy nodes, we make a node with null students
 					nodeList.add(new Node(null));
 				}
 			}
 			
 			//every pairing has a unique link, so we can discard link where i==j (can't link to self) and i<j (Link(i,j) makes Link(j,i) so we only call the constructors for Link(i,j))
 			for (int i = 0; i < nodeList.size(); ++i){ 
 				for (int j = i+1; j < nodeList.size(); ++j){
 					Link newLink = new Link(nodeList.get(i), nodeList.get(j));
 					allLinks.add(newLink);
 					stackedLinks.push(newLink); //stackedLinks will empty, and so we need allLinks as well to fill it back up
 				}
 			}
 			
			//now we can put nodes into groups
 			for (int i = 0; i != numGroups; i++){
 				Group newGroup = new Group();
 				for (int j = 0; j != maxTeamSize; j++){
 					newGroup.addNode(nodeList.get((i * maxTeamSize) + j));
 				}
 				groups.add(newGroup);
 			}
 			
 
 			//now we start the actual algorithm
 			boolean remainingOptions = true;
 			while (remainingOptions){
 				Link currentBestSwitch = null;
 				int currentBestSwitchScore = 0;
 				
 				while (!stackedLinks.isEmpty()){
 					Link currentLink = stackedLinks.pop();
 					Node start = currentLink.getStart();
 					Node end = currentLink.getEnd();
 					
 					int startGroupSizeAfterSwitch = start.getGroup().getCurrentSize() - start.getNumStudents() + end.getNumStudents();
 					int endGroupSizeAfterSwitch = end.getGroup().getCurrentSize() - end.getNumStudents() + start.getNumStudents();
 					
 					if (start.getGroup() == end.getGroup() || // we don't bother switching students in the same group
 						(startGroupSizeAfterSwitch < minTeamSize || startGroupSizeAfterSwitch > maxTeamSize) || // we also have to enforce the group sizes, so don't switch where it would break these constraints
 						(endGroupSizeAfterSwitch < minTeamSize || endGroupSizeAfterSwitch > maxTeamSize)){
 							continue;
 					}
 					
 					//if we reach this point, this link is a valid option, so we have to calculate the profit it would give
 					int currentStartScore = 0;
 					int currentEndScore = 0;
 					int resultantStartScore = 0;	
 					int resultantEndScore = 0;
 					
 					for (Node startsGroupMember : start.getGroup().getMembers()){
 						if (startsGroupMember != start){
 							currentStartScore += start.getLinkTo(startsGroupMember).getValue();
 							resultantEndScore += end.getLinkTo(startsGroupMember).getValue();
 						}
 					}
 					
 					for (Node endsGroupMember : end.getGroup().getMembers()){
 						if (endsGroupMember != end){
 							currentEndScore += end.getLinkTo(endsGroupMember).getValue();
 							resultantStartScore += start.getLinkTo(endsGroupMember).getValue();
 						}
 					}
 					
 					// find the switch that maximises the profit based on the equation below
 					int profit = ((resultantStartScore - currentStartScore) + (resultantEndScore - currentEndScore));
 					if (profit > 0 && profit > currentBestSwitchScore){
 						currentBestSwitch = currentLink;
 						currentBestSwitchScore = profit;
 					}
 				}
 				
 				//end condition! there were no switches that provided a positive profit
 				if (currentBestSwitch == null){
 					remainingOptions = false;
 				}
 				else{
 					//when we reach here, we've found the currentBestSwitch that maximises profit, so perform the switch and reset a
 					currentBestSwitch.getStart().switchGroup(currentBestSwitch.getEnd());
 					System.out.println("Increased profit of solution by " + currentBestSwitchScore);
 					
 					//now every link becomes a valid option again! (except for currentBestSwitch, because switching back makes now sense
 					for (Link link : allLinks){
 						if (link != currentBestSwitch){
 							stackedLinks.push(link);
 						}
 					}
 				}
 			}
 				
 			
 			for (int i = 0; i < groups.size(); i++){
 				Group group = groups.get(i);
 				Team newTeam = new Team(projectID, projectID + " group " + i);
 				for (Node node : group.getMembers()){ 
 					if (node.getStudents() != null){
 						for (Student student : node.getStudents()){
 							student.joinTeam(newTeam.getID());
 						}
 					}
 				}
 			}
 			
 			System.out.println("Group forming for " + projectID + " completed!");
 		}
 	}
 	
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
