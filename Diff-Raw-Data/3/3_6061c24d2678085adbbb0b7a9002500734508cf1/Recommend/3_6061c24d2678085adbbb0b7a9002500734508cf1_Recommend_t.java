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
 	public Map<String, List<Repository>> repoLanguages = new HashMap<String, List<Repository>>();
 
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
 					Iterator<Language> langIt = langs.languages.iterator();
 					while (langIt.hasNext()) {
 						Language l = langIt.next();
 						List<Repository> rl = repoLanguages.get(l.name);
 						if (rl == null) {
 							rl = new ArrayList<Repository>();
 							repoLanguages.put(l.name, rl);
 						}
 						rl.add(repo);
 					}
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
 		int singleLanguage = 0;
 		int mainLanguageCount = 0;
 		int filtered = 0;
 		int languageGuessed = 0;
 		int missed = 0;
 		while (it.hasNext()) {
 			if (i % 280 == 0) System.out.print(".");
 			TestUser testUser = it.next();
 			User bestUser = testUser.bestUser;
 			if (bestUser != null && testUser.bestScore > 1) {
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
 				unique++;
 			}
 			if (testUser.watching.size() == 0) {
 				//System.out.println("User isn't watching anything yet!");
 				empty++;
 			}
 			if (testUser.remaining > 0) {
 
 				if (testUser.watching.size() > 0) {
 					List<Language> userLangs = discoverLanguages(testUser);
 					Collections.sort(userLangs, Collections.reverseOrder(new Language.LinesComparator()));
 					String mainLanguage = null;
 					if (userLangs.size() < 1) {
 						// back to random
 					} else if (userLangs.size() == 1) {
 						// Only interested in language x
 						singleLanguage++;
 						mainLanguage = userLangs.get(0).name;
 					} else {
 						mainLanguage = userLangs.get(0).name;
 					
 					}/* else if (userLangs.size() < 4 && testUser.watching.size() > 1) {
 						// filter out "helper langs" like javascript
 						Iterator<Language> userLangIt = userLangs.iterator();
 						while (userLangIt.hasNext()) {
 							Language lang = userLangIt.next();
 							//if (lang.name.equals("JavaScript")) {
 							//	filtered++;
 							//} else {
 								mainLanguage = lang.name;
 								mainLanguageCount++;
 								break;
 							//}
 						}
 					} else {
 						// They probably don't care about language.
 					}*/
 					/*if (mainLanguage != null) {
 						//System.out.println("Main Language: " + mainLanguage);
 						// Make suggestions based on language
 						
 						int langSuggested = testUser.remaining;
 						List<Repository> repoList = repoLanguages.get(mainLanguage);
 						Collections.sort(repoList, Collections.reverseOrder(new Repository.FollowerComparator()));
 						int startSuggestions = 0;
 						int endSuggestions = testUser.remaining;
 						while (testUser.remaining > 0) {
 							
 							testUser.suggested.addAll(sortedRepositories.subList(startSuggestions, endSuggestions));
 							removeDuplicates(testUser.suggested);
 							removeDuplicates(testUser.watching, testUser.suggested);
 							testUser.remaining = SUGGESTIONS - testUser.suggested.size();
 							if (endSuggestions >= repoList.size() - 1) {
 								break;
 							}
 							startSuggestions = endSuggestions;
 							endSuggestions += testUser.remaining;
 							if (endSuggestions >= repoList.size()) {
 								endSuggestions = repoList.size() - 1;
 							}
 						}
 						languageGuessed += langSuggested - testUser.remaining;
 					}*/
 				}
 
 				int startSuggestions = 0;
 				int endSuggestions = testUser.remaining;
 				int guessedSuggested = testUser.remaining;
 				while (testUser.remaining > 0) {
 					System.out.println(startSuggestions + " to " + endSuggestions + " of " + sortedRepositories.size() + " left " + testUser.remaining);
 					testUser.suggested.addAll(sortedRepositories.subList(startSuggestions, endSuggestions));
 					//removeDuplicates(testUser.suggested);
 					removeDuplicates(testUser.watching, testUser.suggested);
 					testUser.remaining = SUGGESTIONS - testUser.suggested.size();
 					if (endSuggestions >= sortedRepositories.size() - 1) {
 						break;
 					}
 					startSuggestions = endSuggestions;
 					endSuggestions += testUser.remaining;
 					if (endSuggestions >= sortedRepositories.size()) {
 						endSuggestions = sortedRepositories.size();
 					}
 				}
 				guessed += guessedSuggested - testUser.remaining;
 			}
 			missed += testUser.remaining;
 			Collections.sort(testUser.suggested, Collections.reverseOrder(new Repository.FollowerComparator()));
 			i++;
 		}
 		long end = System.nanoTime();
 		System.out.println(" in " + ((end - start)/1000000) + "ms");
 		System.out.println("There were:");
 		System.out.println(" " + empty + " empty test users - not watching anything - These are test users not in the data as users");
 		System.out.println(" " + unique + " unique test users - they watch stuff noone else is watching");
 		System.out.println(" " + guessed + " guessed suggestions - taken from the most popular projects");
 		System.out.println(" " + languageGuessed + " guessed suggestions based on language - taken from the most popular projects");
 		System.out.println(" " + singleLanguage + " single language users");
 		System.out.println(" " + mainLanguageCount + " have a main language");
 		System.out.println(" " + filtered + " filtered languages");
 		System.out.println(" " + missed + " missed guesses SHOULD BE 0");
 		System.out.println(" " + (SUGGESTIONS * testUsers.size()) + " total suggestions");
 	}
 
 	public List<Language> discoverLanguages(TestUser testUser) {
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
 		//System.out.print(testUser.id + ":");
 		while (langIt.hasNext()) {
 			Language src = langIt.next();
 			//System.out.print(src.name + ":" + src.lines + ":");
 		}
 		//System.out.println();
 		List<Language> list = new ArrayList<Language>(languages.values());
 		Collections.sort(list, Collections.reverseOrder(new Language.LinesComparator()));
 		return list;
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
 
 	public void removeDuplicates(List watching, List suggested) {
 		for (int i = 0; i < suggested.size(); i++) {
 			Object o = suggested.get(i);
 			int j = watching.indexOf(o);
			if (j != -1) {
				System.out.println("Removing " + o);
 				suggested.remove(o);
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
 		while(userIt.hasNext() && i < 20) {
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
