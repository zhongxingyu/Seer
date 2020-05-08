 package com.veltro.stattendance.database;
 
 import java.util.HashMap;
 
 /**
  * The ClassMaster houses the directories of {@link #classes}, {@link #teachers}, and {@link #students} and provides
  * utility methods for integrating students and teachers with classes.
  * 
  * @author LinearLogic
  * @since 0.2.5
  */
 public class ClassMaster {
 
 	/**
 	 * A directory of {@link STAClass STA classes} indexed by {@link STAClass#name}
 	 */
 	private HashMap<String, STAClass> classes;
 
 	/**
 	 * A directory of {@link Teacher upper school teachers} indexed by {@link Teacher#id} values
 	 */
 	private HashMap<Integer, Teacher> teachers;
 
 	/**
 	 * A directory of{@link Student upper school students} indexed by {@link Student#id} values
 	 */
 	private HashMap<Integer, Student> students;
 
 	/**
 	 * Constructor - initializes the directories of {@link #classes}, {@link #teachers}, and {@link #students}
 	 */
 	public ClassMaster() {
 		classes = new HashMap<String, STAClass>();
 		teachers = new HashMap<Integer, Teacher>();
 		students = new HashMap<Integer, Student>();
 	}
 
 	/**
 	 * Adds the provided {@link STAClass} to the provided {@link Teacher} and sets the class's teacherID to that of the
 	 * provided teacher. The class is also removed from its old teacher's class list.
 	 * 
 	 * @param c One of the classes contained in the {@link #classes class directory}
 	 * @param t One of the teachers contained in the {@link #teachers teacher directory}
 	 * @return 'true' iff the teacher and class objects were successfully updated
 	 */
 	public boolean addClassToTeacher(STAClass c, Teacher t) {
 		try {
 			teachers.get(c.getTeacherID()).removeClass(c.getName());
 		} catch (NullPointerException e) { }
 		c.setTeacherID(t.getID());
 		return t.addClass(c);
 	}
 
 	/**
 	 * Removes the provided {@link STAClass} from the provided {@link Teacher} and sets the class's teacherID to -1,
 	 * signifying no teacher.
 	 * 
 	 * @param c One of the classes contained in the {@link #classes class directory}
 	 * @param t One of the teachers contained in the {@link #teachers teacher directory}
 	 * @return 'true' iff the teacher and class objects were successfully updated
 	 */
 	public boolean removeClassFromTeacher(STAClass c, Teacher t) {
 		boolean removed = t.removeClass(c);
 		if (!removed)
 			return false;
 		c.setTeacherID(-1);
 		return true;
 	}
 
 	/**
 	 * Adds the provided {@link Student} to the provided {@link STAClass} and vice versa
 	 * 
 	 * @param s One of the students contained in the {@link #students student directory}
 	 * @param c One of the classes contained in the {@link #classes class directory}
 	 * @return 'true' iff the student and class objects were successfully updated
 	 */
 	public boolean addStudentToClass(Student s, STAClass c) {
 		c.addStudent(s);
 		return s.addClass(c);
 	}
 
 
 	/**
 	 * Removes the provided {@link Student} from the provided {@link STAClass} and vice versa
 	 * 
 	 * @param s One of the students contained in the {@link #students student directory}
 	 * @param c One of the classes contained in the {@link #classes class directory}
 	 * @return 'true' iff the student and class objects were successfully updated
 	 */
 	public boolean removeStudentFromClass(Student s, STAClass c) {
 		c.removeStudent(s);
 		return s.removeClass(c);
 	}
 
 	public STAClass[] getClasses() {
 		STAClass[] output = new STAClass[classes.size()];
 		int index = 0;
 		for (STAClass c : classes.values())
 			output[index++] = c;
 		return output;
 	}
 
	public STAClass getClass(String name) {
		return classes.get(name);
	}

	public void addClass(STAClass c) {
 		classes.put(c.getName(), c);
 	}
 
 	/**
 	 * Removes the name of the provided {@link STAClass} from the teacher of the class and the students enrolled in it,
 	 * and then deletes the class's entry in the {@link #classes class directory}
 	 * 
 	 * @param className The name of the class to be removed from the database
 	 */
 	public void removeClass(STAClass c) {
 		removeClass(c.getName());
 	}
 
 	/**
 	 * Removes the provided {@link STAClass class} name from the teacher of the class and the students enrolled in it,
 	 * and then deletes the class's entry in the {@link #classes class directory}
 	 * 
 	 * @param className The name of the class to be removed from the database
 	 */
 	public void removeClass(String className) {
 		STAClass c = classes.get(className);
 		if (c == null)
 			return;
 		try {
 			teachers.remove((Integer) c.getTeacherID());
 		} catch (NullPointerException e) { }
 		for (int id : c.getStudentIDs()) {
 			try {
 				students.get((Integer) id).removeClass(className);
 			} catch (NullPointerException e) {
 				continue;
 			}
 		}
 		classes.remove(className);
 	}
 
 	/**
 	 * Removes every class name from every teacher and student the respective directories, and then clears the
 	 * {@link #classes class directory}
 	 */
 	public void clearClasses() {
 		for (Teacher t : teachers.values())
 			t.clearClasses();
 		for (Student s : students.values())
 			s.clearClasses();
 		classes.clear();
 	}
 
 	public Teacher[] getTeachers() {
 		Teacher[] output = new Teacher[teachers.size()];
 		int index = 0;
 		for (Teacher t : teachers.values())
 			output[index++] = t;
 		return output;
 	}
 
	public Teacher getTeacher(int id) {
		return teachers.get(id);
	}

 	public void addTeacher(Teacher t) {
 		teachers.put(t.getID(), t);
 	}
 
 	/**
 	 * Calls the {@link #removeTeacher(int)} method passing the ID of the provided {@link Teacher} object
 	 * 
 	 * @param t The teacher to be removed from the database
 	 */
 	public void removeTeacher(Teacher t) {
 		removeTeacher(t.getID());
 	}
 
 	/**
 	 * Removes the provided teacher ID from all the classes taught by the faculty member being removed, and then
 	 * deletes the teacher's entry in the {@link #teachers teacher directory}
 	 * 
 	 * @param t The ID number of the teacher to be removed from the database
 	 */
 	public void removeTeacher(int teacherID) {
 		String[] names;
 		try {
 			names = teachers.get((Integer) teacherID).getClasses();
 		} catch (NullPointerException e) {
 			return;
 		}
 		for (String n : names) {
 			try {
 				classes.get(n).setTeacherID(-1);
 			} catch (NullPointerException e) {
 				continue;
 			}
 		}
 		teachers.remove(teacherID);
 	}
 
 	/**
 	 * Sets the teacherID of each {@link STAClass} to -1 and clears the {@link #teachers teacher directory}
 	 */
 	public void clearTeachers() {
 		for (STAClass c : classes.values())
 			c.setTeacherID(-1);
 		teachers.clear();
 	}
 
	public Student[] getStudents() {
 		Student[] output = new Student[students.size()];
 		int index = 0;
 		for (Student s : students.values())
 			output[index++] = s;
 		return output;
 	}
 
	public Student getStudent(int id) {
		return students.get(id);
	}

 	public void addStudent(Student s) {
 		students.put(s.getID(), s);
 	}
 
 	/**
 	 * Calls the {@link #removeStudent(int)} method passing the ID of the provided {@link Student} object
 	 * 
 	 * @param s The student to be deleted from the database
 	 */
 	public void removeStudent(Student s) {
 		removeStudent(s.getID());
 	}
 
 	/**
 	 * Removes the provided student ID from all the classes in which the student is enrolled, and then deletes the
 	 * student's entry in the {@link #students student directory}
 	 * 
 	 * @param s The ID number of the student to be deleted from the database
 	 */
 	public void removeStudent(int studentID) {
 		String[] names;
 		try {
 			names = students.get((Integer) studentID).getClasses();
 		} catch (NullPointerException e) {
 			return;
 		}
 		for (String n : names) {
 			try {
 				classes.get(n).removeStudent(studentID);
 			} catch (NullPointerException e) {
 				continue;
 			}
 		}
 		students.remove(studentID);
 	}
 
 	/**
 	 * Removes all student IDs from all classes and then clears the {@link #students student directory}
 	 */
 	public void clearstudents() {
 		for (STAClass c: classes.values())
 			c.removeAllStudents();
 		students.clear();
 	}
 }
