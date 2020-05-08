 package com.qut.spc.model.converter;
 
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.qut.spc.model.SolarPanel;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 
 public class PanelConverterTest {
 	
 	private SolarPanelConverter converter;
 	private SolarPanel panel;
 	
 	@Before
 	public void setup(){
 		panel=mock(SolarPanel.class);
 		converter=new SolarPanelConverter(panel);
 	}
 
 	@Test
 	public void testGetOutputEnergy_validPanel_panelsResultIsReturn(){
 		when(panel.getOutputEnergy()).thenReturn(2424.4334);
 		
		assertEquals(2424.4334f, converter.getOutputEnergy(),0.00001f);
 	}
 
 	
 }
