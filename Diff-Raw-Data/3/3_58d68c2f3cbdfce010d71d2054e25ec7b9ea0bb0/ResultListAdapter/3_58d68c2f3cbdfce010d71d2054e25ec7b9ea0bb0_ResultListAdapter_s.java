 /*
  * Copyright Aug 12, 2010 Tyler Levine
  * 
  * This file is part of Hunch-for-Android.
  *
  * Hunch-for-Android is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Hunch-for-Android is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Hunch-for-Android.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.hunch.ui;
 
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.hunch.Const;
 import com.hunch.ImageManager;
 import com.hunch.R;
 import com.hunch.api.HunchAPI;
 import com.hunch.api.HunchResult;
 import com.hunch.api.HunchRankedResults.ResultStub;
 
 /**
  * 
  * 
  * @author Tyler Levine
  * @since Aug 12, 2010
  *
  */
 public class ResultListAdapter extends BaseAdapter
 {
 	public static class ResultViewHolder
 	{
 		View wholeView;
 		ProgressBar placeholder;
 		ImageView image;
 		TextView text;
 		TextView number;
 		TextView pct;
 		String resultId;
 		Integer position;
 	}
 	
 	protected static class ResultModel
 	{
 		private boolean mIsStub;
 		
 		private String mName;
 		private String mId;
 		private String mImgUrl;
 		private String mEitherOrPct;
 		
 		public ResultModel( String name, String id, String imgUrl, String eitherOrPct )
 		{
 			this( name, id, imgUrl, eitherOrPct, false );
 		}
 		
 		public static ResultModel createStub( String id, String eitherOrPct )
 		{
 			return new ResultModel( null, id, null, eitherOrPct, true );
 		}
 		
 		public ResultModel( String name, String id, String imgUrl, String eitherOrPct, boolean isStub )
 		{
 			mName = name;
 			mId = id;
 			mImgUrl = imgUrl;
 			mEitherOrPct = eitherOrPct;
 			
 			mIsStub = isStub;
 		}
 		
 		public void unStub( String name, String imgUrl )
 		{
 			if( !mIsStub ) return;
 			
 			mName = name;
 			mImgUrl = imgUrl;
 			
 			mIsStub = false;
 		}
 		
 		public boolean isStub()
 		{
 			return mIsStub;
 		}
 		
 		public String getName()
 		{
 			return mName;
 		}
 		
 		public String getId()
 		{
 			return mId;
 		}
 		
 		public String getImageUrl()
 		{
 			return mImgUrl;
 		}
 		
 		public String getEitherOrPct()
 		{
 			return mEitherOrPct;
 		}
 		
 		public boolean hasEitherOrPct()
 		{
 			return getEitherOrPct() != null;
 		}
 		
 		@Override
 		public String toString()
 		{
 			return "ResultModel[" + mName + "]";
 		}
 		
 		public String inspect()
 		{
 			return String.format( "ResultModel[ name=%s, id=%s, stub=%s ]", mName, mId, mIsStub );
 		}
 		
 		@Override
 		public int hashCode()
 		{
 			int code = 37;
 			
 			if( hasEitherOrPct() )
 			{
 				code = code * 3 + mEitherOrPct.hashCode();
 			}
 			
 			code = code * 3 + mId.hashCode();
 			
 			// stub's wont have the rest of the data
 			// and hence will throw NPE's
 			if( isStub() ) return code;
 			
 			code = code * 3 + mImgUrl.hashCode();
 			code = code * 3 + mName.hashCode();
 			
 			return code;
 		}
 	}
 	
 	protected final Context context;
	//private final ProgressDialog progress;
	//private boolean showProgress = true;
	//protected final Map< ResultModel, HunchResult > resultsCache;
 	protected final List< ResultModel > items;
 
 	public ResultListAdapter( Context context, List< ResultModel > list )
 	{
 		this.context = context;
 		items = list;
 	}
 
 	@Override
 	public int getCount()
 	{
 		return items.size();
 	}
 	
 	@Override
 	public long getItemId( int pos )
 	{
 		return pos;
 	}
 	
 	@Override
 	public ResultModel getItem( int pos )
 	{
 		return items.get( pos );
 	}
 
 	protected boolean shouldLoadInline( int curPos, int size )
 	{
 		return curPos >= size - 3;
 	}
 	
 	protected ResultModel buildModel( HunchResult result, ResultStub stub )
 	{
 		return new ResultModel( result.getName(), String.valueOf( result.getId() ),
 				result.getImageUrl(), stub.getEitherOrPct() );
 	}
 	
 	protected void resetResultView( ResultViewHolder view )
 	{
 		
 		if( view.placeholder.getVisibility() != View.GONE &&
 			view.image.getVisibility() == View.GONE )
 		{
 			// the view has not been set yet, no work to do
 			return;
 		}
 		
 		view.image.setVisibility( View.GONE );
 		view.placeholder.setVisibility( View.VISIBLE );
 		
 		// reset text views (important!)
 		view.number.setText( "" );
 		view.text.setText( "Loading..." );
 		view.pct.setText( "" );
 		view.pct.setVisibility( View.GONE );
 	}
 
 	protected void setupResultView( final ResultViewHolder resultView, int position,
 			ResultModel resultData )
 	{
 		
 		/*if( resultView.isSet )
 		{
 			return;
 		}*/
 		
 		// first download the result image.
 		// this is gonna take a while because it needs
 		// to hit the network in most cases.
 		
 		if( resultView.placeholder.getVisibility() != View.GONE )
 		{
 			ImageManager.getInstance()
 			.getTopicImageWithCallback( context, resultData.getImageUrl(),
 					new ImageManager.Callback()
 			{
 
 				@Override
 				public void callComplete( Drawable d )
 				{
 
 					// remove the progressbar
 					resultView.placeholder.setVisibility( View.GONE );
 
 					// set the drawable
 					resultView.image.setImageDrawable( d );
 				
 					// show the image view
 					resultView.image.setVisibility( View.VISIBLE );
 				}
 			} );
 		}
 		else
 		{
 			ImageManager.getInstance()
 			.getTopicImage( context, resultView.image, resultData.getImageUrl() );
 		}
 
 		// now set the rest of the fields - index, name and the percentage
 		resultView.number.setText( String.valueOf( position + 1 ) + "." );
 
 		resultView.text.setText( resultData.getName() );
 
 		if ( resultData.hasEitherOrPct() )
 		{
 			resultView.pct.setVisibility( View.VISIBLE );
 			resultView.pct.setText( resultData.getEitherOrPct() + "%" );
 		}
 
 	}
 
 	@Override
 	public View getView( final int position, View convertView, ViewGroup parent )
 	{
 		Log.d( Const.TAG, "ResultModelListAdapter.getView() pos: " + position +
 				" convertView: " + convertView + " parent: " + parent );
 		
 		// get the basic result info
 		final ResultModel model = getItem( position );
 		
 		Log.d( Const.TAG, "loaded model: " + model.inspect() );
 		
 		// try it without convert view for now.. there won't
 		// be too many items in the results list typically
 		// convertView = null;
 		
 		// view holder pattern courtesy Romain Guy
 		ResultViewHolder tempHolder;
 		if( convertView == null )
 		{
 			// first find the inflater
 			final LayoutInflater inflater = (LayoutInflater)
 					context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
 			
 			convertView = inflater.inflate( R.layout.result_list_item, parent, false );
 			
 			tempHolder = new ResultViewHolder();
 			tempHolder.placeholder = (ProgressBar) convertView.findViewById( R.id.placeholder );
 			tempHolder.image = (ImageView) convertView.findViewById( R.id.result_icon );
 			tempHolder.text = (TextView) convertView.findViewById( R.id.result_name );
 			tempHolder.number = (TextView) convertView.findViewById( R.id.result_number );
 			tempHolder.pct = (TextView) convertView.findViewById( R.id.result_pct );
 			tempHolder.resultId = model.getId();
 			tempHolder.position = position;
 			tempHolder.wholeView = convertView;
 			
 			convertView.setTag( tempHolder );
 			
 		}
 		else
 		{
 			tempHolder = (ResultViewHolder) convertView.getTag();
 			
 			// update the position -- it may have changed
 			tempHolder.position = position;
 			
 			// also update the result id because
 			// this view may be showing a different result now
 			tempHolder.resultId = model.getId();
 			
 		}
 		
 		// if the result is already downloaded,
 		// just set up the new view and then return
 		if( !model.isStub() )
 		{
 			setupResultView( tempHolder, position, model );
 			return convertView;
 		}
 		
 		final ResultViewHolder holder = tempHolder;
 		
 		//	get the full result off the network
 		HunchAPI.getInstance().getResult( model.getId(), Const.RESULT_IMG_SIZE,
 				new HunchResult.Callback()
 		{
 
 			@Override
 			public void callComplete( HunchResult h )
 			{
 				model.unStub( h.getName(), h.getImageUrl() );
 				setupResultView( holder, position, model );
 			}
 		} );
 		
 		// reset the view until it gets loaded
 		resetResultView( holder );
 		
 		return convertView;
 	}
 	
 	public List< ResultModel > getAdapterData()
 	{
 		return items;
 	}
 }
