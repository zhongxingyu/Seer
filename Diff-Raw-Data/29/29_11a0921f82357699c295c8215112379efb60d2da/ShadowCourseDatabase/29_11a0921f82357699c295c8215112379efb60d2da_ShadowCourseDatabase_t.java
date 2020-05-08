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
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 public class ShadowCourseDatabase implements CourseDatabase {
 	@XStreamImplicit
 	private HashSet<Course> courses = new HashSet<Course>();
 	
 	@XStreamOmitField
 	private DefaultCourseDatabase shadowee;
 
 	@XStreamOmitField
 	private Set<String> departments;
 	
 	@XStreamOmitField
 	private PropertyChangeSupport support;
 
     private static ShadowCourseDatabase instance;
 
     public ShadowCourseDatabase() {
         instance = this;
     }
 
     static CourseDatabase getMainDatabase(){
         return instance;
     }
 	
 	public void shadow(DefaultCourseDatabase mainDB) {
 		this.shadowee = mainDB;
 	}
 
 	public Course[] search(String text) {
 		TreeSet<Course> found = new TreeSet<Course>();
 		for(Course c : courses){
 			if(c.toString().toLowerCase().contains(text.toLowerCase()))
 				found.add(c);
 		}
 		for(Course c : shadowee.search(text))
 			found.add(c); // will only add if not found in shadow database
 		return found.toArray(new Course[0]);
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		if(support == null)
 			support = new PropertyChangeSupport(this);
 		support.addPropertyChangeListener(listener);
 	}
 
 	public Course getCourse(String catalogNumber) {
 		for(Course c : courses){
 			if(c.getCatalogNumber().equals(catalogNumber))
 				return c;
 		}
 		return shadowee.getCourse(catalogNumber);
 	}
 
 	public List<String> getDepartments() {
 		if(departments == null){
 			departments = new HashSet<String>();
 			for(Course c : courses){
 				departments.add(c.getDepartment());
 			}
 			departments.addAll(shadowee.getDepartments());
 		}
 		return new ArrayList<String>(departments);
 	}
 
 	public Course[] listCourses() {
 		if(courses == null)
 			courses = new HashSet<Course>();
 		TreeSet<Course> listedCourses = new TreeSet<Course>();
 		listedCourses.addAll(courses);
 		listedCourses.addAll(shadowee.getCourses());
 		return listedCourses.toArray(new Course[0]);
 	}
 
 	public Degree[] listDegrees() {
 		return shadowee.listDegrees();
 	}
 
 	public void addOrUpdate(Course updated) {
 		courses.remove(updated);
 		courses.add(updated);
 		support.firePropertyChange("courses", null, courses);
 	}
 
 	public Degree getDegree(long id) {
 		return shadowee.getDegree(id);
 	}

    protected Object readResolve(){
        instance = this;
        return this;
    }
 }
