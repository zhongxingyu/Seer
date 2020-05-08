 package com.purdue.nhitze.purdueschedulescraper;
 
 import java.io.*;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.openqa.selenium.By;
//import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 import java.io.File;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
  
//import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class PUScheduleScraper
 {
	@SuppressWarnings("deprecation")
 	public static void main(String[] args)
 	{
 		int classCount = 0;
 		int sectionCount = 0;
 		int deptCount = 0;
 		
 		System.out.println("Attempting to connect driver...");
 		// Connect to the page
 		WebDriver driver = new HtmlUnitDriver();
 		System.out.println("Driver created.\n");
 		try
 		{
 			System.out.println("Attempting connection to course list...");
 			driver.get("https://selfservice.mypurdue.purdue.edu/prod/bwckschd.p_disp_dyn_sched");
 			System.out.println("Connection established.\n");
 		} catch(Exception ex)
 		{
 			System.out.println("Error connecting to selfservice schedule!!");
 			System.exit(0);
 		}
 		
 		// Figure out which semester we are scheduling for
 		Date today = new Date();
 		String season = "";
 		if (today.getMonth() > 6) season = "Spring";
 		else season = "Fall";
 		//TODO Temp hack
 		season = "Spring";
 		//End temp hack
 		String term = season + " " + (today.getYear() + 1900);
 		System.out.println("Searching for term " + term);
 		try
 		{
 			WebElement select = driver.findElement(By.xpath("//select[@name='p_term']"));
 			List<WebElement> allOptions = select.findElements(By.tagName("option"));
 			for (WebElement option : allOptions)
 			{
 				if (option.getText().equals(term))
 				{
 					option.click();
 				}
 			}
 		} catch (Exception ex)
 		{
 			System.out.println("Error clicking option " + term);
 		}
 		
 		System.out.println("Term found.\n");
 		
 		WebElement submit = driver.findElement(By.xpath("//input[@type='submit']"));
 		submit.click();
 		// Now select every department of classes and submit to get a list of all courses
 		String campus = "West Lafayette";
 		try
 		{
 			WebElement select = driver.findElement(By.xpath("//select[@name='sel_subj']"));
 			List<WebElement> allOptions = select.findElements(By.tagName("option"));
 			//int i = 0; //Debug
 			for (WebElement option : allOptions)
 			{
				//TODO remove this limiting debug loop and the counter above
 				//if (i < 1)
 				//{
 					System.out.println("Clicking option " + option.getText());
 					option.click();
 					//i++;
 				//}
 			}
 			
 			//Narrow down by West Lafayette Campus only.
 			select = driver.findElement(By.xpath("//select[@name='sel_camp']"));
 			allOptions = select.findElements(By.tagName("option"));
 			for (WebElement option : allOptions)
 			{
 				if (option.getText().equals(campus))
 				{
 					option.click();
 				}
 			}
 		} catch (Exception ex)
 		{
 			System.out.println("Error selecting class options!");
 		}
 		submit = driver.findElement(By.xpath("//input[@type='submit']"));
 		System.out.println("Attempting to fetch class listings...");
 		submit.click();
 		System.out.println("Class listings retrieved.\n");
 		System.out.println("Parsing HTML...");
 		List<WebElement> titles = driver.findElements(By.xpath("//th[@class='ddlabel']"));
 		System.out.println("Title information found.\n");
 		System.out.println("Retrieving class information...");
 		List<WebElement> information = driver.findElements(By.xpath("//td[@class='dddefault']"));
 		System.out.println("Class information retrieved.");
 		List<String> classes = new LinkedList<String>();
 		List<String> depts = new LinkedList<String>();
 		
 		//Begin writing the XML
 		DocumentBuilderFactory docFactory;
 		DocumentBuilder docBuilder;
 		Document doc;
 		Element rootElement;
 		try
 		{
 			docFactory = DocumentBuilderFactory.newInstance();
 			docBuilder = docFactory.newDocumentBuilder();
 	 
 			// root element <purdue>
 			doc = docBuilder.newDocument();
 			rootElement = doc.createElement("purdue");
 			doc.appendChild(rootElement);
 		} catch(Exception e){
 			System.out.println("Error initializing the XML writer!!");
 			return;
 			}
 		
 		Element dept = null;
 		Element aClass = null;
 		boolean linked = false;
 		int i = -1;
 		for (WebElement element : titles)
 		{
 			i++;
 			//Title line
 			List<WebElement> titleElements = element.findElements(By.xpath("a"));
 			String[] titleInfo = titleElements.get(0).getText().split(" - ");
 			//If there's more than 1 element, that means that there are linked sections!
 			linked = (titleElements.size() > 1) ? true : false;
 			//Some titles have a dash in it. If that's the case, we need to combine the parts that were split.
 			if (titleInfo.length > 4)
 			{
 				titleInfo[0] = titleInfo[0] + " - " + titleInfo[1];
 				titleInfo[1] = titleInfo[2];
 				titleInfo[2] = titleInfo[3];
 				titleInfo[3] = titleInfo[4];
 			}
 			//See if the department is unique
 			// Get the department
 			String department = titleInfo[2].split(" ")[0];
 			if (!(depts.contains(department)))
 			{
 				deptCount++;
 				depts.add(department);
 				//Write the department XML
 				try
 				{
 					dept = doc.createElement("department");
 					rootElement.appendChild(dept);
 					dept.setAttribute("name", department);
 				} catch (Exception e) {
 					System.out.println("Error writing XML department!!");
 					return;
 				}
 				System.out.println(department);
 			}
 			
 			//See if the class is unique
 			if (!(classes.contains(titleInfo[2])))
 			{
 				classes.add(titleInfo[2]);
 				classCount++;
 				
 				//Lets get the course info.
 				String semesterID = "";
 				switch (season)
 				{
 					case "Spring":
 						semesterID = (today.getYear() + 1900)+"20";
 						break;
 					case "Summer":
 						semesterID = (today.getYear() + 1900)+"30";
 						break;
 					case "Fall":
 						semesterID = (today.getYear() + 1899)+"10";
 						break;
 				}
 				String infoURL = "https://selfservice.mypurdue.purdue.edu/prod/bwckctlg.p_disp_course_detail?cat_term_in="+semesterID+"&subj_code_in=" + titleInfo[2].split(" ")[0] + "&crse_numb_in=" + titleInfo[2].split(" ")[1];
 				WebDriver newDriver = new HtmlUnitDriver();
 				newDriver.get(infoURL);
 				WebElement el = newDriver.findElement(By.xpath("//td[@class='nttitle']"));
 				String courseTitle = el.getText().split(" - ")[1];
 				el = newDriver.findElement(By.xpath("//td[@class='ntdefault']"));
 				String descriptiontemp = el.getText().split("\n")[0];
 				String description = descriptiontemp.substring(descriptiontemp.indexOf(". ") + 2);
 				String credits = descriptiontemp.substring(0, descriptiontemp.indexOf(". "));
 				newDriver.close();
 				
 				//Write the class XML
 				try
 				{
 					aClass = doc.createElement("class");
 					dept.appendChild(aClass);
 					aClass.setAttribute("course", titleInfo[2]);
 					aClass.setAttribute("name", courseTitle);
 					aClass.setAttribute("description", description);
 					aClass.setAttribute("credits", credits);
 				} catch (Exception e) {
 					System.out.println("Error XML writing class info!!");
 					return;
 				}
 				System.out.println(titleInfo[2]);
 				System.out.println(courseTitle);
 			}
 			
 			//Get the section info
 			//Need to gather all infoTables and loop through each for the rows.
			if (i == information.size()) break;
 			List<WebElement> infoTables = information.get(i).findElements(By.xpath("//table[@summary='This table lists the scheduled meeting times and assigned instructors for this class..']/tbody"));
 			WebElement infoTable = infoTables.get(i);
 			sectionCount++;
 			//Time
 			String time = infoTable.findElement(By.xpath("tr/td[@class='dddefault'][2]")).getText();
 			//CRN
 			String crn = titleInfo[1];
 			//System.out.println("Section " + crn); // I think this is slowing us down
 			//Days
 			String days = infoTable.findElement(By.xpath("tr/td[@class='dddefault'][3]")).getText();
 			//Type
 			String type = infoTable.findElement(By.xpath("tr/td[@class='dddefault'][6]")).getText();
 			//LinkID
 			String linkedText = "", linkedToText = "";
 			//Instructor
 			String instructor = infoTable.findElement(By.xpath("tr/td[@class='dddefault'][7]")).getText();
 			if (linked) 
 			{
 				linkedText = element.getText().replace(" - ", "");
 				linkedText = linkedText.split(" ")[StringUtils.countMatches(linkedText, " ") - 4];
 				//LinkedToID
 				linkedToText = element.getText();
 				linkedToText = linkedToText.substring(linkedToText.indexOf('(') + 1);
 				linkedToText = linkedToText.substring(0, linkedToText.length() - 1);
 			}
 			//Availability
 			String availability = titleElements.get(0).getAttribute("href");
 			
 			//Add to the XML tree
 			Element section = doc.createElement("section");
 			aClass.appendChild(section);
 			section.setAttribute("time", time);
 			section.setAttribute("crn", crn);
 			section.setAttribute("days", days);
 			section.setAttribute("type", type);
 			section.setAttribute("linkid", linked ? linkedText : "0");
 			section.setAttribute("linkedtoid", linked ? linkedToText : "0");
 			section.setAttribute("instructor", instructor);
 			section.setAttribute("availability", availability);
 		}
 		
 		// write the content into xml file
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 		Transformer transformer;
 		try
 		{
 			transformer = transformerFactory.newTransformer();
 		} catch (TransformerConfigurationException e)
 		{
 			System.out.println("Error transforming to XML!!");
 			e.printStackTrace();
 			return;
 		}		
 		
 		//Perform final XML printing.
 		DOMSource source = new DOMSource(doc);
 		String path = System.getProperty("user.home") + "\\classdata.xml";
 		File file = new File(path);
 		if (file.exists() == false)
 		{
 			try
 			{
 				file.createNewFile();
 			} catch (IOException e)
 			{
 				System.out.println("Failed to create XML file.");
 				e.printStackTrace();
 				return;
 			}
 		}
 		System.out.println("Counted "+sectionCount+" sections in "+classCount+" classes from "+deptCount+" departments.");
 		System.out.println("File created: " + path);
 		StreamResult result = new StreamResult(new File(path));
  
 		try
 		{
 			transformer.transform(source, result);
 		} catch (TransformerException e)
 		{
 			System.out.println("Error performing transform to XML!!");
 			e.printStackTrace();
 			return;
 		}
 	 
 		System.out.println("XML file generated.");
 		
 		driver.close();
 	}
 }
