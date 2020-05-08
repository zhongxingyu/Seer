 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.util.Scanner;
 
 public class PortGenLab7 implements ArgumentGenerator
 {
 	private File	studentDir;
 
 	public PortGenLab7(File studentDir)
 	{
 		this.studentDir = studentDir;
 	}
 
 	@Override
 	public String getNextArgument()
 	{
 		// Get the mdb port number for this student
 		File mdbPortFile = new File(studentDir, "mdb-port.txt");
 		Scanner mdbPortIn = null;
 		try {
 			mdbPortIn = new Scanner(mdbPortFile);
 		} catch (FileNotFoundException e1) {} // It should exist
 		String mdbPortNo = mdbPortIn.nextLine();
 		mdbPortIn.close();
 		String httpPortNo = null;
 		try {
 			ServerSocket socket = new ServerSocket(0);
 			File httpPortFile = new File(studentDir, "http-port.txt");
 			if (httpPortFile.exists())
 				httpPortFile.delete();
 			httpPortFile.createNewFile();
 			PrintWriter out = new PrintWriter(httpPortFile);
 			out.println(socket.getLocalPort());
 			out.close();
 			httpPortNo = Integer.toString(socket.getLocalPort());
 			socket.close();
 		} catch (IOException e) {} // It shouldn't fail
		return httpPortNo + " ./htdocs/preview/hairstylesalon 127.0.0.1 " + mdbPortNo;
 	}
 
 	public static void main(String[] args)
 	{
 		try {
 			ServerSocket socket = new ServerSocket(0);
 			socket.close();
 			System.out.println(socket.getLocalPort());
 		} catch (IOException e) {} // It shouldn't fail
 	}
 }
