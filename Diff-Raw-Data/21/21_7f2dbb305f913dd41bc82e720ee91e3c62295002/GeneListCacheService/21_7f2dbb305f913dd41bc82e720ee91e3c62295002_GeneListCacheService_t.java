 /*
  * Copyright 2008-2010 Microarray Informatics Team, EMBL-European Bioinformatics Institute
  *
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
  *
  *
  * For further details of the Gene Expression Atlas project, including source code,
  * downloads and documentation, please see:
  *
  * http://gxa.github.com/gxa
  */
 
 package ae3.service;
 
 import ae3.service.structuredquery.AtlasGenePropertyService;
 import ae3.service.structuredquery.AutoCompleteItem;
 import ae3.service.structuredquery.Constants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
import uk.ac.ebi.gxa.index.builder.IndexBuilder;
import uk.ac.ebi.gxa.index.builder.IndexBuilderEventHandler;
import uk.ac.ebi.gxa.index.builder.listener.IndexBuilderEvent;
import uk.ac.ebi.gxa.properties.AtlasProperties;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 
 public class GeneListCacheService implements InitializingBean, IndexBuilderEventHandler, DisposableBean {
     public static final int PAGE_SIZE = 1000;
 
     public static boolean done = false;
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private AtlasGenePropertyService genePropertyService;
     private AtlasProperties atlasProperties;
     private Boolean autoGenerate;
     private IndexBuilder indexBuilder;
 
     public AtlasGenePropertyService getGenePropertyService() {
         return genePropertyService;
     }
 
     public void setGenePropertyService(AtlasGenePropertyService genePropertyService) {
         this.genePropertyService = genePropertyService;
     }
 
     public boolean isAutoGenerate() {
         return autoGenerate;
     }
 
     public void setAutoGenerate(boolean autoGenerate) {
         this.autoGenerate = autoGenerate;
     }
 
     public void setIndexBuilder(IndexBuilder builder) {
         this.indexBuilder = builder;
         builder.registerIndexBuildEventHandler(this);
     }
 
     public void setAtlasProperties(AtlasProperties atlasProperties) {
         this.atlasProperties = atlasProperties;
     }
 
     public void onIndexBuildFinish(IndexBuilder builder, IndexBuilderEvent event) {
         afterPropertiesSet();
     }
 
     public void onIndexBuildStart(IndexBuilder builder) {
 
     }
 
     public void afterPropertiesSet() {
         if(autoGenerate == null)
             autoGenerate = atlasProperties != null && atlasProperties.isGeneListCacheAutoGenerate();
 
         if(autoGenerate)
             new Thread() {
                 @Override
                 public void run() {
                     generate();
                 }
             }.start();
     }
 
     public void generate() {
 
         BufferedOutputStream bfind = null;
 
         try {
             log.info("Gene list cache generation started");
 
             bfind = new BufferedOutputStream(new FileOutputStream(getFileName()));
 
             String letters = "0abcdefghigklmnopqrstuvwxyz";
 
             bfind.write("<r>".getBytes());
 
             for (int i = 0; i != letters.length(); i++) {
                 String prefix = String.valueOf(letters.charAt(i));
 
                 Collection<AutoCompleteItem> Genes = queryIndex(prefix, PAGE_SIZE);
 
                 if (prefix.equals("0")) {
                     prefix = "num";
                 }
 
                 for (AutoCompleteItem j : Genes) {
                     String geneName = j.getValue();
 
                     bfind.write(String.format("<%1$s id=\"%3$s\">%2$s</%1$s>\n", prefix, geneName,
                             j.getId()).getBytes());
                 }
             }
 
             bfind.write("</r>".getBytes());
         }
         catch (Exception ex) {
             log.error("Could not create gene names cache", ex);
         }
         finally {
             if (null != bfind) {
                 try {
                     bfind.close();
                     done = true;
                 }
                 catch (Exception Ex) {
                     //no op
                 }
             }
             log.info("Gene list cache generation finished");
         }
     }
 
     private static String getFileName() {
         String basePath = System.getProperty("java.io.tmpdir");
 
         final String geneListFileName = basePath + File.separator + "geneNames.xml";
 
         return geneListFileName;
     }
 
     public Collection<AutoCompleteItem> getGenes(String prefix, Integer recordCount) throws Exception {
        if (!done || recordCount > PAGE_SIZE) {
             return queryIndex(prefix, recordCount);
        } else {
             Collection<AutoCompleteItem> result = new ArrayList<AutoCompleteItem>();
 
             XPath xpath = XPathFactory.newInstance().newXPath();
 
             if (prefix.equals("0")) {
                 prefix = "num";
             }
 
             String expression = "/r/" + prefix;
             InputSource inputSource = new InputSource(getFileName()); //
 
             Object nodes1 = xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
 
             NodeList nodes = (NodeList) nodes1;
 
             for (int i = 0; i != nodes.getLength(); i++) {
                 Node n = nodes.item(i);
 
                 String name = n.getTextContent();
                 String id = ((Element) n).getAttribute("id");
 
                 Long count = 1L;
 
                 AutoCompleteItem ai = new AutoCompleteItem(name, id, name, count);
 
                 result.add(ai);
             }
 
             return result;
         }
     }
 
     private Collection<AutoCompleteItem> queryIndex(String prefix, Integer recordCount) throws Exception {
 
 
         Collection<AutoCompleteItem> Genes = genePropertyService
                 .autoCompleteValues(Constants.GENE_PROPERTY_NAME, prefix, recordCount, null);
 
         //AZ:2008-07-07 "0" means all numbers
         if (prefix.equals("0")) {
             for (int i = 1; i != 10; i++) {
                 Genes.addAll(genePropertyService.autoCompleteValues(Constants.GENE_PROPERTY_NAME,
                                                                                   String.valueOf(i), recordCount,
                                                                                   null));
             }
 
         }
 
         ArrayList<AutoCompleteItem> result = new ArrayList<AutoCompleteItem>();
 
         for (AutoCompleteItem a : Genes) {
             int iPos = Collections.binarySearch(result, a, new Comparator<AutoCompleteItem>() {
                 public int compare(AutoCompleteItem t1, AutoCompleteItem t2) {
                     return t1.getValue().compareTo(t2.getValue());
                 }
             });
 
             if (iPos < 0) {
                 result.add(-1 * (iPos + 1), a);
             }
         }
 
         return result;
     }
 
     public void destroy() throws Exception {
         if(indexBuilder != null)
             indexBuilder.unregisterIndexBuildEventHandler(this);
     }
 }
 
