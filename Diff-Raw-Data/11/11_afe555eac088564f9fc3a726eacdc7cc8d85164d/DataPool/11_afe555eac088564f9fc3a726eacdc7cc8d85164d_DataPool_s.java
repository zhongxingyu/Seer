 package it.giacomos.android.osmer.network.Data;
 
 import it.giacomos.android.osmer.network.state.BitmapType;
 import it.giacomos.android.osmer.network.state.ViewType;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 
 public class DataPool implements DownloadListener 
 {	
 	private HashMap<BitmapType, Boolean> mForceUpdateOnSameBitmapHash;	
 	private HashMap<BitmapType, BitmapData> mBitmapDataHash;	
 	private HashMap<ViewType, StringData> mStringData;	
 	private HashMap<BitmapType, DataPoolBitmapListener> mBitmapListeners;	
 	private HashMap<ViewType, DataPoolTextListener> mTextListeners;	
 	private ArrayList<DataPoolErrorListener> mDataPoolErrorListeners;	
 	private Context mContext;
 
 	public HashMap<BitmapType, BitmapData> getBitmapData()
 	{
 		return mBitmapDataHash;
 	}
 	
 	public HashMap<ViewType, StringData>  getStringData()
 	{
 		return mStringData;
 	}
 	
 	public DataPool(Context ctx)
 	{
 		mBitmapDataHash = new HashMap<BitmapType, BitmapData>();
 		mStringData = new HashMap<ViewType, StringData>();
 		mBitmapListeners = new HashMap<BitmapType, DataPoolBitmapListener>();
 		mTextListeners = new HashMap<ViewType, DataPoolTextListener>();
 		mForceUpdateOnSameBitmapHash = new HashMap<BitmapType, Boolean>();
 		mDataPoolErrorListeners = new ArrayList<DataPoolErrorListener>();
 		mContext = ctx;
 	}
 	
 	public void setForceUpdateEvenOnSameBitmap(BitmapType bt, boolean force)
 	{
 		mForceUpdateOnSameBitmapHash.put(bt, force);
 	}
 
 	public void clear()
 	{
 		/* recycle all bitmaps */
 		for(BitmapData bmpd : mBitmapDataHash.values())
 		{
 			if(bmpd.bitmap != null)
 			{
 				bmpd.bitmap.recycle();
 				bmpd.bitmap = null;
 				bmpd = null;
 			}
 		}
 		/* clear data hashes */
 		mBitmapDataHash.clear();
 		mStringData.clear();
 		mBitmapListeners.clear();
 		mTextListeners.clear();
 		mForceUpdateOnSameBitmapHash.clear();
 		mDataPoolErrorListeners.clear();
 	}
 	
 	public boolean isTextValid(ViewType vt)
 	{
 		return mStringData.containsKey(vt) && mStringData.get(vt).text != null;
 	}
 	
 	public boolean isBitmapValid(BitmapType bt)
 	{
 		return mBitmapDataHash.containsKey(bt) && mBitmapDataHash.get(bt).bitmap != null;
 	}
 	
 	public void registerErrorListener(DataPoolErrorListener l)
 	{
 		mDataPoolErrorListeners.add(l);
 	}
 	
 	public void unregisterTextListener(ViewType vt)
 	{
 		if(vt != null && mTextListeners.containsKey(vt))
 			mTextListeners.remove(vt);
 	}
 	
 	public void unregisterBitmapListener(BitmapType bt)
 	{
 		if(bt != null && mBitmapListeners.containsKey(bt))
 			mBitmapListeners.remove(bt);
 	}
 	
 	public void registerTextListener(ViewType vt, DataPoolTextListener txtL)
 	{
 		mTextListeners.put(vt, txtL);
 		/* immediately notify if data is present */
 		if(mStringData.containsKey(vt))
 		{
 			StringData sd = mStringData.get(vt);
 			/* onTextRefresh called with a final true parameter to indicate that
 			 * the text has changed (which is true for a new text listener just 
 			 * registered)
 			 */
 			if(sd.isValid())
 				txtL.onTextChanged(sd.text, vt, sd.fromCache);
 			else
 				txtL.onTextError(sd.error, vt);
 		}
 	}
 	
 	public void registerBitmapListener(BitmapType bt, DataPoolBitmapListener bmpL)
 	{
 		mBitmapListeners.put(bt, bmpL);
 		if(mBitmapDataHash.containsKey(bt))
 		{
 			BitmapData bd = mBitmapDataHash.get(bt);
 			if(bd.isValid())
 				bmpL.onBitmapChanged(bd.bitmap, bt, bd.fromCache);
 			else
 				bmpL.onBitmapError(bd.error, bt);
 		}
 	}
 
 	@Override
 	public void onTextBytesUpdate(byte[] bytes, ViewType vt) 
 	{
 		Log.e("DataPool.onTextBytesUpdate", "view type " + vt);
 		DataPoolCacheUtils dataPoolCUtils = new DataPoolCacheUtils();
 		dataPoolCUtils.saveToStorage(bytes, vt, mContext);
 	}
 	
 	/** This method is used to store the image on the cache before it is converted to Bitmap.
 	 * The BitmapTask invokes the callback with the InputString as argument before the 
 	 * one with the decoded bitmap.
 	 * Directly saving the bitmap from the input stream saves space on disk and avoids the 
 	 * Bitmap.compress() call upon saving.
 	 * If there was a download error and the bitmap is not valid, then this method is not
 	 * called by BitmapTask.onPostExecute
 	 */
 	@Override
 	public void onBitmapBytesUpdate(byte[] bytes, BitmapType bt) 
 	{
 		DataPoolCacheUtils dataPoolCUtils = new DataPoolCacheUtils();
 		dataPoolCUtils.saveToStorage(bytes, bt, mContext);
 	}
 	
 	/** Implements onBitmapUpdate
 	 * This method is invoked when a BitmapTask has completed.
 	 * This method is called after a network download has been completed successfully.
 	 * 
 	 * @param bmp the new downloaded bitmap
 	 * @param t the BitmapType associated to the bitmap.
 	 * 
 	 * If the new bitmap is not already contained in the internal data hash storing the
 	 * previous bitmap associated to the BitmapType t, the new bitmap is inserted into the 
 	 * data hash.
 	 * Already registered listeners are notified of the bitmap update by calling the
 	 * onBitmapChanged method with a false parameter indicating a network update.
 	 * <br/>
 	 * If the new bitmap is the same as the already stored bitmap for that BitmapType,
 	 * then nothing is done and the previous bitmap is maintained in memory. In this case,
 	 * the new bitmap is recycled.
 	 * <br/>
 	 * If the new bitmap differs from the old bitmap associated to the BitmapType t,
 	 * or the BitmapType has been inserted among the set of bitmap types whose update
 	 * notification is always required (even in the case the new bitmap is equal to the
 	 * previous one), the new bitmap substitutes the old one in the hash, with key 
 	 * t. In this case, the old bitmap is recycled.
 	 * 
 	 */
 	@Override
 	public void onBitmapUpdate(Bitmap bmp, BitmapType t) 
 	{
 		BitmapData cachedBitmapData = mBitmapDataHash.get(t);
 		BitmapData bitmapData = new BitmapData(bmp);
 		/* force update even if the bitmap is the same */
 		boolean forceUpdate = (mForceUpdateOnSameBitmapHash.containsKey(t) &&
 				mForceUpdateOnSameBitmapHash.get(t) == true);
 		
 		if(forceUpdate || cachedBitmapData == null || !bitmapData.equals(cachedBitmapData))
 		{
 			/* new bitmap replaces cached bitmap: after notifying the listeners,
 			 * which will start to use the new bitmap, we can recycle the old one.
 			 */
 			mBitmapDataHash.put(t, new BitmapData(bmp)); /* put in hash */
 			if(mBitmapListeners.containsKey(t))
 				mBitmapListeners.get(t).onBitmapChanged(bmp, t, false);
 			
 			/* recycle the old bitmap */
 			if(cachedBitmapData != null && cachedBitmapData.bitmap != null)
 				cachedBitmapData.bitmap.recycle();
 		}
 		else /* nothing updated */
 		{
 			bmp.recycle(); /* immediately recycle new unused bitmap */
 			bitmapData = null;
 		}
 	}
 
 	@Override
 	public void onBitmapUpdateError(BitmapType t, String error) 
 	{
 		/* do not put null elements in cache */
 		if(mBitmapListeners.containsKey(t))
 			mBitmapListeners.get(t).onBitmapError(error, t);
 		
 		for(DataPoolErrorListener l : mDataPoolErrorListeners)
 			l.onBitmapUpdateError(t, error);
 	}
 	
 	@Override
 	/**
 	 * This method is invoked when a TextTask has completed.
 	 * This method is called after a network download has been completed successfully.
 	 * 
 	 * @param text the new downloaded text
 	 * @param t the ViewType associated to the text.
 	 * 
 	 * If the new text is not already contained in the internal data hash storing the
 	 * previous string associated to the ViewType t, the new text is inserted into the 
 	 * data hash.
 	 * Already registered listeners are notified of the text update by calling the
 	 * onTextChanged method with a false parameter indicating a network update.
 	 * 
 	 */
 	public void onTextUpdate(String text, ViewType t) 
 	{
 		Log.e("DataPool.onTextUpdate", "view type " + t);
 		StringData sd = mStringData.get(t);
 		StringData newSd = new StringData(text);
 		boolean textChanged = !newSd.equals(sd);
 		if(textChanged)
 			mStringData.put(t, newSd); /* put in hash */
 		if(mTextListeners.containsKey(t))
 			mTextListeners.get(t).onTextChanged(text, t, false);
		
 	}
 
 	@Override
 	public void onTextUpdateError(ViewType t, String error) 
 	{
 		Log.e("DataPool.onTextUpdateError", "view type " + t + " error " + error);
 		/* do not put null text in mStringData map */
 		if(mTextListeners.containsKey(t))
 			mTextListeners.get(t).onTextError(error, t);
 		
 		for(DataPoolErrorListener l : mDataPoolErrorListeners)
 				l.onTextUpdateError(t, error);
 	}
 
 }
