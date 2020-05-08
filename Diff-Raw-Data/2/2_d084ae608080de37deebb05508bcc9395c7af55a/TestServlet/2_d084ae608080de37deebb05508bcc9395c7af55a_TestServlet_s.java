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
 import com.google.appengine.api.datastore.Query.SortDirection;
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
 
         private static ArrayList<Key> Keys = null;
         private static ArrayList<Key> Keys24 = null;
         private static HashMap<String, Node> lastReturnedDataSets;
         private HashMap<Key, Entity> KeysAndEntities24;
         private HashMap<Key, Entity> KeysAndEntities;
 
         @Override
         public void init() throws ServletException
             {
                 super.init();
 
                 if (Keys == null)
                     {
                         Keys = new ArrayList<Key>();
                         KeysAndEntities = new HashMap<Key, Entity>();
 
                         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                         Query q = new Query("GraphData")
                                 .addSort("timestamp", SortDirection.ASCENDING)
                                 .setKeysOnly();
                         Query q2 = new Query("GraphData")
                                 .addSort("timestamp", SortDirection.ASCENDING);
 
                         PreparedQuery pq = datastore.prepare(q);
                         PreparedQuery pq2 = datastore.prepare(q2);
 
                         List<Entity> entities = pq.asList(FetchOptions.Builder.withDefaults());
                         List<Entity> entities2 = pq2.asList(FetchOptions.Builder.withDefaults());
 
                         for (Entity entity : entities)
                             {
                                 Key key = entity.getKey();
 
                                 Keys.add(key);
                             }
                         for (Entity entity : entities2)
                             {
                                 Key key = entity.getKey();
 
                                 KeysAndEntities.put(key, entity);
                             }
 
 
                     }
 
                 if (Keys24 == null)
                     {
                         Keys24 = new ArrayList<Key>();
                         KeysAndEntities24 = new HashMap<Key, Entity>();
 
                         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                         Query q_Keys = new Query("GraphData24")
                                 .addSort("timestamp", SortDirection.ASCENDING)
                                 .setKeysOnly();
 
                         Query q_Entities = new Query("GraphData24")
                                 .addSort("timestamp", SortDirection.ASCENDING);
 
                         PreparedQuery pq_Keys = datastore.prepare(q_Keys);
                         PreparedQuery pq_Entities = datastore.prepare(q_Entities);
 
                         List<Entity> entity_Keys = pq_Keys.asList(FetchOptions.Builder.withDefaults());
                         List<Entity> entities = pq_Entities.asList(FetchOptions.Builder.withDefaults());
 
                         for (Entity entity : entity_Keys)
                             {
                                 Key key = entity.getKey();
 
                                 Keys24.add(key);
                             }
 
                         for (Entity entity : entities)
                             {
                                 Key key = entity.getKey();
 
                                 KeysAndEntities24.put(key, entity);
                             }
 
 
                     }
 
             }
 
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
                                 else if (action.equals("datasets"))
                                     {
                                         response.getWriter().write("datasets: \n" + DEBUG_getDatasetsTot());
                                         return;
                                     }
                             }
 
 
                         updateGraphDataStore();
                         update24GraphDataStore();
 
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
                         KeysAndEntities.put(newKey, entity);
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
 
         private void update24GraphDataStore() throws IOException, EntityNotFoundException
             {
 
                 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 Key key = getStoredEntityKey_IfLessThan6hrsOld();
 
                 if (key == null)
                     {
                         Text text = new Text(get24GraphData());
 
                         Entity entity = new Entity("GraphData24");
 
                         entity.setProperty("timestamp", new Date().getTime());
 
                         entity.setProperty("xml", text);
 
                         Key newKey = datastore.put(entity);
 
                         Keys24.add(newKey);
                         KeysAndEntities24.put(newKey, entity);
                     }
 
                 removeOld_24Entries();
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
 
         private String get24GraphData() throws IOException
             {
                 URL url = new URL("http://emm.newsbrief.eu/NewsBrief/clustergraphs/new_en_24hrs_bigest_chart.xml?r="+System.currentTimeMillis());
 
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
 
                 LinkedHashMap<String, Node> datasets = new LinkedHashMap<String, Node>();
                 XPathFactory factory = XPathFactory.newInstance();
 
 
                 /*- add 24 hr data -*/
 
                 try
                     {
                         for (Key key24 : Keys24)
                             {
                                 Entity entity24 = KeysAndEntities24.get(key24);
 //                                Entity entity24 = datastore.get(key24);
 
                                 Boolean isRedeuced = (Boolean) entity24.getProperty("isRedeuced");
 
                                 HashMap<String, Node> datasets24 = null;
 
                                 if ((isRedeuced != null ) && isRedeuced)
                                     {
                                         Text xml24 = (Text) entity24.getProperty("xml");
 
                                         String xmlString24 = xml24.getValue();
 
                                         Document document24 = parseXml_utf16(xmlString24);
 
                                         String expression24 = "/chart/dataset";
 
                                         XPath xpath24 = factory.newXPath();
 
                                         NodeList datasetNodeList24 = (NodeList) xpath24.evaluate(expression24, document24, XPathConstants.NODESET);
 
                                         NodeListIterator nodeListIterator24 = new NodeListIterator(datasetNodeList24);
 
                                         datasets24 = getAll(xpath24, nodeListIterator24);
                                     }
                                 else
                                     {
                                         Text xml24 = (Text) entity24.getProperty("xml");
 
                                         String xmlString24 = xml24.getValue();
 
                                         Document document24 = parseXml_utf8(xmlString24);
 
                                         String expression24 = "/chart/dataset";
 
                                         XPath xpath24 = factory.newXPath();
 
                                         NodeList datasetNodeList24 = (NodeList) xpath24.evaluate(expression24, document24, XPathConstants.NODESET);
 
                                         NodeListIterator nodeListIterator24 = new NodeListIterator(datasetNodeList24);
 
                                         datasets24 = getTop3(entity24, xpath24, nodeListIterator24);
 
                                         datastore.put(entity24);
                                     }
 
 
                                 datasets.putAll(datasets24);
 
 
                             }
                     }
                 catch (Exception e)
 //                catch (EntityNotFoundException e)
                     {
 //                        logger.error(e.getLocalizedMessage(), e);
                     }
 /*
                 catch (XPathExpressionException e)
                     {
 //                        logger.error(e.getLocalizedMessage(), e);
                     }
 */
 
 
                 /*- eo - add 24 hr data -*/
 
                 /*- add current top story data -*/
 
 
                 Collections.sort(Keys, new Comparator<Key>()
                 {
 
                     public int compare(Key key1, Key key2)
                         {
 
                             long L1 = 0;
                             long L2 = 0;
                             try
                                 {
                                     Entity entity1 = KeysAndEntities.get(key1);
                                     Entity entity2 = KeysAndEntities.get(key2);
 //                                    Entity entity1 = datastore.get(key1);
 //                                    Entity entity2 = datastore.get(key2);
 
                                     L1 = (Long) entity1.getProperty("timestamp");
                                     L2 = (Long) entity2.getProperty("timestamp");
                                 }
                             catch (Exception e)
 //                            catch (EntityNotFoundException e)
                                 {
                                     //logger.error(e.getLocalizedMessage(), e);
                                 }
 
 
                             return (int) (L1 - L2);
                         }
                 });
 
 
                 int total = Keys.size();
                 int count = 0;
 
 
                 for (Key key : Keys)
                     {
                         count++;
 
                         try
                             {
                                 Entity entity = KeysAndEntities.get(key);
 //                                Entity entity = datastore.get(key);
 
                                 Text xml = (Text) entity.getProperty("xml");
 
                                 String xmlString = xml.getValue();
 
                                 Document document = parseXml_utf8(xmlString);
 
                                 String expression = "/chart/dataset";
 
                                 XPath xpath = factory.newXPath();
 
                                 try
                                     {
                                         NodeList datasetNodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
 
                                         NodeListIterator nodeListIterator = new NodeListIterator(datasetNodeList);
 
                                         if (count<total)
                                             {
 /*
                                                 while (nodeListIterator.hasNext())
                                                     {
                                                         Node node = nodeListIterator.next();
 
                                                         String seriesName = (String) xpath.evaluate("@seriesName", node, XPathConstants.STRING);
                                                         String color = (String) xpath.evaluate("@color", node, XPathConstants.STRING);
 
                                                         if ((color.equals("FF0000"))||(color.equals("FF8888")))
                                                             {
                                                                 ((Element) node).setAttribute("isOLD", "true");
                                                                 ((Element) node).setAttribute("color", "FF8888");
                                                                 datasets.put(seriesName, node); //overides older ones
                                                             }
 
                                                     }
 */
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
                         catch (Exception e)
 //                        catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
 
 
                     }
 
 
                 Document document = null;
                 Element root = null;
 
                 DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 
                 DocumentBuilder builder = null;
 
                 try
                     {
                         builder = docFactory.newDocumentBuilder();
                         document = builder.newDocument();
 
                         root = (Element) document.createElement("chart");
 
                         root.setAttribute("caption", "Top Stories");
                         root.setAttribute("subCaption", "Navigateable Graph (Beta)");
                         root.setAttribute("yAxisName", "Articles Over Previous 4h");
 
                         document.appendChild(root);
 
                         for (Node node : datasets.values())
                                             {
                                                 Node importedNode = document.importNode(node, true);
                                                 root.appendChild(importedNode);
 
                                             }
 
                     }
                 catch (ParserConfigurationException e)
                     {
                         //                                logger.error(e.getLocalizedMessage(), e);
                     }
 
 
                 lastReturnedDataSets = datasets;
 
                 return getStringFromDoc(document);  //To change body of created methods use File | Settings | File Templates.
             }
 
         private HashMap<String, Node> getTop3(Entity entity24, XPath xpath24, NodeListIterator nodeListIterator24) throws XPathExpressionException
             {
                 String expression24set = "set";
 
                 HashMap<String, Node> datasets24 = new HashMap<String, Node>();
                 HashMap<String, Node> datasets24_top = new HashMap<String, Node>();
 
                 ArrayList<NodeObject> sortedList = new ArrayList<NodeObject>();
 
                 while (nodeListIterator24.hasNext())
                     {
                         Node node24 = nodeListIterator24.next();
 
                         NodeList sets = (NodeList) xpath24.evaluate(expression24set, node24, XPathConstants.NODESET);
 
                         NodeListIterator nodeListIteratorSets = new NodeListIterator(sets);
 
                         int totalArea = 0;
 
                         while (nodeListIteratorSets.hasNext())
                             {
                                 Node set_node = nodeListIteratorSets.next();
 
                                 Double value = (Double) xpath24.evaluate("@value", set_node, XPathConstants.NUMBER);
 
                                 totalArea += value;
                             }
 
                         String seriesName = (String) xpath24.evaluate("@seriesName", node24, XPathConstants.STRING);
 
                         ((Element) node24).setAttribute("totalArea", String.valueOf(totalArea));
 
                         ((Element) node24).setAttribute("is24", "true");
 
                         datasets24.put("(24-hr-OV) - " + seriesName, node24);
 
                         NodeObject nodeObject = new NodeObject(seriesName, totalArea, node24);
 
                         sortedList.add(nodeObject);
 
                     }
 
                 Collections.sort(sortedList, new Comparator<NodeObject>()
                 {
 
                     public int compare(NodeObject key1, NodeObject key2)
                         {
                             long L1 = key1.totalArea;
                             long L2 = key2.totalArea;
 
                             return (int) (L2 - L1);
                         }
                 });
 
                 int top = 3;  //only want the top 3
                 int count = 0;
 
                 for (NodeObject nodeObject : sortedList)
                     {
                         if (count < top)
                             {
                                 datasets24_top.put(nodeObject.seriesName, nodeObject.node);
                             }
 
                         count++;
                     }
 
 
                 /*--*/
 
                 Document document = null;
                 Element root = null;
 
                 DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 
                 DocumentBuilder builder = null;
 
                 try
                     {
                         builder = docFactory.newDocumentBuilder();
                         document = builder.newDocument();
 
                         root = (Element) document.createElement("chart");
 
                         root.setAttribute("caption", "Top 10 stories over 4 hours");
                         root.setAttribute("subCaption", "...");
                         root.setAttribute("yAxisName", "Articles Over Previous 4h");
 
                         document.appendChild(root);
 
                         for (Node node : datasets24_top.values())
                             {
                                 Node importedNode = document.importNode(node, true);
                                 root.appendChild(importedNode);
                             }
 
                         String stringFromDoc = getStringFromDoc(document);
 
                         entity24.setProperty("isRedeuced", true);
                         entity24.setProperty("xml", new Text(stringFromDoc));
                     }
                 catch (ParserConfigurationException e)
                     {
                         System.out.println(e.getLocalizedMessage());
                         //                                logger.error(e.getLocalizedMessage(), e);
                     }
 
 
                 return datasets24_top;
 //                return datasets24;
             }
 
         private HashMap<String, Node> getAll(XPath xpath24, NodeListIterator nodeListIterator24) throws XPathExpressionException
             {
 
                 HashMap<String, Node> datasets24 = new HashMap<String, Node>();
 
                 while (nodeListIterator24.hasNext())
                     {
                         Node node24 = nodeListIterator24.next();
 
                         String seriesName = (String) xpath24.evaluate("@seriesName", node24, XPathConstants.STRING);
 
                         datasets24.put("(24-hr-OV) - " + seriesName, node24);
 
                     }
 
                 return datasets24;
             }
 
         private Key getStoredEntityKey_IfLessThan10minsOld()
             {
 
                 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 for (Key key : Keys)
                     {
 
                         try
                             {
                                 Entity entity = KeysAndEntities.get(key);
 //                                Entity entity = datastore.get(key);
 
                                 Long timestamp = (Long) entity.getProperty("timestamp");
 
                                 Date date = new Date();
                                 date.setTime(timestamp);
 
                                 Date now = new Date();
 
 //                                if (dateDiffInMinutes(now, date) < 2)
 //                        if (dateDiffInMinutes(now, date) <= 10)
                         if (dateDiffInMinutes(now, date) < 9)
                                     {
                                         return key;
                                     }
 
                             }
                         catch (Exception e)
 //                        catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
                     }
 
 
                 return null;
 
             }
 
         private Key getStoredEntityKey_IfLessThan6hrsOld()
             {
 
                 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 for (Key key : Keys24)
                     {
 
                         try
                             {
                                 Entity entity = KeysAndEntities24.get(key);
 //                                Entity entity = datastore.get(key);
 
                                 Long timestamp = (Long) entity.getProperty("timestamp");
 
                                 Date date = new Date();
                                 date.setTime(timestamp);
 
                                 Date now = new Date();
 
 //                                if (dateDiffInMinutes(now, date) < 2)
                         if (dateDiffInHours(now, date) < 6)
                                     {
                                         return key;
                                     }
 
                             }
 
                         catch (Exception e)
 //                        catch (EntityNotFoundException e)
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
                                 Entity entity = KeysAndEntities.get(key);
 //                                Entity entity = datastore.get(key);
 
                                 Long timestamp = (Long) entity.getProperty("timestamp");
 
                                 Date date = new Date();
                                 date.setTime(timestamp);
 
                                 Date now = new Date();
 
                                 int ageInHours = dateDiffInHours(now, date);
 
                                 if (ageInHours > 6)
                                     {
                                         Keys2Delete.add(key);
                                     }
 
                             }
                         catch (Exception e)
 //                        catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
 
                     }
 
                 Keys.removeAll(Keys2Delete);
                 for (Key key : Keys2Delete)
                     {
                         KeysAndEntities.remove(key);
                     }
                 datastore.delete(Keys2Delete);
 
             }
 
         private void removeOld_24Entries()
             {
                 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
                 ArrayList<Key> Keys2Delete = new ArrayList<Key>();
 
                 for (Key key : Keys24)
                     {
                         try
                             {
                                 Entity entity = KeysAndEntities24.get(key);
 //                                Entity entity = datastore.get(key);
 
                                 Long timestamp = (Long) entity.getProperty("timestamp");
 
                                 Date date = new Date();
                                 date.setTime(timestamp);
 
                                 Date now = new Date();
 
                                 int ageInHours = dateDiffInHours(now, date);
 
                                if (ageInHours > 24*7)
                                     {
                                         Keys2Delete.add(key);
                                     }
 
                             }
                         catch (Exception e)
 //                        catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
 
                     }
 
                 Keys24.removeAll(Keys2Delete);
                 for (Key key : Keys2Delete)
                     {
                         KeysAndEntities24.remove(key);
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
                                 Entity entity = KeysAndEntities.get(key);
 //                                Entity entity = datastore.get(key);
 
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
                         catch (Exception e)
 //                        catch (EntityNotFoundException e)
                             {
                                 //logger.error(e.getLocalizedMessage(), e);
                             }
 
                     }
 
                 return sb.toString();
 
             }
 
 
 
         private String DEBUG_getDatasetsTot()
             {
                 if (lastReturnedDataSets == null)
                     {
                         return "lastReturnedDataSets == null";
                     }
                 else
                     {
                         int count = 0;
                         StringBuilder sb = new StringBuilder();
                         for (String key : lastReturnedDataSets.keySet())
                             {
                                 Node node = lastReturnedDataSets.get(key);
 
                                 sb.append("(" + (++count) + ") ").append(key);
                                 sb.append(", points = ").append(node.getChildNodes().getLength()).append("\n");
 
                             }
                         return "lastReturnedDataSets.size() = " + lastReturnedDataSets.size() + "\n" + sb.toString();
                     }
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
 
         private static Document parseXml_utf8(String strXml)
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
 
         private static Document parseXml_utf16(String strXml)
             {
                 strXml = strXml.trim();
 
                 Document doc = null;
                 String strError;
                 try
                     {
                         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                         DocumentBuilder db = dbf.newDocumentBuilder();
 
 //                        strXml ="<a>永</a>";
 
 //                        InputStream is = new ByteArrayInputStream(strXml.getBytes());
                         InputStream is = new ByteArrayInputStream(strXml.getBytes("UTF-16"));
 
 //                        is = checkForUtf8BOM(is);
 //                        is = checkForUtf8BOM(is);
 
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
 
         private class NodeObject
             {
                 public String seriesName;
                 public int totalArea;
                 public Node node;
 
                 public NodeObject(String seriesName, int totalArea, Node node)
                     {
                         this.seriesName = seriesName;
                         this.totalArea = totalArea;
                         this.node = node;
                     }
             }
         //endregion
 
 
         //.............
     }
