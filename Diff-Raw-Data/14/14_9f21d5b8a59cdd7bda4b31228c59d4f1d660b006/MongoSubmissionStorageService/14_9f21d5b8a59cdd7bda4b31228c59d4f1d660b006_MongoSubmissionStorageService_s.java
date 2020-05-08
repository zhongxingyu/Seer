 package edu.harvard.cs262.grading;
 
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.Timestamp;
 import java.util.Set;
 
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.BasicDBObject;
 import com.mongodb.MongoException;
 
 public class MongoSubmissionStorageService implements SubmissionStorageService {
 
 	private Mongo m;
 	private DB db;
 	private DBCollection coll;
 
 	public void init() throws UnknownHostException, MongoException {
 		m = new Mongo();
 		db = m.getDB("dgs");
 		coll = db.getCollection("submissions");
 	}
 
 	@Override
 	public void storeSubmission(Submission submission) {
 		// XXX: Active question: can Mongo's "put" method really accept /any/
 		// objects?
 		// How does that work? Surely they must be serializable?
 
 		BasicDBObject doc = new BasicDBObject();
 
 		doc.put("studentID", submission.getStudent().studentID());
 		doc.put("assignmentID", submission.getAssignment().assignmentID());
 		doc.put("timestamp", submission.getTimeStamp());
 		doc.put("contents", submission.getContents());
 
 		coll.insert(doc);
 	}
 
 	public Submission getSubmission(Student student, Assignment assignment, Timestamp timestamp) {
 		BasicDBObject query = new BasicDBObject();
 		query.put("studentID", student.studentID());
 		query.put("assignmentID", assignment.assignmentID());
 		query.put("timestamp", timestamp);
 		
 		DBObject info = coll.findOne(query);
 		
 		Submission submission = new SubmissionImpl(student, assignment, (byte[]) info.get("contents"));
 		
 		return submission;
 	}
 	
 	public Submission getLatestSubmission(Student student, Assignment assignment) {
 		BasicDBObject query = new BasicDBObject();
 		query.put("studentID", student.studentID());
 		query.put("assignmentID", assignment.assignmentID());
 		
 		DBCursor results = coll.find(query);
 		DBObject toSortBy = new BasicDBObject();
 		
 		// XXX: is this right?
 		toSortBy.put("timestamp", null);
 		results.sort(toSortBy);
 		
 		List<DBObject> objs = results.toArray();
 		DBObject latest = objs.get(objs.size() - 1);
 		
 		Submission submission =
 			new SubmissionImpl(student, assignment,
								(byte[]) latest.get("contents"), (Timestamp) latest.get("timestamp"));
 		
 		return submission;
 	}
 	
 	public Set<Submission> getSubmissions(Student student, Assignment assignment) {
 		BasicDBObject query = new BasicDBObject();
 		query.put("studentID", student.studentID());
 		query.put("assignmentID", assignment.assignmentID());
 		
 		DBCursor results = coll.find(query);
 		
 		Set<Submission> submissions = new LinkedHashSet<Submission>();
 		for (DBObject result : results) {
 			Submission submission = new SubmissionImpl(student, 
 					new AssignmentImpl((Long)(result.get("assignmentID"))), 
 					(byte[]) result.get("contents"),
					(Timestamp) result.get("timestamp"));
 			submissions.add(submission);		
 		}
 		
 		
 		return submissions;		
 	}
 
 	@Override
 	public Set<Submission> getStudentWork(Student student)
 			throws RemoteException {
 
 		BasicDBObject query = new BasicDBObject();
 		query.put("studentID", student);
 		
 		DBCursor results = coll.find(query);
 		
 		Set<Submission> submissions = new LinkedHashSet<Submission>();
 		
 		for (DBObject result : results) {
 			Submission submission = new SubmissionImpl(student, 
 					new AssignmentImpl((Long)(result.get("assignmentID"))), 
 					(byte[]) result.get("contents"),
					(Timestamp) result.get("timestamp"));
 			submissions.add(submission);
 		}
 		
 		return submissions;
 	}
 
 	@Override
 	public Set<Submission> getAllSubmissions(Assignment assignment)
 			throws RemoteException {
 
 		BasicDBObject query = new BasicDBObject();
 		query.put("assignmentID", assignment.assignmentID());
 		
 		DBCursor results = coll.find(query);
 		
 		Set<Submission> submissions = new LinkedHashSet<Submission>();
 		
 		for (DBObject result : results) {
 			Submission submission = new SubmissionImpl(
 					new StudentImpl((Long) result.get("studentID")),
 					assignment, 
 					(byte[]) result.get("contents"),
					(Timestamp) result.get("timestamp"));
 			submissions.add(submission);
 		}
 		
 		return submissions;
 	}
 
 	// TODO (kennyu): Is this right?  How would we know?
 	public static void main(String[] args) {
 		try {
 			MongoSubmissionStorageService obj = new MongoSubmissionStorageService();
 			SubmissionStorageService stub = (SubmissionStorageService) UnicastRemoteObject
 					.exportObject(obj, 0);
 
 			// Bind the remote object's stub in the registry
 			Registry registry = LocateRegistry.getRegistry();
 			obj.init();
 			registry.bind("SubmissionStorageService", stub);
 			
 			System.err.println("MongoSubmissionStorageService running");
 			
 		} catch (Exception e) {
 			System.err.println("Server exception: " + e.toString());
 			e.printStackTrace();
 		}
 	}
 
 }
