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
 
 import java.awt.Component;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.IOException;
 
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.TransferHandler;
 
 import rpiplanner.POSController;
 import rpiplanner.model.Course;
 
 public class CourseTransferHandler extends TransferHandler {
 	private POSController controller;
 
 	public CourseTransferHandler(POSController controller) {
 		this.controller = controller;
 	}
 
 	@Override
 	protected Transferable createTransferable(JComponent c) {
 		if(c instanceof JList){ // dragging from course list
 			Course toExport = (Course)((JList)c).getSelectedValue();
 			return new CourseTransfer(toExport); 
 		}
 		else if(c instanceof CourseDisplay){ // dragging from plan or validation
 			CourseDisplay cd = (CourseDisplay)c;
 			Course toExport = cd.getCourse();
 			return new CourseTransfer(toExport, cd); 
 		}
 		return super.createTransferable(c);
 	}
 	
 	@Override
 	public int getSourceActions(JComponent c) {
 		return MOVE;
 	}
 
 	@Override
 	public boolean importData(JComponent dropTarget, Transferable t) {
 		try {
 			if(dropTarget instanceof JList){ // dropping on catalog, so just remove
 				CourseDisplay home = (CourseDisplay) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.view.CourseDisplay"));
 				if(home != null)
 					controller.removeCourse(home.getParent(), home);
 				return true;
 			}
 			
 			Course toAdd = (Course) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.model.Course"));
 			if(dropTarget instanceof CourseDisplay)
 				dropTarget = (JComponent) dropTarget.getParent();
 			Component[] panels = dropTarget.getParent().getComponents();
 			int index = 0;
 			while(dropTarget != panels[index])
 				index++;
 			CourseDisplay home = (CourseDisplay) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.view.CourseDisplay"));
 			if(home != null && !(home instanceof CourseValidationStatus))
 				controller.removeCourse(home.getParent(), home);
 			controller.addCourse(index, toAdd);
 			return true;
 		} catch (UnsupportedFlavorException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	@Override
 	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
 		for(DataFlavor f : transferFlavors){
			if(f.getRepresentationClass() == Course.class && !(comp instanceof CourseValidationStatus))
 				return true;
 		}
 		return false;
 	}
 	
 	public class CourseTransfer implements Transferable {
 		private Course payload;
 		private CourseDisplay payloadSource;
 		
 		public CourseTransfer(Course toExport) {
 			payload = toExport;
 		}
 
 		public CourseTransfer(Course toExport, CourseDisplay cd) {
 			payload = toExport;
 			payloadSource = cd;
 		}
 
 		public Object getTransferData(DataFlavor flavor)
 				throws UnsupportedFlavorException, IOException {
 			if(flavor.getRepresentationClass() == Course.class){
 				return payload;
 			} else if(flavor.getRepresentationClass() == CourseDisplay.class){
 				return payloadSource;
 			} else{
 				throw new UnsupportedFlavorException(flavor);
 			}
 		}
 
 		public DataFlavor[] getTransferDataFlavors() {
 			try{
 				if(payloadSource != null){
 					DataFlavor[] flavors = {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.model.Course"),
 							new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.view.CourseDisplay")};
 					return flavors;
 				}
 				else{
 					DataFlavor[] flavors = {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.model.Course")};
 					return flavors;
 				}
 			} catch (ClassNotFoundException e){
 				// TODO Auto-generated exception handler
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		public boolean isDataFlavorSupported(DataFlavor flavor) {
 			return flavor.getRepresentationClass() == Course.class;
 		}
 	}
 }
