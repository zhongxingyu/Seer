 package ws.softlabs.lib.kino.dao.server.service.pmf;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import org.apache.log4j.Logger;
 
 import ws.softlabs.lib.kino.dao.server.impl.pmf.PMFDAOUtils;
 import ws.softlabs.lib.kino.dao.server.impl.pmf.PMFHallDAOImpl;
 import ws.softlabs.lib.kino.dao.server.impl.pmf.PMFMovieDAOImpl;
 import ws.softlabs.lib.kino.dao.server.impl.pmf.PMFShowDAOImpl;
 import ws.softlabs.lib.kino.dao.server.impl.pmf.PMFTheaterDAOImpl;
 import ws.softlabs.lib.kino.dao.server.intf.DataService;
 import ws.softlabs.lib.kino.dao.server.intf.HallDAO;
 import ws.softlabs.lib.kino.dao.server.intf.MovieDAO;
 import ws.softlabs.lib.kino.dao.server.intf.ShowDAO;
 import ws.softlabs.lib.kino.dao.server.intf.TheaterDAO;
 import ws.softlabs.lib.kino.dao.server.model.pmf.PHall;
 import ws.softlabs.lib.kino.dao.server.model.pmf.PMovie;
 import ws.softlabs.lib.kino.dao.server.model.pmf.PShow;
 import ws.softlabs.lib.kino.dao.server.model.pmf.PTheater;
 import ws.softlabs.lib.kino.dao.server.util.DAOResultUtils;
 import ws.softlabs.lib.kino.model.client.Hall;
 import ws.softlabs.lib.kino.model.client.Movie;
 import ws.softlabs.lib.kino.model.client.Show;
 import ws.softlabs.lib.kino.model.client.Theater;
 import ws.softlabs.lib.util.client.DateUtils;
 import ws.softlabs.lib.util.client.DayComparator;
 
 @SuppressWarnings("unused")
 public class PMFModelDataService implements DataService {
 
 	private static final Logger log = 
 		Logger.getLogger("kino.pmfdao.service." + PMFModelDataService.class.getSimpleName());
 	
 	private static final TheaterDAO daoTheater = new PMFTheaterDAOImpl();
 	private static final MovieDAO 	daoMovie   = new PMFMovieDAOImpl();
 	private static final HallDAO 	daoHall    = new PMFHallDAOImpl();
 	private static final ShowDAO 	daoShow    = new PMFShowDAOImpl();
 	
 	/* get or create */
 	public Theater getTheater(String name, String url) {
 		log.debug("ENTER (name = " + name + ", url = " + url +")");
 		Theater theater = daoTheater.get(name, url);
 		if (theater!= null) {
 			log.debug("daoTheater returned theater: " + theater.getName());
 			log.debug("EXIT");
 			return theater;	
 		}
 		theater = new Theater();
 		theater.init(name, url);
 		if (daoTheater.add(theater)) {
 			log.debug("daoTheater added theater (" + theater.getName() + ")");
 			log.debug("EXIT");
 			return theater;
 		}
 		else {
 			log.warn("daoTheater could not add theater (" + theater.getName() + ")");
 			log.debug("EXIT returning NULL");
 			return null;
 		}
 	}
 	public Hall    getHall(Theater theater, String name, String html) {
 		log.debug("ENTER (theater = " + theater + ", name = " + name + ", html = " + html +")");
 		Hall hall = daoHall.get(theater, name);
 		if (hall != null) {
 			log.debug("******** got hall from datastore");
 			log.debug("EXIT (hall = " + hall + ")");
 			return hall;	
 		}
 		hall = new Hall();
 		hall.init(theater, name, html);
 		log.debug("created hall: " + hall);
 		if (daoHall.add(hall)) {
 			log.debug("******** daoHall added hall to DAO");
 			log.debug("EXIT (hall = " + hall + ")");
 			return hall;
 		}
 		else {
 			log.debug("******** daoHall can't add hall to DAO");
 			log.debug("EXIT (NULL)");
 			return null;
 		}
 	}
 	public Movie   getMovie(String name, String url) {
 		log.debug("ENTER (name = " + name + ", url = " + url +")");
 		Movie movie = daoMovie.get(name, url);
 		if (movie != null) {
 			log.debug("EXIT (movie = " + movie + ")");
 			return movie;	
 		}
 		movie = new Movie();
 		movie.init(name, url);
 		log.debug("created movie: " + movie);
 		if (daoMovie.add(movie)) {
 			log.debug("EXIT (movie = " + movie + ")");
 			return movie;
 		}
 		else {
 			log.debug("EXIT (NULL)");
 			return null;
 		}
 	}
 	public Show    getShow(Hall hall, Movie movie, Date dateTime,
 							List<Integer> price) {
 		log.debug("ENTER (hall = " + hall + ", movie = " + movie + ", date = " + dateTime + ")");
 		Show show = daoShow.get(hall, dateTime);
 		if (show != null) {
 			log.debug("EXIT (show = " + show + ")");
 			return show;	
 		}
 		show = new Show(0L, hall, movie, dateTime, price);
 		//show.init(hall, movie, dateTime, price);
 		log.debug("created show: " + show);
 		if (daoShow.add(show)) {
 			log.debug("EXIT (show = " + show + ")");
 			return show;
 		}
 		else {
 			log.debug("EXIT (NULL)");
 			return null;
 		}
 	}
 	
 	/* add */
 	public boolean addTheater(Theater theater) {
 		log.debug("ENTER (theater = " + theater +")");
 		boolean result = daoTheater.add(theater);
 		log.debug("EXIT (result = " + result + ")");
 		return result;
 	}
 	public boolean addHall(Hall hall) {
 		log.debug("ENTER (hall = " + hall +")");
 		boolean result = daoHall.add(hall);
 		log.debug("EXIT (result = " + result + ")");
 		return result;
 	}
 	public boolean addMovie(Movie movie) {
 		log.debug("ENTER (movie = " + movie +")");
 		boolean result = daoMovie.add(movie);
 		log.debug("EXIT (result = " + result + ")");
 		return false;
 	}
 	public boolean addShow(Show show) {
 		log.debug("ENTER (show = " + show +")");
 		boolean result = daoShow.add(show);
 		log.debug("EXIT (result = " + result + ")");
 		return false;
 	}
 
 	/* update */ 
 	public boolean updateTheater(Theater theater) {
 		//return daoTheater.update(theater);	
 		return false;
 	}
 	public boolean updateHall(Hall hall) {
 		//return daoHall.update(hall);
 		return false;
 	}
 	public boolean updateMovie(Movie movie) {
 		//return daoMovie.update(movie);
 		return false;
 	}
 	public boolean updateShow(Show show) {
 		//return daoShow.update(show);
 		return false;
 	}
 	
 	/* remove */
 	public boolean removeTheater(Theater theater) {
 		//return daoTheater.remove(theater);	
 		return false;
 	}
 	public boolean removeHall(Hall hall) {
 		//return daoHall.remove(hall);
 		return false;
 	}
 	public boolean removeMovie(Movie movie) {
 //		return daoMovie.remove(movie);
 		return false;
 	}
 	public boolean removeShow(Show show) {
 //		return daoShow.remove(show);
 		return false;
 	}
 
 	/* get existing */
 	public Theater getTheaterExisting(String name, String url) {
 //		return daoTheater.get(name);
 		return null;
 	}
 	public Hall    getHallExisting(Theater theatre, String name, String html) {
 //		return daoHall.get(theatre, name);
 		return null;
 	}
 	public Movie   getMovieExisting(String name, String url) {
 //		return daoMovie.get(name, url);
 		return null;
 	}
 	public Show    getShowExisting(Hall hall, Movie movie, Date dateTime,
 							List<Integer> price) {
 //		return daoShow.get((Hall)hall, dateTime);
 		return null;
 	}
 	
 	/* special queries */
 	public List<Theater> getTheaterList(){
 		log.debug("ENTER");
 		List<Theater> daoList = daoTheater.getList();
 		if (daoList != null && !daoList.isEmpty()) {
 			log.debug("daoTheater returned list of theaters");
 			List<Theater> result = new ArrayList<Theater>(daoList);  
 			log.debug("EXIT (result.size = " + result.size() + ")");
 			return result;
 		}
 		log.debug("EXIT (NULL)");
 		return null;
 	}
 	public List<Hall>    getTheaterHallList(Theater theater) {
 		log.debug("ENTER");
 		List<Hall> daoList = daoHall.getList(theater);
 		if (daoList != null && !daoList.isEmpty()) {
 			log.debug("daoTheater returned list of theaters");
 			List<Hall> result = new ArrayList<Hall>(daoList);
 			log.debug("EXIT (result.size = " + result.size() + ")");
 			return result;
 		} else {
 			log.debug("got no results from datastore");
 			log.debug("EXIT (NULL)");
 			return null;
 		}
 	}
 	public List<Show>    getShowList(Hall hall, Date date){
 		log.debug("ENTER");
 		List<Show> daoList = daoShow.getList(hall, date);
 		/*Since*/ //(new Date(System.currentTimeMillis())));  
 		if (daoList != null && !daoList.isEmpty()) {
 			log.debug("daoShow returned list of shows");
 			List<Show> result = new ArrayList<Show>(daoList);
 			log.debug("EXIT (result.size = " + result.size() + ")");
 			return result;	
 		} else {
 			log.debug("got no results from datastore");
 			log.debug("EXIT (NULL)");
 			return null;			
 		}
 	}
 	public List<String>  getShowDaysList(Theater theater) {
 		log.debug("ENTER");
		List<String> daoShowDays = daoShow.getDaysList(theater, new Date(System.currentTimeMillis()));
 		if (daoShowDays != null && !daoShowDays.isEmpty()) {
 			log.debug("daoShow returned list of days");
			//List<String> daysList = new ArrayList<String>(daoShowDays);
 			Collections.sort(daoShowDays, new DayComparator());
 			log.debug("EXIT (result.size = " + daoShowDays.size() + ")");
 			return daoShowDays;
 		} else {
 			log.debug("got no results from datastore");
 			log.debug("EXIT (NULL)");
 			return null;			
 		}
 	}
 	
 	/********************************************/
 	public List<String>		getRawTheaterList() {
 		List<String> result = daoTheater.getRawList();
 		//DAOResultUtils.printStringList(result);
 		return result;
 	}
 	public List<String>		getRawHallList() {
 		List<String> result = daoHall.getRawList();		
 		//DAOResultUtils.printStringList(result);
 		return result;
 	}
 	public List<String>		getRawDayList() {
 		List<String> result = daoShow.getRawDayList();
 		//DAOResultUtils.printStringList(result);
 		return result;
 	}
 	public List<String>		getRawMovieList() {
 		List<String> result = daoMovie.getRawList();
 		//DAOResultUtils.printStringList(result);
 		return result;
 	}
 	public List<String>		getRawShowList() {
 		List<String> result = daoShow.getRawList();
 		//DAOResultUtils.printStringList(result);
 		return result;
 	}
 	public void clearDatabase() {
 		log.debug("ENTER");
 		PMFDAOUtils.removeAll();	
 		log.debug("EXIT");
 	}
 	@Override
 	public void clearTheaters() {
 		log.debug("ENTER");
 		PersistenceManager pm = PMF.getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PTheater.class);
 			query.deletePersistentAll();
 			log.debug("cleared PTheater");
 		} catch (Exception ex) {
 			log.debug("EXIT (EXCEPTION)" + ex);
 		} finally {
 			pm.close();
 		}
 		log.debug("EXIT");
 	}
 	@Override
 	public void clearHalls() {
 		log.debug("ENTER");
 		PersistenceManager pm = PMF.getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PHall.class);
 			query.deletePersistentAll();
 			log.debug("cleared PHall");
 		} catch (Exception ex) {
 			log.debug("EXIT (EXCEPTION)" + ex);
 		} finally {
 			pm.close();
 		}
 		log.debug("EXIT");
 	}
 	@Override
 	public void clearShows() {
 		log.debug("ENTER");
 		PersistenceManager pm = PMF.getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PShow.class);
 			query.deletePersistentAll();
 			log.debug("cleared PShow");
 		} catch (Exception ex) {
 			log.debug("EXIT (EXCEPTION)" + ex);
 		} finally {
 			pm.close();
 		}
 		log.debug("EXIT");
 	}
 	@Override
 	public void clearMovies() {
 		log.debug("ENTER");
 		PersistenceManager pm = PMF.getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PMovie.class);
 			query.deletePersistentAll();
 			log.debug("cleared PMovie");
 		} catch (Exception ex) {
 			log.debug("EXIT (EXCEPTION)" + ex);
 		} finally {
 			pm.close();
 		}
 		log.debug("EXIT");
 	}	
 	
 }
