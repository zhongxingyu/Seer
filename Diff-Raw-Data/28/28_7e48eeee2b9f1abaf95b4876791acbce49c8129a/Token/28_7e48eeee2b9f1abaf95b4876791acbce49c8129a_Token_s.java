 package htmlscanner;
 
 public class Token {
 	TokenType code;
 	String value;
 	public Token(){
 		code=null;
 		value=null;
 	}
 	public TokenType getCode() {
 		return code;	
 	}
 	public int getIntCode(){
 		return code.getIntCode();
 	}
 	public void setCode(TokenType code) {
 		this.code = code;
 	}
 	public String getValue() {
 		return value;
 	}
 	public void setValue(String value) {
 		this.value = value;
 	}
 	@Override
 	public String toString() {
		return "Token [code=" + code + " ("+code.getIntCode()+"), value='" + value + "']";
 	}
 	public Token(TokenType code, String value) {
 		this.code = code;
 		this.value = value;
 	}
 	public Token(TokenType code) {
 		this(code,null);
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((code == null) ? 0 : code.hashCode());
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Token other = (Token) obj;
 		if (code == null) {
 			if (other.code != null)
 				return false;
 		} else if (!code.equals(other.code))
 			return false;
 		if (value == null) {
 			if (other.value != null)
 				return false;
 		} else if (!value.equals(other.value))
 			return false;
 		return true;
 	}
 	String prettyPrint(){
 		return String.format("(%s, %s)",code.getIntCode(),(value != null ? value : ""));
 	}
 	
 }
