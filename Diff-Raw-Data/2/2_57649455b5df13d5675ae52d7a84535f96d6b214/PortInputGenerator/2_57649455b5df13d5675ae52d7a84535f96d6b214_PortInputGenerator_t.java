 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.util.HashSet;
 
 public class PortInputGenerator extends InputGenerator
 {
 	private static int		MAX_PORT_NUMBER_SEND	= 5;
 	private int				PORT_NUMBER_SENT		= 0;
 	private PrintWriter		out;
 	private HashSet<String>	acceptableOutput		= new HashSet<String>();
 
 	/** Here, we create the file to write to. */
 	public PortInputGenerator(File studentDir)
 	{
 		File results = new File(studentDir, "MDB-OUTPUT.txt");
 		try {
 			out = new PrintWriter(new FileOutputStream(results), true);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getNextInput()
 	{
 		if (PORT_NUMBER_SENT++ < MAX_PORT_NUMBER_SEND)
 			try {
 				// A neat trick. Let java find the next available port for us
 				ServerSocket socket = new ServerSocket(0);
 				socket.close();
 				return Integer.toString(socket.getLocalPort());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		// An error occured or we've sent the maximum number of ports
 		return null;
 	}
 
 	@Override
 	public void putNextStdOut(String line)
 	{
 		if (line == null)
 			out.close();
		else if (!acceptableOutput.contains(line.trim()))
 			out.println("bad");
 	}
 }
