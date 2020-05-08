 package proj.zoie.impl.indexing;
 
 import junit.framework.TestCase;
 import proj.zoie.api.DataConsumer;
 import proj.zoie.example.service.Tweet;
 import proj.zoie.example.service.TwitterFileDataProvider;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ReadableByteChannel;
 
 public class TestTweet extends TestCase {
 
   private static final String[] tweetStrings = {
       "7533892004   1262988000  182         MuhammadAtt         RT @dharmesh: RT @ewiesen: \"Sell something needed\" #startuptriplets  http://bit.ly/8WfDYv",
       "7533892003   1262988000  185         escravoroger_rf     Esse aqui, por exemplo, t muito cuti-cuti! http://bit.ly/8GkNo3.",
       "7533892102   1262988000  344         hetweetalia         Fanvid. AMV. US/UK. PG. http://bit.ly/6BA5ys",
       "7533892100   1262988000  27          sekki_hinode        07:00",
       "7533892104   1262988000  1           jonathan_croft      Pyrotess has completed \"Vanquished Buster\".",
       "7533892200   1262988001  1063        rebeccaViveiros     Are Rainmakers Crashing the Tea Party? - Newsweek (blog) http://bit.ly/7axjIz ",
       "7533892203   1262988001  4           mizunagiworld       @sunotti                         ",                                                                                                             
       "7533892201   1262988001  68          MeteoParkstad       Temp -3,9C Windchill -8,6C Dauwpunt -6,5C Vocht 87% Luchtdruk gelijkblijvend 1018,0hPa Regen 0,0l/m Wind 10min 10,3Km/h O.      "
   };
 
   private static String pad(String s, int length) {
     if(s.length() >= length) {
       return s.substring(0, length - 1) + "\n";
     } else {
       return s + blank(length - s.length() - 1) + "\n";
     }
   }
 
   private static String pad(String s) {
     return pad(s, 200);
   }
 
   private static String blank(int len) {
     String s = "";
     for(int i=0; i<len; i++) s += " ";
     return s;
   }
 
   private static byte[] toBytes(String s) {
     byte[] bytes = new byte[s.length()];
     for(int i=0; i<bytes.length; i++) bytes[i] = (byte)s.charAt(i);
     return bytes;
   }
   
   public void doTestTweet(byte[] data, int numTweetsExpected) throws Exception {
     final ReadableByteChannel channel = new ByteArrayReadableByteChannel(data);
     TwitterFileDataProvider provider = new TwitterFileDataProvider(channel) {
       public void sleep() throws InterruptedException {
         throw new InterruptedException("not gonna sleep here!");
       }
     };
     DataConsumer.DataEvent<Tweet> t;
     int i=0;
     while((t = provider.next()) != null) {
       i++;
     }
     assertEquals(numTweetsExpected, i);
   }
 
   public void testTweet() throws Exception {
     doTestTweet(getData(), 8);
   }
 
   public void testBrokenTweet() throws Exception {
     doTestTweet(getDataWithWrongLengthTweet(190), 6); // last tweet isn't big enough, would block forever, but we bail
     doTestTweet(getDataWithWrongLengthTweet(210), 7); // all but the broken one
     doTestTweet(getDataWithNewlinesInserted(), 7);    // all but the broken one, but last tweet isn't big again!
   }
 
  public void testTweetFileTweet() throws Exception {
    TwitterFileDataProvider provider = new TwitterFileDataProvider("tweets");
    while(provider.next() != null) {
      System.out.println("yay!");
    }
  }

   private byte[] getData() {
     byte[][] b = new byte[tweetStrings.length][];
     int totalLen = 0;
     for(int i=0; i<tweetStrings.length; i++) {
       b[i] = toBytes(pad(tweetStrings[i]));
       totalLen += b[i].length;
     }
     byte[] bytes = new byte[totalLen];
     int offset = 0;
     for(byte[] bb : b) {
       System.arraycopy(bb, 0, bytes, offset, bb.length);
       offset += bb.length;
     }
     return bytes;
   }
 
   private byte[] getDataWithWrongLengthTweet(int length) {
     byte[][] b = new byte[tweetStrings.length][];
     int totalLen = 0;
     for(int i=0; i<tweetStrings.length; i++) {
       if(i != 3) {
         b[i] = toBytes(pad(tweetStrings[i]));
       } else {
         b[i] = toBytes(pad(tweetStrings[i], length));
       }
       totalLen += b[i].length;
     }
     byte[] bytes = new byte[totalLen];
     int offset = 0;
     for(byte[] bb : b) {
       System.arraycopy(bb, 0, bytes, offset, bb.length);
       offset += bb.length;
     }
     return bytes;
   }
 
   private byte[] getDataWithNewlinesInserted() {
     byte[][] b = new byte[tweetStrings.length][];
     int totalLen = 0;
     for(int i=0; i<tweetStrings.length; i++) {
       if(i != 3) {
         b[i] = toBytes(pad(tweetStrings[i]));
       } else {
         b[i] = toBytes(pad("\nasdf" + tweetStrings[i], 200));
       }
       totalLen += b[i].length;
     }
     byte[] bytes = new byte[totalLen];
     int offset = 0;
     for(byte[] bb : b) {
       System.arraycopy(bb, 0, bytes, offset, bb.length);
       offset += bb.length;
     }
     return bytes;
 
   }
 
   public void testNFE() throws Exception {
     long l;
     try {
       l = Long.parseLong("23465  ");
       fail("");
     } catch (NumberFormatException nfe) { } try {
       l = Long.parseLong("\t123523");
       fail("");
     } catch (NumberFormatException nfe) { } try {
       l = Long.parseLong("\n\n12998098\n");
       fail("");
     } catch (NumberFormatException nfe) { } try {
       l = Long.parseLong(("\t\n  234098 \t\t\n\n").trim());
       assertTrue(true);
     } catch (NumberFormatException nfe) { fail(""); }
   }
 
   private static class ByteArrayReadableByteChannel implements ReadableByteChannel {
     byte[] bytes;
     char[] bytesAsChars;
     String s;
     int position = 0;
     boolean open = true;
 
     public ByteArrayReadableByteChannel(byte[] data) {
       bytes = data;
       bytesAsChars = new char[bytes.length];
       for(int i=0;i<bytes.length;i++) bytesAsChars[i] = (char)bytes[i];
       s = new String(bytesAsChars);
     }
 
     @Override
     public boolean isOpen() {
       return open;
     }
 
     @Override
     public void close() throws IOException {
       open = false;
     }
 
     @Override
     public int read(ByteBuffer byteBuffer) throws IOException {
       int result;
       byte[] buffer = new byte [byteBuffer.remaining()];
       int available = bytes.length - position;
       result = Math.min(available, buffer.length);
       System.arraycopy(bytes, position, buffer, 0, result);
       position += result;
       byteBuffer.put(buffer, 0, result);
       return result;
     }
 
   }
 
 }
