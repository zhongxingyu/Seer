 package net.sf.yal10n.diff;
 
 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.velocity.tools.generic.EscapeTool;
 
 /**
  * This class represents a diff produced by subversion.
  * It can convert the diff into a side-by-side comparison html format.
  */
 public class UnifiedDiff
 {
     private static final Pattern HUNK_START_PATTERN = Pattern.compile( "^@@ \\-(\\d+),(\\d+) \\+(\\d+),(\\d+) @@" );
     private String originalName;
     private String newName;
     private List<Hunk> hunks = new ArrayList<Hunk>();
 
     /**
      * Create a new {@link UnifiedDiff} with the given diff.
      * @param diffString the diff data
      */
     public UnifiedDiff( String diffString )
     {
         parse( diffString );
     }
 
     private void parse( String diff )
     {
         String[] lines = diff.split( "\n" );
 
         if ( lines.length < 5 )
         {
             throw new IllegalArgumentException( "The given diff is too short. " );
         }
 
         originalName = lines[2].substring( 4 );
         newName = lines[3].substring( 4 );
 
         int currentLineNumber = 0;
         Hunk currentHunk = null;
 
         for ( int i = 4; i < lines.length; i++ )
         {
             String line = lines[i];
             String nextLine = "\\\\";
             if ( i + 1 < lines.length )
             {
                 nextLine = lines[i + 1];
             }
 
             Matcher m = HUNK_START_PATTERN.matcher( line );
             if ( m.find() )
             {
 
                 if ( currentHunk != null )
                 {
                     currentHunk.lastLineNumber = currentLineNumber;
                     hunks.add( currentHunk );
                 }
                 currentHunk = new Hunk();
                 currentLineNumber = Math.min( Integer.parseInt( m.group( 1 ) ), Integer.parseInt( m.group( 3 ) ) );
                 currentHunk.firstLineNumber = currentLineNumber;
             }
             else
             {
                 if ( currentHunk == null )
                 {
                     // ignore this line outside of a hunk
                     continue;
                 }
 
                 char indicator = line.charAt( 0 );
                 String diffLine = line.substring( 1 );
                 char nextIndicator = nextLine.charAt( 0 );
 
                 switch ( indicator )
                 {
                 case ' ':
                     currentHunk.commonLines.put( currentLineNumber, diffLine );
                     currentHunk.indicators.put( currentLineNumber, ' ' );
                     currentLineNumber++;
                     break;
                 case '-':
                     currentHunk.origLines.put( currentLineNumber, diffLine );
                     if ( nextIndicator == '+' )
                     {
                         currentHunk.indicators.put( currentLineNumber, 'C' );
                     }
                     else
                     {
                         currentHunk.indicators.put( currentLineNumber, '-' );
                         currentLineNumber++;
                     }
                     break;
                 case '+':
                     currentHunk.newLines.put( currentLineNumber, diffLine );
                     if ( currentHunk.indicators.get( currentLineNumber ) == null )
                     {
                         currentHunk.indicators.put( currentLineNumber, '+' );
                     }
                     currentLineNumber++;
                     break;
                 default:
                     // ignore
                 }
             }
         }
         if ( currentHunk != null )
         {
             currentHunk.lastLineNumber = currentLineNumber;
             hunks.add( currentHunk );
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString()
     {
         StringBuilder out = new StringBuilder();
         out.append( "orig: " ).append( originalName ).append( "\n" );
         out.append( " new: " ).append( newName ).append( "\n" );
         out.append( "found " + hunks.size() + " hunks\n" );
         for ( Hunk hunk : hunks )
         {
             for ( int i = hunk.firstLineNumber; i < hunk.lastLineNumber; i++ )
             {
                 Character indicator = hunk.indicators.get( i );
 
                 out.append( indicator ).append( i ).append( "  " );
 
                 switch ( indicator )
                 {
                 case '-':
                     out.append( hunk.origLines.get( i ) );
                     break;
                 case ' ':
                     out.append( hunk.commonLines.get( i ) );
                     break;
                 case '+':
                     out.append( hunk.newLines.get( i ) );
                     break;
                 case 'C':
                 default:
                     out.append( hunk.origLines.get( i ) ).append( " || " ).append( hunk.newLines.get( i ) );
                     break;
                 }
                 out.append( "\n" );
             }
         }
         return out.toString();
     }
 
     /**
      * Converts this diff into a side-by-side html format.
      * @return the html code
      */
     public String asHtmlSnippet()
     {
         EscapeTool escapeTool = new EscapeTool();
         final String lightYellow = "#ffff80";
         final String lightRed = "#f08080";
         final String lightGreen = "#90ee90";
         final String lightGrey = "#d3d3d3";
         final String darkCyan = "#008b8b";
 
        final String firstColumnStyle = " style=\"padding: 0 0.5em 0 0.5em; text-align: right;\"";

         StringBuilder out = new StringBuilder();
        out.append( "<div style=\"margin: 10px; border: 1px #aaaaaa solid; border-radius: 10px; padding: 5px;\">\n" );
         out.append( "<table border=\"0\" width=\"100%\" cellspacing=\"0\">\n" );
        out.append( "<tr><td" ).append( firstColumnStyle ).append( ">&nbsp;</td>" );
         out.append( "<td><strong>" ).append( escapeTool.html( originalName ) ).append( "</strong></td>" );
         out.append( "<td><strong>" ).append( escapeTool.html( newName ) ).append( "</strong></td>" );
         out.append( "</tr>\n" );
 
         for ( Hunk hunk : hunks )
         {
             out.append( "<tr style=\"background: " ).append( darkCyan ).append( ";\")>" );
            out.append( "<td" ).append( firstColumnStyle ).append( ">#</td>" );
             out.append( "<td><strong>Line " ).append( hunk.firstLineNumber ).append( "</td>" );
             out.append( "<td><strong>Line " ).append( hunk.firstLineNumber ).append( "</td>" );
             out.append( "</tr>" );
 
             for ( int i = hunk.firstLineNumber; i < hunk.lastLineNumber; i++ )
             {
                 Character indicator = hunk.indicators.get( i );
                 String currentLine = escapeTool.html( hunk.commonLines.get( i ) );
                 String originalLine = escapeTool.html( hunk.origLines.get( i ) );
                 String newLine = escapeTool.html( hunk.newLines.get( i ) );
 
                 if ( originalLine == null || originalLine.isEmpty() )
                 {
                     originalLine = "&nbsp;";
                 }
                 if ( currentLine == null || currentLine.isEmpty() )
                 {
                     currentLine = "&nbsp;";
                 }
                 if ( newLine == null || newLine.isEmpty() )
                 {
                     newLine = "&nbsp;";
                 }
 
                 out.append( "<tr>" );
                out.append( "<td" ).append( firstColumnStyle ).append( ">" ).append( i ).append( "</td>" );
                 switch ( indicator )
                 {
                 case '-':
                     out.append( "<td style=\"background: " ).append( lightRed ).append( ";\">" )
                         .append( originalLine ).append( "</td>" );
                     out.append( "<td style=\"background: " ).append( lightGrey ).append( ";\">&nbsp;</td>" );
                     break;
                 case ' ':
                     out.append( "<td>" ).append( currentLine ).append( "</td>" );
                     out.append( "<td>" ).append( currentLine ).append( "</td>" );
                     break;
                 case '+':
                     out.append( "<td style=\"background: " ).append( lightGrey ).append( ";\">&nbsp;</td>" );
                     out.append( "<td style=\"background: " ).append( lightGreen ).append( ";\">" )
                         .append( newLine ).append( "</td>" );
                     break;
                 case 'C':
                 default:
                     out.append( "<td style=\"background: " ).append( lightYellow ).append( ";\">" )
                         .append( originalLine ).append( "</td>" );
                     out.append( "<td style=\"background: " ).append( lightYellow ).append( ";\">" )
                         .append( newLine ).append( "</td>" );
                     break;
                 }
                 out.append( "</tr>\n" );
             }
         }
         out.append( "</table>\n" );
 
        out.append( "<hr style=\"border: 1px #aaaaaa solid;\"/>\n" );
        out.append( "<table border=\"0\" cellspacing=\"0\" style=\"border: 1px #aaaaaa solid\">\n" );
         out.append( "<colgroup>\n" );
         out.append( "    <col width=\"50%\" />\n" );
         out.append( "    <col width=\"50%\" />\n" );
         out.append( "</colgroup>\n" );
         out.append( "<tr><td colspan=\"2\">Legend:</td></tr>\n" );
         out.append( "<tr><td style=\"background: " ).append( lightRed ).append( ";\">removed</td>" )
             .append( "<td style=\"background: " ).append( lightGrey ).append( ";\">&nbsp;</td></tr>\n" );
         out.append( "<tr><td colspan=\"2\" style=\"background: " ).append( lightYellow )
             .append( "; text-align: center;\">changed lines</td></tr>\n" );
         out.append( "<tr><td style=\"background: " ).append( lightGrey ).append( ";\">&nbsp;</td>" )
             .append( "<td style=\"background: " ).append( lightGreen ).append( ";\">added</td></tr>\n" );
         out.append( "</table>\n" );
        out.append( "</div>\n" );
 
         return out.toString();
     }
 
     /**
      * Gets the original name.
      *
      * @return the original name
      */
     public String getOriginalName()
     {
         return originalName;
     }
 
     /**
      * Gets the new name.
      *
      * @return the new name
      */
     public String getNewName()
     {
         return newName;
     }
 
     /**
      * Gets the hunks.
      *
      * @return the hunks
      */
     public List<Hunk> getHunks()
     {
         return hunks;
     }
 }
