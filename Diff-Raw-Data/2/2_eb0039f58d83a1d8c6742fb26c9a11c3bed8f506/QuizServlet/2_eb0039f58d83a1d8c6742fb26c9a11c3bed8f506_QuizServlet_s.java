 package no.steria.quizzical;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class QuizServlet extends HttpServlet {
 
 	private Response quizResponse;
 	private MongoQuizDao mongoQuizDao;
 	private MongoResponseDao mongoResponseDao;
	private InputCleaner cleaner;
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		ObjectMapper mapper = new ObjectMapper();		 
 		JsonNode rootNode = mapper.readTree(req.getReader().readLine());
 		
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
 				name = cleaner.clean(entry.getValue().asText());
 			}else if(entry.getKey().equals("email")){
 				email = cleaner.clean(entry.getValue().asText());
 			}
 		}
 		quizResponse = new Response(quizId, name, email, answersToDB);
 		quizResponse.calculateScore(mongoQuizDao.getQuiz(quizId));
 		mongoResponseDao.setResponse(quizResponse);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType("text/json");		
 		ObjectMapper mapper = new ObjectMapper();
 		PrintWriter writer = resp.getWriter();
 
 		int mode = Integer.parseInt(req.getParameter("mode"));
 		if(mode == 1){
 			retrieveQuizByQuizId(req, mapper, writer);
 		}
 	}
 
 	private void retrieveQuizByQuizId(HttpServletRequest req,
 			ObjectMapper mapper, PrintWriter writer) throws IOException,
 			JsonGenerationException, JsonMappingException {
 		int quizId = Integer.parseInt(req.getParameter("quizId"));
 		Quiz quiz = null;
 		quiz = mongoQuizDao.getQuiz(quizId);
 		Iterator<Question> questions = quiz.getQuestions().iterator();
 		while(questions.hasNext()){
 			 questions.next().setAnswer(-1);
 		}
 		if(!quiz.getActive()){
 			throw new IllegalArgumentException();
 		}
 		mapper.writeValue(writer, quiz);
 	}
 	
 	public void setQuizDao(MongoQuizDao quizDao) {
 		this.mongoQuizDao = quizDao;
 	}
 	
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		mongoQuizDao = new MongoQuizDao();
 		mongoResponseDao = new MongoResponseDao();
 	}
 
 }
