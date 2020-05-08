 package com.pk.addits;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.manuelpeinado.fadingactionbar.FadingActionBarHelperHome;
 import com.squareup.picasso.Picasso;
 
 public class FragmentHome extends Fragment
 {
 	static View view;
 	static ScrollGridView grid;
 	static LinearLayout loading;
 	static RelativeLayout frame;
 	static View shadow;
 	static FeedAdapter adapter;
 	static FadingActionBarHelperHome mFadingHelper;
 	
 	static List<Feed> feedList;
 	static Feed[] NewsFeed;
 	
 	static SlideItem[] Slides;
 	static int currentSlide;
 	static Timer timer;
 	Handler timeHandler;
 	long startTime;
 	
 	static Fragment fragSlide;
 	static FragmentManager fm;
 	static FragmentTransaction transaction;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		view = mFadingHelper.createView(inflater);
 		
 		feedList = new ArrayList<Feed>();
 		grid = (ScrollGridView) view.findViewById(R.id.GridView);
 		loading = (LinearLayout) view.findViewById(R.id.loadingNews);
 		frame = (RelativeLayout) view.findViewById(R.id.slider);
 		shadow = view.findViewById(R.id.sliderShadow);
 		frame.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Data.getHeightByPercent(getActivity(), 0.4)));
 		shadow.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Data.getHeightByPercent(getActivity(), 0.125)));
 		currentSlide = 1;
 		adapter = new FeedAdapter(getActivity(), feedList);
 		grid.setAdapter(adapter);
 		grid.setExpanded(true);
 		
 		return view;
 	}
 	
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		fm = getChildFragmentManager();
 		NewsFeed = ActivityMain.getFeed();
 		
 		timer = new Timer();
 		timeHandler = new Handler(new Callback()
 		{
 			@Override
 			public boolean handleMessage(Message msg)
 			{
 				if (currentSlide == 5)
 					currentSlide = 1;
 				else
 					currentSlide++;
 				
 				populateSlide();
 				
 				return false;
 			}
 		});
 		
 		// Dummy Data
 		Slides = Data.generateSlides(NewsFeed);
 		populateSlide();
 		timer.schedule(new firstTask(), 5000, 7000);
 		
 		updateState();
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
 		
 		mFadingHelper = new FadingActionBarHelperHome().actionBarBackground(R.drawable.ab_background).withContext(getActivity()).contentLayout(R.layout.fragment_home);
 		mFadingHelper.initActionBar(activity);
 	}
 	
 	public static void updateState()
 	{
 		NewsFeed = ActivityMain.getFeed();
 		
 		if(NewsFeed == null)
 		{
 			grid.setVisibility(View.GONE);
 			loading.setVisibility(View.VISIBLE);
 			feedList.clear();
 		}
 		else
 		{
 			grid.setVisibility(View.VISIBLE);
 			loading.setVisibility(View.GONE);
 			
 			for (int x = 0; x < NewsFeed.length; x++)
 				feedList.add(new Feed(NewsFeed[x].getTitle(), NewsFeed[x].getDescription(), NewsFeed[x].getContent(), NewsFeed[x].getCommentFeed(), NewsFeed[x].getAuthor(), NewsFeed[x].getDate(), NewsFeed[x].getCategory(), NewsFeed[x].getImage(), NewsFeed[x].getURL(), NewsFeed[x].getComments(), NewsFeed[x].isRead()));
 			
 			adapter.notifyDataSetChanged();
 			
 			Slides = Data.generateSlides(NewsFeed);
 			populateSlide();
 		}
 	}
 	
 	@SuppressLint("Recycle")
 	public static void populateSlide()
 	{
 		fragSlide = FragmentHomeSlider.newInstance(Slides, currentSlide);
 		
 		transaction = fm.beginTransaction();
 		transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_out, R.anim.fade_in);
 		transaction.replace(R.id.slideContent, fragSlide);
 		transaction.commit();
 	}
 	
 	public static void previousSlide()
 	{
 		timer.cancel();
 		timer.purge();
 		
 		if (currentSlide == 1)
 			currentSlide = 5;
 		else
 			currentSlide--;
 		
 		populateSlide();
 	}
 	
 	public static void nextSlide()
 	{
 		timer.cancel();
 		timer.purge();
 		
 		if (currentSlide == 5)
 			currentSlide = 1;
 		else
 			currentSlide++;
 		
 		populateSlide();
 	}
 	
 	// Tells handler to send a message
 	class firstTask extends TimerTask
 	{
 		@Override
 		public void run()
 		{
 			timeHandler.sendEmptyMessage(0);
 		}
 	};
 	
 	public static class FragmentHomeSlider extends Fragment
 	{
 		int Slide;
 		String Text;
 		String SubText;
 		String ImageURL;
 		String URL;
 		
 		ImageView imgImage;
 		TextView txtText;
 		TextView txtSubText;
 		LinearLayout Content;
 		RelativeLayout btnPrevious;
 		RelativeLayout btnNext;
 		
 		View s1;
 		View s2;
 		View s3;
 		View s4;
 		View s5;
 		
 		public static final FragmentHomeSlider newInstance(SlideItem[] Slides, int slide)
 		{
 			FragmentHomeSlider f = new FragmentHomeSlider();
 			Bundle bdl = new Bundle(5);
 			
 			bdl.putInt("Slide", slide);
 			bdl.putString("Text", Slides[slide - 1].getText());
 			bdl.putString("SubText", Slides[slide - 1].getSubText());
 			bdl.putString("ImageURL", Slides[slide - 1].getImageURL());
 			bdl.putString("URL", Slides[slide - 1].getURL());
 			
 			f.setArguments(bdl);
 			return f;
 		}
 		
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 		{
 			View view = inflater.inflate(R.layout.fragment_home_slider, container, false);
 			
 			imgImage = (ImageView) view.findViewById(R.id.Image);
 			txtText = (TextView) view.findViewById(R.id.txtText);
 			txtSubText = (TextView) view.findViewById(R.id.txtSubText);
 			Content = (LinearLayout) view.findViewById(R.id.Content);
 			btnPrevious = (RelativeLayout) view.findViewById(R.id.btnPrevious);
 			btnNext = (RelativeLayout) view.findViewById(R.id.btnNext);
 			s1 = view.findViewById(R.id.slide1);
 			s2 = view.findViewById(R.id.slide2);
 			s3 = view.findViewById(R.id.slide3);
 			s4 = view.findViewById(R.id.slide4);
 			s5 = view.findViewById(R.id.slide5);
 			
 			return view;
 		}
 		
 		@Override
 		public void onStart()
 		{
 			super.onStart();
 			
 			retrieveData();
 			
 			txtText.setText(Text);
 			txtSubText.setText(SubText);
 			btnPrevious.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					FragmentHome.previousSlide();
 				}
 			});
 			btnNext.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					FragmentHome.nextSlide();
 				}
 			});
 			Content.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					Intent i = new Intent(Intent.ACTION_VIEW);
 					i.setData(Uri.parse(URL));
 					getActivity().startActivity(i);
 				}
 			});
 			
 			setIndicator();
 			setImage();
 		}
 		
 		public void retrieveData()
 		{
 			Bundle args = getArguments();
 			Slide = args.getInt("Slide");
 			Text = args.getString("Text");
 			SubText = args.getString("SubText");
 			ImageURL = args.getString("ImageURL");
 			URL = args.getString("URL");
 		}
 		
 		public void setIndicator()
 		{
 			switch (Slide)
 			{
 				case 1:
 					s1.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light));
 					s2.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s3.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s4.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s5.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					break;
 				case 2:
 					s1.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s2.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light));
 					s3.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s4.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s5.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					break;
 				case 3:
 					s1.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s2.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s3.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light));
 					s4.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s5.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					break;
 				case 4:
 					s1.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s2.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s3.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s4.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light));
 					s5.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					break;
 				case 5:
 					s1.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s2.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s3.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s4.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light_transparent));
 					s5.setBackgroundColor(getActivity().getResources().getColor(R.color.holo_blue_light));
 					break;
 				default:
 					break;
 			}
 		}
 		
 		public void setImage()
 		{
 			imgImage.setScaleType(ScaleType.FIT_XY);
 			if(ImageURL.length() > 0)
 				Picasso.with(getActivity()).load(ImageURL).fit().into(imgImage);
 			else
 				Picasso.with(getActivity()).load(R.drawable.no_image_banner).fit().into(imgImage);
 		}
 	}
 	
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
 		
 		public Object getItem(int position)
 		{
 			return listItem.get(position);
 		}
 		
 		public long getItemId(int position)
 		{
 			return position;
 		}
 		
 		public View getView(int position, View view, ViewGroup viewGroup)
 		{
 			ViewHolder holder;
 			Feed entry = listItem.get(position);
 			if (view == null)
 			{
 				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = inflater.inflate(R.layout.feed_item, null);
 				
 				holder = new ViewHolder();
 				holder.txtTitle = (TextView) view.findViewById(R.id.txtTitle);
 				holder.txtDescription = (TextView) view.findViewById(R.id.txtDescription);
 				holder.txtAuthor = (TextView) view.findViewById(R.id.txtAuthor);
 				holder.txtDate = (TextView) view.findViewById(R.id.txtDate);
 				holder.txtCategory = (TextView) view.findViewById(R.id.txtCategory);
 				holder.imgPreview = (ImageView) view.findViewById(R.id.imgPreview);
 				
 				view.setTag(holder);
 			}
 			else
 			{
 				holder = (ViewHolder) view.getTag();
 			}
 			
 			holder.txtTitle.setText(entry.getTitle());
 			holder.txtDescription.setText(entry.getDescription());
 			holder.txtAuthor.setText("Posted by " + entry.getAuthor());
 			holder.txtDate.setText(entry.getDate());
 			holder.txtCategory.setText(entry.getCategory());
 			
 			holder.imgPreview.setScaleType(ScaleType.CENTER_INSIDE);
 			if(entry.getImage().length() > 0)
 				Picasso.with(context).load(entry.getImage()).fit().into(holder.imgPreview);
 			else
 				Picasso.with(context).load(R.drawable.no_image_banner).fit().into(holder.imgPreview);
 			
 			return view;
 		}
 	}
 	
 	private static class ViewHolder
 	{
 		public TextView txtTitle;
 		public TextView txtDescription;
 		public TextView txtAuthor;
 		public TextView txtDate;
 		public TextView txtCategory;
 		public ImageView imgPreview;
 	}
 	
 	public static class SlideItem
 	{
 		String Text;
 		String SubText;
 		String ImageURL;
 		String URL;
 		
 		public SlideItem(String Text, String SubText, String ImageURL, String URL)
 		{
 			this.Text = Text;
 			this.SubText = SubText;
 			this.ImageURL = ImageURL;
 			this.URL = URL;
 		}
 		
 		public String getText()
 		{
 			return Text;
 		}
 		
 		public String getSubText()
 		{
 			return SubText;
 		}
 		
 		public String getImageURL()
 		{
 			return ImageURL;
 		}
 		
 		public String getURL()
 		{
 			return URL;
 		}
 	}
 }
