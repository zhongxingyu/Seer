 /* Name:                Team Enterprise
  * Program:             project_library
  * Problem Statement:   A program to manage a library
  * Input:               Library settings, books, patrons, lots of menu options
  * Output:              Whatever the user asks for, as far as the above.
 */
 
 package project_library;
 import java.util.Scanner;
 import java.io.FileWriter;
 import java.io.File;
 import java.util.List;
 import java.io.IOException;
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 /**
  *
  * @author jacob
  */
 public class Project_Library
 { //begin Project_Library
 
     public static void main(String[] args) 
     { // begin main
         DisplayWelcome();
         Library library = loadLibrary();
        Patron[] patrons = loadPatrons();
         Book[] books = loadBooks();
         if ( askPassword(library) )
             MainMenu(library,patrons,books);
         
         saveLibrary(library);
         savePatrons(patrons);
         saveBooks(books);
         System.exit(0);
     } //End main
     
     public static void DisplayWelcome()
     {
         System.out.println();
         System.out.println("##################################################################");
         System.out.println("######      Welcome to the Library Management System!     ########");
         System.out.println("######         It was designed by Team Enterprise         ########");
         System.out.println("######  It was commissioned by Black Hawk College, CS225  ########");
         System.out.println("######        For the City of Happyville, Illinois        ########");
         System.out.println("##################################################################");
         System.out.println();
     }
     
     public static void MainMenu(Library library, Patron[] patrons, Book[] books)
    {//begin mainMenu
        Scanner keyboard = new Scanner(System.in);
        int next;
        boolean nextLoop = true;
        do
        {//begin do
        System.out.println("Library Management");
        System.out.println("Please select your action:");
        System.out.println("1. Patron Records");
        System.out.println("2. Book Records");
        System.out.println("3. Checkout Book");
        System.out.println("4. Checkin Book");
        System.out.println("5. Library Settings");
        System.out.println("6. Exit");
        System.out.print(": ");
        next = keyboard.nextInt();
        switch (next)
        {//begin switch
            case 1: PatronOptions(); break;
            case 2: BooksMenu(); break;
            case 3: CheckoutBook(); break;
            case 4: CheckinBook(); break;
            case 5: LibrarySettingsMenu(); break;
            case 6: nextLoop = false; break;
        }//end switch
        }while (nextLoop);//end do
    }//end mainMenu
 
 
     public static void BooksMenu()
     {//begin BooksMenu
         Scanner keyboard = new Scanner(System.in);
         String WelcomeMenu;
         int MenuOption;
 
         System.out.println("This is the Books Option Menu.");
 
         do
         {//begin do
        System.out.println("Enter the option you want"
                 + "to do:\n [1] to Search for books."
                 + "\n [2] to list all the books."
                 + "\n [3] to Edit the books."
                 + "\n [0] to Exit to the main menu");
        System.out.print(": ");
        WelcomeMenu = keyboard.nextLine();
         MenuOption= Integer.parseInt(WelcomeMenu);
 
         switch (MenuOption)
         {//begin switch
             case 0: break;
             case 1: BookSearch();break;
             case 2: AllBooks();break;
             case 3: EditBooks();break;
             default:System.out.println("Invalid Option"); break;
         }//end switch
 
         }//end do
         while(MenuOption!=0);
 
         System.out.println("End of Books Menu.");
 
 
     }//end BooksMenu
 
         public static void EditBooks()
     {//begin EditBooks
         Scanner keyboard = new Scanner(System.in);
         System.out.println("This is the Edit Books Method.");
         String EditOptions;
         int EditOptionsChoice;
 
        do
        {//begin Do
        System.out.println("Edit Books Options"
                 + "\n[1] to change book status."
                 + "\n[2] to change the books condition."
                 + "\n[3] to go back to the  EditBooks Menu.");
        System.out.print(": ");
        EditOptions = keyboard.nextLine();
         EditOptionsChoice = Integer.parseInt(EditOptions);
         switch (EditOptionsChoice)
         {//begin Switch
             case 0: break;
             case 1: EditBookStatus();break;
             case 2: EditBookCondition();break;
             default:System.out.println("Invalid Option"); break;
         }//end Switch
        }//end Do
        while (EditOptionsChoice!=3);
     }//end EditBooks
 
     public static void BookSearch()
     {//begin BookSearch
         System.out.println("This is the Book Search Method.");
     }//end BookSearch
 
     public static void AllBooks()
     {//begin AllBooks
         System.out.println("This is the All Books method.");
     }//end AllBooks
 
 
 
         //***** PatronOptions ****************
     public static void PatronOptions()
     {//begin of PatronOptions
         Scanner keyboard = new Scanner(System.in);
         int response;
 
         do 
         { //begin do while
 
             //Give Patron Options menu and ask user where to go
             System.out.println("Patron Options");
             System.out.println("Please enter the number of the option"
                     + " you would like to select.\n");
             System.out.println("[1] Add Patron");
             System.out.println("[2] Search For And Edit Patron");
             System.out.println("[3] List All Patrons");
             System.out.println("[4] Check Patron's Fines");
             System.out.println("[5] Exit To Main Menu\n");
             System.out.print(": ");
             response = keyboard.nextInt();
 
             switch (response)
             {
                 case 1:addPatron();break;
                 case 2:searchEditPatron();break;
                 case 3:listPatrons();break;
                 case 4:patronFines();break;
                 case 5:break;
                 default: System.out.println("Invalid Option");
             }
 
         } while (response != 5); //end do while
     }//end of PatronOptions
 
     //***** addPatron ******************
     public static void addPatron()
     {//begin of addPatron
         System.out.println("\nYou are at Add Patron.");
     }//end of addPatron
 
     //***** addPatron ******************
     public static void searchEditPatron()
     {//begin of searchEditPatron
         System.out.println("\nYou are at Search For And Edit Patron.");
     }//end of searchEditPatron
 
     //***** addPatron ******************
     public static void listPatrons()
     {//begin of listPatrons
         System.out.println("\nYou are at List All Patrons.");
     }//end of listPatrons
 
     //***** addPatron ******************
     public static void patronFines()
     {//begin of patronFines
         System.out.println("\nYou are at Check Patron's Fines.");
     }//end of patronFines
 
     public static void EditBookStatus()
     {//begin EditBookStatus
       System.out.println("This is the Edit Book Status method"
               + " under the Edit Books method.");
     }//end EditBookStatus
 
     public static void EditBookCondition()
     {//begin EditBookCondition
         System.out.println("This is the Edit Books Condition"
                 + " method under the Edit Books Method.");
     }//end EditBookCondition
    
    public static void PatronRecordsMenu()
    {
        System.out.println("You are at the Patron Records Menu");
    }
    
    public static void BookRecordsMenu()
    {
        System.out.println("You are at the Book Records Menu");
    }
    
    public static void CheckoutBook()
    {
        System.out.println("You are at the Checkout Books Menu");
    }
    
    public static void CheckinBook()
    {
        System.out.println("You are at the Checkin Books Menu");
    }
    
    public static void LibrarySettingsMenu()
    {
        System.out.println("You are at the Library Settings Menu");
    }
 
     public static boolean askPassword(Library library)
     {
         System.out.println("This is the method which asks for the password");
         System.out.println("It has not yet been implimented, so we'll just let you in");
         return true;
     }
 
     public static Book[] loadBooks()
     {
         Book[] books = new Book[2];
         System.out.println("The Books will be loaded here!");
         return books;
     }
 
     public static Patron[] loadPatrons()
     {
         SAXBuilder builder = new SAXBuilder();
         String path = "/tmp/tmp_project_library.xml";
         File patronXMLFile = new File(path);
         while (!patronXMLFile.exists());
         {
             path = askForFile(path,"Patron XML File");
             patronXMLFile = new File(path);
         }
         
         try {
             Document document = (Document) builder.build(patronXMLFile);
             Element rootNode = document.getRootElement();
             List list = rootNode.getChildren("patron");
             
             Patron[] patrons = new Patron[list.size()+10];
             for (int i = 0; i < list.size(); i++)
             {
                 Element node = (Element) list.get(i);
                 
                 patrons[i].setIsSet();
                 patrons[i].setFirstName(node.getChildText("firstname"));
                 patrons[i].setLastName(node.getChildText("lastname"));
                 patrons[i].setStreet(node.getChildText("street"));
                 patrons[i].setCity(node.getChildText("city"));
                 patrons[i].setState(node.getChildText("state"));
                 patrons[i].setZipCode(node.getChildText("zipCode"));
                 patrons[i].setPhone(node.getChildText("phone"));
                 patrons[i].setEmail(node.getChildText("email"));
                 patrons[i].setRestrictedTo(node.getChildText("restrictedTo"));
                 patrons[i].setMembershipStatus(node.getChildText("membershipStatus"));
                 patrons[i].setBirthday(node.getChildText("birthday"));
                 patrons[i].setFine(Double.parseDouble(node.getChildText("fine")));
                 patrons[i].setSpecialFine(Double.parseDouble(node.getChildText("specialFine")));
                 patrons[i].setCheckedBooks(Integer.parseInt(node.getChildText("checkedBooks")));
             }
         } catch (IOException ioe) {
             System.out.println(ioe.getMessage());
         } catch (JDOMException jdomex) {
             System.out.println(jdomex.getMessage());
         }
         Patron[] patrons = new Patron[2];
         System.out.println("The Patrons will be loaded here!");
         return patrons;
     }
 
     public static Library loadLibrary()
     {
         Library library = new Library();
         System.out.println("The Library settings will be loaded here!");
         return library;
     }
 
     public static void saveLibrary(Library library)
     {
         System.out.println("This method would save the library settings");
     }
 
     public static void savePatrons(Patron[] patrons)
     {
         //get the file as needed.
         String path = "/tmp/tmp_project_library.xml";
         File patronXMLFile = new File(path);
         while (!patronXMLFile.exists())
         {
             path = askForFile(path,"Patron XML File");
             patronXMLFile = new File(path);
         }
         
         try {
             //create the XML document
             Element patronfile = new Element("patronfile");
             Document doc = new Document(patronfile);
             doc.setRootElement(patronfile);
             
             //get the total number of used patrons in the array
             int usedpatrons = 0;
             for (int i=0; i<patrons.length; i++)
                 if (patrons[i].isSet())
                     usedpatrons++;
             
             //create an XML object for each patron, assign all its values
             Element[] savepatrons = new Element[usedpatrons];
             for (int i=0; i<savepatrons.length;i++)
             {//begin for each patron
                 savepatrons[i] = new Element("patron");
                 savepatrons[i].setAttribute(new Attribute("id",String.valueOf(i)));
                 savepatrons[i].addContent(new Element("firstname").setText(patrons[i].getFirstName()));
                 savepatrons[i].addContent(new Element("lastname").setText(patrons[i].getLastName()));
                 savepatrons[i].addContent(new Element("street").setText(patrons[i].getStreet()));
                 savepatrons[i].addContent(new Element("city").setText(patrons[i].getCity()));
                 savepatrons[i].addContent(new Element("state").setText(patrons[i].getState()));
                 savepatrons[i].addContent(new Element("zipCode").setText(patrons[i].getZipCode()));
                 savepatrons[i].addContent(new Element("phone").setText(patrons[i].getPhone()));
                 savepatrons[i].addContent(new Element("email").setText(patrons[i].getEmail()));
                 savepatrons[i].addContent(new Element("restrictedTo").setText(patrons[i].getRestrictedTo()));
                 savepatrons[i].addContent(new Element("membershipStatus").setText(patrons[i].getMembershipStatus()));
                 savepatrons[i].addContent(new Element("Birthday").setText(patrons[i].getBirthday()));
                 savepatrons[i].addContent(new Element("Fines").setText(String.valueOf(patrons[i].getFine())));
                 savepatrons[i].addContent(new Element("specialFines").setText(String.valueOf(patrons[i].getSpecialFine())));
                 savepatrons[i].addContent(new Element("checkedBooks").setText(String.valueOf(patrons[i].getCheckedBooks())));
                 
                 //add the now-finished xml object to the document
                 doc.getRootElement().addContent(savepatrons[i]);
             }//end for each patron
             
             //write the file
             XMLOutputter xmlOutput = new XMLOutputter();            
             xmlOutput.setFormat(Format.getPrettyFormat());
             xmlOutput.output(doc, new FileWriter(path));
         } catch (IOException io) {
             System.out.println(io.getMessage());
         }
     }
 
     public static void saveBooks(Book[] books)
     {
         System.out.println("This method would save the books");
     }
     
     public static String askForFile(String path, String filename)
     {
         Scanner userInput = new Scanner(System.in);
         System.out.println("Unable to find file at: "+path);
         System.out.println("Please input the path for: "+filename);
         System.out.print(": ");
         
         String response = userInput.nextLine();
         return response;
     }
     
     public static Patron[] loadTestPatron()
     {
        Patron[] patrons = new Patron[1];
         for (int i = 0; i<patrons.length;i++)
         {
             patrons[i].setIsSet();
             patrons[i].setFirstName("Jacob");
             patrons[i].setLastName("Burkamper");
            patrons[i].setStreet("2425 Shaker Ct.");
            patrons[i].setCity("Bettendorf");
            patrons[i].setState("IA");
            patrons[i].setZipCode("52722");
            patrons[i].setPhone("630-621-8887");
             patrons[i].setEmail("jacob.burkamper@gmail.com");
             patrons[i].setRestrictedTo("none");
             patrons[i].setMembershipStatus("member");
            patrons[i].setBirthday("09071991");
             patrons[i].setFine(0.0);
             patrons[i].setSpecialFine(0.0);
             patrons[i].setCheckedBooks(loadTestBookIDArray());
         }
         return patrons;
     }
     
     public static int[] loadTestBookIDArray()
     {
         int[] bookIDs = new int[3];
         bookIDs[0]=0;
         bookIDs[1]=1;
         bookIDs[2]=2;
         return bookIDs;
     }
     
     public static int findNextPatron(Patron[] patrons)
     {
         int NextID=-1;
         for (int i = 0; i < patrons.length; i++)
         {
             if (patrons[i].isSet())
                 continue;
             else
             {
                 if (NextID == -1)
                     NextID = i;
             }
         }
         return NextID;
     }
 
 } //End Project_Library
