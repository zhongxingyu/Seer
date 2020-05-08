 package com.sternerlearn;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.ksoap2.serialization.SoapObject;
 import org.ksoap2.serialization.SoapSerializationEnvelope;
 
 
 public class Student 
 {
 	public Account mAccount;
 	public boolean mValid = false;
 	
 	public HashMap<Course, ArrayList<Grade>> mCourses = new HashMap<Course, ArrayList<Grade>>();
 	
 	public HashMap<Course, ArrayList<Assignment>> mAssignments = new HashMap<Course, ArrayList<Assignment>>();
 	
 	public ArrayList<Message> mMessages = new ArrayList<Message>();
 	
 	public ArrayList<Location> mLocations = new ArrayList<Location>();
 	
 	// DEBUG
 	public String mResponse;
 
 	public Student( Account aAccount )
 	{
 		mAccount = aAccount;
 		updateClasses();
 	}
 	
 	public void updateClasses()
 	{
 		List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
 		properties.add(new PropertyWrapper("aStudentID", mAccount.mId));
 		
     	/* Call web service */
     	try 
     	{		
     		SoapSerializationEnvelope envelope = WebServiceWrapper.getInstance().call(Types.STUDENT_URL, Types.STUDENT_GET_CLASSES, properties);
     	
 	
     		SoapObject response = (SoapObject)envelope.getResponse();
     		mResponse = response.toString() + " " + response.getPropertyCount();
 
     		// It's a list of classes
     		for( int i = 0; i < response.getPropertyCount(); i++ )
     		{
 	    		SoapObject arr = (SoapObject)response.getProperty(i);
 	    		
 		   		if( !arr.hasProperty("mCourseID") ||
 		   			!arr.hasProperty("mStaffID") ||
 		   			!arr.hasProperty("mClassName") )
 		   		{
 		   			mValid = false;
 		   		}
 		   		else
 		   		{
 		   			mValid = true;
 		   			
 		   			int id = java.lang.Integer.parseInt( arr.getProperty("mCourseID").toString() );
 		   			int staffId = java.lang.Integer.parseInt( arr.getProperty("mStaffID").toString() );
 		   			String name = arr.getProperty("mClassName").toString();
 		   			
 		   			mCourses.put( new Course( id, staffId, name ), new ArrayList<Grade>() );
 		   		}
     		}
     	}
     	catch(Exception ex)
     	{
     		mResponse = ex.toString();
     		mValid = false;
 		}				
 
 	}
 	
 	public ArrayList<Assignment> getAssignments( Course course )
 	{
 		ArrayList<Assignment> assignments = Assignment.getAssignments( course );
     	
     	mAssignments.put(course, assignments);
     	return assignments;
 	}
 	
 	Timestamp hackDate( String date )
 	{
 		// Annoyingly the string format for the Java times isn't the same as our SQL data
 		// Hack it up so it accepts it
 		String hackDate = date;
 		hackDate = hackDate.replaceFirst("T", " ");		
 		Timestamp time = Timestamp.valueOf(hackDate); 
 		return time;
 	}
 	
 	public void updateGrades()
 	{
 		if( mCourses.size() == 0 )
 		{
 			updateClasses();
 		}
 		
 		Set<Course> courses = mCourses.keySet();
 		
 		for( Course c : courses )
 		{
 			// First get the assignments for this class
 			ArrayList<Assignment> assignments = getAssignments( c );
 			
 			// Then get the grades for this class
 			
 			List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
 			properties.add(new PropertyWrapper("aStudentID", mAccount.mId));
 			properties.add(new PropertyWrapper("aClassID", c.mId));
 			
 			ArrayList<Grade> grades = new ArrayList<Grade>();
 			
 	    	/* Call web service */
 	    	try 
 	    	{		
 	    		SoapSerializationEnvelope envelope = WebServiceWrapper.getInstance().call(Types.STUDENT_URL, Types.STUDENT_GET_GRADES, properties);
 	    	
 	    		SoapObject response = (SoapObject)envelope.getResponse();
 	
 	    		// It's a list of classes
 	    		for( int i = 0; i < response.getPropertyCount(); i++ )
 	    		{
 		    		SoapObject arr = (SoapObject)response.getProperty(i);
 		    		
 			   		if( arr.hasProperty("mGradeID") &&
 			   			arr.hasProperty("mAssignmentID") &&
 			   			arr.hasProperty("mStudentID") &&
 			   			arr.hasProperty("mPointsEarned") &&
 			   			arr.hasProperty("mDateSubmitted") )
 			   		{
 			   			int gradeId = java.lang.Integer.parseInt( arr.getProperty("mGradeID").toString() );
 			   			int assignId = java.lang.Integer.parseInt( arr.getProperty("mAssignmentID").toString() );
 			   			double pointsEarned = java.lang.Double.parseDouble( arr.getProperty("mPointsEarned").toString() );
 			   			Timestamp time = hackDate( arr.getProperty("mDateSubmitted").toString() );
 			   			
 			   			// Find the assignment that corresponds with this grade
 			   			Assignment ours = null;
 			   			for( Assignment a : assignments )
 			   			{
 			   				if( a.mId == assignId )
 			   				{
 			   					ours = a;
 			   					break;
 			   				}
 			   			}
			   			assignments.remove(ours);
 			   			
 			   			grades.add( new Grade( ours, gradeId, pointsEarned, time ) );
 			   		
 			   		}
 	    		}
 	    	}
 	    	catch(Exception ex) {}	
 	    	
 	    	mCourses.put( c, grades );
 		}
 		
 	}
 	
 	public ArrayList<Assignment> getFutureAssignments()
 	{
 		Collection<ArrayList<Assignment>> col = mAssignments.values();
 		
 		ArrayList<Assignment> rets = new ArrayList<Assignment>();
 		
 		for( ArrayList<Assignment> li : col )
 		{
 			for( Assignment ass : li )
 			{
 				if( ass.mDueDate.after(new Date()))
 				{
 					// If the due date is later than the current time
 					rets.add(ass);
 				}
 			}
 		}
 		
 		return rets;
 		
 	}
 }
