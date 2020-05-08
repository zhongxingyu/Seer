 package Fabflix;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 
 /**
  * Servlet implementation class AdvancedSearch
  */
 public class AdvancedSearch extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public AdvancedSearch() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in
 
 		response.setContentType("text/html"); // Response mime type
 
 		// Output stream to STDOUT
 		PrintWriter out = response.getWriter();
 		ServletContext context = getServletContext();
 		HttpSession session = request.getSession();
 		try {
 			
 			Connection dbcon = Database.openConnection();
 
 			// Get parameters
 			String t = request.getParameter("t");
 			Integer y = 0;
 			try {
 				y = Integer.valueOf(request.getParameter("y"));
 			} catch (Exception e) {
 				y = 0;
 			}
 			String d = request.getParameter("d");
 			String fn = request.getParameter("fn");
 			String ln = request.getParameter("ln");
 			String sub = request.getParameter("sub");
 
 			// ===SORT
 			String sortBy = "";
 			String order = request.getParameter("order");
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
 			Integer page;
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
 			Integer resultsPerPage;
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
 
 			Integer paramCount = 0;
 			if (!(t == null || t.isEmpty())) {
 				paramCount++;
 			} else {
 				t = "";
 			}
 			if (y != 0) {
 				paramCount++;
 			}
 			if (!(d == null || d.isEmpty())) {
 				paramCount++;
 			} else {
 				d = "";
 			}
 			if (!(fn == null || fn.isEmpty())) {
 				paramCount++;
 			} else {
 				fn = "";
 			}
 			if (!(ln == null || ln.isEmpty())) {
 				paramCount++;
 			} else {
 				ln = "";
 			}
 			
 			if (sub == null){sub = "";}
 
 			String searchString = "t=" + java.net.URLEncoder.encode(t, "UTF-8") + "" + "&y=" + y + "&d=" + java.net.URLEncoder.encode(d, "UTF-8") + "&fn="
 					+ java.net.URLEncoder.encode(fn, "UTF-8") + "&ln=" + java.net.URLEncoder.encode(ln, "UTF-8") + "&sub=" + sub;
 
 
 			t = Database.cleanSQL(t);
 			d = Database.cleanSQL(d);
 			fn = Database.cleanSQL(fn);
 			ln = Database.cleanSQL(ln);
 			
 			// If no parameter, show search; If one parameter, do basic search
 			if (paramCount == 0) {
 				// ===Advanced Search Form
 				session.setAttribute("title", "Advanced Search");
 				
 				out.println(Page.header(context, session));
 				
 				out.println("<h1>Advanced Search</h1>");
 
 				out.println("<FORM ACTION=\"AdvancedSearch\" METHOD=\"GET\">" + "Title: <INPUT TYPE=\"TEXT\" NAME=\"t\"><BR>"
 						+ "Year: <INPUT TYPE=\"TEXT\" NAME=\"y\"><BR>" + "Director: <INPUT TYPE=\"TEXT\" NAME=\"d\"><BR>"
 						+ "Star's First Name: <INPUT TYPE=\"TEXT\" NAME=\"fn\"><BR>" 
 						+ "Star's Last Name: <INPUT TYPE=\"TEXT\" NAME=\"ln\"><BR>"
 						+ "Substring Search: <INPUT TYPE=\"CHECKBOX\" NAME=\"sub\" checked><BR>"
 						+ "<INPUT TYPE=\"HIDDEN\" NAME=rpp VALUE=\"" + resultsPerPage
 						+ "\"><INPUT TYPE=\"SUBMIT\" VALUE=\"Search\"> <INPUT TYPE=\"RESET\" VALUE=\"Reset\"> </FORM>");
 				Page.footer(out);
 				out.println("</body></html>");
 			} else if (paramCount == 1 && sub.isEmpty()) {
 				// Redirect to simple search for single parameter
 				if (!(t == null || t.isEmpty())) {
 					response.sendRedirect("ListResults?by=title&arg=" + java.net.URLEncoder.encode(t, "UTF-8"));
 				} else if (y != 0) {
 					response.sendRedirect("ListResults?by=year&arg=" + y);
 				} else if (!(d == null || d.isEmpty())) {
 					response.sendRedirect("ListResults?by=director&arg=" + java.net.URLEncoder.encode(d, "UTF-8"));
 				} else if (!(fn == null || fn.isEmpty())) {
 					response.sendRedirect("ListResults?by=first_name&arg=" + java.net.URLEncoder.encode(fn, "UTF-8"));
 				} else if (!(ln == null || ln.isEmpty())) {
 					response.sendRedirect("ListResults?by=last_name&arg=" + java.net.URLEncoder.encode(ln, "UTF-8"));
 				}
 
 			} else {
 
 				Statement statement = dbcon.createStatement();
 				Statement fullStatement = dbcon.createStatement();
 				String searchArg = "";
 				String query = "";
 				String fullQuery = "";
 				Boolean firstCond = true;
 
 				if (!(t == null || t.isEmpty())) {
 					if (firstCond){
 						firstCond=false;
 					}else{
 						searchArg += " AND ";
 					}
 					if (sub.equals("on")){
 						searchArg += "title LIKE '%" + t + "%' ";
 					}else{
 						searchArg += "title = '" + t + "' ";
 					}
 				}
 				if (y != 0) {
 					if (firstCond){
 						firstCond=false;
 					}else{
 						searchArg += "AND ";
 					}
 					searchArg += "year = " + y + " ";
 				}
 				if (!(d == null || d.isEmpty())) {
 					if (firstCond){
 						firstCond=false;
 					}else{
 						searchArg += "AND ";
 					}
 					if (sub.equals("on")){
 						searchArg += "director LIKE '%" + d + "%' ";
 					}else{
 						searchArg += "director = '" + d + "' ";
 					}
 				}
 				if (!(fn == null || fn.isEmpty())) {
 					if (firstCond){
 						firstCond=false;
 					}else{
 						searchArg += "AND ";
 					}
 					if (sub.equals("on")){
 						searchArg += "first_name LIKE '%" + fn + "%' ";
 					}else{
 						searchArg += "first_name = '" + fn + "' ";
 					}
 				}
 				if (!(ln == null || ln.isEmpty())) {
 					if (firstCond){
 						firstCond=false;
 					}else{
 						searchArg += "AND ";
 					}
 					if (sub.equals("on")){
 						searchArg += "last_name LIKE '%" + ln + "%' ";
 					} else {
 						searchArg += "last_name = '" + ln + "' ";
 					}
 				}
 
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE "
 					+ searchArg + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (" 
 					+ "SELECT DISTINCT m.id FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE "
 						+ searchArg + ") as results";
 
 				// Get results for this page's display
 				ResultSet searchResults = statement.executeQuery(query);
 
 				// Find total number of results
 				ResultSet fullCount = fullStatement.executeQuery(fullQuery);
 				fullCount.next();
 				int numberOfResults = fullCount.getInt(1);
 				int numberOfPages = numberOfResults / resultsPerPage + (numberOfResults % resultsPerPage == 0 ? 0 : 1);
 
 				// Adjust page if beyond scope of the results; 
 				// redirect to last page of search
 				if (numberOfResults > 0 && page > numberOfPages) {
 					response.sendRedirect("AdvancedSearch?" + searchString + "&page=" + numberOfPages + "&rpp=" + resultsPerPage + "&order=" + order);
 				}
 
 				// Open HTML
 
 
 				session.setAttribute("title", "Advanced Search");
 
 				// BODY
 
 				out.println(Page.header(context, session));
 				out.println("<div class=\"list-results\">");
 
 				out.println("<H2>Advanced Search</H2>"); // Show search options
 				
 				if (numberOfResults > 0) {// if results exist
 					out.println("( " + numberOfResults + " Results )");
 					showRppOptions(out, searchString, order, page, resultsPerPage);
 					out.println("<BR>");
 					if (numberOfPages > 1) {
 						showPageControls(out, searchString, order, page, resultsPerPage, numberOfPages);
						out.println("<BR><hr>");
 					}
 				}
 				while (searchResults.next()) {// For each movie, DISPLAY
 					// INFORMATION
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
 
 					out.println("<a href=\"MovieDetails?id=" + movieID + "\"><h2>" + title);
 					Page.addToCart(out, movieID);
 					out.println("</h2><img src=\"" + bannerURL + "\" height=\"200\"></a>");
 					
 					out.println("<div class=\"info\"><ul>");	
 					out.println("<li>ID</li><li><a href=\"MovieDetails?id=" + movieID + "\">" + movieID + "</a></li></ul>");
 					
 					out.println("<ul><li>Year</li><li>");
 					ListResults.listByYearLink(out, year, resultsPerPage);
 					out.println("</li></ul>");
 
 					out.println("<ul><li>Director</li><li>");
 					ListResults.listByDirectorLink(out, director, resultsPerPage);
 					out.println("</li></ul>");
 
 					out.println("<ul><li>Genres</li><li>");
 					ListResults.listGenres(out, dbcon, resultsPerPage, movieID);
 					out.println("</li></ul>");
 
 					out.println("<ul><li>Stars</li><li>");
 					ListResults.listStars(out, dbcon, resultsPerPage, movieID);
 					out.println("</li></ul>");
 
 
 					out.println("</div><HR>");
 
 				}
 
 				if (numberOfResults > 0) {
 					// show prev/next
 					if (numberOfPages > 1) {
 						showPageControls(out, searchString, order, page, resultsPerPage, numberOfPages);
 						out.println("<BR>");
 					}
 					
 					// Results per page Options
 					showRppOptions(out, searchString, order, page, resultsPerPage);
 
 					out.println("<BR>");
 
 				} else {
 					out.println("<H3>No Results.</H3>");
 				}
 
 				out.println("</div>");
 				Page.footer(out);
 
 				searchResults.close();
 				statement.close();
 				dbcon.close();
 			}
 
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
 
 	private void showSortOptions(PrintWriter out, String searchString, String order, Integer page, Integer resultsPerPage) {
 		// sorting and results per page options
 		out.println("Sort by: Title(");
 
 		if (!order.equals("t_a")) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + page + "&rpp=" + resultsPerPage + "&order=t_a\">asc</a>");
 		} else {
 			out.println("asc");
 		}
 
 		out.println(")(");
 
 		if (!order.equals("t_d")) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + page + "&rpp=" + resultsPerPage + "&order=t_d\">des</a>");
 		} else {
 			out.println("des");
 		}
 
 		out.println(") Year(");
 
 		if (!order.equals("y_a")) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + page + "&rpp=" + resultsPerPage + "&order=y_a\">asc</a>");
 		} else {
 			out.println("asc");
 		}
 
 		out.println(")(");
 
 		if (!order.equals("y_d")) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + page + "&rpp=" + resultsPerPage + "&order=y_d\">des</a>");
 		} else {
 			out.println("des");
 		}
 
 		out.println(")");
 	}
 
 	private void showPageControls(PrintWriter out, String searchString, String order, Integer page, Integer resultsPerPage, int numberOfPages) {
 		// Paging
 		if (page != 1) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=1&rpp=" + resultsPerPage + "&order=" + order + "\">First</a>");
 		} else {
 			out.println("Last");
 		}
 
 		out.println(" | ");
 
 		if (page > 1) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + (page - 1) + "&rpp=" + resultsPerPage + "&order=" + order + "\">Prev</a>");
 		} else {
 			out.println("Prev");
 		}
 
 		out.println("| Page: " + page + " of " + numberOfPages + " |");
 
 		if (page >= numberOfPages) {
 			out.println("Next");
 		} else {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + (page + 1) + "&rpp=" + resultsPerPage + "&order=" + order + "\">Next</a>");
 		}
 
 		out.println(" | ");
 
 		if (page < numberOfPages) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + numberOfPages + "&rpp=" + resultsPerPage + "&order=" + order + "\">Last</a>");
 		} else {
 			out.println("Last");
 		}
 	}
 
 	private void showRppOptions(PrintWriter out, String searchString, String order, Integer page, Integer resultsPerPage) {
 		// ===Results per page
 		out.println("Results per page: ");
 
 		if (!(resultsPerPage == 5)) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + page + "&rpp=5&order=" + order + "\">5</a>");
 		} else {
 			out.println("5");
 		}
 
 		if (!(resultsPerPage == 25)) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + page + "&rpp=25&order=" + order + "\">25</a>");
 		} else {
 			out.println("25");
 		}
 
 		if (!(resultsPerPage == 100)) {
 			out.println("<a href=\"AdvancedSearch?" + searchString + "&page=" + page + "&rpp=100&order=" + order + "\">100</a>");
 		} else {
 			out.println("100");
 		}
 	}
 
 	public static void advancedSearchButton(PrintWriter out) {
 		out.println("<a href=\"AdvancedSearch\">Advanced Search</a>");
 
 	}
 
 }
