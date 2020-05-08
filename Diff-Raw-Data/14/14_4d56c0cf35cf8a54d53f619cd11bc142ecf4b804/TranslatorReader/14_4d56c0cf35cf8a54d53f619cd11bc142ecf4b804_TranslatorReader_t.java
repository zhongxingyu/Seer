 /*
    JSPWiki - a JSP-based WikiWiki clone.
 
    Copyright (C) 2001-2005 Janne Jalkanen (Janne.Jalkanen@iki.fi)
 
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.
 
    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package com.ecyrd.jspwiki;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PushbackReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.oro.text.GlobCompiler;
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.MatchResult;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.PatternCompiler;
 import org.apache.oro.text.regex.PatternMatcher;
 import org.apache.oro.text.regex.PatternMatcherInput;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import com.ecyrd.jspwiki.acl.AccessControlList;
 import com.ecyrd.jspwiki.attachment.Attachment;
 import com.ecyrd.jspwiki.attachment.AttachmentManager;
 import com.ecyrd.jspwiki.auth.UserManager;
 import com.ecyrd.jspwiki.auth.WikiSecurityException;
 import com.ecyrd.jspwiki.auth.modules.PageAuthorizer;
 import com.ecyrd.jspwiki.exception.InternalWikiException;
 import com.ecyrd.jspwiki.exception.NoSuchVariableException;
 import com.ecyrd.jspwiki.manager.VariableManager;
 import com.ecyrd.jspwiki.plugin.PluginException;
 import com.ecyrd.jspwiki.plugin.PluginManager;
 import com.ecyrd.jspwiki.providers.ProviderException;
 import com.ecyrd.jspwiki.util.FileUtil;
 import com.ecyrd.jspwiki.util.TextUtil;
 
 
 /**
  * Handles conversion from Wiki format into fully featured HTML. This is where all the magic
  * happens.  It is CRITICAL that this class is tested, or all Wikis might die horribly.
  *
  * <P>
  * The output of the HTML has not yet been validated against the HTML DTD.  However, it is very
  * simple.
  * </p>
  *
  * @author Janne Jalkanen
  */
 public class TranslatorReader
         extends Reader
         implements WikiProperties
 {
     /** DOCUMENT ME! */
     public static final int READ = 0;
 
     /** DOCUMENT ME! */
     public static final int EDIT = 1;
 
     /** DOCUMENT ME! */
     public static final int EMPTY = 2; // Empty message
 
     /** DOCUMENT ME! */
     public static final int LOCAL = 3;
 
     /** DOCUMENT ME! */
     public static final int LOCALREF = 4;
 
     /** DOCUMENT ME! */
     public static final int IMAGE = 5;
 
     /** DOCUMENT ME! */
     public static final int EXTERNAL = 6;
 
     /** DOCUMENT ME! */
     public static final int INTERWIKI = 7;
 
     /** DOCUMENT ME! */
     public static final int IMAGELINK = 8;
 
     /** DOCUMENT ME! */
     public static final int IMAGEWIKILINK = 9;
 
     /** DOCUMENT ME! */
     public static final int ATTACHMENT = 10;
 
     /** DOCUMENT ME! */
     public static final int ATTACHMENTIMAGE = 11;
 
     /** Lists all punctuation characters allowed in WikiMarkup. These will not be cleaned away. */
     private static final String PUNCTUATION_CHARS_ALLOWED = "._";
 
     /**
      * Allow this many characters to be pushed back in the stream.  In effect, this limits the size
      * of a single heading line.
      */
     private static final int PUSHBACK_BUFFER_SIZE = 10 * 1024;
 
     /** DOCUMENT ME! */
     private static Logger log = Logger.getLogger(TranslatorReader.class);
 
     /** These characters constitute word separators when trying to find CamelCase links. */
     private static final String WORD_SEPARATORS = ",.|;+=&()";
 
     /** DOCUMENT ME! */
     protected static final int BOLD = 0;
 
     /** DOCUMENT ME! */
     protected static final int ITALIC = 1;
 
     /** DOCUMENT ME! */
     protected static final int TYPED = 2;
 
     /**
      * This list contains all IANA registered URI protocol types as of September 2004 + a few
      * well-known extra types. JSPWiki recognises all of them as external links.
      */
     private static final String [] EXTERNAL_LINKS = {
         "http:", "ftp:", "https:",
         "mailto:", "news:", "file:",
         "rtsp:", "mms:", "ldap:",
         "gopher:", "nntp:", "telnet:",
         "wais:", "prospero:", "z39.50s",
         "z39.50r", "vemmi:", "imap:",
         "nfs:", "acap:", "tip:",
         "pop:", "dav:", "opaquelocktoken:",
         "sip:", "sips:", "tel:",
         "fax:", "modem:", "soap.beep:",
         "soap.beeps", "xmlrpc.beep", "xmlrpc.beeps",
         "urn:", "go:", "h323:",
         "ipp:", "tftp:", "mupdate:",
         "pres:", "im:", "mtqp",
         "smb:"
     };
 
     /** DOCUMENT ME! */
     private PushbackReader m_in;
 
     /** DOCUMENT ME! */
     private StringReader m_data = new StringReader("");
 
     //private boolean        m_iscode       = false;
 
     /** DOCUMENT ME! */
     private boolean m_isbold = false;
 
     /** DOCUMENT ME! */
     private boolean m_isitalic = false;
 
     /** DOCUMENT ME! */
     private boolean m_isTypedText = false;
 
     /** DOCUMENT ME! */
     private boolean m_istable = false;
 
     /** DOCUMENT ME! */
     private boolean m_isPre = false;
 
     /** DOCUMENT ME! */
     private boolean m_isEscaping = false;
 
     /** DOCUMENT ME! */
     private boolean m_isdefinition = false;
 
     // general list handling
 
     /** DOCUMENT ME! */
     private int m_genlistlevel = 0;
 
     /** DOCUMENT ME! */
     private StringBuffer m_genlistBulletBuffer = new StringBuffer(); // stores the # and * pattern
 
     /** DOCUMENT ME! */
     private boolean m_allowPHPWikiStyleLists = true;
 
     /** DOCUMENT ME! */
     private boolean m_isOpenParagraph = false;
 
     /** Tag that gets closed at EOL. */
     private String m_closeTag = null;
 
     /** DOCUMENT ME! */
     private WikiEngine m_engine;
 
     /** DOCUMENT ME! */
     private WikiContext m_context;
 
     /** Optionally stores internal wikilinks */
     private ArrayList m_localLinkMutatorChain = new ArrayList();
 
     /** DOCUMENT ME! */
     private ArrayList m_externalLinkMutatorChain = new ArrayList();
 
     /** DOCUMENT ME! */
     private ArrayList m_attachmentLinkMutatorChain = new ArrayList();
 
     /** DOCUMENT ME! */
     private ArrayList m_headingListenerChain = new ArrayList();
 
     /** Keeps image regexp Patterns */
     private ArrayList m_inlineImagePatterns;
 
     /** DOCUMENT ME! */
     private PatternMatcher m_inlineMatcher = new Perl5Matcher();
 
     /** DOCUMENT ME! */
     private ArrayList m_linkMutators = new ArrayList();
 
     /** If true, then considers CamelCase links as well. */
     private boolean m_camelCaseLinks = false;
 
     /** If true, consider URIs that have no brackets as well. */
 
     // FIXME: Currently reserved, but not used.
     // private boolean m_plainUris = false;
 
     /** If true, all outward links use a small link image. */
     private boolean m_useOutlinkImage = true;
 
     /** If true, allows raw HTML. */
     private boolean m_allowHTML = false;
 
     /** If true, executes plugins; otherwise ignores them. */
     private boolean m_enablePlugins = true;
 
     /** DOCUMENT ME! */
     private boolean m_useRelNofollow = false;
 
     /** DOCUMENT ME! */
     private PatternMatcher m_matcher = new Perl5Matcher();
 
     /** DOCUMENT ME! */
     private PatternCompiler m_compiler = new Perl5Compiler();
 
     /** DOCUMENT ME! */
     private Pattern m_camelCasePtrn;
 
     /** DOCUMENT ME! */
     private TextRenderer m_renderer;
 
     /** DOCUMENT ME! */
     private boolean m_parseAccessRules = true;
 
     /**
      * Creates a TranslatorReader using the default HTML renderer.
      *
      * @param context DOCUMENT ME!
      * @param in DOCUMENT ME!
      */
     public TranslatorReader(WikiContext context, Reader in)
     {
         initialize(context, in, new HTMLRenderer());
     }
 
     /**
      * Creates a new TranslatorReader object.
      *
      * @param context DOCUMENT ME!
      * @param in DOCUMENT ME!
      * @param renderer DOCUMENT ME!
      */
     public TranslatorReader(WikiContext context, Reader in, TextRenderer renderer)
     {
         initialize(context, in, renderer);
     }
 
     /**
      * Replaces the current input character stream with a new one.
      *
      * @param in New source for input.  If null, this method does nothing.
      *
      * @return the old stream
      */
     public Reader setInputReader(Reader in)
     {
         Reader old = m_in;
 
         if (in != null)
         {
             m_in = new PushbackReader(new BufferedReader(in), PUSHBACK_BUFFER_SIZE);
         }
 
         return old;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param context The WikiEngine this reader is attached to.  Is used to figure out of a page
      *        exits.
      * @param in DOCUMENT ME!
      * @param renderer DOCUMENT ME!
      *
      * @throws InternalWikiException DOCUMENT ME!
      */
 
     // FIXME: TranslatorReaders should be pooled for better performance.
     private void initialize(WikiContext context, Reader in, TextRenderer renderer)
     {
         PatternCompiler compiler = new GlobCompiler();
         ArrayList compiledpatterns = new ArrayList();
 
         m_engine = context.getEngine();
         m_context = context;
 
         m_renderer = renderer;
 
         setInputReader(in);
 
         Collection ptrns = getImagePatterns(m_engine);
 
         //
         //  Make them into Regexp Patterns.  Unknown patterns
         //  are ignored.
         //
         for (Iterator i = ptrns.iterator(); i.hasNext();)
         {
             try
             {
                 compiledpatterns.add(compiler.compile((String) i.next()));
             }
             catch (MalformedPatternException e)
             {
                 log.error("Malformed pattern in properties: ", e);
             }
         }
 
         m_inlineImagePatterns = compiledpatterns;
 
         try
         {
             m_camelCasePtrn =
                 m_compiler.compile(
                     "^([[:^alnum:]]*|\\~)([[:upper:]]+[[:lower:]]+[[:upper:]]+[[:alnum:]]*)[[:^alnum:]]*$");
         }
         catch (MalformedPatternException e)
         {
             log.fatal("Internal error: Someone put in a faulty pattern.", e);
             throw new InternalWikiException("Faulty camelcasepattern in TranslatorReader");
         }
 
         //
         //  Set the properties.
         //
         Configuration conf = m_engine.getWikiConfiguration();
 
         m_camelCaseLinks = conf.getBoolean(PROP_CAMELCASELINKS, PROP_CAMELCASELINKS_DEFAULT);
 
         // m_plainUris = conf.getBoolean(PROP_PLAINURIS, PROP_PLAINURIS_DEFAULT);
 
         m_useOutlinkImage = conf.getBoolean(PROP_USEOUTLINKIMAGE, PROP_USEOUTLINKIMAGE_DEFAULT);
 
         m_allowHTML = conf.getBoolean(PROP_ALLOWHTML, PROP_ALLOWHTML_DEFAULT);
 
         m_useRelNofollow = conf.getBoolean(PROP_USERRELNOFOLLOW, PROP_USERRELNOFOLLOW_DEFAULT);
 
         String runplugins = m_engine.getVariable(m_context, PROP_RUNPLUGINS);
 
         if (runplugins != null)
         {
             enablePlugins(BooleanUtils.toBoolean(runplugins));
         }
 
         if (
             (m_engine.getUserManager() == null)
                         || (m_engine.getUserManager().getAuthenticator() == null))
         {
             disableAccessRules();
         }
     }
 
     /**
      * Sets the currently used renderer.  This method is protected because we only want to use it
      * internally for now.  The renderer interface is not yet set to stone, so it's not expected
      * that third parties would use this.
      *
      * @param renderer DOCUMENT ME!
      */
     protected void setRenderer(TextRenderer renderer)
     {
         m_renderer = renderer;
     }
 
     /**
      * Adds a hook for processing link texts.  This hook is called when the link text is written
      * into the output stream, and you may use it to modify the text.  It does not affect the
      * actual link, only the user-visible text.
      *
      * @param mutator The hook to call.  Null is safe.
      */
     public void addLinkTransmutator(StringTransmutator mutator)
     {
         if (mutator != null)
         {
             m_linkMutators.add(mutator);
         }
     }
 
     /**
      * Adds a hook for processing local links.  The engine transforms both non-existing and
      * existing page links.
      *
      * @param mutator The hook to call.  Null is safe.
      */
     public void addLocalLinkHook(StringTransmutator mutator)
     {
         if (mutator != null)
         {
             m_localLinkMutatorChain.add(mutator);
         }
     }
 
     /**
      * Adds a hook for processing external links.  This includes all http:// ftp://, etc. links,
      * including inlined images.
      *
      * @param mutator The hook to call.  Null is safe.
      */
     public void addExternalLinkHook(StringTransmutator mutator)
     {
         if (mutator != null)
         {
             m_externalLinkMutatorChain.add(mutator);
         }
     }
 
     /**
      * Adds a hook for processing attachment links.
      *
      * @param mutator The hook to call.  Null is safe.
      */
     public void addAttachmentLinkHook(StringTransmutator mutator)
     {
         if (mutator != null)
         {
             m_attachmentLinkMutatorChain.add(mutator);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param listener DOCUMENT ME!
      */
     public void addHeadingListener(HeadingListener listener)
     {
         if (listener != null)
         {
             m_headingListenerChain.add(listener);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void disableAccessRules()
     {
         m_parseAccessRules = false;
     }
 
     /**
      * Can be used to turn on plugin execution on a translator-reader basis
      *
      * @param toggle DOCUMENT ME!
      */
     public void enablePlugins(boolean toggle)
     {
         m_enablePlugins = toggle;
     }
 
     /**
      * Figure out which image suffixes should be inlined.
      *
      * @param engine DOCUMENT ME!
      *
      * @return Collection of Strings with patterns.
      */
     protected static Collection getImagePatterns(WikiEngine engine)
     {
         Configuration conf = engine.getWikiConfiguration();
 
         Configuration patternConf = conf.subset(PROP_INLINEIMAGEPTRN);
 
         List ptrnlist = new ArrayList();
 
         for (Iterator it = patternConf.getKeys(); it.hasNext();)
         {
             String key = (String) it.next();
             ptrnlist.add(patternConf.getString(key));
         }
 
         if (ptrnlist.size() == 0)
         {
             ptrnlist.add(PROP_INLINEIMAGEPTRN_DEFAULT);
         }
 
         return ptrnlist;
     }
 
     /**
      * Returns link name, if it exists; otherwise it returns null.
      *
      * @param page DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private String linkExists(String page)
     {
         try
         {
             return m_engine.getFinalPageName(page);
         }
         catch (ProviderException e)
         {
             log.warn("TranslatorReader got a faulty page name!", e);
 
             return page; // FIXME: What would be the correct way to go back?
         }
     }
 
     /**
      * Calls a transmutator chain.
      *
      * @param list Chain to call
      * @param text Text that should be passed to the mutate() method of each of the mutators in the
      *        chain.
      *
      * @return The result of the mutation.
      */
     private String callMutatorChain(Collection list, String text)
     {
         if ((list == null) || (list.size() == 0))
         {
             return text;
         }
 
         for (Iterator i = list.iterator(); i.hasNext();)
         {
             StringTransmutator m = (StringTransmutator) i.next();
 
             text = m.mutate(m_context, text);
         }
 
         return text;
     }
 
     private void callHeadingListenerChain(Heading param)
     {
         List list = m_headingListenerChain;
 
         for (Iterator i = list.iterator(); i.hasNext();)
         {
             HeadingListener h = (HeadingListener) i.next();
 
             h.headingAdded(m_context, param);
         }
     }
 
     /**
      * Write a HTMLized link depending on its type. The link mutator chain is processed.
      *
      * @param type Type of the link.
      * @param link The actual link.
      * @param text The user-visible text for the link.
      *
      * @return DOCUMENT ME!
      */
     public String makeLink(int type, String link, String text)
     {
         if (text == null)
         {
             text = link;
         }
 
         text = callMutatorChain(m_linkMutators, text);
 
         return m_renderer.makeLink(type, link, text);
     }
 
     /**
      * Just like makeLink, but also adds the section reference (#sect...)
      *
      * @param type DOCUMENT ME!
      * @param link DOCUMENT ME!
      * @param text DOCUMENT ME!
      * @param sectref DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private String makeLink(int type, String link, String text, String sectref)
     {
         if (text == null)
         {
             text = link;
         }
 
         text = callMutatorChain(m_linkMutators, text);
 
         return m_renderer.makeLink(type, link, text, sectref);
     }
 
     /**
      * Cleans a Wiki name.
      *
      * <P>
      * [ This is a link ] -&gt; ThisIsALink
      * </p>
      *
      * @param link Link to be cleared. Null is safe, and causes this to return null.
      *
      * @return A cleaned link.
      *
      * @since 2.0
      */
     public static String cleanLink(String link)
     {
         StringBuffer clean = new StringBuffer();
 
         if (link == null)
         {
             return null;
         }
 
         //
         //  Compress away all whitespace and capitalize
         //  all words in between.
         //
         StringTokenizer st = new StringTokenizer(link, " -");
 
         while (st.hasMoreTokens())
         {
             StringBuffer component = new StringBuffer(st.nextToken());
 
             component.setCharAt(0, Character.toUpperCase(component.charAt(0)));
 
             //
             //  We must do this, because otherwise compiling on JDK 1.4 causes
             //  a downwards incompatibility to JDK 1.3.
             //
             clean.append(component.toString());
         }
 
         //
         //  Remove non-alphanumeric characters that should not
         //  be put inside WikiNames.  Note that all valid
         //  Unicode letters are considered okay for WikiNames.
         //  It is the problem of the WikiPageProvider to take
         //  care of actually storing that information.
         //
         for (int i = 0; i < clean.length(); i++)
         {
             char ch = clean.charAt(i);
 
             if (!(Character.isLetterOrDigit(ch) || (PUNCTUATION_CHARS_ALLOWED.indexOf(ch) != -1)))
             {
                 clean.deleteCharAt(i);
                 --i; // We just shortened this buffer.
             }
         }
 
         return clean.toString();
     }
 
     /**
      * Figures out if a link is an off-site link.  This recognizes the most common protocols by
      * checking how it starts.
      *
      * @param link DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
 
     // FIXME: Should really put the external link types to a sorted set,
     //        then searching for them would be faster.
     private boolean isExternalLink(String link)
     {
         for (int i = 0; i < EXTERNAL_LINKS.length; i++)
         {
             if (link.startsWith(EXTERNAL_LINKS[i]))
             {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Returns true, if the link in question is an access rule.
      *
      * @param link DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private static boolean isAccessRule(String link)
     {
         return link.startsWith("{ALLOW") || link.startsWith("{DENY");
     }
 
     /**
      * Matches the given link to the list of image name patterns to determine whether it should be
      * treated as an inline image or not.
      *
      * @param link DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private boolean isImageLink(String link)
     {
         for (Iterator i = m_inlineImagePatterns.iterator(); i.hasNext();)
         {
             if (m_inlineMatcher.matches(link, (Pattern) i.next()))
             {
                 return true;
             }
         }
 
         return false;
     }
 
     private static boolean isMetadata(String link)
     {
         return link.startsWith("{SET");
     }
 
     /**
      * Returns true, if the argument contains a number, otherwise false. In a quick test this is
      * roughly the same speed as Integer.parseInt() if the argument is a number, and roughly ten
      * times the speed, if the argument is NOT a number.
      *
      * @param s DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private boolean isNumber(String s)
     {
         if (s == null)
         {
             return false;
         }
 
         if ((s.length() > 1) && (s.charAt(0) == '-'))
         {
             s = s.substring(1);
         }
 
         for (int i = 0; i < s.length(); i++)
         {
             if (!Character.isDigit(s.charAt(i)))
             {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Checks for the existence of a traditional style CamelCase link.
      *
      * <P>
      * We separate all white-space -separated words, and feed it to this routine to find if there
      * are any possible camelcase links. For example, if "word" is "__HyperLink__" we return
      * "HyperLink".
      * </p>
      *
      * @param word A phrase to search in.
      *
      * @return The match within the phrase.  Returns null, if no CamelCase hyperlink exists within
      *         this phrase.
      */
     private String checkForCamelCaseLink(String word)
     {
         PatternMatcherInput input;
 
         input = new PatternMatcherInput(word);
 
         if (m_matcher.contains(input, m_camelCasePtrn))
         {
             MatchResult res = m_matcher.getMatch();
 
             String link = res.group(2);
 
             if (res.group(1) != null)
             {
                 if (res.group(1).equals("~") || (res.group(1).indexOf('[') != -1))
                 {
                     // Delete the (~) from beginning.
                     // We'll make '~' the generic kill-processing-character from
                     // now on.
                     return null;
                 }
             }
 
             return link;
         } // if match
 
         return null;
     }
 
     /**
      * When given a link to a WikiName, we just return a proper HTML link for it.  The local link
      * mutator chain is also called.
      *
      * @param wikiname DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private String makeCamelCaseLink(String wikiname)
     {
         String matchedLink;
         String link;
 
         callMutatorChain(m_localLinkMutatorChain, wikiname);
 
         if ((matchedLink = linkExists(wikiname)) != null)
         {
             link = makeLink(READ, matchedLink, wikiname);
         }
         else
         {
             link = makeLink(EDIT, wikiname, wikiname);
         }
 
         return link;
     }
 
     private String makeDirectURILink(String url)
     {
         String last = "";
         String result;
 
         if (url.endsWith(",") || url.endsWith("."))
         {
             last = url.substring(url.length() - 1);
             url = url.substring(0, url.length() - 1);
         }
 
         callMutatorChain(m_externalLinkMutatorChain, url);
 
         if (isImageLink(url))
         {
             result = handleImageLink(url, url, false);
         }
         else
         {
             result = makeLink(EXTERNAL, url, url) + m_renderer.outlinkImage();
         }
 
         result += last;
 
         return result;
     }
 
     /**
      * Image links are handled differently: 1. If the text is a WikiName of an existing page, it
      * gets linked. 2. If the text is an external link, then it is inlined. 3. Otherwise it
      * becomes an ALT text.
      *
      * @param reallink The link to the image.
      * @param link Link text portion, may be a link to somewhere else.
      * @param hasLinkText If true, then the defined link had a link text available. This means that
      *        the link text may be a link to a wiki page, or an external resource.
      *
      * @return DOCUMENT ME!
      */
     private String handleImageLink(String reallink, String link, boolean hasLinkText)
     {
         String possiblePage = cleanLink(link);
         String matchedLink;
         String res = "";
 
         if (isExternalLink(link) && hasLinkText)
         {
             res = makeLink(IMAGELINK, reallink, link);
         }
         else if (((matchedLink = linkExists(possiblePage)) != null) && hasLinkText)
         {
             // System.out.println("Orig="+link+", Matched: "+matchedLink);
             callMutatorChain(m_localLinkMutatorChain, possiblePage);
 
             res = makeLink(IMAGEWIKILINK, reallink, link);
         }
         else
         {
             res = makeLink(IMAGE, reallink, link);
         }
 
         return res;
     }
 
     private String handleAccessRule(String ruleLine)
     {
         if (!m_parseAccessRules)
         {
             return "";
         }
 
         AccessControlList acl;
         WikiPage page = m_context.getPage();
         UserManager mgr = m_context.getEngine().getUserManager();
 
         if (ruleLine.startsWith("{"))
         {
             ruleLine = ruleLine.substring(1);
         }
 
         if (ruleLine.endsWith("}"))
         {
             ruleLine = ruleLine.substring(0, ruleLine.length() - 1);
         }
 
         if (log.isDebugEnabled())
         {
             log.debug("page=" + page.getName() + ", ACL = " + ruleLine);
         }
 
         try
         {
             acl = PageAuthorizer.parseAcl(page, mgr, ruleLine);
 
             page.setAcl(acl);
 
             log.debug(acl.toString());
         }
         catch (WikiSecurityException wse)
         {
             return m_renderer.makeError(wse.getMessage());
         }
 
         return "";
     }
 
     /**
      * Handles metadata setting [{SET foo=bar}]
      *
      * @param link DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private String handleMetadata(String link)
     {
         try
         {
             String args = link.substring(link.indexOf(' '), link.length() - 1);
 
             String name = args.substring(0, args.indexOf('='));
             String val = args.substring(args.indexOf('=') + 1, args.length());
 
             name = name.trim();
             val = val.trim();
 
             if (val.startsWith("'"))
             {
                 val = val.substring(1);
             }
 
             if (val.endsWith("'"))
             {
                 val = val.substring(0, val.length() - 1);
             }
 
             // log.debug("SET name='"+name+"', value='"+val+"'.");
             if ((name.length() > 0) && (val.length() > 0))
             {
                 val = m_engine.getVariableManager().expandVariables(m_context, val);
 
                 m_context.getPage().setAttribute(name, val);
             }
         }
         catch (Exception e)
         {
             m_renderer.makeError(" Invalid SET found: " + link);
         }
 
         return "";
     }
 
     /**
      * Gobbles up all hyperlinks that are encased in square brackets.
      *
      * @param link DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private String handleHyperlinks(String link)
     {
         StringBuffer sb = new StringBuffer();
         String reallink;
         int cutpoint;
 
         if (isAccessRule(link))
         {
             return handleAccessRule(link);
         }
 
         if (isMetadata(link))
         {
             return handleMetadata(link);
         }
 
         if (PluginManager.isPluginLink(link))
         {
             String included = "";
 
             try
             {
                 if (m_enablePlugins)
                 {
                     included = m_engine.getPluginManager().execute(m_context, link);
                 }
             }
             catch (PluginException e)
             {
                 if (log.isInfoEnabled())
                 {
                     log.info("Failed to insert plugin", e);
                     log.info("Root cause:", e.getRootThrowable());
                 }
 
                 included = m_renderer.makeError("Plugin insertion failed: " + e.getMessage());
             }
 
             sb.append(included);
 
             return sb.toString();
         }
 
         link = TextUtil.replaceEntities(link);
 
         if ((cutpoint = link.indexOf('|')) != -1)
         {
             reallink = link.substring(cutpoint + 1).trim();
             link = link.substring(0, cutpoint);
         }
         else
         {
             reallink = link.trim();
         }
 
         int interwikipoint = -1;
 
         //
         //  Yes, we now have the components separated.
         //  link     = the text the link should have
         //  reallink = the url or page name.
         //
         //  In many cases these are the same.  [link|reallink].
         //
         if (VariableManager.isVariableLink(link))
         {
             String value;
 
             try
             {
                 value = m_engine.getVariableManager().parseAndGetValue(m_context, link);
             }
             catch (NoSuchVariableException e)
             {
                 value = m_renderer.makeError(e.getMessage());
             }
             catch (IllegalArgumentException e)
             {
                 value = m_renderer.makeError(e.getMessage());
             }
 
             sb.append(value);
         }
         else if (isExternalLink(reallink))
         {
             // It's an external link, out of this Wiki
             callMutatorChain(m_externalLinkMutatorChain, reallink);
 
             if (isImageLink(reallink))
             {
                 sb.append(handleImageLink(reallink, link, (cutpoint != -1)));
             }
             else
             {
                 sb.append(makeLink(EXTERNAL, reallink, link));
                 sb.append(m_renderer.outlinkImage());
             }
         }
         else if ((interwikipoint = reallink.indexOf(":")) != -1)
         {
             // It's an interwiki link
             // InterWiki links also get added to external link chain
             // after the links have been resolved.
             // FIXME: There is an interesting issue here:  We probably should
             //        URLEncode the wikiPage, but we can't since some of the
             //        Wikis use slashes (/), which won't survive URLEncoding.
             //        Besides, we don't know which character set the other Wiki
             //        is using, so you'll have to write the entire name as it appears
             //        in the URL.  Bugger.
             String extWiki = reallink.substring(0, interwikipoint);
             String wikiPage = reallink.substring(interwikipoint + 1);
 
             String urlReference = m_engine.getInterWikiURL(extWiki);
 
             if (urlReference != null)
             {
                 urlReference = StringUtils.replace(urlReference, "%s", wikiPage);
                 callMutatorChain(m_externalLinkMutatorChain, urlReference);
 
                 sb.append(makeLink(INTERWIKI, urlReference, link));
 
                 if (isExternalLink(urlReference))
                 {
                     sb.append(m_renderer.outlinkImage());
                 }
             }
             else
             {
                 sb.append(link)
                         .append(" ")
                         .append(m_renderer.makeError("No InterWiki reference defined in properties for Wiki called '"
                                         + extWiki + "'!)"));
             }
         }
         else if (reallink.startsWith("#"))
         {
             // It defines a local footnote
             sb.append(makeLink(LOCAL, reallink, link));
         }
         else if (isNumber(reallink))
         {
             // It defines a reference to a local footnote
             sb.append(makeLink(LOCALREF, reallink, link));
         }
         else
         {
             int hashMark = -1;
 
             //
             //  Internal wiki link, but is it an attachment link?
             //
             String attachment = findAttachment(reallink);
 
             if (attachment != null)
             {
                 callMutatorChain(m_attachmentLinkMutatorChain, attachment);
 
                 if (isImageLink(reallink))
                 {
                     attachment = m_context.getURL(WikiContext.ATTACH, attachment);
                     sb.append(handleImageLink(attachment, link, (cutpoint != -1)));
                 }
                 else
                 {
                     sb.append(makeLink(ATTACHMENT, attachment, link));
                 }
             }
             else if ((hashMark = reallink.indexOf('#')) != -1)
             {
                 // It's an internal Wiki link, but to a named section
                 String namedSection = reallink.substring(hashMark + 1);
                 reallink = reallink.substring(0, hashMark);
 
                 reallink = cleanLink(reallink);
 
                 callMutatorChain(m_localLinkMutatorChain, reallink);
 
                 String matchedLink;
 
                 if ((matchedLink = linkExists(reallink)) != null)
                 {
                     StringBuffer sectref = new StringBuffer();
                     sectref.append("section-")
                             .append(m_engine.encodeName(matchedLink))
                             .append("-")
                             .append(namedSection);
 
                     sb.append(makeLink(READ, matchedLink, link, sectref.toString()));
                 }
                 else
                 {
                     sb.append(makeLink(EDIT, reallink, link));
                 }
             }
             else
             {
                 // It's an internal Wiki link
                 reallink = cleanLink(reallink);
 
                 callMutatorChain(m_localLinkMutatorChain, reallink);
 
                 String matchedLink;
 
                 if ((matchedLink = linkExists(reallink)) != null)
                 {
                     sb.append(makeLink(READ, matchedLink, link));
                 }
                 else
                 {
                     sb.append(makeLink(EDIT, reallink, link));
                 }
             }
         }
 
         return sb.toString();
     }
 
     private String findAttachment(String link)
     {
         AttachmentManager mgr = m_engine.getAttachmentManager();
         Attachment att = null;
 
         try
         {
             att = mgr.getAttachmentInfo(m_context, link);
         }
         catch (ProviderException e)
         {
             log.warn("Finding attachments failed: ", e);
 
             return null;
         }
 
         if (att != null)
         {
             return att.getName();
         }
         else if (link.indexOf('/') != -1)
         {
             return link;
         }
 
         return null;
     }
 
     /**
      * Closes all annoying lists and things that the user might've left open.
      *
      * @return DOCUMENT ME!
      */
     private String closeAll()
     {
         StringBuffer buf = new StringBuffer();
 
         if (m_isbold)
         {
             buf.append(m_renderer.closeTextEffect(BOLD));
             m_isbold = false;
         }
 
         if (m_isitalic)
         {
             buf.append(m_renderer.closeTextEffect(ITALIC));
             m_isitalic = false;
         }
 
         if (m_isTypedText)
         {
             buf.append(m_renderer.closeTextEffect(TYPED));
             m_isTypedText = false;
         }
 
         /*
           for( ; m_listlevel > 0; m_listlevel-- )
           {
           buf.append( "</ul>\n" );
           }
 
           for( ; m_numlistlevel > 0; m_numlistlevel-- )
           {
           buf.append( "</ol>\n" );
           }
         */
         // cleanup OL and UL lists
         buf.append(unwindGeneralList());
 
         if (m_isPre)
         {
             buf.append(m_renderer.closePreformatted());
             m_isEscaping = false;
             m_isPre = false;
         }
 
         if (m_istable)
         {
             buf.append(m_renderer.closeTable());
             m_istable = false;
         }
 
         if (m_isOpenParagraph)
         {
             buf.append(m_renderer.closeParagraph());
             m_isOpenParagraph = false;
         }
 
         return buf.toString();
     }
 
     private int nextToken()
             throws IOException
     {
         if (m_in == null)
         {
             return -1;
         }
 
         return m_in.read();
     }
 
     /**
      * Push back any character to the current input.  Does not push back a read EOF, though.
      *
      * @param c DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     private void pushBack(int c)
             throws IOException
     {
         if ((c != -1) && (m_in != null))
         {
             m_in.unread(c);
         }
     }
 
     /**
      * Pushes back any string that has been read.  It will obviously be pushed back in a reverse
      * order.
      *
      * @param s DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      *
      * @since 2.1.77
      */
     private void pushBack(String s)
             throws IOException
     {
         for (int i = s.length() - 1; i >= 0; i--)
         {
             pushBack(s.charAt(i));
         }
     }
 
     private String handleBackslash()
             throws IOException
     {
         int ch = nextToken();
 
         if (ch == '\\')
         {
             int ch2 = nextToken();
 
             if (ch2 == '\\')
             {
                 return m_renderer.lineBreak(true);
             }
 
             pushBack(ch2);
 
             return m_renderer.lineBreak(false);
         }
 
         pushBack(ch);
 
         return "\\";
     }
 
     private String handleUnderscore()
             throws IOException
     {
         int ch = nextToken();
         String res = "_";
 
         if (ch == '_')
         {
             res = m_isbold
                 ? m_renderer.closeTextEffect(BOLD)
                 : m_renderer.openTextEffect(BOLD);
             m_isbold = !m_isbold;
         }
         else
         {
             pushBack(ch);
         }
 
         return res;
     }
 
     /**
      * For example: italics.
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     private String handleApostrophe()
             throws IOException
     {
         int ch = nextToken();
         String res = "'";
 
         if (ch == '\'')
         {
             res = m_isitalic
                 ? m_renderer.closeTextEffect(ITALIC)
                 : m_renderer.openTextEffect(ITALIC);
             m_isitalic = !m_isitalic;
         }
         else
         {
             pushBack(ch);
         }
 
         return res;
     }
 
     private String handleOpenbrace(boolean isBlock)
             throws IOException
     {
         int ch = nextToken();
         String res = "{";
 
         if (ch == '{')
         {
             int ch2 = nextToken();
 
             if (ch2 == '{')
             {
                 res = startBlockLevel() + m_renderer.openPreformatted(isBlock);
                 m_isPre = true;
                 m_isEscaping = true;
             }
             else
             {
                 pushBack(ch2);
 
                 res = m_renderer.openTextEffect(TYPED);
                 m_isTypedText = true;
             }
         }
         else
         {
             pushBack(ch);
         }
 
         return res;
     }
 
     /**
      * Handles both }} and }}}
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     private String handleClosebrace()
             throws IOException
     {
         String res = "}";
 
         int ch2 = nextToken();
 
         if (ch2 == '}')
         {
             int ch3 = nextToken();
 
             if (ch3 == '}')
             {
                 if (m_isPre)
                 {
                     m_isPre = false;
                     m_isEscaping = false;
                     res = m_renderer.closePreformatted();
                 }
                 else
                 {
                     res = "}}}";
                 }
             }
             else
             {
                 pushBack(ch3);
 
                 if (!m_isEscaping)
                 {
                     res = m_renderer.closeTextEffect(TYPED);
                     m_isTypedText = false;
                 }
                 else
                 {
                     pushBack(ch2);
                 }
             }
         }
         else
         {
             pushBack(ch2);
         }
 
         return res;
     }
 
     private String handleDash()
             throws IOException
     {
         int ch = nextToken();
 
         if (ch == '-')
         {
             int ch2 = nextToken();
 
             if (ch2 == '-')
             {
                 int ch3 = nextToken();
 
                 if (ch3 == '-')
                 {
                     // Empty away all the rest of the dashes.
                     // Do not forget to return the first non-match back.
                     while ((ch = nextToken()) == '-');
 
                     pushBack(ch);
 
                     return startBlockLevel() + m_renderer.makeRuler();
                 }
 
                 pushBack(ch3);
             }
 
             pushBack(ch2);
         }
 
         pushBack(ch);
 
         return "-";
     }
 
     /**
      * This method peeks ahead in the stream until EOL and returns the result. It will keep the
      * buffers untouched.
      *
      * @return The string from the current position to the end of line.
      *
      * @throws IOException DOCUMENT ME!
      */
 
     // FIXME: Always returns an empty line, even if the stream is full.
     private String peekAheadLine()
             throws IOException
     {
         String s = readUntilEOL().toString();
         pushBack(s);
 
         return s;
     }
 
     private String handleHeading()
             throws IOException
     {
         StringBuffer buf = new StringBuffer();
 
         int ch = nextToken();
 
         Heading hd = new Heading();
 
         if (ch == '!')
         {
             int ch2 = nextToken();
 
             if (ch2 == '!')
             {
                 String title = peekAheadLine();
 
                 buf.append(m_renderer.makeHeading(Heading.HEADING_LARGE, title, hd));
             }
             else
             {
                 pushBack(ch2);
 
                 String title = peekAheadLine();
                 buf.append(m_renderer.makeHeading(Heading.HEADING_MEDIUM, title, hd));
             }
         }
         else
         {
             pushBack(ch);
 
             String title = peekAheadLine();
             buf.append(m_renderer.makeHeading(Heading.HEADING_SMALL, title, hd));
         }
 
         callHeadingListenerChain(hd);
 
         return buf.toString();
     }
 
     /**
      * Reads the stream until the next EOL or EOF.  Note that it will also read the EOL from the
      * stream.
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     private StringBuffer readUntilEOL()
             throws IOException
     {
         int ch;
         StringBuffer buf = new StringBuffer();
 
         while (true)
         {
             ch = nextToken();
 
             if (ch == -1)
             {
                 break;
             }
 
             buf.append((char) ch);
 
             if (ch == '\n')
             {
                 break;
             }
         }
 
         return buf;
     }
 
     /**
      * Starts a block level element, therefore closing the a potential open paragraph tag.
      *
      * @return DOCUMENT ME!
      */
     private String startBlockLevel()
     {
         if (m_isOpenParagraph)
         {
             m_isOpenParagraph = false;
 
             return m_renderer.closeParagraph();
         }
 
         return "";
     }
 
     /**
      * Like original handleOrderedList() and handleUnorderedList() however handles both ordered
      * ('#') and unordered ('') mixed together.
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
 
     // FIXME: Refactor this; it's a bit messy.
     private String handleGeneralList()
             throws IOException
     {
         StringBuffer buf = new StringBuffer();
 
         buf.append(startBlockLevel());
 
         String strBullets = readWhile("*#");
 
         int numBullets = strBullets.length();
 
         // override the beginning portion of bullet pattern to be like the previous
         // to simulate PHPWiki style lists
         if (m_allowPHPWikiStyleLists)
         {
             // only substitute if different
             if (
                 !(strBullets.substring(0, Math.min(numBullets, m_genlistlevel)).equals(
                                 m_genlistBulletBuffer.substring(
                                     0, Math.min(numBullets, m_genlistlevel)))))
             {
                 if (numBullets <= m_genlistlevel)
                 {
                     // Substitute all but the last character (keep the expressed bullet preference)
                     strBullets =
                         ((numBullets > 1)
                         ? m_genlistBulletBuffer.substring(0, numBullets - 1)
                         : "") + strBullets.substring(numBullets - 1, numBullets);
                 }
                 else
                 {
                     strBullets =
                         m_genlistBulletBuffer + strBullets.substring(m_genlistlevel, numBullets);
                 }
             }
         }
 
         //
         //  Check if this is still of the same type
         //
         if (
             strBullets.substring(0, Math.min(numBullets, m_genlistlevel)).equals(
                             m_genlistBulletBuffer.substring(
                                 0, Math.min(numBullets, m_genlistlevel))))
         {
             if (numBullets > m_genlistlevel)
             {
                 buf.append(m_renderer.openList(strBullets.charAt(m_genlistlevel++)));
 
                 for (; m_genlistlevel < numBullets; m_genlistlevel++)
                 {
                     // bullets are growing, get from new bullet list
                     buf.append(m_renderer.openListItem());
                     buf.append(m_renderer.openList(strBullets.charAt(m_genlistlevel)));
                 }
             }
             else if (numBullets < m_genlistlevel)
             {
                 //  Close the previous list item.
                 buf.append(m_renderer.closeListItem());
 
                 for (; m_genlistlevel > numBullets; m_genlistlevel--)
                 {
                     // bullets are shrinking, get from old bullet list
                     buf.append(
                         m_renderer.closeList(m_genlistBulletBuffer.charAt(m_genlistlevel - 1)));
 
                     if (m_genlistlevel > 0)
                     {
                         buf.append(m_renderer.closeListItem());
                     }
                 }
             }
             else
             {
                 if (m_genlistlevel > 0)
                 {
                     buf.append(m_renderer.closeListItem());
                 }
             }
         }
         else
         {
             //
             //  The pattern has changed, unwind and restart
             //
             int numEqualBullets;
             int numCheckBullets;
 
             // find out how much is the same
             numEqualBullets = 0;
             numCheckBullets = Math.min(numBullets, m_genlistlevel);
 
             while (numEqualBullets < numCheckBullets)
             {
                 // if the bullets are equal so far, keep going
                 if (
                     strBullets.charAt(numEqualBullets) == m_genlistBulletBuffer.charAt(
                                     numEqualBullets))
                 {
                     numEqualBullets++;
                 }
 
                 // otherwise giveup, we have found how many are equal
                 else
                 {
                     break;
                 }
             }
 
             //unwind
             for (; m_genlistlevel > numEqualBullets; m_genlistlevel--)
             {
                 buf.append(m_renderer.closeList(m_genlistBulletBuffer.charAt(m_genlistlevel - 1)));
 
                 if (m_genlistlevel > 0)
                 {
                     buf.append(m_renderer.closeListItem());
                 }
             }
 
             //rewind
             buf.append(m_renderer.openList(strBullets.charAt(numEqualBullets++)));
 
             for (int i = numEqualBullets; i < numBullets; i++)
             {
                 buf.append(m_renderer.openListItem());
                 buf.append(m_renderer.openList(strBullets.charAt(i)));
             }
 
             m_genlistlevel = numBullets;
         }
 
         buf.append(m_renderer.openListItem());
 
         // work done, remember the new bullet list (in place of old one)
         m_genlistBulletBuffer.setLength(0);
         m_genlistBulletBuffer.append(strBullets);
 
         return buf.toString();
     }
 
     private String unwindGeneralList()
     {
         StringBuffer buf = new StringBuffer();
 
         //unwind
         for (; m_genlistlevel > 0; m_genlistlevel--)
         {
             buf.append(m_renderer.closeListItem());
             buf.append(m_renderer.closeList(m_genlistBulletBuffer.charAt(m_genlistlevel - 1)));
         }
 
         m_genlistBulletBuffer.setLength(0);
 
         return buf.toString();
     }
 
     private String handleDefinitionList()
             throws IOException
     {
         if (!m_isdefinition)
         {
             m_isdefinition = true;
 
             m_closeTag = m_renderer.closeDefinitionItem() + m_renderer.closeDefinitionList();
 
             return startBlockLevel() + m_renderer.openDefinitionList()
             + m_renderer.openDefinitionTitle();
         }
 
         return ";";
     }
 
     private String handleOpenbracket()
             throws IOException
     {
         StringBuffer sb = new StringBuffer();
         int ch;
         boolean isPlugin = false;
 
         while ((ch = nextToken()) == '[')
         {
             sb.append((char) ch);
         }
 
         if (ch == '{')
         {
             isPlugin = true;
         }
 
         pushBack(ch);
 
         if (sb.length() > 0)
         {
             return sb.toString();
         }
 
         //
         //  Find end of hyperlink
         //
         ch = nextToken();
 
         while (ch != -1)
         {
             if ((ch == ']') && (!isPlugin || (sb.charAt(sb.length() - 1) == '}')))
             {
                 break;
             }
 
             sb.append((char) ch);
 
             ch = nextToken();
         }
 
         if (ch == -1)
         {
             log.debug("Warning: unterminated link detected!");
 
             return sb.toString();
         }
 
         return handleHyperlinks(sb.toString());
     }
 
     /**
      * Reads the stream until it meets one of the specified ending characters, or stream end.  The
      * ending character will be left in the stream.
      *
      * @param endChars DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     private String readUntil(String endChars)
             throws IOException
     {
         StringBuffer sb = new StringBuffer();
         int ch = nextToken();
 
         while (ch != -1)
         {
             if (ch == '\\')
             {
                 ch = nextToken();
 
                 if (ch == -1)
                 {
                     break;
                 }
             }
             else
             {
                 if (endChars.indexOf((char) ch) != -1)
                 {
                     pushBack(ch);
 
                     break;
                 }
             }
 
             sb.append((char) ch);
             ch = nextToken();
         }
 
         return sb.toString();
     }
 
     /**
      * Reads the stream while the characters that have been specified are in the stream, returning
      * then the result as a String.
      *
      * @param endChars DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     private String readWhile(String endChars)
             throws IOException
     {
         StringBuffer sb = new StringBuffer();
         int ch = nextToken();
 
         while (ch != -1)
         {
             if (ch == '\\')
             {
                 ch = nextToken();
 
                 if (ch == -1)
                 {
                     break;
                 }
             }
             else
             {
                 if (endChars.indexOf((char) ch) == -1)
                 {
                     pushBack(ch);
 
                     break;
                 }
             }
 
             sb.append((char) ch);
             ch = nextToken();
         }
 
         return sb.toString();
     }
 
     private String handleDiv(boolean newLine)
             throws IOException
     {
         int ch = nextToken();
 
         if (ch == '%')
         {
             StringBuffer sb = new StringBuffer();
 
             String style = null;
             String clazz = null;
 
             ch = nextToken();
 
             //
             //  Style or class?
             //
             if (ch == '(')
             {
                 style = readUntil(")");
                 nextToken(); // Pop the ) from the list, too.
             }
             else if (Character.isLetter((char) ch))
             {
                 pushBack(ch);
                 clazz = readUntil(" \t\n\r");
             }
             else
             {
                 //
                 // Anything else stops.
                 //
                 sb.append(m_renderer.closeDiv());
 
                 return sb.toString();
             }
 
             // sb.append( newLine ? "<div" : "<span" );
             sb.append(startBlockLevel());
             sb.append(m_renderer.openDiv(style, clazz));
 
             return sb.toString();
         }
 
         pushBack(ch);
 
         return "%";
     }
 
     private String handleBar(boolean newLine)
             throws IOException
     {
         StringBuffer sb = new StringBuffer();
 
         if (!m_istable && !newLine)
         {
             return "|";
         }
 
         if (newLine)
         {
             if (!m_istable)
             {
                 sb.append(startBlockLevel());
                 sb.append(m_renderer.openTable());
                 m_istable = true;
             }
 
             sb.append(m_renderer.openTableRow());
             m_closeTag = m_renderer.closeTableItem() + m_renderer.closeTableRow();
         }
 
         int ch = nextToken();
 
         if (ch == '|')
         {
             if (!newLine)
             {
                 sb.append(m_renderer.closeTableHeading());
             }
 
             sb.append(m_renderer.openTableHeading());
             m_closeTag = m_renderer.closeTableHeading() + m_renderer.closeTableRow();
         }
         else
         {
             if (!newLine)
             {
                 sb.append(m_renderer.closeTableItem());
             }
 
             sb.append(m_renderer.openTableItem());
             pushBack(ch);
         }
 
         return sb.toString();
     }
 
     /**
      * Generic escape of next character or entity.
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     private String handleTilde()
             throws IOException
     {
         int ch = nextToken();
 
         if (ch == '|')
         {
             return "|";
         }
 
         if (Character.isUpperCase((char) ch))
         {
             return String.valueOf((char) ch);
         }
 
         if (ch == '~')
         {
             return "~"; // Escapes itself.
         }
 
         // No escape.
         pushBack(ch);
 
         return "~";
     }
 
     private void fillBuffer()
             throws IOException
     {
         StringBuffer buf = new StringBuffer();
         StringBuffer word = null;
         int previousCh = -2;
         int start = 0;
 
         boolean quitReading = false;
         boolean newLine = true; // FIXME: not true if reading starts in middle of buffer
 
         while (!quitReading)
         {
             int ch = nextToken();
             String s = null;
 
             //
             //  Check if we're actually ending the preformatted mode.
             //  We still must do an entity transformation here.
             //
             if (m_isEscaping)
             {
                 if (ch == '}')
                 {
                     buf.append(handleClosebrace());
                 }
                 else if (ch == -1)
                 {
                     quitReading = true;
                 }
                 else
                 {
                     m_renderer.doChar(buf, (char) ch);
                 }
 
                 continue;
             }
 
             //
             //  CamelCase detection, a non-trivial endeavour.
             //  We keep track of all white-space separated entities, which we
             //  hereby refer to as "words".  We then check for an existence
             //  of a CamelCase format text string inside the "word", and
             //  if one exists, we replace it with a proper link.
             //
             if (m_camelCaseLinks)
             {
                 // Quick parse of start of a word boundary.
                 if (
                     (word == null)
                                 && (Character.isWhitespace((char) previousCh)
                                 || (WORD_SEPARATORS.indexOf((char) previousCh) != -1) || newLine)
                                 && !Character.isWhitespace((char) ch))
                 {
                     word = new StringBuffer();
                 }
 
                 // Are we currently tracking a word?
                 if (word != null)
                 {
                     //
                     //  Check for the end of the word.
                     //
                     if (
                         Character.isWhitespace((char) ch) || (ch == -1)
                                     || (WORD_SEPARATORS.indexOf((char) ch) != -1))
                     {
                         String potentialLink = word.toString();
 
                         String camelCase = checkForCamelCaseLink(potentialLink);
 
                         if (camelCase != null)
                         {
                             // System.out.println("Buffer is "+buf);
                             // System.out.println("  Replacing "+camelCase+" with proper link.");
                             start = buf.toString().lastIndexOf(camelCase);
                             buf.replace(
                                 start, start + camelCase.length(), makeCamelCaseLink(camelCase));
 
                             // System.out.println("  Resulting with "+buf);
                         }
                         else
                         {
                             // System.out.println("Checking for potential URI: "+potentialLink);
                             if (isExternalLink(potentialLink))
                             {
                                 // System.out.println("buf="+buf);
                                 start = buf.toString().lastIndexOf(potentialLink);
 
                                 if (start >= 0)
                                 {
                                     String link = readUntil(" \t()[]{}!\"'\n|");
 
                                     link = potentialLink + (char) ch + link; // Do not forget the start.
 
                                     // System.out.println("start="+start+", pl="+potentialLink);
                                     buf.replace(
                                         start, start + potentialLink.length(),
                                         makeDirectURILink(link));
 
                                     // System.out.println("Resulting with "+buf);
                                     ch = nextToken();
                                 }
                             }
                         }
 
                         // We've ended a word boundary, so time to reset.
                         word = null;
                     }
                     else
                     {
                         // This should only be appending letters and digits.
                         word.append((char) ch);
                     } // if end of word
                 } // if word's not null
 
                 // Always set the previous character to test for word starts.
                 previousCh = ch;
             } // if m_camelCaseLinks
 
             //
             //  An empty line stops a list
             //
             if (newLine && (ch != '*') && (ch != '#') && (ch != ' ') && (m_genlistlevel > 0))
             {
                 buf.append(unwindGeneralList());
             }
 
             if (newLine && (ch != '|') && m_istable)
             {
                 buf.append(m_renderer.closeTable());
                 m_istable = false;
                 m_closeTag = null;
             }
 
             //
             //  Now, check the incoming token.
             //
             switch (ch)
             {
             case '\r':
 
                 // DOS linefeeds we forget
                 s = null;
 
                 break;
 
             case '\n':
 
                 //
                 //  Close things like headings, etc.
                 //
                 if (m_closeTag != null)
                 {
                     buf.append(m_closeTag);
                     m_closeTag = null;
                 }
 
                 m_isdefinition = false;
 
                 if (newLine)
                 {
                     // Paragraph change.
                     buf.append(startBlockLevel());
 
                     //
                     //  Figure out which elements cannot be enclosed inside
                     //  a <p></p> pair according to XHTML rules.
                     //
                     String nextLine = peekAheadLine();
 
                     if (
                         (nextLine.length() == 0)
                                     || ((nextLine.length() > 0) && !nextLine.startsWith("{{{")
                                     && !nextLine.startsWith("----") && !nextLine.startsWith("%%")
                                     && ("*#!;".indexOf(nextLine.charAt(0)) == -1)))
                     {
                         buf.append(m_renderer.openParagraph());
                         m_isOpenParagraph = true;
                     }
                 }
                 else
                 {
                     buf.append("\n");
                     newLine = true;
                 }
 
                 break;
 
             case '\\':
                 s = handleBackslash();
 
                 break;
 
             case '_':
                 s = handleUnderscore();
 
                 break;
 
             case '\'':
                 s = handleApostrophe();
 
                 break;
 
             case '{':
                 s = handleOpenbrace(newLine);
 
                 break;
 
             case '}':
                 s = handleClosebrace();
 
                 break;
 
             case '-':
                 s = handleDash();
 
                 break;
 
             case '!':
 
                 if (newLine)
                 {
                     s = handleHeading();
                 }
                 else
                 {
                     s = "!";
                 }
 
                 break;
 
             case ';':
 
                 if (newLine)
                 {
                     s = handleDefinitionList();
                 }
                 else
                 {
                     s = ";";
                 }
 
                 break;
 
             case ':':
 
                 if (m_isdefinition)
                 {
                     s = m_renderer.closeDefinitionTitle() + m_renderer.openDefinitionItem();
                     m_isdefinition = false;
                 }
                 else
                 {
                     s = ":";
                 }
 
                 break;
 
             case '[':
                 s = handleOpenbracket();
 
                 break;
 
             case '*':
 
                 if (newLine)
                 {
                     pushBack('*');
                     s = handleGeneralList();
                 }
                 else
                 {
                     s = "*";
                 }
 
                 break;
 
             case '#':
 
                 if (newLine)
                 {
                     pushBack('#');
                     s = handleGeneralList();
                 }
                 else
                 {
                     s = "#";
                 }
 
                 break;
 
             case '|':
                 s = handleBar(newLine);
 
                 break;
 
             case '<':
                 s = m_allowHTML
                     ? "<"
                     : "&lt;";
 
                 break;
 
             case '>':
                 s = m_allowHTML
                     ? ">"
                     : "&gt;";
 
                 break;
 
             case '\"':
                 s = m_allowHTML
                     ? "\""
                     : "&quot;";
 
                 break;
 
             /*
               case '&':
               s = "&amp;";
               break;
             */
             case '~':
                 s = handleTilde();
 
                 break;
 
             case '%':
                 s = handleDiv(newLine);
 
                 break;
 
             case -1:
 
                 if (m_closeTag != null)
                 {
                     buf.append(m_closeTag);
                     m_closeTag = null;
                 }
 
                 quitReading = true;
 
                 break;
 
             default:
                 buf.append((char) ch);
                 newLine = false;
 
                 break;
             }
 
             if (s != null)
             {
                 buf.append(s);
                 newLine = false;
             }
         }
 
         m_data = new StringReader(buf.toString());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public int read()
             throws IOException
     {
         int val = m_data.read();
 
         if (val == -1)
         {
             fillBuffer();
             val = m_data.read();
 
             if (val == -1)
             {
                 m_data = new StringReader(closeAll());
 
                 val = m_data.read();
             }
         }
 
         return val;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param buf DOCUMENT ME!
      * @param off DOCUMENT ME!
      * @param len DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public int read(char [] buf, int off, int len)
             throws IOException
     {
         return m_data.read(buf, off, len);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public boolean ready()
             throws IOException
     {
         if (log.isDebugEnabled())
         {
             log.debug("ready ? " + m_data.ready());
         }
 
         if (!m_data.ready())
         {
             fillBuffer();
         }
 
         return m_data.ready();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void close()
     {
     }
 
     /**
      * All HTML output stuff is here.  This class is a helper class, and will be spawned later on
      * with a proper API of its own so that we can have different kinds of renderers.
      */
 
     // FIXME: Not everything is yet, and in the future this class will be spawned
     //        out to be its own class.
     private class HTMLRenderer
             extends TextRenderer
     {
         /** DOCUMENT ME! */
         private boolean m_isPreBlock = false;
 
         /** DOCUMENT ME! */
         private TranslatorReader m_cleanTranslator;
 
         /*
           FIXME: It's relatively slow to create two TranslatorReaders each time.
         */
         public HTMLRenderer()
         {
         }
 
         /**
          * Does a lazy init.  Otherwise, we would get into a situation where HTMLRenderer would try
          * and boot a TranslatorReader before the TranslatorReader it is contained by is up.
          *
          * @return DOCUMENT ME!
          */
         private TranslatorReader getCleanTranslator()
         {
             if (m_cleanTranslator == null)
             {
                 WikiContext dummyContext = new WikiContext(m_engine, new WikiPage("_Dummy"));
                 m_cleanTranslator = new TranslatorReader(dummyContext, null, new TextRenderer());
                 m_cleanTranslator.m_allowHTML = true;
             }
 
             return m_cleanTranslator;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param buf DOCUMENT ME!
          * @param ch DOCUMENT ME!
          */
         public void doChar(StringBuffer buf, char ch)
         {
             if (ch == '<')
             {
                 buf.append("&lt;");
             }
             else if (ch == '>')
             {
                 buf.append("&gt;");
             }
             else if (ch == '&')
             {
                 buf.append("&amp;");
             }
             else
             {
                 buf.append(ch);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param style DOCUMENT ME!
          * @param clazz DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openDiv(String style, String clazz)
         {
             StringBuffer sb = new StringBuffer();
 
             sb.append("<div ");
 
             if (style != null)
             {
                 sb.append(" style=\"")
                         .append(style)
                         .append("\"");
             }
            else
            {
                sb.append(" class=\"");
                sb.append((clazz != null)
                        ? clazz : WikiConstants.CSS_WIKICONTENT);
                sb.append("\"");
            }
 
            sb.append(">");
             return sb.toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeDiv()
         {
             return "</div>";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openParagraph()
         {
             return new StringBuffer("<p class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
         }
                     
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeParagraph()
         {
             return "</p>\n";
         }
 
         /**
          * Writes out a text effect
          *
          * @param effect DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTextEffect(int effect)
         {
             switch (effect)
             {
             case BOLD:
                 return new StringBuffer("<b class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
                 
             case ITALIC:
                 return new StringBuffer("<i class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
 
             case TYPED:
                 return new StringBuffer("<tt class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
                 
             default:
                 return "";
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param effect DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTextEffect(int effect)
         {
             switch (effect)
             {
             case BOLD:
                 return "</b>";
 
             case ITALIC:
                 return "</i>";
 
             case TYPED:
                 return "</tt>";
 
             default:
                 return "";
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openDefinitionItem()
         {
             return new StringBuffer("<dd class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeDefinitionItem()
         {
             return "</dd>\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openDefinitionTitle()
         {
             return new StringBuffer("<dt class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeDefinitionTitle()
         {
             return "</dt>";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openDefinitionList()
         {
             return new StringBuffer("<dl class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">\n").toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeDefinitionList()
         {
             return "</dl>";
         }
 
         /**
          * Write a HTMLized link depending on its type.
          *
          * <p>
          * This jsut calls makeLink() with "section" set to null.
          * </p>
          *
          * @param type DOCUMENT ME!
          * @param link DOCUMENT ME!
          * @param text DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String makeLink(int type, String link, String text)
         {
             return makeLink(type, link, text, null);
         }
 
         private String getURL(String context, String link)
         {
             return m_context.getURL(context, link, null);
         }
 
         /**
          * Write a HTMLized link depending on its type.
          *
          * @param type Type of the link.
          * @param link The actual link.
          * @param text The user-visible text for the link.
          * @param section Which named anchor to point to.  This may not have any effect on certain
          *        link types.  If null, will ignore it.
          *
          * @return DOCUMENT ME!
          */
         public String makeLink(int type, String link, String text, String section)
         {
             String result;
 
             if (text == null)
             {
                 text = link;
             }
 
             section = (section != null)
                 ? ("#" + section)
                 : "";
 
             // Make sure we make a link name that can be accepted
             // as a valid URL.
             String encodedlink = m_engine.encodeName(link);
 
             if (StringUtils.isEmpty(encodedlink))
             {
                 type = EMPTY;
             }
 
             switch (type)
             {
             case READ:
                 result = new StringBuffer("<a class=\"")
                         .append(WikiConstants.CSS_LINK_WIKIPAGE)
                         .append("\" href=\"")
                         .append(getURL(WikiContext.VIEW, link))
                         .append(section)
                         .append("\">")
                         .append(text)
                         .append("</a>")
                         .toString();
 
                 break;
 
             case EDIT:
                 result = new StringBuffer("<u class=\"")
                         .append(WikiConstants.CSS_WIKICONTENT)
                         .append("\">")
                         .append(text)
                         .append("</u><a class=\"")
                         .append(WikiConstants.CSS_LINK_WIKIPAGE)
                         .append("\" href=\"")
                         .append(getURL(WikiContext.EDIT, link))
                         .append("\">?</a>")
                         .toString();
 
                 break;
 
             case EMPTY:
                 result = new StringBuffer("<u class=\"")
                         .append(WikiConstants.CSS_WIKICONTENT)
                         .append("\">")
                         .append(text)
                         .append("</u>")
                         .toString();
 
                 break;
 
             //
             //  These two are for local references - footnotes and
             //  references to footnotes.
             //  We embed the page name (or whatever WikiContext gives us)
             //  to make sure the links are unique across Wiki.
             //
             case LOCALREF:
                 result = new StringBuffer("<a class=\"")
                         .append(WikiConstants.CSS_LINK_FOOTNOTE_REF)
                         .append("\" href=\"#ref-")
                         .append(m_context.getPage().getName())
                         .append("-")
                         .append(link)
                         .append("\">[")
                         .append(text)
                         .append("]</a>")
                         .toString();
 
                 break;
 
             case LOCAL:
                 result = new StringBuffer("<a class=\"")
                         .append(WikiConstants.CSS_LINK_FOOTNOTE_ANCHOR)
                         .append("\" name=\"ref-")
                         .append(m_context.getPage().getName())
                         .append("-")
                         .append(link.substring(1))
                         .append("\">[")
                         .append(text)
                         .append("]</a>")
                         .toString();
 
                 break;
 
             //
             //  With the image, external and interwiki types we need to
             //  make sure nobody can put in Javascript or something else
             //  annoying into the links themselves.  We do this by preventing
             //  a haxor from stopping the link name short with quotes in
             //  fillBuffer().
             //
             case IMAGE:
                 result = new StringBuffer("<img class=\"")
                         .append(WikiConstants.CSS_IMG_INLINE)
                         .append("\" src=\"")
                         .append(link)
                         .append("\" alt=\"")
                         .append(text)
                         .append("\" />")
                         .toString();
 
                 break;
 
             case IMAGELINK:
                 result = new StringBuffer("<a class=\"")
                         .append(WikiConstants.CSS_LINK_WIKIPAGE)
                         .append("\" href=\"")
                         .append(text)
                         .append("\"><img class=\"")
                         .append(WikiConstants.CSS_IMG_INLINE)
                         .append("\" src=\"")
                         .append(link)
                         .append("\" alt=\"")
                         .append(text)
                         .append("\"/></a>")
                         .toString();
 
                 break;
 
             case IMAGEWIKILINK:
                 String pagelink = getURL(WikiContext.VIEW, text);
 
                 result = new StringBuffer("<a class=\"")
                         .append(WikiConstants.CSS_LINK_WIKIPAGE)
                         .append("\" href=\"")
                         .append(pagelink)
                         .append("\"><img class=\"")
                         .append(WikiConstants.CSS_IMG_INLINE)
                         .append("\" src=\"")
                         .append(link)
                         .append("\" alt=\"")
                         .append(text)
                         .append("\" /></a>")
                         .toString();
 
                 break;
 
             case EXTERNAL:
                 result = new StringBuffer("<a class=\"")
                         .append(WikiConstants.CSS_LINK_EXTERNAL)
                         .append("\" ")
                         .append((m_useRelNofollow
                                         ? "rel=\"nofollow\" "
                                         : ""))
                         .append("href=\"")
                         .append(link)
                         .append(section)
                         .append("\">")
                         .append(text)
                         .append("</a>")
                         .toString();
 
                 break;
 
             case INTERWIKI:
                 result = new StringBuffer("<a class=\"")
                         .append(WikiConstants.CSS_LINK_INTERWIKI)
                         .append("\" href=\"")
                         .append(link)
                         .append(section)
                         .append("\">")
                         .append(text)
                         .append("</a>")
                         .toString();
 
                 break;
 
             case ATTACHMENT:
 
                 String attlink = getURL(WikiContext.ATTACH, link);
                 String infolink = getURL(WikiContext.INFO, link);
                 String imglink = getURL(WikiContext.NONE, "images/attachment_small.png");
 
                 result = new StringBuffer("<a class=\"")
                         .append(WikiConstants.CSS_LINK_ATTACHMENT)
                         .append("\" href=\"")
                         .append(attlink)
                         .append("\">")
                         .append(text)
                         .append("</a>")
                         .append("<a class=\"")
                         .append(WikiConstants.CSS_LINK_WIKIPAGE)
                         .append("\" href=\"")
                         .append(infolink)
                         .append("\"><img src=\"")
                         .append(imglink)
                         .append("\" border=\"0\" alt=\"(info)\"/></a>")
                         .toString();
 
                 break;
 
             default:
                 result = "";
                 
                 break;
             }
             
             return result;
         }
 
         /**
          * Writes HTML for error message.
          *
          * @param error DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String makeError(String error)
         {
             return new StringBuffer("<span class=\"")
                     .append(WikiConstants.CSS_CLASS_ERROR)
                     .append("\">")
                     .append(error)
                     .append("</span>")
                     .toString();
         }
 
         /**
          * Emits a vertical line.
          *
          * @return DOCUMENT ME!
          */
         public String makeRuler()
         {
             return new StringBuffer("<hr class=\"")
                     .append(WikiConstants.CSS_WIKICONTENT)
                     .append("\" />")
                     .toString();
         }
 
         /**
          * Modifies the "hd" parameter to contain proper values.
          *
          * @param baseName DOCUMENT ME!
          * @param title DOCUMENT ME!
          * @param hd DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         private String makeHeadingAnchor(String baseName, String title, Heading hd)
         {
             hd.setTitleText(title);
             title = cleanLink(title);
             hd.setTitleSection(m_engine.encodeName(title));
 
             StringBuffer anchor = new StringBuffer("section-")
                     .append(m_engine.encodeName(baseName))
                     .append("-")
                     .append(hd.getTitleSection());
 
 
             hd.setTitleAnchor(anchor.toString());
 
             return new StringBuffer("<a class=\"")
                     .append(WikiConstants.CSS_ANCHOR)
                     .append("\" name=\"")
                     .append(hd.getTitleAnchor())
                     .append("\">")
                     .toString();
         }
 
         /**
          * Returns XHTML for the start of the heading.  Also sets the line-end emitter.
          *
          * @param level
          * @param title A List to which heading should be added.
          * @param hd DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          *
          * @throws InternalWikiException DOCUMENT ME!
          */
         public String makeHeading(int level, String title, Heading hd)
         {
             String res = "";
 
             String pageName = m_context.getPage().getName();
 
             title = title.trim();
 
             StringWriter outTitle = new StringWriter();
 
             try
             {
                 TranslatorReader read = getCleanTranslator();
                 read.setInputReader(new StringReader(title));
                 FileUtil.copyContents(read, outTitle);
             }
             catch (IOException e)
             {
                 log.fatal("CleanTranslator not working", e);
                 throw new InternalWikiException(
                     "CleanTranslator not working as expected, when cleaning title" + e.getMessage());
             }
 
             hd.setLevel(level);
 
             switch (level)
             {
             case Heading.HEADING_SMALL:
                 res = new StringBuffer("<h4 class=\"")
                         .append(WikiConstants.CSS_WIKICONTENT)
                         .append("\">")
                         .append(makeHeadingAnchor(pageName, outTitle.toString(), hd))
                         .toString();
 
                 m_closeTag = "</a></h4>";
 
                 break;
 
             case Heading.HEADING_MEDIUM:
                 res = new StringBuffer("<h3 class=\"")
                         .append(WikiConstants.CSS_WIKICONTENT)
                         .append("\">")
                         .append(makeHeadingAnchor(pageName, outTitle.toString(), hd))
                         .toString();
                 
                 m_closeTag = "</a></h3>";
                 
                 break;
 
             case Heading.HEADING_LARGE:
                 res = new StringBuffer("<h2 class=\"")
                         .append(WikiConstants.CSS_WIKICONTENT)
                         .append("\">")
                         .append(makeHeadingAnchor(pageName, outTitle.toString(), hd))
                         .toString();
                 
                 m_closeTag = "</a></h2>";
                 
                 break;
                 
             default:
                 break;
             }
             
             return res;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param bullet A character detailing which kind of a list we are dealing with here.
          *        Options are '#' and ''.
          *
          * @return DOCUMENT ME!
          */
         public String openList(char bullet)
         {
             String res = "";
 
             if (bullet == '#')
             {
                 res = new StringBuffer("<ol class=\"")
                         .append(WikiConstants.CSS_WIKICONTENT)
                         .append("\">\n")
                         .toString();
             }
             else if (bullet == '*')
             {
                 res = new StringBuffer("<ul class=\"")
                         .append(WikiConstants.CSS_WIKICONTENT)
                         .append("\">\n")
                         .toString();
             }
             else
             {
                 if (log.isInfoEnabled())
                 {
                     log.info("Warning: unknown bullet character '" + bullet + "' at (+)");
                 }
             }
 
             return res;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openListItem()
         {
             return new StringBuffer("<li class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeListItem()
         {
             return "</li>\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param bullet A character detailing which kind of a list we are dealing with here.
          *        Options are '#' and ''.
          *
          * @return DOCUMENT ME!
          */
         public String closeList(char bullet)
         {
             String res = "";
 
             if (bullet == '#')
             {
                 res = "</ol>\n";
             }
             else if (bullet == '*')
             {
                 res = "</ul>\n";
             }
             else
             {
                 //FIXME unknown character -> error
                 if (log.isInfoEnabled())
                 {
                     log.info("Warning: unknown character in unwind '" + bullet + "'");
                 }
             }
 
             return res;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTable()
         {
             return new StringBuffer("<table class=\"")
             .append(WikiConstants.CSS_WIKICONTENT)
             .append("\">\n")
             .toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTable()
         {
             return "</table>\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTableRow()
         {
             return new StringBuffer("<tr class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTableRow()
         {
             return "</tr>";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTableItem()
         {
             return new StringBuffer("<td class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTableItem()
         {
             return "</td>";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTableHeading()
         {
             return new StringBuffer("<th class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTableHeading()
         {
             return "</th>";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param isBlock DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openPreformatted(boolean isBlock)
         {
             m_isPreBlock = isBlock;
 
             if (isBlock)
             {
                 return new StringBuffer("<pre class=\"").append(WikiConstants.CSS_WIKICONTENT).append("\">").toString();
             }
 
             return "<span style=\"font-family:monospace; whitespace:pre;\">";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closePreformatted()
         {
             if (m_isPreBlock)
             {
                 return "</pre>\n";
             }
 
             return "</span>";
         }
 
         /**
          * If outlink images are turned on, returns a link to the outward linking image.
          *
          * @return DOCUMENT ME!
          */
         public String outlinkImage()
         {
             if (m_useOutlinkImage)
             {
                 return new StringBuffer("<img class=\"")
                         .append(WikiConstants.CSS_IMG_OUTLINK)
                         .append("\" src=\"")
                         .append(getURL(WikiContext.NONE, "images/out.png"))
                         .append("\" alt=\"\" />")
                         .toString();
             }
 
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param clear If true, then flushes all thingies.
          *
          * @return DOCUMENT ME!
          */
         public String lineBreak(boolean clear)
         {
             if (clear)
             {
                 return "<br clear=\"all\" />";
             }
 
             return "<br />";
         }
     } // HTMLRenderer
 
     /**
      * A very simple class for outputting plain text with no formatting.
      */
     private class TextRenderer
     {
         /**
          * DOCUMENT ME!
          *
          * @param buf DOCUMENT ME!
          * @param ch DOCUMENT ME!
          */
         public void doChar(StringBuffer buf, char ch)
         {
             buf.append(ch);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param style DOCUMENT ME!
          * @param clazz DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openDiv(String style, String clazz)
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeDiv()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openParagraph()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeParagraph()
         {
             return "\n\n";
         }
 
         /**
          * Writes out a text effect
          *
          * @param effect DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTextEffect(int effect)
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param effect DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTextEffect(int effect)
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openDefinitionItem()
         {
             return " : ";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeDefinitionItem()
         {
             return "\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openDefinitionTitle()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeDefinitionTitle()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openDefinitionList()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeDefinitionList()
         {
             return "\n";
         }
 
         /**
          * Write a HTMLized link depending on its type.
          *
          * <p>
          * This jsut calls makeLink() with "section" set to null.
          * </p>
          *
          * @param type DOCUMENT ME!
          * @param link DOCUMENT ME!
          * @param text DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String makeLink(int type, String link, String text)
         {
             return text;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param type DOCUMENT ME!
          * @param link DOCUMENT ME!
          * @param text DOCUMENT ME!
          * @param section DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String makeLink(int type, String link, String text, String section)
         {
             return text;
         }
 
         /**
          * Writes HTML for error message.
          *
          * @param error DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String makeError(String error)
         {
             return "ERROR: " + error;
         }
 
         /**
          * Emits a vertical line.
          *
          * @return DOCUMENT ME!
          */
         public String makeRuler()
         {
             return "----------------------------------";
         }
 
         /**
          * Returns XHTML for the start of the heading.  Also sets the line-end emitter.
          *
          * @param level
          * @param title DOCUMENT ME!
          * @param hd DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String makeHeading(int level, String title, Heading hd)
         {
             String res = "";
             title = title.trim();
 
             hd.setLevel(level);
             hd.setTitleText(title);
             hd.setTitleSection("");
             hd.setTitleAnchor("");
 
             switch (level)
             {
             case Heading.HEADING_SMALL:
                 res = title;
                 m_closeTag = "\n\n";
 
                 break;
 
             case Heading.HEADING_MEDIUM:
                 res = title;
                 m_closeTag =  new StringBuffer("\n")
                         .append(StringUtils.repeat("-", title.length()))
                         .append("\n\n")
                         .toString();
 
                 break;
 
             case Heading.HEADING_LARGE:
                 res = title.toUpperCase();
 
                 m_closeTag = new StringBuffer("\n")
                         .append(StringUtils.repeat("=", title.length()))
                         .append("\n\n")
                         .toString();
 
                 break;
                 
             default:
                 break;
 
             }
            
             return res;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param bullet A character detailing which kind of a list we are dealing with here.
          *        Options are '#' and ''.
          *
          * @return DOCUMENT ME!
          */
 
         // FIXME: Should really start a different kind of list depending
         //        on the bullet type
         public String openList(char bullet)
         {
             return "\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openListItem()
         {
             return "- ";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeListItem()
         {
             return "\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param bullet A character detailing which kind of a list we are dealing with here.
          *        Options are '#' and ''.
          *
          * @return DOCUMENT ME!
          */
         public String closeList(char bullet)
         {
             return "\n\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTable()
         {
             return "\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTable()
         {
             return "\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTableRow()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTableRow()
         {
             return "\n";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTableItem()
         {
             return "\t";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTableItem()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openTableHeading()
         {
             return "\t";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closeTableHeading()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param isBlock DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String openPreformatted(boolean isBlock)
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String closePreformatted()
         {
             return "\n";
         }
 
         /**
          * If outlink images are turned on, returns a link to the outward linking image.
          *
          * @return DOCUMENT ME!
          */
         public String outlinkImage()
         {
             return "";
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param clear If true, then flushes all thingies.
          *
          * @return DOCUMENT ME!
          */
         public String lineBreak(boolean clear)
         {
             return "\n";
         }
     } // TextRenderer
 
     /**
      * This class is used to store the headings in a manner which allow the building of a Table Of
      * Contents.
      */
     public static class Heading
     {
         /** DOCUMENT ME! */
         public static final int HEADING_SMALL = 1;
 
         /** DOCUMENT ME! */
         public static final int HEADING_MEDIUM = 2;
 
         /** DOCUMENT ME! */
         public static final int HEADING_LARGE = 3;
 
         /** DOCUMENT ME! */
         private int level;
 
         /** DOCUMENT ME! */
         private String titleText;
 
         /** DOCUMENT ME! */
         private String titleAnchor;
 
         /** DOCUMENT ME! */
         private String titleSection;
 
         public int getLevel()
         {
             return level;
         }
 
         public void setLevel(int level)
         {
             this.level = level;
         }
 
         public String getTitleAnchor()
         {
             return titleAnchor;
         }
 
         public void setTitleAnchor(String titleAnchor)
         {
             this.titleAnchor = titleAnchor;
         }
 
         public String getTitleSection()
         {
             return titleSection;
         }
 
         public void setTitleSection(String titleSection)
         {
             this.titleSection = titleSection;
         }
         public String getTitleText()
         {
             return titleText;
         }
 
         public void setTitleText(String titleText)
         {
             this.titleText = titleText;
         }
     }
 }
