 package com.Centaurii.app.RatingCalculator.adapters;
 
 import java.util.List;
 
 import com.Centaurii.app.RatingCalculator.R;
 import com.Centaurii.app.RatingCalculator.model.Profile;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ProfileListAdapter extends ArrayAdapter<Profile>
 {
     Context context;
     int resource;
     List<Profile> objects;
     
     public ProfileListAdapter(Context context, int resource,
             List<Profile> objects)
     {
         super(context, resource, objects);
         this.context = context;
         this.resource = resource;
         this.objects = objects;
     }
     
     @Override
     public Profile getItem(int i)
     {
         return objects.get(i);
     }
     
     @Override
     public View getView(int position, View convertView, ViewGroup parent)
     {
         View v = convertView;
         ProfileHandler handler;
         if(v == null)
         {
             LayoutInflater inflater = 
                     (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = inflater.inflate(resource, parent, false);
             
             handler = new ProfileHandler();
             handler.profileImage = (ImageView) v.findViewById(R.id.profile_image);
             handler.profileName = (TextView) v.findViewById(R.id.name);
             handler.profileRating = (TextView) v.findViewById(R.id.rating);
             handler.profileProvisional = (TextView) v.findViewById(R.id.provisional);
             
             v.setTag(handler);
         }
         else
         {
             handler = (ProfileHandler) v.getTag();
         }
         
         final Profile prof = getItem(position);
         handler.profileImage.setBackgroundColor(prof.getFavColor());
         handler.profileName.setText(prof.getName());
         handler.profileRating.setText("" + prof.getRating());
         if(prof.isProvisional())
         {
             handler.profileProvisional.setVisibility(View.VISIBLE);
         }
         else
         {
             handler.profileProvisional.setVisibility(View.GONE);
         }
         
         return v;
     }
     
     static class ProfileHandler
     {
         ImageView profileImage;
         TextView profileName;
         TextView profileRating;
         TextView profileProvisional;
     }
 }
