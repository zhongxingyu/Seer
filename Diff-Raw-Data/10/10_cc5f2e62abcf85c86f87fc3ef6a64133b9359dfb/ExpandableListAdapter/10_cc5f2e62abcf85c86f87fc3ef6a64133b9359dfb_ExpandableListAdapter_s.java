 package se.mah.kd330a.project.itsl;
 
 import se.mah.kd330a.project.R;
 import se.mah.kd330a.project.adladok.model.Course;
 import se.mah.kd330a.project.adladok.model.Me;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * @author asampe, marcusmansson
  */
 public class ExpandableListAdapter extends BaseExpandableListAdapter
 {
 	private Context _context;
 	private List<Article> _listDataHeader; // header titles
 	private Date lastUpdate;
 	HashMap<String, Integer> colors = new HashMap<String, Integer>();
 	
 	// Temporary for testing - create arraylist of courses
 	ArrayList<Course> theCourses = new ArrayList<Course>();
 
 	public ExpandableListAdapter(Context context, List<Article> listDataHeader)
 	{
 		this._context = context;
 		this._listDataHeader = listDataHeader;
 		this.lastUpdate = Util.getLatestUpdate(_context);
 		
 		//Default color if the feed is not attached to a course
 				colors.put("", context.getResources().getColor(R.color.red_mah));
 									
 				
 		//Fill hashmap with colors from my courses
 		for (Course c : Me.getCourses())
 		{
 			Log.i("kurs", c.getRegCode());
 			colors.put(c.getRegCode(), c.getColor());			
 		}		
 
 	}
 
 	public List<Article> getList()
 	{
 		return _listDataHeader;
 	}
 
 	public void setList(List<Article> list)
 	{
 		_listDataHeader = list;
 		notifyDataSetInvalidated();
 	}
 
 	@Override
 	public Object getChild(int groupPosition, int childPosititon)
 	{
 		return this._listDataHeader.get(groupPosition);
 	}
 
 	@Override
 	public long getChildId(int groupPosition, int childPosition)
 	{
 		return childPosition;
 	}
 
 	@Override
 	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
 	{
 		if (!_listDataHeader.isEmpty())
 		{
 			if (convertView == null)
 			{
 				LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = infalInflater.inflate(R.layout.itsl_list_item, null);
 			}
 
 			TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
 			txtListChild.setText(this._listDataHeader.get(groupPosition).getArticleText());
 			ImageView imgClrCode = (ImageView) convertView.findViewById(R.id.clrCode);
 
 			String regCode = "";
 			
 			if (this._listDataHeader.get(groupPosition).getArticleCourseCode().contains("-"))
 			{
 				int start = this._listDataHeader.get(groupPosition).getArticleCourseCode().indexOf("-")+1;
 				
 				regCode = this._listDataHeader.get(groupPosition).getArticleCourseCode().substring(start, start+5);
 			}
 			
 			
 			if (colors.get(regCode)!=null)
 			{
 				imgClrCode.setBackgroundColor(colors.get(regCode));
 			}
 			else
 			{
 				imgClrCode.setBackgroundColor(colors.get(""));
 			}	
 		}
 
 		return convertView;
 	}
 
 	@Override
 	public Object getGroup(int groupPosition)
 	{
 		return this._listDataHeader.get(groupPosition);
 	}
 
 	@Override
 	public int getGroupCount()
 	{
 		return this._listDataHeader.size();
 	}
 
 	@Override
 	public long getGroupId(int groupPosition)
 	{
 		return groupPosition;
 	}
 
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
 	{
 		if (!_listDataHeader.isEmpty())
 		{
			if (convertView == null)
 			{
 				LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = infalInflater.inflate(R.layout.itsl_list_group, null);
 			}
 
 			Article headerTitle = (Article) getGroup(groupPosition);
 			TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
 			TextView lblListHeaderDate = (TextView) convertView.findViewById(R.id.lblListHeaderDate);
 			TextView lblListHeaderText = (TextView) convertView.findViewById(R.id.lblListHeaderText);
 			TextView lblListCode = (TextView) convertView.findViewById(R.id.lblListCode);
 			ImageView imgClrCode = (ImageView) convertView.findViewById(R.id.clrCode);
 			ImageView imgPointer = (ImageView) convertView.findViewById(R.id.icPointer);
 			TextView txtClrLine = (TextView) convertView.findViewById(R.id.clrLine);
 
 			lblListHeader.setText(headerTitle.getArticleHeader());
 			lblListHeaderDate.setText(headerTitle.getArticleDate().toString());
 			
 			/*
 			 * find out if this article is new since last time we started the app
 			 */
 			if (lastUpdate.getTime() < headerTitle.getArticlePubDate().getTime())
 				lblListCode.setText("New");
 			else
 				lblListCode.setText("");
 				
 			/*
 			 * If the summary text is visible = not expanded
 			 */
 			if (headerTitle.isTextVisible())
 			{
 
 				lblListHeaderText.setVisibility(View.VISIBLE);
 				lblListHeaderText.setText(headerTitle.getArticleSummary());
 				imgPointer.setVisibility(View.VISIBLE);
 			}
 			else
 			{
 				lblListHeaderText.setVisibility(View.GONE);
 				imgPointer.setVisibility(View.GONE);
 
 			}
 
 			if (headerTitle.getArticleSummary().equals(headerTitle.getArticleText()))
 			{
 				convertView.setClickable(true);
 				imgPointer.setVisibility(View.GONE);
 			}
 			else
 			{
 				convertView.setClickable(false);
 			}
 
 String regCode = "";
 			
 			if (this._listDataHeader.get(groupPosition).getArticleCourseCode().contains("-"))
 			{
 				int start = this._listDataHeader.get(groupPosition).getArticleCourseCode().indexOf("-")+1;
 				
 				regCode = this._listDataHeader.get(groupPosition).getArticleCourseCode().substring(start, start+5);
 			}
 			
 			Log.i("getView", regCode);
 			
 			/* 
 			 * Sets the color to a default color if the coursecode can't be found in
 			 * the list of courses
 			 */
 			
 			if (colors.get(regCode)!=null)
 			{
 				imgClrCode.setBackgroundColor(colors.get(regCode));
 				txtClrLine.setBackgroundColor(colors.get(regCode));
 			}
 			else
 			{
 				imgClrCode.setBackgroundColor(colors.get(""));
 				txtClrLine.setBackgroundColor(colors.get(""));
 			}
 		}
 		return convertView;
 
 	}
 
 	@Override
 	public boolean hasStableIds()
 	{
 		return false;
 	}
 
 	@Override
 	public boolean isChildSelectable(int groupPosition, int childPosition)
 	{
 		return true;
 	}
 
 	@Override
 	public void onGroupCollapsed(int groupPosition)
 	{
 		super.onGroupCollapsed(groupPosition);
 
 		if (!_listDataHeader.isEmpty())
 		{
 			Article headerTitle = (Article) getGroup(groupPosition);
 			headerTitle.setTextVisible(true);
 		}
 	}
 
 	@Override
 	public void onGroupExpanded(int groupPosition)
 	{
 		super.onGroupExpanded(groupPosition);
 
 		if (!_listDataHeader.isEmpty())
 		{
 			Article headerTitle = (Article) getGroup(groupPosition);
 			headerTitle.setTextVisible(false);
 		}
 
 	}
 
 	@Override
 	public int getChildrenCount(int groupPosition)
 	{
 		return 1;
 	}
 }
