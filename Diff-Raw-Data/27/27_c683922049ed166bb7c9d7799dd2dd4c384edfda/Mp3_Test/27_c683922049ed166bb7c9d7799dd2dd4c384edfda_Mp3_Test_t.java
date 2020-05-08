 package com.github.signed.mp3;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 public class Mp3_Test {
 
     @Rule
    public final Mp3Rule samples = new Mp3Rule();
    @SuppressWarnings("unchecked")
    private final ExceptionTranslatingCallback<String> callback = mock(ExceptionTranslatingCallback.class);
 
     @Before
     public void setUp() throws Exception {
         Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
     }
 
     @Test
     public void storeNewTitle() throws Exception {
         Mp3 mp3 = this.samples.whiteNoise();
         mp3.setTitleTo("the new one");
         mp3.saveChanges();
 
         Mp3 reloaded = Mp3.From(mp3);
         reloaded.provideTitleTo(callback);
 
         verify(callback).call("the new one");
     }
 
     @Test
     public void dropTag() throws Exception {
         Mp3 mp3 = this.samples.whiteNoise();
         mp3.drop(Tag.Title);
         mp3.saveChanges();
 
         Mp3 reloaded = Mp3.From(mp3);
 
         reloaded.provideTitleTo(callback);
        verify(callback).fallback();
     }
}
