 package net.cyklotron.cms.search.analysis;
 
 import java.io.IOException;
 
 import org.apache.lucene.analysis.TokenFilter;
 import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
 import org.apache.lucene.util.Attribute;
 import org.apache.lucene.util.AttributeImpl;
 
 public class StemFilter
     extends TokenFilter
 {
     private final Stemmer stemmer;
 
    private final CharTermAttribute charTermAttribute;
 
     private final PositionIncrementAttribute posIncAtt;
 
     private final StateAttribute stateAtt;
 
     public StemFilter(TokenStream in, Stemmer stemmer)
     {
         super(in);
         this.stemmer = stemmer;
        charTermAttribute = addAttribute(CharTermAttribute.class);
         posIncAtt = addAttribute(PositionIncrementAttribute.class);
         stateAtt = addAttribute(StateAttribute.class);
     }
 
     @Override
     public boolean incrementToken()
         throws IOException
     {
         if(stateAtt.getState() == State.ORIGINAL)
         {
             if(input.incrementToken())
             {
                 posIncAtt.setPositionIncrement(0);
                 stateAtt.setState(State.STEM);
                 return true;
             }
             else
             {
                 return false;
             }
         }
         else
         {
            String term = charTermAttribute.toString();
             String s = stemmer.stem(term);
             // If not stemmed, don't waste the time adjusting the token.
             if((s != null) && !s.equals(term))
             {
                charTermAttribute.copyBuffer(s.toCharArray(), 0, s.length());
             }
             posIncAtt.setPositionIncrement(1);
             stateAtt.setState(State.ORIGINAL);          
             return true;
         }
     }
 
     private enum State
     {
         ORIGINAL, STEM
     }
 
     private interface StateAttribute
         extends Attribute
     {
         public State getState();
 
         public void setState(State state);
     }
 
     public static class StateAttributeImpl
         extends AttributeImpl
         implements StateAttribute, Cloneable
     {
         private static final long serialVersionUID = 4003078802203028706L;
         
         private State state = State.ORIGINAL;
 
         public State getState()
         {
             return this.state;
         }
 
         public void setState(State state)
         {
             this.state = state;
         }
 
         @Override
         public void clear()
         {
             state = State.ORIGINAL;
         }
 
         @Override
         public void copyTo(AttributeImpl target)
         {
             ((StateAttribute)target).setState(this.state);
         }
 
         @Override
         public boolean equals(Object other)
         {
             return ((StateAttribute)other).getState() == this.state;
         }
 
         @Override
         public int hashCode()
         {
             return state.hashCode();
         }
     }
 }
