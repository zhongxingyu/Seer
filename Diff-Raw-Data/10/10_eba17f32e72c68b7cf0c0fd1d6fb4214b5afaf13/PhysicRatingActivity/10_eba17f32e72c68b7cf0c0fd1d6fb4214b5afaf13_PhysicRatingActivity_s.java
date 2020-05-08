 package ca.charland.tanita;
 
 import ca.charland.R;
<<<<<<< HEAD
import ca.charland.activity.manage.BasePersonHomeActivity;
=======
 import ca.charland.activity.manage.BaseFirstActivity;
import ca.charland.activity.manage.PersonHomeActivity;
>>>>>>> 8ace915a31c8eda8290e8f7c6f30c1bec02b9a92
 import ca.charland.activity.manage.PreferencesActivity;
 import ca.charland.tanita.db.TanitaDataTable;
import ca.charland.tanita.manage.FirstActivity;
 /**
  * @author mcharland
  */
 public class PhysicRatingActivity extends TextViewActivity {
 
 	@Override
 	protected int getResourceIDForLayout() {
 		return R.layout.physic_rating;
 	}
 
 	@Override
 	public Class<?> getNextClass() {
 		if (PreferencesActivity.isSingleUserModeSet(this)) {
 			return BaseFirstActivity.class;
 		} else {
 			return BasePersonHomeActivity.class;
 		}
 	}
 
 	@Override
 	protected TanitaDataTable.Column getColumnName() {
 		return TanitaDataTable.Column.PHYSIC_RATING;
 	}
 
 	@Override
 	protected int getIDForString() {
 		return R.string.physic_rating;
 	}
 }
