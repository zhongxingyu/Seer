 package aeroport.sgbag.gui;
 
import static org.junit.Assert.*;

 import org.eclipse.swt.widgets.Display;
 import org.junit.Test;
 
 public class MainWindowTest {
 
 	@Test
 	public void test() {
 		MainWindow win = new MainWindow();
 		win.open();
 		Display.getCurrent().dispose();
 	}
 
 }
