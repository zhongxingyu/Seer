 /*
  * This file is part of Beads. See http://www.beadsproject.net for all information.
  */
 package net.beadsproject.beads.core.io;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.Line;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.TargetDataLine;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.ListSelectionModel;
 
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.IOAudioFormat;
 import net.beadsproject.beads.core.AudioIO;
 import net.beadsproject.beads.core.UGen;
 import net.beadsproject.beads.data.Buffer;
 import net.beadsproject.beads.ugens.WavePlayer;
 
 /**
  * The default {@link AudioIO}, uses JavaSound.
  */
 public class JavaSoundAudioIO extends AudioIO {
 
 	/** The default system buffer size. */
 	public static final int DEFAULT_OUTPUT_BUFFER_SIZE = 2000;
 
 	/** The mixer. */
 	private Mixer mixer;
 
 	/** The source data line. */
 	private SourceDataLine sourceDataLine;
 	
 	/** The target data line. */
 	private TargetDataLine targetDataLine;
 
 	/** The system buffer size in frames. */
 	private int systemBufferSizeInFrames;
 
 	/** Thread for running realtime audio. */
 	private Thread audioThread;
 
 	/** The priority of the audio thread. */
 	private int threadPriority;
 
 	/** The current output byte buffer. */
 	private byte[] bbufOut;
 	
 	/** The current input byte buffer. */
 	private byte[] bbufIn;
 	
 	/** Used to determine whether inputs need configuring. */
 	private boolean hasInput;
 	
 
 	/**
 	 * Instantiates a new java sound audio io.
 	 */
 	public JavaSoundAudioIO() {
 		this(DEFAULT_OUTPUT_BUFFER_SIZE);
 	}
 
 	/**
 	 * Instantiates a new java sound audio io.
 	 *
 	 * @param systemBufferSize the system buffer size
 	 */
 	public JavaSoundAudioIO(int systemBufferSize) {
 		this.systemBufferSizeInFrames = systemBufferSize;
 		setThreadPriority((int)(Thread.MAX_PRIORITY));
 	}
 
 	/**
 	 * Initialises JavaSound.
 	 *
 	 * @return true, if successful
 	 */
 	private boolean setupOutputJavaSound() {
 		IOAudioFormat baf = getContext().getAudioFormat();
 		AudioFormat jsaf = new AudioFormat(baf.sampleRate, baf.bitDepth, baf.outputs, baf.signed, baf.bigEndian);
 		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
 				jsaf);
 		try {
 			int inputBufferSize = systemBufferSizeInFrames * jsaf.getFrameSize();
 			sourceDataLine = (SourceDataLine) mixer.getLine(info);
 			if (systemBufferSizeInFrames < 0)
 				sourceDataLine.open(jsaf);
 			else
 				sourceDataLine.open(jsaf, inputBufferSize);
 			System.out.println("JavaSoundAudioIO: Chosen output is "
 					+ sourceDataLine.getLineInfo()
 					+ ", buffer size in bytes: " + inputBufferSize);
 		} catch (LineUnavailableException ex) {
 			System.out
 					.println(getClass().getName() + " : Error getting line\n");
 		}
 		return true;
 	}
 
 	/**
 	 * Setup input java sound.
 	 */
 	private void setupInputJavaSound() {
 		IOAudioFormat baf = getContext().getAudioFormat();
 		AudioFormat jsaf = new AudioFormat(baf.sampleRate, baf.bitDepth, baf.inputs, baf.signed, baf.bigEndian);
 		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
 				jsaf);
 		try {
 			//hackety hack
 			int inputBufferSize = 2 * systemBufferSizeInFrames * jsaf.getFrameSize();
 			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
 			targetDataLine.open(jsaf, inputBufferSize);
 			if (targetDataLine == null)
 				System.out.println("no line");
 			else
 				System.out.println("JavaSoundAudioIO: Chosen input is "
 						+ targetDataLine.getLineInfo()
 						+ ", buffer size in bytes: " + inputBufferSize);
 		} catch (LineUnavailableException ex) {
 			System.out.println(getClass().getName()
 					+ " : Error getting line\n");
 		}
 	}
 	
 	/**
 	 * Presents a choice of mixers on the commandline.
 	 */
 	public void chooseMixerCommandLine() {
 		System.out.println("Choose a mixer...");
 		printMixerInfo();
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		try {
 			selectMixer(Integer.parseInt(br.readLine()) - 1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Presents a choice of mixers and also audio format options in a GUI.
 	 * Also provides options for reading and writing these settings.
 	 */
 	public AudioContext setUpAudioContextFromGUI() {
 		//gui elements
 		JPanel mainPanel = new JPanel();
 		final JFrame f = new JFrame();
 		//sample rate
 		final JComboBox sampleRateChooser = new JComboBox();
 		sampleRateChooser.addItem(44100f);
 		sampleRateChooser.addItem(48000f);
 		sampleRateChooser.addItem(88200f);
 		sampleRateChooser.addItem(96000f);
 		mainPanel.add(sampleRateChooser);
 		//bit depth
 		final JComboBox bitDepthChooser = new JComboBox();
 		bitDepthChooser.addItem(16);
 		bitDepthChooser.addItem(24);
 		mainPanel.add(bitDepthChooser);
 		//input channels
 		final JComboBox inputChannelsChooser = new JComboBox();
 		for(int i = 0; i <= 32; i++) {
 			inputChannelsChooser.addItem(i);
 		}
 		mainPanel.add(inputChannelsChooser);
 		//output channels
 		final JComboBox outputChannelsChooser = new JComboBox();
 		for(int i = 1; i <= 32; i++) {
 			outputChannelsChooser.addItem(i);
 		}
 		outputChannelsChooser.setSelectedIndex(1);
 		mainPanel.add(outputChannelsChooser);
 		//signed/unsigned
 		final JComboBox signedUnsignedChooser = new JComboBox();
 		signedUnsignedChooser.addItem("Signed");
 		signedUnsignedChooser.addItem("Unsigned");
 		mainPanel.add(signedUnsignedChooser);
 		//big endian
 		final JComboBox bigEndianChooser = new JComboBox();
 		bigEndianChooser.addItem("Big endian");
 		bigEndianChooser.addItem("Litte endian");
 		mainPanel.add(bigEndianChooser);
 		//buffer size
 		final JComboBox bufferSizeChooser = new JComboBox();
 		bufferSizeChooser.addItem(64);
 		bufferSizeChooser.addItem(128);
 		bufferSizeChooser.addItem(512);
 		bufferSizeChooser.addItem(1024);
 		bufferSizeChooser.addItem(2048);
 		bufferSizeChooser.addItem(4096);
 		bufferSizeChooser.setSelectedIndex(2);
 		mainPanel.add(bufferSizeChooser);
 		//list of available devices
 		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
 		final JList mixerList = new JList(mixerinfo);
 		mixerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		mixerList.setSelectedIndex(0);
 		mainPanel.add(mixerList);
 		//confirm button
 		JButton confirmButton = new JButton();
 		mainPanel.add(confirmButton);
 		class AudioContextHandle {
 			AudioContext ac;
 			boolean done;
 		}
 		final AudioContextHandle acHandle = new AudioContextHandle();
 		confirmButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int mixerSelection = 0;
 				IOAudioFormat audioFormat = null;
 				float sampleRate = (Float)sampleRateChooser.getSelectedItem();
 				int sampleSizeInBits = (Integer)bitDepthChooser.getSelectedItem();
 				int inputChannels = (Integer)inputChannelsChooser.getSelectedItem();
 				int outputChannels = (Integer)outputChannelsChooser.getSelectedItem();
 				boolean signed = ((String)signedUnsignedChooser.getSelectedItem()).equals("Signed");
 				boolean bigEndian = ((String)bigEndianChooser.getSelectedItem()).equals("Big endian");
 				audioFormat = new IOAudioFormat(sampleRate, sampleSizeInBits, inputChannels, outputChannels, signed, bigEndian);
 				mixerSelection = mixerList.getSelectedIndex();
 				//OK, we're ready to go
 				AudioFormat jsafIn = new AudioFormat(audioFormat.sampleRate, audioFormat.bitDepth, audioFormat.inputs, audioFormat.signed, audioFormat.bigEndian);
 				AudioFormat jsafOut = new AudioFormat(audioFormat.sampleRate, audioFormat.bitDepth, audioFormat.outputs, audioFormat.signed, audioFormat.bigEndian);
 				
 				//but first we should test the mixer for the given audioFormat
 				if(!testMixer(mixerSelection, jsafIn, jsafOut)) {
 					JOptionPane.showMessageDialog(f, "The chosen mixer doesn't work with the given settings.");
 					return;
 				}
 				int bufferSize = (Integer)bufferSizeChooser.getSelectedItem();
 				selectMixer(mixerSelection);
 				acHandle.ac = new AudioContext(bufferSize, JavaSoundAudioIO.this, audioFormat);
 				acHandle.done = true;
 				f.dispose();
 			}
 		});
 		//finish up and show
 		f.setContentPane(mainPanel);
 		f.pack();
 		f.setResizable(false);
 		f.setVisible(true);
 		while(!acHandle.done) {
 			try {
 				Thread.sleep(10);
 			} catch(InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		return acHandle.ac;
 	}
 	
 	public boolean testMixer(int i, AudioFormat inputAudioFormat, AudioFormat outputAudioFormat) {
 		boolean success = false;
 		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
 		Mixer testMixer = AudioSystem.getMixer(mixerinfo[i]);
 		DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, outputAudioFormat);
 		DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, inputAudioFormat);
 		try {
 			if(outputAudioFormat.getChannels() > 0) {
 				SourceDataLine testSourceDataLine = (SourceDataLine) testMixer.getLine(outputInfo);
 				testSourceDataLine.open(outputAudioFormat);
 				testSourceDataLine.close();
 			}
 			if(inputAudioFormat.getChannels() > 0) {
 				TargetDataLine testTargetDataLine = (TargetDataLine) testMixer.getLine(inputInfo);
 				testTargetDataLine.open(inputAudioFormat);
 				testTargetDataLine.close();
 			}
 			success = true;
 		} catch(Exception e) {
 			success = false;
 			e.printStackTrace();
 		}
 		return success;
 	}
 
 	/**
 	 * Select a mixer by index.
 	 * 
 	 * @param i
 	 *            the index of the selected mixer.
 	 */
 	public void selectMixer(int i) {
 		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
 		mixer = AudioSystem.getMixer(mixerinfo[i]);
 		if (mixer != null) {
 			System.out.print("JavaSoundAudioIO: Chosen mixer is ");
 			System.out.println(mixer.getMixerInfo().getName() + ".");
 		} else {
 			System.out.println("JavaSoundAudioIO: Failed to get mixer.");
 		}
 	}
 
 	/**
 	 * Prints information about the current Mixer to System.out.
 	 */
 	public static void printMixerInfo() {
 		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
 		for (int i = 0; i < mixerinfo.length; i++) {
 			String name = mixerinfo[i].getName();
 			if (name.equals(""))
 				name = "No name";
 			System.out.println((i + 1) + ") " + name + " --- "
 					+ mixerinfo[i].getDescription());
 			Mixer m = AudioSystem.getMixer(mixerinfo[i]);
 			Line.Info[] lineinfo = m.getSourceLineInfo();
 			for (int j = 0; j < lineinfo.length; j++) {
 				System.out.println("  - " + lineinfo[j].toString());
 			}
 		}
 	}
 
 	/**
 	 * Sets the priority of the audio thread. Default priority is
 	 * Thread.MAX_PRIORITY.
 	 *
 	 * @param priority the new thread priority
 	 */
 	public void setThreadPriority(int priority) {
 		this.threadPriority = priority;
 		if (audioThread != null)
 			audioThread.setPriority(threadPriority);
 	}
 
 	/**
 	 * Gets the thread priority.
 	 *
 	 * @return The priority of the audio thread.
 	 */
 	public int getThreadPriority() {
 		return this.threadPriority;
 	}
 
 	/**
 	 * Shuts down JavaSound elements, SourceDataLine and Mixer.
 	 *
 	 * @return true, if successful
 	 */
 	private boolean destroy() {
 		sourceDataLine.drain();
 		sourceDataLine.flush();
 		sourceDataLine.stop();
 		sourceDataLine.close();
 		sourceDataLine = null;
 		if(mixer != null) {
 			mixer.close();
 			mixer = null;
 		}
 		System.out.println("JavaSoundAudioIO: stopped.");
 		return true;
 	}
 
 	/**
 	 * Starts the audio system running.
 	 *
 	 * @return true, if successful
 	 */
 	@Override
 	protected boolean start() {
 		audioThread = new Thread(new Runnable() {
 			public void run() {
 				// create JavaSound stuff only when needed
 				if(mixer == null) {
 					selectMixer(0);
 				}
 				setupOutputJavaSound();
 				if(hasInput && targetDataLine == null) {
 					setupInputJavaSound();
 				}
 				// start the update loop
 				runRealTime();
 				// return from above method means context got stopped, so now
 				// clean up
 				destroy();
 			}
 		});
 		audioThread.setPriority(threadPriority);
 		audioThread.start();
 		return true;
 	}
 
 	/** Update loop called from within audio thread (created in start() method). */
 	private void runRealTime() {
 		AudioContext context = getContext();
 		IOAudioFormat audioFormat = context.getAudioFormat();
 		int bufferSizeInFrames = context.getBufferSize();
 		boolean isBigEndian = audioFormat.bigEndian;
 		int channels = audioFormat.outputs;
		bbufOut = new byte[bufferSizeInFrames * audioFormat.bitDepth * 8];
 		try {
 			sourceDataLine.open();
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
 		}
 		sourceDataLine.start();
 		if(hasInput) {
			bbufIn = new byte[bufferSizeInFrames * audioFormat.bitDepth * 8];
 			targetDataLine.start();
 		}
 		//time check
 //		int nLoops = 1;
 //		long lastTimeMillis = System.currentTimeMillis();
 //		long nLoopsInterval = (long)(context.getBufferSize() * 1000 * nLoops / context.getSampleRate());
 //		System.out.println(nLoopsInterval);
 //		int count = 0;
 
 //		System.out.println("SLEEPING AT START");
 		/*
 		 * This allows the audio inputs to buffer up some input data.
 		 * At present this is rather crude, stepping us straight into the 
 		 * middle of the buffered space, introducing some io latency.
 		 */
 		try {
 			Thread.sleep((long)context.samplesToMs(sourceDataLine.getBufferSize() / 2));
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		int i,j, counter;
 		
 		while (context.isRunning()) {
 			
 			/////////////////// TEST CODE //////////////////
 			/*
 			 * Evidently, JavaSound on OS X is wretched. There is no
 			 * hope when using audio inputs. 
 			 */
 			//first consider waiting
 //			if(count % nLoops == 0) {
 //				long timeNow = System.currentTimeMillis();
 //				long timeSince = timeNow - lastTimeMillis;
 //				lastTimeMillis = timeNow;
 //				if(timeSince < nLoopsInterval) {
 //					System.out.println("AHEAD " + timeSince + " " + nLoopsInterval);
 //					try {
 //						Thread.sleep(nLoopsInterval - timeSince);
 //					} catch (InterruptedException e) {
 //						e.printStackTrace();
 //					}
 //				} else {
 //					System.out.println("BEHIND " + timeSince + " " + nLoopsInterval);
 //				}
 //			}
 //			count++;
 			/////////////////////////////////////////////////
 			
 			
 
 			/////// UPDATE ///////
 			
 			update(); // this propagates update call to context
 			
 			/////// OUTPUT ///////
 			
 			if (isBigEndian) {
 				for (i = 0, counter = 0; i < bufferSizeInFrames; ++i) {
 					for (j = 0; j < channels; ++j) {
 						short y = (short) (32767. * Math.min(Math.max(
 								context.out.getValue(j, i), -1.0f), 1.0f));
 						bbufOut[counter++] = (byte) ((y >> 8) & 0xFF);
 						bbufOut[counter++] = (byte) (y & 0xFF);
 					}
 				}
 			} else {
 				for (i = 0, counter = 0; i < bufferSizeInFrames; ++i) {
 					for (j = 0; j < channels; ++j) {
 						short y = (short) (32767. * Math.min(Math.max(
 								context.out.getValue(j, i), -1.0f), 1.0f));
 						bbufOut[counter++] = (byte) (y & 0xFF);
 						bbufOut[counter++] = (byte) ((y >> 8) & 0xFF);
 					}
 				}
 			}
 //			if(sourceDataLine.available() < bbufOut.length) {
 //				System.out.println("Output Blocking");
 //			}
 			sourceDataLine.write(bbufOut, 0, bbufOut.length);
 			
 			////// INPUT ///////
 			
 			if(hasInput) { 
 //				if(targetDataLine.available() < bbufIn.length) {
 //					System.out.println("Input Blocking");
 //				}
 				targetDataLine.read(bbufIn, 0, bbufIn.length);
 			}
 			
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.beadsproject.beads.core.AudioIO#getAudioInput(int[])
 	 */
 	@Override
 	protected synchronized UGen getAudioInput(int[] channels) {
 		if(targetDataLine == null) {
 			if(context.isRunning()) {
 				setupInputJavaSound();
 				targetDataLine.start();
				bbufIn = new byte[context.getBufferSize() * context.getAudioFormat().bitDepth * 8];
 			} else {
 			}
 		}
 		hasInput = true;
 		return new JavaSoundRTInput(context, channels);
 	}
 
 	/**
 	 * JavaSoundRTInput gathers audio from the JavaSound audio input device.
 	 */
 	private class JavaSoundRTInput extends UGen {
 
 		/** The channels to serve. */
 		private int[] channelsToServe;
 
 		/**
 		 * Instantiates a new RTInput.
 		 *
 		 * @param context the AudioContext.
 		 * @param channelsToServe the channels to serve
 		 */
 		JavaSoundRTInput(AudioContext context, int[] channelsToServe) {
 			super(context, channelsToServe.length);
 			this.channelsToServe = channelsToServe;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
 		 */
 		@Override
 		public void calculateBuffer() {
 			int ib;
 			int offset = 0;
 			if (context.getAudioFormat().bigEndian) {
 				for (int i = 0; i < bufferSize; i++) {
 					for (int j = 0; j < channelsToServe.length; j++) {
 						ib = (channelsToServe[j] + offset) * 2;
 						bufOut[j][i] = ((bbufIn[ib] << 8) | (bbufIn[ib + 1] & 0xFF)) / 32768.0F;
 					}
 					offset += context.getAudioFormat().inputs;
 				}
 			} else {
 				for (int i = 0; i < bufferSize; i++) {
 					for (int j = 0; j < channelsToServe.length; j++) {
 						ib = (channelsToServe[j] + offset) * 2;
 						bufOut[j][i] = ((bbufIn[ib] & 0xFF) | (bbufIn[ib + 1] << 8)) / 32768.0F;
 					}
 					offset += context.getAudioFormat().inputs;
 				}
 			}
 
 		}
 
 	}
 
 
 	public static void main(String[] args) {
 		JavaSoundAudioIO jsaio = new JavaSoundAudioIO();
 		AudioContext ac = jsaio.setUpAudioContextFromGUI();
 		if(ac == null) {
 			System.out.println("Failure: AudioContext is NULL!");
 			System.exit(1);
 		}
 		WavePlayer wp = new WavePlayer(ac, 5000, Buffer.SINE);
 		ac.out.addInput(wp);
 		ac.start();
 	}
 
 }
