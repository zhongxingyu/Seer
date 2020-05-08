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
 
 package rpiplanner.view;
 
 import javax.swing.AbstractListModel;
 import javax.swing.ComboBoxModel;
 
 import rpiplanner.model.CourseDatabase;
 import rpiplanner.model.PlanOfStudy;
 
 public class DegreeListModel extends AbstractListModel implements ComboBoxModel {
 	private CourseDatabase database;
 	private PlanOfStudy plan;
 	private Object selectedItem;
 	
 	public DegreeListModel(CourseDatabase database){
 		this.database = database;
 	}
 
 	public DegreeListModel(PlanOfStudy plan) {
 		this.plan = plan;
 	}
 
 	public Object getElementAt(int index) {
 		if(database != null)
 			return database.listDegrees()[index];
 		else
 			return plan.getDegrees().get(index);
 	}
 
 	public int getSize() {
 		if(database != null)
 			return database.listDegrees().length;
 		else
 			return plan.getDegrees().size();
 	}
 	
 	public void degreeAdded(){
 		fireIntervalAdded(this, getSize()-1, getSize()-1);
 	}
 	
 	public void degreeRemoved(){
 		fireContentsChanged(this, 0, getSize());
 	}
 
 	public void newPlan(PlanOfStudy plan2) {
 		this.plan = plan2;
 		fireContentsChanged(this, 0, getSize());
 	}
 
 	public Object getSelectedItem() {
 		return selectedItem;
 	}
 
 	public void setSelectedItem(Object anItem) {
 		selectedItem = anItem;
 	}
 }
