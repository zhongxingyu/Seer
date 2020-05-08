 package com.mirantis.aminakov.student;
 
 import java.text.*;
 import java.util.*;
 
 
 public class Storage {
 	
 	public static void addStudent(List<Student> students, Scanner sc) {
 		Student student = new Student();
 		student.setId();
 		System.out.println("Surname");
 		String sn = sc.next();
 		student.setSurname(sn);
 		System.out.println("Name");
 		String n = sc.next();
 		student.setName(n);
 		System.out.println("Patronymic");
 		String p = sc.next();
 		student.setPatronymic(p);
 		System.out.println("Enter date of birth (12 December 1990)");
 		int attempts = 0;
 		sc.nextLine();
 		while(true) {
 			try {
 				String date = sc.nextLine();
 				student.setDate(date);
 				break;
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				System.err.println("Error " + e);
 				attempts++;
 				if(attempts == 5) {
 					System.out.println("Exit ...");
 					sc.close();
 					break;	
 				}
 				System.out.println("Try again");
 				
 			}
 		}
 		System.out.println("Enter the address");
 		student.setAddress(sc.next());
 		System.out.println("Enter phone");
 		student.setPhone(sc.next());
 		System.out.println("Enter faculty");
 		student.setFaculty(sc.next());
 		System.out.println("Enter course (1-6)");
 		student.setCourse(sc.nextInt());
 		System.out.println("Enter group");
 		student.setGroup(sc.next());
 		students.add(student);
 		System.out.println(student.toString());
 
 	}
 	public static void deleteStudent(List<Student> students, Scanner sc) {
 		Student student;
 		if (students.size() > 0) {
 			System.out.println("Students.size = " + students.size());
 			//for(Student s: students) {
 			//	System.out.println("Index of = " + students.indexOf(s));
 			//}
 			System.out.println("Enter ID");
 			int id = sc.nextInt();
 			//if (id >= 0 && id <= (students.size() - 1)) {
 					for(int i = 0; i < (students.size()); i++) {
 						student = students.get(i);
 						if(student.getId() == id){
 							student.toString();
 							students.remove(student);
 						} else {
 							System.out.println("Wrong ID!!!");
 						}
 					}
 				
 		} else {
 			System.out.println("List is empty!!!");
 		}
 		
 	}
 	public static void findStudent(List<Student> students, Scanner sc) {
 		if (students.size() > 0) {
 			all: while (true) {
 				System.out.println("Find by..." + "\n" + "ID. Press 1." + "\n"
 						+ "surname. Press 2." + "\n" + "name. Press 3." + "\n"
 						+ "patronymic. Press 4." + "\n" + "date. Press 5."
 						+ "\n" + "address. Press 6." + "\n" + "phone. Press 7."
 						+ "\n" + "faculty. Press 8." + "\n"
 						+ "course. Press 9." + "\n" + "group. Press 10." 
 						+ "\n" + "EXIT. Press 11.");
 				int answ = sc.nextInt();
 				Student student;
 				switch (answ) {
 					case 1:
 					System.out.println("Enter ID.");
 					int id = sc.nextInt();
 					if(id > 0 && id < (students.size() - 1)) {
 						student = students.get(id);
 						student.toString();
 					} else {
 						System.out.println("Wrong ID!!!");
 					}
 					break;
 				case 2:
 					System.out.println("Enter surname.");
 					String surname = sc.next();
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						if (student.getSurname().equalsIgnoreCase(surname))
 							System.out.println(student.toString());
 					}
 					break;
 				case 3:
 					System.out.println("Enter name.");
 					String name = sc.next();
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						if (student.getName().equalsIgnoreCase(name))
 							System.out.println(student.toString());
 					}
 					break;
 				case 4:
 					System.out.println("Enter patronymic.");
 					String patronymic = sc.next();
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						if (student.getPatronymic().equalsIgnoreCase(patronymic))
 							System.out.println(student.toString());
 					}
 					break;
 				case 5:
 					System.out.println("Enter date, (12 December 1990)");
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						sc.nextLine();
 						String find_date = sc.nextLine();
 						SimpleDateFormat sdf = new SimpleDateFormat(
 								"dd MMMM yy", Locale.ROOT);
 						Date d = null;
 						try {
 							d = sdf.parse(find_date);
 						} catch (ParseException e) {
 							// TODO Auto-generated catch block
 							//e.printStackTrace();
 							System.err.println("Error " + e);
 							System.out.println("Error");
 							break;
 						}
 						String string_date = d.toString();
 						if (student.getDate().toString().equals(string_date))
 							System.out.println(student.toString());
 					}
 					break;
 				case 6:
 					System.out.println("Enter address.");
 					String address = sc.next();
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						if (student.getAddress().equalsIgnoreCase(address))
 							System.out.println(student.toString());
 					}
 					break;
 				case 7:
 					System.out.println("Enter phone.");
 					String phone = sc.next();
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						if (student.getPhone().equalsIgnoreCase(phone))
 							System.out.println(student.toString());
 					}
 					break;
 				case 8:
 					System.out.println("Enter faculty.");
 					String faculty = sc.next();
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						if (student.getFaculty().equalsIgnoreCase(faculty))
 							System.out.println(student.toString());
 					}
 					break;
 				case 9:
 					System.out.println("Enter course.");
 					int course = sc.nextInt();
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						if (student.getCourse() == course)
 							System.out.println(student.toString());
 					}
 					break;
 				case 10:
 					System.out.println("Enter group.");
 					String group = sc.next();
					for (int i = 0; i < students.size(); i++) {
 						student = students.get(i);
 						if (student.getGroup().equalsIgnoreCase(group))
 							System.out.println(student.toString());
 					}
 					break;
 				case 11: 
 					break all;
 				default:
 					break all;
 				}
 			}
 		} else { 
 			System.out.println("List is empty!!!");
 		}
 	}
 	public static void printList(List<Student> students) {
 		for(Student s: students) {
 			System.out.println(s.toString());
 		}
 	}
 
 }
