 import java.io.*;
 public class cat {
 	public static void main(String[] args) {
		String filename;
 		File file;
 		try {
 			filename = args[0];
 			file = new File(filename);
			BufferedReader in = new BufferedReader(new FileReader(file));
 			
 			System.out.println(filename + ":");
			while((String line = in.readLine()) != null) {
				System.out.print(line);
 			}
 		} catch (IndexOutOfBoundsException ex) {
 			System.out.println("Must provide a file to cat: use >java cat <file name>");
 		} catch (FileNotFoundException ex) {
 			System.out.println("File " + filename + " does not exist");
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} finally {
			in.close();
 		}
 	}
 }
