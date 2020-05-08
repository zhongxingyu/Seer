 /*******************************************************************************
  *Copyright (c) 2013 IBM Corporation and others.
  *All rights reserved. This program and the accompanying materials
  *are made available under the terms of the Eclipse Public License v1.0
  *which accompanies this distribution, and is available at
  *http://www.eclipse.org/legal/epl-v10.html
  *
  *Contributors:
  *    IBM Corporation - initial API and implementation
  *******************************************************************************/
 package servlets;
 
 import infoResource.RegistrationElements;
 
 import java.io.File;
 
 import java.io.IOException;
 
 import java.io.PrintWriter;
 import java.sql.Date;
 import java.util.List;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.FileItemFactory;
 import org.apache.tomcat.util.http.fileupload.FileUploadException;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
 
 import unclassified.DateProvider;
 
 import dao.DAOFactory;
 import dao.UserDAO;
 
 import entity.Profile;
 import entity.User;
 
 /**
  * Servlet implementation class UserRegistrator
  */
 @WebServlet(description = "Registers a user", urlPatterns = { "/UserRegistrator" })
 public class UserRegistrator extends BaseServlet {
 
     /**
      * Default constructor. 
      */
     public UserRegistrator() { }
 
 	@Override
 	public void executeCustomCode(HttpServletRequest request,HttpServletResponse response) {
 		PrintWriter out=null;
 
 		try{
 			HttpSession theSession=request.getSession();
 			out = response.getWriter();
 			out.write("Path "+request.getContextPath());
 			
 			User myUser=createUser(request,out);
 			
 			if(myUser!=null)
 				theSession.setAttribute("user", myUser);
 			
 			 
 			redirectUser(myUser, request, response);
 			
 		}
 		catch(IOException IOe){
 			out.print("IO problem");
 		}
 		catch(Exception ex){
 			out.print("Problem in registering");
 			}
 		
 	}
 	
 	/**
 	 * Create a user to be stored in the database
 	 * <p>
 	 * sample<br/>
 	 * user name nej<br/>
 	 * password: see
 	 * </p>
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 */
 	private User createUser(HttpServletRequest request,PrintWriter out) throws IOException,Exception{
 		User newUser=new User();
 		Profile userProfile=new Profile();
 		
 		//Initialize DAO
 		DAOFactory myDAOFactory = DAOFactory.getInstance(DAOFactory.MYSQL);
         UserDAO myUserDAO = myDAOFactory.createUserDAO();
         
         FileItemFactory factory = new DiskFileItemFactory();
         ServletFileUpload upload = new ServletFileUpload(factory);
 		
         List<FileItem> items = null;	//List that will contain the items
 
         /*
          * The items from the request are allocated
          * to the list
          */
 			  items = upload.parseRequest(request);
 			  out.println("items: "+items);
         
 		  int month=1,day=1,year=2000;
 		  Date userBday=null;
 		  /*Loop where the form items are iterated*/
 		  for(FileItem item: items){
 			  if (item.isFormField()){
 				  if(RegistrationElements.usernameElem.toString().matches(item.getFieldName()))
 					  newUser.setName(item.getString());
 				  else if(RegistrationElements.Role.toString().matches(item.getFieldName()))
 					  newUser.setRole(item.getString());
 				  else if(RegistrationElements.confirPassID.toString().matches(item.getFieldName()))
 					  newUser.setPassword(item.getString());
 				  else if(RegistrationElements.fNameID.toString().matches(item.getFieldName()))
 					  userProfile.setFirstName(item.getString());
 				  else if(RegistrationElements.sNameID.toString().matches(item.getFieldName()))
 					  userProfile.setSurname(item.getString());
 				  else if(RegistrationElements.month.toString().matches(item.getFieldName()))
 					  month=getMonthNumber(item.getString());
 				  else if(RegistrationElements.day.toString().matches(item.getFieldName()))
 					  day=Integer.parseInt(item.getString());
 				  else if(RegistrationElements.year.toString().matches(item.getFieldName()))
 					  year=Integer.parseInt(item.getString());
 				  
 			userBday=new Date(year-1900,month,day);//This deprecated tag has some issues
 			
 			out.println("Name: "+item.getFieldName()+" value: "+item.getString());
 			  }//End If
 			  else saveFile(item, out,userProfile); //if for item is a file
 		  }//End of loop
 		  userProfile.setBirthDay(userBday);
 		  out.println("Bday: "+userBday);
 		  out.println("TranslatedPath: "+request.getPathTranslated());
 		  myUserDAO.addUser(newUser, userProfile);
 		  
 		  if(newUser.getName()!=null&&!newUser.getName().isEmpty())
 			  return myUserDAO.findUserWithName(newUser.getName());
 		  return null;
 		  
 	}
 	
 	/**
 	 * saves the image found in the request
 	 * @param item
 	 * @param out
 	 * @throws Exception 
 	 */
 	private void saveFile(FileItem item,PrintWriter out,Profile myProfile) throws Exception{
 		//"uploadedFiles/ || /home/nickleus/Pictures/upload/";'
 		init(getServletConfig());
 		  out.println("Token: "+getServletConfig().getInitParameter("token"));
 		String pathPrefix="upPics/";
 		
 				String itemName = item.getName();
 				Random generator = new Random();
 				int r = Math.abs(generator.nextInt());
 
 				String reg = "[.*]";
 				String replacingtext = "";
 				out.println("Text before replacing is:-" + itemName);
 				Pattern pattern = Pattern.compile(reg);
 				Matcher matcher = pattern.matcher(itemName);
 				StringBuffer buffer = new StringBuffer();
 
 				while (matcher.find()) {
 					matcher.appendReplacement(buffer, replacingtext);
 				}
 				int IndexOf = itemName.indexOf("."); 
 					String domainName = itemName.substring(IndexOf);
 						//out.println("domainName: "+domainName);
 
 					String finalimage = buffer.toString()+"_"+r+domainName;
 					//out.println("Final Image==="+finalimage);
 
 					String imgURL=pathPrefix+finalimage,dbURL="upPics/"+finalimage;
 					File savedFile = new File(imgURL);
 					item.write(savedFile);
 
 					myProfile.setImageURL(dbURL);
 					out.println("<img src="+finalimage+">");
 			
 	}
 	
 	/**
 	 * returns the corrct numerical equivalent of the month
 	 * @param month
 	 * @return
 	 */
 	private int getMonthNumber(String month){
 		DateProvider Dp=new DateProvider();
 		String[] months=Dp.getMonths();
 		int index=0;
 		
 		while(index<12){
 			if(months[index].equalsIgnoreCase(month))
 				return index+1;
 			index++;
 		}
 		return index;
 	}
 	
 	
	public void redirectUser(User myUser,HttpServletRequest request,HttpServletResponse response){
 		try{
 			if(myUser==null)
 				response.sendRedirect("LogInFail.jsp");
 			
 			if(User.Roles.learner.isLearner(myUser.getRole()))
				response.sendRedirect("../StorySense/LearnerHomeSample2.jsp");
 			else if(User.Roles.learner.isReviwer(myUser.getRole()))
				response.sendRedirect("../StorySense/ReviewerHome.jsp");
 			
 		}catch(IOException IOEx){
 			
 		}
 	}
 	
 	public String getImageFileExtension(String given){
 		given=given.toLowerCase();
 		if(given.endsWith(".jpg")||given.endsWith(".jpeg"))
 			return ".jpg";
 		else if(given.endsWith(".png"))
 			return ".png";
 		else return ".bmp";
 	}
 
 }
