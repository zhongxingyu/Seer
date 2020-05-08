 package SWE437.Jenkins_Test;
 /**
  * This immutable class holds a name and a favorite number for a student
  * 
  * @author James Robertson
  *
  */
 
 
 public class Student {
 
 		private String name;
 		private int favoriteNumber;
 		
 		
 		/**
 		 * @author James Robertson
 		 * 
 		 * @param name							The name of the student
 		 * @param favoriteNumber				The favorite number of the student
 		 * @throws IllegalArgumentException 	if name is "" or favorite number <0
 		 * @throws NullPointerException 		if name is null
 		 */
 		public Student(String name, int favoriteNumber){
 			if(name==null)
 				throw new NullPointerException();
 			if(name.equals("") || favoriteNumber<0)
 				throw new IllegalArgumentException();
 			this.name = name;
 			this.favoriteNumber = favoriteNumber;
 		}
 		
 		public String getName() {
 			return name;
 		}
 
 		public int getFavoriteNumber() {
			return FavoriteNumber;
 		}
 
 		@Override
 		public String toString(){
 			return name+" "+favoriteNumber;
 		}
 		
 		@Override
 		public boolean equals(Object o){
 			if(!(o instanceof Student))
 				return false;
 			Student s = (Student) o;
 			if(s.getName().equals(this.getName()) && s.getFavoriteNumber()==this.favoriteNumber)
 				return true;
 			return false;
 		}
 		
 		@Override
 		public int hashCode(){
 			return name.hashCode()+favoriteNumber*47;
 		}
 }
