 package controller;
 
 import model.petition.*;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import model.DataModel;
 import model.entities.*;
 import model.response.Response;
 import model.response.ResponseObject;
 import model.response.Status;
 
 public class ServletController extends HttpServlet 
 {
     private DataModel dataModel;
     private PetitionFactory petitionFactory;
     
     @Override
     public void init()
     {
         dataModel = DataModel.getInstance();
         petitionFactory = PetitionFactory.getInstance();
     }
     
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
     {
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
     {
         String fullUrl = request.getRequestURL().toString();
        
         if(!petitionFactory.ValidPattern(fullUrl))
         {
             this.sendError(request, response);
             return;
         }
         
         Petition petition = petitionFactory.newPetition(request, fullUrl);
         
         //Dispatch Response
         Response dataResponse = dataModel.execute(petition);
         this.dispatch(request, response, dataResponse);
     }
     
     
     private void dispatch(HttpServletRequest request,HttpServletResponse response, Response dataResponse) throws ServletException, IOException 
     {
         Petition petition = dataResponse.getPetition();
         RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/error.jsp");
         HttpSession session = request.getSession();
         
         switch(petition.getFunction())
         {
             case login:
                 
                 if(dataResponse.getStatus()== Status.Succes)
                 {   
                     User user = (User)dataResponse.get(ResponseObject.user);
                     session.setAttribute(Entity.user.toString(), user);
 
                     if(user.getUserType()==UserType.admin)
                    {
                        session.setAttribute(ResponseObject.users.toString(), dataResponse.get(ResponseObject.users));
                         dispatcher = request.getRequestDispatcher("/admin.jsp");
                    }
                     else
                    {
                         dispatcher = request.getRequestDispatcher("/prospects.jsp");
                    }
                 }
                 else
                 {
                     dispatcher = request.getRequestDispatcher("/index.jsp");
                 }
                 
                 break;
             case add:
                 
                 if(petition.getEntity()==Entity.user)
                 {
                     User user = (User)dataResponse.get(ResponseObject.user);
                     session.setAttribute(Entity.user.toString(), user);
                     dispatcher = request.getRequestDispatcher("/profile.jsp");
                 }
                 
                 break;
             case apply:
                 break;
             case delete:
                 
                 if(petition.getEntity()==Entity.user)
                 {
                     session.removeAttribute(Entity.user.toString());
                     dispatcher = request.getRequestDispatcher("/index.jsp");
                 }
                 
                 break;
             case modify:
                 
                 if(petition.getEntity()==Entity.applicant || petition.getEntity()==Entity.company)
                 {
                     session.setAttribute(Entity.user.toString(), dataResponse.get(ResponseObject.user));
                     dispatcher = request.getRequestDispatcher("/profile.jsp");
                 }
                 
                 break;
             case logout:
                 
                 session.removeAttribute(Entity.user.toString());
                 dispatcher = request.getRequestDispatcher("/index.jsp");
                     
                 break;
             case get:
                 
                 this.dispatchGetJSON(request, response, dataResponse);
                 
                 break;
             default:
                 return;
         }
         
         dispatcher.forward(request, response);
     }
     
     private void dispatchGetJSON(HttpServletRequest request,HttpServletResponse response, Response dataResponse) throws ServletException, IOException
     {
         response.setHeader("content-type","application/json");
         PrintWriter out = response.getWriter();
         try 
         {
             switch(dataResponse.getPetition().getEntity())
             {
                 case skill:
                     out.write(dataResponse.get(ResponseObject.skills).toString());
                     break;
                 case employment:
                     out.write(dataResponse.get(ResponseObject.employments).toString());
                     break;
                 default:
                     break;
             }
         } finally {
             out.close();
         }
     }
     
     protected void sendError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
     {
         RequestDispatcher distatcher = request.getRequestDispatcher("/pages/error.jsp");
         distatcher.forward(request, response);
     }
 }
