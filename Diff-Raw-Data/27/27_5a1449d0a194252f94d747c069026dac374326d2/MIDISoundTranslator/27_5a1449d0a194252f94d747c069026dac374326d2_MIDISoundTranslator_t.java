 /**
  * 
  */
 package uk.ac.soton.ecs.wais.fest13.sound.midi;
 
 import javax.sound.midi.MidiChannel;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.midi.Synthesizer;
 
 import org.openimaj.audio.util.WesternScaleNote;
 
 import uk.ac.soton.ecs.jsh2.mediaeval13.placing.evaluation.GeoLocation;
 import uk.ac.soton.ecs.wais.fest13.SocialComment;
 import uk.ac.soton.ecs.wais.fest13.UserInformation;
 import uk.ac.soton.ecs.wais.fest13.sound.SoundTranslator;
 
 /**
  * 	Translates social commentary into wonderful MIDI music (ahem)
  * 
  * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
  * 	@created 12 Sep 2013
  * 	@version $Author$, $Revision$, $Date$
  */
 public class MIDISoundTranslator implements SoundTranslator
 {
 	/** The MIDI synth in use */
 	private Synthesizer synth;
 	
 	/** Next channel on which to play a note */
 	private int nextChannel = 0;
 
	/** Notes to generate - based on the original Hang drum */
 	private String[] notesToGenerate = new String[]{"D","A","A#","C","D","E","F"};
 	
	/** Octaves of the notes to generate - based on the original Hang drum too */
 	private int[] octavesOfNotesToGenerate = new int[]{3,3,3,4,4,4,4};
 
	/** The pan controller - to set where in the stereo field a note is played */
 	public int PAN_CONTROLLER = 10;
 	
 	/**
 	 * 	Default constructor
 	 *	@throws MidiUnavailableException If a synth could not be created
 	 */
 	public MIDISoundTranslator() throws MidiUnavailableException
 	{
 		this.synth = MidiSystem.getSynthesizer();
 		synth.open();
 	}
 
 	/**
 	 *	{@inheritDoc}
 	 * 	@see uk.ac.soton.ecs.wais.fest13.sound.SoundTranslator#translate(uk.ac.soton.ecs.wais.fest13.SocialComment, uk.ac.soton.ecs.wais.fest13.UserInformation)
 	 */
 	@Override
 	public void translate( SocialComment comment, UserInformation userInformation )
 	{
 		// Give a bit more range to the notes we've provided
 		int alterOctave = (int)(Math.random()*3)-1;
 		
 		// Create the note
 		int indx = (int)(Math.random() * notesToGenerate.length);
 		WesternScaleNote note = WesternScaleNote.createNote( 
 				notesToGenerate[indx], 
 				octavesOfNotesToGenerate[indx]+alterOctave );
 		
 		// Get the channel
 		System.out.println( nextChannel );
 		MidiChannel chan = synth.getChannels()[nextChannel];
 		
 		// For now we will just use the piano
 		chan.programChange( 0 );
 		
 		// Set the pan position
 		chan.controlChange( PAN_CONTROLLER, 
 				getPanPosition( comment.location.longitude ) );
 		
 		// Play the note
 		chan.noteOn( note.noteNumber, 100 );
 		
 		// increment the channel. We don't use channel 10 (drums in GM)
 		nextChannel++;
 		if( nextChannel == 9 ) nextChannel++;
 		nextChannel %= 16;
 	}
 	
 	/**
 	 * 	Converts -180 to +180 longitude to a pan position
 	 * 	0 - 127
 	 * 
 	 *	@param longitude -180 to 180
 	 *	@return pan position 0 to 127
 	 */
 	public int getPanPosition( double longitude )
 	{
 		return (int)((longitude+180)*127/360);
 	}
 
 	/**
 	 *	{@inheritDoc}
 	 * 	@see uk.ac.soton.ecs.wais.fest13.sound.SoundTranslator#stop()
 	 */
 	@Override
 	public void stop()
 	{
 		this.synth.close();
 	}
 
 	/**
 	 *	@param args
 	 *	@throws MidiUnavailableException
 	 * 	@throws InterruptedException 
 	 */
 	public static void main( String[] args ) throws MidiUnavailableException, InterruptedException
 	{		
 		MIDISoundTranslator mst = new MIDISoundTranslator();
 		
 		for( int i = 0; i < 20; i++ )
 		{
 			SocialComment comment = new SocialComment();
 			comment.location = new GeoLocation( Math.random()*90, Math.random()*360-180 );
 			UserInformation userInformation = new UserInformation();
 			
 			mst.translate( comment, userInformation );
 			Thread.sleep( 600 );
 		}
 		
 		Thread.sleep( 5000 );
 	}
 }
