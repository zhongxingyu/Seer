 package ca.charland.db;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.database.Cursor;
 
 public class DateListDataSource extends DataSource {
 
 	public DateListDataSource(DatabaseConnection helper) {
 		super(helper);
 	}
 
 	@Override
 	public Data convertToAbstractData(Cursor cursor) {
 		BaseData td = getData();
 		td.setId(cursor.getInt(DataTable.Column.ID.ordinal()));
 		td.setPerson(cursor.getInt(DataTable.Column.PERSON.ordinal()));
 
 		long rawDate = cursor.getLong(DataTable.Column.DATE.ordinal());
 		td.setDate(new Date(rawDate));
 		return td;
 	}
 
 	protected BaseData getData() {
 		return new BaseData();
 	}
 
 	@Override
 	public List<String> getAllColumns() {
 		List<String> columns = new ArrayList<String>();
 		columns.add(DataTable.ID_COLUMN_NAME);
 		columns.add(DataTable.PERSON_COLUMN_NAME);
 		columns.add(DataTable.DATE_COLUMN_NAME);
 		return columns;
 	}
 }
