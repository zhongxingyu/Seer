 package ws.wiklund.beerguide.activities;
 
 import ws.wiklund.beerguide.db.BeerDatabaseHelper;
 import ws.wiklund.guides.activities.ModifyBeverageActivity;
 import ws.wiklund.guides.db.BeverageDatabaseHelper;
 
 public class ModifyBeerActivity extends ModifyBeverageActivity {
 
 	@Override
 	protected BeverageDatabaseHelper getDatabaseHelper() {
 		return new BeerDatabaseHelper(this);
 	}
 	
 }
