 package com.bradmcevoy.http.webdav;
 
 import com.bradmcevoy.http.CollectionResource;
 import com.bradmcevoy.http.PropFindableResource;
 import com.bradmcevoy.http.Resource;
 import com.bradmcevoy.http.Response.Status;
 import com.bradmcevoy.http.Utils;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.http.values.ValueAndType;
 import com.bradmcevoy.http.webdav.PropFindResponse.NameAndError;
 import com.bradmcevoy.property.PropertySource;
 import com.bradmcevoy.property.PropertySource.PropertyMetaData;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
import java.util.EnumMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.xml.namespace.QName;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class performs the main part of PROPFIND processing, which is given
  * a field request (either named fields or an allprop request) and a target
  * resource, iterate over that resource and its children (depending on the
  * depth header) and list a list of PropFindResponse objects.
  *
  * These PropFindResponse objects contain typed values for all of the known
  * fields, and a set of unknown fields. These will be used to build the xml
  * which is ultimately sent back to the client.
  *
  * This class uses a list of PropertySource's, where each PropertySource represents
  * some mechanism to read properties from a resource.
  *
  * @author brad
  */
 public class PropFindPropertyBuilder {
 
     private static final Logger log = LoggerFactory.getLogger(PropFindPropertyBuilder.class);
     private final List<PropertySource> propertySources;
 
     /**
      *
      * @param propertySources - the list of property sources used to read properties
      * from resources
      */
     public PropFindPropertyBuilder(List<PropertySource> propertySources) {
         this.propertySources = propertySources;
         log.debug("num property sources: " + propertySources.size());
     }
 
     /**
      * Construct a list of PropFindResponse for the given resource, using
      * the PropertySource's injected into this class.
      *
      *
      * @param pfr - the resource to interrogate
      * @param depth - the depth header. 0 means only look at the given resource. 1 is to include children
      * @param parseResult - contains the list of fields, or a true boolean indicating all properties
      * @param url - the URL of the given resource
      * @return
      */
     public List<PropFindResponse> buildProperties(PropFindableResource pfr, int depth, PropFindRequestFieldParser.ParseResult parseResult, String url) {
         List<PropFindResponse> propFindResponses = new ArrayList<PropFindResponse>();
         appendResponses(propFindResponses, pfr, depth, parseResult, url);
         return propFindResponses;
     }
 
     public ValueAndType getProperty(QName field, Resource resource) throws NotAuthorizedException {
         log.debug("num property sources: " + propertySources.size());
         for (PropertySource source : propertySources) {
             PropertyMetaData meta = source.getPropertyMetaData(field, resource);
             if (meta != null && !meta.isUnknown()) {
                 Object val = source.getProperty(field, resource);
                 return new ValueAndType(val, meta.getValueType());
             }
         }
         return null;
     }
 
     private void appendResponses(List<PropFindResponse> responses, PropFindableResource resource, int requestedDepth, PropFindRequestFieldParser.ParseResult parseResult, String encodedCollectionUrl) {
         try {
             String collectionHref = suffixSlash(resource, encodedCollectionUrl);
             URI parentUri = new URI(collectionHref);
 
             collectionHref = parentUri.toASCIIString();
             processResource(responses, resource, parseResult, collectionHref, requestedDepth, 0, collectionHref);
 
         } catch (URISyntaxException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public void processResource(List<PropFindResponse> responses, PropFindableResource resource, PropFindRequestFieldParser.ParseResult parseResult, String href, int requestedDepth, int currentDepth, String collectionHref) {
         collectionHref = suffixSlash(resource, collectionHref);
         final LinkedHashMap<QName, ValueAndType> knownProperties = new LinkedHashMap<QName, ValueAndType>();
         final ArrayList<NameAndError> unknownProperties = new ArrayList<NameAndError>();
 
         if (resource instanceof CollectionResource) {
             if (!href.endsWith("/")) {
                 href = href + "/";
             }
         }
		System.out.println("processResource: href: " + href);		
         Set<QName> requestedFields;
         if (parseResult.isAllProp()) {
             requestedFields = findAllProps(resource);
         } else {
             requestedFields = parseResult.getNames();
         }
         Iterator<QName> it = requestedFields.iterator();
         while (it.hasNext()) {
             QName field = it.next();
             if (field.getLocalPart().equals("href")) {
                 knownProperties.put(field, new ValueAndType(href, String.class));
             } else {
                 boolean found = false;
                 for (PropertySource source : propertySources) {
                     PropertyMetaData meta = source.getPropertyMetaData(field, resource);
                     if (meta != null && !meta.isUnknown()) {
                         Object val;
                         try {
                             val = source.getProperty( field, resource );
                             knownProperties.put(field, new ValueAndType(val, meta.getValueType()));
                         } catch( NotAuthorizedException ex ) {
                             unknownProperties.add(new NameAndError(field, "Not authorised"));
                         }
                         found = true;
                         break;
                     }
                 }
                 if (!found) {
                     if (log.isDebugEnabled()) {
                         log.debug("unknown: " + field.toString());
                     }
                     unknownProperties.add(new NameAndError(field, null));
                 }
 
             }
         }
         if (log.isDebugEnabled()) {
             if (unknownProperties.size() > 0) {
                 log.debug("some properties could not be resolved. Listing property sources:");
                 for (PropertySource ps : propertySources) {
                     log.debug(" - " + ps.getClass().getCanonicalName());
                 }
             }
         }
 
        //Map<Status, List<NameAndError>> errorProperties = new HashMap<Status, List<NameAndError>>();
		Map<Status, List<NameAndError>> errorProperties = new EnumMap<Status, List<NameAndError>>(Status.class);
         errorProperties.put(Status.SC_NOT_FOUND, unknownProperties);
         PropFindResponse r = new PropFindResponse(href, knownProperties, errorProperties);
         responses.add(r);
 
         if (requestedDepth > currentDepth && resource instanceof CollectionResource) {
             CollectionResource col = (CollectionResource) resource;
             List<? extends Resource> list = col.getChildren();
             list = new ArrayList<Resource>(list);
             for (Resource child : list) {
                 if (child instanceof PropFindableResource) {
                     String childName = child.getName();
                     if (childName == null) {
                         log.warn("null name for resource of type: " + child.getClass() + " in folder: " + href + " WILL NOT be returned in PROPFIND response!!");
                     } else {
                        String childHref = href + Utils.percentEncode(childName);
                         // Note that the new collection href, is just the current href
						System.out.println("  next processResource: childHref: " + childHref + "  href: " + href + "   colHref: " + collectionHref);
                         processResource(responses, (PropFindableResource) child, parseResult, childHref, requestedDepth, currentDepth + 1, href);
                     }
                 }
             }
         }
 
     }
 
     private String suffixSlash(PropFindableResource resource, String s) {
         if ( resource instanceof CollectionResource && !s.endsWith("/")) {
             s = s + "/";
         }
         return s;
     }
 
     public Set<QName> findAllProps(PropFindableResource resource) {
         Set<QName> names = new LinkedHashSet<QName>();
         for (PropertySource source : this.propertySources) {
             List<QName> allprops = source.getAllPropertyNames(resource);
             if (allprops != null) {
                 names.addAll(allprops);
             }
         }
         return names;
     }
 }
