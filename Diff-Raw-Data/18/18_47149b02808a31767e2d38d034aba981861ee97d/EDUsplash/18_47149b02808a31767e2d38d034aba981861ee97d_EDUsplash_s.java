 package bu.edu.cs673.edukid;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import bu.edu.cs673.edukid.db.Database;
 import bu.edu.cs673.edukid.db.model.CategoryType;
 
 public class EDUsplash extends Activity {
 
 	private long SPLASH_DURATION = 3000;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edusplash);
 
 		Thread Timer = new Thread() {
 			public void run() {
 				try {
 					sleep(SPLASH_DURATION);
 					Intent intent = new Intent(EDUsplash.this, EDUkid.class);
 					startActivity(intent);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} finally {
 					finish();
 				}
 			}
 		};
 
 		Timer.start();
 		setupDatabse();
 	}
 
 	private void setupDatabse() {
 		Database database = Database.getInstance(this);
 		Resources resources = getResources();
 
 		if (database.getUserAccounts().size() == 0) {
 			database.addUserAccount("Jasjot",
 					resources.getDrawable(R.drawable.mickey_mouse));
 		}
 
 		if (database.getAllCategories().size() == 0) {
 			database.addCategory(CategoryType.ALPHABET.toString(),
 					resources.getDrawable(R.drawable.tiletry));
 			database.addCategory(CategoryType.NUMBERS.toString(),
 					resources.getDrawable(R.drawable.numbersnew));
 			database.addCategory(CategoryType.SHAPES.toString(),
 					resources.getDrawable(R.drawable.shapesnew));
 			database.addCategory(CategoryType.COLORS.toString(),
 					resources.getDrawable(R.drawable.colorsnew));
 		}
 	if (database.getLetters().size()==0)
 		{
 			database.addLetters("A");
 			database.addLetters("tst");
 			database.addLetters("C");
 			database.addLetters("D");
 			database.addLetters("E");
 			database.addLetters("F");
 			database.addLetters("G");
 			database.addLetters("H");
 			database.addLetters("I");
 			database.addLetters("J");
 			database.addLetters("K");
 			database.addLetters("L");
 			database.addLetters("M");
 			database.addLetters("N");
 			database.addLetters("O");
 			database.addLetters("P");
 			database.addLetters("Q");
 			database.addLetters("R");
 			database.addLetters("S");
 			database.addLetters("T");
 			database.addLetters("U");
 			database.addLetters("V");
 			database.addLetters("W");
 			database.addLetters("X");
 			database.addLetters("Y");
 			database.addLetters("Z");
 		}
 		if (database.getThemes().size()==0)
 		{
			database.addThemes("FRUITS");
 			database.addLetters("ANIMALS");
 			
 			
 		}
 		
 	}
 }
