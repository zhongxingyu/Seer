 package org.mobicents.servlet.sip.restcomm;
 
 import static org.junit.Assert.assertTrue;
 
 import it.sauronsoftware.jave.EncoderException;
 import it.sauronsoftware.jave.InputFormatException;
 
 import java.io.File;
 import java.net.URI;
 
 import org.apache.commons.configuration.BaseConfiguration;
 import org.junit.Before;
 import org.junit.Test;
 import org.mobicents.servlet.sip.restcomm.tts.ttsapi.TtsApiSynthesizer;
 
 /**
  * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
  */
 
 public class TtsApiSynthesizerTest {
 
 	BaseConfiguration conf;
 	
 	@Before
 	public void setUp(){
 		conf = new BaseConfiguration();
 		conf.addProperty("service-root", "http://tts-api.com/tts.mp3");
 		conf.addProperty("cache-path",System.getProperty("java.io.tmpdir"));		
 	}
 	
 	@Test
 	public void testSynthesizer() throws IllegalArgumentException, InputFormatException, EncoderException{
 		TtsApiSynthesizer ttsapi = new TtsApiSynthesizer();
 		ttsapi.configure(conf);
 		ttsapi.start();
 		URI ttsURI = ttsapi.synthesize("Hello World. How are you");
//		URI wavURI = ConvertMp3ToWav.convert(ttsURI);
 		File ttsFile = new File(ttsURI);
//		File wavFile = new File(wavURI);
		System.out.println(ttsURI);
		
//		System.out.println(wavFile);
 		assertTrue(ttsFile.exists());
//		assertTrue(wavFile.exists());
//		assertTrue(ttsFile.delete());
 	}
 }
