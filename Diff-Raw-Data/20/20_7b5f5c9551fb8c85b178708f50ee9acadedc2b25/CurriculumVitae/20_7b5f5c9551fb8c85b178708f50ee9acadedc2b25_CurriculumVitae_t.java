 import java.io.File;
 import java.io.FileNotFoundException;
 
 /**
  * Exports a .TeX file of a basic CV with very little information about the
  * person.
  *
  * @author Michael Mardaus
  */
 public class CurriculumVitae {
     /**
      * the firstname.
      */
     private static String firstname;
     /**
      * the lastname.
      */
     private static String lastname;
     /**
      * the path of a picture file.
      */
     private static String picturePath;
     /**
      * the telephone number.
      */
     private static String telephone;
     /**
      * the email address.
      */
     private static String email;
 
     /**
      * @return the firstname
      */
     public static String getFirstname() {
         return firstname;
     }
 
     /**
      * @param firstname
      *            the firstname to set
      */
     public final void setFirstname(final String firstname) {
         CurriculumVitae.firstname = firstname;
     }
 
     /**
      * @return the lastname
      */
     public static String getLastname() {
         return lastname;
     }
 
     /**
      * @param lastname
      *            the lastname to set
      */
     public final void setLastname(final String lastname) {
         CurriculumVitae.lastname = lastname;
     }
 
     /**
      * @return the picturePath
      */
     public static String getPicturePath() {
         return picturePath;
     }
 
     /**
      * @param picturePath
      *            the picturePath to set
      */
     public final void setPicturePath(final String picturePath) {
         CurriculumVitae.picturePath = picturePath;
     }
 
     /**
      * @return the telephone
      */
     public static String getTelephone() {
         return telephone;
     }
 
     /**
      * @param telephone
      *            the telephone to set
      */
     public final void setTelephone(final String telephone) {
         CurriculumVitae.telephone = telephone;
     }
 
     /**
      * @return the email
      */
     public static String getEmail() {
         return email;
     }
 
     /**
      * @param email
      *            the email to set
      */
     public final void setEmail(final String email) {
         CurriculumVitae.email = email;
     }
 
     /**
      * Replaces all german umlauts with LaTeX compatible ones.
      *
      * @param s
      *            the input string
      * @return new replaced string
      */
     public static String convertUmlaut(final String s) {
        String newS = s.replaceAll("ä", "\\\\\"a").replaceAll("ü", "\\\\\"u")
                .replaceAll("ö", "\\\\\"o").replaceAll("ß", "\\\\ss ")
                .replaceAll("Ä", "\\\\\"A").replaceAll("Ö", "\\\\\"O")
                .replaceAll("Ü", "\\\\\"U");
         return newS;
     }
 
     /**
      * Generates the first lines with personal information.
      *
      * @return two lines if the path is not accessible, three lines if a file is
      *         found at the given path.
      * @throws MissingNameException
      *             if a name is not given
      * @throws FileNotFoundException
      *             if the file is not accessible
      */
     public static String createPersonalData() throws MissingNameException,
             FileNotFoundException {
 
         if (getFirstname() == null || getLastname() == null) {
             throw new MissingNameException("first or lastname was null");
         }
 
         String s = "\\firstname{" + firstname + "}\n" + "\\familyname{"
                 + lastname + "}\n";
         // try to read File
         if (getPicturePath() != null) {
             File f = new File(getPicturePath());
             if (f.isFile() && f.canRead()) {
                 s = s + "\\photo[96pt]{" + picturePath + "}\n";
             } else {
                 throw new FileNotFoundException(
                         "cannot find or read the given picture file");
             }
         }
         return s;
     }
 
     /**
      * Generates one line with phone information.
      *
      * @return a single line
      * @throws InvalidMobileNumberException
      *             if the format is not correct
      * @throws InvalidCVLineException
      *             should not happen
      */
     public static String createMobileLine()
             throws InvalidMobileNumberException, InvalidCVLineException {
         String number = getTelephone();
         if (number != null && number.matches("^\\+[1-9]+[0-9 ]+$")) {
             return createCVLine("\\mobilesymbol", number);
         } else {
             throw new InvalidMobileNumberException(
                     "the phonenumber you entered is not valid");
         }
     }
 
     /**
      * Generates one line with email information.
      *
      * @return a single line
      * @throws InvalidEmailException
      *             if the format is not correct
      */
     public static String createEmailLine() throws InvalidEmailException {
         String email = getEmail();
         if (email != null
                 && email.matches("^[_A-Za-z0-9-\\+]+"
                         + "(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*"
                         + "(\\.[A-Za-z]{2,})$")) {
             return "\\cvline{\\emailsymbol}{\\href{mailto:" + email + "}{"
                     + email + "}}\n";
         } else {
             throw new InvalidEmailException(
                     "the emailadress you entered is not valid");
         }
     }
 
     /**
      * Generates generic cv line.
      *
      * @param string1
      *            the first parameter
      * @param string2
      *            the second parameter
      * @return a single line
      * @throws InvalidCVLineException
      *             if the second string is not given
      */
     public static String createCVLine(final String string1,
             final String string2) throws InvalidCVLineException {
         if (string2 != null && !string2.isEmpty()) {
             return "\\cvline{" + string1 + "} {" + string2 + "}\n";
         } else {
             throw new InvalidCVLineException("the cv line has no 2nd argument");
         }
     }
 
     /**
      * Generates a cv entry line.
      *
      * @param entry
      *            a string array with exactly 6 strings
      * @return a single line
      * @throws InvalidCVEntryException
      *             if one of the first two strings is empty
      */
     public static String createCVEntry(final String[] entry)
             throws InvalidCVEntryException {
         if (entry != null && entry.length == 6 && entry[0] != null
                 && entry[1] != null && !entry[0].isEmpty()
                 && !entry[1].isEmpty()) {
             return "\\cventry{" + entry[0] + "} {" + entry[1] + "} {"
                     + entry[2] + "} {" + entry[3] + "} {" + entry[4] + "} {"
                     + entry[5] + "}\n";
         } else {
             throw new InvalidCVEntryException("the cv entry is malformed");
         }
     }
 
     /**
      * the main function.
      *
      * @param args
      *            commandline arguments
      */
     public static void main(final String[] args) {
        String s = "Hallo ich bin ein Umlaut ä und ich heiße Ä und das"
                + " sind meine Freunde öäüßÄÖÜ bla.";
         System.out.println(convertUmlaut(s));
 
         CurriculumVitae cv = new CurriculumVitae();
         cv.setFirstname("Kermit");
         cv.setLastname("the Frog");
        cv.setPicturePath("kermit.jpg");
         cv.setTelephone("+49 21321 546546");
         cv.setEmail("kermit@muppets.com");
 
         try {
             System.out.println(createPersonalData());
         } catch (MissingNameException | FileNotFoundException e) {
             System.err.println("You messed up! Please validate your"
                     + " input and try again.");
             e.printStackTrace();
         }
 
         try {
             System.out.println(createMobileLine());
         } catch (InvalidMobileNumberException | InvalidCVLineException e) {
             System.err.println("You messed up! Please validate your"
                     + " input and try again.");
             e.printStackTrace();
         }
 
         try {
             System.out.println(createEmailLine());
         } catch (InvalidEmailException e) {
             System.err.println("You messed up! Please validate your"
                     + " input and try again.");
             e.printStackTrace();
         }
 
         try {
             System.out.println(createCVLine("erster String", "zweiter String"));
         } catch (InvalidCVLineException e) {
             System.err.println("You messed up! Please validate your"
                     + " input and try again.");
             e.printStackTrace();
         }
 
         try {
             String[] testEntry = {"eins", "zwei", "drei", "vier", "fuenf",
                     "sechs" };
             System.out.println(createCVEntry(testEntry));
         } catch (InvalidCVEntryException e) {
             System.err.println("You messed up! Please validate your"
                     + " input and try again.");
             e.printStackTrace();
         }
     }
 }
 
 /**
  * Exception if a name is not given.
  *
  * @author Michael Mardaus
  */
 class MissingNameException extends Exception {
 
     /**
      *
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Constructor with Message.
      *
      * @param message
      *            the error message
      */
     public MissingNameException(final String message) {
         super(message);
     }
 }
 
 /**
  * Exception if a phonenumber is not valid.
  *
  * @author Michael Mardaus
  */
 class InvalidMobileNumberException extends Exception {
 
     /**
      *
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Constructor with Message.
      *
      * @param message
      *            the error message
      */
     public InvalidMobileNumberException(final String message) {
         super(message);
     }
 }
 
 /**
  * Exception if a email is not valid.
  *
  * @author Michael Mardaus
  */
 class InvalidEmailException extends Exception {
 
     /**
      *
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Constructor with Message.
      *
      * @param message
      *            the error message
      */
     public InvalidEmailException(final String message) {
         super(message);
     }
 }
 
 /**
  * Exception if a cv line is not valid.
  *
  * @author Michael Mardaus
  */
 class InvalidCVLineException extends Exception {
 
     /**
      *
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Constructor with Message.
      *
      * @param message
      *            the error message
      */
     public InvalidCVLineException(final String message) {
         super(message);
     }
 }
 
 /**
  * Exception if a cv entry is not valid.
  *
  * @author Michael Mardaus
  */
 class InvalidCVEntryException extends Exception {
 
     /**
      *
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Constructor with Message.
      *
      * @param message
      *            the error message
      */
     public InvalidCVEntryException(final String message) {
         super(message);
     }
}
