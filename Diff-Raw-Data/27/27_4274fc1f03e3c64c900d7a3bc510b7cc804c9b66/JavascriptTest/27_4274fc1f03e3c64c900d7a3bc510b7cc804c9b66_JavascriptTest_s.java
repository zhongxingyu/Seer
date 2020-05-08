 package li.rudin.rt.test.web;
 
 import java.io.InputStream;
 
 import li.rudin.rt.api.RT;
 import li.rudin.rt.api.RTServer;
 import li.rudin.rt.core.resource.MappedResource;
 import li.rudin.rt.core.resource.ResourceMapping;
 import li.rudin.rt.servlet.test.AbstractServerTest;
 
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 
 public class JavascriptTest extends AbstractServerTest implements MappedResource
 {
 
 	
 	@Test
	//@Ignore
 	public void test() throws Exception
 	{
 		ResourceMapping.add(this);
 
 		HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_17);
 		driver.setJavascriptEnabled(true);
 		
 		driver.get("http://127.0.0.1:" + PORT + "/rt?mode=testindex");
 
 		//Wait until ready
 		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
             public Boolean apply(WebDriver d) {
                 return d.findElement(By.id("state")).getText().equals("ready");
             }
         });
 		
		//Thread.sleep(1500);
 		
 		RTServer rt = RT.getProvider().getInstance();
 		MyBean myBean = new MyBean();
 		myBean.setX(100);
 		rt.send("myId", myBean);
 		
 		//Wait until finished
 		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
             public Boolean apply(WebDriver d) {
                 return d.findElement(By.id("state")).getText().equals("finished");
             }
         });
 		
 
 	}
 	
 	public static class MyBean
 	{
 		private int x;
 
 		public int getX()
 		{
 			return x;
 		}
 
 		public void setX(int x)
 		{
 			this.x = x;
 		}
 	}
 	
 	
 	@Override
 	public InputStream getInputStream()
 	{
 		return JavascriptTest.class.getResourceAsStream("/index.html");
 	}
 
 	@Override
 	public String getName()
 	{
 		return "testindex";
 	}
 
 	@Override
 	public String getContentType()
 	{
 		return "text/html";
 	}
 
 }
