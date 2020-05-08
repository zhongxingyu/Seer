 package nl.napauleon.sabber.search;
 
 import android.app.Activity;
 import android.text.Html;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nl.napauleon.sabber.R;
 
 public class SearchListAdapter extends ArrayAdapter<NzbInfo> {
 
     private static final Pattern SANITIZE_KEYWORDS_PATTERN = Pattern.compile("[^a-z0-9-_ ]");
     private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
 
     private List<NzbInfo> content;
     private Activity activity;
     private String[] keywords;
 
     public SearchListAdapter(Activity activity, List<NzbInfo> content, String query) {
         super(activity, R.layout.searchrowlayout, content);
         this.activity = activity;
         this.content = content;
 
         if (TextUtils.isEmpty(query)) {
             this.keywords = new String[]{};
         } else {
             this.keywords = TextUtils.split(
                     SANITIZE_KEYWORDS_PATTERN.matcher(query).replaceAll(""),
                     WHITESPACE_PATTERN);
         }
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         LayoutInflater inflater = activity.getLayoutInflater();
         View rowView = inflater.inflate(R.layout.searchrowlayout, null, true);
 
         TextView nameView = (TextView) rowView.findViewById(R.id.name);
         TextView sizeView = (TextView) rowView.findViewById(R.id.size);
         NzbInfo nzbInfo = content.get(position);
         nameView.setText(highlightQueryInText(nzbInfo.getTitle()));
         sizeView.setText(nzbInfo.getSize());
         return rowView;
     }
 
     private CharSequence highlightQueryInText(String text) {
         if (TextUtils.isEmpty(text)) {
             return "";
         }
 
         String escaped = Html.escapeHtml(text);
         for (String keyword : keywords) {
             escaped = Pattern
                     .compile("(" + keyword + ")", Pattern.CASE_INSENSITIVE)
                     .matcher(escaped)
                     .replaceAll("<font color=#33b5e5><b>$1</b></font>");
         }
 
         return Html.fromHtml(escaped);
     }
 }
