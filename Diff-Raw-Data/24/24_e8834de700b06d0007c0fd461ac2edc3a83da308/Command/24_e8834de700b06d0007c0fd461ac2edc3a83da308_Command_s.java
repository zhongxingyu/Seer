 package eleven;
 
 public class Command {
 
 	private Token currentToken;
 	private Token prevNum1;
 	private Token prevNum2;
 	private Token oppositeToken;
 	private Token oppositeNum1;
 	private Token oppositeNum2;
 	private	Command oppositeCommand;
 	
 	public Command(Token token) {
 		setPrevToken(token);
 		if (token instanceof AddHouseWordToken) {
 			setOppositeToken(new RemoveHouseWordToken("rm"));
 		} else if (token instanceof RemoveHouseWordToken) {
 			setOppositeToken(new AddHouseWordToken("ah"));
 		} else if (token instanceof AnimateWordToken) {
 			setOppositeToken(new AnimateWordToken("am"));
 		}
 //		TODO HOW TO prevent cyclical error
 //		setOppositeCommand(new Command(getOppositeToken()));
 	}
 	public Command(Token token, Token num1, Token num2) {
 		setPrevToken(token);
 		setOppositeToken(token);
 		setPrevNum1(num1);
 		setPrevNum2(num2);
		setOppositeNum1( new NumberToken( Integer.toString( (((NumberToken) num1 ).toInt())*(-1))) );
		setOppositeNum2( new NumberToken( Integer.toString( (((NumberToken) num2 ).toInt())*(-1))) );
 		setOppositeCommand(new Command(getOppositeToken(), getOppositeNum1(), getOppositeNum2()));
 	}
 
 	public Token getOppositeNum1() {
 		return this.oppositeNum1;
 	}
 	public void setOppositeNum1(Token int1) {
 		this.oppositeNum1 = int1;
 	}
 	public Token getOppositeNum2() {
 		return this.oppositeNum2;
 	}
 	public void setOppositeNum2(Token int2) {
 		this.oppositeNum2 = int2;
 	}
 	public Token getCurrentToken() {
 		return this.currentToken;
 	}
 	public void setPrevToken(Token token) {
 		this.currentToken = token;
 	}
 	public Token getOppositeToken() {
 		return this.oppositeToken;
 	}
 	public void setOppositeToken(Token oppositeToken) {
 		this.oppositeToken = oppositeToken;
 	}
 	public Token getPrevNum2() {
 		return prevNum2;
 	}
 	public void setPrevNum2(Token prevNum2) {
 		this.prevNum2 = prevNum2;
 	}
 	public Token getPrevNum1() {
 		return prevNum1;
 	}
 	public void setPrevNum1(Token prevNum1) {
 		this.prevNum1 = prevNum1;
 	}
 	public Command getOppositeCommand() {
 		return oppositeCommand;
 	}
 	public void setOppositeCommand(Command oppositeCommand) {
 		this.oppositeCommand = oppositeCommand;
 	}
 }
