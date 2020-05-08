 package org.geoquest.test;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import com.phonegap.geoquestweb.MainActivity;
 import com.phonegap.geoquestweb.R;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 import static org.mockito.Mockito.*;
 
 @RunWith(RobolectricTestRunner.class)
 public class GeoQuestClientTest extends TestCase {
 
 	@Test
 	public void shouldHaveHappySmiles() throws Exception {
		String hello = new MainActivity().getResources().getString(
				R.string.app_name);
 		assertThat(hello, equalTo("Geoquest Client"));
 	}
 
 
 /*
 	@Test
 	public void testParse() throws Exception {
 		
 		FormBuilder mockedBuilder = mock(FormBuilder.class);
 		
 		verify(mockedBuilder).buildSection(startsWith("welcome_"), anyString());
 		verify(mockedBuilder).buildText(eq("welcome_text"), anyString());
 		verify(mockedBuilder).buildSection(eq("contact"), anyString());
 		verify(mockedBuilder,times(2)).startTable(anyString());
 		verify(mockedBuilder,times(3)).startRow();
 		verify(mockedBuilder).buildLabeledTextField(eq("phone_nr"), anyString(),eq(true),eq("phone"));
 		verify(mockedBuilder).buildLabeledTextField(eq("mobile_phone_nr"), anyString(),eq(false),eq("phone"));
 		verify(mockedBuilder,times(3)).endRow();
 
 		verify(mockedBuilder).buildMatrix(eq("disease"),
 				eq(false),
 				argThat(new IdLabelListMatcher(new String[]{"yes","no","maybe"})),
 				argThat(new IdLabelListMatcher(new String[]{"gout","cancer"})));
 
 	}
 */
 }
