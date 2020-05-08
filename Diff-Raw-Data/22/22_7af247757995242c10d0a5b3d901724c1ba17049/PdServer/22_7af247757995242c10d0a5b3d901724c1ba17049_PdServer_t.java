 package ensemble.apps.pd_testing;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import ensemble.*;
 import ensemble.apps.pd_testing.PdWorld;
 import javax.sound.sampled.*;
 
 import org.puredata.core.PdBase;
 
 public class PdServer extends EventServer 
 {
 	PdWorld world;
 	protected String agent_name;
 	protected String agent_component_name;
 	
 	protected ArrayList < PdEvent > events;
 	private HashMap < String, PdAudioBlockStream > memory_readers;
   
     private PdProcessor pd_processor;
     private PdReceiver pd_receiver;
     
     private SourceDataLine line;
 	
 	@Override
 	public boolean configure ( )
 	{
 		setEventType ( PdConstants.DEFAULT_EVENT_TYPE );
 		return true;
 	}
 	@Override
 	public boolean init ( ) 
 	{  
 	    pd_processor = PdProcessor.get_instance ( );
 	    pd_receiver = PdReceiver.get_instance ( );
 	    
 	    events = new ArrayList< PdEvent > ( );
 	    memory_readers = new HashMap< String, PdAudioBlockStream > ( );
 						
 		world = ( PdWorld ) envAgent.getWorld ( );
 		AudioFormat format = new AudioFormat ( AudioFormat.Encoding.PCM_SIGNED, ( float ) PdConstants.SAMPLE_RATE, 
 												  PdConstants.BITS_PER_SAMPLE, PdConstants.OUTPUT_CHANNELS, PdConstants.BYTES_PER_SAMPLE,
 												  ( float ) PdConstants.SAMPLE_RATE, true );
 		DataLine.Info info = new DataLine.Info ( SourceDataLine.class, format );
 		try 
 		{
 			line = ( SourceDataLine ) AudioSystem.getLine ( info );
 			line.open ( format, PdConstants.DEFAULT_SAMPLES_PER_BUFFER * PdConstants.BYTES_PER_SAMPLE );
 			line.start ( );
 		}
 		catch ( LineUnavailableException e ) 
 		{
 			e.printStackTrace();
 		}
 		return true;
 	}
 	@Override
 	public boolean finit ( )
 	{
 	    line.drain ( );
 	    line.stop ( );
 		return true;
 	}
 	protected void new_message_event ( String target, PdMessage new_message )
 	{
 		String[ ] event_target = target.split ( PdConstants.SEPARATOR );
 		
 		PdEvent pd_event = new PdEvent ( PdConstants.MESSAGE, new_message );
 		Event new_event = new Event ( );
 		new_event.objContent = pd_event;
 		
 		addOutputEvent ( event_target[ 0 ], event_target[ 1 ], new_event );
 	}
 	protected void new_bang_event ( String target, String bang )
 	{
 		String[ ] event_target = target.split ( PdConstants.SEPARATOR );
 		
 		PdEvent pd_event = new PdEvent ( PdConstants.BANG, bang );
 		Event new_event = new Event ( );
 		new_event.objContent = pd_event;
 		
 		addOutputEvent ( event_target[ 0 ], event_target[ 1 ], new_event );
 	}
 	protected void new_float_event ( String target, PdFloat value )
 	{
 		String[ ] event_target = target.split ( PdConstants.SEPARATOR );
 		
 		PdEvent pd_event = new PdEvent ( PdConstants.FLOAT, value );
 		Event new_event = new Event ( );
 		new_event.objContent = pd_event;
 		
 		addOutputEvent ( event_target[ 0 ], event_target[ 1 ], new_event );
 	}
 	protected void new_audio_block_event ( String target, PdAudioBlock audio_block )
 	{
 		String[ ] event_target = target.split ( PdConstants.SEPARATOR );
 		
 		PdEvent pd_event = new PdEvent ( PdConstants.AUDIO_BLOCK, audio_block );
 		Event new_event = new Event ( );
 		new_event.objContent = pd_event;
 		
 		addOutputEvent ( event_target[ 0 ], event_target[ 1 ], new_event );
 	}
 	protected void process_message ( PdMessage message )
 	{
 		String source = message.get_source ( );
 		String[ ] split_source = source.split ( PdConstants.SEPARATOR );
 		String symbol = message.get_symbol ( );
 		Object[ ] arguments = message.get_arguments ( );
 		if ( actuators.containsKey ( source ) )
 		{
 			String actuator_target = ( String ) arguments[ 0 ];
 			Parameters targeted_sensor = sensors.get ( actuator_target );
 			if ( split_source[ 1 ].equals ( PdConstants.SELF_ACTUATOR ) )
 			{
 				new_message_event ( split_source[ 0 ] + PdConstants.SEPARATOR + PdConstants.SELF_SENSOR, message );
 			}
 			else if ( targeted_sensor != null )
 			{
 				new_message_event ( actuator_target, message );
 				PdMessage sensor_message = new PdMessage ( actuator_target, symbol, arguments );
 				pd_receiver.send_message ( sensor_message );
 			}
 			else if ( actuator_target.equals ( PdConstants.GLOBAL_KEY ) )
 			{
 				for ( String sensor : sensors.keySet ( ) )
 				{
 					if ( ! ( sensor.split ( PdConstants.SEPARATOR )[ 1 ].equals ( PdConstants.SELF_SENSOR ) ) )
 					{
 						new_message_event ( sensor, message );						
 						PdMessage sensor_message = new PdMessage ( sensor, symbol, arguments );
 						pd_receiver.send_message ( sensor_message );
 					}
 				}
 			}
 			else
 			{
 				System.err.println ( "Error: Message with no destination!" );
 			}
 		}
 	}
 	protected void process_bang ( String bang )
 	{
 		if ( actuators.containsKey ( bang ) )
 		{
 			String[ ] source = bang.split( PdConstants.SEPARATOR );
 			String actuator_target = actuators.get ( bang ).get ( PdConstants.SCOPE );
 			Parameters targeted_sensor = sensors.get ( actuator_target );
 			if ( source[ 1 ].equals ( PdConstants.SELF_ACTUATOR ) )
 			{
 				new_bang_event ( source[ 0 ] + PdConstants.SEPARATOR + PdConstants.SELF_SENSOR, bang );
 			}
 			else if ( targeted_sensor != null )
 			{
 				new_bang_event ( actuator_target, bang );
 				pd_receiver.send_bang ( actuator_target );
 			}
 			else if ( actuator_target.equals ( PdConstants.GLOBAL_KEY ) )
 			{
 				for ( String sensor : sensors.keySet ( ) )
 				{
 					if ( ! ( sensor.split ( PdConstants.SEPARATOR )[ 1 ].equals( PdConstants.SELF_SENSOR ) ) )
 					{
 						new_bang_event ( sensor, bang );
 						pd_receiver.send_bang ( sensor );
 					}
 				}
 			}
 			else
 			{
 				System.err.println ( "Error: Bang to actuator with no destination!" );
 			}
 		}
 	}
 	protected void process_float ( PdFloat value )
 	{
 		String source = value.get_source ( );
 		String[ ] split_source = source.split( PdConstants.SEPARATOR );
 		if ( actuators.containsKey ( source ) )
 		{
 			String actuator_target = actuators.get ( source ).get ( PdConstants.SCOPE );
 			Parameters targeted_sensor = sensors.get ( actuator_target );
 			if ( split_source[ 1 ].equals ( PdConstants.SELF_ACTUATOR ) )
 			{
 				new_float_event ( split_source[ 0 ] + PdConstants.SEPARATOR + PdConstants.SELF_SENSOR, value );
 			}
 			else if ( targeted_sensor != null )
 			{
 				new_float_event ( actuator_target, value );
 				pd_receiver.send_float ( actuator_target, value.get_value ( ) );
 			}
 			else if ( actuator_target.equals( PdConstants.GLOBAL_KEY ) )
 			{
 				for ( String sensor : sensors.keySet ( ) )
 				{
 					if ( ! ( sensor.split ( PdConstants.SEPARATOR )[ 1 ].equals( PdConstants.SELF_SENSOR ) ) )
 					{
 						new_float_event ( sensor, value );
 						pd_receiver.send_float ( sensor, value.get_value ( ) );
 					}
 				}
 			}
 		}
 	}
 	protected float[ ] process_actuator_block ( float[ ] samples, String actuator )
 	{
 		float[ ] new_samples;
 		float[ ] actuator_samples = new float[ PdConstants.PD_BLOCK_SIZE ];
 		String[ ] source = actuator.split ( PdConstants.SEPARATOR );
 
 		PdBase.readArray ( actuator_samples, 0 , actuator, 0, PdConstants.PD_BLOCK_SIZE );
 		PdAudioBlock audio_block = new PdAudioBlock ( actuator_samples, actuator );
 		for ( String memory_reader : memory_readers.keySet ( ) )
 		{
 			String[ ] target = memory_reader.split ( PdConstants.SEPARATOR );
 			if ( target[ 0 ].equals ( source[ 0 ] ) &&
 				 target[ 1 ].equals ( source[ 1 ] ) )
 			{
 				PdAudioBlockStream audio_stream = memory_readers.get ( memory_reader );
 				audio_stream.write_block ( audio_block );
 			}
 		}
 		new_audio_block_event ( source[ 0 ] + PdConstants.SEPARATOR + PdConstants.SELF_SENSOR, audio_block );
 		if ( samples == null )
 		{			
 			new_samples = actuator_samples;
 		}
 		else
 		{
 			new_samples = add_samples ( samples, actuator_samples );
 		}
 		return new_samples;
 	}
	protected void process_sensor_block ( float[ ] samples, String sensor )
	{
		String[ ] source = sensor.split ( PdConstants.SEPARATOR );

		PdAudioBlock audio_block = new PdAudioBlock ( samples, sensor );
		for ( String memory_reader : memory_readers.keySet ( ) )
		{
			String[ ] target = memory_reader.split ( PdConstants.SEPARATOR );
			if ( target[ 0 ].equals ( source[ 0 ] ) &&
				 target[ 1 ].equals ( source[ 1 ] ) )
			{
				PdAudioBlockStream audio_stream = memory_readers.get ( memory_reader );
				audio_stream.write_block ( audio_block );
			}
		}
	}
 	protected void play_audio_samples ( float[ ] samples )
 	{
 		byte[ ] byte_samples = new byte[ samples.length * PdConstants.BYTES_PER_SAMPLE ];
 		int byte_index = 0;
 		for ( int i = 0; i < samples.length; i++ )
 		{
 			float sample = samples[ i ];
 			int int_sample = Math.round ( sample * 32767.0F );
 
 			byte_samples[ byte_index ] = ( byte ) ( ( int_sample >> 8 ) & 0xFF );
 			byte_samples[ byte_index + 1 ] = ( byte ) ( int_sample & 0xFF );
 			
 			byte_index += PdConstants.BYTES_PER_SAMPLE;
 		}
 		line.write ( byte_samples, 0, byte_samples.length );
 	}
 	protected float[ ] add_samples ( float[ ] old_samples, float[ ] new_samples )
 	{
 		float[ ] samples = new float[ old_samples.length ];
 		for ( int i = 0; i < samples.length; i++ )
 		{
 			samples[ i ] = ( old_samples[ i ] + new_samples[ i ] ) / 2;
 		}
 		return samples;
 	}
 	protected void process ( )
 	{
 		float[ ] samples = null;
 		
 		for ( int i = 0; i < events.size ( ); i++ )
 		{
 			PdEvent event = events.get ( i );
 			String type = event.get_type ( );
 			if ( type.equals( PdConstants.BANG ) )
 			{
 				pd_receiver.send_bang( ( String ) event.get_content ( ) );
 			}
 			else if ( type.equals ( PdConstants.MESSAGE ) )
 			{
 				PdMessage message = ( PdMessage ) event.get_content ( );
 				pd_receiver.send_message ( message );
 			}
 			else if ( type.equals ( PdConstants.FLOAT ) )
 			{
 				PdFloat new_float = ( PdFloat ) event.get_content ( );
 				pd_receiver.send_float ( new_float.get_source ( ), new_float.get_value ( )  );
 			}
 			else if ( type.equals ( PdConstants.READ_MEMORY_T ) )
 			{
 				PdMessage message = ( PdMessage ) event.get_content ( );
 				String source = message.get_source ( );
 				int memory_offset = Math.round ( Float.parseFloat ( message.get_symbol ( ) ) );
 				if ( memory_readers.containsKey ( source ) )
 				{
 				}
 				else
 				{
 					PdAudioBlockStream audio_stream = new PdAudioBlockStream ( memory_offset * 3 );
 					memory_readers.put ( source, audio_stream );
 				}
 			}
 		}
 		events.clear ( );
 		act ( );
 		
 		pd_processor.process_ticks ( PdConstants.DEFAULT_TICKS );
 		pd_receiver.fetch_pd_messages ( );
 
 		for ( PdMessage message : pd_receiver.get_messages ( ) )
 		{
 			process_message ( message );
 		}
 		for ( String bang : pd_receiver.get_bangs ( ) )
 		{
 			process_bang ( bang );
 		}
 		for ( PdFloat pd_float : pd_receiver.get_floats ( ) )
 		{
 			process_float ( pd_float );
 		}
 		for ( String actuator : pd_receiver.get_audio_actuators ( ) )
 		{
 			samples = process_actuator_block ( samples, actuator );
 		}
 		for ( String memory_reader : memory_readers.keySet ( ) )
 		{
 			PdAudioBlockStream audio_stream = memory_readers.get ( memory_reader );
 			float[ ] read_block = audio_stream.read_block ( );
 			if ( read_block != null )
 			{
 				PdBase.writeArray ( memory_reader, 0, read_block, 0, read_block.length );
 				pd_receiver.send_bang ( memory_reader + PdConstants.TABREAD4_SIGNAL );
 			}
 		}
 		if ( samples != null )
 		{
 			for ( String sensor : pd_receiver.get_audio_sensors ( ) )
 			{
 			/* if ( source.can_reach ( sensor ) )
 			 * { */
				process_sensor_block ( samples, sensor );
 				PdBase.writeArray ( sensor, 0, samples, 0, samples.length );
 				pd_receiver.send_bang ( sensor + PdConstants.TABREAD4_SIGNAL );
				/* TODO: TELL AGENTS OF THIS BLOCK!!! */
 				if ( sensor.equals ( PdConstants.AVATAR_SENSOR ) )
 				{
 					play_audio_samples ( samples );
 				}
 			/* }
 			 */
 			}
 		}
 		pd_receiver.start_new_cycle ( );
 
 	}
 	@Override
 	public void processSense ( Event new_event ) 
 	{
 		PdEvent event = ( PdEvent ) new_event.objContent;
 		events.add ( event );
 	}
 	@Override
 	protected Parameters actuatorRegistered ( String agentName, String actuatorName, Parameters userParam ) throws Exception 
 	{
 		String sub_type = userParam.get ( PdConstants.SUB_TYPE );
 		if ( ! ( actuatorName.equals ( PdConstants.SELF_ACTUATOR ) ) && sub_type.equals ( PdConstants.AUDIO_EVENT ) )
 		{
 			pd_receiver.register_audio_actuator ( agentName + PdConstants.SEPARATOR + actuatorName );
 		}
 		return userParam;
 	}
 	protected Parameters sensorRegistered ( String agentName, String sensorName, Parameters userParam ) throws Exception 
 	{
 		String sub_type = userParam.get ( PdConstants.SUB_TYPE );
 		if ( ! ( sensorName.equals ( PdConstants.SELF_SENSOR ) ) && sub_type.equals ( PdConstants.AUDIO_EVENT ) )
 		{
 			pd_receiver.register_audio_sensor ( agentName + PdConstants.SEPARATOR + sensorName );
 		}
 		return userParam;
 	}
 	/*DEBUG
 	private void print_message ( PdMessage message )
 	{
 		String message_source = message.get_source ( );
 		String symbol = message.get_symbol ( );
 		Object[ ] arguments = message.get_arguments ( );
 		System.err.println ( "Received Message: " );
 		System.err.println ( "\tSource: " + message_source );
 		System.err.println ( "\tSymbol: " + symbol );
 		for ( Object argument : arguments )
 		{
 			try
 			{
 				System.err.println ( "\tArg: \"" + ( Float ) argument + "\"" );
 			}
 			catch ( ClassCastException e )
 			{
 				System.err.println ( "\tArg: \"" + ( String ) argument + "\"" );
 			}
 		}
 	}*/
 }
