 package org.gnuton.newshub.adapters;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import org.gnuton.newshub.R;
 import org.gnuton.newshub.types.RSSEntry;
 import org.gnuton.newshub.utils.FontsProvider;
 import org.gnuton.newshub.utils.NetworkUtils;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created by gnuton on 5/21/13.
  */
 public class ArticleListAdapter extends ArrayAdapter<RSSEntry> {
     private final List<RSSEntry> entries;
     private int dayOfTheMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
 
     public ArticleListAdapter(Context context, int textViewResourceId, List<RSSEntry> entries) {
         super(context, textViewResourceId, entries);
         this.entries = entries;
     }
 
     public static class ViewHolder{
         public TextView title;
         public TextView date;
         public TextView url;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         ViewHolder holder;
 
         // Create delegate (View + view holder) when needed, or get the holder for the current view to convert.
         if (v == null) {
             LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.articlelist_item, null);
             holder = new ViewHolder();
             holder.title = (TextView) v.findViewById(R.id.ListItemTitleTextView);
 
             holder.url = (TextView) v.findViewById(R.id.ListItemProviderTextView);
             holder.url.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"));
 
             holder.date = (TextView) v.findViewById(R.id.ListItemDateTextView);
             holder.date.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"));
 
             v.setTag(holder);
         }
         else
             holder=(ViewHolder)v.getTag();
 
         // Update the delegate setting data stored in the holder
         final RSSEntry e = entries.get(position);
         if (e != null) {
             Spanned titleSpanned = Html.fromHtml(e.title, null, null);
             holder.title.setText(titleSpanned, TextView.BufferType.SPANNABLE);
             holder.title.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"), e.isRead ? Typeface.NORMAL : Typeface.BOLD);
             holder.url.setText(NetworkUtils.getDomainName(e.link));
             holder.date.setText(dateToString(e.date));
         }
 
         // returns the updated delegate
         return v;
     }
 
     String dateToString(Calendar cal) {
         String strdate = null;
         SimpleDateFormat sdf;
 
         if (dayOfTheMonth == cal.get(Calendar.DAY_OF_MONTH))
             sdf = new SimpleDateFormat("HH:mm");
         else
             sdf = new SimpleDateFormat("EEE, dd MMM");
 
         if (cal != null) {
             strdate = sdf.format(cal.getTime());
         }
         return strdate;
     }
 }
