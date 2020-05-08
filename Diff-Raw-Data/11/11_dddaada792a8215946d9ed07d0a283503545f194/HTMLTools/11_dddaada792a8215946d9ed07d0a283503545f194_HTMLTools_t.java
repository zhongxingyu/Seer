 package ca.usask.gmcte.util;
 
 import java.lang.reflect.Method;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import ca.usask.gmcte.currimap.model.Assessment;
 import ca.usask.gmcte.currimap.model.AssessmentGroup;
 import ca.usask.gmcte.currimap.model.CourseOutcome;
 import ca.usask.gmcte.currimap.model.LinkCourseOfferingAssessment;
 import ca.usask.gmcte.currimap.model.OrganizationOutcome;
 import ca.usask.gmcte.currimap.model.ProgramOutcome;
 
 public class HTMLTools
 {
 
 	public static String createSelect(String name, List<String> valueList, List<String> textList, String selectedValue, String onchange)
 	{
 		StringBuilder s = new StringBuilder();
 		s.append("<select name=\"");
 		s.append(name);
 		s.append("\" id=\"");
 		s.append(name);
 		s.append("\" ");
 		if(onchange != null)
 		{
 			s.append(" onchange=\"");
 			s.append(onchange);
 			s.append("\" ");
 		}
 		s.append(" >");
 		
 		for(int i = 0; i< valueList.size(); i++)
 		{
 			String value = valueList.get(i);
 			String text = textList.get(i);
 		
 			s.append("<option value=\"");
 			s.append(value);
 			s.append("\" ");
 			if(selectedValue != null && selectedValue.equals(""+value) || (selectedValue == null  && i==0))
 			{
 				s.append("selected=\"SELECTED\"");
 			}
 			s.append(">");
 			s.append(text);
 			s.append("</option>\n");
 		}
 		s.append("</select>");
 		return s.toString();
 	}
 	public static String createSelect(String name, List<?> objects,String valueField, String displayField, String selectedValue, String onchange)
 	{
 		 
 		List<String> valueList = new ArrayList<String>();
 		List<String> displayList = new ArrayList<String>();
 		try
 		{
 			valueField = "get"+valueField.substring(0,1).toUpperCase()+valueField.substring(1);
 			displayField = "get"+displayField.substring(0,1).toUpperCase()+displayField.substring(1);
 			for(Object o : objects)
 			{
 				Method valueMethod = o.getClass().getMethod(valueField);
 				String value = "" + valueMethod.invoke(o);
 				Method displayMethod = o.getClass().getMethod(displayField);
 				String display = "" + displayMethod.invoke(o);
 				valueList.add(value);
 				displayList.add(display);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return createSelect(name, valueList, displayList, selectedValue,onchange);
 	}
 	public static String createSelect(String name, List<CourseOutcome> outcomes, Integer selectedValue, String onchange,boolean includePrompt)
 	{
 		 
 		List<String> valueList = new ArrayList<String>();
 		valueList.add("-1");
 		List<String> displayList = new ArrayList<String>();
 		displayList.add("Please select an outcome to add");
		int maxLength = 70;
 		try
 		{
 			
 			for(int i = 0; i < outcomes.size();  i++)
 			{
 				CourseOutcome o = outcomes.get(i);
				String display = o.getName();
				if (display.length() > maxLength+5)
				{
					display = display.substring(0,maxLength - 10) + " ... " + display.substring(display.length()-10); 
				}
 				valueList.add(""+o.getId());
 				//the No match value should not receive a number
				displayList.add( (i+1)+". "+ display);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return createSelect(name, valueList, displayList, ""+selectedValue,onchange);
 	}
 	public static String createSelect(String name, List<LinkCourseOfferingAssessment> assessments, Integer selectedValue,boolean includePrompt)
 	{
 		NumberFormat formatter = NumberFormat.getInstance();
 		formatter.setMaximumFractionDigits(1);
 		List<String> valueList = new ArrayList<String>();
 		valueList.add("-1");
 		List<String> displayList = new ArrayList<String>();
 		displayList.add("Please select an assessment");
 		try
 		{
 			
 			for(int i = 0; i < assessments.size();  i++)
 			{
 				LinkCourseOfferingAssessment o = assessments.get(i);
 				
 				String additionalInfo = o.getAdditionalInfo();
 				AssessmentGroup group = o.getAssessment().getGroup();
 				String infoDisplay = "";
 				if(group != null)
 					infoDisplay = o.getAssessment().getGroup().getShortName()+ ": ";
 				
 				infoDisplay += o.getAssessment().getName() + (HTMLTools.isValid(additionalInfo)?" ( "+additionalInfo+" )":"") +", "+formatter.format(o.getWeight()) + "%, " + o.getWhen().getName();
 				valueList.add(""+o.getId());
 				displayList.add( infoDisplay);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return createSelect(name, valueList, displayList, ""+selectedValue,null);
 	}
 	public static String createSelectProgramOutcomes(String name, List<ProgramOutcome> list, String selectedValue)
 	{
 		StringBuilder s = new StringBuilder();
 		s.append("<select multiple=\"multiple\" size=\"10\" name=\"");
 		s.append(name);
 		s.append("\" id=\"");
 		s.append(name);
 		s.append("\">");
 		
 		int prevGroup = 0;
 		for(ProgramOutcome o : list)
 		{
 			if(o.getGroup().getId() != prevGroup)
 			{
 				if(prevGroup > 0) //not the first one
 				{
 					s.append("</optgroup>\n");
 				}
 				
 				s.append("<optgroup label=\"");
 				s.append(o.getGroup().getName());
 				s.append("\">\n");
 			}
 				s.append("\t<option value=\"");
 				s.append(o.getId());
 			s.append("\" ");
 			if(selectedValue != null && selectedValue.equals(""+o.getId()) || (selectedValue == null  && prevGroup == 0))
 			{
 				s.append("selected=\"SELECTED\"");
 			}
 			s.append(">");
 			s.append(o.getName());
 			s.append("</option>\n");
 			prevGroup = o.getGroup().getId();
 		}
 		s.append("</optgroup>\n");
 		
 		s.append("</select>");
 		return s.toString();
 	}
 	public static String createSelectAssessmentMethods(String name, List<Assessment> list, String selectedValue)
 	{
 		StringBuilder s = new StringBuilder();
 		s.append("<select size=\"1\" name=\"");
 		s.append(name);
 		s.append("\" id=\"");
 		s.append(name);
 		s.append("\">");
 		
 		int prevGroup = 0;
 		for(Assessment o : list)
 		{
 			if(o.getGroup().getId() != prevGroup)
 			{
 				if(prevGroup > 0) //not the first one
 				{
 					s.append("</optgroup>\n");
 				}
 				
 				s.append("<optgroup label=\"");
 				s.append(o.getGroup().getName());
 				s.append("\">\n");
 			}
 				s.append("\t<option value=\"");
 				s.append(o.getId());
 			s.append("\" ");
 			if(selectedValue != null && selectedValue.equals(""+o.getId()) || (selectedValue == null  && prevGroup == 0))
 			{
 				s.append("selected=\"SELECTED\"");
 			}
 			s.append(">");
 			s.append(o.getName());
 			if(isValid(o.getDescription()))
 			{
 					s.append(" ");
 					s.append(o.getDescription());
 			}
 			s.append("</option>\n");
 			prevGroup = o.getGroup().getId();
 		}
 		s.append("</optgroup>\n");
 		
 		s.append("</select>");
 		return s.toString();
 	}
 	public static String createSelectOrganizationOutcomes(String name, List<OrganizationOutcome> list, String selectedValue)
 	{
 		StringBuilder s = new StringBuilder();
 		s.append("<select multiple=\"multiple\" size=\"10\" name=\"");
 		s.append(name);
 		s.append("\" id=\"");
 		s.append(name);
 		s.append("\">");
 		
 		int prevGroup = 0;
 		for(OrganizationOutcome o : list)
 		{
 			if(o.getGroup().getId() != prevGroup)
 			{
 				if(prevGroup > 0) //not the first one
 				{
 					s.append("</optgroup>\n");
 				}
 				
 				s.append("<optgroup label=\"");
 				s.append(o.getGroup().getName());
 				s.append("\">\n");
 			}
 				s.append("\t<option value=\"");
 				s.append(o.getId());
 			s.append("\" ");
 			if(selectedValue != null && selectedValue.equals(""+o.getId()) || (selectedValue == null  && prevGroup == 0))
 			{
 				s.append("selected=\"SELECTED\"");
 			}
 			s.append(">");
 			s.append(o.getName());
 			s.append("</option>\n");
 			prevGroup = o.getGroup().getId();
 		}
 		s.append("</optgroup>\n");
 		
 		s.append("</select>");
 		return s.toString();
 	}
 	public static int getInt(String s)
 	{
 		if(isValid(s))
 		{
 			try
 			{
 				return Integer.parseInt(s);
 			}
 			catch(Exception e)
 			{}
 		}
 		return -1;
 	}
 	
 	public static boolean isValid(String id)
 	{
 		if(id == null)
 			return false;
 		
 		id = id.trim();
 		if(id.length() < 1)
 			return false;
 		if(id.equals("null") || id.equals("undefined"))
 			return false;
 		return true;
 	}
 	public static String addBracketsIfNotNull(String s)
 	{
 		if(!isValid(s))
 		{
 			return "";
 		}
 		return "("+s+")";
 	}
 	public static boolean sendEmailMessage(String to, String from, String subject, String body)
 	{
 		boolean result = false;
 		try
 		{ // Set the host smtp address
 			Properties props = new Properties();
 			props.put("mail.smtp.host", "campus.usask.ca");
 
 			// create some properties and get the default Session
 			Session session = Session.getDefaultInstance(props, null);
 			// session.setDebug(true);
 
 			// create a message
 			Message msg = new MimeMessage(session);
 			// set the from and to address
 			InternetAddress addressFrom = new InternetAddress(from);
 			msg.setFrom(addressFrom);
 
 			InternetAddress[] addressTo = new InternetAddress[1];
 			addressTo[0] = new InternetAddress(to);
 			msg.setRecipients(Message.RecipientType.TO, addressTo);
 
 			msg.setSubject(subject);
 			Multipart multipart = new MimeMultipart();
 			MimeBodyPart messagePart = new MimeBodyPart();
 			messagePart.setText(body);
 			messagePart.setHeader("Content-Type", "text/html");
 
 			multipart.addBodyPart(messagePart);
 			msg.setContent(multipart);
 
 			Transport.send(msg);
 			result = true;
 		} catch (Exception ex)
 		{
 
 			result = false;
 		}
 		
 		return result;
 	}
 }
