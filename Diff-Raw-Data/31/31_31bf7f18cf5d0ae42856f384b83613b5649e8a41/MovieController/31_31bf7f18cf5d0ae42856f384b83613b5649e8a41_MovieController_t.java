 package com.potatorental.controller;
 
 import com.potatorental.model.Account;
 import com.potatorental.model.Actor;
 import com.potatorental.model.Customer;
 import com.potatorental.model.Movie;
 import com.potatorental.repository.AccountDao;
 import com.potatorental.repository.MovieDao;
 import com.potatorental.repository.PersonsDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.security.Principal;
 import java.util.List;
 
 /**
  * User: milky
  * Date: 5/6/13
  * Time: 8:04 PM
  */
 @Controller
 @RequestMapping("/movies")
 @SessionAttributes("movies")
 public class MovieController {
 
     private MovieDao movieDao;
     private AccountDao accountDao;
     private PersonsDao personsDao;
 
     @Autowired
     public MovieController(MovieDao movieDao, AccountDao accountDao, PersonsDao personsDao) {
         this.movieDao = movieDao;
         this.accountDao = accountDao;
         this.personsDao = personsDao;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView getMovies(ModelMap modelMap) {
         if (modelMap.get("movies") != null)
             return new ModelAndView("movies", modelMap);
 
         modelMap.addAttribute("movies", movieDao.getAllMovies());
 /*        if (num == null)
             modelMap.addAttribute("movies", movieDao.getAllMovies());
         else
             modelMap.addAttribute("movies", movieDao.getNumMovies(num));*/
 
         return new ModelAndView("movies", modelMap);
     }
 
     @RequestMapping(value = "{movieid}", method = RequestMethod.GET)
     public ModelAndView getMovie(@PathVariable int movieid, ModelMap modelMap,
                                  HttpServletRequest request, Principal principal) {
         Movie movie = movieDao.getMovieById(movieid);
         List<Actor> actors = movieDao.getMovieActors(movie);
 
         modelMap.addAttribute("movie", movie);
         modelMap.addAttribute("movieactors", actors);
 
         SecurityContextHolderAwareRequestWrapper sc = new SecurityContextHolderAwareRequestWrapper(request, "ROLE_");
         if (sc.isUserInRole("USER")) {
             Account account = accountDao.getAccount((Customer) personsDao.getPersonByEmail(principal.getName()));
             if (accountDao.isMovieQueued(account, movieid))
                 modelMap.addAttribute("hasMovie", true);
            else
                modelMap.addAttribute("hasMovie", false);
         }
 
         return new ModelAndView("movie", modelMap);
     }
 /*
     @RequestMapping(value = "insert", method = RequestMethod.GET)
     public ModelAndView insertMovies(ModelMap modelMap) {
         BufferedReader reader = null;
         List<Movie> movies = new ArrayList<>();
 
         try {
             reader = new BufferedReader(new FileReader("C:\\Users\\Milky\\IdeaProjects\\" +
                     "PotatoRental\\src\\main\\webapp\\resources\\movies.txt"));
 
             Random ran = new Random();
             String title = null;
             String genre = null;
             do {
                 title = reader.readLine();
                 if (title == null)
                    break;
                 genre = reader.readLine();
 
                 Movie movie = new Movie();
                 movie.setName(title);
                 movie.setType(genre);
                 movie.setNumCopies(ran.nextInt(25));
                 movie.setRating(ran.nextInt(100) < 15 ? ran.nextInt(3) + 2 : ran.nextInt(2) + 4);
                 movie.setDistrFee(ran.nextFloat() * (ran.nextInt(2) * 10000 + 1000) + ran.nextInt(100) * 100);
 
                 movies.add(movie);
 
                 movieDao.insertMovie(movie);
 
             } while (title != null);
 
 
         } catch (IOException e) {
             System.err.println("File not found");
         }
 
         modelMap.addAttribute("movies", movies);
 
         return new ModelAndView("movies", modelMap);
     }*/
 }
