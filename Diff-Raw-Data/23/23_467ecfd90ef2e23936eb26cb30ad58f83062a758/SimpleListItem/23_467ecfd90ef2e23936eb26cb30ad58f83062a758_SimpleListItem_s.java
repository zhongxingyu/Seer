 package com.github.sgdesmet.android.utils.list.items;
 
 import android.content.Context;
 import android.view.*;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.github.sgdesmet.android.utils.R;
 import com.github.sgdesmet.android.utils.list.ListItem;
 import com.github.sgdesmet.android.utils.service.image.loader.ImageLoader;
 import com.github.sgdesmet.android.utils.service.image.loader.ImageLoaderFactory;
 
 
 /**
  * TODO description
  * <p/>
  * Date: 17/06/13
  * Time: 17:12
  *
  * @author: sgdesmet
  */
 public class SimpleListItem implements ListItem {
 
    public static final int ITEM_VIEW_TYPE = 1;
 
     String imageUrl;
     CharSequence title;
     CharSequence description;
 
     boolean clickable = true;
 
     Context applicationContext;
 
     protected View.OnClickListener onClickListener;
 
     protected SimpleListItem(){
 
     }
 
     public SimpleListItem(final Context applicationContext, final String imageUrl, final CharSequence title, final CharSequence description,
                           final View.OnClickListener onClickListener) {
 
         this.imageUrl = imageUrl;
         this.title = title;
         this.description = description;
         this.onClickListener = onClickListener;
         this.applicationContext = applicationContext;
     }
 
     public Context getApplicationContext() {
 
         return applicationContext;
     }
 
     public void setApplicationContext(final Context applicationContext) {
 
         this.applicationContext = applicationContext;
     }
 
     public String getImageUrl() {
 
         return imageUrl;
     }
 
     public void setImageUrl(final String imageUrl) {
 
         this.imageUrl = imageUrl;
     }
 
     public CharSequence getTitle() {
 
         return title;
     }
 
     public void setTitle(final CharSequence title) {
 
         this.title = title;
     }
 
     public CharSequence getDescription() {
 
         return description;
     }
 
     public void setDescription(final String description) {
 
         this.description = description;
     }
 
     public View.OnClickListener getOnClickListener() {
 
         return onClickListener;
     }
 
     public void setOnClickListener(final View.OnClickListener onClickListener) {
 
         this.onClickListener = onClickListener;
     }
 
     public boolean isClickable() {
 
         return clickable;
     }
 
     public void setClickable(final boolean clickable) {
 
         this.clickable = clickable;
     }
 
     @Override
     public void onClick() {
 
         if (onClickListener != null) {
             onClickListener.onClick( null );
         }
     }
 
     @Override
     public View inflate(final View convertView, final ViewGroup parent) {
 
         ViewHolder holder;
         View rowView = convertView;
         if (rowView == null) {
             LayoutInflater inflater = (LayoutInflater) applicationContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
             holder = new ViewHolder();
             rowView = inflater.inflate( R.layout.simple_row_text_image, null );
             holder.imageView = (ImageView) rowView.findViewById( R.id.utils_row_image );
             holder.title = (TextView) rowView.findViewById( R.id.utils_row_text );
             holder.description = (TextView) rowView.findViewById( R.id.utils_row_description );
             rowView.setTag( holder );
         }
         return rowView;
     }
 
     @Override
     public void configure(final View inflatedView) {
 
         if (inflatedView != null) {
             ViewHolder holder = (ViewHolder) inflatedView.getTag();
             if (!this.equals( holder.content )) {
                 if (imageUrl != null)
                     ImageLoaderFactory.get()
                                       .loadImage( imageUrl, holder.imageView, ImageLoader.KEEP_CURRENT, ImageLoader.KEEP_CURRENT );
                 else
                     holder.imageView.setVisibility( View.GONE );
                 if (title != null)
                     holder.title.setText( title );
                 if (description != null)
                     holder.description.setText( description );
                 holder.content = this;
             }
         }
     }
 
     @Override
     public int viewType() {
 
         return ITEM_VIEW_TYPE;
     }
 
     @Override
     public boolean enabled() {
 
         return clickable;
     }
 
     @Override
     public boolean equals(final Object o) {
 
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         SimpleListItem that = (SimpleListItem) o;
 
         if (applicationContext != null? !applicationContext.equals( that.applicationContext ): that.applicationContext != null) {
             return false;
         }
         if (description != null? !description.equals( that.description ): that.description != null) {
             return false;
         }
         if (imageUrl != null? !imageUrl.equals( that.imageUrl ): that.imageUrl != null) {
             return false;
         }
         if (onClickListener != null? !onClickListener.equals( that.onClickListener ): that.onClickListener != null) {
             return false;
         }
         if (title != null? !title.equals( that.title ): that.title != null) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
 
         int result = imageUrl != null? imageUrl.hashCode(): 0;
         result = 31 * result + (title != null? title.hashCode(): 0);
         result = 31 * result + (description != null? description.hashCode(): 0);
         result = 31 * result + (applicationContext != null? applicationContext.hashCode(): 0);
         result = 31 * result + (onClickListener != null? onClickListener.hashCode(): 0);
         return result;
     }
 
     public static ListItem item(final Context applicationContext, final String imageUrl, final CharSequence title, final CharSequence description,
                                       final View.OnClickListener onClickListener) {
 
         return new SimpleListItem( applicationContext, imageUrl, title, description, onClickListener );
     }
 
     public static ListItem item(final Context applicationContext, final String imageUrl, final CharSequence title, final CharSequence description,
                                 final View.OnClickListener onClickListener, boolean clickable) {
 
         SimpleListItem item = new SimpleListItem( applicationContext, imageUrl, title, description, onClickListener );
         item.setClickable( clickable );
         return item;
     }
 }
