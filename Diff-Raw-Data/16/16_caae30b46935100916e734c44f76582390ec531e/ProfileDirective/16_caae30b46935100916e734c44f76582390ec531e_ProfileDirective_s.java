 
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
 
 import java.util.*;
 import java.io.*;
 import org.webmacro.util.*;
 import org.webmacro.*;
 
 
 /**
   * ProfileDirective wraps a block of text on a template and 
   * times it. The timing information can be inspected using a
   * tool like the examples/Profile servlet. 
   * <p>
   * @see Macro
   */
 final class ProfileDirective implements Directive, Visitable
 {
 
    /**
      * This is the block we include if myCondition is not null
      */
    final private Macro myBlock;
 
    /**
      * This is the name associated with the timing statistics
      */
    final private String myName;
 
    /**
      * Create a new profile timing block
      */
    ProfileDirective(String name, Macro block) 
    { 
       myName = name;
       myBlock = block;
    }
 
    public static final Object build(BuildContext rc, Object name, Macro block)
       throws BuildException
    {
       if (name instanceof Macro) {
          throw new BuildException(
             "Profile name must be a static string, not a dynamic macro");
       }
       if (Flags.PROFILE) {
          return new ProfileDirective(name.toString(), block);
       } else {
         return null;
       }
    }
 
    /**
      * Evaluate the current macro and return it as a string. Same
      * basic operation as calling write.
      * @exception PropertyException is required data is missing
      */ 
    public Object evaluate(Context context) throws PropertyException
    {
       Object ret;
       boolean timing = context.isTiming();
       if (timing) context.startTiming(myName);
       ret = myBlock.evaluate(context);
       if (timing) context.stopTiming();
       return ret;
    }  
 
    /**
      * Interpret the directive and write it out to the supplied stream.
      * <p>
      * @exception PropertyException if required data was missing from context
      * @exception IOException if we could not successfully write to out
      */
    public void write(FastWriter out, Context context) 
       throws PropertyException, IOException
    {
       boolean timing = context.isTiming();
       if (timing) context.startTiming(myName);
       myBlock.write(out,context);
       if (timing) context.stopTiming();
    }
 
    public void accept(TemplateVisitor v) {
       v.beginDirective("profile");
       v.visitDirectiveArg("ProfileBlock", myBlock);
       v.endDirective();
    }
    
 }
 
