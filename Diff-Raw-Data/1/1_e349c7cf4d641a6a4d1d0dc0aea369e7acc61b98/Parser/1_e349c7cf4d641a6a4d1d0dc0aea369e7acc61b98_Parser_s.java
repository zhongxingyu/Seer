 package edu.rit.se.antipattern.htmleditor.models;
 
 import java.util.ArrayList;
 import java.util.regex.*;
 
 /*
  * A parser for well-formed HTML.
  * 
  * @author Team ReichMafia
  */
 public class Parser {
     
     private static final String NO_ELEMENTS_REMAIN = "";
     
     /**
      * Parses a string of HTML into a tree of HTML Elements.
      * 
      * @param html String of HTML to be parsed
      * @return The root Element of the tree
      * @throws ParseException 
      */
     public static Element parseDocument( String html ) throws ParseException {
         Element[] parsed = parse(html);
         Matcher m = (Pattern.compile("(?i)html")).matcher(parsed[0].getName());
         if ( parsed.length > 1 || !m.find() ) {
             throw new ParseException("Document not completely enclosed in <html> tag");
         } else {
             return parsed[0];
         }
     }
     
     // parse a line of html into an array of separate, non-overlapping elements
     protected static Element[] parse( String html ) throws ParseException {
         html = stripSpecialChars(html);
         ArrayList<Element> elements = new ArrayList<Element>();
         for ( String htmlElement : getElementStrings(html) ) {
             elements.add( parseSingle(htmlElement) );
         }
         
         Element[] elementsArray = new Element[elements.size()];
         elements.toArray(elementsArray);
         return elementsArray;
     }
     
     protected static String stripSpecialChars( String html ) {
         String specialCharsRegex = "(\\n|\\r)*";
         return html.replaceAll( specialCharsRegex, "" );
     }
     
     // get the strings representing separate, non-overlapping elements in html
     protected static ArrayList<String> getElementStrings( String html ) throws ParseException {
         ArrayList<String> elementStrings = new ArrayList<String>();
         String nextElement = getFirstElementString(html);
         while( !nextElement.equals( NO_ELEMENTS_REMAIN ) ) {
             html = html.replaceFirst(nextElement, "");
             elementStrings.add( nextElement );
             nextElement = getFirstElementString(html);
         }
         return elementStrings;
     }
     
     // get the string of the first HTML element in the string
     // this is everything from the first opening tag to its corresponding closing tag
     // e.g. getFirstElementString("<b><em>asdf</em>text</b><p></p>") will return
     // "<b><em>asdf</em>text</b>"
     protected static String getFirstElementString( String html ) throws ParseException {
         if ( html.equals( NO_ELEMENTS_REMAIN ) ) return html;
         if ( !(html.charAt(0) == '<') ) return getLeadingText(html);
         String tagName = getTagName(html);
         int tagCount = 1;
         Pattern open = getOpeningTagRegex(tagName);
         Pattern close = getClosingTagRegex(tagName);
         Matcher openMatcher = open.matcher(html);
         Matcher closeMatcher = close.matcher(html);
         openMatcher.find();
         
         while( tagCount > 0 ) {
             if ( openMatcher.find() ) {
                 tagCount++;
                 closeMatcher.region(openMatcher.end(), html.length());
             } else if ( closeMatcher.find() ) {
                 tagCount--;
             } else {
                 String errmsg = "Unmatched tags in: %s";
                 String formattedErrmsg = String.format(errmsg, html);
                 throw new ParseException(formattedErrmsg);
             }
         }
         
         return html.substring( 0, closeMatcher.end() );
     }
     
     // get any text between the beginning of the html string and whichever comes
     // first: a tag or the end of the string
     protected static String getLeadingText( String html ) {
         int endIndex = html.indexOf("<");
         endIndex = endIndex == -1 ? html.length() : endIndex;
         return html.substring(0, endIndex);
     }
     
     // get a Pattern object with regex matching opening tags for given tag name
     protected static Pattern getOpeningTagRegex( String name ) {
         String tagRegex = String.format("<(\\s)*%s(.)*>", name);
         return Pattern.compile(tagRegex);
     }
     
     // get a Pattern object with regex matching closing tags for given tag name
     protected static Pattern getClosingTagRegex( String name ) {
         String tagRegex = String.format("<(\\s)*/%s(\\s)*>", name);
         return Pattern.compile(tagRegex);
     }
     
     // parse the string of a single html element and its contents in the correct element
     protected static Element parseSingle( String html ) throws ParseException {
         if ( isJustText(html) ) {
             return new TextElement(html);
         }
         String name = getTagName(html);
         return new TagElement( name, parse(getTagContentString(html)) );
     }
     
     // get the name of the outter-most html element in the given string
     // A string with multiple, non-overlapping elements will return the name of
     // the first tag (e.g. getTagName("<b></b><em></em>") will return "b")
     protected static String getTagName( String html ) throws ParseException {
         int firstRightAngledBracket = html.indexOf(">");
         if ( firstRightAngledBracket == -1 ) {
             String errmsg = "Unmatched opening angled bracket in: %s";
             String formattedErrmsg = String.format(errmsg, html);
             throw new ParseException(formattedErrmsg);
         }
         String[] splitOpeningTag = html.substring(1, firstRightAngledBracket).split(" ");
         return splitOpeningTag[0].equals("") ? splitOpeningTag[1] : splitOpeningTag[0];
     }
     
     // get the string of contents within the outter-most html tag in the given string
     protected static String getTagContentString( String html ) throws ParseException {
         String strippedHTML = html.substring(1, html.length() - 1 );
         int firstRightAngledBracket = strippedHTML.indexOf(">");
         int lastLeftAngledBracket = strippedHTML.lastIndexOf("<");
         if ( firstRightAngledBracket == -1 || lastLeftAngledBracket == -1 ||
                 lastLeftAngledBracket < firstRightAngledBracket ) {
             String errmsg = "Unmatched angled brackets in: %s";
             String formattedErrmsg = String.format(errmsg, html);
             throw new ParseException(formattedErrmsg);
         }
         return strippedHTML.substring(firstRightAngledBracket + 1, lastLeftAngledBracket);
     }
     
     protected static boolean isJustText( String html ) {
         return !( html.contains("<") || html.contains(">") );
     }
 }
