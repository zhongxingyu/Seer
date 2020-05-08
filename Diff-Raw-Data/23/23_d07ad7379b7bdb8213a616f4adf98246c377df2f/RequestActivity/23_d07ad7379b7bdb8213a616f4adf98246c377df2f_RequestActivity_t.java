 package autobahn.android;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.Toast;
 import autobahn.android.adapters.RequestActivityAdapter;
 import com.example.autobahn.R;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tavu
  * Date: 7/17/13
  * Time: 3:34 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RequestActivity extends Activity {
 
     private ArrayList<String> groupList;
     private LinkedHashMap<String, ArrayList<String>> headerInfoCollection;
     private ExpandableListView expandableListView;
 
    public void onsCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.request_reservation_activity);
 
         headerInfoCollection = new LinkedHashMap();
 
         groupList = new ArrayList(Arrays.asList(getResources().getString(R.string.basicParameters),
                 getResources().getString(R.string.optionalParameters),
                 getResources().getString(R.string.pathConstraints)));
         prepareParameters();
 
         expandableListView = (ExpandableListView) findViewById(R.id.requestExpandableList);
 
         final RequestActivityAdapter listAdapter = new RequestActivityAdapter(this, groupList, headerInfoCollection);
         expandableListView.setAdapter(listAdapter);
 
         expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
             @Override
             public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                 final String selected = (String) listAdapter.getChild(groupPosition, childPosition);
                 Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG).show();
                 return true;
             }
         });
     }
 
     private void prepareParameters() {
         for (String parametersGroup : groupList) {
             ArrayList<String> parameterList;
             if (parametersGroup.equals(getResources().getString(R.string.basicParameters))) {
                 parameterList = new ArrayList<String>(Arrays.asList(getResources().getString(R.string.startPort),
                         getResources().getString(R.string.endPort),
                         getResources().getString(R.string.delay)));
             } else if (parametersGroup.equals(getResources().getString(R.string.optionalParameters))) {
                 parameterList = new ArrayList<String>(Arrays.asList(getResources().getString(R.string.delay)));
             } else {
                 parameterList = new ArrayList<String>();
             }
 
             headerInfoCollection.put(parametersGroup, parameterList);
         }
     }
 }
