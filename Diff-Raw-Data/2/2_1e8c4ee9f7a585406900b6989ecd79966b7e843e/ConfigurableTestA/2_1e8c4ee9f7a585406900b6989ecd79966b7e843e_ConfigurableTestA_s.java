 package game.configuration;
 
 
 public class ConfigurableTestA extends Configurable {
 	
 	public String optionA1;
 	
 	public String optionA2;
 	
 	public String optionA3;
 	
 	public ConfigurableTestB optionA4;
 	
 	public ConfigurableTestC optionA5;
 	
 	private class StringLengthCheck implements ErrorCheck<String> {
 		
 		private int minimumLength;
 		
 		public StringLengthCheck(int minimumLength) {
 			this.minimumLength = minimumLength;
 		}
 		
 		@Override
 		public String getError(String value) {
 			if (value.length() < minimumLength)
 				return "should have at least " + minimumLength + " characters";
 			else
 				return null;
 		}
 
 	}
 	
 	public ConfigurableTestA() {
 		addOptionBinding("optionA1",			"optionA4.optionB1", "optionA5.optionC1");
 		addOptionBinding("optionA2",			"optionA4.optionB2");
 		addOptionBinding("optionA4.optionB3",	"optionA5.optionC2");
 		
		addOptionCheck("optionA3", new StringLengthCheck(20));
 	}
 
 }
