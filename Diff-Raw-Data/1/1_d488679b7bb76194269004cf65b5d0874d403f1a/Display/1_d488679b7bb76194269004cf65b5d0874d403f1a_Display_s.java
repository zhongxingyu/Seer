 package calculator.model;
 
 import javax.swing.event.EventListenerList;
 
 import calculator.model.events.DisplayChangedEvent;
 import calculator.model.events.DisplayEventListener;
 
 public class Display {
 
 	private static final String YOU_DO_NOT_RESPECT_THE_WORKFLOW = "YOU DO NOT RESPECT THE WORKFLOW";
 	private static final String DEZIMAL_POINT = ".";
 	private static final String INITIAL_VALUE = "0";
 	private String content;
 	private EventListenerList listeners = new EventListenerList();
 	private boolean isErrorState = false;
 
 	public Display() {
 		clear();
 	}
 
 	public void clear() {
 		setContent(INITIAL_VALUE);
 	}
 
 	public void addDigit(int digit) {
 		addContent(String.valueOf(digit));		
 	}
 
 	private void setContent(String content) {
 		this.content = content;
 		raiseDisplayChangedEvent(new DisplayChangedEvent(this, this));
 	}
 
 	public void addContent(String suffix) {
 		isErrorState = false;
 		if(isInitialDisplay() && !isDezimalPoint(suffix)) {
 			setContent(suffix);
 		} else {
 			setContent(content + suffix);
 		}
 	}
 
 	public boolean isDezimalPoint(String suffix) {
 		return suffix.equals(DEZIMAL_POINT);
 	}
 
 	public boolean isInitialDisplay() {
 		return content.length() <= 1 && content.startsWith(INITIAL_VALUE);
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setDisplay(Double number) {
 		isErrorState = false;
 		if(isInteger(number)) {
 			setContent(String.valueOf(number.intValue()));
 		} else {		
 			setContent(String.valueOf(number));
 		}
 	}
 
 	private boolean isInteger(Double number) {
 		return number.intValue() == number;
 	}
 
 	public void addListener(DisplayEventListener listener) {
 		listeners .add(DisplayEventListener.class, listener);
 	}
 
 	public void removeListener(DisplayEventListener listener) {
 		listeners.remove(DisplayEventListener.class, listener);
 	}
 
 	private void raiseDisplayChangedEvent(DisplayChangedEvent event) {
 		for (DisplayEventListener listener : listeners.getListeners(DisplayEventListener.class)) {
 			listener.onDisplayChanged(event);
 		}
 	}
 
 	public double getNumber() {
 		return Double.valueOf(content);
 	}
 
 	public void error() {
 		isErrorState = true;
 		setContent(YOU_DO_NOT_RESPECT_THE_WORKFLOW);		
 	}
 
 	public boolean isErrorMessage() {
 		return isErrorState ;
 	}
 
 }
