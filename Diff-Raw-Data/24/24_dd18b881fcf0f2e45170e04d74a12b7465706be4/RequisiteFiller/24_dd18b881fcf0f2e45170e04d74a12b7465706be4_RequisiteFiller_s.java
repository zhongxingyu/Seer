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
 
 package rpiplanner;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import rpiplanner.model.Course;
 import rpiplanner.model.CourseComparator;
 import rpiplanner.model.DummyPOSComparator;
 import rpiplanner.model.Pair;
 import rpiplanner.model.RequisiteSet;
 import rpiplanner.model.Term;
 
 public class RequisiteFiller {
 	private POSController controller;
 	private Course course;
 	
 	public RequisiteFiller(POSController controller, Course course) {
 		this.controller = controller;
 		this.course = course;
 		
 		ArrayList<Pair<Course, Integer>> dummyPOS = new ArrayList<Pair<Course, Integer>>();
 		fillRequisites(course, controller.getTerm(course), dummyPOS);
 		fillCourses(dummyPOS);
 	}
 
 	private void fillCourses(ArrayList<Pair<Course, Integer>> dummyPOS) {
 		Collections.sort(dummyPOS, new DummyPOSComparator());
 		
 		for (int i = 0; i < dummyPOS.size(); i++) {
 			int term = dummyPOS.get(i).getSecond();
 			boolean adjusted = false;
 			boolean validToPushBack = true;
 			while (validToPushBack) {
				validToPushBack = controller.wouldCourseBeValid(dummyPOS.get(i).getFirst(), term, dummyPOS) && term > 0;
				if (!validToPushBack) {
					if ((dummyPOS.get(i).getFirst().getAvailableTerms().length < 2) && (dummyPOS.get(i).getFirst().getAvailableTerms()[0] != controller.getPlan().getTerm(term).getTerm())) {
						validToPushBack = controller.wouldCourseBeValid(dummyPOS.get(i).getFirst(), term - 1, dummyPOS) && term > 0;
					}
				}
				
 				if (validToPushBack) {
 					term--;
					adjusted = true;
 				}
 			}
 			
 			if (adjusted) {
 				term++;
 			}
 			
 			dummyPOS.get(i).setSecond(term);
 		}
 		
 		for (int i = 0; i < dummyPOS.size(); i++) {
 			controller.addCourse(dummyPOS.get(i).getSecond(), dummyPOS.get(i).getFirst());
 		}
 	}
 	
 	private void fillRequisites(Course fillCourse, int term, ArrayList<Pair<Course, Integer>> dummyPOS) {
 		fillCourse = Course.get(fillCourse.getCatalogNumber());	
 		if (fillCourse != null) {
 			RequisiteSet reqs = fillCourse.getPrerequisites();
 			Collections.sort(reqs, new CourseComparator());
 			
 			for (int i = 0; i < reqs.size(); i++) {
 				if ((fillCourse.getAvailableTerms().length < 2) && (fillCourse.getAvailableTerms()[0] != controller.getPlan().getTerm(term).getTerm())) {
 					term--;
 				}
 				
 				if (term - 1 >= 0) {
 					fillRequisites(reqs.get(i), term - 1, dummyPOS);
 				}
 				
 				else {
 					return;
 				}
 			}
 			
 			boolean dupeCourse = false;
 			List<Term> dupes = controller.getPlan().getTerms();
 			ArrayList<Course> dupeCourses = new ArrayList<Course>();
 			for (int i = 0; i < dupes.size(); i++) {
 				ArrayList<Course> tmp = dupes.get(i).getCourses();
 				for (int k = 0; k < tmp.size(); k++) {
 					dupeCourses.add(tmp.get(k));
 				}
 			}
 			
 			for (int i = 0; i < dummyPOS.size(); i++) {
 				dupeCourses.add(dummyPOS.get(i).getFirst());
 			}
 			
 			for (int dc = 0; dc < dupeCourses.size(); dc++) {
 				if (dupeCourses.get(dc).equals(fillCourse) || fillCourse.getCatalogNumber().equals("MATH-1500")) {
 					dupeCourse = true;
 					break;
 				}
 			}
 			
 			if (!dupeCourse) {
 				dummyPOS.add(new Pair<Course, Integer>(fillCourse, term));
 			}
 			
 			RequisiteSet coreqs = fillCourse.getCorequisites();
 			Collections.sort(coreqs, new CourseComparator());
 			for (int k = 0; k < coreqs.size(); k++) {
 				fillRequisites(coreqs.get(k), term, dummyPOS);
 			}
 			
 			return;
 		}
 	}
 }
