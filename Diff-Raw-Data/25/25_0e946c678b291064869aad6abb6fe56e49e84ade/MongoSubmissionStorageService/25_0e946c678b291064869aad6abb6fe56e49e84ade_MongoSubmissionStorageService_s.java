 package edu.harvard.cs262.grading.server.services;
 
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Date;
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
 import com.mongodb.ServerAddress;
 
 public class MongoSubmissionStorageService implements SubmissionStorageService {
 
 	private Mongo m;
 	private DB db;
 	private DBCollection coll;
 
 	public void init() throws UnknownHostException, MongoException {
 		ConfigReader config = new ConfigReaderImpl();
 		List<String> servers = config.getRegistryLocations("SubmissionStorageServiceDB");
 		List<ServerAddress> addrs = new ArrayList<ServerAddress>();
 		for (String server : servers)
 		{
 			int split = server.indexOf(":");
 			String host = server.substring(0, split);
 			int port = Integer.parseInt(server.substring(split+1));
 			addrs.add(new ServerAddress(host, port));
 		}
 		m = new Mongo(addrs);
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
 
 	public Submission getSubmission(Student student, Assignment assignment,
 			Timestamp timestamp) {
 		BasicDBObject query = new BasicDBObject();
 		query.put("studentID", student.studentID());
 		query.put("assignmentID", assignment.assignmentID());
 		query.put("timestamp", timestamp);
 
 		DBObject info = coll.findOne(query);
 
 		Submission submission = new SubmissionImpl(student, assignment,
				(byte[]) info.get("contents"));
 
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
 		if (objs.size() == 0)
 			return null;
 
 		DBObject latest = objs.get(objs.size() - 1);
 
 		Submission submission = new SubmissionImpl(student, assignment,
 				(byte[]) latest.get("contents"), new Timestamp(
 						((Date) latest.get("timestamp")).getTime()));
 
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
 					new AssignmentImpl((Long) (result.get("assignmentID"))),
 					(byte[]) result.get("contents"), new Timestamp(
 							((Date) result.get("timestamp")).getTime()));
 			submissions.add(submission);
 		}
 
 		return submissions;
 	}
 
 	@Override
 	public Set<Submission> getStudentWork(Student student)
 			throws RemoteException {
 
 		BasicDBObject query = new BasicDBObject();
 		query.put("studentID", student.studentID());
 
 		DBCursor results = coll.find(query);
 
 		Set<Submission> submissions = new LinkedHashSet<Submission>();
 
 		for (DBObject result : results) {
 			Submission submission = new SubmissionImpl(student,
 					new AssignmentImpl((Long) (result.get("assignmentID"))),
 					(byte[]) result.get("contents"), new Timestamp(
 							((Date) result.get("timestamp")).getTime()));
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
 			Submission submission = new SubmissionImpl(new StudentImpl(
 					(Long) result.get("studentID")), assignment,
 					(byte[]) result.get("contents"), new Timestamp(
 							((Date) result.get("timestamp")).getTime()));
 			submissions.add(submission);
 		}
 
 		return submissions;
 	}
 
 	public static void main(String[] args) {
 
 		try {
 			MongoSubmissionStorageService obj = new MongoSubmissionStorageService();
 			obj.init();
 			SubmissionStorageService stub = (SubmissionStorageService) UnicastRemoteObject
 					.exportObject(obj, 0);
 
 			// Bind the remote object's stub in the registry
 			Registry registry = LocateRegistry.getRegistry();
 
 			// check for registry update command
 			boolean forceUpdate = false;
 			for (int i = 0, len = args.length; i < len; i++)
 				if (args[i].equals("--update"))
 					forceUpdate = true;
 
 			if (forceUpdate) {
 				registry.rebind("SubmissionStorageService", stub);
 			} else {
 				registry.bind("SubmissionStorageService", stub);
 			}
 
 			System.err.println("MongoSubmissionStorageService running");
 
 		} catch (Exception e) {
 			System.err.println("Server exception: " + e.toString());
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void heartbeat() throws RemoteException {
 		// TODO Auto-generated method stub
 
 	}
 
 }
