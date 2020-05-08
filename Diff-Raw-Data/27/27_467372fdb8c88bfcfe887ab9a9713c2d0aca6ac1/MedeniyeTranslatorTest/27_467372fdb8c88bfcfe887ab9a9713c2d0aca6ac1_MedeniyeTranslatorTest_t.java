 /**
  * 
  */
 package name.webdizz.clt.crx.client.translation.medeniye;
 
 import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 import name.webdizz.clt.crx.client.translation.ITranslationHandler;
 import name.webdizz.clt.crx.client.translation.ITranslator;
import name.webdizz.clt.crx.client.translation.TranslationException;
import name.webdizz.clt.crx.client.translation.TranslationResult;
 
 import org.junit.Before;
 import org.junit.Test;
import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
 
 import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;
 
 /**
  * @author webdizz
  * 
  */
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({ URL.class })
@SuppressStaticInitializationFor({ "com.google.gwt.dom.client.NativeEvent" })
 public class MedeniyeTranslatorTest {
 
 	private ITranslator translator;
 
 	@Mock
 	private MedeniyeRequestBuilder builder;
 
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 		translator = new MedeniyeTranslator(builder);
		PowerMockito.mockStatic(URL.class);
		PowerMockito.when(URL.encode(anyString())).thenReturn("word");
 	}
 
 	@Test
 	public void shouldNotTranslateEmptyWord() throws Exception {
 		ITranslationHandler handler = mock(ITranslationHandler.class);
 		translator.translate("", handler);
 
 		verify(handler, never()).onTranslate(any(TranslationResult.class));
 	}
 
 	@Test
 	public void shouldNotTranslateWordWithSpaces() throws Exception {
 		ITranslationHandler handler = mock(ITranslationHandler.class);
 		translator.translate("  ", handler);
 
 		verify(handler, never()).onTranslate(any(TranslationResult.class));
 	}
 
 	@Test(expected = TranslationException.class)
 	public void shouldFailIfRequestBuilderIsNull() throws Exception {
 		ITranslationHandler handler = mock(ITranslationHandler.class);
 		translator = new MedeniyeTranslator(null);
 		translator.translate("word", handler);
 	}
 
 	@Test
 	public void shouldSendTranslationRequest() throws Exception {
 		ITranslationHandler handler = mock(ITranslationHandler.class);
 		translator.translate("word", handler);
 
 		verify(builder).send("word=word", handler);
 	}
 
 	@Test(expected = TranslationException.class)
 	public void shouldHandleRequestException() throws Exception {
 		ITranslationHandler handler = mock(ITranslationHandler.class);
 		when(builder.send("word=word", handler)).thenThrow(new RequestException());
 		translator.translate("word", handler);
 	}
 
 }
