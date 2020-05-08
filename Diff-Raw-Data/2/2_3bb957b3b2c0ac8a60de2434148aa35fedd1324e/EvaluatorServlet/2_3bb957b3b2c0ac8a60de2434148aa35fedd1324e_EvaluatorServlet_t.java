 package xmlExercises;
 
 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author slaweet
  */
 @WebServlet(name = "EvaluatorServlet",
 urlPatterns = {EvaluatorServlet.ACTION_RESULT, EvaluatorServlet.ACTION_TASK})
 public class EvaluatorServlet extends HttpServlet {
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     static final String ACTION_TASK = "/Task";
     static final String ACTION_RESULT = "/Restult";
     static final String ATTRIBUTE_TASK = "task";
     static final String ATTRIBUTE_RESULT = "results";
     static final String ATTRIBUTE_ERROR = "errormessage";
     static final String JSP_TASK = "/task.jsp";
     static final String JSP_RESULT = "/result.jsp";
     static final String JSP_ERROR = "/syntaxerror.jsp";
     static final String RESOURCES_DIR = "";
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
 
         if (request.getServletPath().equals(ACTION_TASK)) {
             task(request, response);
         } else if (request.getServletPath().equals(ACTION_RESULT)) {
             result(request, response);
         } else {
             throw new RuntimeException("Unknown operation: " + request.getServletPath());
         }
 
 
     }
 
     private void task(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         String type = request.getParameter("type");
         List<String> tasks = Utils.scanDirectoryStructure(RESOURCES_DIR + type);
         Random randomGenerator = new Random();
         int id = Integer.parseInt(tasks.get(randomGenerator.nextInt(tasks.size())));
         Task task = Utils.getTask(id, type);
         task.replaceTags();
         request.setAttribute(ATTRIBUTE_TASK, task);
         request.getRequestDispatcher(JSP_TASK).forward(request, response);
     }
 
     private void result(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 
         String userSolution = request.getParameter("userSolution");
         String type = request.getParameter("type");
         int id = Integer.parseInt(request.getParameter("id"));
 
         Evaluator evaluator = Utils.getEvaluator(type);
         Task task = Utils.getTask(id, type);
         ServletContext context = getServletContext();
         //String path = context.getContextPath() + "/" + type + "/" + id + "/data.xml";
         String path = RESOURCES_DIR + type+"/"+id+"/";
         List<Result> results = new ArrayList();
 
         try {
            for (int i = 1; i <= task.getData().size(); i++) {
                 Result result = new Result();
                 String file = path  + "data" + i +".xml";
                 result.setCorrectSolution(evaluator.eval(task.getSolution(), file));
                 result.setUserSolution(evaluator.eval(userSolution, file));
                 result.setIsCorrect(evaluator.compare(result.getCorrectSolution(), result.getUserSolution()));
                 //result.replaceTags();
                 results.add(result);
             }
             request.setAttribute(ATTRIBUTE_RESULT, results);
             request.getRequestDispatcher(JSP_RESULT).forward(request, response);
             
         } catch (SyntaxErorException ex) {
 
             request.setAttribute(ATTRIBUTE_ERROR, ex.getMessage());
             request.getRequestDispatcher(JSP_ERROR).forward(request, response);
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
