 package arcane_arcade_field;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import arcane_arcade_main.Options;
 import arcane_arcade_main.SoundEffectPlayer;
 import arcane_arcade_status.Element;
 import sound.WavSoundQueue;
 
 /**
  * WizardSoundQueuePlayer plays soundqueues made from wizard sounds. The player 
  * can play either element sounds or dialogs.
  *
  * @author Mikko Hilpinen.
  *         Created 8.9.2013.
  */
 public class WizardSoundQueuePlayer
 {
 	// ATTRIBUTES	------------------------------------------------------
 	
 	private HashMap<Avatar, WavSoundQueue> elementqueues;
 	private WavSoundQueue dialogqueue;
 	private WizardRelay wizardrelay;
 	
 	
 	// CONSTRUCTOR	------------------------------------------------------
 	
 	/**
 	 * Creates a new wizardsoundqueueplayer ready to play wizard sounds as 
 	 * queues
 	 * 
 	 * @param wizardrelay The relay that contains information about all the 
 	 * wizards in the game
 	 */
 	public WizardSoundQueuePlayer(WizardRelay wizardrelay)
 	{
 		// Initializes attributes
 		this.dialogqueue = new WavSoundQueue(false);
 		this.elementqueues = new HashMap<Avatar, WavSoundQueue>();
 		this.wizardrelay = wizardrelay;
 	}
 	
 	
 	// OTHER METHODS	--------------------------------------------------
 	
 	/**
 	 * Plays two elementsounds in a succession
 	 *
 	 * @param element1 The first element
 	 * @param element2 The second element
 	 * @param avatar The avatar that uses the elements
 	 * @param x The x-coordinate of the sound's source on the screen
 	 */
 	public void playElementSoundCombo(Element element1, Element element2, 
 			Avatar avatar, double x)
 	{
 		// If there is already an element queue playing, ends it
 		if (this.elementqueues.containsKey(avatar) && 
 				!this.elementqueues.get(avatar).isDead())
 			this.elementqueues.get(avatar).stop();
 		
 		// Creates the new queue
 		WavSoundQueue newqueue = new WavSoundQueue(true);
 		// Adds the sounds to the queue and plays it
 		String soundname1 = element1.getSoundName() + "1";
 		String soundname2 = element2.getSoundName() + "2";
 		
 		//System.out.println("Plays sounds: " + soundname1 + ", " + soundname2);
 		
 		newqueue.addWavSound(avatar.getVoiceBank().getSound(soundname1), 
 				Options.voicevolumeadjustment, 
 				SoundEffectPlayer.getEnvironmentalPan(x), false);
 		newqueue.addWavSound(avatar.getVoiceBank().getSound(soundname2), 
 				Options.voicevolumeadjustment, 
 				SoundEffectPlayer.getEnvironmentalPan(x), true);
 		
 		// Adds the queue to the map
 		this.elementqueues.put(avatar, newqueue);
 	}
 	
 	/**
 	 * Plays a dialog where multiple mages shout a catchphrase depending on 
 	 * the situation.
 	 *
 	 * @param event The situation the wizard is in
 	 * @param source The wizard that will make the initial dialog voice.
 	 */
 	public void playDialogEvent(DialogEvent event, Wizard source)
 	{
		System.out.println("Plays a single dialog: " + event);
 		
 		// If the event is noevent, plays no dialog
 		if (event == DialogEvent.NODIALOG)
 			return;
 		
 		// Checks the soundnames used in the dialog
 		String firstvoicename = event.toString().toLowerCase();
 		String secondvoicename = 
 				event.getOpposingEvent().toString().toLowerCase();
 		
 		// For some dialog events, the remaining HP of the wizard affects the 
 		// played sounds
 		if (event == DialogEvent.DAMAGE || event == DialogEvent.STRIKE)
 		{
 			// Calculates the larges HP the other wizards have
			int othermaxhp = getMaxOtherWizardHP(source);
 			// Adjusts the soundnames
 			// TODO: Check if this would be better with >
			if (source.getMaxHP() >= othermaxhp)
 			{
 				firstvoicename += "win";
 				secondvoicename += "loss";
 			}
 			else
 			{
 				firstvoicename += "loss";
 				secondvoicename += "win";
 			}
 		}
 		
 		// Adds the dialog(s) to the queue
 		// Adds firstsound by source to the gueue
 		System.out.println("Wizard 1: " + firstvoicename);
 		this.dialogqueue.addWavSound(
 				source.getAvatar().getVoiceBank().getSound(firstvoicename), 
 				Options.voicevolumeadjustment, 
 				SoundEffectPlayer.getEnvironmentalPan(source.getX()), true);
 		// Adds secondsound by other wizards to the queue (if needed)
 		if (event.getOpposingEvent() != DialogEvent.NODIALOG)
 		{
 			ArrayList<Wizard> opponents = this.wizardrelay.getWizardsFromSide(
 					source.getScreenSide().getOppositeSide());
 			
 			for (Wizard opponent : opponents)
 			{
				System.out.println("Wizard 2: " + secondvoicename);
 				// TODO: It seems like damageloss or strikewin is never played
 				
 				//Wizard opponent = this.wizardrelay.getWizard(i);
 				this.dialogqueue.addWavSound(opponent.getAvatar().getVoiceBank(
 						).getSound(secondvoicename), 
 						Options.voicevolumeadjustment, 
 						SoundEffectPlayer.getEnvironmentalPan(opponent.getX()), 
 						true);
 			}
 		}
 	}
 	
 	// Returns the largest HP the other wizards have
 	private int getMaxOtherWizardHP(Wizard wizard)
 	{
 		int maxhp = 0;
 		// Goes through all the (other) wizards
		for (int i = 0; i < this.wizardrelay.getWizardNumber(); i++)
 		{
			Wizard w = this.wizardrelay.getWizard(i);
			if (w.getMaxHP() > maxhp && !w.equals(wizard))
				maxhp = w.getMaxHP();
 		}
 		
 		return maxhp;
 	}
 	
 	
 	// ENUMERATIONS
 	
 	/**
 	 * DialogEvent is used to define which sound is played at which situation.
 	 *
 	 * @author Mikko Hilpinen.
 	 *         Created 8.9.2013.
 	 */
 	public enum DialogEvent
 	{
 		/**
 		 * Victory event is usually played when a wizard wins the round
 		 */
 		VICTORY, 
 		/**
 		 * Loss event is usually played when a wizard dies
 		 */
 		LOSS, 
 		/**
 		 * Damage event is usually played when a wizard takes damage
 		 */
 		DAMAGE,
 		/**
 		 * Strike event is played when the wizard deals damage
 		 */
 		STRIKE, 
 		/**
 		 * Curse event is played when the wizard has cursed the opponent
 		 */
 		CURSE, 
 		/**
 		 * Cursed event is played when a wizard has been cursed
 		 */
 		CURSED, 
 		/**
 		 * Special event is played when a wizard makes something extraordinary
 		 */
 		SPECIAL, 
 		/**
 		 * Nodialog event is played when no sound effect is needed
 		 */
 		NODIALOG;
 		
 		
 		// OTHER METHODS	----------------------------------------------
 		
 		private DialogEvent getOpposingEvent()
 		{
 			switch (this)
 			{
 				case VICTORY: return LOSS;
 				case LOSS: return VICTORY;
 				case DAMAGE: return STRIKE;
 				case STRIKE: return DAMAGE;
 				case CURSE: return CURSED;
 				case CURSED: return CURSE;
 				default: return NODIALOG;
 			}
 		}
 	}
 }
