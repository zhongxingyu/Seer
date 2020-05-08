 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.border.EmptyBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.plaf.metal.MetalSliderUI;
 
 import org.jnativehook.GlobalScreen;
 import org.jnativehook.NativeHookException;
 import org.jnativehook.keyboard.NativeKeyEvent;
 import org.jnativehook.keyboard.NativeKeyListener;
 import org.jnativehook.mouse.NativeMouseWheelEvent;
 import org.jnativehook.mouse.NativeMouseWheelListener;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import java.awt.Color;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 
 
 @SuppressWarnings("serial")
 public class MainWindow extends JFrame implements MusicPlayerListener, NativeKeyListener, NativeMouseWheelListener {
 
 	private JPanel contentPane;
 	private JPanel shuffle_songs_checkPanel;
 	private JPanel repeat_song_checkPanel;
 	private JPanel stop_buttonPanel;
 	private JLabel timeLabel;
 	private JLabel timeleftLabel;
 	private JPanel next_buttonPanel;
 	private JPanel back_buttonPanel;
 	private JPanel play_pause_switchButtonPanel;
 	private JLabel nowPlayingLabel;
 
 	private MP3FileList songs;
 	private MusicPlayer mp;
 	private boolean altPressed = false;
 	private int indexPlaying;
 	private MusicPlayerListener stupidReference;
 	private JSlider volumeSlider;
 	@SuppressWarnings("rawtypes")
 	private JList list;
 	private JSlider positionSlider;
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			GlobalScreen.registerNativeHook();
 	    }
 	    catch (NativeHookException ex) {
 	    	System.out.println("hooking error");
 	    }
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainWindow frame = new MainWindow();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
 	public MainWindow() {
 		
 		GlobalScreen.getInstance().addNativeKeyListener(this);
 		GlobalScreen.getInstance().addNativeMouseWheelListener(this);
 		
 		stupidReference = this;
 		
 		songs = new MP3FileList();
 		mp = new MusicPlayer();
 		mp.addListener(this);
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setIconImage(Toolkit.getDefaultToolkit().getImage("note.png"));
 		setTitle("Ians Jukebox #better than Benzaiten");
 		setBounds(100, 100, 442, 350);
 		
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setForeground(Color.WHITE);
 		menuBar.setBackground(Color.WHITE);
 		setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmOpen = new JMenuItem("Open...");
 		mntmOpen.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				JFileChooser chooser = new JFileChooser();
 				chooser.setFileFilter(new FileFilter() {
 	                public boolean accept(File f) {
 	                    return f.getName().toLowerCase().endsWith(".mp3") || f.isDirectory();
 	                }
 	                public String getDescription() {
 	                    return "MP3 komprimierte Musikdateien (*.mp3)";
 	                }
 	            });
 			    int returnVal = chooser.showOpenDialog(contentPane);
 			    
 			    if(returnVal == JFileChooser.APPROVE_OPTION)
 			    {
 			    	File f = chooser.getSelectedFile();
 			    	System.out.println(f.getName().toLowerCase().endsWith(".mp3"));
 			    	if(f.getName().toLowerCase().endsWith(".mp3") && !f.isDirectory())
 			    	{
 				    	songs.addFile(new MP3File(f));
 			    	}
 			    	else
 			    	{
 			    		String[] tmp = f.getName().split("\\.");
 			    		String fileEnd = tmp[tmp.length-1];
 			    		JOptionPane.showMessageDialog(contentPane, "Can't open \""+fileEnd+"\" files.", "Error opening file", JOptionPane.WARNING_MESSAGE);
 			    	
 			    	}
 			    }
 			}
 		});
 		mnFile.add(mntmOpen);
 		
 		JMenu mnEdit = new JMenu("Edit");
 		menuBar.add(mnEdit);
 		
 		JMenuItem mntmPreferences = new JMenuItem("Preferences");
 		mnEdit.add(mntmPreferences);
 		
 		contentPane = new JPanel();
 		contentPane.addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(ComponentEvent e) {
 				nowPlayingLabel.setBounds(nowPlayingLabel.getBounds().x, 
 						                  nowPlayingLabel.getBounds().y, 
 						                  contentPane.getWidth(), 
 						                  nowPlayingLabel.getBounds().height);
 			}
 		});
 		contentPane.setBackground(Color.DARK_GRAY);
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		positionSlider = new JSlider();
 		positionSlider.setForeground(Color.WHITE);
 		positionSlider.setBackground(Color.DARK_GRAY);
 		positionSlider.setValue(0);
 		positionSlider.setToolTipText("position of song");
 		positionSlider.setUI(new MetalSliderUI() {
 		    protected void scrollDueToClickInTrack(int direction) {
 		        int value = slider.getValue(); 
 
 		        if (slider.getOrientation() == JSlider.HORIZONTAL) {
 		            value = this.valueForXPosition(slider.getMousePosition().x);
 		            mp.seek(value);
 		            mp.setGain(volumeSlider.getValue());
 		        } else if (slider.getOrientation() == JSlider.VERTICAL) {
 		            value = this.valueForYPosition(slider.getMousePosition().y);
 		        }
 		        slider.setValue(value);
 		    }
 		});
 		
 		JScrollPane scrollPane = new JScrollPane();
 		
 		nowPlayingLabel = new JLabel("Now Playing:");
 		nowPlayingLabel.addPropertyChangeListener("text",new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent e) {
 				if(nowPlayingLabel.getText().toLowerCase().contains("linkin park"))
 				{
 					nowPlayingLabel.setForeground(Color.GREEN);
 					Image tmp = new ImageIcon("res/lp.png").getImage().getScaledInstance(nowPlayingLabel.getHeight(), nowPlayingLabel.getHeight(), java.awt.Image.SCALE_SMOOTH);
 					nowPlayingLabel.setIcon(new ImageIcon(tmp));
 				}
 				else if(nowPlayingLabel.getText().toLowerCase().contains("portal"))
 				{
 					nowPlayingLabel.setForeground(Color.BLUE);
 					Image tmp = new ImageIcon("res/aperture.png").getImage().getScaledInstance(nowPlayingLabel.getHeight()-2, nowPlayingLabel.getHeight()-2, java.awt.Image.SCALE_SMOOTH);
 					nowPlayingLabel.setIcon(new ImageIcon(tmp));
 				}
 				else
 				{
 					nowPlayingLabel.setForeground(Color.WHITE);
 					nowPlayingLabel.setIcon(new ImageIcon());
 				}
 			}
 		});
 		nowPlayingLabel.addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(ComponentEvent e) {
 				nowPlayingLabel.setBounds(nowPlayingLabel.getBounds().x, 
 		                  nowPlayingLabel.getBounds().y, 
 		                  contentPane.getWidth(), 
 		                  nowPlayingLabel.getBounds().height);
 			}
 		});
 		nowPlayingLabel.setBackground(Color.DARK_GRAY);
 		nowPlayingLabel.setForeground(Color.WHITE);
 		
 		JPanel panel = new JPanel();
 		panel.setBackground(Color.DARK_GRAY);
 		
 		timeLabel = new JLabel("  00:00");
 		timeLabel.setForeground(Color.WHITE);
 		timeLabel.setFont(new Font("Lucida Console", Font.PLAIN, 11));
 		timeLabel.setToolTipText("time of song");
 		
 		timeleftLabel = new JLabel("- 00:00");
 		timeleftLabel.setForeground(Color.WHITE);
 		timeleftLabel.setFont(new Font("Lucida Console", Font.PLAIN, 11));
 		timeleftLabel.setToolTipText("time left of song");
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addComponent(nowPlayingLabel, GroupLayout.PREFERRED_SIZE, 416, GroupLayout.PREFERRED_SIZE)
 				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
 				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
 						.addComponent(timeleftLabel)
 						.addComponent(timeLabel))
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addComponent(positionSlider, GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE))
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addComponent(nowPlayingLabel)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addGap(18)
 							.addComponent(timeleftLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(positionSlider, GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addGap(209)
 					.addComponent(timeLabel)
 					.addContainerGap())
 		);
 		
 		play_pause_switchButtonPanel = new SwitchButtonPanel(new ImageIcon("res/play.png"), new ImageIcon("res/play_highlight.png"), 
 															 new ImageIcon("res/pause.png"), new ImageIcon("res/pause_highlight.png"));
 		play_pause_switchButtonPanel.setBackground(Color.DARK_GRAY);
 		play_pause_switchButtonPanel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(mp.getStatus() == MusicPlayer.PLAYING)
 				{
 					mp.pause();
 				}
 				else if(mp.getStatus() == MusicPlayer.PAUSED)
 				{
 					mp.resume();
 				}
 				else
 				{
 					int index = list.getSelectedIndex();
 		            indexPlaying = index;
 		            mp.stop();
 		            mp.removeListener(stupidReference);
 		     		mp = null;
 		     		mp = new MusicPlayer();	
 		     		mp.addListener(stupidReference);
 		            mp.open(songs.getFileAt(index).getFile().getAbsolutePath());
 		            mp.play();
 		            mp.setGain(volumeSlider.getValue());
 		            nowPlayingLabel.setText("Now Playing: "+songs.getElementAt(index));
 				}
 			}
 		});
 		play_pause_switchButtonPanel.setToolTipText("play/pause song");
 		
 		next_buttonPanel = new ButtonPanel(new ImageIcon("res/next.png"), new ImageIcon("res/next_highlight.png"));
 		next_buttonPanel.setBackground(Color.DARK_GRAY);
 		next_buttonPanel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				playNextSong();
 			}
 		});
 		next_buttonPanel.setToolTipText("next");
 		
 		back_buttonPanel = new ButtonPanel(new ImageIcon("res/back.png"), new ImageIcon("res/back_highlight.png"));
 		back_buttonPanel.setBackground(Color.DARK_GRAY);
 		back_buttonPanel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				playLastSong();
 			}
 		});
 		back_buttonPanel.setToolTipText("back");
 		
 		stop_buttonPanel = new ButtonPanel(new ImageIcon("res/stop.png"), new ImageIcon("res/stop_highlight.png"));
 		stop_buttonPanel.setBackground(Color.DARK_GRAY);
 		stop_buttonPanel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				mp.stop();
 			}
 		});
 		stop_buttonPanel.setToolTipText("stop playback");
 		
 		repeat_song_checkPanel = new CheckPanel(new ImageIcon("res/repeat.png"), new ImageIcon("res/repeat_highlight.png"));
 		repeat_song_checkPanel.setBackground(Color.DARK_GRAY);
 		repeat_song_checkPanel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				((CheckPanel)repeat_song_checkPanel).toggleSelected();
 			}
 		});
 		repeat_song_checkPanel.setToolTipText("repeat song");
 		
 		shuffle_songs_checkPanel = new CheckPanel(new ImageIcon("res/shuffle.png"), new ImageIcon("res/shuffle_highlight.png"));
 		shuffle_songs_checkPanel.setBackground(Color.DARK_GRAY);
 		shuffle_songs_checkPanel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				((CheckPanel)shuffle_songs_checkPanel).toggleSelected();
 			}
 		});
 		shuffle_songs_checkPanel.setToolTipText("shuffle songs");
 		
 		volumeSlider = new JSlider();
 		volumeSlider.setForeground(Color.WHITE);
 		volumeSlider.setBackground(Color.DARK_GRAY);
 		volumeSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				mp.setGain(volumeSlider.getValue());
 			}
 		});
 		volumeSlider.setMajorTickSpacing(5);
 		volumeSlider.setPaintTicks(true);
 		volumeSlider.setToolTipText("volume");
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addGap(39)
 					.addComponent(shuffle_songs_checkPanel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(repeat_song_checkPanel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(stop_buttonPanel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(back_buttonPanel, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(play_pause_switchButtonPanel, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(next_buttonPanel, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(volumeSlider, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.TRAILING)
 				.addComponent(play_pause_switchButtonPanel, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(next_buttonPanel, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(back_buttonPanel, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(stop_buttonPanel, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(repeat_song_checkPanel, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(shuffle_songs_checkPanel, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
 				.addComponent(volumeSlider, GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
 		);
 		panel.setLayout(gl_panel);
 		
 		list = new JList(songs);
 		list.setBackground(Color.DARK_GRAY);
 		list.setForeground(Color.WHITE);
 		list.setToolTipText("playlist");
 		
 		list.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == KeyEvent.VK_DELETE)
 				{
 					songs.removeFile(list.getSelectedIndex());
 				}
 			}
 		});
 		
 		MouseListener mouseListener = new MouseAdapter() {
 		     public void mouseClicked(MouseEvent e) {
 		         if (e.getClickCount() == 2) {
 		             int index = list.locationToIndex(e.getPoint());
 		             indexPlaying = index;
 		             mp.stop();
 		             mp.removeListener(stupidReference);
 		     		 mp = null;
 		     		 mp = new MusicPlayer();	
 		     		 mp.addListener(stupidReference);
 		             mp.open(songs.getFileAt(index).getFile().getAbsolutePath());
 		             mp.play();
 		             mp.setGain(volumeSlider.getValue());
 		             nowPlayingLabel.setText("Now Playing: "+songs.getElementAt(index));
 		          }
 		     }			
 		 };
 		 list.addMouseListener(mouseListener);
 		 
 		 DropTargetListener dropTargetListener = new DropTargetListener() {
 			@Override
 			public void dragEnter(DropTargetDragEvent arg0) {
 				
 			}
 			@Override
 			public void dragExit(DropTargetEvent arg0) {
 				
 			}
 			@Override
 			public void dragOver(DropTargetDragEvent arg0) {
 				
 			}
 			@Override
 			public void drop(DropTargetDropEvent e) {				
 				try {
 					Transferable tr = e.getTransferable();
 				    DataFlavor[] flavors = tr.getTransferDataFlavors();
 				    for (int i = 0; i < flavors.length; i++)
 				    	if (flavors[i].isFlavorJavaFileListType()) {
 				    		e.acceptDrop (e.getDropAction());
 				    		List files = (List) tr.getTransferData(flavors[i]);
 				    		for(int x = 0; x < files.size(); x++)
 				    		{
 				    			File f = new File(files.get(x).toString());
 						    	if(f.getName().toLowerCase().endsWith(".mp3") && !f.isDirectory())
 						    	{
 							    	songs.addFile(new MP3File(f));
 						    	}
 				    		}
 				    		e.dropComplete(true);
 				    		return;
 				    	}
 				} catch (Throwable t) { 
 					t.printStackTrace(); 
 				}
 				e.rejectDrop();
 				    
 			}
 			@Override
 			public void dropActionChanged(DropTargetDragEvent dtde) {
 				
 			}			
 		 };
 		 
 		 DropTarget dropTarget = new DropTarget(list, dropTargetListener);
 		
 		scrollPane.setViewportView(list);
 		contentPane.setLayout(gl_contentPane);
 	}
 	
 	@Override
 	public void positionChanged(int newPosition) {
 		positionSlider.setValue(newPosition);
 		int seconds = (int)((double)songs.getFileAt(this.indexPlaying).getDuration()/(double)positionSlider.getMaximum()*(double)newPosition);
 		int secondsLeft = songs.getFileAt(this.indexPlaying).getDuration()-seconds;
 		timeLabel.setText("  "+secToMinSec(seconds));
 		timeleftLabel.setText("- "+secToMinSec(secondsLeft));
 		if(newPosition == mp.getLength())
 			playNextSong();		
 	}
 	
 	private String secToMinSec(int v)
 	{
 		int seconds = v % 60;
 		int minutes = v / 60;
 		String minSec = "";
 		
 		if(minutes < 10)
 		{
 			minSec = "0"+minutes;
 		}
 		else
 		{
 			minSec = ""+minutes;
 		}
 		
 		minSec += ":";
 		
 		if(seconds < 10)
 		{
 			minSec += "0"+seconds;
 		}
 		else
 		{
 			minSec += ""+seconds;
 		}
 		
 		return minSec;
 	}
 	
 	private void playLastSong()
 	{
 		int newIdx = this.indexPlaying - 1;
 		
 		if(newIdx <= -1)
 		{
 			newIdx = songs.getSize()-1;
 		}
 		
 		mp.stop();
 		mp.removeListener(this);
 		mp = null;
 		mp = new MusicPlayer();	
 		mp.addListener(this);
 		mp.open(songs.getFileAt(newIdx).getFile().getAbsolutePath());
         mp.play();
 		mp.setGain(volumeSlider.getValue());
 		
 		nowPlayingLabel.setText("Now Playing: "+songs.getElementAt(newIdx));
 		
 		this.indexPlaying = newIdx;
 	}
 
 	private void playNextSong()
 	{
 		if(((CheckPanel)repeat_song_checkPanel).isSelected())
 		{
 			mp.stop();
 			mp.removeListener(this);
 			mp = null;
 			mp = new MusicPlayer();
 			mp.addListener(this);
 			mp.open(songs.getFileAt(indexPlaying).getFile().getAbsolutePath());
             mp.play();
 			mp.setGain(volumeSlider.getValue());
 		}
 		if(((CheckPanel)shuffle_songs_checkPanel).isSelected())
 		{
 			int newIdx = new java.util.Random().nextInt(songs.getSize());
 			
 			while(newIdx == this.indexPlaying)
 			{
 				newIdx = new java.util.Random().nextInt(songs.getSize());
 			}
 			
 			mp.stop();
 			mp.removeListener(this);
 			mp = null;
 			mp = new MusicPlayer();	
 			mp.addListener(this);
 			mp.open(songs.getFileAt(newIdx).getFile().getAbsolutePath());
             mp.play();
 			mp.setGain(volumeSlider.getValue());
 			
 			nowPlayingLabel.setText("Now Playing: "+songs.getElementAt(newIdx));
 			
 			this.indexPlaying = newIdx;
 		}
 		else
 		{
 			int newIdx = this.indexPlaying + 1;
 			if(newIdx >= this.songs.getSize())
 			{
 				newIdx = 0;
 			}
 			
 			mp.stop();
 			mp.removeListener(this);
 			mp = null;
 			mp = new MusicPlayer();	
 			mp.addListener(this);
 			mp.open(songs.getFileAt(newIdx).getFile().getAbsolutePath());
             mp.play();
 			mp.setGain(volumeSlider.getValue());
 			
 			nowPlayingLabel.setText("Now Playing: "+songs.getElementAt(newIdx));
 			
 			this.indexPlaying = newIdx;
 		}
 	}
 	
 	@Override
 	public void lengthChanged(int length) {	
 		positionSlider.setMaximum(length);
 	}
 
 	@Override
 	public void nativeKeyPressed(NativeKeyEvent e) {
 		if(e.getRawCode() == 176)
 		{
 			playNextSong();
 		}
 		else if(e.getRawCode() == 177)
 		{
 			int proPer = (int)(100.0/(double)positionSlider.getMaximum()*(double)positionSlider.getValue());
			if(proPer >= 15)
 			{
 				mp.seek(0);
 	            mp.setGain(volumeSlider.getValue());
 			}
 			else
 			{
 				playLastSong();
 			}
 		}
 		else if(e.getRawCode() == 179)
 		{
 			if(mp.getStatus() == MusicPlayer.PLAYING)
 			{
 				mp.pause();
 			}
 			else if(mp.getStatus() == MusicPlayer.PAUSED)
 			{
 				mp.resume();
 			}
 			else
 			{
 				int index = list.getSelectedIndex();
 	            indexPlaying = index;
 	            mp.stop();
 	            mp.removeListener(stupidReference);
 	     		mp = null;
 	     		mp = new MusicPlayer();	
 	     		mp.addListener(stupidReference);
 	            mp.open(songs.getFileAt(index).getFile().getAbsolutePath());
 	            mp.play();
 	            mp.setGain(volumeSlider.getValue());
 	            nowPlayingLabel.setText("Now Playing: "+songs.getElementAt(index));
 			}
 		}
 		else if(e.getRawCode() == 178)
 		{
 			mp.stop();
 		}
 		
 		else if(e.getKeyCode() == NativeKeyEvent.VK_ALT)
 		{
 			this.altPressed = true;
 		}
 	}
 
 	@Override
 	public void nativeKeyReleased(NativeKeyEvent e) {
 		
 		if(e.getKeyCode() == NativeKeyEvent.VK_ALT)
 		{
 			this.altPressed = false;
 		}
 	}
 
 	@Override
 	public void nativeKeyTyped(NativeKeyEvent e) {
 		
 	}
 
 	@Override
 	public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
 		if(altPressed)
 		{
 			int curVol = volumeSlider.getValue();
 			int maxDif = 100 - (curVol + (e.getScrollAmount() * (-1) *e.getWheelRotation()));
 			if(maxDif < 0)
 			{
 				mp.setGain(100);
 				volumeSlider.setValue(100);
 			}
 			else if(maxDif < 100)
 			{
 				mp.setGain(curVol + e.getScrollAmount());
 				volumeSlider.setValue(curVol + (e.getScrollAmount() * (-1) *e.getWheelRotation()));
 			}
 		}		
 	}
 
 	@Override
 	public void statusChanged(int status) {
 		if(status == MusicPlayer.PLAYING)
 		{
 			((SwitchButtonPanel)play_pause_switchButtonPanel).setSelected(true);
 		}
 		else if(status == MusicPlayer.PAUSED || status == MusicPlayer.STOPPED)
 		{
 			((SwitchButtonPanel)play_pause_switchButtonPanel).setSelected(false);
 		}
 		
 	}
 }
