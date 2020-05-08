 package domain;
 
 public enum CustomerStatus {
 	OVERDUE_TO_MANY, OVERDUE, TO_MANY_BOOKS, OK;
 	
 	public String toString(){
 		switch (this){
 			case OVERDUE_TO_MANY:
			 	return "überfällig und zu viele Bücher";
 			case OVERDUE:
				return "überfällig und zu viele Bücher";
			case TO_MANY_BOOKS:
 				return "überfällig";
 			case OK:
 				return "OK";
 			default:
 				return "no returntype?!";
 		}
 	}
 }
