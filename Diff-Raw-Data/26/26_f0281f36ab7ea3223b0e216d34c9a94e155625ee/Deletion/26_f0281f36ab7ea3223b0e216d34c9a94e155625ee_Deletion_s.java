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
 
 package gpframework.algorithms.components.mutations;
 
 import gpframework.algorithms.components.Mutation;
 import gpframework.common.exceptions.ArityException;
 import gpframework.problems.Problem;
 import gpframework.program.Function;
 import gpframework.program.Program;
 import gpframework.program.Term;
 import gpframework.program.Terminal;
 
 /** 
  * Mutation operator which deletes a terminal and replaces its n-ary parent 
  * with a function of arity n-1. If its parent is binary then it replaces its 
  * parent with another terminal. 
  */
 public class Deletion extends Mutation {
 
     /**
      * Constructor.
      * 
      * @param problem problem linked to this mutation (allows to generate Terms)
      */
     public Deletion(Problem problem) 
     {
         super(problem);
     }
     
     @Override
     public void apply(Terminal t)
     {
         Program program = t.getProgram();
        
         // Special handling if term being mutated is root
         if (t.isRoot())
         {
             // Replace with null terminal
             Term nil = problem.getNull();
             nil.setProgram(program);
             program.setRoot(nil);        
             return;
         }
         
         // Get parent of this terminal
         Function parent = t.getParent();
         
         // Special handling if parent is binary
         if (parent.arity() == 2) 
         {
             // Replace parent with non-deleted descendant
             Term s = parent.getDescendant(t.getParentIndex() == 0 ? 1 : 0);
             
             // If parent was root, replace root with sibling otherwise
             // replace parent with sibling and do not reset root
            if (parent.isRoot())
                 t.getProgram().setRoot(s);
            else
                 parent.getParent().replaceDescendant(parent, s);
         } 
         else 
         {
             // Find random n-1-arity function to replace parent
             Function n = null;
             try {
                 n = problem.getFunctionByArity(parent.arity()-1);
             } catch (ArityException ex) {
                 // Fail gently if no such functions are found
                 System.err.println("Can't find functions of arity " + (parent.arity()-1) + " to replace " + parent);
                 return;
             }
             
             // Assign descendants to new function
             for( int i = 0; i < n.arity(); i++)
                 if (parent.getDescendant(i) != t)
                     n.setDescendant(i, parent.getDescendant(i));
             
             // Replace parent with (n-1)-ary function
             parent.getParent().replaceDescendant(parent, n);
         }
     }
 
     @Override
     public void apply(Program program) 
     {    
         // If the program is empty (e.g. contains only a null node)
         if (program.isEmpty())
             return;
 
         // Otherwise apply mutation on random terminal
         program.getRandomTerminal().applyMutation(this);
     }
 
     @Override
     public Object clone() 
     {
         return new Deletion(problem);
     }
 }
