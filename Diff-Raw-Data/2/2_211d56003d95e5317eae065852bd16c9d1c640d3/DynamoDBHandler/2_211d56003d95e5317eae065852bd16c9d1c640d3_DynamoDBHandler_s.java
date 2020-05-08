 package de.fhb.trendsys.amazonfunctions.dynamodb;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
 import com.amazonaws.regions.Region;
 import com.amazonaws.regions.Regions;
 import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
 import com.amazonaws.services.dynamodbv2.model.AttributeValue;
 import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
 import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
 import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
 import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
 import com.amazonaws.services.dynamodbv2.model.GetItemResult;
 import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
 import com.amazonaws.services.dynamodbv2.model.TableDescription;
 import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
 
 /**
  * Diese Klasse stellt CRUD-Funktionen fr die Amazon DynamoDB zur Verfgung.
  * Fr die genauere Funktionsweise siehe {@link http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/}.
  * 
  * @author Frank Mertens
  */
 public class DynamoDBHandler {
 	private AmazonDynamoDBClient ddbClient;
 	private String selectedTable;
 	public static final Region DEFAULT_REGION = Region.getRegion(Regions.EU_WEST_1);
 	
 	/**
 	 * Erzeugt einen Handler, der CRUD-Operationen fr eine Tabelle in der Amazon DynamoDB zur Verfgung stellt.
 	 * Die zu verwendene Tabelle muss bereits angelegt und im Status "ACTIVE" sein.
 	 * 
 	 * @param region Standort der DynamoDB, zB Regions.EU_WEST_1
 	 * @param tableName Tabelle, auf der gearbeitet werden soll.
 	 * @see DynamoDBHandler#selectTable(String)
 	 * @see DynamoDBHandler#getTableStatus()
 	 */
 	public DynamoDBHandler(Regions region, String tableName) {
		AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
 				
 		Region currentRegion = Region.getRegion(region);
 		ddbClient.setRegion(currentRegion);		
 		
 		selectTable(tableName);
 	}
 	
 	/**
 	 * ndert die Tabelle, auf der gearbeitet werden soll.
 	 * Die Tabelle muss bereits angelegt und im Status "ACTIVE" sein.
 	 * 
 	 * @param tableName Tabelle, auf der gearbeitet werden soll.
 	 * @see DynamoDBHandler#getTableStatus()
 	 */
 	public void selectTable(String tableName) {
 		this.selectedTable = tableName;
 	}
 	
 	/**
 	 * Fragt ein Tupel ab.
 	 * 
 	 * @param id Primrschlssel des Tupels
 	 * @return Tupel
 	 * @see DynamoDBHandler#addItem(int, Map)
 	 * @see DynamoDBHandler#deleteItem(int)
 	 * @see DynamoDBHandler#updateItem(int, Map)
 	 */
 	public Map<String, AttributeValue> getItem(int id) {
 		Map<String, AttributeValue> key = constructNewItem(id);
 		GetItemRequest request = new GetItemRequest()
 									 .withTableName(selectedTable)
 									 .withKey(key);
 		
 		GetItemResult result = ddbClient.getItem(request);
 		
 		return result.getItem();		
 	}
 	
 	/**
 	 * Lscht ein Tupel.
 	 * 
 	 * @param id Primrschlssel des Tupels
 	 * @see DynamoDBHandler#addItem(int, Map)
 	 * @see DynamoDBHandler#getItem(int)
 	 * @see DynamoDBHandler#updateItem(int, Map)
 	 */
     public void deleteItem(int id) {
     	Map<String, AttributeValue> key = constructNewItem(id);
 		DeleteItemRequest request = new DeleteItemRequest()
         								.withTableName(this.selectedTable)
         								.withKey(key);
 
     	ddbClient.deleteItem(request);
     }
     
     /**
      * Speichert ein Tupel in der Datenbank.
      * Exisitert bereits ein Tupel mit derselben ID, so wird dieses berschrieben.
      * 
      * @param id Primrschlssel des Tupels
      * @param content Key/Values des Tupels
 	 * @see DynamoDBHandler#getItem(int)
 	 * @see DynamoDBHandler#deleteItem(int)
 	 * @see DynamoDBHandler#updateItem(int, Map)
      */
     public void addItem(int id, Map<String, AttributeValue> content) {
         Map<String, AttributeValue> item = constructNewItem(id, content);
 
         PutItemRequest putItemRequest = new PutItemRequest()
         									.withTableName(selectedTable)
         									.withItem(item);
         
         ddbClient.putItem(putItemRequest);        
     }
     
     /**
      * Erstellt ein neues Tupel mit seiner ID als einzigen Inhalt.
      * 
      * @param id Primrschlssel des Tupels
      * @return Tupel
      * @see DynamoDBHandler#addItem(int, Map)
      */
     private Map<String, AttributeValue> constructNewItem(int id) {
     	return constructNewItem(id, new HashMap<String, AttributeValue>());
     }
     
     /**
      * Erstellt ein neues Tupel.
      * 
      * @param id Primrschlssel des Tupels
      * @param content Key/Values des Tupels
      * @return Tupel
      * @see DynamoDBHandler#addItem(int, Map)
      */
     private Map<String, AttributeValue> constructNewItem(int id, Map<String, AttributeValue> content) {
     	content.put("id", new AttributeValue()
         				     .withN(Integer.toString(id)));
                 
         return content;
     }
     
     /**
      * Aktualisiert ein Tupel um neue Attribute und deren Inhalte.
      * 
      * @param id Primrschlssel des Tupels
      * @param content Key/Values des Tupels
      * @see DynamoDBHandler#addItem(int, Map)
      * @see DynamoDBHandler#deleteItem(int)
 	 * @see DynamoDBHandler#getItem(int)
 	 * @see DynamoDBHandler#constructUpdateContentItem(Map)
      */
     public void updateItem(int id, Map<String, AttributeValue> content) {
     	Map<String, AttributeValue> key = constructNewItem(id);
     	Map<String, AttributeValueUpdate> value = constructUpdateContentItem(content);
 
         UpdateItemRequest request = new UpdateItemRequest()
         								.withTableName(selectedTable)
         								.withKey(key)
         								.withAttributeUpdates(value);
     	
         ddbClient.updateItem(request);        
     	
     }
     
     /**
      * Konvertiert einen Key/Value-Speicher in ein Format, dass zur Aktualisierung eines Tupel bentigt wird.
      * 
      * @param content Key/Values des Tupels
      * @return Key/ValueUpdates des Tupels
      * @see DynamoDBHandler#updateItem(int, Map)
      */
     private Map<String, AttributeValueUpdate> constructUpdateContentItem(Map<String, AttributeValue> content) {
     	Map<String, AttributeValueUpdate> updatedContent = new HashMap<String, AttributeValueUpdate>();
     	Set<Entry<String, AttributeValue>> contentSet = content.entrySet();
     	Iterator<Entry<String, AttributeValue>> iterator = contentSet.iterator();
     	
     	while (iterator.hasNext()) {
     		Entry<String, AttributeValue> keyValue = iterator.next();
     		
     		String key = keyValue.getKey();
     		AttributeValueUpdate value = new AttributeValueUpdate()
     										 .withValue(keyValue.getValue());
     		
     		updatedContent.put(key, value);
     	}
     	
         return updatedContent;
     }
     
     /**
      * Gibt den aktuellen Status der Tabelle zurck.
      * 
      * @return Status
      * @see DynamoDBHandler#selectTable(String)
      */
 	public String getTableStatus() {
 		DescribeTableRequest request = new DescribeTableRequest()
 										   .withTableName(this.selectedTable);
         
 		TableDescription tableDescription = ddbClient.describeTable(request).getTable();
         
         return tableDescription.getTableStatus();
 	}
 }
