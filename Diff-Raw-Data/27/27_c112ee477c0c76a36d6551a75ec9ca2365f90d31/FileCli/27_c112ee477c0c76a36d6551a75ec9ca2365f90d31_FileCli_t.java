 package com.voladroid.ui.cli;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
 import java.util.Scanner;
 
 public class FileCli extends AbstractCli {
 
 	private String name;
 	private Scanner scanner;
 
	public FileCli(String name, File file) throws FileNotFoundException {
		this(name, new Scanner(file));
	}

	public FileCli(String name, InputStream stream) {
		this(name, new Scanner(stream));
	}

	public FileCli(String name, Scanner scanner) {
		this.scanner = scanner;
 		this.name = name;
	}
 
	public FileCli(String data) {
		this("<stdin>", new Scanner(new StringReader(data)));
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
