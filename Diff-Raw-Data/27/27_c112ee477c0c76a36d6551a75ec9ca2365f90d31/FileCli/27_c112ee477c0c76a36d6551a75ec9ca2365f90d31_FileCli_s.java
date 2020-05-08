 package com.voladroid.ui.cli;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Scanner;
 
 public class FileCli extends AbstractCli {
 
 	private String name;
 	private Scanner scanner;
 
	public FileCli(String name) throws FileNotFoundException {
 		this.name = name;
		this.scanner = new Scanner(new File(name));
 
 	}
 
 	@Override
 	public String in(String msg) throws IOException {
 		String cmd = scanner.nextLine();
 		out(">> %s", cmd);
 		return cmd;
 	}
 
 	@Override
 	public void onStart() {
 		out("Reading file: %s", name);
 	}
 
 	@Override
 	public boolean hasCommand() {
 		return scanner.hasNextLine();
 	}
 
 }
