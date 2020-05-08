 package sql;
 
 import etc.Constants;
 
 public class InfoPackage {
 	private static java.util.Date d_start, d_end;
 	private static int [] _actions;
 	private static String name, email, gender, age, ethnicity;
 	
 	public InfoPackage () {
 		_actions = new int [500];
 		for(int i=0; i<500; i++) {
 			_actions[i] = 0;
 		}
 	}
 	
 	public void setDates(java.util.Date s, java.util.Date e) {
 		d_start = s;
 		d_end = e;
 	}
 	
 	public void setActions(int [] a) {//sends in action array. a[] is the actions[]
 		for(int i=0; i<500; i++) {
 			if(a[i] != Constants.DIR_LEFT && a[i] != Constants.DIR_RIGHT && a[i] != Constants.DIR_UP && a[i] != Constants.DIR_DOWN && a[i] != 0) {
 				System.out.printf("ERROR (IP): Unexpected action value!\n");
 			} else {
 				_actions[i] = a[i];
 			}
 		}
 	}
 	
 	public void setSurvey(String s_name, String s_email, String s_gender, String s_age, String s_ethnicity) {
 		if(s_name != null) {
 			name = s_name;
 		} else {
 			name = "ERROR: No good parameter";
 		}
 		if(s_email != null) {
 			email = s_email;
 		} else {
 			email = "ERROR: No good parameter";
 		}
 		if(s_gender != null) {
 			gender = s_gender;
 		} else {
 			gender = "ERROR: No good parameter";
 		}
 		if(s_age != null) {
 			age = s_age;
 		} else {
 			age = "---";
 		}
 		if(s_ethnicity != null) {
 			ethnicity = s_ethnicity;
 		} else {
 			ethnicity = "ERROR: No good parameter";
 		}
 	}
 	
 	public int [] getActions() {
 		return _actions;
 	}
 	
 	public java.util.Date getDate(int which) {
 		if(which == 1) {
 			return d_start;
 		} else {
 			return d_end;
 		}
 	}
 	
 	public String getSurvey(int which) {
 		switch(which) {
 		case 0:
 			return name;
 		case 1:
 			return email;
 		case 2:
 			return gender;
 		case 3:
 			return age;
 		}
 		
 		return "";
 	}
 }
