 package edu.grinnell.sandb.data;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 
 /* Class to store the article images and image titles in a db */
 public class Image {
 
 	protected int id;
 	protected int articleId;
 	protected String url;
 	protected byte[] image;
 	protected String imgTitle;
 
 	protected Image(int ArticleID, String articleURL, byte[] articleImage,
 			String articleImgTitle) {
 		articleId = ArticleID;
 		url = articleURL;
 		image = articleImage;
 		imgTitle = articleImgTitle;
 	}
 
 	protected Image(int ID, int ArticleID, String articleURL,
 			byte[] articleImage, String articleImgTitle) {
 		this(ArticleID, articleURL, articleImage, articleImgTitle);
 		id = ID;
 	}
 
 	public Drawable toDrawable(Context c) {
 		
 		Bitmap bm = toBitmap();
 		Drawable d = new BitmapDrawable(c.getResources(), bm);
 		d.setBounds(0, 0, 0 + d.getIntrinsicWidth(), 0 
                 + d.getIntrinsicHeight()); 
 		return d;	
 	}
 	
 	public Bitmap toBitmap() {
		return (image != null) ? BitmapFactory.decodeByteArray(image, 0, image.length)
				: null;
 	}
 	
 	public int getId() {
 		return id;
 	}
 
 	public int getArticleId() {
 		return articleId;
 	}
 
 	public String getURL() {
 		return url;
 	}
 
 	public byte[] getImg() {
 		return image;
 	}
 
 	public String getImgTitle() {
 		return imgTitle;
 	}
 
 }
