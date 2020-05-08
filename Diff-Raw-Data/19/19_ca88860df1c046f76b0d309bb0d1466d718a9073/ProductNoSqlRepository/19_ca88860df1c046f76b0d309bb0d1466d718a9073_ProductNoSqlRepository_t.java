 package fr.soat.cassandra;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.nio.ByteBuffer;
 
 import me.prettyprint.cassandra.model.CqlQuery;
 import me.prettyprint.cassandra.model.CqlRows;
 import me.prettyprint.cassandra.serializers.BytesArraySerializer;
 import me.prettyprint.cassandra.serializers.DoubleSerializer;
 import me.prettyprint.cassandra.serializers.IntegerSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
 import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
 import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.exceptions.HectorException;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.soat.bean.Product;
 
 /**
  * @author Mouhcine MOULOU
  *
  */
 public class ProductNoSqlRepository extends AbstractNosqlRepository {
 	
 	private static Logger logger = LoggerFactory.getLogger(ProductNoSqlRepository.class);
 	
 	/**
 	 * Column familly Name 
 	 */
	private static final String COLUMN_FAMILLY_NAME = "PRODUCT"; // FIXME
 	
 	/**
 	 * avoid initialize Serializers on local methods. declare them just once as static fields (good practice) 
 	 */
 	private static StringSerializer stringSerializer = StringSerializer.get();
 	private static DoubleSerializer doubleSerializer = DoubleSerializer.get();
 	private static IntegerSerializer integerSerializer = IntegerSerializer.get();
 	private static BytesArraySerializer bytesArraySerializer = BytesArraySerializer.get();
 	
 	private ColumnFamilyTemplate<String, String> columnFamilyTemplate; 
 	
 	public ProductNoSqlRepository(Cluster cluster, Keyspace keyspace) {
 		super(cluster, keyspace);
 		columnFamilyTemplate = new ThriftColumnFamilyTemplate<String, String>(keyspace, COLUMN_FAMILLY_NAME, stringSerializer, stringSerializer);
 	}
 	
 	public ProductNoSqlRepository(String clusterName, String host, String keyspaceName) {
 		super(clusterName, host, keyspaceName);
 	}
 	
 	/**
 	 * @param product
 	 */
 	public void insert(Product product){
 		
 		checkNotNull(product.getRef(), "product.ref must not be null.");
 		
 		logger.info("insert new product: " + product.toString());
 		Mutator<String> mutator = HFactory.createMutator(getKeyspace(), StringSerializer.get());
 		
 		// Same As : HColumn<String, Integer> nameColumn = HFactory.createColumn("NAME", product.getName(), stringSerializer, stringSerializer);
 		HColumn<String, String> nameColumn = HFactory.createStringColumn("NAME", product.getName());
 		
 		HColumn<String, Integer> quantityColumn = HFactory.createColumn("QUANTITY", product.getQuantity(), stringSerializer, integerSerializer);
 		HColumn<String, Double> unitPriceColumn = HFactory.createColumn("UNIT_PRICE", product.getUnitPrice(), stringSerializer, doubleSerializer);
 		
 		mutator.addInsertion(product.getRef(), COLUMN_FAMILLY_NAME,nameColumn)
 				.addInsertion(product.getRef(), COLUMN_FAMILLY_NAME, quantityColumn)
 				.addInsertion(product.getRef(), COLUMN_FAMILLY_NAME, unitPriceColumn);
 
 		mutator.execute();
 	}
 	
 	public Product getByRefCQL(String ref) {
 		
 		checkNotNull(ref, "ref must not be null.");
 		
 		logger.info("Getting product its ref where ref= " + ref + ", using CQL query.");
 		
 		CqlQuery<String, String, byte[]> cqlQuery = new CqlQuery<String, String, byte[]>(getKeyspace(), stringSerializer, stringSerializer, bytesArraySerializer);
 		String query = String.format("Select * from %s where key = '%s' ", COLUMN_FAMILLY_NAME, ref);
 		cqlQuery.setQuery(query);
 		QueryResult<CqlRows<String, String, byte[]>> queryResult = cqlQuery.execute();
 		
 		// use 'checkNotNull' (google.guava) to avoid null test with 'if else' clause
 		CqlRows<String, String, byte[]> cqlRows = checkNotNull(queryResult.get(), "empty result");
 		Row<String, String, byte[]> row = checkNotNull(cqlRows.getByKey(ref), "product with ref:" + ref + "not found");
 		
 		return createProduct(ref, row.getColumnSlice());
 	}
 	
 	public Product getByRef(String ref){
 		
 		checkNotNull(ref, "ref must not be null.");
 		logger.info("Getting product its ref where ref= " + ref);
 		
 		// we use bytesArraySerializer because we have multiple column with different datatypes
 		RangeSlicesQuery<String, String, byte[]> rangeSlicesQuery = HFactory.createRangeSlicesQuery(getKeyspace(), stringSerializer,stringSerializer, bytesArraySerializer);
 		rangeSlicesQuery.setColumnFamily(COLUMN_FAMILLY_NAME);
 		rangeSlicesQuery.setKeys(ref, "");
 		rangeSlicesQuery.setRowCount(1);
 		
 		QueryResult<OrderedRows<String, String, byte[]>> result = rangeSlicesQuery.execute();
 		OrderedRows<String, String, byte[]> orderedRows = result.get();
 
 		Row<String, String, byte[]> row = orderedRows.peekLast();
 		return createProduct(ref, row.getColumnSlice());
 	}
 	
 	public void deleteByRef(String ref) {
 		checkNotNull(ref, "ref must not be null.");
 		logger.info("Deleting product with ref= " + ref);
 		columnFamilyTemplate.deleteRow(ref);
 	}
 	
 	public void deleteByRefCQL(String ref) {
 		
 		checkNotNull(ref, "ref must not be null.");
 		logger.info("Deleting product with ref= " + ref + ", using CQL query");
 		
 		CqlQuery<String, String, byte[]> cqlQuery = new CqlQuery<String, String, byte[]>(getKeyspace(), stringSerializer, stringSerializer, bytesArraySerializer);
 		String query = String.format("Delete from %s where key = '%s' ", COLUMN_FAMILLY_NAME, ref);
 		cqlQuery.setQuery(query);
 		cqlQuery.execute();
 	}
 
 	/**
 	 * @param product
 	 */
 	public void update(Product product) {
 		
 		checkNotNull(product.getRef(), "product.ref must not be null.");
 		logger.info("updating row with ref: " + product.getRef());
 		
 		ColumnFamilyUpdater<String, String> updater = columnFamilyTemplate.createUpdater(product.getRef());
 		updater.setString("NAME", product.getName());
 		updater.setInteger("QUANTITY", product.getQuantity());
 		updater.setDouble("UNIT_PRICE", product.getUnitPrice());
 		try {
 			columnFamilyTemplate.update(updater);
 		} catch (HectorException e) {
 			e.printStackTrace();
 		    throw new RuntimeException("Unable to update product with key: " + product.getRef(), e);
 		}
 	}
 	
 	public void updateCQL(Product product) {
 		
 		checkNotNull(product.getRef(), "product.ref must not be null.");
 		logger.info("updating row with ref: " + product.getRef() + ", using CQL query");
 		
 		CqlQuery<String, String, byte[]> cqlQuery = new CqlQuery<String, String, byte[]>(getKeyspace(), stringSerializer, stringSerializer, bytesArraySerializer);
 		String nonFormatedQuery = "update %s set 'NAME' = '%s', 'QUANTITY' = %s, UNIT_PRICE = %s WHERE KEY = '%s'";
 
 		// FIXME: use serializers ?????
 		String query = String.format(nonFormatedQuery, COLUMN_FAMILLY_NAME, product.getName(), product.getQuantity(), product.getUnitPrice(), product.getRef());
 		cqlQuery.setQuery(query);
 	    cqlQuery.execute();
 	}
 
 	/**
 	 * @param ref
 	 * @param columns
 	 * @return
 	 */
 	private Product createProduct(String ref,
 			ColumnSlice<String, byte[]> columnSlice) {
 		
 		checkNotNull(ref, "ref must not be null.");
 		checkArgument((columnSlice!= null && !columnSlice.getColumns().isEmpty()), "columnsSlice must not be null or empty");
 		
 		String name = new String(columnSlice.getColumnByName("NAME").getValue());
 		int quantity = ByteBuffer.wrap(columnSlice.getColumnByName("QUANTITY").getValue()).getInt();
 		double unitPrice = ByteBuffer.wrap(columnSlice.getColumnByName("UNIT_PRICE").getValue()).getDouble();
 		
 		logger.info("initializing  new product: [REF=' " + ref +"', NAME=' " + name + "', QUANTITY=" + quantity + ", UNIT_PRICE="  + unitPrice);
 		return new Product(ref,	name, quantity, unitPrice);
 	}
 	
 }
