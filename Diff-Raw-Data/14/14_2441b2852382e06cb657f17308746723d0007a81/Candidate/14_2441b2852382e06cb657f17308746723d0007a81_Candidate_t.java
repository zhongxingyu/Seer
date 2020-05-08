// $Id: Candidate.java,v 1.9 2000/06/05 11:38:17 loeffel Exp $
 
 package jobmatch.business.candidate;
 
 import jobmatch.business.entity.*;
 import jobmatch.data.*;
 import jobmatch.business.candidate.cv.*;
 import java.util.*;
 
 /**
  *  Candidate Business Object
  *  Inherites the methods from CandidateBDO, who provides
  *  access to the database
  *
  *  @since May 4 2000
 *  @author $Author: loeffel $
 *  @version $Revision: 1.9 $
  **/
 public class Candidate extends CandidateBDO {
     
     public Candidate() throws Exception {
 	super();
     }
 
     public Candidate(CandidateDO dataObject) {
 	super(dataObject);
     }
 
     public List getAllFormations() {
 	return Formation.getAllFormationsFor(this);
     }
 
     public boolean isMale() {
 	try {
 	    return "m".equals(this.getSex());
 	}catch (Exception err){
 	    throw new RuntimeException(err.toString());
 	}
     }
 
     /**
      * Returns the candidates nationality
      **/
     public Country getNationalityBO(){
 	try {
 	    return new Country(this.getNationality());
 	} catch (Exception err) {
 	    throw new RuntimeException(err.toString());
 	}
     }
 
     /**
      * Returns the candidates address
      **/
     public Address getAddressBO(){
 	try {
 	    AdressDO data = this.getAdress();
 	    if (data == null) {
 		data = AdressDO.createVirgin();
 		data.commit();
		this.setAdress(data);
		this.commit();
 	    }
 	    return new Address(data);
 	} catch (Exception err){
 	    throw new RuntimeException(err.toString());
 	}
     }  
 	    
 	    
     
     /** @see Object.equals **/
     public boolean equals(Object other) {
 	if (other == null) {
 	    return false;
 	}
 	
 	try {
 	    return semanticEquality(this, (Candidate) other);
 	}
 	catch (ClassCastException e) {
 	    return false;
 	}
     }
 
     /** @return true if a is considered the same as b **/
     private static boolean semanticEquality(Candidate a, Candidate b) {
 	//XXX define the equality and remove the exception !!!
 	throw new RuntimeException("semanticEqualitiy not defined");
 	//return (a == b);
     }
 
     /** @see Object.toString **/
     public String toString() {
 	return super.toString();
     }
        
 } //class
 
 
 /*
  * $Log: Candidate.java,v $
 * Revision 1.9  2000/06/05 11:38:17  loeffel
 * Fixed bug which did not set the link to the address
 *
  * Revision 1.8  2000/06/04 11:50:41  locher
  * many little fixes
  *
  * Revision 1.7  2000/06/02 16:02:29  locher
  * introduced address entities
  *
  * Revision 1.6  2000/05/31 12:15:44  studer
  * Javadoc added
  *
  * Revision 1.5  2000/05/30 12:47:52  studer
  * DropDown wird jetzt via DB abefuellt
  *
  * Revision 1.4  2000/05/26 11:54:53  locher
  * Fromation object and query skeleton in Candidate
  *
  * Revision 1.3  2000/05/23 14:10:53  locher
  * authentification mechanisms
  *
  * Revision 1.2  2000/05/19 10:59:29  locher
  * matcher and mailer service including test skeletons
  *
  * Revision 1.1  2000/05/04 14:21:24  locher
  * makefiles
  *
  * Revision 1.1.1.1  2000/04/01 14:04:42  locher
  *
  */
