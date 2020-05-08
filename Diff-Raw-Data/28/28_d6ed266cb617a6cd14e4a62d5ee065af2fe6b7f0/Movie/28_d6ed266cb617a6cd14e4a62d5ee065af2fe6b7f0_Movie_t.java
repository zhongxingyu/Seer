 package parsing;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 public class Movie{
 
 
 	private String name; // the "clean" name of the movie
 	private String id; // the movie name as appear in yago files (with "<" ">" and "(film)"
 	private String duration; // the movie duration in minutes
 	private String dateCreated; // the year created
 	private Set<String> genreList = new LinkedHashSet<String>(); // the list of genres
 	private String language; // need to do
 	private Person director; // the director of the movie
 	private List<Person> actorsLst = new ArrayList<Person>();// actors list
 	private String wikiURL; // wikipedia url
 	private String plot; // plot of the movie
 	private String youTubeURL; // need to do
 
 
 	/* constructor */	
 
 	public Movie(String id){
 		this.id = id;
 	}
 
 
 	/** adding an actor to the actors List */
 	public void addActor(Person actor) {
 		if(actor != null)
 			this.actorsLst.add(actor);
 	}
 	
 	/** adding genre to the genreList */
 	public void addGenre(String genre) {
 		this.genreList.add(genre);
 	}
 	
 	
 	@Override
 	public String toString() {
 		return "id= " + getId() + ",   name=" + name + "\n" + "actors= " + actorsLst +"\n"
 				+ "director= " + getDirector() +"\n" + "year= " + getDateCreated()
 				+ ", duration= " + getDuration() + "\n"+ "wiki= " + getWikiURL() +"\n" +
				"generes: " + getGenre() + " language: "+ getLanguage() + "\n"+
				"Plot: " + getPlot() + "\n";
 	}
 
 	
 
 	/* getters & setters */
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDuration() {
 		return duration;
 	}
 
 	public void setDuration(String duration) {
 		this.duration = duration;
 	}
 
 	public Set<String> getGenre() {
 		return genreList;
 	}
 
 	public void setGenre(Set<String> genreList) {
 		this.genreList = genreList;
 	}
 
 	public String getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	public Person getDirector() {
 		return director;
 	}
 
 	public void setDirector(Person director) {
 		this.director = director;
 	}
 
 	public List<Person> getActorsLst() {
 		return actorsLst;
 	}
 
 	public String getWikiURL() {
 		return wikiURL;
 	}
 
 	public void setWikiURL(String wikiURL) {
 		this.wikiURL = wikiURL;
 	}
 
 	public String getYouTubeURL() {
 		return youTubeURL;
 	}
 
 	public void setYouTubeURL(String youuTubeURL) {
 		this.youTubeURL = youuTubeURL;
 	}
 
 
 	public String getDateCreated() {
 		return dateCreated;
 	}
 
 
 	public void setDateCreated(String dateCreated) {
 		this.dateCreated = dateCreated;
 	}
 	
 	
 	public String getPlot() {
 		return plot;
 	}
 
 	
 	public void setPlot(String plot) {
 		this.plot = plot;
 	}
 
 
 	public String getId() {
 		return id;
 	}
 
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 }
