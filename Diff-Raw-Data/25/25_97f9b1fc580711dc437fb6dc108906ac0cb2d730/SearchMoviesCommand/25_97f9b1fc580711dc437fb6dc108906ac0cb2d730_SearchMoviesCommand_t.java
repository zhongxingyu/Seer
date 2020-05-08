 package dk.kea.si.movies.commands;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 
 import javax.servlet.ServletException;
 
 import dk.kea.si.movies.domain.MovieSearchResults;
 import dk.kea.si.movies.gateways.RottenTomatoesGateway;
 
 public class SearchMoviesCommand  extends FrontCommand {
 	
 	@Override
 	public void process() throws ServletException, IOException {
 		String search = URLEncoder.encode(request.getParameter("search_field"),
 				"UTF-8");
 		String page = request.getParameter("page");
 		MovieSearchResults results = RottenTomatoesGateway.searchMovies(
 				rottenTomatoesApiKey, search, page);
 		request.setAttribute("results", results);
 		forward("/search.jsp");
 	}
 
 	@Override
 	public void processPost() throws ServletException, IOException {
 		process();
 	}
 
 }

