 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.fragments;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.MuldvarpService;
 import no.hials.muldvarp.v2.TopActivity;
 import no.hials.muldvarp.v2.domain.Domain;
 import no.hials.muldvarp.v2.domain.Programme;
 import no.hials.muldvarp.v2.domain.Video;
 import no.hials.muldvarp.v2.adapter.ListAdapter;
 import no.hials.muldvarp.v2.adapter.SectionedListAdapter;
 import no.hials.muldvarp.v2.domain.ScheduleDay;
 import no.hials.muldvarp.v2.view.SectionHeaderView;
 
 /**
  * This class defines a fragment containing a list of specified ListItems.
  *
  * @author johan
  */
 public class ListFragment extends MuldvarpFragment {
     ProgressDialog progressDialog;
     BaseAdapter listAdapter;
     ListView listView;
     SectionHeaderView headerView;
     View fragmentView;
     List<Domain> items = new ArrayList<Domain>();
     Class destination;
     public enum ListType {COURSE, PROGRAMME, DOCUMENT, VIDEO, NEWS, QUIZ, TOPIC, TIMEEDIT};
     ListType type;
 
     public ListFragment(String fragmentTitle, int iconResourceID, ListType type) {
         super.fragmentTitle = fragmentTitle;
         super.iconResourceID = iconResourceID;
         this.type = type;
     }
 
     public ListFragment(String fragmentTitle, int iconResourceID, List<Domain> items, ListType type) {
         super.fragmentTitle = fragmentTitle;
         super.iconResourceID = iconResourceID;
         this.items = items;
         this.type = type;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         if(fragmentView == null) {            
             if(type == ListType.TIMEEDIT) {
                 fragmentView = inflater.inflate(R.layout.layout_timeedit_listview, container, false);
                 headerView = (SectionHeaderView)fragmentView.findViewById(R.id.layoutSectionHeaderView);
                 headerView.setPinnedHeaderView(inflater.inflate(R.layout.layout_timeedit_listitem_day_header, headerView, false));
             } else {
                 fragmentView = inflater.inflate(R.layout.layout_listview, container, false);
                 listView = (ListView)fragmentView.findViewById(R.id.layoutlistview);
             }
         }
 //        progressDialog = new ProgressDialog(owningActivity);
         itemsReady();
         
         return fragmentView;
     }
 
     private void updateItems() {
         Domain d = owningActivity.domain;
         System.out.println("ID " + d.getId());
         System.out.println("NAME " + d.getName());
 
         MuldvarpService service = owningActivity.getService();
         try {
             if(service == null) {
                 Log.e("ListFragment","MuldvarpService is null in updateItems");
             } else {
                 switch(type) {
                     case COURSE:
                         items.clear();
                         Programme p = (Programme)owningActivity.domain;
                         items.addAll(p.getCourses());
                         break;
                     case PROGRAMME:
                         items.clear();
                         items.addAll(service.mProgrammes);
                         break;
                     case NEWS:
                         items.clear();
                         items.addAll(service.mNews);
                         break;
                     case TIMEEDIT:
                         items.clear();
                         items.addAll(service.timeEditDays);
                         break;
                         
                 } 
             }
         } catch (NullPointerException ex) {
             Log.e("ListFragment", "Lista er tom");
         }
         
 
         if(listAdapter != null) {
             listAdapter.notifyDataSetChanged();
         }
     }
 
     public void setDestinationClass(Class destinationClass) {
         this.destination = destinationClass;
     }
 
     public void itemsReady() {
 
         //If the items are empty, add temporary dummydata from database
 //        if(items.isEmpty()) {
 //            if(owningActivity.domain == null) {
 //                items.addAll(DummyDataProvider.getFromDatabase(owningActivity));
 //            }
 //        }
 
 //        showProgressDialog();
         updateItems();
         if(type == ListType.TIMEEDIT){
             List<ScheduleDay> castList = (List<ScheduleDay>)(List<?>) items;
             
 //            listView.setAdapter(new TimeEditListAdapter(
 //                    fragmentView.getContext(),
 //                    R.layout.layout_timeedit_listitem,
 //                    R.id.text,
 //                    castList)
 //            );
             headerView.setAdapter(new SectionedListAdapter(
                     fragmentView.getContext(),
                     R.layout.layout_timeedit_listitem,
                     R.id.text,
                     castList));
             
             listAdapter = (SectionedListAdapter) headerView.getAdapter();
         } else {
             listView.setAdapter(new ListAdapter(
                     fragmentView.getContext(),
                     R.layout.layout_listitem,
                     R.id.text,
                     items,
                     false)
             );
             listAdapter = (ListAdapter) listView.getAdapter();
             listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
                 //The idea is that one click should start a corresponding activity.
                 Domain selectedItem = (Domain)listAdapter.getItem(position);
 
                 if(selectedItem instanceof Video) {
                     Video v = (Video)selectedItem;
                     Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + v.getUri()));
                     startActivity(intent);
                 } else if(selectedItem.getActivity() != null) {
                     destination = selectedItem.getActivity();
                     Intent myIntent = new Intent(view.getContext(), destination);
                     myIntent.putExtra("Domain", selectedItem);
                     myIntent.putExtra("type", type);
                     startActivityForResult(myIntent, 0);
                 } else {
                     Intent myIntent = new Intent(view.getContext(), TopActivity.class);
                     myIntent.putExtra("Domain", selectedItem);
                     startActivityForResult(myIntent, 0);
                     //Burde erstattes med en feilbeskjed fra en string i xml-fil
 //                    Toast show = Toast.makeText(owningActivity, "Muldvarp vet ikke hvordan det skal åpne dette innlegget.", Toast.LENGTH_SHORT);
 //                    show.show();
                 }
             }
         });
 
 
         listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {   //triggers if a listitem is pressed and held.
                 Domain selectedItem = (Domain)listAdapter.getItem(pos);                                       //Saves the clicked item in the selectedItem variable.
                 if(owningActivity.getLoggedIn() && selectedItem instanceof Domain){         //If the long-clicked item is a course, it is added to the users list of courses.
                     Domain domain = (Domain) selectedItem;
                     createDialog(domain);                                                   //Creates a new alertDialog asking whether the user wants to add the course to his/her favourites.
                     return true;                                                            //Tells the activity that the click has been "consumed", meaning that onItemClick should not be triggered.
                 }
                 else{                                                                       //If the clicked item isn't a course
                     return false;                                                           //Tells the activity that the click has not been "consumed", meaning that onItemClick will be triggered.
                 }
             }
         });
         }        
     }
 
     @Override
     public void queryText(String text){
         if(listAdapter instanceof ListAdapter) {
//            listAdapter.filter(text);
         }        
     }
 
     public void createDialog(final Domain d){
         AlertDialog.Builder builder = new AlertDialog.Builder(owningActivity);
         builder.setMessage("vil du legge til " + d.getName() + " i mine snarveier?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         owningActivity.getService().getUser().addDomain(d);                          //Adds the domain to the list of personal shortcuts.
                         Toast toast = Toast.makeText(fragmentView.getContext(),                      //Shows a short toast to the user as feedback, telling him/her that the domain has been added to the user list.
                         d.getName() + " er lagt til i mine snarveier.", Toast.LENGTH_SHORT);
                         owningActivity.updateRBMMenu();
                         toast.show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();                                                              //Dismisses the dialog without doing anything.
                    }
                });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void showProgressDialog(){
 
         if(!progressDialog.isShowing()){
 
             progressDialog.setMessage(getString(R.string.loading));
             progressDialog.setIndeterminate(true);
             progressDialog.setCancelable(false);
             progressDialog.show();
         }
     }
 }
