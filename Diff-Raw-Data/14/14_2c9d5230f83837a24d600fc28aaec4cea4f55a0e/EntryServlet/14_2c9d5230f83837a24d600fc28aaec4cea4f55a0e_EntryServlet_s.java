 package com.web.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.mapper.dataObjects.ResultDAO;
 import com.mapper.relationMatcher.WebTupleProcessor;
 import com.mapper.search.QueryEngine;
 import com.mapper.utility.Constants;
 
 /**
  * Servlet class to handle requests for testing the matching performance
  * 
  * @author Arnab Dutta
  */
 public class EntryServlet extends HttpServlet
 {
 
     // define Logger
     static Logger logger = Logger.getLogger(EntryServlet.class.getName());
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Constructor of the object.
      */
     public EntryServlet()
     {
         super();
     }
 
     /**
      * Destruction of the servlet. <br>
      */
     public void destroy()
     {
         super.destroy(); // Just puts "destroy" string in log
         // Put your code here
     }
 
     /**
      * The doPost method of the servlet. <br>
      * This method is called when a form has its tag value method equals to post.
      * 
      * @param request the request send by the client to the server
      * @param response the response send by the server to the client
      * @throws ServletException if an error occurred
      * @throws IOException if an error occurred
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
 
         List<ResultDAO> retListPredLookUp = new ArrayList<ResultDAO>();
         List<ResultDAO> retListPredSearch = new ArrayList<ResultDAO>();
         List<ResultDAO> retListObj = new ArrayList<ResultDAO>();
         List<ResultDAO> retListSubj = new ArrayList<ResultDAO>();
 
         List<List<ResultDAO>> retList = new ArrayList<List<ResultDAO>>();
         List<ResultDAO> retListPred = new ArrayList<ResultDAO>();
 
         // initialize with some default values
         int topK = Constants.TOPK;
 
         // we just need two threads to perform the search
         ExecutorService pool = Executors.newFixedThreadPool(2);
 
         try {
             if (request.getParameter("topk") != null) {
                 topK = Integer.parseInt(request.getParameter("topk"));
             }
 
             QueryEngine.setTopK(topK);
 
             String subject = request.getParameter("subject");
             String predicate = request.getParameter("predicate");
             String object = request.getParameter("object");
 
             // This is simple search mode. just qyerying for terms
             if (!Constants.PREDICTIVE_SEARCH_MODE) {
                 // fetch the answer terms
                 if (!subject.equals("Subject") && !subject.equals("") && !object.equals("Object") && !object.equals("")) {
 
                     WebTupleProcessor webTupleProc = new WebTupleProcessor(pool, subject, object, predicate);
                     // make a call to the Engine with the required parameters
                     webTupleProc.processTuples(null);
                     retList = webTupleProc.getRetList();
 
                     retListSubj = retList.get(0);
                     retListObj = retList.get(1);
                 } else {
                     if (!subject.equals("Subject") && !subject.equals("")) {
                         retListSubj = QueryEngine.doSearch(subject);
                     }
                     if (!object.equals("Object") && !object.equals("")) {
                         retListObj = QueryEngine.doSearch(object);
                     }
                 }
                // we will predict the predicate based on the knowledge we have learned from other IE data sets
                // if not then we go for lexical match
                if (!predicate.equals("Predicate") && !predicate.equals("")) {
                    // do lookup from learnt knowledge
                    retListPredLookUp = QueryEngine.doLookUpSearch(predicate);
                    // do lexical match
                    retListPredSearch = QueryEngine.doSearch(predicate);

                }
 
             } else { // This is advanced search mode. where the system tries to predict the best matches based on the
                      // input combination
                 if (!subject.equals("Subject") && !subject.equals("") && !object.equals("Object") && !object.equals("")) {
 
                     WebTupleProcessor webTupleProc = new WebTupleProcessor(pool, subject, object, predicate);
                     // make a call to the Engine with the required parameters
                     webTupleProc.processTuples(null);
                     // get the subjects and objects
                     retList = webTupleProc.getRetList();
 
                     // get the predicates
                     retListPredLookUp = webTupleProc.getRetListPredLookUp();
                     retListPredSearch = webTupleProc.getRetListPredSearch();
 
                     retListSubj = retList.get(0);
                     retListObj = retList.get(1);
                 }
 
             }
 
             // set the request parameter for results display
             if (retListSubj.size() > 0) {
                 request.setAttribute("matchingListSubj", retListSubj);
             }
             if (retListObj.size() > 0) {
                 request.setAttribute("matchingListObj", retListObj);
             }
             if (retListPredLookUp.size() > 0) {
                 request.setAttribute("matchingListPredLookup", retListPredLookUp);
             }
             if (retListPredSearch.size() > 0) {
                 request.setAttribute("matchingListPredSearch", retListPredSearch);
             }
 
             // for resetting the text boxes
             request.setAttribute("subject", subject);
             request.setAttribute("predicate", predicate);
             request.setAttribute("object", object);
             request.setAttribute("topk", topK);
 
             // redirect to page
             request.getRequestDispatcher("entry.jsp").forward(request, response);
 
         }
 
         catch (Throwable theException) {
             System.out.println(theException);
         }
 
     }
 
     /**
      * Initialization of the servlet. <br>
      * 
      * @throws ServletException if an error occurs
      */
     public void init() throws ServletException
     {
         // Put your code here
     }
 
 }
