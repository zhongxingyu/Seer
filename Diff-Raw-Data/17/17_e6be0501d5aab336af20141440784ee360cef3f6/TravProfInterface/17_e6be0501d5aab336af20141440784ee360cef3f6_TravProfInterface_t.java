 import java.io.BufferedReader;
 import java.util.*;
 import java.io.IOException;
 import java.io.InputStreamReader;
 public class TravProfInterface {
 	String[] menuChoices;
 	String travAgentID;
 	TravProfDB database;
 
 	public TravProfInterface(String fileName) {
 		database = new TravProfDB(fileName);
		menuChoices = new String[8];
 		menuChoices[0] = "Create a new traveler profile";
 		menuChoices[1] = "Delete a traveler by Name and Travel Agent ID";
 		menuChoices[2] = "Find and display a Traveler Profile by Name and Travel Agent ID";
 		menuChoices[3] = "Traveler Profile Modifications";
 		menuChoices[4] = "Display all profiles";
 		menuChoices[5] = "Write to Database";
 		menuChoices[6] = "Initialize Database";
 		menuChoices[7] = "Quit";
 		getUserChoice();
 	}
 	public void getUserChoice()
 	{
		
 		boolean valid;
 		
 		do {
			System.out.println("Menu:");
			for (int i=0; i<menuChoices.length; i++)
			{
				System.out.println(i + " - " + menuChoices[i]);
			}
			String travelAgentID;
 			valid = false;
 			int menuOption = Integer.parseInt(inputString("Menu Option?","int"));
 			travelAgentID = inputString("Travel Agent ID?","string");
 				switch (menuOption) 
 				{
 					case 0: 
 						database.insertNewProfile(createNewTravProf(travelAgentID));
 						break;
 					case 1:
 						deleteTravProf();
 						break;
 					case 2:
 						findTravProf();
 						break;
 					case 3:
 						modifyTravProf();
 						break;
 					case 4:
 						displayAllProfiles();
 						break;
 					case 5:
 						database.writeAllTravProf();
 						break;
 					case 6:
 						database.initializeDatabase();
 						break;
 					case 7:
 						valid = true;
 					default:
 						break;
 					
 				}	
 		} while(valid == false);
 		System.exit(0);
 	}
 
 	
 	private TravProf createNewTravProf(String travelAgentID)
 	{
 		String[][] requestedData = new String[8][2];
 		requestedData[0][0] = "Traveler Agent ID";
 		requestedData[0][1] = travelAgentID;
 		requestedData[1][0] = "First Name:";
 		requestedData[2][0] = "Last Name:";
 		requestedData[3][0] = "Address:";
 		requestedData[4][0] = "Phone:";
 		requestedData[5][0] = "Trip Cost:";//Float, NOT A STRING
 		requestedData[6][0] = "Travel Type:";
 		requestedData[7][0] = "Payment Type:";
 		
 		for (int i = 1; i < requestedData.length; i++)
 		{
 			if (i != 5)//Trip cost is a float, so get a float for that
 				requestedData[i][1] = inputString(requestedData[i][0],"string");
 			else
 				requestedData[i][1] = inputString(requestedData[i][0],"float");
 		}
 
 		TravProf newProf = new TravProf(requestedData[0][1], requestedData[1][1], requestedData[2][1], requestedData[3][1], requestedData[4][1], Float.parseFloat(requestedData[5][1]), requestedData[6][1], requestedData[7][1], createNewMedCond());
 		return newProf;
 		
 	}
 	
 	private void deleteTravProf()
 	{
 		boolean success = true;
 		try
 		{
 			database.deleteProfile(travAgentID, inputString("Last name of profile to delete?","string"));
 		}
 		catch (java.lang.Exception e)
 		{
 			success = false;
 		}
 		if (success)
 			System.out.println("Successfully deleted");
 		else
 			System.out.println("Profile was not successfully deleted. Maybe the travel agent ID and last name don't match?");
 	}
 	
 	private MedCond createNewMedCond()
 	{
 		//For med condition
 		MedCond newMed = null;
 		boolean valid;
 		do {
 			valid = true;
 			String[][] medData = new String[4][2];
 			medData[0][0] = "Medical Contact:";
 			medData[1][0] = "Medical Phone:";
 			medData[2][0] = "Allergies?";
 			medData[3][0] = "Illness?";
 			
 			for (int i = 0; i<medData.length; i++)
 				medData[i][1] = inputString(medData[i][0],"string");
 			
 			try
 			{
 				newMed = new MedCond(medData[0][1],medData[1][1],medData[2][1],medData[3][1]);
 			}
 			catch (java.lang.Exception e)
 			{
 				System.out.println(e.getMessage());
 				System.out.println("Please reenter info with valid data");
 				valid = false;
 			}
 		} while (valid == false);
 		return newMed;
 		
 	}
 	
 	private void findTravProf()
 	{
 		try
 		{
 			TravProf temp = database.findProfile(travAgentID, inputString("Last name:","string"));
 			displayTravProf(temp);
 		}
 		catch (java.lang.Exception e)
 		{
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	private void displayTravProf(TravProf temp)
 	{
 		System.out.println("Travel Agent ID:" + temp.getTravAgentID());
 		System.out.println("Last Name:" + temp.getLastName());
 		System.out.println("First Name:" + temp.getFirstName());
 		System.out.println("1 - Address:" + temp.getAddress());
 		System.out.println("2 - Phone:" + temp.getPhone());
 		System.out.println("3 - Payment Type:" + temp.getPaymentType());
 		System.out.println("4 - Travel Type:" + temp.getTravelType());
 		System.out.println("5 - Trip Cost:" + temp.getTripCost());
 		System.out.println("6 - Medical Contact:" + temp.getMedCondInfo().getMdContact());
 		System.out.println("7 - Medical Phone:" + temp.getMedCondInfo().getMdPhone());
 		System.out.println("8 - Allergy Type:" + temp.getMedCondInfo().getAlgType());
 		System.out.println("9 - Illness Type:" + temp.getMedCondInfo().getIllType());
 	}
 	/* This method is used for getting user input. The method prompts the user
 	 * for data with the prompt string argument. If there is an IO error, or the
 	 * data isn't valid for the question (ex - entering a word instead of a number
 	 * for a question that requires a numerical answer), the method will continue
 	 * to prompt the user until it gets valid data.
 	 */
 	private void displayAllProfiles()
 	{
 		try {
 			displayTravProf(database.findFirstProfile());
 			boolean done = false;
 			while (done == false) {
 				try 
 				{
 					displayTravProf(database.findNextProfile());
 				}
 				catch (java.lang.Exception e)
 				{
 					done = true;
 				}
 			}
 		}
 		catch (java.lang.Exception e)
 		{
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	private void modifyTravProf()
 	{
 		boolean valid;
 		do {
 			valid = true;
 			try
 			{
 				TravProf temp = database.findProfile(travAgentID, inputString("Last name? ", "string"));
 				displayTravProf(temp);
 				int choice = Integer.parseInt(inputString("Modify which field? (Enter the number)","int"));
 				switch (choice) {
 					case 1:
 						temp.setAddress(inputString("New Address?", "string"));
 						break;
 					case 2:
 						temp.setPhone(inputString("New Phone?", "string"));
 						break;
 					case 3:
 						temp.setPaymentType((inputString("New Payment Type?", "string")));
 						break;
 					case 4:
 						temp.setTravelType(inputString("New Travel Type?", "string"));
 						break;
 					case 5:
 						temp.setTripCost(Float.parseFloat(inputString("New Trip Cost?", "float")));
 						break;
 					case 6:
 						MedCond tempMed = temp.getMedCondInfo();
 						tempMed.setMdContact(inputString("New Medical Contact?","string"));
 						break;
 					case 7:
 						MedCond tempMed1 = temp.getMedCondInfo();
 						tempMed1.setMdPhone(inputString("New Medical Phone?","string"));
 						break;
 					case 8:
 						MedCond tempMed2 = temp.getMedCondInfo();
 						boolean validAlg;
 						do {
 							validAlg = true;
 							try
 							{
 								tempMed2.setAlgType(inputString("New Allergy?","string"));
 							}
 							catch (java.lang.Exception e)
 							{
 								validAlg = false;
 							}
 							
 						} while (validAlg == false);
 						break;
 					case 9:
 						MedCond tempMed3 = temp.getMedCondInfo();
 						boolean validIllness;
 						do {
 							validIllness = true;
 							try
 							{
 								tempMed3.setIllType(inputString("New Illness?","string"));
 							}
 							catch (java.lang.Exception e)
 							{
 								validIllness = false;
 							}
 							
 						} while (validIllness == false);
 						break;
 					default:
 						valid = false;
 						break;
 				}
 			}
 			catch (java.lang.Exception e)
 			{
 				System.out.println(e.getMessage());
 			}
 		} while (valid == false);
 		
 	}
 	
 	private String inputString(String prompt, String type)
 	{
 		boolean valid;
 		String input = "";
 		do {
 			System.out.println(prompt);
 			valid = true;
 			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 			try {
 		         input = br.readLine();
 		        	 
 			} catch (IOException ioe) {
 		         System.out.println("IO Error. Try entering your input again.");
 		         valid = false;
 		         
 			}
 			if (type == "string")
 			{}
 			else if (type == "int") 
 			{
 				try 
 				{
 					Integer.parseInt(input);
 				}
 				catch (java.lang.Exception e)
 				{
 					valid = false;
 				}
 			}
 			else if (type == "float")
 			{
 				try 
 				{
 					Float.parseFloat(input);
 				}
 				catch (java.lang.Exception e)
 				{
 					valid = false;
 				}
 			}
 			if (valid==false)
 				System.out.println("That wasn't a valid entry for the question. Try again.");
 			
 		} while (valid==false);
 		return input;
 	}
 	
 	
 
 }
