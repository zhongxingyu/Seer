 package de.uvwxy.ledlamp;
 
 import java.io.IOException;
 
 import de.uvwxy.net.AConnection;
 
 public class LEDDriverT2V1 extends LEDDriver {
 
 	public LEDDriverT2V1(AConnection con) {
 		super(con);
 	}
 
 	@Override
 	public void setBlue(int b) throws IOException {
		getCon().getOut().write(("B;" + b).getBytes());
 	}
 
 	@Override
 	public void setGreen(int g) throws IOException {
		getCon().getOut().write(("G;" + g).getBytes());
 	}
 
 	@Override
 	public void setRed(int r) throws IOException {
		getCon().getOut().write(("R;" + r).getBytes());
 	}
 
 	@Override
 	public void setSpeed(int s) throws IOException {
		//getCon().getOut().write(("B;" + s).getBytes());
 	}
 
 	@Override
 	public void setWhite(int w) throws IOException {
		getCon().getOut().write(("W;" + w).getBytes());
 	}
 
 }
