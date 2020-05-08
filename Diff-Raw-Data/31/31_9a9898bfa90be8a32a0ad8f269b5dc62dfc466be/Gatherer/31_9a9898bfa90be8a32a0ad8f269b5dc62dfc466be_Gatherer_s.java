 /*  Magic Builder
  * 	A simple java program that grabs the latest prices and displays them per set.
     Copyright (C) 2013  Manuel Gonzales Jr.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
 
 package com.macleod2486.magicbuilder;
 
 import java.io.File;
 import java.io.FileOutputStream;

 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class Gatherer 
 {
 	private String Sets[]=new String[300];
 	private String Names[]=new String[300];
 	
 	//Urls of each of the different formats from the Wizards website
 	private String structuredFormat[]={"https://www.wizards.com/magic/magazine/article.aspx?x=judge/resources/sfrstandard",
							   "https://www.wizards.com/Magic/TCG/Resources.aspx?x=judge/resources/sfrextended",
							   "https://www.wizards.com/Magic/TCG/Resources.aspx?x=judge/resources/sfrmodern"};
 	//Url of all the formats
 	private String allFormat="http://store.tcgplayer.com/magic?partner=WWWTCG";
 	private int pointer=0;
 	int year=0;
 	
 	//Connects to the TCG website to then gather the prices
 	public void tcg()
 	{
 		try
 		{
 			//Keeps track of the rows within the sheet as data is being written
 			int rowNum;
 			
 			//Creates a excel file for the information to be stored
 			HSSFWorkbook standard = new HSSFWorkbook();
 			HSSFSheet setname;
 			Row newRow;
 			Cell info;
 			
 			//Various values to screen the data
 			String clean;
 			String highprice;
 			String mediumPrice;
 			String lowPrice;
 			
 			//Variables to take in information
 			Document page;
 			Element table;
 			Elements row;
 			Elements item;
 			
 			/*
 			 * Grabs the modified set values to then be used for the website url format
 			 * Not the most effecient for loop but will be modified as time goes on.
 			 */
 			for(int limit=0; limit<pointer; limit++)
 			{
 				rowNum=0;
 				
 				System.out.println("\nSet name: "+Names[limit]+"\n");
 				
 				//Creates a new sheet per set after it filters out bad characters
 				if(Names[limit].contains(":"))
					Names[limit].replace(":", " ");
 				setname=standard.createSheet(Names[limit]);
 				
 				//Sets up the initial row in the sheet
 				newRow = setname.createRow(0);
 				info=newRow.createCell(0);
 				info.setCellValue("Card Name");
 				info=newRow.createCell(1);
 				info.setCellValue("High Price");
 				info=newRow.createCell(2);
 				info.setCellValue("Medium Price");
 				info=newRow.createCell(3);
 				info.setCellValue("Low Price");
 				
 				
 				/*Each modified string value is then put in the following url to then parse
 				  the information from it. */
 				
 				page = Jsoup.connect("http://magic.tcgplayer.com/db/price_guide.asp?setname="+Sets[limit]).get();
 				table = page.select("table").get(2);
 				row=table.select("tr");
 				
 				
 				//Grabs each card that was selected
 				for(Element tableRow: row)
 				{
 					//Gets the first row 
 					item=tableRow.select("td");
 					clean=item.get(0).text();
 					
 					//Filters out land cards
 					if(!clean.contains("Forest")&&!clean.contains("Mountain")&&!clean.contains("Swamp")&&!clean.contains("Island")&&!clean.contains("Plains")&&!clean.isEmpty())
 					{
 						if(item.get(5).text().length()>2&&item.get(6).text().length()>2&&item.get(6).text().length()>2)
 						{
 							//Creates new row in the sheet
 							newRow = setname.createRow(rowNum+1);
 							
 							//Gets the name of the card
 							clean=clean.substring(1);
 							info=newRow.createCell(0);
 							info.setCellValue(clean);
 							
 							//This gets the high price
 							highprice=item.get(5).text();
 							highprice=highprice.substring(1,highprice.length()-2);
 							info=newRow.createCell(1);
 							info.setCellValue(highprice);
 							
 							//This gets the medium price
 							mediumPrice=item.get(6).text();
 							mediumPrice=mediumPrice.substring(1,mediumPrice.length()-2);
 							info=newRow.createCell(2);
 							info.setCellValue(mediumPrice);
 							
 							//This gets the low price
 							lowPrice = item.get(7).text();
 							lowPrice = lowPrice.substring(1,lowPrice.length()-2);
 							info=newRow.createCell(3);
 							info.setCellValue(lowPrice);
 							
 							
 							System.out.println(clean+"  H:$"+highprice+" M:$"+mediumPrice+" L:$"+lowPrice);
 							rowNum++;
 							
 						}
 					}
 					
 				}
				
 			}
 			
 			//Writes the workbook to the file and closes it
 			File standardFile = new File("Structured.xls");
 			FileOutputStream standardOutput = new FileOutputStream(standardFile);
 			standard.write(standardOutput);
 			standardOutput.close();
 			
 		}
 	
 		catch(Exception e)
 		{
 			System.out.println("Error! "+e);
 			if(e.toString().contains("Status=400"))
 			{
 				System.out.println("That webpage does not exist!");
 			}
 			else if(e.toString().contains("SocketTimeout"))
 			{
 				System.out.println("Your connection timed out");
 			}
 		}
 		
 	}
 	//Similer to the other method but selects every set from a different location
 	public boolean gatherAll()
 	{
 		String clean;
 		char check;
 		
 		try
 		{
 			//Grabs the webpage then selects the list of sets
 			Document page = Jsoup.connect(allFormat).get();
 			Elements article = page.select("div#advancedSearchSets");
 			Elements table = article.select("table");
 			Elements tableBody = table.select("tbody");
 			Elements tableRow = tableBody.select("tr");
 			Elements list;
 			this.pointer = 0;
 			
 			//Loops through each item within the list of available sets
 			for(Element item: tableRow)
 			{
 				//Selects all the links within the table rows
 				list = item.select("a[href]");
 				
 				for(Element itemName: list)
 				{	
 					Names[pointer]=itemName.text();
 					clean = itemName.text().replaceAll(" ", "%20");
 					
 					//Further processes the items within found on the site
 					for(int length=0; length<clean.length(); length++)
 					{
 						check=clean.charAt(length);
 						if(check=='(')
 						{
 							clean = clean.substring(0,length-3);
 						}
 					}
 					
 					//Checks to see if the set is a core set or not
 					if(clean.matches(".*\\d\\d\\d\\d.*"))
 					{
 						Sets[pointer]=coreSet(clean);
 						
 					}
 					else
 					{
 						Sets[pointer]=clean;
 					}
 					this.pointer++;
 				}
 			}
 			
 			return true;
 		}
 		catch(Exception e)
 		{
 			System.out.println("Error! "+e);
 			return false;
 		}
 	}
 	
 	public boolean gather(int selection)
 	{
 		String clean;
 		char check;
 		
 		try
 		{
 			//Grabs the webpage then selects the list of sets
 			Document page = Jsoup.connect(structuredFormat[selection]).get();
 			Elements article = page.select("div.article-content");
 			Element table = article.select("ul").get(0);
 			Elements list = table.select("li");
 			pointer = 0;
 			//Loops through each item within the list of available standard sets
 			for(Element item: list)
 			{
 				Names[pointer]=item.text();
 				System.out.println(Names[pointer]);
 				clean = item.text().replaceAll(" ", "%20");
 				
 				//Further processes the items within found on the site
 				for(int length=0; length<clean.length(); length++)
 				{
 					check=clean.charAt(length);
 					if(check=='(')
 					{
 						clean = clean.substring(0,length-3);
 					}
 				}
 				
 				//Checks to see if the set is a core set or not
 				if(clean.matches(".*\\d\\d\\d\\d.*"))
 				{
 					Sets[pointer]=coreSet(clean);
 					
 				}
 				else
 				{
 					Sets[pointer]=clean;
 				}
 				
 				this.pointer++;
 			}
 			
 			return true;
 		}
 		catch(Exception e)
 		{
 			System.out.println("Error! "+e);
 			return false;
 		}
 	}
 	
 	//If a core set is detected then the name is edited to fit the TCG format
 	private String coreSet(String input)
 	{
 		String output=input+"%20(M"+input.substring(input.length()-2)+")";
 		return output;
 	}

 
 }
