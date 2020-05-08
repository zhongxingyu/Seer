 package ex2;
 
 import ex1.RateableMovie;
 import ex1.Watchable;
 
 public class UserRatedMovie implements RateableMovie
 {
 	private final Movie movie;
 	private final int rating;
 
	public UserRatedMovie(Watchable movie, int rating)
 	{
 		super();
 		this.movie = movie;
 		this.rating = rating;
 	}
 	
 	public Watchable getMovie()
 	{
 		return movie;
 	}
 	
 	public int getRating()
 	{
 		return rating;
 	}
 
 }
