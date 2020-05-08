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
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 //Apache Imports
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Row;
 //JSoup imports
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
 	int selection = 4;
 	
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
 			
 			//Various values to screen the data
 			String clean;
 			double highprice = 0;
 			double mediumPrice = 0;
 			double lowPrice = 0;
 			String temp;
 			//Variables to take in information
 			Document page;
 			Element table;
 			Elements row;
 			Elements item;
 			
 			//Variables for extra information about the set
 			double averageHighPrice = 0;
 			double averageMediumPrice = 0;
 			double averageLowPrice = 0;
 			
 			DecimalFormat format = new DecimalFormat("#.00");
 			
 			
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
 				{	
 					Names[limit]=Names[limit].replaceAll(":", "\\W");
 				}
 				else if(Names[limit].contains("/"))
 				{
 					Names[limit]=Names[limit].replaceAll("/", "\\W");
 				}
 				setname=standard.createSheet(Names[limit]);
 				
 				//Sets up the initial row in the sheet
 				newRow = setname.createRow(0);
 				newRow.createCell(0).setCellValue("Card Name");
 				newRow.createCell(1).setCellValue("High Price");
 				newRow.createCell(2).setCellValue("Medium Price");
 				newRow.createCell(3).setCellValue("Low Price");
 				
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
 							newRow.createCell(0).setCellValue(clean);
 							
 							//This gets the high price
 							temp=item.get(5).text();
 							highprice=removeCommas(temp.substring(1,temp.length()-2));
 							newRow.createCell(1).setCellValue(highprice);
 							averageHighPrice += highprice;
 							
 							//This gets the medium price
 							temp=item.get(6).text();
 							mediumPrice=removeCommas(temp.substring(1,temp.length()-2));
 							newRow.createCell(2).setCellValue(mediumPrice);
 							averageMediumPrice +=mediumPrice;
 							
 							//This gets the low price
 							temp = item.get(7).text();
 							lowPrice = removeCommas(temp.substring(1,temp.length()-2));
 							newRow.createCell(3).setCellValue(lowPrice);
 							averageLowPrice += lowPrice;
 							
 							System.out.println(clean+"  H:$"+highprice+" M:$"+mediumPrice+" L:$"+lowPrice);
 							rowNum++;
 							
 						}
 					}
 					
 				}
 				
 				
 				if(Double.isNaN(averageHighPrice)&&Double.isNaN(averageMediumPrice)&&Double.isNaN(averageLowPrice))
 				{
 					//Finds the averages
 					averageHighPrice /= rowNum;
 					averageMediumPrice /= rowNum;
 					averageLowPrice /= rowNum;
 					
 					//Formats them
 					averageHighPrice = Double.parseDouble(format.format(averageHighPrice));
 					averageMediumPrice = Double.parseDouble(format.format(averageMediumPrice));
 					averageLowPrice = Double.parseDouble(format.format(averageLowPrice));
 					
 					//Inserts the values into the table
 					newRow = setname.getRow(0); 
 					
 					newRow.createCell(4).setCellValue("Average High Price");
 					newRow.createCell(5).setCellValue("Average Medium Price");
 					newRow.createCell(6).setCellValue("Average Low Price");
 					
 					newRow = setname.getRow(1);
 					newRow.createCell(4).setCellValue(averageHighPrice);
 					newRow.createCell(5).setCellValue(averageMediumPrice);
 					newRow.createCell(6).setCellValue(averageLowPrice);
 					
 					System.out.println("Average Prices "+averageHighPrice+" "+averageMediumPrice+" "+averageLowPrice);
 				}
 				
 				//Zeroes them out
 				averageHighPrice = averageMediumPrice = averageLowPrice = 0;
 				
 				//Sets the sheet to auto size columns
 				setname.autoSizeColumn(0);
 				setname.autoSizeColumn(1);
 				setname.autoSizeColumn(2);
 				setname.autoSizeColumn(3);
 				setname.autoSizeColumn(4);
 				setname.autoSizeColumn(5);
 				setname.autoSizeColumn(6);
 
 			}
 			
 			//Creates the date to be added in the output file name. 
 			DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
 		    Date date = new Date();
 		    
 			if(this.selection == 0)
 			{
 				File standardFile = new File("Standard-"+dateFormat.format(date)+"-.xls");
 				FileOutputStream standardOutput = new FileOutputStream(standardFile);
 				standard.write(standardOutput);
 				standardOutput.close();
 			}
 			else if(this.selection == 1)
 			{
 				File standardFile = new File("Extended-"+dateFormat.format(date)+"-.xls");
 				FileOutputStream standardOutput = new FileOutputStream(standardFile);
 				standard.write(standardOutput);
 				standardOutput.close();
 			}
 			else if(this.selection == 2)
 			{
 				File standardFile = new File("Modern-"+dateFormat.format(date)+"-.xls");
 				FileOutputStream standardOutput = new FileOutputStream(standardFile);
 				standard.write(standardOutput);
 				standardOutput.close();
 			}
 			else
 			{
 				File standardFile = new File("All-"+dateFormat.format(date)+"-.xls");
 				FileOutputStream standardOutput = new FileOutputStream(standardFile);
 				standard.write(standardOutput);
 				standardOutput.close();
 			}
 			
 		}
 	
 		catch(Exception e)
 		{
 			e.printStackTrace();
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
 					
 					//Replaces all blank characters with the %20 characters 
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
 					
 					//Since there is a in-consistancy within the database these are necessary
 					if(clean.contains("Starter")&&clean.contains("1999"))
 					{
 						Sets[pointer]="Starter%201999";
 					}
 					else if(clean.contains("Starter")&&clean.contains("2000"))
 					{
 						Sets[pointer]="Starter%202000";
 					}
 					else if(clean.contains("Magic")&&clean.contains("2010"))
 					{
 						Sets[pointer]="Magic%202010";
 					}
 					else if(clean.contains("Planechase")&&clean.contains("2012"))
 					{
 						Sets[pointer]="Planechase%202012";
 					}
 					else if(clean.contains("PDS")&&clean.contains("Fire"))
 					{
 						Sets[pointer]="Premium%20Deck%20Series:%20Fire%20and%20Lightning";
 					}
 					else if(clean.contains("PDS")&&clean.contains("Slivers"))
 					{
 						Sets[pointer]="Premium%20Deck%20Series:%20Slivers";
 					}
 					else if(clean.contains("PDS")&&clean.contains("Graveborn"))
 					{
 						Sets[pointer]="Premium%20Deck%20Series:%20Graveborn";
 					}
 					else if(clean.contains("vs")&&clean.contains("Knights")&&clean.contains("Dragons"))
 					{
 						Sets[pointer]="Duel%20Decks:%20Knights%20vs%20Dragons";
 					}
 					else if(clean.contains("vs."))
 					{
 						Sets[pointer]="Duel%20Decks:%20"+clean;
 					}
 					else if(clean.contains("Sixth")&&clean.contains("Edition"))
 					{
 						Sets[pointer]="Classic%20Sixth%20Edition";
 					}
 					else if(clean.contains("Seventh")&&clean.contains("Edition"))
 					{
 						Sets[pointer]="7th%20Edition";
 					}
 					else if(clean.contains("Eighth")&&clean.contains("Edition"))
 					{
 						Sets[pointer]="8th%20Edition";
 					}
 					else if(clean.contains("Ninth")&&clean.contains("Edition"))
 					{
 						Sets[pointer]="9th%20Edition";
 					}
 					else if(clean.contains("Tenth")&&clean.contains("Edition"))
 					{
 						Sets[pointer]="10th%20Edition";
 					}
 					
 					//Checks to see if the set is a core set or not
 					else if(clean.matches(".*\\d\\d\\d\\d.*"))
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
 		this.selection = selection;
 		
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
 				
 				//Since there is a in-consistancy within the database these two are necessary
 				if(clean.contains("Magic")&&clean.contains("2010"))
 				{
 					Sets[pointer]="Magic%202010";
 				}
 				else if(clean.contains("Ravnica")&&clean.contains("City"))
 				{
 					Sets[pointer]="Ravnica";
 				}
 				else if(clean.contains("Sixth")&&clean.contains("Edition"))
 				{
 					Sets[pointer]="Classic%20Sixth%20Edition";
 				}
 				else if(clean.contains("Seventh")&&clean.contains("Edition"))
 				{
 					Sets[pointer]="7th%20Edition";
 				}
 				else if(clean.contains("Eighth")&&clean.contains("Edition"))
 				{
 					Sets[pointer]="8th%20Edition";
 				}
 				else if(clean.contains("Ninth")&&clean.contains("Edition"))
 				{
 					Sets[pointer]="9th%20Edition";
 				}
 				else if(clean.contains("Tenth")&&clean.contains("Edition"))
 				{
 					Sets[pointer]="10th%20Edition";
 				}
 				//Checks to see if the set is a core set or not
 				else if(clean.matches(".*\\d\\d\\d\\d.*"))
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
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	//If a core set is detected then the name is edited to fit the TCG format
 	private String coreSet(String input)
 	{
 		String output=input+"%20(M"+input.substring(input.length()-2)+")";
 		return output;
 	}
 	
 	private Double removeCommas(String input)
 	{
 		double output=0;
 		boolean found = false;
 		for(int start =0; start<input.length(); start++)
 		{
 			if(input.charAt(start)==',')
 			{
 				output=Double.parseDouble(input.substring(0,start)+input.substring(start+1));
 				found=true;
 				break;
 			}
 		}
 		
 		if(found)
 			return output;
 		else
 			return(Double.parseDouble(input));
 		
 		
 	}
 	
 
 }
