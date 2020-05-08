 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.adapter;
 
 import android.content.Context;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.BaseAdapter;
 import android.widget.SectionIndexer;
 import android.widget.TextView;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.ScheduleDay;
 import no.hials.muldvarp.v2.domain.ScheduleLecture;
 import no.hials.muldvarp.v2.view.SectionHeaderView;
 
 /**
  * Adapter for displaying schedules with sections. Intended for use with TimeEdit
  * @author johan
  */
 public class SectionedListAdapter extends BaseAdapter implements SectionIndexer, OnScrollListener{
     
     List<Pair<String, List<ScheduleLecture>>> items;
 //    List<Pair<String, List<Pair<String,ScheduleLecture>>>> items;
     private LayoutInflater mInflater;
     
     /**
      * Header is pinned, set to GONE
      */
     public static final int PINNED_HEADER_GONE = 0;
 
     /**
      * Header is pinned, set to VISIBLE
      */
     public static final int PINNED_HEADER_VISIBLE = 1;
 
     /**
      * Pinned header state: show the header. If the header extends beyond
      * the bottom of the first shown element, push it up and clip.
      */
     public static final int PINNED_HEADER_PUSHED_UP = 2;
     
     public SectionedListAdapter(Context context, int resource, int textViewResourceId, List<ScheduleDay> dayList) {
         items = setupItemsFromDayList(dayList);
         mInflater = LayoutInflater.from(context);
     }
     
     public List<Pair<String, List<ScheduleLecture>>> setupItemsFromDayList(List<ScheduleDay> dayList){
         List<Pair<String, List<ScheduleLecture>>> retVal = new ArrayList<Pair<String, List<ScheduleLecture>>>();
         for(int i = 0; i < dayList.size(); i++){
             
             retVal.add(new Pair<String, List<ScheduleLecture>>(
                    dayList.get(i).getDayName() + "" + dayList.get(i).getDate(),
                     dayList.get(i).getLectures()));
         }
         return retVal;
         
     }
     
     /**
      * Computes the desired state of the pinned header for the given
      * position of the first visible list item. Allowed return values are
      * {@link #PINNED_HEADER_GONE}, {@link #PINNED_HEADER_VISIBLE} or
      * {@link #PINNED_HEADER_PUSHED_UP}.
      */
     public int getPinnedHeaderState(int position) {
     	if (position < 0 || getCount() == 0) {
     		return PINNED_HEADER_GONE;
     	}
     	
     	// The header should get pushed up if the top item shown
     	// is the last item in a section for a particular letter.
     	int section = getSectionForPosition(position);
     	int nextSectionPosition = getPositionForSection(section + 1);
     	if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
     		return PINNED_HEADER_PUSHED_UP;
     	}
     	
     	return PINNED_HEADER_VISIBLE;
     }
     
     @Override
     public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
             ((SectionHeaderView) view).configureHeaderView(firstVisibleItem);
 	}
     
     @Override
     public void onScrollStateChanged(AbsListView view, int scrollState) {
             // no implementation. damnit can't figure out why this is ABSOLUTELY needed
     }
 
     @Override
     public int getCount() {
             int count = 0;
             for (int i = 0; i < items.size(); i++) {
                     count += items.get(i).second.size();
             }
             return count;
     }
     
     @Override
     public ScheduleLecture getItem(int position) {
             int count = 0;
             for (int i = 0; i < items.size(); i++) {
                     if (position >= count && position < count + items.get(i).second.size()) {
                             return items.get(i).second.get(position - count);
                     }
                     count += items.get(i).second.size();
             }
             return null;
     }
 
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     protected void onNextPageRequested(int page) {
     }
 
     /**
      * uncomment as needed
      * @param view
      * @param position
      * @param displaySectionHeader 
      */
     protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
         if (displaySectionHeader) {
             //WEEKS currently disabled
             view.findViewById(R.id.scheduleWeek).setVisibility(View.GONE);
             view.findViewById(R.id.scheduleDay).setVisibility(View.VISIBLE);
             TextView dayText = (TextView) view.findViewById(R.id.scheduleDay);
             dayText.setText(getSections()[getSectionForPosition(position)]);
         } else {
             //Weeks currently disabled
             view.findViewById(R.id.scheduleWeek).setVisibility(View.GONE);
             view.findViewById(R.id.scheduleDay).setVisibility(View.GONE);
         }
     }
 
         
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder = new ViewHolder();
         if(convertView == null){
             convertView = mInflater.inflate(R.layout.layout_timeedit_listitem, parent, false);            
             holder.scheduleClassNames = (TextView) convertView.findViewById(R.id.scheduleClassNames);
             holder.scheduleCourseName = (TextView) convertView.findViewById(R.id.scheduleCourseName);
             holder.scheduleDay = (TextView) convertView.findViewById(R.id.scheduleDay);
             holder.scheduleEndTime = (TextView) convertView.findViewById(R.id.scheduleEndTime);
             holder.scheduleLectureType = (TextView) convertView.findViewById(R.id.scheduleLectureType);
             holder.scheduleRoomName = (TextView) convertView.findViewById(R.id.scheduleRoomName);
             holder.scheduleStartTime = (TextView) convertView.findViewById(R.id.scheduleStartTime);
             holder.scheduleWeek= (TextView) convertView.findViewById(R.id.scheduleWeek);
             ScheduleLecture tempLecture = getItem(position);
             String courseString = "";
             for(int i = 0; i < tempLecture.getCourses().size(); i++){
                 courseString += tempLecture.getCourses().get(i).getCourseName();
                 if(i > 1 && !(i == tempLecture.getCourses().size())){
                     courseString += ", ";
                 }
             }
             holder.scheduleCourseName.setText(courseString);
             holder.scheduleClassNames.setText(tempLecture.getClassId());
             holder.scheduleStartTime.setText(tempLecture.getLectureStart());
             holder.scheduleEndTime.setText(tempLecture.getLectureEnd());
             holder.scheduleLectureType.setText(tempLecture.getType());
             holder.scheduleRoomName.setText(tempLecture.getRoom());                        
             convertView.setTag(holder);
         }
            
         final int section = getSectionForPosition(position);
         boolean displaySectionHeaders = (getPositionForSection(section) == position);
 
         bindSectionHeader(convertView, position, displaySectionHeaders);
 
         return convertView;
     }
         
     /**
      * This method configures the currently pinned header at various scrollpositions.
      * @param header
      * @param position
      * @param alpha 
      */
     public void configurePinnedHeader(View header, int position, int alpha) {
             TextView sectionHeader = (TextView)header;
             sectionHeader.setText(getSections()[getSectionForPosition(position)]);
             //Hvit bakgrunnsfarge, må endres
             sectionHeader.setBackgroundColor(alpha << 24 | (0xcccccc));
             //FADE IT AWAY
             sectionHeader.setTextColor(alpha << 24 | (0x000000));
     }
 
     @Override
     public int getPositionForSection(int section) {
             if (section < 0) section = 0;
             if (section >= items.size()) section = items.size() - 1;
             int c = 0;
             for (int i = 0; i < items.size(); i++) {
                     if (section == i) { 
                             return c;
                     }
                     c += items.get(i).second.size();
             }
             return 0;
     }
 
     @Override
     public int getSectionForPosition(int position) {
             int c = 0;
             for (int i = 0; i < items.size(); i++) {
                     if (position >= c && position < c + items.get(i).second.size()) {
                             return i;
                     }
                     c += items.get(i).second.size();
             }
             return -1;
     }
 
     @Override
     public String[] getSections() {
             String[] res = new String[items.size()];
             for (int i = 0; i < items.size(); i++) {
                     res[i] = items.get(i).first;
             }
             return res;
     }
 
     
     static class ViewHolder {
         TextView scheduleWeek;
         TextView scheduleDay;
         TextView scheduleStartTime;
         TextView scheduleEndTime;
         TextView scheduleCourseName;
         TextView scheduleClassNames;
         TextView scheduleRoomName;
         TextView scheduleLectureType;
     }
 }
