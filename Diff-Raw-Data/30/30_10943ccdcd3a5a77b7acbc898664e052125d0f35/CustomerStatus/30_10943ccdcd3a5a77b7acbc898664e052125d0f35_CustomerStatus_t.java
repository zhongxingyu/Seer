 package domain;
 
 public enum CustomerStatus {
 	OVERDUE_TO_MANY, OVERDUE, TO_MANY_BOOKS, OK;
 	
 	public String toString(){
 		switch (this){
 			case OVERDUE_TO_MANY:
			 	return "überfällig und zu viel ausgeliehene Bücher";
 			case OVERDUE:
 				return "überfällig";
			case TO_MANY_BOOKS:
				return "zu viel ausgeliehene Bücher";
 			case OK:
 				return "OK";
 			default:
 				return "no returntype?!";
 		}
 	}
 }
