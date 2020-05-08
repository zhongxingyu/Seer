 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.engine;
 
 import org.webmacro.util.java2.*;
 import java.util.*;
 import java.io.*;
 import org.webmacro.util.*;
 import org.webmacro.*;
 
 
 /**
   * An if directive is used to include a block only when a condition 
   * is true. Structurally it has a condition, followed by a block, 
   * followed by an optional "#else" statmeent and another block.
   * 
   * Here is an example:<pre>
   *
   *     #if ($userID) {
   *        Your user id is: $userID
  *     } #else {
   *        &lt;a href="register"&gt;Click here to register&lt;/a&gt;
   *     }
   *
   * </pre> The condition is true if it evaluates to a non-null value. The 
   * #if block will be included if (and only if) the condition evaluates
   * to a non-null value. The #else block will be included if (and
   * only if) the condition evaluates to null.
   * <p>
   * When evaluating whether a condition is null or not, the IfDirective
   * will check whether it is a Macro. If it is a Macro, it calls 
   * Macro.evaluate() and checks whether the return value is null. If 
   * it is not a macro, then it is true or false simply based on whether
   * the value passed in was actually null itself.
   * <p>
   * @see Macro
   */
 final class IfDirective implements Directive
 {
 
    /**
      * We are true if this object is defined and not null
      */
    final private Condition myCondition;
 
    /**
      * This is the block we include if myCondition is not null
      */
    final private Macro myIfBlock;
 
    /**
      * This is the block we include if myCondition is null
      */
    final private Macro myElseBlock;
 
    /**
      * Create a new IfCondition
      * <p>
      * @param condition True when this evaluates to a non-Null value
      * @param ifBlock included when condition is non-null
      * @param elseBlock (may be null) included when condition is null
      */
    IfDirective(Condition cond, Macro ifBlock, Macro elseBlock) 
    { 
        myCondition = cond; 
        myIfBlock =  ifBlock;
        myElseBlock =  elseBlock;
    }
 
    public static final String[] getSubDirectives() {
       String[] deps = { "else" };
       return deps;
    }
 
    public static final Object build(BuildContext rc,
          Condition cond, Macro body, Object elseDir)
       throws BuildException
    {
       if (cond instanceof Macro) {
          Macro elseMacro = (elseDir != null) ? 
             MacroAdapter.createMacro(elseDir) : null;
          return new IfDirective(cond, body, elseMacro);  
       } else {
          return (cond.test(rc)) ? body : elseDir;
       }
    }
 
    public static final String[] getSubordinateNames()
    {
       String[] names = { "else" };
       return names;
    }
 
 
    /**
      * Evaluate the current macro and return it as a string. Same
      * basic operation as calling write.
      * @exception InvalidContextException is required data is missing
      */ 
    public Object evaluate(Object context)
       throws InvalidContextException
    {
       try {
          StringWriter sw = new SizedStringWriter(512);
          write(sw,context);
          return sw.toString();
       } catch (IOException e) {
          Engine.log.exception(e);
          Engine.log.error(
                "If: evaluate got IO exception on write to StringWriter");
          return "";
       }
    }  
 
    /**
      * Interpret the directive and write it out to the supplied stream.
      * <p>
      * @exception InvalidContextException if required data was missing from context
      * @exception IOException if we could not successfully write to out
      */
    public void write(Writer out, Object context) 
       throws InvalidContextException, IOException
    {
 
       if (Log.debug) {
          Engine.log.debug("If: evaluating #if condition:" + myCondition);
       }
 
       if (myCondition.test(context)) 
       {
          if (Log.debug) {
             Engine.log.debug("If: writing myIfBlock");
          }
 	 if (myIfBlock != null) {
             myIfBlock.write(out, context);
          } else {
 	    Engine.log.warning("If: Block for an #if directive was null");
 	 }
       } else { 
 	 if (myElseBlock != null) {
             if (Log.debug) Engine.log.debug("If: writing myElseBlock: " + myCondition);
             myElseBlock.write(out, context);
 	 }
       } 
    }
 }
 
 
