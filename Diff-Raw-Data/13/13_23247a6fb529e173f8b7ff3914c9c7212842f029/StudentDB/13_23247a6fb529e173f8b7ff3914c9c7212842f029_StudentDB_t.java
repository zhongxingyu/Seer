 import java.util.ArrayList;
 
 
 public class StudentDB {
 	ArrayList<Students> students;
 	public StudentDB (){
 		students = new ArrayList<Students>();
 	}
 	
 	public void addStudent (Students s) {
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
 }
