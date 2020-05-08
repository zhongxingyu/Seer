 package Engine;
 
 import Modules.Container;
 
 public class InputModule extends Module
 {
 	private static final int FREQUENCY_SOURCE = 0;
 	private double[] frequencies;
	private long[] activation_times;
 	
 	public InputModule(Container container)
 	{
 		super(container);
 		
 		NUM_INPUT_PIPES = 0;
 		NUM_OUTPUT_PIPES = 1;
 		
 		input_pipes = new Pipe[NUM_INPUT_PIPES];
 		output_pipes = new Pipe[NUM_OUTPUT_PIPES];
 		
 		input_pipe_names = new String[NUM_INPUT_PIPES];
 		output_pipe_names = new String[NUM_OUTPUT_PIPES];
 		output_pipe_names[0] = "Frequency input";
 		
 		module_type = Engine.Constants.MODULE_INPUT;
 		
 		MODULE_NAME = "Input";
 		
 		frequencies = new double[Constants.NUM_CHANNELS];
		activation_times = new long[Constants.NUM_CHANNELS];
		// Deactivate the activation times, because 0 is a legal number (-1 isn't)
		for (int i=0; i<activation_times.length; i++)
		{
			activation_times[i] = -1;
		}
 	}
 	
 	@Override
 	public void run(EngineMaster engine)
 	{
 		for (int i=0; i<Constants.NUM_CHANNELS; i++)
 		{
 			if (output_pipes[FREQUENCY_SOURCE] != null)
 			{
				if (activation_times[i] >= 0)
 				{
					output_pipes[FREQUENCY_SOURCE].activation_times[i] = activation_times[i];
 					if (audio_mode == Constants.MONO)
 					{
 						for (int j=0; j<Constants.SNAPSHOT_SIZE; j++)
 						{
 							output_pipes[FREQUENCY_SOURCE].inner_buffers[i][0][j] = frequencies[i];
 						}
 					}
 					else
 					{
 						for (int j=0; j<Constants.SNAPSHOT_SIZE; j++)
 						{
 							output_pipes[FREQUENCY_SOURCE].inner_buffers[i][0][j] = frequencies[i];
 							output_pipes[FREQUENCY_SOURCE].inner_buffers[i][1][j] = frequencies[i];
 						}
 					}	
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void run(EngineMaster engine, int channel)
 	{
 		// No need for this
 		// Yet I must implement it because yay java
 	}
 	
 	public void add_frequency(EngineMaster engine, double freq)
 	{
 		for (int i=0; i<Constants.NUM_CHANNELS; i++)
 		{
			if (activation_times[i] < 0)
 			{
				frequencies[i] = freq;
				activation_times[i] = engine.get_snapshot_counter();
				break;
 			}
 		}
 	}
 }
