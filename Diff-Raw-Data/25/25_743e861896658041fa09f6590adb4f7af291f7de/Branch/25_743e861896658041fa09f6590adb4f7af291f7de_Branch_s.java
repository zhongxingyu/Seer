 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class Branch {
 	public String name;
 	String server;
 	int ServPort = 12345;
 	int GUIPort = 12345;
 
 	public HashMap<String, Branch> branches = new HashMap<String, Branch>();
         public HashMap<String, Branch> inNeighbors = new HashMap<String, Branch>();
         public HashMap<String, Branch> outNeighbors = new HashMap<String, Branch>();
  
 	Branch() {
 	}
 
 	Branch(Branch other) {
 		name = other.name;
 		server = other.server;
 		ServPort = other.ServPort;
 		GUIPort = other.GUIPort;
 	}
 
 	Branch(String s_name, int n_servport) {
 		name = s_name;
 		ServPort = n_servport;
 	}
 
 	Branch(String s_name, String s_server, int n_servport, int n_guiport) {
 		name = s_name;
 		server = s_server;
 		ServPort = n_servport;
 		GUIPort = n_guiport;
 	}
 
 	public String toString() {
 		return name+":"+server+":"+ServPort+":"+GUIPort;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public boolean equals(Object o) {
 		if (o instanceof Branch) {
 			Branch other = (Branch)o;
 			if (other.name.equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public int hashCode() {
 		return name.hashCode();
 	}
 
         public String getBranches() {
                 String result = "";
 
 		String outNeighborsStr = "";
 		String inNeighborsStr = "";
 
                 Iterator iterator = null;
 
                 for (Map.Entry<String, Branch> branch : inNeighbors.entrySet()) {
                         String key = branch.getKey();
                         Branch value = branch.getValue();
 
                         if (inNeighborsStr.length() > 0)
                                 inNeighborsStr = inNeighborsStr + ",";
                         inNeighborsStr = inNeighborsStr + key + "=" + value.ServPort;
                 }
 
                 for (Map.Entry<String, Branch> branch : outNeighbors.entrySet()) {
                         String key = branch.getKey();
                         Branch value = branch.getValue();
 
                         if (outNeighborsStr.length() > 0)
                                 outNeighborsStr = outNeighborsStr + ",";
                         outNeighborsStr = outNeighborsStr + key + "=" + value.ServPort;
                 }
 	
 		result = result + inNeighborsStr;	
 		if (inNeighborsStr.length() < 1)
 			result = result + "0";
 
 		result = result + " ";
 
 		result = result + outNeighborsStr;	
 		if (outNeighborsStr.length() < 1)
 			result = result + "0";
 		
                 return result;
         }
 
         public void addComm(Branch branch) {
                 if (branch == null || branches.containsKey(branch.name) || this.name == branch.name) {
                         System.out.println("Not adding link");
                         return;
                 }
 
                 System.out.println("Adding link from " + this.name + " to " + branch.name);
                 branches.put(branch.name, branch);
                 return;
         }
 
 
 
 
 	/**
 	 * Adds a branch that can be communicated with
 	 */
 	public void addOutEdge(Branch branch) {
 		if (branch == null || outNeighbors.containsKey(branch.name) || this.name == branch.name) {
 			System.out.println("Not adding link");
 			return;
 		}
 
 		System.out.println("Adding link from " + this.name + " to " + branch.name);
 		outNeighbors.put(branch.name, branch);
 		return;
 	}
 
 	/**
 	 * Adds a branch that can communicate with this branch
 	 */
 	public void addInEdge(Branch branch) {
 		if (branch == null || inNeighbors.containsKey(branch.name) || this.name == branch.name) {
 			System.out.println("Not adding link");
 			return;
 		}
 
 		System.out.println("Adding link from " + branch.name + " to " + this.name);
 		inNeighbors.put(branch.name, branch);
 		return;
 	}
 
 }
