 package edu.wheaton.simulator.test.gui;
 
 import javax.swing.JButton;
 import junit.framework.TestCase;
 
 import org.junit.Assert;
 
 import edu.wheaton.simulator.gui.Display;
 import edu.wheaton.simulator.gui.FieldAddListener;
 
 public class TestFieldAddListener extends TestCase{
 
 	private Display display;
 	private MockManager mm;
 	private MockScreen ms1;
 	private MockScreen ms2;
 	private FieldAddListener fal;
 	private JButton add;
 	
 	@Override
 	public void setUp(){
 		display = new Display();
 		ms1 = new MockScreen("Edit Fields", mm);
 		ms2 = new MockScreen("Edit Field Screen", mm);
 		mm = new MockManager(display, ms1.getName(), ms1);
 		mm.addScreen(ms2.getName(), ms2);
 		fal = new FieldAddListener(mm);
 		add = new JButton();
 		add.addActionListener(fal);
 		add.doClick();
 	}
 	
 	public void testGetScreen(){
 		Assert.assertTrue("Edit Fields returned from get screen", mm.getScreen("Edit Fields") instanceof MockScreen);
 	}
 }
