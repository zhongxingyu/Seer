 package com.jamtwo.randommeme;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 
 public class MemeStack {
 	
 	private static ILoadMoreMemesListener loadMoreMemesListener;
 	private static ArrayList<Meme> mMemes;
 	private static MemeStack mStack =null;
 	private static int mCurrentIndex;
 	
 	// Load more random memes when we get to X memes before the last one
 	private static int RELOAD_INDEX = 3;
 	
 	private static final String CLASS = "MemeStack";
 	
 	protected MemeStack(){
 		mMemes = new ArrayList<Meme>();
 	}
 	
 	public static MemeStack getInstance(){
 		if (mStack==null){
 			mStack = new MemeStack();
 		}
 		return mStack;
 	}
 	
 	public static Meme getNextMeme()
 	{
 		String TAG = CLASS + ".getNextMeme()";
 		Meme returnMeme;
 		
 		//int memeSize = mMemes.size();
 		if(mMemes.size() == 0)
 		{
 			return null; // does not need to do anything
 		}
 		mCurrentIndex++;
 		returnMeme = mMemes.get(mCurrentIndex);
 		return returnMeme;
 	}
 	
 	public static boolean nextMemeIsReady()
 	{
 		String TAG = CLASS + ".nextMemeIsReady()";
 		//Log.v(TAG, "checking if the next meme is ready [mCurrentIndex = " +mCurrentIndex+"]");
 		//Log.v(TAG, "hasNextMeme is ready? " + MemeStack.hasNextMeme());
 		//Log.v(TAG, "memeAtIndex("+mCurrentIndex+").isReadyToDisplay()? " + getMemeAtIndex(mCurrentIndex).readyToDisplay());
 		if(MemeStack.hasNextMeme())
 		{
			if(getMemeAtIndex(mCurrentIndex+1).readyToDisplay())
 			{
 				//Log.v(TAG, "MemeStack has the next meme and it is ready");
 				return true;
 			}
 		}
 		//else
 		//{
 		//Log.v(TAG, "MemeStack does not have a next meme, or it is not ready.");
 		return false;
 		//}
 	}
 	
 	public static boolean prevMemeIsReady()
 	{
 		if(MemeStack.hasPrevMeme() && getMemeAtIndex(mCurrentIndex - 1).readyToDisplay())
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	public static Meme getMemeAtIndex(int index)
 	{
 		String TAG = CLASS + "getMemeAtIndex()";
 		//Log.v(TAG, "callde iwth index: " + index);
 		return mMemes.get(index);
 	}
 	
 	public static boolean hasNextMeme()
 	{
 		String TAG = CLASS + ".hasNextMeme";
 		//Log.v(TAG, "called");
 
		return(mMemes.size() > 0 && (mCurrentIndex+1 < mMemes.size()));
 	}
 	
 	public static boolean atFirstMeme()
 	{
 		if(mCurrentIndex == 0)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	public static boolean atLastMeme()
 	{
 		String TAG = CLASS + ".atLastMeme()";
 		//Log.d(TAG, "is " + (mCurrentIndex == mMemes.size()));
 		return(mCurrentIndex == mMemes.size());
 	}
 	
 	public static boolean nMemesFromLast(int n)
 	{
 		Log.v(".nMemesFromLast()", "RESULT: "+ (mMemes.size()-mCurrentIndex <= n));
 		return( (mMemes.size()-mCurrentIndex) <= n);
 	}
 	
 	public static boolean hasPrevMeme()
 	{
 		String TAG = CLASS + ".hasPrevMeme";
 		//Log.v(TAG, "called");
 
 		return(mMemes.size() > 0 && (mCurrentIndex-1 >= 0));
 	}
 	
 	public static void downloadMoreMemes(int count)
 	{
 		String TAG = CLASS + ".downloadAnotherMeme";
 		Log.v(TAG, "called");
 		
 		loadMoreMemesListener.downloadMoreMemes(count);
 	}
 	
 	public static void loadMoreMemes()
 	{
 		String TAG = CLASS + ".loadMoreMemes";
 		Log.v(TAG, "SHOULD NOT BE called - use one of the meme download engines");
 		
 		//loadMoreMemesListener.loadMoreMemes();
 	}
 	
 	public static Meme getPrevMeme(){
 		String TAG = CLASS + ".getPrevMeme";
 		Meme returnMeme;
 		if (mMemes.size() > 0 && mCurrentIndex > 0)
 		{
 			mCurrentIndex--;
 			
 		} else
 		{
 			mCurrentIndex=mMemes.size()-1;			
 		}
 		returnMeme = mMemes.get(mCurrentIndex);
 		
 		return returnMeme;
 	}
 	
 	public static void addMeme(Meme meme)
 	{
 		String TAG = CLASS + ".addMeme()";
 		//Log.w(TAG, "adding Meme["+mMemes.size()+"]: " + meme.getUrl());
 		meme.setStackPosition(mMemes.size());
 		mMemes.add(meme);
 	}
 	
 	public static void removeMeme(Meme meme)
 	{
 		mMemes.remove(meme);
 		mCurrentIndex--;
 	}
 	
 	public static Meme getCurrentMeme()
 	{
 		String TAG = CLASS + ".getCurrentMeme";
 		Log.w(TAG, "called with mCurrentIndex: " + mCurrentIndex);
 		Log.w(TAG, "called with mMemes.size(): " + mMemes.size());
 		if (mMemes.size()>(mCurrentIndex))
 		{
 			//Log.v(TAG, "returning mCurrentIndex: " + mCurrentIndex);
 			return mMemes.get(mCurrentIndex);
 		}
 		//Log.v(TAG, "no meme to return");
 		return null;
 	}
 	
 	public void registerLoadMoreMemesListener(ILoadMoreMemesListener listener)
 	{
 		loadMoreMemesListener = listener;
 	}
 	
 	public static void updateWebView()
 	{
 		String TAG = CLASS + ".updateWebView()";
 		Log.w(TAG, "DO NOT CALL updating web view - use the meme parser engines");
 		//loadMoreMemesListener.updateWebView();
 	}
 
 }
