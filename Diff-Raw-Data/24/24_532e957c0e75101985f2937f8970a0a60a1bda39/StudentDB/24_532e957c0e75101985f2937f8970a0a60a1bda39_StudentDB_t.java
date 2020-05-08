 import java.io.Serializable;
 import java.util.ArrayList;
 
 
 public class StudentDB implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	ArrayList<Students> students;
 	private int waitlist = 0;
 	private ClassFactory clsFac;
 	public StudentDB (ClassFactory cf){
 		clsFac = cf;
 		students = new ArrayList<Students>();
 	}
 	
 	public void addStudent (Students s) {
 		s.setWLPosition(waitlist);
 		waitlist ++;
 		students.add(s);
 		//new NewFrame(frame,this);
 		
 	}
 	
 	public Students getStudent (int id) {
 		for (int i = 0; i < students.size(); i ++) {
 			Students std = students.get(i);
 			if (std.getId() == id) {
 				return std;
 			}
 		}
 		return null;
 	}
 	
 	public boolean hasStudent (int id) {
 		for (int i = 0; i < students.size(); i ++) {
 			Students std = students.get(i);
 			if (std.getId() == id) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void modifyStudent (int id, Students s) {
 		for (int i = 0; i < students.size(); i ++) {
 			Students std = students.get(i);
 			if (std.getId() == id) {
 				students.remove(i);
 				students.add(i, s);
 			}
 		}
 	}
 	
 	public void removeStudent (int id) {
 		for (int i = 0; i < students.size(); i ++) {
 			Students std = students.get(i);
 			if (std.getId() == id) {
 				students.remove(i);
 				if (clsFac != null) 
 					clsFac.kickout(std);
 			}
 			
 		}
 	}
 
 	public int getSize() {
 		return students.size();
 	}
 	
 	public ArrayList<Students> getStudents() {
 		return students;
 	}
 	
 	public void removeAll() {
 		students.removeAll(students);
 	}
 	
 	public void reInit() {
 		for (int i = 0; i < students.size(); i ++) {
 			if(students.get(i).toString().contains("null")) {
 				students.remove(i);
 				i --;
 			}
 		}
 	}
	
 }
