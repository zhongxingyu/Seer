 package au.edu.adelaide.physics.opticsstatusboard;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.Jsoup;
 import org.jsoup.select.Elements;
 import org.jsoup.nodes.Element;
 
 import android.os.AsyncTask;
 import android.widget.ArrayAdapter;
 
 public class WebParser extends AsyncTask<URL, Void, ArrayList<Person>>{
 	private BufferedReader input;
 	private String data;
 	private Elements cols, rows;
 	private int numPeople, sortMode;
	private ArrayList<Person> people, newList;
 	private ArrayAdapter<Person> adapter;
 	private MainActivity activity;
 	private Person newUser;
 	private boolean loggedIn;
 	
 	public WebParser(ArrayList<Person> people, ArrayAdapter<Person> adapter, MainActivity activity, String sortMode) {
 		this.people = people;
 		this.adapter = adapter;
 		this.activity = activity;
 		newUser = null;
 		loggedIn = false;
 		this.sortMode = Integer.parseInt(sortMode.replace("\""," ").trim());
 	}
 	
 	public ArrayList<Person> fetchPeople() {
 		return people;
 	}
 	
 	protected ArrayList<Person> doInBackground(URL... website) {
 		try {
 			input = new BufferedReader((new InputStreamReader(website[0].openStream())));
 			input.readLine();
 			data = input.readLine();
 			input.close();
 		} catch (IOException e) {
 			System.out.println("IOException");
 			e.printStackTrace();
 		}
 		
 		if (data == null) {
 			return null;
 		} else {
 			parse();
 		}
 		
 		return getPeople();
 	}
 	
 	protected void onPreExecute() {
 		activity.setNetworking(true);
 		activity.disableRefreshButton();
 	}
 	
	protected void onPostExecute(ArrayList<Person> newList) {
		people.clear();
		people.addAll(newList);
 		
 		if (activity.getRetries() > 0 && activity.getUser() != null) {
 			if (newUser.equals(activity.getUser())) {
 				activity.setRetries(0);
 				activity.setNetworking(false);
 				sort();
 				activity.enableRefreshButton();
 				adapter.notifyDataSetChanged();
 			} else {
 				activity.decRetries();
 				activity.postData();
 			}
 		} else {
 			activity.setNetworking(false);
 			if (loggedIn) {
 				activity.setUser(newUser);
 				activity.setStatusButton(newUser.getStatus());
 			}
 			sort();
 			activity.enableRefreshButton();
 			adapter.notifyDataSetChanged();
 		}
 	}
 	
 	private void parse() {
 		Document parsedData = Jsoup.parse(data);
 	    Element mainBody = parsedData.body();
 
 	    rows = mainBody.getElementsByTag("tr");
 
 	    numPeople = rows.size() - 2;
 	}
 	
 	private ArrayList<Person> getPeople() {
 		//Clear the array for all the people
		newList = new ArrayList<Person>(numPeople);
 		
 		//Populate the array with new people
 		for (int j = 2; j < (numPeople+2); j++) {
 			Element row = rows.get(j);
 		    cols = row.getElementsByTag("td");
 		
 			//Get the name
 			String firstName = cols.get(3).getElementsByTag("font").html();
 			String lastName = cols.get(2).getElementsByTag("font").html();
 			
 			//Get the username
 			String username = cols.get(3).getElementsByTag("a").attr("onclick");
 			int usernameStartIndex = username.indexOf("name=") + 5;
 			int usernameEndIndex = username.indexOf("'", usernameStartIndex);
 			username = username.substring(usernameStartIndex, usernameEndIndex);
 
 			//Get the mobile number
 			String mob = cols.get(4).getElementsByTag("font").html();
 
 			//Get is they have a message left for them
 			boolean hasMessage = !cols.get(1).html().contains("&nbsp;");
 
 			//Get the email address
 			String email = "";
 			if (!cols.get(5).html().contains("&nbsp;")) {
 				String rawEmail = cols.get(5).getElementsByTag("a").outerHtml();
 				int eIndStart = rawEmail.indexOf("mailto:") + 7;
 				int eIndEnd = rawEmail.indexOf(">", eIndStart) - 1;
 				email = rawEmail.substring(eIndStart, eIndEnd);
 			}
 			
 
 			//Get the status as an integer (for the ListView to deal with)
 			int status = 1;
 			for (int i = 6; i < 12; i++) {
 				if (!cols.get(i).html().contains("&nbsp;")) {
 					status = i - 6;
 					break;
 				}
 			}
 
 			//Get the back message
 			String backMessage = "";
 			if (!cols.get(12).html().contains("&nbsp;")) {
 				backMessage = cols.get(12).getElementsByTag("font").html();
 			}
 
 			//Get the general message
 			String message = "";
 			if (!cols.get(13).html().contains("&nbsp;")) {
 				message = cols.get(13).getElementsByTag("font").html();
 			}
 			
 			Person newPerson = new Person(firstName, lastName, username, hasMessage, mob, email, status, backMessage, message);
 			if (username.equals(activity.getUsername())) {
 				newUser = newPerson;
 				loggedIn = true;
			} else {
				newList.add(newPerson);
 			}
 		}
 		
		return newList;
 	}
 	
 	private void sort() {
 		switch (sortMode) {
 		case 0: //Sort by what it is online
 			break; //So do nothing...
 		case 1: //Sort by first name
 			Collections.sort(people, new Comparator<Person>() {
 				@Override
 				public int compare(Person left, Person right) {
 					return left.getFirstName().compareTo(right.getFirstName());
 				}
 			});
 			break;
 		case 2: //Sort by last name
 			Collections.sort(people, new Comparator<Person>() {
 				@Override
 				public int compare(Person left, Person right) {
 					return left.getLastName().compareTo(right.getLastName());
 				}
 			});
 			break;
 		case 3: //Sort by in/out (in on top)
 			Collections.sort(people, new Comparator<Person>() {
 				@Override
 				public int compare(Person left, Person right) {
 					int leftStat = left.getStatus();
 					int rightStat = right.getStatus();
 					if (leftStat > 1)
 						leftStat = 1;
 					if (rightStat > 1)
 						rightStat = 1;
 					
 					if (leftStat == rightStat) {
 						return 0;
 					} else if (leftStat > rightStat) {
 						return 1;
 					} else {
 						return -1;
 					}
 				}
 			});
 			break;
 		}
 	}
 	
 	public void setSortMode(int sortMode) {
 		this.sortMode = sortMode;
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
