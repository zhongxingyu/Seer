 
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
   * Template objects represent the user defined layout into which the 
   * webmacro package will substitute values. It is a very simple kind of
   * interpreted language containing text, blocks, and directives. Text is
   * to be passed through verbatim. LList group text and directives into
   * linear lists and sublists. Directives determine how subsequent blocks
   * are to be processed and constitute the commands of the language. 
   * <p>
   * The Template is lazily evaluated: it does not parse or open the 
   * supplied filename until it is used. Once it has parsed the file, it 
   * never alters its data. The intent is to allow a Template to be parsed
   * once (somewhat expensive) and then used many times; and also not to 
   * incur any parsing costs at all if the Template is never actually used.
   */
 
 abstract public class WMTemplate implements Template
 {
 
    /**
      * The resource broker used to resolve things in this template
      */
    private Broker _broker;
 
    /**
      * What this template contains is a top level block
      */
    private Block _content; 
 
    /**
      *
      */
    private String _parserName;
 
    /**
      * Log
      */
    private static Log _log = new Log("tmpl", "Template Processing");
 
    /**
      * Template parameters
      */
    private Map _parameters;
 
    /**
      * Template filters
      */
    private Map _filters;
 
    /**
      * Template macros
      */
    private Map _macros;
 
    /**
      * Create a new Template. Constructors must supply a broker.
      */
    protected WMTemplate(Broker broker) {
       this(broker,"wm");
    }
 
    /**
      * Create a new Template specifying both the broker and the 
      * parsing language.
      */
    protected WMTemplate(Broker broker, String parserName) {
       _broker = broker;
       _parserName = parserName;
    }
 
    /**
      * Get the stream the template should be read from. Parse will 
      * call this method in order to locate a stream.
      * @exception IOException if unable to read template
      */
    protected abstract Reader getReader() throws IOException;
 
    /**
      * Return a name for this template. For example, if the template reads
      * from a file you might want to mention which it is--will be used to
      * produce error messages describing which template had a problem.
      */
    public abstract String toString(); 
 
 
    /**
      * Subclasses can override this if they wish to invoke a parser
      * other than the WM parser, or choose the parser on some more 
      * complicated condition. This method will be called by parse();
      */
    protected Parser getParser() throws TemplateException {
       try {
          return (Parser) _broker.getValue("parser","wm"); 
       } catch (Exception e) {
          Engine.log.exception(e);
          throw new TemplateException("Could not load parser type \"" + 
                _parserName + "\": " + e);
       }
    }
 
    /**
      * Template API
      */
    public final void parse() throws IOException, TemplateException
    {
 
       // thread policy:
       //
       // unsynchronized code elsewhere will access _content. We must
       // ensure that any data copied into _content is ready for public 
       // use--which means dealing with two subtle issues. First, the Block
       // created by this method must be written back to main memory BEFORE
       // being referenced by _content. Second, once we add it to _content,
       // our copy of _content has to be copied back to main memory as well.
       //
       // Therefore we synchronize around the access to _content so as to 
       // accomplish both these things. newContent will be written to main 
       // memory at the start of the synchronized block, and _content will
       // be written to main memory at the close.
 
       Block newContent = null;
       Map newParameters = null;
       Reader source = null;
       Map newFilters = null;
       try {
          Parser parser = getParser();
         Reader in = getReader();
          BlockBuilder bb = parser.parseBlock(toString(),in);
          in.close();
          BuildContext bc = new BuildContext(_broker);
          newParameters = bc.getLocalVariables();
          newFilters = bc.getFilters();
          newContent = (Block) bb.build(bc);
       } catch (BuildException be) {
          newContent = null;
          _log.error("Template contained invalid data: " + be);
          throw be;
       } catch (IOException e) {
          newContent = null; // don't let the old one survive
          _log.error("Template: Could not read template: " + this);
          throw e;
       } finally {
          try { source.close(); } catch (Exception e) { }
          synchronized(this) {
             _parameters = newParameters;
             _filters = newFilters;
             _content = newContent; 
          }
       }
    }
 
 
    /**
      * Parse the Template against the supplied context data and 
      * return it as a string. If the operation fails for some reason, 
      * such as unable to read template or unable to introspect the context
      * then this method will return a null string.
      */
    public final Object evaluate(Context data)
    {
       try {
          ByteArrayOutputStream os = new ByteArrayOutputStream(512); 
          FastWriter fw = new FastWriter(os, "UTF8");
          write(fw,data);
 	 fw.flush();
    	 return os.toString("UTF8");
       } catch (IOException e) {
          _log.exception(e);
          _log.error("Template: Could not write to ByteArrayOutputStream!");
          return null;
       }
    }
 
    /**
      * A macro has a write method which takes a context and applies
      * it to the macro to create a resulting String value, which is 
      * then written to the supplied stream. Something will always be
      * written to the stream, even if the operation is not really 
      * successful because of a parse error (an error message will 
      * be written to the stream in that case.)
      * <p>
      * @exception IOException if there is a problem writing to the Writer
      * @return whether the operation was a success
      */
    public final void write(FastWriter out, Context data) 
       throws IOException
    {
 
       // thread policy: Access to _content is unsynchronized here at 
       // the cost of having a slightly stale copy. This is OK, because 
       // you might have requested it slightly earlier anyway. You will 
       // always get a consistent copy--either the current one, or one 
       // that was the current one a few milliseconds ago. This is because
       // a thread setting _content may not update main memory immediately,
       // and this thread may not update its copy of _content immediately
       // (it may have a stale copy it read earlier).
 
       Block content = _content; // copy to a local var in case it changes
 
       // Make sure that all the contents of "content" are up to date--that
       // our thread is fully synchronized with main memory, so that we don't
       // have a half-created version. Synchronize on anything to do this: 
       // we will use an object we don't share with any other thread.
       synchronized(data) { } 
 
       if (content == null) {
          try {
             synchronized(this) {
                // double check under the lock
                if (_content == null) {
                   parse();   
                }
                content = _content;
             }
          } catch (Exception e) {
             _log.exception(e);
             _log.error("Template: Unable to read template: " + this);
             out.write("<!--\n Template failed to read. Reason: ");
             out.write(e.toString());
             out.write(" \n-->");
          }
       } 
 
       try {
          content.write(out,data);
       } catch (ContextException e) {
          _log.exception(e);
          String warning = 
             "Template: Missing data in Map passed to template " + this;
          _log.warning(warning);
          
          out.write("<!--\n Could not interpret template. Reason: ");
          out.write(warning);
          out.write(e.toString());
          out.write(" \n-->");
       }
    }
 
    /**
      * Template API
      */
    public Object getParam(String key) 
       throws IOException, TemplateException 
    {
 
       // perform double checked lock in case template has not yet 
       // been parsed.
 
       try {
          return _parameters.get(key);
       } catch (NullPointerException e) {
          synchronized(this) {
             parse();
             return _parameters.get(key);
          }
       }
    }
 
    public Map getParameters()
    {
       return _parameters;
    }
 
    /**
      * Return the subfilter associated with the supplied top level 
      * property name
      */
    public Filter getFilter(String name) {
       return (Filter) _filters.get(name);
    }
 
 }
 
