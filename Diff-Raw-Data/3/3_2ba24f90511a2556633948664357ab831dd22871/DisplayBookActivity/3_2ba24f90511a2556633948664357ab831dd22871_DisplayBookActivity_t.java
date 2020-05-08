 package com.github.norwae.whatiread;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.github.norwae.whatiread.data.BookInfo;
 import com.github.norwae.whatiread.util.Strings;
 
 public class DisplayBookActivity extends Activity {
 
 	private final class FinishCallback extends ProgressBarDialogCallback<Void> {
 		private FinishCallback() {
 			super(getWindow().getDecorView(), R.id.progressBar, R.id.save,
 					R.id.delete);
 		}
 
 		@Override
 		protected void onAsyncResult(Void aResult) {
 			finish();
 		}
 	}
 
 	public static final String BOOK_INFO_VARIABLE = "EXTRA_BOOK_INFO";
 	public static final String WARN_FOR_READ_BOOKS_VARIABLE = "EXTRA_DISPLAY_READ_WARNING";
 
 	private BookInfo info;
 	private MainMenuHandler menuHandler = new MainMenuHandler();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_book_display);
 		info = (BookInfo) getIntent().getExtras().get(BOOK_INFO_VARIABLE);
 
 		initViewFields();
 
 		if (getIntent().getExtras().getBoolean(WARN_FOR_READ_BOOKS_VARIABLE)
 				&& !info.isAddition()) {
 			AlertDialog alert = new AlertDialog.Builder(this)
 					.setMessage(getString(R.string.alert_alreadyKnown))
 					.setNegativeButton(android.R.string.ok,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									finish();
 								}
 							})
 					.setPositiveButton(R.string.action_edit,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// remain in the mask
 								}
 							}).create();
 			alert.show();
 		}
 	}
 
 	protected void deleteAndReturn() {
 		BookDelete delete = new BookDelete(this, new FinishCallback());
 
 		delete.execute(info);
 	}
 
 	protected void saveAndReturn() {
 		info.setComment(stringFromView(R.id.comment));
 		info.setAuthors(Strings.split(", ", stringFromView(R.id.author)));
 		info.setTitle(stringFromView(R.id.title));
 		info.setSubtitle(stringFromView(R.id.subtitle));
 		info.setPublisher(stringFromView(R.id.publisher));
 		info.setSeries(stringFromView(R.id.series));
 
 		BookSave persist = new BookSave(this, new FinishCallback());
 
 		persist.execute(info);
 	}
 
 	private String stringFromView(int id) {
 		TextView input = (TextView) findViewById(id);
 		CharSequence text = input.getText();
 		return text != null ? text.toString() : null;
 	}
 
 	private void initTextField(CharSequence value, int id) {
 		TextView view = (TextView) findViewById(id);
 		view.setText(value);
 	}
 
 	private void initViewFields() {
 		initTextField(Strings.join(", ", info.getAuthors()), R.id.author);
 		initTextField(info.getTitle(), R.id.title);
 		initTextField(info.getSubtitle(), R.id.subtitle);
 		initTextField(info.getSeries(), R.id.series);
 		initTextField(info.getPublisher(), R.id.publisher);
 		initTextField(
 				info.getPageCount() == 0 ? null : "" + info.getPageCount(),
 				R.id.pageCount);
 		initTextField(info.getIsbn(), R.id.isbn);
 		initTextField(info.getComment(), R.id.comment);
 
 		if (info.getThumbnailSmall() != null) {
 			new ImageFetch(this, R.id.coverImage).execute(info
 					.getThumbnailSmall());
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.display_book, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.save:
 			saveAndReturn();
 			return true;
 		case R.id.delete:
 			deleteAndReturn();
 			return true;
 		}
 		return menuHandler.handleMenuSelected(this, item.getItemId())
 				|| super.onMenuItemSelected(featureId, item);
 	}
 }
