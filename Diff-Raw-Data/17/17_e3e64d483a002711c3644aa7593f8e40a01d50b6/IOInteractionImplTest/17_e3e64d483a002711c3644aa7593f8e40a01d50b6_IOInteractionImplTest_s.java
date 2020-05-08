 package com.joshcheek.ttt.cli;
 
 import com.joshcheek.ttt.library.Game;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 
 import static com.joshcheek.ttt.cli.TestHelpers.assertContains;
 import static com.joshcheek.ttt.cli.TestHelpers.assertDoesntContain;
 
 /**
  * Created by IntelliJ IDEA.
  * User: joshuajcheek
  * Date: 9/8/11
  * Time: 11:32 PM
  * To change this template use File | Settings | File Templates.
  */
 public class IOInteractionImplTest extends junit.framework.TestCase {
     ByteArrayOutputStream       outputStream    = new ByteArrayOutputStream();
     private PrintStream         stdoutMock      = new PrintStream(outputStream, true);
     private IOInteractionImpl   interaction;
 
     public void testItCanBeInstantiatedWithStdinAndStdout() {
         new IOInteractionImpl(System.in, System.out); // doesn't raise an error
     }
 
     public void testItDisplaysResultsForTie() {
         mockInteraction("");
         interaction.displayResults(new Game("112221121"));
         assertContains(output(), "tie");
     }
 
     public void testItDisplaysResultsForPlayer1Winning() {
         mockInteraction("");
         interaction.displayResults(new Game("111220000"));
         assertContains(output(), "Player 1");
         assertDoesntContain(output(), "Player 2");
         assertDoesntContain(output(), "tie");
     }
 
     public void testItDisplaysResultsForPlayer2Winning() {
         mockInteraction("");
         interaction.displayResults(new Game("222110100"));
         assertContains(output(), "Player 2");
         assertDoesntContain(output(), "Player 1");
         assertDoesntContain(output(), "tie");
     }
 
     public void testItDisplaysTheBoardWhenGameIsOver() {
         mockInteraction("");
         interaction.displayResults(new Game("222110100"));
         assertContains(output(), "|");
         assertContains(output(), "---");
     }
 
     public void testItPromptsForPlayer1() {
         mockInteraction("h");
         interaction.promptForPlayer(1);
         assertDoesntContain(output(), "Player 2");
         assertContains(output(), "Player 1");
     }
 
     public void testItPromptsForPlayer2() {
         mockInteraction("h");
         interaction.promptForPlayer(2);
         assertContains(output(), "Player 2");
         assertDoesntContain(output(), "Player 1");
     }
 
     public void testItReturnsHWhenItPromptsForPlayerAndReceivesH() {
         mockInteraction("h");
         assertEquals('h', interaction.promptForPlayer(1));
         assertDoesntContain(output(), "Invalid");
     }
 
     public void testItReturnsCWhenItPromptsForPlayerAndReceivesC() {
         mockInteraction("c");
         assertEquals('c', interaction.promptForPlayer(1));
         assertDoesntContain(output(), "Invalid");
     }
 
     public void testItContinuesPromptingForPlayerUntilItReceivesAnHOrAC() {
         mockInteraction("x\nc\n");
         assertEquals('c', interaction.promptForPlayer(1));
         assertContains(output(), "Invalid");
 
         mockInteraction("xylaphone\nh\n");
         assertEquals('h', interaction.promptForPlayer(1));
         assertContains(output(), "Invalid");
     }
 
     public void testItDoesntCareAboutCaseOfInputWhenPromptingForPlayer() {
         mockInteraction("HELLO");
         assertEquals('h', interaction.promptForPlayer(1));
     }
 
 
 
 
     public void testItDisplaysAvailableOptionsWhenPromptingForPlayerMove() {
         mockInteraction("6");
         interaction.promptMove(new Game("121210000"));
         assertContains(output(), "6, 7, 8, or 9");
     }
 
     public void testItDisplaysOnlyAvailableOptionWhenPromptingForPlayerMove() {
         mockInteraction("6");
         interaction.promptMove(new Game("112220121"));
         assertContains(output(), "6");
         assertDoesntContain(output(), "or");
     }
 
     public void testItReturnsCorrectPlayerMoveWhenAvailable() {
         mockInteraction("1");
         assertEquals(1, interaction.promptMove(new Game()));
     }
 
     public void testItRepromptsWhenPlayerMoveIsNotAvailable() {
         mockInteraction("1\n2\n3\n4\n");
         assertEquals(3, interaction.promptMove(new Game("110220000")));
         assertContains(output(), "can't move there");
     }
 
     public void testItDisplaysTheBoardWhenPromptingForMove() {
         mockInteraction("1");
         interaction.promptMove(new Game("010020010"));
         assertContains(output(), "|");      // just some general syntax you'd expect from the board
         assertContains(output(), "---");    // without going so far as to specify appearance format
     }
 
 
     private void mockInteraction(String input) {
         interaction = new IOInteractionImpl(mockStdin(input), stdoutMock);
     }
 
     private String output() {
         return outputStream.toString();
     }
 
     public static InputStream mockStdin(final String input) {
         return new java.io.InputStream() {
             private int index = 0;
             public int read() throws IOException {
                 if(index < input.length()) return input.charAt(index++);
                 throw new IOException("No index " + index + " for " + input);
             }
         };
     }
 
 }
