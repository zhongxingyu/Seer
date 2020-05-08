 package de.metalab.namnam.parser.erlangennuernberg;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import de.metalab.namnam.model.Mensa;
 import de.metalab.namnam.model.Mensaessen;
 import de.metalab.namnam.model.Tagesmenue;
 import de.metalab.namnam.parser.NamNamParser;
 import de.metalab.namnam.parser.util.MutableNamespaceContext;
 import de.metalab.namnam.parser.util.XPathUtil;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * common functionality for all menues from the erlangen/nuernberg studentenwerk
  * 
  * if you wonder why this parser is subclassed just for the urls, keep in mind that
  * the format of the html pages can change any time and may be inconsistent between
  * the differen locations.
  *
  * @author fake
  */
 public abstract class NamNamParserErlangenNuernbergBase implements NamNamParser {
 
     protected String theURL; // this is what children fill in
     protected SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
     protected DecimalFormat df = new DecimalFormat();
     protected DecimalFormatSymbols decf = new DecimalFormatSymbols(Locale.GERMAN);
 
     protected NamNamParserErlangenNuernbergBase() {
         this.df.setDecimalFormatSymbols(this.decf);
     }
 
     abstract public String getMensaName();
 
     public Mensa getCurrentMenues() throws Exception {
         Mensa mensa = new Mensa(this.getMensaName());
 
         Node node = XPathUtil.getHtmlUrlNode(theURL, "latin1");
         MutableNamespaceContext nc = new MutableNamespaceContext();
         nc.setNamespace("html", "http://www.w3.org/1999/xhtml");
 
         String QUERY = "//html:div[@id='content']/html:div[1]/html:table";
 
         // leave it all behind
         Node table = XPathUtil.query(node, QUERY, nc).item(0).cloneNode(true);
 
         QUERY = "//*[contains(html:div,'Essen')]";
 
         NodeList anchors = XPathUtil.query(table, QUERY, nc);
         for (int n = 0; n < anchors.getLength(); n++) {
             Node td = anchors.item(n);
             String idx = td.getChildNodes().item(0).getTextContent();
             Node descTd = td.getNextSibling();
             String desc = descTd.getChildNodes().item(0).getTextContent();
             Node sPriceTd = descTd.getNextSibling();
             String sPrice = sPriceTd.getChildNodes().item(0).getTextContent();
             Node bPriceTd = sPriceTd.getNextSibling();
             String bPrice = bPriceTd.getChildNodes().item(0).getTextContent();
 
             // that was easy. the tricky part is to find the date!
             String date = "";
            if (idx.endsWith("1")) { // Esen 1: go down
                 date = td.getParentNode().getNextSibling().
                         getChildNodes().item(0).getChildNodes().item(0).getTextContent();
            } else if (idx.endsWith("2")) { // Essen 2: right here
                 date = td.getPreviousSibling().
                         getChildNodes().item(0).getTextContent();
            } else if (idx.endsWith("3")) { // Essen 3: go up
                 date = td.getParentNode().getPreviousSibling().
                         getChildNodes().item(0).getChildNodes().item(0).getTextContent();
             } else {
                System.err.println("Warning: unknown index skipped! " + idx);
                 continue;
             }
 
             Date d = getDateFromString(date);
             if(d == null) continue;
             
             Tagesmenue daymeal = mensa.getMenuForDate(d);
             if (daymeal == null) {
                 daymeal = new Tagesmenue(d);
                 mensa.addDayMenue(daymeal);
             }
             Mensaessen me = new Mensaessen(desc, getPriceInCents(bPrice), getPriceInCents(sPrice));
             daymeal.addMenu(me);
         }
 
         return mensa;
     }
 
     protected Integer getPriceInCents(String s) throws Exception {
         if (s == null || "".equals(s.trim())) return null;
         return new Double(df.parse(s.substring(0, s.indexOf(',')+2)).doubleValue() * 100).intValue();
     }
 
     protected Date getDateFromString(String d) throws Exception {
         if(d == null || "".equals(d.trim())) return null;
         Date date = sdf.parse(d.substring(3));
         return date;
     }
 }
