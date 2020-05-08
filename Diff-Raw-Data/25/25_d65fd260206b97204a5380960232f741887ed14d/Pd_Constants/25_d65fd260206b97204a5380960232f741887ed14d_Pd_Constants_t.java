 package ensemble.apps.pd_testing;
 
 public final class Pd_Constants 
 {
 	private Pd_Constants ( ) { };
 	/*
 	 * Numbers:
 	 */
 	public static final int SAMPLE_RATE = 44100;
 	public static final int PD_EVENT_BUFFER_SIZE = 200;
 	public static final int BYTES_PER_SAMPLE = 2;
 	public static final int BITS_PER_SAMPLE = 8 * BYTES_PER_SAMPLE;
 	public static final int INPUT_CHANNELS = 0;
 	public static final int OUTPUT_CHANNELS = 2;
 	public static final int BANG_OUTLETS = 5;
 	public static final int FLOAT_OUTLETS = 5; 	
 	public static final int START_INSTANT = 0;
 	
	public static final float DEFAULT_SECONDS = 5;
 	/*
 	 * Text:
 	 */
 	/* Arguments:
 	 * 
 	 */
 	public static final String CURRENT_INSTANT = "CURRENT_INSTANT";
 	public static final String PATCH_ARGUMENT = "PATCH";
 	public static final String AGENT_NUMBER_ARGUMENT = "AGENTS";
 	/*
 	 * Ensemble will send bangs to these symbols:
 	 */
 	public static final String PROCESSING_ON = "processing_on";
 	public static final String PROCESSING_OFF = "processing_off";
 	/*
 	 * Ensemble will subscribe to these symbols:
 	 */
 	public static final String SUBSCRIPTION = "subscribe";
 	public static final String UNSUBSCRIPTION = "unsubscribe";
 	public static final String BANG = "bang";
 	public static final String FLOAT = "float";
 	public static final String NEW_SUBSCRIPTION = "subscribe";
 	public static final String AUDIO_TOGGLE = "audio_toggle";
 	public static final String AUDIO_ON = "audio_on";
 	public static final String AUDIO_OFF = "audio_off";
 	public static final String TICK = "set_ticks";
 	public static final String SECONDS = "set_seconds";
 	public static final String MUTE = "MUTE";
 }
