 package edu.harvard.cs262.grading.server.services;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class ShardImpl implements Shard {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2985077449389912337L;
 	private Map<Long, Set<Long>> sharding;
 
 	public ShardImpl() {
 		sharding = new LinkedHashMap<Long, Set<Long>>();
 	}
 	
 	public ShardImpl(Map<Long, Set<Long>> sharding) {
 		this.sharding = sharding;
 	}
 
 	/*
 	 * public ShardImpl(int shardID) { this.shardID = shardID; sharding = new
 	 * LinkedHashMap<Student, Set<Student>>(); }
 	 */
 
	public void addGrader(Student grader, Student gradee) {
		if (sharding.containsKey(grader.studentID())) {
			sharding.get(grader.studentID()).add(gradee.studentID());
 		} else {
 			Set<Long> gradeeSet = new LinkedHashSet<Long>();
			gradeeSet.add(gradee.studentID());
			sharding.put(grader.studentID(), gradeeSet);
 		}
 	}
 
 	@Override
 	public Set<Long> getGraders(Student student) {
 		Set<Long> graderSet = new LinkedHashSet<Long>();
 
 		for (Long grader : sharding.keySet()) {
 			if (sharding.get(grader).contains(student)) {
 				graderSet.add(grader);
 			}
 		}
 		return graderSet;
 	}
 
 	@Override
 	public Map<Long, Set<Long>> getShard() {
 		return sharding;
 	}
 
 	@Override
 	public int shardID() {
 		return this.hashCode();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((sharding == null) ? 0 : sharding.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ShardImpl other = (ShardImpl) obj;
 		if (sharding == null) {
 			if (other.sharding != null)
 				return false;
 		} else if (!sharding.equals(other.sharding))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "ShardID: " + shardID() + " Mapping:\n" + sharding;
 	}
 
 }
