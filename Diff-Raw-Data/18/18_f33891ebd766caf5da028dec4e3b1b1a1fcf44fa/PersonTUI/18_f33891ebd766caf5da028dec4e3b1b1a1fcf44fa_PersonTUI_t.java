 package de.htwg.seapal.person.views.tui;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Scanner;
 
 import de.htwg.seapal.person.controllers.IPersonController;
 import de.htwg.seapal.person.util.observer.Event;
 import de.htwg.seapal.person.util.observer.IObserver;
 
 
 public class PersonTUI implements IObserver {
 	
 	private IPersonController controller;
 
 	public PersonTUI(IPersonController controller) {
 		this.controller = controller;
 		controller.addObserver(this);
 	}
 
 	public void update(Event e) {
 		printTUI();
 	}
 
 	public boolean processInputLine(String line) {
 		
 		boolean continu = true;
 		
		Scanner scanner = new Scanner(System.in);
 		
 		if (line.equalsIgnoreCase("q")) {
 			continu = false;
 		}
 		
 		if (line.equalsIgnoreCase("f")) {
 			System.out.print("Firstname: ");
 			controller.setPersonFirstname(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("l")) {
 			System.out.print("Lastname: ");
 			controller.setPersonLastname(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("b")) {
 			System.out.print("Birth (dd.mm.yyyy): ");
 			
 			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 			
 			Date date = null;
 			
 			try {
 				date = dateFormat.parse(scanner.next());
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			
 			controller.setPersonBirth(date);
 		}
 		
 		if (line.equalsIgnoreCase("r")) {
 			System.out.print("Registration (dd.mm.yyyy): ");
 			
 			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 			
 			Date date = null;
 			
 			try {
 				date = dateFormat.parse(scanner.next());
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			
 			controller.setPersonRegistration(date);
 		}
 		
 		if (line.equalsIgnoreCase("a")) {
 			System.out.print("Age: ");
 			Integer age = new Integer(scanner.next());
 			controller.setPersonAge(age.intValue());
 		}
 		
 		if (line.equalsIgnoreCase("n")) {
 			System.out.print("Nationality: ");
 			controller.setPersonNationality(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("e")) {
 			System.out.print("Email: ");
 			controller.setPersonEmail(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("t")) {
 			System.out.print("Telephone: ");
 			controller.setPersonTelephone(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("m")) {
 			System.out.print("Mobile: ");
 			controller.setPersonMobile(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("s")) {
 			System.out.print("Street: ");
 			controller.setPersonStreet(scanner.nextLine());
 		}
 		
 		if (line.equalsIgnoreCase("p")) {
 			System.out.print("Postcode: ");
 			Integer postcode = new Integer(scanner.next());
 			controller.setPersonPostcode(postcode.intValue());
 		}
 		
 		if (line.equalsIgnoreCase("c")) {
 			System.out.print("City: ");
 			controller.setPersonCity(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("o")) {
 			System.out.print("Country: ");
 			controller.setPersonCountry(scanner.next());
 		}
 		
 		return continu;
 	}
 	
 	public void printTUI() {
 		
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("PersonDemo: ").append("\n");
 		sb.append("f - set Firstname").append("\n");
 		sb.append("l - set Lastname").append("\n");
 		sb.append("b - set Birth").append("\n");
 		sb.append("r - set Registration").append("\n");
 		sb.append("a - set Age").append("\n");
 		sb.append("n - set Nationality").append("\n");
 		sb.append("e - set Email").append("\n");
 		sb.append("t - set Telephone").append("\n");
 		sb.append("m - set Mobile").append("\n");
 		sb.append("s - set Street").append("\n");
 		sb.append("p - set Postcode").append("\n");
 		sb.append("c - set City").append("\n");
 		sb.append("o - set Country").append("\n");
 		sb.append("\n");
 		sb.append(controller.getPersonString());
 		sb.append("\n");
 		sb.append("Command: ");
 		
 		System.out.print(sb.toString());
 	}
 
 }
