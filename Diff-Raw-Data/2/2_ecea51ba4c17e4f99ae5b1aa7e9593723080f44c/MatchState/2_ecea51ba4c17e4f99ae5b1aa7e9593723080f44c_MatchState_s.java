 /*
     jaiml - java AIML library
     Copyright (C) 2004-2005  Kim Sullivan
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package aiml;
 
 import java.util.*;
 import aiml.context.*;
 
 /**
  * <p>The MatchState class stores complete information about the current match
  * state. It originally started only as a wrapper for certain variables that
  * would be passed down in the recursion tree, but currently it tries to store
  * as much information as possible. In future versions, it might even contain a
  * node traversal stack so that matching can be resumed even after
  * exiting the recursion tree.</p>
  * @author Kim Sullivan
  * @version 1.0
  */
 
 public class MatchState {
 
   /** The currently matched context */
   public int context;
 
   /** The contextStack list stores the history of context traversal, used for
    *  backtracking.
    */
   private LinkedList contextStack = new LinkedList();
 
   /** The current depth in the context */
   public int depth;
 
   /**
    * An array of Strings that represent the individual state of the context variables
    * during matching.
    */
   String contextValues[] = new String[ContextInfo.getCount()];
 
   /**
    * An array of lists, each list represents wildcards from a context
    */
   List wildcards[] = new List[ContextInfo.getCount()];
 
   /**
    * This contains the result of the matching.
    */
   private Object result;
 
   /**
    * <p>This inner class represents a single matched wildcard inside a context.
    * Normally, only the beginning and ending positions of the wildcard are stored to
    * save resources during matching. Once the actual wildcard value is requested
    * (this could even be during the process of matching), the wildcard string
    * is constructed.<p>
    *
    * <p>To maintain consistency during template processing,
    * a copy of the context variable is stored in the
    * MatchState, so that a
    * wildcard request after a set/get/srai (that can theoretically change the
    * "live" context variable still produces the same results.</p>
    *
    * @author Kim Sullivan
    * @version 1.0
    */
   public class Wildcard {
     /**
      * The beginning index of a wildcard in a string
      */
     private int beginIndex;
 
     /**
      * The ending index of a wildcard in a string
      */
     private int endIndex;
 
     /**
      * The context that this wildcard has been matched in
      */
     private int context;
 
     /**
      * Creates a new wildcard with length 0
      * @param context The context this wildcard applies to
      * @param beginIndex The starting position of this wildcard
      */
     public Wildcard(int context, int beginIndex) {
       this.beginIndex = beginIndex;
       this.endIndex = beginIndex;
       this.context = context;
     }
 
     /**
      * Increases the size of the string matched by the wildcard by one
      */
     public void grow() {
       endIndex++;
     }
 
     /**
      * Increases the size of the string matched by the wildcard
      * @param length how much more characters are matched by the wildcard
      */
     public void grow(int length) {
       endIndex += length;
     }
 
     public void growRest() {
      endIndex = contextValues[context].length() - 1;
     }
 
     /**
      * Returns the length of this wildcard
      * @return the length of this wildcard
      */
     public int getLength() {
       return endIndex - beginIndex;
     }
 
     /**
      * Returns the starting index of the wildcard
      * @return the starting index of the wildcard
      */
     public int getBeginIndex() {
       return beginIndex;
     }
 
     /**
      * Produces the actual wildcard value.
      * @return the wildcard value
      */
     public String getValue() {
       return contextValues[context].substring(beginIndex, endIndex);
     }
 
     /**
      * Returns a string representation of this wildcard. Information also includes
      * the start, length and context of the wildcard
      * @return a string representation of this wildcard
      */
     public String toString() {
       return "WC{" + context + ":(" + getBeginIndex() + "," + getLength() +
           ")=\"" + getValue() + "\"}";
     }
   };
 
   /**
    * Creates a new MatchState object, makes a snapshot of the context variables.
    */
   public MatchState() {
     if (ContextInfo.getCount() <= 0) {
       throw new NoContextPresentException();
     }
     for (int i=0;i<ContextInfo.getCount();i++)
       contextValues[i] = ContextInfo.getContext(i).getValue();
   };
 
   /**
    * Adds a new context to the match state.
    * @param context The new context
    */
   public void addContext(int context) {
     contextStack.addLast(new Integer(this.context));
     this.context = context;
     depth = 0;
   }
 
   /**
    * <p>Drops the current context and restores the last. The reason why this isn't
    * called removeContext() is that the context's cached value is retained.</p>
    *
    * <p><i>Note to self III:</i> More a side note, really...a Context classes
    * backed by an array might be interesting in some cases.</p>
    */
   public void dropContext() {
     //shouldn't this be error-checked? The default NoSuchElementException is probably enough though...
     this.context = ( (Integer) contextStack.removeLast()).intValue();
     depth = getContextValue().length();
   }
 
   /**
    * Add a new wildcard to the current context at the current depth
    * @return Wildcard
    */
   public Wildcard addWildcard() {
     return addWildcard(context,depth);
   }
 
   /**
    * Add a new wildcard to s context at a certain depth
    * @param context the context
    * @param depth the depth (the starting index of the wildcard)
    * @return Wildcard
    */
 
   public Wildcard addWildcard(int context, int depth) {
     Wildcard wc = new Wildcard(context, depth);
     if (wildcards[context] == null) {
       wildcards[context] = new ArrayList();
     }
     wildcards[context].add(wc);
     return wc;
   }
 
   /**
    * Get the last wildcard. The matching algorithm processes only one
    * wildcard at a time.
    * @return Wildcard
    */
   public Wildcard getWildcard() {
     return getWildcard(context,wildcards[context].size() - 1);
   }
 
   /**
    * <p>Return a wildcard. After matching has finished (and in some special
    * contexts, even when it hasn't), it is natural to want acess to all the
    * matched wildcards.</p>
    *
    * <p>Currently, this method returns null for wildcards out of range. It should probably
    * throw an exception for contexts out of range. It constructs a "full"
    * wildcard for contexts that haven't been matched.</p>
    * @param context The context
    * @param index The index of the wildcard
    * @return The wildcard, or null if there is none
    */
   public Wildcard getWildcard(int context, int index) {
     if (wildcards[context] == null) {
       if (index!=1) return null;
       Wildcard wc = addWildcard(context,0);
       wc.growRest();
       return wc;
     }
     if (index > wildcards[context].size() || index < 0) {
       return null;
     }
     return (Wildcard) wildcards[context].get(index);
   }
 
   /**
    * Removes the last wildcard during matching.
    */
   public void removeWildcard() {
     wildcards[context].remove(wildcards[context].size() - 1);
   }
 
   /**
    * Returns the value of the current context.
    * @return the value of the current context
    */
   public String getContextValue() {
     return contextValues[context];
   }
 
   /**
    * Set the result object.
    * @param o the result object
    */
   public void setResult(Object o) {
     result = o;
   }
 
   /**
    * Get the result object
    * @return the result object
    */
   public Object getResult() {
     return result;
   }
 
   /**
    * Return a string representation of the match state. If a match was found,
    * return the objects' toString() value, otherwise details about the match state.
    * Also includes a list of current wildcards.
    * @return a string representation of the match state.
    */
   public String toString() {
     StringBuffer sb = new StringBuffer();
     if (result == null) {
       sb.append("[CONTEXT]" + context + "[DEPTH]" + depth + "\n" +
                 "[CVALUE]" + getContextValue() + "\n");
     }
     else {
       sb.append("[RESULT]" + result + "\n");
     }
     sb.append("[WILDCARDS]\n");
     for (int i = 0; i < wildcards.length; i++) {
       if ( (wildcards[i] != null) && (wildcards[i].size() > 0)) {
         sb.append("<" + i + ">" + wildcards[i] + "\n");
       }
     }
 
     return sb.toString();
   }
 
 }
