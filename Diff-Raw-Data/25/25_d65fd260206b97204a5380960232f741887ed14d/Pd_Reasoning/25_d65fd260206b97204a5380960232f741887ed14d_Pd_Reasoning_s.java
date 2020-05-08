 package ensemble.apps.pd_testing;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ShortBuffer;
 import java.util.ArrayList;
 
 import ensemble.*;
 import ensemble.apps.pd_testing.Pd_Constants;
 import ensemble.memory.*;
 import org.puredata.core.*;
 
 /*
  * This ensemble.Reasoning extension
  * uses a pure-data patch as a source
  * of audio samples and other data.
  * 
  * It is able to receive bangs, floats and
  * messages from patches.
  * 
  * The method "process_pd_messages" is
  * called once every pd processing cycle,
  * and its extension can provide processing
  * of information from the patch.
  * 
  *  Audio samples are directly sent to the event server
  *  at this point.
  */
 public class Pd_Reasoning extends Reasoning
 {
 	/* 
 	 * This reasoning will have its own
 	 * audio Actuator.
 	 */
 	private Actuator speaker;
 	private Memory speaker_memory;
 	private Control_Symbols control_symbols;
 	private Control_Symbols user_control_symbols;
 	/*
 	 * Buffers and constants for
 	 * initialising Pd.
 	 */
     private float seconds = Pd_Constants.DEFAULT_SECONDS;
     /* Number of Pd ticks to get one second worth of samples. */
     private int ticks = ( int ) ( seconds * ( Pd_Constants.SAMPLE_RATE / ( float ) PdBase.blockSize ( ) ) );
     
 	private int patch;
 	private Pd_Receiver receiver;
 	private boolean pd_audio_output = true;
 	private boolean mute_patch = false;
 	
     private int frames;
     private short[ ] dummy_pd_input;
     private short[ ] samples;
     private byte[ ] raw_samples;
     private byte[ ] dummy_samples;
     
     private ByteBuffer buf;
     private ShortBuffer shortBuf;
     
     private void open_dsp ( int target_patch )
     {
     	PdBase.sendBang ( target_patch + Pd_Constants.PROCESSING_ON );
     }
     private void close_dsp ( int target_patch )
     {
     	PdBase.sendBang ( target_patch + Pd_Constants.PROCESSING_OFF );
     }
     private void process_ensemble_control_messages ( )
     {
     	/*
     	 * Process all messages sent
     	 * to default control symbols.
     	 * 
     	 */
     	PdBase.pollPdMessageQueue ( );
     	ArrayList< Pd_Message > messages = receiver.get_messages ( );
     	ArrayList< Pd_Float > floats = receiver.get_floats ( );
     	ArrayList< String > bangs = receiver.get_bangs ( );
     	for ( Pd_Message message : messages )
     	{
     		if ( message.get_source ( ).equals ( Pd_Constants.SUBSCRIPTION ) )
     		{
 				System.err.println ( "PURE_DATA: REGISTERED_USER_SYMBOL: " + message.get_symbol ( ) );
     			user_control_symbols.register_symbol( message.get_symbol ( ) );
 				PdBase.subscribe ( message.get_symbol ( ) );
     		}
     		else if ( message.get_source ( ).equals ( Pd_Constants.UNSUBSCRIPTION ) )
     		{
 				System.err.println ( "PURE_DATA: DEREGISTERED_USER_SYMBOL: " + message.get_symbol ( ) );
     			user_control_symbols.deregister_symbol ( message.get_symbol ( ) );
 				PdBase.unsubscribe ( message.get_symbol ( ) );
     		}
     	}
     	for ( Pd_Float sent_float : floats )
     	{
 			if ( sent_float.get_source ( ).equals ( Pd_Constants.TICK ) )
 			{
 				ticks = ( int ) sent_float.get_value ( );
 				seconds = ( float ) ticks / ( Pd_Constants.SAMPLE_RATE / ( float ) PdBase.blockSize ( ) );
 				System.err.print ( "PURE_DATA_SETTING_TICK_SIZE: " + ticks + "\n" );
 			}
 			else if ( sent_float.get_source ( ).equals ( Pd_Constants.SECONDS ) )
 			{
 				seconds = sent_float.get_value ( );
 				ticks = ( int ) ( seconds * ( Pd_Constants.SAMPLE_RATE / ( float ) PdBase.blockSize ( ) ) );
 				System.err.print ( "PURE_DATA_SETTING_SECONDS: " + seconds + "\n" );
 			}
     	}
     	for ( String sent_bang : bangs )
     	{
     		if ( sent_bang.equals ( Pd_Constants.AUDIO_OFF ) )
     		{
     			pd_audio_output = false;
     			System.err.println ( "PURE_DATA: AUDIO_OFF" );
     		}
     		else if ( sent_bang.equals ( Pd_Constants.AUDIO_ON ) )
     		{
     			pd_audio_output = true;
     			System.err.println ( "PURE_DATA: AUDIO_ON" );
     		}
     		else if ( sent_bang.equals ( Pd_Constants.AUDIO_TOGGLE ) )
     		{			
     			pd_audio_output = ! ( pd_audio_output );
     			System.err.println ( "PURE_DATA: AUDIO_TOGGLED" );
     		}
     		else if ( sent_bang.equals ( Pd_Constants.MUTE ) )
     		{
     			mute_patch = true;
     			pd_audio_output = ! ( mute_patch );
     			System.err.println ( "PURE_DATA: PATCH WITH NO AUDIO OUTPUT." );
     		}
     	}
     }
 	private void process_pd_ticks ( int target_patch )
 	{
 		open_dsp ( target_patch );
         PdBase.process ( ticks, dummy_pd_input, samples );
         shortBuf.rewind ( );
         shortBuf.put ( samples );
         close_dsp ( target_patch );
 	}
 	/*
 	 * User can implement message checking here.
 	 */
     private void process_pd_messages ( ) 
     { 
     	ArrayList< Pd_Message > messages = receiver.get_messages ( );
     	ArrayList< Pd_Float > floats = receiver.get_floats ( );
     	ArrayList< String > bangs = receiver.get_bangs ( );
     	for ( Pd_Message message : messages )
     	{
 			for ( String symbol : user_control_symbols.get_list ( ) )
 			{				
 				if ( message.get_source ( ).equals( symbol ) )
 				{
 					System.err.println ( "PURE_DATA: MESSAGE: SRC=" + message.get_source ( ) + " SYM=" + message.get_symbol ( ) );
 				}
 			}
     	}
     	for ( Pd_Float sent_float : floats )
     	{
 			for ( String symbol : user_control_symbols.get_list ( ) )
 			{
 				if ( sent_float.get_source ( ).equals( symbol ) )
 				{
 					System.err.println ( "PURE_DATA: FLOAT: SRC=" + sent_float.get_source ( ) + " NUM=" + sent_float.get_value ( ) );
 				}
 			}
     	}
     	for ( String sent_bang : bangs )
     	{
 			for ( String symbol : user_control_symbols.get_list ( ) )
 			{
 				if ( sent_bang.equals( symbol ) )
 				{
 					System.err.println ( "PURE_DATA: BANG: SRC=" + sent_bang );
 				}
 			}
     	}
     }
 	/*
 	 * The init method will be called once every time this
 	 * Reasoning is included in an Agent's Components.
 	 * 
 	 * (non-Javadoc)
 	 * @see ensemble.MusicalAgentComponent#init()
 	 */
 	@Override
 	public boolean init ( ) 
 	{
		getAgent ( ).getKB ( ).registerFact ( Pd_Constants.CURRENT_INSTANT, String.valueOf ( Pd_Constants.START_INSTANT ), false );
 		user_control_symbols = new Control_Symbols ( );
 		control_symbols = new Control_Symbols ( );
 		control_symbols.register_symbol ( Pd_Constants.AUDIO_TOGGLE );
 		control_symbols.register_symbol ( Pd_Constants.AUDIO_ON );
 		control_symbols.register_symbol ( Pd_Constants.AUDIO_OFF );
 		control_symbols.register_symbol ( Pd_Constants.TICK );
 		control_symbols.register_symbol ( Pd_Constants.SECONDS );
 		control_symbols.register_symbol ( Pd_Constants.MUTE );
 		control_symbols.register_symbol ( Pd_Constants.SUBSCRIPTION );
 		control_symbols.register_symbol ( Pd_Constants.UNSUBSCRIPTION );
 		/*
 		 * Pd Setup
 		 * 
 		 * Each reasoning has its own
 		 * receiver.
 		 * 
 		 */
 		receiver = new Pd_Receiver ( );
 		PdBase.setReceiver ( receiver );
 
 		dummy_pd_input = new short[ Pd_Constants.INPUT_CHANNELS ];
 		/*
 		 * Subscribing to known control symbols.
 		 */
 		for ( String symbol : control_symbols.get_list ( ) )
 		{
 			PdBase.subscribe ( symbol );
 		}
 		/* TODO: Subscribe to float outlets dinamically?
 		 *       Subscribe to bang outlets dinamically? 
 		 */
 		for ( int i = 0; i < Pd_Constants.BANG_OUTLETS; i++ )
 		{
 			user_control_symbols.register_symbol ( Pd_Constants.BANG + i );
 			PdBase.subscribe(  Pd_Constants.BANG + i );
 		}
 		for ( int i = 0; i < Pd_Constants.FLOAT_OUTLETS; i++ )
 		{
 			user_control_symbols.register_symbol ( Pd_Constants.FLOAT + i );
 			PdBase.subscribe(  Pd_Constants.FLOAT + i );
 		}
 		/*
          * Patch opening.
          */
 		try 
 		{
 			patch = PdBase.openPatch ( parameters.get( Pd_Constants.PATCH_ARGUMENT ) );
 			System.err.println ( "PURE_DATA: PATCH_ID=" + patch + " PATH=" + "\"" + parameters.get( Pd_Constants.PATCH_ARGUMENT ) + "\"" );
 			process_ensemble_control_messages ( );
 			process_pd_messages ( );
 	    	( ( Pd_Receiver ) receiver ).start_new_cycle ( );
 		} 
 		catch ( IOException e ) 
 		{
 			e.printStackTrace ( );
 		}
 		/*
 		 * Initialising Pd audio buffers.
 		 */
 		frames = PdBase.blockSize ( ) * ticks;        
 		samples = new short[ frames * Pd_Constants.OUTPUT_CHANNELS ];       
 		raw_samples = new byte[ samples.length * Pd_Constants.BYTES_PER_SAMPLE ];
 		dummy_samples = new byte[ samples.length * Pd_Constants.BYTES_PER_SAMPLE ];
 		buf = ByteBuffer.wrap ( raw_samples );
 		shortBuf = buf.asShortBuffer ( );
 		/*
 		 * The dummy samples buffer is used when a patch is not
 		 * suposed to produced samples, but we do not want to
 		 * lose real-time due to lack of samples to play, so we
 		 * just play zeroes.
 		 * 
 		 */
 		for ( int i = 0; i < dummy_samples.length; i++ )
 		{
 			dummy_samples[ i ] = 0;
 		}
 		close_dsp ( patch );
 		return true;
 	}
 	/*
 	 * The process method is called once every cycle
 	 * of the framework.
 	 * 
 	 * (non-Javadoc)
 	 * @see ensemble.Reasoning#process()
 	 */
 	@Override
 	public void process ( ) 
 	{
 		byte[ ] output;
		int current_instant = Integer.parseInt ( getAgent ( ).getKB ( ).readFact ( Pd_Constants.CURRENT_INSTANT ) );
 
 		process_pd_ticks ( patch );
 		process_ensemble_control_messages ( );
 		process_pd_messages ( );
 		( ( Pd_Receiver ) receiver ).start_new_cycle ( );
 		
 		if ( pd_audio_output && ! ( mute_patch ) )
 		{
 			output = raw_samples;
 		}
 		else
 		{
 			output = dummy_samples;
 		}
 		try 
 		{
 			speaker_memory.writeMemory ( new Pd_Audio_Buffer ( output, current_instant, getAgent ( ).getAgentName ( ) ) );
 		} 
 		catch ( MemoryException e ) 
 		{
 			e.printStackTrace ( );
 		}
 		current_instant += 1;
		getAgent ( ).getKB ( ).updateFact ( Pd_Constants.CURRENT_INSTANT, String.valueOf ( current_instant ) );
 		speaker.act ( );
 	}
 	/*
 	 * Called when an event handler is registered in the agent.
 	 * 
 	 * (non-Javadoc)
 	 * @see ensemble.Reasoning#eventHandlerRegistered(ensemble.EventHandler)
 	 */
 	@Override
 	protected void eventHandlerRegistered ( EventHandler event_handler ) 
 	{
 		speaker = ( Pd_Speaker ) event_handler;
 		speaker_memory = getAgent ( ).getKB ( ).getMemory ( speaker.getComponentName ( ) );
 	}
 	/*
 	 * Class to manage subscribed symbols.
 	 * 
 	 */
 	public final class Control_Symbols
 	{
 		ArrayList< String > symbols;
 		public Control_Symbols ( )
 		{
 			symbols = new ArrayList< String > ( );
 		}
 		public void register_symbol ( String new_symbol )
 		{
 			symbols.add ( new_symbol );
 		}
 		public void deregister_symbol ( String target )
 		{
 			symbols.remove ( target );
 		}
 		public ArrayList< String > get_list ( )
 		{
 			return symbols;
 		}
 	}
 }
