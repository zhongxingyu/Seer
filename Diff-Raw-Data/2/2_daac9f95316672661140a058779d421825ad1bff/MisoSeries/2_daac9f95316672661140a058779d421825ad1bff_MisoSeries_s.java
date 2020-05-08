 package models;
 
 import java.sql.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import play.Logger;
 import play.db.jpa.Model;
 
 import com.google.gson.Gson;
 
 import controllers.Application;
 
 /**
  * Details of an specified Series
  * 
  * @author prime
  * 
  */
 @Entity
 public class MisoSeries extends Model {
 	/**
 	 * Base Episodes URL
 	 */
 	private static String	EPISODESURL	= "http://gomiso.com/api/oauth/v1/episodes.json?media_id=";
 	/**
 	 * The media id for this checkin. 5678
 	 */
 	
 	public Long				media_id;
 	/**
 	 * The total number of episodes for the given media item 136
 	 */
 	public Long				episode_count;
 	/**
 	 * The total number of seasons for the given media item 7
 	 */
 	public Long				season_count;
 	/**
 	 * The seasons for which we have episodes for the given media item (for
 	 * cases where there is a season 0 and other anomalies) [0, 1, 2, 6]
 	 */
 	public String[]			seasons;
 
 	@ManyToMany(cascade = CascadeType.PERSIST)
 	public Set<MisoEpisode>	episodes;
 
 	public MisoEpisode getLatestEpisode() {
 		return MisoEpisode.find("media_id = ? order by label", media_id).first();
 	}
 
 	public MisoEpisode getLatestCheckinEpisode(String label) {
 		MisoEpisode me = MisoEpisode.find("media_id = ? and label = ? order by label", media_id, label).first();
 		
 		if (me == null) {
 			return MisoEpisode.find("media_id = ? order by label", media_id).first();
 		}
 		 return me;
 	}
 
 	/**
 	 * @param media_id
 	 *            The media id for this checkin. 5678
 	 * @param user
 	 *            The Authenticated User
 	 * @return MisoSeries Series Details
 	 */
 	public static MisoSeries getSeriesDetails(Long media_id, User user) {
 		if (!hasSeriesData(media_id)) {
 			MisoSeries misoSeries = createSeriesandEpisodes(media_id, user);
 			return misoSeries;
 		} else {
 			MisoSeries ms = MisoSeries.find("byMedia_id", media_id).first();			
 			if (ms.episodes.size() == 0) {
 				TreeSet<MisoEpisode> rsme = createEpisodes(media_id, user, ms);
 				ms.episodes.addAll(rsme);		
 				ms.save();
 			}
 			return ms;
 		}
 	}
 
 	/**
 	 * @param media_id
 	 * @param user
 	 * @return
 	 */
 	public static MisoSeries createSeriesandEpisodes(Long media_id, User user) {
 		Logger.debug("Creating Series... (%s)", media_id);
 		String strEpisodes = Application.getJsonBodyforUrl(user, EPISODESURL + media_id, Application.GET);
 		MisoSeries misoSeries = new Gson().fromJson(strEpisodes, MisoSeries.class);
 		misoSeries.media_id = media_id;
 		misoSeries.episodes.clear();
 		TreeSet<MisoEpisode> rsme = createEpisodes(media_id, user, misoSeries);
 		misoSeries.episodes.addAll(rsme);		
 		misoSeries.save();
 		return misoSeries;
 	}
 
 	/**
 	 * @param media_id
 	 * @param user
 	 * @param misoSeries
 	 * @return
 	 */
 	public static TreeSet<MisoEpisode> createEpisodes(Long media_id, User user, MisoSeries misoSeries) {
 		Logger.debug("Creating Episodes... (%s)", media_id);
 		TreeSet<MisoEpisode> rsme = new TreeSet<MisoEpisode>();
 		Long curSeason = 0L;
 		Long curEpisode = 0L;
		for (Long i = 1L; i < misoSeries.episode_count; i++) {
 			MisoEpisode me = MisoEpisode.getEpisodeDetails(media_id, user, curEpisode, curSeason);
 			if (me != null) {
 				curSeason = me.season_num;
 				curEpisode = me.episode_num + 1;
 				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
 				DateTime dt = null;
 				if (me.aired == null || me.aired == "") {
 					dt = fmt.parseDateTime("1970-01-01");
 				} else {
 					dt = fmt.parseDateTime(me.aired);
 				}
 				me.aired_date = dt.toDateTimeISO().toDate();
 				me.save();
 				rsme.add(me);
 			}
 		}
 		return rsme;
 	}
 
 	/**
 	 * Has the Series any Data
 	 * 
 	 * @return true or false
 	 */
 	public static Boolean hasSeriesData(Long media_id) {
 		if (findSeriesData(media_id) == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @param media_id
 	 * @return
 	 */
 	public static MisoSeries findSeriesData(Long media_id) {
 		return MisoSeries.find("byMedia_id", media_id).first();
 	}
 	
 	public static List getSeries() {
 		return MisoSeries.findAll();
 	}
 
 }
