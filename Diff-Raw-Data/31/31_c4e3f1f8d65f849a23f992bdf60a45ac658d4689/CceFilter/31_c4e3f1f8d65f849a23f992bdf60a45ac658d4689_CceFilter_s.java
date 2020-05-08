 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package calliope.importer.filters;
 
 import calliope.constants.CSSStyles;
 import calliope.exception.ImportException;
 import calliope.importer.Archive;
 import calliope.json.corcode.Range;
 import calliope.json.JSONDocument;
 import java.io.IOException;
 import java.io.ByteArrayOutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Stack;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 /**
  * Convert CCT documents to XML for import to HRIT
  * @author desmond
  */
 public class CceFilter extends Filter
 {
     static boolean preservingLineBreaks = true;
     int paraStart;
     boolean paraSeen;
     HashMap<String,String> headings;
     HashMap<String,String> formats;
     HashMap<String,String> characters;
     HashMap<String,Map<Character,Character>> accents;
     HashSet<String> endcodes;
     ArrayList<String> lineCommands;
     Stack<Location> nestedCommands;
     public CceFilter()
     {
         super();
         // chaaracter substitutions (aka entities)
         characters = new HashMap<String,String>();
         characters.put(" -- "," – ");
         characters.put("op\"","“");
         characters.put("op'","‘");
         characters.put("---","—");
         characters.put("oe","œ");
         characters.put("ae","æ");
         characters.put("mi","–");
         characters.put("bp","£");
         characters.put("di","÷");
         characters.put("l/","ł");
         characters.put("ecd","ę");
         characters.put("acd","ą");
         characters.put("s6","°");
         characters.put("pi","π");
         characters.put("s1","¶");
         characters.put("s2","½");
         characters.put("s3","¢");
         characters.put("s4","¼");
         characters.put("s5","±");
         characters.put("s7","†");
         characters.put("|","|");
         characters.put("prg","\n   ");
         // accents
         accents = new HashMap<String,Map<Character,Character>>();
         HashMap<Character,Character> acutes = new HashMap<Character,Character>();
         acutes.put('e','é');
         acutes.put('a','á');
         acutes.put('i','í');
         accents.put("a",acutes);
         HashMap<Character,Character> graves = new HashMap<Character,Character>();
         graves.put('a','à');
         graves.put('e','è');
         accents.put("gr",graves);
         accents.put("g",graves);
         HashMap<Character,Character> umlauts = new HashMap<Character,Character>();
         umlauts.put('u','ü');
         umlauts.put('o','ö');
         umlauts.put('a','ä');
         umlauts.put('i','ï');
         accents.put("um",umlauts);
         HashMap<Character,Character> tildes = new HashMap<Character,Character>();
         tildes.put('n','ñ');
         tildes.put('a','ã');
         tildes.put('o','õ');
         accents.put("tld",tildes);
         HashMap<Character,Character> macrons = new HashMap<Character,Character>();
         macrons.put('a','ā');
         macrons.put('e','ē');
         macrons.put('i','ī');
         macrons.put('o','ō');
         macrons.put('u','ū');
         accents.put("mac",macrons);
         HashMap<Character,Character> breves = new HashMap<Character,Character>();
         breves.put('a','ă');
         breves.put('e','ĕ');
         breves.put('i','ĭ');
         breves.put('o','ŏ');
         breves.put('u','ŭ');
         accents.put("mac",breves);
         HashMap<Character,Character> icarets = new HashMap<Character,Character>();
         icarets.put('e','ḙ');
         icarets.put('u','ṷ');
         accents.put("mac",icarets);
         HashMap<Character,Character> itildes = new HashMap<Character,Character>();
         itildes.put('e','ḛ');
         itildes.put('i','ḭ');
         itildes.put('u','ṵ');
         accents.put("mac",itildes);
         // headings cancelled at line-end
         headings = new HashMap<String,String>();
         headings.put("ht","head");
         headings.put("ct","head");
         headings.put("pt","titlePart");
         headings.put("cn","head");
         headings.put("pn","head");
         headings.put("sxt","head");
         headings.put("au","head-italic");
         headings.put("cst","head-italic");
         headings.put("sti","head-italic");
         headings.put("sht","head-italic");
         headings.put("ha","head");
         headings.put("hb","head");
         headings.put("hn","head");
         headings.put("ep","epigraph");
         headings.put("sep","epigraph");
         headings.put("epa","signed");
         headings.put("eph","head");
         headings.put("seph","head-italic");
         headings.put("epha","signed");
         headings.put("de","dedication");
         headings.put("sde","dedication");
         headings.put("cpt","desc");
         headings.put("sx","head");
         headings.put("sx2","head");
         
         formats = new HashMap<String,String>();
         formats.put("cop","dedication");
         formats.put("it","emph");
<<<<<<< HEAD
         formats.put("oi","emph");
=======
>>>>>>> 7e642f34b4f5a9c09ac3bb2e3b950337d4b375b1
         formats.put("bo","bold");
         formats.put("sc","smallcaps");
         formats.put("i","subscript");
         formats.put("ii","subsubscript");
         formats.put("s","superscript");
         formats.put("ss","supersuperscript");
         formats.put("sp","speaker");
         formats.put("sd","stage-italic");
         
         endcodes = new HashSet<String>();
         endcodes.add("stx");
         endcodes.add("ro");
         endcodes.add("ei");
         endcodes.add("es");
         endcodes.add("/as");
         endcodes.add("/sp");
         endcodes.add("/sd");
         lineCommands = new ArrayList<String>();
         nestedCommands = new Stack<Location>();
     }
     public String getDescription()
     {
         return "Cambridge Conrad Edition";
     }
     public void configure( JSONDocument jdoc )
     {
     }
     /**
      * Read a single dot command and write it to the output
      * @param line the line containing the command
      * @param txt the byte array output stream
      * @return true if it worked
      * @throws Exception 
      */
     void convertDotCommand( String line, ByteArrayOutputStream txt ) 
         throws Exception
     {
         if ( line.length()>1&& line.charAt(1)=='p' )
         {
             String pn = line.substring(2);
             Range r = new Range( "pb", written, 0 );
             r.addAnnotation( "n", pn );
             markup.add( r );
         }
         else
         {
             System.out.println("unknown dot command "+line+" ignored");
         }
     }
     /**
      * Convert all the files in a directory 
      * @param input the raw text input string
      * @param name the name of the new version
      * @param cortext a cortext mvd archive
      * @param corcode a corcode mvd archive
      * @return the log
      */
     public String convert( String input, String name, Archive cortex, 
         Archive corcode ) throws ImportException
     {
         try
         {
             written = 0;
             paraSeen = false;
             paraStart = 0;
             markup.clear();
             lineCommands.clear();
             nestedCommands.clear();
             ByteArrayOutputStream txt = new ByteArrayOutputStream();
             String lastWord = "";
             String firstWord = "";
             String[] lines = input.split("\n");
             for ( int i=0;i<lines.length;i++ ) 
             {
                 String str = lines[i].trim();
                 firstWord = getFirstWord( str );
                 if ( str.startsWith(".")
                     &&str.length()>1
                     &&Character.isLetter(str.charAt(1)) )
                 {
                     convertDotCommand( str, txt );
                     if ( !lastEndsInHyphen && written > 0 )
                         writeCurrent( txt, SPACE );
                     // don't reset lastWord
                     continue;
                 }
                 else if ( lines[i].startsWith("   ") )
                 {
                     if ( !paraSeen )
                         paraSeen = true;
                     else if ( written>paraStart )
                     {
                         // write previous para range
                         Range r = new Range("p",paraStart,written-paraStart);
                         markup.add( r );
                     }
                     paraStart = written;
                     writeLineContents( str, txt );
                 }
                 else
                 {
                     if ( lastEndsInHyphen )
                     {
                         if ( isHardHyphen(lastWord,firstWord) )
                         {
                             Range r = new Range( CSSStyles.STRONG, written-1, 1 );
                             markup.add( r );
                         }
                         else
                         {
                             Range r = new Range( CSSStyles.WEAK, written-1, 1 );
                             markup.add( r );
                         }
                     }
                     else if ( written > 0 )
<<<<<<< HEAD
                     {
                         writeCurrent( txt, CR );
                         if ( written == paraStart+1 )
                             paraStart = written;
                     }
=======
                        writeCurrent( txt, CR );
>>>>>>> 7e642f34b4f5a9c09ac3bb2e3b950337d4b375b1
                     writeLineContents( str, txt );
                 }
                 if ( !lineCommands.isEmpty() )
                 {
                     for ( int j=lineCommands.size()-1;j>=0;j-- )
                     {
<<<<<<< HEAD
                         Range r = new Range( lineCommands.get(j), paraStart, 
                             written-paraStart );
=======
                        Range r = new Range( lineCommands.get(j), 0, written );
>>>>>>> 7e642f34b4f5a9c09ac3bb2e3b950337d4b375b1
                         markup.add( r );
                     }
                     lineCommands.clear();
                     paraStart = written;
                 }
                 lastWord = getLastWord( str );
             }
             markup.sort();
             byte[] bytes = txt.toByteArray();
             cortex.put( name, bytes );
             String json = markup.toSTILDocument().toString();
<<<<<<< HEAD
            //System.out.println(json);
=======
>>>>>>> 7e642f34b4f5a9c09ac3bb2e3b950337d4b375b1
             corcode.put( name, json.getBytes() );
         }
         catch ( Exception e )
         {
             e.printStackTrace( System.out );
         }
         return "";
     }
     /**
      * Escape quotes when writing text
      * @param input the input text
      * @return the escaped version of input
      */
     String escape( String input )
     {
         StringBuilder sb = new StringBuilder( input );
         for ( int i=0;i<sb.length();i++ )
         {
             char token = sb.charAt(i);
             if ( token == 34 )
                 sb.setCharAt(i,'”');
             else if ( token == 39 )
                 sb.setCharAt(i,'’');
         }
         return sb.toString();
     }
     /**
      * An accent has been detected. Process it
      * @param txt the byte output stream
      * @param accent the name of the accent in CCE
      * @param pending the preceding text in the line
      * @throws Exception 
      */
     void processAccent( ByteArrayOutputStream txt, String pending, 
             String accent ) throws Exception
     {
         Map<Character,Character> map = accents.get(accent);
         if ( pending != null && pending.length()>0 )
         {
             char last = pending.charAt(pending.length()-1);
             if ( map.containsKey(last) )
             {
                 String adjusted = pending.substring(
                     0,pending.length()-1)+map.get(last);
                 writeCurrent( txt, adjusted.getBytes(ENC) );
                 return;
             }
         }
         writeCurrent( txt, accent.getBytes(ENC) );
     }
     /**
      * Write out the pending text
      * @param txt the destination byte stream
      * @param pending the pending text
      * @return null to clear pending
      * @throws IOException 
      */
     String writePending( ByteArrayOutputStream txt, String pending )
         throws IOException
     {
         if ( pending != null )
         {
             writeCurrent( txt, pending.getBytes(ENC) );
             pending = null;
         }
         return null;
     }             
     /**
      * Write the contents of a line
      * @param line the current line
      * @param fos the output stream
      * @return true if it worked
      */
     boolean writeLineContents( String line, ByteArrayOutputStream txt ) 
         throws Exception
     {
         boolean result = true;
         StringTokenizer st = new StringTokenizer( line, "{}", true );
         int state = 0;
         String pending = null;
         while ( st.hasMoreTokens() )
         {
             String token = st.nextToken();
             switch ( state )
             {
                 case 0: // looking for '{'
                     if ( token.equals("{") )
                         state = 1;
                     else
                         pending = token;
                     break;
                 case 1: // reading command
                     if ( accents.containsKey(token) )
                     {
                         pending = writePending( txt, pending );
                         processAccent(txt,pending,token);
                     }
                     else if ( characters.containsKey(token) )
                     {
                         String rep = characters.get(token);
                         pending = writePending( txt, pending );
                         writeCurrent( txt, rep.getBytes(ENC) );
                     }
                     else if ( headings.containsKey(token) )
                     {
                         lineCommands.add( headings.get(token) );
                     }
                     else if ( formats.containsKey(token) )
                     {
                         pending = writePending( txt, pending );
                         Location loc = new Location( formats.get(token),written );
                         nestedCommands.push( loc );
                     }
                     else if ( endcodes.contains(token) 
                         && nestedCommands.size()>0 )
                     {
                         Location loc = nestedCommands.remove(
                             nestedCommands.size()-1);
                         pending = writePending( txt, pending );
                         Range r = new Range( loc.name, loc.loc, written-loc.loc );
                         markup.add( r );
                     }
                     else
                         System.out.println("ignoring unknown command "+token);
                     state = 2;
                     break;
                 case 2: // reading closing brace
                     if ( !token.equals("}") )
                     {
                         System.out.println("missing closing brace");
                     }
                     state = 0;
                     break;
             }
         }
         writePending( txt, pending );
         return result;
     }
     class Location
     {
         String name;
         int loc;
         Location( String name, int loc )
         {
             this.loc = loc;
             this.name = name;
         }
     }
 }
