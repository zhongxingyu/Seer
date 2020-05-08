 /**
  * This file has a main method. From which the application starts. 
  */
 package pl.polsl.flota.view;
 
 //TODO: Dodac komentarze na poczatku klas. 
 //TODO: Zmienic kolejnosc pierwsze opis potem autor.
 //TODO: Tworzenie samochodow nie za pomoca konstruktora tylko budowniczgo - przyklad zakomentiwany.
 //TODO: Usunac zbedne entery przed klamrami.
//FIXME: !!!!!!!!!COŚ NIE TAK Z ZAPISEM PO USUNIĘCIU TYCH STATIC Z LIST W MODELU -AAAAAA! Singleton?
 //Testy jednostkowe dla klas modelu 
 //- testy zbiorcze. 
 //Uwaga: klasy modelu nie mogą zawierać składników statycznych. 
 
 //FIXME: IMPLEMNETACJA r3 do 18/11/2011 ;)
 import java.io.IOException;
 import java.util.Scanner;
 
 import pl.polsl.flota.exceptions.ElementAlredyExists;
 import pl.polsl.flota.exceptions.ElementNotFound;
 import pl.polsl.flota.exceptions.MenuItemNotFound;
 import pl.polsl.flota.helpers.*;
 
 /**
  * This is the main class of the application instance.
  * 
  * @author Marcin Jabrzyk
  */
 public final class MainWindow {
 	AdminView adminView;
 	DriverView driverView;
 	Integer currentUserId;
 	/**
 	 * Default constructor which initializes some values which are global for
 	 * whole Application.
 	 */
 	public MainWindow() {
 		currentUserId = -2; //An non-valid user id.
 
 		adminView = new AdminView();
 		driverView = new DriverView();
 
 	}
 
 	/**
 	 * @param args
 	 *            Runtime args
 	 */
 	public static void main(String[] args) {
 		MainWindow mywindow = new MainWindow();
 
 		if (args.length < 2) {
 			System.out
 					.println("Podaj nazwy pliku z użytkownikami i samochodami");
 			System.out.println("np. flota users.json cars.json ");
 			System.exit(0);
 		}
 		System.out.println("Witaj w programie Flota.");
 		try {
 			mywindow.adminView.setFileNameUserList(args[0]);
 			mywindow.driverView.setFileNameUserList(args[0]);
 			mywindow.adminView.setFileNameCarList(args[1]);
 			mywindow.driverView.setFileNameCarList(args[1]);
 		} catch (IOException e) {
 			System.out.println("Błąd podczas otwierania pliku.");
 			System.exit(0);
 		}
 
 		while (true) {
 			mywindow.currentUserId = loginScreen(mywindow.currentUserId,
 					mywindow);
 			Helpers.clearScren();
 			if (mywindow.currentUserId < 0) {
 				System.out.println("Podaj prawidłowy login i hasło");
 				mywindow.currentUserId = loginScreen(mywindow.currentUserId,
 						mywindow);
 			} else {
 				if (mywindow.adminView.userIsAdmin(mywindow.currentUserId) == true) {
 					mainMenuAdmin(mywindow);
 				} else {
 					mainMenuDriver(mywindow);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Login screen at startup of the app. If currentId is valid then simply
 	 * returns out of the function.
 	 * 
 	 * @param currentId
 	 *            te id of current working user
 	 * @param mywindow
 	 *            refrence to current window
 	 * @return the userId
 	 */
 	private static Integer loginScreen(Integer currentId, MainWindow mywindow) {
 		if (currentId < 0) {
 			Scanner scanner = new Scanner(System.in);
 			System.out.println("Podaj login: ");
 			String login = scanner.nextLine();
 			System.out.println("Podaj hasło: ");
 			String passwd = scanner.nextLine();
 
 			return mywindow.adminView.checkUser(login, passwd);
 
 		}
 		return currentId;
 
 	}
 
 	/**
 	 * This function generates and handles the main menu for application admin.
 	 * 
 	 * @param mywindow
 	 */
 	private static void mainMenuAdmin(MainWindow mywindow) {
 		AdminMenu retVal = null;
 		try {
 		retVal = Helpers.presentAdminMenuAndGetValue();
 		} catch (MenuItemNotFound e) {
 			System.out.println("Podano nieprawidłową wartość. Podaj ponownie.");
 			mainMenuAdmin(mywindow);
 		}
 		switch (retVal) {
 		case AM_DODAJ_POJAZD : {
 			try {
 				mywindow.adminView.addCar();
 			} catch (ElementAlredyExists e) {
 				System.out.println("Taki samochód już istnieje.");
 			}
 			break;
 		}
 		case  AM_PRZEGLADAJ_POJAZDY: {
 			mywindow.adminView.listCars();
 			break;
 		}
 		case AM_EDYTUJ_POJAZD: {
 			try {
 				mywindow.adminView.editOrViewCar();
 			} catch (ElementNotFound e) {
 				System.out
 						.println("Samochód o podanych parametrach nie został znaleiziony.");
 			}
 
 			break;
 		}
 		case AM_USUN_POJAZD: {
 			try {
 				mywindow.adminView.deleteCar();
 			} catch (ElementNotFound e) {
 				System.out
 						.println("Samochód o podanych parametrach nie został znaleiziony.");
 			}
 			break;
 		}
 		case AM_KIEROWCA : {
 			Helpers.clearScren();
 			mainMenuAdminDriver(mywindow);
 			break;
 		}
 		case AM_WYLOGUJ: {
 			mywindow.adminView.save();
 			mywindow.currentUserId = -2;
 			break;
 		}
 		case AM_WYJSCIE : {
 			mywindow.adminView.save();
 			System.exit(0);
 		}
 		}
 	}
 
 	/**
 	 * This function generates and handles Driver section in admin menu
 	 * 
 	 * @param mywindow
 	 */
 	private static void mainMenuAdminDriver(MainWindow mywindow) {
 		AdminDriverMenu retVal = null;
 		try {
 		retVal = Helpers.presentAdminDriverMenuAndGetValue();
 		} catch (MenuItemNotFound e) {
 			System.out.println("Podano nieprawidłową wartość. Podaj ponownie.");
 			mainMenuAdminDriver(mywindow);
 		}
 		switch (retVal) {
 		case AD_DODAJ_KIEROWCE: {
 			try {
 				mywindow.adminView.addUser();
 			} catch (ElementAlredyExists e) {
 				System.out.println("Taki użytkownik już istnieje");
 			}
 			break;
 		}
 		case AD_PRZEGLADAJ_KIEROWCOW: {
 			mywindow.adminView.listUsers();
 			break;
 		}
 		case AD_ZMIEN_HASLO: {
 			try {
 				mywindow.adminView.editUser();
 			} catch (ElementNotFound e) {
 				System.out.println("Podano błędne id użytkownika.");
 			}
 			break;
 		}
 		case AD_USUN_KIEROWCE: {
 			try {
 				mywindow.adminView.deleteUser();
 			} catch (ElementNotFound e) {
 				System.out.println("Podano błędne id użytkownika.");
 			}
 			break;
 		}
 		case AD_COFNIJ_DO_MENU: {
 			Helpers.clearScren();
 			mainMenuAdmin(mywindow);
 			break;
 		}
 		}
 	}
 
 	/**
 	 * This function generates and handles Driver menu
 	 * 
 	 * @param mywindow
 	 */
 	private static void mainMenuDriver(MainWindow mywindow) {
 		//Integer retVal = Helpers.menuToOptionId(mywindow.menuDriverLvl1);
 		DriverMenu retVal = null;
 		try {
 		retVal = Helpers.presentDriverMenuAndGetValue();
 		} catch (MenuItemNotFound e) {
 			System.out.println("Podano nieprawidłową wartość. Podaj ponownie.");
 			mainMenuDriver(mywindow);
 		}
 		switch (retVal) {
 		case D_ZANOTUJ_TANKOWANIE: {
 			try {
 				Helpers.clearScren();
 				mywindow.driverView.userRefuel(mywindow.currentUserId);
 			} catch (NumberFormatException e) {
 				System.out
 						.println("Błąd podczas dodawania tankowania. Sprawdź format danych.");
 			} catch (ElementNotFound e) {
 				System.out.println("Błąd podczas dodawania tankowania.");
 			}
 			break;
 		}
 		case D_ZMIEN_POJAZD: {
 			try {
 				Helpers.clearScren();
 				mywindow.driverView.userChangeCar(mywindow.currentUserId);
 			} catch (ElementNotFound e) {
 				System.out
 						.println("Podano nieprawidłowy numer rejestracyjny pojazdu.");
 			}
 			break;
 		}
 		case D_WYLOGUJ: {
 			mywindow.driverView.save();
 			mywindow.currentUserId = -2;
 			break;
 		}
 		case D_WYJSCIE: {
 			mywindow.driverView.save();
 			System.exit(0);
 		}
 		}
 	}
 }
