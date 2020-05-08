 package com.intalker.borrow.isbn;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import com.intalker.borrow.HomeActivity;
 import com.intalker.borrow.R;
 import com.intalker.borrow.data.AppData;
 import com.intalker.borrow.data.BookInfo;
 import com.intalker.borrow.isbn.parser.DoubanBookInfoParser;
 import com.intalker.borrow.isbn.parser.BookInfoParser;
 import com.intalker.borrow.isbn.parser.OpenISBNBookInfoParser;
 import com.intalker.borrow.ui.book.BookShelfItem;
 import com.intalker.borrow.ui.book.BookShelfView;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 
 public class ISBNResolver {
 
 	private static ISBNResolver instance = null;
 	private static BookInfoParser parser = null;
 
 	public static ISBNResolver getInstance() {
 		if (null == instance) {
 			instance = new ISBNResolver();
 		}
 		return instance;
 	}
 
 	public void getBookInfoByISBN(Context context, String isbn) {
 		GetBookInfoTask task = new GetBookInfoTask(context, isbn);
 		task.execute();
 	}
 	
 	public void batchGetBookInfo(Context context) {
 		BatchGetBookInfoTask task = new BatchGetBookInfoTask(context);
 		task.execute();
 	}
 	
 	private BookInfoParser getParser() {
 		if (null == parser) {
 			parser = new DoubanBookInfoParser();
 			//parser = new OpenISBNBookInfoParser();
 		}
 		return parser;
 	}
 
 	class GetBookInfoTask extends AsyncTask<String, Void, InputStream> {
 		private BookInfoParser isbnParser = null;
 		private ProgressDialog mProgressDialog = null;
 
 		public GetBookInfoTask(Context context, String isbn) {
 			super();
 			isbnParser = getParser();
 			isbnParser.reset(isbn);
 			mProgressDialog = new ProgressDialog(context);
 			mProgressDialog.setCancelable(false);
			mProgressDialog.setTitle(HomeActivity.getApp().getString(R.string.please_wait));
 			mProgressDialog.setMessage(HomeActivity.getApp().getString(R.string.searching_book_info) + isbn);
 			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			mProgressDialog.show();
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			
 			//TODO: should change to use BookGallery later.
 			BookShelfView.getInstance().addBookForLoading();
 			BookShelfItem lastBook = BookShelfItem.lastBookForTest;
 			if(null != lastBook)
 			{
 				lastBook.setISBN(isbnParser.getISBN());
 			}
 		}
 
 		@Override
 		protected InputStream doInBackground(String... params) {
 
 			isbnParser.parse();
 			
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(InputStream result) {
 			super.onPostExecute(result);
 			mProgressDialog.dismiss();
 			BookShelfItem lastBook = BookShelfItem.lastBookForTest;
 			if(null != lastBook)
 			{
 				Bitmap coverImage = isbnParser.getCoverImage();
 				lastBook.setDetailInfo(isbnParser.getBookName(),
 						isbnParser.getAuthor(), isbnParser.getPublisher(),
 						isbnParser.getPageCount(),
 						isbnParser.getDescription());
 				if (null != coverImage) {
 					lastBook.setCoverImage(coverImage);
 				}
 				else {
 					lastBook.setCoverAsUnknown();
 				}
 				lastBook.show();
 			}
 		}
 	}
 	
 	//Batch method
 	class BatchGetBookInfoTask extends AsyncTask<String, String, InputStream> {
 		private Context mContext = null;
 		private BookInfoParser mParser = null;
 		private ProgressDialog mProgressDialog = null;
 		
 		public BatchGetBookInfoTask(Context context) {
 			super();
 			mContext = context;
 			mParser = getParser();
 			mProgressDialog = new ProgressDialog(mContext);
 			mProgressDialog.setCancelable(false);
 			mProgressDialog.setTitle(mContext.getString(R.string.please_wait));
 			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			mProgressDialog.show();
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 		}
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			mProgressDialog.setMessage(mContext.getString(R.string.searching_book_info) + values[0]);
 		}
 
 		@Override
 		protected InputStream doInBackground(String... params) {
 
 			AppData appData = AppData.getInstance();
 			ArrayList<BookInfo> bookInfoList = appData.getBooks();
 			int length = bookInfoList.size();
 			for(int i = 0; i < length; ++i)
 			{
 				BookInfo bookInfo = bookInfoList.get(i);
 				if(null != bookInfo && !bookInfo.getInitialized())
 				{
 					String isbn = bookInfo.getISBN();
 					publishProgress(isbn);
 					
 					mParser.reset(isbn);
 					mParser.parse();
 					
 					bookInfo.setData(mParser);
 				}
 			}
 			
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(InputStream result) {
 			super.onPostExecute(result);
 			mProgressDialog.dismiss();
 			AppData appData = AppData.getInstance();
 			ArrayList<BookInfo> bookInfoList = appData.getBooks();
 			int length = bookInfoList.size();
 			for(int i = 0; i < length; ++i)
 			{
 				BookInfo bookInfo = bookInfoList.get(i);
 				if(null != bookInfo && !bookInfo.getInitialized())
 				{
 					BookShelfView.getInstance().addBookByExistingInfo(bookInfo);
 					bookInfo.setInitialized(true);
 				}
 			}
 		}
 	}
 }
