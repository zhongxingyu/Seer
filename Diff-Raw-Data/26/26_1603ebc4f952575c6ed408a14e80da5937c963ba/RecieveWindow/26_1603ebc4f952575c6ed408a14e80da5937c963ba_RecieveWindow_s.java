 package GUI;
 
 import java.awt.EventQueue;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JTextArea;
 import java.awt.Color;
 import javax.swing.JScrollPane;
 
 public class RecieveWindow 
 {
 	private JFrame frame;
 	private JPanel contentPane;
 	private JTextArea textArea;
 	
 	public RecieveWindow()
 	{
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
 					
 					payload();
 				} 
 				catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});	
 	}
 	
 	public void payload()
 	{
 	frame = new JFrame("Payload Terminal");
 	frame.setVisible(true);
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	frame.setSize(400,300);
 	frame.setResizable(false);
 	contentPane = new JPanel();
 	contentPane.setBackground(Color.WHITE);
 	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 	frame.setContentPane(contentPane);
 	contentPane.setLayout(null);
 	
 	JScrollPane scrollPane = new JScrollPane();
 	scrollPane.setBounds(10, 11, 374, 250);
 	contentPane.add(scrollPane);
 	
 	textArea = new JTextArea();
 	textArea.setWrapStyleWord(true);
 	scrollPane.setViewportView(textArea);
 	textArea.setEditable(false);
 	textArea.setLineWrap(true);
 	
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
 	
 	public void updateText(final String updateString)
 	{
 		try
 		{
 			textArea.append(updateString);
 		}
 		catch(Exception e) 
 		{ 
 			System.out.println(e);
 		}
 	}
 }
