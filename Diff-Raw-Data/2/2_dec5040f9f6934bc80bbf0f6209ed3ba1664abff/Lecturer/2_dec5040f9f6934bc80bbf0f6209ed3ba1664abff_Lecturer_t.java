 package de.dhbw.wbs;
 
 public class Lecturer {
 	private String name;
 
 	public Lecturer(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public boolean equals(Object aLecturer) {
 		if (!(aLecturer instanceof Lecturer))
 			return false;
 
		return ((Lecturer) aLecturer).name.equals(this.name);
 	}
 }
