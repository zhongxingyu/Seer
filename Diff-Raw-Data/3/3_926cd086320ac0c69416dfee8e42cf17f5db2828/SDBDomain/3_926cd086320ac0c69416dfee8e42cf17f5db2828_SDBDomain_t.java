 package com.github.vmorev.amazon;
 
 import com.amazonaws.services.simpledb.AmazonSimpleDB;
 import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
 import com.amazonaws.services.simpledb.model.*;
 import com.github.vmorev.amazon.utils.SDBDataHelper;
 import org.apache.commons.beanutils.PropertyUtils;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Valentin_Morev
  * Date: 14.02.13
  */
 public class SDBDomain extends AmazonService {
     private static AmazonSimpleDB sdb;
     private String name;
 
     public static AmazonSimpleDB getSDB() {
         if (sdb == null)
             sdb = new AmazonSimpleDBClient(getCredentials());
         return sdb;
     }
 
     public static void listDomains(ListFunc<String> func) throws Exception {
         String nextToken = null;
         do {
             ListDomainsResult result = getSDB().listDomains((new ListDomainsRequest()).withNextToken(nextToken));
             nextToken = result.getNextToken() == null || result.getNextToken().equals("") ? null : result.getNextToken();
             for (String domainName : result.getDomainNames()) {
                 func.process(domainName);
             }
         } while (nextToken != null);
     }
 
     public SDBDomain(String name) {
         //get name if url was provided instead of name
         if (name.contains("/"))
             name = name.substring(name.lastIndexOf("/") + 1, name.length());
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void createDomain() throws Exception {
         if (!isDomainExists())
             getSDB().createDomain(new CreateDomainRequest(name));
     }
 
     public void deleteDomain() {
         getSDB().deleteDomain(new DeleteDomainRequest(name));
     }
 
     public <T> void listObjects(String query, Class<T> clazz, ListFunc<T> func) throws Exception {
         String nextToken = null;
         do {
             SelectResult result = getSDB().select(new SelectRequest(query).withNextToken(nextToken));
             nextToken = result.getNextToken() == null || result.getNextToken().equals("") ? null : result.getNextToken();
             for (Item item : result.getItems()) {
                 func.process(convertFromSDB(item.getAttributes(), clazz));
             }
 
         } while (nextToken != null);
     }
 
     public <T> void saveObject(String itemName, T entity) throws Exception {
         List<ReplaceableAttribute> attributes = convertToSDB(entity);
         PutAttributesRequest request = new PutAttributesRequest().withDomainName(name).withItemName(itemName).withAttributes(attributes);
         getSDB().putAttributes(request);
     }
 
     public <T> T getObject(String itemName, boolean isReadConsistent, Class<T> clazz) throws Exception {
         GetAttributesRequest request = (new GetAttributesRequest()).withDomainName(name).withItemName(itemName).withConsistentRead(isReadConsistent);
         List<Attribute> attributes = getSDB().getAttributes(request).getAttributes();
         return convertFromSDB(attributes, clazz);
     }
 
     protected <T> List<ReplaceableAttribute> convertToSDB(T obj) throws Exception {
         List<ReplaceableAttribute> attributes = new ArrayList<>();
         List<Field> fields = SDBDataHelper.getClassFields(obj.getClass());
 
         for (Field field : fields) {
             String value = SDBDataHelper.convertValueToString(PropertyUtils.getProperty(obj, field.getName()), field.getType());
            if (value != null)
                attributes.add(new ReplaceableAttribute().withName(field.getName()).withValue(value));
         }
         return attributes;
     }
 
     protected <T> T convertFromSDB(List<Attribute> attributes, Class<T> clazz) throws Exception {
         List<Field> fields = SDBDataHelper.getClassFields(clazz);
         T obj = clazz.newInstance();
 
         for (Field field : fields)
             for (Attribute attribute : attributes)
                 if (attribute.getName().equals(field.getName())) {
                     PropertyUtils.setProperty(obj, field.getName(), SDBDataHelper.convertStringToValue(attribute.getValue(), field.getType()));
                     break;
                 }
         return obj;
     }
 
     protected boolean isDomainExists() throws Exception {
         final boolean[] result = new boolean[1];
         result[0] = false;
 
         listDomains(new ListFunc<String>() {
             public void process(String domainName) {
                 if (domainName.equals(name))
                     result[0] = true;
             }
         });
         return result[0];
     }
 }
