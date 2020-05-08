 /** This class defines a general analog oscillator.
  *  Should be inherited from by specific oscillators.
  *  
  *  All (void) methods should return 0 if execution succeeded, a positive error code if not
  */
 
 /**
  * @author orpheon
  *
  */
 // TODO: Make this abstract
 public class Oscillator
 {
 	private int frequency = 1;
 	private double phase_offset = 0.0;
 
 	// 6.28318530718 = 2*pi
 	private double period = 6.28318530718;
 	private double current_position = 0.0;
 	
 	private int samplerate = 1;
 	private double samplelength = 1;
 	
 	public Oscillator(int frequency, double phase_offset, int samplerate)
 	{
 		this.set_frequency(frequency);
 		this.set_period(Functions.get_period(frequency));
 		this.current_position = 0.0;
 		this.set_phase(phase_offset);
 		this.set_samplerate(samplerate);
 	}
 	
 	public byte[] get_sound(int num_samples)
 	{
 		// 16 bytes per sample, so "length" samples will mean 16*length bytes
 		int output_length = num_samples*16;
 		double sample;
 
 		byte[] output;
 		output = new byte[output_length];
 
		for (int i=0; i<output_length; i+=16)
 		{
 			sample = this.get_value(this.current_position);
 			this.current_position += this.samplelength;
 //			while (this.current_position > this.period)
 //			{
 //				this.current_position -= this.period;
 //			}
 			// Then convert "sample" into a byte array and copy that to the output buffer
 			// FIXME: This is pretty inefficient, a casting for every number, is it possible to convert the whole array at once?
 			System.arraycopy(Functions.convert_to_bytearray(sample), 0, output, i, 2);
 		}
 
 		return output;
 	}
 
 	public int get_frequency()
 	{
 		return frequency;
 	}
 
 	public void set_frequency(int frequency)
 	{
 		this.frequency = frequency;
 		this.set_period(Functions.get_period(frequency));
 	}
 
 	public double get_phase()
 	{
 		return phase_offset;
 	}
 
 	public void set_phase(double phase_offset)
 	{
 		// Move the current_pos by the same offset
 		this.current_position += (phase_offset - this.phase_offset);
 		while (this.current_position >= this.period)
 		{
 			this.current_position -= this.period;
 		}
 
 		this.phase_offset = phase_offset;
 	}
 
 	public int get_samplerate()
 	{
 		return samplerate;
 	}
 
 	public void set_samplerate(int samplerate)
 	{
 		this.samplerate = samplerate;
 		this.samplelength = 1.0 / this.samplerate;
 	}
 
 	public double get_period()
 	{
 		return period;
 	}
 
 	public void set_period(double period)
 	{
 		this.period = period;
 		while (this.current_position >= this.period)
 		{
 			this.current_position -= this.period;
 		}
 	}
 	
 	private double get_value(double position)
 	{
 		// !!! OVERWRITE THIS !!!
 
 		// This method returns the actual sound value at a certain position
 		// Requires a position in time
 		
 		// DEBUGTOOL:
 		return Math.sin(this.frequency * position);
 	}
 }
