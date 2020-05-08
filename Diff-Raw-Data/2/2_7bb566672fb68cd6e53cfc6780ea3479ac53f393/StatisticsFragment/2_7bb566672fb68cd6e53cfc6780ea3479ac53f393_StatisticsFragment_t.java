 package il.ac.huji.chores;
 
 import android.app.Fragment;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ExpandableListView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import com.parse.ParseException;
 import il.ac.huji.chores.dal.ApartmentDAL;
 import il.ac.huji.chores.dal.ChoreStatisticsDAL;
 import il.ac.huji.chores.dal.RoommateDAL;
 import il.ac.huji.chores.exceptions.UserNotLoggedInException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class StatisticsFragment extends Fragment {
 
     private ListView listView;
     private StatisticsListAdapter adapter;
     private List<ChoreApartmentStatistics> statistics;
     private ProgressBar progressBar;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_statistics, container, false);
         listView = (ListView) view.findViewById(R.id.statisticsListView);
         progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
 
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 View table = v.findViewById(R.id.details);
                 if (table.getVisibility() == View.GONE) {
                     table.setVisibility(View.VISIBLE);
                 } else if (table.getVisibility() == View.VISIBLE) {
                     table.setVisibility(View.GONE);
                 }
             }
         });
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected Void doInBackground(Void... params) {
                 try {
                     String apartmentId = RoommateDAL.getApartmentID();
                     List<String> names = ChoreStatisticsDAL.getChoreStatisticsNames();
                     for (String name : names) {
                         statistics.add(ChoreStatisticsDAL.getChoreApartmentStatistic(name, apartmentId));
                         publishProgress();
                     }
                 } catch (UserNotLoggedInException e) {
                     return null;
                 } catch (ParseException e) {
                     return null;
                 }
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             protected void onPreExecute() {
                 super.onPreExecute();    //To change body of overridden methods use File | Settings | File Templates.
                statistics = new ArrayList<ChoreApartmentStatistics>();
                 adapter = new StatisticsListAdapter(getActivity(), statistics);
                 listView.setAdapter(adapter);
             }
 
             @Override
             protected void onPostExecute(Void aVoid) {
                 super.onPostExecute(aVoid);    //To change body of overridden methods use File | Settings | File Templates.
                 progressBar.setVisibility(View.GONE);
             }
 
             @Override
             protected void onProgressUpdate(Void... values) {
                 super.onProgressUpdate(values);    //To change body of overridden methods use File | Settings | File Templates.
                 adapter.notifyDataSetChanged();
             }
         }.execute();
         return view;
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
     }
 
 }
