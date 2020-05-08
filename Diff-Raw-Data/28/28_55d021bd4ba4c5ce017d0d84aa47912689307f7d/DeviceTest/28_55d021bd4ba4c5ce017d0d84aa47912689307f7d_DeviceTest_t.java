 package com.leandog.robogherk;
 
import static org.junit.Assert.*;
import junit.framework.AssertionFailedError;

 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 import android.view.View;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class DeviceTest {
 	
 	private Solo solo = mock(Solo.class);
 	private ViewFinder viewFinder = mock(ViewFinder.class);
 	private Device device = new Device(solo, viewFinder);
 	
 	@Test
 	public void clickFindsTheView() {
		View view = mock(View.class);
		when(viewFinder.find(anyString())).thenReturn(view);
 		device.click("hello.*world");
 		verify(viewFinder).find("hello.*world");
 	}
 	
 	@Test
 	public void clickClicksTheFoundView() {
 		View view = mock(View.class);
 		when(viewFinder.find(anyString())).thenReturn(view);
 		device.click("someRegex");
 		verify(solo).clickOnView(view);
 	}
	
	@Test
	public void clickGivesUsAPrettyErrorWhenItCannotFindTheView() {
		when(viewFinder.find(anyString())).thenReturn(null);
		
		try {
			device.click("an arbitrary regex");
			fail("wha?! click() should have thrown a JUnit assertion");
		} catch (AssertionFailedError e) {
			assertEquals("Could not find a clickable view matching 'an arbitrary regex'", e.getMessage());
		}
	}
 }
