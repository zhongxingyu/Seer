 package movieTheater.main;
 
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import movieTheater.GUI.CreateMovie;
 import movieTheater.GUI.SearchMovie;
 import movieTheater.Movie.Actor;
 import movieTheater.Movie.Director;
 import movieTheater.Movie.Genre;
 import movieTheater.Movie.Movie;
 import movieTheater.Persons.SalesPerson;
 import movieTheater.SQL.SQLMovieLoad;
 import movieTheater.SQL.SQLMovieSave;
 
 /**
  * 
  * @author Henrik
  *
  */
 public class MovieController 
 {
 	private SQLMovieLoad load;
 	private SQLMovieSave save;
 	private CreateMovie createMovie;
 	private Movie movie;
 	public static ArrayList<Movie> movies;
 	public static ArrayList<Actor> actors;
 	public static ArrayList<Actor> newActors;
 	public static ArrayList<Director> directors;
 	public static ArrayList<Director> newDirectors;
 	public static ArrayList<Genre> genres;
 	public static ArrayList<Genre> newGenres;
 	public static Map<Actor, String> addToCast;
 	private boolean isNotReadyForSave;
 	
 	/**
 	 * 
 	 */
 	public MovieController()
 	{
 		load = new SQLMovieLoad();
 		save = new SQLMovieSave();
 		newActors = new ArrayList<Actor>();
 		newDirectors = new ArrayList<Director>();
 		newGenres = new ArrayList<Genre>(); 
 		addToCast = new HashMap<Actor, String>();
 		movies = new ArrayList<Movie>();
 		isNotReadyForSave = false;
 	}
 	/**
 	 * Getting parameters from GUI, creates an Movie object and saves its data in the database. 
 	 */
 	public void setMovie()
 	{
 		movie = new Movie();					//creates new movie, so movieID is == 0;
 		loadAttributes();
 		showPanel();
 	}
 
 	/**
 	 * searches via GUI for the movie to edit. Gets new parameters via GUI, alter the Movie object and saves its data in the database. 
 	 */
 	public void EditMovie()
 	{
 		SearchMovie searchMovie = new SearchMovie(this);
 		searchMovie.setVisible(true);
 				
 		try
 		{
 			searchMovie.latch.await();
 		} 
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		movie = searchMovie.getMovie();
 		if(movie!=null)
 		{
 			loadAttributes();
 			showPanel();
 		}
 	}
 
 	private void showPanel()
 	{
 		do
 		{
 			createMovie = new CreateMovie(movie, !(MainController.loggedOn instanceof SalesPerson));
 			createMovie.setVisible(true);
 			try
 			{
 				createMovie.latch.await();
 			} 
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 			if (createMovie.areChangesMade() && !createMovie.isCancelChosen()) 
 			{
 				checkAndSaveMovie();
 			}
 			else if (createMovie.isCancelChosen())
 			{
 				isNotReadyForSave = false;
 				createMovie.dispose();
 			}
 			else
 			{
 				createMovie.dispose();
 			}
 		}
 		while(isNotReadyForSave);
 	}
 
 	/**
 	 * 	 searches via GUI for the movie to delete. Ask whether the user i sure and then deletes the data in the database. 
 	 */
 	public void deleteMovie()
 	{
 		SearchMovie searchMovie = new SearchMovie(this);
 		searchMovie.setVisible(true);
 		try
 		{
 			searchMovie.latch.await();
 		} 
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 
 		movie = searchMovie.getMovie();
 		if(movie!=null)
 		{
			int choise = CreateMovie.showOKCancelDialog("Er du sikker p du vil slette "+movie.getTitle());	
 			if (choise == 0)
 			{
 				save.deleteMovie(movie.getMovieID());
 			}
 		}
 	}
 	/**
 	 * Searches for movies in the database that have some of the parameters attached. Movie objects are created accordingly and placed in an ArrayList 
 	 * @param title
 	 * @param orgTitle
 	 * @param actorFName
 	 * @param directorFName
 	 * @return ArrayList<Movie>
 	 * @throws SQLException
 	 */
 	public ArrayList<Movie> searchMovie(String title, String orgTitle, String actorFName, String directorFName)throws SQLException
 	{
 		movies.clear();
 		try
 		{
 			movies = load.searchMovie(title, orgTitle, actorFName, directorFName);
 		}
 		catch(java.lang.NumberFormatException e)
 		{
 			e.getStackTrace();
 		}
 		return movies;
 	}
 
 	private void checkAndSaveMovie()
 	{
 		java.sql.Date sqlDatePremier = null;
 		java.sql.Date sqlDateEnd = null;
 		int playingTime = 0;
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 
 		try
 		{	
 			Date premierDate = dateFormat.parse(createMovie.getPremierDate());
 			Date offDate = dateFormat.parse(createMovie.getOffDate());	
 			sqlDatePremier = new java.sql.Date(premierDate.getTime());	
 			sqlDateEnd = new java.sql.Date(offDate.getTime());
 			movie.setReleaseDate(sqlDatePremier);
 			movie.setTimeEnd(sqlDateEnd);
 		}
 		catch (ParseException e6)
 		{
 			createMovie.showMessage("Datofelter skal udfyldes");					
 			isNotReadyForSave = true;
 		}
 
 		try
 		{
 			playingTime = Integer.parseInt(createMovie.getPlayingTime());
 			movie.setLength(playingTime);
 		}
 		catch (NumberFormatException e7)
 		{
 			createMovie.showMessage("Spilletid udfyldes med antal minutter");				
 			isNotReadyForSave = true;
 		}
 
 
 		// Checks whether a title is selected
 		if (movie.getTitle().isEmpty() || movie.getTitle() == null)
 		{
 			createMovie.showMessage("Der skal indtastes en title");
 			isNotReadyForSave = true;
 		}
 		// Checks whether premier date is before TimeEnd. 
 		if (movie.getReleaseDate() != null && movie.getTimeEnd() != null)
 		{
 			if (movie.getReleaseDate().after(movie.getTimeEnd()))
 			{
 				createMovie.showMessage("Udlbsdato skal ligge efter premierdato");
 				isNotReadyForSave = true;
 			}
 			// Checks whether timeEnd is after current time
 			Date currentTime = new Date();
 			if (movie.getTimeEnd().before(currentTime))
 			{
 				int choise = createMovie.showYesNoDialog("Udlbsdato er overskredet. Vil du ndre datoen");
 				if (choise == 0)
 				{
 					isNotReadyForSave = true;
 				}
 			}
 		}
 		// Checks whether a director is selected
 		if (movie.getDirector() == null)
 		{
 			createMovie.showMessage("Der skal vlges en instruktr");
 			isNotReadyForSave = true;
 		}
 		// Checks whether a genre is selected
 		if (movie.getGenre() == null)
 		{
 			createMovie.showMessage("Der skal vlges en genre");
 			isNotReadyForSave = true;
 		}
 		//TODO Check whether the movie is like any saved one. 
 		//
 		for (Director currentNewDirector : newDirectors) //check om der er gengangere og fjern dem. 
 		{
 			boolean isToBeSaved = false;
 
 			for (Director currentDirector : directors)
 			{
 				if (currentNewDirector.equals(currentDirector) && currentDirector.getDirectorID() != 0)
 				{
 					createMovie.showMessage("Den oprettede instruktr findes allerede og erstattes.");
 					if (movie.getDirector().equals(currentDirector))
 					{
 						movie.setDirector(currentDirector);			//Changes movies director to the one allready in the database. 
 					}
 				}
 				if (!currentNewDirector.equals(currentDirector) && currentNewDirector.getDirectorID() == 0)
 				{   
 					isToBeSaved = true;
 				}
 			}
 			if (isToBeSaved)
 			{
 				if (movie.getDirector().equals(currentNewDirector))
 				{
 					movie.setDirector(save.saveDirector(currentNewDirector));
 				}
 				else
 				{
 					save.saveDirector(currentNewDirector);
 				}
 			}
 		}
 		
 		//TODO kontrol af om der er doubletter bland skuespillere. 
 		saveNewActors();									//Saves the created actors and adds actorID
 
 		for (Genre currentNewGenre : newGenres) //check om der er gengangere og fjern dem. 
 		{
 			boolean isToBeSaved = false;
 
 			for (Genre currentGenre : genres)
 			{
 				if (currentNewGenre.equals(currentGenre) && currentGenre.getGenreID() != 0)
 				{
 					createMovie.showMessage("Den oprettede gerne finde allerede og erstattes.");
 					if (movie.getGenre().equals(currentGenre))
 					{
 						movie.setGenre(currentGenre);			//Changes movies director to the one allready in the database. 
 					}
 				}
 				if (!currentNewGenre.equals(currentGenre) && currentNewGenre.getGenreID() == 0)
 				{   
 					isToBeSaved = true;
 				}
 			}
 			if (isToBeSaved)
 			{
 				if (movie.getGenre().equals(currentNewGenre))
 				{
 					movie.setGenre(save.saveGenre(currentNewGenre));
 				}
 				else
 				{
 					save.saveGenre(currentNewGenre);
 				}
 			}
 		}
 
 		if(!isNotReadyForSave)	//= is Ready For Save
 		{
 			if (movie.getMovieID() != 0)				//Movie being edited
 			{
 				save.updateMovie(movie);
 				if(!addToCast.isEmpty())
 				{
 					save.saveCastList(addToCast, movie.getMovieID());
 				}
 			}
 			else										//New movie being created
 			{
 				movie = save.saveMovie(movie); 			// returns with a created movieID. 
 				if (!movie.getCast().getCast().isEmpty())
 				{
 					save.saveCastList(movie.getCast().getCast(), movie.getMovieID());
 				}
 			}
 		}
 		createMovie.dispose();
 	}
 
 	private void saveNewActors()
 	{
 		if (!newActors.isEmpty())
 		{
 			for (Actor currentActor : newActors)
 			{
 				currentActor = save.saveActor(currentActor);
 			}
 		}
 	}
 	private void loadAttributes()
 	{
 		actors = load.LoadActors();
 		directors = load.LoadDirector();
 		genres = load.LoadGenres();
 	}
 
 	public void loadMovies()
 	{
 		movies = load.LoadMovie();
 	}
 }
 
