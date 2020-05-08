 package examples;
 
 import types.BasicCommand;
 import types.FlowData;
 import types.Predicate;
 
 public class ArithmeticExpression implements Predicate {
 
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
 		return "Arithmethic Expression";
 	}
 
 	@Override
 	public Class[] getInputTypes() {
		return new Class[]{int.class, String.class};
 	}
 	
 	@Override
 	public boolean execute(FlowData data) {
 		int intNumber = Integer.valueOf(number);
 		int intData = (Integer) data.getData();
 		return GeneralUtils.checkExpression(intData, intNumber, sign);
 	}
 }
