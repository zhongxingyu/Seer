 package predicates;
 
 import java.sql.Date;
 
 import types.FlowData;
 import types.Predicate;
 
 public class IsSameString implements Predicate{
 	
 	public String givenString;
 
 	public String getString() {
 		return givenString;
 	}
 
 	public void setString(String string) {
 		this.givenString = string;
 	}
 
 	@Override
 	public String getLabel() {
 		// TODO Auto-generated method stub
 		return "Is it the same string?";
 	}
 
 	@Override
 	public Class[] getInputTypes() {
 		return new Class[]{String.class};
 	}
 
 	@Override
 	public boolean execute(FlowData data) {
		String strData = data.toString();
 		return strData.equals(givenString);
 	}
 
 }
