 package com.eggs;
 
 import com.eggs.impl.ConsoleMenuPrinter;
 import com.eggs.impl.CsvFileMenuRepository;
 
 public class App {
 	public static void main(String[] args) {
		MenuRepository repo = new CsvFileMenuRepository("karcsi.txt", "etterem.txt");
 		ConsoleMenuPrinter printer = new ConsoleMenuPrinter(repo);
 		
 	    printer.printMenus();
 	}
 }
