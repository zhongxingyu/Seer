 package edu.ucla.cs.cs144;
 
 import java.io.IOException;
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class SearchServlet extends HttpServlet implements Servlet {
        
     public SearchServlet() {}
 /*
     String[] parseParams(String query, int numParams) {
     	// eg. q=timothy&numResultsToSkip=0&numResultsToReturn=30 
     	String[] params = new String[numParams];
     	int j=0;
     	int k=0;
 
     	int i=0;
     	for (i=0; i<numParams-1;i++) {
     		while (j<query.length()) {
     			if (query.charAt(j) == '=') {
     				break;
     			}
     			j++;
     		}
     		k=j;
     		while (k<query.length()) {
     			if (query.charAt(k) == '&') {
     				break;
     			}
     			k++;
     		}
     		params[i] = query.substring(j+1,k);
     		j=k;
     	}
     	// find last param
     	while (j<query.length()) {
 			if (query.charAt(j) == '=') {
 				break;
 			}
 			j++;
     	}
     	params[i] = query.substring(j+1,query.length());
 
     	return params;
     }
 */
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
 
 	    	request.setAttribute("search", params[0]);
 	    	request.setAttribute("skip", params[1]);
 	    	request.setAttribute("show", params[2]);
 
 	    	int numberShow = Integer.parseInt(params[2]);
 		    AuctionSearchClient sc = new AuctionSearchClient();
 		    SearchResult[] results = sc.basicSearch(params[0], Integer.parseInt(params[1]), numberShow);
 		    String result = "";
 
 	    	request.setAttribute("total", results.length);
 
 		    int length = (results.length<numberShow ? results.length : numberShow);
 		    int num = Integer.parseInt(params[1]);
 		    for (int i=0; i<length;i++) {
 		    	num++;
		    	result += num + ". " + "<a href=\"/eBay/item?itemID=" + results[i].getItemId() + "\">" + results[i].getName() + "</a><br>";
 		    }
 	    	request.setAttribute("result", result);
 
 		    request.getRequestDispatcher("/keywordResult.jsp").forward(request, response);
 
     	}
     	else {
 		    request.getRequestDispatcher("/keywordSearch.html").forward(request, response);
     	}
     }
 }
