 package de.htwg.seapal.person.views.tui;
 
 import java.io.PrintStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import com.google.inject.Inject;
 
 import de.htwg.seapal.common.plugin.Plugin;
 import de.htwg.seapal.person.controllers.IPersonController;
 import de.htwg.seapal.person.util.observer.Event;
 import de.htwg.seapal.person.util.observer.IObserver;
 
 
 public class PersonTUI implements IObserver, Plugin {
 	
 	private IPersonController controller;
 	private Map<Character, TuiCommand> commands;
 	private String personId = null;
 	private PrintStream consoleUi = System.out;
 	
 	@Inject
 	public PersonTUI(IPersonController controller) {
 		this.controller = controller;
 		controller.addObserver(this);
 		addImplementedCommands();
 	}
 
 	private void addImplementedCommands() {
 		commands = new HashMap<Character, TuiCommand>();
 		commands.put('q', new QuitCommand());
 		commands.put('x', new AddPersonCommand());
 		commands.put('f', new SetPersonFirstNameCommand());
 		commands.put('l', new SetPersonLastNameCommand());
 		commands.put('b', new SetPersonBirthCommand());
 		commands.put('a', new SetPersonAgeCommand());
 		commands.put('r', new SetPersonRegistrationCommand());
 		commands.put('n', new SetPersonNationalityCommand());
 		commands.put('e', new SetPersonEmailCommand());
 		commands.put('t', new SetPersonTelephoneCommand());
 		commands.put('m', new SetPersonMobileCommand());
 		commands.put('s', new SetPersonStreetCommand());
 		commands.put('p', new SetPersonPostcodeCommand());
 		commands.put('c', new SetPersonCityCommand());
 		commands.put('o', new SetPersonCountryCommand());
 		commands.put('i', new SavePersonIdCommand());
 		commands.put('d', new PrintPersonCommand());
 		commands.put('h', new PrintCommandsCommand());
 	}
 	
 	@Override
 	public void update(Event e) {
 		consoleUi.printf("Current person: %n");
 		(new PrintPersonCommand()).execute(null);
 	}
 	
 	@Override
 	public boolean processInputLine(String line) {
 		
 		boolean continu = true;
 		Scanner scanner = new Scanner(line);
 
 		scanner.useDelimiter(" ");
 		
 		try {
 			scanner.next();
 			char command = line.charAt(0);
 			TuiCommand tcmd = commands.get(command);
 			if (tcmd == null) {
 				throw new IllegalArgumentException("Unknown Command! Pleas try again ...");
 			} else {
 				continu = tcmd.execute(scanner);
 			}
 		} catch (Exception ex) {
			consoleUi.print(ex.getMessage());
 		} finally {
 			scanner.close();
 			scanner = null;
 		}
 		return continu;
 	}
 	
 	@Override
 	public void printTUI() {
 		consoleUi.printf("%nCommand (h for help): ");
 	}
 
 	@Override
 	public String getMenuEntry() {
 		return "Person";
 	}
 
 	@Override
 	public char getMenuKey() {
 		return 'p';
 	}
 	
 	private interface TuiCommand {
 		boolean execute(Scanner arguments);
 
 		String getDescription();
 	}
 	
 	private class PrintCommandsCommand implements TuiCommand {
 
 		@Override
 		public boolean execute(Scanner arguments) {
 			for (Entry<Character, TuiCommand> command : commands.entrySet()) {
 				consoleUi.printf(command.getKey() + " - " + command.getValue().getDescription() +  "%n");
 			}
 			consoleUi.printf("Exmaple:%n> x%n> i PERSON-0%n> f Max%n> l Mustermann%n> d%n");
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "print commands";
 		}
 	}
 	
 	private class QuitCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			return false;
 		}
 
 		@Override
 		public String getDescription() {
 			return "quit program";
 		}
 	}
 	
 	private class SavePersonIdCommand implements TuiCommand {
 
 		@Override
 		public boolean execute(Scanner arguments) {
 			personId = arguments.next();
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "save person id";
 		}
 	}
 	
 	private class SetPersonFirstNameCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonFirstname(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set first name";
 		}
 	}
 	
 	private class PrintPersonCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			consoleUi.printf(controller.getPersonString(personId) + "%n");
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "print person";
 		}
 	}
 	
 	private class SetPersonLastNameCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonLastname(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set last name";
 		}
 	}
 	
 	private class SetPersonBirthCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonBirth(personId, parseDate(arguments.next()));
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set birth date";
 		}
 	}
 	
 	private class SetPersonAgeCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonAge(personId, arguments.nextInt());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set age";
 		}
 	}
 	
 	private class SetPersonRegistrationCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonRegistration(personId, parseDate(arguments.next()));
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set registration";
 		}
 	}
 	
 	private class SetPersonNationalityCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonNationality(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set nationality";
 		}
 	}
 	
 	private class SetPersonEmailCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonEmail(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set email";
 		}
 	}
 	
 	private class SetPersonTelephoneCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonTelephone(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set telephone";
 		}
 	}
 	
 	private class SetPersonMobileCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonMobile(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set mobile";
 		}
 	}
 	
 	private class SetPersonStreetCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonStreet(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set street";
 		}
 	}
 	
 	private class SetPersonPostcodeCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonPostcode(personId, arguments.nextInt());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set postcode";
 		}
 	}
 	
 	private class SetPersonCityCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonCity(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set city";
 		}
 	}
 	
 	private class SetPersonCountryCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			controller.setPersonCountry(personId, arguments.next());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set country";
 		}
 	}
 	
 	private class AddPersonCommand implements TuiCommand {
 		@Override
 		public boolean execute(Scanner arguments) {
 			consoleUi.printf("New person id: %s %n", controller.addPerson());
 			return true;
 		}
 
 		@Override
 		public String getDescription() {
 			return "add person";
 		}
 	}
 	
 	private Date parseDate(String input) {
 		
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 		
 		Date date = null;
 		
 		try {
 			date = dateFormat.parse(input);
 		} catch (ParseException e) {
 			date = new Date();
 		}
 		
 		return date;
 	}
 
 }
