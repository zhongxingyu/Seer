 package lq;
 
 import java.io.Writer;
 import java.io.PrintWriter;
 
 public class Printer {
     private final PrintWriter out;
     
     public Printer(Writer out) {
         this.out = new PrintWriter(out);
     }
 
     public void printQuote(Quote quote) {
        printQuote(quote, null);
    }

    public void printQuote(Quote quote, Long displayIndex) {
        if (displayIndex == null) {
            displayIndex = quote.getId();
        }
         out.println("<br>");
         out.println("<a href=\"/view_quote?id=" + quote.getId() + "\">" +
            "#" + displayIndex +
             "</a>"+
             ", lagt til av " + Strings.escape(quote.getAuthor()) + "<br>");
 
         String date = DateUtil.dateFormat.format(quote.getQuoteDate());
         out.println("Dato: " + date + ", Score: ");
 
         out.println("<span id=\"v" + quote.getId() + "\">");
         out.println(QuoteUtil.formatScore(quote));
         out.println("<br> Vote: <font size=\"-1\">");
         for(int nv=1; nv<=5; nv++) 
             out.println("<a href=\"javascript:vote(" + quote.getId() + ","+nv+")\">"+nv+"</a> ");
         out.println("</font> </span>");
         
         out.println("<br> <br>");
         out.println();
         String content = Strings.escape(quote.getContent());
         out.println(content
                 .replaceAll("(http://[^ \r\n]+)","<a href=\"$1\">$1</a>")
                 .replaceAll("\n","<br>\n"));
         out.println("");
         out.println("<hr>");
     }
 }
