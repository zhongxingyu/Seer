 package com.charlesmadere.android.classygames;
 
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.Toast;
 import com.actionbarsherlock.view.MenuItem;
 
 
 public final class SharkActivity extends BaseActivity
 {
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState, R.string.sharkwork, true);
 		setContentView(R.layout.shark_activity);
 
 		final ImageView sharkwork = (ImageView) findViewById(R.id.shark_activity_sharkwork);
 		sharkwork.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(final View v)
 			{
 				Toast.makeText(SharkActivity.this, R.string.sharkwork_by_tristan_kidder, Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				onBackPressed();
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 }
