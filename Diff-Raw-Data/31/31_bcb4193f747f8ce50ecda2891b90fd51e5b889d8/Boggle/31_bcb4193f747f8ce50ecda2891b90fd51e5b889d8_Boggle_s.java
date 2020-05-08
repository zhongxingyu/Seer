 // The "Boggle" class.
 import java.awt.*;
 import hsa.Console;
<<<<<<< HEAD

=======
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
>>>>>>> master
 public class Boggle
 {
     static Console c;           // The output console
 
     public static void main (String[] args)
     {
 	c = new Console ();
 	int wordRow = 16;
 	int wordCollumn = 3;
 	int score = 0;
 	String gottenWord;
 
 	c.clear ();
 	char board[] [] = boardGen ();
 	drawBoard (board);
 
<<<<<<< HEAD
=======
 
 
>>>>>>> master
 	while (true)
 	{
 	    c.setCursor (wordRow, wordCollumn);
 	    c.print ("          ");
 	    c.setCursor (wordRow, wordCollumn);
<<<<<<< HEAD
	    gottenWord=c.readString ();
	    wordRow++;
	    
=======
 	    gottenWord = c.readString ();
 	    wordRow++;
 	    if (spellCheck (gottenWord))
 		score++;
 
>>>>>>> master
 	    if (wordRow > 24)
 	    {
 		c.print ("          ");
 		wordRow = 5;
 		wordCollumn += 30;
 	    }
 	    c.setCursor (wordRow + 1, wordCollumn);
 	    c.print ("SCORE: " + score);
 	}
 
 
     }
 
 
     public static void drawBoard (char[] [] board)
     {
 	c.setFont (new Font ("Arial", Font.PLAIN, 50));
 	c.drawString ("  Boggle", 0, 40);
 
 	for (int row = 0 ; row < 4 ; row++) //prents every element of every row
 	{
 	    for (int collumn = 0 ; collumn < 4 ; collumn++)   //prints each element of roe i
 	    {
 
 		c.drawRoundRect (row * 60, collumn * 60 + 60, 50, 50, 10, 10);
 		c.setFont (new Font ("Arial", Font.PLAIN, 50));
 		c.drawString ((board [row] [collumn] + " "), (collumn * 60 + 5), (row * 60 + 105));
 	    }
 	}
     }
 
 
     public static char[] [] boardGen ()
     {
 	int row, collumn;
 	char board[] [] = {  //Creates array of spaces
 		{' ', ' ', ' ', ' '},
 		{' ', ' ', ' ', ' '},
 		{' ', ' ', ' ', ' '},
 		{' ', ' ', ' ', ' '}};
 	for (int i = 0 ; i < 16 ; i++) //For each dieid
 	{
 	    do
 	    {
 		row = (int) (Math.random () * 4); //generates a random position
 		collumn = (int) (Math.random () * 4);
 
 	    }
 	    while (board [row] [collumn] != ' '); //Runs until positrion is empty
 	    board [row] [collumn] = letterRoll (i); //places the die in that position
 
 	}
 	return board;
     }
 
 
     public static char letterRoll (int dieId)
     {
 	char dice[] [] = {  //2D array of 1D arrays of characters representing each letter on a given die
 		{'A', 'A', 'C', 'I', 'O', 'T'},
 		{'A', 'H', 'M', 'O', 'R', 'S'},
 		{'E', 'G', 'K', 'L', 'U', 'Y'},
 		{'A', 'B', 'I', 'L', 'T', 'Y'},
 		{'A', 'C', 'D', 'E', 'M', 'P'},
 		{'E', 'G', 'I', 'N', 'T', 'V'},
 		{'G', 'I', 'L', 'R', 'U', 'W'},
 		{'E', 'L', 'P', 'S', 'T', 'U'},
 		{'D', 'E', 'N', 'O', 'S', 'W'},
 		{'A', 'C', 'E', 'L', 'R', 'S'},
 		{'A', 'B', 'J', 'M', 'O', 'Q'},
 		{'E', 'E', 'F', 'H', 'I', 'Y'},
 		{'E', 'H', 'I', 'N', 'P', 'S'},
 		{'D', 'K', 'N', 'O', 'T', 'U'},
 		{'A', 'D', 'E', 'N', 'V', 'Z'},
 		{'B', 'I', 'F', 'O', 'R', 'X'}};
 
 	return dice [dieId] [(int) (Math.random () * 6)];
     }
<<<<<<< HEAD
}

=======
 
 
     public static boolean spellCheck (String word)
     {
 	String filename = "corncob_caps.txt";
 	//Uses the corncob caps dictionary file
 	//http://www.mieliestronk.com/wordlist.html
 	boolean goodSpelling;
 	try
 	{
 	    return readFromFile (filename, word);
 	}
 	catch (IOException e)
 	{
 
 	    return false;
 	}
 
 
     }
 
 
     //This code taken from alexmcchessers on Stack Overflow
     //http://stackoverflow.com/questions/9913/java-file-io-compendium
 
     public static boolean readFromFile (String filename, String word) throws /*FileNotFoundException,*/IOException
     {
 	StringBuffer readBuffer = new StringBuffer ();
 	BufferedReader fileReader = new BufferedReader (new FileReader (filename));
 	String sRead = null;
 
 
 	do
 	{
	    sRead = fileReader.readLine ();
	    if (sRead.equals(word))
 	    {
 		return true;
 	    }
 	}
 	while (sRead != null);
 	return false;
     }
 }
 
 
 
 
>>>>>>> master
