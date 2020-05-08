 /**
  * 
  */
 package openscriptures.text;
 
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.NoSuchElementException;
 import java.util.UUID;
 
 
 /**
  * @author Neal Audenaert
  */
 public abstract class AbstractTokenSequence implements TokenSequence {
 
     
     protected AbstractTokenSequence() {
     }
     
     /**
      * 
      * @param t
      * @return
      */
     private boolean isSameWork(Token t) {
         UUID myId = this.getWork().getId();
         UUID tId = t.getWork().getId();
         
         return tId.equals(myId);
     }
     
     /**
      * Checks if a relative index is within the bounds of this <tt>TokenSequence</tt>.
      * @param index the index to check
      * 
      * @throws IndexOutOfBoundsException if the supplied index is out of bounds
      */
     private void checkIndexInBounds(int index) {
         if (index < 0 || index >= this.size())
             throw new IndexOutOfBoundsException();
     }
     
     /**
      * 
      * @param t
      * @throws InvalidTokenException
      */
     protected void checkWork(Token t) throws InvalidTokenException {
         if (isSameWork(t))
             throw new InvalidTokenException(
                     "The token's work does not match this sequence.", t);
     }
     
     /**
      * Indicates whether or not this <tt>TokenSequence</tt> has tokens in it.
      * 
      * @return <tt>true</tt> if this structure has no textual content (i.e. it is a place 
      *      marker).
      * 
      * @see openscriptures.text.TokenSequence#isEmpty()
      */
     @Override
     public boolean isEmpty() {
         return this.size() == 0;
     }
     
     /**
      * 
      * @see openscriptures.text.TokenSequence#size()
      */
     @Override
     public int size() {
         return this.getEnd() - this.getStart();
     }
     
     /** 
      * Returns the textual contents of this <tt>Structure</tt> or the empty string 
      * if it is empty.
      * 
      * @see openscriptures.text.TokenSequence#getText()
      */
     @Override
     public String getText() {
         if (this.isEmpty())
             return null;
 
         StringBuilder sb = new StringBuilder();
         Iterator<Token> i = this.iterator();
         while (i.hasNext()) {
             sb.append(i.next().getText());
         }
         
         return sb.toString();
     }
 
     /** 
      * Determines whether the supplied token is contained in this sequence.
      * @see openscriptures.text.TokenSequence#contains(openscriptures.text.Token)
      */
     @Override
     public boolean contains(Token token) {
         long pos = token.getPosition();
         return isSameWork(token) && (this.getStart() <= pos && pos < this.getEnd());
     }
     
     /**
      * Returns the index of the supplied token relative to this structure.
      * 
      * @see openscriptures.text.TokenSequence#indexOf(openscriptures.text.Token)
      */
     @Override
     public int indexOf(Token token) {
         return this.contains(token) 
             ? token.getPosition() - this.getStart()
             : -1;
     }
 
     /** 
      * Returns the <tt>Token</tt> at the specified index relative to this structure.
      *  
      * @param index The index of the token to retrieve relative to this <tt>Structure</tt>
      * 
      * @throws IndexOutOfBoundsException if the index is out of range 
      *      (index < 0 || index >= size())
      *      
      * @see openscriptures.text.TokenSequence#get(int)
      */
     @Override
     public Token get(int index) {
         checkIndexInBounds(index);
         
         return this.getWork().get(this.getStart() + index);
     }
 
     /* (non-Javadoc)
      * @see openscriptures.text.TokenSequence#iterator()
      */
     @Override
     public Iterator<Token> iterator() {
         return new TokenSequenceIterator(this);
     }
 
     /* (non-Javadoc)
      * @see openscriptures.text.TokenSequence#iterator(int)
      */
     @Override
     public Iterator<Token> iterator(int startAt) {
         return new TokenSequenceIterator(this, startAt);
     }
 
     /* (non-Javadoc)
      * @see openscriptures.text.TokenSequence#listIterator()
      */
     @Override
     public ListIterator<Token> listIterator() {
         return new TokenSequenceIterator(this);
     }
 
     /* (non-Javadoc)
      * @see openscriptures.text.TokenSequence#listIterator(int)
      */
     @Override
     public ListIterator<Token> listIterator(int startAt) {
         return new TokenSequenceIterator(this, startAt);
     }
 
     
 
     /* (non-Javadoc)
      * @see openscriptures.text.TokenSequence#subSequence(int, int)
      */
     @Override
     public TokenSequence subSequence(int fromIndex, int toIndex) {
         checkIndexInBounds(fromIndex);
         checkIndexInBounds(toIndex);
         
         
         return new SubSequence(this, fromIndex + this.getStart(), toIndex + this.getStart());
     }
 
     /* (non-Javadoc)
      * @see openscriptures.text.TokenSequence#toArray()
      */
     @Override
     public Token[] toArray() {
         Token[] tokens = new Token[this.size()];
         
         int ix = 0;
         Iterator<Token> i = this.iterator();
         while (i.hasNext()) {
             tokens[ix++] = i.next();
         }
         
         return tokens;
     }
 
     /** 
      * Analogous to the <tt>List.toArray(T[]) method, this returns an array containing all 
      * of the token in this structure in proper sequence (from first to last token). If the 
      * list fits in the specified array, it is returned therein. Otherwise, a new array 
      * is allocated with the size of this structure. If the tokens fits in the specified 
      * array with room to spare (i.e., the array has more tokens than the structure), the 
      * token in the array immediately following the end of the list is set to null. 
      * 
      * <p>This may, under certain circumstances, be used to save allocation costs.
      * 
      * @param tokens the array into which the tokens of this structure are to be stored, 
      *      if it is big enough; otherwise, a new array is allocated for this purpose.
      * @return an array containing the tokens of this structure
      * 
      * @throws NullPointerException if the specified array is <tt>null</tt>
      * 
      * @see openscriptures.text.TokenSequence#toArray(openscriptures.text.Token[])
      */
     @Override
     public Token[] toArray(Token[] tokens) {
         if (tokens.length < this.size())
             tokens = new Token[this.size()];
         
         int ix = 0;
         Iterator<Token> i = this.iterator();
         while (i.hasNext()) {
             tokens[ix++] = i.next();
         }
         
         if (tokens.length > this.size())
             tokens[this.size()] = null;
         
         return tokens;
     }
     
 
 //========================================================================================
 // INNER CLASS
 //========================================================================================
       
     /**
      * 
      * @author Neal Audenaert
      */
     protected static class TokenSequenceIterator implements ListIterator<Token> {
 
     //========================================================================================
     // MEMBER VARIABLES
     //========================================================================================
 
         private int ix = -1;
          private TokenSequence s = null;
          
      //========================================================================================
      // CONSTRUCTORS
      //========================================================================================
 
          TokenSequenceIterator(TokenSequence s) {
              this.s = s;
          }
          
          TokenSequenceIterator(TokenSequence s, int startAt) {
              this.s = s;
              this.ix = startAt;
              
              adjustStartValue();
          }
          
          /**
           * Adjusts the starting value as needed to make sure that it is in bounds.
           */
          private void adjustStartValue() {
              assert (ix >= 0) && (ix < s.size()) : "Starting index is out of bounds";
              
              if (ix < 0) 
                  this.ix = 0;
              else if (ix >= s.size())
                  this.ix = s.size() - 1;            
          }
 
      //========================================================================================
      // METHODS
      //========================================================================================
 
          /* (non-Javadoc)
           * @see java.util.ListIterator#hasNext()
           */
          @Override
          public boolean hasNext() {
              return ix < (s.size() - 1);
          }
 
          /* (non-Javadoc)
           * @see java.util.ListIterator#hasPrevious()
           */
          @Override
          public boolean hasPrevious() {
              return ix > 0;
          }
 
          /* (non-Javadoc)
           * @see java.util.ListIterator#next()
           */
          @Override
          public Token next() {
              if (!this.hasNext())
                  throw new NoSuchElementException();
              
              return s.get(++ix);
          }
 
          /* (non-Javadoc)
           * @see java.util.ListIterator#nextIndex()
           */
          @Override
          public int nextIndex() {
              return hasNext() ? ix + 1 : s.size();
          }
 
          /* (non-Javadoc)
           * @see java.util.ListIterator#previous()
           */
          @Override
          public Token previous() {
              if (!this.hasPrevious())
                  throw new NoSuchElementException();
              
              return s.get(--ix);
          }
 
          /* (non-Javadoc)
           * @see java.util.ListIterator#previousIndex()
           */
          @Override
          public int previousIndex() {
              return hasPrevious() ? ix - 1 : -1;
          }
 
          //========================================================================================
          // UNIMPLEMENTED METHODS
          //========================================================================================
          
          /** Unsupported operation
           * @see java.util.ListIterator#add(java.lang.Object)
           */
          @Override
          public void add(Token t) {
              throw new UnsupportedOperationException();
          }
 
          /** Unsupported operation
           * @see java.util.ListIterator#set(java.lang.Object)
           */
          @Override
          public void set(Token arg0) {
              throw new UnsupportedOperationException();
          }
          
          /** Unsupported operation
           * @see java.util.ListIterator#remove()
           */
          @Override
          public void remove() {
              throw new UnsupportedOperationException();
          }
      }
     
     private static class SubSequence extends AbstractTokenSequence {
         /** The index of the first token (inclusive) in the sequence. */ 
         protected int start;
         
         /** The index of the last token (exclusive) in the sequence. */
         private int end;
         
         TokenSequence s;
         
         /**
          * 
          * @param s
          * @param start
          * @param end
          */
         SubSequence(TokenSequence s, int start, int end) {
             String msg = 
                 "Invalid sequence range: the start token must not come after the end token.";
             assert (start <= end) : msg;
             if (start > end)
                 throw new InvalidTokenException(msg);
             
             this.start = start;
             this.end = end;
             
             this.s = s;
         }
         
         public Work getWork() {
             return s.getWork();
         }
         
         public int getStart() {
             return start;
         }
         
         public int getEnd() {
             return end;
         }
     }
 }
