 package intnet13.project.contacts;
 
 import java.io.*;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class DatabaseClient {
 	private PrintWriter output;
 	private BufferedReader input;
 	private Socket s;
 	private String host, user, password;
 	private int port;
 	private HashMap<String, String[]> contacts;
 	private HashMap<String, String[]> groups;
 	private HashMap<String, ArrayList<String>> contacts_in_group;
 	
 	public DatabaseClient(String host, int port, String user, String password) {
 		this.host = host;
 		this.port = port;
 		this.user = user;
 		this.password = password;
 		init();
 	}
 	private void init() {
 		contacts = new HashMap<String, String[]>();
 		groups = new HashMap<String, String[]>();
 		contacts_in_group = new HashMap<String, ArrayList<String>>();		
 		authenticate();
 		loadContacts();
 	}
 	
 	private boolean authenticate() {
 		int[] repsonse;
 		repsonse = query("1", null);
 		if (repsonse[0]== 1) {
 			System.out.println("Authenticated!");
 			return true;
 		}
 		System.out.println("Access denied!");
 		return false;
 	}
 	
 	private boolean loadGroups() {	
 		int[] response;
 		response = query("6", null);
 		if(response[0] != 1) {
 			System.out.println("Data request failed");
 			if(response[0] == 3)
 				System.out.println("Access denied: Invalid user and/or password!");
 			return false;
 		}		
 		for (int i = 0; i<response[1]/3; i++) {
 			try {
 				String g_name = input.readLine();
 				String g_desc = input.readLine();
 				String g_id = input.readLine();
 				addGroup(g_name, g_desc, g_id);
 			} 
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return true;
 	}
 	
 	private void loadContacts() {
 		if(!loadGroups()) //Reqest to get groups failed
 			return;
 		int[] response;
 		response = query("2", null);
 		if(response[0] != 1) {
 			System.out.println("Data request failed");
 			if(response[0] == 3)
 				System.out.println("Access denied: Invalid user and/or password!");
 			return;
 		}		
 		for (int i = 0; i<response[1]/5; i++) {
 			try {
 				String c_name = input.readLine();
 				String c_phone = input.readLine();
 				String c_email = input.readLine();
 				String c_id = input.readLine();
 				String g_name = input.readLine();
 				addContact(c_name, c_phone, c_email, c_id);
 				addContactInGroup(g_name, c_name);
 			} 
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void addContact(String name, String phone, String email, String id) {
 		// If contact is new
 		if(!contacts.containsKey(name)) {
 			String[] arr = new String[3];
 			arr[0] = phone;
 			arr[1] = email;
 			arr[2] = id;
 			contacts.put(name, arr);
 		}
 	}
 	
 	private void addGroup(String name, String desc, String id) {
 		// If group is new
 		if(!groups.containsKey(name)) {
 			String[] arr = new String[2];
 			arr[0] = desc;
 			arr[1] = id;
 			groups.put(name, arr);
 		}
 	}
 	
 	private void addContactInGroup(String g_name, String c_name) {
 		ArrayList<String> arr;
 		// If group is new
 		if(!contacts_in_group.containsKey(g_name)) {
 			arr = new ArrayList<String>();
 			arr.add(c_name);
 			contacts_in_group.put(g_name, arr);
 		}
 		else {
 			arr = contacts_in_group.get(g_name);
 			arr.add(c_name);
 		}
 	}
 	
 	public boolean removeContact(String name) {
 		if(!contacts.containsKey(name)) {
 			System.out.println("Contact doesn't exist!");
 			return false;
 		}
 		int[] response;
 		response = query("3",  contacts.get(name));
 		if(response[0] == 1) {
 			contacts.remove(name);
 			// Remove contact's membership in all groups
 			Iterator at = contacts_in_group.entrySet().iterator();
 			ArrayList<String> contact_in_group;
 			String current;
 		    while (at.hasNext()) {
 		        Map.Entry pairs = (Map.Entry)at.next();
 		        contact_in_group = (ArrayList<String>) pairs.getValue();
 		        for(int i = 0; i<contact_in_group.size(); i++) {
 		        	current = contact_in_group.get(i);
 		        	if(current.equals(name))
 		        		contact_in_group.remove(i);
 		        }
 		    }
 		}
 		else {
 			System.out.println("Connection to db failed");
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean saveGroup(String[] options) {
 		if(groups.containsKey(options[0])) {
 			System.out.println("Failed to save group: Group already exists");
 			return false;
 		}
 		int[] response;
 		response = query("5", options);
 		if (response[0] != 1) {
 			System.out.println("Failed to save group to external db");
 			return false;
 		}
 		// response[1] = new group_id
 		addGroup(options[0], options[1], Integer.toString(response[1]));
 		return true;
 	}
 	
 	public boolean saveContact(String contactName, String phoneNumber, String email,
 			String group) {
 		if(!groups.containsKey(group)) {
 			String[] options = new String[2];
 			options[0] = group;
 			options[1] = group + " gruppen";
 			if(!saveGroup(options))
 				return false;
 		}
 		String g_id = groups.get(group)[1];
 		String[] options = new String[4];
 		options[0] = contactName;
 		options[1] = phoneNumber;
 		options[2] = email;
 		options[3] = g_id;
 		int[] response;
 		response = query("4",  options);
 		if(response[0] != 1) {
 			System.out.println("Failed to save contact to external db");
 			return false;
 		}
 		addContact(contactName, phoneNumber, email, Integer.toString(response[1]));
 		addContactInGroup(group, contactName);
 		return true;
 	}
 	
 	private void debugPrint() {
 		System.out.println("*** User_groups ***");
 		Iterator it = groups.entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)it.next();
 	        String [] i = (String[]) pairs.getValue();
 	        System.out.println(pairs.getKey() + " " + i[0] + " " + i[1]);
 	    }
 	    System.out.println("\n*** Contacts ***");
 	    Iterator ut = contacts.entrySet().iterator();
 	    while (ut.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)ut.next();
 	        String [] info = (String[]) pairs.getValue();
 	        System.out.println(pairs.getKey() + " " + info[0] + " " + info[1] + " " + info[2]);
 	    } 
 	    System.out.println("\n*** Contacts_in_group ***");
 	    Iterator at = contacts_in_group.entrySet().iterator();
 	    while (at.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)at.next();
 	        ArrayList<String> info = (ArrayList<String>) pairs.getValue();
 	        System.out.println(pairs.getKey());
 	        for(String a: info)
 	        	System.out.println(a);
 	        System.out.println();
 	    }
 	}
 	
 	private void openConnection() {
 		try {
 	    	s = new Socket(host, port);
 			output = new PrintWriter(s.getOutputStream());
 			input = new BufferedReader(new InputStreamReader(s.getInputStream()));
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void closeConnection() {
 		try {
 			output.close();
 			input.close();
 			s.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int[] query(String type, String[] options) {
 		openConnection();
 		output.println(type);
 		output.println(user);
 		output.println(password);
 		int i = Integer.parseInt(type);
 		switch (i) {
 			case 3: //Remove contact [contact_id]
 				output.println(options[2]);
 				break;
 			case 4: //Add contact [name, phone, email, group_id]
 				output.println(options[0]);
 				output.println(options[1]);
 				output.println(options[2]);
 				output.println(options[3]);
 				break;
 			case 5:
 				output.println(options[0]);
 				output.println(options[1]);
 				break;
 			default:
 				break;
 		}
 		output.flush();
 		return receiveMessage();
 	}
 	
 	private int[] receiveMessage() {
 		int[] response = new int[2];
 		response[0] = -1;
 		response[1] = -1;
 		try {
 			response[0] = Integer.parseInt(input.readLine());
 			response[1] = Integer.parseInt(input.readLine());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		//System.out.println("Status: " + response[0] + "\nRows: " + response[1]);
 		return response;
 	}
 
 	
 	public static void main(String[] args) throws Exception{
     	String host = args[0];
 		int port = Integer.parseInt(args[1]);
 		String user = args[2];
 		String password = args[3];
     	new DatabaseClient(host, port, user, password);
     }
 	
 	public String[] getGroups() {
 		String[] res = new String[groups.size()+1];
 		Iterator it = groups.entrySet().iterator();
 		res[0] = "Alla";
 		int i = 1;
 	    while (it.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)it.next();
 	        res[i] = (String) pairs.getKey();
 	        i++;
 	    }
 		return res;
 	}
 	
 	public String[] getContacts() {
 		String[] res = new String[contacts.size()];
 		Iterator it = contacts.entrySet().iterator();
 		int i = 0;
 	    while (it.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)it.next();
 	        res[i] = (String) pairs.getKey();
 	        i++;
 	    }
 		return res;
 	}
 	public String[] search(String contactName) {
 		if(contacts.containsKey(contactName))
 			return new String[]{contactName};
 		return new String[]{""};		
 	}
 	
 	public String[] getByGroup(String groupName) {
 		// Done
 		if(groupName.equals("Alla"))
 			return getContacts();
 		//String[] temp = {groupName+"1", groupName+"2", groupName+"3"};
 		//return temp;
 		if(!groups.containsKey(groupName))
 			return new String[]{""};
 		ArrayList<String> groupMembers = contacts_in_group.get(groupName);
 		String[] res = new String[groupMembers.size()];
 		for (int i = 0; i<groupMembers.size(); i++) {
 			res[i] = groupMembers.get(i);
 		}
 		return res;
 	}
 	
 
 	public String[] getContactInfo(String contactName) {
 		if(!contacts.containsKey(contactName))
 			return new String[]{""};
 		String[] info = contacts.get(contactName);
		String[] res = new String[info.length+1];
 		res[0] = contactName;
 		res[1] = info[0];
 		res[2] = info[1];
 		//return new String[]{contactName, "070-0707070", "test@noob.com"};
 		return res;
 	}
 }
