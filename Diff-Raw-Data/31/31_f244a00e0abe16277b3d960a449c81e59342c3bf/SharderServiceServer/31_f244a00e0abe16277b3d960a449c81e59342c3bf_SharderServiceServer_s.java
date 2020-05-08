 package edu.harvard.cs262.grading.server.services;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 
 public class SharderServiceServer implements SharderService {
 
 	static int GRADERS_PER_SUBMISSION = 2;
 
 	private Mongo m;
 	private DB db;
 	private DBCollection coll;
 
 	private SubmissionStorageService storage;
 
 	private SubmissionStorageService getStorage() throws RemoteException {
 		ConfigReader config = new ConfigReaderImpl();
 		SubmissionStorageService storage = (SubmissionStorageService) ServiceLookupUtility
 				.lookupService(config, "SubmissionStorageService");
 		if (storage == null) {
 			System.err.println("Looking up SubmissionStorageService failed.");
 			return null;
 		} else {
 			return storage;
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private int getNextShardID() {
 		BasicDBObject query = new BasicDBObject();
 
 		DBCursor results = coll.find(query);
 
 		int nextID = 0;
 		for (DBObject result : results) {
 			if (((Integer) result.get("id")) > nextID)
 				nextID = (Integer) result.get("id");
 		}
 
 		return nextID + 1;
 	}
 
 	private Shard readShard(int ShardID) {
 		BasicDBObject query = new BasicDBObject();
 		query.put("id", ShardID);
 
 		ShardImpl shard = new ShardImpl();
 
 		DBCursor results = coll.find(query);
 
 		for (DBObject result : results) {
 			shard.addGrader((Long) result.get("grader"),
 					(Long) result.get("gradee"));
 		}
 
 		return shard;
 	}
 
 	private void writeShard(Shard shard, Assignment assignment) {
 		Map<Long, Set<Long>> sharding = shard.getShard();
 
 		for (Long grader : sharding.keySet()) {
 			for (Long gradee : sharding.get(grader)) {
 
 				BasicDBObject doc = new BasicDBObject();
 
 				doc.put("id", shard.shardID());
 				doc.put("assignmentID", assignment.assignmentID());
 				doc.put("grader", grader);
 				doc.put("gradee", gradee);
 
 				coll.insert(doc);
 			}
 		}
 	}
 
 	public Shard assign(Set<Student> students, Set<Submission> submissions)
 			throws RemoteException {
 		ShardImpl shard = new ShardImpl();
 
 		Random rand = new Random();
 		Map<Student, Integer> canStillGrade = new LinkedHashMap<Student, Integer>();
 		for (Student s : students) {
 			canStillGrade.put(s, GRADERS_PER_SUBMISSION);
 		}
 
 		for (Submission s : submissions) {
 			for (int i = 0; i < GRADERS_PER_SUBMISSION; i++) {
 				Student grader = s.getStudent();
 				if (canStillGrade.isEmpty())
 					throw new RemoteException("Can't find enough graders");
 				if ((canStillGrade.size() == 1)
 						&& canStillGrade.containsKey(s.getStudent())) {
 					return assign(students, submissions);
 				}
 				while (grader.equals(s.getStudent()))
 					grader = (Student) canStillGrade.keySet().toArray()[rand
 							.nextInt(canStillGrade.size())];
 				shard.addGrader(grader.studentID(), s.getStudent().studentID());
 				if (canStillGrade.get(grader) == 1) {
 					canStillGrade.remove(grader);
 				} else {
 					canStillGrade.put(grader, canStillGrade.get(grader) - 1);
 				}
 			}
 		}
 
 		return shard;
 	}
 
 	@Override
 	public Shard generateShard(Assignment assignment) throws RemoteException {
 		Set<Submission> submissions = storage.getAllSubmissions(assignment);
 
 		// Compile list of students
 		Set<Student> students = new LinkedHashSet<Student>();
 		for (Submission s : submissions) {
 			students.add(s.getStudent());
 		}
 
 		// For each student, this will store the latest submission
 		// XXX: This does a ton of database queries. But it's the simplest thing
 		// to implement
 		Set<Submission> latestSubmissions = new LinkedHashSet<Submission>();
 		for (Student student : students) {
 			latestSubmissions.add(storage.getLatestSubmission(student,
 					assignment));
 		}
 
 		// Randomly assign graders. We could be smarter.
 		Shard shard = assign(students, latestSubmissions);
 
 		writeShard(shard, assignment);
 		return shard;
 
 	}
 
 	@Override
 	public Shard getShard(int shardID) throws RemoteException {
 		return readShard(shardID);
 	}
 
	public int getShardID(Assignment assignment) throws RemoteException,
			NoShardsForAssignmentException {
 		BasicDBObject query = new BasicDBObject();
 		query.put("assignmentID", assignment.assignmentID());
 
 		DBCursor results = coll.find(query);
 		DBObject toSortBy = new BasicDBObject();
 
 		// XXX: is this right?
 		toSortBy.put("id", null);
 		results.sort(toSortBy);
 
 		if (results.size() == 0) {
 			throw new NoShardsForAssignmentException(assignment);
 		} else {
 			List<DBObject> objs = results.toArray();
 			DBObject latest = objs.get(objs.size() - 1);
 			return (Integer) latest.get("id");
 		}
 
 	}
 
 	public static void main(String[] args) {
 
 		try {
 			SharderServiceServer obj = new SharderServiceServer();
 			obj.init();
 			SharderService stub = (SharderService) UnicastRemoteObject
 					.exportObject(obj, 0);
 
 			// Bind the remote object's stub in the registry
 			Registry registry = LocateRegistry.getRegistry();
 
 			// check for registry update command
 			boolean forceUpdate = false;
 			for (int i = 0, len = args.length; i < len; i++)
 				if (args[i].equals("--update"))
 					forceUpdate = true;
 
 			if (forceUpdate) {
 				registry.rebind("SharderService", stub);
 			} else {
 				registry.bind("SharderService", stub);
 			}
 
 			System.err.println("SharderService running");
 
 		} catch (Exception e) {
 			System.err.println("Server exception: " + e.toString());
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void init() throws Exception {
 
 		m = new Mongo();
 		db = m.getDB("dgs");
 		coll = db.getCollection("shards");
		storage = getStorage();
 
 	}
 
 	@Override
 	public void heartbeat() throws RemoteException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
	public long putShard(Assignment assignment, Map<Long, Set<Long>> gradermap)
			throws RemoteException {
 		Shard shard = new ShardImpl(gradermap);
 		writeShard(shard, assignment);
 
 		return shard.shardID();
 	}
 
}
