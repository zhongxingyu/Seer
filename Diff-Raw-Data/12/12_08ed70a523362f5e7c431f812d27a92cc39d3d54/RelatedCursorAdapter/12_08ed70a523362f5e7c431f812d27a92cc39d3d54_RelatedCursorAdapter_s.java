 package ru.rutube.RutubeApp.data;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import ru.rutube.RutubeAPI.content.FeedContract;
 import ru.rutube.RutubeAPI.models.Video;
 import ru.rutube.RutubeApp.BuildConfig;
 import ru.rutube.RutubeApp.R;
 import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Сергей
  * Date: 11.05.13
  * Time: 11:53
  * To change this template use File | Settings | File Templates.
  */
 
 /**
  * Адаптер для ленты похожих видео
  */
 public class RelatedCursorAdapter extends FeedCursorAdapter {
     protected static final boolean D = BuildConfig.DEBUG;
     private static final String LOG_TAG = RelatedCursorAdapter.class.getName();
    static {
        THUMBNAIL_SIZE = "s";
    }
 
     protected static class ViewHolder extends FeedCursorAdapter.ViewHolder {
         public TextView hits;
     }
 
     public RelatedCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
         super(context, layout, c, from, to, flags);
     }
 
     @Override
     protected ViewHolder initHolder(View view) {
         ViewHolder holder = new ViewHolder();
         initHolder(view, holder);
         return holder;
     }
 
     @Override
     protected void initHolder(View view, FeedCursorAdapter.ViewHolder holder) {
         super.initHolder(view, holder);
         ViewHolder h = (ViewHolder)holder;
         h.hits = (TextView) view.findViewById(R.id.hitsTextView);
     }
 
     @Override
     protected void initView(FeedCursorAdapter.ViewHolder holder) {
         ViewHolder h = (ViewHolder)holder;
         h.title.setTypeface(mNormalFont);
         h.author.setTypeface(mLightFont);
         h.hits.setTypeface(mLightFont);
         initOnClickListeners(holder);
     }
 
     @Override
     protected void bindCreated(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
     }
 
     @Override
     protected void bindAvatar(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
     }
 
     @Override
     protected void bindDescription(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
     }
 
     @Override
     protected void bindDuration(Cursor cursor, FeedCursorAdapter.ViewHolder holder) {
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View view = super.getView(position, convertView, parent);
         decorateItem(view, position == 0);
         return view;
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         super.bindView(view, context, cursor);
         try {
             bindHits(cursor, (ViewHolder)getViewHolder(view));
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     protected void setTags(int position, View view) {
         ClickTag tag = new ClickTag(position);
         ViewHolder holder = (ViewHolder)getViewHolder(view);
         holder.title.setTag(tag);
         holder.thumbnail.setTag(tag);
         holder.hits.setTag(tag);
 
     }
 
     @Override
     protected void initOnClickListeners(FeedCursorAdapter.ViewHolder holder) {
         ViewHolder h = (ViewHolder)holder;
         h.title.setOnClickListener(mOnClickListener);
         h.thumbnail.setOnClickListener(mOnClickListener);
         h.hits.setOnClickListener(mOnClickListener);
     }
 
     protected void bindHits(Cursor cursor, ViewHolder holder) {
         int hitsIndex = cursor.getColumnIndexOrThrow(FeedContract.RelatedVideo.HITS);
         int hits = cursor.getInt(hitsIndex);
         holder.hits.setText(Video.getHitsText(mContext, hits));
     }
 
     /**
      * Изменяет представление элемента списка в зависимости от того, первый ли он в списке.
      * @param view элемент списка
      * @param isFirst флаг первого элемента списка
      */
     private void decorateItem(View view, boolean isFirst) {
         View cardView = view.findViewById(R.id.card);
         // при проставлении в качестве фона 9-patch, отступы сбрасываются
         int pl = cardView.getPaddingLeft();
         int pt = cardView.getPaddingTop();
         int pr = cardView.getPaddingRight();
         int pb = cardView.getPaddingBottom();
         // меняем фон для первого элемента списка
         if (isFirst)
             cardView.setBackgroundResource(R.drawable.first_related_bg);
         else
             cardView.setBackgroundResource(R.color.card_background);
 
         // меняем верхний отступ для рамки элемента
         int topItemPadding = (int) mContext.getResources().getDimension(
                 R.dimen.related_item_padding_top);
         // меняем верхний отступ для карточки видео (влияет установленный 9-patch)
         int topCardPadding = (int) mContext.getResources().getDimension(
                 R.dimen.related_card_padding_top);
 
         cardView.setPadding(pl, isFirst? topCardPadding: pt, pr, pb);
         // первая карточка задает верхним отступом рамку для всего списка, поэтому topItemPadding
         view.setPadding(
                 view.getPaddingLeft(),
                 isFirst ? topItemPadding: 0,
                 view.getPaddingRight(),
                 view.getPaddingBottom());
     }
 
 }
