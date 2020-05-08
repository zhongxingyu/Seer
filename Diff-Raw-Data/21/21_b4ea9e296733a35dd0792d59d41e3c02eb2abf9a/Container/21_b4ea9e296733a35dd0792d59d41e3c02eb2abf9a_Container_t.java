 package Modules;
 
 import java.util.LinkedList;
 
 import Engine.Constants;
 import Engine.Module;
 import Engine.MonoPipe;
 import Engine.Pipe;
 import Engine.StereoPipe;
 import Distortion.TanhDistortion;
 
 /*
  * This class serves as a general container for modules.
  * It receives input and output through a pair of specialized modules, but I haven't implemented that modularity yet.
  * Right now those input-output modules are directly hijacked by the engine master.
  */
 
 public class Container extends Module
 {
 	// One needs to keep track of what modules are alive
 	private LinkedList<Engine.Module> module_list;
 	// Special modules
 	Engine.InputModule input_source;
 	Engine.OutputModule output_sink;
 	
 	public Container()
 	{
 		super();
 		initialize();
 	}
 	
 	public Container(Container container)
 	{
 		super(container);
 		initialize();
 	}
 	
 	private void initialize()
 	{
 		NUM_INPUT_PIPES = 0;
 		NUM_OUTPUT_PIPES = 0;
 		// Pipe naming
 		input_pipe_names = new String[NUM_INPUT_PIPES];
 		output_pipe_names = new String[NUM_OUTPUT_PIPES];
 		// Special modules
 		input_source = new Engine.InputModule(this);
 		output_sink = new Engine.OutputModule(this);
 		
 		module_type = Constants.MODULE_CONTAINER;
 		
 		module_list = new LinkedList<Engine.Module>();
 		
 		MODULE_NAME = "Container";
 	}
 	
 	public void run(Engine.EngineMaster engine)
 	{
 		// Containers work slightly differently than all other modules, and hence need their own run() function
 		
		// First run the input
		input_source.run(engine);
		
 		// Execute all modules inside the container
 		// TODO: Use iterators here
 		for (int i=0; i<module_list.size(); i++)
 		{
 			module_list.get(i).run(engine);
 		}
		
		// Finally, run the output
		output_sink.run(engine);
 	}
 	
 	public Module add_module(int type)
 	{
 		Module m;
     	switch (type)
     	{
     		case Constants.MODULE_OSCILLATOR:
     			m = new Modules.Oscillator(this);
     			break;
     			
     		case Constants.MODULE_MERGER:
     			m = new Modules.Merger(this);
     			break;
     			
     		case Constants.MODULE_COPYER:
     			m = new Modules.Copyer(this);
     			break;
     			
     		case Constants.MODULE_CONTAINER:
     			m = new Modules.Container(this);
     			break;
     		
     		case Constants.MODULE_CONSTANT:
     			m = new Modules.Constant(this);
     			break;
     			
     		default:
     			System.out.println("ERROR: Invalid module type requested: "+type);
     			m = null;
     	}
     	// Goddamn java and it's obsession with the possibility that some code might never be executed
     	if (m != null)
     	{
     		module_list.add(m);
     	}
     	return m;
 	}
 	
 	public Module add_module(int type, int subtype)
 	{
 		Module m;
     	switch (type)
     	{
     		case Constants.MODULE_DISTORTION:
     			switch (subtype)
     			{
     				case Constants.DISTORTION_TANH:
     					m = new TanhDistortion(this);
     					break;
     					
     				default:
     					System.out.println("ERROR: Invalid module subtype requested: "+type+"; "+subtype);
     	    			m = null;
     			}
     			break;
     			
     		default:
     			System.out.println("ERROR: Invalid module type requested: "+type+"; "+subtype);
     			m = null;
     	}
     	// Goddamn java and it's obsession with the possibility that some code might never be executed
     	if (m != null)
     	{
     		module_list.add(m);
     	}
     	return m;
 	}
 	
 	public void remove_module(Module m)
 	{
 		module_list.remove(m);
 	}
 	
 	public Pipe create_pipe(int audio_type)
 	{
 		if (audio_type == Engine.Constants.STEREO)
 		{
 			return new StereoPipe();
 		}
 		else
 		{
 			return new MonoPipe();
 		}
 	}
 	
 	public void connect_modules(Module module_1, int out_port, Module module_2, int in_port, int audio_mode)
     {
 		Pipe pipe = create_pipe(audio_mode);
 		// Remember that the pipe must be connected to the output of the first module and go in the second.
     	module_1.connect_output(pipe, out_port);
     	module_2.connect_input(pipe, in_port);
     }
 	
 	public int get_num_inputs()
 	{
 		return NUM_INPUT_PIPES;
 	}
 	
 	public int get_num_outputs()
 	{
 		return NUM_OUTPUT_PIPES;
 	}
 	
 	public Engine.InputModule get_input()
 	{
 		return input_source;
 	}
 	
 	public Engine.OutputModule get_output()
 	{
 		return output_sink;
 	}
 	
 	public void close()
 	{
     	// Destroy and disconnect all the modules (who will take care of the pipes themselves)
     	Module m;
     	while (module_list.size() > 0)
     	{
     		m = module_list.getFirst();
     		// Modules disconnect themselves from EngineMaster.module_list automatically
     		m.close(this);
     	}
 	}
 
 	@Override
 	public void run(Engine.EngineMaster engine, int channel)
 	{
 		// TODO Auto-generated method stub
 		// TODO: Find out how to get rid of this. Since Modules is abstract and declares this function, I need to implement it
 		System.out.println("ERROR: run(int channel) method executed on container; this should never happen.");
 	}
 }
