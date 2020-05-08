 package edu.bu.cs673.AwesomeAlphabet.controller;
 import java.awt.image.BufferedImage;
 
 import edu.bu.cs673.AwesomeAlphabet.model.Alphabet;
 import edu.bu.cs673.AwesomeAlphabet.model.IPageObserver;
 import edu.bu.cs673.AwesomeAlphabet.model.Letter;
 import edu.bu.cs673.AwesomeAlphabet.model.PageName;
 import edu.bu.cs673.AwesomeAlphabet.view.LetterPageView;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 
 /**
 * This class defines the Alphabet Page Controller.
  */
 public class LetterPageController extends PageController {
 
 	private LetterPageView m_view;
 	private Alphabet m_alphabet;
 	
 	
 	/**
 	 * Class constructor.
 	 * 
 	 * @param pageObserver   A page observer reference so that page
 	 *                       transitions may be requested.  For example,
 	 *                       this may refer to the main window.
 	 * @param view           The view.
 	 * @param alphabet       The Alphabet model.
 	 */
 	public LetterPageController(IPageObserver pageObserver, LetterPageView view, Alphabet alphabet) {
 		super(pageObserver);
 		
 		m_view = view;
 		m_alphabet = alphabet;
 		
 		m_view.SetController(this);
 		
 		alphabet.addObserver(m_view);
 		alphabet.GetCurrentLetter().addObserver(m_view);
 	}
 
 	
 	/**
 	 * This method is used to notify the controller that the image
 	 * of the letter was clicked.  This will cause a phonetic
 	 * letter sound to be played.
 	 */
 	public void LetterClicked()
 	{
 		//Play letter sound
 		throw new NotImplementedException();
 	}
 	
 	
 	/**
 	 * This method is used to notify the controller that the word's
 	 * picture was clicked.  This will cause the word sound to be
 	 * played.
 	 */
 	public void PictureClicked()
 	{
 		Letter letter = m_alphabet.GetCurrentLetter();
 		letter.playSound();
 	}
 	
 	
 	/**
 	 * This method is used to notify the controller that the
 	 * word was clicked.  This will cause the word sound to be
 	 * played.
 	 */
 	public void WordClicked()
 	{
 		//Play picture/word sound
 		throw new NotImplementedException();
 	}
 	
 	
 	/**
 	 * This method causes the controller to update the Alphabet
 	 * model so that the next letter is displayed.
 	 * 
 	 * @return   True if next letter can be displayed.
 	 */
 	public boolean GetNextLetter()
 	{
 		Letter letter = m_alphabet.GetCurrentLetter();
 		Letter nextLetter = m_alphabet.GoToNextLetter();
 		
 		if(nextLetter == null)
 			return false;
 		
 		letter.deleteObserver(m_view);
 		nextLetter.addObserver(m_view);
 		return true;
 	}
 	
 	
 	/**
 	 * This method causes the controller to update the Alphabet
 	 * model so that the previous letter is displayed.
 	 * 
 	 * @return   True if previous letter can be displayed.
 	 */
 	public boolean GetPreviousLetter()
 	{
 		Letter letter = m_alphabet.GetCurrentLetter();
 		Letter prevLetter = m_alphabet.GoToPreviousLetter();
 		
 		if(prevLetter == null)
 			return false;
 		
 		letter.deleteObserver(m_view);
 		prevLetter.addObserver(m_view);
 		return true;	
 	}
 	
 	
 	/**
 	 * Causes the Alphabet Page to be shown.
 	 */
 	public void GoToAlphabetPage()
 	{
 		if(GoToPage(PageName.AlphabetPage))
 			m_alphabet.GetCurrentLetter().deleteObserver(m_view);
 	}
 	
 	
 	/**
 	 * Causes the Title Page to be shown.
 	 */
 	public void GoToTitlePage()
 	{
 		if(GoToPage(PageName.TitlePage))
 			m_alphabet.GetCurrentLetter().deleteObserver(m_view);
 	}
 	
 	
 	/**
 	 * This method causes the controller to update the current
 	 * Letter model so that the next word example is shown.
 	 */
 	public void GetNextExample()
 	{
 		Letter letter = m_alphabet.GetCurrentLetter();
 		letter.nextExample();
 	}
 	
 	
 	/**
 	 * This method gets the current letter from the model.
 	 * 
 	 * @return    Current letter as a lower-case char.
 	 */
 	public char GetLetterAsChar()
 	{
 		return m_alphabet.GetCurrentLetter().GetLetterAsChar();
 	}
 	
 	
 	/**
 	 * This method gets the current word picture from the 
 	 * Letter model.
 	 * 
 	 * @return    Word picture as a BufferedImage.
 	 */
 	public BufferedImage GetPicture()
 	{
 		throw new NotImplementedException();
 	}
 	
 	
 	/**
 	 * This method gets the current word from the 
 	 * Letter model.
 	 * 
 	 * @return    Letter word as a String.
 	 */
 	public String GetWord()
 	{
 		throw new NotImplementedException();	
 	}
 }
