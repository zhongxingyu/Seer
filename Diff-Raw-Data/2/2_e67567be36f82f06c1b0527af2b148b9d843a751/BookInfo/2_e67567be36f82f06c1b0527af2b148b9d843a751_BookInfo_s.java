 package com.intalker.borrow.data;
 
 import com.intalker.borrow.isbn.parser.BookInfoParser;
 import android.graphics.Bitmap;
 
 public class BookInfo {
 	private String mISBN = "";
 	private String mName = "";
 	private String mAuthor = "";
 	private String mPublisher = "";
 	private String mPageCount = "";
 	private String mSummary = "";
 	private int mQuantity = 1;
 	
 	private Bitmap mCoverImage = null;
 	
 	//Currently, only used for synchronizing with cloud
 	private boolean mInitialized = true;
 	private boolean mFoundCacheData = false;
 
 	public BookInfo() {
 	}
 
 	public BookInfo(String isbn) {
 		setISBN(isbn);
 	}
 	
 	public BookInfo clone() {
 		BookInfo bookInfo = new BookInfo();
 		bookInfo.setISBN(mISBN);
 		bookInfo.setBookName(mName);
 		bookInfo.setAuthor(mAuthor);
 		bookInfo.setSummary(mSummary);
 		bookInfo.setPageCount(mPageCount);
 		bookInfo.setPublisher(mPublisher);
 		try {
 			if (null != mCoverImage) {
 				bookInfo.setCoverImage(mCoverImage.copy(
 						mCoverImage.getConfig(), true));
 			}
		} catch (Exception ex) {
 
 		}
 		
 		//bookInfo.setInitialized(true);
 		return bookInfo;
 	}
 	
 	public void setFoundCacheData(boolean foundCacheData) {
 		mFoundCacheData = foundCacheData;
 	}
 	
 	public void setInitialized(boolean initialized) {
 		mInitialized = initialized;
 	}
 
 	public void setISBN(String isbn) {
 		mISBN = isbn;
 	}
 
 	public void setCoverImage(Bitmap coverImage) {
 		mCoverImage = coverImage;
 	}
 
 	public void setBookName(String name) {
 		mName = name;
 	}
 
 	public void setAuthor(String author) {
 		mAuthor = author;
 	}
 
 	public void setPublisher(String publisher) {
 		mPublisher = publisher;
 	}
 
 	public void setPageCount(String count) {
 		mPageCount = count;
 	}
 	
 	public void setQuantity(int quantity) {
 		mQuantity = quantity;
 	}
 	
 	public void setSummary(String summary) {
 		mSummary = summary;
 	}
 
 	public String getISBN() {
 		return mISBN;
 	}
 
 	public Bitmap getCoverImage() {
 		return mCoverImage;
 	}
 
 	public String getBookName() {
 		return mName;
 	}
 
 	public String getAuthor() {
 		return mAuthor;
 	}
 
 	public String getPublisher() {
 		return mPublisher;
 	}
 
 	public String getPageCount() {
 		return mPageCount;
 	}
 	
 	public String getSummary() {
 		return mSummary;
 	}
 	
 	public boolean getInitialized() {
 		return mInitialized;
 	}
 	
 	public boolean getFoundCacheData() {
 		return mFoundCacheData;
 	}
 	
 	public int getQuantity() {
 		return mQuantity;
 	}
 	
 	public void setData(BookInfoParser parser) {
 		setISBN(parser.getISBN());
 		setBookName(parser.getBookName());
 		setAuthor(parser.getAuthor());
 		setCoverImage(parser.getCoverImage());
 		setSummary(parser.getDescription());
 		setPageCount(parser.getPageCount());
 		setPublisher(parser.getPublisher());
 		setInitialized(true);
 		
 		cacheData();
 	}
 
 	public void cacheData() {
 		CacheData.cacheBookInfo(this.clone());
 	}
 }
