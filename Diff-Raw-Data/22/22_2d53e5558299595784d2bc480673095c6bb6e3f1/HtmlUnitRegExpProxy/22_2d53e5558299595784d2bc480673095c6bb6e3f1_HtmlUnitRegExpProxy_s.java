 /*
  * Copyright (c) 2002-2008 Gargoyle Software Inc. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. The end-user documentation included with the redistribution, if any, must
  *    include the following acknowledgment:
  *
  *       "This product includes software developed by Gargoyle Software Inc.
  *        (http://www.GargoyleSoftware.com/)."
  *
  *    Alternately, this acknowledgment may appear in the software itself, if
  *    and wherever such third-party acknowledgments normally appear.
  * 4. The name "Gargoyle Software" must not be used to endorse or promote
  *    products derived from this software without prior written permission.
  *    For written permission, please contact info@GargoyleSoftware.com.
  * 5. Products derived from this software may not be called "HtmlUnit", nor may
  *    "HtmlUnit" appear in their name, without prior written permission of
  *    Gargoyle Software Inc.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GARGOYLE
  * SOFTWARE INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.gargoylesoftware.htmlunit.javascript.regexp;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.RegExpProxy;
 import org.mozilla.javascript.ScriptRuntime;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.regexp.NativeRegExp;
 import org.mozilla.javascript.regexp.RegExpImpl;
 
 /**
  * Begins customization of JavaScript RegExp base on JDK regular expression support.
  *
  * @version $Revision$
  * @author Marc Guillemot
  * @author Ahmed Ashour
  */
 public class HtmlUnitRegExpProxy extends RegExpImpl {
     private final RegExpProxy wrapped_;
     
     /**
      * Wraps a proxy to enhance it
      * @param wrapped the original proxy
      */
     public HtmlUnitRegExpProxy(final RegExpProxy wrapped) {
         wrapped_ = wrapped;
     }
     
     /**
      * Use wrapped proxy except for replacement with string arg where it uses Java regular expression
      * {@inheritDoc}
      */
     public Object action(final Context cx, final Scriptable scope, final Scriptable thisObj,
             final Object[] args, final int actionType) {
         
         // in a first time just improve replacement with a String (not a function)
         if (RA_REPLACE == actionType && args.length == 2 && (args[1] instanceof String))
         {
             final String thisString = Context.toString(thisObj);
             final String replacement = ((String) args[1]).replaceAll("\\\\", "\\\\\\\\");
             final Object arg0 = args[0];
             if (arg0 instanceof String) {
                 // arg0 should not be interpreted as a RegExp
                 return StringUtils.replaceOnce(thisString, (String) arg0, replacement);
             }
             else if (arg0 instanceof NativeRegExp) {
                 try {
                     final NativeRegExp regexp = (NativeRegExp) arg0;
                     final String str = arg0.toString();
                     final String regex = readNativeRegExpPattern(regexp);
                     final String flagsStr = StringUtils.substringAfterLast(str, "/");
                     final int flags = jsFlagsToPatternFlags(flagsStr);
                     final Pattern pattern = Pattern.compile(regex, flags);
                     final Matcher matcher = pattern.matcher(thisString);
                     if (flagsStr.indexOf('g') != -1) {
                         return matcher.replaceAll(replacement);
                     }
                     else {
                         return matcher.replaceFirst(replacement);
                     }
                 }
                 catch (final PatternSyntaxException e) {
                     getLog().warn(e);
                 }
             }
         }
         else if (RA_MATCH == actionType) {
             if (args.length == 0) {
                 return null;
             }
             final Object arg0 = args[0];
             final String thisString = Context.toString(thisObj);
             final String regex;
             final int flags;
             if (arg0 instanceof NativeRegExp) {
                 regex = readNativeRegExpPattern((NativeRegExp) arg0);
                 flags = readNativeRegExpFlags((NativeRegExp) arg0);
             }
             else {
                 regex = Context.toString(arg0);
                 flags = 0;
             }
             final Pattern pattern = Pattern.compile(regex, flags);
             final Matcher matcher = pattern.matcher(thisString);
             if (!matcher.find()) {
                 return null;
             }
             final Object[] groups = new Object[matcher.groupCount() + 1];
             for (int i = 0; i <= matcher.groupCount(); ++i) {
                 groups[i] = matcher.group(i);
                 if (groups[i] == null) {
                     groups[i] = Context.getUndefinedValue();
                 }
             }
            return cx.newArray(scope, groups);
         }
         
         return wrappedAction(cx, scope, thisObj, args, actionType);
     }
     
     private int readNativeRegExpFlags(final NativeRegExp nativeRegExp) {
         final String str = nativeRegExp.toString(); // the form is /regex/flags
         return jsFlagsToPatternFlags(str);
     }
 
     private String readNativeRegExpPattern(final NativeRegExp nativeRegExp) {
         final String str = nativeRegExp.toString(); // the form is /regex/flags
         return StringUtils.substringBeforeLast(str.substring(1), "/");
     }
 
     /**
      * Calls action on the wrapped RegExp proxy.
      */
     private Object wrappedAction(final Context cx, final Scriptable scope, final Scriptable thisObj,
             final Object[] args, final int actionType) {
         
         // take care to set the context's RegExp proxy to the original one as this is checked
         // (cf org.mozilla.javascript.regexp.RegExpImp:334)
         try {
             ScriptRuntime.setRegExpProxy(cx, wrapped_);
             return wrapped_.action(cx, scope, thisObj, args, actionType);
         }
         finally {
             ScriptRuntime.setRegExpProxy(cx, this);
         }
     }
 
     /**
      * Convert JavaScript RegExp flags "img" to Java Pattern flags
      * @param flagsStr the flags (a combination of i, m and g)
      * @return the Java Pattern flags
      */
     private int jsFlagsToPatternFlags(final String flagsStr) {
         int flags = 0;
         if (flagsStr.indexOf('i') != -1) {
             flags |= Pattern.CASE_INSENSITIVE;
         }
         if (flagsStr.indexOf('m') != -1) {
             flags |= Pattern.MULTILINE;
         }
         return flags;
     }
 
     /**
      * {@inheritDoc}
      */
     public Object compileRegExp(final Context cx, final String source, final String flags) {
         return wrapped_.compileRegExp(cx, source, flags);
     }
 
     /**
      * {@inheritDoc}
      */
     public int find_split(final Context cx, final Scriptable scope, final String target,
             final String separator, final Scriptable re, final int[] ip, final int[] matchlen,
             final boolean[] matched, final String[][] parensp) {
         return wrapped_.find_split(cx, scope, target, separator, re, ip, matchlen, matched, parensp);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isRegExp(final Scriptable obj) {
         return wrapped_.isRegExp(obj);
     }
 
     /**
      * {@inheritDoc}
      */
     public Scriptable wrapRegExp(final Context cx, final Scriptable scope, final Object compiled) {
         return wrapped_.wrapRegExp(cx, scope, compiled);
     }
 
     /**
      * Return the log object for this object.
      * @return The log object for this object.
      */
     protected final Log getLog() {
         return LogFactory.getLog(getClass());
     }
 
 }
