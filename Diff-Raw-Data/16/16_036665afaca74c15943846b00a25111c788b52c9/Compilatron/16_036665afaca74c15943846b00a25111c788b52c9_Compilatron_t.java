 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import javax.swing.JFileChooser;
 
 public class Compilatron {
 	public static void main(String[] args) {
 		JFileChooser chooser = new JFileChooser();
 		File file;
 		FileWriter output;
 
 		try {
 			file = chooser.getSelectedFile();
 		}
 		catch(Exception e) {
 			Util.printError("Could not open file: " + e);
 			System.exit(1);
 		}
 
 		Compiler compiler;
 		try {
 			compiler = new Compiler(file);
 		}
 		catch(FileNotFoundException e) {
 			Util.printError("Could not open file: " + e);
 			System.exit(1);
 		}
 
 		int[] memory;
 		try {
 			memory = compiler.compile();
 		}
 		catch(OutOfMemoryException e) {
			Util.printError("Error: Program can only learn 100 moves");
 			System.exit(3);
 		}
 		catch(IllegalArgumentException e) {
			Util.printError("Error: Lonely if statement at " + compiler.getLineNumber());
 			System.exit(4);
 		}
 		catch(InvalidVariableException e) {
			Util.printError("Error: Poor naming choice at " + compiler.getLineNumber());
 			System.exit(4);
 		}
 		catch(SyntaxException e) {
			Util.printError("Error: You dun goofed at " + compiler.getLineNumber());
 			System.exit(4);
 		}
 		catch(GotoException e) {
			Util.printError("Error: Tried to goto a nonexistent line at " + compiler.getLineNumber());
 			System.exit(5);
 		}
 		catch(LineNumberException e) {
			Util.printError("Error: Clever, but you can't go back in time at " + compiler.getLineNumber());
 			System.exit(5);
 		}
 		catch(UndefinedVariableException e) {
			Util.printError("Error: Identity crisis at " + compiler.getLineNumber());
 			System.exit(5);
 		}
 
 		try {
 			output = new FileWriter(chooser.getSelectedFile());
 		}
 		catch(IOException e) {
 			Util.printError("Could not open file: " + e);
 			System.exit(1);
 		}
 
 		try {
 			for(int i = 0; i < memory.length; i++) {
 				output.write(memory[i]);
 				for(int ii = 1; ii < 10; ii++)
 					output.write(" " + memory[i + ii]);
 				output.write("\n");
 			}
 		}
 		catch(IOException e) {
 			Util.printError("Could not write to file: " + e);
 			System.exit(2);
 		}
 	}
 }
