 package school.planner;
 import school.planner.R;
 import android.app.ExpandableListActivity;
 import android.os.Bundle;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import android.widget.SimpleExpandableListAdapter;
 
 public class ClassInformation extends ExpandableListActivity
 {
     static final String headers[] =
     	{
 		"General Information", "Professor Information", "Assignments (active)"
 		};
 
 	static final String subHeaders[][] =
 		{
 			{
 				"Course Number:", "CM333",
 				"Room Number:", "MO 017",
 				"Time:", "11:00 AM T-Th"
 			},
 			{
 				"Name:", "Cecil Shmidt",
 				"Office:", "ST 317",
 				"Office Hours:", "12-2 M-W",
 				"Phone #:", "555-5555",
 				"E-mail:", "Cecil.Schmidt@washburn.edu"
 			},
 			{
 				"New Assignment", "This is a new Assignment"
 			}
 		};
 		
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.listlayout);
 		SimpleExpandableListAdapter expListAdapter = new SimpleExpandableListAdapter(
 				this,
 				createGroupList(),
 				R.layout.group_row, //Stylesheet for the text in this row
 				new String[] { "headerName" }, //I actually have no idea why these needs brackets. using "=" didn't seem to work.
 				new int[] { R.id.groupname },
 				createChildList(),
 				R.layout.child_row,
 				new String[] { "subheaderName", "subheader2Name" },
 				new int[] { R.id.childname, R.id.child2name } //references to the "child_row" stylesheet
 			);
 		setListAdapter( expListAdapter );
     }
 // This creates the headers: Class info, Professor info, etc.
	private List createGroupList() 
	{
 	  ArrayList result = new ArrayList();
 	  for( int i = 0; i < headers.length ; i++ ) 
 	  {
 		HashMap m = new HashMap();
 	    m.put( "headerName",headers[i] );
 		result.add( m );
 	  }
 	  return (List)result;
     }
 // And this creates the subcategories: Professor name, room number, etc.
   private List createChildList() {
 	ArrayList result = new ArrayList();
 	for( int i = 0 ; i < subHeaders.length ; i++ ) 
 	{
 	  ArrayList secList = new ArrayList();
 //Along with the subcategories, this fills in the prewritten information (see the Strings above).
 //Not sure how we want to code it to alternate between subcategory titles and user-input information?
 	  for( int n = 0 ; n < subHeaders[i].length ; n += 2 )
 	  {
 	    HashMap child = new HashMap();
 		child.put( "subheaderName", subHeaders[i][n] );
 	    child.put( "subheader2Name", subHeaders[i][n+1]);
 		secList.add(child);
 	  }
 	  result.add(secList);
 	}
 	return result;
   }
 
 }
