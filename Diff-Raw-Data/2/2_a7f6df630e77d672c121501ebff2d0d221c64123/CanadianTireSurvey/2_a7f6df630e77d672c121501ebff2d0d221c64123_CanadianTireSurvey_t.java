 package org.cpower.surveyor;
 
 import java.util.Map;
 import org.openqa.selenium.*;
 
 public class CanadianTireSurvey extends Survey {
 
 	public CanadianTireSurvey(Map<String, String> map) {
 		super(map);
 	}
 
 	@Override
 	public void submit() throws Exception {
 		driver.get("http://ww16.empathica.com/sxml/cantire/retailSurvey/landing.jsp");
 		driver.findElement(By.cssSelector("img")).click();
 
 		String[] time = map.get(TIME).split(":");
 		driver.findElement(By.id("tf_Hour")).clear();
 		driver.findElement(By.id("tf_Hour")).sendKeys(time[0]);
 		driver.findElement(By.id("tf_Min")).clear();
 		driver.findElement(By.id("tf_Min")).sendKeys(time[1]);
 
 		String[] price = map.get(PRICE).split("\\.");
 		driver.findElement(By.id("tf_Dollar")).clear();
 		driver.findElement(By.id("tf_Dollar")).sendKeys(price[0]);
 		driver.findElement(By.id("tf_Cent")).clear();
		driver.findElement(By.id("tf_Cent")).sendKeys(price[1]);
 
 		String[] code = map.get(CODE).split("-");
 		driver.findElement(By.id("tf_csi1")).clear();
 		driver.findElement(By.id("tf_csi1")).sendKeys(code[0]);
 		driver.findElement(By.id("tf_csi2")).clear();
 		driver.findElement(By.id("tf_csi2")).sendKeys(code[1]);
 		driver.findElement(By.id("tf_csi3")).clear();
 		driver.findElement(By.id("tf_csi3")).sendKeys(code[2]);
 		driver.findElement(By.id("tf_csi4")).clear();
 		driver.findElement(By.id("tf_csi4")).sendKeys(code[3]);
 		driver.findElement(By.cssSelector("#apDiv8 > a > img")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.xpath("(//input[@name='SERVICE_VEHICLE'])[2]")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.xpath("(//input[@name='OV_SAT_NEW'])[2]")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.xpath("(//input[@name='SELF_CASH'])[2]")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.name("CASHIER_THANKYOU")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.xpath("(//input[@name='TEAM_CARE'])[3]")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.xpath("(//input[@name='EMPLOYEE_WOW'])[2]")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.xpath("(//input[@name='ADDITIONAL_QUESTIONS'])[2]")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.name("GENDER")).click();
 		driver.findElement(By.name("AGE")).clear();
 		driver.findElement(By.name("AGE")).sendKeys("1982");
 		driver.findElement(By.xpath("(//input[@name='INCOME'])[6]")).click();
 		driver.findElement(By.xpath("(//input[@name='CHILDRENUAGE'])[2]")).click();
 		driver.findElement(By.xpath("(//input[@name='CTCREDITCARD'])[2]")).click();
 		driver.findElement(By.xpath("(//input[@name='PAYWITH'])[3]")).click();
 		driver.findElement(By.xpath("(//input[@name='CTROADASSISTMEMBER'])[2]")).click();
 		driver.findElement(By.cssSelector("img[alt=\"Next\"]")).click();
 		driver.findElement(By.name("FirstName")).clear();
 		driver.findElement(By.name("FirstName")).sendKeys(map.get(FIRST_NAME));
 		driver.findElement(By.name("LastName")).clear();
 		driver.findElement(By.name("LastName")).sendKeys(map.get(LAST_NAME));
 		driver.findElement(By.name("PostCode")).clear();
 		driver.findElement(By.name("PostCode")).sendKeys(map.get(POSTAL_CODE));
 		driver.findElement(By.name("TelephoneAreaCode")).clear();
 		driver.findElement(By.name("TelephoneAreaCode")).sendKeys(map.get(PHONE_AREA));
 		driver.findElement(By.name("TelephoneNumber")).clear();
 		driver.findElement(By.name("TelephoneNumber")).sendKeys(map.get(PHONE_LOCAL));
 		driver.findElement(By.cssSelector("img[alt=\"Send\"]")).click();
 	}
 }
