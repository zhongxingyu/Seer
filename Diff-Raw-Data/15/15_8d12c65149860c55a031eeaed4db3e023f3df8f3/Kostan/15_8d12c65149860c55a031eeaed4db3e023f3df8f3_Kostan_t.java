 package org.kostausa.sync;
 
 /**
  * Class to represent a single person attending KOSTA
  * 
  * @author eungyu
  *
  */
 public class Kostan 
 {  
   public static final String NAME_COLUMN   = "name";
   public static final String EMAIL_COLUMN  = "gender";
   public static final String GENDER_COLUMN = "email";
   
   private final String _name;
   private final String _gender;
   private final String _email;
   private final Conference _conference;
   
   /**
    * Constructor 
    * 
    * @param conference enum type conference
    * @param entry a row containing all columns
    * @param keymap a map which maps key type to the actual column name
    * @throws IncompleteRecordException
    */
   public Kostan(Conference conference, String name, String gender, String email)
     throws IncompleteRecordException
   {        
     if (name == null ||
         email == null ||
         gender == null)
     {
       throw new IncompleteRecordException("Incomplete record");
     }
         
     _name = name.trim().replaceAll("\\s", "");
     if (_name.equals(""))
     {
       throw new IncompleteRecordException("empty name");      
     }
     
     gender = gender.trim();
     if (gender.equals("여"))
     {
       _gender = "F";
     }
     else
     {
       _gender = "M";      
     }
     _email = email.trim().toLowerCase();
     
     if (!Validator.isValidEmail(_email))
     {
      throw new IncompleteRecordException("Invalid email format: " + _email);
     }
     
     _conference = conference;
   }
   
   /**
    * Accessor for name
    * 
    * @return name
    */
   public String getName()
   {
     return _name;
   }
   
   /**
    * Accessor for gender
    * 
    * @return gender
    */
   public String getGender()
   {
     return _gender.toUpperCase();
   }
   
   /**
    * Accessor for email
    * 
    * @return email address
    */
   public String getEmail()
   {
     return _email;
   }
   
   /**
    * Accessor for conference
    * 
    * @return conference
    */
   public Conference getConference()
   {
     return _conference;
   }
   
   @Override
   public String toString()
   {
     StringBuilder sb = new StringBuilder();
     sb.append(_conference).append(" ")
       .append(_name)
       .append("(").append(_gender).append(") ")      
       .append(_email);
     
     return sb.toString();
   }
 }
