 /*
  * Copyright 2010 DTO Labs, Inc. (http://dtolabs.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 /*
 * NodeGenerator.java
 * 
 * User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
 * Created: Oct 18, 2010 7:03:37 PM
 * 
 */
 package com.dtolabs.rundeck.ec2;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.PropertiesCredentials;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.*;
 import com.dtolabs.rundeck.core.common.INodeEntry;
 import com.dtolabs.rundeck.core.common.NodeEntryImpl;
 import com.dtolabs.shared.resources.ResourceXMLConstants;
 import com.dtolabs.shared.resources.ResourceXMLGenerator;
 import org.apache.commons.beanutils.BeanUtils;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * NodeGenerator produces Rundeck node definitions in XML format from EC2 Instances 
  *
  * @author Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
  */
 public class NodeGenerator {
     public static void main(final String[] args) throws IOException, GeneratorException {
         File outfile = null;
 
         //load generator mapping
        if (args.length < 2) {
             System.err.println(
                "usage: <credentials.properties> <endpoint> [mapping.properties] [outfile] [query parameters, \"a=b\" ...]");
             System.err.println(
                 "\t optional arguments can be replaced by \"-\" to use the default, and then query parameters appended");
             System.exit(2);
         }
 
         final InputStream stream = new FileInputStream(args[0]);
        final String endPoint = args[1];
         final AWSCredentials credentials = new PropertiesCredentials(stream);
 
         Properties mapping = new Properties();
        if (args.length > 2 && !"-".equals(args[2])) {
            mapping.load(new FileInputStream(args[2]));
         } else {
             mapping.load(NodeGenerator.class.getClassLoader().getResourceAsStream("simplemapping.properties"));
         }
 
 
         final ResourceXMLGenerator gen;
        if (args.length > 3 && !"-".equals(args[3])) {
            outfile = new File(args[3]);
             gen = new ResourceXMLGenerator(outfile);
         } else {
             //use stdout
             gen = new ResourceXMLGenerator(System.out);
         }
         ArrayList<String> params=new ArrayList<String>();
        if(args.length>4){
            for (int i=4;i<args.length;i++) {
                 params.add(args[i]);
             }
         }
 
 
        Set<Instance> instances = performQuery(credentials, endPoint, params);
 
         for (final Instance inst : instances) {
             final INodeEntry iNodeEntry = instanceToNode(inst, mapping);
             if(null!=iNodeEntry){
                 gen.addNode(iNodeEntry);
             }
         }
         gen.generate();
         //
         if (null != outfile) {
             System.out.println("XML Stored: " + outfile.getAbsolutePath());
         }
     }
 
    private static Set<Instance> performQuery(AWSCredentials credentials, final String endPoint, final ArrayList<String> filterParams) {
         AmazonEC2Client ec2 = new AmazonEC2Client(credentials);
        if(null!=endPoint && !"".equals(endPoint) && !"-".equals(endPoint)){
            ec2.setEndpoint(endPoint);
        }
 
         //create "running" filter
         ArrayList<Filter> filters = new ArrayList<Filter>();
         Filter filter = new Filter("instance-state-name").withValues(InstanceStateName.Running.toString());
         filters.add(filter);
 
         if(null!=filterParams){
             for (final String filterParam : filterParams) {
                 String[] x=filterParam.split("=",2);
                 if(!"".equals(x[0]) && !"".equals(x[1])) {
                     filters.add(new Filter(x[0]).withValues(x[1]));
                 }
             }
         }
         DescribeInstancesRequest request = new DescribeInstancesRequest().withFilters(filters);
 
         DescribeInstancesResult describeInstancesRequest = ec2.describeInstances(request);
         List<Reservation> reservations = describeInstancesRequest.getReservations();
         Set<Instance> instances = new HashSet<Instance>();
 
         for (final Reservation reservation : reservations) {
             instances.addAll(reservation.getInstances());
         }
         return instances;
     }
 
     public static INodeEntry instanceToNode(final Instance inst, final Properties mapping) throws GeneratorException {
         String hostSel = mapping.getProperty("hostname.selector");
         String host = applySelector(inst, hostSel, mapping.getProperty("hostname.default"));
         if(null==host) {
             System.err.println("Unable to determine hostname for instance: " + inst.getInstanceId());
             return null;
         }
         String nameSel = mapping.getProperty("name.selector");
         String name = applySelector(inst, nameSel, mapping.getProperty("name.default"));
         if(null==name){
             name=host;
         }
         NodeEntryImpl node = new NodeEntryImpl(host, name);
         String descSel = mapping.getProperty("description.selector");
         String desc = applySelector(inst, descSel, mapping.getProperty("description.default"));
         node.setDescription(desc);
 
         for (final String prop : ResourceXMLConstants.nodeProps) {
             final String value = applySelector(inst, mapping.getProperty(prop + ".selector"),
                 mapping.getProperty(prop + ".default"));
             if (null != value) {
                 try {
                     BeanUtils.setProperty(node, prop, value);
                 } catch (Exception e) {
                     throw new GeneratorException(e);
                 }
             }
         }
         String[] attrProps = new String[]{ResourceXMLConstants.NODE_REMOTE_URL, ResourceXMLConstants.NODE_EDIT_URL};
         for (final String attrProp : attrProps) {
             final String value = applySelector(inst, mapping.getProperty(attrProp + ".selector"),
                 mapping.getProperty(attrProp + ".default"));
             if (null != value) {
                 if(null==node.getAttributes()) {
                     node.setAttributes(new HashMap<String, String>());
                 }
                 node.getAttributes().put(attrProp, value);
             }
         }
 
         Pattern settingPat = Pattern.compile("^setting\\.(.+?)\\.selector$");
         //evaluate setting selectors
         for (final Object o : mapping.keySet()) {
             String key = (String) o;
             String selector = mapping.getProperty(key);
             Matcher m = settingPat.matcher(key);
             if (m.matches()) {
                 String setName = m.group(1);
                 if (null == node.getAttributes()) {
                     node.setAttributes(new HashMap<String, String>());
                 }
                 final String value = applySelector(inst, selector, mapping.getProperty(
                     "setting." + setName + ".default"));
                 if (null != value) {
                     //use nodename-settingname to make the setting unique to the node
                     node.getAttributes().put(setName, value);
                 }
             }
         }
         //evaluate single settings.selector=tags/* mapping
         if ("tags/*".equals(mapping.getProperty("settings.selector"))) {
             //iterate through instance tags and generate settings
             for (final Tag tag : inst.getTags()) {
                 if (null == node.getAttributes()) {
                     node.setAttributes(new HashMap<String, String>());
                 }
                 node.getAttributes().put(tag.getKey(), tag.getValue());
             }
         }
         //evaluate single settings.selector=tags/* mapping
         if ("tags/*".equals(mapping.getProperty("attributes.selector"))) {
             //iterate through instance tags and generate settings
             for (final Tag tag : inst.getTags()) {
                 if (null == node.getAttributes()) {
                     node.setAttributes(new HashMap<String, String>());
                 }
                 node.getAttributes().put(tag.getKey(), tag.getValue());
             }
         }
         if(null!=mapping.getProperty("tags.selector")){
             final String selector = mapping.getProperty("tags.selector");
             final String value = applySelector(inst, selector, mapping.getProperty("tags.default"));
             if(null!=value){
                 final String[] values = value.split(",");
                 final HashSet<String> tagset = new HashSet<String>();
                 for (final String s : values) {
                     tagset.add(s.trim());
                 }
                 if (null == node.getTags()) {
                     node.setTags(tagset);
                 }else {
                     node.getTags().addAll(tagset);
                 }
             }
         }
 
         //apply specific tag selectors
         Pattern tagPat = Pattern.compile("^tag\\.(.+?)\\.selector$");
         //evaluate tag selectors
         for (final Object o : mapping.keySet()) {
             String key = (String) o;
             String selector = mapping.getProperty(key);
             //split selector by = if present
             String[] selparts = selector.split("=");
             Matcher m = tagPat.matcher(key);
             if (m.matches()) {
                 String tagName = m.group(1);
                 if (null == node.getAttributes()) {
                     node.setAttributes(new HashMap<String, String>());
                 }
                 final String value = applySelector(inst, selparts[0], null);
                 if (null != value) {
                     if (selparts.length > 1 && !value.equals(selparts[1])) {
                         continue;
                     }
                     //use add the tag if the value is not null
                     if (null == node.getTags()) {
                         node.setTags(new HashSet());
                     }
                     node.getTags().add(tagName);
                 }
             }
         }
 
         //apply attribute selectors
 
         Pattern attribPat = Pattern.compile("^attribute\\.(.+?)\\.selector$");
         //evaluate setting selectors
         for (final Object o : mapping.keySet()) {
             String key = (String) o;
             String selector = mapping.getProperty(key);
             Matcher m = attribPat.matcher(key);
             if (m.matches()) {
                 String attrName = m.group(1);
                 if (null == node.getAttributes()) {
                     node.setAttributes(new HashMap<String, String>());
                 }
                 final String value = applySelector(inst, selector, mapping.getProperty(
                     "attribute." + attrName + ".default"));
                 if (null != value) {
                     //use nodename-settingname to make the setting unique to the node
                     node.getAttributes().put(attrName, value);
                 }
             }
         }
         return node;
     }
 
     public static String applySelector(Instance inst, String selector, String defaultValue) throws GeneratorException {
         if (null != selector && selector.startsWith("tags/")) {
             String tag = selector.substring("tags/".length());
             final List<Tag> tags = inst.getTags();
             for (final Tag tag1 : tags) {
                 if (tag.equals(tag1.getKey())) {
                     return tag1.getValue();
                 }
             }
         } else if (null != selector) {
             try {
                 final String value = BeanUtils.getProperty(inst, selector);
                 if(null!=value){
                     return value;
                 }
             } catch (Exception e) {
                 throw new GeneratorException(e);
             }
         }
 
         return defaultValue;
     }
 
     public static class GeneratorException extends Exception {
         public GeneratorException() {
         }
 
         public GeneratorException(String message) {
             super(message);
         }
 
         public GeneratorException(String message, Throwable cause) {
             super(message, cause);
         }
 
         public GeneratorException(Throwable cause) {
             super(cause);
         }
     }
 
 
     public void writeNodeElement(Writer out, Map<String, String> data) throws IOException {
         out.write("<node ");
         //write attribs
 
 
         out.write(">\n");
 
 
         //write content
 
         //write close
         out.write("</node>\n");
     }
 }
