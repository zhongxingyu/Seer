 package net.sprauer.sitzplaner.EA.operations;
 
 import java.awt.Point;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Vector;
 
 import net.sprauer.sitzplaner.EA.Chromosome;
 import net.sprauer.sitzplaner.EA.EAOperation;
 import net.sprauer.sitzplaner.model.DataBase;
 import net.sprauer.sitzplaner.view.ClassRoom;
 import net.sprauer.sitzplaner.view.helper.Parameter;
 
 public class OperationGreedy extends EAOperation {
 
 	private final Vector<Integer> studentsByPriority = new Vector<Integer>();
 	private final Vector<Point> lockedPositions = new Vector<Point>();
 
 	@Override
 	public void invoke(Chromosome gene) throws Exception {
 
 		fillAndSortStudents(gene);
 
 		int x = 0;
 		int y = Parameter.numRows - 1;
 		while (!studentsByPriority.isEmpty()) {
 			int student = getNextStudentByPriority();
 			final Point newPos = new Point(x, y);
 			if (!positionIsAlreadyUsed(newPos)) {
 				gene.setPositionOf(student, newPos);
			} else {
				studentsByPriority.insertElementAt(student, 0);
 			}
 			x += 1;
 			if (x >= ClassRoom.instance().getDimensions().getWidth()) {
 				x = 0;
 				y -= 1;
 			}
 		}
 	}
 
 	private boolean positionIsAlreadyUsed(Point newPos) {
 		return lockedPositions.contains(newPos);
 	}
 
 	private void fillAndSortStudents(Chromosome gene) {
 		studentsByPriority.clear();
 		for (int i = 0; i < DataBase.instance().getSize(); i++) {
 			if (DataBase.instance().getStudent(i).isLocked()) {
 				gene.setPositionOf(i, DataBase.instance().getStudent(i).getLockPosition());
 				lockedPositions.add(DataBase.instance().getStudent(i).getLockPosition());
 			} else {
 				studentsByPriority.add(i);
 			}
 		}
 		Collections.sort(studentsByPriority, new Comparator<Integer>() {
 			@Override
 			public int compare(Integer o1, Integer o2) {
 				return DataBase.instance().getPriority(o2) - DataBase.instance().getPriority(o1);
 			}
 		});
 	}
 
 	private int getNextStudentByPriority() {
 		return studentsByPriority.remove(0);
 	}
 }
