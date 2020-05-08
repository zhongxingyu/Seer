 /**
  * Loads and saves from and to XML files.
  * 
  * @author Brett M. Story
  * @date 13 October, 2013
  */
 
 package io;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import objects.*;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class parseXML {
 	
 	private static MyCourse course;
         
     private static MyCourse loadCourseInfo(File file) {   
     	
     	course = new MyCourse(null);
     	
     	try {
             // Initial setup of document parser         
             DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
             Document doc = docBuilder.parse(file);
             
             // normalize text representation
             doc.getDocumentElement().normalize();
             
             /* ------------------------------
              * Section for courseName 
              * -------------------------------*/
             NodeList courseNameList = doc.getElementsByTagName("courseName");
             Node courseNameNode = courseNameList.item(0);
             
             //Note: you must do .getFirstChild().getNodeValue() to return what's inside the tags
             course.setName(courseNameNode.getFirstChild().getNodeValue());
         
             /* ------------------------------
              * Section for courseID/#/Section
              * -------------------------------*/
             NodeList courseIDList = doc.getElementsByTagName("courseID");
             Node courseIDNode = courseIDList.item(0);
             
             course.setCourseID(courseIDNode.getFirstChild().getNodeValue());
             
             NodeList courseNumberList = doc.getElementsByTagName("courseNumber");
             Node courseNumberNode = courseNumberList.item(0);
             
             course.setCourseNumber(Integer.parseInt(courseNumberNode.getFirstChild().getNodeValue()));
             
             NodeList sectionList = doc.getElementsByTagName("section");
             Node sectionNode = sectionList.item(0);
             
             course.setSection(sectionNode.getFirstChild().getNodeValue());
             
             /* ------------------------------
              * Section for building/roomNumber
              * -------------------------------*/
             
             NodeList buildingList = doc.getElementsByTagName("building");
             Node buildingNode = buildingList.item(0);
             
            Node buildingTextNode = buildingNode.getFirstChild();
            if (buildingTextNode != null) {
                course.setBuilding(buildingTextNode.getNodeValue());
            }
             
             NodeList roomIDList = doc.getElementsByTagName("roomID");
             Node roomIDNode = roomIDList.item(0);
             
            Node roomIDTextNode = roomIDNode.getFirstChild();
            if (roomIDTextNode != null) {
                course.setRoomID(roomIDTextNode.getNodeValue());
            }
             
             /* ------------------------------
              * Section for meetingTime
              * -------------------------------*/
             
             NodeList meetingTimeList = doc.getElementsByTagName("meetingTime");
             Node meetingTimeNode = meetingTimeList.item(0);
             
            Node meetingTimeTextNode = meetingTimeNode.getFirstChild();
            if (meetingTimeTextNode != null) {
                course.setMeetingTime(meetingTimeTextNode.getNodeValue());
            }
             
             /* ------------------------------
              * Section for semester
              * -------------------------------*/
             
             NodeList semesterList = doc.getElementsByTagName("semester");
             Node semesterNode = semesterList.item(0);
             
             course.setSemester(semesterNode.getFirstChild().getNodeValue());
            Node semesterTextNode = semesterNode.getFirstChild();
            if (semesterTextNode != null) {
                course.setSemester(semesterTextNode.getNodeValue());
            }
             
             /* ------------------------------
              * Section for index
              * -------------------------------*/
             
             NodeList indexList = doc.getElementsByTagName("index");
             Node assignmentNode = indexList.item(0);
             Node categoryNode = indexList.item(1);
             if (assignmentNode.getFirstChild().getNodeValue().equals("null") || categoryNode.getFirstChild().getNodeValue().equals("null")) {
             	course.setLastAssignmentIndex(null);
             	course.setLastCategoryIndex(null);
             }
             else {
                 course.setLastAssignmentIndex(Integer.parseInt(assignmentNode.getFirstChild().getNodeValue()));
                 course.setLastCategoryIndex(Integer.parseInt(categoryNode.getFirstChild().getNodeValue()));
             }
             
         //Beyond this point are all catches
         }catch (SAXParseException err)
         {
             System.out.println("** Parsing error" + ", line " +
                     err.getLineNumber() + ", uri " + err.getSystemId());
             System.out.println(" " + err.getMessage());
             
         }catch (SAXException e) {
             // TODO Auto-generated catch block
             Exception x = e.getException();
             ((x == null) ? e: x).printStackTrace();
             
         }catch (Throwable t) {
             // TODO Auto-generated catch block
             t.printStackTrace();
         }
         return course;
     }
     
     private static void loadStudentInfo(File file) {
         
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             SAXParser saxParser = factory.newSAXParser();
             
             DefaultHandler handler = new DefaultHandler() {
             	
             	String firstName = null;
             	String lastName = null;
             	String psuedoName = null;
             	
                 boolean isFirstName = false;
                 boolean isLastName = false;
                 boolean isPsuedoName = false;
                 boolean isGhostName = false;
 
     
                 // A SAX callback method which finds the start of an XML element
                 public void startElement (String uri, String localName, String qName,
                     Attributes attributes) throws SAXException {
                 	
                     if (qName.equalsIgnoreCase("firstName")) {
                         isFirstName = true;
                     }
                     if (qName.equalsIgnoreCase("lastName")) {
                         isLastName = true;
                     }
                     if (qName.equalsIgnoreCase("psuedoName")) {
                         isPsuedoName = true;
                     }
                     if (qName.equalsIgnoreCase("ghostName")) {
                     	isGhostName = true;
                     }
                 }
                 
                 // A SAX callback method which finds the end of an XML element
                 public void endElement(String uri, String localName, 
                     String qName) throws SAXException {
                 	
                 	// adds a student to course when it reaches </student>
                 	if (qName.equalsIgnoreCase("student")) {
                 		course.addStudentXML(firstName, lastName, psuedoName);
                 	}
                 }
                 
                 // A SAX callback method which contains all the characters in an element
                 public void characters(char ch[], int start, int length) 
                     throws SAXException {
                 	
                     //If the element is <name>
                     if (isFirstName) {
                         firstName = new String(ch, start, length);
                         isFirstName = false; // must declare name false for next search
                     }
                     //If the element is <name>
                     if (isLastName) {
                         lastName = new String(ch, start, length);
                         isLastName = false; // must declare name false for next search
                     }
                     //If the element is <psuedoName>
                     if (isPsuedoName) {
                         psuedoName = new String(ch, start, length);
                         isPsuedoName = false;   
                     }
                     if (isGhostName) {
                     	course.addGhostStudentXML(new String(ch, start, length));
                     	isGhostName = false;
                     }
                 }
                 
             };
             
             saxParser.parse(file, handler);
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private static void loadAssignmentInfo(File file) {
         
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             SAXParser saxParser = factory.newSAXParser();
             
             DefaultHandler handler = new DefaultHandler() 
             {
             	String assignmentName;
             	int assignmentWorth;
             	
             	String currentCategoryName;
             	int currentCategoryIndex = 0;
             	
             	boolean isCategoryName = false;
                 boolean isAssignmentName = false;
                 boolean isWorth = false;
                 boolean isGrade = false;
 
                 int studentIndex = 0;
                 int totalStudents = course.getNumberOfStudents();
                 
                 // A SAX callback method which finds the start of an XML element
                 public void startElement (String uri, String localName, String qName,
                     Attributes attributes) throws SAXException {
                     if (qName.equalsIgnoreCase("categoryName")) {
                         isCategoryName = true;
                     }
                     if (qName.equalsIgnoreCase("assignmentName")) {
                         isAssignmentName = true;
                     }    
                     if (qName.equalsIgnoreCase("worth")) {
                         isWorth = true;
                     }               
                     if (qName.equalsIgnoreCase("grade")) {
                         isGrade = true;
                     }
                 }
                 
                 // A SAX callback method which finds the end of an XML element
                 public void endElement(String uri, String localName, 
                     String qName) throws SAXException {
                     if (qName.equalsIgnoreCase("assignment")) {
                         studentIndex = 0;
                     }
                 }
                 
                 // A SAX callback method which contains all the characters in an element
                 public void characters(char ch[], int start, int length) 
                     throws SAXException {
                 	if (isCategoryName) {
                 		currentCategoryName = new String(ch, start, length);
                 		course.addAssignmentCategory(currentCategoryName);
                 		currentCategoryIndex = course.getAssignmentCategoryIndex(currentCategoryName);
                 		isCategoryName = false;
                 	}
                     if (isAssignmentName) {
                         assignmentName = new String(ch, start, length);
                         isAssignmentName = false;
                     }
                     if (isWorth) {
                     	assignmentWorth = Integer.parseInt(new String(ch, start, length));
                     	course.getAssignmentCategory(currentCategoryIndex).addAssignment(assignmentName, assignmentWorth);
                     	isWorth = false;
                     }
                    if (isGrade) {
                         AssignmentCategory currentCategory = course.getAssignmentCategory(currentCategoryIndex);
                         Assignment currentAssignment = currentCategory.getAssignment(currentCategory.getAssignmentIndex(assignmentName));
                         String gradeValue = new String(ch, start, length);
                         String pseudoName = null;
                        boolean student = false;
                         if (studentIndex < totalStudents) {
                             pseudoName = course.getStudent(studentIndex).getPseudoName();
                            student = true;
                         }
                         else {
                             pseudoName = course.getGhostStudent(studentIndex - totalStudents).getPseudoName();
                         }
 
                         if (gradeValue.equals("null") || gradeValue.equals("")) {
                            currentAssignment.setGrade(pseudoName, null, student);
                         }
                         else {
                            currentAssignment.setGrade(pseudoName, Integer.parseInt(gradeValue), student);
                         }
                         isGrade = false;
                         studentIndex ++;
                    }
                 }
             };
             
             saxParser.parse(file, handler);
             
         } catch (Exception e) {
             e.printStackTrace();
         }
         
     }
     
     public static MyCourse loadXML(File file) {
     	
     	course = loadCourseInfo(file);
     	
     	loadStudentInfo(file);
     	
     	loadAssignmentInfo(file);
     	
     	return course;
     }
     
     public static void saveXML(MyCourse tmpCourse) {  	
     	
     	String fileDir = "gradebooks" + File.separator + tmpCourse.getIdentifier() + ".xml";
     	
     	File file = new File(fileDir);
 		if (file.exists())
 			file.delete();
 		
 		try {
 			file.createNewFile();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		Writer writer = null;
 
     	try {  		
     		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
     	    
     	    //Add header of XML file
     	    writer.write("<class id=\""+file.toString()+"\">\n");
     	    
     	    //FIXME use non-OS-specific newlines
     	    //Add general course information to file
     	    writer.write("\t<courseName>" + tmpCourse.getName() + "</courseName>\n");
     	    writer.write("\t<courseID>" + tmpCourse.getCourseID() + "</courseID>\n");
     	    writer.write("\t<courseNumber>" + tmpCourse.getCourseNumber() + "</courseNumber>\n");
     	    writer.write("\t<section>" + tmpCourse.getSection() + "</section>\n");
     	    writer.write("\t<building>" + tmpCourse.getBuilding() + "</building>\n");
     	    writer.write("\t<roomID>" + tmpCourse.getRoomID() + "</roomID>\n");
     	    writer.write("\t<meetingTime>" + tmpCourse.getMeetingTime() + "</meetingTime>\n");
     	    writer.write("\t<semester>" + tmpCourse.getSemester() + "</semester>\n");
     	    if (tmpCourse.getLastAssignmentIndex() != null && tmpCourse.getLastCategoryIndex() != null
     	    		&& tmpCourse.getLastCategoryIndex() < tmpCourse.getNumberOfAssignmentCategories()
     	    		&& tmpCourse.getLastAssignmentIndex() < tmpCourse.getCategories().get(tmpCourse.getLastCategoryIndex()).getNumberOfAssignments()) {
 	    	    writer.write("\t<index>" + tmpCourse.getLastAssignmentIndex() + "</index>\n");
 	    	    writer.write("\t<index>" + tmpCourse.getLastCategoryIndex() + "</index>\n\n");
     	    }
     	    else {
 	    	    writer.write("\t<index>null</index>\n");
 	    	    writer.write("\t<index>null</index>\n\n");
     	    }
     	    
     	    //Add student information
             for (int i = 0; i < tmpCourse.getNumberOfStudents(); i++) {
                 writer.write("\t<student>\n");
                 writer.write("\t\t<firstName>" + tmpCourse.getStudent(i).getFirstName() + "</firstName>\n");
                 writer.write("\t\t<lastName>" + tmpCourse.getStudent(i).getLastName() + "</lastName>\n");
                 writer.write("\t\t<psuedoName>" + tmpCourse.getStudent(i).getPseudoName() + "</psuedoName>\n");
                 writer.write("\t</student>\n");
             }
             
         	writer.write("\t<!-- Ghost Students -->\n");
             
             //Add ghost students
             for (int i = 0; i <tmpCourse.getNumberOfGhostStudents(); i++) {
                 writer.write("\t<ghostStudent>\n");
                 writer.write("\t\t<ghostName>" + tmpCourse.getGhostStudent(i).getPseudoName() + "</ghostName>\n");
                 writer.write("\t</ghostStudent>\n");
             }
     	    
             //Add assignment information           
             for (int i = 0; i < tmpCourse.getNumberOfAssignmentCategories(); i++) {
             	writer.write("\t<category>\n");
             	writer.write("\t\t<categoryName>" + tmpCourse.getAssignmentCategory(i).getName() + "</categoryName>\n");
             	
             	for (int j = 0; j < tmpCourse.getAssignmentCategory(i).getNumberOfAssignments(); j++) {
                 	writer.write("\t\t<assignment>\n");
                 	writer.write("\t\t\t<assignmentName>" + tmpCourse.getAssignmentCategory(i).getAssignment(j).getName() + "</assignmentName>\n");
                 	writer.write("\t\t\t<worth>" + tmpCourse.getAssignmentCategory(i).getAssignment(j).getWorth() + "</worth>\n");
                 	
                 	for (int k = 0; k < tmpCourse.getNumberOfStudents(); k++) {
                 		writer.write("\t\t\t<grade id=\"" + tmpCourse.getStudent(k).getFullName() + "\">" + 
                 				tmpCourse.getAssignmentCategory(i).getAssignment(j).getGrade(tmpCourse.getStudent(k).getPseudoName()) + "</grade>\n");
                 	}
                 	
                 	writer.write("\t\t\t<!-- Ghost Students -->\n");
 
                 	for (int k = 0; k < tmpCourse.getNumberOfGhostStudents(); k++) {
                 		writer.write("\t\t\t<grade id=\"" + tmpCourse.getGhostStudent(k).getPseudoName() + "\">" +
                 				tmpCourse.getAssignmentCategory(i).getAssignment(j).getGrade(tmpCourse.getGhostStudent(k).getPseudoName()) + "</grade>\n");
                 	}
                 	
                 	writer.write("\t\t</assignment>\n");
             	}
             	
             	writer.write("\t</category>\n");
             }
             
             writer.write("</class>");
     	    
     	} catch (IOException e){
     		System.out.println(e);
     	} finally {
     	   try {writer.close();} catch (Exception e) {
     		   System.out.println(e);
     	   }
     	}
     }
     
     public static Boolean archiveCourse(MyCourse tmpCourse) {
     	String fileDir = "gradebooks" + File.separator + tmpCourse.getIdentifier() + ".xml";
     	String desiredDir = "gradebooks" + File.separator + "archive" + File.separator + tmpCourse.getIdentifier() + ".xml";
     	
     	File desired = new File (desiredDir);
     	if (desired.exists())
     		desired.delete();
     	
     	try {
 	    	File file = new File(fileDir);
 	    	if (file.renameTo(desired))
 	    		return true;
 	    	else
 	    		return false;
 	    	
     	} catch(Exception e) {
     		e.printStackTrace();
     	}
     	return false;    	
     }
     /*
     public static void main(String argv[]) {
     	course = loadXML(new File("structure.xml"));
     	
     	course.removeStudent(7);
     	
     	saveXML(course, new File("output.xml"));
     }*/
     
 }
