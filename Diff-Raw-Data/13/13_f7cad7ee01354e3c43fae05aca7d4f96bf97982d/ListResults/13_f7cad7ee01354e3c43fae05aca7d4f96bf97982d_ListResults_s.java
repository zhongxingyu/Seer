 package Fabflix;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 
 // Servlet implementation class listResults
 public class ListResults extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	public ListResults() {
 		super();
 	}
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in
 
 		response.setContentType("text/html"); // Response mime type
 
 		// Output stream to STDOUT
 		PrintWriter out = response.getWriter();
 
 		ServletContext context = getServletContext();
 		HttpSession session = request.getSession();
 		try {
 
 			Connection dbcon = Database.openConnection();
 
 			String searchBy = request.getParameter("by");// title,letter,genre,year,director
 			String arg = request.getParameter("arg");// search string
 			String order = request.getParameter("order");// t_a,t_d,y_a,y_d
 			Integer page;
 			Integer resultsPerPage;
 
 			// ===Search By
 			try {
 				if (!(searchBy.equals("title") || searchBy.equals("letter") || searchBy.equals("genre") || searchBy.equals("year")
 						|| searchBy.equals("director") || searchBy.equals("first_name") || searchBy.equals("last_name") || searchBy.equals("all"))) {
 					searchBy = "all";
 				}
 			} catch (NullPointerException e) {
 				searchBy = "all";
 			}
 
 			// ===Argument value
 			if (arg == null) {
 				arg = "";
 			}
 
 			if (searchBy.equals("letter")) {
 				if (arg.isEmpty()) {
 					arg = " ";
 				} else {
 					arg = arg.substring(0, 1);// Only take first character for
 					// character search
 				}
 			}
 
 			if (searchBy.equals("title") || searchBy.equals("all")) {
 				try {
 					Pattern.compile(arg);
 				} catch (PatternSyntaxException exception) {
 					arg = "";
 				}
 			}
 
 			
 			// ===SORT
 			String sortBy = "";
 			try {
 				if (order.equals("t_d")) {
 					sortBy = "ORDER BY title DESC";
 				} else if (order.equals("y_d")) {
 					sortBy = "ORDER BY year DESC";
 				} else if (order.equals("y_a")) {
 					sortBy = "ORDER BY year";
 				} else {
 					sortBy = "ORDER BY title"; // DEFAULT to title ascending
 					order = "t_a";
 				}
 			} catch (NullPointerException e) {
 				sortBy = "ORDER BY title"; // DEFAULT to title ascending
 				order = "t_a";
 			}
 
 			// ===Paging
 			try {
 				page = Integer.valueOf(request.getParameter("page"));
 				if (page < 1) {
 					page = 1;
 				}
 			} catch (NumberFormatException e) {
 				page = 1;
 			} catch (NullPointerException e) {
 				page = 1;
 			}
 
 			// ===Results per page
 			try {
 				resultsPerPage = Integer.valueOf(request.getParameter("rpp"));
 				if (resultsPerPage < 1) {
 					resultsPerPage = 5;
 				}
 			} catch (NumberFormatException e) {
 				resultsPerPage = 5;
 			} catch (NullPointerException e) {
 				resultsPerPage = 5;
 			}
 
 			int listStart;
 			if (page > 0) {
 				listStart = (page - 1) * resultsPerPage;
 			} else {
 				listStart = 0;
 				page = 1;
 			}
 
 			String cleanArg = Database.cleanSQL(arg);//CLEAN FOR SQL
 			
 			// Declare our statement
 			Statement statement = dbcon.createStatement();
 			Statement fullStatement = dbcon.createStatement();
 			String query;
 			String fullQuery;// full search to count results
 			if (cleanArg.isEmpty()) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT * FROM movies) AS results";
 			} else if (searchBy.equals("genre")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m LEFT OUTER JOIN genres_in_movies g ON g.movie_id=m.id LEFT OUTER JOIN genres gr ON g.genre_id=gr.id WHERE name = '"
 						+ cleanArg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m LEFT OUTER JOIN genres_in_movies g ON g.movie_id=m.id LEFT OUTER JOIN genres gr ON g.genre_id=gr.id WHERE name = '"
 						+ cleanArg + "') as results";
 			} else if (searchBy.equals("letter")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE title REGEXP '^" + cleanArg + "' " + sortBy + " LIMIT " + listStart
 						+ "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '^" + cleanArg + "') as results";
 			} else if (searchBy.equals("title")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE title REGEXP '" + cleanArg + "' " + sortBy + " LIMIT " + listStart
 						+ "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '" + cleanArg + "') as results";
 			} else if (searchBy.equals("first_name") || searchBy.equals("last_name")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE "
 						+ searchBy + " = '" + cleanArg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE "
 						+ searchBy + " = '" + cleanArg + "') as results";
 			} else if (searchBy.equals("all")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE title REGEXP '" + cleanArg + "' OR year REGEXP '" + cleanArg + "' OR director REGEXP '" + cleanArg + "' OR s1.first_name REGEXP '" + cleanArg + "' OR s1.last_name REGEXP '" + cleanArg + "'   " + sortBy + " LIMIT " + listStart+ "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE  title REGEXP '" + cleanArg + "' OR year REGEXP '" + cleanArg + "' OR director REGEXP '" + cleanArg + "' OR s1.first_name REGEXP '" + cleanArg + "' OR s1.last_name REGEXP '" + cleanArg + "' ) as results";
 			} else {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE " + searchBy + " = '" + cleanArg + "' " + sortBy + " LIMIT "
 						+ listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE " + searchBy + " = '" + cleanArg + "') as results";
 			}
 
 			
 			// Get results for this page's display
 			ResultSet searchResults = statement.executeQuery(query);
 
 			// Find total number of results
 			ResultSet fullCount = fullStatement.executeQuery(fullQuery);
 			fullCount.next();
 			int numberOfResults = fullCount.getInt(1);
 			int numberOfPages = numberOfResults / resultsPerPage + (numberOfResults % resultsPerPage == 0 ? 0 : 1);
 
 			// Adjust page if beyond scope of the results; redirect to last page
 			// of search
 			if (numberOfResults > 0 && page > numberOfPages) {
 				response.sendRedirect("ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + numberOfPages + "&rpp="
 						+ resultsPerPage + "&order=" + order);
 			}
 
 			// ===Start Writing Page===========================================
 
 			// TITLE
 
 			session.setAttribute("title", "Search by " + searchBy + ": " + arg);
 
 			out.println(Page.header(context, session));
 			out.println("<div class=\"list-results\">");
 			// BODY
 
 			out.println("<H2>Search by " + searchBy + ": " + arg + "</H2>");
 			out.println(Page.browseMenu(searchBy, arg, order, page, resultsPerPage));
 
 			if (numberOfResults > 0) {// if results exist
 				out.println("( " + numberOfResults + " Results )");
 				showRppOptions(out, searchBy, arg, order, page, resultsPerPage);
 				out.println("<BR>");
 				if (numberOfPages > 1) {
 					showPageControls(out, searchBy, arg, order, page, resultsPerPage, numberOfPages);
					out.println("<BR><hr>");
 				}
 			}
 
 			while (searchResults.next()) {// For each movie, DISPLAY INFORMATION
 				Integer movieID;
 				try {
 					movieID = Integer.valueOf(searchResults.getString("id"));
 				} catch (Exception e) {
 					movieID = 0;
 				}
 				String title = searchResults.getString("title");
 				Integer year = searchResults.getInt("year");
 				String bannerURL = searchResults.getString("banner_url");
 				String director = searchResults.getString("director");
 
 				out.println("<a href=\"MovieDetails?id=" + movieID + "\"><h2>" + title + " (" + year + ")");
 				Page.addToCart(out, movieID);
 				out.println("</h2><img src=\"" + bannerURL + "\" height=\"200\" alt=\""+title+"\" width=\"200px\"></a>");
 
 				out.println("<div class=\"info\"><ul>");
 				out.println("<li>ID</li><li><a href=\"MovieDetails?id=" + movieID + "\">" + movieID + "</a></li></ul>");
 				
 				out.println("<ul><li>Year</li><li>");
 				listByYearLink(out, year, resultsPerPage);
 				out.println("</li></ul>");
 
 				out.println("<ul><li>Director</li><li>");
 				listByDirectorLink(out, director, resultsPerPage);
 				out.println("</li></ul>");
 
 				out.println("<ul><li>Genres</li><li>");
 				listGenres(out, dbcon, resultsPerPage, movieID);
 				out.println("</li></ul>");
 
 				out.println("<ul><li>Stars</li><li>");
 				listStars(out, dbcon, resultsPerPage, movieID);
 				out.println("</li></ul>");
 
 				// String target = (String) session.getAttribute("user.dest");
 
 				out.println("</div><HR>");
 			}
 
 			if (numberOfResults > 0) {
 				// show prev/next
 				if (numberOfPages > 1) {
 					showPageControls(out, searchBy, arg, order, page, resultsPerPage, numberOfPages);
 					out.println("<BR>");
 				}
 
 				// Results per page Options
 				showRppOptions(out, searchBy, arg, order, page, resultsPerPage);
 
 				out.println("<BR>");
 
 			} else {
 				out.println("<H3>No Results.</H3>");
 			}
 			
 			out.println("</div>");
 			Page.footer(out);
 
 			searchResults.close();
 			statement.close();
 			fullStatement.close();
 			dbcon.close();
 
 		} catch (SQLException ex) {
 			out.println(Page.header(context, session));
 			while (ex != null) {
 				out.println("SQL Exception:  " + ex.getMessage());
 				ex = ex.getNextException();
 			} // end while
 			out.println("</DIV></BODY></HTML>");
 		} // end catch SQLException
 		catch (java.lang.Exception ex) {
 			out.println(Page.header(context, session));
 			out.println("<P>SQL error in doGet: " + ex.getMessage() + "<br>"
 					+ ex.toString() + "</P></DIV></BODY></HTML>");
 			return;
 		}
 		out.close();
 	}
 //	
 //	public static void listByYearLink(PrintWriter out, Integer year) {
 //		listByYearLink(out, year, 0);
 //	}
 
 	public static void listByYearLink(PrintWriter out, Integer year, Integer rpp) {
 		out.println("<a href=\"ListResults?by=year&arg=" + year + "&rpp=" + rpp + "\">" + year + "</a>");
 	}
 
 //	public static void listByDirectorLink(PrintWriter out, String director) throws UnsupportedEncodingException {
 //		listByDirectorLink(out, director, 0);
 //	}
 
 	public static void listByDirectorLink(PrintWriter out, String director, Integer rpp) throws UnsupportedEncodingException {
 		out.println("<a href=\"ListResults?by=director&arg=" + java.net.URLEncoder.encode(director, "UTF-8") + "&rpp=" + rpp + "\">" + director
 				+ "</a>");
 	}
 
 	private void showPageControls(PrintWriter out, String searchBy, String arg, String order, Integer page, Integer resultsPerPage, Integer numberOfPages)
 			throws UnsupportedEncodingException {
 		// ===Paging
 
 		if (page != 1) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=1&rpp=" + resultsPerPage
 					+ "&order=" + order + "\">First</a>");
 		} else {
 			out.println("Last");
 		}
 
 		out.println(" | ");
 
 		if (page > 1) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + (page - 1) + "&rpp="
 					+ resultsPerPage + "&order=" + order + "\">Prev</a>");
 		} else {
 			out.println("Prev");
 		}
 
 		out.println("| Page: " + page + " of " + numberOfPages + " |");
 
 		if (page >= numberOfPages) {
 			out.println("Next");
 		} else {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + (page + 1) + "&rpp="
 					+ resultsPerPage + "&order=" + order + "\">Next</a>");
 		}
 
 		out.println(" | ");
 
 		if (page < numberOfPages) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + numberOfPages + "&rpp="
 					+ resultsPerPage + "&order=" + order + "\">Last</a>");
 		} else {
 			out.println("Last");
 		}
 	}
 
 	public static void searchTitlesBox(PrintWriter out, String arg, Integer resultsPerPage) {
 		// ===Search Box
 		out.println("<FORM ACTION=\"ListResults\" METHOD=\"GET\">  Search Titles (RegEx): <INPUT TYPE=\"TEXT\" NAME=\"arg\" VALUE=\""+arg+"\">"
 				+ "<INPUT TYPE=\"HIDDEN\" NAME=rpp VALUE=\"" + resultsPerPage + "\"><INPUT TYPE=\"SUBMIT\" VALUE=\"Search\">");
 		AdvancedSearch.advancedSearchButton(out);
 		out.println("</FORM>");
 	}
 
 	public static void searchTitlesBox(PrintWriter out, Integer resultsPerPage) {
 		// ===Search Box
 		out.println("<FORM ACTION=\"ListResults\" METHOD=\"GET\">  Search Titles (RegEx): <INPUT TYPE=\"TEXT\" NAME=\"arg\">"
 				+ "<INPUT TYPE=\"HIDDEN\" NAME=rpp VALUE=\"" + resultsPerPage + "\"><INPUT TYPE=\"SUBMIT\" VALUE=\"Search\">");
 		AdvancedSearch.advancedSearchButton(out);
 		out.println("</FORM>");
 	}
 
 	private void showRppOptions(PrintWriter out, String searchBy, String arg, String order, Integer page, Integer resultsPerPage)
 			throws UnsupportedEncodingException {
 		// ===Results per page
 		out.println("Results per page: ");
 
 		if (!(resultsPerPage == 5)) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=5&order=" + order
 					+ "\">5</a>");
 		} else {
 			out.println("5");
 		}
 
 		if (!(resultsPerPage == 25)) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=25&order="
 					+ order + "\">25</a>");
 		} else {
 			out.println("25");
 		}
 
 		if (!(resultsPerPage == 100)) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=100&order="
 					+ order + "\">100</a>");
 		} else {
 			out.println("100");
 		}
 	}
 
 	public static void listStars(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException {
 		Statement statement = dbcon.createStatement();
 		// ===STARS; comma separated list
 		out.println("<ul class=\"list\">");
 		ResultSet stars = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 " + "WHERE s.movie_id=m.id "
 				+ "AND s.star_id=s1.id " + "AND m.id = '" + movieID + "' ORDER BY last_name");
 		if (stars.next()) {
 			String starName = stars.getString("first_name") + " " + stars.getString("last_name");
 			String starID = stars.getString("star_id");
 			out.println("<li><a href=\"StarDetails?id=" + starID + "\">" + starName + "</a></li>");
 			while (stars.next()) {
 				starName = stars.getString("first_name") + " " + stars.getString("last_name");
 				starID = stars.getString("star_id");
 				out.println("<li><a href=\"StarDetails?id=" + starID + "\">" + starName + "</a></li>");
 			}
 			out.println("</ul>");
 		}
 		stars.close();
 		statement.close();
 	}
 
 	public static void listStarsIMG(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID, Boolean edit) throws SQLException {
 		Statement statement = dbcon.createStatement();
 		// ===STARS; list of images
 //		out.println("Stars: ");
 		out.println("<ul class=\"list\">");
 		if (edit) {
 			out.println("<li>");
 			EditMovie.addStarGenreLink(out, movieID, "star ID");
 			out.println("</li>");
 		}
 //		out.println("<BR><BR>");
 		ResultSet stars = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 WHERE s.movie_id=m.id AND s.star_id=s1.id AND m.id = '" + movieID
 						+ "' ORDER BY last_name");
 		while (stars.next()) {
 			String starName = stars.getString("first_name") + " " + stars.getString("last_name");
 			String starIMG = stars.getString("photo_url");
 			int starID = stars.getInt("star_id");
 			out.println("<li>");
 			out.println("<a href=\"StarDetails?id=" + starID + "\">" + "<img class=\"star\" src=\"" + starIMG + "\" height=\"120\">" + starName + "</a>");
 			if (edit) {
 				EditMovie.removeStarGenreLink(out, movieID, starID, "star", starName);
 			}
 			out.println("</li>");
 //			out.println("<BR>");
 		}
 		out.println("</ul>");
 		stars.close();
 		statement.close();
 	}
 
 	public static void listMoviesIMG(PrintWriter out, Connection dbcon, Integer rpp, Integer starID, Boolean edit) throws SQLException {
 		Statement statement = dbcon.createStatement();
 		out.println("Starred in:");
 		if (edit) {
 			EditStar.addMovieLink(out, starID, "movie");
 		}
 		out.println("<BR><BR>");
 		ResultSet movies = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 " + "WHERE s.movie_id=m.id "
 				+ "AND s.star_id=s1.id " + "AND s1.id = '" + starID + "' ORDER BY year DESC");
 
 		while (movies.next()) {
 			String title = movies.getString("title");
 			Integer year = movies.getInt("year");
 			Integer movieID = movies.getInt("movie_id");
 			String bannerURL = movies.getString("banner_url");
 
 			out.println("<a href=\"MovieDetails?id=" + movieID + "\"><img src=\"" + bannerURL + "\" height=\"200\" alt=\""+title+"\">" + title + " (" + year + ")" + "</a>");
 			if (edit){
 				EditStar.removeMovieLink(out, starID, movieID, title + " ("+year+")");
 			}else{
 				Page.addToCart(out, movieID);
 			}
 			out.println("<BR><BR>");
 		}
 
 	}
 
 	public static void listGenres(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException, UnsupportedEncodingException {
 		listGenres(out, dbcon, rpp, movieID, false);
 	}
 
 	public static void listGenres(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID, Boolean edit) throws SQLException,
 			UnsupportedEncodingException {
 		// ===GENRES; comma separated list
 //		out.println("Genre: ");
 		out.println("<ul class=\"list\">");
 		if (edit) {
 			out.println("<li>");
 			EditMovie.addStarGenreLink(out, movieID, "genre");
 			out.println("</li>");
 		}
 		Statement statement = dbcon.createStatement();
 		ResultSet genres = statement.executeQuery("SELECT DISTINCT name,genre_id FROM movies m, genres_in_movies g, genres g1 WHERE g.movie_id=m.id AND g.genre_id=g1.id AND m.id ='"
 						+ movieID + "' ORDER BY name");
 		if (genres.next()) {
 			String genre = genres.getString("name").trim();
 			Integer delID = genres.getInt("genre_id");
 			out.println("<li>");
 			out.println("<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genre, "UTF-8") + "&rpp=" + rpp + "\">" + genre + "</a>");
 			if (edit) {
 				EditMovie.removeStarGenreLink(out, movieID, delID, "genre", genre);
 			}
 			out.println("</li>");
 			while (genres.next()) {
 				genre = genres.getString("name").trim();
 				delID = genres.getInt("genre_id");
 				out.println("<li>");
 				out.println("<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genre, "UTF-8") + "&rpp=" + rpp + "\">" + genre + "</a>");
 				if (edit) {
 					EditMovie.removeStarGenreLink(out, movieID, delID, "genre", genre);
 				}
 				out.println("</li>");
 			}
 		}
 		out.println("</ul>");
 		genres.close();
 		statement.close();
 	}
 
 }
