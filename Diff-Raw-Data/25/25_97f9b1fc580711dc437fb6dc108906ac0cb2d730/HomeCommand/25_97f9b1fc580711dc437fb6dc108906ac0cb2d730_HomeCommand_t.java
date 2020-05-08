 package dk.kea.si.movies.commands;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 
 import dk.kea.si.movies.domain.MovieSearchResults;
 import dk.kea.si.movies.gateways.RottenTomatoesGateway;
 
 public class HomeCommand extends FrontCommand {
 
 	@Override
 	public void process() throws ServletException, IOException {
 		MovieSearchResults results = RottenTomatoesGateway
 				.findCurrentlyInTheaters(rottenTomatoesApiKey);
 		request.setAttribute("inTheaters", results);
 		
 		MovieSearchResults comingSoon = RottenTomatoesGateway
 				.findComingSoon(rottenTomatoesApiKey);
 		request.setAttribute("comingSoon", comingSoon);
 		
 		MovieSearchResults opening = RottenTomatoesGateway
 				.findOpening(rottenTomatoesApiKey);
 		request.setAttribute("opening", opening);
 		
 		forward("/home.jsp");
 	}
 
 	@Override
 	public void processPost() throws ServletException, IOException {
 		// TODO Auto-generated method stub
 
 	}
 
}

