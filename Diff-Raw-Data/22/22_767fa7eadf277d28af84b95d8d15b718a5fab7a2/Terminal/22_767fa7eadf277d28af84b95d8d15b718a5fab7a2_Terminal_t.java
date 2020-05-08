 package GUI;
 
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyledDocument;
 import javax.swing.JScrollPane;
 import Componets.AutoscrollCheckBox;
 import Componets.SendButton;
 import Componets.SendLineTextField;
 import Componets.StartStopButton;
 import Componets.TerminalText;
 import java.awt.Color;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 public class Terminal 
 {
 	private JScrollPane scrollPane;
 	private JPanel panel;
 	private JPanel contentPane;
 	private StartStopButton btnStartStop;
 	private AutoscrollCheckBox chckbxAutoscroll;
 	private JFrame frame;
 	public TerminalText terminalText;
 	public SendButton btnSend;
 	public String textSend;
 	public StyledDocument doc;
 	public SendLineTextField sendLine;
 	public String deviceName;
 	public Terminal(String deviceName)
 	{
 		this.deviceName = deviceName;
 		EventQueue.invokeLater(new Runnable() 
 		{
 			public void run() 
 			{
 				try 
 				{
 					try 
 					{
 						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 				    } 
 				    catch (UnsupportedLookAndFeelException e) { }
 				    catch (ClassNotFoundException e) {}
 				    catch (InstantiationException e) {}
 				    catch (IllegalAccessException e) {}
 					
 					terminal();
 				} 
 				catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});	
 	}
 
 	public void terminal() 
 	{
 		String terminalName = "Terminal Payload -- " + deviceName;
 		frame = new JFrame(terminalName);
 		frame.setVisible(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setBounds(100, 100, 640, 630);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		frame.setContentPane(contentPane);
 		contentPane.setLayout(null);
 		frame.setMinimumSize(new Dimension(400,300));
 		
 		panel = new JPanel();
 		panel.setBackground(Color.WHITE);
 		panel.setSize(622,592);
 		panel.setLocation(0,0);
 		contentPane.add(panel);
 		panel.setLayout(null);
 		
 
 		scrollPane = new JScrollPane();
 		scrollPane.setSize(602,535);
 		scrollPane.setLocation(10,10);
 		panel.add(scrollPane);
 		
 		final int scrollPaneX = panel.getWidth()-scrollPane.getWidth();
 		final int scrollPaneY = panel.getHeight()-scrollPane.getHeight();
 		
 		terminalText = new TerminalText(terminalText);
 		terminalText.setEditable(false);
 		terminalText.setBackground(Color.WHITE);
 		scrollPane.setViewportView(terminalText);
 		doc = terminalText.getStyledDocument();
 		
 
 		chckbxAutoscroll = new AutoscrollCheckBox(terminalText);
 		chckbxAutoscroll.setBackground(Color.WHITE);
 		chckbxAutoscroll.setSelected(true);
 		chckbxAutoscroll.setSize(92,23);
 		chckbxAutoscroll.setLocation(530,556);
 		panel.add(chckbxAutoscroll);
 		chckbxAutoscroll.setActionListener();
 		terminalText.setActionListener(terminalText,chckbxAutoscroll);
 		
 		
 
 		sendLine = new SendLineTextField();
 		sendLine.setSize(344,23);
 		sendLine.setLocation(10,556);
 		panel.add(sendLine);
 		sendLine.setActionListener();
 		
 		final int sendLineX = sendLine.getX();
 		final int sendLineXresize = panel.getWidth()-sendLine.getWidth();
 		final int sendLineY =  panel.getHeight()-sendLine.getY();
 
 		
 		btnSend = new SendButton(sendLine);
 		btnSend.setSize(75,23);
 		btnSend.setLocation(364,556);
 		panel.add(btnSend);
 		btnSend.setActionListener();
 		
 		final int btnSendX = panel.getWidth()-btnSend.getX();
 		final int btnSendY = panel.getHeight()-btnSend.getY();
 		
 		btnStartStop = new StartStopButton();
 		btnStartStop.setSize(75,23);
 		btnStartStop.setLocation(449,556);
 		panel.add(btnStartStop);
 		btnStartStop.setActionListener();
 		
 		final int btnStartStopX = panel.getWidth()-btnStartStop.getX();
 		final int btnStartStopY = panel.getHeight()-btnStartStop.getY();
 		
 
 		final int chckbxAutoscrollX = panel.getWidth()-chckbxAutoscroll.getX();
 		final int chckbxAutoscrollY = panel.getHeight()-chckbxAutoscroll.getY();
 		
 		frame.getContentPane().addComponentListener(new ComponentAdapter() 
 		{
 			public void componentResized(ComponentEvent arg0) 
 			{
 				int frameWidth= panel.getWidth();
 				int frameHeight = panel.getHeight();
 				
 				panel.setSize(frame.getContentPane().getSize());
 				scrollPane.setSize(frameWidth-scrollPaneX, frameHeight-scrollPaneY);
 				sendLine.setSize(frameWidth-sendLineXresize,sendLine.getHeight());
 				
 				sendLine.setLocation(sendLineX, frameHeight-sendLineY);
 				btnSend.setLocation(frameWidth-btnSendX, frameHeight-btnSendY);
 				btnStartStop.setLocation(frameWidth-btnStartStopX, frameHeight-btnStartStopY);
 				chckbxAutoscroll.setLocation(frameWidth-chckbxAutoscrollX, frameHeight-chckbxAutoscrollY);
 			}
 		});
 		
 		frame.addWindowListener(new WindowListener() 
 		{
 			@Override
 			public void windowActivated(WindowEvent arg0) {}
 			@Override
 			public void windowClosed(WindowEvent arg0)
 			{
 			}
 			@Override
 			public void windowDeactivated(WindowEvent arg0) {}
 			@Override
 			public void windowDeiconified(WindowEvent arg0) {}
 			@Override
 			public void windowIconified(WindowEvent arg0) {}
 			@Override
 			public void windowOpened(WindowEvent arg0) {}
 			@Override
 			public void windowClosing(WindowEvent e) 
 			{
 				System.exit(0);
 			}
		});	
 }
 
 	public void updateText(final String updateString, SimpleAttributeSet type)
 	{
 		try
 		{
 			doc.insertString(doc.getLength(),updateString, type);
 			
 			System.out.print(updateString);
 		}
 		catch(Exception e) 
 		{ 
 			System.out.println(e);
 		}
 	}
 }
