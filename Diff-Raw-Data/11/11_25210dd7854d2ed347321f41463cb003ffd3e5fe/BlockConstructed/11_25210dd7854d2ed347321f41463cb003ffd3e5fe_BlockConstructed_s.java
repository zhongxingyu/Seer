 /*  Magic Builder
   	A simple java program that grabs the latest prices and displays them per set.
     Copyright (C) 2014  Manuel Gonzales Jr.
 
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
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Row;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 public class BlockConstructed
 {
 	//Url for the block list
 	private String blockList = "https://www.wizards.com/Magic/TCG/Resources.aspx?x=judge/resources/sfrblock";
 	private String tcgSite = "http://magic.tcgplayer.com/db/price_guide.asp?setname=";
 	
 	/*
 	 * How the block names variable is broken down
 	 * 
 	 * Index 0: Name of the block.
 	 * Index 1: How many sets are within the block.
 	 * Index 2: Which index does the BlockSets start.
 	 */
 	
 	private String BlockNames[][] = new String[300][3];
 	private String BlockSets [] = new String[300];
 	
 	private int setIndex = 0;
 	private int nameIndex = 0;
 	
 	//Gathers the list of the blocks
 	public void gatherBlocks()
 	{
 		try
 		{
 			Document page = Jsoup.connect(blockList).get();
 			Elements blockSection = page.select("div.article-content");
 			Element table = blockSection.select("ul").get(0);
 			Elements setsValue = table.select("li");
 			Elements setName;
 			
 			for(int setVIndex = 0; setVIndex<setsValue.size(); setVIndex++)
 			{
 				setName = setsValue.get(setVIndex).select("i");
 				BlockNames[nameIndex][0]=setName.get(0).toString().replaceAll("<i>","").replaceAll("</i>", "");
 				
 				setName.remove(0);
 				
 				if(setName.size()>1)
 				{
 					BlockNames[nameIndex][1]=Integer.toString(setName.size());
 					BlockNames[nameIndex][2]=Integer.toString(setIndex);
 					for(Element name: setName)
 					{
 						filter(name.toString());
 					}
 				}
 				else
 				{
 					filter(setName.get(0).toString());
 				}
 				
 				System.out.println(nameIndex+1+") "+BlockNames[nameIndex][0]+" "+BlockNames[nameIndex][1]);
 				
 				nameIndex++;
 				
 			}
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	//To correct for the webmasters mistakes.
 	private void filter(String name)
 	{
 		name = name.replaceAll(" ", "%20");
 		name = name.replaceAll("'", "%27");
 		
 		if(name.contains("<i>")&&name.contains("</i>"))
 		{
 			name = name.replaceAll("<i>","").replaceAll("</i>","");
 		}
 		
 		if(name.contains(","))
 		{
 			int numberOfCommas= (name.length() - name.replace(",", "").length()) + 1;
 			
			BlockNames[nameIndex][1]=Integer.toString(numberOfCommas);
 			BlockNames[nameIndex][2]=Integer.toString(setIndex);
 			
 			while(numberOfCommas > 0)
 			{
 				
 				if(name.charAt(0)=='%')
 					name = name.substring(3,name.length());
 				
 				if(name.contains(","))
 					BlockSets[setIndex] = name.substring(0,name.indexOf(','));
 				else
 					BlockSets[setIndex] = name;
 				
 				if(BlockSets[setIndex].contains(":")&&BlockSets[setIndex].contains("Ravnica"))
 				{
 					BlockSets[setIndex] = BlockSets[setIndex].substring(0,name.indexOf(":"));
 				}
 				
 				name = name.substring(name.indexOf(',')+1);
 				
 				setIndex++;
 				numberOfCommas--;
 			}
 		}
 		else
 		{
 			BlockSets[setIndex] = name;
 			setIndex++;
 		}
		
 	}
 	
 	//Obtains the selected blocks
 	public void getBlock(int selection)
 	{
 		selection--;
 		try
 		{
 			int indexAdjust = 0;
 			int rowIndex;
 			int numberOfSets = Integer.parseInt(BlockNames[selection][1]);
 			int blockNameIndex = selection;
 			selection = Integer.parseInt(BlockNames[selection][2]);
 			String tempItem;
 			
 			//Various elements
 			Document page;
 			Element setSection;
 			Elements row;
 			Elements td;
 			
 			//Worksheet the information will be written too
 			HSSFWorkbook block = new HSSFWorkbook();
 			HSSFSheet blockSet;
 			Row infoRow;
 			
 			DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
 		    Date date = new Date();
 			
 			while(numberOfSets>0)
 			{
 				page = Jsoup.connect(tcgSite+BlockSets[selection+indexAdjust]).get();
 				setSection = page.select("table").get(2);
 				row = setSection.select("tr");
 				rowIndex = 1;
 				
 				blockSet=block.createSheet(BlockSets[selection+indexAdjust].replaceAll("%20"," ").replaceAll("%27", " "));
 				
 				infoRow = blockSet.createRow(0);
 				infoRow.createCell(0).setCellValue("Card Name");
 				infoRow.createCell(1).setCellValue("High Price");
 				infoRow.createCell(2).setCellValue("Medium Price");
 				infoRow.createCell(3).setCellValue("Low Price");
 				
 				
 				for(Element cardrow: row)
 				{
 					td = cardrow.select("td");
 					tempItem = td.get(0).text().substring(1);
 					
 					if(!tempItem.contains("Forest")&&!tempItem.contains("Mountain")&&!tempItem.contains("Swamp")&&!tempItem.contains("Island")&&!tempItem.contains("Plains")&&!tempItem.isEmpty())
 					{
 						if(td.get(5).text().length()>2&&td.get(6).text().length()>2&&td.get(7).text().length()>2)
 						{
 							infoRow = blockSet.createRow(rowIndex);
 							infoRow.createCell(0).setCellValue(td.get(0).text().substring(1));
 							infoRow.createCell(1).setCellValue(Double.parseDouble(td.get(5).text().substring(1,td.get(5).text().length()-1).replace(",","")));
 							infoRow.createCell(2).setCellValue(Double.parseDouble(td.get(6).text().substring(1,td.get(6).text().length()-1).replace(",","")));
 							infoRow.createCell(3).setCellValue(Double.parseDouble(td.get(7).text().substring(1,td.get(7).text().length()-1).replace(",","")));
 							
 							rowIndex++;
 						}
 						
 					}
 				}
 				
 				blockSet.autoSizeColumn(0);
 				blockSet.autoSizeColumn(1);
 				blockSet.autoSizeColumn(2);
 				
 				indexAdjust++;
 				numberOfSets--;
 				
 			}
 			
 			File blockFile = new File(BlockNames[blockNameIndex][0]+"-Block-"+dateFormat.format(date)+"-.xls");
 			FileOutputStream blockOutput = new FileOutputStream(blockFile);
 			block.write(blockOutput);
 			blockOutput.close();
 			
 		}
 		
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 }
