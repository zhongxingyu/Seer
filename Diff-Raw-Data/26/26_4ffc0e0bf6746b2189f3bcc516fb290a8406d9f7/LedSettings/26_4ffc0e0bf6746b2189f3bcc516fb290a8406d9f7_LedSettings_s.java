 package be.kuleuven.med.brainfuck.domain.settings;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class LedSettings {

	public static final int MAX_INTENSITY = 254;
 	
 	public static final int MIN_INTENSITY = 0;
 	
 	public static final int MIN_FLICKER_FREQUENCY = 0;
 	
 	public static final int DEFAULT_SECONDS_TO_RUN = 2;
 
 	private LedPosition ledPosition;
 	
 	private int rowPin;
 	
 	private int columnPin;
 	
 	private int intensity = MAX_INTENSITY;
 	
 	private int flickerFrequency = MIN_FLICKER_FREQUENCY;
 	
 	private int secondsToRun = DEFAULT_SECONDS_TO_RUN;
 	
 	public LedSettings() { }
 
 	public LedSettings(LedPosition ledPosition) {
 		this.ledPosition = ledPosition;
 	}
 
 	public int getX() {
 		return ledPosition.getX();
 	}
 	
 	public void setX(int x) {
 		ledPosition.setX(x);
 	}
 
 	public LedPosition getLedPosition() {
 		return ledPosition;
 	}
 	
 	public void setLedPosition(LedPosition ledPosition) {
 		this.ledPosition = ledPosition;
 	}
 
 	public void setY(int yPosition) {
 		ledPosition.setY(yPosition);
 	}
 
 	public int getY() {
 		return ledPosition.getY();
 	}
 
 	public int getRowPin() {
 		return rowPin;
 	}
 
 	public void setRowPin(int rowPin) {
 		this.rowPin = rowPin;
 	}
 
 	public int getColumnPin() {
 		return columnPin;
 	}
 
 	public void setColumnPin(int columnPin) {
 		this.columnPin = columnPin;
 	}
 
 	public int getIntensity() {
 		return intensity;
 	}
 
 	public void setIntensity(int intensity) {
 		this.intensity = intensity;
 	}
 
 	public int getFlickerFrequency() {
 		return flickerFrequency;
 	}
 
 	public void setFlickerFrequency(int flickerFrequency) {
 		this.flickerFrequency = flickerFrequency;
 	}
 
 	public int getSecondsToRun() {
 		return secondsToRun;
 	}
 
 	public void setSecondsToRun(int secondsToRun) {
 		this.secondsToRun = secondsToRun;
 	}
 	
 }
