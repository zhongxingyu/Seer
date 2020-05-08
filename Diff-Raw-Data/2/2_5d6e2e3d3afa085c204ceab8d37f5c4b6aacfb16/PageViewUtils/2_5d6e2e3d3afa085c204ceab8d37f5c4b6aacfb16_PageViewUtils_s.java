 package com.whysearchtwice.container;
 
 import java.util.Iterator;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.thinkaurelius.titan.core.TitanGraph;
 import com.tinkerpop.frames.FramedGraph;
 import com.whysearchtwice.frames.PageView;
 
 public final class PageViewUtils {
     private enum Property {
         type {
             @Override
             public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager) {
                 pv.setType((String) value);
             }
         },
         pageUrl {
             @Override
             public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager) {
                 pv.setPageUrl((String) value);
             }
         },
         pageOpenTime {
             @Override
             public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager) {
                 pv.setPageOpenTime((Long) value);
             }
         },
         pageCloseTime {
             @Override
             public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager) {
                 pv.setPageCloseTime((Long) value);
             }
         },
         tabId {
             @Override
             public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager) {
                 pv.setTabId((Integer) value);
             }
         },
         windowId {
             @Override
             public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager) {
                 pv.setWindowId((Integer) value);
             }
         },
         parentId {
             @Override
             public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager) {
                 PageView parent = manager.getVertex((String) value, PageView.class);
                 pv.addParent(parent);
             }
         },
         predecessorId {
             @Override
             public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager) {
                 PageView predecessor = manager.getVertex((String) value, PageView.class);
                pv.addParent(predecessor);
             }
         };
 
         abstract public void store(PageView pv, Object value, FramedGraph<TitanGraph> manager);
     }
 
     private PageViewUtils() {
     }
 
     /**
      * Export the contents of the PageView into a JSON Object for sending back
      * to the client.
      * 
      * @return JSONObject
      * @throws JSONException
      */
     public static JSONObject asJSON(PageView pv) throws JSONException {
         JSONObject json = new JSONObject();
 
         json.put("tabId", pv.getTabId());
         json.put("windowId", pv.getWindowId());
         json.put("pageOpenTime", pv.getPageOpenTime());
         json.put("pageCloseTime", pv.getPageCloseTime());
         json.put("pageUrl", pv.getPageUrl());
         json.put("type", pv.getType());
         json.put("id", pv.asVertex().getId());
 
         for (PageView parent : pv.getParents()) {
             // This iterator should only have one item
             json.put("parentId", parent.asVertex().getId());
         }
 
         for (PageView predecessor : pv.getPredecessors()) {
             // This iterator should only have one item
             json.put("predecessorId", predecessor.asVertex().getId());
         }
 
         return json;
     }
 
     /**
      * Add the attributes from a JSONObject to a PageView
      * 
      * @param pv
      *            PageView which should have attributes set on
      * @param attributes
      *            JSONObject to retrieve attributes from
      */
     public static void populatePageView(PageView pv, FramedGraph<TitanGraph> manager, JSONObject attributes) {
         @SuppressWarnings("rawtypes")
         Iterator keysIter = attributes.keys();
         while (keysIter.hasNext()) {
             String key = (String) keysIter.next();
 
             try {
                 Property.valueOf(key).store(pv, attributes.get(key), manager);
             } catch (JSONException e) {
                 // The key is from the keys list and will always be there
             } catch (IllegalArgumentException e) {
                 // Any key not in the enum should be ignored
             }
         }
     }
 
     public static boolean inTimeRange(PageView pv, long searchTime, int timeRange) {
         long pageOpenTime = pv.getPageOpenTime();
 
         boolean closeTimeInRange = false;
         if (pv.getPageCloseTime() != null) {
             long pageCloseTime = pv.getPageCloseTime();
             closeTimeInRange = searchTime - timeRange < pageCloseTime && searchTime + timeRange > pageCloseTime;
         }
 
         return closeTimeInRange || (searchTime - timeRange < pageOpenTime && searchTime + timeRange > pageOpenTime);
     }
 }
