 /**
  * 
  */
 package uk.ac.soton.ecs.wais.fest13.sound.liquinth;
 
import java.util.Arrays;

 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.Mixer.Info;
 
 import jvst.examples.liquinth.Liquinth;
 import jvst.examples.liquinth.Player;
 import uk.ac.soton.ecs.wais.fest13.SocialComment;
 import uk.ac.soton.ecs.wais.fest13.UserInformation;
 import uk.ac.soton.ecs.wais.fest13.sound.SoundTranslator;
 
 /**
  *	Uses the Liquinth analogue synthesizer to generate sounds for a
 *	given social comment.	
  *
  *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
  *  @created 12 Sep 2013
  */
 public class LiquinthSoundTranslator implements SoundTranslator
 {
 	public int OVERDRIVE = 0;
 	public int FILTER_CUTOFF = 1;
 	public int FILTER_RESONANCE = 2;
 	public int FILTER_ATTACK = 3;
 	public int FILTER_DECAY = 4;
 	public int PORTAMENTO_SPEED = 5;
 	public int WAVEFORM = 6;
 	public int ENV_ATTACK = 7;
 	public int ENV_RELEASE = 8;
 	public int DETUNE = 9;
 	public int VIBRATO_SPEED = 10;
 	public int VIBRATO_DEPTH = 11;
 	public int PULSE_WIDTH = 12;
 	public int TIMBRE = 13;
 	
 	/** This is the analogue synth we're going to use */
 	private Liquinth synth = new Liquinth( Player.SAMPLING_RATE );
 	
 	/** This is the mixer for the synth */
 	private Player player = new Player( synth );
 	
 	/**
 	 * @throws InterruptedException 
 	 * 
 	 */
 	public LiquinthSoundTranslator( Mixer mixer ) throws InterruptedException
 	{
 		synth.setController( OVERDRIVE, 127 );
 		synth.setController( FILTER_CUTOFF, 127 );
 		synth.setController( FILTER_RESONANCE, 80 );
 		synth.setController( FILTER_ATTACK, 20 );
 		synth.setController( FILTER_DECAY, 20 );
 		synth.setController( PORTAMENTO_SPEED, 0 );
 		synth.setController( WAVEFORM, 32 );
 		synth.setController( ENV_ATTACK, 4 );
 		synth.setController( ENV_RELEASE, 70 );
 		synth.setController( DETUNE, 10 );
 		synth.setController( VIBRATO_SPEED, 0 );
 		synth.setController( VIBRATO_DEPTH, 0 );
 		synth.setController( PULSE_WIDTH, 30 );
 		synth.setController( TIMBRE, 0 );
 		synth.setModWheel( 0 );
 		synth.setPitchWheel( 0 );
 
 		player.setMixer( mixer );
 		new Thread( player ).start();
 	}
 	
 	/**
 	 *	{@inheritDoc}
 	 * 	@see uk.ac.soton.ecs.wais.fest13.sound.SoundTranslator#translate(uk.ac.soton.ecs.wais.fest13.SocialComment, uk.ac.soton.ecs.wais.fest13.UserInformation)
 	 */
 	@Override
 	public void translate( SocialComment comment, UserInformation userInformation )
 	{
 		synth.noteOn( 60, 100 );
 	}
 	
 	/**
 	 *	{@inheritDoc}
 	 * 	@see uk.ac.soton.ecs.wais.fest13.sound.SoundTranslator#stop()
 	 */
 	@Override
 	public void stop()
 	{
 		player.stop();
 	}
 	
 	public static void main( String[] args ) throws InterruptedException
 	{
 		// This will print all the mixers for your information.
 		// You need to select the index of the output you need.
 		Info[] mi = AudioSystem.getMixerInfo();
 		for( int i = 0; i < mi.length; i++ )
 			System.out.println( i+" : "+mi[i] );
 		
 		// PUT YOUR INDEX HERE:
 		int index = 0;
 		
 		// Select the mixer to which the synth will output
 		Mixer mixer = AudioSystem.getMixer( mi[index] );
 
 		Thread.sleep( 500 );
 		
 		// Test the translator with a dummy social comment
 		LiquinthSoundTranslator lst = new LiquinthSoundTranslator( mixer );
 		
 		System.out.println( "Player now playing..." );
 		Thread.sleep( 500 );
 		
 		// Translate some social comment to sound.
 		SocialComment comment = new SocialComment();
 		UserInformation userInformation = new UserInformation();		
 		lst.translate( comment, userInformation );
 		
 		// Wait for a bit.
 		Thread.sleep( 2000 );
 		
 		lst.stop();
 		
 		System.out.println( "Done." );
 	}
 }
