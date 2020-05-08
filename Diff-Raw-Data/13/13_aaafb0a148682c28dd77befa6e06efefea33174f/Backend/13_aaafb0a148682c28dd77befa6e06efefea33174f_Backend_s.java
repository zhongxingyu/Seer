 /**
     This file is part of Bibbla.
 
     Bibbla is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Bibbla is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Bibbla.  If not, see <http://www.gnu.org/licenses/>.    
  **/
 
 package dat255.grupp06.bibbla.backend;
 
 import dat255.grupp06.bibbla.backend.tasks.DetailedViewJob;
 import dat255.grupp06.bibbla.backend.tasks.MyBooksJob;
 import dat255.grupp06.bibbla.backend.tasks.MyDebtJob;
 import dat255.grupp06.bibbla.backend.tasks.MyReservationsJob;
 import dat255.grupp06.bibbla.backend.tasks.SearchJob;
 import dat255.grupp06.bibbla.backend.tasks.Task;
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.utils.Callback;
 import dat255.grupp06.bibbla.utils.PrivateCredentials;
 
 /**
  * Performs tasks like searching, reserving and logging in.
  * Does all the heavy lifting.
  * 
  * @author Niklas Logren
  */
 public class Backend {
 	
 	private Settings settings;
 	private Session session;
 	
 	/**
 	 * Creates a new instance of our Backend.
 	 * Initialises a new session and fetches settings.
 	 */
 	public Backend() {
 		settings = new Settings(PrivateCredentials.name,PrivateCredentials.code,PrivateCredentials.pin);
 		session = new Session(settings.getName(), settings.getCode(), settings.getPin());
 	}
 	
 	/**
 	 *  @returns the user's username.
 	 *  If no name is saved, returns empty string.
 	 */
 	public String getUserName() {
 		return session.getName();
 	}
 	
 	/**
 	 *  Starts fetching the user's current debt. Reports results using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called when logging in is done. 
 	 */
 	public void fetchUserDebt(Callback frontendCallback) {
 		
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				MyDebtJob job = new MyDebtJob(session);
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 * Starts the login process. Reports results using callback.
 	 * 
 	 * @param frontendCallback - the callback object which will be called when logging in is done.
 	 */
 	public void login(final Callback frontendCallback) {
 
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				message = session.login();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 *  Searches backend for the supplied string, and reports results using callback.
 	 *  
 	 *  @param s - The string to search for.
 	 *  @param page - which page of search results to fetch. If too high, returns empty list.
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
 	 */
 	public void search(final String s, final int page, final Callback frontendCallback) {
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
				SearchJob job = new SearchJob(s,0);
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 *  Fetches the DetailedView of the supplied book.
 	 *  Sends a new book with all the additional information to the callback.
 	 */
 	public void fetchDetailedView(final Book book, final Callback frontendCallback) {
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				DetailedViewJob job = new DetailedViewJob(book);
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 *  Fetches a list of the user's current reservations.
 	 *  Returns it using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
 	 */
 	public void fetchReservations(Callback frontendCallback) {
 		Task task = new Task(frontendCallback) {
 			@Override
 			protected Void doInBackground(String... arg0) {
 				MyReservationsJob job = new MyReservationsJob(session);
 				message = job.run();
 				return null;
 			}
 		};
 		task.execute();
 	}
 
 	/**
 	 *  Fetches a list of the user's currently loaned books. Returns it using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
 	 */
 	public void fetchLoans(Callback frontendCallback) {
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				MyBooksJob job = new MyBooksJob(session);
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 * Logs the user out.
 	 */
 	public void logOut(){
 		session = new Session();	
 	}
 	
 	public boolean isLoggedIn() {
 		return session.checkLogin();
 	}
 }
