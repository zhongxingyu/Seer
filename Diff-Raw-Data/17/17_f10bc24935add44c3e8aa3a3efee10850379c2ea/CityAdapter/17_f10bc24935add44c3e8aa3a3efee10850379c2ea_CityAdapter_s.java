 package be.uclouvain.sinf1225.gourmet;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Filter;
 import android.widget.TextView;
 
 public class CityAdapter extends ArrayAdapter<City>
 {
 	static class ViewIds
     {
     	TextView name;
     	TextView distance;
         TextView restaurantCount;
     }
 	
 	public class CityComparator implements Comparator<City>
 	{
 	    @Override
 	    public int compare(City city1, City city2)
 	    {
 	    	int ret = 0;
 	        if(orderby.equals("name"))
 	        	ret = city1.getName().compareTo(city2.getName());
 	        else if(orderby.equals("distance"))
 	        	ret = Double.valueOf(city1.getLocation().distanceTo(lastLocation)).compareTo((double) city2.getLocation().distanceTo(lastLocation));
 	        else if(orderby.equals("restaurants"))
 	        	ret = Integer.valueOf(city1.getRestaurantCount()).compareTo(city2.getRestaurantCount());
 	        
 	        if(!orderasc) ret = -ret;
 	        return ret;
 	    }
 	}
 	
 	private class CityFilter extends Filter
 	{
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint)
 		{
 			FilterResults results = new FilterResults();
             String prefix = constraint.toString().toLowerCase(Locale.FRENCH);
 
             if (prefix == null || prefix.length() == 0)
             {
                 ArrayList<City> list = new ArrayList<City>(cities);
                 results.values = list;
                 results.count = list.size();
             }
             else
             {
                 final ArrayList<City> list = new ArrayList<City>(cities);
                 final ArrayList<City> nlist = new ArrayList<City>();
 
                 for (int i = 0; i < list.size(); i++)
                 {
                     final City city = list.get(i);
 
                     if (city.getName().toLowerCase(Locale.FRENCH).startsWith(prefix))
                     {
                         nlist.add(city);
                     }
                 }
                 
                 results.values = nlist;
                 results.count = nlist.size();
             }
             return results;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void publishResults(CharSequence constraint, FilterResults results)
 		{
 			filteredCities = (ArrayList<City>)results.values;
 
             clear();
             int count = filteredCities.size();
             for (int i=0; i<count; i++)
                 add(filteredCities.get(i));
             
             sort();
 		}
 		
 	}
 	
     private Context context; 
     private int layoutResourceId;    
     protected List<City> cities = null;
     protected List<City> filteredCities = null;
     protected Location lastLocation = null;
     private Filter filter;
     protected String orderby = "name";
     protected boolean orderasc = true;
 	
     public CityAdapter(Context context, int layoutResourceId, List<City> cities, Location loc)
     {
         super(context, layoutResourceId, new ArrayList<City>(cities));
         this.layoutResourceId = layoutResourceId;
         this.context = context;
         this.cities = cities;
         this.filteredCities = new ArrayList<City>(cities);
         this.lastLocation = loc;
     }
 
     public void updateLocation(Location loc)
     {
     	lastLocation = loc;
     	notifyDataSetChanged();
     	sort();
     }
     
     @Override
     public Filter getFilter()
     {
         if (filter == null)
             filter = new CityFilter();
 
         return filter;
     }
     
     public void sort()
     {
     	Collections.sort(filteredCities, new CityComparator());
     	clear();
         int count = filteredCities.size();
         for (int i=0; i<count; i++)
             add(filteredCities.get(i));
     }
     
     public void setSort(String sort)
     {
     	orderby = sort;
     	sort();
     }
     
     public void setSort(String sort, boolean sortorder)
     {
     	orderby = sort;
     	orderasc = sortorder;
     	sort();
     }
     
     public void setSortOrder(boolean sort)
     {
     	orderasc = sort;
     	sort();
     }
     
     @Override
     public View getView(int position, View convertView, ViewGroup p)
     {
         View row = convertView;
         ViewIds viewIds = null;
         
         //Si il n'y a pas encore de lignes
         if(row == null)
         {
         	//Crons une nouvelle ligne
             LayoutInflater inflater = ((Activity)context).getLayoutInflater();
             row = inflater.inflate(layoutResourceId, p, false);
             
             //Et une correspondance entre cette ligne et les champs de texte
             viewIds = new ViewIds();
             viewIds.name = (TextView)row.findViewById(R.id.cityListRowName);
             viewIds.distance = (TextView)row.findViewById(R.id.cityListRowDist);
             viewIds.restaurantCount = (TextView)row.findViewById(R.id.cityListRowRestos);
            
             row.setTag(viewIds);
         }
         else
         {
         	viewIds = (ViewIds)row.getTag();
         }
         
         City city = filteredCities.get(position);
         viewIds.name.setText(city.getName());
         double dist = -1;
         if(lastLocation != null)
         	dist = city.getLocation().distanceTo(lastLocation);
         if(dist >= 0)
         	viewIds.distance.setText("Distance: "+new DecimalFormat("#.##").format(dist/1000)+" Km");
         else
         	viewIds.distance.setText("");
         viewIds.restaurantCount.setText(""+city.getRestaurantCount()+ " restaurant(s)");
         return row;
     }
 }
