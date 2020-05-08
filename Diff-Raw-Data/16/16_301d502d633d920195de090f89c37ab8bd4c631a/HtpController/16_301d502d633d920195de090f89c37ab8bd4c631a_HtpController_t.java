 //----------------------------------------------------------------------------
 // $Id$
 //----------------------------------------------------------------------------
 
 package hexgui.htp;
 
 import hexgui.hex.HexPoint;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.BufferedReader;
 import java.io.PrintStream;
 import java.io.IOException;
 import java.util.Vector;
 
 //----------------------------------------------------------------------------
 
 /** Sends HTP commands and parses the response. */
 public class HtpController
 { 
     public interface IOInterface
     {
 	void sentCommand(String str);
 	void receivedResponse(String str);
 	void receivedError(String str);
     }
 
     //------------------------------------------------------------
 
     public static Vector<HexPoint> parsePointList(String str)
     {
 	Vector<HexPoint> ret = new Vector<HexPoint>();
 	String[] pts = str.trim().split(" ");
 	for (int i=0; i<pts.length; i++) {
 	    HexPoint p = HexPoint.get(pts[i].trim());
 	    ret.add(p);
 	}
 	return ret;
     }
 
     //------------------------------------------------------------
 
     /** Constructor */
     public HtpController(InputStream in, OutputStream out, IOInterface io)
     {
 	System.out.println("controller: in constructor.");
 	m_in = new BufferedReader(new InputStreamReader(in));
 	m_out = new PrintStream(out);
 	m_io = io;
 	m_connected = true;
     }
     
     public void sendCommand(String cmd) throws HtpError
     {
 	sendCommand(cmd, null);
     }
 
     public void sendCommand(String cmd, Runnable callback) throws HtpError
     {
 	if (!m_connected) 
 	    throw new HtpError("Hex Program Disconnected.");
 
 	System.out.println("controller: sending '" + cmd.trim() + "'");
 	m_out.print(cmd);
 	m_out.flush();
 	m_io.sentCommand(cmd);
         handleResponse(callback);
 
 	if (callback != null) {
 	    //System.out.println("controller: running callback.");
 	    callback.run();
 	}
     }
 
     public boolean wasSuccess() { return m_success; }
     public String getResponse() { return m_response; }
 
     private void handleResponse(Runnable callback) throws HtpError
     {
 	String response;
 	try {
 	    response = waitResponse();
 	}
 	catch (IOException e) {
 	    throw new HtpError("IOException waiting for response!");
 	}
 
 	//System.out.println("got: '" + response + "'");

	if (response == null) {
	    m_success = false;
	    m_response = "";
	    throw new HtpError("Null response received!");
	} else if (response.length() < 2) {
	    m_success = false;
	    m_response = response;
	    throw new HtpError("Response length too short! '"+response+"'");
	} else if (response.substring(0,2).equals("= ")) {
 	    m_success = true;
 	    m_response = response.substring(2);
 	    System.out.print("controller: success: ");
 	    m_io.receivedResponse(response);
 	} else if (response.substring(0,2).equals("? ")) {
 	    m_success = false;
 	    m_response = response.substring(2);
 	    System.out.print("controller: error: "); 
 	    m_io.receivedError(response);
 	} else {
 	    m_response = response;
 	    m_success = false;
 	    System.out.print("controller: invalid: "); 
 	    throw new HtpError("Invalid HTP response:'" + response + "'.");
 	}
 	System.out.println("'" + m_response.trim() + "'");
     }
 
     private String waitResponse() throws IOException
     {
 	StringBuilder ret = new StringBuilder();
 	while (true) {
 	    //System.out.println("blocking on response");
 	    String line = m_in.readLine();
 	    //System.out.println("readline: '" + line + "'");
 	    if (line == null) {
		System.out.println("controller: Disconnected!");
 		m_connected = false;
 		break;
 	    }
 	    String clean = cleanInput(line);
 	    ret.append(clean);
 	    ret.append('\n');
 
 	    if (clean.equals(""))
 		break;
 	}
 	//System.out.println("controller: done waiting on response.");
 	return ret.toString();
     }
 
     /** Cleans the input.  Removes all occurances of '\r'. 
 	Converts all '\t' to ' '. 
     */
     private String cleanInput(String in)
     {
 	StringBuilder out = new StringBuilder();
 	for (int i=0; i<in.length(); i++) {
 	    if (in.charAt(i) == '\t') 
 		out.append(' ');
 	    else if (in.charAt(i) != '\r') {
 		out.append(in.charAt(i));
 	    }
 	}      
 	return out.toString();
     }
 
     private boolean m_connected;
     private BufferedReader m_in;
     private PrintStream m_out;   
     private IOInterface m_io;
 
     private String m_response;
     private boolean m_success;
 }
 
 //----------------------------------------------------------------------------
