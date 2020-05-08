 package com.slyvr.init;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.geom.Rectangle;
 
 import com.slyvr.beans.*;
 
 public class InitMenus {
 
 	static int width = 193;
 	static int height = 58;
 	public static void init(Global global){
 		
 		global.setMenus(new ArrayList<Menu>());
 		
 		global.getMenus().add(initConstant(global));
 		global.getMenus().add(initMain(global));
 		global.getMenus().add(initOptions(global));
 		global.getMenus().add(initPause(global));
 		global.getMenus().add(initPlay(global));
 		global.getMenus().add(initSingle(global));
 		//global.getMenus().add(initMulti(global));
 		global.getMenus().add(initSave(global));
 		global.getMenus().add(initLoad(global));
 		global.getMenus().add(initGame(global));
 		
 		global.getCurrent().setMenu(global.getMenuByName("main"));
 	}
 	public static Menu initConstant(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("btn_cursor",global.getImageByName("btn_cursor"),new Rectangle(0,0,30,30)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("constant");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initMain(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("logo",global.getImageByName("logo"),new Rectangle(250,100,400,150)));
 			menuItems.add(new MenuItem("btn_play",global.getSheetByName("btns"),0,1,new Rectangle(400,280,width,height)));
 			//menuItems.add(new MenuItem("btn_multi",global.getSheetByName("btns2"),0,4,new Rectangle(500,280,width,height)));
 			menuItems.add(new MenuItem("btn_options",global.getSheetByName("btns"),0,2,new Rectangle(400,360,width,height)));
 			menuItems.add(new MenuItem("btn_exit",global.getSheetByName("btns"),0,4,new Rectangle(400,440,width,height)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("main");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initOptions(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("btn_changeVol",global.getSheetByName("btns2"),0,3,new Rectangle(350,200,width,height)));
 			menuItems.add(new MenuItem("txt_volume","Volume: 1.0",new Rectangle(400,200,0,0)));
 			
 			menuItems.add(new MenuItem("btn_changeFullScreen",global.getSheetByName("btns2"),0,3,new Rectangle(350,260,width,height)));
 			menuItems.add(new MenuItem("txt_fullscreen","Windowed",new Rectangle(400,260,0,0)));
 			
 			menuItems.add(new MenuItem("btn_back",global.getSheetByName("btns"),0,6,new Rectangle(400,360,width,height)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("options");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initPause(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("pause");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initPlay(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("btn_resume",global.getSheetByName("btns2"),0,3,new Rectangle(400,120,width,height)));
 			menuItems.add(new MenuItem("btn_newgame",global.getSheetByName("btns2"),0,1,new Rectangle(400,200,width,height)));
 			menuItems.add(new MenuItem("btn_save",global.getSheetByName("btns"),0,0,new Rectangle(400,280,width,height)));
 			menuItems.add(new MenuItem("btn_load",global.getSheetByName("btns"),0,3,new Rectangle(400,360,width,height)));
 			menuItems.add(new MenuItem("btn_back",global.getSheetByName("btns"),0,6,new Rectangle(400,440,width,height)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("play");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initSingle(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("btn_resume",global.getSheetByName("btns2"),0,3,new Rectangle(400,120,width,height)));
 			menuItems.add(new MenuItem("btn_newgame",global.getSheetByName("btns2"),0,1,new Rectangle(400,200,width,height)));
 			menuItems.add(new MenuItem("btn_save",global.getSheetByName("btns"),0,0,new Rectangle(400,280,width,height)));
 			menuItems.add(new MenuItem("btn_load",global.getSheetByName("btns"),0,3,new Rectangle(400,360,width,height)));
 			menuItems.add(new MenuItem("btn_back",global.getSheetByName("btns"),0,6,new Rectangle(400,440,width,height)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("single");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initMulti(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("btn_back",global.getSheetByName("btns"),0,6,new Rectangle(400,360,width,height)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("multi");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initSave(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("btn_back",global.getSheetByName("btns"),0,6,new Rectangle(400,360,width,height)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("save");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initLoad(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("btn_back",global.getSheetByName("btns"),0,6,new Rectangle(400,360,width,height)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("load");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 	public static Menu initGame(Global global){
 		Menu menu = new Menu();
 		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
 		
 		try{
 			menuItems.add(new MenuItem("leveltext","Level 1",new Rectangle(0, 0, 0, 0)));
 			menuItems.add(new MenuItem("inventory",global.getImageByName("inventory"),new Rectangle(960, 0, 30, 30)));
			menuItems.add(new MenuItem("invblock",global.getCurrent().getCurrentBlockType().getBlockImg(),new Rectangle(964, 4, 22, 22)));
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		menu.setName("game");
 		menu.setMenuItems(menuItems);
 		return menu;
 	}
 }
