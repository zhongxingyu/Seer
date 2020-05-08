 package sw.app_250grice;
 
 import java.util.List;
 
 import sw.app_database.*;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.database.sqlite.SQLiteDatabase;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	protected List<Item> listItem;
 	protected List<Page> listPage;
 	DatabaseManager manager;
 	DatabaseHelper helper;
 	protected SQLiteDatabase db;
 	PageHandler pageHandler;
 	TextView tv;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		System.out.println(savedInstanceState);
 		setContentView(R.layout.activity_main);		
 		
 		if(savedInstanceState == null)
 			loadData();
		
		//createDummyDate();
 				
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		
 		tv = (TextView)findViewById(R.id.text_view);
 		String toDisplay = "";
 		tv.setText(toDisplay);
 
 		for (Page toShowPage : this.pageHandler.getPages()) {
 			toDisplay += toShowPage.toString() + "\n";
 			for(Item toShowItem : toShowPage.getItems() )
 				toDisplay+= toShowItem.toString() + "\n";
 			
 		}
 		tv.setText(toDisplay);	
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	private void createDummyDate() {
 		Page toAddPage = new Page("P1");
 		Item toAddItem = new Item("Testitem", 12.12, Units.GRAMM);
 		toAddPage.addItem(toAddItem);
 		toAddItem = new Item("Testitem2", 4.3);
 		toAddPage.addItem(toAddItem);
 		
 		this.manager.setPage(toAddPage);
 		for(Item items : toAddPage.getItems())
 			this.manager.setItem(items);
 		
 		listPage = this.manager.getPages();
 		for (Page toShowPage : listPage) {
 			System.out.println("PAGE: " + toShowPage.toString());
 			for(Item toShowItem : toShowPage.getItems() )
 					System.out.println("ITEM: " + toShowItem.toString());
 			
 		}		
 	}
 	
 	private void loadData() {
 		this.pageHandler = PageHandler.getPageHandler();
 		
 		// DatabaseHelper for first dropping Tables then creating new Tables for Pages and Items
 		this.helper = new DatabaseHelper(getApplicationContext(), db);
 
 		// DatabaseManager for accessing Database contents
 		this.manager = new DatabaseManager(this.helper);		
 		
 		this.manager.buildPageHandler();
 		
 		for (Page toShowPage : this.pageHandler.getPages()) {
 			System.out.println("PAGE: " + toShowPage.toString());
 			for(Item toShowItem : toShowPage.getItems() )
 					System.out.println("ITEM: " + toShowItem.toString());
 			
 		}
 	}
 
 }
