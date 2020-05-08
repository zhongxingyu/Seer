 package com.github.sgdesmet.android.utils.list.items;
 
 import android.content.Context;
 import android.view.*;
 import android.widget.*;
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
 public class SimpleCheckedListItem extends SimpleListItem {
 
    public static final int ITEM_VIEW_TYPE = 3;
 
     boolean checked;
 
     protected View.OnClickListener checkBoxListener;
 
     public SimpleCheckedListItem(final Context applicationContext, final String imageUrl, final String title, final String description, boolean checked,
                                  final View.OnClickListener onClickListener, final View.OnClickListener checkBoxListener) {
 
         super(applicationContext, imageUrl, title, description, onClickListener );
         this.checked = checked;
         this.checkBoxListener = checkBoxListener;
     }
 
     public View.OnClickListener getCheckBoxListener() {
 
         return checkBoxListener;
     }
 
     public void setCheckBoxListener(final View.OnClickListener checkBoxListener) {
 
         this.checkBoxListener = checkBoxListener;
     }
 
     public boolean isChecked() {
 
         return checked;
     }
 
     public void setChecked(final boolean checked) {
 
         this.checked = checked;
     }
 
     @Override
     public View inflate(final View convertView, final ViewGroup parent) {
 
         ViewHolder holder;
         View rowView = convertView;
         if (rowView == null) {
             LayoutInflater inflater = (LayoutInflater) applicationContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
             holder = new ViewHolder();
             rowView = inflater.inflate( R.layout.simple_row_text_image_checkbox, null );
             holder.imageView = (ImageView) rowView.findViewById( R.id.utils_row_image );
             holder.title = (TextView) rowView.findViewById( R.id.utils_row_text );
             holder.description = (TextView) rowView.findViewById( R.id.utils_row_description );
             holder.checkBox = (CheckBox) rowView.findViewById( R.id.utils_row_checkbox );
             holder.checkBox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                     if (SimpleCheckedListItem.this.checked != isChecked)
                         SimpleCheckedListItem.this.checked = isChecked;
                 }
             } );
             holder.checkBox.setFocusable( false );
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
                 if (checked != holder.checkBox.isChecked())
                     holder.checkBox.setChecked( checked );
                 if (checkBoxListener != null){
                     holder.checkBox.setOnClickListener( checkBoxListener );
                 }
                 holder.content = this;
             }
         }
     }
 
     @Override
     public int viewType() {
 
         return ITEM_VIEW_TYPE;
     }
 
     @Override
     public boolean equals(final Object o) {
 
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
         if (!super.equals( o )) {
             return false;
         }
 
         SimpleCheckedListItem that = (SimpleCheckedListItem) o;
 
         if (checked != that.checked) {
             return false;
         }
         if (checkBoxListener != null? !checkBoxListener.equals( that.checkBoxListener ): that.checkBoxListener != null) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
 
         int result = super.hashCode();
         result = 31 * result + (checked? 1: 0);
         result = 31 * result + (checkBoxListener != null? checkBoxListener.hashCode(): 0);
         return result;
     }
 
     public static ListItem item(final Context applicationContext, final String imageUrl, final String title, final String description, boolean checked,
                                       final View.OnClickListener onClickListener, final View.OnClickListener checkBoxListener) {
 
         return new SimpleCheckedListItem( applicationContext, imageUrl, title, description, checked, onClickListener, checkBoxListener );
     }
 }
