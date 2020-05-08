 package de.egore911.aspparser;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.antlr.runtime.Parser;
 import org.antlr.runtime.RecognizerSharedState;
 import org.antlr.runtime.TokenStream;
 
 public class WarningAwareParser extends Parser {
 
 	public WarningAwareParser(TokenStream input) {
 		super(input);
 	}
 
 	public WarningAwareParser(TokenStream input, RecognizerSharedState state) {
 		super(input, state);
 	}
 
 	private List<String> errorMessages = new ArrayList<>(0);
 	private List<String> warningMessages = new ArrayList<>(0);
 
 	public void emitErrorMessage(String msg) {
 		errorMessages.add(msg);
 	}
 
 	public void setErrorMessages(List<String> errorMessages) {
 		this.errorMessages = errorMessages;
 	}
 	
 	public List<String> getErrorMessages() {
 		return errorMessages;
 	}
 
 	public void emitWarningMessage(String msg) {
 		warningMessages.add(msg);
 	}
 
 	public void setWarningMessages(List<String> warningMessages) {
 		this.warningMessages = warningMessages;
 	}
 	
 	public List<String> getWarningMessages() {
 		return warningMessages;
 	}
 
 }
