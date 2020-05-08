 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine.Info;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.border.EmptyBorder;
 
 
 public class VideoIndexer {
 	
 	private final int videoCount = 3;
 	public enum VideoStatus {PLAYING, PAUSED, STOPPED, SEARCHING, START;};
 	BufferedImage vdo[][] = new BufferedImage[videoCount][720];
 	MyPanel imgPanel = new MyPanel();
 	JPanel bottomPanel;
 	JSlider colorSlider, audioSlider, motionSlider;
 	int videoFrame;
 	ScheduledThreadPoolExecutor videoTimer;
 	ScheduledThreadPoolExecutor audioTimer;
 	ScheduledFuture audioStopper;
 	JButton playButton, pauseButton, stopButton, searchButton;
 	MyListener listener;
 	VideoStatus status = VideoStatus.START;
     int height = 288;
     int width = 352;
     int numFrames = 720;
     
     AudioInputStream audioInputStream;
     InputStream waveStream;
     Info info;
     AudioFormat audioFormat;
     SourceDataLine dataLine;
     
 	int readBytes = 0;
 	byte[][] audioBuffer; // = new byte[EXTERNAL_BUFFER_SIZE];;
 	int buffersize;
 	int currentVideo = 0; // TODO change this to whatever video you want
 	File audio;
 	
 	String[][] colorIndex = new String[videoCount][numFrames];
 	int[][] audioIndex = new int[videoCount][numFrames];
 	int[][] motionIndex = new int[videoCount][numFrames];
 	boolean readInIndex = false;
 	String[] videofilenames = new String[12];
 	String[] audiofilenames = new String[12];
 	
 	public static void main(String[] args){
 			VideoIndexer vi = new VideoIndexer();		
 	}
 	
 	public VideoIndexer(){
 		videofilenames[0] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo1/vdo1.rgb";
 		videofilenames[1] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo2/vdo2.rgb";
 		videofilenames[2] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo3/vdo3.rgb";
		videofilenames[3] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo4/vdo4.rgb";
 		videofilenames[4] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo5/vdo5.rgb";
 		videofilenames[5] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo6/vdo6.rgb";
 		videofilenames[6] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo7/vdo7.rgb";
 		videofilenames[7] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo8/vdo8.rgb";
 		videofilenames[8] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo9/vdo9.rgb";
		videofilenames[9] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo10/vdo10.rgb";
 		videofilenames[10] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo11/vdo11.rgb";
 		videofilenames[11] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo12/vdo12.rgb";
 
 		audiofilenames[0] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo1/vdo1.wav";
 		audiofilenames[1] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo2/vdo2.wav";
 		audiofilenames[2] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo3/vdo3.wav";
		audiofilenames[3] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo4/vdo4.wav";
 		audiofilenames[4] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo5/vdo5.wav";
 		audiofilenames[5] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo6/vdo6.wav";
 		audiofilenames[6] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo7/vdo7.wav";
 		audiofilenames[7] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo8/vdo8.wav";
 		audiofilenames[8] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo9/vdo9.wav";
		audiofilenames[9] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo10/vdo10.wav";
 		audiofilenames[10] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo11/vdo11.wav";
 		audiofilenames[11] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo12/vdo12.wav";
 
 		try{
 
 			audioBuffer = new byte[videoCount][];
 			for(int newVideoIndex=0; newVideoIndex < videoCount; newVideoIndex++){
 				File file = new File(videofilenames[newVideoIndex]);
 				audio = new File(audiofilenames[newVideoIndex]);
 	
 				waveStream = new FileInputStream(audio);
 				int audiolen = (int) audio.length();
 				System.out.println("audiolen: " + audiolen);
 				buffersize = (int) Math.round((double) audiolen * 42.0 / 30000.0);
 				System.out.println("buffersize: " + buffersize);
 			    InputStream is = new FileInputStream(file);
 			    audioBuffer[newVideoIndex] = new byte[buffersize];
 			    long len = file.length();
 			    byte[] bytes = new byte[(int)len];
 			    
 			    
 			    int offset = 0;
 		        int numRead = 0;
 		        
 		        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
 		            offset += numRead;
 		        }
 		        int ind;
 		    	int maxInd = height * width;
 		    	for(int i = 0; i < numFrames; i++){
 		    		ind = maxInd * i * 3;
 		    		//System.out.println("ind in outer loop: " + ind);
 		    		vdo[newVideoIndex][i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 					for(int y = 0; y < height; y++){
 				
 						for(int x = 0; x < width; x++){
 					 
 							byte a = 0;
 							byte r = bytes[ind];
 							byte g = bytes[ind+height*width];
 							byte b = bytes[ind+height*width*2]; 
 							
 							int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
 							//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
 							//System.out.println("x: " + x + "; y: "+ y + "; pix " + pix + "; ind: " + ind);
 							vdo[newVideoIndex][i].setRGB(x,y,pix);
 							ind++;
 						}
 					}
 	
 					ind = (ind * 3) / (i + 1);
 		    	}
 
 			}
 		}catch(Exception e){
 			System.out.println(e);
 		}
 	
 		listener = new MyListener();
 		
 		JFrame frame = new JFrame();
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
 	    JPanel container = new JPanel();
 	    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
 
 	    JScrollPane scrollContainer = new JScrollPane(container);
 	    
 	    JPanel topPanel = new JPanel();
 	    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
 	    JPanel bottomContainer = new JPanel();
 	    bottomPanel = new JPanel();
 	    JScrollPane bottomScrollPane = new JScrollPane(bottomPanel); // TODO
 	    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
 	    bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 	    bottomContainer.add(bottomScrollPane);
 	    bottomScrollPane.setPreferredSize(new Dimension(450, 250));
 	    
 	    //imgPanel.setBounds(0, 0, width, height);
 	    imgPanel.setPreferredSize(new Dimension(width, height));
 	    imgPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 	    
 	    JPanel buttonPanel = new JPanel();
 	    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 	    playButton = new JButton("Play");
 	    playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 	    playButton.addActionListener(listener);
 	    pauseButton = new JButton("Pause");
 	    pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 	    pauseButton.addActionListener(listener);
 	    stopButton = new JButton("Stop");
 	    stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 	    stopButton.addActionListener(listener);
 	    searchButton = new JButton("Search");
 	    searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 	    searchButton.addActionListener(listener);
 	    
 	    colorSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
 	    audioSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
 	    motionSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
 
 	    colorSlider.setMajorTickSpacing(100);
 	    colorSlider.setMinorTickSpacing(25);
 	    colorSlider.setPaintTicks(true);
 	    colorSlider.setPaintLabels(true);
 	    
 	    audioSlider.setMajorTickSpacing(100);
 	    audioSlider.setMinorTickSpacing(25);
 	    audioSlider.setPaintTicks(true);
 	    audioSlider.setPaintLabels(true);
 	    
 	    motionSlider.setMajorTickSpacing(100);
 	    motionSlider.setMinorTickSpacing(25);
 	    motionSlider.setPaintTicks(true);
 	    motionSlider.setPaintLabels(true);
 	    
 	    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
 	    labelTable.put( new Integer(0), new JLabel("0"));
 	    labelTable.put( new Integer(50), new JLabel(".5"));
 	    labelTable.put( new Integer(100), new JLabel("1"));
 	    labelTable.put( new Integer(150), new JLabel("1.5"));
 	    labelTable.put( new Integer(200), new JLabel("2"));
 	    colorSlider.setLabelTable(labelTable);
 	    audioSlider.setLabelTable(labelTable);
 	    motionSlider.setLabelTable(labelTable);
 	    
 	    JPanel csliderp = new JPanel();
 	    csliderp.add(new JLabel("C"));
 	    csliderp.add(colorSlider);
 	    JPanel asliderp = new JPanel();
 	    asliderp.add(new JLabel("A"));
 	    asliderp.add(audioSlider);
 	    JPanel msliderp = new JPanel();
 	    msliderp.add(new JLabel("M"));
 	    msliderp.add(motionSlider);
 	    
 	    buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 	    buttonPanel.add(playButton);
 	    buttonPanel.add(pauseButton);
 	    buttonPanel.add(stopButton);
 	    buttonPanel.add(searchButton);
 	    buttonPanel.add(csliderp);
 	    buttonPanel.add(asliderp);
 	    buttonPanel.add(msliderp);
 	    
 	    topPanel.add(imgPanel);
 	    topPanel.add(buttonPanel);
 	    
 	    for (int i = 0; i < videoCount; i++){
 	    	ImageStripPanel temp = new ImageStripPanel(i, -1);
 	    	bottomPanel.add(temp);
 	    	temp.setAlignmentX(Component.CENTER_ALIGNMENT);
 	    }
 
 	   // bottomPanel.add(new JLabel("image strip of frames will go here"));
 	    container.add(topPanel);
 	    container.add(bottomContainer);
 	    
 	    frame.getContentPane().add(scrollContainer, BorderLayout.CENTER);
 	    frame.pack();
 	    frame.setVisible(true); 	
 
 		videoFrame = 0;
 	    imgPanel.img = vdo[currentVideo][videoFrame];
 
 
 		audio = new File(audiofilenames[currentVideo]);
 
 		audioInputStream = null;
 		try {
 
 			waveStream = new FileInputStream(audio);
 			InputStream bufferedIn = new BufferedInputStream(waveStream);
 		    audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
 		} catch (UnsupportedAudioFileException e1) {
 			System.out.println(e1);
 		    //throw new PlayWaveException(e1);
 		} catch (IOException e1) {
 			System.out.println(e1);
 		    //throw new PlayWaveException(e1);
 		}
 	
 		// Obtain the information about the AudioInputStream
 		audioFormat = audioInputStream.getFormat();
 		System.out.println("audio frame rate: " + audioFormat.getFrameRate());
 		System.out.println("audio frame size: " + audioFormat.getFrameSize());
 		
 		info = new Info(SourceDataLine.class, audioFormat);
 	
 		// opens the audio channel
 		dataLine = null;
 		try {
 		    dataLine = (SourceDataLine) AudioSystem.getLine(info);
 		    dataLine.open(audioFormat, buffersize);
 		} catch (LineUnavailableException e1) {
 			System.out.println(e1);
 		    //throw new PlayWaveException(e1);
 		}
 	}
 
 	
 	public void play(){
 		if (status != VideoStatus.PLAYING){
 			status = VideoStatus.PLAYING;
 					
 			videoTimer = new ScheduledThreadPoolExecutor(5);
 			videoTimer.scheduleAtFixedRate(new PlayVideo(), 0, 41666666, TimeUnit.NANOSECONDS); // 41.66	
 			dataLine.start();
 			audioTimer = new ScheduledThreadPoolExecutor(5);
 			audioTimer.scheduleAtFixedRate(new PlayAudio(), 0, 41666666, TimeUnit.NANOSECONDS);
 		}
 	}
 	
 	public void pause(){
 		if (status == VideoStatus.PLAYING){
 			status = VideoStatus.PAUSED;
 			videoTimer.shutdownNow();
 			audioTimer.shutdownNow();
 			dataLine.stop();	
 		}
 	}
 	
 	public void stop(){
 		if (status == VideoStatus.SEARCHING)
 			return;
 		
 		status = VideoStatus.STOPPED;
 
 		videoTimer.shutdownNow();
 		videoFrame = 0;
 	    imgPanel.img = vdo[currentVideo][videoFrame];
 	    imgPanel.repaint();	    
 
 		audioTimer.shutdownNow();
 		dataLine.stop();
 		
 
 		try {
 			waveStream = new FileInputStream(audio);
 		} catch (FileNotFoundException e) {
 			System.out.println(e);
 			
 		}
 	    audioInputStream = null;
 		try {
 			InputStream bufferedIn = new BufferedInputStream(waveStream);
 		    audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
 		} catch (UnsupportedAudioFileException e1) {
 			System.out.println(e1);
 		    //throw new PlayWaveException(e1);
 		} catch (IOException e1) {
 			System.out.println(e1);
 		    //throw new PlayWaveException(e1);
 		}
 	
 		// Obtain the information about the AudioInputStream
 		audioFormat = audioInputStream.getFormat();
 		info = new Info(SourceDataLine.class, audioFormat);
 	
 		// opens the audio channel
 		dataLine = null;
 		try {
 		    dataLine = (SourceDataLine) AudioSystem.getLine(info);
 		    dataLine.open(audioFormat, buffersize);
 		} catch (LineUnavailableException e1) {
 			System.out.println(e1);
 		    //throw new PlayWaveException(e1);
 		}
 		
 	}
 	
 	public void search(){
 		if (status == VideoStatus.PLAYING) {
 			pause();
 		}
 		status = VideoStatus.SEARCHING;
 		System.out.print("\nSearching...");
 		try{
 			
 			//read in color index
 			if (!readInIndex){
 				FileInputStream cfstream = new FileInputStream("colorindex.txt");
 				DataInputStream cin = new DataInputStream(cfstream);
 				BufferedReader cbr = new BufferedReader(new InputStreamReader(cin));
 				FileInputStream afstream = new FileInputStream("audioindex.txt");
 				DataInputStream ain = new DataInputStream(afstream);
 				BufferedReader abr = new BufferedReader(new InputStreamReader(ain));
				FileInputStream mfstream = new FileInputStream("motionindex.txt");
 				DataInputStream min = new DataInputStream(mfstream);
 				BufferedReader mbr = new BufferedReader(new InputStreamReader(min));
 				String cline, aline, mline;
 				int count = 0;
 				while ((cline = cbr.readLine()) != null && (aline = abr.readLine()) != null &&  (mline = mbr.readLine()) != null && count < videoCount)   { // read in color index into an array
 					
 					StringTokenizer cst = new StringTokenizer(cline);
 					StringTokenizer ast = new StringTokenizer(aline);
 					StringTokenizer mst = new StringTokenizer(mline);
 					int videoIndex = Integer.parseInt(cst.nextToken());
 					ast.nextToken();
 					mst.nextToken();
 						for(int i = 0; i < numFrames; i++){
 							colorIndex[videoIndex][i] = cst.nextToken().trim();
 							audioIndex[videoIndex][i] = Integer.parseInt(ast.nextToken());
 							motionIndex[videoIndex][i]= Integer.parseInt(mst.nextToken());
 						}
 					
 					count++;
 				}
 				readInIndex = true;
 				cin.close();
 				ain.close();
 				min.close();
 			}
 
 			int[] highlightFrame = new int[videoCount]; // tells you which frame to highlight for each video
 			double[] minDiffs = new double[videoCount];
 			String currentHSV = colorIndex[currentVideo][videoFrame];
 			int currentAudio = audioIndex[currentVideo][videoFrame];
 			int currentMotion = audioIndex[currentVideo][videoFrame];
 			
 			double cOffset, aOffset, mOffset;
 			if (colorSlider.getValue() != 0){
 				cOffset = 1.0 / ((double) colorSlider.getValue() / 100.0);
 			}else{
 				cOffset = 0;
 			}
 
 			if (audioSlider.getValue() != 0){
 				aOffset = 1.0 / ((double) audioSlider.getValue() / 100.0);
 			}else{
 				aOffset = 0;
 			}
 
 			if (motionSlider.getValue() != 0){
 				mOffset = 1.0 / ((double) motionSlider.getValue() / 100.0);
 			}else{
 				mOffset = 0;
 			}
 			
 			for(int i = 0; i < videoCount; i++){	
 				double minDiff = 32 * 3; // this is max difference
 				int minFrame = 0;
 				if (i != currentVideo){ // don't compare video to itself
 					for(int j = 0; j < numFrames; j++){
 						double newDiff = (getHsvDifference(currentHSV, colorIndex[i][j]) * cOffset)
 										+ (Math.abs(currentAudio - audioIndex[i][j]) * aOffset)
 										+ (Math.abs(currentMotion - motionIndex[i][j]) * mOffset);
 						if (newDiff < minDiff){
 							minDiff = newDiff;
 							minFrame = j;
 							//System.out.println("new minFrame:" + minFrame + "; minDiff: " + minDiff);
 						}
 					}
 				}
 				
 				highlightFrame[i] = minFrame;
 				minDiffs[i] = minDiff;
 			}
 			
 			int[] sorted = new int[videoCount - 1];
 			for (int i = 0; i < sorted.length; i++){
 				sorted[i] = i;
 			}
 			if (currentVideo != videoCount - 1){
 				sorted[currentVideo] = videoCount - 1;
 			}
 			
 			int min1;
 			for (int i=0; i< sorted.length; i++){
 				min1 = i;
 				for (int j=i; j<sorted.length; j++){
 					if (minDiffs[sorted[j]] < minDiffs[sorted[min1]]){
 						min1 = j;
 					}
 				}
 				if(min1 != i){
 					int temp = sorted[i];
 					sorted[i] = sorted[min1];
 					sorted[min1] = temp;
 				}
 				
 			}
 			
 			bottomPanel.removeAll();
 			bottomPanel.add(new ImageStripPanel(currentVideo, -1));
 			for (int i = 0; i < sorted.length; i++){
 					bottomPanel.add(new ImageStripPanel(sorted[i], highlightFrame[sorted[i]]));
 			}
 			bottomPanel.validate();
 			bottomPanel.repaint();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		System.out.print("Done!\n");
 	}
 	
 	public int getHsvDifference(String hsv1, String hsv2){
 		int h1 = Integer.parseInt(hsv1.substring(0, 2));
 		int s1 = Integer.parseInt(hsv1.substring(2, 3));
 		int v1 = Integer.parseInt(hsv1.substring(3));
 		int h2 = Integer.parseInt(hsv2.substring(0, 2));
 		int s2 = Integer.parseInt(hsv2.substring(2, 3));
 		int v2 = Integer.parseInt(hsv2.substring(3));
 		
 		int h = Math.abs(h1 - h2);
 		if (h > 8){
 			int diff = h - 8;
 			h = h - (2 * diff);
 		}
 		
 		int s = Math.abs(s1 - s2);
 		int v = Math.abs(v1 - v2);
 		
 		return (h * 2) + s + v;
 	}
 	
 	
 	public class MyListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == playButton){
 				System.out.println("play pressed");				
 				play();
 			}else if (e.getSource() == pauseButton){
 				System.out.println("pause pressed");
 				pause();
 			}else if (e.getSource() == stopButton){
 				System.out.println("stop pressed");
 				stop();
 			}else if (e.getSource() == searchButton){
 				System.out.println("search pressed");
 				search();
 			}			
 		}
 		
 	} // end of MyListener class
 	
 	public class PlayVideo implements Runnable {
 		public void run(){
 			videoFrame++;
 			if (videoFrame >= vdo[currentVideo].length){
 				videoFrame = 0;
 				videoTimer.shutdownNow();
 				return;
 			}
 			imgPanel.img = vdo[currentVideo][videoFrame];
 			imgPanel.repaint();
 		}
 	} // end of PlayVideo Timer class
 
 	
 	public class PlayAudio implements Runnable {
 		public void run(){
 			try {
 
 				readBytes = audioInputStream.read(audioBuffer[currentVideo], 0, audioBuffer[currentVideo].length);
 
 				if (readBytes >= 0){
 					//System.out.println(readBytes);
 					//System.arraycopy(audioBuffer, 0, temp, 0, audioBuffer.length);
 					
 					byte temp[] = new byte[audioBuffer.length];
 					for (int i = 0; i < temp.length; i++)
 						temp[i] = audioBuffer[currentVideo][i];
 					
 					int[] toReturn = new int[temp.length / 2];
 				    int index = 0;
 				    
 				    for (int audioByte = 0; audioByte < temp.length;)
 				    {
 				        //for (int channel = 0; channel < nbChannels; channel++)
 				        //{
 				            // Do the byte to sample conversion.
 				            int low = (int) temp[audioByte];
 				            audioByte++;
 				            if (audioByte < temp.length) {
 					            int high = (int) temp[audioByte];
 					            audioByte++;
 					            
 					            int sample = (high << 8) + (low & 0x00ff);
 					            if (sample > 10000)
 					            System.out.println(sample);
 					            toReturn[index] = sample;
 				            }
 				        //}
 				        index++;
 				    }
 
 				    dataLine.write(audioBuffer[currentVideo], 0, readBytes);
 				    
 				}else{
 					dataLine.drain();
 				    dataLine.close();
 					audioTimer.shutdownNow();
 					return;
 				}
 				
 			} catch (IOException e) {
 				System.out.println();
 			}
 		}
 	}
 	
 	public class MyPanel extends JPanel{
 		public BufferedImage img;
 		int imgFrame = -1;
 		int videoIndex = -1;
 		
 		MyPanel(BufferedImage i){
 			super();
 			img = i;
 		}
 		
 		MyPanel(){
 			super();
 		}
 		
 		public void paintComponent(Graphics g){
 			super.paintComponent(g);
 			Graphics2D g2d = (Graphics2D) g;
 			if (img != null){
 				g2d.drawImage(img, null, 3,3);
 			}
 		}
 		
 		public void paint(Graphics g){
 			super.paint(g);
 			Graphics2D g2d = (Graphics2D) g;
 			if (img != null){
 				g2d.drawImage(img, null, 3,3);
 			}
 		}
 		
 		public String toString(){
 			return("imgFrame: " + imgFrame);
 		}
 		
 
 	} // end MyPanel class
 	
 	public class ImageStripPanel extends JPanel{
 		int numPanelFrames = 20;
 		int newHeight = 50;
 		int newWidth = 61;
 		MyPanel panels[] = new MyPanel[numPanelFrames];
 		MyMouseListener mml;
 		
 		public ImageStripPanel(int videoIndex, int highlightFrame){
 			super();
 			JPanel panel = new JPanel();
 			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 			JScrollPane scrollpane = new JScrollPane(panel);
 			this.add(scrollpane);
 			scrollpane.setPreferredSize(new Dimension(400, 75));			
 			this.setPreferredSize(new Dimension(400, 85));
 			mml = new MyMouseListener();
 			int highlighti = -1; 
 			if (highlightFrame != -1){ // need to highlight a frame
 				highlighti = (int) Math.round((double)highlightFrame / (double)(numFrames / numPanelFrames));
 			}
 			for (int i = 0; i < numPanelFrames; i++){
 				BufferedImage tempImg;
 				if (i == highlighti){
 					tempImg = vdo[videoIndex][highlightFrame];
 				}else{
 					tempImg = vdo[videoIndex][numFrames * i / numPanelFrames];
 				}
 				BufferedImage newImg = new BufferedImage(newWidth, newHeight, tempImg.getType());  
 		        Graphics2D g2d = newImg.createGraphics();  
 		        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
 		        g2d.drawImage(tempImg, 0, 0, newWidth, newHeight, 0, 0, width, height, null);  
 		        g2d.dispose();  
 		        
 		        panels[i] = new MyPanel(newImg);
 		        panels[i].videoIndex = videoIndex;
 				if (i == highlighti){
 			        panels[i].imgFrame = highlightFrame;
 			        panels[i].setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.RED));
 				}else{
 			        panels[i].imgFrame = numFrames * i / numPanelFrames;
 				}
 		        panels[i].setPreferredSize(new Dimension(newWidth + 5, newHeight + 5));
 		        panels[i].addMouseListener(mml);
 		        panel.add(panels[i]);
 		        panels[i].repaint();
 			}
 			
 		}
 		
 		public ImageStripPanel(){
 			super();
 		}
 	} // end ImageStripPanel
 	
 	
 	public class MyMouseListener implements MouseListener{
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			//System.out.println(arg0.getSource().toString());
 			if (status == VideoStatus.PLAYING){
 				pause();
 			}
 			int videoIndex = ((MyPanel)arg0.getSource()).videoIndex;	
 			if(videoIndex != currentVideo){
 				currentVideo = videoIndex;
 				audio = new File(audiofilenames[currentVideo]);
 			}
 			
 			int temp = ((MyPanel)arg0.getSource()).imgFrame;
 
 			videoFrame = temp;
 			imgPanel.img = vdo[currentVideo][videoFrame];
 			imgPanel.repaint();
 			
 			
 			
 			try {
 				waveStream = new FileInputStream(audio);
 			} catch (FileNotFoundException e) {
 				System.out.println(e);
 			}
 			
 		    audioInputStream = null;
 			try {
 				InputStream bufferedIn = new BufferedInputStream(waveStream);
 				audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
 			    //audioInputStream.skip(temp * buffersize);
 			} catch (UnsupportedAudioFileException e1) {
 				System.out.println(e1);
 			    //throw new PlayWaveException(e1);
 			} catch (IOException e1) {
 				System.out.println(e1);
 				e1.printStackTrace();
 			    //throw new PlayWaveException(e1);
 			}
 		
 			// Obtain the information about the AudioInputStream
 			audioFormat = audioInputStream.getFormat();
 			info = new Info(SourceDataLine.class, audioFormat);
 
 		      
 			// opens the audio channel
 			dataLine = null;
 			try {
 				//System.out.println("img frame temp: " + temp);
 				long offset = temp * 30 * (long) audioFormat.getFrameRate() * (long)audioFormat.getFrameSize() / (long)numFrames;
 				System.out.println("skipping " + (long)offset + " bytes");				
 				audioInputStream.skip(temp * 30 * (long) audioFormat.getFrameRate() * (long)audioFormat.getFrameSize() / (long)numFrames);
 			    dataLine = (SourceDataLine) AudioSystem.getLine(info);
 			    dataLine.open(audioFormat, buffersize);
 			} catch (LineUnavailableException e1) {
 				System.out.println(e1);
 			    //throw new PlayWaveException(e1);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 }
