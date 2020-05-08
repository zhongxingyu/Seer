 /*  Copyright (c) 2012 Tommaso Urli, Markus Wagner
  * 
  *  Tommaso Urli    tommaso.urli@uniud.it   University of Udine
  *  Markus Wagner   wagner@acrocon.com      University of Adelaide
  *
  *  Permission is hereby granted, free of charge, to any person obtaining
  *  a copy of this software and associated documentation files (the
  *  "Software"), to deal in the Software without restriction, including
  *  without limitation the rights to use, copy, modify, merge, publish,
  *  distribute, sublicense, and/or sell copies of the Software, and to
  *  permit persons to whom the Software is furnished to do so, subject to
  *  the following conditions:
  *
  *  The above copyright notice and this permission notice shall be
  *  included in all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 
 package gpframework.program;
 
 import gpframework.algorithms.components.Mutation;
 import gpframework.common.Utils;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * This class represents any subtree of a program, be it a terminal or a join.
  */
 public abstract class Term implements Cloneable
 {        
     /** Reference to creating factory */
     protected Object factory;
     
     /** Node ordinal */
     protected static Integer ordinal = 0;
     
     /** Id of this node */
     protected Integer ID;
     
     /** Parent program */
     protected Function parent = null;
     
     protected  Program program;
     
     /** Position in the list of parent's descendants */
     protected int parentIndex;
     
     public Term()
     {
         ID = Term.ordinal++;
     }
     
     /**
      * Retrieves the parent node of this program.
      * 
      * @return the parent
      */
     public Function getParent() {
         return parent;
     }
 
     /**
      * Sets a new parent node for this program.
      * 
      * @param parent the parent to set
      * @param parentIndex position in the list of parent's descendant
      */
     public void setParent(Function parent, int parentIndex) 
     {
         this.parent = parent;
         this.program = parent.getProgram();
         this.parentIndex = parentIndex;
     }
 
     /**
      * Executes the program.
      * 
      * @return the result of the execution 
      */
     public abstract Object parse();
 
     /**
      * Retrieves the root node of this program.
      * 
      * @return the root
      */
     public Term getRoot() 
     {
         // Go up the tree until you find that parent == null
         return getProgram().getRoot();
     }
     
     /**
      * Check if this term is the root.
      * 
      * @return true if this term is the root
      */
     public boolean isRoot()
     {
         return getRoot() == this;
     }
         
     /**
      * Get all the terminals of this (sub)program.
      * 
      * @return a list of terminals
      */
     public abstract List<Terminal> getTerminals();
     
     /**
      * Get all the joins of this (sub)program.
      * 
      * @return a list of joins
      */
     public abstract List<Function> getFunctions();
         
     /**
      * Returns all the nodes in the program tree.
      * 
      * @return a list of all nodes in the program tree
      */
     public List<Term> getTerms() 
     {
         List<Term> nodes = new ArrayList<Term>();
         nodes.addAll(getTerminals());
         nodes.addAll(getFunctions());
         Collections.shuffle(nodes, Utils.random);
         return nodes;
     }
     
     public Term getRandomTerm()
     {
         List<Term> terms = getTerms();
         return terms.get(Utils.random.nextInt(terms.size()));
     }
 
     public Function getRandomFunction()
     {
         List<Function> function = getFunctions();
         return function.get(Utils.random.nextInt(function.size()));
     }
         
     public Terminal getRandomTerminal()
     {
         List<Terminal> terminals = getTerminals();
         return terminals.get(Utils.random.nextInt(terminals.size()));
     }
     
     /**
      * Get this object's position in the parent's list of descendants.
      * 
      * @return the parentIndex
      */
     public int getParentIndex() {
         return parentIndex;
     }
     
     @Override
     public abstract Object clone();
 
     /**
      * Get the program of which this term is a part.
      * 
      * @return the program
      */
     public Program getProgram() 
     {
         return program;
     }
 
     /**
      * Set the program for this term.
      * 
      * @param program 
      */
     public void setProgram(Program program) 
     {
         this.program = program;
     }
     
     /**
      * Apply a mutation to this term.
      * 
      * @param m the mutation to apply
      */
     public void applyMutation(Mutation m) 
     {
         m.apply(this);
     }
     
     /**
      * Length of this subprogram.
      * 
      * @return the complexity of this subprogram
      */
     public abstract int complexity();
 
     /**
      * @return the factory
      */
     public Object getFactory() {
         return factory;
     }
 
     /**
      * @param factory the factory to set
      */
     public void setFactory(Object factory) {
         this.factory = factory;
     }
     
     /**
      * Tells if this terminal is null.
      * @return false
      */
     public boolean isNull()
     {
         return false;
     }
 
     @Override
     public boolean equals(Object other)
     {
         return other.hashCode() == hashCode();
     }
 
     @Override
     public int hashCode() 
     {
         int hash = 7;
         hash = 37 * hash + (this.ID != null ? this.ID.hashCode() : 0);
         return hash;
     }
 }
