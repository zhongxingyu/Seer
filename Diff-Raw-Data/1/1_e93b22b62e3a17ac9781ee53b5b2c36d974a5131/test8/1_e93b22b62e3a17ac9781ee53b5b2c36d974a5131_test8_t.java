 //package examples;
 
 import java.util.*;
 
 public class VCollection<A, B> with [VCollection A.myfield] ownedby B {
 	public List<A> col;
 	VCollection(List<A> col) {
 		this.col = col;
 	}
 
 	public void add(A a) {
 		this.col.add(a);
 
 		B b = ownerof this;
 		a.myfield.col.add(b);  //this is not infinite loop
 	}
 }
 
 public class People {
 	public String name;
 	public People(String name) {
 		this.name = name;
 	}
 	public String toString() {return name;}
 	public final VCollection<Class, People> classes = new VCollection<Class, People> with [Class.students] (new ArrayList<Class>());  //~ inverse ac;
 }
 
 public class Class {
 	public String classname;
 	public Class(String classname) {
 		this.classname = classname;
 	}
 	public String toString() {return classname;}
 
 	public final VCollection<People, Class> students = new VCollection<People, Class> with [People.classes] (new ArrayList<People>());	//~ inverse bc
 }
 
 public class Test {
 	public static void main(String args[]) {
 		People jim = new People("jim");
 		People tom = new People("tom");
 		People sam = new People("sam");
 		List<People> allstudents = new ArrayList<People>();
 		allstudents.add(jim);
 		allstudents.add(tom);
 		allstudents.add(sam);
 
 		Class english = new Class("ENG");
 		Class chinese = new Class("CHN");
 		Class japanese = new Class("JPN");
 		List<Class> allclasses = new ArrayList<Class>();
 		allclasses.add(english);
 		allclasses.add(chinese);
 		allclasses.add(japanese);
 
 		//adding relations
 		jim.classes.add(english);
 		jim.classes.add(japanese);
 
 		tom.classes.add(japanese);
 
 		sam.classes.add(japanese);
 
 		chinese.students.add(jim);
 		chinese.students.add(tom);
 
 		System.out.println("test 1: traversal through students, then classes");
 		for (People s : allstudents) {
 			System.out.println(s + ":");
 			for (Class c : s.classes.col) {
 				System.out.println("\t" + c);
 			}
 		}
 
 		System.out.println("\ntest 2: traversal through classes, then students");
 		for (Class c : allclasses) {
 			System.out.println(c + ":");
 			for (People s: c.students.col) {
 				System.out.println("\t" + s);
 			}
 		}
 	}
 }
