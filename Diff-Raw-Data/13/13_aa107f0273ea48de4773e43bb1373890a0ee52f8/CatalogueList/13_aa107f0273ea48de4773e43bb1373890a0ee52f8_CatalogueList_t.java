 package by.bsu.courseproject.ui;
 
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import by.bsu.courseproject.R;
 import by.bsu.courseproject.db.ProjectManagerProvider;
 
 import static by.bsu.courseproject.db.DBConstants.Columns;
 
 public class CatalogueList extends ListActivity {
 
   public static final int PROJECT = 0;
   public static final int CUSTOMER = 1;
   public static final int INVESTOR = 2;
   public static final int EMPLOYEE = 3;
 
   private final int SIZE = 4;
 
   private String[] mCategories;
 
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
 
     super.onCreate(savedInstanceState);
 
     mCategories = new String[SIZE];
     mCategories[PROJECT] = this.getString(R.string.label_projects);
     mCategories[CUSTOMER] = this.getString(R.string.label_customers);
     mCategories[INVESTOR] = this.getString(R.string.label_investors);
     mCategories[EMPLOYEE] = this.getString(R.string.label_employees);
 
     setListAdapter(new ArrayAdapter<String>(this,
                                             android.R.layout.simple_list_item_1, mCategories));
  //  getListView().setBackgroundColor(getResources().getColor(R.color.gray));
   }
 
   public static final String PROJECT_PROJECTNAME = "PROJECTNAME";
   public static final String PROJECT_PROJECTDUEDATE = "PROJECTDUEDATE";
   public static final String PROJECT_CATEGORY = "CATEGORY";
   public static final String PROJECT_STATUS = "STATUS";
   public static final String PROJECT_PRIORITY = "PRIORITY";
   public static final String PROJECT_PROJECTDESCRIPTION = "PROJECTDESCRIPTION";
 
 
 
 
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
 
     super.onListItemClick(l, v, position, id);
 
     Intent intent = new Intent();
     intent.setClass(CatalogueList.this, CatalogueFragment.class);
     String[] projection = null;
     intent.putExtra(CatalogueFragment.FROM_LIST, position);
 
     switch (position) {
     case PROJECT:
       projection = new String[]{
           Columns._ID,
           Columns.PROJECT_PROJECTNAME,
           Columns.PROJECT_CATEGORY,
           Columns.PROJECT_PRIORITY,
           Columns.PROJECT_STATUS,
           Columns.PROJECT_PROJECTDUEDATE/*,
           Columns.PERSON_FIRSTNAME,
           Columns.PERSON_MIDDLENAME,
           Columns.PERSON_LASTNAME*/
       };
 
       intent.setData(ProjectManagerProvider.CONTENT_URI);
       intent.putExtra(CatalogueListFragment.ARG_FILTER_COLUMNS,
                       new String[]{Columns.PROJECT_PROJECTNAME,
                                    Columns.PROJECT_CATEGORY,
                                    Columns.PROJECT_PRIORITY,
                                    Columns.PROJECT_STATUS,
                                    Columns.PROJECT_PROJECTDUEDATE/*,
                                    Columns.PERSON_FIRSTNAME,
                                    Columns.PERSON_MIDDLENAME,
                                    Columns.PERSON_LASTNAME,
                                    Columns.PERSON_FIRSTNAME,
                                    Columns.PERSON_MIDDLENAME,
                                    Columns.PERSON_LASTNAME*/});
       intent.putExtra(CatalogueListFragment.ARG_ADAPTER_FROM,
 
                       new String[]{Columns.PROJECT_PROJECTNAME, Columns.PROJECT_CATEGORY, Columns.PROJECT_PROJECTDUEDATE});
 
       intent.putExtra(CatalogueListFragment.ARG_ADAPTER_TO,
                       new int[]{android.R.id.text1, android.R.id.text2});
       break;
     case EMPLOYEE:
       projection = new String[]{
           Columns._ID,
           Columns.PERSON_FIRSTNAME,
           Columns.PERSON_MIDDLENAME,
           Columns.PERSON_LASTNAME,
           Columns.PERSON_EMAIL,
           Columns.PERSON_SKYPE,
           Columns.PERSON_PHONE,
           Columns.PERSON_BIRTHDAY
       };
 
       intent.setData(ProjectManagerProvider.PERSON_URI);
       intent.putExtra(CatalogueListFragment.ARG_FILTER_COLUMNS,
                       new String[]{Columns.PERSON_FIRSTNAME, Columns.PERSON_MIDDLENAME,
                                    Columns.PERSON_LASTNAME,
                                    Columns.PERSON_EMAIL,
                                    Columns.PERSON_SKYPE,
                                    Columns.PERSON_PHONE, Columns.PERSON_BIRTHDAY});
       intent.putExtra(CatalogueListFragment.ARG_ADAPTER_FROM,
                       new String[]{Columns.PERSON_LASTNAME, Columns.PERSON_FIRSTNAME, Columns.PERSON_MIDDLENAME,
 
                                    Columns.PERSON_PHONE});
       intent.putExtra(CatalogueListFragment.ARG_ADAPTER_TO,
                       new int[]{R.id.fName, R.id.mName, R.id.lName, R.id.phone});
       break;
     case CUSTOMER:
       projection = new String[]{
           Columns._ID,
           Columns.PERSON_DISCRIMINATOR,
           Columns.PERSON_FIRSTNAME,
           Columns.PERSON_MIDDLENAME,
           Columns.PERSON_LASTNAME,
           Columns.PERSON_EMAIL,
           Columns.PERSON_SKYPE,
           Columns.PERSON_PHONE
       };
 
       intent.setData(ProjectManagerProvider.PERSON_URI);
       intent.putExtra(CatalogueListFragment.ARG_FILTER_COLUMNS,
                       new String[]{Columns.PERSON_FIRSTNAME, Columns.PERSON_MIDDLENAME,
                                    Columns.PERSON_LASTNAME,
                                    Columns.PERSON_EMAIL,
                                    Columns.PERSON_SKYPE,
                                    Columns.PERSON_PHONE});
       intent.putExtra(CatalogueListFragment.ARG_ADAPTER_FROM,
                       new String[]{Columns.PERSON_LASTNAME, Columns.PERSON_FIRSTNAME, Columns.PERSON_MIDDLENAME,
 
                                    Columns.PERSON_PHONE});
       intent.putExtra(CatalogueListFragment.ARG_ADAPTER_TO,
                       new int[]{R.id.fName, R.id.mName, R.id.lName, R.id.phone});
       break;
     case INVESTOR:
       projection = new String[]{
           Columns._ID,
           Columns.PERSON_DISCRIMINATOR,
           Columns.PERSON_FIRSTNAME,
           Columns.PERSON_MIDDLENAME,
           Columns.PERSON_LASTNAME,
           Columns.PERSON_EMAIL,
           Columns.PERSON_SKYPE,
           Columns.PERSON_PHONE
       };
 
       intent.setData(ProjectManagerProvider.PERSON_URI);
       intent.putExtra(CatalogueListFragment.ARG_FILTER_COLUMNS,
                       new String[]{Columns.PERSON_FIRSTNAME, Columns.PERSON_MIDDLENAME,
                                    Columns.PERSON_LASTNAME,
                                    Columns.PERSON_EMAIL,
                                    Columns.PERSON_SKYPE,
                                    Columns.PERSON_PHONE});
       intent.putExtra(CatalogueListFragment.ARG_ADAPTER_FROM,
                       new String[]{Columns.PERSON_LASTNAME, Columns.PERSON_FIRSTNAME, Columns.PERSON_MIDDLENAME,
 
                                    Columns.PERSON_PHONE});
       intent.putExtra(CatalogueListFragment.ARG_ADAPTER_TO,
                       new int[]{R.id.fName, R.id.mName, R.id.lName, R.id.phone});
       break;
     default:
       return;
     }
 
 
     intent.putExtra(CatalogueListFragment.ARG_PROJECTION, projection);
     startActivity(intent);
   }
 
 
 }
 
 
