 /***************************************************************************
     begin                : aug 01 2010
     copyright            : (C) 2010 by Benoit Valot
     email                : benvalot@gmail.com
  ***************************************************************************/
 
 /***************************************************************************
  *                                                                         *
  *   This program is free software; you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation; either version 23 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  ***************************************************************************/
 
 package asi.val;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ArticlesList extends AsiActivity {
 	protected ListView maListViewPerso;
 
 	protected String color;
 
 	protected int image;
 
 	protected Parcelable state;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		// Récupération de la listview créée dans le fichier main.xml
 		maListViewPerso = (ListView) findViewById(R.id.listview);
 
 		int catId = this.getIntent().getExtras().getInt("id");
 		
 		// get the category
 		Uri catUri = ContentUris.withAppendedId(Category.CATEGORIES_URI, catId);
 		Cursor category = getContentResolver().query(catUri, null, null, null, null);
 
 		category.moveToFirst();
 		String title = category.getString(category.getColumnIndex(Category.TITLE_NAME));
 		String color = category.getString(category.getColumnIndex(Category.COLOR_NAME));
 		String image2 = category.getString(category.getColumnIndex(Category.IMAGE_NAME));
 		category.close();
 				
 		TextView text = (TextView) findViewById(R.id.list_text);
 		text.setText(title);
 		this.color = color;
 		text.setBackgroundColor(Color.parseColor(color));
 
 		// récupération de l'image
 		image = this.getResources().getIdentifier(
 				image2, "drawable",
 				this.getPackageName());
 		ImageView v = (ImageView) findViewById(R.id.cat_image);
 		if (image != 0) {
 			v.setImageResource(image);
 		} else
 			v.setImageResource(R.drawable.toutlesite);
 
 		this.loadContent();
 	}
 	
 	private class ArticleViewBinder implements ViewBinder {
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 			if (columnIndex == cursor.getColumnIndex(Article.COLOR_NAME)) {
 				view.setBackgroundColor(Color.parseColor(cursor.getString(columnIndex)));
 				return true;
 			} else if (columnIndex == cursor.getColumnIndex(Article.DATE_NAME)) {
 				Date date = new Date(cursor.getLong(columnIndex));
 				SimpleDateFormat format = new SimpleDateFormat("E dd/MM kk:mm");
 			    ((TextView) view).setText(format.format(date));
 				return true;
 			} else if (columnIndex == cursor.getColumnIndex(Article.DESCRIPTION_NAME)) {
 				int color = R.color.unread;
 				if (cursor.getInt(cursor.getColumnIndex(Article.READ_NAME)) == 1) {
 					color = R.color.read;
 				}
 				((View) view.getParent()).setBackgroundResource(color);
 				return false;
 			}
 			return false;
 		}
 	}
 
 	public void loadContent() {
 		int catId = this.getIntent().getExtras().getInt("id");
 		Cursor c = managedQuery(ContentUris.withAppendedId(Article.ARTICLES_URI, catId), null, null, null, Article.DATE_NAME +" DESC");
 		// create adapter
 		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.listview, c, 
 				new String[] { Article.COLOR_NAME, Article.TITLE_NAME, 
 							   Article.DESCRIPTION_NAME, Article.DATE_NAME },
 				new int[] { R.id.color, R.id.titre, R.id.description,
 							R.id.date });
 		// on ajoute le binder
 		adapter.setViewBinder(new ArticleViewBinder());
 		//on sauve
 		state = maListViewPerso.onSaveInstanceState();
 		maListViewPerso.setAdapter(adapter);
 		maListViewPerso.setEmptyView(findViewById(R.id.progress));
 		maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> adapterView, View view, int position,
 					long arg3) {
 				SimpleCursorAdapter adapter = (SimpleCursorAdapter) maListViewPerso.getAdapter();
 				Cursor c = adapter.getCursor();
 				c.moveToPosition(position);
 				String title = c.getString(c.getColumnIndex(Article.TITLE_NAME));
 				long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
 				ArticlesList.this.loadPage(id, title);
 			}
 		});
 		maListViewPerso
 				.setOnItemLongClickListener(new OnItemLongClickListener() {
 					public boolean onItemLongClick(AdapterView<?> a, View v,
 							int position, long arg3) {
 						SimpleCursorAdapter adapter = (SimpleCursorAdapter) maListViewPerso.getAdapter();
 						Cursor c = adapter.getCursor();
 						c.moveToPosition(position);
 						String url = c.getString(c.getColumnIndex(Article.URL_NAME));
 						String title = c.getString(c.getColumnIndex(Article.TITLE_NAME));
 						long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
 						ArticlesList.this.menuItem(id, url, title);
 						return false;
 					}
 
 				});
 		maListViewPerso.onRestoreInstanceState(state);
 	}
 
 	public void onResume() {
 		super.onResume();
 		Log.d("ASI", "liste_article onResume");
 		//loadContent();
 	}
 
 	public void onSaveInstanceState(final Bundle b) {
 		Log.d("ASI", "liste_article onSaveInstanceState");
 		state = maListViewPerso.onSaveInstanceState();
 		super.onSaveInstanceState(b);
 	}
 
 	private void menuItem(final long id, final String url, final String title) {
 		final CharSequence[] items = { "Visualiser", "Partager",
 				"Marquer comme lu" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(title);
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				if (items[item].equals("Visualiser")) {
 					ArticlesList.this.loadPage(id, title);
 				} else if (items[item].equals("Partager")) {
 					ArticlesList.this.share(url, title);
 				} else {
 					ArticlesList.this.markAsRead(id);
 				}
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void share(String url, String title) {
 		try {
 			Intent emailIntent = new Intent(Intent.ACTION_SEND);
 			emailIntent.putExtra(Intent.EXTRA_TEXT,
					"Un article interessant sur le site arretsurimage.net :\n"
 							+ title + "\n" + url);
 			emailIntent.setType("text/plain");
 			startActivity(Intent.createChooser(emailIntent,
 					"Partager cet article"));
 		} catch (Exception e) {
 			new ErrorDialog(this, "Chargement de la page", e).show();
 		}
 	}
 
 	private void loadPage(long id, String title) {
 		try {
 			Intent i = new Intent(this, Page.class);
 			i.putExtra("id", id);
 			i.putExtra("title", title);
 			this.startActivity(i);
 		} catch (Exception e) {
 			new ErrorDialog(this, "Chargement de la page", e).show();
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.liste_article_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.item4:
 			SimpleCursorAdapter adapter = (SimpleCursorAdapter) maListViewPerso.getAdapter();
 			Cursor c = adapter.getCursor();
 			c.moveToFirst();
 			for(int i = 0; i < c.getCount(); i++) {
 				c.moveToPosition(i);
 				this.markAsRead(c.getLong(c.getColumnIndex(BaseColumns._ID)));
 			}
 			// reload content
 			c.requery();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	protected void markAsRead(long id) {
 		Uri uri = ContentUris.withAppendedId(Article.ARTICLE_URI, id);
 		ContentValues values = new ContentValues();
 		values.put(Article.READ_NAME, 1);
 	    getContentResolver().update(uri, values, null, null);
 	}
 }
