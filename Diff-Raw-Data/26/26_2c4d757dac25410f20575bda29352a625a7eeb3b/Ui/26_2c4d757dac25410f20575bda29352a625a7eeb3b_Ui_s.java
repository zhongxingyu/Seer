 package ui;
 
 import io.*;
 
 public class Ui {
 
	Input input;
	Output output;
 
 	public Ui(Input input, Output output) {
 
 		this.input = input;
 		this.output = output;
 	}
 
 	public String onInput() {
 
 		return input.readLine();
 	}
 
 	public int onInputInt() throws NumberFormatException {
 
 		return Integer.valueOf(onInput());
 	}
 
 	public void onOutput(String string) {
 
 		output.write(string);
 	}
 
 	public void onOutputLine(String string) {
 
 		output.writeLine(string);
 	}
 
 	public void onOutput(Object object) {
 
 		onOutput(object.toString());
 	}
 
 	public void onOutputLine(Object object) {
 
 		onOutputLine(object.toString());
 	}
 
 }
