 package com.servolabs.thomas.dummy;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
import org.junit.runner.RunWith;
 
import com.servolabs.robolectric.ThomasTestRunner;

@RunWith(ThomasTestRunner.class)
 public class DummyContentTest {

 	@Test
	public void makeThreeDummyItems() {
 		assertThat(DummyContent.ITEMS.size(), is(3));
 	}
 
 }
