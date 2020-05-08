 /* *****************************************************************************
  * ScriptCompiler.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
  * Copyright 2001-2006, 2008 Laszlo Systems, Inc.  All Rights Reserved.              *
  * Use is subject to license terms.                                            *
  * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.sc;
 import java.io.*;
 import java.util.*;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.iv.flash.api.*;
 import org.openlaszlo.iv.flash.api.action.*;
 import org.openlaszlo.iv.flash.util.*;
 import org.apache.log4j.*;
 import org.openlaszlo.cache.Cache;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.compiler.CompilationError;
 
 /** Utility class for compiling scripts, translating Java objects
  * (maps, lists, and strings) to source expressions for the
  * corresponding JavaScript object.
  *
  * @author  Oliver Steele
  */
 public class ScriptCompiler extends Cache {
     private static Logger mLogger = Logger.getLogger(ScriptCompiler.class);
     private static Writer intermediateWriter = null;
 
     static public final String SCRIPT_CACHE_NAME = "scache";
 
     /** Map(String properties+script, byte[] bytes), or null if the
      * cache hasn't been initialized, has been cleared, or the cache
      * size is zero. */
     // TODO: [2002-11-28 ows] use org.apache.commons.util.BufferCache?
     // TODO: [2002-11-28 ows] wrap in Collections.synchronizedMap?
     private static ScriptCompiler mScriptCache = null;
     
     public  ScriptCompiler(String name, File cacheDirectory, Properties props)
         throws IOException {
         super(name, cacheDirectory, props);
     }
 
     public static ScriptCompiler getScriptCompilerCache() {
         return mScriptCache;
     }
 
     public static void setIntermediateWriter(Writer writer) {
         intermediateWriter = writer;
     }
 
     public static synchronized ScriptCompiler initScriptCompilerCache(File cacheDirectory, Properties initprops)
       throws IOException {
         if (mScriptCache != null) {
             return mScriptCache;
         }
         mScriptCache = new ScriptCompiler(SCRIPT_CACHE_NAME, cacheDirectory, initprops);
         return mScriptCache;
     }
 
     /**
      * Compiles the ActionScript in script to a new movie in the swf
      * file named by outfile.
      *
      * @param script a <code>String</code> value
      * @param outfile a <code>File</code> value
      */
     public static void compile(String script, File outfile, int swfversion)
         throws IOException
     {
         FileOutputStream ostream = new FileOutputStream(outfile);
         compile(script, ostream, swfversion);
         ostream.close();
     }
 
     /**
      * Compile the ActionScript in script to a movie, that's written
      * to output.
      *
      * @param script a <code>String</code> value
      * @param ostream an <code>OutputStream</code> value
      */
     public static void compile(String script, OutputStream ostream, int swfversion)
         throws IOException
     {
         byte[] action = compileToByteArray(script, new Properties());
         writeScriptToStream(action, ostream, swfversion);
     }
 
     /*
     // TODO: [2004-01-07 hqm] This cache clearing method has the
     following bug now; if the in memory ScriptCache has not been
     created yet when clearCache is called, then the disk cache won't
     get cleared. We need to make sure mScriptCache is initialized at
     server startup.]
     */
     public static boolean clearCacheStatic() {
         if (mScriptCache != null) {
             return mScriptCache.clearCache();
         }
         return false;
     }
 
 
     private static byte[] _compileToByteArray(String script,
                                               Properties properties) {
         org.openlaszlo.sc.Compiler compiler =
             new org.openlaszlo.sc.Compiler(properties);
         try {
             return compiler.compile(script);
         } catch (org.openlaszlo.sc.parser.TokenMgrError e) {
             // The error message isn't helpful, and has the wrong
             // source location in it, so ignore it.
             // TODO: [2003-01-09 ows] Fix the error message.
             throw new CompilerException(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Lexical error.  The source location is for the element that contains the erroneous script.  The error may come from an unterminated comment."
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ScriptCompiler.class.getName(),"051018-119")
 );
         }
     }
 
     /**
      * @return a cache key for the given properties
      */
     static String sortedPropertiesList(Properties props) {
         TreeSet  sorted = new TreeSet ();
         for (java.util.Enumeration e = props.propertyNames();
              e.hasMoreElements(); ) {
             String key = (String) e.nextElement();
 
             String value = props.getProperty(key);
             StringBuffer buf = new StringBuffer();
             buf.append(key);
             buf.append(' ');
             buf.append(value);
 
             sorted.add(buf.toString());
         }
         StringBuffer buf = new StringBuffer();
         for (java.util.Iterator e = sorted.iterator(); e.hasNext(); ) {
             String str = (String) e.next();
             buf.append(str);
         }
         String propstring = buf.toString();
         return propstring;
     }
 
     /** Collects the script into an intermediate output file
      * if we are configured to do so.
      *
      * @param script a script
      */
     private static void writeIntermediateFile(String script)
     {
         if (intermediateWriter != null) {
             try {
                 intermediateWriter.write(script);
             }
             catch (IOException ioe) {
                 throw new CompilationError("Could not create intermediate script file: " + ioe);
             }
         }
     }
 
     /** Compiles the specified script into bytecode
      *
      * @param script a script
      * @return an array containing the bytecode
      */
     public static byte[] compileToByteArray(String script,
                                             Properties properties) {
 
         writeIntermediateFile(script);
 
         // We only want to keep off the properties that affect the
         // compilation.  Currently, "filename" is the only property
         // that tends to change and that the script compiler ignores,
         // so make a copy of properties that neutralizes that.
         properties = (Properties) properties.clone();
         properties.setProperty("filename", "");
         // The key is a string representation of the arguments:
         // properties, and the script.
         StringWriter writer = new StringWriter();
         writer.write(sortedPropertiesList(properties));
         writer.getBuffer().append(script);
         String key = writer.toString();
         // Check the cache.  clearCache may clear the cache at any
         // time, so use a copy of it so that it doesn't change state
         // between a test that it's null and a method call on it.
         ScriptCompiler cache = mScriptCache;
         Item item = null;
         byte[] code = null;
         try {
             if (mScriptCache == null) {
                 return _compileToByteArray(script, properties);
             } else {
                 synchronized (mScriptCache) {
                     item = mScriptCache.findItem(key, null, false);
                 }
             }
 
             if (item.getInfo().getSize() != 0) {
                 code = item.getRawByteArray();
             } else {
                 code = _compileToByteArray(script, properties);
                 // Another thread might already have set this since we
                 // called get.  That's okay --- it's the same value.
                 synchronized (mScriptCache) {
                     item.update(new ByteArrayInputStream(code), null);
                     item.updateInfo();
                     item.markClean();
                 }
             }
             
             mScriptCache.updateCache(item);
 
             return (byte[]) code;
         } catch (IOException e) {
             throw new CompilationError(e, "IOException in compilation/script-cache");
         }
     }
     
     /**
      * @param action actionscript byte codes
      * @param ostream outputstream to write SWF
      */
     public static void writeScriptToStream(byte[] action, 
            OutputStream ostream, int swfversion) throws IOException {
         FlashFile file = FlashFile.newFlashFile();
         Script s = new Script(1);
         file.setMainScript(s);
         file.setVersion(swfversion);
         Program program = new Program(action, 0, action.length);
         Frame frame = s.newFrame();
         frame.addFlashObject(new DoAction(program));
         InputStream input;
         try {
             input = file.generate().getInputStream();
         }
         catch (IVException e) {
             throw new ChainedException(e);
         }
 
         byte[] buffer = new byte[1024];
         int b = 0;
         while((b = input.read(buffer)) > 0) {
             ostream.write(buffer, 0, b);
         }
     }
 
 
 
     public static String objectAsJavascript(Object object) {
         try {
             java.io.Writer writer = new java.io.StringWriter();
             writeObject(object, writer);
             return writer.toString();
         } catch (java.io.IOException e) {
             throw new ChainedException(e);
         }
     }
 
 
     /** Writes a LaszloScript expression that evaluates to a
      * LaszloScript representation of the object.
      *
      * @param elt an element
      * @param writer a writer
      * @throws java.io.IOException if an error occurs
      */
     public static void writeObject(Object object, java.io.Writer writer)
         throws java.io.IOException
     {
         if (object instanceof Map) {
             writeMap((Map) object, writer);
         } else if (object instanceof List) {
             writeList((List) object, writer);
         } else if (object != null) {
             writer.write(object.toString());
         } else {
           // A declared property with no intial value
           writer.write("void 0");
         }
     }
 
     /** Writes a LaszloScript object literal whose properties are the
      * keys of the map and whose property values are the LaszloScript
      * representations of the map's values.
      *
      * The elements of the map are strings that represent JavaScript
      * expressions, not values.  That is, the value "foo" will compile
      * to a reference to the variable named foo; "'foo'" or "\"foo\""
      * is necessary to enter a string in the map.
      *
      * @param map String -> Object
      * @param writer a writer
      * @return a string
      */
     private static void writeMap(Map map, java.io.Writer writer)
         throws java.io.IOException
     {
         writer.write("{");
         // Sort the keys, so that regression tests aren't sensitive to
         // the undefined order of iterating a (non-TreeMap) Map.
         SortedMap smap = new TreeMap(map);
         for (Iterator iter = smap.entrySet().iterator(); iter.hasNext(); ) {
             Map.Entry entry = (Map.Entry) iter.next();
             String key = (String) entry.getKey();
             Object value = entry.getValue();
             if (!isIdentifier(key))
                 key = quote(key);
             writer.write(key + ": ");
             writeObject(value, writer);
             if (iter.hasNext()) {
                 writer.write(", ");
             }
         }
         writer.write("}");
     }
     
     /** Writes a LaszloScript array literal that evaluates to a
      * LaszloScript array whose elements are LaszloScript
      * representations of the arguments elements.
      *
      * The elements of the list are strings that represent JavaScript
      * expressions, not values.  That is, the value "foo" will compile
      * to a reference to the variable named foo; "'foo'" or "\"foo\""
      * is necessary to enter a string in the array.
      *
      * @param list a list
      * @param writer a writer
      * @return a string
      */
     private static void writeList(List list, java.io.Writer writer)
         throws java.io.IOException
     {
         writer.write("[");
         for (java.util.Iterator iter = list.iterator();
              iter.hasNext(); ) {
             writeObject(iter.next(), writer);
             if (iter.hasNext()) {
                 writer.write(", ");
             }
         }
         writer.write("]");
     }
     
     /** Returns true iff the string is a valid JavaScript identifier. */
     public static boolean isIdentifier(String s) {
         if (s.length() == 0)
             return false;
         if (!Character.isJavaIdentifierStart(s.charAt(0)))
             return false;
         for (int i = 1; i < s.length(); i++)
             if (!Character.isJavaIdentifierPart(s.charAt(i)))
                 return false;
        String[] keywords = {"break", "continue", "delete", "else", "false", "for", "function", "if", "implements", "in", "inherits", "interface", "instanceof", "mixin", "new", "null", "return", "this", "trait", "true", "typeof", "var", "void", "while", "with", "case", "catch", "class", "const", "debugger", "default", "do", "enum", "export", "extends", "finally", "import", "super", "switch", "throw", "try"};
         for (int i = 0; i < keywords.length; i++) {
            if (s.equals(keywords[i]))
                 return false;
         }
         return true;
     }
     
     /** Enclose the specified string in double-quotes, and character-quote
      * any characters that need it.
      * @param s a string
      * @return a quoted string
      */
     public static String quote(String s) {
         try {
             final char CHAR_ESCAPE = '\\';
             java.io.StringReader reader = new java.io.StringReader(s);
             java.io.StringWriter writer = new java.io.StringWriter();
             int i;
             int n = 0;
             char quote = '\"';
             // Minimize escaping of quotes
             if (s.indexOf('\'') >= 0 || s.indexOf('\"') >= 0) {
               while ((i = reader.read()) != -1) {
                 char c = (char) i;
                 switch (c) {
                   case '\'': n--; break;
                   case '\"': n++; break;
                 }
               }
               reader.reset();
               quote = n > 0 ? '\'' : '\"';
             }
             writer.write(quote);
             while ((i = reader.read()) != -1) {
                 char c = (char) i;
                 switch (c) {
                 case '\n':
                     writer.write("\\n");
                     break;                    
                 case '\r':
                     writer.write("\\r");
                     break;
                 case '\b':
                     writer.write("\\b");
                     break;
                 case '\t':
                     writer.write("\\t");
                     break;
                 case '\u000B':
                     writer.write("\\v");
                     break;
                 case '\f':
                     writer.write("\\f");
                     break;
                 case '\\':
                     writer.write(CHAR_ESCAPE);
                     writer.write(c);
                     break;
                 case '\'':
                 case '\"':
                     if (c == quote) {
                       writer.write(CHAR_ESCAPE);
                     }
                     writer.write(c);
                     break;
                 default:
                     if (i == 0) {
                         // ECMAScript NUL is a special case
                         writer.write(CHAR_ESCAPE);
                         writer.write('0');
                     } else if (i < 32 || (i >= 128 && i <= 0xff)) {
                         // ECMAScript string literal hex unicode escape sequence
                         writer.write(CHAR_ESCAPE);
                         writer.write('x');
                         // Format as \ xXX two-digit zero padded hex string
                         writer.write(hexchar((c >> 4) & 0x0F));
                         writer.write(hexchar(c & 0x0F));
                     } else if (i > 0xff) {
                         // ECMAScript string literal hex unicode escape sequence
                         writer.write(CHAR_ESCAPE);
                         writer.write('u');
                         // Format as \ uXXXX four-digit zero padded hex string
                         writer.write(hexchar((c >> 12) & 0x0F));
                         writer.write(hexchar((c >> 8) & 0x0F));
                         writer.write(hexchar((c >> 4) & 0x0F));
                         writer.write(hexchar(c & 0x0F));
                     } else {
                         writer.write(c);
                     }
                 }
             }
             writer.write(quote);
             return writer.toString();
         } catch (java.io.IOException e) {
             throw new ChainedException(e);
         }
     }
 
     static char hexchar (int c) {
         char hexchars[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
         return (hexchars[c & 0x0F]);
     }
 }
