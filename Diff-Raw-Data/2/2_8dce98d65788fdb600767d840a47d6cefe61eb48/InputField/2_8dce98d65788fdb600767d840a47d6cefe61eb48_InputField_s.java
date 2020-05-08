 /**
  * 
  */
 package pl.dmcs.whatsupdoc.client.fields;
 
 import java.util.Arrays;
 
 import pl.dmcs.whatsupdoc.shared.FieldVerifier;
 
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 
 /**
  * 05-11-2012
  * @author Jakub Jeleński, jjelenski90@gmail.com
  * 
  * 
  */
 public class InputField extends Field implements FieldConstraint {
 
 	private InputFieldType myType;
 	private Label title, error;
 	private TextBox input;
 	
 	/**
 	 * @param title - label for field
 	 * @param type - what type is this input field
 	 */
 	public InputField(String title, InputFieldType type){
 		super(3, Arrays.asList(new String[]{"firstColumn", "secondColumn", "thirdColumn"}));
 		this.title = new Label(title);
 		this.title.setStyleName("title");
 		this.myType = type;
 		if(myType==InputFieldType.PASSWORD_BOX){
 			this.input = new PasswordTextBox();
 		}else{
 			this.input = new TextBox();
 		}
 		this.input.setStyleName("textBox");
 		this.error = new Label();
 		this.error.setStyleName("error");
 		
 		subDiv.get(0).add(this.title);
 		subDiv.get(1).add(this.input);
 		subDiv.get(2).add(this.error);
 	}
 	
 	/* (non-Javadoc)
 	 * @see pl.dmcs.whatsupdoc.client.fields.FieldConstraint#checkConstraint()
 	 */
 	@Override
 	public boolean checkConstraint() {
 		error.setText("");
 		switch (this.myType) {
 			case PASSWORD_BOX:
 				if(FieldVerifier.isValidPassword(this.input.getText())){
 					return true;
 				}else{
 					error.setText("Hasło musi być dłuższe niż 4 znaki i nie może zawierać spacji!");
 					return false;
 				}
 				
 			case TEXT_BOX:
 				if(FieldVerifier.isValidName(this.input.getText())){
 					return true;
 				}else{
 					error.setText("Imie oraz nazwisko musi być dłuższe niż 3 znaki!");
 					return false;
 				}
 				
 			case PESEL_BOX:
 				if(FieldVerifier.isValidPESEL(this.input.getText())){
 					return true;
 				}else{
 					error.setText("PESEL musi się skladać z tylko jedenastu cyfr!");
 					return false;
 				}
 				
 			case PHONE_BOX:
 				if(FieldVerifier.isValidPhone(this.input.getText())){
 					return true;
 				}else{
 					error.setText("Numer telefonu składa się z 9 cyfr!");
 				}
 				
 			case EMAIL:
 				if(FieldVerifier.isValidEmail(this.input.getText())){
 					return true;
 				}else{
 					error.setText("Podany email ma nieprawidłową składnię!");
 					return false;
 				}
 				
 			case CITY:
 				if(FieldVerifier.isValidCity(this.input.getText())){
 					return true;
 				}else{
 					error.setText("Nazwa miasta musi być dłuższa niż 3 znaki oraz nie może zawierać spacji!");
 					return false;
 				}
 				
 			case STREET:
 				if(FieldVerifier.isValidStreet(this.input.getText())){
 					return true;
 				}else{
 					error.setText("Nazwa ulicy musi być dłuższa niż 4 znaki!");
 					return false;
 				}
 				
 			case HOUSE_NR:
				if(FieldVerifier.isValidEmail(this.input.getText())){
 					return true;
 				}else{
 					error.setText("Musisz podać numer mieszkania!");
 					return false;
 				}
 	
 			default:
 				return false;
 			
 		}
 
 	}
 	
 	/* (non-Javadoc)
 	 * @see pl.dmcs.whatsupdoc.client.fields.Field#clear()
 	 */
 	@Override
 	public void clear(){
 		error.setText("");
 		input.setText("");
 	}
 
 	/* (non-Javadoc)
 	 * @see pl.dmcs.whatsupdoc.client.fields.Field#getValue()
 	 */
 	@Override
 	public String getValue() {
 		return this.input.getText();
 	}
 
 }
