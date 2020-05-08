 package ca.ubc.ctlt.copyalerts.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import ca.ubc.ctlt.copyalerts.jsonintermediate.CourseFiles;
 import ca.ubc.ctlt.copyalerts.jsonintermediate.FileList;
 import ca.ubc.ctlt.copyalerts.jsonintermediate.FilePath;
 
 import blackboard.cms.filesystem.CSFile;
 import blackboard.data.course.Course;
 import blackboard.db.BbDatabase;
 import blackboard.db.ConnectionManager;
 import blackboard.db.ConnectionNotAvailableException;
 import blackboard.persist.Id;
 import blackboard.persist.PersistenceException;
 import blackboard.persist.course.CourseDbLoader;
 
 public class FilesTable
 {
 	private final static String TABLENAME = "ubc_ctlt_ca_files";
 	private final static int ENTRYPERPAGE = 25;
 	
 	// store mapping of course names to course title, names are id like, title is human readable
 	private HashMap<String, String> courseNameToTitle = new HashMap<String, String>(); 
 	private HashMap<String, String> courseNameToId = new HashMap<String, String>();
 	
 	// have to load paging specially as each course can be paged
 	// have a loadCourseFiles or getCourseFiles
 	public FileList load(String userid) throws InaccessibleDbException
 	{
 		HashMap<String, CourseFiles> ret = loadAll(userid);
 		for (Entry<String, CourseFiles> e : ret.entrySet())
 		{
 			CourseFiles cf = e.getValue();
 			cf = splitToPage(cf, 1);
 			ret.put(e.getKey(), cf);
 		}
 		
 		return new FileList(ret, 1, 1);
 	}
 	
 	public CourseFiles loadCourseFiles(String userid, String courseId, int page) throws InaccessibleDbException
 	{
 		HashMap<String, CourseFiles> list = loadAll(userid);
 		if (list.containsKey(courseId))
 		{
 			CourseFiles cf = list.get(courseId);
 			return splitToPage(cf, page);
 		}
 		return null;
 	}
 
 	public void add(CSFile file, ArrayList<Id> users) throws InaccessibleDbException
 	{
 		ConnectionManager cm = BbDatabase.getDefaultInstance().getConnectionManager();
 		Connection conn = null;
 		String query = "insert into "+ TABLENAME +" (pk1, userid, course, filepath) values ("+ TABLENAME +"_seq.nextval, ?, ?, ?)";
 		PreparedStatement stmt;
 		try
 		{
 			conn = cm.getConnection();
 			// convert the query string into a compiled statement for faster execution
 			stmt = conn.prepareStatement(query);
 			
 			String courseName = parseCourseName(file.getFullPath());
 
 			for (Id e : users)
 			{
 				stmt.setString(1, e.toExternalString());
 				stmt.setString(2, getCourseTitle(courseName));
 				stmt.setString(3, file.getFullPath());
 				stmt.executeUpdate();
 			}
 			stmt.close();
 		} catch (SQLException e)
 		{
 			throw new InaccessibleDbException("Couldn't execute query", e);
 		} catch (ConnectionNotAvailableException e)
 		{
 			throw new InaccessibleDbException("Unable to connect to db", e);
 		} catch (PersistenceException e)
 		{
 			throw new InaccessibleDbException("Unable to read from db", e);
 		}
 		finally
 		{
 			if (conn != null) cm.releaseConnection(conn); // MUST release connection or we'll exhaust connection pool
 		}
 	}
 	
 	public void deleteAll() throws InaccessibleDbException
 	{
 		ConnectionManager cm = BbDatabase.getDefaultInstance().getConnectionManager();
 		Connection conn = null;
 		String query = "delete from "+ TABLENAME;
 		Statement stmt;
 		try
 		{
 			conn = cm.getConnection();
 			stmt = conn.createStatement();
 			stmt.executeUpdate(query);
 		} catch (SQLException e)
 		{
 			throw new InaccessibleDbException("Couldn't execute query", e);
 		} catch (ConnectionNotAvailableException e)
 		{
 			throw new InaccessibleDbException("Unable to connect to db", e);
 		}
 		finally
 		{
 			if (conn != null) cm.releaseConnection(conn); // MUST release connection or we'll exhaust connection pool
 		}
 	}
 	
 	/**
 	 * Parse out the course name from the full content system path.
 	 * @param path
 	 * @return
 	 */
 	private String parseCourseName(String path)
 	{
 		// try to parse out the course name from the path
 		// first remove the /courses/
 		String courseName = path.substring(9);
 		// next segment in the path should be the course name
 		courseName = courseName.substring(0, courseName.indexOf("/"));
 		return courseName;
 	}
 	
 	/**
 	 * Get the course title from the course name. The course title is human readable and includes
 	 * the full text of the subject being taught. E.g.: Course name would be something like Math101
 	 * while course title would be Math101 - Integral Calculus with Applications to Physical Sciences
 	 * @param courseName
 	 * @return
 	 * @throws PersistenceException
 	 */
 	private String getCourseTitle(String courseName) throws PersistenceException
 	{
 		cacheCourseInfo(courseName);
 		return courseNameToTitle.get(courseName);
 	}
 	
 	/**
 	 * Get the course id, this is the id string used by blackboard to identify courses.
 	 * @param courseName
 	 * @return
 	 * @throws PersistenceException
 	 */
 	private String getCourseId(String courseName) throws PersistenceException
 	{
 		cacheCourseInfo(courseName);
 		return courseNameToId.get(courseName);
 	}
 	
 	/**
 	 * Store course id and course title in a cache for faster access. Course titles and id are stored in hash maps
 	 * accessed by their course name.
 	 */
 	private void cacheCourseInfo(String courseName) throws PersistenceException
 	{
 		CourseDbLoader cload = CourseDbLoader.Default.getInstance();
 		if (!courseNameToTitle.containsKey(courseName) || !courseNameToId.containsKey(courseName))
 		{
 			Course course = cload.loadByCourseId(courseName);
 			courseNameToTitle.put(courseName, course.getTitle());
 			courseNameToId.put(courseName, course.getId().toExternalString());
 		}
 	}
 	
 	/**
 	 * Load all files that the given user needs to tag.
 	 * @param userid
 	 * @return
 	 * @throws InaccessibleDbException
 	 */
 	private HashMap<String, CourseFiles> loadAll(String userid) throws InaccessibleDbException
 	{
 		ConnectionManager cm = BbDatabase.getDefaultInstance().getConnectionManager();
 		Connection conn = null;
 
 		HashMap<String, CourseFiles> ret = new HashMap<String, CourseFiles>();
 		try 
 		{
 			conn = cm.getConnection();
 			String query = "SELECT course, filepath FROM "+ TABLENAME +" WHERE userid=?";
 	        PreparedStatement stmt = conn.prepareStatement(query);
 			stmt.setString(1, userid);
 	        ResultSet res = stmt.executeQuery();
 	
 	        while(res.next())
 	        {
	        	String courseTitle = res.getString(1);
 	        	String path = res.getString(2);
         		String courseName = parseCourseName(path);
 	        	String id = getCourseId(courseName);
 	        	// STORE PATH FOR RETURN
 	        	if (ret.containsKey(id))
 	        	{ // add to existing entry
 	        		ret.get(id).addFile(path);
 	        	}
 	        	else
 	        	{ // create new entry
	        		CourseFiles cf = new CourseFiles(id, courseName, courseTitle, path);
 	        		ret.put(id, cf);
 	        	}
 	        }
 	
 	        res.close();
 	
 	        stmt.close();
 		} catch (SQLException e)
 		{
 			throw new InaccessibleDbException("Couldn't execute query", e);
 		} catch (ConnectionNotAvailableException e)
 		{
 			throw new InaccessibleDbException("Unable to connect to db", e);
 		} catch (PersistenceException e)
 		{
 			throw new InaccessibleDbException("Unable to load course data", e);
 		}
 		finally
 		{
 			if (conn != null) cm.releaseConnection(conn);
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Pagination helper to split up an ArrayList of FilePaths into pages.
 	 * 
 	 * @param cf
 	 * @param page
 	 * @return
 	 */
 	private CourseFiles splitToPage(CourseFiles cf, int page)
 	{
 		int numPages = (int) Math.ceil(cf.files.size() / (double) ENTRYPERPAGE);
 		
 		if (page < 1 || page > numPages)
 		{ // trying to get a non-existent page, just return the first page
 			System.out.println("page " + page + " numpages " + numPages);
 			return splitToPage(cf, 1);
 		}
 		
 		int toIndex = (page * ENTRYPERPAGE);
 		int fromIndex = toIndex - ENTRYPERPAGE;
 		
 		if (toIndex > cf.files.size())
 		{ // prevent out of bounds exceptions
 			toIndex = cf.files.size();
 		}
 		
 		cf.files = new ArrayList<FilePath>(cf.files.subList(fromIndex, toIndex));
 		cf.page = page;
 		cf.numPages = numPages;
 		
 		return cf;
 	}
 }
