 package de.tobiasfiebiger.mobile.teachapp.widget;
 
 import android.app.Activity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 import de.tobiasfiebiger.mobile.teachapp.R;
 import de.tobiasfiebiger.mobile.teachapp.TeachingApp;
 import de.tobiasfiebiger.mobile.teachapp.model.Material;
 
 public class MaterialAdapter extends MasterAdapter<Material> {
 
   private static final String TAG = "MaterialAdapter";
 
   public MaterialAdapter(Activity context) {
 	super(context);
   }
 
   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
 	if (inflater != null) {
 	  Material currentMaterial = dataObjects.get(position);
 	  MaterialViewHolder holder;
 	  if (convertView == null) {
 		convertView = inflater.inflate(R.layout.grid_item_material, null);
 
 		holder = new MaterialViewHolder();
 		holder.text = (TextView) convertView.findViewById(R.id.material_text);
 		holder.image = (AsyncImageView) convertView.findViewById(R.id.material_image);
 
 		convertView.setTag(holder);
 	  } else {
 		holder = (MaterialViewHolder) convertView.getTag();
 	  }
 
 	  if (holder != null) {
 		if (currentMaterial != null) {
 		  // holder.text.setText(Html.fromHtml(text));
 		  if (holder.image != null) {
 			// holder.image.loadImage("http://upload.wikimedia.org/wikipedia/commons/2/23/Lake_mapourika_NZ.jpeg");
 			holder.image.loadImage(currentMaterial.getThumbnailURL().toString());
			holder.text.setText(currentMaterial.getTitle());
 		  } else {
 			Toast.makeText(TeachingApp.getApp(), "image in view holder is null", Toast.LENGTH_LONG).show();
 		  }
 
 		} else {
 		  Toast.makeText(TeachingApp.getApp(), "material is null", Toast.LENGTH_LONG).show();
 		}
 	  } else {
 		Toast.makeText(TeachingApp.getApp(), "view holder is null", Toast.LENGTH_LONG).show();
 	  }
 	}
 	return convertView;
   }
 
 }
 
 class MaterialViewHolder {
   public TextView       text;
   public AsyncImageView image;
 }
