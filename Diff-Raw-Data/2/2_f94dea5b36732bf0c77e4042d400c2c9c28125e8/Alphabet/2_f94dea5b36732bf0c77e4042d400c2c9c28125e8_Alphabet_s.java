 package edu.bu.cs673.AwesomeAlphabet.model;
 import java.io.File;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import edu.bu.cs673.AwesomeAlphabet.main.AAConfig;
 
 
 /**
  * The class defines the Alphabet model.  It creates and
  * maintains references to 26 Letter objects; one for each
  * letter of the English alphabet.  It also maintains the
  * current letter selection and has methods for getting
  * the Letter object, changing the letter selection, and
  * loading resources.
  */
 public class Alphabet extends Observable {
 
 	protected static final int AA_ALPHABET_SIZE	= 26;
 	protected static Logger log = Logger.getLogger(Alphabet.class);
 	
 	private Letter[] m_letters = new Letter[AA_ALPHABET_SIZE];
 	public int m_iCurLetterIndex;
 	private GameSound m_alphabetsong;
 	private ThemeManager m_themeMgr;
 	private Database m_db;
 	private String m_currentWordEditing;
 	
 	/* 
 	 * A cache of all word strings. This will not be in sync with any changes to model.
 	 * So every time it should be flushed and populated fresh to get the latest
 	 * list
 	 */
 	private List<String> m_word_cache = new LinkedList<String>();
 	
 	
 	/**
 	 * Class constructor.  
 	 */
 	public Alphabet(ThemeManager themeMgr)
 	{
 		m_themeMgr = themeMgr;
 		Initialize();
 	}
 	
 	
 	/**
 	 * Responsible for creating the Letter objects.
 	 */
 	public void Initialize()
 	{
 		m_db = Database.getDatabaseInstance();
 
 		for (int i=0; i<AA_ALPHABET_SIZE; i++) {
 			if (m_letters[i] != null)
 				m_letters[i].removeAllEntries();
 			m_letters[i] = new Letter((char)((int)'a' + i), m_themeMgr);
 		}
 	}
 	
 	
 	/**
 	 * Gets the array index of the specified letter.
 	 * 
 	 * @param c   The letter, represented as a char.
 	 * @return    The index of the letter in the array or
 	 *            -1 if the letter is invalid.
 	 */
 	public int GetLetterIndex(char c)
 	{
 		char cTemp = Character.toLowerCase(c);
 		
 		if(cTemp < 'a' || cTemp > 'z')
 			return -1;
 		else
 			return (int)cTemp - (int)'a';		
 	}
 	
 	
 	/**
 	 * Gets an iterator to the list of Letter objects.
 	 * 
 	 * @return   An iterator to the list of Letter objects. 
 	 */
 	public Iterator<Letter> GetIterator()
 	{
 		return Arrays.asList(m_letters).iterator();
 	}
 	
 	/**
 	 * Gets an iterator to the list of String objects.
 	 * 
 	 * @return   An iterator to the list of String objects. 
 	 */
 	public Iterator<String> GetWordCacheIterator()
 	{
 		Iterator<Letter> iter_letter = GetIterator();
 		Iterator<WordPictureSound> iter_wps;
 		WordPictureSound wps;
 		
 		/* Flush existing cache */
 		while(!m_word_cache.isEmpty())
 			m_word_cache.remove(0);
 		
 		/* Populate list again */
 		while (iter_letter.hasNext()) {
 			Letter l = iter_letter.next();
 			iter_wps = l.GetIterator();
 			while (iter_wps.hasNext()) {
 				wps = iter_wps.next();
 				m_word_cache.add(wps.GetWordString());
 			}
 		}
 		return m_word_cache.listIterator();
 	}
 	
 	public Iterator<String> GetWordCacheIterator(String regex)
 	{
 		Iterator<Letter> iter_letter = GetIterator();
 		Iterator<WordPictureSound> iter_wps;
 		WordPictureSound wps;
 		
 		if (regex.isEmpty())
 			return GetWordCacheIterator();
 		
 		/* Flush existing cache */
 		while(!m_word_cache.isEmpty())
 			m_word_cache.remove(0);
 		
 		/* Populate list again */
 		while (iter_letter.hasNext()) {
 			Letter l = iter_letter.next();
 			iter_wps = l.GetIterator();
 			while (iter_wps.hasNext()) {
 				wps = iter_wps.next();
 				if (wps.GetWordString().startsWith(regex))
 					m_word_cache.add(wps.GetWordString());
 			}
 		}
 		return m_word_cache.listIterator();
 	}
 	
 	/**
 	 * Sets the letter selection.
 	 * 
 	 * @param letter   The letter to select.
 	 * @return         A reference to the newly selected Letter
 	 *                 object or null if the selection was invalid.
 	 */
 	public Letter SetCurrentLetter(Letter letter)
 	{
 		int iIndex;
 		
 		if(letter == null)
 			return null;
 		
 		iIndex = GetLetterIndex(letter.GetLetterAsChar());
 		
 		if(iIndex < 0 || iIndex > m_letters.length - 1)
 			return null;
 		else
 		{
 			m_iCurLetterIndex = iIndex;
 			setChanged();
 			notifyObservers(m_letters[m_iCurLetterIndex]);
 			return m_letters[m_iCurLetterIndex];
 		}
 	}
 	
 	
 	/**
 	 * Returns the selected letter.
 	 * 
 	 * @return   The selected letter.
 	 */
 	public Letter GetCurrentLetter()
 	{
 		//log.info("Current Letter set to " + m_letters[m_iCurLetterIndex]);
 		return m_letters[m_iCurLetterIndex];
 	}
 	
 	
 	/**
 	 * Changes letter selection to previous letter.
 	 * 
 	 * @return   The previous letter object or null
 	 *           if there are no previous letters.
 	 */
 	public Letter GoToPreviousLetter()
 	{
 		if(m_iCurLetterIndex <= 0)
 			return null;
 		else
 		{
 			m_iCurLetterIndex -= 1;
 			setChanged();
 			notifyObservers(m_letters[m_iCurLetterIndex]);
 			return m_letters[m_iCurLetterIndex];
 		}
 	}
 	
 	
 	/**
 	 * Changes letter selection to next letter.
 	 * 
 	 * @return   The next letter object or null
 	 *           if there are no more letters.
 	 */
 	public Letter GoToNextLetter()
 	{
 		if(m_iCurLetterIndex >= m_letters.length - 1)
 			return null;
 		else
 		{
 			m_iCurLetterIndex += 1;
 			setChanged();
 			notifyObservers(m_letters[m_iCurLetterIndex]);
 			return m_letters[m_iCurLetterIndex];
 		}
 	}
 	
 	
 	
 	/**
 	 * Loads word, picture, and sound resources into Letter objects.
 	 * 
 	 * @param prop   The property list containing resource information.
 	 */
 	public void LoadResources(Properties prop) {
 		
 		//if(m_themeMgr == null)
 		//	return;
 		boolean reload_db = true;
 		int nr_rows = 0;
 		
 		/* This is a hack to detect if database should be reloaded or not */
 		nr_rows = m_db.getNumberRowsWordTable();
 		log.info("Number of rows in Word Table=" + nr_rows);
 		if (nr_rows > 0)
 			reload_db = false;
 		
 		/* Parse letter.properties and populate database */
 		for (char c = 'a'; c <= 'z'; c++) {
 			Letter letter = m_letters[GetLetterIndex(c)];
 
 			for (int i = 1; i <= 10; i++) {
 				String propName = "letter." + c + "." + i + ".";
 				try {
 					String wordText = prop.getProperty(propName + "word");
 					
 					if (wordText == null)
 						break;
 					
 					String imageName = wordText + ".jpg";
 					String soundName = wordText + ".wav";
 					String themeName = prop.getProperty(propName + "theme");
 					
 					if(themeName == null) {
 						themeName = Theme.DEFAULT_THEME_NAME;
 					}
 						
 					if(reload_db && !m_themeMgr.addTheme(themeName))
 						throw new Exception("Error adding theme.");
 				
 					
 					if (reload_db && !m_db.addWord(wordText, imageName, soundName, c, themeName))
 						throw new Exception("Error adding word to database.");
 					letter.addResource(imageName, soundName, wordText, 
 								m_themeMgr.getTheme(themeName));
 				} catch (Exception e) {
 					log.error("An exception occurred while loading properties for leter "+c);
 					log.error(e.getMessage());
 					e.printStackTrace();
 				}
 			}
 			
 			log.info("Add Letter Sound");
 			try {
 				//String propName = "letter." + c + ".lettersound";
 				//String letterSoundName = prop.getProperty(propName);
 				String letterSoundName = c + ".wav";
 				if (letterSoundName != null)
 					letter.addLetterSoundResource(letterSoundName);
 			} catch (Exception e) {
 				log.error("An exception occurred while getting the letter sound for letter " + c);
 				log.error(e.getMessage());
 				e.printStackTrace();
 			}
 			// log.info("Add Phonic Sound");
 			try {
 				String phonicSoundName = c + "phonics.wav";
 				if (phonicSoundName != null)
 					letter.addPhonicSoundResource(phonicSoundName);
 			} catch (Exception e) {
 				log.error("An exception occurred while getting the phonice sound for letter " + c);
 				log.error(e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		
 		// log.info("Load alphabet song");
 		try {
 			String soundName = prop.getProperty("alphabetsong");
 			if (soundName != null) {
 				m_alphabetsong = new GameSound(soundName);
 			}
 		} catch (Exception e) {
 			log.error("An exception occurred while loading the alphabet song ");
 			log.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	public void PlayAlphabetSong() {
 		m_alphabetsong.PlaySound();
 	}
 	
 	public void StopAlphabetSound() {
 		m_alphabetsong.StopSound();
 	}
 	
 	/**
 	 * Given a word, return matching WordPictureSound object, if it exists.
 	 * @param word to match
 	 * @return WordPictureSound object associated with word 
 	 */
 	 
 	public WordPictureSound getWordPictureSound(String word) {
 		// Traverse through all WordPictureSound objects to find a match
 		Iterator<Letter> iter_letter = GetIterator();
 		Iterator<WordPictureSound> iter_wps;
 		WordPictureSound wps;
 		
 		while (iter_letter.hasNext()) {
 			Letter l = iter_letter.next();
 			iter_wps = l.GetIterator();
 			while (iter_wps.hasNext()) {
 				wps = iter_wps.next();
				if (word == wps.GetWordString())
 					return wps;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Add a new word
 	 * @return 0 on success. Failure otherwise. 
 	 */
 	 
 	public int addNewWord(String wordText, char associatedLetter, String imageName, String soundName, String themeName) {
 		int letter_index = GetLetterIndex(associatedLetter);
 		Letter letter = m_letters[letter_index];
 		WordPictureSound wps;
 		
 		//Verify new word does not exist already.
 		wps = getWordPictureSound(wordText);
 		if (wps != null)
 			return 1;
 		
 		// Add sound and image files to resource dir.
 		AAConfig.addSoundResource(soundName, wordText + ".wav");
 		AAConfig.addImageResource(imageName, wordText + ".jpg");
 				
 		log.info("Add word word=" + wordText + " letter=" + associatedLetter + " image=" + wordText + ".jpg" + " sound=" + wordText + ".wav" + " theme=" + themeName);
 		m_db.addWord(wordText, wordText + ".jpg", wordText + ".wav", associatedLetter, themeName);
 		AAConfig.addWordToIndex(associatedLetter, wordText, themeName);
 		
 		letter.addResource(wordText + ".jpg", wordText + ".wav", wordText, m_themeMgr.getTheme(themeName));
 		return 0;
 	}
 	
 	/**
 	 * delete a word
 	 * @return 0 on success. Failure otherwise. 
 	 */
 	 
 	public int deleteWord(String wordText) {
 		char letter_c;
 		int letter_index;
 		Letter letter;
 		WordPictureSound wps;
 		
 		
 		wps = getWordPictureSound(wordText);
 		if (wps == null)
 			return 1;
 		
 		log.info("Delete Word: " + wordText);
 		letter_c = wps.getWordLetter();
 		letter_index = GetLetterIndex(letter_c);
 		letter = m_letters[letter_index];
 		
 		// Remove sound and image files from resource dir.
 		AAConfig.removeSoundResource(wordText + ".wav");
 		AAConfig.removeImageResource(wordText + ".jpg");
 		m_db.deleteWord(wordText);
 		AAConfig.removeWordFromIndex(letter_c, wordText);
 		
 		letter.removeResource(wps);
 		return 0;
 	}
 
 	/**
 	 * Edit properties of existing word
 	 * @return 0 on success. Failure otherwise. 
 	 */
 	 
 	public int editWord(String wordText, char associated_letter, String imageName, String soundName, String themeName) {
 		WordPictureSound old_wps;
 		String soundDir, imageDir, srcSoundFile, srcImageFile, absSoundDir, absImageDir;
 		File currentdir = new File(".");
 		
 		log.info("Edit word: " + wordText +  " image=" + imageName + " sound=" + soundName + " theme=" + themeName);
 		old_wps = getWordPictureSound(m_currentWordEditing);
 		if (old_wps == null) {
 			log.error("There is no current word being edited");
 			return 1;
 		}
 		
 		try {
 			/* If sound and Image file names have not changed, save these away
 			 * otherwise these will be deleted upon delete word. Save these away.
 			 */
 			soundDir = AAConfig.getSoundResourceDir() + "/";
 			imageDir = AAConfig.getGraphicsResourceDir() + "/";
 			
 			absSoundDir = currentdir.getCanonicalPath() + "/" + soundDir + "/";
 			absImageDir = currentdir.getCanonicalPath() + "/" + imageDir + "/";
 		
 			srcSoundFile = absSoundDir + old_wps.GetWordString() + ".wav";
 			srcImageFile = absImageDir + old_wps.GetWordString() + ".jpg";
 			
 			AAConfig.copy_file(srcSoundFile, soundDir + "temp.wav");
 			AAConfig.copy_file(srcImageFile, imageDir +"temp.jpg");
 		
 			deleteWord(old_wps.GetWordString());
 			addNewWord(wordText, associated_letter, absImageDir + "temp.jpg", absSoundDir + "temp.wav", themeName);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 	
 	public boolean setCurrentWordEditing(String wordText) {
 		WordPictureSound wps;
 		
 		wps = getWordPictureSound(wordText);
 		
 		if (wps == null)
 			return false;
 		
 		m_currentWordEditing = wordText;
 		return true;
 		
 	}
 	
 	public boolean unsetCurrentWordEditing() {
 		m_currentWordEditing = null;
 		return true;
 	}
 	
 	public WordPictureSound getCurrentWordEditing() {
 		if (m_currentWordEditing == null)
 			return null;
 		return getWordPictureSound(m_currentWordEditing);
 	}
 	
 	public String getAbsImageFilePath(String wordText)
 	{
 		File currentdir = new File(".");
 		String imageDir, absImageDir = null;
 		
 		try {
 			imageDir = AAConfig.getGraphicsResourceDir() + "/";
 			absImageDir = currentdir.getCanonicalPath() + "/" + imageDir + "/";
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		if (absImageDir != null)
 			return absImageDir + wordText + ".jpg";
 		else
 			return null;
 	}
 	
 	public String getAbsSoundFilePath(String wordText)
 	{
 		File currentdir = new File(".");
 		String soundDir, absSoundDir = null;
 		
 		try {
 			soundDir = AAConfig.getSoundResourceDir() + "/";
 			absSoundDir = currentdir.getCanonicalPath() + "/" + soundDir + "/";
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		if (absSoundDir != null)
 			return absSoundDir + wordText + ".wav";
 		else
 			return null;
 	}
 }
