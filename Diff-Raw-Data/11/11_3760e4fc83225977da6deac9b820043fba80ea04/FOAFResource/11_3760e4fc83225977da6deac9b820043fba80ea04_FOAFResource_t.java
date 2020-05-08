 /*
  * Copyright 2007 Wyona
  */
 
 package org.wyona.yanel.impl.resources.foaf;
 
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.IntrospectableV1;
 import org.wyona.yanel.core.api.attributes.ModifiableV2;
 import org.wyona.yanel.core.api.attributes.ViewableV2;
 import org.wyona.yanel.core.attributes.viewable.View;
 import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
 import org.wyona.yanel.impl.resources.BasicXMLResource;
 import org.wyona.yanel.impl.resources.xml.ConfigurableViewDescriptor;
 
 import org.apache.log4j.Category;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringBufferInputStream;
 import java.net.URL;
 
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 /**
  *
  */
 public class FOAFResource extends BasicXMLResource implements IntrospectableV1, ModifiableV2, ViewableV2 {
 //public class FOAFResource extends Resource implements ViewableV2 {
 
     private static Category log = Category.getInstance(FOAFResource.class);
 
     /**
      *
      */
     public FOAFResource() {
     }
 
     /**
      *
      */
     public boolean exists() {
         log.error("Implementation not finished yet! Needs to check existence of local or remote FOAF.");
         return true;
     }
 
     /**
      *
      */
     public String getMimeType(String viewId) {
         if (viewId !=  null && viewId.equals("source")) {
             return "application/xml";
         } else if (viewId !=  null && viewId.equals("rdf+xml")) {
             return "application/rdf+xml";
         } else if (viewId !=  null && viewId.equals("atom")) {
             return "application/atom+xml";
         } else {
             return "application/xhtml+xml";
         }
     }
 
     /**
      *
      */
     public long getSize() {
         log.error("Implementation not done yet!");
         return -1;
     }
 
     /**
      *
      */
     public View getView(String viewId) {
         View view = new View();
         String path = getPath();
 
 
         try {
 	    if (path.lastIndexOf(".rdf") == path.length() - 4) {
                 view.setInputStream(getRDFAsInputStream());
                 view.setMimeType(getMimeType("rdf+xml"));
                 return view;
             }
 
             StringBuffer sb = new StringBuffer("<?xml version=\"1.0\"?>");
 // WORKAROUND: The XIncludeTransformer does to propagate the namespaces ...
             sb.append("<wyona:foaf xmlns:wyona=\"http://www.wyona.org/foaf/1.0\" xmlns:admin=\"http://webns.net/mvcb/\" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:i18n=\"http://www.wyona.org/yanel/i18n/1.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
             if (getRequest().getParameter("href") != null) {
                 sb.append("<wyona:third-party-source href=\"" + getRequest().getParameter("href") + "\"/>");
             } else {
                 sb.append("<wyona:source href=\"" + path.substring(0, path.lastIndexOf(".html")) + ".rdf\"/>");
             }
 /*
             // TODO: The following leads to errors if the RDF contains special characters!
             BufferedReader br = new BufferedReader(new InputStreamReader(getRDFAsInputStream()));
             String line;
             while((line = br.readLine()) != null){
                 if (line.indexOf("<?xml") < 0) {
                     sb.append(line);
                 }
             }
 */
             if (getRequest().getParameter("href") != null) {
                 URL url = new URL(getRequest().getParameter("href"));
                 sb.append("<xi:include href=\"" + url.toString() + "\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>");
             } else {
                 String rdfPath = getPath();
 	        if (rdfPath.endsWith(".html")) {
                     rdfPath = path.substring(0, path.lastIndexOf(".html")) + ".rdf";
                 }
                 sb.append("<xi:include href=\"yanelresource:" + rdfPath + "\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/>");
             }
             sb.append("</wyona:foaf>");
 
             org.xml.sax.XMLReader xmlReader = getXMLReader();
             org.apache.xml.serializer.Serializer serializer = getSerializer();
             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
             serializer.setOutputStream(baos);
             org.wyona.yanel.core.transformation.XIncludeTransformer xIncludeTransformer = new org.wyona.yanel.core.transformation.XIncludeTransformer();
             org.wyona.yanel.core.source.SourceResolver resolver = new org.wyona.yanel.core.source.SourceResolver(this);
             xIncludeTransformer.setResolver(resolver);
             xmlReader.setContentHandler(new javax.xml.transform.sax.SAXResult(xIncludeTransformer).getHandler());
             xIncludeTransformer.setResult(new javax.xml.transform.sax.SAXResult(serializer.asContentHandler()));
             //xmlReader.setContentHandler(serializer.asContentHandler());
             xmlReader.parse(new org.xml.sax.InputSource(new StringBufferInputStream(sb.toString())));
 
             //log.error("DEBUG: XML: " + sb.toString());
 
             if (viewId != null && viewId.equals("source")) {
                 view.setInputStream(new java.io.ByteArrayInputStream(baos.toByteArray()));
                 //view.setInputStream(new StringBufferInputStream(sb.toString()));
                 view.setMimeType(getMimeType(viewId));
                 return view;
 	    } else if (viewId != null && viewId.equals("rdf+xml")) {
                 view.setInputStream(getRDFAsInputStream());
                 view.setMimeType(getMimeType(viewId));
                 return view;
 	    } else if (viewId != null && viewId.equals("atom")) {
                 view.setInputStream(new StringBufferInputStream(new StringBuffer("Not implemented yet!").toString()));
                 view.setMimeType("text/plain");
 /*
                 view.setInputStream(getRDFAsInputStream());
                 view.setMimeType(getMimeType(viewId));
 */
                 return view;
             } else {
                 //return getXMLView(viewId, new StringBufferInputStream(sb.toString()));
                 return getXMLView(viewId, new java.io.ByteArrayInputStream(baos.toByteArray()));
             }
         } catch (java.io.FileNotFoundException e) {
             log.error(e);
             view.setInputStream(new StringBufferInputStream(new StringBuffer("No such file: " + e.getMessage()).toString()));
             view.setMimeType("text/plain");
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             view.setInputStream(new StringBufferInputStream(new StringBuffer("Exception: " + e.getMessage()).toString()));
             view.setMimeType("text/plain");
         }
         return view;
     }
 
     /**
      *
      */
     public ViewDescriptor[] getViewDescriptors() {
         ConfigurableViewDescriptor[] vd = new ConfigurableViewDescriptor[4];
 
         vd[0] = new ConfigurableViewDescriptor("default");
         vd[0].setMimeType(getMimeType(null));
 
         vd[1] = new ConfigurableViewDescriptor("source");
         vd[1].setMimeType(getMimeType("source"));
 
         vd[2] = new ConfigurableViewDescriptor("rdf+xml");
         vd[2].setMimeType(getMimeType("rdf+xml"));
 
         vd[3] = new ConfigurableViewDescriptor("atom");
         vd[3].setMimeType(getMimeType("atom"));
         return vd;
     }
 
     /**
      *
      */
     private InputStream getRDFAsInputStream() throws Exception {
         if (getRequest().getParameter("href") != null) {
             URL url = new URL(getRequest().getParameter("href"));
             return url.openConnection().getInputStream();
         } else {
             String path = getPath();
 	    if (path.endsWith(".html")) {
                 path = path.substring(0, path.lastIndexOf(".html")) + ".rdf";
             }
             //log.error("DEBUG: RDF path: " + path);
             return getProfilesRepository().getNode(path).getInputStream();
         }
     }
 
     /**
      *
      */
     private Repository getProfilesRepository() throws Exception {
 	Repository repoProfiles = ((org.wyona.yanel.impl.map.FOAFRealm) getRealm()).getProfilesRepository();
         if (repoProfiles != null) return repoProfiles;
 
         return getRealm().getRepository();
     }
     
     /**
      * Get introspection for Introspectable interface
      * TODO: What about XML which is being generated dynamically, e.g. http://yanel.wyona.org/roadmap-timeline.html
      */
     public String getIntrospection() throws Exception {
         String name = org.wyona.yanel.core.util.PathUtil.getName(getPath());
        String username = name.substring(0, name.lastIndexOf(".html"));
         StringBuffer sb = new StringBuffer("<?xml version=\"1.0\"?>");
         sb.append("<introspection xmlns=\"http://www.wyona.org/neutron/2.0\">");
     
 /*
         sb.append("<navigation>");
         sb.append("  <sitetree href=\"./\" method=\"PROPFIND\"/>");
         sb.append("</navigation>");
 */
     
        sb.append("<resource name=\"Profile of '" + username + "'\">");
         sb.append("<edit mime-type=\"application/xml\">");
        sb.append("<checkout url=\"" + username + ".rdf" + "?yanel.resource.usecase=checkout\" method=\"GET\"/>");
        sb.append("<checkin  url=\"" + username + ".rdf" + "?yanel.resource.usecase=checkin\"  method=\"PUT\"/>");
        sb.append("<release-lock url=\"" + username + ".rdf" + "?yanel.resource.usecase=release-lock\" method=\"GET\"/>");
         sb.append("</edit>");
         sb.append("</resource>");
         sb.append("</introspection>");
         
         return sb.toString();
     }
 
     /**
      * Delete data of node resource
      */
     public boolean delete() throws Exception {
         getProfilesRepository().getNode(getRDFPath()).delete();
         return true;
     }
 
     /**
      *
      */
     public long getLastModified() throws Exception {
        long lastModified = -1;
        if (getRequest().getParameter("href") == null) {
            Node node = getProfilesRepository().getNode(getRDFPath());
            if (node.isResource()) {
                lastModified = node.getLastModified();
            } else {
                lastModified = 0;
            }
        }
 
        return lastModified;
    }
 
     /**
      *
      */
     public void write(InputStream in) throws Exception {
         log.warn("Not implemented yet!");
     }
 
     /**
      *
      */
     public java.io.OutputStream getOutputStream() throws Exception {
         String path = getRDFPath();
 
         if (!getProfilesRepository().existsNode(path)) {
             // TODO: create node recursively ...
             getProfilesRepository().getNode(new org.wyona.commons.io.Path(path).getParent().toString()).addNode(new org.wyona.commons.io.Path(path).getName().toString(), org.wyona.yarep.core.NodeType.RESOURCE);
         }
         getProfilesRepository().getNode(path).setMimeType("application/xml");
         //getProfilesRepository().getNode(path).setMimeType("text/xml");
         return getProfilesRepository().getNode(path).getOutputStream();
     }
 
     /**
      *
      */
     public java.io.Writer getWriter() throws Exception {
         log.error("Not implemented yet!");
         return null;
     }
 
     /**
      *
      */
     public InputStream getInputStream() throws Exception {
         return getProfilesRepository().getNode(getRDFPath()).getInputStream();
     }
 
     /**
      *
      */
     public java.io.Reader getReader() throws Exception {
         return new InputStreamReader(getInputStream(), "UTF-8");
     }
 
     /**
      *
      */
     private String getRDFPath() {
         String path = getPath();
 	if (path.endsWith(".html")) {
             path = path.substring(0, path.lastIndexOf(".html")) + ".rdf";
         }
         return path;
     }
 
     /**
      *
      */
     private org.xml.sax.XMLReader getXMLReader() throws Exception {
         org.xml.sax.XMLReader xmlReader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
         org.apache.xml.resolver.tools.CatalogResolver catalogResolver = new org.apache.xml.resolver.tools.CatalogResolver();
         xmlReader.setEntityResolver(catalogResolver);
         xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
         return xmlReader;
     }
 
     /**
      *
      */
     private org.apache.xml.serializer.Serializer getSerializer() {
         return org.wyona.yanel.core.serialization.SerializerFactory.getSerializer(org.wyona.yanel.core.serialization.SerializerFactory.XML);
     }
 }
