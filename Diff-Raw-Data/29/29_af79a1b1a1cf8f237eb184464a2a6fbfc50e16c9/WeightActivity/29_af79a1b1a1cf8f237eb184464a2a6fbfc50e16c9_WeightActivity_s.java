 package ca.charland.tanita;
 
import android.content.Context;
 import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
 import ca.charland.tanita.db.TanitaDataTable;
 
 /**
  * The Class WeightActivity.
  * 
  * @author mcharland
  */
 public class WeightActivity extends AbstractTextViewActivity {
 
 	/** {@inheritDoc} */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
 	}
 	/** {@inheritDoc} */
 	@Override
 	int getLayoutResID() {
 		return R.layout.weight;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public Class<?> getNextClass() {
 		return DailyCaloricIntakeActivity.class;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	protected TanitaDataTable.Column getColumnName() {
 		return TanitaDataTable.Column.WEIGHT;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	protected int getStringID() {
 		return R.string.weight;
 	}
 }
