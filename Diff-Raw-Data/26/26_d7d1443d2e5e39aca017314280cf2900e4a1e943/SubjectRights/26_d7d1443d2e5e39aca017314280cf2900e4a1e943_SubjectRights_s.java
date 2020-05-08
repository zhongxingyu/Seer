 package model.userRights;
 
 public class SubjectRights extends ContentRights {
 	private int subjectID;
 
 	public int getSubjectID() {
 		return subjectID;
 	}
 
 	public void setSubjectID(int subjectID) {
 		this.subjectID = subjectID;
 	} 
 	
 	public String toValueNames() {
 		String valueNames = super.toValueNames() + ", subjectID";
 		return valueNames;
 	}
 	
	public String toValueStrings() {
 		String values = super.toValues() + ", "+subjectID;
 		return values;
 	}
 	
 	public String[] toValueNamesArray() {
 		String[] valueNames = super.toValueNamesArray();
 		valueNames[4] = "subjectID";
 		return valueNames;
 	}
 	
 	public String[] toValuesArray() {
 		String[] values = super.toValuesArray();
 		values[4] = "" + subjectID;
 		return values;
 	}
 }
