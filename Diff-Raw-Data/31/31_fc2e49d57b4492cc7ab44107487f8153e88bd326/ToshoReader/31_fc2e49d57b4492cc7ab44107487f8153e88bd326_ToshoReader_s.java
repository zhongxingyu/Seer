 package toshomal.common;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 import net.socialchange.doctype.Doctype;
 import net.socialchange.doctype.DoctypeChangerStream;
 import net.socialchange.doctype.DoctypeGenerator;
 import net.socialchange.doctype.DoctypeImpl;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.xml.sax.InputSource;
 
 import toshomal.data.DbShow;
 import toshomal.data.MalEntry;
 import toshomal.data.ParsedFileName;
 import toshomal.data.SearchString;
 
 import java.io.*;
 import java.net.*;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.Inflater;
 import java.util.zip.InflaterInputStream;
 
 import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;
 
 public class ToshoReader {
 
     private final static String tosho_url = "http://tokyotosho.info/rss.php?filter=1";;
     private EmbeddedDBConnector db;
     //static PrintWriter out;
     private String uname;
     private String pass;
 
     public ToshoReader()
     {
         db = new EmbeddedDBConnector();
         String[] creds = db.getCredentials();
         uname = creds[0];
         pass = creds[1];
     }
 
 //    public static void setCredentials()
 //    {
 //        String[] creds = db.getCredentials();
 //        uname = creds[0];
 //        pass = creds[1];
 //    }
 
     private String getUrl (String URL)
     {
         InputStream content = null;
         URL u;
         URLConnection uc;
         System.out.println(URL);
         try {
             u = new URL(URL);
             try {
                 uc = u.openConnection();
                 if (uname != null)
                 {
                     uc.setRequestProperty("Authorization", String.format("Basic %s", encodeBase64URLSafeString((uname + ":" + pass).getBytes())));
                 }
                 content = uc.getInputStream();
                 BufferedInputStream in = new BufferedInputStream(content);
                 if (in.available() < 1)
                     return null;
             } catch (Exception e) {
                 e.printStackTrace();
                 System.out.println("ERROR: "+e.getMessage());
             }
         } catch (MalformedURLException e) {
             e.printStackTrace();
             System.out.println(URL + " is not a parseable URL");
         }
 //        DoctypeChangerStream changer = new DoctypeChangerStream(content);
 //        changer.setGenerator(
 //                new DoctypeGenerator()
 //                {
 //                    public Doctype generate(Doctype old) {
 //                        return new DoctypeImpl(null,
 //                                "-//W3C//DTD XHTML 1.0 Transitional//EN",
 //                                "xhtml1-transitional.dtd",
 //                                null);
 //                    }
 //                }
 //        );
 //
 //        InputSource src = new InputSource(changer);
 //        src.setEncoding("iso-8859-1");
 //        return src;
         try {
 			return IOUtils.toString(content, "UTF-8");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
     }
 
     private MalEntry getShowDetails(String input, String showTitle, int LVdist)
     {
         try {
             Pattern p_synsplit = Pattern.compile("\\s*;\\s*");
             //SAXBuilder builder = new SAXBuilder();
             //Document maldoc = builder.build(new StringReader(StringEscapeUtils.unescapeHtml(input)));
             //Element root = maldoc.getRootElement();
             //List listOfEntries = root.getChildren("entry");
             //Iterator i = listOfEntries.iterator();
             List<MalEntry> listOfEntries = MalEntry.parseFromString(input);
             MalEntry best_entry = null;
             for (MalEntry mal_entry : listOfEntries)
             //while (i.hasNext())
             {
                 //Element mal_entry = (Element)i.next();
             	//Element mal_entry = (Element) e;
                 //Element mal_stat = mal_entry.getChild("status");
                 String mal_title = mal_entry.get("title");
                 int lv = StringUtils.getLevenshteinDistance(showTitle, mal_title);
                 if (lv < LVdist)
                 {
                     LVdist = lv;
                     best_entry = mal_entry;
                 }
                 String mal_syn = mal_entry.get("synonyms");
                 //System.out.println("MAL Title: " + mal_title.getTextTrim());
                 //System.out.println("MAL Status: " + mal_stat.getTextTrim());
                 if (mal_syn != null && mal_syn.length() > 0)
                 {
                     String[] mal_synonyms = p_synsplit.split(mal_syn);
                     for (String mal_synonym : mal_synonyms) {
                         lv = StringUtils.getLevenshteinDistance(showTitle, mal_synonym);
                         if (lv < LVdist) {
                             LVdist = lv;
                             best_entry = mal_entry;
                         }
                     }
                 }
             }
             return best_entry;
         } catch (Exception ex) {
         	ex.printStackTrace();
             //System.out.println("ERROR: " + ex.getMessage());
             return null;
         }
     }
 
 //    private void PrintEntry(PrintWriter out, Element entry, SyndEntry t_entry) {
 //        try {
 //            Matcher m_thumb = Pattern.compile("\\.jpg$").matcher(entry.getChild("image").getTextTrim());
 //            Pattern p_synsplit = Pattern.compile("\\s*;\\s*");
 //            out.println("<tr><td valign=\"top\" width=\"55\"><div class=\"picSurround\">");
 //            out.println("<img src=\"" + m_thumb.replaceFirst("t.jpg") + "\" border=\"0\">");
 //            out.println("</div></td><td valign=\"top\" class=\"borderClass\">");
 //            out.println("<div><strong>" + entry.getChild("title").getTextTrim() + "</strong></div>");
 //            if (entry.getChild("synonyms").getTextTrim() != null && entry.getChild("synonyms").getTextTrim().length() > 0)
 //            {
 //                String[] mal_synonyms = p_synsplit.split(entry.getChild("synonyms").getTextTrim());
 //                for (String mal_synonym : mal_synonyms) {
 //                    out.println("<div><i>" + mal_synonym + "</i></div>");
 //                }
 //            }
 //            out.println("<div><a href=\"" + t_entry.getLink() + "\">" + t_entry.getTitle() + "</a></div>");
 //            out.println("<div>&nbsp;</div></td></tr>");
 //        } catch (Exception ex) {
 //            System.out.println("ERROR: " + ex.getMessage());
 //        }
 //    }
 
     private static ParsedFileName parseFileEntry(String file)
     {
         Matcher m_stag = Pattern.compile("^[_ ]*[\\[\\(]([^\\]\\)]+)[\\]\\)][_ ]*").matcher("");
         Matcher m_etag = Pattern.compile("[_ ]*[\\[\\(]([^\\]\\)]+)[\\]\\)][_ ]*").matcher("");
         Matcher m_wht = Pattern.compile("_").matcher("");
         Matcher m_eps = Pattern.compile("[-\\s]+((?:\\d+-)?\\d+)(\\s*v(\\d+))*\\s*$").matcher("");
         Matcher m_eps_se = Pattern.compile("[-\\s]+[sS]\\d+\\s*[eE](\\d+)").matcher("");
         Matcher m_ext = Pattern.compile("\\.(\\w{3})$").matcher("");
         Matcher m_md5 = Pattern.compile("^([0-9A-F]{8})$").matcher("");
 
         String eps = "0";
         String ver = "0";
         String ext = "";
         String sub = "0";
         String md5 = "";
         List<String> tags = new ArrayList<String>();
 
         m_wht.reset(file);
         String search = m_wht.replaceAll(" ");
         System.out.println(search);
         m_stag.reset(search);
         while (m_stag.find())
         {
             System.out.println("stag->" + m_stag.group(1));
             tags.add(m_stag.group(1));
             search = m_stag.replaceFirst("");
             m_stag.reset(search);
         }
         m_etag.reset(search);
         while (m_etag.find())
         {
             System.out.println("etag->" + m_etag.group(1));
             m_md5.reset(m_etag.group(1));
             if (m_md5.find())
             { md5 = m_md5.group(1); }
             else
             { tags.add(m_etag.group(1)); }
             search = m_etag.replaceFirst("");
             m_etag.reset(search);
         }
         m_ext.reset(search);
         if (m_ext.find())
         {
             System.out.println("ext->" + m_ext.group(1));
             ext = m_ext.group(1);
             search = m_ext.replaceFirst("");
         }
         m_eps.reset(search);
         if (m_eps.find())
         {
             System.out.println("eps->" + m_eps.group(1));
             eps = m_eps.group(1);
             if (m_eps.group(3) != null && m_eps.group(3).length() > 0)
             {
                 System.out.println("sub->" + m_eps.group(3));
                 ver = m_eps.group(3);
             }
             search = m_eps.replaceFirst("");
         }
         else
         {
             m_eps_se.reset(search);
             if (m_eps_se.find())
             {
                 System.out.println("eps->" + m_eps_se.group(1));
                 eps = m_eps_se.group(1);
                 search = m_eps_se.replaceFirst("");
             }
         }
         System.out.println("title->" + search);
 
         return new ParsedFileName(file, eps, sub, ver, ext, md5, tags, search);
     }
 
     public boolean getToshoContent()
     {
         Date lastUpdate = db.getLatestUpdate();
         boolean result = false;
         //setCredentials();
         try {
             URL feedUrl = new URL(tosho_url);
             InputStream rcvData = null;
             HttpURLConnection http = (HttpURLConnection) feedUrl.openConnection();
             http.setRequestProperty("Accept-Encoding", "gzip,deflate");
             http.connect();
             String encoding = http.getContentEncoding();
             if (encoding.equalsIgnoreCase("gzip"))
                 rcvData = new GZIPInputStream(http.getInputStream());
             else if (encoding.equalsIgnoreCase("deflate"))
                 rcvData = new InflaterInputStream(http.getInputStream(), new Inflater(true));
             else
                 rcvData = http.getInputStream();
             XmlReader reader = new XmlReader(rcvData);
             Matcher m_200b = Pattern.compile("\u200B").matcher("");
 
             SyndFeedInput input = new SyndFeedInput();
             SyndFeed feed = input.build(reader);
 
             Iterator entryIter = feed.getEntries().iterator();
             Date newUpdate = lastUpdate;
             while (entryIter.hasNext())
             {
                 SyndEntry entry = (SyndEntry) entryIter.next();
                 if (! entry.getPublishedDate().after(lastUpdate))
                 {
                     System.out.println("Old Entry: " + entry.getPublishedDate().toString() + "   " + entry.getTitle());
                     continue;
                 }
                 if (entry.getPublishedDate().after(newUpdate))
                 {
                     newUpdate = entry.getPublishedDate();
                     result = true;
                 }
                 String title = entry.getTitle();
                 m_200b.reset(title);
                 title = m_200b.replaceAll("");
                 ParsedFileName fname = parseFileEntry(title);
                 SearchString full_title = new SearchString(fname.getSearch());
                 //InputSource ins = null;
                 String content = null;
                 String search = full_title.getFirst();
                 System.out.println("Search string: " + search);
                 content = getUrl("http://myanimelist.net/api/anime/search.xml?q=" + URLEncoder.encode(search,"utf-8"));
                 MalEntry best_entry = null;
                 int lvdist = 500;
                 while (content != null)
                 {
                     best_entry = getShowDetails(content, fname.getSearch(),lvdist);
                     if (best_entry != null)
                         System.out.println("MAL best match: " + best_entry.get("title"));
                     String part = full_title.getLeft();
                     if (search.compareTo(part) == 0)
                         break;
                     search = part;
                     System.out.println("Search string: " + search);
                     content = getUrl("http://myanimelist.net/api/anime/search.xml?q=" + URLEncoder.encode(search,"utf-8"));
                 }
                 if (search.compareTo(full_title.getRight()) != 0)
                 {
                     search = full_title.getRight();
                     System.out.println("Search string: " + search);
                     content = getUrl("http://myanimelist.net/api/anime/search.xml?q=" + URLEncoder.encode(search,"utf-8"));
                     while (content != null)
                     {
                         best_entry = getShowDetails(content, fname.getSearch(),lvdist);
                         if (best_entry != null)
                             System.out.println("MAL best match: " + best_entry.get("title"));
                         String part = full_title.getRight();
                         if (search.compareTo(part) == 0)
                             break;
                         search = part;
                         System.out.println("Search string: " + search);
                         content = getUrl("http://myanimelist.net/api/anime/search.xml?q=" + URLEncoder.encode(search,"utf-8"));
                     }
                 }
                 if (best_entry != null)
                 {
                     System.out.println("FINAL MAL best match: " + best_entry.get("title"));
                     //PrintEntry(out, best_entry, entry);
                     db.UpdateEntry(best_entry, entry, fname);
                 }
                 System.out.println("========================================");
             }
             //out.println("</table></body></html>");
             //out.close();
         }   catch (Exception ex) {
             ex.printStackTrace();
             //System.out.println("ERROR: "+ex.getMessage());
         }
         return result;
     }
 
     public DbShow searchForShow(String search)
     {
         DbShow show = null;
         try {
             String content = getUrl("http://myanimelist.net/api/anime/search.xml?q=" + URLEncoder.encode(search,"utf-8"));
             if(content != null)
             {
                 MalEntry result = getShowDetails(content, search ,500);
                 if (result != null)
                 {
                     Matcher m_thumb = Pattern.compile("\\.jpg$").matcher(result.get("image"));
                     String image = m_thumb.replaceFirst("t.jpg");
                     show = new DbShow(-1,
                             result.get("title"),
                             Integer.parseInt(result.get("id")),
                             result.get("status"),
                             result.get("type"),
                             Integer.parseInt(result.get("episodes")),
                             image);
                 }
             }
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         return show;
     }
 
     public static void main(String[] args) {
         try {
             new ToshoReader();
             //getToshoContent();
         }
         catch (Exception ex) {
             ex.printStackTrace();
             System.out.println("ERROR: "+ex.getMessage());
         }
     }
 
 }
 
