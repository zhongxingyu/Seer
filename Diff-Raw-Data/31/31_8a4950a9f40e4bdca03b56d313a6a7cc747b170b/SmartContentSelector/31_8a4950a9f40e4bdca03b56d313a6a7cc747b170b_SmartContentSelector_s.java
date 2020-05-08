 package us.codecraft.spider.selector;
 
 import com.google.common.util.concurrent.AtomicDouble;
 import org.apache.log4j.Logger;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 
 import java.util.*;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * 找到clear
  * User: cairne
  * Date: 13-4-21
  * Time: 下午4:42
  */
 public class SmartContentSelector implements Selector {
 
     private Logger logger = Logger.getLogger(getClass());
 
     public SmartContentSelector() {
     }
 
     @Override
     public String select(String text) {
         HtmlCleaner htmlCleaner = new HtmlCleaner();
         TagNode tagNode = htmlCleaner.clean(text);
         if (tagNode == null) {
             return null;
         }
         TagNode[] nodes = tagNode.getElementsByName("p", true);
         TagNode[] pres = tagNode.getElementsByName("pre", true);
         Map<TagNode, AtomicDouble> pDensityCountMap = new HashMap<TagNode, AtomicDouble>();
         countPdensity(nodes, pDensityCountMap);
         countPdensity(pres, pDensityCountMap);
         for (TagNode pre : pres) {
             addCounter(pre, pDensityCountMap, 2);
         }
         List<Map.Entry<TagNode, AtomicDouble>> sortList = new ArrayList<Map.Entry<TagNode, AtomicDouble>>();
         if (pDensityCountMap.size() == 0) {
             return null;
         }
         for (Map.Entry<TagNode, AtomicDouble> entry : pDensityCountMap.entrySet()) {
             if (logger.isDebugEnabled()) {
                 logger.debug("p\t" + entry.getKey().getName() + "#" + entry.getKey().getAttributeByName("id") +
                         "@" + entry.getKey().getAttributeByName("class") + ":" + entry.getValue().get());
             }
             sortList.add(entry);
         }
 
         Collections.sort(sortList, new Comparator<Map.Entry<TagNode, AtomicDouble>>() {
             @Override
             public int compare(Map.Entry<TagNode, AtomicDouble> o1, Map.Entry<TagNode, AtomicDouble> o2) {
                 Double d1 = o1.getValue().get();
                 Double d2 = o2.getValue().get();
                 return -d1.compareTo(d2);
             }
         });
         TagNode contentNode = sortList.get(0).getKey();
        System.out.println(contentNode.getName() + "@" + contentNode.getAttributeByName("class"));
         return htmlCleaner.getInnerHtml(contentNode);
     }
 
     private void addCounter(TagNode node, Map<TagNode, AtomicDouble> countMap, double delta) {
         AtomicDouble counter = countMap.get(node);
         if (counter == null) {
             counter = new AtomicDouble(delta);
             countMap.put(node, counter);
         } else {
             counter.addAndGet(delta);
         }
     }
 
     private static final double parentWeight = 0.7;
 
     private void countPdensity(TagNode[] nodes, Map<TagNode, AtomicDouble> pDensityCountMap) {
         for (TagNode node : nodes) {
            if (node==null){
                 continue;
             }
             TagNode parent = node.getParent();
             double pDensity = 1;
             while (parent != null) {
                 addCounter(parent, pDensityCountMap, pDensity);
                 parent = parent.getParent();
                 pDensity = pDensity * parentWeight;
             }
         }
     }
 
     private TagNode findLowestCommonParent(List<TagNode> tagNodes, int maxMargin, Map<TagNode, AtomicInteger> countMap) {
         TagNode contentNode = tagNodes.get(0);
         return contentNode;
     }
 
     @Override
     public List<String> selectList(String text) {
         throw new UnsupportedOperationException();
     }
 }
