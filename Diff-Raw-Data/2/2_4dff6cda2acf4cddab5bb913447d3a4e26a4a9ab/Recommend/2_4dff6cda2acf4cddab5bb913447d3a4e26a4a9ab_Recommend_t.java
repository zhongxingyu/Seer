 package github;
 
 import java.io.*;
 import java.util.*;
 import java.text.*;
 
 public class Recommend {
 
 	//public static int CONFIDENCE = 10;
 	public static int SUGGESTIONS = 10;
 	public static int MAX_THREADS = 2;
 
 	public Map<String, Repository> repositories = new HashMap<String, Repository>();
 	public Map<String, User> users = new HashMap<String, User>();
 	//public Map<String, User> watchingUsers = new HashMap<String, User>();
 	public Map<String, TestUser> testUsers = new HashMap<String, TestUser>();
 	public Map<String, List> languagesProject = new HashMap<String, List>();
 
 	// stats
 	public List<Repository> sortedRepositories;
 	public List<TestUser> sortedUsers;
 
 	public void loadRepos(String filename) throws IOException, ParseException {
 		System.out.print("Loading Repositories");
 		long start = System.nanoTime();
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new FileReader(filename));
 			String line;
 			int i = 0;
 			while ((line = in.readLine()) != null) {
 				Repository repo = Repository.parse(line);
 				if (i % 10000 == 0) System.out.print(".");
 				repositories.put(repo.id, repo);
 				i++;
 			}
 			System.out.print(i);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 
 	}
 
 	public void loadUsers(String filename) throws IOException {
 
 		System.out.print("Loading Watches");
 		long start = System.nanoTime();
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new FileReader(filename));
 			String line;
 			int i = 0;
 			while ((line = in.readLine()) != null) {
 				Watch watch = Watch.parse(line);
 				if (i % 25000 == 0) System.out.print(".");
 				User user = users.get(watch.user);
 				if (user == null) {
 					user = new User(watch.user);
 					users.put(watch.user, user);
 				}
 				Repository repo = repositories.get(watch.repo);
 				user.watch(repo);
 /*				if (user.watching.size() > CONFIDENCE) {
 					watchingUsers.put(watch.user, user);
 				}*/
 				i++;
 			}
 			System.out.print(i);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 
 	}
 
 	public void loadLanguages(String filename) throws IOException {
 		System.out.print("Loading Languages");
 		long start = System.nanoTime();
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new FileReader(filename));
 			String line;
 			int i = 0;
 			while ((line = in.readLine()) != null) {
 				Languages langs = Languages.parse(line);
 				if (i % 4500 == 0) System.out.print(".");
 				Repository repo = repositories.get(langs.repo);
 				if (repo == null) {
 					//System.err.println("Languages for an unknown repository: " + langs.repo);
 				} else {
 					repo.languages = langs.languages;
 				}
 
 				i++;
 			}
 			System.out.print(i);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 	}
 
 	public void loadTest(String filename) throws IOException, ParseException {
 		System.out.print("Loading Test users");
 		long start = System.nanoTime();
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new FileReader(filename));
 			String line;
 			int i = 0;
 			while ((line = in.readLine()) != null) {
 				String userId = User.parse(line);
 				if (i % 290 == 0) System.out.print(".");
 				User user = users.get(userId);
 				if (user == null) {
 					user = new TestUser(userId);
 					users.put(userId, user);
 					testUsers.put(userId, new TestUser(user));
 				} else {
 					testUsers.put(userId, new TestUser(user));
 				}
 				i++;
 			}
 			System.out.print(i);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 
 	}
 
 	public void compareUsers() throws InterruptedException {
 		System.out.print("Profiling users");
 		long start = System.nanoTime();
 		Iterator<TestUser> it = testUsers.values().iterator();
 		int i = 0;
 		Object monitor = new Object();
 		synchronized(monitor) {
 			ThreadGroup group = new ThreadGroup("Workers");
 			while (it.hasNext()) {
 				if (group.activeCount() >= MAX_THREADS) {
 					monitor.wait();
 				}
 				TestUser testUser = it.next();
 				if (testUser.watching.size() > 0) {
 					UserCompare c = new UserCompare(monitor, this, testUser);
 					new Thread(group, c, "Worker").start();
 				}
 				//compareUser(testUser);
 				if (i % 280 == 0) System.out.print(".");
 				i++;
 			}
 		}
 		System.out.print(i);
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 	}
 
 	private class UserCompare implements Runnable {
 
 		TestUser testUser;
 		Recommend rec;
 		Object monitor;
 		public UserCompare(Object monitor, Recommend rec, TestUser testUser) {
 			this.monitor = monitor;
 			this.rec = rec;
 			this.testUser = testUser;
 
 		}
 		public void run() {
 			rec.compareUser(testUser);
 			//System.out.println(testUser.id + ":" + testUser.bestScore + ":" + testUser.getWatches());
 			if (testUser.bestUser != null) {
 				System.out.println(testUser.id + ":" + testUser.bestUser.id + ":" + testUser.bestScore + ":" + testUser.alternateUser.size());
 			}
 			
 			synchronized(monitor) {
 				monitor.notify();
 			}
 		}
 
 	}
 
 	public void compareUser(TestUser testUser) {
 		Iterator<User> it = users.values().iterator();
 		while (it.hasNext()) {
 			User user = it.next();
 			if (user.equals(testUser)) {
 				continue;
 			}
 			int score = testUser.similarTo(user);
 					//System.out.println(testUser.id + " : " + score + " : " + user.id);
 			if (score == -1) {
 				// Our test user can't be profiled
 				return;
 			} else if (score > 0) {
 				if (score > testUser.bestScore) {
 					testUser.alternateUser.clear();
 					testUser.bestUser = user;
 					testUser.bestScore = score;
 					testUser.alternateUser.add(user);
 					//System.out.println("For " + testUser.id + " next score " + score + " user " + user.id);
 					//System.out.println("T:" + testUser + ":" + testUser.getWatches());
 					//System.out.println("U:" + testUser.bestUser + ":" + testUser.bestUser.getWatches());
 				} else if (score == testUser.bestScore) {
 					// Add these users to other best scores
 					testUser.alternateUser.add(user);
 				}
 			}
 		}
 	}
 
 	public void saveProfiles(String filename) throws IOException {
 		System.out.print("Saving profiles");
 		long start = System.nanoTime();
 		BufferedWriter out = null;
 		try {
 			out = new BufferedWriter(new FileWriter(filename));
 			String line;
 			int i = 0;
 			Iterator<TestUser> it = testUsers.values().iterator();
 			while (it.hasNext()) {
 				TestUser testUser = it.next();
 				String userId = testUser.id;
 				out.write(userId);
 				if (testUser.bestUser != null) {
 					out.write(":" + testUser.bestUser.id + ":" + testUser.bestScore + testUser.getAlternates());
 				}
 				out.newLine();
 				if (i % 290 == 0) System.out.print(".");
 				i++;
 			}
 			System.out.print(i);
 		} finally {
 			if (out != null) {
 				out.close();
 			}
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 	}
 
 	public void loadProfiles(String filename) throws IOException {
 		System.out.print("Loading Profiles");
 		long start = System.nanoTime();
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new FileReader(filename));
 			String line;
 			int i = 0;
 			while ((line = in.readLine()) != null) {
 				TestUser testUser = TestUser.parseProfile(line, this);
 				testUsers.put(testUser.id, testUser);
 				if (i % 290 == 0) System.out.print(".");
 				i++;
 			}
 			System.out.print(i);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 	}
 
 	public void buildSuggestions() {
 		System.out.print("Suggesting");
 		long start = System.nanoTime();
 		Iterator<TestUser> it = testUsers.values().iterator();
 		int i = 0;
 		int unique = 0;
 		int empty = 0;
 		int guessed = 0;
 		while (it.hasNext()) {
 			if (i % 280 == 0) System.out.print(".");
 			TestUser testUser = it.next();
 			User bestUser = testUser.bestUser;
 			if (bestUser != null) {
 				Set<Repository> bestUserRepos = new HashSet<Repository>();
 				bestUserRepos.addAll(bestUser.watching);
 				Iterator<User> userIt = testUser.alternateUser.iterator();
 				while (userIt.hasNext()) {
 					User user = userIt.next();
 					bestUserRepos.addAll(user.watching);
 				}
 
 				Collections.sort(new ArrayList<Repository>(bestUserRepos), Collections.reverseOrder(new Repository.FollowerComparator()));
 				Iterator<Repository> repoIt = bestUserRepos.iterator();
 				int j = 0;
 				while (repoIt.hasNext() && j < SUGGESTIONS) {
 					Repository repo = repoIt.next();
 					if (!testUser.suggested.contains(repo)) {
 						testUser.suggest(repo);
 						j++;
 					}
 				}
 				
 			}
 
 			if (testUser.remaining == 10 && testUser.bestUser == null) {
 				//System.out.println(testUser + ":" + testUser.getWatches());
 				//System.out.println(testUser.bestUser);
 				// Either they aren't watching anything or
 				Map<String,Language> languages = new HashMap<String,Language>();
 				Iterator<Repository> repoIt = testUser.watching.iterator();
 				while (repoIt.hasNext()) {
 					Repository repo = repoIt.next();
 					if (repo.languages != null) {
 						Iterator<Language> langIt = repo.languages.iterator();
 						while (langIt.hasNext()) {
 							Language src = langIt.next();
 							Language lang = languages.get(src.name);
 							if (lang == null) {
 								lang = new Language(src.name, 0);
 								lang.name = src.name;
 								languages.put(src.name, lang);
 							}
 							lang.lines += src.lines;
 						}
 					}
 				}
 				Iterator<Language> langIt = languages.values().iterator();
 				System.out.print(testUser.id + ":");
 				while (langIt.hasNext()) {
 					Language src = langIt.next();
 					System.out.print(src.name + ":" + src.lines + ":");
 				}
 				System.out.println();
 				unique++;
 			}
 			if (testUser.watching.size() == 0) {
 				//System.out.println("User isn't watching anything yet!");
 				empty++;
 			}
 			int startSuggestions = 0;
 			while (testUser.remaining > 0) {
 				guessed += testUser.remaining;
 				//System.out.println("Added top " + testUser.remaining + " repos");
 				//List<Repository> shuffled = sortedRepositories.subList(0, 100);
 				//Collections.shuffle(shuffled);
 				//testUser.suggested.addAll(shuffled.subList(0, testUser.remaining));
 //				System.out.println("Suggesting : " + startSuggestions
 				testUser.suggested.addAll(sortedRepositories.subList(startSuggestions, startSuggestions + testUser.remaining));
				startSuggestions += testUser.remaining;
 				removeDuplicates(testUser.suggested);
 				testUser.remaining = SUGGESTIONS - testUser.suggested.size();
 			}
 			Collections.sort(testUser.suggested, Collections.reverseOrder(new Repository.FollowerComparator()));
 			i++;
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 		System.out.println("There were:");
 		System.out.println(" " + empty + " empty test users - not watching anything - These are test users not in the data as users");
 		System.out.println(" " + unique + " unique test users - they watch stuff noone else is watching");
 		System.out.println(" " + guessed + " guessed suggestions - taken from the most popular projects");
 		System.out.println(" " + (SUGGESTIONS * testUsers.size()) + " total suggestions");
 	}
 
 	public void removeDuplicates(List list) {
 		for (int i = 0; i < list.size(); i++) {
 			Object o = list.get(i);
 			int j = list.lastIndexOf(o);
 			if (j != -1 && j != i) {
 				list.remove(o);
 			}
 		}
 	}
 
 	public void buildStats() {
 
 		System.out.print("Sorting Repositories");
 		long start = System.nanoTime();
 		sortedRepositories = Arrays.asList(repositories.values().toArray(new Repository[0]));
 		Collections.sort(sortedRepositories, Collections.reverseOrder(new Repository.FollowerComparator()));
 		long end = System.nanoTime();
 		System.out.println(" " + sortedRepositories.size() + " in " + ((end - start)/1000000) + "ms");
 
 
 		System.out.print("Sorting TestUsers " + testUsers.size() + " = ");
 		start = System.nanoTime();
 		sortedUsers = Arrays.asList(testUsers.values().toArray(new TestUser[0]));
 		Collections.sort(sortedUsers, Collections.reverseOrder(new TestUser.WatchCountComparator()));
 		end = System.nanoTime();
 		System.out.println(" " + sortedUsers.size() + " in " + ((end - start)/1000000) + "ms");
 
 	}
 
 	public void printStats() {
 		Iterator<Repository> it = sortedRepositories.iterator();
 		int i = 0;
 		while(it.hasNext() && i < 10) {
 			Repository repo = it.next();
 			System.out.println((i+1) + ": " + repo);
 			i++;
 		}
 		Iterator<TestUser> userIt = sortedUsers.iterator();
 		i = 0;
 		while(userIt.hasNext() && i < 10) {
 			TestUser user = userIt.next();
 			System.out.println((i+1) + ": " + user);
 			i++;
 		}
 
 	}
 
 	public void saveResults(String filename) throws IOException {
 		System.out.print("Saving results");
 		long start = System.nanoTime();
 		BufferedWriter out = null;
 		try {
 			out = new BufferedWriter(new FileWriter(filename));
 			String line;
 			int i = 0;
 			Iterator<TestUser> it = testUsers.values().iterator();
 			while (it.hasNext()) {
 				TestUser testUser = it.next();
 				String userId = testUser.id;
 				String suggestions = testUser.getSuggestions();
 				out.write(userId + ":" + suggestions);
 				out.newLine();
 				if (i % 290 == 0) System.out.print(".");
 				i++;
 			}
 			System.out.print(i);
 		} finally {
 			if (out != null) {
 				out.close();
 			}
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 	}
 
 
 
 	public static void main(String[] args) throws Exception {
 
 		Recommend rec = new Recommend();
 
 		rec.loadRepos("repos.txt");
 
 		rec.loadUsers("data.txt");
 
 		rec.loadLanguages("lang.txt");
 
 		File profiles = new File("profiles.txt");
 		if (profiles.exists()) {
 			rec.loadProfiles("profiles.txt");
 			rec.buildStats();
 			rec.printStats();
 		} else {
 			rec.loadTest("test.txt");
 			rec.buildStats();
 			rec.printStats();			
 			rec.compareUsers();
 			rec.saveProfiles("profiles.txt");
 		}
 
 
 
 		
 
 		rec.buildSuggestions();
 
 		rec.saveResults("results.txt");
 
 
 	}
 
 }
