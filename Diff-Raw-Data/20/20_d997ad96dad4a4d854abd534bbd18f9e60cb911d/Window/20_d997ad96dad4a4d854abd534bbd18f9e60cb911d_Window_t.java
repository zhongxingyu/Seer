 package me.paulrose.lptc;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
 import org.newdawn.slick.CanvasGameContainer;
 import org.newdawn.slick.SlickException;
 
 import me.paulrose.lptc.editor.EditorPanel;
 import me.paulrose.lptc.simulator.AntArena;
 
 public class Window extends JFrame {
 	
 	private EditorPanel editor;
 	private CanvasGameContainer canvas;
 	private JMenuBar menuBar;
 	private JPanel canvasControlBar;
 
 	public Window(){
 		
 		// Set window properties
 		setSize(1080, 720);
 		setTitle("Learning Programming Through Competition");
 				
 		// Create an instance of the editor
 		editor = new EditorPanel();
 	
 		try {
 			canvas = new CanvasGameContainer(new AntArena(true, editor.output));
 			canvas.getContainer().setAlwaysRender(false);
 
 		} catch (Exception e)
 		{
 			JOptionPane.showMessageDialog(this,
 		   "Exception caught!, check output for details. 1. Stopping the arena \n " + e.getMessage(),
 		   "Error!",
 		    JOptionPane.ERROR_MESSAGE);
 		}
 		
 		// Create menu bar
 		//createMenuBar();
 		createCanvasControlBar();
 		
 		// Wrapper bar for the canvas and their controls
 		JPanel center = new JPanel();
 		center.setLayout(new BorderLayout());
 		center.add(canvas, BorderLayout.CENTER);
 		center.add(canvasControlBar, BorderLayout.SOUTH);
 		
 		// Set up all window elements, Show time
 		//add(menuBar, BorderLayout.NORTH);
 		add(editor, BorderLayout.WEST);
 		add(center);
 		
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
				canvas.getContainer().exit();
				  if (Display.isCreated()) 
			            Display.destroy();
			      if (AL.isCreated()) 
			            AL.destroy();
 				System.exit(0);
 			}
 		});
 		
 		setVisible(true);
 		
 	}
 	
 	private void createCanvasControlBar() 
 	{
 		canvasControlBar = new JPanel();
 		
 		JButton start = new JButton("Start");
 		
 		start.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				
 				try {
 					canvas.start();
 					JButton b = (JButton)e.getSource();
 					b.setEnabled(false);
 				}catch (Exception e1)
 				{
 					JOptionPane.showMessageDialog(Window.this,
 				   "Exception caught!, check output for details. 2. Stopping the arena \n " + e1.getMessage(),
 				   "Error!",
 				    JOptionPane.ERROR_MESSAGE);
 					canvas.getContainer().exit();
 				}
 				
 			}
 			
 		});
 		
 		JButton pause = new JButton("Pause");
 		
 		pause.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				canvas.getContainer().pause();
 			}
 		});
 		
 		
 		JButton resume = new JButton("Resume");
 		
 		resume.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				canvas.getContainer().resume();
 			}
 
 		});
 		
 		JButton reset = new JButton("Reset");
 		
 		reset.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				try
 				{
 					canvas.getContainer().reinit();
 				}catch (Exception e)
 				{
 					JOptionPane.showMessageDialog(Window.this,
 				   "Exception caught!, check output for details. 3. Stopping the arena \n " + e.getMessage(),
 				   "Error!",
 				    JOptionPane.ERROR_MESSAGE);
 					canvas.getContainer().exit();
 				}
 				
 			}
 		});
 		
 		canvasControlBar.add(start);
 		canvasControlBar.add(pause);
 		canvasControlBar.add(resume);
 		canvasControlBar.add(reset);
 	}
 
 
 	private void createMenuBar(){
 		
 		menuBar = new JMenuBar();
 		
 		JMenu menu;
 		JMenuItem item;
 		
 		// Create the file menu
 		menu = new JMenu("File");
 		
 		// Items in the file menu
 		item = new JMenuItem("New Simulation");
 		menu.add(item);
 		
 		menuBar.add(menu);
 		
 	}
 }
