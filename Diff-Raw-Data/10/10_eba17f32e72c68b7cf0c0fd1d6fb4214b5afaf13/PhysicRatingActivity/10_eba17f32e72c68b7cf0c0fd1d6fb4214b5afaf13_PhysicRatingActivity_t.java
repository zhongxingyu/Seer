 package ca.charland.tanita;
 
 import ca.charland.R;
 import ca.charland.activity.manage.BaseFirstActivity;
import ca.charland.activity.manage.BasePersonHomeActivity;
 import ca.charland.activity.manage.PreferencesActivity;
 import ca.charland.tanita.db.TanitaDataTable;

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
