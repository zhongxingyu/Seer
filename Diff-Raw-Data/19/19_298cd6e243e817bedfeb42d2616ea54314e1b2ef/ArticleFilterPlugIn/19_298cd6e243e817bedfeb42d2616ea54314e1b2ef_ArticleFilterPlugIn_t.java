 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms are permitted provided
   that: (1) source distributions retain this entire copyright notice and
   comment; and (2) modifications made to the software are prominently
   mentioned, and a copy of the original software (or a pointer to its
   location) are included. The name of the author may not be used to endorse
   or promote products derived from this software without specific prior
   written permission.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
   Effectively, this means you can do what you want with the software except
   remove this notice or take advantage of the author's name. If you modify
   the software and redistribute your modified version, you must indicate that
   your version is a modification of the original, and you must provide either
   a pointer to or a copy of the original.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.plugins;
 
 import org.clapper.curn.Constants;
 import org.clapper.curn.Curn;
 import org.clapper.curn.CurnConfig;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.FeedConfigItemPlugIn;
 import org.clapper.curn.MainConfigItemPlugIn;
 import org.clapper.curn.PostFeedParsePlugIn;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.text.HTMLUtil;
 import org.clapper.util.text.TextUtil;
 import org.clapper.util.logging.Logger;
 
 import java.io.IOException;
 import java.io.StringReader;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 /**
  * The <tt>ArticleFilterPlugIn</tt> provides per-feed filtering capabilities.
  * It can be used to filter out items (articles) that match one or more regular
 * expressions. 
  *
  * <p>The filtering syntax is (shamelessly) adapted from the
  * <a href="http://offog.org/code/rawdog.html" target="_top"><i>rawdog</i></a>
  * RSS reader's
  * <a href="http://offog.org/files/rawdog-plugins/article-filter.py" target="_top"><i>article-filter</i></a>
  * plug-in. (<i>rawdog</i> is similar to <i>curn</i>: It's
  * a command line-driven RSS reader, written in Python.) A feed filter is
 * configured by adding an <tt>ArticleFilter</tt> property to the
  * feed's configuration section. The property's value consists of one or
  * more filter command sequences, separated by ";" characters. (The ";" must
  * be surrounded by white space; see below.) Each filter command sequence
  * is of this form:</p>
  *
  *
  * <blockquote>show|hide [<i>field</i> '<i>regexp</i>' [<i>field</i> '<i>regexp</i>' ...]]</blockquote>
  *
  * <p><i>field</i> can be one of:</p>
  *
  * <ul>
  *   <li> author: search the author field
  *   <li> title: search the title field
  *   <li> summary: search the summary, or description, field
  *   <li> text: search the full content, if available
  *   <li> category: search the article's category (or categories)
  *   <li> any: search all fields
  * </ul>
  *
  * <p>Each regular expression <i>must</i> be enclosed in single quotes.</p>
  *
  * <p>For example:</p>
  *
  * <blockquote><pre>
  * hide author 'Raymond Luxury-yacht' ; show author 'Arthur .Two-sheds. Jackson'
  * </pre></blockquote>
  *
  * <p>If the command is "hide", then the entry will be hidden if the
  * specified field matches the regular expression. If the command is
  * "show", then the entry will be shown if the field matches the regular
  * expression. If there are no fields or regular expressions, then the
  * command is a wildcard match. That is:</p>
  *
  * <blockquote><pre>hide</pre></blockquote>
  *
  * <p>is equivalent to:</p>
  *
  * <blockquote><pre>hide any '.*'</pre></blockquote>
  *
  * <p>and:</p>
  *
  * <blockquote><pre>show</pre></blockquote>
  *
  * <p>is equivalent to:</p>
  *
  * <blockquote><pre>show any '.*'</pre></blockquote>
  *
  * <p>Wildcard matches are useful in situations where you want to
  * hide or show "everything but ...". See the examples, below, for
  * details.</p>
  *
  * <p>All filtering commands are processed, and the end result is what defines
  * whether a given entry is suppressed or not. Regular expressions are
 * matched in a case-blind fashion. The match logic also
 *
 * <ul>
 *   <li>ignores any embedded newlines in article contents
 *   <li>(temporarily) strips all HTML from the article text before matching
 * </ul>
  *
  * <p>You can use multiple <tt>ArticleFilter</tt> parameters per feed (as long
  * as they have unique suffixes. All filters are applied to each article to
  * determine whether the article should be filtered out or not.</p>
  *
  * <h3>Examples</h3>
  *
  * <p>Some examples will help clarify the syntax.</p>
  *
  * <p>For example, the following set of commands hide all articles with the
  * phrase "mash-up" (because mash-ups bore me):</p>
  *
  * <blockquote>
  * <pre>
  * ArticleFilter: hide any 'mash[- \t]?up'
  * </pre>
  * </blockquote>
  *
  * <p>The following, more complicated, entry hides everything by author
  * "Joe Blow", unless the title has the word "rant" in it ('cause his rants
  * are hilarious):</p>
  * 
  * <blockquote>
  * <pre>
  * ArticleFilter: hide author '^joe *blow$' ; show author '^joe *blow$' title rant
  * </pre>
  * </blockquote>
  *
  * <p>Finally, this example hides everything except articles by Moe Howard:</p>
  *
  * <blockquote>
  * <pre>
  * ArticleFilter: hide ; show author '^moe *howard$'
  * </pre>
  * </blockquote>
  *
  * @version <tt>$Revision$</tt>
  */
 public class ArticleFilterPlugIn
     implements FeedConfigItemPlugIn,
                PostFeedParsePlugIn
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String VAR_ITEM_FILTER   = "ArticleFilter";
     private static final char   COMMAND_DELIM     = ';';
     private static final String STR_COMMAND_DELIM = "" + COMMAND_DELIM;
 
     private static Map<String,Field> FIELD_NAME_MAP
         = new HashMap<String,Field>();
     static
     {
         for (Field field : Field.values())
             FIELD_NAME_MAP.put (field.toString().toLowerCase(), field);
     }
 
     private static Map<String,Command> COMMAND_MAP
         = new HashMap<String,Command>();
     static
     {
         for (Command command : Command.values())
             COMMAND_MAP.put (command.toString().toLowerCase(), command);
     }
 
     /*----------------------------------------------------------------------*\
                               Private Classes
     \*----------------------------------------------------------------------*/
 
     private static enum Command {HIDE, SHOW}
     private static enum Field   {AUTHOR, TITLE, SUMMARY, TEXT, CATEGORY, ANY}
 
     private class FieldMatchRule
     {
         private Collection<Field> fields = new HashSet<Field>();
         private Pattern regex;
 
         FieldMatchRule()
         {
         }
 
         public FieldMatchRule addField (Field field)
         {
             fields.add (field);
             return this;
         }
 
         public Collection<Field> getFields()
         {
             return fields;
         }
 
         public Pattern getRegex()
         {
             return regex;
         }
 
         public void setRegex (Pattern regex)
         {
             this.regex = regex;
         }
 
         public String toString()
         {
             StringBuilder buf = new StringBuilder();
             String sep = "<";
             for (Field field : fields)
             {
                 buf.append (sep);
                 buf.append (field);
                 sep = ",";
             }
 
             buf.append ("> '");
             buf.append (regex.toString());
             buf.append ("'");
 
             return buf.toString();
         }
     }
 
     private class MatchRule
     {
         private Command command;
         private String unparsedFilter;
         private Collection<FieldMatchRule> matchRules =
             new ArrayList<FieldMatchRule>();
 
         MatchRule (Command command, String unparsedFilter)
         {
             this.command = command;
             this.unparsedFilter = unparsedFilter;
         }
 
         private void addFieldRule (FieldMatchRule rule)
         {
             matchRules.add (rule);
         }
 
         private Collection<FieldMatchRule> getFieldRules()
         {
             return matchRules;
         }
 
         private Command getCommand()
         {
             return command;
         }
 
         public String toString()
         {
             StringBuilder buf = new StringBuilder();
 
             buf.append (command);
             for (FieldMatchRule fieldRule : matchRules)
             {
                 buf.append (" ");
                 buf.append (fieldRule.toString());
             }
 
             return buf.toString();
         }
     }
 
     private class FeedFilterRules implements Iterable<MatchRule>
     {
         private Collection<MatchRule> filterRules = new ArrayList<MatchRule>();
 
         public void add (MatchRule rule)
         {
             filterRules.add (rule);
         }
 
         public String toString()
         {
             return filterRules.toString();
         }
 
         public Iterator<MatchRule> iterator()
         {
             return filterRules.iterator();
         }
     }
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * For log messages
      */
     private static Logger log = new Logger (ArticleFilterPlugIn.class);
 
     /**
      * Per-feed match rules
      */
     private Map<FeedInfo,FeedFilterRules> perFeedMatchRules =
         new HashMap<FeedInfo,FeedFilterRules>();
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Default constructor (required).
      */
     public ArticleFilterPlugIn()
     {
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get a displayable name for the plug-in.
      *
      * @return the name
      */
     public String getName()
     {
         return "Article Filter";
     }
 
     /*----------------------------------------------------------------------*\
                Public Methods Required by *PlugIn Interfaces
     \*----------------------------------------------------------------------*/
 
     /**
      * Called immediately after <i>curn</i> has read and processed a
      * configuration item in a "feed" configuration section. All
      * configuration items are passed, one by one, to each loaded plug-in.
      * If a plug-in class is not interested in a particular configuration
      * item, this method should simply return without doing anything. Note
      * that some configuration items may simply be variable assignment;
      * there's no real way to distinguish a variable assignment from a
      * blessed configuration item.
      *
      * @param sectionName  the name of the configuration section where
      *                     the item was found
      * @param paramName    the name of the parameter
      * @param config       the active configuration
      * @param feedInfo     partially complete <tt>FeedInfo</tt> object
      *                     for the feed. The URL is guaranteed to be
      *                     present, but no other fields are.
      * 
      * @return <tt>true</tt> to continue processing the feed,
      *         <tt>false</tt> to skip it
      *
      * @throws CurnException on error
      *
      * @see CurnConfig
      * @see FeedInfo
      * @see FeedInfo#getURL
      */
     public boolean runFeedConfigItemPlugIn (String     sectionName,
                                             String     paramName,
                                             CurnConfig config,
                                             FeedInfo   feedInfo)
 	throws CurnException
     {
         try
         {
             if (paramName.equals (VAR_ITEM_FILTER))
             {
                 String rawValue = config.getRawValue (sectionName, paramName);
                 perFeedMatchRules.put (feedInfo, 
                                        parseFilterSpec (sectionName,
                                                         paramName,
                                                         rawValue));
             }
 
             return true;
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
     }
 
     /**
      * Called immediately after a feed is parsed, but before it is
      * otherwise processed. This method can return <tt>false</tt> to signal
      * <i>curn</i> that the feed should be skipped. For instance, a plug-in
      * that filters on the parsed feed data could use this method to weed
      * out non-matching feeds before they are downloaded. Similarly, a
      * plug-in that edits the parsed data (removing or editing individual
      * items, for instance) could use method to do so.
      *
      * @param feedInfo  the {@link FeedInfo} object for the feed that
      *                  has been downloaded and parsed.
      * @param channel   the parsed channel data
      *
      * @return <tt>true</tt> if <i>curn</i> should continue to process the
      *         feed, <tt>false</tt> to skip the feed. A return value of
      *         <tt>false</tt> aborts all further processing on the feed.
      *         In particular, <i>curn</i> will not pass the feed along to
      *         other plug-ins that have yet to be notified of this event.
      *
      * @throws CurnException on error
      *
      * @see RSSChannel
      * @see FeedInfo
      */
     public boolean runPostFeedParsePlugIn (FeedInfo   feedInfo,
                                            RSSChannel channel)
 	throws CurnException
     {
         FeedFilterRules rules = perFeedMatchRules.get (feedInfo);
         if (rules != null)
         {
             for (RSSItem item : channel.getItems())
             {
                 if (nukeItem (item, rules, feedInfo))
                 {
                     // Since getItems() returns a copy of the list of
                     // items, this call will not cause a
                     // ConcurrentModificationException to be thrown.
 
                     log.debug ("Feed \""
                              + feedInfo.getURL()
                              + "\": Filtering out item \""
                              + item.getTitle()
                              + "\"");
                     channel.removeItem (item);
                 }
             }
         }
 
         return true;
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     private FeedFilterRules parseFilterSpec (String sectionName,
                                              String paramName,
                                              String rawValue)
         throws CurnException
     {
         FeedFilterRules result = new FeedFilterRules();
         String[] tokens = parseFilterTokens (rawValue);
 
         try
         {
             int i = 0;
             while (i < tokens.length)
             {
                 // Strip off the first token, which should be the command.
 
                 String sCommand = tokens[i++];
                 Command cmd = COMMAND_MAP.get (sCommand);
                 if (cmd == null)
                 {
                     throw new CurnException
                         (Constants.BUNDLE_NAME,
                          "ArticleFilterPlugIn.badFilterCommand",
                          "Configuration section \"{0}\": Value for parameter "
                        + "\"{1}\" has a bad command field of \"{2}\".",
                          new Object[] {sectionName, paramName, sCommand});
                 }
 
                 MatchRule matchRule = new MatchRule (cmd, rawValue);
 
                 // Check for a wildcard field.
 
                 if (tokens[i].equals (STR_COMMAND_DELIM))
                 {
                     // Wild card rule.
 
                     FieldMatchRule fieldRule = new FieldMatchRule();
                     fieldRule.addField (Field.AUTHOR)
                              .addField (Field.TITLE)
                              .addField (Field.SUMMARY)
                              .addField (Field.TEXT)
                              .addField (Field.CATEGORY)
                              .setRegex (compileRegex (".*"));
                     i++;
                     matchRule.addFieldRule (fieldRule);
                 }
 
                 else
                 {
                     // No wildcard. Strip off the fields and regular
                     // expressions, until we hit the next ";" or an end of
                     // line.
 
                     while ((i < tokens.length) &&
                            (! tokens[i].equals (STR_COMMAND_DELIM)))
                     {
                         // Strip off field name.
 
                         String strField = tokens[i++];
                         String uncompiledRegex = tokens[i++];
                         FieldMatchRule fieldRule = new FieldMatchRule();
                         Field field = FIELD_NAME_MAP.get (strField);
                         if (field == null)
                         {
                             throw new CurnException
                                 (Constants.BUNDLE_NAME,
                                  "ArticleFilterPlugIn.badRSSField",
                                  "Configuration section \"{0}\": Value for "
                                + "parameter \"{1}\" has a bad RSS field of "
                                + "\"{2}\".",
                                  new Object[]
                                  {
                                      sectionName,
                                      paramName,
                                      strField
                                  });
                         }
 
                         if (field == Field.ANY)
                         {
                             fieldRule.addField (Field.AUTHOR)
                                      .addField (Field.TITLE)
                                      .addField (Field.SUMMARY)
                                      .addField (Field.TEXT)
                                      .addField (Field.CATEGORY);
                         }
 
                         else
                         {
                             fieldRule.addField (field);
                         }
 
                         fieldRule.setRegex (compileRegex (uncompiledRegex));
                         matchRule.addFieldRule (fieldRule);
                     }
 
                     i++;
                 }
 
                 result.add (matchRule);
             }
         }
 
         catch (ArrayIndexOutOfBoundsException ex)
         {
             throw new CurnException
                 (Constants.BUNDLE_NAME,
                  "ArticleFilterPlugIn.wrongNumberOfFields",
                  "Configuration section \"{0}\": Value for parameter "
                + "\"{1}\" is missing at least one field.",
                  new Object[] {sectionName, paramName});
         }
 
         return result;
     }
 
     private boolean nukeItem (RSSItem         item,
                               FeedFilterRules rules,
                               FeedInfo        feedInfo)
     {
         boolean killItem = false;
         String  itemId = feedInfo.getURL()
                        + ", "
                        + item.getTitle();
 
         log.debug ("item "
                  + itemId
                  + ": checking filter: "
                  + rules.toString());
         for (MatchRule rule : rules)
         {
             boolean match = true;
             boolean hide = (rule.getCommand() == Command.HIDE);
 
             for (FieldMatchRule fieldRule : rule.getFieldRules())
             {
                 log.debug ("item="
                          + item.getTitle()
                          + ", command="
                          + rule.getCommand()
                          + ", "
                          + fieldRule.toString());
 
                 StringBuilder buf = new StringBuilder();
 
                 for (Field field : fieldRule.getFields())
                 {
                     switch (field)
                     {
                         case AUTHOR:
                             Collection<String> authors = item.getAuthors();
                             if ((authors != null) && (authors.size() > 0))
                                 buf.append (TextUtil.join (authors, " "));
                             break;
 
                         case CATEGORY:
                             Collection<String> cats = item.getCategories();
                             if ((cats != null) && (cats.size() > 0))
                                 buf.append (TextUtil.join (cats, " "));
                             break;
 
                         case TITLE:
                             buf.append (item.getTitle());
                             break;
 
                         case SUMMARY:
                             buf.append (item.getSummary());
                             break;
 
                         case TEXT:
                             buf.append
                                 (item.getFirstContentOfType ("text/plain",
                                                              "text/html"));
                             break;
 
                         default:
                             assert (false);
                             break;
                     }
                 }
 
                 String toMatch = HTMLUtil.textFromHTML (buf.toString());
                 Pattern regex = fieldRule.getRegex();
                 Matcher matcher = regex.matcher (toMatch);
                 boolean regexMatches = matcher.find();
                 log.debug ("regex '"
                          + regex.toString()
                          + "' "
                          + (regexMatches ? "matches: " : "doesn't match: ")
                          + toMatch);
                 if (! regexMatches)
                     match = false;
             }
 
             if (match)
                 killItem = hide;
         }
 
         log.debug ("item: " + itemId + ", kill=" + killItem);
         return killItem;
     }
 
     private String[] parseFilterTokens (String rawValue)
         throws CurnException
     {
         ArrayList<String> tokens = new ArrayList<String>();
 
         StringReader r = new StringReader (rawValue);
         StringBuilder buf = new StringBuilder();
         int col = 0;
         try
         {
             int ich;
             while ((ich = r.read()) != -1)
             {
                 col++;
 
                 char ch = (char) ich;
                 if (ch == '\'')
                 {
                     // Look for ending quote.
 
                     buf.setLength (0);
                     while ( ((ich = r.read()) != -1) &&
                             (((char) ich) != '\'') )
                     {
                         col++;
                         buf.append ((char) ich);
                     }
 
                     if (ich == -1)
                     {
                         throw new CurnException
                             (Constants.BUNDLE_NAME,
                              "ArticleFilterPlugIn.unmatchedQuote",
                              "Unmatched single quote at column {0} in \"{1}\"",
                              new Object[] {col, rawValue});
                     }
 
                     tokens.add (buf.toString());
                 }
 
                 else if (ch == COMMAND_DELIM)
                 {
                     tokens.add (String.valueOf (ch));
                 }
 
                 else if (Character.isWhitespace (ch))
                 {
                 }
 
                 else
                 {
                     // Keep going until we hit white space or the end of
                     // the line.
 
                     buf.setLength (0);
                     buf.append (ch);
                     while ( ((ich = r.read()) != -1) &&
                             (((char) ich) != '\'') &&
                             (! Character.isWhitespace ((char) ich)) )
                     {
                         col++;
                         buf.append ((char) ich);
                     }
 
                     tokens.add (buf.toString());
                 }
             }
         }
 
         catch (IOException ex)
         {
             // Shouldn't happen
 
             throw new CurnException ("Huh? IOException reading StringReader.",
                                      ex);
         }
 
         return tokens.toArray (new String[tokens.size()]);
     }
 
     private Pattern compileRegex (String strRegex)
         throws CurnException
     {
         try
         {
             return Pattern.compile (strRegex, Pattern.CASE_INSENSITIVE);
         }
 
         catch (PatternSyntaxException ex)
         {
             throw new CurnException
                 (Constants.BUNDLE_NAME, "ArticleFilterPlugIn.badRegex",
                  "\"{0}\" is an invalid regular expression",
                  new Object[] {strRegex},
                  ex);
         }
     }
 }
