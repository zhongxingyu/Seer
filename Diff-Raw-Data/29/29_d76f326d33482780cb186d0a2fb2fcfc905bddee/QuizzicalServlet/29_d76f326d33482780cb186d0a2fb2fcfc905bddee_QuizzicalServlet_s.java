 package no.steria.quizzical;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class QuizzicalServlet extends HttpServlet {
 
 	private QuizDao quizDao;
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		this.doGet(req, resp);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		int quizId = Integer.parseInt(req.getParameter("quizId"));			

		ObjectMapper mapper = new ObjectMapper();
		Quiz quiz = quizDao.getQuiz(quizId);
 		
 		PrintWriter writer = resp.getWriter();
		mapper.writeValue(writer, quiz);
		resp.setContentType("text/json");
 	}
 
 	public void setQuizDao(QuizDao quizDao) {
 		this.quizDao = quizDao;
 	}
 	
 	@Override
 	public void init() throws ServletException {
 		super.init();
 	
 		quizDao = new MongoQuizDao();
 	}
 
 }
