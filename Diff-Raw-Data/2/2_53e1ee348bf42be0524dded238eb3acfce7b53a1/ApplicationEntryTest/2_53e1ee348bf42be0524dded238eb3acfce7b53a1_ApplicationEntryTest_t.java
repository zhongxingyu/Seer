 package Ide.desktopentry;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import Ide.locale.LcMessages;
 
 public class ApplicationEntryTest extends BaseTest {
 
   @Test
   public void works() {
     File f = fromResources("assistant.desktop");
     try (BufferedReader r = new BufferedReader(new FileReader(f))) {
       String line = r.readLine();
       Assert.assertEquals("[Desktop Entry]", line);
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   private File fromResources(final String name) {
     URL res = getClass().getClassLoader().getResource(name);
     return new File(res.getPath());
   }
 
   @Test
   public void desktopEntryGroupHeader() {
     File f = fromResources("mozilla-firefox.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNotNull(a);
   }
 
   @Test
   public void missingDesktopEntryGroupHeader() {
     File f = fromResources("missing-header.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
 
   @Test
   public void applicationType() {
     File f = fromResources("gftp.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNotNull(a);
   }
 
   @Test
   public void otherType() {
     File f = fromResources("missing-type.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
 
   @Test
   public void invalidLine() {
     File f = fromResources("invalid-line.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
 
   @Test
   public void twoGroups() {
     File f = fromResources("two-groups.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNotNull(a);
   }
 
   @Test
   public void name() {
     File f = fromResources("mozilla-firefox.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("C"));
     Assert.assertEquals("Firefox", a.getName());
   }
 
   @Test
   public void missingName() {
     File f = fromResources("missing-name.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
 
   @Test
   public void missingName2() {
     File f = fromResources("missing-name2.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
 
   @Test
   public void localizedName() {
     File f = fromResources("mozilla-firefox.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("eo_EO.UTF-8"));
     Assert.assertEquals("Mozilo Fajrovulpo", a.getName());
   }
 
   @Test
   public void localizedName2() {
     File f = fromResources("mozilla-firefox.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("pa_PA.UTF-8"));
     Assert.assertEquals("ਫਾਇਰਫੋਕਸ", a.getName());
   }
 
   @Test
   public void genericName() {
     File f = fromResources("mplayer.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("C"));
     Assert.assertEquals("Media Player", a.getGenericName());
   }
 
   @Test
   public void genericNameMissing() {
     File f = fromResources("xterm.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("C"));
     Assert.assertEquals(null, a.getGenericName());
   }
 
   @Test
   public void genericNameMissing2() {
     File f = fromResources("missing-generic-name2.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("C"));
     Assert.assertNull(a);
   }
 
   @Test
   public void translatedGenericName() {
     File f = fromResources("mplayer.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("ja_JP.UTF-8"));
     Assert.assertEquals("メディアプレーヤー", a.getGenericName());
   }
 
   @Test
   public void noDisplayMissing() {
     File f = fromResources("mplayer.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("ja_JP.UTF-8"));
     Assert.assertEquals(true, a.isVisible());
   }
 
   @Test
   public void noDisplay() {
     File f = fromResources("nm-applet.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("ja_JP.UTF-8"));
     Assert.assertEquals(false, a.isVisible());
   }
 
   @Test
   public void badNoDisplayValue() {
     File f = fromResources("bad-nodisplay.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("ja_JP.UTF-8"));
     Assert.assertNull(a);
   }
 
   @Test
   public void comment() {
     File f = fromResources("gucharmap.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("C"));
     Assert.assertEquals("Insert special characters into documents", a.getComment());
   }
 
   @Test
   public void translatedComment() {
     File f = fromResources("gucharmap.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("ar_AR"));
     Assert.assertEquals("أدرج محارف خاصة في المستندات", a.getComment());
   }
 
   @Test
   public void badComment() {
     File f = fromResources("bad-comment.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("ar_AR"));
     Assert.assertNull(a);
   }
 
   @Test
   public void missingComment() {
     File f = fromResources("assistant.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a.getComment());
   }
 
   @Test
   public void icon() {
     File f = fromResources("gvim.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("C"));
     Assert.assertEquals("gvim.png", a.getIcon());
   }
 
   @Test
   public void translatedIcon() {
     File f = fromResources("gvim.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("ru_RU"));
     Assert.assertEquals("gvimru.png", a.getIcon());
   }
 
   @Test
   public void badIcon() {
     File f = fromResources("bad-icon.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
 
   @Test
   public void missingIcon() {
     File f = fromResources("missing-icon.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a.getIcon());
   }
 
   @Test
   public void hidden() {
     File f = fromResources("hidden.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertFalse(a.isVisible());
   }
 
   @Test
   public void badHidden() {
     File f = fromResources("bad-hidden.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
 
   @Test
   public void missingHidden() {
     File f = fromResources("geeqie.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertTrue(a.isVisible());
   }
 
   @Test
   public void wrongCharactersInKey() {
     File f = fromResources("bad-key.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
 
   @Test
   public void onlyShowInXFCE() {
     File f = fromResources("xfce4-about.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertFalse(a.isVisible());
   }
   
   @Test
   public void onlyShowIn1deAndXFCE() {
     File f = fromResources("only-show-in-1de.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertTrue(a.isVisible());
   }
   
   @Test
   public void onlyShowInMissing() {
     File f = fromResources("gimp.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertTrue(a.isVisible());
   }
   
   @Test
   public void notShowInXGeeqie() {
     File f = fromResources("geeqie.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertTrue(a.isVisible());
   }
   
   @Test
   public void notShowIn1deAndKDE() {
     File f = fromResources("not-show-in-1de.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertFalse(a.isVisible());
   }
   
   @Test
   public void notShowInMissing() {
     File f = fromResources("gimp.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertTrue(a.isVisible());
   }
   
   @Test
   public void missingValue() {
     File f = fromResources("missing-value.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
   
   @Test
   public void missingValue2() {
     File f = fromResources("missing-value2.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a);
   }
   
   @Test
   public void tryExec() {
     File f = fromResources("audacious.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertEquals("audacious", a.getTryExec());
   }
   
   @Test
   public void missingTryExec() {
     File f = fromResources("seamonkey.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertNull(a.getTryExec());
   }
   
   @Test
   public void exec() {
     File f = fromResources("wicd.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertEquals("wicd-gtk", a.getExecutable());
     Assert.assertEquals(FileArgsSupport.NOPE, a.argumentsSupport());
     List<String> args = a.getArguments();
     Assert.assertEquals("--no-tray", args.get(0));
     Assert.assertEquals(1, args.size());
   }
   
   @Test
   public void missingExec() {
     File f = fromResources("missing-exec.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertEquals(null, a.getExecutable());
     Assert.assertEquals(FileArgsSupport.NOPE, a.argumentsSupport());
     Assert.assertEquals(0, a.getArguments().size());
   }
   
   @Test
   public void execWithIcon() {
     File f = fromResources("gimp.desktop");
     ApplicationEntry a = ApplicationEntry.parse(f);
     Assert.assertEquals("gimp-2.8", a.getExecutable());
     Assert.assertEquals(FileArgsSupport.MANY_URLS, a.argumentsSupport());
     List<String> args = a.getArguments();
     Assert.assertEquals("--icon", args.get(0));
     Assert.assertEquals("gimp", args.get(1));
     Assert.assertEquals(2, args.size());
   }
   
   @Test
   public void execWithMissingIcon() {
     File f = fromResources("missing-icon.desktop");
    ApplicationEntry a = ApplicationEntry.parse(f, LcMessages.parseLcMessages("C"));
     Assert.assertEquals("gvim", a.getExecutable());
     Assert.assertEquals(FileArgsSupport.SINGLE_FILE, a.argumentsSupport());
     List<String> args = a.getArguments();
     Assert.assertEquals("-f", args.get(0));
     Assert.assertEquals(1, args.size());
   }
 }
