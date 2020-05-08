 package me.raspass.abode;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.GridView;
 
 public class Main extends Activity implements AdapterView.OnItemClickListener
 {
 	private GridView gridview;
 	
 	@Override
	public void onCreate( Bundle savedInstanceState )
 	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );
         
		gridview = ( GridView ) findViewById( R.id.gridview );
		gridview.setAdapter( new ImageAdapter( this ) );
		gridview.setOnItemClickListener(this);
	}
     
 	public void onItemClick( AdapterView<?> parent, View v, int position, long id )
 	{
 		( ( ImageAdapter ) parent.getAdapter() ).getItem( position );
 	}
 }
