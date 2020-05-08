 package download;
 
 import java.io.File;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.TimeoutException;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.Reporter;
 import org.testng.annotations.Test;
 
 import basic.BasicTestCase;
 
 public class Download extends BasicTestCase {
 	
 	String filePath = null ; 
 	
 	
 
 	@Test(description ="Проверяет скачку треков ")
 	public void download () throws Exception {
 		WebDriverWait wait = new WebDriverWait(driver, 100);
 		driver.get(musicUrl);
	
 		//click to download btn
 		driver.findElement(By.xpath("//*[@class='addthis control download download-track-counter']")).click(); //click to download btn
 		String errorUrl = driver.getCurrentUrl();
		
 		/* Check PC */
 		String userName = System.getProperty("user.name");
 			  filePath = "C:\\Users\\"+userName+"\\Downloads\\";
			  clearDownloadDir(filePath);
 		
 			
 		/* Switch to new window opened */
 		for(String winHandle : driver.getWindowHandles()){
 		    driver.switchTo().window(winHandle);
 		}
 		/* click on download link */
 	    try {  
 	    	driver.findElement(By.xpath("//*[@class='one-track-block']/a[1]")).click(); 
 	      	}
 	    	catch (NoSuchElementException e) {
 	    	  Reporter.log("На странице "+driver.getCurrentUrl()+ " неотображается ссылка <<Скачать песню>>",true);
 	      	}
 	       
 	    	driver.get("chrome://downloads/");
 	        Reporter.log("Ссылка на загрузку трека "+driver.findElement(By.xpath("//*[@class='src-url']")).getText(),true);
 	    
 	      /* wait until track will be downloaded */
 	      try{
 	        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@class='controls']/a[1]")));
 	        	if (driver.findElement(By.xpath("//*[@class='status']")).getText().equals("Неизвестная сетевая ошибка.")) {
 	        		Reporter.log("На странице "+errorUrl+" не скачался трек по причине "+driver.findElement(By.xpath("//*[@class='status']")).getText(),true);
 	        		throw new NullPointerException ();
 	        		} 		else {
 	        				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@class='controls']/a[1]")));
 	        				}
 	      }
 	      catch (TimeoutException e) {
 	    	  Reporter.log("На странице "+errorUrl+" не скачался трек по причине "+driver.findElement(By.xpath("//*[@class='status']")).getText(),true);
 	    	  throw new NullPointerException ();
 	      }
 	      
 	      /* getting name of downloaded track */
 	      String fileName = driver.findElement(By.xpath("//*[@class='title-area']/a")).getText();
 	      // file path
 	      File file = new File(filePath+fileName); 
 	    
 	      /* check if file not existing print error */
 	      if  (!file.exists()) {
 		    	Reporter.log("Файл не существует",true);
 		    	throw new NullPointerException(); 
 		    }   /* if exist delete file  */ 
 		    	else file.delete();
 	      		Reporter.log("Трек успешно скачан, и был удален");
 		}
 	
	public void clearDownloadDir (String folderPath) {
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        String fileNames  = listOfFiles[i].getName();
		        	if (fileNames.contains("(music.nur.kz).mp3.crdownload")) {
		        		listOfFiles[i].delete();
		        		Reporter.log("Файлы удалены",true);
		        	}
		      
		      	}
		    }
	}
	
 }
