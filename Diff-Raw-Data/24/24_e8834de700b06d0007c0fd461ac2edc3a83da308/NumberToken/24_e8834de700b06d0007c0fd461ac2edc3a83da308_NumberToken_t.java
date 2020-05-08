 package eleven;
 
 public class NumberToken extends Token implements IToken {
 
 	private final String description = "Number";
	private int oppositeNumber;
	
 	public NumberToken(String substring) {	
 		this.setStringValue(substring);
		int opposite = Integer.parseInt(substring)*-1;
		setOppositeNumber(opposite);
 	}
 	
 	//StringValue
 	public void setStringValue(String substring) {
 		super.setStringValue(substring.toLowerCase());
 	}
 	public String getStringValue() {
 		return super.getStringValue();
 	}
 	public int getInteger() {
 		return Integer.parseInt(super.getStringValue());	
 	}
 	public String getDescription() {
 		return this.description;
 	}
 	public int toInt() {
 		return this.getInteger();
 	}

	public int getOppositeNumber() {
		return oppositeNumber;
	}

	public void setOppositeNumber(int oppositeNumber) {
		this.oppositeNumber = oppositeNumber;
	}
 	
 }
 
