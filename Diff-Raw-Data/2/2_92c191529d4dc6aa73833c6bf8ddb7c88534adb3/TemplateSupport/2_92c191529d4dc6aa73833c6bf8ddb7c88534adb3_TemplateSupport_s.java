 /**
  * Copyright (C) 2009-2013 enStratus Networks Inc
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.vcloud.compute;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AsynchronousTask;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.compute.AbstractImageSupport;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.ImageClass;
 import org.dasein.cloud.compute.ImageCreateOptions;
 import org.dasein.cloud.compute.ImageFilterOptions;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.compute.MachineImageFormat;
 import org.dasein.cloud.compute.MachineImageState;
 import org.dasein.cloud.compute.MachineImageType;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VmState;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.cloud.util.Cache;
 import org.dasein.cloud.util.CacheLevel;
 import org.dasein.cloud.vcloud.vCloud;
 import org.dasein.cloud.vcloud.vCloudMethod;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.Jiterator;
 import org.dasein.util.JiteratorPopulator;
 import org.dasein.util.PopulatorThread;
 import org.dasein.util.uom.time.Minute;
 import org.dasein.util.uom.time.TimePeriod;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 import java.util.TreeSet;
 
 /**
  * Implements vApp Template support in accordance with the Dasein Cloud image support model. Dasein Cloud images map
  * to vApp templates in a vCloud catalog.
  * <p>Created by George Reese: 9/17/12 10:58 AM</p>
  * @author George Reese
  * @version 2013.04 initial version
  * @since 2013.04
  */
 public class TemplateSupport extends AbstractImageSupport {
     static private final Logger logger = vCloud.getLogger(TemplateSupport.class);
 
     static public class Catalog {
         public String catalogId;
         public String name;
         public boolean published;
         public String owner;
     }
 
     public TemplateSupport(@Nonnull vCloud cloud) {
         super(cloud);
     }
 
     @Override
     protected MachineImage capture(@Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> task) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.capture");
         try {
             vCloudMethod method = new vCloudMethod((vCloud)getProvider());
             String vmId = options.getVirtualMachineId();
 
             if( vmId == null ) {
                 throw new CloudException("A capture operation requires a valid VM ID");
             }
             VirtualMachine vm = ((vCloud)getProvider()).getComputeServices().getVirtualMachineSupport().getVirtualMachine(vmId);
             String vAppId = (vm == null ? null : (String)vm.getTag(vAppSupport.PARENT_VAPP_ID));
 
             if( vm == null ) {
                 throw new CloudException("No such virtual machine: " + vmId);
             }
             else if( vAppId == null ) {
                 throw new CloudException("Unable to determine virtual machine vApp for capture: " + vmId);
             }
             long timeout = (System.currentTimeMillis() + CalendarWrapper.MINUTE * 10L);
 
             while( timeout > System.currentTimeMillis() ) {
                 if( vm == null ) {
                     throw new CloudException("VM " + vmId + " went away");
                 }
                 if( !vm.getCurrentState().equals(VmState.PENDING) ) {
                     break;
                 }
                 try { Thread.sleep(15000L); }
                 catch( InterruptedException ignore ) { }
                 try { vm = ((vCloud)getProvider()).getComputeServices().getVirtualMachineSupport().getVirtualMachine(vmId); }
                 catch( Throwable ignore ) { }
             }
             boolean running = !vm.getCurrentState().equals(VmState.STOPPED);
             String vappId = (String)vm.getTag(vAppSupport.PARENT_VAPP_ID);
 
             if( running ) {
                 ((vCloud)getProvider()).getComputeServices().getVirtualMachineSupport().undeploy(vappId);
             }
             try {
                 String endpoint = method.toURL("vApp", vAppId);
                 StringBuilder xml = new StringBuilder();
 
                 xml.append("<CaptureVAppParams xmlns=\"http://www.vmware.com/vcloud/v1.5\" xmlns:ovf=\"http://schemas.dmtf.org/ovf/envelope/1\" name=\"").append(vCloud.escapeXml(options.getName())).append("\">");
                 xml.append("<Description>").append(options.getDescription()).append("</Description>");
                 xml.append("<Source href=\"").append(endpoint).append("\" type=\"").append(method.getMediaTypeForVApp()).append("\"/>");
                 xml.append("</CaptureVAppParams>");
 
                 String response = method.post(vCloudMethod.CAPTURE_VAPP, vm.getProviderDataCenterId(), xml.toString());
 
                 if( response.equals("") ) {
                     throw new CloudException("No error or other information was in the response");
                 }
                 Document doc = method.parseXML(response);
 
                 try {
                     method.checkError(doc);
                 }
                 catch( CloudException e ) {
                     if( e.getMessage().contains("Stop the vApp and try again") ) {
                         logger.warn("The cloud thinks the vApp or VM is still running; going to check what's going on: " + e.getMessage());
                         vm = ((vCloud)getProvider()).getComputeServices().getVirtualMachineSupport().getVirtualMachine(vmId);
                         if( vm == null ) {
                             throw new CloudException("Virtual machine went away");
                         }
                         if( !vm.getCurrentState().equals(VmState.STOPPED) ) {
                             logger.warn("Current state of VM: " + vm.getCurrentState());
                             ((vCloud)getProvider()).getComputeServices().getVirtualMachineSupport().undeploy(vappId);
                         }
                         response = method.post(vCloudMethod.CAPTURE_VAPP, vm.getProviderDataCenterId(), xml.toString());
                         if( response.equals("") ) {
                             throw new CloudException("No error or other information was in the response");
                         }
                         doc = method.parseXML(response);
                         method.checkError(doc);
                     }
                     else {
                         throw e;
                     }
                 }
 
                 NodeList vapps = doc.getElementsByTagName("VAppTemplate");
 
                 if( vapps.getLength() < 1 ) {
                     throw new CloudException("No vApp templates were found in response");
                 }
                 Node vapp = vapps.item(0);
                 String imageId = null;
                 Node href = vapp.getAttributes().getNamedItem("href");
 
                 if( href != null ) {
                     imageId = ((vCloud)getProvider()).toID(href.getNodeValue().trim());
                 }
                 if( imageId == null || imageId.length() < 1 ) {
                     throw new CloudException("No imageId was found in response");
                 }
                 MachineImage img = loadVapp(imageId, getContext().getAccountNumber(), false, options.getName(), options.getDescription(), System.currentTimeMillis());
 
                 if( img == null ) {
                     throw new CloudException("Image was lost");
                 }
                 method.waitFor(response);
                 publish(img);
                 return img;
             }
             finally {
                 if( running ) {
                     ((vCloud)getProvider()).getComputeServices().getVirtualMachineSupport().deploy(vappId);
                 }
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     private void publish(@Nonnull MachineImage img) throws CloudException, InternalException {
         vCloudMethod method = new vCloudMethod((vCloud)getProvider());
         Catalog c = null;
 
         for( Catalog catalog : listPrivateCatalogs() ) {
             if( catalog.owner.equals(getContext().getAccountNumber()) ) {
                 c = catalog;
                 if( catalog.name.equals("Standard Catalog") ) {
                     break;
                 }
             }
         }
         StringBuilder xml;
 
         if( c == null ) {
             xml = new StringBuilder();
             xml.append("<AdminCatalog xmlns=\"http://www.vmware.com/vcloud/v1.5\" name=\"Standard Catalog\">");
             xml.append("<Description>Standard catalog for custom vApp templates</Description>");
             xml.append("<IsPublished>false</IsPublished>");
             xml.append("</AdminCatalog>");
             String response = method.post("createCatalog", method.toAdminURL("org", getContext().getRegionId()) + "/catalogs", method.getMediaTypeForActionAddCatalog(), xml.toString());
             String href = null;
 
             method.waitFor(response);
             if( !response.equals("") ) {
                 NodeList matches = method.parseXML(response).getElementsByTagName("AdminCatalog");
 
                 for( int i=0; i<matches.getLength(); i++ ) {
                     Node m = matches.item(i);
                     Node h = m.getAttributes().getNamedItem("href");
 
                     if( h != null ) {
                         href = h.getNodeValue().trim();
                         break;
                     }
                 }
             }
             if( href == null ) {
                 throw new CloudException("No catalog could be identified for publishing vApp template " + img.getProviderMachineImageId());
             }
             c = getCatalog(false, href);
             if( c == null ) {
                 throw new CloudException("No catalog could be identified for publishing vApp template " + img.getProviderMachineImageId());
             }
         }
 
         xml = new StringBuilder();
         xml.append("<CatalogItem xmlns=\"http://www.vmware.com/vcloud/v1.5\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
         xml.append("name=\"").append(vCloud.escapeXml(img.getName())).append("\">");
         xml.append("<Description>").append(vCloud.escapeXml(img.getDescription())).append("</Description>");
         xml.append("<Entity href=\"").append(method.toURL("vAppTemplate", img.getProviderMachineImageId())).append("\" ");
         xml.append("name=\"").append(vCloud.escapeXml(img.getName())).append("\" ");
         xml.append("type=\"").append(method.getMediaTypeForVAppTemplate()).append("\" xsi:type=\"").append("ResourceReferenceType\"/>");
         xml.append("</CatalogItem>");
 
         method.waitFor(method.post("publish", method.toURL("catalog", c.catalogId) + "/catalogItems", method.getMediaTypeForCatalogItem(), xml.toString()));
     }
 
     private @Nullable Catalog getCatalog(boolean published, @Nonnull String href) throws CloudException, InternalException {
         String catalogId = ((vCloud)getProvider()).toID(href);
         vCloudMethod method = new vCloudMethod((vCloud)getProvider());
         String xml = method.get("catalog", catalogId);
 
         if( xml == null ) {
             logger.warn("Unable to find catalog " + catalogId + " indicated by org " + getContext().getAccountNumber());
             return null;
         }
         Document doc = method.parseXML(xml);
         NodeList cNodes = doc.getElementsByTagName("Catalog");
 
         for( int i=0; i<cNodes.getLength(); i++ ) {
             Node cnode = cNodes.item(i);
 
             Node name = cnode.getAttributes().getNamedItem("name");
             String catalogName = null;
 
             if( name != null ) {
                 catalogName = name.getNodeValue().trim();
             }
             if( cnode.hasChildNodes() ) {
                 NodeList attributes = cnode.getChildNodes();
                 String owner = "--public--";
                 boolean p = false;
 
                 for( int j=0; j<attributes.getLength(); j++ ) {
                     Node attribute = attributes.item(j);
 
                     if( attribute.getNodeName().equalsIgnoreCase("IsPublished") ) {
                         p = (attribute.hasChildNodes() && attribute.getFirstChild().getNodeValue().trim().equalsIgnoreCase("true"));
                     }
                     else if( attribute.getNodeName().equalsIgnoreCase("Link") && attribute.hasAttributes() ) {
                         Node rel = attribute.getAttributes().getNamedItem("rel");
 
                         if( rel != null && rel.getNodeValue().trim().equalsIgnoreCase("up") ) {
                             Node type = attribute.getAttributes().getNamedItem("type");
 
                             if( type != null && type.getNodeValue().trim().equalsIgnoreCase(method.getMediaTypeForOrg()) ) {
                                 Node h = attribute.getAttributes().getNamedItem("href");
 
                                 if( h != null ) {
                                     owner = method.getOrgName(h.getNodeValue().trim());
                                 }
                             }
                         }
                     }
                 }
                 if( p == published ) {
                     Catalog catalog = new Catalog();
                     catalog.catalogId = ((vCloud)getProvider()).toID(href);
                     catalog.published = p;
                     catalog.owner = owner;
                     catalog.name = catalogName;
                     return catalog;
                 }
             }
 
         }
         return null;
     }
 
     @Override
     public MachineImage getImage(@Nonnull String providerImageId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.getImage");
         try {
             for( MachineImage image : listImages((ImageFilterOptions)null) ) {
                 if( image.getProviderMachineImageId().equals(providerImageId) ) {
                     return image;
                 }
             }
            for( MachineImage image : searchPublicImages(null, null, null) ) {
                 if( image.getProviderMachineImageId().equals(providerImageId) ) {
                     return image;
                 }
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         return "vApp Template";
     }
 
     @Override
     public boolean isImageSharedWithPublic(@Nonnull String machineImageId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.isImageSharedWithPublic");
         try {
             MachineImage img = getImage(machineImageId);
 
             if( img == null ) {
                 return false;
             }
             String p = (String)img.getTag("public");
 
             return (p != null && p.equalsIgnoreCase("true"));
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.isSubscribed");
         try {
             return (getProvider().testContext() != null);
         }
         finally {
             APITrace.end();
         }
     }
 
     private Iterable<Catalog> listPublicCatalogs() throws CloudException, InternalException {
         Cache<Catalog> cache = Cache.getInstance(getProvider(), "publicCatalogs", Catalog.class, CacheLevel.REGION_ACCOUNT, new TimePeriod<Minute>(30, TimePeriod.MINUTE));
         Iterable<Catalog> catalogs = cache.get(getContext());
 
         if( catalogs == null ) {
             vCloudMethod method = new vCloudMethod((vCloud)getProvider());
             String xml = method.get("org", getContext().getRegionId());
 
             if( xml == null ) {
                 catalogs = Collections.emptyList();
             }
             else {
                 ArrayList<Catalog> list = new ArrayList<Catalog>();
                 Document doc = method.parseXML(xml);
                 NodeList links = doc.getElementsByTagName("Link");
 
                 for( int i=0; i<links.getLength(); i++ ) {
                     Node link = links.item(i);
 
                     if( link.hasAttributes() ) {
                         Node rel = link.getAttributes().getNamedItem("rel");
 
                         if( rel != null && rel.getNodeValue().trim().equalsIgnoreCase("down") ) {
                             Node type = link.getAttributes().getNamedItem("type");
 
                             if( type != null && type.getNodeValue().trim().equals(method.getMediaTypeForCatalog()) ) {
                                 Node href = link.getAttributes().getNamedItem("href");
                                 Catalog c = getCatalog(true, href.getNodeValue().trim());
 
                                 if( c != null ) {
                                     list.add(c);
                                 }
                             }
                         }
                     }
                 }
                 catalogs = list;
             }
             cache.put(getContext(), catalogs);
         }
         return catalogs;
     }
 
     private Iterable<Catalog> listPrivateCatalogs() throws CloudException, InternalException {
         Cache<Catalog> cache = Cache.getInstance(getProvider(), "privateCatalogs", Catalog.class, CacheLevel.REGION_ACCOUNT, new TimePeriod<Minute>(30, TimePeriod.MINUTE));
         Iterable<Catalog> catalogs = cache.get(getContext());
 
         if( catalogs == null ) {
             vCloudMethod method = new vCloudMethod((vCloud)getProvider());
             String xml = method.get("org", getContext().getRegionId());
 
             if( xml == null ) {
                 catalogs = Collections.emptyList();
             }
             else {
                 ArrayList<Catalog> list = new ArrayList<Catalog>();
                 Document doc = method.parseXML(xml);
                 NodeList links = doc.getElementsByTagName("Link");
 
                 for( int i=0; i<links.getLength(); i++ ) {
                     Node link = links.item(i);
 
                     if( link.hasAttributes() ) {
                         Node rel = link.getAttributes().getNamedItem("rel");
 
                         if( rel != null && rel.getNodeValue().trim().equalsIgnoreCase("down") ) {
                             Node type = link.getAttributes().getNamedItem("type");
 
                             if( type != null && type.getNodeValue().trim().equals(method.getMediaTypeForCatalog()) ) {
                                 Node href = link.getAttributes().getNamedItem("href");
                                 Catalog c = getCatalog(false, href.getNodeValue().trim());
 
                                 if( c != null ) {
                                     list.add(c);
                                 }
                             }
                         }
                     }
                 }
                 catalogs = list;
             }
             cache.put(getContext(), catalogs);
         }
         return catalogs;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> listImages(final @Nullable ImageFilterOptions options) throws CloudException, InternalException {
         getProvider().hold();
         PopulatorThread<MachineImage> populator = new PopulatorThread<MachineImage>(new JiteratorPopulator<MachineImage>() {
             @Override
             public void populate(@Nonnull Jiterator<MachineImage> iterator) throws Exception {
                 try {
                     APITrace.begin(getProvider(), "Image.listImages");
                     try {
                         for( Catalog catalog : listPrivateCatalogs() ) {
                             vCloudMethod method = new vCloudMethod((vCloud)getProvider());
                             String xml = method.get("catalog", catalog.catalogId);
 
                             if( xml == null ) {
                                 logger.warn("Unable to find catalog " + catalog.catalogId + " indicated by org " + getContext().getAccountNumber());
                                 continue;
                             }
                             Document doc = method.parseXML(xml);
                             NodeList cNodes = doc.getElementsByTagName("Catalog");
 
                             for( int i=0; i<cNodes.getLength(); i++ ) {
                                 Node cnode = cNodes.item(i);
 
                                 if( cnode.hasChildNodes() ) {
                                     NodeList items = cnode.getChildNodes();
 
                                     for( int j=0; j<items.getLength(); j++ ) {
                                         Node wrapper = items.item(j);
 
                                         if( wrapper.getNodeName().equalsIgnoreCase("CatalogItems") && wrapper.hasChildNodes() ) {
                                             NodeList entries = wrapper.getChildNodes();
 
                                             for( int k=0; k<entries.getLength(); k++ ) {
                                                 Node item = entries.item(k);
 
                                                 if( item.getNodeName().equalsIgnoreCase("CatalogItem") && item.hasAttributes() ) {
                                                     Node href = item.getAttributes().getNamedItem("href");
 
                                                     if( href != null ) {
                                                         String catalogItemId = ((vCloud)getProvider()).toID(href.getNodeValue().trim());
                                                         MachineImage image = loadTemplate(catalog.owner, catalogItemId, catalog.published);
 
                                                         if( image != null ) {
                                                             if( options == null || options.matches(image) ) {
                                                                 image.setTag("catalogItemId", catalogItemId);
                                                                 iterator.push(image);
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 }
 
                             }
                         }
                     }
                     finally {
                         APITrace.end();
                     }
                 }
                 finally {
                     getProvider().release();
                 }
             }
         });
 
         populator.populate();
         return populator.getResult();
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
         return Collections.singletonList(MachineImageFormat.VMDK);
     }
 
     @Override
     public @Nonnull Iterable<String> listShares(@Nonnull String forMachineImageId) throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     private @Nullable MachineImage loadTemplate(@Nonnull String ownerId, @Nonnull String catalogItemId, boolean published) throws CloudException, InternalException {
         vCloudMethod method = new vCloudMethod((vCloud)getProvider());
         String xml = method.get("catalogItem", catalogItemId);
 
         if( xml == null ) {
             logger.warn("Catalog item " + catalogItemId + " is missing from the catalog");
             return null;
         }
         Document doc = method.parseXML(xml);
         NodeList items = doc.getElementsByTagName("CatalogItem");
 
         if( items.getLength() < 1 ) {
             return null;
         }
         Node item = items.item(0);
 
         if( item.hasAttributes() && item.hasChildNodes() ) {
             Node name = item.getAttributes().getNamedItem("name");
             String imageName = null, imageDescription = null;
             long createdAt = 0L;
 
             if( name != null ) {
                 String n = name.getNodeValue().trim();
 
                 if( n.length() > 0 ) {
                     imageName = n;
                 }
             }
             NodeList entries = item.getChildNodes();
             String vappId = null;
 
             for( int i=0; i<entries.getLength(); i++ ) {
                 Node entry = entries.item(i);
 
                 if( entry.getNodeName().equalsIgnoreCase("description") && entry.hasChildNodes() ) {
                     String d = entry.getFirstChild().getNodeValue().trim();
 
                     if( d.length() > 0 ) {
                         imageDescription = d;
                     }
                 }
                 else if( entry.getNodeName().equalsIgnoreCase("datecreated") && entry.hasChildNodes() ) {
                     createdAt = ((vCloud)getProvider()).parseTime(entry.getFirstChild().getNodeValue().trim());
                 }
                 else if( entry.getNodeName().equalsIgnoreCase("entity") && entry.hasAttributes() ) {
                     Node href = entry.getAttributes().getNamedItem("href");
 
                     if( href != null ) {
                         vappId = ((vCloud)getProvider()).toID(href.getNodeValue().trim());
                     }
                 }
             }
             if( vappId != null ) {
                 return loadVapp(vappId, ownerId, published, imageName, imageDescription, createdAt);
             }
         }
         return null;
     }
     //imageId, options.getName(), options.getDescription(), System.currentTimeMillis()
     private @Nullable MachineImage loadVapp(@Nonnull String imageId, @Nonnull String ownerId, boolean published, @Nullable String name, @Nullable String description, @Nonnegative long createdAt) throws CloudException, InternalException {
         vCloudMethod method = new vCloudMethod((vCloud)getProvider());
 
         String xml = method.get("vAppTemplate", imageId);
 
         if( xml == null ) {
             return null;
         }
         Document doc = method.parseXML(xml);
         NodeList templates = doc.getElementsByTagName("VAppTemplate");
 
         if( templates.getLength() < 1 ) {
             return null;
         }
         Node template = templates.item(0);
         TreeSet<String> childVms = new TreeSet<String>();
 
         if( name == null ) {
             Node node = template.getAttributes().getNamedItem("name");
 
             if( node != null ) {
                 String n = node.getNodeValue().trim();
 
                 if( n.length() > 0 ) {
                     name = n;
                 }
             }
         }
         NodeList attributes = template.getChildNodes();
         Platform platform = Platform.UNKNOWN;
         Architecture architecture = Architecture.I64;
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
 
             if( attribute.getNodeName().equalsIgnoreCase("description") && description == null && attribute.hasChildNodes() ) {
                 String d = attribute.getFirstChild().getNodeValue().trim();
 
                 if( d.length() > 0 ) {
                     description = d;
                     if( name == null ) {
                         name = d;
                     }
                 }
             }
             else if( attribute.getNodeName().equalsIgnoreCase("children") && attribute.hasChildNodes() ) {
                 NodeList children = attribute.getChildNodes();
 
                 for( int j=0; j<children.getLength(); j++ ) {
                     Node child = children.item(j);
 
                     if( child.getNodeName().equalsIgnoreCase("vm") && child.hasChildNodes() ) {
                         Node childHref = child.getAttributes().getNamedItem("href");
 
                         if( childHref != null ) {
                             childVms.add(((vCloud)getProvider()).toID(childHref.getNodeValue().trim()));
                         }
                         NodeList vmAttrs = child.getChildNodes();
 
                         for( int k=0; k<vmAttrs.getLength(); k++ ) {
                             Node vmAttr = vmAttrs.item(k);
 
                             if( vmAttr.getNodeName().equalsIgnoreCase("guestcustomizationsection") && vmAttr.hasChildNodes() ) {
                                 NodeList custList = vmAttr.getChildNodes();
 
                                 for( int l=0; l<custList.getLength(); l++ ) {
                                     Node cust = custList.item(l);
 
                                     if( cust.getNodeName().equalsIgnoreCase("computername") && cust.hasChildNodes() ) {
                                         String n = cust.getFirstChild().getNodeValue().trim();
 
                                         if( n.length() > 0 ) {
                                             if( name == null ) {
                                                 name = n;
                                             }
                                             else {
                                                 name = name + " - " + n;
                                             }
                                         }
                                     }
                                 }
                             }
                             else if( vmAttr.getNodeName().equalsIgnoreCase("ovf:ProductSection") && vmAttr.hasChildNodes() ) {
                                 NodeList prdList = vmAttr.getChildNodes();
 
                                 for( int l=0; l<prdList.getLength(); l++ ) {
                                     Node prd = prdList.item(l);
 
                                     if( prd.getNodeName().equalsIgnoreCase("ovf:Product") && prd.hasChildNodes() ) {
                                         String n = prd.getFirstChild().getNodeValue().trim();
 
                                         if( n.length() > 0 ) {
                                             platform = Platform.guess(n);
                                         }
                                     }
                                 }
                             }
                             else if( vmAttr.getNodeName().equalsIgnoreCase("ovf:OperatingSystemSection") && vmAttr.hasChildNodes() ) {
                                 NodeList os = vmAttr.getChildNodes();
 
                                 for( int l=0; l<os.getLength(); l++ ) {
                                     Node osdesc = os.item(l);
 
                                     if( osdesc.getNodeName().equalsIgnoreCase("ovf:Description") && osdesc.hasChildNodes() ) {
                                         String desc = osdesc.getFirstChild().getNodeValue();
 
                                         platform = Platform.guess(desc);
 
                                         if( desc.contains("32") || (desc.contains("x86") && !desc.contains("64")) ) {
                                             architecture = Architecture.I32;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             else if (attribute.getNodeName().equalsIgnoreCase("LeaseSettingsSection") && attribute.hasChildNodes()){
             	if (logger.isTraceEnabled()){
             		logger.trace("Checking lease settings for VAppTemplate : " +  name);
             	}
             	NodeList children = attribute.getChildNodes();
                 for( int j=0; j<children.getLength(); j++ ) {
                     Node child = children.item(j);
                     if( child.getNodeName().equalsIgnoreCase("StorageLeaseExpiration") && child.hasChildNodes() ) {
                     	String expiryDateString = child.getFirstChild().getNodeValue().trim();
                     	Date expiryDate = vCloud.parseIsoDate(expiryDateString);
                     	if (expiryDate != null){
                     		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                     		if (cal.getTimeInMillis() > expiryDate.getTime()){
                     			if (logger.isTraceEnabled()){
                     				logger.trace("vAppTemplate " + name + " has an expired storage lease.");
                     			}
                     			return null;
                     		}
                     	}
                     }
                 }
                 
             }
             else if( attribute.getNodeName().equalsIgnoreCase("datecreated") && attribute.hasChildNodes() ) {
                 createdAt = ((vCloud)getProvider()).parseTime(attribute.getFirstChild().getNodeValue().trim());
             }
         }
         if( name == null ) {
             name = imageId;
         }
         if( description == null ) {
             description = name;
         }
         if( platform.equals(Platform.UNKNOWN) ) {
             platform = Platform.guess(name + " " + description);
         }
         MachineImage image = MachineImage.getMachineImageInstance(ownerId, getContext().getRegionId(), imageId, MachineImageState.ACTIVE, name, description, architecture, platform).createdAt(createdAt);
         StringBuilder ids = new StringBuilder();
 
         for( String id : childVms ) {
             if( ids.length() > 0 ) {
                 ids.append(",");
             }
             ids.append(id);
         }
         image.setTag("childVirtualMachineIds", ids.toString());
         if( published ) {
             image.setTag("public", "true");
         }
         return image;
     }
 
     @Override
     public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.remove");
         try {
             MachineImage image = getImage(providerImageId);
 
             if( image == null ) {
                 throw new CloudException("No such image: " + providerImageId);
             }
             vCloudMethod method = new vCloudMethod((vCloud)getProvider());
             String catalogItemId = (String)image.getTag("catalogItemId");
 
             method.delete("vAppTemplate", providerImageId);
             if( catalogItemId != null ) {
                 method.delete("catalogItem", catalogItemId);
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchPublicImages(@Nonnull ImageFilterOptions options) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.searchPublicImages");
         try {
             ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 
             for( Catalog catalog : listPublicCatalogs() ) {
                 vCloudMethod method = new vCloudMethod((vCloud)getProvider());
                 String xml = method.get("catalog", catalog.catalogId);
 
                 if( xml == null ) {
                     logger.warn("Unable to find catalog " + catalog.catalogId + " indicated by org " + getContext().getAccountNumber());
                     continue;
                 }
                 Document doc = method.parseXML(xml);
                 NodeList cNodes = doc.getElementsByTagName("Catalog");
 
                 for( int i=0; i<cNodes.getLength(); i++ ) {
                     Node cnode = cNodes.item(i);
 
                     if( cnode.hasChildNodes() ) {
                         NodeList items = cnode.getChildNodes();
 
                         for( int j=0; j<items.getLength(); j++ ) {
                             Node wrapper = items.item(j);
 
                             if( wrapper.getNodeName().equalsIgnoreCase("CatalogItems") && wrapper.hasChildNodes() ) {
                                 NodeList entries = wrapper.getChildNodes();
 
                                 for( int k=0; k<entries.getLength(); k++ ) {
                                     Node item = entries.item(k);
 
                                     if( item.getNodeName().equalsIgnoreCase("CatalogItem") && item.hasAttributes() ) {
                                         Node href = item.getAttributes().getNamedItem("href");
 
                                         if( href != null ) {
                                             MachineImage image = loadTemplate(catalog.owner, ((vCloud)getProvider()).toID(href.getNodeValue().trim()), catalog.published);
 
                                             if( image != null ) {
                                                 if( options == null || options.matches(image) ) {
                                                     images.add(image);
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
 
                 }
             }
             return images;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean supportsCustomImages() {
         return true;
     }
 
     @Override
     public boolean supportsImageCapture(@Nonnull MachineImageType type) throws CloudException, InternalException {
         return type.equals(MachineImageType.VOLUME);
     }
 
     @Override
     public boolean supportsPublicLibrary(@Nonnull ImageClass cls) {
         return cls.equals(ImageClass.MACHINE);
     }
 }
