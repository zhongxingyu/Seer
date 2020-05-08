 /* RPI Planner - Customized plans of study for RPI students.
  *
  * Copyright (C) 2008 Eric Allen allene2@rpi.edu
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package rpiplanner.model;
 
 import java.util.ArrayList;
 
 import rpiplanner.SchoolInformation;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 @XStreamAlias("term")
 public class Term {
 	@XStreamOmitField
 	PlanOfStudy plan;
 	
 	@XStreamImplicit
 	private ArrayList<Course> courses = new ArrayList<Course>(SchoolInformation.getDefaultCoursesPerSemester());
 	
 	private int year;
 	private YearPart term;
 	
 	public int getYear() {
 		return year;
 	}
 
 	public void setYear(int year) {
 		this.year = year;
 	}
 
 	public YearPart getTerm() {
 		return term;
 	}
 
 	public void setTerm(YearPart term) {
 		this.term = term;
 	}
 
 	public ArrayList<Course> getCourses() {
 		if(courses == null){
 			courses = new ArrayList<Course>(SchoolInformation.getDefaultCoursesPerSemester());
 		}
 		return courses;
 	}
 
 	public void add(Course toAdd) {
 		courses.add(toAdd);
 		plan.fireCoursesChanged();
 	}
 	
 	public void remove(int index){
 		courses.remove(index);
 		plan.fireCoursesChanged();
 	}
 
 	public void replace(int index, Course replacement) {
 		courses.set(index, replacement);
 		plan.fireCoursesChanged();
 	}
 }
