 /**
  * Copyright 2013 MicMun
  *
  * This program is free software: you can redistribute it and/or modify it under 
  * the terms of the GNU >General Public License as published by the 
  * Free Software Foundation, either version 3 of the License, or >
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; >without even the implied warranty of MERCHANTABILITY 
  * or FITNESS FOR A PARTICULAR PURPOSE. >See the GNU General Public License 
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along with 
  * this program. If not, see >http://www.gnu.org/licenses/.
  */
 package de.micmun.android.miwotreff.utils;
 
 import android.content.Context;
 import android.content.res.ColorStateList;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.widget.Filterable;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.ResourceCursorAdapter;
 import android.widget.TextView;
 
 import java.util.GregorianCalendar;
 
 import de.micmun.android.miwotreff.R;
 
 /**
  * Handles the view of the data for the {@link ListView}.
  *
  * @author MicMun
  * @version 2.0, 18.01.2013
  */
 public class SpecialCursorAdapter
         extends ResourceCursorAdapter
         implements Filterable {
 
     /**
      * Creates a new SpecialCursorAdapter with context and cursor.
      *
      * @param ctx Context.
      * @param c   Cursor.
      */
     public SpecialCursorAdapter(Context ctx, Cursor c) {
         super(ctx, R.layout.list_row, c, false);
     }
 
     /**
      * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context,
      * android.database.Cursor)
      */
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         ViewHolder viewHolder = (ViewHolder) view.getTag();
         if (viewHolder == null) {
             viewHolder = new ViewHolder(view, context);
             view.setTag(viewHolder);
         }
         viewHolder.bind(cursor);
     }
 
     /**
      * Manage the text fields of a row (binding data to view).
      *
      * @author Michael Munzert, Maximilian Salomon
      * @version 1.0, 11.12.2012
      */
     private static class ViewHolder {
 
         private Context ctx;
         private ColorStateList normalTextColor = null;
         private TextView datum;
         private TextView thema;
         private TextView person;
         private RelativeLayout background;
         private String nextWednesday = null;
 
         /**
          * Creates a new ViewHolder with a view.
          *
          * @param view View.
          */
         public ViewHolder(View view, Context context) {
             ctx = context;
             datum = (TextView) view.findViewById(R.id.text_datum);
             thema = (TextView) view.findViewById(R.id.text_thema);
             person = (TextView) view.findViewById(R.id.text_person);
             background = (RelativeLayout) view.findViewById(R.id.row_background);
 
             normalTextColor = datum.getTextColors();
         }
 
         /**
          * Binds the data from cursor to the text fields.
          *
          * @param c Cursor.
          */
         public void bind(Cursor c) {
             if (c != null) {
                 // date
                 long d = c.getLong(c.getColumnIndex(DBConstants.KEY_DATUM));
                 String sd = DBDateUtility.getDateString(d);
                 datum.setText(sd);
 
                 if (isNextWednesday(sd)) {
                    background.setBackgroundResource
                            (R.drawable.list_row_background_drawable);
                    //background.setBackground(ctx.getResources().getDrawable(R.drawable.list_row_background_drawable));
                    datum.setTextColor(ctx.getResources().getColor(R.color.white));
                    thema.setTextColor(ctx.getResources().getColor(R.color.white));
                    person.setTextColor(ctx.getResources().getColor(R.color.white));
                 } else if (normalTextColor != null) {
                     background.setBackgroundColor(Color.TRANSPARENT);
                     datum.setTextColor(normalTextColor);
                     thema.setTextColor(normalTextColor);
                     person.setTextColor(normalTextColor);
                 }
 
                 // topic
                 String t = c.getString(c.getColumnIndex(DBConstants.KEY_THEMA));
                 thema.setText(t);
 
                 // person
                 String p = c.getString(c.getColumnIndex(DBConstants.KEY_PERSON));
                 person.setText(p);
             }
         }
 
         /**
          * Returns <code>true</code>, if the date is the next wednesday.
          *
          * @param d Date in format dd.MM.yyyy.
          * @return <code>true</code>, if the date is the next wednesday.
          */
         private boolean isNextWednesday(String d) {
             if (nextWednesday == null) {
                 GregorianCalendar today = new GregorianCalendar();
 
                 int diff = GregorianCalendar.WEDNESDAY -
                         today.get(GregorianCalendar.DAY_OF_WEEK);
 
                 if (!(diff >= 0)) {
                     diff += 7;
                 }
                 today.add(GregorianCalendar.DAY_OF_MONTH, diff);
                 nextWednesday = DateFormat.format("dd.MM.yyyy", today).toString();
             }
 
             if (nextWednesday.equals(d)) {
                 return true;
             }
 
             return false;
         }
     }
 }
