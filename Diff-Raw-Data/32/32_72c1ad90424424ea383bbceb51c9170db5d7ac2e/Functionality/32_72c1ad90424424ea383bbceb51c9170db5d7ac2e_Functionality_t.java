 package businessLogic_layer;
 
 import java.util.Random;
 import java.util.Scanner;
 import dao_interfaces.IOperatoerDAO;
 import dao_interfaces.IProduktBatchDAO;
 import dao_interfaces.IRaavareBatchDAO;
 import dao_interfaces.IRaavareDAO;
 import dao_interfaces.IReceptDAO;
 import dao_interfaces.IReceptKompDAO;
 import db_mysqldao.MySQLOperatoerDAO;
 import db_mysqldao.MySQLProduktBatchDAO;
 import db_mysqldao.MySQLRaavareBatchDAO;
 import db_mysqldao.MySQLRaavareDAO;
 import db_mysqldao.MySQLReceptDAO;
 import db_mysqldao.MySQLReceptKompDAO;
 import dao_interfaces.DALException;
 
 public class Functionality implements IFunctionality{
 
 	private IProduktBatchDAO produktBatchDAO = new MySQLProduktBatchDAO();
 	private IOperatoerDAO oprDAO = new MySQLOperatoerDAO();
 	private IRaavareDAO raavareDAO = new MySQLRaavareDAO();
 	private IReceptDAO receptDAO = new MySQLReceptDAO();
 	private IReceptKompDAO receptKompDAO = new MySQLReceptKompDAO();
 	private IRaavareBatchDAO raavareBatchDAO = new MySQLRaavareBatchDAO();
 
 	public Functionality(IProduktBatchDAO produktBatchDAO, IOperatoerDAO oprDAO, IRaavareDAO raavareDAO, IReceptDAO receptDAO, IReceptKompDAO receptKompDAO, IRaavareBatchDAO raavareBatchDAO) {
 		super();
 		this.produktBatchDAO = produktBatchDAO;
 		this.oprDAO = oprDAO;
 		this.raavareDAO = raavareDAO;
 		this.receptDAO = receptDAO;
 		this.raavareBatchDAO = raavareBatchDAO;
 	}
 
 	public Functionality(){
 
 	}
 
 	Scanner scan = new Scanner(System.in);
 
 	/**
 	 * Genererer automatisk et password ud fra de givet kriterier 
 	 */
 	public String generatePassword() {
 		String password = ""; // Indeholder password
 		int numOfLowercase = 0; // Antal af Lowercase char's i passwd
 		int numOfUppercase = 0; // Antal af Upppercase..
 		int numOfNumber = 0;
 		int numOfSymbol = 0;
 		int differentTypes = 0; // Forskellige kategorier/typer -
 		// Lowercase,Uppercase, osv i passwd.
 		int[] arrayOfTypes = { 1, 2, 3, 4 };
 		while (password.length() < 6 || differentTypes < 3) {
 			Random r = new Random();
 			switch (arrayOfTypes[r.nextInt(arrayOfTypes.length)]) {
 			// lower case
 			case 1:
 				password += randomLowercase();
 				numOfLowercase++;
 				if (numOfLowercase <= 1) {
 					differentTypes++;
 				}
 				break;
 			// upper case
 			case 2:
 				password += randomUppercase();
 				numOfUppercase++;
 				if (numOfUppercase <= 1) {
 					differentTypes++;
 				}
 				break;
 			// number
 			case 3:
 				password += randomNumber();
 				numOfNumber++;
 				if (numOfNumber <= 1) {
 					differentTypes++;
 				}
 				break;
 			// symbol
 			case 4:
 				password += randomSymbol();
 				numOfSymbol++;
 				if (numOfSymbol <= 1) {
 					differentTypes++;
 				}
 				break;
 			}
 		}
 		return password;
 	}
 
 	public static char randomLowercase() {
 		Random r = new Random();
 		char c = (char) (r.nextInt(26) + 'a');
 		return c;
 	}
 	public static char randomUppercase() {
 		Random r = new Random();
 		char c = (char) (r.nextInt(26) + 'A');
 		return c;
 	}
 
 	public static char randomNumber() {
 		Random r = new Random();
 		char c = (char) (r.nextInt(9) + '0');
 		return c;
 	}
 
 	public static char randomSymbol() {
 		Random r = new Random();
 		char[] arrayOfSymbols = { '.', '-', '_', '+', '!', '?', '=' };
 		char c = (char) arrayOfSymbols[r.nextInt(arrayOfSymbols.length)];
 		return c;
 	}
 
 	public void askForOprName() {
 		scan.nextLine();
 	}
 
 	/**
 	 * Her sammenlignes operatør id fra brugerinput med eksisterende id'er i vores Array
 	 * Fra index {0;9} indeholder listen objekter med parametrerne null.
 	 * Derfor bruges catch p� NullPointerException, s� man ikke kan tilg� disse objekter.  
 	 */
 	public boolean testId(String inputID) throws DALException {
 		try {
 			int id = Integer.parseInt(inputID);
 			return (id == oprDAO.getOperatoer(id).getOprId());
 		}
 
 		catch (IndexOutOfBoundsException e) {
 			throw new DALException("ID findes ikke");
 		}
 		catch (NullPointerException f) {
 			throw new DALException("ID findes ikke");
 		}
 		catch(NumberFormatException g){
 			return false;
 		}
 		catch (DALException h) {
 			return false;
 		}
 	}
 	/**
 	 * Her sammenlignes pb id fra brugerinput med eksisterende id'er i vores Array på samme måde som ovenover.
 	 */
 	
 	
 	public boolean testPbId(int i) throws DALException {
 		try {
 			return (i == produktBatchDAO.getProduktBatch(i).getPbId());
 		}
 	
 		catch (IndexOutOfBoundsException e) {
 			throw new DALException("ID findes ikke");
 		}
 		catch (NullPointerException f) {
 			throw new DALException("ID findes ikke");
 		}
 		catch (DALException h) {
 			return false;
 		}
 	}
 
 
 	public boolean testRaavareBatchId(int i) throws DALException {
 		try {
 			return (i == raavareBatchDAO.getRaavareBatch(i).getRbId());
 		}
 		catch (IndexOutOfBoundsException e) {
 			throw new DALException("ID findes ikke");
 		}
 		catch (NullPointerException f) {
 			throw new DALException("ID findes ikke");
 		}
 	}
 	/**
 	 * Her sammenlignes password fra brugerinput med eksisterende password i vores Array 
 	 * baseret p� det f�rstindtastede id (int i) som er unikt for operat�r til operat�r  
 	 */
 	public boolean testPassword(int oprID, String password) throws DALException {
 		return (password.equals(oprDAO.getOperatoer(oprID).getPassword()));
 	}
 
 	/**
 	 * Her valideres hvert enkelt element i det indtastede password for at overholde
 	 * de givet kriterier for et password  
 	 */
	public boolean checkPasswordStandards(int oprId, String newPassword) throws DALException {
 		boolean containLowerCase = false, containUpperCase = false, containNumber = false, containSymbol = false;
 		int differentTypes = 0;
 
 		for(int i = 0; i < newPassword.length(); i++) {
 			if (checkLowercase(newPassword.charAt(i)) && !containLowerCase){
 				containLowerCase = true;
 				differentTypes++;
 			}
 			else if (checkUppercase(newPassword.charAt(i)) && !containUpperCase){
 				containUpperCase = true;
 				differentTypes++;
 			}
 			else if (checkNumber(newPassword.charAt(i)) && !containNumber){
 				containNumber = true;
 				differentTypes++;
 			}
 			else if (checkSymbol(newPassword.charAt(i)) && !containSymbol){
 				containSymbol = true;
 				differentTypes++;
 			}
 		}
 		if (newPassword.length() >= 6 && differentTypes >= 3) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Her tjekkers om det f�rst indtastede nye password er ens 
 	 * med det andet indtastede nye password  
 	 */
 	public boolean checkIfIdentical(String s, String k){
 		return (s.equals(k));
 	}
 
 	public static boolean checkLowercase(char c) {
 		return (c >= 'a' && c <= 'z');
 	}
 	public static boolean checkUppercase(char c) {
 		return (c >= 'A' && c <= 'Z');
 	}
 	public static boolean checkNumber(char c) {
 		return (c >= '0' && c <= '9');
 	}
 	public static boolean checkSymbol(char c) {
 		return (c == '.' || c == '-' || c == '_' || c == '+' | c == '!' || c == '?' || c == '=');
 	}
 
 	/**
 	 * Her udregnes netto-vægten fra bruger-inputs, hvilket sker under menupunkt 3  
 	 */
 	public double calculateWeight(double tare, double brutto) {
 		double netto = brutto - tare;
 		return netto;
 	}
 
 	public IRaavareDAO getRaavareDAO() {
 		return raavareDAO;
 	}
 	public IRaavareBatchDAO getRaavareBatchDAO() {
 		return raavareBatchDAO;
 	}
 	public IProduktBatchDAO getProduktBatchDAO() {
 		return produktBatchDAO;
 	}
 	public IReceptDAO getReceptDAO() {
 		return receptDAO;
 	}
 	public IReceptKompDAO getReceptKompDAO() {
 		return receptKompDAO;
 	}
 	public IOperatoerDAO getOprDAO() {
 		return oprDAO;
 	}
 
 	public void setOprDAO(IOperatoerDAO oprDAO) {
 		this.oprDAO = oprDAO;
 	}
 }
