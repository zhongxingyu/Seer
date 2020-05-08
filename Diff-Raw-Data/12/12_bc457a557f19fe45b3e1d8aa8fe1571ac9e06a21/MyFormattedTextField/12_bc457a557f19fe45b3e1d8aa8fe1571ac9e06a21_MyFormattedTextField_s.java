 package cx.ath.jbzdak.jpaGui.ui.formatted;
 
 import static cx.ath.jbzdak.jpaGui.Utils.makeLogger;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.Document;
 import org.slf4j.Logger;
 
 import java.awt.Color;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.io.Serializable;
 
 public class MyFormattedTextField extends JTextField{
 
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger log  = makeLogger();
 
 
 	private MyFormatter formatter;
 
 	/**
 	 * Prawdziwe jeśli tekst odpowiada wartości w polu.
 	 */
 	private boolean valueCurrent;
 
 	private boolean ignoreSetText;
 
    private boolean parsingText;
 
 	private Object value;
 
 	private TextListener listener;
 
 	private Exception parseResults;
 
 	private String userEnteredText;
 
 	private boolean clearTextField;
 
 	private boolean separateuserTextAndValue= true;
 
 	private boolean echoErrorsAtOnce = false;
 
 	public MyFormattedTextField() {
 		super();
 		addFocusListener(new FocusListener());
 		setColumns(6);
 	}
 
 	public MyFormattedTextField(MyFormatter formatter) {
 		this();
 		setFormatter(formatter);
 	}
 
 	@Override
 	public void setDocument(Document doc) {
 		if(getDocument()!=null){
 			getDocument().removeDocumentListener(getListener());
 		}
 		super.setDocument(doc);
 		doc.addDocumentListener(getListener());
 	}
 
 	TextListener getListener() {
 		if(listener == null){
 			listener = new TextListener();
 		}
 		return listener;
 	}
 
    public Exception getParseResults() {
 		return parseResults;
 	}
 
 	void setParseResults(Exception parseResults) {
 		Exception oldErrors = this.parseResults;
 		this.parseResults = parseResults;
 		firePropertyChange("parseResults", oldErrors, parseResults);
 		firePropertyChange("hadParseErrors", oldErrors!=null, parseResults!=null);
 
 	}
 
 	public Object getValue() {
 		return value;
 	}
 
 	public void setValue(Object value) {
 		Object oldValue = this.value;
 		this.value = value;
 		firePropertyChange("value", oldValue, this.value);
        setParseResults(null);
		//if(StringUtils.isEmpty(getText())){
			formatValue();
		//}
 	}
 
 	public void setValueFromBean(Object value) {
 		setValue(value);
 		formatValue();
 		userEnteredText = getText();
 	}
 
 	public boolean isValueCurrent() {
 		return valueCurrent;
 	}
 
 	void setValueCurrent(boolean valueCurrent) {
 		boolean old = this.valueCurrent;
 		this.valueCurrent = valueCurrent;
 		firePropertyChange("valueCurrent", old, this.valueCurrent);
 	}
 
 	public  void formatValue(){
 		ignoreSetText = true;
 		try{
          if(!parsingText){
 			   setText(formatter.formatValue(getValue()));
          }
 		} catch (FormattingException e) {
          setParseResults(e);
 		}finally{
 			ignoreSetText = false;
 		}
 	}
 
 	public String getUserEnteredText() {
 		return userEnteredText;
 	}
 
 	public void setUserEnteredText(String userEnteredText) {
 		String oldText = this.userEnteredText;
 		this.userEnteredText = userEnteredText;
 		firePropertyChange("userEnteredText", oldText, this.userEnteredText);
 	}
 
	void attemptParseText(){
       parsingText = true;
 		try {
			setValue(formatter.parseValue(getText()));
 			setValueCurrent(true);
 			setParseResults(null);
 			setBackground(UIManager.getColor("TextField.background"));
 		} catch (Exception e) {
 			setParseResults(e);
 			setValueCurrent(false);
 			if(echoErrorsAtOnce){
 				setBackground(Color.PINK);
 			}
 		}finally {
          parsingText = false; 
       }
 	}
 
 	private void onTextChange(){
 		if(!ignoreSetText){
 			setUserEnteredText(getText());
 			attemptParseText();
 		}
 	}
 
 	private class TextListener implements DocumentListener, Serializable{
 		private static final long serialVersionUID = 1L;
 		@Override
 		public void changedUpdate(DocumentEvent e) { onTextChange(); }
 		@Override
 		public void insertUpdate(DocumentEvent e) { onTextChange();	}
 		@Override
 		public void removeUpdate(DocumentEvent e) {	onTextChange();	}
 	}
 
 	private class FocusListener extends FocusAdapter{
 		private static final long serialVersionUID = 1L;
 		@Override
 		public void focusGained(FocusEvent e) {
 			if(separateuserTextAndValue){
 				setText(userEnteredText);
 			}
 			setBackground(UIManager.getColor("TextField.background"));
 		}
 
 		@Override
 		public void focusLost(FocusEvent e) {
 			if(separateuserTextAndValue){
 				formatValue();
 			}
 			if(parseResults!=null){
 				setBackground(Color.PINK);
 			}
 		}
 	}
 
 	public void clear(){
 		if(clearTextField){
 			setUserEnteredText("");
 			setText("");
 		}
 	}
 
 	public boolean getHadParseErrors(){
 		return parseResults!=null;
 	}
 
 	public boolean isClearTextField() {
 		return clearTextField;
 	}
 
 	public void setClearTextField(boolean clearTextField) {
 		this.clearTextField = clearTextField;
 	}
 
 	public MyFormatter getFormatter() {
 		return formatter;
 	}
 
 	public void setFormatter(MyFormatter formatter) {
       boolean oldEcho = echoErrorsAtOnce;
 		this.formatter = formatter;
       try{
          echoErrorsAtOnce = false;
          attemptParseText();
       }finally {
          echoErrorsAtOnce = oldEcho;
       }
 	}
 
 	public boolean isSeparateuserTextAndValue() {
 		return separateuserTextAndValue;
 	}
 
 	public void setSeparateuserTextAndValue(boolean separateuserTextAndValue) {
 		this.separateuserTextAndValue = separateuserTextAndValue;
 	}
 
 	public boolean isEchoErrorsAtOnce() {
 		return echoErrorsAtOnce;
 	}
 
 	public void setEchoErrorsAtOnce(boolean echoErrorsAtOnce) {
 		this.echoErrorsAtOnce = echoErrorsAtOnce;
 	}
 
 
 }
