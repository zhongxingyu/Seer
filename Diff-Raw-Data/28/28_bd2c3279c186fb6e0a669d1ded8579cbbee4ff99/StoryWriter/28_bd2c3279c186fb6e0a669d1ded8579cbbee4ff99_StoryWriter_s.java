 /*******************************************************************************
  *Copyright (c) 2013 StorySense.
  *All rights reserved. This program and the accompanying materials
  *are made available under the terms of the Eclipse Public License v1.0
  *which accompanies this distribution, and is available at
  *http://www.eclipse.org/legal/epl-v10.html
  *
  *Contributors:
  *   Nickleus Jimenez
  *******************************************************************************/
 package servlets;
 import serializableObjects.StoryFileAccess;
 import webEncoder.StoryEncoder;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import achievementExecutors.WritingAchievements;
 
 
 import dao.AcomplishmentDAO;
 import dao.ConceptDAO;
 import dao.DAOFactory;
 import dao.RelationDAO;
 import dao.TemplateDAO;
 import dao.UserDAO;
 import entity.Acomplishment;
 import entity.Template;
 import entity.User;
 
 import model.Story;
 
 /**
  * Servlet implementation class StoryWriter
  */
 @WebServlet(description = "Writes the story of the learner and saves it", urlPatterns = { "/StoryWriter" })
 public class StoryWriter extends BaseServlet {
     private int templateID;
 	/**
      * Default constructor. 
      */
     public StoryWriter() {
         templateID=1;
     }
 
 	@Override
 	public void executeCustomCode(HttpServletRequest request,
 			HttpServletResponse response) {
 		PrintWriter out=null;
 		HttpSession session=request.getSession();
 		
 		try{
 			out = response.getWriter();
 			Story myStory=(Story)session.getAttribute("Story");
 			
 			templateID=Integer.valueOf(session.getAttribute(StoryEncoder.TEMPLATEID).toString());
 			out.println("The story");
 			
 			if(myStory!=null){
 				out.println(myStory.getsStory());
 				submitStory(myStory, out,request);
 				response.sendRedirect("../StorySense/LearnerHomeSample.jsp");
 			}
 			else out.println("Not forwarded");
 		}catch(Exception ex){
 			out.println("Error in getting the story "+ex.getMessage());
 			ex.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Saves the Story
 	 * @param Answers
 	 * @param myStory
 	 */
 	public void saveStory(ArrayList<String> Answers,Story myStory,String storyName){
 		/*
 		 * Where to save the file
 		 * What path shall we take
 		 */
 		 StoryFileAccess savedStory=new StoryFileAccess(myStory, Answers);
 		Random generator = new Random();
 		int r = Math.abs(generator.nextInt());
 		
 		 try{
 			 FileOutputStream fo = new FileOutputStream("uploadedFiles/"+storyName+r+"Story.story");
 			 ObjectOutputStream oo = new ObjectOutputStream(fo);
 			 oo.writeObject(savedStory);
 			 fo.close();
 		 }catch(IOException ioEx){
 			 
 		 }
 		 
 	}
 	
 	/**
 	 * Saves the story with the user name included on the file name
 	 * @param Answers
 	 * @param myStory
 	 * @param storyName
 	 * @param userName
 	 */
 	public void saveStory(ArrayList<String> Answers,Story myStory,String storyName,String userName){
 		/*
 		 * Where to save the file
 		 * What path shall we take
 		 */
 		 StoryFileAccess savedStory=new StoryFileAccess(myStory, Answers);
 		Random generator = new Random();
 		int r = Math.abs(generator.nextInt());
 		
 		 try{
 			 //Save the file
 			 String fName="uploadedFiles/"+storyName+r+userName+".story";
 			 FileOutputStream fo = new FileOutputStream(fName);
 			 ObjectOutputStream oo = new ObjectOutputStream(fo);
 			 oo.writeObject(savedStory);
 			 fo.close();
 			 
 			 /*Put file URL to database*/
 			 DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 			AcomplishmentDAO myAcomDAO=myDAOFactory.createAcomplishmentDAO();
 			Acomplishment story=new Acomplishment();
 			 
 			story.setTemplateID(templateID);
 			story.setAccountID(0);
 			story.setFileURL(fName);
 			story.setName(storyName);
 			myAcomDAO.addStoryAcomplishment(story);
 			
 		 }catch(IOException ioEx){
 			 
 		 }
 		 
 	}
 	
 	public void saveStory(ArrayList<String> Answers,Story myStory,String storyName,User givenU){
 		/*
 		 * Where to save the file
 		 * What path shall we take
 		 */
 		 StoryFileAccess savedStory=new StoryFileAccess(myStory, Answers);
 		Random generator = new Random();
 		int r = Math.abs(generator.nextInt());
 		
 		 try{
 			 //Save the file
 			 String fName="uploadedFiles/"+storyName+r+givenU.getName()+".story";
 			 FileOutputStream fo = new FileOutputStream(fName);
 			 ObjectOutputStream oo = new ObjectOutputStream(fo);
 			 oo.writeObject(savedStory);
 			 fo.close();
 			 
 			 /*Put file URL to database*/
 			 DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 			AcomplishmentDAO myAcomDAO=myDAOFactory.createAcomplishmentDAO();
 			Acomplishment story=new Acomplishment();
 			 
 			story.setTemplateID(templateID);
 			story.setAccountID(givenU.getAccountID());
 			story.setFileURL(fName);
 			story.setName(storyName);
 			myAcomDAO.addStoryAcomplishment(story);
 			
 			/*Update the score of the user after saving the story*/
 			//updateUserScore(givenU, getTemplateFromDB(myDAOFactory));
 		 }catch(IOException ioEx){
 			 
 		 }
 		 
 	}
 	
 	/**
 	 * gets a template from the database
 	 * @param myDAOFactory
 	 * @return
 	 */
 	private Template getTemplateFromDB(DAOFactory myDAOFactory){
 		TemplateDAO templateDao=myDAOFactory.createTemplateDAO();
 		return templateDao.getTemplate(templateID);
 	}
 
 	
 	public Template getTemplateFromDB(int templateKey){
 		DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 		TemplateDAO templateDao=myDAOFactory.createTemplateDAO();
 		return templateDao.getTemplate(templateKey);
 	}
 	
	private void updateUserScore(User givenUser,Template givenTemplate){
 		DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 		UserDAO userDao=myDAOFactory.createUserDAO();
 		
 		givenUser.setPoints(givenUser.getPoints()+givenTemplate.getPlusScore());
 		userDao.increaseUserPoints(givenUser, givenTemplate.getPlusScore());
 	}
 	private void operateAchievements(User given){
 		WritingAchievements wAchievements=new WritingAchievements();
 		wAchievements.awardFirstStory(given.getAccountID());
 	}
 	
 	/**
 	 * Submits the story.
 	 * Puts the assertions to the database
 	 * @param Story
 	 * @param out
 	 * @param request
 	 */
 	public void submitStory(Story Story,PrintWriter out,HttpServletRequest request){
 		DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
 		ConceptDAO conceptDao=myDAOFactory.createConceptDAO();
 		String attName="";
 		ArrayList<String> answers=new ArrayList<String>();
 		RelationDAO relationAccess=myDAOFactory.createRelationDAO();
 		
 		/*
 		 * Collect the information from the text boxes
 		 */
 		for(int ctr=1;ctr<Story.getBlanks();ctr++){
 			attName="answer"+ctr;
 			answers.add(request.getParameter(attName));
 			conceptDao.AddConcept(answers.get(ctr-1));
 		}
 		
 		/*
 		 * Traverse all the assertions
 		 * placed the answer to their corresponding blank concepts 
 		 * to be filled
 		 */
 		for(int i=0;i<Story.getAssertions().size(); i++){
 			for(int j=0; j<Story.getAssertions().get(i).size();j++){
 				//turn the ? into the ones inputted in the blanks
                 if(Story.getAssertions().get(i).get(j).getConcept1().equals("?"))
                 {
                     Story.getAssertions().get(i).get(j).setConcept1(answers.get(i));
                 }
                 if(Story.getAssertions().get(i).get(j).getConcept2().equals("?"))
                 {
                     Story.getAssertions().get(i).get(j).setConcept2(answers.get(i));
                 }
                 
                 /*Increment frequency if the relation is existing*/
                 if(relationAccess.RelationIsExisting(Story.getAssertions().get(i).get(j).getConcept1(), 
                 		Story.getAssertions().get(i).get(j).getConcept2(),
                 		Story.getAssertions().get(i).get(j).getRelationship()))
                 {
                 	relationAccess.incrementFrequencyCount(Story.getAssertions().get(i).get(j).getConcept1(), 
                 			Story.getAssertions().get(i).get(j).getConcept2(), 
                 			Story.getAssertions().get(i).get(j).getRelationship());
                 }
               //for synonyms check the opposite order if existing
                 else if(Story.getAssertions().get(i).get(j).getRelationship().equals("Synonym") && 
                 		relationAccess.RelationIsExisting(Story.getAssertions().get(i).get(j).getConcept2(), 
                 				Story.getAssertions().get(i).get(j).getConcept1(), 
                 				Story.getAssertions().get(i).get(j).getRelationship()))
                 {
                 	relationAccess.incrementFrequencyCount(Story.getAssertions().get(i).get(j).getConcept2(), 
                 			Story.getAssertions().get(i).get(j).getConcept1(), 
                 			Story.getAssertions().get(i).get(j).getRelationship());
                 }
              // add new assertion
                 else{
                 	relationAccess.AddRelation(Story.getAssertions().get(i).get(j).getConcept1(), 
                 			 Story.getAssertions().get(i).get(j).getConcept2(), 
                 			 Story.getAssertions().get(i).get(j).getRelationship());
                 }
 			}//End of inner loop
 		}//End of outer loop
 		HttpSession session=request.getSession();
 		User user=(User)session.getAttribute("user");
 		
 		if(user==null)
 			saveStory(answers, Story,request.getParameter("storyName"),"Unknown");
 		else {
 			saveStory(answers, Story,request.getParameter("storyName"),user);
 			operateAchievements(user);
 		}
 	}
 	
 
 }
