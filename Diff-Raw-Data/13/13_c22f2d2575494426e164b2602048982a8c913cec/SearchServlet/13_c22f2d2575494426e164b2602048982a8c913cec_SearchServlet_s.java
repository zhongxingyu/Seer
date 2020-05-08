 package edu.ucla.cs.cs144;
 
 import java.io.IOException;
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class SearchServlet extends HttpServlet implements Servlet {
        
     public SearchServlet() {}
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
     	String query = request.getQueryString();
 
         // your codes here
        String pageTitle = "My Search Servlet";
 	    request.setAttribute("title", pageTitle);
 
 		if (query != null) {
 			String[] params = new String[3];
 			if(request.getParameter("q") == null)
                 params[0] = "";
 	        else
                 params[0] = request.getParameter("q");
             if(request.getParameter("numResultsToSkip") == null)
                 params[1] = "0";
 	        else
                 params[1] = request.getParameter("numResultsToSkip");
             if(request.getParameter("numResultsToReturn") == null)
                 params[2] = "10";
 	        else
                 params[2] = request.getParameter("numResultsToReturn");
 
 		    String result = "";
 
 		    if (params[0].equals("")){
 		    	request.setAttribute("total", 0);
 		    	request.setAttribute("result", "");
 		    }
 		    else 
 		    {
 		    	request.setAttribute("search", params[0]);
 		    	request.setAttribute("skip", params[1]);
 		    	request.setAttribute("show", params[2]);
 
 		    	int numberShow = Integer.parseInt(params[2]);
 			    AuctionSearchClient sc = new AuctionSearchClient();
 			    SearchResult[] results = sc.basicSearch(params[0], Integer.parseInt(params[1]), numberShow);
 
 		    	request.setAttribute("total", results.length);
 		    	int length = (results.length<numberShow ? results.length : numberShow);
 			    int num = Integer.parseInt(params[1]);
 			    for (int i=0; i<length;i++) {
 			    	num++;
 			    	result += num + ". " + "<a href=\"/eBay/item?itemID=" + results[i].getItemId() + "\">" + results[i].getName() + "</a><br>";
 			    }
 		    	request.setAttribute("result", result);
 		    }
 
 		    request.getRequestDispatcher("/keywordResult.jsp").forward(request, response);
     	}
     	else {
 		    request.getRequestDispatcher("/keywordSearch.html").forward(request, response);
     	}
     }
 }
