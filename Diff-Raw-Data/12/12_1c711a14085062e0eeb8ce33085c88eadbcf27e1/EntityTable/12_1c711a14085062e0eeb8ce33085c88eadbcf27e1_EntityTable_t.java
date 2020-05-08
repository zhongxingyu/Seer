 package sr.entityset;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import sr.entityset.constraints.Constraint;
 import sr.entityset.constraints.ConstraintError;
 import sr.entityset.constraints.UniqueConstraint;
 import sr.entityset.exceptions.ConstraintViolationException;
 import sr.entityset.exceptions.InvalidNullValueException;
 import sr.entityset.exceptions.PrimaryKeyConstraintException;
 import sr.entityset.exceptions.RemovedRowAccessException;
 import sr.entityset.exceptions.WrongTypeException;
 import ca.odell.glazedlists.EventList;
 import ca.odell.glazedlists.GlazedLists;
 import ca.odell.glazedlists.ObservableElementList;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 
 public class EntityTable
 {	
 	private String name;
 	private List<EntityColumn> columns = null;
 	private boolean allowStructuralChanges = true;
 	private boolean rejectNullViolations = true;
 	
 	private UniqueConstraint primaryKeyConstraint = null;
 	private List<Constraint> constraints = new ArrayList<Constraint>();
 	private List<Index> indexes = new ArrayList<Index>();
 	
 	private ObservableElementList<EntityRow> visibleRows;
 	private Collection<EntityRow> changedRows;
 	private boolean isLoadingData = false;
 
 	public EntityTable(String name)
 	{
 		this.name = name;
 		
 		EventList<EntityRow> rootlist = GlazedLists.eventList(new ArrayList<EntityRow>());
 		ObservableElementList<EntityRow> observableElementList = 
 				new ObservableElementList<EntityRow>(
 						rootlist, 
 						GlazedLists.beanConnector(EntityRow.class));
 		
 		this.visibleRows = observableElementList;
 		this.changedRows = new HashSet<EntityRow>();
 		this.columns = new ArrayList<EntityColumn>();
 	}
 	
 	public EntityColumn addColumn(String name, Class<?> type, boolean allowNul) 
 			throws InvalidRowStateException	{
 		return this.internalAddColumn(name, type, false, allowNul);
 	}
 	
 	public EntityColumn addColumn(String name, Class<?> type) 
 			throws InvalidRowStateException {
 		return this.internalAddColumn(name, type, false, false);
 	}
 	
 	public EntityColumn addPrimaryKeyColumn(String name, Class<?> type) 
 			throws InvalidRowStateException {
 		return this.internalAddColumn(name, type, true, false);
 	}
 
 	public List<EntityColumn> getColumns() {
 		return columns;
 	}
 	
 	public List<Index> getIndexes() {
 		return indexes;
 	}
 	
 	public void addIndex(String... indexColumnNames) 
 	{
 		EntityColumn[] indexedColumns = Collections2.transform(
 				Arrays.asList(indexColumnNames), 
 				new Function<String, EntityColumn>() {
 					@Override
 					public EntityColumn apply(String arg0) {
 						return getColumn(arg0);
 					}			
 		}).toArray(new EntityColumn[] {});
 		
 		this.indexes.add(new Index(this, indexedColumns));
 	}
 	
 	/***
 	 * Get a column from its name
 	 * @param columnName Name of the column
 	 * @return the column if found, else returns null
 	 */
 	public EntityColumn getColumn(String columnName) 
 	{
 		for(EntityColumn col : this.getColumns()) {
 			if (col.getName().equals(columnName))
 				return col;
 		}
 		
 		return null;
 	}
 
 	public EntityRow newRow() 
 	{
 		EntityRow row = new EntityRow(this);
 		return row;
 	}
 	
 	public void addRow(EntityRow row) throws 
 		InvalidRowStateException, PrimaryKeyConstraintException, 
 		InvalidNullValueException, WrongTypeException
 	{
 		if (row.getState() != RowState.Detached)
 			throw new InvalidRowStateException(
 					"Can not add a row which state is " + row.getState().toString());
 
 		if (!this.isLoadingData)
 			ensureValidatesBeforeAdding(row.getObjectArray());
 		
 		this.internalAddRow(row);
 	}
 	
 	/***
 	 * Warning: adding row with addFastRow bypasses a lot
 	 * of the consistent-check logic, e.g. it does not keep
 	 * track of the state of the rows.
 	 * 
 	 * May result in undefined behavior.
 	 * @param values
 	 */
 	public EntityRow addFastRow(Object... values)
 	{
 		EntityRow row = new EntityRow(this, values, RowState.Added);
 		this.visibleRows.add(row);
 		
 		return row;
 	}
 	
 	public void endAddingFastRow() {
 		this.indexes = new ArrayList<Index>();
 	}
 	
 	public EntityRow addRow(Object... values) throws 
 		IllegalArgumentException, InvalidRowStateException, 
 		PrimaryKeyConstraintException, InvalidNullValueException, 
 		WrongTypeException
 	{
 		if (values.length != this.columns.size())
 			throw new IllegalArgumentException("Passed value array size ("
 					+ values.length + ") is not the same as the number of columns in the table (" 
 					+ this.columns.size() + ")");
 		
 		EntityRow row = this.newRow();
 		row.setObjectArray(values);
 		
 		this.addRow(row);
 		
 		return row;
 	}
 	
 	public void removeRow(EntityRow row) throws InvalidRowStateException {
 		this.internalRemoveRow(row);
 	}
 
 	public Collection<EntityRow> changedRows() {
 		return this.changedRows;
 	}
 
 	public ObservableElementList<EntityRow> rows() {
 		return this.visibleRows;
 	}
	
 	public String getName() {
 		return name;
 	}
	
 	public void acceptChanges() 
 	{
 		for(EntityRow row : this.visibleRows)
 			row.setState(RowState.Unchanged);
 		
 		for(EntityRow changedRow : this.changedRows)
 			changedRow.acceptChanges();
 		
 		this.changedRows.clear();
 	}
 
 	public Collection<EntityColumn> getPrimaryKeyColumns()
 	{
 		if (this.primaryKeyConstraint == null)
 			return new ArrayList<EntityColumn>();
 		else
 			return this.primaryKeyConstraint.getColumns();
 	}
 	
 	public EntityRow findByPrimaryKey(Object singleColumnPrimaryKeyValue) {
 		return this.findByPrimaryKey(new Object[] {singleColumnPrimaryKeyValue});
 	}
 	
 	public EntityRow findByPrimaryKey(Object... primaryKeyValues)
 	{
 		Index pkIndex = this.getPrimaryKeyIndex();
 		List<EntityRow> rows = pkIndex.findRows(primaryKeyValues);
 		
 		if (rows.size() > 1)
 			throw new RuntimeException("Got " + rows.size() 
 					+ " rows returned from primary key index for values '" 
 					+ StringUtils.join(primaryKeyValues, ", ") 
 					+ "' when expecting one or zero.");
 		
 		if (rows.size() == 1)
 			return rows.get(0);
 		else
 			return null;
 	}
 	
 	public void addConstraint(Constraint constraint) {
 		this.constraints.add(constraint);
 	}
 
 	public Index getIndex(Collection<EntityColumn> columns) 
 	{
 		EntityColumn[] requstedColumnsArray = columns.toArray(new EntityColumn[0]);
 		for(Index index : this.indexes)
 		{
 			if (Arrays.equals(requstedColumnsArray, index.getColumns()))			
 				return index;
 		}
 		
 		Index newIndex = new Index(this, requstedColumnsArray);
 		this.indexes.add(newIndex);
 		
 		return newIndex;
 	}
 	
 	public void beginLoadData() 
 	{
 		this.isLoadingData = true;
 		
 		// reset indexes (they won't get updated as we add rows)
 		this.indexes = new ArrayList<Index>();
 	}
 
 	public void endLoadData() 
 	{
 		// verify all constraints
 		for(Constraint constraint : this.constraints)
 		{
 			for(EntityRow row : this.rows())
 			{
 				ConstraintError result = constraint.validateExistingRow(row);
 				throwExceptionIfViolation(constraint, result);
 			}
 		}
 		
 		this.isLoadingData = false;
 	}
 	
 	public EntityColumn getDisplayColumn()
 	{
 		Collection<EntityColumn> validUniqueColumns = Collections2.transform(
 				this.getConstraints(), new Function<Constraint, EntityColumn>() 
 		{
 			@Override public EntityColumn apply(Constraint arg0) {
 				if (arg0 instanceof UniqueConstraint)
 				{
 					UniqueConstraint uniqueConstraint = (UniqueConstraint)arg0;
 					if (uniqueConstraint.getColumns().size() == 1)
 					{
 						EntityColumn uniqueColumn = uniqueConstraint.
 								getColumns().toArray(new EntityColumn[0])[0];
 						if (uniqueColumn.getType().equals(String.class))
 							return uniqueColumn;
 					}
 				}
 				return null;
 			}
 		});
 
 		for(EntityColumn col : this.getColumns())
 		{
 			if (validUniqueColumns.contains(col))
 				if (this.getPrimaryKeyColumns().contains(col))
 				{
 					if (col.getType().equals(String.class))
 						return col;	
 				}
 				else
 					return col;
 		}
 
 		throw new IllegalStateException(
 				"Unable to find a display column for table " 
 						+ this.getName() 
 						+ " (table probably lacks a UniqueConstraint)");
 	}
 	
 	public List<Constraint> getConstraints() {
 		return this.constraints;
 	}
 
 	public boolean isRejectNullViolations() {
 		return rejectNullViolations;
 	}
 
 	public void setRejectNullViolations(boolean rejectNullViolations) 
 	{
 		this.ensureCanDoStructuralChanges();
 		this.rejectNullViolations = rejectNullViolations;
 	}
 	
 	public Index getPrimaryKeyIndex() {
 		return this.getIndex(this.getPrimaryKeyColumns());
 	}
 	
 	public boolean isChanged() {
 		return this.changedRows.size() > 0;
 	}
 	
 	//////////////////////////////////////////////////////////////////////////////////////////////
 
 	void onCellValueModificationProposed(EntityRow modifiedRow, 
 			Object proposedValue, EntityColumn modifiedCellColumn) 
 					throws PrimaryKeyConstraintException, RemovedRowAccessException
 	{
 		if (modifiedRow.getState().equals(RowState.Removed))
 			throw new RemovedRowAccessException();
 		
 		ensureNoConstraintViolationsOnModifying(proposedValue, modifiedCellColumn, modifiedRow);
 		//this.internalModifiedRow(modifiedRow);
 	}
 	
 	void onCellValueModificationCommitted(EntityRow row, Object value, EntityColumn column) {
 		this.internalModifiedRow(row);
 	}
 
 	///////////////////////////////////////////////////////////////////////////////////////////////	
 
 	private void ensureValidatesBeforeAdding(Object[] valueArray) 
 			throws PrimaryKeyConstraintException, 
 			InvalidNullValueException, WrongTypeException 
 	{
 		// check valid null-state and data-type
 		for(EntityColumn column : this.columns)
 		{
 			Object curValue = valueArray[column.getNumber()];
 			
 			boolean isPrimaryKeyField = this.getPrimaryKeyColumns().contains(column);
 			if (curValue == null && column.getAllowNull() == false 
 					&& (this.rejectNullViolations || isPrimaryKeyField))
 				throw new InvalidNullValueException(column.getName());
 			
 			if (curValue != null && curValue.getClass().equals(column.getType()) == false)
 				throw new WrongTypeException(column.getType(), curValue.getClass(), column.getName());
 		}
 		
 		ensureNoConstraintViolationsOnAdding(valueArray);
 	}
 
 	private void ensureNoConstraintViolationsOnAdding(Object[] valueArray) 
 	{
 		for(Constraint constraint : this.constraints)
 		{
 			ConstraintError result = constraint.validatePropositionOnRowAdding(valueArray);
 			throwExceptionIfViolation(constraint, result);
 		}
 	}
 
 	private void throwExceptionIfViolation(Constraint constraint, ConstraintError result) 
 	{
 		if (result != null)
 			if (constraint == this.primaryKeyConstraint)
 				throw new PrimaryKeyConstraintException(result.getMessage());
 			else
 				throw new ConstraintViolationException(result.getMessage());
 	}
 	
 	private void ensureNoConstraintViolationsOnModifying(
 			Object proposedValue, EntityColumn modifyingColumn, EntityRow modifyingRow) 
 	{
 		for(Constraint constraint : this.constraints)
 		{
 			ConstraintError result = constraint.
 					validatePropositionOnRowModifiying(
 							modifyingRow, 
 							proposedValue, 
 							modifyingColumn);
 			throwExceptionIfViolation(constraint, result);
 		}
 	}
 	
 	private void ensureNoConstraintViolationsOnRemoving(EntityRow removingRow) 
 	{
 		for(Constraint constraint : this.constraints)
 		{
 			ConstraintError result = constraint.
 					validatePropositionOnRowRemoving(removingRow);
 			if (result != null)
 				throw new ConstraintViolationException(result.getMessage());
 		}
 	}
 	
 	private EntityColumn internalAddColumn(String name, Class<?> type,
 			boolean isPrimaryKey, boolean allowNull)
 	{
 		ensureCanDoStructuralChanges();
 		
 		EntityColumn col = new EntityColumn(this, 
 				this.columns.size(), name, type, allowNull);
 		this.columns.add(col);
 		
 		if (isPrimaryKey)
 			this.internalAddPrimaryKeyColumn(col);
 
 		return col;
 	}
 
 	private void internalAddPrimaryKeyColumn(EntityColumn col) 
 	{
 		Collection<EntityColumn> incumbentPrimaryKeyColumns = null;
 		
 		if (this.primaryKeyConstraint == null)
 			incumbentPrimaryKeyColumns = new ArrayList<EntityColumn>();
 		else
 		{
 			incumbentPrimaryKeyColumns = this.primaryKeyConstraint.getColumns();
 			this.constraints.remove(this.primaryKeyConstraint);
 		}
 		
 		incumbentPrimaryKeyColumns.add(col);
 		this.primaryKeyConstraint = new UniqueConstraint(
 				"PrimaryKeyConstraint", this, incumbentPrimaryKeyColumns, true);
 		this.constraints.add(this.primaryKeyConstraint);
 	}
 
 	private void internalAddRow(EntityRow row)
 	{
 		this.allowStructuralChanges = false;
 		this.visibleRows.add(row);
 		
 		for(Index index : indexes)
 			index.updateOnRowAdded(row);
 		
 		row.setState(RowState.Added);
 		this.changedRows.add(row);
 	}
 	
 	private void internalModifiedRow(EntityRow row) 
 	{
 		for(Index index : indexes)
 			index.updateOnRowModified(row);
 		
 		// if row state is Added: do nothing
 		if (row.getState().equals(RowState.Unchanged))
 		{
 			row.setState(RowState.Modified);
 			this.changedRows.add(row);
 		}
 	}
 	
 	private void internalRemoveRow(EntityRow row) throws InvalidRowStateException
 	{
 		if (row.getState().equals(RowState.Detached))
 			throw new InvalidRowStateException(
 					"Can not remove detached row");
 		
 		this.ensureNoConstraintViolationsOnRemoving(row);
 		
 		this.visibleRows.remove(row);
 		
 		if (row.getState().equals(RowState.Added)) 
 		{
 			this.changedRows.remove(row);
 			row.setState(RowState.Detached);
 		}
 		else if (row.getState().equals(RowState.Unchanged)) 
 		{
 			this.changedRows.add(row);
 			row.setState(RowState.Removed);
 		}
 		else if (row.getState().equals(RowState.Modified)) {
 			row.setState(RowState.Removed);
 		}
 		
 		for(Index index : indexes)
 			index.updateOnRowRemoved(row);
 	}
 	
 	private void ensureCanDoStructuralChanges() 
 	{
 		if (this.allowStructuralChanges == false)
 			throw new IllegalStateException("Can not change table structure "
 					+ "after table started handling data.");
 	}
 }
