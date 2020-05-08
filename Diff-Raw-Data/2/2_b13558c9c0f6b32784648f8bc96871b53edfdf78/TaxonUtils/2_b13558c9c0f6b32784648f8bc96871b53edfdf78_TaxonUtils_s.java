 package uk.ac.ebi.fgpt.sampletab.utils;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.concurrent.ExecutionException;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 
 public class TaxonUtils {    
     private static Logger log = LoggerFactory.getLogger("uk.ac.ebi.fgpt.sampletab.utils.TaxonUtils");
    
 
     private static LoadingCache<Integer, String> taxNameCache = CacheBuilder.newBuilder()
         .maximumSize(10000)
         .build(new CacheLoader<Integer, String>() {
             public String load(Integer taxID) throws TaxonException {
                 // TODO add meta information identifying this tool
                 URL url;
                 try {
                     url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=taxonomy&id=" + taxID);
                 } catch (MalformedURLException e) {
                     throw new TaxonException(e);
                 }
                 
                 Document doc = null;
                 try {
                     doc = XMLUtils.getDocument(url);
                 } catch (DocumentException e) {
                     throw new TaxonException(e);
                 } catch (IOException e) {
                     throw new TaxonException(e);
                 }
                 
                 Element root = doc.getRootElement();
                 if (root != null) {
                     Element docsum = XMLUtils.getChildByName(root, "DocSum");
                     if (docsum != null) {
                         for (Element item : XMLUtils.getChildrenByName(docsum, "Item")) {
                             if ("ScientificName".equals(item.attributeValue("Name"))) {
                                 return item.getTextTrim();
                             }
                         }
                     }
                 }
                 //if we got here, we could not find a match
                 throw new TaxonException("Unable to find ScientificName for "+taxID);
             }
         }
     );
 
     private static LoadingCache<String, Integer> taxIDCache = CacheBuilder.newBuilder()
         .maximumSize(10000)
         .build(new CacheLoader<String, Integer>() {
             public Integer load(String taxName) throws TaxonException {
              // TODO add meta information identifying this tool
                 URL url;
                 try {
                     url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=taxonomy&term="
                             + URLEncoder.encode(taxName, "UTF-8"));
                 } catch (UnsupportedEncodingException e) {
                     throw new TaxonException(e);
                 } catch (MalformedURLException e) {
                     throw new TaxonException(e);
                 }
                 Document doc;
                 try {
                     doc = XMLUtils.getDocument(url);
                 } catch (DocumentException e) {
                     throw new TaxonException(e);
                 } catch (IOException e) {
                     throw new TaxonException(e);
                 }
                 Element root = doc.getRootElement();
                 if (root != null) {
                     Element idlist = XMLUtils.getChildByName(root, "IdList");
                     if (idlist != null) {
                         Element id = XMLUtils.getChildByName(idlist, "Id");
                        if (id == null) {
                             return new Integer(id.getTextTrim());
                         }
                     }
                 }
                 throw new TaxonException("Unable to find taxID  for "+taxName);
             }
         }
     );
 
     private static LoadingCache<Integer, String> taxDivisionCache = CacheBuilder.newBuilder()
         .maximumSize(10000)
         .build(new CacheLoader<Integer, String>() {
             public String load(Integer taxID) throws TaxonException {
              // TODO add meta information identifying this tool
                 String URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=taxonomy&id=" + taxID;
                 Document doc = null;
                 try {
                     doc = XMLUtils.getDocument(URL);
                 } catch (DocumentException e) {
                     throw new TaxonException(e);
                 }
                 Element root = doc.getRootElement();
                 if (root == null)
                     throw new TaxonException("Unable to find document root of taxid "+taxID);
                 Element docsum = XMLUtils.getChildByName(root, "DocSum");
                 if (docsum == null)
                     throw new TaxonException("Unable to find DocSum element of taxid "+taxID);
                 for (Element item : XMLUtils.getChildrenByName(docsum, "Item")) {
                     if ("Division".equals(item.attributeValue("Name"))) {
                         return item.getTextTrim();
                     }
                 }
                 //if we got here, we could not find a match
                 throw new TaxonException("Unable to find Division for "+taxID);
             }
         }
     );
 
     public static String getSpeciesOfID(int taxID) throws TaxonException {
         if (taxID < 0){
             throw new IllegalArgumentException();
         }
         
         try {
             return taxNameCache.get(taxID);
         } catch (ExecutionException e) {
             throw new TaxonException(e);
         }
     }
     
     public static String getDivisionOfID(int taxID) throws TaxonException {
         if (taxID < 0){
             throw new IllegalArgumentException();
         }
         
         try {
             return taxDivisionCache.get(taxID);
         } catch (ExecutionException e) {
             throw new TaxonException(e);
         }
     }
 
     public static Integer findTaxon(String species) throws TaxonException {
         species = species.trim();
         //strip out brackets because these are interpreted as special search characters
         species = species.replace("(", " ");
         species = species.replace(")", " ");
         if (species == null || species.length() == 0) {
             throw new IllegalArgumentException();
         }
         try {
             return taxIDCache.get(species);    
         } catch (ExecutionException e) {
             throw new TaxonException(e);
         }
     }
 }
