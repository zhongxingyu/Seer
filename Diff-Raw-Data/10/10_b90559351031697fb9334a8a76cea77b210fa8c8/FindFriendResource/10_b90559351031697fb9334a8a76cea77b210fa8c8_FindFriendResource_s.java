 /*
  * Copyright 2007 Wyona
  */
 
 package org.wyona.yanel.impl.resources.findfriend;
 
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.ViewableV2;
 import org.wyona.yanel.core.attributes.viewable.View;
 import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
 
 import org.wyona.meguni.parser.Parser;
 import org.wyona.meguni.util.ResultSet;
 
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 
 import java.io.File;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class FindFriendResource extends Resource implements ViewableV2 {
 
     private static Category log = Category.getInstance(FindFriendResource.class);
 
     /**
      *
      */
     public FindFriendResource() {
     }
 
     /**
      *
      */
     public boolean exists() {
         return true;
     }
 
     /**
      *
      */
     public String getMimeType(String viewId) {
         if (viewId != null && viewId.equals("source")) {
             return "application/xml";
         } else {
             return "application/xhtml+xml";
         }
     }
 
     /**
      *
      */
     public long getSize() {
         return -1;
     }
 
     /**
      *
      */
     public View getView(String viewId) throws Exception {
         StringBuffer sb = new StringBuffer("<foaf xmlns=\"http://www.wyona.org/foaf/1.0\">");
         String qs = getRequest().getParameter("q");
         if (qs != null) {
 
         // Results from Google, Facebook, ...
         try {
             ResultSet resultSet = getSearchResults(qs.replaceAll(" ", "+") + "+FOAF");
 
             if (resultSet != null && resultSet.size() > 0) {
                 sb.append("<provider source-name=\"" + resultSet.getSourceName() + "\" source-domain=\"" + resultSet.getSourceDomain() + "\" numberOfResults=\"" + resultSet.size() + "\">");
                 for (int k = 0;k < resultSet.size(); k++) {
                     java.net.URL url = resultSet.get(k).url;
                     if (url.toString().endsWith("rdf")) {
                         sb.append("<result number=\"" + (k+1) + "\" source-name=\"" + resultSet.getSourceName() + "\">");
                         sb.append("<title><![CDATA[" + resultSet.get(k).title + "]]></title>");
                         sb.append("<excerpt><![CDATA[" + resultSet.get(k).excerpt + "]]></excerpt>");
                         sb.append("<url><![CDATA[" + resultSet.get(k).url + "]]></url>");
                         sb.append("<last-modified><![CDATA[" + resultSet.get(k).lastModified + "]]></last-modified>");
                         sb.append("<mime-type suffix=\"rdf\">application/rdf+xml</mime-type>");
                         sb.append("</result>");
                     } else {
                         log.warn("DEBUG: Does not seem to be a RDF: " + url);
                     }
                 }
                 sb.append("</provider>");
             }
         } catch (Exception e) {
             sb.append("<exception>" + e.getMessage() + "</exception>");
         }
 
         // Results from Wyona-FOAF
         // TODO: Remove hard-coded ...
             sb.append("<provider source-name=\"" + "Wyona-FOAF" + "\" source-domain=\"" + "http://foaf.wyona.org" + "\" numberOfResults=\"" + "1" + "\">");
 
             Repository pRepo = getProfilesRepository();
             Node[] pNodes = pRepo.search(qs);
             //Node[] pNodes = pRepo.getNode("/profiles").getNodes();
             if (pNodes != null) {
             for (int i = 0; i < pNodes.length; i++) {
                 sb.append("<result number=\"" + "1" + "\" source-name=\"" + "Wyona-FOAF" + "\">");
                 sb.append("<title><![CDATA[" + "Foo Bar" + "]]></title>");
                 sb.append("<excerpt><![CDATA[" + "About Foo Bar ..." + "]]></excerpt>");
                 sb.append("<url><![CDATA[" + "profiles/" + withoutSuffix(pNodes[i].getName()) + ".html" + "]]></url>");
                 sb.append("<last-modified><![CDATA[" + "null" + "]]></last-modified>");
                 sb.append("<mime-type suffix=\"html\">application/xhtml+xml</mime-type>");
                 sb.append("</result>");
             }
             } else {
                 sb.append("<no-results/>");
             }
 
 /*
             Nodes[] nodes = getRealm().getRepository().searchProperty("firstname", firstname);
             Nodes[] nodes = getRealm().getRepository().searchProperty("lastname", lastname);
 */
 
             sb.append("</provider>");
         }
 
         sb.append("</foaf>");
 
         View view = new View();
         if (viewId != null && viewId.equals("source")) {
             view.setInputStream(new java.io.StringBufferInputStream(sb.toString()));
         } else {
             StreamSource source = new StreamSource(new java.io.StringBufferInputStream(sb.toString()));
             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(); 
             getTransformer().transform(source, new StreamResult(baos));
             view.setInputStream(new java.io.ByteArrayInputStream(baos.toByteArray()));
         }
         view.setMimeType(getMimeType(viewId));
         return view;
     }
 
     /**
      *
      */
     public ViewDescriptor[] getViewDescriptors() {
         return null;
     }
 
     /**
      *
      */
     private ResultSet getSearchResults(String queryString) throws Exception {
         String className = getResourceConfigProperty("parser");
         if (className == null) className = "org.wyona.meguni.parser.impl.MSNParser";
         Parser parser = (Parser) Class.forName(className).newInstance();
         return parser.parse(queryString);
     }
 
     /**
      *
      */
     private Transformer getTransformer() throws Exception {
         File xsltFile = org.wyona.commons.io.FileUtil.file(rtd.getConfigFile().getParentFile().getAbsolutePath(), "xslt" + File.separator + "foaf2xhtml.xsl");
         Transformer tf = TransformerFactory.newInstance().newTransformer(new StreamSource(xsltFile));
         if (getRequest().getParameter("q") != null) {
             tf.setParameter("query", getRequest().getParameter("q"));
         }
         if (getResourceConfigProperty("type") != null) {
             tf.setParameter("type", getResourceConfigProperty("type"));
         } else {
             tf.setParameter("type", "simple");
         }
         return tf;
     }
 
     /**
      *
      */
     private Repository getProfilesRepository() throws Exception {
        Repository repo = getRealm().getRepository();
        String repoConfig = getResourceConfigProperty("repo-config");
        if (repoConfig != null) {
            repo = new RepositoryFactory().newRepository("profiles", new File(repoConfig));
        }
        return repo;
     }
 
     /**
      *
      */
     private String withoutSuffix(String name) {
         return name.substring(0, name.lastIndexOf("."));
     }
 }
