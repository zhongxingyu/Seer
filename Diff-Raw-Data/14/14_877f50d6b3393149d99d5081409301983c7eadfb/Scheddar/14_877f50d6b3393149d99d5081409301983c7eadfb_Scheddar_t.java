 package edu.brown.cs32.scheddar;
 
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 
 /**
  * This is the main class of the application
  * @author atutino
  *
  */
 
 
 public class Scheddar implements ScheddarFace {
 	
 	private HashMap<String,Group> groups; // maps names of groups to Groups 
 	private HashMap<String,Person> people; // maps names of people People
 	private HashMap<String,Meeting> meetings; // maps names of meetings to Meetings
 	private String name;
 	private String adminName; // the name of the admin of this Scheddar
 	public ScheddarXML sxml;
 	private String dest;
 
 	private String username; // the username of the admin's email account
 	private String password; // the password of the admin's email account
 	
 	private EmailParser emailParser;
 	
 	// The number of days in advance that a meeting will be finalized
 	
 	private int daysBeforeMeetingFinalized = 3;
 	
 	
     Scheddar(String dest){
 		
 		//Instantiate the hashMaps
 		people = new HashMap<String, Person>();
 		groups = new HashMap<String, Group>();
 		meetings = new HashMap<String, Meeting>();
 		
 		//Initialize the destination and the XML file
 		this.dest = dest;
 		this.sxml = new ScheddarXML(this);
 		
 		//Adding meetings to allMeetings just to test the meetings parser
 		ScheddarTime tff = new ScheddarTime(14,30, 2, 0, 12,10, 2012, false);
 		ArrayList<ScheddarTime> nl = new ArrayList<ScheddarTime>();
 		nl.add(tff);
 		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
 		hm.put(1,1.0);
 		ArrayList<String> gi = new ArrayList<String>();
 		gi.add("noone");
 		Meeting m = new Meeting("meeting1", false, tff, tff, nl, hm, gi, "blabla");
 		meetings.put("meeting1", m);
 		
 		//Finally, load the file contents from the XML location
 		sxml.makeDBFromXML(dest);
 		
 		System.out.println("allPersons has "+ people.size() + " members");
 		System.out.println("allGroups has "+ groups.size() + " members");
 		
 	}
 	
 	
 	/**
 	 * Hi, I implemented a constructor and getRootGroup() to work with my group tree.
 	 * Feel free to reimplement if you want it done differently. 
 	 * 
 	 * This one constructs a completely new Scheddar
 	 * ~sdemane
 	 * 
 	 * 
 	 * @param name
 	 */
 	
 	public Scheddar(String rootGroupName, String adminName, String username, String password) {
 		this.name = rootGroupName;
 		groups = new HashMap<String,Group>();
 		people = new HashMap<String,Person>();
 		meetings = new HashMap<String,Meeting>();
 		addGroup(new Group(rootGroupName));
 		this.adminName = adminName;
 		this.username = username;
 		this.password = password;
 		this.emailParser = new EmailParser(username,password);
 	}
 	
 	/**
 	 * Construct a Scheddar from an XML file
 	 * 
 	 * Ask Prateek what data structure represents an XML file
 	 */
 	
 	
 	//TODO : Make this a constructor for loading XML
 
 	public Scheddar(File xmlFile){
 		
 	}
 	
 	/**
 	 * @return The root group has the same name as the overall scheddar project, and contains all other groups and members
 	 */
 	public Group getRootGroup() {
 		return groups.get(name);
 	}
 	
 	public String getAdminName(){
 		return this.adminName;
 	}
 	
 	public String getDest(){
 		return this.dest;
 	}
 	
 	/**
 	 * Adds a new person to the Hashmap of people
 	 * 
 	 * @param p the person to add to the Hashmap
 	 */
 	
 	public void addPerson(Person p){
 		this.people.put(p.getFullName(),p);
 	}
 	
 	/** 
 	 * Adds a group to the Hashmap of groups
 	 * 
 	 * @param g the group to add to the hashmap
 	 */
 	
 	public void addGroup(Group g){
 		this.groups.put(g.getName(),g);
 	}
 	
 	public Group getGroup(String name) {
 		return groups.get(name);
 	}
 	
 	public Person getPerson(String name) {
 		return people.get(name);
 	}
 	
 	public void setGroups(HashMap<String,Group> groups){
 		this.groups = groups;
 	}
 	
 	public void setPeople(HashMap<String,Person> people){
 		this.people = people;
 	}
 	
 	public void setMeetings(HashMap<String,Meeting> meetings){
 		this.meetings = meetings;
 	}
 	
 	/**
 	 * Adds a meeting to the Hashmap of meetings
 	 * 
 	 * @param m the meeting to add
 	 */
 	
 	public void addMeeting(Meeting m){
 		this.meetings.put(m.getName(),m);
 	}
 	
 	/**
 	 * Removes a person from the HashMap of people
 	 * 
 	 * @param name the person's name to remove
 	 */
 	
 	public void removePerson(String name){
 		this.people.remove(name);
 	}
 	
 	/**
 	 * Removes a group from the Hashmap of groups
 	 * 
 	 * @param name the name of the group to remove
 	 */
 	
 	public void removeGroup(String name){
 		this.groups.remove(name);
 	}
 	
 	/**
 	 * Removes a meeting from the hashmap of meetings
 	 * 
 	 * @param name the name of the meeting to remove
 	 */
 	
 	public void removeMeeting(String name){
 		this.meetings.remove(name);
 	}
 	
 	/**
 	 * Returns a List of the email addresses of everyone in a group.
 	 * 
 	 * @param name the name of the group to get the emails of
 	 */
 	
 	public List<String> getGroupEmails(String name){
 		List<String> emailList = new LinkedList<String>();
 		Group g = groups.get(name);
 		if(g==null){
 			System.out.println("That group does not exist");
 			return emailList;
 		}
 		else{
 			List<Person> groupMembers = g.getMembers();
 			for(Person p : groupMembers){
 				emailList.add(p.getEmail());
 			}
 			return emailList;
 		}
 	}
 	
 	/**
 	 * Save all the data in the corresponding ScheddarXML file
 	 */
 	public void saveData(){
 		sxml.saveLocalDataToXML();	
 	}
 	
 	/**
 	 * Return all Meetings that have a proposed time in a given month
 	 * 
 	 * @param month the number of the month
 	 * @param year the year number
 	 * @return a list of all Meetings in that month
 	 */
 	
 	public List<Meeting> monthMeetings(int month, int year){
 		List<Meeting> meetingList = new LinkedList<Meeting>();
 		for(Meeting m : this.meetings.values()){
 			for(ScheddarTime t : m.getProposedTimes()){
 			    if(t.getMonth()==month && t.getYear()==year){
 				    meetingList.add(m);
 				    break;
 			    }
 			}
 		}
 		return meetingList;
 	}
 	
 	/**
 	 * Return all Meetings that are occurring in a given week
 	 * 
 	 * @param day the start day of the week
 	 * @param month the starting month of the week
 	 * @param year the starting month of the week
 	 * @return a list of all Meetings in a week
 	 */
 	
 	public List<Meeting> weekMeetings(int day, int month, int year){
 		return null;
 	}
 	
 	
 	/**
 	 * Return all Meetings that have a proposed time in a given day
 	 * 
 	 * @param day the day number
 	 * @param month the month number
 	 * @param year the year number
 	 * @return a list of all the Meetings occurring on that day
 	 */
 	
 	public List<Meeting> dayMeetings(int day, int month, int year){
 		List<Meeting> meetingList = new LinkedList<Meeting>();
 		for(Meeting m : this.meetings.values()){
 			for(ScheddarTime t : m.getProposedTimes()){
 				if(t.getDay()==day && t.getMonth()==month && t.getYear()==year){
 					meetingList.add(m);
 					break;
 				}
 			}
 		}
 		return meetingList;
 	}
 
 	/**
 	 * Interface methods
 	 */
 	
 	/**
 	 * Getter for groups
 	 * @return
 	 */
 	public HashMap<String, Group> getGroups(){
 		return groups;
 	}
 	
 	
 	/**
 	 * Getter for persons
 	 * @return
 	 */
 	public HashMap<String, Person> getPersons(){
 		return people;
 	}
 	
 	/**
 	 * Getter for meetings
 	 * @return
 	 */
 	public HashMap<String, Meeting> getMeetings(){
 		return meetings;
 	}
 	
 	@Override
 	public Person getPersonFromName(String personName) {
 		return this.people.get(personName);
 	}
 
 	@Override
 	public Group getGroupFromName(String groupName) {
 		return this.groups.get(groupName);
 	}
 
 	@Override
 	public Meeting getMeetingFromName(String meetingName) {
 		return this.meetings.get(meetingName);
 	}
 
 	@Override
 	public String getPersonEmail(String personName) {
 		return this.people.get(personName).getEmail();
 	}
 
 	@Override
 	public String getPersonPhoneNum(String personName) {
 		return this.people.get(personName).getPhoneNum();
 	}
 
 	@Override
 	public String getPersonDescription(String personName) {
 		return this.people.get(personName).getDescription();
 	}
 
 	@Override
 	public List<String> getMemberNames(String groupName) {
 		return this.groups.get(groupName).getPeopleFullNamesInGroup();
 	}
 
 	@Override
 	public List<String> getSubgroupNames(String groupName) {
 		List<Group> subgroups = this.groups.get(groupName).getSubgroups();
 		List<String> subgroupNames = new LinkedList<String>();
 		for(Group g : subgroups){
 			subgroupNames.add(g.getName());
 		}
 		return subgroupNames;
 	}
 
 	@Override
 	public List<Group> getSubgroups(String name) {
 		return this.groups.get(name).getSubgroups();
 	}
 	
 	/**
 	 * Returns the name of a group's parent group if one exists.
 	 * If not, returns "".
 	 */
 	
 	@Override
 	public String getParentGroupName(String name) {
 		Group parentGroup = this.groups.get(name).getParentGroup();
 		if(parentGroup==null){
 			return "";
 		}
 		else{
 			return parentGroup.getName();
 		}
 	}
 
 	@Override
 	public Group getParentGroup(String name) {
 		return this.groups.get(name).getParentGroup();
 	}
 	
 	/**
 	 * Returns a list of the people only in the main group of the given
 	 * group name and not in any of its subgroups
 	 */
 	
 	@Override
 	public List<String> getPeopleOnlyMainGroup(String name) {
 		List<String> memberNames = this.groups.get(name).getPeopleFullNamesInGroup();
 		List<Group> subgroups = getSubgroups(name);
 		for(Group g : subgroups){
 			List<String> subgroupMemberNames = g.getPeopleFullNamesInGroup();
 			for(String n : subgroupMemberNames){
 				memberNames.remove(n);
 			}
 		}
 		return memberNames;
 	}
 	
 	/**
 	 * Return a Collection of all the people in this Scheddar instance
 	 */
 	
 	public Collection<Person> getAllPeople(){
 		return this.people.values();
 	}
 	
 	/**
 	 * Return a Collection of all the groups in this Scheddar instance
 	 */
 	
 	public Collection<Group> getAllGroups(){
 		return this.groups.values();
 	}
 	
 	/**
 	 * Return a Collection of all the meetings in this Scheddar instance
 	 */
 	
 	public Collection<Meeting> getAllMeetings(){
 		return this.meetings.values();
 	}
 	
 	/**
 	 * This algorithm gives the admin a ranking of proposed times for a meeting based
 	 * upon the recurring conflicts of people in the groups involved in that meeting. This
 	 * should be run after an admin puts in some suggested times for a meeting, display
 	 * the ratings to the admin, and then give them the choice to change some of the proposed
 	 * times if they were not to their liking
 	 * 
 	 * @param proposedTimes a list of the times that the admin is proposing
 	 * @return an array containing the preliminary ratings for each time, indexes same as list of proposedTimes
 	 */
 	
 	public double[] getRecurringRankings(Meeting m){
 		List<ScheddarTime> timeList = m.getProposedTimes();
 		double[] ratings = new double[timeList.size()];
 		for(int i=0;i<timeList.size();i++){
 			ScheddarTime currTime = timeList.get(i);
 			double currScore = 0.0; // keep track of weights of people with conflicts
 			double totalWeight = 0.0; // keep track of the total weight of people in the groups for normalizing results
 			
 			for(Group g : m.getGroupsInvolved()){
 				for(Person p : g.getMembers()){
 					totalWeight += g.getMemberRanking(p);
 					List<ScheddarTime> conflicts = p.getConflicts();
 					for(ScheddarTime t : conflicts){
 						if(t.getDayOfWeek()==currTime.getDayOfWeek()){
 							t.setDay(currTime.getDay());
 							t.setMonth(currTime.getMonth());
 							t.setYear(currTime.getYear());
 							if(UsefulMethods.doTimesConflict(t,currTime)){
 								currScore += g.getMemberRanking(p);
 								break;
 							}
 						}
 					}
 				}
 			}
 			
 			ratings[i] = ((totalWeight - currScore) / totalWeight) * 100; // No conflicts gives a score 0f 100, all conflicts gives score of 0
 		}
 		return ratings;
 	}
 	
 	/**
 	 * This method reads all unread emails and updates values within this Scheddar accordingly
 	 */
 	
 	public void readAndParseEmails(){
 		List<Triple<String,String,String>> emailTriples = emailParser.getEmailTriples();
 		for(Triple<String,String,String> emailTriple : emailTriples){
 			parseAndRespondEmail(emailTriple);
 		}
 	}
 	
 	/**
 	 * Handles parsing for a single email triple
 	 * @param emailTriple the email triple to be parsed
 	 */
 	
 	public void parseAndRespondEmail(Triple<String,String,String> emailTriple){
 		String subject = emailTriple.x; // the subject of the email that was received
 		String body = emailTriple.y; // the body of the email that was received
 		String address = emailTriple.z; // the address from which the email was sent (set to "" if could not get for some reason)
 		
 		String[] subjectSpl = subject.split(" ");
 		String[] bodySpl = body.split(" ");
 
 		if(subjectSpl.length<3){
 			emailParser.sendSubjectErrorEmail(address, subject);
 			return;
 		}
 
 		String personName = (subjectSpl[0] + " " + subjectSpl[1]);
 
 		if(!this.people.containsKey(personName)){
 			emailParser.sendSubjectErrorEmail(address, subject);
 			return;
 		}
 		if(subjectSpl[2].equals("Conflicts")){ // we are parsing a conflicts email
 			try{
 				for(int i=0;i<bodySpl.length-1;i+=2){
 					String dayAbbrev = bodySpl[i];
 					int dayOfWeek = -1;
 					if(dayAbbrev.equals("Sun")) dayOfWeek = 0;
 					if(dayAbbrev.equals("Mon")) dayOfWeek = 1;
 					if(dayAbbrev.equals("Tue")) dayOfWeek = 2;
 					if(dayAbbrev.equals("Wed")) dayOfWeek = 3;
 					if(dayAbbrev.equals("Thu")) dayOfWeek = 4;
 					if(dayAbbrev.equals("Fri")) dayOfWeek = 5;
 					if(dayAbbrev.equals("Sat")) dayOfWeek = 6;
 					String timeRange = bodySpl[i+1];
 					int startHour = -1;
 					int startMinutes = -1;
 					int duration = -1;
 					String[] dashSpl = timeRange.split("-");
 					String[] timeSpl1 = dashSpl[0].split(":");
 					String[] timeSpl2 = dashSpl[1].split(":");
 					startHour = Integer.parseInt(timeSpl1[0]);
 					startMinutes = Integer.parseInt(timeSpl1[1]);
 					duration = Integer.parseInt(timeSpl2[0]) * 60 + Integer.parseInt(timeSpl2[1]) - (startHour * 60) - startMinutes;
 					this.people.get(personName).addConflict(new ScheddarTime(startHour,startMinutes,duration,dayOfWeek,0,0,0,true));
 				}
 			} catch (Exception e){
 				emailParser.sendInvalidBodyEmail(address, body);
 				return;
 			}
 		}
 		else if(subjectSpl[2].equals("MeetingTimes")){ // we are parsing a MeetingTimes email
 			try{
 				String meetingName = "";
 				for(int i=3;i<subjectSpl.length;i++){
 					meetingName += subjectSpl[i];
 					if(i!=subjectSpl.length-1){
 						meetingName += " ";
 					}
 				}
 				Meeting m = meetings.get(meetingName); // the meeting the person responded to
 				Person p = people.get(personName);
 				double importance = m.getPersonImportance(p);
 				for(String index : bodySpl){
 					int ind = Integer.parseInt(index);
 					m.updateScore(ind,importance);
 				}
 			} catch (Exception e) {
 				emailParser.sendInvalidBodyEmail(address, body);
 				return;
 			}
 		}
 		else{
 			emailParser.sendSubjectErrorEmail(address, subject);
 			return;
 		}
 	}
 	
 
 	/**
 	 * Interface email sending methods
 	 */
 	
 	public void sendFinalizedMeetingEmail(String toEmail, String name, Meeting meeting) {
 		emailParser.sendFinalizedMeetingEmail(toEmail,name,meeting);
 	}
 	
 	
 	public void sendAddedPersonEmail(String toEmail, String adminName, String personName, List<String> groupList) {
 		emailParser.sendAddedPersonEmail(toEmail,adminName,personName,groupList);
 	}
 	
 	
 	public void sendMeetingRequestEmail(String toEmail, String personName, Meeting meeting) {
 		emailParser.sendMeetingRequestEmail(toEmail, personName, meeting);
 	}
 }
