 package ensemble.apps.pd_testing;
 
 import java.util.ArrayList;
 import ensemble.*;
 import ensemble.apps.pd_testing.Pd_World;
 import javax.sound.sampled.*;
 
 public class Pd_SoundEventServer extends EventServer 
 {
 	Pd_World world;
 	protected String agent_name;
 	protected String agent_component_name;
 	private int[ ] samples_per_instant = new int[ Pd_Constants.PD_EVENT_BUFFER_SIZE ];
 	private int agent_number;
 	ArrayList< Pd_Audio_Buffer > events = new ArrayList< Pd_Audio_Buffer > ( );
 	private int current_sample = 0;
 
 	/*
 	 * Audio
 	 */
 	private SourceDataLine line;   // to play the sound
 
 	@Override
 	public boolean configure ( ) 
 	{
 		setEventType ( "SOUND" );
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ensemble.EventServer#init()
 	 */
 	@Override
 	public boolean init ( ) 
 	{
 		world = ( Pd_World ) envAgent.getWorld ( );
 		
 		for ( int i = 0; i < Pd_Constants.PD_EVENT_BUFFER_SIZE; i ++ )
 		{
 			samples_per_instant[ i ] = 0;
 		}
 		for ( int i = 0; i < Pd_Constants.PD_EVENT_BUFFER_SIZE; i++ )
 		{
 			events.add ( i, null );
 		}
 		agent_number = Integer.parseInt ( envAgent.getParameter ( Pd_Constants.AGENT_NUMBER_ARGUMENT ) );
 		AudioFormat format = new AudioFormat ( ( float ) Pd_Constants.SAMPLE_RATE, Pd_Constants.BITS_PER_SAMPLE, Pd_Constants.OUTPUT_CHANNELS, true, true );
 		DataLine.Info info = new DataLine.Info ( SourceDataLine.class, format );        
 		try 
 		{
 			line = ( SourceDataLine ) AudioSystem.getLine ( info );
 		    line.open ( format );
 		} 
 		catch ( LineUnavailableException e ) 
 		{
 			e.printStackTrace();
 		}
 		line.start ( );
 		return true;
 	}
 	@Override
 	public boolean finit ( )
 	{
         line.drain ( );
         line.stop ( );
 		return true;
 	}
 	private byte[ ] add_buffers ( byte[ ] samples, Pd_Audio_Buffer old_buffer )
 	{
 		byte[ ] previous_samples = old_buffer.get_audio_samples ( );
 		
 		if ( samples.length >= previous_samples.length )
 		{
 			for ( int i = 0; i < samples.length; i++ )
 			{
 				if ( i < previous_samples.length )
 				{
					samples[ i ] += previous_samples[ i ];
 				}
 			}
 			return samples;
 		}
 		else
 		{
 			for ( int i = 0; i < previous_samples.length; i++ )
 			{
 				if ( i < samples.length )
 				{
					previous_samples[ i ] += samples[ i ];
 				}
 			}
 			return previous_samples;
 		}
 	}
 	private void add_new_buffer ( int instant, byte[ ] new_buffer )
 	{
 		events.set ( instant, new Pd_Audio_Buffer ( new_buffer, instant, "EVENT_SERVER" ) );
 	}
 	private void process_audio_buffer ( Pd_Audio_Buffer new_buffer, int instant )
 	{
 		Pd_Audio_Buffer event = new_buffer;
 		byte[ ] samples = event.get_audio_samples ( );
 		for ( int i = 0; i < samples.length; i++ )
 		{	
 			samples[ i ] = ( byte ) ( samples[ i ] / ( float ) agent_number ); 
 		}
 		if ( events.get( instant ) != null )
 		{
 			Pd_Audio_Buffer previous_event = events.get ( instant );
 			events.set( instant, new Pd_Audio_Buffer ( add_buffers ( samples, previous_event ), instant, "EVENT_SERVER" ) );
 			samples_per_instant[ instant ] += 1;
 		}
 		else
 		{
 			add_new_buffer ( instant, samples );
 			samples_per_instant[ instant ] += 1;
 		}
 	}
 	protected void process ( )
 	{
 		byte[ ] samples;
 		current_sample %= Pd_Constants.PD_EVENT_BUFFER_SIZE;
 		if ( samples_per_instant [ current_sample ] >=  agent_number )
 		{
 			samples = events.get ( current_sample ).get_audio_samples ( );
 			line.write( samples, 0, samples.length );
 			samples_per_instant [ current_sample ] = 0;
 			events.set ( current_sample, null );
 			current_sample += 1;
 		}
 	}
 	@Override
 	public void processSense ( Event new_event ) 
 	{
 		Pd_Audio_Buffer event = ( Pd_Audio_Buffer ) new_event.objContent;
 		int instant = event.get_pd_time_tag ( ) % Pd_Constants.PD_EVENT_BUFFER_SIZE;
 		process_audio_buffer ( event, instant );
 	}
 }
