 import java.io.IOException;
 import java.util.Stack;
 
 public class TableDrivenParser extends Parser
 
 {
   public Stack stackAttack;
   private ParsingTable kleinTable;
 
     public TableDrivenParser( Scanner source )
     {
         super( source );
         kleinTable = makeKleinParsingTable();
     }
 
     protected void parseProgram() throws IOException, LexicalException, SemanticException
     {
       Stack parseStack = new Stack();
       
       new Push( new Token(Token.TYPE.EOS) ).execute(parseStack);   // Step 1
       new Push( "PROGRAM" ).execute(parseStack); // Step 2
       
       while( true ){ // Step 3?
         Object symbol = parseStack.pop(); // Pop A
         
         skipComments( scanner.peek() );
         
         if( symbol instanceof Token ){ // A is terminal
           Token terminal = (Token) symbol;
           if( terminal.getType() == Token.TYPE.EOS ){
             System.out.println("LE FIN");
             return;
           } else if( terminal.equals( scanner.peek() )){
             // * For debugging *
             System.out.println( "Consumed: " + scanner.getNextToken() );
           } else{
             // * For debugging *
             System.out.println( "Next Token: " + scanner.peek() );
             System.out.println( "Terminal: " + terminal.typeToInt() );
             throw new SemanticException("Bad things found");
           }
         }
         else { // A is non-terminal
           String nonTerminal = (String) symbol;
           Token tmp = scanner.peek();
           ParseAction rule = kleinTable.lookup(nonTerminal,scanner.peek());
           
           if( rule instanceof PushSequence ){
             System.out.println("Found a rule for nonTerminal: " + nonTerminal + " and token: " + scanner.peek());
             rule.execute(parseStack); // PushSequence loops through rules backwards
           } else if( rule instanceof PushNothing ){
             // **** Do nothing? ****
           }else{ // Failed to find rule for table[A,i]
             throw new SemanticException("No rule for non-terminal: " + nonTerminal + " and terminal: " + scanner.peek());
           }
         }
       }
       // ... the algorithm
       // return answer;
     }
 
     // -------------------------------------------------------------------
     // AC PARSING TABLE FACTORY
 
     private ParsingTable makeKleinParsingTable()
     {
        ParsingTable table = new ParsingTable();
 
        try {
         
        Token endOfStream        = new Token( Token.TYPE.EOS );
        Token plusOp             = new Token( Token.TYPE.PLUS );
        Token minusOp            = new Token( Token.TYPE.MINUS );
        Token multiplyOp         = new Token( Token.TYPE.MULTIPLY );
        Token assignmentOp       = new Token( Token.TYPE.ASSIGNMENT );
        Token lessThanOp         = new Token( Token.TYPE.LESSTHAN );
        Token openParen          = new Token( Token.TYPE.OPEN_PAREN );
        Token closedParen        = new Token( Token.TYPE.CLOSED_PAREN );
        Token ifOp               = new Token( Token.TYPE.IF );
        Token thenOp             = new Token( Token.TYPE.THEN );
        Token elseOp             = new Token( Token.TYPE.ELSE );
        Token endIfOp            = new Token( Token.TYPE.END_IF );
        Token notOp              = new Token( Token.TYPE.NOT );
        Token orOp               = new Token( Token.TYPE.OR );
        Token andOp              = new Token( Token.TYPE.AND );
        Token comment            = new Token( Token.TYPE.COMMENT );
        Token forwardSlash       = new Token( Token.TYPE.FORWARD_SLASH );
        Token comma              = new Token( Token.TYPE.COMMA );
        Token booleanOp          = new Token( Token.TYPE.BOOLEAN );
        Token trueOp             = new Token( Token.TYPE.TRUE );
        Token falseOp            = new Token( Token.TYPE.FALSE );
        Token colonOp            = new Token( Token.TYPE.COLON );
        Token integerOp          = new Token( Token.TYPE.INTEGER );
        Token identifierOp       = new Token( Token.TYPE.IDENTIFIER );
        Token print              = new Token( Token.TYPE.PRINT );
        Token number             = new Token( Token.TYPE.NUMBER );
 
        
 
        ParseAction rule00 = new PushNothing();
        ParseAction rule01 = new PushSequence(
                  new ParseAction[] { new Push("DEFINITIONS"),
                                      new Push(endOfStream)
                       } );
        ParseAction rule02 = new PushSequence(
                  new ParseAction[] { new Push("DEF1")
                                 
                       } );
        ParseAction rule03 = new PushSequence(
                  new ParseAction[] { new Push("DEF"),
                        new Push("DEFINITIONS1")
                                      
                       } );
        ParseAction rule04 = new PushSequence(
                  new ParseAction[] {  new Push("Definitions"),
                                 
                       } );
        ParseAction rule05 = new PushSequence(
                 new ParseAction[] { new Push(identifierOp),
                                     new Push(openParen),
                                     new Push("FORMALS"),
                                     new Push(closedParen),
                                     new Push(colonOp),
                                     new Push("TYPE"),
                                     new Push("BODY")
                       } );
        ParseAction rule06 = new PushSequence(
                  new ParseAction[] {
                                      new Push("NONEMPTYFORMALS")
                       } );
        ParseAction rule07 = new PushSequence(
                  new ParseAction[] { new Push("FORMAL"),
                                      new Push("NONEMPTYFORMALS1")
                       } );
        ParseAction rule08 = new PushSequence(
                  new ParseAction[] { new Push(comma),
                                      new Push("NONEMPTYFORMALS"),
                                 
                       } );
        ParseAction rule09 = new PushSequence(
                  new ParseAction[] { new Push(identifierOp),
                                      new Push(colonOp),
                                      new Push("TYPE")
                       } );
        ParseAction rule10 = new PushSequence(
                new ParseAction[] { new Push("PRINT"),
                                    new Push("BODY1")                             
                     } );
        ParseAction rule11 = new PushSequence(
                new ParseAction[] { new Push("EXPR"),
                     } );
        ParseAction rule12 = new PushSequence(
                new ParseAction[] { new Push("BODY"),
                     } );
        ParseAction rule13 = new PushSequence(
                new ParseAction[] { new Push(integerOp),
                                    
                     } );
        ParseAction rule99 = new PushSequence(
                new ParseAction[] { 
                                    new Push(booleanOp)
                     } );
        ParseAction rule14 = new PushSequence(
                new ParseAction[] { new Push("SIMPLE-EXPR"),
                                    new Push("SIMPLE-EXPR1"),
                     } );
        ParseAction rule15 = new PushSequence(
                new ParseAction[] { new Push(lessThanOp),
                                    new Push("EXPR"),
                     } );
        ParseAction rule16 = new PushSequence(
                new ParseAction[] { new Push(assignmentOp),
                           new Push("EXPR"),
                     } );
        ParseAction rule17 = new PushSequence(
                new ParseAction[] { new Push("TERM"),
                               new Push("TERM2"),
                     } );
        ParseAction rule18 = new PushSequence(
                new ParseAction[] { new Push(orOp),
                                    new Push("SIMPLE-EXPR"),
                     } );
        ParseAction rule19 = new PushSequence(
                new ParseAction[] { new Push(plusOp),
                               new Push("SIMPLE-EXPR"),
                     } );
        ParseAction rule20 = new PushSequence(
                new ParseAction[] { new Push(minusOp),
                                    new Push("SIMPLE-EXPR"),
                     } );
        ParseAction rule21 = new PushSequence(
                new ParseAction[] { new Push("FACTOR"),
                                    new Push("TERM1"),
                     } );
        ParseAction rule22 = new PushSequence(
                new ParseAction[] { new Push(andOp),
                                    new Push("TERM"),
                     } );
        ParseAction rule23 = new PushSequence(
                new ParseAction[] { new Push(multiplyOp),
                                    new Push("TERM"),
                     } );
        ParseAction rule24 = new PushSequence(
                new ParseAction[] {  new Push(forwardSlash),
                                     new Push("TERM"),
                     } );
        ParseAction rule25 = new PushSequence(
                new ParseAction[] { new Push(ifOp),
                               new Push("EXPR"),
                               new Push(thenOp),
                               new Push("EXPR"),
                               new Push(elseOp),
                               new Push("EXPR"),
                               new Push(endIfOp)
                     } );
        ParseAction rule26 = new PushSequence(
                new ParseAction[] { new Push(notOp),
                                    new Push("FACTOR"),
                     } );
        ParseAction rule27 = new PushSequence(
                new ParseAction[] { new Push("IDENTIFIER1"),
                     } );
        ParseAction rule28 = new PushSequence(
                new ParseAction[] { new Push("LITERAL"),
                     } );
        ParseAction rule29 = new PushSequence(
                new ParseAction[] { new Push(minusOp),
                                    new Push("FACTOR"),
                     } );
        ParseAction rule30 = new PushSequence(
                new ParseAction[] { new Push(identifierOp),
                                    new Push("ACTUALS1"),
                     } );
        ParseAction rule31 = new PushSequence(
                new ParseAction[] { new Push(openParen),
                                    new Push("ACTUALS"),
                                    new Push(closedParen)
                     } );
        ParseAction rule32 = new PushSequence(
                new ParseAction[] { new Push("NONEMPTYACTUALS"),
                     } );
        ParseAction rule33 = new PushSequence(
                new ParseAction[] { new Push("EXPR"),
                                   new Push("NONEMPTYACTUALS1")
                     } );
        ParseAction rule34 = new PushSequence(
                new ParseAction[] { new Push(comma),
                                    new Push("NONEMPTYACTUALS")
                     } );
        ParseAction rule35 = new PushSequence(
                new ParseAction[] { new Push(number),
                      
                     } );
        ParseAction rule98 = new PushSequence(
                new ParseAction[] { 
                      new Push("BOOLEAN"),
                     } );
        ParseAction rule36 = new PushSequence(
                new ParseAction[] { new Push(print),
                                 new Push(openParen),
                          new Push("EXPR"),
                      new Push(closedParen)
                     } );
        ParseAction rule96 = new PushSequence(
                                              new ParseAction[] { new Push(trueOp) }
                                              );
        ParseAction rule97 = new PushSequence(
                                              new ParseAction[] { new Push(falseOp) }
                                              );
        
        table.add( "PROGRAM", identifierOp, rule01 );
        table.add( "DEFINITIONS", identifierOp, rule02 );
        table.add( "DEF1", identifierOp, rule03 );
        
        
        table.add( "DEFINITIONS1", identifierOp, rule04 );
        table.add( "DEFINITIONS1", endOfStream, rule00 );
 
        
        table.add( "DEF", identifierOp, rule05 );
        
        
        table.add( "FORMALS", identifierOp, rule06 );
        //table.add( "FORMALS", endOfStream,  rule00 );
        table.add( "FORMALS", closedParen,  rule00 );
        
        
        table.add( "NONEMPTYFORMALS", identifierOp, rule07 );
        table.add( "NONEMPTYFORMALS", closedParen, rule00 ); // Follow set
        
        
        table.add( "NONEMPTYFORMALS1", comma, rule08 );
        table.add( "NONEMPTYFORMALS1", closedParen, rule00 ); // Not in follow set?
 
        
        table.add( "FORMAL", identifierOp , rule09 );
        table.add( "FORMAL", closedParen, rule00 );
        table.add( "FORMAL", comma, rule00 );
        
        
        table.add( "BODY", print, rule10 );
        table.add( "BODY", ifOp, rule11 );
        table.add( "BODY", notOp, rule11 );
        table.add( "BODY", identifierOp, rule11 );
        table.add( "BODY", number, rule11 );
        table.add( "BODY", trueOp, rule11 );
        table.add( "BODY", falseOp, rule11 );
        table.add( "BODY", minusOp, rule11 );
        
        table.add( "BODY", endOfStream, rule00 );
        
        
        table.add( "BODY1", print, rule12 );
        table.add( "BODY1", ifOp, rule12 );
        table.add( "BODY1", notOp, rule12 );
        table.add( "BODY1", identifierOp, rule12 );
        table.add( "BODY1", number, rule12 );
        table.add( "BODY1", trueOp, rule12 );
        table.add( "BODY1", falseOp, rule12 );
        table.add( "BODY1", minusOp, rule12 );
        
        table.add( "BODY1", endOfStream, rule00 );
        
        
        table.add( "TYPE", integerOp, rule13 );
        table.add( "TYPE", booleanOp, rule99 );
        
        table.add("TYPE", print, rule00 );
        table.add("TYPE", ifOp, rule00 );
        table.add("TYPE", notOp, rule00 );
        table.add("TYPE", identifierOp, rule00 );
        table.add("TYPE", number, rule00 );
        table.add("TYPE", minusOp, rule00 );
        table.add("TYPE", closedParen, rule00 );
        table.add("TYPE", comma, rule00 );
        table.add("TYPE", endOfStream, rule00 );
        
        
        table.add( "EXPR", ifOp,  rule14 );
        table.add( "EXPR", notOp,  rule14 );
        table.add( "EXPR", identifierOp,  rule14 );
        table.add( "EXPR", number, rule14 );
        table.add( "EXPR", trueOp, rule14 );
        table.add( "EXPR", falseOp, rule14 );
        table.add( "EXPR", minusOp,  rule14 );
        
        table.add( "EXPR", closedParen,  rule00 );
        table.add( "EXPR", thenOp,  rule00 );
        table.add( "EXPR", elseOp,  rule00 );
        table.add( "EXPR", endIfOp,  rule00 );
        table.add( "EXPR", comma,  rule00 );
        table.add( "EXPR", endOfStream,  rule00 );
        
        
        table.add( "SIMPLE-EXPR1", lessThanOp,  rule15 );
        table.add( "SIMPLE-EXPR1", assignmentOp,  rule16 );
        
        table.add( "SIMPLE-EXPR1", thenOp, rule00 );
        table.add( "SIMPLE-EXPR1", elseOp, rule00 );
        table.add( "SIMPLE-EXPR1", endIfOp, rule00 );
        table.add( "SIMPLE-EXPR1", identifierOp, rule00 );
        table.add( "SIMPLE-EXPR1", comma, rule00 );
        table.add( "SIMPLE-EXPR1", closedParen, rule00 );
        table.add( "SIMPLE-EXPR1", endOfStream,  rule00 );
        
        
        table.add( "SIMPLE-EXPR", ifOp,  rule17 );
        table.add( "SIMPLE-EXPR", notOp,  rule17 );
        table.add( "SIMPLE-EXPR", identifierOp,  rule17 );
        table.add( "SIMPLE-EXPR", number, rule17 );
        table.add( "SIMPLE-EXPR", booleanOp, rule17 );
        table.add( "SIMPLE-EXPR", minusOp,  rule17 );
        
        table.add( "SIMPLE-EXPR", lessThanOp,  rule00 );
        table.add( "SIMPLE-EXPR", thenOp, rule00 );
        table.add( "SIMPLE-EXPR", elseOp, rule00 );
        table.add( "SIMPLE-EXPR", endIfOp, rule00 );
        table.add( "SIMPLE-EXPR", assignmentOp, rule00 );
        table.add( "SIMPLE-EXPR", closedParen, rule00 );
        table.add( "SIMPLE-EXPR", comma, rule00 );
        table.add( "SIMPLE-EXPR", endOfStream, rule00 );
        
        
        table.add( "TERM2", orOp, rule18 );
        table.add( "TERM2", plusOp, rule19 );
        table.add( "TERM2", minusOp, rule20 );
        
        
        table.add( "TERM2", lessThanOp, rule00 );
        table.add( "TERM2", thenOp, rule00 );
        table.add( "TERM2", elseOp, rule00 );
        table.add( "TERM2", endIfOp, rule00 );
        table.add( "TERM2", assignmentOp, rule00 );
        table.add( "TERM2", identifierOp, rule00 );
        table.add( "TERM2", comma, rule00 );
        table.add( "TERM2", closedParen, rule00 );
       //table.add( "TERM2", endOfStream, rule00 );
 
        
        table.add( "TERM", ifOp, rule21 );
        table.add( "TERM", notOp, rule21 );
        table.add( "TERM", identifierOp, rule21 );
        table.add( "TERM", minusOp, rule21 );
        table.add( "TERM", number, rule21 );
        table.add( "TERM", booleanOp, rule21 );
        
        table.add( "TERM", assignmentOp, rule00 );
        table.add( "TERM", closedParen, rule00 );
        table.add( "TERM", thenOp, rule00 );
        table.add( "TERM", elseOp, rule00 );
        table.add( "TERM", endIfOp, rule00 );
        table.add( "TERM", lessThanOp, rule00 );
        table.add( "TERM", minusOp, rule00 );
        table.add( "TERM", orOp, rule00 );
        table.add( "TERM", plusOp, rule00 );
        table.add( "TERM", comma, rule00 );
        table.add( "TERM", endOfStream, rule00 );
        
        
        table.add( "TERM1", andOp, rule22 );
        table.add( "TERM1", multiplyOp, rule23 );
        table.add( "TERM1", forwardSlash, rule24 );
        
        table.add( "TERM1", minusOp, rule00 );
        table.add( "TERM1", lessThanOp, rule00 );
        table.add( "TERM1", orOp, rule00 );
        table.add( "TERM1", plusOp, rule00 );
        table.add( "TERM1", thenOp, rule00 );
        table.add( "TERM1", elseOp, rule00 );
        table.add( "TERM1", endIfOp, rule00 );
        table.add( "TERM1", assignmentOp, rule00 );
        table.add( "TERM1", identifierOp, rule00 );
        table.add( "TERM1", comma, rule00 );
        table.add( "TERM1", closedParen, rule00 );
        table.add( "TERM1", endOfStream, rule00 );
        
        
        table.add( "FACTOR", ifOp, rule25 );
        table.add( "FACTOR", notOp, rule26 );
        table.add( "FACTOR", identifierOp, rule27 );
        table.add( "FACTOR", number, rule28 );
        table.add( "FACTOR", trueOp, rule28 );
        table.add( "FACTOR", falseOp, rule28 );
        table.add( "FACTOR", minusOp, rule29 );
        
        table.add( "FACTOR", andOp, rule00 );
        table.add( "FACTOR", multiplyOp, rule00 );
        table.add( "FACTOR", forwardSlash, rule00 );
        table.add( "FACTOR", orOp, rule00 );
        table.add( "FACTOR", plusOp, rule00 );
        table.add( "FACTOR", minusOp, rule00 );
        table.add( "FACTOR", lessThanOp, rule00 );
        table.add( "FACTOR", assignmentOp, rule00 );
        table.add( "FACTOR", closedParen, rule00 );
        table.add( "FACTOR", thenOp, rule00 );
        table.add( "FACTOR", elseOp, rule00 );
        table.add( "FACTOR", endIfOp, rule00 );
        table.add( "FACTOR", comma, rule00 );
        table.add( "FACTOR", endOfStream, rule00 );
        
        table.add( "IDENTIFIER1", identifierOp, rule30 );
        
        table.add( "IDENTIFIER1", andOp, rule00 );
        table.add( "IDENTIFIER1", multiplyOp, rule00 );
        table.add( "IDENTIFIER1", forwardSlash, rule00 );
        table.add( "IDENTIFIER1", orOp, rule00 );
        table.add( "IDENTIFIER1", plusOp, rule00 );
        table.add( "IDENTIFIER1", minusOp, rule00 );
        table.add( "IDENTIFIER1", lessThanOp, rule00 );
        table.add( "IDENTIFIER1", assignmentOp, rule00 );
        table.add( "IDENTIFIER1", closedParen, rule00 );
        table.add( "IDENTIFIER1", thenOp, rule00 );
        table.add( "IDENTIFIER1", elseOp, rule00 );
        table.add( "IDENTIFIER1", endIfOp, rule00 );
        table.add( "IDENTIFIER1", comma, rule00 );
        table.add( "IDENTIFIER1", endOfStream, rule00 );
        
        
        table.add( "ACTUALS1", openParen, rule31 );
        
        table.add( "ACTUALS1", andOp, rule00 );
        table.add( "ACTUALS1", multiplyOp, rule00 );
        table.add( "ACTUALS1", forwardSlash, rule00 );
        table.add( "ACTUALS1", orOp, rule00 );
        table.add( "ACTUALS1", plusOp, rule00 );
        table.add( "ACTUALS1", minusOp, rule00 );
        table.add( "ACTUALS1", lessThanOp, rule00 );
        table.add( "ACTUALS1", assignmentOp, rule00 );
        table.add( "ACTUALS1", closedParen, rule00 );
        table.add( "ACTUALS1", thenOp, rule00 );
        table.add( "ACTUALS1", elseOp, rule00 );
        table.add( "ACTUALS1", endIfOp, rule00 );
        table.add( "ACTUALS1", comma, rule00 );
        
        
        table.add( "ACTUALS", ifOp, rule32 );
        table.add( "ACTUALS", notOp, rule32 );
        table.add( "ACTUALS", identifierOp, rule32 );
        table.add( "ACTUALS", number, rule32 );
        table.add( "ACTUALS", booleanOp, rule32 );
        table.add( "ACTUALS", minusOp, rule32 );
        
        table.add( "ACTUALS", closedParen, rule00 );
        table.add( "ACTUALS", endOfStream, rule00 );
        
        
        table.add( "NONEMPTYACTUALS", ifOp, rule33 );
        table.add( "NONEMPTYACTUALS", notOp, rule33 );
        table.add( "NONEMPTYACTUALS", identifierOp, rule33 );
        table.add( "NONEMPTYACTUALS", number, rule33 );
        table.add( "NONEMPTYACTUALS", booleanOp, rule33 );
        table.add( "NONEMPTYACTUALS", minusOp, rule33 );
       table.add( "NONEMPTYACTUALS", endOfStream, rule33 );
        
        table.add( "NONEMPTYACTUALS", closedParen, rule00 );
        
        
        table.add( "NONEMPTYACTUALS1", comma, rule34 );
        
        table.add( "NONEMPTYACTUALS1", closedParen, rule00 );
        
        
        table.add( "LITERAL", number, rule35 );
        table.add( "LITERAL", trueOp, rule96 );
        table.add( "LITERAL", falseOp, rule97 );
        
        
        table.add( "PRINT", print, rule36 );
 
        } catch (Exception e) {
            System.out.println( "Trouble building parse table.  No entries." );
            System.out.println("Error: " + e);
        }
 
        return table;
     }
     
     private void skipComments( Token nextToken ) throws IOException, LexicalException
     {
       if( nextToken.getType() == Token.TYPE.COMMENT ){
         skipComments( scanner.getNextToken() ); // Consume this token and check again
       }
     }
 }
