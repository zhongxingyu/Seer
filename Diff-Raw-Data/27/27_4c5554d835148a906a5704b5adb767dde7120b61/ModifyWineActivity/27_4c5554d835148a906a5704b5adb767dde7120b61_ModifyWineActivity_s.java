 package ws.wiklund.vinguiden.activities;
 
 import ws.wiklund.guides.activities.ModifyBeverageActivity;
 import ws.wiklund.guides.db.BeverageDatabaseHelper;
 import ws.wiklund.vinguiden.db.WineDatabaseHelper;
 
 public class ModifyWineActivity extends ModifyBeverageActivity {
 
 	@Override
 	protected BeverageDatabaseHelper getDatabaseHelper() {
 		return new WineDatabaseHelper(this);
 	}
 	
 }
