 package ca.charland.tanita;
 
 import ca.charland.tanita.db.TanitaDataTable;
import ca.charland.tanita.manage.FirstActivity;
 import ca.charland.tanita.manage.PersonHomeActivity;
import ca.charland.tanita.manage.PreferencesActivity;
 
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
			return FirstActivity.class;
		} else {
			return PersonHomeActivity.class;
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
