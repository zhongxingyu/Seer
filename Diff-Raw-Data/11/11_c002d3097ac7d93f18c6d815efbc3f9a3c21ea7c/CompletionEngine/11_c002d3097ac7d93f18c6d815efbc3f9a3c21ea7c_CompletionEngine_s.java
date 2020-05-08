 package net.cscott.sdr.calls.grm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.runner.RunWith;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.jutil.UnmodifiableIterator;
 import net.cscott.sdr.calls.Program;
 import net.cscott.sdr.calls.grm.Grm.Alt;
 import net.cscott.sdr.calls.grm.Grm.Concat;
 import net.cscott.sdr.calls.grm.Grm.Mult;
 import net.cscott.sdr.calls.grm.Grm.Nonterminal;
 import net.cscott.sdr.calls.grm.Grm.Terminal;
 import net.cscott.sdr.util.LL;
 import net.cscott.sdr.util.Tools;
 
 /**
  * Uses {@link Grm#grammar(Program)} to compute possible completions for a
  * partially-input call.
  * <p>
  * We are careful about expanding "+" and "*" to avoid generating an
  * infinite list of options.  We only expand once past the end of the
  * given input.
  *
  * @doc.test Get completions for partial phrases, based on the grammar:
  *  js> importPackage(net.cscott.sdr.calls);
  *  js> function c(txt) {
  *    >   for (s in Iterator(CompletionEngine.complete(Program.PLUS, txt, 100))) {
  *    >     print(s);
  *    >   }
  *    > }
  *  js> c("sq");
  *  square thru
  *  square thru <times>
  *  square thru and roll
  *  square thru and sweep <fraction>
  *  square thru <number>
  *  square thru <number> <times>
  *  square thru <number> and roll
  *  square thru <number> and sweep <fraction>
  *  square thru <number> hands
  *  square thru <number> hands <times>
  *  square thru <number> hands and roll
  *  square thru <number> hands and sweep <fraction>
  *  square thru <number> hands around
  *  square thru <number> hands around <times>
  *  square thru <number> hands around and roll
  *  square thru <number> hands around and sweep <fraction>
  *  square thru <number> hands round
  *  square thru <number> hands round <times>
  *  square thru <number> hands round and roll
  *  square thru <number> hands round and sweep <fraction>
  *  js> c("square thru 1 1/2 h");
  *  square thru 1 1/2 hands
  *  square thru 1 1/2 hands <times>
  *  square thru 1 1/2 hands and roll
  *  square thru 1 1/2 hands and sweep <fraction>
  *  square thru 1 1/2 hands around
  *  square thru 1 1/2 hands around <times>
  *  square thru 1 1/2 hands around and roll
  *  square thru 1 1/2 hands around and sweep <fraction>
  *  square thru 1 1/2 hands round
  *  square thru 1 1/2 hands round <times>
  *  square thru 1 1/2 hands round and roll
  *  square thru 1 1/2 hands round and sweep <fraction>
  *  js> c("tr");
  *  trade
  *  trade <times>
  *  trade and roll
  *  trade and sweep <fraction>
  *  trade by
  *  trade by <times>
  *  trade by and roll
  *  trade by and sweep <fraction>
  *  trade the wave
  *  trade the wave <times>
  *  trade the wave and roll
  *  trade the wave and sweep <fraction>
  *  track <number>
  *  track <number> <times>
  *  track <number> and roll
  *  track <number> and sweep <fraction>
  *  js> c("trade")
  *  trade
  *  trade <times>
  *  trade and roll
  *  trade and sweep <fraction>
  *  trade by
  *  trade by <times>
  *  trade by and roll
  *  trade by and sweep <fraction>
  *  trade the wave
  *  trade the wave <times>
  *  trade the wave and roll
  *  trade the wave and sweep <fraction>
  *  js> c("trade a");
  *  trade and roll
  *  trade and roll <times>
  *  trade and roll and roll
  *  trade and roll and sweep <fraction>
  *  trade and sweep <fraction>
  *  trade and sweep <fraction> <times>
  *  trade and sweep <fraction> and roll
  *  trade and sweep <fraction> and sweep <fraction>
  *  js> c("trade and roll");
  *  trade and roll
  *  trade and roll <times>
  *  trade and roll and roll
  *  trade and roll and sweep <fraction>
  *  js> c("trade and roll a");
  *  trade and roll and roll
  *  trade and roll and roll <times>
  *  trade and roll and roll and roll
  *  trade and roll and roll and sweep <fraction>
  *  trade and roll and sweep <fraction>
  *  trade and roll and sweep <fraction> <times>
  *  trade and roll and sweep <fraction> and roll
  *  trade and roll and sweep <fraction> and sweep <fraction>
  *  js> c("scoot back once a");
  *  scoot back once and a half
  *  scoot back once and a half <times>
  *  scoot back once and a half and roll
  *  scoot back once and a half and sweep <fraction>
  *  scoot back once and a third
  *  scoot back once and a third <times>
  *  scoot back once and a third and roll
  *  scoot back once and a third and sweep <fraction>
  *  scoot back once and a quarter
  *  scoot back once and a quarter <times>
  *  scoot back once and a quarter and roll
  *  scoot back once and a quarter and sweep <fraction>
  *  scoot back once and one half
  *  scoot back once and one half <times>
  *  scoot back once and one half and roll
  *  scoot back once and one half and sweep <fraction>
  *  scoot back once and one third
  *  scoot back once and one third <times>
  *  scoot back once and one third and roll
  *  scoot back once and one third and sweep <fraction>
  *  scoot back once and one quarter
  *  scoot back once and one quarter <times>
  *  scoot back once and one quarter and roll
  *  scoot back once and one quarter and sweep <fraction>
  *  scoot back once and two thirds
  *  scoot back once and two thirds <times>
  *  scoot back once and two thirds and roll
  *  scoot back once and two thirds and sweep <fraction>
  *  scoot back once and two quarters
  *  scoot back once and two quarters <times>
  *  scoot back once and two quarters and roll
  *  scoot back once and two quarters and sweep <fraction>
  *  scoot back once and three quarters
  *  scoot back once and three quarters <times>
  *  scoot back once and three quarters and roll
  *  scoot back once and three quarters and sweep <fraction>
  */
 @RunWith(value=JDoctestRunner.class)
 public class CompletionEngine {
     /**
      * Return an iterator over the possible completions for the input string
      * at the given dance program.
      */
     public static Iterator<String> complete(Program program, String input) {
         return new CompletionIterator(program, input.replace('-',' '));
     }
     /** Return a size-limited list of possible completions for the input string
      * at the given dance program. */
     public static List<String> complete(Program program, String input,
                                         int limit) {
         Iterator<String> it = complete(program, input);
         List<String> result = new ArrayList<String>(limit);
         for (int i=0; i<limit && it.hasNext(); i++)
             result.add(it.next());
         return result;
     }
     static class CompletionIterator extends UnmodifiableIterator<String>{
         final Program program;
         final List<Token> input;
         List<Integer> lastState;
         String next;
         boolean needNext, hasNext;
         public CompletionIterator(Program program, String partial) {
             this.program = program;
             this.input = CompletionTokenizer.tokenize(partial);
             this.lastState = Collections.emptyList();
             this.needNext = true;
         }
         /** This method does the real work, invoking a CompleteVisitor on the
          *  nextState from last time. */
         @Override
         public boolean hasNext() {
             if (needNext) {
                 this.needNext = false;
                 CompleteVisitor cv = new CompleteVisitor
                     (Grm.grammar(program), new CompleteState
                         (LL.create(input), LL.create(lastState),
                          LL.<Integer>NULL(), LL.<String>NULL(), false));
                 Grm g = cv.rules.get("start");
                 this.hasNext = g.accept(cv);
                 StringBuilder sb = new StringBuilder();
                 for (Iterator<String> it=cv.cs.completion.reverse().iterator();
                      it.hasNext(); ) {
                     sb.append(it.next());
                     if (it.hasNext()) sb.append(' ');
                 }
                 this.next = sb.toString();
                 this.lastState = cv.cs.nextState.toList();
             }
             return this.hasNext;
         }
         /** This just returns the results of the call to {@link #hasNext()}. */
         @Override
         public String next() {
             if (this.needNext) hasNext();
             this.needNext = true;
             return this.next;
         }
     }
 
     /** Types of tokens we'll lex from the partial input string. */
     static class Token {
         enum TokenType {
             FRAGMENT, STRING, FRACTION;
         }
         final String text;
         final TokenType type;
         Token(String text, TokenType type) { this.text=text; this.type=type; }
         boolean matches(String literal) {
             switch(type) {
             case STRING:
                 return literal.equalsIgnoreCase(text);
             case FRAGMENT:
                 return literal.toLowerCase().startsWith(text.toLowerCase());
             case FRACTION:
                 return false;
             }
             assert false : "unmatched input";
             return false;
         }
         public String toString() {
             return "<"+this.text+","+this.type+">";
         }
     }
 
     /** Tracks the state of the grammar match. */
     private static class CompleteState implements Cloneable {
         LL<Token> partialInput;
         LL<Integer> lastState;
         LL<Integer> nextState;
         LL<String> completion;
         boolean matchedTerminal;
         CompleteState(LL<Token> partialInput,
                       LL<Integer> lastState, LL<Integer> nextState,
                       LL<String> completion, boolean matchedTerminal) {
             this.partialInput = partialInput;
             this.lastState = lastState;
             this.nextState = nextState;
             this.completion = completion;
             this.matchedTerminal = matchedTerminal;
         }
         public CompleteState clone() {
             return new CompleteState(partialInput, lastState, nextState,
                                      completion, matchedTerminal);
         }
         void popInput() {
             if (!partialInput.isEmpty())
                 partialInput = partialInput.pop();
         }
         int popState() {
             if (lastState.isEmpty()) return 0;
             int v = lastState.head;
             lastState = lastState.pop();
             // increment the state by one if this is the last state, that
             // ensures that our invocation returns the 'next' completion, not
             // the same completion as last time.
             return (lastState.isEmpty()) ? (v+1) : v;
         }
         void pushState(int v) {
             this.nextState = this.nextState.push(v);
         }
         void pushCompletion(String s) {
             this.completion = this.completion.push(s);
         }
     }
     /** Attempt to match the partial input against the call grammar. */
     static final class CompleteVisitor extends GrmVisitor<Boolean> {
         final Map<String,Grm> rules;
         CompleteState cs;
         CompleteVisitor(Map<String,Grm> rules, CompleteState cs) {
             this.rules = rules;
             this.cs = cs;
         }
 
         @Override
         public Boolean visit(Alt alt) {
             // try choices in order
             int i=cs.popState();
             CompleteState saved = this.cs.clone();
             saved.lastState = LL.NULL(); // if we fail, don't try to use the
                                          // rest of the saved state.
             for ( ; i<alt.alternates.size(); i++) {
                 Grm g = alt.alternates.get(i);
                 if (g.accept(this)) {
                     cs.pushState(i); // record where we were
                     return true; // got one!
                 }
                 this.cs = saved.clone();
             }
             // none matched.
             return false;
         }
         @Override
         public Boolean visit(Concat concat) {
             CompleteState saved = this.cs.clone();
             List<Integer> n = new ArrayList<Integer>();
             assert this.cs.nextState.isEmpty();
             // no choices here, either we match or we don't.
             AGAIN: while (true) {
                 for (Grm g : concat.sequence) {
                     if (g.accept(this)) {
                         // add nextState to n; clear n for next g
                         transferNextTo(n);
                     } else {
                         this.cs = saved.clone();
                         this.cs.lastState = LL.create(n);
                         n.clear();
                         if (!this.cs.lastState.isEmpty())
                             // retry a different alternative
                             continue AGAIN;
                         // no chance this will work
                         return false;
                     }
                 }
                 // well, i guess this worked!
                 this.cs.nextState = LL.create(n);
                 return true;
             }
         }
         @Override
         public Boolean visit(Mult mult) {
 	    // desugar '+' into Concat(x, Mult(x, STAR))
 	    if (mult.type == Mult.Type.PLUS)
 	        return new Concat(Tools.l
 	                (mult.operand, new Mult(mult.operand, Mult.Type.STAR)))
 	                .accept(this);
 	    // desugar '?' into Alt(<NULL>, x)
 	    // also, make * into ? if partialInput.isEmpty()
 	    if (mult.type == Mult.Type.QUESTION || cs.partialInput.isEmpty())
 	        return new Alt(Tools.l(new Nonterminal("<NULL>", -1),
 	                               mult.operand)).accept(this);
 	    // otherwise, '*' desugars to Alt(<NULL>, Mult(x, PLUS))
 	    return new Alt(Tools.l(new Nonterminal("<NULL>", -1),
 	                           new Mult(mult.operand, Mult.Type.PLUS)))
 	                .accept(this);
         }
         @Override
         public Boolean visit(Nonterminal nonterm) {
             // for <NUMBER> look also for <digit> (<digit> / <digit>)?
 	    // note that we don't try to match <number> or <fraction> or
 	    // <digit_greater_than_two> here; <NUMBER> is the only thing
 	    // which can match a TokenType.FRACTION
 	    if ((!cs.partialInput.isEmpty()) &&
                 cs.partialInput.head.type==Token.TokenType.FRACTION &&
 		nonterm.ruleName.equals("NUMBER")) {
 		matchterm(cs.partialInput.head.text);
 		return true;
             }
             // special match for <digit>, <EOF> (others?)
             if (nonterm.ruleName.equals("EOF"))
                 // <EOF> matches iff we've grabbed all the partial input.
                 return cs.partialInput.isEmpty();
             if (nonterm.ruleName.equals("<NULL>"))
                 return true; // trivial match
             // if "no terminals past partialInput yet" then we'll grab the
             // nt from the GrmDB and recurse; otherwise we'll return
             // "<"+nonterm.prettyname+">" in the completion string & true.
             // prettyName==null means "never show this nonterminal to the user
             // in a completion"
             boolean expandNT = true;
             // don't expand non terminal if we can make progress without it
             if (cs.matchedTerminal) expandNT = false;
             // don't expand non terminal if it's a number
             if (cs.partialInput.isEmpty()) {
                 if (nonterm.ruleName.equals("number") ||
                     nonterm.ruleName.equals("times") ||
                     nonterm.ruleName.equals("digit_greater_than_two"))
                     expandNT = false;
             }
             // always expand non terminal if the pretty name is null
             if (nonterm.prettyName==null) expandNT = true;
             // okay, what's the verdict?
             if (!expandNT) {
                 cs.pushCompletion("<"+nonterm.prettyName+">");
                 return true;
             }
             Grm g = rules.get(nonterm.ruleName);
            if (g==null) return false; // XXX MISSING RULE
             return g.accept(this);
         }
         @Override
         public Boolean visit(Terminal term) {
             // do we match the nonterminal, or not?
             if (cs.partialInput.isEmpty() ||
                 cs.partialInput.head.matches(term.literal)) {
                 // okay, we match.
                 matchterm(term.literal);
                 return true;
             }
             // not a match
             return false;
         }
         // helper function for Concat (and for Mult, where + and * turns
         // into Concat)
         private void transferNextTo(List<Integer> n) {
             for (Integer i: this.cs.nextState)
                 n.add(i);
             this.cs.nextState = LL.NULL();
         }
         // helper function for Terminal/Nonterminal matches
         private void matchterm(String s) {
             if (cs.partialInput.isEmpty()) cs.matchedTerminal=true;
             cs.popInput(); // safe even if cs.partialInput.isEmpty()
             cs.pushCompletion(s);
         }
     }
 }
