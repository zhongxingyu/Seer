 /**
  * 
  */
 package edu.thu.keg.mdap_impl.datamodel;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.naming.OperationNotSupportedException;
 
 import edu.thu.keg.mdap.datamodel.DataContent;
 import edu.thu.keg.mdap.datamodel.DataField;
 import edu.thu.keg.mdap.datamodel.DataSet;
 import edu.thu.keg.mdap.datamodel.Query;
 import edu.thu.keg.mdap.datamodel.Query.Order;
 import edu.thu.keg.mdap.datasetfeature.DataSetFeature;
 import edu.thu.keg.mdap.provider.DataProvider;
 import edu.thu.keg.mdap.provider.DataProviderException;
 
 /**
  * A general implementation of the interface DataSet
  * @author Yuanchao Ma
  *
  */
 public class DataSetImpl implements DataSet {
 
 	private String name = null;
 	private DataProvider provider = null;
 	private String description = null;
 	private boolean loadable;
 	private DataField[] fields;
 	private HashSet<DataSetFeature> features;
 
 	
 	public DataSetImpl(){
 		features = new HashSet<DataSetFeature>();
 	}
 
 
 	public DataSetImpl(String name, String description, DataProvider provider,
 			 boolean loadable, DataField[] fields, DataSetFeature[] features) {
 		super();
 		this.name = name;
 		this.provider = provider;
 		this.loadable = loadable;
 		this.description = description;
 		setDataFields(fields);
 		this.features = new HashSet<DataSetFeature>();
 		for (DataSetFeature feature : features) {
 			this.features.add(feature);
 		}
 	}
 	
 	private void setDataFields(DataField[] fields) {
 		this.fields = fields;
 		for (DataField field : fields) {
 			field.setDataSet(this);
 		}
 	}
 
 
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public boolean isLoadable() {
 		return this.loadable;
 	}
 	@Override
 	public DataField[] getDataFields() {
 		return this.fields;
 	}
 	@Override
 	public DataProvider getProvider() {
 		return this.provider;
 	}
 	
 	@Override
 	public Query getQuery() throws OperationNotSupportedException,
 			DataProviderException {
 		Query q = new QueryImpl(this);
 		return q;
 	}
 	@Override
 	public Set<DataSetFeature> getFeatures() {
 		return features;
 	}
 	@Override
 	public String getDescription() {
 		return this.description;
 	}
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends DataSetFeature> T getFeature(Class<T> type) {
 		for (DataSetFeature feature : features) {
 			if (feature.hasFeatureType(type))
 				return (T) feature;
 		}
 		return null;
 	}
 	@Override
 	public void writeData(DataContent content) throws DataProviderException {
 		this.getProvider().writeDataSetContent(this, content);
 	}
 
 
 	@Override
 	public Query getQuery(Class<? extends DataSetFeature> featureType)
 			throws OperationNotSupportedException, DataProviderException {
 		DataSetFeature feature = this.getFeature(featureType);
 		Query q = this.getQuery();
 		q = q.select(feature.getAllFields());
 		if (feature.getValueFields() != null) {
 			q = q.orderBy(feature.getValueFields()[0], Order.DESC);
 		}
 		return q;
 	}
 
 
 	@Override
 	public DataField getField(String columnName) {
 		for (DataField f : this.fields) {
			if (f.getColumnName() == columnName) 
 				return f;
 		}
		throw new IllegalArgumentException(); 
 	}
 }
