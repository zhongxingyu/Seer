 package de.skuzzle.polly.core.parser.problems;
 
 import java.util.SortedSet;
 
 import de.skuzzle.polly.core.parser.ParseException;
 import de.skuzzle.polly.core.parser.Position;
 import de.skuzzle.polly.core.parser.SyntaxException;
 import de.skuzzle.polly.core.parser.Token;
 import de.skuzzle.polly.core.parser.TokenType;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Type;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
 
 
 public class SimpleProblemReporter extends MultipleProblemReporter {
 
     
     public SimpleProblemReporter() {
         super();
     }
     
     
     private SimpleProblemReporter(SortedSet<Problem> problems, Position position) {
         super(problems, position);
     }
     
     
     
     @Override
     public ProblemReporter subReporter(Position clipping) {
         return new SimpleProblemReporter(this.problems, clipping);
     }
     
     @Override
     public void lexicalProblem(String problem, Position position) throws ParseException {
         super.lexicalProblem(problem, position);
         throw new ParseException(problem, this.clip(position));
     }
     
     
     
     @Override
     public void syntaxProblem(String problem, Position position, Object... params)
             throws ParseException {
         super.syntaxProblem(problem, position, params);
         throw new ParseException(Problems.format(problem, params), this.clip(position));
     }
     
     
     
     @Override
     public void syntaxProblem(TokenType expected, Token occurred, Position position)
             throws ParseException {
         super.syntaxProblem(expected, occurred, position);
         throw new SyntaxException(expected, occurred, this.clip(position));
     }
     
     
     
     @Override
     public void semanticProblem(String problem, Position position, Object... params)
             throws ParseException {
         throw new ParseException(Problems.format(problem, params), this.clip(position));
     }
     
     
     
     @Override
     public void typeProblem(Type expected, Type occurred, Position position)
             throws ASTTraversalException {
         super.typeProblem(expected, occurred, position);
         throw new ASTTraversalException(this.clip(position), 
             Problems.format(Problems.TYPE_ERROR, expected, occurred));
     }
     
     
     
     @Override
     public void runtimeProblem(String problem, Position position, Object... params)
             throws ASTTraversalException {
         super.runtimeProblem(problem, position, params);
         throw new ASTTraversalException(this.clip(position), 
             Problems.format(problem, params));
     }
 }
