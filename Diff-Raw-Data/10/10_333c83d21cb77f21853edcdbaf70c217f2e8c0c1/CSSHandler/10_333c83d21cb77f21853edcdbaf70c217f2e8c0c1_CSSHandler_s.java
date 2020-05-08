 /* *****************************************************************************
  * CSSHandler.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.css;
 
 import java.io.*;
 import java.util.*;
 import org.w3c.css.sac.*;
 import org.apache.log4j.*;
 import org.jdom.*;
 
 /**
  * Handler used to parse CSS file and process style rules on a document element.
  *
  * @author <a href="mailto:pkang@laszlosystems.com">Pablo Kang</a>
  */
 public class CSSHandler implements DocumentHandler, Serializable, ErrorHandler {
 
     //==========================================================================
     // class static
     //==========================================================================
 
     /** Logger. */
     private static Logger mLogger = Logger.getLogger(CSSHandler.class);
 
     /** CSS parser factory. */ 
     private static org.w3c.css.sac.helpers.ParserFactory mCSSParserFactory = null;
     
     static {
         // This system property is required for the SAC ParserFactory.
         if (System.getProperty("org.w3c.css.sac.parser") == null) {
             System.setProperty("org.w3c.css.sac.parser",
                                "org.apache.batik.css.parser.Parser");
         }
         mCSSParserFactory = new org.w3c.css.sac.helpers.ParserFactory();
     }
 
    /**
      * Entry point to creating a CSSHandler to read from an external
      *    stylesheet file
      * @param rootDir the directory where cssFile exists.
      * @param cssFile the css file to read.
      */
     public static CSSHandler parse(File file)
            throws CSSException {
        try {
            mLogger.info("creating CSSHandler");
            CSSHandler handler = new CSSHandler(file);
            Parser parser = mCSSParserFactory.makeParser();
            parser.setDocumentHandler(handler);
            parser.setErrorHandler(handler);
            parser.parseStyleSheet(handler.getInputSource());
            return handler;
        } catch (CSSParseException e) {
            mLogger.error("got css parse exception");
            throw e;
        } catch (CSSException e) {
            mLogger.error("got css exception");
            throw e;
        } catch (Exception e) {
            mLogger.error("exception while parsing CSS");
            throw new CSSException(e.getMessage());
        }
     }
 
     /**
      * Entry point to creating a CSSHandler from just a string of inlined css
      * @param cssString a string containing the entire CSS to parse
      */
     public static CSSHandler parse(String cssString)
             throws CSSException {
         try {
             mLogger.debug("entering CSSHandler.parse with inline string");
             CSSHandler handler = new CSSHandler(cssString);
             Parser parser = mCSSParserFactory.makeParser();
             parser.setDocumentHandler(handler);
             parser.setErrorHandler(handler);
             java.io.Reader cssReader = new java.io.StringReader(cssString);
             InputSource inputSource = new InputSource(cssReader);
             parser.parseStyleSheet(inputSource);
             return handler;
         } catch (CSSParseException e) {
             mLogger.error("got css parse exception on inline css, " + e.getMessage());
             throw e;
         } catch (CSSException e) {
             mLogger.error("got css exception on inline css" + e.getMessage());
             throw e;
         } catch (Exception e) {
             mLogger.error("exception while parsing inline css");
             throw new CSSException(e.getMessage());
         }
     }
 
 
 
     //==========================================================================
     // instance
     //==========================================================================
 
     /** The css file itself */
     File mFile;
 
     /** List of Rule instances. */
     public List mRuleList;
 
     /** Used as a map of style properties for selector group being parsed. */
     Map mStyleMap;
 
     /** A list of CSS files separated by two file separators characters. */
     String mFileDependencies;
     
 
     /** protected constructor */
     CSSHandler(File file) {
         mFile = file;
         mRuleList = new Vector();
         mFileDependencies = getFullPath();
     }
     
     /** protected constructor */
     CSSHandler(String cssString) {
         mFile = null; // No file associated with inline css
         mRuleList = new Vector();
         mFileDependencies = ""; // inline css doesn't add any file dependencies
     }
     
 
     /** Helper function to log and throw an error. */
     void throwCSSException(String errMsg) throws CSSException {
         mLogger.error(errMsg);
         throw new CSSException(errMsg);
     }
 
     /** @return full path to CSS file */
     String getFullPath () {
         try {
             return mFile.getCanonicalPath();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return ""; 
         }
     }
 
     /** @return InputSource object pointing to the CSS file. */
     InputSource getInputSource() throws FileNotFoundException {
         InputSource is =
             new InputSource(new FileReader(mFile));
 //         is.setEncoding("ISO-8859-1");
         return is;                                         
     }
 
     /** 
      * Get a string containing a list CSS files required by the parse. Includes
      * imported CSS files.
      * @return a list of CSS files separated by two file separators characters.
      */
     public String getFileDependencies() {
         return mFileDependencies;
     }
 
     //--------------------------------------------------------------------------
     // Implement DocumentHandler interface.
     //--------------------------------------------------------------------------
 
     public void startSelector(SelectorList selectors) throws CSSException {
         mStyleMap = new HashMap();
     }
 
     public void endSelector(SelectorList selectors) throws CSSException {
         if (mStyleMap.size() != 0) {
             for (int i=0; i <  selectors.getLength(); i++) {
                 mRuleList.add(new Rule(selectors.item(i), mStyleMap));
             }
         }
     }
 
     public void property(String name, LexicalUnit value, boolean important)
         throws CSSException {
         mStyleMap.put(name, new StyleProperty(luToString(value), important));
     }
 
     public void importStyle(String uri, SACMediaList media,
                             String defaultNamespaceURI) throws CSSException {
         try {
             CSSHandler handler = new CSSHandler(new File(uri));
             Parser parser = mCSSParserFactory.makeParser();
             parser.setDocumentHandler(handler);
             parser.parseStyleSheet(handler.getInputSource());
         } catch (Exception e) {
             mLogger.error("Exception", e);
             throw new CSSException(e.getMessage());
         }
     }
     
     public void startDocument(InputSource source) throws CSSException {
         /* ignore */
     }
 
     public void endDocument(InputSource source) throws CSSException {
         /* ignore */
     }
 
     public void comment(String text) {
         /* ignore */
     }
 
     public void startFontFace() throws CSSException {
         throwCSSException("start font face unsupported");
     }
     public void endFontFace() throws CSSException {
         throwCSSException("end font face unsupported");
     }
     public void startMedia(SACMediaList media) throws CSSException {
         throwCSSException("start media unsupported");
     }
     public void endMedia(SACMediaList media) throws CSSException {
         throwCSSException("start media unsupported");
     }
     public void startPage(String name, String pseudoPage) throws CSSException {
         throwCSSException("start page unsupported: " + name + ", " + pseudoPage);
     }
     public void endPage(String name, String pseudoPage) throws CSSException {
         throwCSSException("end page unsupported: " + name + ", " + pseudoPage);
     }
     public void namespaceDeclaration(String prefix, String uri)
         throws CSSException {
         throwCSSException("namespace declaration unsupported: " + prefix + ":" + uri);
     }
     public void ignorableAtRule(String atRule) throws CSSException {
         throwCSSException("ignorable at rule: " + atRule);
     }
 
 
     //--------------------------------------------------------------------------
     // helper methods
     //--------------------------------------------------------------------------
     
     /** @return an RGB formatted hex string like #FFFFFF. */
     String getRGBString(LexicalUnit lu) {
       int rr = lu.getLexicalUnitType() == LexicalUnit.SAC_PERCENTAGE ?
         (int)Math.round(Math.min(lu.getFloatValue() * 255.0 / 100.0, 255.0)) :
         lu.getIntegerValue();
       lu = lu.getNextLexicalUnit().getNextLexicalUnit(); // skip comma
       int gg = lu.getLexicalUnitType() == LexicalUnit.SAC_PERCENTAGE ?
         (int)Math.round(Math.min(lu.getFloatValue() * 255.0 / 100.0, 255.0)) :
         lu.getIntegerValue();
       lu = lu.getNextLexicalUnit().getNextLexicalUnit(); // skip comma
       int bb = lu.getLexicalUnitType() == LexicalUnit.SAC_PERCENTAGE ?
         (int)Math.round(Math.min(lu.getFloatValue() * 255.0 / 100.0, 255.0)) :
         lu.getIntegerValue();
 
       return "0x"
         + (rr < 16 ? "0" : "") + Integer.toHexString(rr).toUpperCase()
         + (gg < 16 ? "0" : "") + Integer.toHexString(gg).toUpperCase()
         + (bb < 16 ? "0" : "") + Integer.toHexString(bb).toUpperCase();
     }
     
     /**
       * Convert LexicalUnit to a Javascript value (represented
       * as a String).
       */
      String luToString(LexicalUnit lu) {
          String str = "";
          switch (lu.getLexicalUnitType()) {
 
          case LexicalUnit.SAC_ATTR:
           // attr needs to be defined when this is evaluated
           str = "attr(\"" + lu.getStringValue() + "\")";
            break;
 

          case LexicalUnit.SAC_IDENT:
             str = lu.getStringValue();
             break;
 
          case LexicalUnit.SAC_STRING_VALUE:
            str = "\"" + lu.getStringValue() + "\"";
            break;
 
          case LexicalUnit.SAC_URI:
            // url needs to be defined when this is evaluated
            str = "url(\"" + lu.getStringValue() + "\")";
            break;
 
          case LexicalUnit.SAC_CENTIMETER:
          case LexicalUnit.SAC_DEGREE:
          case LexicalUnit.SAC_DIMENSION:
          case LexicalUnit.SAC_EM:
          case LexicalUnit.SAC_EX:
          case LexicalUnit.SAC_GRADIAN:
          case LexicalUnit.SAC_HERTZ:
          case LexicalUnit.SAC_INCH:
          case LexicalUnit.SAC_KILOHERTZ:
          case LexicalUnit.SAC_MILLIMETER:
          case LexicalUnit.SAC_MILLISECOND:
          case LexicalUnit.SAC_PICA:
          case LexicalUnit.SAC_PIXEL:
          case LexicalUnit.SAC_POINT:
          case LexicalUnit.SAC_RADIAN:
          case LexicalUnit.SAC_SECOND:
              // Dimensioned values are stored as strings for now, but
              // perhaps they should be converted to
              // length/frequency/angle in the appropriate base unit?
            float val = lu.getFloatValue();
            str = "\"" + (val == 0 ? "0" : Float.toString(val)) +
              lu.getDimensionUnitText() + "\"";
            break;
 
          case LexicalUnit.SAC_PERCENTAGE:
              str = Float.toString(lu.getFloatValue() / 100);
              break;
 
          case LexicalUnit.SAC_REAL:
              str = Float.toString(lu.getFloatValue());
              break;
 
          case LexicalUnit.SAC_INTEGER:
              str = Integer.toString(lu.getIntegerValue());
              break;
 
          case LexicalUnit.SAC_RGBCOLOR:
              str = getRGBString(lu.getParameters());
              break;
 
          //----------------------------------------------------------------------
          // TODO [2005-10-07 pkang]: don't think I'm handling these literal
          // lexical units correctly. Not worrying for now since we only
          // support basic values
          //----------------------------------------------------------------------
 
          case LexicalUnit.SAC_INHERIT:
              str = "inherit";
              break;
          case LexicalUnit.SAC_OPERATOR_COMMA:
              str = ",";
              break;
          case LexicalUnit.SAC_OPERATOR_EXP:
              str = "^";
              break;
          case LexicalUnit.SAC_OPERATOR_GE:
              str = ">=";
              break;
          case LexicalUnit.SAC_OPERATOR_GT:
              str = ">";
              break;
          case LexicalUnit.SAC_OPERATOR_LE:
              str = "<=";
              break;
          case LexicalUnit.SAC_OPERATOR_LT:
              str = "<";
              break;
          case LexicalUnit.SAC_OPERATOR_MINUS:
              str = "-";
              break;
          case LexicalUnit.SAC_OPERATOR_MOD:
              str = "%";
              break;
          case LexicalUnit.SAC_OPERATOR_MULTIPLY:
              str = "*";
              break;
          case LexicalUnit.SAC_OPERATOR_PLUS:
              str = "+";
              break;
          case LexicalUnit.SAC_OPERATOR_SLASH:
              str = "/";
              break;
          case LexicalUnit.SAC_OPERATOR_TILDE:
              str = "~";
              break;
 
          // Can we support these as functions?
          case LexicalUnit.SAC_COUNTER_FUNCTION:
              throwCSSException("SAC_COUNTER_FUNCTION unsupported");
          case LexicalUnit.SAC_COUNTERS_FUNCTION:
              throwCSSException("SAC_COUNTERS_FUNCTION unsupported");
          case LexicalUnit.SAC_RECT_FUNCTION:
          case LexicalUnit.SAC_FUNCTION:
            // Function needs to be defined when this is evaluated
            str = lu.getFunctionName() + "(" + luToString(lu.getParameters()) + ")";
            break;
          case LexicalUnit.SAC_SUB_EXPRESSION:
              throwCSSException("SAC_SUB_EXPRESSION unsupported");
          case LexicalUnit.SAC_UNICODERANGE:
              throwCSSException("SAC_UNICODERANGE unsupported");
          default:
              throwCSSException("unsupported lexical unit type: " +
                                lu.getLexicalUnitType());
          }
          return str;
      }
 
 
     public void warning(CSSParseException e) throws CSSException {
         mLogger.error("warning ", e);
         throw e;
     }
 
     public void error(CSSParseException e) throws CSSException {
         mLogger.error("error ", e);
         throw e;
     }
 
     public void fatalError(CSSParseException e) throws CSSException {
         mLogger.error("fatal error while parsing css", e);
         throw e;
     }
 }
