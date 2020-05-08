 package net.bluecow.spectro.clipAndFrame;
 
 import java.awt.Rectangle;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.TargetDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.undo.UndoableEditSupport;
 
 import javazoom.spi.mpeg.sampled.convert.DecodedMpegAudioInputStream;
 
 import ddf.minim.effects.BandPass;
 import ddf.minim.effects.IIRFilter;
 
 import net.bluecow.spectro.detection.BeatDetector;
 import net.bluecow.spectro.inputReader.InputDecoder;
 import net.bluecow.spectro.inputReader.MP3Decoder;
 import net.bluecow.spectro.inputReader.WavDecoder;
 import net.bluecow.spectro.math.AudioFileUtils;
 import net.bluecow.spectro.math.OverlapBuffer;
 import net.bluecow.spectro.painting.ClipDataChangeEvent;
 import net.bluecow.spectro.painting.ClipDataChangeListener;
 import net.bluecow.spectro.painting.ClipDataEdit;
 import net.bluecow.spectro.windowFunctions.NullWindowFunction;
 import net.bluecow.spectro.windowFunctions.VorbisWindowFunction;
 import net.bluecow.spectro.windowFunctions.WindowFunction;
 
 public class Clip
 {
 	//private static final Logger logger = Logger.getLogger(Clip.class.getName());
 
 	public static double volumeControl = 65535;
 
 	private static final AudioFormat AUDIO_FORMAT = new AudioFormat(44100.0F, 16, 1, true, true);
 
 	private ArrayList<ArrayList< Frame >> frames;
 
 	private double filteredPartials[][];
 
 	private double spectralScale = 10000.0D;
 	private ClipDataEdit currentEdit;
 	private final UndoableEditSupport undoEventSupport = new UndoableEditSupport();
 
 	private final List<ClipDataChangeListener> clipDataChangeListeners = new ArrayList<ClipDataChangeListener> ();
 	private IIRFilter filters[];
 
 	//private WindowFunction preWindowFunction;
 	//private WindowFunction postWindowFunction;
 	private boolean finishedReading = false;
 	private InputDecoder input;
 
 	//uses an array to add in the intensity
 
 	public BeatDetector[] detectors = new BeatDetector[4];//one for the total then the three parts
 
 
 	public Clip(File file) throws UnsupportedAudioFileException, IOException
 	{
 		input = InputDecoder.createInputDecoder( spectralScale, file );
 		filters = new IIRFilter[11];
 		frames = new ArrayList< ArrayList< Frame > >();
 		for( int i = 0; i < 11; i++ )
 		{
 			frames.add( new ArrayList< Frame >() );
 		}
 
 		filters[0] = new BandPass(16400.0F, 3600.0f, input.getSampleRate() );
 		filters[1] = new BandPass(9600.0F, 3200.0f, input.getSampleRate() );
 		filters[2] = new BandPass(4800.0F, 1600.0f, input.getSampleRate() );
 		filters[3] = new BandPass(2400.0F, 800.0f, input.getSampleRate() );
 		filters[4] = new BandPass(1200.0F, 400.0f, input.getSampleRate() );
 		filters[5] = new BandPass(600.0F, 200.0f, input.getSampleRate() );
 		filters[6] = new BandPass(300.0f, 100.0f, input.getSampleRate() );
 		filters[7] = new BandPass(150.0f, 50.0f, input.getSampleRate() );
 		filters[8] = new BandPass(75.0f, 25.0f, input.getSampleRate() );
 		filters[9] = new BandPass(37.5f, 12.5f, input.getSampleRate() );
 		filters[10] = new BandPass(18.75f, 6.25f, input.getSampleRate() );
 
 		detectors[0] = new BeatDetector(null);
 		detectors[1] = new BeatDetector(new BandPass(150.0f, 6.25f, input.getSampleRate() ));//lower register
 		detectors[2] = new BeatDetector(new BandPass(800.0f, 200f, input.getSampleRate() ));//middle register
 		detectors[3] = new BeatDetector(new BandPass(16400.0f,900f, input.getSampleRate() ));//high register
 		//preWindowFunction = new VorbisWindowFunction(input.frameSize);
 		//postWindowFunction = new NullWindowFunction();
 
 		Frame.SetUp( input.getSampleRate(), input.frameSize );
 
 		int k = 0;
 		while( !finishedReading )
 		{
 			readAndFilter();
 			//System.out.println( "done"+k );
 			if( k % 100 == 0 )
 			{
 				System.out.println( "StillGoing"+k );
 			}
 			k++;
 		}
 		System.out.println( "done"+k );
 		postFilter();
 		System.out.println( "done post filter" );
 
 	}
 
 	public void readAndFilter() throws IOException
 	{
 		filteredPartials = new double[11][input.frameSize];
 		float[] partArray = input.readSeparately();
 
 		if( partArray != null )
 		{
 			calculateVE(partArray);
 
 			prefilter(partArray);
 			for( int i = 0; i < 11; i++ )
 			{
 				frames.get(i).add( new Frame( filteredPartials[i], i ) );
 			}
 		}
 		else
 		{
 			finishedReading = true;
 		}
 		filteredPartials = null;
 	}
 
 	/**
 	 * Attempts to create an intensity array based off of previous parts of the song
 	 * @param partArray
 	 */
 	public void creatIntensityWindow(float[] partArray)
 	{
 	}
 
 	public void postFilter()
 	{
 		for(int k = 0; k<frames.size();k++)
 		{
 
 		}
 	}
 
 
 	/**
 	 * runs the entire array through a filter to filter out certain sounds
 	 * @param in
 	 * @return
 	 */
 	private void prefilter( float[] a )
 	{
 		float temp[] = new float[ input.frameSize ];
 		for( int i = 0; i < 11; i++ )
 		{
 			for( int j = 0; j < input.frameSize; j ++ )
 			{
 				temp[j] = a[j];
 			}
 
 			//filters[i].process( temp );
 
 			for( int j = 0; j < input.frameSize; j++ )
 			{
 				filteredPartials[i][j] = (double)temp[j];
 			}
 		}
 	}
 
 	  public int getFrameTimeSamples()
 	  {
 	    return this.input.frameSize;
 	  }
 
 	  public int getFrameFreqSamples()
 	  {
 	    return this.input.frameSize;
 	  }
 
 	  public int getFrameCount()
 	  {
 	    return this.frames.get(0).size();
 	  }
 
 	  public ArrayList<Frame> getFrame(int i)
 	  {
 		  return this.frames.get(i);
 	  }
 
 	  public AudioInputStream getAudio()
 	  {
 	    return getAudio(0);
 	  }
 
 	  public AudioInputStream getAudio(int sample)
 	  {
 	    final int initialFrame = sample / getFrameTimeSamples();
 
 	    InputStream audioData = new InputStream()
 	    {
 	      int nextFrame = initialFrame;
 
 	      int currentSample;
 
 	      double currentTimeData[];
 
 	      boolean currentByteHigh = true;
 
 	      public int available() throws IOException
 	      {
 	        return 2147483647;
 	      }
 
 	      public int read() throws IOException
 	      {
 	    	  if( nextFrame >= getFrameCount() )
 	    		  return -1;
 	    	  if( currentTimeData == null )
 	    	  {
 	    		  currentTimeData = new double[ input.frameSize ];
 	    	  }
 	    	  if( currentSample == 0 )
 	    	  {
 	    		  for( int i = 0; i < currentTimeData.length; i++ )
 	    		  {
 	    			  currentTimeData[i] = 0.0;
 	    		  }
 	    		  for( int i = 0; i < 11; i++ )
 	    		  {
 	    			  ArrayList<Frame> frames = getFrame( i );
 
 	    			  Frame f = frames.get( nextFrame );
 
 	    			  double temp[] = f.asTimeData();
 
 	    			  for( int j = 0; j < currentTimeData.length; j++ )
 		    		  {
 		    			  currentTimeData[j] += temp[j];
 		    		  }
 	    			  /*if( nextFrame % 10 == 0 )
 	    			  {
 	    				  for( int j = 0; j < currentTimeData.length; j += currentTimeData.length/8 )
 			    		  {
 			    			  System.out.print( temp[j] + " , ");
 			    		  }
 	    				  System.out.println( );
 	    				  System.out.println( );
 	    			  }*/
 	    		  }
 	    	  }
 
 	    	  int t = (int)( volumeControl*currentTimeData[currentSample] );
 	    	  //System.out.println( currentSample );
 	    	  //System.out.println( nextFrame );
 	    	 //System.out.println( t );
 	    	  if (currentByteHigh)
 	    	  {
 	    		  //t *= spectralScale;
 	    		  currentByteHigh = false;
 
 	    		  return t >> 8 & 0xFF;
 	    	  }
 	    	  currentByteHigh = true;
 	    	  currentSample++;
 	    	  if( currentSample == input.frameSize )
 	    	  {
 	    		  nextFrame++;
 	    		  currentSample = 0;
 	    	  }
 	    	  return t & 0xFF;
 	      }
 	    };
 	    int length = getFrameCount() * getFrameTimeSamples() * (AUDIO_FORMAT.getSampleSizeInBits() / 8);
 	    return new AudioInputStream(audioData, AUDIO_FORMAT, length);
 	  }
 
 	  public void beginEdit(Rectangle region, String description)
 	  {
 	    if (this.currentEdit != null) {
 	      throw new IllegalStateException("Already in an edit: " + this.currentEdit);
 	    }
 	    this.currentEdit = new ClipDataEdit(this, region.x, region.y, region.width, region.height);
 	  }
 
 	  public void endEdit()
 	  {
 	    if (this.currentEdit == null) {
 	      throw new IllegalStateException("No edit is in progress");
 	    }
 	    this.currentEdit.captureNewData();
 	    this.undoEventSupport.postEdit(this.currentEdit);
 	    regionChanged(this.currentEdit.getRegion());
 	    this.currentEdit = null;
 	  }
 
 	  public void beginCompoundEdit(String presentationName)
 	  {
 	    this.undoEventSupport.beginUpdate();
 	  }
 
 	  public void endCompoundEdit()
 	  {
 	    this.undoEventSupport.endUpdate();
 	  }
 
 	  public void regionChanged(Rectangle region)
 	  {
 	    fireClipDataChangeEvent(region);
 	  }
 
 	  public void addClipDataChangeListener(ClipDataChangeListener l)
 	  {
 	    this.clipDataChangeListeners.add(l);
 	  }
 
 	  public void removeClipDataChangeListener(ClipDataChangeListener l) {
 	    this.clipDataChangeListeners.remove(l);
 	  }
 
 	  private void fireClipDataChangeEvent(Rectangle region) {
 	    ClipDataChangeEvent e = new ClipDataChangeEvent(this, region);
 	    for (int i = this.clipDataChangeListeners.size() - 1; i >= 0; i--)
 	      ((ClipDataChangeListener)this.clipDataChangeListeners.get(i)).clipDataChanged(e);
 	  }
 
 	  public void addUndoableEditListener(UndoableEditListener l)
 	  {
 	    this.undoEventSupport.addUndoableEditListener(l);
 	  }
 
 	  public UndoableEditListener[] getUndoableEditListeners() {
 	    return this.undoEventSupport.getUndoableEditListeners();
 	  }
 
 	  public void removeUndoableEditListener(UndoableEditListener l) {
 	    this.undoEventSupport.removeUndoableEditListener(l);
 	  }
 
 	  public double getSamplingRate() {
 	    return AUDIO_FORMAT.getSampleRate();
 	  }
 
 
 	  /**
 	   	 * David methods below
 	   	 * in array [0] it is the volume
 	   	 * in array [1] it is the energy
 	   	 * the length of the array is the size of the given array/1320
 	   	 */
 	   	public void calculateVE(float[] timeData)
 	   	{
 	   		int timeLength = timeData.length;
	   	//	detectors[0].calculateVE(Arrays.copyOf(timeData,timeLength));
 	   		for(int k=0;k<detectors.length;k++)
 	   		{
 	   			detectors[k].calculateVE(Arrays.copyOf(timeData,timeLength));
 	   		}
 	   	}
 
 	}
