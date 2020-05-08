 /*
     SaxonStyleFrame.java
 
     @author: <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
     Date: March 20, 2001
 
  */
 
 package xslt.debugger.saxon;
 
 import java.util.Enumeration;
 import java.util.ArrayList;
 
 import com.icl.saxon.style.StyleElement;
 import com.icl.saxon.Context;
 
 import xslt.debugger.Variable;
 import xslt.debugger.StyleFrame;
 import xslt.debugger.Manager;
 
 public class SaxonStyleFrame extends StyleFrame
 {
   Context context;
   StyleElement element;
   int frameId = -1;
   ArrayList localVariables = null;
   ArrayList globalVariables = null;
   
   public SaxonStyleFrame(Context context,
                          StyleElement element,
                          String name,
                          String filename,
                          int line,
                          int column,
                          Manager manager)
   {
     super(name, filename, line, column, manager);
     this.context = context;
     this.element = element;
   }
 
   /**
    * Get the local variables of this style frame.
    *
    * @return an <code>ArrayList</code> value
    */
   public ArrayList getLocalVariables()
   {
     if (localVariables == null) {
       getVariableNames();
       return localVariables;
     }
    return localVariables;
   }
 
  /**
   * Get the global variables of the stylesheet. The Saxon API
   * requires a StyleElement object to be able to get the global
   * variables, even though they are kept at the stylesheet level.
   *
   * @return an <code>ArrayList</code> value
   */
   public ArrayList getGlobalVariables()
   {
     if (globalVariables == null) {
       getVariableNames();
       return globalVariables;
     }
    return globalVariables;
   }
 
   /**
    * Get the value of element.
    * @return value of element.
    */
   public StyleElement getElement()
   {
     return element;
   }
 
   /**
    * <code>getVariableNames</code> collects the variable names from
    * Saxon and places them in the internal data structures for later
    * reuse.
    */
   protected void getVariableNames()
   {
     // First initialize the frameId
     if (frameId == -1)
       frameId = context.getBindery().getFrameId();
 
     localVariables = new ArrayList();
     globalVariables = new ArrayList();
     
     // Now get the local variables. Saxon's getVariableNames() method
     // returns an array of two Enumerations, the first one for global
     // variables, the second one for local variables.
     Enumeration[] e = element.getVariableNames();
     
     while (e[0].hasMoreElements()) {
       String name = extractName((String)e[0].nextElement());
       Variable variable
         = new SaxonVariable(name, context, element, frameId, this);
       globalVariables.add(variable);
     }
 
     while (e[1].hasMoreElements()) {
       String name = extractName((String)e[1].nextElement());
       Variable variable
         = new SaxonVariable(name, context, element, frameId, this);
       localVariables.add(variable);
     }
   }
 
   /**
    * Extract the name from the full name (which looks like URI^name).
    *
    * @param fullName a <code>String</code> value
    * @return a <code>String</code> value
    */
   protected String extractName(String fullName)
   {
     int index = fullName.lastIndexOf("^");
 
     if (index == -1)
       return fullName;
     else
       return fullName.substring(index + 1, fullName.length());
   }
 }
