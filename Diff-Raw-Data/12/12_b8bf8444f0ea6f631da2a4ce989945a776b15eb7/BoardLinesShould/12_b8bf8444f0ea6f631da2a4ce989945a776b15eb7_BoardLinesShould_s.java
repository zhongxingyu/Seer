 package org.craftedsw.tictactoe;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.craftedsw.tictactoe.BoardLines.ROW_2;
 import static org.hamcrest.Matchers.*;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertThat;
 
 public class BoardLinesShould {
 
     private BoardLines boardLines;
 
     @Before
     public void initialise() {
         boardLines = new BoardLines();
     }
 
     @Test public void
     should_return_null_when_there_is_no_winning_line() {
        String[] marks = new String[]{"0", " ", " ", " ", " ", " ", "0", " ", " "};
 
         assertThat(boardLines.winningLine(marks),  is(nullValue()));
     }
 
     @Test public void
     should_return_a_winning_line() {
         String[] marks = new String[] {"0", " ", " ", "X", " ", "X", "0", " ", " "};
 
         Line winningLine = boardLines.winningLine(marks);
 
         assertThat(winningLine, is(ROW_2));
     }
 
 
 }
