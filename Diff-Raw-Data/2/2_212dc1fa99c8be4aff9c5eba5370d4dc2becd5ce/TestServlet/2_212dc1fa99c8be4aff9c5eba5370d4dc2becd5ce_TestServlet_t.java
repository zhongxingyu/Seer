 package emm4xmas;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import java.io.*;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.io.IOException;
 import java.util.*;
 
 import com.google.appengine.api.datastore.*;
 import com.google.appengine.api.datastore.Text;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.SAXException;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: User
  * Date: 12/12/12
  * Time: 12:58
  * To change this template use File | Settings | File Templates.
  */
 public class TestServlet extends HttpServlet
     {
 
         private static ArrayList<Key> Keys = new ArrayList<Key>();
 
         protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
             {
 
                 try
                     {
                         URL url = new URL("http://www.example.com/atom.xml");
                         BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                         String line;
 
                         while ((line = reader.readLine()) != null)
                             {
                                 // ...
                             }
                         reader.close();
 
                     } catch (MalformedURLException e)
                     {
                         // ...
                     } catch (IOException e)
                     {
                         // ...
                     }
 
             }
 
         protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
             {
                 setResponseHeaders(response);
                 setIncomingCharEncoding(request);
 
                 String action = request.getParameter("action");
 
                 try
                     {
 
                         if (action != null)
                             {
                                 if (action.equals("size"))
                                     {
                                         response.getWriter().write("size = " + Keys.size());
                                         return;
                                     }
                                 else if (action.equals("age"))
                                     {
                                         response.getWriter().write("age data: \n" + DEBUG_removeOldEntries());
                                         return;
                                     }
                             }
 
 
                         updateGraphDataStore();
 
                         String combinedGraphData_NEW = getCombinedGraphData();
 
                         response.getWriter().write(combinedGraphData_NEW);
 
 
 /*
                         String key = getStoredEntityKey_IfLessThan10minsOld(allGraphData, allGraphData.getProperties());
                         if (key == null)
                             {
                                 text = new Text(getGraphData());
 
                                 allGraphData.setProperty("" + new Date().getTime(), text);
                             }
                         else
                             {
                                 text = (Text) allGraphData.getProperty(key);
                             }
 
 
                                                     datastore.put(allGraphData);
 
                         removeOldEntries(allGraphData, allGraphData.getProperties());
 
                         String combinedGraphData = getCombinedGraphData(allGraphData, allGraphData.getProperties());
 
 //                        String s = text.getValue();
 
                         response.getWriter().write(combinedGraphData);
 
 */
 
 
 
                     }
                 catch (EntityNotFoundException e)
                     {
                         //log.error(e.getLocalizedMessage(), e);
                     }
 
 
             }
 
         private void updateGraphDataStore() throws IOException, EntityNotFoundException
             {
 
                 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 Key key = getStoredEntityKey_IfLessThan10minsOld();
 
                 if (key == null)
                     {
                         Text text = new Text(getGraphData());
 
                         Entity entity = new Entity("GraphData");
 
                         entity.setProperty("timestamp", new Date().getTime());
 
                         entity.setProperty("xml", text);
 
                         Key newKey = datastore.put(entity);
 
                         Keys.add(newKey);
                     }
 /*
                 else
                     {
                         Entity entity = datastore.get(key);
 
                         Text text = (Text) entity.getProperty("xml");
                     }
 */
 
                 removeOldEntries();
             }
 
         private String getGraphData() throws IOException
             {
                 URL url = new URL("http://emm.newsbrief.eu/NewsBrief/clustergraphs/new_en_chart.xml?r="+System.currentTimeMillis());
 
                 URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
                 HTTPResponse response = fetcher.fetch(url);
 
                 byte[] content = response.getContent();
 
                 String str = new String(content, "UTF-8");
 
 
                 // if redirects are followed, this returns the final URL we are redirected to
                 URL finalUrl = response.getFinalUrl();
 
                 // 200, 404, 500, etc
                 int responseCode = response.getResponseCode();
                 List headers = response.getHeaders();
 
                 return str;
             }
 
         private String getCombinedGraphData()
             {
                 final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 Collections.sort(Keys, new Comparator<Key>()
                 {
 
                     public int compare(Key key1, Key key2)
                         {
 
                             long L1 = 0;
                             long L2 = 0;
                             try
                                 {
                                     Entity entity1 = datastore.get(key1);
                                     Entity entity2 = datastore.get(key2);
 
                                     L1 = (Long) entity1.getProperty("timestamp");
                                     L2 = (Long) entity2.getProperty("timestamp");
                                 }
                             catch (EntityNotFoundException e)
                                 {
                                     //logger.error(e.getLocalizedMessage(), e);
                                 }
 
 
                             return (int) (L1 - L2);
                         }
                 });
 
 
                 HashMap<String, Node> datasets = new HashMap<String, Node>();
 
                 int total = Keys.size();
                 int count = 0;
 
                 for (Key key : Keys)
                     {
                         count++;
 
                         try
                             {
                                 Entity entity = datastore.get(key);
 
                                 Text xml = (Text) entity.getProperty("xml");
 
                                 String xmlString = xml.getValue();
 
                                 Document document = parseXml(xmlString);
 
                                 String expression = "/chart/dataset";
 
                                 XPathFactory factory = XPathFactory.newInstance();
                                 XPath xpath = factory.newXPath();
 
                                 try
                                     {
                                         NodeList datasetNodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
 
                                         NodeListIterator nodeListIterator = new NodeListIterator(datasetNodeList);
 
                                         if (count<total)
                                             {
                                                 while (nodeListIterator.hasNext())
                                                     {
                                                         Node node = nodeListIterator.next();
 
                                                         String seriesName = (String) xpath.evaluate("@seriesName", node, XPathConstants.STRING);
                                                         String color = (String) xpath.evaluate("@color", node, XPathConstants.STRING);
 
                                                         if (color.equals("FF0000"))
                                                             {
                                                                 ((Element) node).setAttribute("isOLD", "true");
                                                                 datasets.put(seriesName, node); //overides older ones
                                                             }
 
                                                     }
                                             }
                                         else
                                             {
                                                 while (nodeListIterator.hasNext())
                                                     {
                                                         Node node = nodeListIterator.next();
 
                                                         String seriesName = (String) xpath.evaluate("@seriesName", node, XPathConstants.STRING);
 
                                                         datasets.put(seriesName, node); //overides older ones
 
                                                     }
 
                                             }
 
                                     }
                                 catch (XPathExpressionException e)
                                     {
                                         //logger.error(e.getLocalizedMessage(), e);
                                     }
 
                             }
                         catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
 
 
                     }
 
                 Document document = null;
                 Element root = null;
 
                 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
                 DocumentBuilder builder = null;
 
                 try
                     {
                         builder = factory.newDocumentBuilder();
                         document = builder.newDocument();
 
                         root = (Element) document.createElement("chart");
 
                         root.setAttribute("caption", "Top 10 stories over 4 hours");
                         root.setAttribute("subCaption", "...");
                         root.setAttribute("yAxisName", "Articles Over Previous 4h");
 
                         document.appendChild(root);
 
                         for (Node node : datasets.values())
                                             {
                                                 Node importedNode = document.importNode(node, true);
                                                 root.appendChild(importedNode);
 
                                             }
 
                     } catch (ParserConfigurationException e)
                     {
                         //                                logger.error(e.getLocalizedMessage(), e);
                     }
 
 
 
 
                 return getStringFromDoc(document);  //To change body of created methods use File | Settings | File Templates.
             }
 
         private Key getStoredEntityKey_IfLessThan10minsOld()
             {
 
                 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 for (Key key : Keys)
                     {
 
                         try
                             {
                                 Entity entity = datastore.get(key);
 
                                 Long timestamp = (Long) entity.getProperty("timestamp");
 
                                 Date date = new Date();
                                 date.setTime(timestamp);
 
                                 Date now = new Date();
 
 //                                if (dateDiffInMinutes(now, date) < 2)
                         if (dateDiffInMinutes(now, date) < 5)
                                     {
                                         return key;
                                     }
 
                             }
                         catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
                     }
 
 
                 return null;
 
             }
 
         private void removeOldEntries()
             {
                 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 ArrayList<Key> Keys2Delete = new ArrayList<Key>();
 
                 for (Key key : Keys)
                     {
                         try
                             {
                                 Entity entity = datastore.get(key);
 
                                 Long timestamp = (Long) entity.getProperty("timestamp");
 
                                 Date date = new Date();
                                 date.setTime(timestamp);
 
                                 Date now = new Date();
 
                                 if (dateDiffInHours(now, date) > 2)
                                     {
                                         Keys2Delete.add(key);
                                     }
 
                             }
                         catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
 
                     }
 
                 datastore.delete(Keys2Delete);
 
             }
 
 
 
         //region DEBUG
         private String DEBUG_removeOldEntries()
             {
                 StringBuilder sb = new StringBuilder();
 
                 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 for (Key key : Keys)
                     {
                         try
                             {
                                 Entity entity = datastore.get(key);
 
                                 Long timestamp = (Long) entity.getProperty("timestamp");
 
                                 Date date = new Date();
                                 date.setTime(timestamp);
 
                                 Date now = new Date();
 
                                if (dateDiffInHours(now, date) >= 2)
                                     {
                                         sb.append("property will be deleted > 2 hours old (" +
                                                 dateDiffInHours(now, date) +
                                                 ")");
                                         sb.append("\n");
                                     }
                                 else
                                     {
                                         sb.append("property will be kept < 2 hours old (" +
                                                 dateDiffInHours(now, date) +
                                                 ")");
                                         sb.append("\n");
                                     }
 
                             }
                         catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
 
                     }
 
                 return sb.toString();
 
             }
         //endregion
 
 
         //region UTILS
         private void setResponseHeaders(HttpServletResponse response)
             {
                 response.setContentType("text/xml;charset=UTF-8");
                 response.setCharacterEncoding("UTF-8");
                 response.setHeader("Cache-Control", "no-cache");
             }
 
         protected void setIncomingCharEncoding(HttpServletRequest request) throws UnsupportedEncodingException
             {
                 if (request.getCharacterEncoding() == null)
                     {
                         request.setCharacterEncoding("UTF-8");
                     }
                 else
                     {
                         request.setCharacterEncoding(request.getCharacterEncoding());
                     }
             }
 
         public String getStringFromDoc(org.w3c.dom.Document doc)
             {
                 DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
                 LSSerializer lsSerializer = domImplementation.createLSSerializer();
                 return lsSerializer.writeToString(doc);
             }
 
         private static Document parseXml(String strXml)
             {
                 strXml = strXml.trim();
 
                 Document doc = null;
                 String strError;
                 try
                     {
                         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                         DocumentBuilder db = dbf.newDocumentBuilder();
 
 //                        strXml ="<a>永</a>";
 
                         InputStream is = new ByteArrayInputStream(strXml.getBytes("UTF-8"));
 
                         is = checkForUtf8BOM(is);
 
 //                        UnicodeReader reader = new UnicodeReader(is, "UTF-8");
 //                        InputSource inputSource = new InputSource(reader);
 
 
                         doc = db.parse(is);
 
                         Node item = doc.getDocumentElement().getChildNodes().item(0);
                         String nodeValue = item.getNodeValue();
                         System.out.println(nodeValue);
 
                         return doc;
                     } catch (IOException ioe)
                     {
                         strError = ioe.toString();
                     } catch (ParserConfigurationException pce)
                     {
                         strError = pce.toString();
                     } catch (SAXException se)
                     {
                         strError = se.toString();
                     } catch (Exception e)
                     {
                         strError = e.toString();
                     }
 
                 //log.severe("parseXml: " + strError);
                 return null;
             }
 
         private static InputStream checkForUtf8BOM(InputStream inputStream) throws IOException
             {
                 PushbackInputStream pushbackInputStream = new PushbackInputStream(new BufferedInputStream(inputStream), 3);
                 byte[] bom = new byte[3];
                 if (pushbackInputStream.read(bom) != -1)
                     {
                         if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF))
                             {
                                 pushbackInputStream.unread(bom);
                             }
                     }
                 return pushbackInputStream;
             }
 
         private static int dateDiffInHours(Date toDate, Date fromDate)
             {
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(toDate);
                 long ms = cal.getTimeInMillis();
                 cal.setTime(fromDate);
                 ms -= cal.getTimeInMillis();
                 final long msPerHour = 1000L * 60L * 60L;
                 int hours = (int) (ms / msPerHour);
                 return hours;
             }
 
         private static int dateDiffInMinutes(Date toDate, Date fromDate)
             {
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(toDate);
                 long ms = cal.getTimeInMillis();
                 cal.setTime(fromDate);
                 ms -= cal.getTimeInMillis();
                 final long msPerMinute = 1000L * 60L;
                 int minutes = (int) (ms / msPerMinute);
                 return minutes;
             }
         //endregion
 
 
         //.............
     }
