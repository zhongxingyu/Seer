 package edu.nus.tp.web.training;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.nus.tp.engine.BayesClassifier;
 import edu.nus.tp.engine.saver.InMemoryPersistence;
 import edu.nus.tp.engine.saver.RedisPersistence;
 import edu.nus.tp.engine.utils.Category;
 import edu.nus.tp.web.tweet.ClassifiedTweet;
 import static edu.nus.tp.engine.utils.Constants.*;
 
 
 /**
  * Servlet implementation class TrainClassifier
  */
 @WebServlet("/TrainClassifier")
 public class TrainClassifier extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public TrainClassifier() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		List result = (ArrayList) request.getSession().getAttribute("tweetData");
 		List<ClassifiedTweet> classifiedTweet=new ArrayList<ClassifiedTweet>();
 		Iterator<String> i = result.iterator();
 		int j = 0;		
 		while (i.hasNext()) {
 			String tweet=i.next();
 			String cls=request.getParameter("classification"+j);
 			if(cls!=null)
 			{
				
 				ClassifiedTweet cTweet = new ClassifiedTweet(tweet, Category.getCategoryForId(Integer.parseInt(cls)),request.getSession().getAttribute("topic")+"");		
 				writeToFile(cTweet);
 				classifiedTweet.add(cTweet);
 			}			
 			j++;
 		}
 		BayesClassifier classifier=new BayesClassifier(new RedisPersistence());
 		classifier.train(classifiedTweet);
 		request.getRequestDispatcher("./Generate.html").forward(request, response);
 		//Remove Stop words
 		//add terms to the existing database of words
 		
 	}
 
 	private void writeToFile(ClassifiedTweet tweet){
 	String file = TEMP_FILES;
 		if (tweet.getClassification() == Category.POSITIVE){
 			file += "Positive.txt";
 		} else if (tweet.getClassification() == Category.NEGATIVE){
 			file += "Negative.txt";
 		} else{
 			file += "Neutral.txt";
 		}
 		
 		try {
 		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
 		    out.println(tweet.getTweetContent());
 		    out.close();
 		} catch (IOException e) {
 		}
 	}
 	
 }
