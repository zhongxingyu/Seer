 package ca.charland.tanita;
 
 import roboguice.inject.InjectView;
 import android.content.ContentValues;
 import android.os.Bundle;
 import android.widget.TextView;
 import ca.charland.tanita.db.TanitaDataTable;
 
 /**
  * Since most of the classes have a text view when entering data a specific class was created.
  * 
  * @author mcharland
  * 
  */
 public abstract class AbstractTextViewActivity extends AbstractBaseActivity {
 
 	/**
 	 * The text. This cannot be injected because of a limitation in robo guice not allowing injection from an abstract base class.
 	 */
 	private TextView text;
 
 	/** The enter your text. */
 	@InjectView(R.id.enter_your)
 	private TextView enter;
 
 	/** {@inheritDoc} */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		text = (TextView) findViewById(R.id.editTextEntry);
 
 		String raw = getResources().getString(R.string.enterYour);
 		enter.setText(String.format(raw, getResources().getString(getStringID())));
 	}
 
 	/**
 	 * Gets the string id.
 	 * 
 	 * @return the string id
 	 */
 	protected abstract int getStringID();
 
 	/**
 	 * Gets the column name for the table used by the class.
 	 * 
 	 * @return The column name for the table used by the class.
 	 */
 	protected abstract TanitaDataTable.Column getColumnName();
 
 	/** {@inheritDoc} */
 	@Override
 	protected final ContentValues getValues() {
 		ContentValues values = new ContentValues();
 		values.put(getColumnName().toString(), text.getText().toString());
 		return values;
 	}
 }
