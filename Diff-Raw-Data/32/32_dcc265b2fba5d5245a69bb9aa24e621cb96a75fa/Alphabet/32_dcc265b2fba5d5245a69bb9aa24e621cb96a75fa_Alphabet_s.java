 package edu.bu.cs673.AwesomeAlphabet.model;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Properties;
 
 
 /**
  * The class defines the Alphabet model.  It creates and
  * maintains references to 26 Letter objects; one for each
  * letter of the English alphabet.  It also maintains the
  * current letter selection and has methods for getting
  * the Letter object, changing the letter selection, and
  * loading resources.
  */
 public class Alphabet extends Observable {
 
 	private Letter[] m_letters = new Letter[26];
 	public int m_iCurLetterIndex;
 	private GameSound m_alphabetsong;
 	
 	
 	/**
 	 * Class constructor.  Responsible for creating the
 	 * Letter objects.
 	 */
 	public Alphabet()
 	{
 		for(int i=0; i<26; i++)
 			m_letters[i] = new Letter((char)((int)'a' + i));
 	}
 	
 	
 	/**
 	 * Gets the array index of the specified letter.
 	 * 
 	 * @param c   The letter, represented as a char.
 	 * @return    The index of the letter in the array or
 	 *            -1 if the letter is invalid.
 	 */
 	private int GetLetterIndex(char c)
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
 		for (char c = 'a'; c <= 'z'; c++) {
 			for (int i = 1; i <= 10; i++) {
 				String propName = "letter." + c + "." + i + ".";
 				try {
 					String imageName = prop.getProperty(propName + "image");
 					String soundName = prop.getProperty(propName + "sound");
 					String wordText = prop.getProperty(propName + "word");
 					if (imageName == null && soundName == null && wordText == null)
 						break;
 					m_letters[GetLetterIndex(c)].addResource(imageName, soundName, wordText);
 				} catch (Exception e) {
 					i = 10;
 				}
 			}
 		}
 		// Load alphabet song
 		try {
 			String soundName = prop.getProperty("alphabetsong");
 			if (soundName != null) {
 				m_alphabetsong = new GameSound(soundName);
 			}
 		} catch (Exception e) {
 			
 		}
 		
 	}
 	public void PlayAlphabetSong() {
 		m_alphabetsong.PlaySound();
 	}
 }
