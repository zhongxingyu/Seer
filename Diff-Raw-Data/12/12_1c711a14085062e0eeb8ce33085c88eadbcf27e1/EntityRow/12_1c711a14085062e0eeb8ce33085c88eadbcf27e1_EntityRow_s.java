 package sr.entityset;
 
 import java.beans.Introspector;
 import java.util.Arrays;
 
 import sr.entityset.exceptions.InvalidNullValueException;
 import sr.entityset.exceptions.PrimaryKeyConstraintException;
 import sr.entityset.exceptions.RemovedRowAccessException;
 import sr.entityset.exceptions.WrongTypeException;
 
 public class EntityRow extends AbstractModelObject
 {
 	private RowState state = RowState.Detached;
 	private Object[] data = null;
 	private Object[] originalData = null;
 	private EntityTable parentTable = null;
 	
 	public EntityRow(EntityTable parentTable)
 	{
 		this.parentTable = parentTable;
 		this.data = new Object[parentTable.getColumns().size()];
 	}
 	
 	public EntityRow(EntityTable parentTable, Object[] data, RowState state)
 	{
 		this.parentTable = parentTable;
 		this.data = data;
 		this.state = state;
 	}
 
 	public Object getValue(EntityColumn column)
 	{
 		int columnNumber = column.getNumber();
 		return this.getValue(columnNumber);
 	}
 	
 	/**
 	 * Get the value for the row with column number.
 	 * @param columnNumber (the zero based column number).
 	 * @return
 	 */
 	public Object getValue(int columnNumber) {
 		return this.data[columnNumber];
 	}
 	
 	public Object getOriginalValue(EntityColumn column)
 	{
 		int columnNumber = column.getNumber();
 		return this.getOriginalValue(columnNumber);
 	}
 	
 	public Object getOriginalValue(int columnNumber) 
 	{
 		if (this.originalData == null)
 			throw new IllegalStateException(
 					"Can not get original value when row state is '" + this.getState() + "'");
 		
 		return this.originalData[columnNumber];
 	}
 	
 	public void setValue(int columnNumber, Object value) throws 
 		WrongTypeException, RemovedRowAccessException,
 		InvalidNullValueException, PrimaryKeyConstraintException
 	{
 		EntityColumn column = this.parentTable.getColumns().get(columnNumber);
 		this.setValue(column, value);
 	}

 	public void setValue(EntityColumn column, final Object value) 
 			throws WrongTypeException, RemovedRowAccessException, 
 			InvalidNullValueException, PrimaryKeyConstraintException
 	{
 		validateCellModification(column, value);
 		
 		int columnNumber = column.getNumber();
 		Object oldValue = this.data[columnNumber];
 		
 		if ((oldValue == null && value == null) ||
 				(oldValue != null && oldValue.equals(value))) return;
 		
 		if (this.originalData == null)
 			this.originalData = Arrays.copyOf(this.data, this.data.length);
 		
 		this.data[columnNumber] = value;
 		
 		this.firePropertyChange(Introspector.decapitalize(column.getName()), oldValue, value);
 		
 		if (this.getState() != RowState.Detached)
 			this.parentTable.onCellValueModificationCommitted(this, oldValue, column);
 	}
 
 	private void validateCellModification(EntityColumn column, final Object value) 
 			throws WrongTypeException, InvalidNullValueException, RemovedRowAccessException 
 	{
 		if (value != null && !column.getType().equals(value.getClass()))
 			throw new WrongTypeException(column.getType(), value.getClass(), column.getName());
 		
 		if (value == null && column.getAllowNull() == false 
 				&& (this.parentTable.isRejectNullViolations()
 						|| this.parentTable.getPrimaryKeyColumns().contains(column)))
 			throw new InvalidNullValueException(column.getName());
 		
 		if (this.getState() != RowState.Detached)
 			this.parentTable.onCellValueModificationProposed(this, value, column);
 	}
 
 	public RowState getState() {
 		return state;
 	}
 
 	public void setState(final RowState stateParam) {
 		this.state = stateParam;
 	}
 
 	public Object[] getObjectArray() {
 		return this.data;
 	}
 	
 	void setObjectArray(Object[] values) {
 		this.data = values;
 	}
 	
 	public EntityTable getParentTable() {
 		return parentTable;
 	}
 
 	public void acceptChanges() {
 		this.originalData = null;
 	}
 }
