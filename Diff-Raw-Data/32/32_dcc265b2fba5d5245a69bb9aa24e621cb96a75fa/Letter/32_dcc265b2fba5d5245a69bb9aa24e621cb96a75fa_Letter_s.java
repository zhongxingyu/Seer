 package edu.bu.cs673.AwesomeAlphabet.model;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 
 import javax.swing.Icon;
 
 
 /**
  * This class defines the Letter model.  It represents a
  * letter in the English alphabet and contains a list
  * words that start with the letter, along with associated
  * pictures and sounds.  An index is maintained for the
  * word list to keep track of which word was most recently
  * shown.
  */
 public class Letter extends Observable {
 
 	private char m_cLetter;
 	private List<WordPictureSound> m_wps = new LinkedList<WordPictureSound>();
 	private int index = 0;
 	
 	
 	/**
 	 * Class constructor.
 	 * 
 	 * @param cLetter   The letter that this object will represent.
 	 */
 	public Letter(char cLetter) {
 	
 		m_cLetter = Character.toLowerCase(cLetter);
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
 	 * Adds a new WordPictureSound object to the word list.
 	 * 
 	 * @param imageName    The filename of the word image.
 	 * @param soundName    The filename of the word sound.
 	 * @param wordText     The text of the word.
 	 */
 	public void addResource(String imageName, String soundName, String wordText) {
 		m_wps.add(new WordPictureSound(wordText, imageName, soundName));
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
 		if (m_wps.size() > index)
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
 		WordPictureSound wps = getWPSData(index);
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
 		WordPictureSound wps = getWPSData(index);
 		if (wps == null)
 			return null;
 		return wps.GetWordImage(width, height);
 	}
 
 	
 	/**
 	 * Advances the word list index and notifies
 	 * observers that the word example has changed.
 	 */
 	public void nextExample() {
 		index++;
 		if (index >= m_wps.size())
 			index = 0;
 		setChanged();
 		notifyObservers(this);
 	}
 
 	
 	/**
 	 * Plays the sound associated with the current word.
 	 */
 	public void playSound() {
 		WordPictureSound wps = getWPSData(index);
 		if (wps == null)
 			return;
 		wps.PlaySound();
 	}
 }
