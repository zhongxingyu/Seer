 package editor.animations;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferStrategy;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSplitPane;
 
 import editor.ConfigurationEditor;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ParserKeys;
 
 
 
 /**
  * 
  * @author Connor Schenck
  *
  */
 public class AnimationEditor implements ActionListener, ConfigurationEditor, Runnable {
 
 	
 	private JPanel mainPanel; 
 	private Canvas canvas;
 	private BufferStrategy strategy;
 	private JButton addFiles;
 	private JButton resetSpeed;
 	private JButton setBackground;
 	private JButton clearBackground;
 	private JButton setSpeedRange;
 	private JButton removeSelected;
 	private JPanel scrollPanel;
 	private JScrollPane scrollPane;
 	private JPanel leftPanel;
 	private JSlider speedSlider;
 	
 	private Sprite background = null;
 	
 	private ArrayList<Frame> list = new ArrayList<Frame>();
 	private ArrayList<Frame> newList = null;
 	
 	private String animationFolder;
 	
 	public AnimationEditor(String folderToPutImages)
 	{
 		this.animationFolder = folderToPutImages;
 		
 		//set up the Canvas
 		canvas = new Canvas();
 		canvas.setSize(500, 500);
 		mainPanel = new JPanel();
 		
 		
 		//set up the menu
 		setBackground = new JButton("Set Background");
 		setBackground.addActionListener(this);
 		clearBackground = new JButton("Clear Background");
 		clearBackground.addActionListener(this);
 		setSpeedRange = new JButton("Set Background Speed Max");
 		setSpeedRange.addActionListener(this);
 		
 		
 		//set up the buttons
 		addFiles = new JButton("Add Files");
 		addFiles.addActionListener(this);
 		
 		ArithmaticPanel ap = new ArithmaticPanel(this);
 		
 		speedSlider = new JSlider(JSlider.VERTICAL, -1000, 1000, 0);
 		speedSlider.setMinorTickSpacing(1);
 		
 		resetSpeed = new JButton("Reset Background Speed");
 		resetSpeed.addActionListener(this);
 		
 		removeSelected = new JButton("Remove Selected");
 		removeSelected.addActionListener(this);
 		
 		JPanel buttonLeftPanel = new JPanel();
 		buttonLeftPanel.setLayout(new BoxLayout(buttonLeftPanel, BoxLayout.Y_AXIS));
 		
 		JPanel buttonPanel = new JPanel();
 		buttonLeftPanel.add(addFiles);
 		buttonLeftPanel.add(setBackground);
 		buttonLeftPanel.add(clearBackground);
 		buttonLeftPanel.add(setSpeedRange);
 		buttonLeftPanel.add(resetSpeed);
 		buttonLeftPanel.add(removeSelected);
 		buttonPanel.add(buttonLeftPanel);
 		buttonPanel.add(ap);
 		buttonPanel.add(speedSlider);
 		
 		leftPanel = new JPanel();
 		leftPanel.setLayout(new BorderLayout());
 		leftPanel.add(buttonPanel, BorderLayout.NORTH);
 		scrollPanel = new JPanel();
 		scrollPane = new JScrollPane(scrollPanel);
 		scrollPane.setPreferredSize(new Dimension(300, 300));
 		leftPanel.add(scrollPane, BorderLayout.CENTER);
 		
 		JSplitPane pane = new JSplitPane();
 		pane.setLeftComponent(leftPanel);
 		pane.setRightComponent(canvas);
 		pane.setDividerLocation(500);
 		
 		mainPanel.add(pane, BorderLayout.CENTER);
 //		mainPanel.setMinimumSize(new Dimension(800, 600));
 		
 		scrollPanel.removeAll();
         scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));
         
         try {
         	Scanner s = new Scanner(new File("lastBackground.txt"));
         	background = new Sprite(s.nextLine());
         }
         catch(Exception e) {}
 	}
 	
 	public void run() 
 	{
 		
 		canvas.setIgnoreRepaint(true);
 		canvas.requestFocus();
 		canvas.setFocusTraversalKeysEnabled(false);
 		canvas.createBufferStrategy(3);
 		strategy = canvas.getBufferStrategy();
 		
 		boolean b = true;
 		int current = 0;
 		long lastChangeTime = System.currentTimeMillis();
 		int backgroundPos = 0;
 		long loopTime = System.currentTimeMillis();
 		while(b)
 		{
 			//get graphics object to draw to
 			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
 			g.setColor(Color.black);
 			g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
 			
 			if(current > list.size())
 				current = 0;
 			
 			//draw the background if there is one
 			if(background != null)
 			{
 				int height = (int) (((double)canvas.getWidth()/background.getWidth())*background.getHeight());
 				backgroundPos += ((double)-speedSlider.getValue()*(System.currentTimeMillis() - loopTime)/1000.0);
 				backgroundPos %= height;
 				loopTime = System.currentTimeMillis();
 				
 				for(int pos = backgroundPos - height; pos <= canvas.getHeight(); pos += height )
 					background.draw(g, 0, pos, canvas.getWidth(), height);
 			}
 			
 			if(!list.isEmpty())
 			{
 				list.get(current).getFrame().draw(g, 0, 0, canvas.getWidth(), canvas.getWidth());
 				
 				if(System.currentTimeMillis() - lastChangeTime > list.get(current).getTime())
 				{
 					current = (current + 1)%list.size();
 					lastChangeTime = System.currentTimeMillis();
 				}
 			}
 			else if(background == null)
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {}
 				
 			//flip the buffers
 			g.dispose();
 			strategy.show();
 				
 			if(newList != null)
 			{
 				list.clear();
 				scrollPanel.removeAll();
 				for(Frame f : newList)
 				{
 					list.add(f);
 					scrollPanel.add(f);
 				}
 				newList = null;
 				current = 0;
 				scrollPanel.validate();
 				scrollPanel.updateUI();
 			}
 			
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {}
 			
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if(arg0.getSource().equals(addFiles))
 		{
 			JFileChooser fc = new JFileChooser();
 			
 			try {
 				Scanner s = new Scanner(new File("lastDirectory.txt"));
 				fc = new JFileChooser(s.nextLine());
 			} catch (FileNotFoundException e) {	}
 			
 			fc.setMultiSelectionEnabled(true);
 			int returnVal = fc.showOpenDialog(mainPanel);
 
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	            File[] file = fc.getSelectedFiles();
 	            
 	            ArrayList<Frame> newList = new ArrayList<Frame>();
 	            
 	            //add the current list
 	            for(Frame f : list)
 	            	newList.add(f);
 	            
 	            for(File f : file)
 	            {
 	            	try {
 						newList.add(new Frame(f.getAbsolutePath()));
 					} catch (IOException e) {
 						e.printStackTrace();
 						return;
 					}
 	            }
 	            
 	            this.newList = newList;
 	            
 	            scrollPane.validate();
 	            scrollPane.updateUI();
 	            
 	            
 	            try {
 					FileWriter fWriter = new FileWriter("lastDirectory.txt");
 					fWriter.write(file[0].getParent());
 					fWriter.flush();
 					fWriter.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 	        }
 		}
 		else if(arg0.getSource().equals(setBackground))
 		{
 			JFileChooser fc = new JFileChooser();
 			
 			try {
 				Scanner s = new Scanner(new File("lastDirectory.txt"));
 				fc = new JFileChooser(s.nextLine());
 			} catch (FileNotFoundException e) {	}
 			
 			fc.setMultiSelectionEnabled(false);
 			int returnVal = fc.showOpenDialog(mainPanel);
 
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	        	try {
 					background = new Sprite(fc.getSelectedFile().getAbsolutePath());
 				} catch (IOException e1) {
 					e1.printStackTrace();
 					return;
 				}
 	        	FileWriter fw;
 				try {
 					fw = new FileWriter("lastBackground.txt");
 					fw.write(background.toString());
 		        	fw.flush();
 		        	fw.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 	        }
 		}
 		else if(arg0.getSource().equals(clearBackground))
 		{
 			background = null;
 			FileWriter fw;
 			try {
 				fw = new FileWriter("lastBackground.txt");
 				fw.write("");
 	        	fw.flush();
 	        	fw.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else if(arg0.getSource().equals(resetSpeed))
 		{
 			speedSlider.setValue(0);
 		}
 		else if(arg0.getSource().equals(setSpeedRange))
 		{
 			String s = (String)JOptionPane.showInputDialog(mainPanel, "Input the maximum background speed", "",
 					JOptionPane.PLAIN_MESSAGE, null, null, "" + speedSlider.getMaximum());
			try {
				int i = Integer.valueOf(s);
				speedSlider.setMaximum(i);
				speedSlider.setMinimum(-i);
			} catch(NumberFormatException e) {
				
			}
 		}
 		else if(arg0.getSource().equals(removeSelected))
 		{
 			 ArrayList<Frame> newList = new ArrayList<Frame>();
 			for(int i = 0; i < list.size();i++)
 			{
 				if(!list.get(i).getChecked())
 					newList.add(list.get(i));
 			}
 			
 			this.newList = newList;
 		}
 		
 		mainPanel.validate();
 		mainPanel.updateUI();
 	}
 	
 	public ArrayList<Frame> getFrames()
 	{
 		return list;
 	}
 
 	@Override
 	public void setData(ConfigData cd) {
 		setData(cd, false);
 	}
 
 	@Override
 	public void forceSetData(ConfigData cd) {
 		setData(cd, true);		
 	}
 	
 	private void setData(ConfigData cd, boolean force)
 	{
 		if(!this.isValid(cd) && !force)
 			throw new IllegalArgumentException("The config data object is not valid");
 		
 		
     	scrollPanel.removeAll();
         scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));
         newList = new ArrayList<Frame>();
         
     	List<String> names = cd.getStringList(ParserKeys.icon);
     	List<Double> times = cd.getNumberList(ParserKeys.displayTime);
     	
     	ArrayList<Frame> newList = new ArrayList<Frame>();
         for(int i = 0; i < Math.min(names.size(), times.size()); i++)
         {
         	Frame f;
 			try {
 				f = new Frame(new File(new File(animationFolder), names.get(i)).getAbsolutePath());
 			} catch (Exception e) {
 				if(force)
 					continue;
 				else
 					throw new IllegalArgumentException(names.get(i) + " is not in " + animationFolder);
 			}
         	f.setTime(times.get(i).toString());
         	newList.add(f);
         	scrollPanel.add(f);
         }
         
         this.newList = newList;
 	}
 
 	@Override
 	public void reset() {
 		scrollPanel.removeAll();
         scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));
         newList = new ArrayList<Frame>();		
 	}
 
 	@Override
 	public ConfigData getData() {
 		ConfigData cd = new ConfigData();
 		File dir = new File(animationFolder);
 		for(Frame f : list)
 		{
 			File to = new File(dir, new File(f.getFrame().toString()).getName());
 			if(!to.exists())
 			{
 				try {
 					FileCopy.copy(f.getFrame().toString(), to.getAbsolutePath());
 				} catch (IOException e) {
 					e.printStackTrace();
 					continue;
 				}
 			}
 			
 			cd.add(ParserKeys.icon, new File(f.getFrame().toString()).getName());
 			cd.add(ParserKeys.displayTime, (double)f.getTime());
 		}
 		
 		return cd;
 	}
 
 	@Override
 	public boolean isValidConfig() {
 		ConfigData cd = this.getData();
 		return isValid(cd);
 	}
 	
 	private boolean isValid(ConfigData cd)
 	{
 		if(cd.getNumberList(ParserKeys.displayTime).size() != cd.getStringList(ParserKeys.icon).size())
 			return false;
 		if(cd.getStringList(ParserKeys.icon).size() <= 0)
 			return false;
 		
 		return true;
 	}
 
 	@Override
 	public ParserKeys getType() {
 		return ParserKeys.animationURI;
 	}
 
 	@Override
 	public JPanel getPanel() {
 		return mainPanel;
 	}
 
 }
