 import java.io.*;
 import java.util.*;
 import javax.sound.sampled.*;
 
 public class JRGenerator extends JRNode {
 
 	private int waveform;
 	private AudioInputStream oscillator;
 	private float frequency;
 	
 	public JRGenerator ( int waveform ) {
 		super();
 		
 		// define waveform properties
 		this.waveform = waveform;
 		this.frequency = 550.0F;
 		float	amplitude = 0.7F;
 		long lengthInFrames = AudioSystem.NOT_SPECIFIED;
 		
 		// initialize oscillator
 		this.oscillator = new JROscillator( 
 			waveform, this.frequency, amplitude, this.audioFormat, lengthInFrames);
 	}
 	
 	public void addChild ( JRNode child ) throws JRInvalidEdgeException {
 		
 		// Generators only accept control inputs
 		// So, if the child is not a Controller, we throw a JRInvalidEdgeException
 		JRController controller = null;
 		try { controller = (JRController)child; }
 		catch ( ClassCastException e ) { 
 			throw new JRInvalidEdgeException( "JRGenerator only accepts JRController as children" ); 
 		}
 		
 		super.addChild( child );
 	}
 
 
 	public int available ( ) throws IOException {
 		if ( this.getDegree() == 0 ) { return oscillator.available(); }
 		else { throw new IOException("JRGenerator.available() is unsupported when degree > 0"); }
 	}
 
 	public int getNumAudioOutputs ( ) { return 1; }
 	public int getNumAudioInputs ( ) { return 0; }
 	public int getNumControlOutputs ( ) { return 0; }
 	public int getNumControlInputs ( ) { return Integer.MAX_VALUE; }
 	
 	public boolean isInputSatisfied ( ) {
 		// A generator is immediately input-satisfied upon creation.
 		// i.e. A generator does not need any children in order to output.
 		return true;
 	}
 
 	/*
 	  this method should throw an IOException if the frame size is not 1.
 	  Since we currently always use 16 bit samples, the frame size is
 	  always greater than 1. So we always throw an exception.
 	*/
 	public int read() throws IOException {
 		throw new IOException("JRGenerator.read() is unsupported");
 	}
 	
 	
 	public int read(byte[] abData, int nOffset, int nLength) throws IOException {
 	
 		// Requested length must be multiple of frame size
 		if (nLength % getFormat().getFrameSize() != 0) {
 			throw new IOException("length must be an integer multiple of frame size");
 		}
 		
 		// If there are no controller children
 		if ( this.getDegree() == 0 ) { 
 			return oscillator.read(abData, nOffset, nLength); 
 		}
 		
 		// One controller
 		else if ( this.getDegree() == 1 ) {
 
 			// read from oscillator
 			byte[] b1 = new byte[nLength];
 			int nRead = oscillator.read(b1, nOffset, nLength);
 			//System.out.println( "Read " + nRead + " bytes from oscillator" );
 			
 			// read from controller
 			byte[] b2 = new byte[nRead];
 			JRController c = (JRController)this.getFirstChild();
 			int nReadCtrl = c.read(b2, nOffset, nRead); // nRead? nLength?
 
 			// assertion: equal signal read length
 			if (nRead != nReadCtrl) { 
 				throw new IOException ( "Assertion failed: Controller signal read (" + nReadCtrl + ") and generator signal read (" + nRead + ") were not the same length" ); 
 			}
 			
 			// assertion: frame size is four bytes
 			if (nRead % 4 != 0) {
 				throw new IOException ( "Assertion failed: invalid number of bytes read (" + nRead + ")" ); 
 			}
 			
 			// itterate over signal one frame at a time
 			for (int i = 0; i < nRead; i = i + 4) {
 
 				/* Assume that data comes in 16 bit stereo, big endian.
 				Assume both channels are the same, so we only process one channel. 
 				Thus, bytes three and four are discarded */
 				int generatorSample = (b1[i] << 8) | (b1[i+1] & 0xFF);
 				int controllerSample = (b2[i] << 8) | (b2[i+1] & 0xFF);
 				
 				// normalize the controller sample to a range from -1.0 to 1.0
 				float controllerSampleNormalized = controllerSample / 32768.0F;
 				
 				// calculate result
 				int resultSample = Math.round(generatorSample * controllerSampleNormalized);
 				//System.out.println("g = " + generatorSample + " c = " + controllerSample + " cn = " + controllerSampleNormalized + " r = " + resultSample);
 				
 				// assign the result to the left channel of this frame 
 				// in the provided big-endian result buffer
 				abData[i+0] = (byte) ((resultSample >>> 8) & 0xFF);
 				abData[i+1] = (byte) (resultSample & 0xFF);
 			
 				// assume the right channel is the same as the left
 				abData[i+2] = abData[i];
 				abData[i+3] = abData[i+1];
 				
 				}
 				
 			//System.out.println("Done mixing controller signal and generator signal");
 			return nRead;
 		}
 		
 		else { 
 			//throw new IOException("JRGenerator.read(byte[], int, int) is unsupported when degree > 1"); 
 			
 			// read from oscillator
 			byte[] oscillatorData = new byte[nLength];
 			int nRead = oscillator.read(oscillatorData, nOffset, nLength);
 			
 			/* Read from controllers.  This could be a very big buffer.
 			size in bytes = nRead * (JRNode.maximumDegree + 1)
 			given a nRead of 64kb the max size is 437kb
 			I guess that is a reasonable size.  It could be cut in half again by 
 			reducing the controller signal to one channel */
 			byte[][] controllerData = new byte[this.getDegree()][nRead];
 			Iterator childIterator = this.getChildIterator();
 			int ci = 0;
 			while ( childIterator.hasNext() ) {
 				JRController c = (JRController) childIterator.next();			
 				int nReadCtrl = c.read(controllerData[ci], nOffset, nRead);
 				ci++;
 				if ( nReadCtrl != nRead ) { throw new IOException("read length mismatch"); }
 			}
 			
 			/* Itterate over oscillator data, multiplying each oscillator sample
 			by each corresponding control sample.  Even if we use single channel 
 			control signals, this step is computationally expensive, 
 			requiring (degree * (nRead / 2)) multiplications.  With a maximum 
 			degree of 6, and a nRead of 64000, that's 192000 multiplications! */
 			for (int i = 0; i < nRead; i = i + 4) {
 				int resultSample = 0;
 			
 				/* Assume that data comes in 16 bit stereo, big endian.
 				Assume both channels are the same, so we only process one channel. 
 				Thus, bytes three and four are discarded */
 				int generatorSample = (oscillatorData[i] << 8) | (oscillatorData[i+1] & 0xFF);
 				resultSample = generatorSample;
 				
 				// For each controller
 				for (ci = 0; ci < this.getDegree(); ci ++) {
 					int controllerSample = (controllerData[ci][i] << 8) | (controllerData[ci][i+1] & 0xFF);
 				
 					// normalize the controller sample to a range from -1.0 to 1.0
 					float controllerSampleNormalized = controllerSample / 32768.0F;
 				
 					resultSample *= controllerSampleNormalized;
 				}
 			
 				// Assign the result to the left channel of 
 				// this frame in the provided big-endian result buffer
 				abData[i+0] = (byte) ((resultSample >>> 8) & 0xFF);
 				abData[i+1] = (byte) (resultSample & 0xFF);
 			
 				// assume the right channel is the same as the left
 				abData[i+2] = abData[i];
 				abData[i+3] = abData[i+1];
 			}
 			
 			return nRead;
 		}
 	}
 	
 
 	public String toString ( ) {
 		String r;
 		switch ( this.waveform ) {
 			case JROscillator.WAVEFORM_SINE : r = "Sinewave Generator"; break;
 			case JROscillator.WAVEFORM_SQUARE : r = "Squarewave Generator"; break;
 			case JROscillator.WAVEFORM_TRIANGLE : r = "Triangle Generator"; break;
 			case JROscillator.WAVEFORM_SAWTOOTH : r = "Sawtooth Generator"; break;
 			default : r = "Generator";
 		}
 		return r;
 	}
 
 	// setAngle() returns true if it chages something,
 	// resulting in a stale buffer
 	public boolean setAngle ( float a ) { 
 		// new frequency in range from 500-800 Hz
 		float newFrequency = 500.0F + (300.0F * a);
 		return this.setFrequency( newFrequency );		
 	}
 
 	// setFrequency() returns true if it chages something,
 	// resulting in a stale buffer	
 	public boolean setFrequency ( float newFrequency ) {
 	
 		boolean bufferIsNowStale = false;
 	
 		/* TODO: There is a thread safety problem here. I want to replace
 		the oscillator, but what if the syth is reading from it at the
 		same time.  This is bound to happen sooner or later */
 		
 		// Frequency change must be big enough to be worth the effort
		if ( Math.abs( newFrequency - this.frequency ) > 0.1 ) {
 			
 			// unchanged waveform properties
 			float	amplitude = 0.7F;
 			long lengthInFrames = AudioSystem.NOT_SPECIFIED;
 	
 			// initialize oscillator
 			this.oscillator = new JROscillator( 
 				this.waveform, newFrequency, amplitude, this.audioFormat, lengthInFrames);
 			
 			// update freq
 			this.frequency = newFrequency;
 			bufferIsNowStale = true;
 		}
 		
 		return bufferIsNowStale;
 	}
 
 }
