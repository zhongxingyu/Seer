 package VetPet;
 
 
 /*==========================================================================*/
 /** \file VetClinic.java
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
  \class VetClinic
  \brief main class for a single vet clinic
 
 */
 
 
 import java.util.ArrayList;
 import VetPet.Animals.*;
 
 public class VetClinic
 {
 	
 	public ArrayList<Animal> AnimalList = new ArrayList<Animal>();
	private String vetClinicName = null; 
	
     //---------------------------------------------------------------------------
     /**
        \brief constructor
        \param[in] Name the name of the vet clinic
      */
     public VetClinic(String Name)
     {
	vetClinicName = Name;
     }
     
     public boolean validID(String ID)
 	{
 		if(ID.length() != 6 ) return false;
 
 		for(int i=0; i < ID.length(); i++ )
 			if( !Character.isDigit(ID.charAt(i))) return false; 
 		
 		return true;		
 	}
     
                 
     //return true is dog has been added, false if the ID number is inncorect
      
     
     public boolean addDog(String ID,String type, String name,  String K9_family )
 	{
 
 		if(!validID(ID))
 			return false;
 		
 		Dog doggy = new Dog(ID, type, name, K9_family);
 		
 		AnimalList.add(doggy);
 		
 		return true;
 			
 	}
     
     
           	       
     //return true is cat has been added, false if the ID number is inncorect
      
     public boolean addCat(String ID, String type, String name, String toy )
     {
     	if(!validID(ID))
     		return false;
 
     	Cat kitty = new Cat(ID, type, name, toy);
 
     	AnimalList.add(kitty);
 	
     	return true;
     }
     //---------------------------------------------------------------------------
     public int numAnimals() { return numDogs + numCats; }
     public String getName() { return Name; }
     //---------------------------------------------------------------------------
     /**
        \brief lists all the animalsin the clinic
        \return the list of animals
      */
     public String listAll_animals()
     {
     	String catString = "";
 		String dogString = "";
 		
 		
 		for(int i=0;i< AnimalList.size(); i++)
 		{
 
 			if(AnimalList.get(i) instanceof Cat)
 				catString += AnimalList.get(i).toString();
 
 			if(AnimalList.get(i) instanceof Dog)
 				dogString += AnimalList.get(i).toString();
 		}
 
 		return("Animal: Cats\n" + catString + "\nAnimal: Dogs\n" + dogString);
     }
 
 	
    
     private Cat Catlist[] = new Cat[10]; /**< \brief list of all cats in the clinic */
     private Dog Doglist[] = new Dog[10]; /**< \brief list of all dogs in the clinic */
     private int numDogs = 0; /**< \brief number of dogs */
     private int numCats = 0; /**< \brief numberof cats */
     private String Name = ""; /**< \brief name of the clinic */
 }
