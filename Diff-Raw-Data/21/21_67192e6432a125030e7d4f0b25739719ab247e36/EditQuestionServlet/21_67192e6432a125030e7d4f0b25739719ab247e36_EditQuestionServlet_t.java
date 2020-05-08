 //Created By Ilan Godik
 package servlets;
 
 import db.DB;
 import db.RealDB;
 import model.Answer;
 import model.Question;
 import model.User;
 import util.Validation.InputValidation;
 import util.Validation.IntValidator;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 import static util.Marking.mark;
 
 public class EditQuestionServlet extends HttpServlet {
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         req.setCharacterEncoding("UTF-8");
         resp.setCharacterEncoding("UTF-8");
         HttpSession session = req.getSession();
         User adminUser = (User) session.getAttribute("user");
 
         if (adminUser != null && adminUser.isAdmin) {
             DB db = new RealDB();
 
             int id = Integer.parseInt(req.getParameter("id"));
             String question = req.getParameter("question");
             String answer1 = req.getParameter("answer1");
             String answer2 = req.getParameter("answer2");
             String answer3 = req.getParameter("answer3");
             String answer4 = req.getParameter("answer4");
             String correctAnswerS = req.getParameter("correctAnswer");
 
 
             InputValidation valid = new InputValidation(req, db);
 
            int correctAnswer = 1;// Doesn't matter!!!
             IntValidator validator = new IntValidator();
             if (!validator.isValid(correctAnswerS)) {
                 valid.fail("correctAnswer");
             } else {
                 correctAnswer = Integer.parseInt(correctAnswerS);
                 if (correctAnswer < 1 || correctAnswer > 4) {
                     valid.fail("correctAnswer");
                 }
             }
 
             if (!valid.hasFailed()) {
                 Question q = new Question(db, id);
                Question newQ = new Question(id, q.number, question, correctAnswer);
                 newQ.save(db);
 
                Answer oldA1 = new Answer(db, 1, q.id);
                Answer a1 = new Answer(oldA1.id, 1, q.id, answer1);
                 a1.save(db);
                Answer oldA2 = new Answer(db, 2, q.id);
                Answer a2 = new Answer(oldA2.id, 2, q.id, answer2);
                 a2.save(db);
                Answer oldA3 = new Answer(db, 3, q.id);
                Answer a3 = new Answer(oldA3.id, 3, q.id, answer3);
                 a3.save(db);
                Answer oldA4 = new Answer(db, 4, q.id);
                Answer a4 = new Answer(oldA4.id, 4, q.id, answer4);
                 a4.save(db);
                 mark(req, "edit-success");
             }
             req.getRequestDispatcher("/admin/editQuestion.jsp?id=" + id).forward(req, resp);
         }
     }
 }
