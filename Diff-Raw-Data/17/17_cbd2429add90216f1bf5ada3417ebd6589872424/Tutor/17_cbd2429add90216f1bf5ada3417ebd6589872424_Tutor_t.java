 /**
  * ---------------------------------------------------------------------------
  * File name: Tutor.java<br/>
  * Project name: TypingTutor<br/>
  * ---------------------------------------------------------------------------
  * Creator's name and email: Matthew Paul, paulmr@goldmail.etsu.edu<br/>
  * Course:  CSCI 1260-088<br/>
  * Creation Date: Apr 16, 2012<br/>
  * Date of Last Modification: Apr 16, 2012
  * ---------------------------------------------------------------------------
  */
 
 package core;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 import front.Gui;
 
 
 /**
  * Contains the code to handle the collection of words<br>
  *
  * <hr>
  * Date created: Apr 16, 2012<br>
  * Date last modified: Apr 16, 2012<br>
  * <hr>
  * @author Matthew Paul
  */
 public class Tutor
 {
 //////////////////
 //	
 //FIELDS
 //	
 //////////////////
 //	How many points are deducted from the score for every error
 	protected static final int	ERROR_DEDUCTION	= 30;
 
 //	how many lives to start out with
 	protected static final int	DEFAULT_LIVES_LEFT	= 3;
 
 //	how many errors to start out with
 	protected static final int	DEFAULT_ERRORS	= 0;
 
 //	the score to start at
 	protected static final int 	DEFAULT_SCORE = 0;
 
 //	how many points the player gets for clearing a word
 	protected static final int	CORRECT_WORD_SCORE	= 100;
 
 //	a list of words that are on the screen
 	protected ArrayList<Word> wordsOnScreen,
 	
 //	a list of words that have the valid inputs. These are the ones that can be typed, given what was typed before
 							acceptableWords,
 							
 //	the list of words that have been successfully cleared
 							clearedWords;
 	
 //	the number of times a word can reach the end without being typed. After each word, it is decremented. When it's less than 1, game over
 	protected int livesLeft;
 	
 //	Score for the player
 	protected int score;
 	
 //	the number of incorrectly typed characters
 	protected int errors;
 	
 //	the buffer containing the typed characters
 	protected StringBuffer buffer;
 	
 //	the wordpool from which the game draws its words
 	protected WordPool pool;
 	
 //	a count of how many frames have passed since the game has started
 	protected int frameCount;
 	
 //	Word that was killed in the last update cycle. This mostly null. It is set to a word to do drawings but is reset to null afterwards
 	protected Word killedWord;
 	
 //	true if the game is over
 	protected boolean gameOver;
 	
 //	the words per minute of the player. Based on standard 5 character word
 	protected int wpm;
 
 //	true if player won the game
 	public boolean	win;
 	
 	
 	
 //////////////////
 //	
 //CONSTRUCTORS
 //	
 //////////////////
 	
 	
 	
 	/**
 	 * Full Constructor <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 17, 2012 <br>
 	 * Date last modified: Apr 17, 2012 <br>
 	 *
 	 * <hr>
 	 * @param wordsOnScreen - an arrayList containing the words that are currently on the screen
 	 * @param acceptableWords - an arrayList containing the words that are still acceptable
 	 * @param clearedWords - an arrayList containing the words that have been cleared
 	 * @param livesLeft - the number of times a word can reach the end point before game over
 	 * @param errors - the number of times the player has made an error
 	 */
 	public Tutor(ArrayList<Word> wordsOnScreen,
 	             ArrayList<Word> acceptableWords,
 	             ArrayList<Word> clearedWords,
 	             int livesLeft,
 	             int errors,
 	             StringBuffer buffer)
 	{
 		this.wordsOnScreen = new ArrayList<Word>(wordsOnScreen);
 		this.acceptableWords = new ArrayList<Word>(acceptableWords);
 		this.clearedWords = new ArrayList<Word>(clearedWords);
 		
 		this.livesLeft = livesLeft;
 		this.errors = errors;
 		
 		this.buffer = new StringBuffer(buffer);
 		
 		FileOperator file = new FileOperator( );
 		
 		String [ ] lines = null;
 		
 		try
 		{
 			lines = file.read(FileOperator.WORD_FILE);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		this.pool = createWordPool(lines);
 		
 		file = null;
 	}
 	
 	protected WordPool createWordPool(String[] lines)
 	{
 		Word[] words = new Word[lines.length];
 		
 		Random r = new Random();
 		
 		for (int c = 0; c < words.length; c++)
 		{
 			words[c] = new Word(lines[c], r.nextInt(600), 0);
 		}
 		
 		return new WordPool(words);
 	}
 	
 	
 	/**
 	 * Constructor <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 18, 2012 <br>
 	 * Date last modified: Apr 18, 2012 <br>
 	 *
 	 * <hr>
 	 * @param array a String array containing the starting words
 	 */
 	public Tutor(String[] array)
 	{
 		this(new ArrayList<Word>(), 
 			new ArrayList<Word>(), 
 			new ArrayList<Word>(),
 			DEFAULT_LIVES_LEFT,
 			DEFAULT_ERRORS,
 			new StringBuffer(""));
 		
 		ArrayList<Word> list = new ArrayList<Word>();
 		
 		for (String word: array)
 		{
 			list.add(new Word(word, 0, 0));
 		}
 		
 		this.wordsOnScreen = list;
 	}
 	
 	/**
 	 * No-Arg Constructor <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 17, 2012 <br>
 	 * Date last modified: Apr 17, 2012 <br>
 	 *
 	 * <hr>
 	 */
 	public Tutor()
 	{
 		this(new ArrayList<Word>(), 
 			new ArrayList<Word>(), 
 			new ArrayList<Word>(),
 			DEFAULT_LIVES_LEFT,
 			DEFAULT_ERRORS,
 			new StringBuffer(""));
 	}
 	
 	
 //////////////////
 //
 //METHODS
 //
 //////////////////
 	
 	/**
 	 * Processes the character that the user has typed. <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 18, 2012 <br>
 	 * Date last modified: Apr 18, 2012 <br>
 	 *
 	 * <hr>
 	 * @param key a KeyEvent generated by the keyboard
 	 */
 	public void processCharacter(KeyEvent key)
 	{
 //		get the character from the KeyEvent
 		char character = key.getKeyChar( );
 		
 //		assume the character was not found
 		boolean characterFound = false;
 		
 //		stick the character into the buffer
 		buffer.append(character);
 		
 //		for every word in the acceptable word list
 		for (int c = 0; c < acceptableWords.size( ); c++)
 		{
 //			if the character equals the next character in the word from the list
 			if (character == acceptableWords.get(c).getNextChar( ))
 			{
 //				the character has been found
 				characterFound = true;
 				
 //				Tell the word that they have had a correct character typed, so move pointer up one character
 				acceptableWords.get(c).advanceCharacter( );
 				
 //				if the character typed finished the word
 				if(acceptableWords.get(c).isCleared( ))
 				{
 //					add the correct amount to the score
 					score += CORRECT_WORD_SCORE;
 					
 					killedWord = acceptableWords.get(c);
 					
 //					remove the word from the words on the screen and add another from the word pool
 					wordsOnScreen.remove(acceptableWords.get(c));
 					
 					if(pool.hasMoreWords( ))
 						wordsOnScreen.add(pool.getNextWord( ));
 					
 //					add the word to the list of cleared words
 					clearedWords.add(acceptableWords.get(c));
 					
 //					remove the word from the list of acceptable words
 					acceptableWords.remove(c);
 					
 //					set the acceptable words to all the words on the screen
 					resetAcceptableWords( );
 				}
 			}
 		}
 		
 //		if, by the end of the loop, the character was not found
 		if(!characterFound)
 		{
 //			increment error
 			errors++;
 			
 //			delete the wrong key from the buffer
 			buffer.deleteCharAt(buffer.length( ) - 1);
 		}
 		
 //		for every word in the acceptable word list
 		for (int i = 0; i < acceptableWords.size( ); i++)
 		{
 //			if the word's already typed characters does not equal what's in the buffer
 			if (!acceptableWords.get(i).getClearedString( ).equals(buffer.toString( )))
 			{
 //				reset the word that is being removed
 				acceptableWords.get(i).reset( );
 				
 //				remove the word from the list of acceptable words
 				acceptableWords.remove(i);
 //				decrement the iterating variable to keep subscripts in the correct place
 				i--;
 			}
 		}
 	}
 
 	/**
 	 * Reset the characters that have been typed so far. <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 21, 2012 <br>
 	 * Date last modified: Apr 21, 2012 <br>
 	 *
 	 * <hr>
 	 */
 	
 	protected void resetAcceptableWords()
 	{
 		acceptableWords = new ArrayList<Word>(wordsOnScreen);
 		
 //		for every word in the acceptable word list
 		for (Word word: acceptableWords)
 		{
 //			reset the word's pointer back to 0
 			word.reset( );
 		}
 		
 //		clear the buffer
 		buffer.delete(0, buffer.length( ));
 	}
 	
 	/**
 	 * Draws the game. Meant to be overridded by the child class. <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 17, 2012 <br>
 	 * Date last modified: Apr 17, 2012 <br>
 	 *
 	 * <hr>
 	 * @param g The graphics object to be written to
 	 * @param deltaTime The time elapsed since the last frame
 	 */
 	public void drawGame(Graphics g)
 	{
 //		set the color to black and write the frame count in the middle of the screen
 		g.setColor(Color.BLACK);
 		g.drawString(String.valueOf(frameCount), Gui.WIDTH / 2, Gui.HEIGHT / 2);
 		
 //		prepare an output buffer
 		StringBuffer output = new StringBuffer("");
 		
 //		draw each word that shows up on the screen
 		for (Word word: wordsOnScreen)
 		{
 			word.drawWord(g);
 		}
 		
 //		store all acceptable words in the output
 		for (Word word: acceptableWords)
 		{
 			output.append(String.valueOf(word.getCharacters( )) + " ");
 		}
 		
 //		draw the stats, the output, and the character buffer
 		g.drawString("Score: " +String.valueOf(score) + " Errors: " + String.valueOf(errors) +
 			" Lives Left: " + String.valueOf(livesLeft), 10, Gui.HEIGHT - 20);
 		g.drawString(output.toString( ), 10, Gui.HEIGHT - 10);
 		g.drawString(buffer.toString( ), 10, Gui.HEIGHT);
 	}
 	
 	/**
 	 * Updates the game. Meant to be overridden. <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 17, 2012 <br>
 	 * Date last modified: Apr 17, 2012 <br>
 	 *
 	 * <hr>
 	 * @param deltaTime The time elapsed since the last frame
 	 */
 	public void update(int deltaTime)
 	{
 //		increment the frame count
 		frameCount++;
 		
 //		for every word on the screen...
 		for (int c = 0; c < wordsOnScreen.size( ); c++)
 		{
 //			for every fourth frame...
 			if (frameCount %  determineDifficulty( ) == 0)
 //				move the word down by one pixel
 				wordsOnScreen.get(c).offset(0, 1);
 //			if the word reaches the shield...
 			if (wordsOnScreen.get(c).getLocY( ) > 420)
 			{
 //				remove the word and take a life away
 				acceptableWords.remove(wordsOnScreen.get(c));
 				wordsOnScreen.remove(c);
 				
 //				decrement the counter
 				c--;
 				
 //				reset all the acceptable words
 				resetAcceptableWords( );
 				
 //				decrement the lives left
 				livesLeft--;
 			}
 		}
 		
 //		create accumulator for the characters typed
 		int charactersTyped = 0;
 		for (int c = 0; c < clearedWords.size( ); c++)
 		{
 			charactersTyped += clearedWords.get(c).getCharactersCleared( );
 		}
 		
 //		calculate the words per minute
 		wpm = (charactersTyped == 0? 1: charactersTyped) * 720 / (frameCount == 0? 1: frameCount);
 		
 //		if there are no lives left, game over for the player
 		if(livesLeft < 1)
 		{
 			makeGameOver( );
 		}
 		
 //		if there is no gameover, the pool has no words, and no words on the screen
 		if(!gameOver && !pool.hasMoreWords( ) && wordsOnScreen.isEmpty( ))
 			makeWin();
 	}
 
 	/**
 	 * Does the win stuff for the player <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 21, 2012 <br>
 	 * Date last modified: Apr 21, 2012 <br>
 	 *
 	 * <hr>
 	 */
 	protected void makeWin()
 	{
 		win = true;
 		score *= wpm;
 		score -= errors * ERROR_DEDUCTION;
 	}
 	
 	/**
 	 * return true if the players make a win. <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 24, 2012 <br>
 	 * Date last modified: Apr 25, 2012 <br>
 	 *
 	 * <hr>
 	 * @return
 	 */
 	public boolean isWin()
 	{
 		return win;
 	}
 
 	/**
 	 * Sets the things necessary for game over <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 21, 2012 <br>
 	 * Date last modified: Apr 21, 2012 <br>
 	 *
 	 * <hr>
 	 */
 	
 	protected void makeGameOver()
 	{
 		gameOver = true;
 		score *= wpm;
 		score -= errors * ERROR_DEDUCTION;
 	}
 	
 	/**
 	 * Determine the difficulty based on the words-per-minute <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 24, 2012 <br>
 	 * Date last modified: Apr 25, 2012 <br>
 	 *
 	 * <hr>
 	 * @return
 	 */
 	private int determineDifficulty()
 	{
 		if (wpm < 1)
 			return 4;
 		else if (wpm < 10)
 			return 3;
 		else if (wpm < 30)
 			return 2;
 		else
 			return 1;
 	}
 	
 	/**
 	 * Returns whether the game has been lost or not <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 21, 2012 <br>
 	 * Date last modified: Apr 21, 2012 <br>
 	 *
 	 * <hr>
 	 * @return boolean. True indicates game has been lost.
 	 */
 	public boolean isGameOver()
 	{
 		return gameOver;
 	}
 	
 	/**
 	 * Initializes the game. Meant to be overridden. <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 17, 2012 <br>
 	 * Date last modified: Apr 17, 2012 <br>
 	 *
 	 * <hr>
 	 */
 	public void initialize()
 	{
 		for (int c = 0; c < 5 && pool.hasMoreWords( ); c++)
 		{
 			wordsOnScreen.add(pool.getNextWord( ));
 		}
 		
 //		initialize the acceptable word pool to all the words on the screen
 		acceptableWords = new ArrayList <Word>(wordsOnScreen);		
 	}
 
 	/**
 	 * Render the game over screen. Meant to be overridden. <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 21, 2012 <br>
 	 * Date last modified: Apr 21, 2012 <br>
 	 *
 	 * <hr>
 	 * @param g
 	 */
 	public void renderGameOver(Graphics g)
 	{
 		g.drawString("Game Over!", Gui.WIDTH / 2, Gui.HEIGHT / 2);
 	}
 
 	/**
 	 * Render the win screen, for the win. <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 24, 2012 <br>
 	 * Date last modified: Apr 25, 2012 <br>
 	 *
 	 * <hr>
 	 * @param g
 	 */
 	public void renderWin(Graphics g)
 	{
 		g.drawString("You Won!", Gui.WIDTH / 2, Gui.HEIGHT / 2);
 	}
 	
 	
 }
