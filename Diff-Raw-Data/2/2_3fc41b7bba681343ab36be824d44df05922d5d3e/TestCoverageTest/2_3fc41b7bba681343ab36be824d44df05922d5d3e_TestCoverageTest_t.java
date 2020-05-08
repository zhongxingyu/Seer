 package collabode;
 
 import static org.junit.Assert.assertEquals;
 import static org.openqa.selenium.Keys.ENTER;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.junit.Test;
 import org.openqa.selenium.*;
 
 import com.google.common.base.Function;
 
 @BrowserTest.Fixtures("test-coverage")
 public class TestCoverageTest extends BrowserTest {
     
     private static final Pattern DECLARE = Pattern.compile(".*public.*\\(.*\\) \\{$", Pattern.MULTILINE);
     private static final Pattern SIGNATURE = Pattern.compile("\\w+\\(.*\\)");
     private static final Pattern INDENT = Pattern.compile("^ +");
     
     private static final By TEST = By.id("test");
     private static final By FILES = By.id("files");
     private static final By FILE = By.className("pad");
     
     private static final String FOLD = " ... ";
     
     final Map<String, Map<String, String>> sources;
     
     CollabodeDriver driver;
     
     public TestCoverageTest() throws IOException {
         sources = new HashMap<String, Map<String, String>>();
         for (String file : new String[] { "English", "Formatter", "FormatterTest" }) {
             sources.put(file, new HashMap<String, String>());
             Scanner code = new Scanner(initial(file + ".java"));
             while (true) {
                 String decl = code.findWithinHorizon(DECLARE, 0);
                 if (decl == null) { break; }
                 Matcher sig = SIGNATURE.matcher(decl); sig.find();
                 Matcher indent = INDENT.matcher(decl); indent.find();
                 StringBuilder body = new StringBuilder(decl);
                 while (true) {
                     String line = code.nextLine();
                     body.append(line);
                     if (line.equals(indent.group() + "}")) { break; }
                 }
                 sources.get(file).put(sig.group(), body.toString());
             }
         }
     }
     
     @Test public void testCoverage() throws IOException {
         driver = connect();
         driver.get(fixture() + "/src/Formatter.java");
         driver.wait.until(new Function<WebDriver, WebElement>() {
             public WebElement apply(WebDriver driver) {
                 return driver.findElement(By.cssSelector(".test"));
             }
         });
         
         driver.get("coverage/" + fixture() + ":FormatterTest.testFormatCount");
         assertCoverage(
                 FOLD + sources.get("FormatterTest").get("testFormatCount()") + FOLD,
                 FOLD +
                 sources.get("Formatter").get("Formatter(L lang)") +
                 sources.get("Formatter").get("format(int count, String thing)") +
                 FOLD,
                 FOLD + sources.get("English").get("pluralize(int count, String singular)") + FOLD);
         
         driver.get("coverage/" + fixture() + ":FormatterTest.testFormatList");
         assertCoverage(
                 FOLD +
                 sources.get("FormatterTest").get("newList()") +
                 FOLD +
                 sources.get("FormatterTest").get("testFormatList()") +
                 FOLD,
                 FOLD +
                 sources.get("Formatter").get("Formatter(L lang)") +
                 FOLD +
                 sources.get("Formatter").get("format(List<String> things)") +
                 FOLD,
                 FOLD + sources.get("English").get("joiners(int length)") + FOLD);
     }
     
     @Test public void testUnintegrated() {
         String text = "1" + ENTER + "2" + ENTER + "3" + ENTER;
         
         driver = connect();
         driver.get(fixture() + "/src/Formatter.java");
         driver.findEditorLine("private", 1).click();
         driver.switchToEditorInner().sendKeys(text);
         
         CollabodeDriver other = connect();
         other.get(fixture() + "/src/Formatter.java");
         other.findEditorLine("class", 1).click();
         other.switchToEditorInner().sendKeys(text);
         
         driver.findEditorLine("things").click();
         driver.switchToEditorInner().sendKeys(ENTER + "things.add(\"kiwi\");");
         
         driver.switchToPage();
         driver.wait.until(new Function<WebDriver, WebElement>() {
             public WebElement apply(WebDriver driver) {
                 return driver.findElement(By.cssSelector(".test.failure"));
             }
         });
         
         driver.get("coverage/" + fixture() + ":FormatterTest.testFormatCount");
         assertCoverage(
                 FOLD + sources.get("FormatterTest").get("testFormatCount()") + FOLD,
                 FOLD +
                 sources.get("Formatter").get("Formatter(L lang)") +
                 sources.get("Formatter").get("format(int count, String thing)") +
                 FOLD,
                 FOLD + sources.get("English").get("pluralize(int count, String singular)") + FOLD);
     }
     
     private void assertCoverage(String test, String... files) {
        assertEqualsModWhitespace(test, driver.findElement(TEST).findElement(FILE).getText());
         
         SortedSet<String> expected = new TreeSet<String>();
         for (String file : files) {
             expected.add(regularWhitespace(file));
         }
         SortedSet<String> found = new TreeSet<String>();
         for (WebElement elt : driver.findElement(FILES).findElements(FILE)) {
             found.add(regularWhitespace(elt.getText()));
         }
         assertEquals(expected, found);
     }
 }
