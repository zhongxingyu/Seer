 /*******************************************************************************
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package edu.upenn.mkse212.server;
 
 import edu.upenn.mkse212.IKeyValueStorage;
 import edu.upenn.mkse212.KeyValueStoreFactory;
 import edu.upenn.mkse212.client.Database;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.amazonaws.services.simpledb.*;
 import com.amazonaws.services.simpledb.model.*;
 import com.amazonaws.auth.*;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class DatabaseImpl extends RemoteServiceServlet implements Database {
 	
 	
 	private AmazonSimpleDBClient db;
 	
 	public DatabaseImpl() {
 		String userID = "AKIAJQV2RJ5OZMECVPQA";
 		String authKey = "PAlrjITfdQ+RmNxGpqfV8MXOqxSEf8ZLSJoNwA4w";
 		db = new AmazonSimpleDBClient(new BasicAWSCredentials(userID, authKey));
 	} 
 	
 	//checks to see if username already exists
 	//if not, creates a new entry in the user database with info
 	//and a new entry in the password database with the (hashed) password
 	//adds user to online in database
 	public boolean addUser(String username, String password, String firstName, 
 			String lastName, String email, String network, String interests,
 			String birthday) {
 		String time = String.valueOf(System.currentTimeMillis());
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest("users", username));
 		List<Attribute> attributesList = result.getAttributes();
 		//checks if username already exists
 		for (Attribute a : attributesList) {
 			if (a.getName().equals(username)) {
 				return new Boolean(false);
 			}
 		}
 		//creates a list with all attributes to be put into the database
 		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
 		list.add(new ReplaceableAttribute("firstName", ""+firstName,false));
 		list.add(new ReplaceableAttribute("lastName", ""+lastName,false));
 		list.add(new ReplaceableAttribute("email", ""+email,false));
 		list.add(new ReplaceableAttribute("network", ""+network,false));
 		list.add(new ReplaceableAttribute("interests", ""+interests, true));
 		list.add(new ReplaceableAttribute("birthday", ""+birthday,false));
 		String friends = "";
 		list.add(new ReplaceableAttribute("friends", ""+friends, true));
 		list.add(new ReplaceableAttribute("lastLogin", ""+time,true));
 		list.add(new ReplaceableAttribute("numLogins", "1",true));
 		
 		db.putAttributes(new PutAttributesRequest("users", username, list, 
 				new UpdateCondition()));
 		
 		//puts hashed password into passwords database
 		List<ReplaceableAttribute> l = new ArrayList<ReplaceableAttribute>();
 		l.add(new ReplaceableAttribute("password",
 				""+String.valueOf(password.hashCode()),true));
 		db.putAttributes(new PutAttributesRequest("passwords", username, l,
 				new UpdateCondition()));
 		
 		//puts user in online database
 		List<ReplaceableAttribute> l1 = new ArrayList<ReplaceableAttribute>();
 		l1.add(new ReplaceableAttribute("timestamp", String.valueOf(time), true));
 		db.putAttributes(new PutAttributesRequest("online", username, l1,
 				new UpdateCondition()));
 		return new Boolean(true);
 	}
 	
 	public String getNetwork(String username) {
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest("users",username));
 		List<Attribute> aList = result.getAttributes();
 		for (Attribute a : aList) {
 			if (a.getName().equals("network")) {
 				return a.getValue();
 			}
 		}
 		return null;
 	}
 	
 	//updates both the user's list of  as well as the other user's
 	//list of friends
 	//and adds both to friends database
 	public boolean addFriend(String username, String otherUsername) {
 		//populates users' store of friends
 		updateOnline(username);
 		String time = String.valueOf(System.currentTimeMillis());
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest("users", username));
 		List<Attribute> attributeList = result.getAttributes();
 		StringBuffer buff = new StringBuffer();
 		String friend = "";
 		Boolean check1 = false;
 		Boolean check2 = false;
 		for (Attribute a : attributeList) {
 			if (a.getName().equals("friends")) {
 				buff.append(otherUsername+"~"+a.getValue());
 				buff.deleteCharAt(buff.length()-1);
 				//friend+=a.getValue()+"~";
 				//friend+=otherUsername;
 				friend = buff.toString();
 				List<ReplaceableAttribute> li = 
 						new LinkedList<ReplaceableAttribute>();
 				li.add(new ReplaceableAttribute("friends",friend,true));
 				db.putAttributes(new PutAttributesRequest("users",username,li,
 						new UpdateCondition()));
 				check1 = true;
 			}
 		}
 		result = db.getAttributes(new GetAttributesRequest("users", 
 				otherUsername));
 		attributeList = result.getAttributes();
 		StringBuffer b = new StringBuffer();
 		String f = "";
 		for (Attribute a : attributeList) {
 			if (a.getName().equals("friends")) {
 				b.append(username+"~"+a.getValue());
 				buff.deleteCharAt(buff.length()-1);
 				//f+=a.getValue()+"~";
 				//f+=username;
 				f = b.toString();
 				List<ReplaceableAttribute> li = 
 						new LinkedList<ReplaceableAttribute>();
 				li.add(new ReplaceableAttribute("friends",friend,true));
 				db.putAttributes(new PutAttributesRequest("users",otherUsername,li,
 						new UpdateCondition()));
 				check2 = true;
 			}
 		}
 		
 		//populates friends database
 		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
 		List<ReplaceableAttribute> l = new ArrayList<ReplaceableAttribute>();
 		list.add(new ReplaceableAttribute("friend", ""+otherUsername,false));
 		list.add(new ReplaceableAttribute("timestamp", ""+time,false));
 		list.add(new ReplaceableAttribute("network",
 				""+getNetwork(username),false));
 		l.add(new ReplaceableAttribute("friend", ""+username,false));
 		l.add(new ReplaceableAttribute("timestamp", ""+time,false));
 		l.add(new ReplaceableAttribute("network",
 				""+getNetwork(otherUsername),false));
 		db.putAttributes(new PutAttributesRequest("friends", username, list,
 				new UpdateCondition()));
 		db.putAttributes(new PutAttributesRequest("friends", otherUsername, l,
 				new UpdateCondition()));
 		
 		return new Boolean(check1&&check2);
 	}
 	
 	//key posted to
 	//first column posted by
 	//add an update to the database if novel post
 	//else it just updates the associated text
 	//username = posted to
 	//otherUsername = posted by
 	public boolean addPost(String username, String otherUsername, String text) {
 		updateOnline(otherUsername);
 		String time = String.valueOf(System.currentTimeMillis());
 		List<ReplaceableAttribute> l = new ArrayList<ReplaceableAttribute>();
 		l.add(new ReplaceableAttribute("postID", ""+time,false));
 		db.putAttributes(new PutAttributesRequest("updates", username, l,
 				new UpdateCondition()));
 		
 		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
 
 		list.add(new ReplaceableAttribute("postedBy", ""+otherUsername,false));
 		list.add(new ReplaceableAttribute("post",""+text,false));
 		list.add(new ReplaceableAttribute("comments", "",true));
 		db.putAttributes(new PutAttributesRequest("posts", time, list,
 				new UpdateCondition()));
 		return true;
 	}
 	
 	//username passed the the user that is adding the comment
 	//postID implicitly tells us who it is posted to
 	public boolean addComment(String username, String text, String postID) {
 		updateOnline(username);
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest("posts",postID));
 		List<Attribute> l = result.getAttributes();
 		for(Attribute a : l) {
 			if (a.getName().equals("comments")) {
 				List<ReplaceableAttribute> li = 
 						new LinkedList<ReplaceableAttribute>();
 				li.add(new ReplaceableAttribute("comments", a.getValue() + 
 						username + "~" + text + "~~",true));
 				db.putAttributes(new PutAttributesRequest("posts", postID, li,
 						new UpdateCondition()));
 			}
 		}
 		return true;
 	}
 	
 	//returns a list where the first entry is the original post
 	//and each subsequent entry is the associated comments
 	public List<String> getPostAndComment(String postID) {
 		List<String> ret = new LinkedList<String>();
 		GetAttributesResult results = db.getAttributes(
 				new GetAttributesRequest("posts",postID));
 		List<Attribute> l = results.getAttributes();
 		ret.add(0, postID);
 		ret.add(1,"");
 		ret.add(2,"");
 		ret.add(3,"");
 		for (Attribute a : l) {
 			if (a.getName().equals("post")) {
 				ret.remove(2);
 				ret.add(2, a.getValue());
 			} else if (a.getName().equals("postedBy")) {
 				ret.remove(1);
 				ret.add(1, a.getValue());
 			} else if (a.getName().equals("comments")) {
 				ret.remove(3);
 				ret.add(3, a.getValue());
 			}
 		}
 		return ret;
 	}
 	
 	//returns things twice
 	//returns a list of strings representing different posts on a user's
 	//wall including status updates and comments
 	public List<List<String>> getWall(String username) {
 		updateOnline(username);
 		SelectResult r = db.select(new SelectRequest("select postID from " +
 				"updates where itemName() = '"+username+"' and postID is not " +
 				"null order by postID desc",true));
 		List<Item> item = r.getItems();
 		LinkedList<List<String>> ret = new LinkedList<List<String>>();
 		for (Item i : item) {
 			for (Attribute a : i.getAttributes()) {
 				if (a.getName().equals("postID")) {
 					ret.addFirst(getPostAndComment(a.getValue()));
 				}
 			}
 		}
 		return ret;
 	}
 	
 	//not totally correct, probably have to do something a little different 
 	public List<List<String>> getFeed(String username) {
 		updateOnline(username);
 		List<List<String>> ret = new LinkedList<List<String>>();
 		GetAttributesResult results = db.getAttributes(
 				new GetAttributesRequest("users",username));
 		List<Attribute> l = results.getAttributes();
 		List<String> friends = new ArrayList<String>();
 		StringBuffer buff = new StringBuffer();
 		buff.append("(");
 		for(Attribute a : l) {
 			if (a.getName().equals("friends")) {
 				String[] f = a.getValue().split("~");
 				for(String s : f) {
 					friends.add(s);
 					buff.append("'"+s+"',");
 				}
 			}
 		}
 		buff.deleteCharAt(buff.length()-1);
 		buff.append(")");
 		String string = buff.toString();
 		SelectResult sr = db.select(new SelectRequest("select postID from" +
 				" updates where itemName() in "+string+" and postID is " +
 						"not null order by postID desc",true));
 		List<Item> item = sr.getItems(); 
 		for (Item i : item) {
 			LinkedList<String> postIDs = new LinkedList<String>();
 			for (Attribute a : i.getAttributes()) {
 				if (a.getName().equals("postID")) {
 					postIDs.addFirst(a.getValue());
 				}
 			}
 				for (String s : postIDs) {
 					List<Attribute> alist = db.getAttributes(
 						new GetAttributesRequest("posts",s)).getAttributes();
 					List<String> lis = new LinkedList<String>();
 					lis.add(0,s);
 					lis.add(1,"");
 					lis.add(2,i.getName());
 					lis.add(3,"");
 					lis.add(4,"");
 					for (Attribute a2 : alist) {
 						if (a2.getName().equals("postedBy")) {
 							lis.remove(1);
 							lis.add(1,a2.getValue());
 						} else if (a2.getName().equals("post")) {
 							lis.remove(3);
 							lis.add(3,a2.getValue());
 						} else if (a2.getName().equals("comments")) {
 							lis.remove(4);
 							lis.add(4,a2.getValue());
 						}
 					}
 					ret.add(lis);
 				}
 		} 
 		System.out.println(ret.toString());
 		return ret;
 	}
 	
 	//method to be used throughout databaseimpl
 	//updates the timestamp for a user's latest activity
 	public void updateOnline(String username) {
 		long time = System.currentTimeMillis();
 		List<ReplaceableAttribute> l = new LinkedList<ReplaceableAttribute>();
 		l.add(new ReplaceableAttribute("timestamp", ""+String.valueOf(time),true));
 		db.putAttributes(new PutAttributesRequest("online", username, l, 
 				new UpdateCondition()));
 	}
 	
 	//returns a list of a user's friends that are currently online
 	//get list of friends and iterates over the list to see if there
 	//has been activity in the past 5 minutes
 	public List<String> getOnline(String username) {
 		List<String> online = new ArrayList<String>();
 		long time = System.currentTimeMillis();
 		GetAttributesResult results = db.getAttributes(
 				new GetAttributesRequest("friends",username));
 		List<Attribute> aList = results.getAttributes();
		String friends = "";
 		for (Attribute a : aList) {
			if (a.getName().equals("friends")) {
				friends+=a.getValue();
 			}
 		}
		String[] arr = friends.split("~");
		for (String s : arr) {
 			GetAttributesResult r = db.getAttributes(
 					new GetAttributesRequest("online",s));
 			List<Attribute> list = results.getAttributes();
 			for (Attribute a : list) {
 				if (a.getName().equals("timestamp")) {
 					long ts = Long.parseLong(a.getValue());
 					if ((time - ts) > 300000.0) {
 						online.add(s);
 					}
 				}
 			}
 		}
 		return online;
 	}
 	
 	//given the querrying user and their query, returns a set of strings
 	//representing the possible usernames they could be looking for
 	public Map<String,String> getSuggestions(String username) {
 		Map<String, String> map = new HashMap<String, String>();
 		GetAttributesResult results = db.getAttributes(
 				new GetAttributesRequest("users",username));
 		List<Attribute> list = results.getAttributes();
 		String friends = "";
 		for (Attribute a : list) {
 			if (a.getName().equals("friends")) {
 				friends+=a.getValue();
 			}
 		}
 		String[] f = friends.split("~");
 		for (String s : f) {
 			List<Attribute> l = db.getAttributes(
 					new GetAttributesRequest("users",s)).getAttributes();
 			String ret = "";
 			for (Attribute a : l) {
 				if (a.getName().equals("firstName")) {
 					ret = a.getValue() + " " + ret;
 				} else if (a.getName().equals("lastName")) {
 					ret = ret+a.getValue();
 				}
 				
 			}
 			map.put(ret, s);
 		}
 		return map;
 	}
 	
 	//allows the user the change what their interests are
 	public boolean updateInterests(String username, String update) {
 		updateOnline(username);
 		List<ReplaceableAttribute> l = new LinkedList<ReplaceableAttribute>();
 		l.add(new ReplaceableAttribute("interests",""+update,true));
 		db.putAttributes(new PutAttributesRequest("users",username,l,
 				new UpdateCondition()));
 		return false;
 	}
 	
 	
 	
 	//creates the file for adsorption
 	//gets all friendships from all users
 	public void getAllConnections() {
 		try {
 			BufferedWriter buff = new BufferedWriter(new FileWriter("adsorption.txt"));
 			SelectResult results = db.select(
 					new SelectRequest("select * from friends"));
 			List<Item> list = results.getItems();
 			for (Item i : list) {
 				List<Attribute> aList = i.getAttributes();
 				for (Attribute a : aList) {
 					if(a.getName().equals("friends")) {
 						buff.write(i.getName()+"\t"+a.getValue());
 						buff.newLine();
 					} else if (a.getName().equals("network")) {
 						buff.write(i.getName()+"\t"+a.getValue());
 					}
 				}
 			}
 			List<Item> item = db.select(
 					new SelectRequest("select interests from users",true)).getItems();
 			for (Item i : item) {
 				for (Attribute a : i.getAttributes()) {
 					if (a.getName().equals("interest")) {
 						String[] arr = a.getValue().split(",");
 						for (String s : arr) {
 							buff.write(i.getName()+"\t"+s);
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//creates file for use of visualization
 	//gets all friends of a user, and all people who are also in their network
 	public void visualizationFile(String username) {
 		try {
 			String network = "";
 			BufferedWriter buff = new BufferedWriter(new FileWriter("visualize.txt"));
 			GetAttributesResult r = db.getAttributes(
 					new GetAttributesRequest("users",username));
 			List<Attribute> l = r.getAttributes();
 			for (Attribute a : l) {
 				if (a.getName().equals("network")) {
 					network+=a.getValue();
 				}
 			}
 			GetAttributesResult req = db.getAttributes(
 					new GetAttributesRequest("friends",username));
 			List<Attribute> l2 = req.getAttributes();
 			for (Attribute a : l2) {
 				if (a.getName().equals("friend")) {
 					buff.write(username+"\t"+a.getValue());
 				}
 			}
 			SelectResult nResult = db.select(new SelectRequest
 					("select username from friends " +"where network = " +
 							"'"+network+"'"+"username !='"+username+"'"));
 			List<Item> ilist = nResult.getItems();
 			for (Item i : ilist) {
 				buff.write(username+"\t"+i.getName());
 				buff.newLine();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 	public Map<String,String> nameUsername() {
 		Map<String,String>nameUsername = new HashMap<String,String>();
 		SelectResult r = db.select(
 				new SelectRequest("select firstName, lastName from users"));
 		for (Item i : r.getItems()) {
 			String ret = "";
 			for (Attribute a : i.getAttributes()) {
 				if (a.getName().equals("firstName")) {
 					ret = a.getValue() + " " + ret;
 				} else if (a.getName().equals("lastName")) {
 					ret = ret + a.getValue();
 				}
 			}
 			nameUsername.put(ret, i.getName());
 		}
 		return nameUsername;
 	}
 	
 	public Map<String,String> usernameName() {
 		Map<String, String> usernameName = new HashMap<String, String>();
 		SelectResult r = db.select(
 				new SelectRequest("select firstName, lastName from users"));
 		for (Item i : r.getItems()) {
 			String ret = "";
 			for (Attribute a : i.getAttributes()) {
 				if (a.getName().equals("firstName")) {
 					ret = a.getValue() + " " + ret;
 				} else if (a.getName().equals("lastName")) {
 					ret = ret + a.getValue();
 				}
 			}
 			usernameName.put(i.getName(), ret);
 		}
 		return usernameName;
 	}
 	
 	
 	public Boolean isFriend(String username, String otherUsername) {
 		List<Item> item = db.select(new SelectRequest("select friends " +
				"from users where itemName() = "+username)).getItems();
 		for (Item i : item) {
 			for (Attribute a : i.getAttributes()) {
 				if (a.getName().equals("friends")) {
 					String[] f = a.getValue().split("~");
 					for (String s : f) {
 						if (otherUsername.equals(s)) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	//returns an array containing all of the specified user's info
 	public String[] getInfo(String username) {
 		String ret[] = new String[7];
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest("users",username));
 		List<Attribute> list = result.getAttributes();
 		for(Attribute a : list) {
 			if (a.getName().equals("firstName")) {
 				ret[0] = a.getValue();
 			} else if (a.getName().equals("lastName")) {
 				ret[1] = a.getValue();
 			} else if (a.getName().equals("email")) {
 				ret[2] = a.getValue();
 			} else if (a.getName().equals("network")) {
 				ret[3] = a.getValue();
 			} else if (a.getName().equals("birthday")) {
 				ret[5] = a.getValue();
 			} 
 		}
 		List<Item> l = db.select(new SelectRequest("select interests from users" +
 				" where itemName() = '"+username+"'",true)).getItems();
 		for (Item i : l) {
 			for (Attribute a : i.getAttributes()) {
 				if (a.getName().equals("interests")) {
 					ret[4] = a.getValue();
 				}
 			}
 		}
 		ret[6] = username;
 		return ret;
 	}
 	
 	
 	//updated given code to match implemented databases
 	//also adds user to online database
 	public boolean validateLogin(String username, String password) {
 		updateOnline(username);
 		List<ReplaceableAttribute> list = 
 				new ArrayList<ReplaceableAttribute>();
 		long time = System.currentTimeMillis();
 		list.add(new ReplaceableAttribute("timestamp", 
 				String.valueOf(time), true));
 		db.putAttributes(new PutAttributesRequest("online", 
 				username, list, new UpdateCondition()));
 		if (username.equals("ahae") || username.equals("susangr")) {
 			GetAttributesResult result = db.getAttributes(
 					new GetAttributesRequest("users", username));
 			List<Attribute> attributeList = result.getAttributes();
 			for (Attribute a : attributeList) {
 				if (a.getName().equals("password")) {
 					return new Boolean(a.getValue().equals(password));
 				}
 			}
 		} else {
 			GetAttributesResult result = db.getAttributes(
 					new GetAttributesRequest("passwords", username));
 			List<Attribute> attributeList = result.getAttributes();
 			for (Attribute a : attributeList) {
 				if (a.getName().equals("password")) {
 					return new Boolean(a.getValue().equals(
 							String.valueOf(password.hashCode())));
 				}
 			}
 		}
 		return new Boolean(false);
 	}
 	
 	//given code
 	public Integer incrementLogins(String username) {
 		GetAttributesResult result = db.getAttributes(
 			new GetAttributesRequest("users", username));
 		List<Attribute> attributeList = result.getAttributes();
 		int loginsSoFar = 0;
 		for (Attribute a : attributeList) {
 			if (a.getName().equals("loginsSoFar")) {
 				loginsSoFar = Integer.valueOf(a.getValue()).intValue();
 			}
 		}
 		loginsSoFar ++;
 		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
 		list.add(new ReplaceableAttribute("loginsSoFar", ""+loginsSoFar, false));
 		db.putAttributes(new PutAttributesRequest("users", username, list, 
 				new UpdateCondition()));
 		return new Integer(loginsSoFar);
 	}
 	
 	
 	public Set<String> getFriendsList(String username)
 			throws IllegalArgumentException {
 		Set<String> results = new HashSet<String>();
 		IKeyValueStorage socialGraph = KeyValueStoreFactory.getKeyValueStore(KeyValueStoreFactory.STORETYPE.BERKELEY, 
 			    "socialGraph", "/home/mkse212/bdb/", "user", "authKey", false);
 		results.addAll(socialGraph.get(username));
 		return results;
 	}
 	
 
 	
 	
 
 
 }
