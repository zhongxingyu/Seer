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
 
 import java.util.Comparator;
 import rpiplanner.model.Course;
 
 public class CourseComparator implements Comparator {
 	public int compare(Object lhs, Object rhs) {
 		String lSub = ((Course) lhs).getCatalogNumber();
 		String rSub = ((Course) rhs).getCatalogNumber();
 		
 		int lIndex = lSub.indexOf('-');
 		int rIndex = rSub.indexOf('-');
 		
 		lSub = lSub.substring(lIndex + 1, lSub.length());
 		rSub = rSub.substring(rIndex + 1, rSub.length());
 		
		if (Integer.parseInt(rSub) < Integer.parseInt(lSub)) {
			return -1;
 		}
 		
		else if (Integer.parseInt(rSub) > Integer.parseInt(lSub)) {
			return 1;
 		}
 		
 		else {
 			return 0;
 		}
 	}
 }
