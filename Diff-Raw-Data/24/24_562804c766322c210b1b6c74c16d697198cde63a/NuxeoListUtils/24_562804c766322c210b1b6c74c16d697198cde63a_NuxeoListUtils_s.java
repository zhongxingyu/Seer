 package org.easysoa.registry.utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 
 public class NuxeoListUtils {
     
     /** to help List.toArray() */
     public static final String[] EMPTY_STRING_ARRAY = new String[0];
 
     public static ArrayList<DocumentModel> list(DocumentModel...objs) {
         ArrayList<DocumentModel> list = new ArrayList<DocumentModel>(objs.length);
         for (DocumentModel obj : objs) {
             list.add(obj);
         }
         return list;
     }
     
     public static String toCsv(Collection<String> values) throws ClientException {
         if (values == null || values.isEmpty()) {
             return "";
         }
         Iterator<String> valuesIt = values.iterator();
         StringBuffer valuesSbuf = new StringBuffer("'"); // NB. '\'' doesn't work, counts as int !!
         valuesSbuf.append(valuesIt.next());
         valuesSbuf.append('\'');
         while (valuesIt.hasNext()) {
             valuesSbuf.append(", '");
             valuesSbuf.append(valuesIt.next());
             valuesSbuf.append('\'');
         }
         return valuesSbuf.toString();
     }
     
     public static String toLiteral(Collection<String> values) throws ClientException {
         return '(' + toCsv(values) + ')';
     }
 
     /**
      * Does not use an intermediary set
      * @param session
      * @param proxies
      * @return
      * @throws ClientException
      */
     public static String getProxiedIdLiteralList(CoreSession session, List<DocumentModel> proxies) throws ClientException {
         return toLiteral(getProxiedIds(session, proxies));
     }
 
     public static ArrayList<String> getProxiedIds(CoreSession session, List<DocumentModel> proxies) throws ClientException {
         ArrayList<String> proxiedIds = new ArrayList<String>();
         for (DocumentModel proxy : proxies) {
             proxiedIds.add(session.getSourceDocument(proxy.getRef()).getId());
         }
         return proxiedIds;
     }
 
     public static ArrayList<String> getProxyIds(CoreSession session, List<DocumentModel> docs, DocumentRef root) throws ClientException {
         ArrayList<String> proxyIds = new ArrayList<String>();
         for (DocumentModel doc : docs) {
             for (DocumentModel proxy : session.getProxies(doc.getRef(), root)) {
                 proxyIds.add(proxy.getId());
             }
         }
         return proxyIds;
     }
 
     public static ArrayList<String> getProxyIds(CoreSession session, Collection<String> docIds, DocumentRef root) throws ClientException {
         ArrayList<String> proxyIds = new ArrayList<String>();
         for (String docId : docIds) {
             for (DocumentModel proxy : session.getProxies(new IdRef(docId), root)) {
                 proxyIds.add(proxy.getId());
             }
         }
         return proxyIds;
     }
     
     public static ArrayList<String> getParentIds(List<DocumentModel> docs) throws ClientException {
         ArrayList<String> parentIds = new ArrayList<String>();
         for (DocumentModel doc : docs) {
             parentIds.add(doc.getParentRef().reference().toString());
         }
         return parentIds;
     }
     
     public static ArrayList<String> getIds(List<DocumentModel> docModels) throws ClientException {
         ArrayList<String> ids = new ArrayList<String>(docModels.size());
         for (DocumentModel doc : docModels) {
             ids.add(doc.getId());
         }
         return ids;
     }
     
 }
