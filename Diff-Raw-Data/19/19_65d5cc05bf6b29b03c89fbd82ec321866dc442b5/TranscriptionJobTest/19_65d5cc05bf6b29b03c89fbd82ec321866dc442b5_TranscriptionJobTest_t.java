 import org.junit.*;
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import play.Logger;
 import play.libs.F.Promise;
 import play.test.*;
 import service.transcription.TranscriptionJob;
 import models.*;
 
 public class TranscriptionJobTest extends UnitTest {
 
     @Test
     public void simpleConversionTest() throws Exception {
         Promise job = new TranscriptionJob("これは日本語のテキストです。東京から京都へ新幹線で行きたいですよ。").now();
         
         TranscriptionResult result = (TranscriptionResult) job.get();
         
         assertTrue(result.getSize() > 0);
         assertEquals("これ", result.rubies.get(0).getPlainForm());
         assertEquals("", result.rubies.get(0).getFurigana());
         assertEquals("日本語", result.rubies.get(2).getPlainForm());
         assertEquals("にほんご", result.rubies.get(2).getFurigana());
         assertEquals("テキスト", result.rubies.get(4).getPlainForm());
         assertEquals("", result.rubies.get(4).getFurigana());
         assertEquals("行き", result.rubies.get(13).getPlainForm());
         assertEquals("いき", result.rubies.get(13).getFurigana());
     }
 
     @Test
     public void canHandleRomaji() throws Exception {
     	Promise job = new TranscriptionJob("これはjapanese text").now();
         
         TranscriptionResult result = (TranscriptionResult) job.get();
         assertEquals("これ", result.rubies.get(0).getPlainForm());
         assertEquals("japanese", result.rubies.get(2).getPlainForm());
     }
     
     @Test
     public void canHandleOkuriganaAndPrefixes() throws Exception {
         Promise job = new TranscriptionJob("考える。お茶。行く。勉強する。田中さん。").now();
         
         TranscriptionResult result = (TranscriptionResult) job.get();
         
         Logger.info("Rubies: %s", result.rubies);
         
         assertTrue(result.getSize() > 0);
         List<RubyPart> rubyParts = result.rubies.get(0).rubyParts;
 		assertEquals("考", rubyParts.get(0).written);
 		assertEquals("かんが", rubyParts.get(0).furigana);
 		assertEquals("える", rubyParts.get(1).written);
 		assertEquals("える", rubyParts.get(1).furigana);
 		
 		rubyParts = result.rubies.get(2).rubyParts;
 		assertEquals("お", rubyParts.get(0).written);
 		assertEquals("お", rubyParts.get(0).furigana);
 		assertEquals("茶", rubyParts.get(1).written);
 		assertEquals("ちゃ", rubyParts.get(1).furigana);
 		
 		rubyParts = result.rubies.get(4).rubyParts;
 		assertEquals("行", rubyParts.get(0).written);
 		assertEquals("い", rubyParts.get(0).furigana);
 		assertEquals("く", rubyParts.get(1).written);
 		assertEquals("く", rubyParts.get(1).furigana);
 		
 		rubyParts = result.rubies.get(6).rubyParts;
 		assertEquals("勉強", rubyParts.get(0).written);
 		assertEquals("べんきょお", rubyParts.get(0).furigana);
 		
 		rubyParts = result.rubies.get(7).rubyParts;
 		assertEquals("する", rubyParts.get(0).written);
 		assertEquals("する", rubyParts.get(0).furigana);
 		
 		rubyParts = result.rubies.get(9).rubyParts;
 		assertEquals("田中", rubyParts.get(0).written);
 		assertEquals("たなか", rubyParts.get(0).furigana);
 		
 		rubyParts = result.rubies.get(10).rubyParts;
 		assertEquals("さん", rubyParts.get(0).written);
 		assertEquals("さん", rubyParts.get(0).furigana);
     }
    
    @Test
    public void canHandleRomajiWithDuplicateChars() throws Exception {
    	Promise job = new TranscriptionJob("soon").now();
        
        TranscriptionResult result = (TranscriptionResult) job.get();
        final List<RubyPart> rubyParts = result.rubies.get(0).rubyParts;
		assertEquals("soon", rubyParts.get(0).furigana);
        assertEquals("soon", rubyParts.get(0).written);
    }
 }
