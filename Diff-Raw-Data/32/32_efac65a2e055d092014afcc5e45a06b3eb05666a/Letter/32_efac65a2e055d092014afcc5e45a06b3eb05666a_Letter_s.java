 package edu.bu.cs673.AwesomeAlphabet.model;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.Icon;
 
 import org.apache.log4j.Logger;
 
 import edu.bu.cs673.AwesomeAlphabet.main.Settings;
 
 
 /**
  * This class defines the Letter model.  It represents a
  * letter in the English alphabet and contains a list
  * words that start with the letter, along with associated
  * pictures and sounds.  An index is maintained for the
  * word list to keep track of which word was most recently
  * shown.
  */
 public class Letter extends Observable implements Observer {
 
 	private char m_cLetter;
 	private List<WordPictureSound> m_wps = new LinkedList<WordPictureSound>();
 	private GameSound m_LetterSound;
 	private GameSound m_PhonicSound;
 	private int m_index = -1;
 	private enum Sound_Type {NONE, WPS, LETTER, PHONIC};
 	private Sound_Type curr_sound = Sound_Type.NONE;
 	private ThemeManager m_themeMgr;
 	protected static Logger log = Logger.getLogger(Letter.class);
 
 	
 	
 	/**
 	 * Class constructor.
 	 * 
 	 * @param cLetter   The letter that this object will represent.
 	 */
 	public Letter(char cLetter, ThemeManager themeMgr) {
 	
 		m_cLetter = Character.toLowerCase(cLetter);
 		m_themeMgr = themeMgr;
 		
 		if(m_themeMgr != null)
 			m_themeMgr.addObserver(this);
 	}
 
 	
 	/**
 	 * Gets the lower-case letter that this object represents.
 	 * 
 	 * @return   The letter as a char.
 	 */
 	public char GetLetterAsChar()
 	{
 		return m_cLetter;
 	}
 	
 	
 	/**
 	 * Gets the upper-case letter that this object represents.
 	 * 
 	 * @return   The letter as a char.
 	 */
 	public char GetUppercaseLetter()
 	{
 		return Character.toUpperCase(m_cLetter);
 	}
 	
 	/**
 	 * Gets an iterator to the list of WordPictureSound objects.
 	 * 
 	 * @return   An iterator to the list of WordPictureSound objects. 
 	 */
 	public ListIterator<WordPictureSound> GetIterator()
 	{
 
 		log.info("return the iterator");
 		return m_wps.listIterator();
 	}
 
 	
 	/**
 	 * Adds a new WordPictureSound object to the word list.
 	 * 
 	 * @param imageName    The filename of the word image.
 	 * @param soundName    The filename of the word sound.
 	 * @param wordText     The text of the word.
 	 * @param theme        The theme.
 	 */
 	public void addResource(String imageName, String soundName, String wordText, Theme theme) {
 
 		int id = m_wps.size()+1;
 		m_wps.add(new WordPictureSound(m_cLetter, wordText, imageName, soundName, theme, id));
 
 		if(m_index < 0)
 			nextExample();
 
 	}
 	
 	public void addLetterSoundResource(String soundName) {
 		m_LetterSound = new GameSound(soundName);
 	}
 	
 	public void addPhonicSoundResource(String soundName) {
 		m_PhonicSound = new GameSound(soundName);
 	}
 	
 	/**
 	 * Gets the WordPictureSound object stored at the provided
 	 * list index.
 	 * 
 	 * @param index   The index into the list.
 	 * @return        The WordPictureSound object or null if
 	 *                the index is invalid.
 	 */
 	private WordPictureSound getWPSData(int index)
 	{
 		if (index >= 0 && index < m_wps.size())
 			return m_wps.get(index);
 		return null;
 	}
 
 	
 	/**
 	 * Gets the text of the current word.
 	 * 
 	 * @return   The text of the current word as a String.
 	 */
 	public String getWord()
 	{
 		WordPictureSound wps = getWPSData(m_index);
 		if (wps == null)
 			return null;
 		return wps.GetWordString();
 	}
 
 	
 	/**
 	 * Gets the image associated with the current word.
 	 * 
 	 * @return   The image associated with the current word.
 	 */
 	public Icon getIcon(int width, int height) {
 		WordPictureSound wps = getWPSData(m_index);
 		if (wps == null)
 			return null;
 		return wps.GetWordImage(width, height);
 	}
 
 	
 	/**
 	 * Advances the word list index and notifies
 	 * observers that the word example has changed.
 	 */
 	public void nextExample() {
 		int iWpsSize = m_wps.size();
 		int iStartIndex;
 		Theme curTheme = (m_themeMgr == null) ? null : m_themeMgr.getCurrentTheme();
 		Theme theme;
 		
 		setChanged();
 		
 		//If there are no words for the current letter
 		if(iWpsSize <= 0)
 		{
 			m_index = -1;
 			notifyObservers(this);
 			return;
 		}
 		
 		//Make sure that we are iterate though valid indices.
 		//m_index may have been previously set to -1 if there were no valid
 		//words for the current theme.
 		if(m_index < 0 || m_index >= iWpsSize)
 			iStartIndex = m_index = iWpsSize-1;
 		else
 			iStartIndex = m_index;
 		
 		do
 		{
 			//Advance Index
 			m_index++;
			if (m_index >= iWpsSize || m_index >= Settings.getMaxExamples());
 				m_index = 0;
			
 			//Break out of the loop if there is no current theme
 			if(curTheme == null)
 				break;
 			else
 				theme = getWPSData(m_index).getTheme();
 			
 			//If we have iterated through the entire collection
 			if(m_index == iStartIndex)
 			{
 				//If we have not found a word that is part of the current theme.
 				if(theme != curTheme)
 					m_index = -1;
 				break;
 			}
 		} while(theme != curTheme); //While the word is not part of the current theme
 		
 		notifyObservers(this);
 	}
 
 	
 	/**
 	 * Plays the sound associated with the current word.
 	 */
 	public void playSound() {
 		WordPictureSound wps = getWPSData(m_index);
 		if (wps == null)
 			return;
 		curr_sound = Sound_Type.WPS;
 		wps.PlaySound();
 	}
 	
 	public void playSoundLetter() {
 		if (m_LetterSound == null)
 			return;
 		curr_sound = Sound_Type.LETTER;
 		m_LetterSound.PlaySound();
 	}
 	
 	public void playSoundPhonic() {
 		if (m_PhonicSound == null)
 			return;
 		curr_sound = Sound_Type.PHONIC;
 		m_PhonicSound.PlaySound();
 	}
 	
 	public void stopSound() {
 		switch(curr_sound) {
 		case NONE:
 			return;
 		case WPS:
 			WordPictureSound wps = getWPSData(m_index);
 			if(wps != null)
 				wps.StopSound();
 			curr_sound = Sound_Type.NONE;
 			return;
 		case LETTER:
 			m_LetterSound.StopSound();
 			curr_sound = Sound_Type.NONE;
 			return;
 		case PHONIC:
 			m_PhonicSound.StopSound();
 			curr_sound = Sound_Type.NONE;
 			return;
 		}
 	}
 
 	public void removeAllEntries() {
 		while (m_wps.size() > 0) {
 			m_wps.remove(0);
 		}
 	}
 
 	
 	
 	@Override
 	public void update(Observable o, Object arg) {
 
 	    if(o == null)
 	        return;
 	    else if(o == m_themeMgr)
 	    {
 	        //There was a change to the theme model.
 	        //The current theme was possibly changed
 	        //or a theme may have been deleted.
 	       
 	        Iterator<WordPictureSound> it = m_wps.iterator();
 	        WordPictureSound wps;
 	        Theme defaultTheme = m_themeMgr.getTheme(Theme.DEFAULT_THEME_NAME);
 	        Theme theme;
 	       
 	        //If theme was deleted, set WPS theme references to the default
 	        //theme if they originally pointed to deleted theme object.
 	        if(defaultTheme != null)
 	        {
 	            while(it.hasNext())
 	            {
 	                wps = it.next();
 	                theme = wps.getTheme();
 	               
 	                if(m_themeMgr.hasTheme(theme.getThemeName()) == false)
 	                    wps.changeTheme(defaultTheme);
 	            }
 	        }
 	       
 	        //Get another word example since existing word may
 	        //no longer be valid for current theme selection.
 	        m_index = -1;
 	        nextExample();
 	    }
 	}
 	
 	
 	
 	public int removeResource(WordPictureSound wps) {
 		boolean reset_index = false;
 		
 		/* If current word is being deleted, reset the index */
 		if (wps.GetWordString().equals(getWord())) {
 			reset_index = true;
 		}
 		
 		wps.getTheme().decRefCount();
 		m_wps.remove(wps);
 		
 		if (reset_index == true) {
 			m_index = -1;
 			nextExample();
 		}
 		return 0;
 	}
 
 
 	/**
 	 * Shuffle the list of words/examples
 	 */
 	public void shuffleList() {
 		log.info("Shuffle the list");
 		Collections.shuffle(m_wps);
 		
 	}
 	
 	/**
 	 * Sort the list of words/examples
 	 */
 	public void sortList(){
 		log.info("sort the list");
 		Collections.sort(m_wps);
 	}
 	
 	/**
 	 * Reset default order of the examples as they were initially loaded/added to the list
 	 */
 	public void resetDeafultOrder(){
 		log.info("reset order of the list");
 		Collections.sort(m_wps, WordPictureSound.compareById);
 	}
 }
