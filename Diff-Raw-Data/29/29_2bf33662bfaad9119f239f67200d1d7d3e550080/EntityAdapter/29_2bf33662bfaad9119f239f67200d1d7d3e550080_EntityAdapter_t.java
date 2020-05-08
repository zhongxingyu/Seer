 package jp.mixi.androidstudy01;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import jp.mixi.androidstudy01.diary.entity.DiaryEntity;
 import jp.mixi.androidstudy01.entity.ComposeEntity;
 
 import java.text.SimpleDateFormat;
import java.util.Date;
 
 public class EntityAdapter extends ArrayAdapter<ComposeEntity> {
     /**
      * Androidのコーディングガイドラインでは、staticなprivate変数名には、接頭辞として"s"をつけます。
      *
      * Add "s" prefix to a private static member according to android code style guideline.
      */
     private static SimpleDateFormat sFormat;
     public EntityAdapter(Context context) {
         super(context, R.layout.main_list_item, R.id.MainListPostDate);
 
         sFormat = new SimpleDateFormat(
                 context.getString(R.string.activity_main_listview_date_format));
     }
 
     /**
      * ListViewの一行分を表すレイアウトを返します。
      * Returns a list row layout corresponding to the data.
      * <blockquote>
      * Get a View that displays the data at the specified position in the data set.
      * You can either create a View manually or inflate it from an XML layout file.
      * When the View is inflated, the parent View (GridView, ListView...) will apply
      * default layout parameters unless you use inflate(int, android.view.ViewGroup, boolean)
      * to specify a root view and to prevent attachment to the root.
      * </blockquote>
      *
      * @param position The position of the item within the adapter's data set of the item whose view we want.
      * @param convertView The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view. Heterogeneous lists can specify their number of view types, so that this View is always of the right type (see getViewTypeCount() and getItemViewType(int)).
      * @param parent The parent that this view will eventually be attached to
      * @return A View corresponding to the data at the specified position.
      */
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         // AdapterはGoFのデザインパターンの、Flyweightパターンに沿う形で実装する必要があります。
         // potisionの位置に来るList上のオブジェクトに対応するViewを毎回詰め込み直すような実装となります。
         View view = super.getView(position, convertView, parent);
         TextView title = (TextView) view.findViewById(R.id.MainListTitle);
        TextView body = (TextView) view.findViewById(R.id.MainListBody);
        TextView date = (TextView) view.findViewById(R.id.MainListPostDate);
 
        ComposeEntity entity = getItem(position);
 
        if (entity instanceof DiaryEntity) {
            DiaryEntity diary = (DiaryEntity) entity;
            title.setText(diary.getTitle());
            body.setText(diary.getBody());
            date.setText(sFormat.format(new Date(diary.getPostDate())));
        }
         return view;
     }
 }
