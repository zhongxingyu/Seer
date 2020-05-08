 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import com.sun.org.apache.bcel.internal.generic.Select;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
     	List<School> schools = School.findAll();
     	Long cschools = School.count();
     	Long cteachers = Teacher.count();
     	Long cclassrms = Classroom.count();
     	Long cstudents = StudentUser.count();
     	String noticias = "Currently the system contains " + cschools + " schools, " + cteachers + " teachers, " + cclassrms + " classrooms, and " + cstudents + " students.";
     	
         render( schools, noticias );
     }
         
  
     public static void startActivity( String schoolname, String classname, int classyear, String activityname, String src ) {
     	String ret = "FAIL";
     	School s = School.connect(schoolname);
     	if (s != null)
     	{
 	    	Classroom c = Classroom.connect(classname, classyear);
 	 
 	    	if ( c != null  &&  c.isSchool(s) )
 	    	{
 		    	Session a = new Session( c, src );
 		    	if ( a != null )
 		    	{
 			    	a.sessionMessage = activityname;
 			    	a.save();
 			    	Long aid = a.getId();
 			    	ret = String.valueOf(aid);
 		    	}
 	    	}
     	}
     	renderJSON(ret);
     }
     
     
     public static void nameActivity( Long aid,  String activityname )
     {
     	Session a = Session.getActivity(aid);
     	if ( a == null)
     	{
     		renderJSON("FAIL");
     	}
     	else
     	{
 	    	a.sessionMessage = activityname;
 	    	a.save();
 	    	renderJSON( "Renamed Session To: " + activityname );
     	}
     }
     
     public static void appendAnnotationToActivity( Long aid, String annotation )
     {
     	Session a = Session.getActivity(aid);
     	if ( a == null )
     	{
     		renderJSON("FAIL");
     	}
     	else
     	{
     		a.annotation += "\n" + annotation;
 	    	a.save();
	    	renderJSON( "Added Annotation: '" + annotation + "' to this activity." );
     	}
     }
     
     
     //login with username, schoolname, classname AND classyear.  COULD have an "open classroom" that allows us to return the classroom object's ID in the JSON...
     public static void login(String username, String schoolname,  String classname, int classyear ) {
     	
 		System.err.println("Received login request for user: " + username + "\n      attempting to enter class: " + classname + " with classyear = " + classyear);
 
 		School s = School.connect(schoolname);
 		if (s != null)
 		{
 			Classroom croom = Classroom.connect( classname, classyear );
 			if ( croom == null )
 			{
 				renderJSON("FAILURE-CLASSROOM -- no classroom/classyear combo: '" + classname + "/" + classyear);
 			}
 			else
 			{
 				if ( croom.isSchool( s ) )
 				{
 					StudentUser theGuy = StudentUser.connect(username, croom);
 					if ( theGuy == null )
 					{
 						renderJSON("FAILURE-STUDENT -- no student '" + username + "' in class '" + classname + "'");
 					}
 					else
 					{
 						renderJSON("SUCCESS");
 					}
 				}	
 				else
 				{
 					renderJSON("School/classroom mismatch - school name: " + schoolname + " classname/year: " + classname + "/" + classyear );
 				}
 			}
 
 		}
 		else 
 		{
 			renderJSON("COULDNT FIND SCHOOL" + schoolname );
 		}
     }
     
     
     public static void logContribution(String stype, String username, Long actid, String contribid, String contribution ) {
     	System.err.println("contribution logged");
     	Session act = Session.getActivity(actid);
     	
 			if (act == null )
 			{
				renderJSON("FAILURE-no  activity with id = " + actid);
 			}
 			else
 			{
 				Classroom croom = act.classroom;
 		    	StudentUser theGuy = StudentUser.connect(username, croom);
 		    	if ( theGuy == null )
 		    	{
 		    		renderJSON("FAILURE-STUDENT -- no student '" + username + "' in classroom '" + croom.toString() + "'");
 		    	}
 		    	else
 		    	{
 		    		ContributionType ct = ContributionType.POINT;
 		    		if ( stype.equals("EQUATION") )
 		    			ct = ContributionType.EQUATION;
 		    			
 		    		Contribution c = new Contribution(ct, theGuy, act, contribid, contribution );
 		    		c.save();
 		    		renderJSON("Logged contribution from user: " + username + ":" + croom.toString() +  "\nContents: " + contribution );
 		    	}
 			}
 	 
     }
     
     
     public static void getAllActivities()
     {
     	String toReturn = "ACTIVITIES:\n";
     	List<Session> acts = Session.findAll();
     	for (Session a : acts)
     	{
     		toReturn += a.toString() + "\n";
     	}
     	renderJSON( toReturn );
     }
     
     
     //test methods...
     public static void getAllTeachers()
     {
     	List<Teacher> teachers = Teacher.find("select t from Teacher t").fetch();
     	String reply = "Teachers:\n";
     	if (teachers.isEmpty())
     		reply = "NO TEACHERS";
     	for ( Teacher t: teachers)
     	{
     		reply += t.toString() + "\n";
     	}
     	renderJSON( reply );
     }
     
     public static void getAllSchools()
     {
     	List<School> schools = School.find("select s from School s").fetch();
     	String reply = "Schools:\n";
     	if (schools.isEmpty())
     		reply = "NO Schools";
     	for ( School s: schools)
     	{
     		reply += s.toString() + "\n";
     	}
     	renderJSON( reply );
     }
     
     public static void getAllStudents()
     {
     	List<StudentUser> ss = StudentUser.find("select s from StudentUser s").fetch();
     	String reply = "Students:\n";
     	if (ss.isEmpty())
     		reply = "NO Students";
     	for ( StudentUser s: ss)
     	{
     		reply += s.toString() + "\n";
     	}
     	renderJSON( reply );
     }
     
     //with null name argument, this one returns a list
     public static void getAllClassroomsMatching( String cname  )
     {
     	String reply = "NO Matching Classrooms Found";
     	List<Classroom> cs = null;
     	if (cname == null)
     	{
     		cs = Classroom.find( "select c from Classroom c" ).fetch();
     	}
     	else
     	{
     		cs = Classroom.find("select c from Classroom c where c.classname like '"+cname+"' order by c.startYear").fetch();
     	}
     	
     	if (cs != null && cs.size() > 0)
     	{
     		reply = "Matching Classrooms:\n";
     		for ( Classroom c : cs )
     		{
     		
     			reply += "Name=" + c.classname + "; Teacher=" + c.teacher + "; Starting Year=" + c.startYear + "\n";
     		}
     	}
     	renderJSON( reply );
     }
     
     //with null name argument this one puts in "firstperiodmath" -- for testing.
     public static void getAllClassroomsDummy( String cname )
     {
     	if ( cname == null )
     	{
     		cname = "FirstPeriodMath";
     	}
     	String reply = "NO Matching Classrooms Found";
     	List<Classroom> cs = Classroom.find("select c from Classroom c where c.classname like '"+cname+"' order by c.startYear").fetch();
     	if ( cs != null && cs.size() > 0)
     	{
     		reply = "Matching Classroom(s):\n";
 	    	for ( Classroom c : cs )
 			{
 					reply += "Name=" + c.classname + "; Teacher=" + c.teacher + "; Starting Year=" + c.startYear + "\n";
 			}
     	}
     	
     	renderJSON( reply );
     }
     
     public static void getAllStudentsInClassroom( String cname, int year )
     {
     	String reply = "NO Matching Classrooms Found";
     	List<Classroom> cs = Classroom.find("select c from Classroom c where c.classname like '"+cname+"'").fetch();
     	for ( Classroom c : cs )
 		{
 			if ( c.startYear == year )
 			{
 				int i = 0;
 				reply = "Classroom Found:  Name=" + c.classname + "; Year=" + c.startYear + "\n";
 				for ( StudentUser s : c.students)
 				{
 					i++;
 					reply += "#" + i + "Username: " + s.username + "\n";
 				}
 			}
 		}
     	renderJSON( reply );
     }
     
     
    /*stupid method -- need to specify classsroom , then get current activity, etc.*/
     public static void getAllContributions( )
     {
     	List<Contribution> allcontribs = Contribution.find("select c from Contribution c order by c.timestamp").fetch();
     	String reply = "Contributions:\n";
     	if (allcontribs.isEmpty())
     		reply = "NO CONTRIBUTIONS";
     	for ( Contribution c : allcontribs )
     	{
     		//reply += c.toString() + "\n";
     		reply += c.toTSVLine() + "\n";
     	}
     	renderJSON(reply);
     }
     
 
     
     public static void getContributionsAfterSequenceNumber( Long aid, int ind )
     {
     	String reply = "OOPS -- problem in looking up Activtity with id:" + aid;
 
 	    Session a = Session.getActivity(aid);
 	    if ( a != null )
 	    {
 	    	List<Contribution> afteri = a.getContributionsAfterNumber(ind);
 	    	reply = "Contributions:\n";
 	    	if (afteri.isEmpty())
 	    		reply = "NO CONTRIBUTIONS MATCHING CONDITION";
 	    	for ( Contribution c : afteri )
 	    	{
 	    		reply += c.toTSVLine() + "\n";
 	    	}
     	}
     	renderJSON(reply);
     }
     
     public static void execute( String sql )
     {
     	try
     	{
     		List<Object> results = Utilities.executeQuery(sql);
     		String retn = "";
     		for (Object result : results )
     			retn += result.toString();
     		renderJSON( retn );
     	}
     	catch (Exception e )
     	{
     		String reply =  "ERROR in executing query: " + sql + "\n" + e.getMessage();
     		renderJSON( reply);
     	}
     }
     
     
 
 }
