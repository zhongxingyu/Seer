 package no.steria.quizzical;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class SubmitServlet extends HttpServlet {
 
 	private MongoResponseDao mongoResponseDao;
 	private Response quizResponse;
 	private MongoQuizDao mongoQuizDao;
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		ObjectMapper mapper = new ObjectMapper();		 
 		JsonNode rootNode = mapper.readTree(req.getReader().readLine());
		System.out.println();
 		
 		int quizId=0; 
 		String name="", email="";
 		HashMap<String,Integer> answersToDB = new HashMap<String,Integer>();
 		
 		Iterator<Entry<String,JsonNode>> allEntries = rootNode.getFields();
 		while(allEntries.hasNext()){
 			Entry<String, JsonNode> entry = allEntries.next();
 			if(entry.getKey().equals("answers")){
 				Iterator<Entry<String,JsonNode>> answerEntries = entry.getValue().getFields();
 				while(answerEntries.hasNext()){
 					Entry<String,JsonNode> answer = answerEntries.next();
 					answersToDB.put(answer.getKey(), answer.getValue().asInt());					
 				}
 			}else if(entry.getKey().equals("quizId")){
				quizId = Integer.parseInt(entry.getValue().asText());
 			}else if(entry.getKey().equals("name")){
				name = entry.getValue().asText();
 			}else if(entry.getKey().equals("email")){
				email = entry.getValue().asText();
 			}
 		}
 		
 		quizResponse = new Response(quizId, name, email, answersToDB);
 		quizResponse.calculateScore(mongoQuizDao.getQuiz(quizId));
 		mongoResponseDao.setResponse(quizResponse);
 	}
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		
 	}
 	
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		mongoResponseDao = new MongoResponseDao();
 		mongoQuizDao = new MongoQuizDao();
 	}
 
 }
