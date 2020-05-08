 /**
  * 
  */
 package edu.thu.keg.mdap.datamodel;
 
 
 
 /**
 
  * A general implementation of the interface DataField.
  * This class can be instantiated directly, or sub-classed 
  * to form other specific-purpose fields.
  * @author Yuanchao Ma 
  */
 public class GeneralDataField implements DataField {
 	protected String name;
 	protected String queryName;
 	protected DataSet dataset;
 	protected String desp;
 	protected boolean isKey;
 	protected FieldType type;
 	protected boolean allowNull;
 	protected FieldFunctionality func;
 	private boolean isDim;
 	@Override
 	public String getQueryName() {
 		return this.queryName;
 	}
 	@Override
 	public DataSet getDataSet() {
 		return dataset;
 	}
 	@Override
 	public String getDescription() {
 		return this.desp;
 	}
 	@Override
 	public boolean isKey() {
 		return this.isKey;
 	}
 	/**
 	 * Construct a instance using the given
 	 *   name, type and description, and whether is key
 	 * Initialize the fields of the object
 	 * @param name Name of the column
 	 * @param type FieldType of this field
 	 * @param description Description of the field
 	 * @param isKey Whether the field is key
 	 * @param allowNull Whether the field allows null value
 	 */
 	public GeneralDataField(String name, FieldType type, String description,
 			boolean isKey, boolean allowNull, boolean isDim, FieldFunctionality func) {
 		if (isKey == true && allowNull == true)
 			throw new IllegalArgumentException();
 		this.name = name;
 		this.queryName = name;
 		this.type = type;
 		this.dataset = null;
 		this.desp = description;
 		this.isKey = isKey;
 		this.allowNull = allowNull;
 		this.func = func;
 	}
 	
 	/**
 	 * Construct a instance using the given
 	 *   name, type and description, and whether is key
 	 * Initialize the fields of the object
 	 * @param name Name of the column
 	 * @param type FieldType of this field
 	 * @param description Description of the field
 	 * @param func Functionality of the field
 	 */
 	public GeneralDataField(String name, FieldType type, String description,
 			boolean isKey, FieldFunctionality func) {
 		this.name = name;
 		this.queryName = name;
 		this.type = type;
 		this.dataset = null;
 		this.desp = description;
		this.isKey = isKey;
 		this.allowNull = !isKey;
 		this.func = func;
 		this.isDim = !func.equals(FieldFunctionality.Value);
 	}
 		
 	@Override
 	public FieldType getFieldType() {
 		return this.type;
 	}
 
 	@Override
 	public void setDataSet(DataSet ds) {
 		this.dataset = ds;
 	}
 	@Override
 	public boolean allowNull() {
 		return this.allowNull;
 	}
 	@Override
 	public String getName() {
 		return this.name;
 	}
 	@Override
 	public FieldFunctionality getFunction() {
 		return this.func;
 	}
 	@Override
 	public boolean isDim() {
 		return this.isDim;
 	}
 }
