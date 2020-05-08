 package me.raspass.abode;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class ImageAdapter extends BaseAdapter
 {
 	private Context context;
 	private List<ResolveInfo> apps = new ArrayList<ResolveInfo>();
 	private PackageManager pm;
 	
 	public ImageAdapter( Context context )
 	{
 		this.context = context;
 		this.pm      = context.getPackageManager();
 
 		Intent intent = new Intent( Intent.ACTION_MAIN, null );
 		       intent.addCategory(  Intent.CATEGORY_LAUNCHER );
 	
 		for ( ResolveInfo app : pm.queryIntentActivities( intent, 0 ) )
 		{
 			int i;
 
 			for ( i = 0; i < apps.size(); i++ )
 			{
 				if ( apps.get( i ).loadLabel( pm ).toString().compareTo( app.loadLabel( pm ).toString() ) > 0 ){ break; }
 			}
 
 			this.apps.add( i, app );
 		}
 	}
 	
 	public View getView( int position, View convertView, ViewGroup parent )
 	{
 		ResolveInfo app = apps.get( position );

		TextView view = ( TextView ) ( convertView == null ? ( ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE ) ).inflate( R.layout.icon, null ) : convertView );

 		Drawable img = app.loadIcon( pm );
 		img.setBounds( 0, 0, 70, 70 );
		view.setCompoundDrawables( null, img, null, null );
 
		view.setText( app.loadLabel( pm ) );

		return view;
 	}
 	
 	public int getCount()
 	{
 		return apps.size();
 	}
  
 	public Object getItem( int position )
 	{
 		context.startActivity( pm.getLaunchIntentForPackage( apps.get( position ).activityInfo.packageName ) );
 
 		return null;
 	}
 
 	public long getItemId( int position )
 	{
 		return 0;
 	}
 }
