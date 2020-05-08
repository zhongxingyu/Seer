 package com.pk.addits;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.ShareActionProvider;
 import android.widget.TextView;
 
 import com.pk.addits.fadingactionbar.FadingActionBarHelper;
 import com.squareup.picasso.Picasso;
 
 public class FragmentArticle extends Fragment
 {
 	ActionBar actionBar;
 	private ShareActionProvider mShareActionProvider;
 	View view;
 	static FadingActionBarHelper mFadingHelper;
 	Feed Article;
 	private Thread loadCommentsThread;
 	private Handler mHandler;
 	private List<CommentFeed> commentList;
 	private CommentFeedAdapter adapter;
 	static MenuItem shareItem;
 	static Menu optionsMenu;
 	
 	ImageView imgHeader;
 	TextView txtTitle;
 	TextView txtAuthor;
 	TextView txtDate;
 	TextView txtContent;
 	
 	FrameLayout commentCard;
 	TextView txtLoadComments;
 	ProgressBar progressBar;
 	ListView comments;
 	
 	Typeface fontRegular;
 	Typeface fontBold;
 	Typeface fontLight;
 	
 	public static FragmentArticle newInstance(Feed article)
 	{
 		FragmentArticle f = new FragmentArticle();
 		Bundle bundle = new Bundle();
 		
 		bundle.putString("Title", article.getTitle());
 		bundle.putString("Description", article.getDescription());
 		bundle.putString("Content", article.getContent());
 		bundle.putString("Comment Feed", article.getCommentFeed());
 		bundle.putString("Author", article.getAuthor());
 		bundle.putString("Date", article.getDate());
 		bundle.putString("Category", article.getCategory());
 		bundle.putString("Image", article.getImage());
 		bundle.putString("URL", article.getURL());
 		bundle.putInt("Comments", article.getComments());
 		bundle.putBoolean("Favorite", article.isFavorite());
 		bundle.putBoolean("Read", article.isRead());
 		
 		f.setArguments(bundle);
 		
 		return f;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		view = mFadingHelper.createView(inflater);
 		setHasOptionsMenu(true);
 		
 		imgHeader = (ImageView) view.findViewById(R.id.image_header);
 		txtTitle = (TextView) view.findViewById(R.id.txtTitle);
 		txtAuthor = (TextView) view.findViewById(R.id.txtAuthor);
 		txtDate = (TextView) view.findViewById(R.id.txtDate);
 		txtContent = (TextView) view.findViewById(R.id.txtContent);
 		
 		commentCard = (FrameLayout) view.findViewById(R.id.commentCard);
 		txtLoadComments = (TextView) view.findViewById(R.id.txtLoadComments);
 		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
 		comments = (ListView) view.findViewById(R.id.ListView);
 
 		fontRegular = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Regular.ttf");
 		fontBold = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Bold.ttf");
 		fontLight = Typeface.createFromAsset(getActivity().getAssets(), "RobotoSlab-Light.ttf");
 		
 		txtTitle.setTypeface(fontBold);
 		txtAuthor.setTypeface(fontLight);
 		txtDate.setTypeface(fontLight);
 		txtContent.setTypeface(fontRegular);
 		
 		return view;
 	}
 	
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		
 		actionBar = getActivity().getActionBar();
 		retrieveArguments();
 		mHandler = new Handler();
 		configureShare();
 		
 		actionBar.setTitle(Article.getTitle());
 		if (Article.getImage().length() > 0)
 			Picasso.with(getActivity()).load(Article.getImage()).error(R.drawable.no_image_banner).fit().into(imgHeader);
 		else
 			Picasso.with(getActivity()).load(R.drawable.no_image_banner).fit().into(imgHeader);
 		
 		txtTitle.setText(Article.getTitle());
 		txtAuthor.setText("Posted by " + Article.getAuthor());
		txtDate.setText(Article.getDate());
 		txtContent.setText(Article.getContent());
 		
 		if (Article.getComments() > 0)
 		{
 			txtLoadComments.setText("Load Comments");
 			commentCard.setOnClickListener(new View.OnClickListener()
 			{
 				@Override
 				public void onClick(View v)
 				{
 					progressBar.setVisibility(View.VISIBLE);
 					txtLoadComments.setText("Loading " + Article.getComments() + " Comments...");
 					commentCard.setClickable(false);
 					
 					if (loadCommentsThread == null)
 					{
 						initializeLoadCommentsThread();
 						loadCommentsThread.start();
 					}
 					else if (!loadCommentsThread.isAlive())
 					{
 						initializeLoadCommentsThread();
 						loadCommentsThread.start();
 					}
 				}
 			});
 		}
 		else
 		{
 			txtLoadComments.setText("No Comments");
 			commentCard.setClickable(false);
 		}
 	}
 	
 	@Override
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 		
 		mFadingHelper = new FadingActionBarHelper().actionBarBackground(R.drawable.ab_background).headerLayout(R.layout.header_light).contentLayout(R.layout.fragment_article).lightActionBar(false);
 		mFadingHelper.initActionBar(activity);
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
 		menu.clear();
 		inflater.inflate(R.menu.article, menu);
 		optionsMenu = menu;
 		
 		shareItem = menu.findItem(R.id.Share_Label);
 		mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
 	}
 	
 	public static void menuVisibility(boolean drawerOpen)
 	{
 		if(shareItem != null && optionsMenu != null)
 			shareItem.setVisible(!drawerOpen);
 	}
 	
 	public void retrieveArguments()
 	{
 		Bundle args = getArguments();
 		
 		int ID = args.getInt("ID");
 		String Title = args.getString("Title");
 		String Description = args.getString("Description");
 		String Content = args.getString("Content");
 		String CommentFeed = args.getString("Comment Feed");
 		String Author = args.getString("Author");
 		String Date = args.getString("Date");
 		String Category = args.getString("Category");
 		String Image = args.getString("Image");
 		String URL = args.getString("URL");
 		int Comments = args.getInt("Comments");
 		boolean Favorite = args.getBoolean("Favorite");
 		boolean Read = args.getBoolean("Read");
 		
 		Article = new Feed(ID, Title, Description, Content, CommentFeed, Author, Date, Category, Image, URL, Comments, Favorite, Read);
 	}
 	
 	public void configureShare()
 	{
 		/** Uncomment this when website launches **/
 		// String shareBody = Article.getTitle(); + "\n\n" + Article.getURL();
 		String shareBody = "This feature is currently disabled... BWAHAHAHAHA!!";
 		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
 		shareIntent.setType("text/plain");
 		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
 		if (mShareActionProvider != null)
 			mShareActionProvider.setShareIntent(shareIntent);
 	}
 	
 	private void initializeLoadCommentsThread()
 	{
 		loadCommentsThread = new Thread()
 		{
 			public void run()
 			{
 				try
 				{
 					Data.downloadCommentFeed(Article.getCommentFeed());
 					commentList = Data.retrieveCommentFeed(getActivity());
 					mHandler.post(loadComments);
 				}
 				catch (Exception e)
 				{
 					Log.v("Download Comments", "ERROR: " + e.getMessage());
 				}
 				
 				stopThread(this);
 			}
 		};
 	}
 	
 	private synchronized void stopThread(Thread theThread)
 	{
 		if (theThread != null)
 		{
 			theThread = null;
 		}
 	}
 	
 	Runnable loadComments = new Runnable()
 	{
 		public void run()
 		{
 			adapter = new CommentFeedAdapter(getActivity(), commentList);
 			comments.setAdapter(adapter);
 			adapter.notifyDataSetChanged();
 			
 			txtLoadComments.setVisibility(View.GONE);
 			progressBar.setVisibility(View.GONE);
 		}
 	};
 	
 	public class CommentFeedAdapter extends BaseAdapter
 	{
 		private Context context;
 		
 		private List<CommentFeed> listItem;
 		
 		public CommentFeedAdapter(Context context, List<CommentFeed> listItem)
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
 			CommentFeed entry = listItem.get(position);
 			if (view == null)
 			{
 				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = inflater.inflate(R.layout.commentfeed_item, null);
 				
 				holder = new ViewHolder();
 				holder.txtCreator = (TextView) view.findViewById(R.id.txtCreator);
 				holder.txtContent = (TextView) view.findViewById(R.id.txtContent);
 				holder.txtDate = (TextView) view.findViewById(R.id.txtDate);
 				
 				holder.txtCreator.setTypeface(fontBold);
 				holder.txtDate.setTypeface(fontLight);
 				holder.txtContent.setTypeface(fontRegular);
 				
 				view.setTag(holder);
 			}
 			else
 			{
 				holder = (ViewHolder) view.getTag();
 			}
 			
 			holder.txtCreator.setText(entry.getCreator());
 			holder.txtContent.setText(entry.getContent());
 			holder.txtDate.setText(Data.parseDate(context, entry.getDate()));
 			
 			return view;
 		}
 	}
 	
 	private static class ViewHolder
 	{
 		public TextView txtCreator;
 		public TextView txtContent;
 		public TextView txtDate;
 	}
 	
 	public static class CommentFeed
 	{
 		String Creator;
 		String Content;
 		String Date;
 		
 		public CommentFeed(String Creator, String Content, String Date)
 		{
 			this.Creator = Creator;
 			this.Content = Content;
 			this.Date = Date;
 		}
 		
 		public String getCreator()
 		{
 			return Creator;
 		}
 		
 		public String getContent()
 		{
 			return Content;
 		}
 		
 		public String getDate()
 		{
 			return Date;
 		}
 	}
 }
