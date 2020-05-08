 package examples;
 
 import java.util.ArrayList;
 
 import types.FlowData;
 import types.Predicate;
 
 public class RecordsNumFromQuery implements Predicate {
 
 	private String number;
 	private String sign;
 	
 	public String getSign() {
 		return sign;
 	}
 
 	public void setSign(String sign) {
 		this.sign = sign;
 	}
 
 	public String getNumber() {
 		return number;
 	}
 
 	public void setNumber(String number) {
 		this.number = number;
 	}
 
 	@Override
 	public String getLabel() {
 		return "Number of returned records from query";
 	}
 
 	@Override
 	public Class[] getInputTypes() {
 		return new Class[]{ArrayList.class};
 	}
	
	public Class[] getOutputTypes() {
		return new Class[]{int.class};
	}
 
 	@Override
 	public boolean execute(FlowData data) {
 		ArrayList<Object> arrData = new ArrayList<Object>();
 		arrData = (ArrayList<Object>) data.getData();
 		int intNumber = Integer.valueOf(number);
 		int numOfRows = arrData.size();
 		return GeneralUtils.checkExpression(numOfRows, intNumber, sign);
 	}
 
 }
