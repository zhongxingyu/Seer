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
 
 import java.util.List;
 
 public class EditableCourse extends Course {
 	public EditableCourse(Course toEdit) {
 		this.title = toEdit.title;
 		this.description = toEdit.description;
 		this.department = toEdit.department;
 		this.catalogNumber = toEdit.catalogNumber;
 		this.credits = toEdit.credits;
 		if(toEdit.prerequisites != null)
 			this.prerequisites = (RequisiteSet)toEdit.prerequisites.clone();
 		if(toEdit.corequisites != null)
 			this.corequisites = (RequisiteSet)toEdit.corequisites.clone();
 		if(toEdit.availableTerms != null)
 			this.availableTerms = toEdit.availableTerms.clone();
 	}
 	public EditableCourse() {}
 	
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public void setDepartment(String department) {
 		this.department = department;
 	}
 	public void setCatalogNumber(String catalogNumber) {
 		this.catalogNumber = catalogNumber;
 	}
 	public void setCredits(int credits) {
 		this.credits = credits;
 	}
 	public void setPrerequisites(List<Course> prerequisites) {
		this.prerequisites = new RequisiteSet();
 		this.prerequisites.addAll(prerequisites);
 	}
 	public void setCorequisites(List<Course> corequisites) {
		this.corequisites = new RequisiteSet();
 		this.corequisites.addAll(corequisites);
 	}
 	public void setAvailableTerms(YearPart[] availableTerms) {
 		this.availableTerms = availableTerms;
 	}
 }
