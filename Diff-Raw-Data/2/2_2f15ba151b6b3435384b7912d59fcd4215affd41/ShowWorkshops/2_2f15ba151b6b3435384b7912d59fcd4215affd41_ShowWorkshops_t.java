 package event.monitor;
 
 import event.monitor.models.Workshop;
 import event.monitor.WorkshopAdapter;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 
 public class ShowWorkshops extends Activity{
   private Object workshops;
   private Workshop workshop;
   private ListView list_view;
 
   @Override
     public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.show_workshops);
 
       getViewData();
       setupWorkshop();
       setupAdapter();
 
       list_view = (ListView) findViewById(R.id.workshops);
       list_view.setTextFilterEnabled(true);
       list_view.setOnItemClickListener(new OnItemClickListener(){
   		@Override
         public void onItemClick(AdapterView<?> arg0, View v, int position, long id){
           //Con esto obtenemos el contenido de los TextView contenidos en el Item
           String  name=(String)((TextView)v.findViewById(R.id.label_name)).getText();
           String  id1=(String)((TextView)v.findViewById(R.id.label_id)).getText();
           AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowWorkshops.this);
           alertDialog.setTitle("Workshop");
           alertDialog.setMessage("The selected item is: " + name + " " + id1);
           alertDialog.setPositiveButton("Aceptar", null);
           alertDialog.show();
         }
 
       });
 
       Button back = (Button)findViewById(R.id.back);
       back.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
           // TODO Auto-generated method stub
           Intent myIntent = new Intent(v.getContext(), MainActivity.class);
           startActivityForResult(myIntent,0);
         }
       });
     }
 
   public void getViewData(){
     workshops = (ListView)findViewById(R.id.workshops);
   }
 
   public void setupWorkshop(){
    workshop = new Workshop(getApplicationContext(),"workshop",null,1);
   }
 
   private void setupAdapter(){
     if(workshop.all()!=null)
     {
       WorkshopAdapter adapter = new WorkshopAdapter(this,R.layout.workshop_item, workshop.all());
       ((ListView) workshops).setAdapter(adapter);
     }
   }
 
   public void onResume(){
     super.onResume();
     setupAdapter();
   }
 
 }
