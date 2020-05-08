 package Modules;
 
 import Engine.Constants;
 import Engine.Module;
 import Engine.Pipe;
 
 /**
  * @author orpheon
  *
  */
 public class Oscillator extends Module
 {
 	public static final int FREQUENCY_INPUT = 0;
 	public static final int PHASE_INPUT = 1;
 	
 	public static final int SIGNAL_OUTPUT = 0;
 	
 	public static final int SINE_WAVE = 0;
 	public static final int SAW_WAVE = 1;
 	public static final int SQUARE_WAVE = 2;
 	
 	private int osc_type = SAW_WAVE;
 
 	public Oscillator()
 	{		
 		super();
 		
 		NUM_INPUT_PIPES = 2;
 		NUM_OUTPUT_PIPES = 1;
 		
 		input_pipe_names = new String[NUM_INPUT_PIPES];
 		output_pipe_names = new String[NUM_OUTPUT_PIPES];
 		input_pipe_names[FREQUENCY_INPUT] = "Frequency input";
 		input_pipe_names[PHASE_INPUT] = "Phase input";
 		output_pipe_names[SIGNAL_OUTPUT] = "Sound output";
 		
 		input_pipes = new Pipe[NUM_INPUT_PIPES];
 		output_pipes = new Pipe[NUM_OUTPUT_PIPES];
 		
 		activation_source = FREQUENCY_INPUT;
 		module_type = Engine.Constants.MODULE_OSCILLATOR;
 		MODULE_NAME = "Oscillator";
 	}
 
 	public void run(Engine.EngineMaster engine, int channel)
 	{
 		// First check whether all pipes are connected
 		boolean everything_connected = true;
 		for (int i=0; i<NUM_INPUT_PIPES; i++)
 		{
 			if (input_pipes[i] == null)
 			{
 				everything_connected = false;
 			}
 		}
 		for (int i=0; i<NUM_OUTPUT_PIPES; i++)
 		{
 			if (output_pipes[i] == null)
 			{
 				everything_connected = false;
 			}
 		}
 		if (everything_connected)
 		{
 			// Activation times are taken from frequency pipe
 			if (input_pipes[FREQUENCY_INPUT].activation_times[channel] >= 0)
 			{
 				for (int side=0; side<audio_mode; side++)
 				{
 					double time, value;
 					for (int i=0; i<Engine.Constants.SNAPSHOT_SIZE; i++)
 					{
 						// Calculate the precise time we wish to sample
 						time = (((double)(engine.get_snapshot_counter() - input_pipes[FREQUENCY_INPUT].activation_times[channel]) * Engine.Constants.SNAPSHOT_SIZE) + i) /
 								(double)Engine.Constants.SAMPLING_RATE;
						time %= 1/input_pipes[FREQUENCY_INPUT].get_pipe(channel)[side][i];
 						value = get_value(time, input_pipes[FREQUENCY_INPUT].get_pipe(channel)[side][i], input_pipes[PHASE_INPUT].get_pipe(channel)[side][i]);
 						if (Math.abs(value) > 1)
 						{
 							System.out.println("ALERT! VALUE IS OVER 1 AT "+value+"; time="+time+"; Oscillator object.");
 						}
 						output_pipes[SIGNAL_OUTPUT].get_pipe(channel)[side][i] = value;
 					}
 				}
 			}
 		}
 	}
 	
 	protected double get_value(double time, double freq, double phase)
 	{
 		// Sampling at a certain position
 		// 0 <= time <= 1
 		// TODO: Make anti-aliased saws and squares
 		switch (osc_type)
 		{
 			case SINE_WAVE:
 				return Math.sin(time*freq*Constants.pi_times_2 + phase);
 			
 			case SAW_WAVE:
 				double result = 0;
 				// Sawtooth = infinite sum of odd harmonics with A=1/n for nth harmonic
 				// Doing like this for bandlimiting
 				// Source: http://en.wikipedia.org/wiki/Sawtooth_wave
 				for (int k=1; k*freq<Constants.SAMPLING_RATE/2; k++)
 				{
 					result += Math.sin(time*freq*Constants.pi_times_2*k + phase)/k;
 				}
 				// This might destroy the nice bandlimiting, but I want my values -1 <= x <= 1, not -1.0903783642160645
 				return Math.min(1, Math.max(-1, 2*result/Math.PI));
 				
 			case SQUARE_WAVE:
 				if (time < 0.5)
 				{
 					return -1.0;
 				}
 				else
 				{
 					return 1.0;
 				}
 				
 			default:
 				System.out.println("ERROR: Oscillator "+index+" has the invalid osc_type "+osc_type+"!");
 				return 0;
 		}
 	}
 	
 	public void set_osctype(int type)
 	{
 		osc_type = type;
 	}
 	
 	public int get_osctype()
 	{
 		return osc_type;
 	}
 }
