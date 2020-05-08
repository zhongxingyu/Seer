 package gamod.test;
 
 import gamod.format.*;
 import gamod.player.Mod;
 import java.io.*;
 import android.test.ActivityTestCase;
 
 public class TestModLoader extends ActivityTestCase {
   public void test_recognize_random_file_fails() {
     Parser parser = new ParserMod();
     for (int i = 0; i < 200; i++) {
       byte[] a = new byte[(int)(Math.random() * 10000)];
       for (int j = 0; j < a.length; j++)
         a[j] = (byte)(Math.random() * 256);
       assertFalse(i + " " + a.length, parser.test(a));
       assertNull(i + " " + a.length, parser.parse(a));
     }
   }
 
   public void test_empty_mod() {
     byte[] a = readResource(R.raw.empty);
    gamod.format.Parser parser = new ParserMod();
     assertTrue(parser.test(a));
     Mod mod = parser.parse(a);
     assertEquals(4, mod.tracks);
     assertEquals(31, mod.instruments.length);
     assertEquals(1, mod.songLength);
   }
 
   public void test_protracker_mod() {
     byte[] a = readResource(R.raw.arkanoid);
     Parser parser = new ParserMod();
     assertTrue(parser.test(a));
     Mod mod = parser.parse(a);
     assertEquals(4, mod.tracks);
     assertEquals(31, mod.instruments.length);
     assertEquals(2, mod.songLength);
   }
 
   public void test_unusual_mod() {
     byte[] a = readResource(R.raw.parallax);
     Parser parser = new ParserMod();
     assertTrue(parser.test(a));
     Mod mod = parser.parse(a);
     assertEquals(4, mod.tracks);
     assertEquals(31, mod.instruments.length);
     assertEquals(99, mod.songLength);
   }
 
   public void test_legacy_mod() {
     byte[] a = readResource(R.raw.bally);
     Parser parser = new ParserMod();
     assertTrue(parser.test(a));
     Mod mod = parser.parse(a);
     assertEquals(4, mod.tracks);
     assertEquals(15, mod.instruments.length);
     assertEquals(20, mod.songLength);
   }
 
   private byte[] readResource(int resId) {
     try {
       InputStream in = getInstrumentation().getContext().getResources().openRawResource(resId);
       ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());
       for (int c = in.read(); c >= 0; c = in.read())
         out.write(c);
       return out.toByteArray();
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 }
