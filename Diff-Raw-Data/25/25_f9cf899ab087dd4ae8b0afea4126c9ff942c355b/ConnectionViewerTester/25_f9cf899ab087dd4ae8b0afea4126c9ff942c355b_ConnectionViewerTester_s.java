 package helpertools;
 
 import java.io.*;
 import java.net.Socket;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Random;
 
 import javax.swing.JOptionPane;
 
 import helpertools.ActiveConnectionObj;
 
 public class ConnectionViewerTester implements Runnable {
 
 	private Socket socket;
 	private Thread t; 
 	private ObjectOutputStream oos;
 	private ObjectInputStream ois;
 	private static String user;
 	
 	private String[] surnames = {"SMITH","JOHNSON","WILLIAMS","JONES","BROWN","DAVIS","MILLER","WILSON","MOORE","TAYLOR","ANDERSON","THOMAS","JACKSON","WHITE","HARRIS","MARTIN","THOMPSON","GARCIA","MARTINEZ","ROBINSON","CLARK","RODRIGUEZ","LEWIS","LEE","WALKER","HALL","ALLEN","YOUNG","HERNANDEZ","KING","WRIGHT","LOPEZ","HILL","SCOTT","GREEN","ADAMS","BAKER","GONZALEZ","NELSON","CARTER","MITCHELL","PEREZ","ROBERTS","TURNER","PHILLIPS","CAMPBELL","PARKER","EVANS","EDWARDS","COLLINS","STEWART","SANCHEZ","MORRIS","ROGERS","REED","COOK","MORGAN","BELL","MURPHY","BAILEY","RIVERA","COOPER","RICHARDSON","COX","HOWARD","WARD","TORRES","PETERSON","GRAY","RAMIREZ","JAMES","WATSON","BROOKS","KELLY","SANDERS","PRICE","BENNETT","WOOD","BARNES","ROSS","HENDERSON","COLEMAN","JENKINS","PERRY","POWELL","LONG","PATTERSON","HUGHES","FLORES","WASHINGTON","BUTLER","SIMMONS","FOSTER","GONZALES","BRYANT","ALEXANDER","RUSSELL","GRIFFIN","DIAZ","HAYES","2. Jackson"
 };
 	private String[] firstnames = {" Sophia"," Emma"," Isabella"," Olivia"," Ava"," Lily"," Chloe"," Madison"," Emily"," Abigail"," Addison"," Mia"," Madelyn"," Ella"," Hailey"," Kaylee"," Avery"," Kaitlyn"," Riley"," Aubrey"," Brooklyn"," Peyton"," Layla"," Hannah"," Charlotte"," Bella"," Natalie"," Sarah"," Grace"," Amelia"," Kylie"," Arianna"," Anna"," Elizabeth"," Sophie"," Claire"," Lila"," Aaliyah"," Gabriella"," Elise"," Lillian"," Samantha"," Makayla"," Audrey"," Alyssa"," Ellie"," Alexis"," Isabelle"," Savannah"," Evelyn"," Leah"," Keira"," Allison"," Maya"," Lucy"," Sydney"," Taylor"," Molly"," Lauren"," Harper"," Scarlett"," Brianna"," Victoria"," Liliana"," Aria"," Kayla"," Annabelle"," Gianna"," Kennedy"," Stella"," Reagan"," Julia"," Bailey"," Alexandra"," Jordyn"," Nora"," Caroline"," Mackenzie"," Jasmine"," Jocelyn"," Kendall"," Morgan"," Nevaeh"," Maria"," Eva"," Juliana"," Abby"," Alexa"," Summer"," Brooke"," Penelope"," Violet"," Kate"," Hadley"," Ashlyn"," Sadie"," Paige"," Katherine"," Sienna"," Piper"," Aiden"," Jackson"," Mason"," Liam"," Jacob"," Jayden"," Ethan"," Noah"," Lucas"," Ethan"," Noah"," Lucas"," Logan"," Caleb"," Caden"," Jack"," Ryan"," Connor"," Michael"," Elijah"," Brayden"," Benjamin"," Nicholas"," Alexander"," William"," Matthew"," James"," Landon"," Nathan"," Dylan"," Evan"," Luke"," Andrew"," Gabriel"," Gavin"," Joshua"," Owen"," Daniel"," Carter"," Tyler"," Cameron"," Christian"," Wyatt"," Henry"," Eli"," Joseph"," Max"," Isaac"," Samuel"," Anthony"," Grayson"," Zachary"," David"," Christopher"," John"," Isaiah"," Levi"," Jonathan"," Oliver"," Chase"," Cooper"," Tristan"," Colton"," Austin"," Colin"," Charlie"," Dominic"," Parker"," Hunter"," Thomas"," Alex"," Ian"," Jordan"," Cole"," Julian"," Aaron"," Carson"," Miles"," Blake"," Brody"," Adam"," Sebastian"," Adrian"," Nolan"," Sean"," Riley"," Bentley"," Xavier"," Hayden"," Jeremiah"," Jason"," Jake"," Asher"," Micah"," Jace"," Brandon"," Josiah"," Hudson"," Nathaniel"," Bryson"
 };
 	private String[] titles = {"Actuary","Assessor","Benefits Officer","Branch Manager","Budget Analyst","Cash Manager","Certified Financial Planner","Chartered Wealth Manager","Chief Executive Officer","Chief Financial Officer","Claims Adjuster","Commercial Appraiser","Commercial Real Estate Agent","Commercial Real Estate Broker","Controller","Credit Analyst","Credit Manager","Damage Appraiser","Financial Analyst","Hedge Fund Manager","Hedge Fund Principal","Hedge Fund Trader","Insurance Adjuster","Insurance Agent","Insurance Appraiser","Insurance Broker","Insurance Claims Examiner","Insurance Investigator","Investment Advisor","Investment Banker","Investor Relations Officer","Leveraged Buyout Investor","Loan Officer","Loss Control Specialist","Mortgage Banker","Mutual Fund Analyst","Portfolio Management Marketing","Portfolio Manager","Ratings Analyst","Real Estate Appraiser","Real Estate Officer","Residential Appraiser","Residential Real Estate Agent","Residential Real Estate Broker","Risk Manager","Service Representative","Stockbroker","Treasurer","Trust Officer","Underwriter"
 };
 	private boolean[] truths = {true, false};
 	
 	public static void main(String[] args) {
 		try {
 			user = JOptionPane.showInputDialog("Enter Target User:");
 			
		for (int i = 0; i < 10; i++) {
 			new ConnectionViewerTester("user" + i, "compName" + i, new Date(), "Client");
 		}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public ConnectionViewerTester (String user, String compName, Date date, String userLevel) throws Exception {
 		socket = new Socket("localhost", 1234);
 		oos = new ObjectOutputStream(socket.getOutputStream());
 		ois = new ObjectInputStream(socket.getInputStream());
 		oos.writeObject(new ActiveConnectionObj(user, compName, date, userLevel));
 		
 		t = new Thread(this);
 		t.start();
 	}
 	
 	@Override
 	/* Test the server for functionality */
 	public void run() {
 //		boolean sentUser = false;
 //		boolean emailedUser = false;
 //		boolean emailSuccess = false;
 		System.out.println("Connected to server... sending user information now.");
 		
 		try {
 			sendUser(); // start of receive loop
 			while (true) {
 				Object serverResponse = ois.readObject();
 				// get form status
 				if (serverResponse instanceof FormStatus) {
 					FormStatus fs = (FormStatus) serverResponse;
 					int serverStatus = fs.getStatusID();
 					
 					if (serverStatus == UserStatusID.FORM_RECIEVED) { // form received code
 						// email it
						oos.writeObject(new RequestAuth(user));
 					}
 					else if (serverStatus == UserStatusID.EMAIL_SENT) { // email sent code
 						// make sure I know that it was sent
 						System.out.println("Message was sent out!");
 						break; // you're done!
 					}
 					else {
 						// error msg for no email sent...
 						System.out.println("Message not Sent...");
 					}
 				}
 			}
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 	
 	/* Modular test units */
 	private void sendUser () throws ParseException, IOException {
 		/* Get the input from the form */
 		Random r = new Random();
 		char initial = (char)(r.nextInt(26) + 'A');
 		 // Randomizer formula is start + random# * (end - start)
 		int startYear = 80 + r.nextInt(35);
 		int startMonth = 1 + r.nextInt(11);
 		int day = 1 + r.nextInt(27);
 		@SuppressWarnings("deprecation")
 		Date gc = new Date(startYear, startMonth, day);
 		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMMM dd, yyyy");
 		
 		String l_name = surnames[(int) (Math.random() * surnames.length)].trim();
 		String f_name = firstnames[(int)(Math.random() * firstnames.length)].trim();
 		String m_name = "" + initial;
 		String p_name = firstnames[(int)(Math.random() * firstnames.length)].trim();
 		String emp_loc = "" + (char)(r.nextInt(26) + 'A') + (char)(r.nextInt(26) + 'A') + (char)(r.nextInt(26) + 'A');
 		String emp_start = "" + sdf.format(gc);
 		String emp_title = titles[(int)(Math.random() * titles.length)].trim();
 		String emp_manager = firstnames[(int)(Math.random() * firstnames.length)].trim() + " " + surnames[(int) (Math.random() * surnames.length)].trim() ;
 		String emp_dept = "Engineering";
 		String emp_ID = "" + r.nextInt(10000);
 		boolean emp_rehire = truths[(int)(Math.random() * truths.length)];
 		boolean emp_reg = truths[(int)(Math.random() * truths.length)];
 		
 		NewUser u = new NewUser(f_name, l_name, p_name, m_name, emp_title, emp_manager, emp_dept, emp_ID, emp_loc, emp_start, emp_rehire, emp_reg);
 		oos.writeObject(u);
 	}
 	
 }
