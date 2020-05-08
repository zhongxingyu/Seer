 package com.pk.addits.widget;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.widget.RemoteViews;
 
 import com.pk.addits.R;
 import com.pk.addits.data.Data;
 import com.pk.addits.data.DatabaseHelper;
 import com.pk.addits.models.Article;
 import com.squareup.picasso.Picasso;
 
 public class WidgetArticleProvider extends AppWidgetProvider
 {
 	public static DatabaseHelper db = null;
 	private List<Article> articleList = new ArrayList<Article>();
 	
 	public static final String ARTICLE_ACTION = "com.pk.addits.widget.ARTICLE_ACTION";
 	public static final String EXTRA_ID = "com.pk.addits.widget.EXTRA_ID";
 	
 	public static final String TOAST_ACTION = "com.example.android.stackwidget.TOAST_ACTION";
 	public static final String EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM";
 	
 	@Override
 	public void onDeleted(Context context, int[] appWidgetIds)
 	{
 		super.onDeleted(context, appWidgetIds);
 	}
 	
 	@Override
 	public void onDisabled(Context context)
 	{
 		super.onDisabled(context);
		//ComponentName component = new ComponentName("com.pk.addits", "com.pk.addits.widget.WidgetArticleProvider");
		//PackageManager pm = context.getPackageManager();
		//pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
 	}
 	
 	@Override
 	public void onEnabled(Context context)
 	{
 		super.onEnabled(context);
		//ComponentName component = new ComponentName("com.pk.addits", "com.pk.addits.widget.WidgetArticleProvider");
		//PackageManager pm = context.getPackageManager();
		//pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
 	}
 	
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
 	{
 		db = new DatabaseHelper(context);
 		articleList = db.getAllArticles();
 		
 		Timer timer = new Timer();
 		timer.scheduleAtFixedRate(new MyTime(context, appWidgetManager), 1, 10000);
 	}
 	
 	private class MyTime extends TimerTask
 	{
 		Context mContext;
 		Article currentArticle;
 		RemoteViews remoteViews;
 		AppWidgetManager appWidgetManager;
 		ComponentName thisWidget;
 		
 		public MyTime(Context context, AppWidgetManager appWidgetManager)
 		{
 			mContext = context;
 			this.appWidgetManager = appWidgetManager;
 			currentArticle = new Article();
 			remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_article);
 			thisWidget = new ComponentName(context, WidgetArticleProvider.class);
 		}
 		
 		@Override
 		public void run()
 		{
 			Random generator = new Random();
			int r = generator.nextInt(articleList.size());
			currentArticle = db.getArticle(articleList.get(r).getID());
 			remoteViews.setTextViewText(R.id.txtTitle, currentArticle.getTitle());
 			remoteViews.setTextViewText(R.id.txtAuthor, currentArticle.getAuthor());
 			remoteViews.setTextViewText(R.id.txtDate, Data.parseRelativeDate(currentArticle.getDate()));
 			try
 			{
				Bitmap bm = Picasso.with(mContext).load(currentArticle.getImage()).resize(500, 220).get();
 				remoteViews.setImageViewBitmap(R.id.articlePreview, bm);
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
 		}
 	}
 }
