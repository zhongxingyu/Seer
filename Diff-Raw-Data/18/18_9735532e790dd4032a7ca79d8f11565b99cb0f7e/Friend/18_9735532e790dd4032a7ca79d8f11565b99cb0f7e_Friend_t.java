 /*
  * Project: FriendCounter - Linked List
  * File: Friend.java
  * @author Zachary Pratt Gibbs
  * Date: 18 Feb 2013
  * 
  * Description: This is a friend class to be used in the Friend Counter program.
  */
 package friendcounter;
 //Needs to change constructors and set methods to check for valid entry.
 
 /**
  *
  * @author zachary
  */
 public class Friend 
 {
     private String name;
     private int age;// Age in years -1 for Unknown
     private boolean ageIsApprox;
     private int gender; // 0 = Male, 1 = Female, -1 = Unknown
     
     private int quality; //Quality of friendship. Scale from -5 to 5 where 0 is 
                            //neutral, -5 is sworn Enemy and 5 is like a brother
                            //from another mother.
     
     private int cuteness; //Cuteness. -5 to 5. 0 is the normal, -5 is looks like
                             //a monster and 5 is having an angelic beauty
     
     private int relationship;//0 is single, 1 is in a relationship, 2 is married 
                             //and -1 is unknown.
     private boolean isFamily;
     /**
      * Default constructor Makes an unknown friend
      */
     public Friend()
     {
         this.name = "Unknown";
         this.age = -1;
         this.ageIsApprox = false;
         this.gender = -1;
         this.quality = 0;
         this.cuteness = 0;
         this.relationship = -1;
         this.isFamily = false;
     }
     /**
      * Constructor if Only the Name is known
      * @param name 
      */
     public Friend(String name)
     {
         this.name = name;
         this.age = -1;
         this.ageIsApprox = false;
         this.gender = -1;
         this.quality = 0;
         this.cuteness = 0;
         this.relationship = -1;
         this.isFamily = false;
     }
     public Friend(String name, int age, boolean ageIsApprox)
     {
		if(age < -1)
		{
			throw new InvalidParameterException;
		}
         this.name = name;
         this.age = age;
         this.ageIsApprox = ageIsApprox;
         this.gender = -1;
         this.quality = 0;
         this.cuteness = 0;
         this.relationship = -1;
         this.isFamily = false;
     }
     public Friend(String name, int age, boolean ageIsApprox, int gender)
     {
		if(age < -1 ¦¦ gender < -1 ¦¦ gender > 1)
		{
			throw new InvalidParameterException;
		}
         this.name = name;
         this.age = age;
         this.ageIsApprox = ageIsApprox;
         this.gender = gender;
         this.quality = 0;
         this.cuteness = 0;
         this.relationship = -1;
         this.isFamily = false;
     }
     public Friend(String name, int age, boolean ageIsApprox, int gender, 
             int quality, int cuteness)
     {
    	if(age < -1 ¦¦ gender < -1 ¦¦ gender > 1 ¦¦ quality < -5 ¦¦ quality > 5 ¦¦ cuteness < -5 ¦¦ cuteness > 5)
    	{
        	throw new InvalidParameterException;
    	}
         this.name = name;
         this.age = age;
         this.ageIsApprox = ageIsApprox;
         this.gender = gender;
         this.quality = quality;
         this.cuteness = cuteness;
         this.relationship = -1;
         this.isFamily = false;
     }
     public Friend(String name, int age, boolean ageIsApprox, int gender, 
             int quality, int cuteness, int relationship)
     {
         this.name = name;
         this.age = age;
         this.ageIsApprox = ageIsApprox;
         this.gender = gender;
         this.quality = quality;
         this.cuteness = cuteness;
         this.relationship = relationship;
         this.isFamily = false;
     }
     public String getName()
     {
         return name;
     }
     public void setName(String name)
     {
         this.name = name;
     }
     public int getAge()
     {
         return age;
     }
     public boolean getAgeIsApprox()
     {
         return ageIsApprox;
     }
     public void setAge(int age)
     {
         this.age = age;
     }
     public void setAge(int age, boolean ageIsApprox)
     {
         this.age = age;
         this.ageIsApprox = ageIsApprox;
     }
     public int getGender()
     {
         return gender;
     }
     public void setGender(int gender)
     {
         this.gender = gender;
     }
     public int getQuality()
     {
         return quality;
     }
     public void setQuality(int quality)
     {
         this.quality = quality;
     }
     public int getCuteness()
     {
         return cuteness;
     }
     public void setCuteness(int cuteness)
     {
         this.cuteness = cuteness;
     }
     public int getRelationship()
     {
         return relationship;
     }
     public void setRelationship(int relationship)
     {
         this.relationship = relationship;
     }
     public boolean isFamily()
     {
         return isFamily;
     }
     public void setFamily(boolean isFamily)
     {
         this.isFamily = isFamily;
     }
     @Override
     public String toString()
     {
         String friend = "";
         friend = friend + "Name: " + name + "/n";
         friend = friend + "Age: ";
         if(age == -1)
         {
             friend = friend + "Unknown/n";
         }
         else
         {
             if(ageIsApprox)
             {
                 friend = friend + " approx/n";
             }
          else
             {
                 friend = friend + "/n";
             }
         }
         friend = friend + "Gender: ";
         switch(this.gender)
         {
             case -1:
                 friend = friend + "Unknown";
                 break;
             case 0:
                 friend = friend + "Male";
                 break;
             case 1:
                 friend = friend + "Female";
                 break;
         }
         friend = friend + "/n";
         friend = friend + "Quality: " + quality;
         friend = friend + "Cuteness Level: " + cuteness;
         friend = friend + "Relationship Status: ";
         switch(relationship)
         {
             case -1:
                 friend = friend + "Unknown";
                 break;
             case 0:
                 friend = friend + "Single";
                 break;
             case 1:
                 friend = friend + "In a relationship";
                 break;
             case 2:
                 friend = friend + "Married";
                 break;
         }
         friend = friend + "/n";
         if(isFamily)
         {
             friend = friend + "Is family/n";
         }
         else
         {
             friend = friend + "Not Family/n";
         }
         return friend;
     }
}
