 package Engine;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 
 import Modules.Container;
 
 /*
  * @author orpheon
  * This class contains the entire audio generating structure, and also handles output and input (although the latter should change someday)
  */
 public class EngineMaster
 {
 	// The line on which sound will be outputted
 	private javax.sound.sampled.SourceDataLine line;
 	
 	// We generate audio in chunks that may not be what the system wants, so we need a buffer between
 	private byte[] sound_buffer = new byte[Engine.Constants.SAMPLE_SIZE * Engine.Constants.SNAPSHOT_SIZE];
 	private int sound_buffer_position = sound_buffer.length;
 	
 	private boolean is_playing;
	private double global_volume = 0.2;
 	
 	private long snapshot_counter = 0;
 	
 //	// DEBUGTOOL:
 //	private static int debug_counter = 1;
 	
 	// FIXME: Should be private but GUI windows need to access it
 	public Container main_container;
     
     /*
 	 * @throws LineUnavailableException
 	 * @throws InterruptedException
 	 */
 
     public EngineMaster() throws LineUnavailableException, InterruptedException
     {
 		//Open up audio output, using 44100hz sampl2ing rate, 16 bit samples, mono, signed and little endian byte ordering
 		AudioFormat format = new AudioFormat(Engine.Constants.SAMPLING_RATE, Engine.Constants.SAMPLE_SIZE*8, 1, true, false);
 		
 		// Start up the line
 		this.line = (SourceDataLine)AudioSystem.getSourceDataLine(format);
 		this.line.open(format);  
 		this.line.start();
 
 		// Create a container to hold everything
 		main_container = new Container();
     }
     
     public void update()
     {
     	// line.available() changes during execution of the method and that breaks stuff
     	int line_available = line.available();
     	
     	// If we still need to play something
     	if (sound_buffer_position < sound_buffer.length)
     	{
     		// If we can just play what's left,
     		if (sound_buffer.length - sound_buffer_position <= line_available)
     		{
     			// do so
     			line.write(sound_buffer, sound_buffer_position, sound_buffer.length - sound_buffer_position);
     			sound_buffer_position = sound_buffer.length;
     		}
     		else
     		{
     			// play all that we can and update the position counter
     			int delta = line_available;
     			line.write(sound_buffer, sound_buffer_position, delta);
     			sound_buffer_position += delta;
     		}
     	}
     	else
     	{
     		// We already played to end the last chunk
     		// Now either don't do anything if we aren't playing or play the next chunk if we are
     		if (is_playing)
     		{
     			while (true)
     			{
 	    			// Run the entire chain of events and generate another chunk of audio
     				line_available = line.available();
     				snapshot_counter++;
 	    			main_container.run(this);
 	    			byte[] tmp;
 	    			int counter=0;
 	    			double sum=0;
 	    			boolean[] channel_active = new boolean[Engine.Constants.NUM_CHANNELS];
 	    			int num_channels_active = 0;
 	    			
 	    			// Check how many channels actually are outputting
 	    			for (int channel=0; channel<Engine.Constants.NUM_CHANNELS; channel++)
 	    			{
 	    				if (main_container.get_output().get_sink().activation_times[channel] >= 0)
 	    				{
 	    					channel_active[channel] = true;
 	    					num_channels_active++;
 	    				}
 	    				else
 	    				{
 	    					channel_active[channel] = false;
 	    				}
 	    			}
 	    			
 //	    			// DEBUGTOOL
 //	    			int c=0;
 //    				byte[] b = new byte[2*Engine.Constants.SNAPSHOT_SIZE];
 //    				for (int j=0; j<Engine.Constants.SNAPSHOT_SIZE; j++)
 //    				{
 //	    				tmp = Functions.convert_to_16bit_bytearray(main_container.get_inner_output_pipe(0).get_pipe(0)[0][j]);
 //	    				System.arraycopy(tmp, 0, b, c, 2);
 //	    				c += 2;
 //    				}
 //    				if (debug_counter <= 10)
 //    				{
 //    					Functions.array_write(b, "Buffer output "+debug_counter++);
 //    				}
 	    			
 	    			// Copy this chunk to the sound buffer
 	    			for (int i=0; i<Engine.Constants.SNAPSHOT_SIZE; i++)
 	    			{
 	    				sum = 0;
 	    				for (int channel=0; channel<Engine.Constants.NUM_CHANNELS; channel++)
 	    				{
 	    					if (channel_active[channel])
 	    					{
 	    						sum += global_volume * main_container.get_output().get_sink().get_pipe(channel)[0][i];
 	    					}
 	    				}
 	    				tmp = Functions.convert_to_16bit_bytearray(sum/num_channels_active);
 	    				System.arraycopy(tmp, 0, sound_buffer, counter, 2);
 	    				counter += 2;
 	    			}
 	    			
 	    			// And then output it (or atleast as much as we can now)
 	    			counter = line_available;
 	    			if (counter <= sound_buffer.length)
 	    			{
 	    				// If we can fill the entire line
 		    			line.write(sound_buffer, 0, counter);
 		    			sound_buffer_position = counter;
 		    			// Stop for now
 		    			break;
 	    			}
 	    			else
 	    			{
 	    				// If we can't, then output all we have and generate more sound (this is inside a while loop)
 	    				line.write(sound_buffer, 0, sound_buffer.length);
 	    			}
     			}
     		}
     	}
     }
     
     public long get_snapshot_counter()
     {
     	return snapshot_counter;
     }
     
     public Module add_module(int type)
     {
     	return main_container.add_module(type);
     }
     
     public Module add_module(int type, int subtype)
     {
     	return main_container.add_module(type, subtype);
     }
     
     public void connect_modules(Module module_1, int out_port, Module module_2, int in_port, int audio_mode)
     {
     	// TODO: Another thing that's going to have to disappear when GUI is here.
     	main_container.connect_modules(module_1, out_port, module_2, in_port, audio_mode);
     }
     
     public void set_frequency(double f1, double f2, double f3)
     {
     	main_container.get_input().add_frequency(this, f1);
     	main_container.get_input().add_frequency(this, f2);
     	main_container.get_input().add_frequency(this, f3);
     }
     
     public void start_playing()
     {
     	is_playing = true;
     }
     
     public void stop_playing()
     {
     	is_playing = false;
     }
 	
     public boolean is_playing()
     {
     	return is_playing;
     }
 
     public double get_globalvolume()
 	{
 		return global_volume;
 	}
 
 	public void set_globalvolume(double global_volume)
 	{
 		this.global_volume = global_volume;
 	}
 
 	public void close()
     {
 		//Done playing the whole waveform, now wait until the queued samples finish playing, then clean up and exit
 		line.drain();
     	line.close();
     	
     	main_container.close();
     }
 }
