 package io.http.servlet;
 
 import student.database.engine.StudentDataBaseEngine;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 @WebServlet(urlPatterns = "/process")
 public class IOHttpServlet extends HttpServlet {
     private StudentDataBaseEngine sdbe;
 
     @Override
     public void init() throws ServletException {
         sdbe = new StudentDataBaseEngine();
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {
 
         String output = sdbe.execute(req.getParameter("argstring"));
        String[] outArray = output.split("\\|\\|");
 
         req.setAttribute(
             "out",
            outArray[0].equals(outArray[0].split("::")) ?
            outArray[0] :
            outArray);
 
         req.getRequestDispatcher("index.jsp").forward(req, resp);
     }
 }
