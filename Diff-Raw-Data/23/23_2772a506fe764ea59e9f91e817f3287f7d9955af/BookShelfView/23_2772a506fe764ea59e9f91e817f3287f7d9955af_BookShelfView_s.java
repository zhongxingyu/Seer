 package com.intalker.borrow.ui.book;
 
 import java.util.ArrayList;
 import com.intalker.borrow.R;
 import com.intalker.borrow.data.BookInfo;
import com.intalker.borrow.ui.control.DragRefreshScroll;
 import com.intalker.borrow.util.LayoutUtil;
 
 import android.content.Context;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 
 //public class BookShelfView extends DragRefreshScroll {	// Test code for using drag-down refresh
 public class BookShelfView extends ScrollView {
 	private LinearLayout mScrollContent = null;
 	private ArrayList<BookShelfRow> mBookShelfRows = new ArrayList<BookShelfRow>();
 	private static BookShelfView instance = null;
 
 	public static BookShelfView getInstance() {
 		return instance;
 	}
 
 	public BookShelfView(Context context) {
 		super(context);
 
 		initializeUI();
 
 		addRow(false);
 
 		this.setBackgroundResource(R.drawable.wood_bk);
 		instance = this;
 	}
 
 	private void initializeUI() {
 
 		// This is the test code for using drag-down refresh
 		// {
 		// mScrollContent = this.getScrollContent();
 		// }
 
 		{
 			mScrollContent = new LinearLayout(this.getContext());
 			mScrollContent.setOrientation(LinearLayout.VERTICAL);
 			this.addView(mScrollContent);
 		}
 	}
 
 	public void addBookForLoading() {
 		BookShelfRow lastRow = mBookShelfRows.get(mBookShelfRows.size() - 1);
 		if (lastRow.isFull()) {
 			lastRow = addRow(false);
 		}
 		lastRow.addBook(false, null);
 	}
 
 	public void clearShelfRows() {
 		for (BookShelfRow row : mBookShelfRows) {
 			mScrollContent.removeView(row);
 		}
 		mBookShelfRows.clear();
 	}
 
 	public void initializeShelfRows() {
 		for (BookShelfRow row : mBookShelfRows) {
 			mScrollContent.removeView(row);
 		}
 		mBookShelfRows.clear();
 		addRow(false);
 	}
 
 	private BookShelfRow addRow(boolean createRandomBooks) {
 		BookShelfRow row = new BookShelfRow(this.getContext(),
 				createRandomBooks);
 		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.FILL_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		lp.height = LayoutUtil.getShelfRowHeight();
 		mScrollContent.addView(row, lp);
 		mBookShelfRows.add(row);
 		return row;
 	}
 
 	// for test
 	public void addRandomBooks() {
 		for (int i = 0; i < 10; ++i) {
 			addRow(true);
 		}
 	}
 
 	public void addBookByExistingInfo(BookInfo bookInfo) {
 		BookShelfRow lastRow = mBookShelfRows.get(mBookShelfRows.size() - 1);
 		if (lastRow.isFull()) {
 			lastRow = addRow(false);
 		}
 		lastRow.addBook(true, bookInfo);
 	}
 }
