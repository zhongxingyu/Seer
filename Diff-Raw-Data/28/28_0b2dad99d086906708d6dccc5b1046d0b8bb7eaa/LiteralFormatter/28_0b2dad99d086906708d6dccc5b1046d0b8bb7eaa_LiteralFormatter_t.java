 package de.skuzzle.polly.core.parser.ast.expressions.literals;
 
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Iterator;
import java.util.Locale;
 
 import de.skuzzle.polly.core.parser.ast.declarations.Declaration;
 import de.skuzzle.polly.core.parser.ast.expressions.Expression;
 import de.skuzzle.polly.core.parser.ast.lang.Function;
 import de.skuzzle.polly.core.parser.ast.visitor.Unparser;
 import de.skuzzle.polly.core.parser.util.TimeSpanFormat;
 import de.skuzzle.polly.tools.streams.StringBuilderWriter;
 import de.skuzzle.polly.tools.strings.IteratorPrinter;
 import de.skuzzle.polly.tools.strings.IteratorPrinter.StringProvider;
 
 /**
  * Visitor style interface to format literals into a string representation. It can be
  * used in conjunction with {@link Literal#format(LiteralFormatter)}.
  * 
  * @author Simon Taddiken
  */
 public interface LiteralFormatter {
     
     /**
      * This formatter formats all literals so that the result will be parseable again.
      */
     public LiteralFormatter DEFAULT = new LiteralFormatter() {
         
         private final TimeSpanFormat TIMESPAN_FORMAT = new TimeSpanFormat(false);
         private final DateFormat DATE_FORMAT = new SimpleDateFormat(
             "HH:mm@dd.MM.yyyy");
         
         @Override
         public String formatNumberLiteral(NumberLiteral number) {
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
                    Locale.ENGLISH);
            df.applyPattern("0.#####");
             final double val = number.getValue();
             if (Math.round(val) == val) {
                 int intVal = (int) val;
                 return Integer.toString(intVal, number.getRadix());
             }
            return df.format(val);
         }
         
         
 
         @Override
         public String formatDate(DateLiteral date) {
             return DATE_FORMAT.format(date.getValue());
         }
 
         
         
         @Override
         public String formatTimespan(TimespanLiteral timespan) {
             return TIMESPAN_FORMAT.format(timespan.getSeconds());
         }
 
 
 
         @Override
         public String formatChannel(ChannelLiteral channelLiteral) {
             return "#" + channelLiteral.getValue();
         }
 
 
 
         @Override
         public String formatString(StringLiteral string) {
             return "\"" + string.getValue() + "\"";
         }
 
 
 
         @Override
         public String formatUser(UserLiteral user) {
             return "@" + user.getValue();
         }
 
 
 
         @Override
         public String formatList(ListLiteral listLiteral) {
             StringBuilder b = new StringBuilder();
             b.append("{");
             IteratorPrinter.print(
                 listLiteral.getContent(), ", ", 
                 new StringProvider<Expression>() {
                     @Override
                     public String toString(Expression o) {
                         return Unparser.toString(o);
                     }
             }, new PrintWriter(new StringBuilderWriter(b)));
             b.append("}");
             return b.toString();
         }
 
 
 
         @Override
         public String formatFunction(FunctionLiteral functionLiteral) {
             final StringBuilder b = new StringBuilder();
             b.append("\\(");
             final Iterator<Declaration> it = functionLiteral.getFormal().iterator();
             while (it.hasNext()) {
                 final Declaration formal = it.next();
                 b.append(formal.getType().getName());
                 b.append(" ");
                 b.append(formal.getName());
                 if (it.hasNext()) {
                     b.append(",");
                 }
             }
             if (!(functionLiteral.getBody() instanceof Function)) {
                 b.append(":");
                 b.append(Unparser.toString(functionLiteral.getBody(), this));
             }
             b.append(")");
             return b.toString();
         }
 
 
 
         @Override
         public String formatHelp(HelpLiteral helpLiteral) {
             return "?";
         }
     };
     
     
     /**
      * Formats a number literal to string.
      * 
      * @param number The literal to format.
      * @return A String representation of that literal.
      */
     public String formatNumberLiteral(NumberLiteral number);
     
     
     /**
      * Formats a date literal to string.
      * 
      * @param date The literal to format.
      * @return A String representation of that literal.
      */
     public String formatDate(DateLiteral date);
     
     /**
      * Formats a timespan literal to string.
      * 
      * @param timespan The literal to format.
      * @return A String representation of that literal.
      */
     public String formatTimespan(TimespanLiteral timespan);
 
     /**
      * Formats a channel literal to string.
      * 
      * @param channel The literal to format.
      * @return A String representation of that literal.
      */
     public String formatChannel(ChannelLiteral channel);
     
     /**
      * Formats a string literal to string (yeah, funny ehh?).
      * 
      * @param string The literal to format.
      * @return A String representation of that literal.
      */
     public String formatString(StringLiteral string);
 
     /**
      * Formats a user literal to string.
      * 
      * @param user The literal to format.
      * @return A String representation of that literal.
      */
     public String formatUser(UserLiteral user);
 
     /**
      * Formats a list literal to string.
      * 
      * @param listLiteral The literal to format.
      * @return A String representation of that literal.
      */
     public String formatList(ListLiteral listLiteral);
 
     /**
      * Formats a function literal to string.
      * 
      * @param functionLiteral The literal to format.
      * @return A String representation of that literal.
      */
     public String formatFunction(FunctionLiteral functionLiteral);
 
     /**
      * Formats a help literal to string.
      * 
      * @param helpLiteral The literal to format.
      * @return A String representation of that literal.
      */
     public String formatHelp(HelpLiteral helpLiteral);
 }
