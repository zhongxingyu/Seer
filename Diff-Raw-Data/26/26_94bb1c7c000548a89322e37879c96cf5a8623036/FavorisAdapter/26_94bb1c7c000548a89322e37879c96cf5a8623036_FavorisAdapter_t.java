 package com.example.monappli;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 
 public class FavorisAdapter extends BaseAdapter {
 	LayoutInflater inflater;
 	ImageLoader imageLoader;
 	List<Result> results;
 	List<Result> origresults;
 	SharedPreferences settings;
 	SharedPreferences.Editor editor;
 	String favoris;
 	ImageView addFavoris;
 	Result res;
 	Context currentContext;
 	View vue;
	ViewHolder holder;
 	
 
 	public static final String TAG = "MyActivity";
 
 	public FavorisAdapter(Context context, List<Result> objects) {
 		super();
 		inflater = LayoutInflater.from(context);
 		this.results = objects;
 		this.origresults = objects;
 
 		imageLoader = ImageLoader.getInstance();
 		// Initialize ImageLoader with configuration. Do it once.
 		imageLoader.init(ImageLoaderConfiguration.createDefault(context));
 
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 	
		
 		settings = parent.getContext().getSharedPreferences("PRIVATE_FAVORIS",
 				0);
 		editor = settings.edit();
 		favoris = settings.getString("favoris", "");
 
 		Log.d(TAG, favoris);
 		Log.d(TAG, "passFav");
 		if (convertView == null) {
 			Log.v("test", "convertView is null");
 			holder = new ViewHolder();
 			convertView = inflater.inflate(R.layout.item_favoris, null);
 			holder.name = (TextView) convertView.findViewById(R.id.name);
 			holder.quartier = (TextView) convertView.findViewById(R.id.car);
 			holder.secteur = (TextView) convertView.findViewById(R.id.sec);
 			holder.image = (ImageView) convertView.findViewById(R.id.image);
 			holder.favoris = (ImageView) convertView.findViewById(R.id.favoris);
 			holder.delFavoris=(ImageView) convertView.findViewById(R.id.delFavoris);
 			convertView.setTag(holder);
 		} else {
 			Log.v("test", "convertView is not null");
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		holder = (ViewHolder) convertView.getTag();
 		
 		currentContext = convertView.getContext();
 		res = results.get(position);
 		holder.name.setText(res.getName());
 		holder.quartier.setText(res.getQuartier());
 		holder.secteur.setText(res.getSecteur());
 		holder.favoris.setImageResource(R.drawable.ic_favoris);
 		holder.delFavoris.setImageResource(R.drawable.delete_icon);
		holder.delFavoris.setId(res.getId());
 		holder.delFavoris.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentContext);
 
 						// set title
 						alertDialogBuilder.setTitle("Alerte !!");
 						vue=v;
 						// set dialog message
 						alertDialogBuilder
 							.setMessage("Voulez-vous supprimer le favoris?")
 							.setCancelable(false)
 							.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,int id) {
 									// if this button is clicked, close
 									// current activity
 									favoris=favoris.replace(";" + String.valueOf(vue.getId())
 											,"");
 									Log.d(TAG, favoris);
 									editor.putString("favoris", favoris);
 									editor.commit();
									
									for(int i=0;i<results.size();i++)  
								    {  
								              
								                if(results.get(i).getId()==vue.getId())  
								                {      
								                   Toast.makeText(currentContext,
															results.get(i).getName() + " a t supprim.",
															Toast.LENGTH_SHORT).show();
								                   results.remove(i);
								                }
								    }  
								          //i=0;  hope this one will be a dirt code  
    
 									notifyDataSetChanged();
 								}
 							  })
 							.setNegativeButton("No",new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,int id) {
 									dialog.cancel();
 								}
 							});
 			 
 							// create alert dialog
 							AlertDialog alertDialog = alertDialogBuilder.create();
 			 
 							// show it
 							alertDialog.show();
 					
 				}
 			});
 
 
 		imageLoader.displayImage(res.getUrlImage(), holder.image);
 		return convertView;
 	}
 
 	private class ViewHolder {
 		TextView name;
 		TextView quartier;
 		TextView secteur;
 		ImageView image;
 		ImageView favoris;
 		ImageView delFavoris;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return results.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		// TODO Auto-generated method stub
 		return results.get(arg0);
 	}
 
 	@Override
 	public long getItemId(int arg0) {
 		// TODO Auto-generated method stub
 		return arg0;
 	}
 
 	public void filter(String filter) {
 		List<Result> filteredArrList = new ArrayList<Result>();
 		results = origresults;
 		if (filter != null || filter.length() != 0) {
 
 			for (int i = 0; i < results.size(); i++) {
 				Result data = results.get(i);
 				if (data.getName().toLowerCase().startsWith(filter.toString())
 						|| data.getSecteur().toLowerCase()
 								.startsWith(filter.toString())
 						|| data.getQuartier().toLowerCase()
 								.startsWith(filter.toString())) {
 					filteredArrList.add(data);
 					Log.d(TAG, "*** data: " + data.toString());
 				}
 			}
 
 		} else {
 			filteredArrList.clear();
 			for (int i = 0; i < origresults.size(); i++) {
 				filteredArrList.add(origresults.get(i));
 			}
 
 		}
 		this.results = filteredArrList;
 		notifyDataSetChanged();
 
 	}
 	
 
 }
