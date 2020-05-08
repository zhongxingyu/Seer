 package org.vamdc.validator.gui.console;
 
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.io.ByteArrayOutputStream;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import org.vamdc.validator.Setting;
 import org.vamdc.validator.gui.PositionMemoryDialog;
 
 public class ConsolePanel extends PositionMemoryDialog{
 
 	private static final long serialVersionUID = 5729446011617248198L;
 
 	private JTextArea text = new JTextArea();
 	private JPanel panel = new JPanel();
 
 	public ConsolePanel(Frame owner){
 		super("Log console",owner,Setting.GUILogConsoleDim);
 		initDialog();
 		initCloseEvent();
 		initLayout();
 		initStreams();
 		loadDimensions();
 	}
 
 	private void initDialog() {
 		this.setContentPane(panel);
 		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setModal(false);
 	}
 
 	private void initStreams() {
 		PrintStream printer = new PrintStream(new TextPrinter(text),true);
 		System.setOut(printer);
 		System.setErr(printer);
 		this.clear();
 	}
 
 	private void initLayout(){
 		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
 
 		JScrollPane scroll = new JScrollPane(text);

 		panel.add(scroll);
 		panel.setPreferredSize(new Dimension(640,480));
 	}
 
 	private class TextPrinter extends FilterOutputStream{
 		private JTextArea textArea;
 
 		TextPrinter(JTextArea text){
 			super(new ByteArrayOutputStream());
 			this.textArea = text;
 		}
 
 		@Override
 		public void flush() throws IOException {
 			super.flush();
 			String text = out.toString();
 			out = new ByteArrayOutputStream();
 			textArea.append(text);
 		}
 
 	}
 
 	public void clear(){
 		text.setText("");
 		System.out.println("Application log console");
 	}
 
 
 
 }
