 package Modules;
 
 import Engine.EngineMaster;
 import Engine.Module;
 import Engine.Pipe;
 
 public class Constant extends Module
 {
 	public double value;
 	public static final int OUTPUT_PIPE = 0;
 	
 	public Constant()
 	{
 		super();
 		
 		NUM_INPUT_PIPES = 0;
 		NUM_OUTPUT_PIPES = 1;
 		
 		value = 0;
 		
 		input_pipes = new Pipe[NUM_INPUT_PIPES];
 		output_pipes = new Pipe[NUM_OUTPUT_PIPES];
 		
 		input_pipe_names = new String[NUM_INPUT_PIPES];
 		output_pipe_names = new String[NUM_OUTPUT_PIPES];
 		output_pipe_names[OUTPUT_PIPE] = "Constant output";
 		
 		module_type = Engine.Constants.MODULE_CONSTANT;
 		
 		MODULE_NAME = "Constant";
 	}
 	
 	@Override
 	public void run(EngineMaster engine)
 	{
 		// Big hack, but in the case of constants I think it's actually a good idea
		if (engine.is_playing())
 		{
 			for (int channel=0; channel<Engine.Constants.NUM_CHANNELS; channel++)
 			{
 				run(engine, channel);
 				output_pipes[OUTPUT_PIPE].activation_times[channel] = 0;
 			}
 			
 		}
 	}
 	
 	@Override
 	public void run(EngineMaster engine, int channel)
 	{
 		// Big hack, but in the case of constants I think it's actually a good idea
 		if (engine.is_playing() && output_pipes[OUTPUT_PIPE] != null)
 		{
 			for (int i=0; i<Engine.Constants.SNAPSHOT_SIZE; i++)
 			{
 				for (int side=0; side<audio_mode; side++)
 				{
 					output_pipes[OUTPUT_PIPE].get_pipe(channel)[side][i] = value;
 				}
 			}
 		}
 	}
 
 	public void set_value(Double value)
 	{
 		this.value = value.doubleValue();
 	}
 
 }
