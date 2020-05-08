 package mk.tomatoes.entity;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import mk.tomatoes.core.RTConstants;
 import mk.tomatoes.utils.Log;
 import mk.tomatoes.utils.Pair;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 public class RTMovie extends RTEntity {
 
 	//region Fields
 
 	/**
 	 * The movie critics score.
 	 */
 	private int criticsScore;
 	
 	/**
	 * The movie audiance score.
 	 */
	private int audianceScore;
 	
 	/**
 	 * The movie MPAA rating.
 	 */
 	private String mpaa;
 	
 	/**
 	 * The movie studio.
 	 */
 	private String studio = null;
 	
 	/**
 	 * The movie title.
 	 */
 	private String title;
 	
 	/**
 	 * The movie poster.
 	 */
 	private URL[] posters = new URL[4];
 	
 	/**
 	 * The movie plot.
 	 */
 	private String plot;
 	
 	/**
 	 * The movie IMDb (imdb.com) ID.
 	 */
 	private String imdbDd;
 	
 	/**
 	 * The movie Rotten Tomatoes ID.
 	 */
 	private int id;
 	
 	/**
 	 * The movie vote count.
 	 */
 	private Integer count;
 	
 	/**
 	 *The movie release date.
 	 */
 	private Date release;
 	
 	/**
 	 * The DVD release date.
 	 */
 	private Date dvdRelease;
 	
 	/**
 	 * The movie critics consensus.
 	 */
 	private String criticsConsensus;
 	
 	/**
 	 * The movie runtime.
 	 */
 	private int runtime;
 	
 	/**
 	 * The movie year.
 	 */
 	private int year;
 
 	/**
 	 * The movie cast.
 	 */
 	private Set<Pair<String, Set<String>>> cast = Collections.synchronizedSet(new LinkedHashSet<Pair<String, Set<String>>>());
 	
 	/**
 	 * The movie directors.
 	 */
 	private Set<String> directors = Collections.synchronizedSet(new LinkedHashSet<String>());
 	
 	/**
 	 * The movie genres.
 	 */
 	private Set<String> genres = Collections.synchronizedSet(new LinkedHashSet<String>());
 	
 	/**
 	 * The movie clips.
 	 */
 	private Set<RTClip> clips = Collections.synchronizedSet(new LinkedHashSet<RTClip>());
 	
 	/**
 	 * The movie reviews.
 	 */
 	private Set<RTReview> reviews = Collections.synchronizedSet(new LinkedHashSet<RTReview>());
 	
 	//endregion
 	
 	/**
 	 * Creates a new instance of IMDbMovie based on the origin JSON object.
 	 *  
 	 * @param json The origin JSON object
 	 */
 	public RTMovie(JSONObject json) {
 		super(json);
 		parseJSON(json);
 	}
 
 	//region Getters/Setters
 	
 	public int getCriticsScore() {
 		return criticsScore;
 	}
 
 	public void setCriticsScore(int criticsScore) {
 		this.criticsScore = criticsScore;
 	}
 
	public int getAudianceScore() {
		return audianceScore;
 	}
 
	public void setAudianceScore(int audianceScore) {
		this.audianceScore = audianceScore;
 	}
 
 	public String getMpaa() {
 		return mpaa;
 	}
 
 	public void setMpaa(String mpaa) {
 		this.mpaa = mpaa;
 	}
 
 	public String getStudio() {
 		return studio;
 	}
 
 	public void setStudio(String studio) {
 		this.studio = studio;
 	}
 
 	public boolean isStudioSet() {
 		return studio != null;
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public URL[] getPosters() {
 		return posters;
 	}
 
 	public void setPosters(URL[] posters) {
 		this.posters = posters;
 	}
 
 	public String getPlot() {
 		return plot;
 	}
 
 	public void setPlot(String plot) {
 		this.plot = plot;
 	}
 
 	public String getImdbDd() {
 		return imdbDd;
 	}
 
 	public void setImdbDd(String imdbDd) {
 		this.imdbDd = imdbDd;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public Integer getCount() {
 		return count;
 	}
 
 	public void setCount(Integer count) {
 		this.count = count;
 	}
 	
 	public int getYear() {
 		return year;
 	}
 
 	public void setYear(int year) {
 		this.year = year;
 	}
 
 	public Date getRelease() {
 		return release;
 	}
 
 	public void setRelease(String release) {
 		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		try {
 			this.release = (Date)formatter.parse(release);
 		} catch (ParseException e) {
 			Log.print(e);
 		}
 	}
 
 	public Date getDvdRelease() {
 		return dvdRelease;
 	}
 
 	public void setDvdRelease(String dvdRelease) {
 		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		try {
 			this.dvdRelease = (Date)formatter.parse(dvdRelease);
 		} catch (ParseException e) {
 			Log.print(e);
 		}
 	}
 
 	public String getCriticsConsensus() {
 		return criticsConsensus;
 	}
 
 	public void setCriticsConsensus(String criticsConsensus) {
 		this.criticsConsensus = criticsConsensus;
 	}
 
 	public int getRuntime() {
 		return runtime;
 	}
 
 	public void setRuntime(int runtime) {
 		this.runtime = runtime;
 	}
 
 	public Set<Pair<String, Set<String>>> getCast() {
 		return cast;
 	}
 
 	public void setCast(Set<Pair<String, Set<String>>> cast) {
 		this.cast = cast;
 	}
 
 	public Set<String> getDirectors() {
 		return directors;
 	}
 
 	public void setDirectors(Set<String> directors) {
 		this.directors = directors;
 	}
 
 	public Set<String> getGenres() {
 		return genres;
 	}
 
 	public void setGenres(Set<String> genres) {
 		this.genres = genres;
 	}
 
 	public Set<RTClip> getClips() {
 		return clips;
 	}
 
 	public void setClips(Set<RTClip> clips) {
 		this.clips = clips;
 	}
 
 	public Set<RTReview> getReviews() {
 		return reviews;
 	}
 
 	public void setReviews(Set<RTReview> reviews) {
 		this.reviews = reviews;
 	}
 	
 	//endregion
 
 	private void parseJSON(JSONObject json) {
 		
 		if (json.has(RTConstants.ID)) setId(json.getInt(RTConstants.ID));
 		if (json.has(RTConstants.YEAR)) setYear(json.getInt(RTConstants.YEAR));
 		if (json.has(RTConstants.TITLE)) setTitle(json.getString(RTConstants.TITLE));
 		if (json.has(RTConstants.MPAA_RATING)) setMpaa(json.getString(RTConstants.MPAA_RATING));
 		if (json.has(RTConstants.RUNTIME)) setRuntime(json.getInt(RTConstants.RUNTIME));
 		if (json.has(RTConstants.CONSENSUS)) setCriticsConsensus(json.getString(RTConstants.CONSENSUS));
 		if (json.has(RTConstants.SYNOPSIS)) setPlot(json.getString(RTConstants.SYNOPSIS));
 		if (json.has(RTConstants.MOVIE_STUDIO)) setStudio(json.getString(RTConstants.MOVIE_STUDIO));
 		
 		if (json.has(RTConstants.GENRES)) {
 			JSONArray array = json.getJSONArray(RTConstants.GENRES);
 			for (Object obj : array) {
 				genres.add((String) obj);
 			}
 		}
 		
 		if (json.has(RTConstants.RELEASE_DATES)) {
 			JSONObject jobj = json.getJSONObject(RTConstants.RELEASE_DATES);
 			if (jobj.has(RTConstants.DVD)) setDvdRelease(jobj.getString(RTConstants.DVD));
 			if (jobj.has(RTConstants.THEATRE)) setRelease(jobj.getString(RTConstants.THEATRE));
 		}
 		
 		if (json.has(RTConstants.RATINGS)) {
 			JSONObject jobj = json.getJSONObject(RTConstants.RATINGS);
 			if (jobj.has(RTConstants.CRITICS_SCORE)) setCriticsScore(jobj.getInt(RTConstants.CRITICS_SCORE));
			if (jobj.has(RTConstants.AUDIANCE_SCORE)) setAudianceScore(jobj.getInt(RTConstants.AUDIANCE_SCORE));
 		}
 		
 		if (json.has(RTConstants.POSTERS)) {
 			JSONObject jobj = json.getJSONObject(RTConstants.POSTERS);
 			
 			if (jobj.has(RTConstants.THUMBNAIL)) {
 				try {
 					posters[PosterType.THUMBNAIL.getValue()] = new URL(jobj.getString(RTConstants.THUMBNAIL));
 				} catch (MalformedURLException e) {
 					Log.print(e);
 				}
 			}
 			
 			if (jobj.has(RTConstants.PROFILE)) {
 				try {
 					posters[PosterType.PROFILE.getValue()] = new URL(jobj.getString(RTConstants.PROFILE));
 				} catch (MalformedURLException e) {
 					Log.print(e);
 				}
 			}
 			
 			if (jobj.has(RTConstants.DETAILED)) {
 				try {
 					posters[PosterType.DETAILED.getValue()] = new URL(jobj.getString(RTConstants.DETAILED));
 				} catch (MalformedURLException e) {
 					Log.print(e);
 				}
 			}
 			
 			if (jobj.has(RTConstants.ORIGINAL)) {
 				try {
 					posters[PosterType.ORIGINAL.getValue()] = new URL(jobj.getString(RTConstants.ORIGINAL));
 				} catch (MalformedURLException e) {
 					Log.print(e);
 				}
 			}
 		}
 		
 		if (json.has(RTConstants.MOVIE_CAST)) {
 			JSONArray array = json.getJSONArray(RTConstants.MOVIE_CAST);
 			for (Object obj : array) {
 				JSONObject jobj = (JSONObject) obj;
 				
 				String name =  jobj.getString(RTConstants.NAME);
 				
 				JSONArray charsArray = jobj.getJSONArray(RTConstants.CHARACTERS);
 				Set<String> chars = new LinkedHashSet<String>();
 				for (Object c : charsArray) {
 					chars.add((String) c);
 				}
 				
 				cast.add(new Pair<String, Set<String>>(name, chars));
 			}
 		}
 		
 		if (json.has(RTConstants.MOVIE_DIRECTORS)) {
 			JSONArray array = json.getJSONArray(RTConstants.MOVIE_CAST);
 			for (Object obj : array) {
 				directors.add(((JSONObject) obj).getString(RTConstants.NAME));
 			}
 		}
 		
 		if (json.has(RTConstants.ALTERNATE_IDS)) {
 			setImdbDd(json.getJSONObject(RTConstants.ALTERNATE_IDS).getString(RTConstants.IMDB));
 		}
 		
 	}
 
 }
