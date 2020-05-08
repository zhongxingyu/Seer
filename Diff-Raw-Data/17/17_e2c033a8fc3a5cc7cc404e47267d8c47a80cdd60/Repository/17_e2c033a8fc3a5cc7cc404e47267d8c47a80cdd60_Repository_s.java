 package net.maltera.daranable.edinet.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.ref.SoftReference;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import net.maltera.daranable.edinet.model.Database.DatabaseVersionException;
 
 import com.google.common.collect.Maps;
 
 public class Repository {
 	private Map<Integer, SoftReference< Course >> 
 			courses = Maps.newHashMap();
 	
 	private Map<Integer, SoftReference< Assignment >> 
 			assignments = Maps.newHashMap();
 	
 	private Map<TermReference, SoftReference< Term >>
 			terms = new WeakHashMap<TermReference, SoftReference<Term>>();
 	
 	private Database database;
 	
 	public Repository()
 	throws IOException, SQLException, DatabaseVersionException {
 		this.database = new Database();
 	}
 	
 	private PreparedStatement stmtCourseById;
 	
 	public Course getCourse( int id ) 
 	throws SQLException, IOException {
 		SoftReference<Course> cached = courses.get( id );
 		if (null != cached && null != cached.get())
 			return cached.get();
 		
 		if (null == stmtCourseById) {
 			stmtCourseById = database.getConnection().prepareStatement(
 					"SELECT course.*\n" +
 					"FROM courses AS course\n" +
 					"WHERE course.id = ?" );
 		}
 		
		stmtCourseById.setInt( 0, id );
 		ResultSet result = stmtCourseById.executeQuery();
 		if (!result.next()) return null;
 		
 		Course inst = Course.load( this, result );
 		courses.put( id, new SoftReference<Course>( inst ) );
 		return inst;
 	}
 	
 	private PreparedStatement stmtAssignmentById;
 	
 	public Assignment getAssignment( int id )
 	throws SQLException, IOException {
 		SoftReference<Assignment> cached = assignments.get( id );
 		if (null != cached && null != cached.get())
 			return cached.get();
 		
 		if (null == stmtAssignmentById) {
 			stmtAssignmentById = database.getConnection().prepareStatement( 
 					"SELECT assignment.*\n" +
 					"FROM assignments as assignment\n" +
 					"WHERE assignment.id = ?" );
 		}
 		
		stmtAssignmentById.setInt( 0, id );
 		ResultSet result = stmtAssignmentById.executeQuery();
 		if (!result.next()) return null;
 		
 		Assignment inst = Assignment.load( this, result );
 		assignments.put( id, new SoftReference<Assignment>( inst ) );
 		return inst;
 	}
 	
 	private PreparedStatement stmtTermByReference;
 	
 	public Term getTerm( TermReference ref )
 	throws SQLException, IOException {
 		SoftReference<Term> cached = terms.get( ref );
 		if (null != cached && null != cached.get())
 			return cached.get();
 		
 		if (null == stmtTermByReference) {
 			stmtTermByReference = database.getConnection().prepareStatement(
 					"SELECT term.*\n" +
 					"FROM terms as term\n" +
 					"WHERE term.year = ? AND term.serial = ?" );
 		}
 		
		stmtTermByReference.setInt( 0, ref.getYear() );
		stmtTermByReference.setInt( 1, ref.getSerial() );
 		ResultSet result = stmtTermByReference.executeQuery();
 		if (!result.next()) return null;
 		
 		Term inst = Term.load( this, result );
		// FIXME: implement TermReference.hashCode() to make this work
 		terms.put( inst, new SoftReference<Term>( inst ) );
 		return inst;
 	}
 }
