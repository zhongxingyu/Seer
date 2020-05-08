 package com.pk.addits;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.pk.addits.fadingactionbar.FadingActionBarHelperHome2;
 import com.squareup.picasso.Picasso;
 
 public class FragmentHome extends Fragment
 {
 	static View view;
 	static ListView list;
 	static FrameLayout frame;
 	static FeedAdapter adapter;
 	static FadingActionBarHelperHome2 mFadingHelper;
 	
 	static List<Feed> feedList;
 	static Feed[] NewsFeed;
 	
 	static int currentSlide;
 	static Timer timer;
 	Handler timeHandler;
 	long startTime;
 	
 	static Fragment fragSlide;
 	static FragmentManager fm;
 	static Integer currentSlideID;
 	
 	Typeface fontRegular;
 	Typeface fontBold;
 	Typeface fontLight;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		view = mFadingHelper.createView(inflater);
 		
 		feedList = new ArrayList<Feed>();
 		list = (ListView) view.findViewById(android.R.id.list);
 		
 		adapter = new FeedAdapter(getActivity(), feedList);
 		list.setAdapter(adapter);
 		currentSlide = 1;
 		
 		return view;
 	}
 	
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		fm = getChildFragmentManager();
 		NewsFeed = ActivityMain.getFeed();
 		fontRegular = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Regular.ttf");
 		fontBold = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Bold.ttf");
 		fontLight = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Light.ttf");
 		
 		timer = new Timer();
 		timeHandler = new Handler(new Callback()
 		{
 			@Override
 			public boolean handleMessage(Message msg)
 			{
 				if (mFadingHelper.mLastScrollPosition < 10 && NewsFeed != null)
 					populateSlide();
 				
 				return false;
 			}
 		});
 		timer.schedule(new firstTask(), 5000, 7000);
 		
 		populateSlide();
 		updateState();
 		
 		list.setOnItemClickListener(new OnItemClickListener()
 		{
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view, int position, long index)
 			{
 				if(position > 0)
 				{
 					int ID = feedList.get(position - 1).getID();
 					String Title = feedList.get(ID).getTitle();
 					String Description = feedList.get(ID).getDescription();
 					String Content = feedList.get(ID).getContent();
 					String CommentFeed = feedList.get(ID).getCommentFeed();
 					String Author = feedList.get(ID).getAuthor();
 					String Date = feedList.get(ID).getDate();
 					String Category = feedList.get(ID).getCategory();
 					String Image = feedList.get(ID).getImage();
 					String URL = feedList.get(ID).getURL();
 					boolean Favorite = feedList.get(ID).isFavorite();
 					boolean Read = feedList.get(ID).isRead();
 					
 					Feed Article = new Feed(ID, Title, Description, Content, CommentFeed, Author, Date, Category, Image, URL, Favorite, Read);
 					ActivityMain.callArticle(getActivity(), Article);
 				}
 				else if (currentSlideID != null)
 				{
 					int ID = NewsFeed[currentSlideID].getID();
 					String Title = NewsFeed[currentSlideID].getTitle();
 					String Description = NewsFeed[currentSlideID].getDescription();
 					String Content = NewsFeed[currentSlideID].getContent();
 					String CommentFeed = NewsFeed[currentSlideID].getCommentFeed();
 					String Author = NewsFeed[currentSlideID].getAuthor();
 					String Date = NewsFeed[currentSlideID].getDate();
 					String Category = NewsFeed[currentSlideID].getCategory();
 					String Image = NewsFeed[currentSlideID].getImage();
 					String URL = NewsFeed[currentSlideID].getURL();
 					boolean Favorite = NewsFeed[currentSlideID].isFavorite();
 					boolean Read = NewsFeed[currentSlideID].isRead();
 					
 					Feed Article = new Feed(ID, Title, Description, Content, CommentFeed, Author, Date, Category, Image, URL, Favorite, Read);
 					ActivityMain.callArticle(getActivity(), Article);
 				}
 			}
 		});
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		
 		timer.cancel();
 		timer.purge();
 	}
 	
 	@Override
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 		
 		frame = new FrameLayout(activity);
 		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Data.getHeightByPercent(activity, 0.35));
 		frame.setLayoutParams(layoutParams);
 		frame.setId(R.id.slider_content);
 		frame.setClickable(true);
 		mFadingHelper = new FadingActionBarHelperHome2().actionBarBackground(R.drawable.ab_background).headerView(frame).contentLayout(R.layout.fragment_home);
 		mFadingHelper.initActionBar(activity);
 	}
 	
 	public static void updateState()
 	{
 		NewsFeed = ActivityMain.getFeed();
 		
 		if (NewsFeed == null)
 		{
 			feedList.clear();
 		}
 		else
 		{
 			Log.v("Guess what!", "VAGINA!!!!");
 			//feedList = new ArrayList<Feed>();
 			feedList.clear();
 			
 			for (int x = 0; x < NewsFeed.length; x++)
 				feedList.add(new Feed(NewsFeed[x].getID(), NewsFeed[x].getTitle(), NewsFeed[x].getDescription(), NewsFeed[x].getContent(), NewsFeed[x].getCommentFeed(), NewsFeed[x].getAuthor(), NewsFeed[x].getDate(), NewsFeed[x].getCategory(), NewsFeed[x].getImage(), NewsFeed[x].getURL(), NewsFeed[x].isFavorite(), NewsFeed[x].isRead()));
 			
 			adapter.notifyDataSetChanged();
 			populateSlide();
 		}
 	}
 	
 	public static void populateSlide()
 	{
 		String sTitle = "";
 		String sAuthor = "";
 		String sDate = "";
 		String sImage = "";
 		String sCategory = "";
 		
 		if (NewsFeed != null)
 		{
 			Random generator = new Random();
 			int r = generator.nextInt(NewsFeed.length);
 			sTitle = NewsFeed[r].getTitle();
 			sAuthor = NewsFeed[r].getAuthor();
			sDate = NewsFeed[r].getDate();
 			sImage = NewsFeed[r].getImage();
 			sCategory = NewsFeed[r].getCategory();
 			currentSlideID = NewsFeed[r].getID();
 		}
 		
 		if (!sCategory.equalsIgnoreCase("DAILY SAVER"))
 			fragSlide = Slider.newInstance(sTitle, sAuthor, sDate, sImage);
 		else
 		{
 			populateSlide();
 			return;
 		}
 		
 		FragmentTransaction trans = fm.beginTransaction();
 		trans.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_out, R.anim.fade_in);
 		trans.replace(R.id.slider_content, fragSlide);
 		trans.commit();
 	}
 	
 	class firstTask extends TimerTask
 	{
 		@Override
 		public void run()
 		{
 			timeHandler.sendEmptyMessage(0);
 		}
 	};
 	
 	public class FeedAdapter extends BaseAdapter
 	{
 		private Context context;
 		
 		private List<Feed> listItem;
 		
 		public FeedAdapter(Context context, List<Feed> listItem)
 		{
 			this.context = context;
 			this.listItem = listItem;
 		}
 		
 		public int getCount()
 		{
 			return listItem.size();
 		}
 		
 		public Feed getItem(int position)
 		{
 			return listItem.get(position);
 		}
 		
 		public long getItemId(int position)
 		{
 			return position;
 		}
 		
 		public View getView(int position, View view, ViewGroup viewGroup)
 		{
 			final ViewHolder holder;
 			Feed entry = listItem.get(position);
 			if (view == null)
 			{
 				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = inflater.inflate(R.layout.feed_item, null);
 				
 				holder = new ViewHolder();
 				holder.Card = (LinearLayout) view.findViewById(R.id.Card);
 				holder.lblUnread = view.findViewById(R.id.lblUnread);
 				holder.lblAuthor = (RelativeLayout) view.findViewById(R.id.lblAuthor);
 				holder.txtTitle = (TextView) view.findViewById(R.id.txtTitle);
 				holder.txtDescription = (TextView) view.findViewById(R.id.txtDescription);
 				holder.txtAuthor = (TextView) view.findViewById(R.id.txtAuthor);
 				holder.txtDate = (TextView) view.findViewById(R.id.txtDate);
 				holder.txtCategory = (TextView) view.findViewById(R.id.txtCategory);
 				holder.imgPreview = (ImageView) view.findViewById(R.id.imgPreview);
 				
 				holder.txtTitle.setTypeface(fontBold);
 				holder.txtDescription.setTypeface(fontRegular);
 				holder.txtAuthor.setTypeface(fontLight);
 				holder.txtDate.setTypeface(fontLight);
 				holder.txtCategory.setTypeface(fontRegular);
 				
 				view.setTag(holder);
 			}
 			else
 			{
 				holder = (ViewHolder) view.getTag();
 			}
 			
 			holder.txtTitle.setText(entry.getTitle());
 			holder.txtDescription.setText(entry.getDescription());
 			holder.txtAuthor.setText("Posted by " + entry.getAuthor());
 			holder.txtDate.setText(Data.parseDate(context, entry.getDate()));
 			holder.txtCategory.setText(entry.getCategory());
 			
 			if (entry.getImage().length() > 0)
 				Picasso.with(context).load(entry.getImage()).error(R.drawable.loading_image_error).fit().skipCache().into(holder.imgPreview);
 			else
 				holder.imgPreview.setVisibility(View.GONE);
 			
 			holder.imgPreview.setAdjustViewBounds(false);
 			
 			if (entry.isRead())
 			{
 				holder.lblUnread.setVisibility(View.INVISIBLE);
 				if (entry.getImage().length() > 0)
 				{
 					int height = holder.imgPreview.getHeight() + holder.lblAuthor.getHeight();
 					holder.lblAuthor.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height));
 				}
 			}
 			else
 			{
 				holder.lblUnread.setVisibility(View.VISIBLE);
 				if (entry.getImage().length() > 0)
 				{
 					RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 					holder.lblAuthor.setLayoutParams(layoutParams);
 				}
 			}
 			
 			Animation cardAnimation = AnimationUtils.loadAnimation(context, R.anim.card_anim_list);
 			holder.Card.startAnimation(cardAnimation);
 			
 			return view;
 		}
 	}
 	
 	private static class ViewHolder
 	{
 		public LinearLayout Card;
 		public View lblUnread;
 		public RelativeLayout lblAuthor;
 		public TextView txtTitle;
 		public TextView txtDescription;
 		public TextView txtAuthor;
 		public TextView txtDate;
 		public TextView txtCategory;
 		public ImageView imgPreview;
 		
 	}
 }
