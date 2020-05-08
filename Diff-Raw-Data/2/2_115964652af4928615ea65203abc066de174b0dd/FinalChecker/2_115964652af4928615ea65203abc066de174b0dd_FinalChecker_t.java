 package tvdb;
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.Select;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 
 public class FinalChecker {
 
 	/**
 	 * @param args
 	 * @throws InterruptedException 
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws InterruptedException, IOException {
 		FirefoxProfile profile = new FirefoxProfile();
 		profile.setPreference("print.print_footerleft", "");
 		profile.setPreference("print.print_footerright", "");
 		profile.setPreference("print.print_headerleft", "");
 		profile.setPreference("print.print_headerright", "");
 		profile.setPreference("print_printer", "Bullzip PDF Printer");
 		profile.setPreference("printer_Bullzip_PDF_Printer.print_footerleft", "");
 		profile.setPreference("printer_Bullzip_PDF_Printer.print_footerright", "");
 		profile.setPreference("printer_Bullzip_PDF_Printer.print_headerleft", "");
 		profile.setPreference("printer_Bullzip_PDF_Printer.print_headerright", "");
 		profile.setPreference("print.always_print_silent", true);
 		
 		WebDriver driver = new FirefoxDriver(profile);
         
 		Utils.openTvdb(driver, null);
 		String mainUrl = driver.getCurrentUrl();
 
 		// U
 		Calendar now = Calendar.getInstance();
 		int year = now.get(Calendar.YEAR) - 1911;
 		int month = now.get(Calendar.MONTH) > 7 ? 10 : 3;
 		
 		driver.findElement(By.partialLinkText("   ")).click();
 		
		int currentTableIdx = 0;
 		
 		eachTvdbTable:
 		while(true) {
 			Select tabSelect = new Select(driver.findElement(By.cssSelector("select[name='TabName']")));
 			int tableCount = tabSelect.getOptions().size();
 
 			String tableName = tabSelect.getFirstSelectedOption().getAttribute("value");
 			
 			// look for the correct HTML table
 			for(WebElement htmlTable: driver.findElements(By.tagName("table"))) {
 				List<WebElement> trs = htmlTable.findElements(By.tagName("tr"));
 				
 				if(trs.size() == 0)
 					continue;
 				
 				List<WebElement> tableHeaderTds = trs.get(0).findElements(By.tagName("td"));
 				
 				if(tableHeaderTds.size() >= 5 && tableHeaderTds.get(2).getText().equals("U]")) {
 					trs.remove(0); // remove table header
 					
 					// look for not-yet-confirmed records
 					for(WebElement tr: trs) {
 						List<WebElement> tds = tr.findElements(By.tagName("td"));
 						WebElement confirmChkBox = tds.get(3).findElement(By.cssSelector("input[type='checkbox']"));
 						if(confirmChkBox.isEnabled()) {
 							// a record found to be not-yet-confirmed 
 							if(tds.get(4).getText().equals(""+year+":"+month)) {
 								// 
 								if(new File(Utils.TVDB_DIR, "򥻸ƪ/"+tableName+"-L.txt").exists()) {
 									// T{LLƪ
 									System.out.println(":L: ["+tableName+"]: => T{");
 									confirmChkBox.click();
 									confirmChkBox.submit();
 									driver.switchTo().alert().accept();
 									continue eachTvdbTable; // restart the outermost loop since the form has been submitted
 								}
 								else {
 									System.err.println(":L: ["+tableName+"]: => |T{");
 								}
 							}
 							else {
 								System.err.println("v:L: ["+tableName+"]["+tds.get(4).getText()+"]");
 							}
 						}
 					}
 					break;
 				}
 			}
 			
 			currentTableIdx++;
 			
 			if(currentTableIdx >= tableCount)
 				break;
 			
 			// change to the next table
 			
 			removeButton(driver);
 			tabSelect.selectByIndex(currentTableIdx);
 			waitForButton(driver);
 		}
 
 		// Uˮ`
 		
 		driver.get(mainUrl);
 		driver.findElement(By.partialLinkText("JU@")).click();
 
 		boolean allOk = true;
 		boolean changed = false;
 		
 		// UT
 		List<WebElement> trs = driver.findElements(By.tagName("table")).get(0).findElements(By.tagName("tr"));
 		trs.remove(0); // remove table header
 		
 		int trIdx = 1;
 
 		for (WebElement tr : trs) {
 			List<WebElement> tds = tr.findElements(By.tagName("td"));
 			String tableName = tds.get(1).getText();
 			String unitStr = tds.get(5).getText().trim();
 			if(unitStr.isEmpty()) {
 				System.err.println("["+tableName+"]: ]w");
 				allOk = false;
 				((JavascriptExecutor)driver).executeScript("document.getElementsByTagName('table')[0].getElementsByTagName('tr')["+trIdx+"]." +
 						"getElementsByTagName('td')[5].style.backgroundColor='#ff0000';");
 			}
 			if(tds.get(6).getText().trim().equals("L") && tds.get(7).getText().trim().isEmpty()) {
 				System.err.println("["+tableName+"]: |T{L");
 				allOk = false;
 				((JavascriptExecutor)driver).executeScript("document.getElementsByTagName('table')[0].getElementsByTagName('tr')["+trIdx+"]." +
 						"getElementsByTagName('td')[7].style.backgroundColor='#ff0000';");
 			}
 			trIdx++;
 		}
 		
 		// Uˮ֩
 		trs = driver.findElements(By.tagName("table")).get(1).findElements(By.tagName("tr"));
 		trs.remove(0); // remove table primary header
 		trs.remove(0); // remove table secondary header
 		
 		trIdx = 2;
 
 		for (WebElement tr : trs) {
 			List<WebElement> tds = tr.findElements(By.tagName("td"));
 			String checkerIdx = tds.get(0).getText();
 			String checkerName = tds.get(2).getText();
 			String unitStr = tds.get(5).getText().trim();
 			if(unitStr.isEmpty()) {
 				System.err.println("("+checkerIdx+") ["+checkerName+"]: ]wˮֳ");
 				allOk = false;
 				((JavascriptExecutor)driver).executeScript("document.getElementsByTagName('table')[1].getElementsByTagName('tr')["+trIdx+"]." +
 						"getElementsByTagName('td')[5].style.backgroundColor='#ff0000';");
 			}
 			Select checked = new Select(tds.get(6).findElement(By.tagName("select")));
 			if(checked.getFirstSelectedOption().getAttribute("value").equals("n")) {
 				checked.selectByValue("y");
 				System.out.println("("+checkerIdx+") ["+checkerName+"]: ]wˮ");
 				changed = true;
 			}
 			trIdx++;
 		}
 		
 		if(allOk) {
 			if(changed) {
 				driver.findElement(By.cssSelector("input[type='submit'][value='Twxs']")).click();
 		        ((JavascriptExecutor)driver).executeScript("alert('Done!')");
 			}
 			else {
 		        ((JavascriptExecutor)driver).executeScript("alert('Nothing to do.')");
 			}
 		}
 		else {
 	        ((JavascriptExecutor)driver).executeScript("alert('Эק~, U  <Twxs> s')");
 		}
 	}
 
 	private static void waitForButton(WebDriver driver) {
 		(new WebDriverWait(driver, 10)).until(
         		ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='button'][value='']")));
 	}
 
 	private static void removeButton(WebDriver driver) {
 		((JavascriptExecutor)driver).executeScript(
 				"var inputs = document.getElementsByTagName('input');" +
 				"for(var i=inputs.length-1; i>=0; i--) {" +
 				"  if(inputs[i].type=='button' && inputs[i].value=='') {" +
 				"    inputs[i].parentNode.removeChild(inputs[i]);" +
 				"  }" +
 				"}");
 	}
 
 }
