 package com.open.rotile.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 
 @Entity
 public class Project {
 	@Id
 	private String name;
 	protected List<Vote> votes = new ArrayList<Vote>();
 
 	public Project() {
 	}
 
 	public Project(String name) {
 		this.name = name;
 	}
 
 	public String name() {
 		return name;
 	}
 
 	public void vote(int vote) {
 		votes.add(new Vote(vote));
 	}
 
 	public int nbVotes() {
 		return votes.size();
 	}
 
 	public int average() {
 		int average = 0;
 		for (Vote vote : votes) {
 			average += vote.vote();
 		}
 		return average / votes.size();
 	}
 
 	@Override
 	public String toString() {
 		return name + " - " + votes.toString();
 	}
 
 }
