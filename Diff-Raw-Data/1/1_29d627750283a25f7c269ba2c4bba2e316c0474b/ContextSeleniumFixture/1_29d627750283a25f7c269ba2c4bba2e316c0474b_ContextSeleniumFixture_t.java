 package fit;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.BindException;
 import java.net.ServerSocket;
 
 import org.openqa.selenium.server.SeleniumServer;
 
 import com.thoughtworks.selenium.DefaultSelenium;
 import com.thoughtworks.selenium.Selenium;
 import com.thoughtworks.selenium.Wait;
 
 public class ContextSeleniumFixture extends ArgumentFixture
 {
   private static final int SELENIUM_PORT = 4444;
 
   private static PrintStream ps;
 
   private static SeleniumServer server;
 
   public int defaultTimeout = 3000;
 
   public Selenium selenium;
 
   @Override
   protected void interpretTables(Parse tables)
   {
     super.interpretTables(tables);
   }
 
   @Override
   public void doCells(Parse cells)
   {
     if (cells.text().equals("start") || selenium != null)
     {
       super.doCells(cells);
     }
     else
     {
       wrong(cells, "Selenium Client is not init!");
     }
   }
 
   public void timeout(Argument selektor, Argument arg)
   {
     String timeoutValue = selektor.text();
     this.defaultTimeout = Integer.parseInt(timeoutValue);
   }
 
   static
   {
     startSeleniumServer();
   }
 
   public void start(Argument selektor, Argument arg)
   {
     String testhost = selektor.text();
     String port = arg.text();
     String browser = arg.getCell().more.text();
     String baseUrl = arg.getCell().more.more.text();
 
     if (!connectSelenium(selektor, testhost, port, browser, baseUrl, true))
     {
       System.out.println("Unable to find SELENIUM !!! ");
       setStop(true);
     }
   }
 
   public static void startSeleniumServer()
   {
     try
     {
       ServerSocket serverSocket = new ServerSocket(SELENIUM_PORT);
       serverSocket.close();
 
       try
       {
         // SeleniumServer.main(new String[] {"-multiWindow"});
         server = new SeleniumServer(4444, true, true);
         server.start();
       }
       catch (Exception e)
       {
         System.err.println("Could not create Selenium Server because of: "
             + e.getMessage());
         e.printStackTrace();
       }
     }
     catch (BindException e)
     {
       System.out.println("Selenium server already up, will reuse...");
     }
     catch (IOException e)
     {
       System.out.println("Selenium server already up, will reuse...");
       e.printStackTrace();
     }
 
   }
 
   protected boolean connectSelenium(Argument selektor, String testhost,
       String port, String browser, String baseUrl, boolean markSelector)
   {
     selenium = new DefaultSelenium(testhost, Integer.valueOf(port), browser,
         baseUrl);
 
     try
     {
       selenium.start();
       return true;
     }
     catch (Exception e)
     {
       if (markSelector)
       {
         exception(selektor, e);
       }
       return false;
     }
   }
 
   public void open(Argument selektor, Argument arg) throws Exception
   {
     String url = selektor.text();
     try
     {
       selenium.open(url);
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void stopSelenium(Argument selektor, Argument arg)
   {
     selenium.stop();
   }
 
   public void type(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.type(selektor.text(), arg.text());
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void clickAndWait(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.click(selektor.text());
 
       selenium.waitForPageToLoad(getTimeout(arg.getCell()));
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   private String getTimeout(String text)
   {
     if (text != null && text.length() > 0)
     {
       return text;
     }
     return Integer.toString(defaultTimeout);
   }
 
   private String getTimeout(Parse cell)
   {
     if (cell == null)
     {
       return getTimeout("");
     }
     return getTimeout(cell.text());
   }
 
   private Integer getTimeoutInt(String text)
   {
     return Integer.parseInt(getTimeout(text));
   }
 
   private int getTimeoutInt(Parse cell)
   {
     if (cell == null)
     {
       return getTimeoutInt("");
     }
     return getTimeoutInt(cell.text());
   }
 
   public void click(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.click(selektor.text());
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
   
   public void uncheck(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.uncheck(selektor.text());
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
   
   public void check(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.check(selektor.text());
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void mouseMove(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.mouseMove(selektor.text());
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void mouseOver(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.mouseOver(selektor.text());
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void mouseOut(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.mouseOut(selektor.text());
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void mouseUp(Argument selektor, Argument arg) throws Exception
   {
     try
     {
       selenium.mouseUp(selektor.text());
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyTextNotPresent(Argument selektor, Argument arg)
   {
     String text = selektor.text();
     if (!selenium.isTextPresent(text))
     {
       right(selektor);
     }
     else
     {
       wrong(selektor.getCell(), text + " is present on site");
     }
   }
 
   public void verifyTextPresent(Argument selektor, Argument arg)
   {
     String text = selektor.text();
     if (selenium.isTextPresent(text))
     {
       right(selektor);
     }
     else
     {
       dumpWrongImage("verifyTextPresent");
       wrong(selektor.getCell(), text + " is not present on site");
     }
   }
 
   private void dumpWrongImage(String name)
   {
     String curDir = System.getProperty("user.dir");
     File f = new File(curDir, name + ".png");
     // selenium.captureEntirePageScreenshot(f.getPath());
   }
 
   public void verifyTitle(Argument selektor, Argument arg)
   {
     try
     {
       String actualText = selenium.getTitle();
       if (actualText.equals(selektor.text()))
       {
         right(selektor);
       }
       else
       {
         wrong(selektor, actualText);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyTable(Argument selektor, Argument arg)
   {
     try
     {
       String actualText = selenium.getTable(selektor.text());
       if (actualText.equals(arg.text()))
       {
         right(arg);
       }
       else
       {
         wrong(selektor, actualText);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyValue(Argument selektor, Argument arg)
   {
     try
     {
       String actualText = selenium.getValue(selektor.text());
       if (actualText.equals(arg.text()))
       {
         right(arg);
       }
       else
       {
         wrong(selektor, actualText);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyVisible(Argument selektor, Argument arg)
   {
     try
     {
 
       if (selenium.isVisible(selektor.text()))
       {
         right(selektor);
       }
       else
       {
         wrong(selektor);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyChecked(Argument selektor, Argument arg)
   {
     try
     {
       if (selenium.isChecked(selektor.text()))
       {
         right(selektor);
       }
       else
       {
         wrong(selektor);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyEditable(Argument selektor, Argument arg)
   {
     try
     {
       if (selenium.isEditable(selektor.text()))
       {
         right(selektor);
       }
       else
       {
         wrong(selektor);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyExpression(Argument selektor, Argument arg)
   {
     try
     {
       if (selenium.getExpression(selektor.text()).equals(arg.text()))
       {
         right(selektor);
         right(arg);
       }
       else
       {
         wrong(selektor);
         wrong(arg);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyConfirmation(Argument selektor, Argument arg)
   {
     try
     {
       if (selenium.getConfirmation().equals(selektor.text()))
       {
         right(selektor);
       }
       else
       {
         wrong(selektor);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifySelectedLable(Argument selektor, Argument arg)
   {
     try
     {
       if ((selenium.getSelectedLabel(selektor.text()).equals(arg.text())))
       {
         right(selektor);
         right(arg);
       }
       else
       {
         wrong(selektor);
         wrong(arg, selenium.getSelectedLabel(selektor.text()));
       }
 
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyText(Argument selektor, Argument arg)
   {
     try
     {
       String actualText = selenium.getText(selektor.text());
       if (actualText.equals(arg.text()))
       {
         right(arg);
       }
       else
       {
         wrong(selektor, actualText);
       }
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void verifyElementPresent(Argument selektor, Argument arg)
   {
     String text = selektor.text();
     if (selenium.isElementPresent(text))
     {
       right(selektor);
     }
     else
     {
       wrong(selektor.getCell(), text + " is not present on site");
     }
   }
 
   public void verifyPromptPresent(Argument selektor, Argument arg)
   {
     if (selenium.isPromptPresent())
     {
       right(selektor);
     }
     else
     {
       wrong(selektor.getCell(), "No prompt present on site");
     }
   }
 
   public void verifyAlertPresent(Argument selektor, Argument arg)
   {
     if (selenium.isAlertPresent())
     {
       right(selektor);
     }
     else
     {
       wrong(selektor.getCell(), " No Alert present on site");
     }
   }
 
   // public void verifyCookiePresent(Argument selektor, Argument arg)
   // {
   // String text = selektor.text();
   // if (selenium.isCookiePresent(text))
   // {
   // right(selektor);
   // }
   // else
   // {
   // wrong(selektor.getCell(), text + " is not present on site");
   // }
   // }
 
   public void assertAlert(Argument selektor, Argument arg)
   {
     verifyAlertPresent(selektor, arg);
     if (!selektor.isRight())
     {
       setStop(true);
     }
   }
 
   public void assertAlertNotPresent(Argument selektor, Argument arg)
   {
     verifyAlertPresent(selektor, arg);
     if (selektor.isRight())
     {
       setStop(true);
     }
   }
 
   // public void assertAllButtons(Argument selektor, Argument arg)
   // {
   // selenium.getAllButtons();
   // }
   //
   // public void assertAllFields(Argument selektor, Argument arg)
   // {
   // selenium.getAllFields();
   // }
   //
   // public void assertAllWindowIds(Argument selektor, Argument arg)
   // {
   // selenium.getAllWindowIds();
   // }
   //
   // public void assertAllWindowTitles(Argument selektor, Argument
   // arg)
   // {
   // selenium.getAllWindowNames();
   // }
   //
   // public void assertAttributeFromAllWindows(Argument selektor,
   // Argument
   // arg)
   // {
   // selenium.getAttributeFromAllWindows(selektor.text());
   // }
   //
   // public void assertAllWindowNames(Argument selektor, Argument arg)
   // {
   // selenium.getAllWindowNames();
   // }
   //
   // public void assertBodyText(Argument selektor, Argument arg)
   // {
   // selenium.getBodyText();
   // }
 
   public void assertChecked(Argument selektor, Argument arg)
   {
     verifyChecked(selektor, arg);
     if (!selektor.isRight())
     {
       setStop(true);
     }
   }
 
   public void assertExpression(Argument selektor, Argument arg)
   {
     verifyExpression(selektor, arg);
     if (!selektor.isRight() || !arg.isRight())
     {
       setStop(true);
     }
   }
 
   // public void assertConfirmation(Argument selektor, Argument arg)
   // {
   // // TODO
   // }
   //
   // public void assertEval(Argument selektor, Argument arg)
   // {
   // // TODO
   // }
 
   public void waitForTextPresent(Argument selektor, Argument arg)
   {
     Wait x = new WaitForTextToAppear(selektor.text());
     x.wait("Cannot find text " + selektor.text() + " after " + defaultTimeout
         + " seconds", defaultTimeout * 1000);
   }
 
   public void waitForPageLoad(Argument selektor, Argument arg)
   {
     String wartezeit = arg.getCell().text();
     if (wartezeit == null || wartezeit.length() == 0)
     {
       selenium.waitForPageToLoad(Integer.toString(defaultTimeout));
     }
     else
     {
       selenium.waitForPageToLoad(wartezeit);
     }
   }
 
   public void waitForText(Argument selektor, Argument arg)
   {
     Wait x = new WaitForTextToAppear(arg.text());
     x.wait("Cannot find text " + arg.text() + " after " + defaultTimeout
         + " seconds", defaultTimeout * 1000);
   }
 
   public void waitForChecked(Argument selektor, Argument arg)
   {
     String locator = selektor.text();
     Boolean checked = arg.text().matches("true");
     Wait x = new WaitForChecked(locator, checked);
     try
     {
       x.wait("Checkbox " + locator + " is " + (checked ? "not" : "still")
           + " checked after " + defaultTimeout + " seconds",
           defaultTimeout * 1000);
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void waitForElementPresent(Argument selektor, Argument arg)
   {
     Wait x = new WaitForElementToAppear(selektor.text());
     try
     {
       int timeout = getTimeoutInt(arg.getCell());
       x.wait("Cannot find element " + selektor.text() + " after " + timeout
           + " ms", timeout);
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void waitForFieldValue(Argument selektor, Argument arg)
   {
     Wait x = new WaitForFieldValue(selektor.text(), arg.text());
     try
     {
       x.wait("Cannot find value" + arg.text() + " in " + selektor.text()
           + " after " + defaultTimeout + " seconds", defaultTimeout * 1000);
     }
     catch (Exception e)
     {
       exception(selektor, e);
       return;
     }
   }
 
   public void assertText(Argument selektor, Argument arg)
   {
     verifyText(selektor, arg);
     if (!selektor.isRight() || !arg.isRight())
     {
       setStop(true);
     }
   }
 
   public void assertElementPresent(Argument selektor, Argument arg)
   {
     verifyElementPresent(selektor, arg);
     if (!selektor.isRight())
     {
       setStop(true);
     }
   }
 
   public void assertTextPresent(Argument selektor, Argument arg)
   {
     verifyTextPresent(selektor, arg);
     if (!selektor.isRight())
     {
       setStop(true);
     }
   }
 
   public void assertAlertPresent(Argument selektor, Argument arg)
   {
     verifyAlertPresent(selektor, arg);
     if (!selektor.isRight())
     {
       setStop(true);
     }
   }
 
   private class WaitForTextToAppear extends Wait
   {
     private String text;
 
     public WaitForTextToAppear(String text)
     {
       this.text = text;
     }
 
     public boolean until()
     {
       return selenium.isTextPresent(text);
     }
   }
 
   private class WaitForChecked extends Wait
   {
     private String locator;
 
     private boolean checked;
 
     public WaitForChecked(String locator, boolean checked)
     {
       this.locator = locator;
       this.checked = checked;
     }
 
     public boolean until()
     {
       if (!selenium.isElementPresent(locator))
         return false;
       return selenium.isChecked(locator) == checked;
     }
   }
 
   private class WaitForElementToAppear extends Wait
   {
     private String text;
 
     public WaitForElementToAppear(String text)
     {
       this.text = text;
     }
 
     public boolean until()
     {
       return selenium.isElementPresent(text);
     }
   }
 
   @SuppressWarnings("unused")
   private class WaitForElementToDisappear extends Wait
   {
     private String text;
 
     public WaitForElementToDisappear(String text)
     {
       this.text = text;
     }
 
     public boolean until()
     {
       return (!selenium.isElementPresent(text));
     }
   }
 
   private class WaitForFieldValue extends Wait
   {
     private String text;
 
     private String elementLocator;
 
     public WaitForFieldValue(String element, String value)
     {
       this.text = value;
       this.elementLocator = element;
     }
 
     public boolean until()
     {
       String value = selenium.getValue(elementLocator);
       if (value == null)
       {
         return false;
       }
       return value.indexOf(text) >= 0;
     }
   }
 
   public void pause(int milliseconds)
   {
     try
     {
       Thread.sleep(milliseconds);
     }
     catch (InterruptedException iex)
     {
       iex.getMessage();
     }
   }
   
   public void select(Argument selektor, Argument arg)
   {
 	  selenium.select(selektor.text(), arg.text());
   }
   
   public void selectAndWait(Argument selektor, Argument arg) throws InterruptedException
   {
      select(selektor, arg);
 	  Thread.sleep(2000);
   }
   
 }
