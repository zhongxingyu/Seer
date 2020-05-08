 package jcolin.consoles;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Vector;
 
 import jcolin.commands.Command;
 import jcolin.commands.Command.RedirectMode;
 
 public abstract class Console implements IConsole {
 	private PrintWriter m_fileWriter;
 	private StringBuffer m_outputBuffer;
 	private boolean m_outputDisplay;
 
 	public abstract void displayPrompt(String str);
 	public abstract String getNextLine(Vector<Command> commandHistory);
 	public abstract void clearScreen();
 	public abstract boolean hasEscaped();
 
 	public Console() {
 		m_outputBuffer = new StringBuffer();
 	}
 	
 	public boolean redirectToFile(String str) {
 		if (m_fileWriter != null) {
 			m_fileWriter.printf("%s", str);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean openRedirectFile(String fileName, RedirectMode redirectMode) {
 		assert(m_fileWriter == null);
 		try {
 			switch (redirectMode) {
 			case OVERWRITE:
 				m_fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
 				return true;
 
 			case APPEND:
 				m_fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
 				return true;
 
 			default:
 				assert(false);
 				return false;
 			}
 			
 		} catch (IOException e) {			
 			//notifier.notify(src, Notifier.NOTIFY_ERROR, String.format("Unable to open file for writing: %s", fileName));
 			// TODO
 			m_fileWriter = null;
 			return false;
 		}
 	}
 
 	public void closeRedirectFile() {
 		if (m_fileWriter != null) {
 			m_fileWriter.close();
 			m_fileWriter = null;			
 		}
 	}
 	
 	public void clearOutputBuffer() {
 		m_outputBuffer = new StringBuffer();
 	}
 	
 	public void addCommandOutput(String output) {
 		m_outputBuffer.append(output);
 	}
 	
 	public String getCommandOutput() {
 		return m_outputBuffer.toString().trim();		
 	}
 	
 	public void setOutputDisplay(boolean outputDisplay) {
 		m_outputDisplay = outputDisplay;
 	}
 	
     public boolean getOutputDisplay() {
     	return m_outputDisplay;
     }
 }
