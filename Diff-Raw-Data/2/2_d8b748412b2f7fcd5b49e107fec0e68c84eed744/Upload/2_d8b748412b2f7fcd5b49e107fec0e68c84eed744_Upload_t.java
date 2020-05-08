 package upload;
 
 import static org.testng.AssertJUnit.assertEquals;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.support.PageFactory;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.annotations.Test;
 
 import pageFactory.ContentPage;
 import basic.BasicTestCase;
 
 public class Upload extends BasicTestCase {
 	private ContentPage obj = PageFactory.initElements(getWebDriver(), ContentPage.class);
 	WebDriverWait wait = new WebDriverWait(driver, 30);
 	
 	
 //Upload track                     
 @Test(priority = 1,dependsOnGroups={"Login"})
 	public void UploadTrack () throws Exception {
 	driver.get(musicUrl);
 	obj.Upload.click(); //go to upload link on head
 	wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(@id,'SWFUpload_0')]"))).click();//click on upload button
	Runtime.getRuntime().exec("resource/up.exe"); // use script 
 	wait.until(ExpectedConditions.elementToBeClickable(By.xpath(".//*[@id='file_complete_status']/a"))).click(); // go to play list after download
 	driver.navigate().refresh();
 	assertEquals("Неизвестный исполнитель — test" ,driver.findElement(By.xpath("//*[@class='left']//*[@class='advanced_playlist']/li[1]")).getAttribute("audio_name")); //check track
 	obj.DelTrack.click(); //delete track from play list
 	}
 }
