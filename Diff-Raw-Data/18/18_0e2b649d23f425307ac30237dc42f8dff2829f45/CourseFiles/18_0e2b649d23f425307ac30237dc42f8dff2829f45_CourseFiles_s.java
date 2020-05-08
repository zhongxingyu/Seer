 package ca.ubc.ctlt.copyalerts.jsonintermediate;
 
 import java.util.ArrayList;
 
 public class CourseFiles
 {
 	public String courseId;
 	public String name;
 	public int numFiles;
 	public int page = 1;
 	public int numPages = 2;
 	public ArrayList<FilePath> files = new ArrayList<FilePath>();
 	
 	/**
 	 * Assuming that all entries in paths belongs in the same course.
 	 * 
 	 * @param paths
 	 */
	public CourseFiles(String courseId, String course, String path)
 	{
		this.name = course;
 		this.courseId = courseId;
 		addFile(path);
 	}
 	
 	public void addFile(String path)
 	{
 		FilePath newPath = new FilePath(path);
 		files.add(newPath);
 		numFiles = files.size();
 	}
 	
 }
