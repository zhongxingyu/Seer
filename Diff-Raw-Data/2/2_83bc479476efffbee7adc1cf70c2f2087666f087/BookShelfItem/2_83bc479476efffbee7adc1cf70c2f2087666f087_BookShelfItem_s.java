 package com.intalker.borrow.ui.book;
 
 import com.intalker.borrow.R;
 import com.intalker.borrow.data.AppData;
 import com.intalker.borrow.data.BookInfo;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.RelativeLayout;
 
 public class BookShelfItem extends RelativeLayout {
 	public static BookShelfItem lastBookForTest = null;
	private static BookDetailDialog detailDialog = null;
 	private ImageView mCoverImageView = null;
 	private BookInfo mInfo = null;
 
 	public BookShelfItem(Context context, BookInfo bookInfo) {
 		super(context);
 		createUI();
 		if (null != bookInfo) {
 			mInfo = bookInfo;
 			Bitmap coverImage = mInfo.getCoverImage();
 			if(null != coverImage)
 			{
 				mCoverImageView.setImageBitmap(coverImage);
 			}
 			else
 			{
 				mCoverImageView.setImageResource(R.drawable.bookcover_unknown);
 			}
 		} else {
 			mInfo = new BookInfo();
 			AppData.getInstance().addBook(mInfo);
 		}
 		
 		this.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				BookShelfItem item = (BookShelfItem) arg0;
 				if(null != item)
 				{
 					if(null == detailDialog)
 					{
 						detailDialog = new BookDetailDialog(arg0.getContext());
 					}
 					detailDialog.setInfo(item.mInfo);
 					detailDialog.show();
 				}
 			}
 			
 		});
 	}
 
 	public void setCoverImage(Bitmap coverImage) {
 		mCoverImageView.setImageBitmap(coverImage);
 		mInfo.setCoverImage(coverImage);
 	}
 
 	public void setISBN(String isbn) {
 		mInfo.setISBN(isbn);
 	}
 
 	public void setCoverAsUnknown() {
 		mCoverImageView.setImageResource(R.drawable.bookcover_unknown);
 	}
 
 	private void createUI() {
 		mCoverImageView = new ImageView(this.getContext());
 		RelativeLayout.LayoutParams coverImgaeViewLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		mCoverImageView.setScaleType(ScaleType.FIT_END);
 
 		double random = Math.random();
 
 		int resId = 0;
 		if (random < 0.3) {
 			resId = R.drawable.bookcover_test;
 		} else if (random < 0.7) {
 			resId = R.drawable.bookcover_test1;
 		} else {
 			resId = R.drawable.bookcover_test2;
 		}
 		mCoverImageView.setImageResource(resId);
 
 		coverImgaeViewLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		coverImgaeViewLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		this.addView(mCoverImageView, coverImgaeViewLP);
 	}
 
 	public void hideBeforeLoaded() {
 		this.setVisibility(View.GONE);
 		lastBookForTest = this;
 	}
 
 	public void show() {
 		this.setVisibility(View.VISIBLE);
 	}
 	
 	public void setDetailInfo(String name, String author, String publisher,
 			String pageCount, String description) {
 		mInfo.setName(name);
 		mInfo.setAuthor(author);
 		mInfo.setPublisher(publisher);
 		mInfo.setPageCount(pageCount);
 		mInfo.setDescription(description);
 	}
 }
