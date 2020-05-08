 package VetPet;
 
 
 /*==========================================================================*/
 /** \file VetManager.java
     \brief 
     \author Jordan James Kidney
     \date Sept 25, 2005
 
     
 */
 /* 
    Location: University of Calgary, Alberta, Canada
    Created on:    Sept 25, 2005
    Last Updated   Sept 25, 2005
 ========================================================================*/
 
 /**
  \class VetManager
  \brief used to maintain a group of vets
 */
 
 
 
 import java.util.Hashtable;
 
 public class VetManager
 {
 
     //----------------------------------------------------------------------------
     /**
        \brief adds a new vet clinic to the manager
        
        \param[in] Name the name of the clinic
        
        \exception IllegalArgumentException if Name already exists in the Manager
        \exception NullPointerException if Name is null
      */
     public void addVetClinic(String Name) throws IllegalArgumentException,
 						 NullPointerException
     {
 	if(Name == null)
 	    throw new NullPointerException("Vet Clinic nameis null");
 	
 
	if( listOfClinics.containsKey(Name) )
 	    throw new IllegalArgumentException("Vet clinic name already exists");
     
 	VetClinic vet = new VetClinic(Name);
        
	listOfClinics.put(Name, vet );
     }
     //----------------------------------------------------------------------------
     /**
        \brief returns the clinic object based upon the input name
        
        \param[in] Name the name of the clinic
        
        \return the clinic object if found
        \exception IllegalArgumentException clinic not in the manager
      */
     public VetClinic getVetClinic(String Name)
     {
 	if(Name == null)
 	    throw new NullPointerException("Vet Clinic nameis null");
 	
 
	if( !listOfClinics.containsKey(Name) )
 	    throw new IllegalArgumentException("Vet clinic '"+ Name +"' does not exist in the manager");
     
 	
	return (VetClinic) listOfClinics.get(Name);
 
     }
     //----------------------------------------------------------------------------
     /**
        \brief returns a string with the all the vet names listed on seperate lines 
        \return the list of vet clinic
     */
     public String list_all_vet_clinics()
     {
 	String ret ="";
 	
	java.util.Enumeration e = listOfClinics.elements();
 	
 	while(e.hasMoreElements())
 	    {
 		VetClinic vet = (VetClinic) e.nextElement();
 		ret += vet.getName() + "\n";
 	    }
 
 	return ret;
     }
     //----------------------------------------------------------------------------
     /**
        \brief returns a string with the all the animalsin all the vets 
        \return the list of animals
     */
     public String list_all_animals()
     {
 	String ret ="";
 
	java.util.Enumeration e = listOfClinics.elements();
 
 	while(e.hasMoreElements())
 	    {
 		VetClinic vet = (VetClinic) e.nextElement();
 
 		ret += "============================================================\n";
 		ret += "VetClinic: " + vet.getName() + "\n";
 		ret += vet.listAll_animals();
 		
 	    }
 
 	return ret;
     }
 
 
    private Hashtable listOfClinics = new Hashtable(); /**< \brief main list of vet clinic*/
 }
