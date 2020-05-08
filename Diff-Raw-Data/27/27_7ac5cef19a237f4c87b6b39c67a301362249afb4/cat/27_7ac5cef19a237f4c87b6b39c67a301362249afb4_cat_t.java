 import java.io.*;
 public class cat {
 	public static void main(String[] args) {
		String filename = null;
 		File file;
		BufferedReader in = null;
 		try {
 			filename = args[0];
 			file = new File(filename);
			if(file.isDirectory()) {
				System.out.println(filename + "is a directory!");
				return;
			}
			
			in = new BufferedReader(new FileReader(file));
 			
 			System.out.println(filename + ":");
			String line;
			while((line = in.readLine()) != null) {
				System.out.println(line);
 			}
 		} catch (IndexOutOfBoundsException ex) {
 			System.out.println("Must provide a file to cat: use >java cat <file name>");
 		} catch (FileNotFoundException ex) {
 			System.out.println("File " + filename + " does not exist");
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} finally {
			try {
				if(in != null) {
					in.close();
				}
			}catch (IOException ex) {
				System.out.println("Unable to close " + filename);
			}
 		}
 	}
 }
