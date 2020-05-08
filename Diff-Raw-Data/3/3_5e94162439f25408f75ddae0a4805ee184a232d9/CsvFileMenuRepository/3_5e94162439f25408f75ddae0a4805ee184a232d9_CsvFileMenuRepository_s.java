 package com.eggs.impl;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.eggs.Menu;
 import com.eggs.MenuBuilder;
 import com.eggs.MenuRepository;
 
 public class CsvFileMenuRepository
 	implements MenuRepository
 {
 	private String[] fileNames;
 	
 	public CsvFileMenuRepository(String ... files)
 	{
 		fileNames = files;
 	}
 	
 	private Menu getMenu(String path)
 	{
 		MenuBuilder menu = MenuBuilder.menu();
 		
 		BufferedReader br = null;
 		
 		try
 		{
 			InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
 			br = new BufferedReader(new InputStreamReader(is));
 			
 			String s;
 			int i = 0;
 			while((s = br.readLine()) != null)
 			{
 				if(i == 0) menu.restaurant(s);
 				else
 				{
 					String[] food = s.split(",");
					menu.food(food[0], food[1], Float.parseFloat(food[2]));
 				}
 				++i;
 			}
 		}
 		catch (FileNotFoundException e)
 		{
 			return null;
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if(br != null)
 				try
 				{
 					br.close();
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 		}
 		
 		return menu.build();
 	}
 
 	public List<Menu> getAllmenu()
 	{
 		List<Menu> list = new ArrayList<Menu>();
 		
 		for (String file : fileNames)
 		{
 			Menu m1 = getMenu(file);
 			if(m1 != null) list.add(m1);
 		}
 		
 		return list;
 	}
 
 }
