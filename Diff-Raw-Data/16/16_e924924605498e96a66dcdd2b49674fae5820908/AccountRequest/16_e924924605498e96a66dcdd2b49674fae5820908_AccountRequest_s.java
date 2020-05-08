 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.user;
 
 import java.util.List;
 
 public interface AccountRequest {
 
     /**
      * Returns the internal unique user id.
      *
      * @return the internal unique id
      */
    public int getID();
 
     /**
      * Sets the user id of the user, or null if the user id is not available.
      *
      * @param userID the user id
      */
     public void setUserID(String userID);
 
     /**
      * Returns the user id of the user, or null if the user id is not available.
      *
      * @return the user id
      */
     public String getUserID();
 
     /**
      * Returns the full name of the user, or null if the full name is not available.
      * The full name contains given names, family names and possibly a title or suffix.
      * Therefore, the full name may be different from the concatenation of given and family name.
      *
      * @return the full name
      */
     public String getFullName();
 
     /**
      * Sets the full name of the user, or null if the full name is not available.
      * The full name contains given names, family names and possibly a title or suffix.
      * Therefore, the full name may be different from the concatenation of given and family name.
      *
      * @param fullName the full name
      */
     public void setFullName(String fullName);
 
     /**
      * Sets the family (aka last) name of the user.
      *
      * @param familyName the family name
      */
     public void setFamilyName(String familyName);
 
     /**
      * Returns the family (aka first) name of the user, or null if the family name is not available.
      *
      * @return the family name
      */
     public String getFamilyName();
 
     /**
      * Returns the given (aka first) name of the user, or  if the given name is not available.
      *
      * @return the given name
      */
     public String getGivenName();
 
     /**
      * Sets the given (aka first) name of the user, or  if the given name is not available.
      *
      * @param givenName the given name
      */
     public void setGivenName(String givenName);
 
     /**
      * Returns the given e-mail of the user or null if none is available.
      *
      * @return the email address
      */
     public String getEmailAddress();
 
     /**
      * Sets the given e-mail of the user.
      *
     * @param the email address
      */
     public void setEmailAddress(String emailAddress);
 
     /**
      * Sets the users organizational affiliation
      *
      * @param organization the users organizational affiliation
      */
     public void setOrganization(String organization);
 
     /**
      * Returns the users organizational affiliation
      *
      * @return the users organizational affiliation
      */
     public String getOrganization();
 
     /**
      * Sets the list of myproxy user names that can be used for this user
      *
     * @param the list of myproxy user names expressed as Strings
      */
     public void setMyproxyUserNames(List myproxyUserNames);
 
     /**
      * Returns the list of myproxy user names that can be used for this user
      *
      * @return the list of myproxy user names expressed as Strings
      */
     public List getMyproxyUserNames();
 
     /**
      * Sets the list of myproxy user names that can be used for this user
      *
      * @param userdns the array of strings containing user DN information
      */
     public void setMyproxyUserDN(List userdns);
 
     /**
      * Returns the list of myproxy user names that can be used for this user
      *
     * @param userdns the array of strings containing user DN information
      */
     public List getMyProxyUserDN();
 
     /**
      * Sets the list of groups the user wishes to join
      *
      * @param groups the list of PortletGroups the user wishes to join
      */
     public void setDesiredGroups(List groups);
 
     /**
      * Returns the list of groups the user wishes to join
     *
     * @param groups the array of strings containg the groups the user wishes to join
      */
     public List getDesiredGroups();
 
     /**
      * Prints the constents of the account request to a String
      *
      * @return the account request information
      */
     public String toString();
 }
