 
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
   * <p>
   * CONCURRENCY: You must parse() the template before making it available
   * to other threads. After a template has been parsed it is immutable and
   * can be shared between threads without synchronization. This class 
   * performs no synchronization itself but instead relies on the 
   * TemplateProvider/Broker to synchronize access.
   */
 abstract public class WMTemplate implements Template
 {
 
    /**
      * The resource broker used to resolve things in this template
      */
    private Broker _broker;
 
    /**
      * Where we log our errors
      */
    private Log _log;
 
    /**
      * What this template contains is a top level block
      */
    private Block _content; 
 
    /**
      *
      */
    private String _parserName;
 
    /**
      * Template parameters
      */
    private Map _parameters;
 
    /**
      * Create a new Template. Constructors must supply a broker.
      */
    protected WMTemplate(Broker broker) {
       this("wm", broker);
    }
 
    /**
      * Create a new Template specifying both the broker and the 
      * parsing language.
      */
    protected WMTemplate(String parserName, Broker broker) {
       _broker = broker;
       _parserName = parserName;
       _log = broker.getLog("template");
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
          return (Parser) _broker.get("parser","wm"); 
       } catch (Exception e) {
          throw new TemplateException("Could not load parser type \"" + 
                _parserName + "\": " + e);
       }
    }
 
    /**
      * Template API
      */
    public final void parse() throws IOException, TemplateException
    {
       Block newContent = null;
       Map newParameters = null;
       Reader in = null;
       try {
          Parser parser = getParser();
          in = getReader();
          BlockBuilder bb = parser.parseBlock(toString(),in);
          in.close();
          BuildContext bc = new BuildContext(_broker);
          newParameters = bc.getGlobalVariables();
          newContent = (Block) bb.build(bc);
       } catch (BuildException be) {
          newContent = null;
          _log.error("Template contained invalid data", be);
          throw be;
       } catch (IOException e) {
          newContent = null; // don't let the old one survive
          _log.error("Template: Could not read template: " + this);
          throw e;
       } finally {
          try { in.close(); } catch (Exception e) { }
          _parameters = newParameters;
          _content = newContent; 
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
          FastWriter fw = FastWriter.getInstance();
          write(fw,data);
          String ret = fw.toString();
          fw.close();
          return ret;
       } catch (IOException e) {
          _log.error("Template: Could not write to ByteArrayOutputStream!",e);
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
       try {
          if (_content == null) {
             parse();   
          }
      } catch (TemplateException e) {
         _log.error("Template: Unable to parse template: " + this, e);
         out.write("<!--\n Template failed to parse. Reason: ");
          out.write(e.toString());
          out.write(" \n-->");
       }
 
       try {
          _content.write(out,data);
       } catch (PropertyException e) {
          String warning = 
            "Template: Missing data in Context passed to template " + this;
          _log.warning(warning,e);
          
          out.write("<!--\n Could not interpret template. Reason: ");
          out.write(warning);
          out.write(e.toString());
          out.write(" \n-->");
       } 
    }
 
    public void accept(TemplateVisitor v) { _content.accept(v); }
 
    /**
      * Template API
      */
    public Object getParam(String key) 
       throws IOException, TemplateException 
    {
       try {
          return _parameters.get(key);
       } catch (NullPointerException e) {
          parse();
          return _parameters.get(key);
       }
    }
 
    public Map getParameters()
    {
       return _parameters;
    }
 }
 
