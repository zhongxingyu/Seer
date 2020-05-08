 import java.util.ArrayList;
 
 
 public abstract class BasicHangmanGame implements HangmanGame {
 	protected String secretWord; //stores the secret word
 	protected int secretWordLength; //stores the length of the secret word
 	protected int remainingGuesses; //stores the number of remaining guesses
 	protected char guessedLetter; //the letter that the user guessed
 	protected ArrayList<Character> guessHistory;
 	protected int numberOfUnGuessedLetters;
 	protected String currentGameState;
 	
 	public BasicHangmanGame(String secretWord, int secretWordLength, int numberOfGuesses, ArrayList<Character> guessHistory) {
 		this.secretWord = secretWord;
 		this.secretWordLength = secretWordLength;
 		this.remainingGuesses = numberOfGuesses;
 		this.guessHistory = guessHistory;
 		this.guessedLetter = ' ';
 		this.numberOfUnGuessedLetters = secretWordLength;
		
 		for(int i = 0; i < secretWord.length(); i++)
         {
             this.currentGameState += "_ ";
             for(int j = i; j > 0; j--)
             {
                 if(secretWord.charAt(i) == secretWord.charAt(j-1))
                 {
                     this.numberOfUnGuessedLetters--;//If the letter appears many times in the secret word, it will be counted just once.
                     break;
                 }
             }
         }
 	}
 	
 	public BasicHangmanGame(int secretWordLength, int numberOfGuesses) {
 		this("", secretWordLength, numberOfGuesses, new ArrayList<Character>());
 	}
 	
 	public BasicHangmanGame(String secretWord, int numberOfGuesses, ArrayList<Character> guessHistory) {
 		this(secretWord, secretWord.length(), numberOfGuesses, guessHistory);
 	}
 	
 	public BasicHangmanGame() {
 		this("",0,new ArrayList<Character>());
 	}
 	
 	public String getSecretWord() {
 		return this.secretWord;
 	}
 
 	public boolean makeGuess(char ch) {
 		if (Character.isLetter(ch) == false) return false;
         boolean tempB = true;
         this.guessedLetter = ch;
         int i;
         for(i = 0; i < this.secretWord.length(); i++)
         {
             if(this.secretWord.charAt(i) == ch)//if the user guess right, adjust the current state.
             {
                 String temp = "";
                 for(int j = 0; j < this.secretWord.length(); j++)
                 {
                     if(this.secretWord.charAt(j) == ch)
                     {
                         temp = temp + ch + " ";
                     }
                     else
                     {
                         temp = temp + this.currentGameState.charAt(2*j) + this.currentGameState.charAt(2*j+1);              
                     }
                 }
                 this.currentGameState = temp;
                 tempB = true;
                 break;
             }
             else
             {
                 tempB = false;
             }
         }
         if(!RepeatInput(ch))
         {
             this.guessHistory.add(this.guessedLetter);
 
             if(tempB)
             {
                 this.numberOfUnGuessedLetters--;
             }
             else
             {
                 this.remainingGuesses--;
             }
             return tempB;
         }
         else return false;
 	}
 	
 	public boolean RepeatInput(char c)
     {
     	for (int i = 0; i < this.guessHistory.size(); i++) {
     		if (this.guessHistory.get(i) == c) return true;
     	}
     	return false;
     }
 
 	public boolean isWin() {
 		if(this.remainingGuesses == 0)
             return false; //if the user have no chance to guess again, it means the user loses.
         else
             return true;
 	}
 
 	public boolean gameOver() {
 		if(this.remainingGuesses == 0 || this.numberOfUnGuessedLetters == 0)
             return true;
         else
             return false;
 	}
 
 	public int numGuessesRemaining() {
 		return this.remainingGuesses;
 	}
 
 	public int numLettersRemaining() {
 		return this.numberOfUnGuessedLetters;
 	}
 
 	public String displayGameState() {
 		return this.currentGameState;
 	}
 
 	public ArrayList<Character> lettersGuessed() {
 		return this.guessHistory;
 	}
 
 }
