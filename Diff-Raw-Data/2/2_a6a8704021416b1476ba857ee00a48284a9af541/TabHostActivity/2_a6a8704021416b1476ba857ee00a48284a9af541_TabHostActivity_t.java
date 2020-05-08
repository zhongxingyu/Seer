 package com.manish.tabdemo;
 
 import android.app.TabActivity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.LinearLayout;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabWidget;
 import android.widget.TextView;
 
 
 
 @SuppressWarnings("deprecation")
 public class TabHostActivity extends TabActivity implements OnTabChangeListener
 { 
  private static final String[] TABS = { "HomeGroupActivity", "AboutGroupActivity", "ContactGroupActivity" };
  private static final String[] TAB_NAMES = { "Home", "About", "Contact"};
  public static TabHost tabs ;
     public static TabWidget tabWidget ;    
  protected Bitmap roundedImage;
     public boolean checkTabsListener = false;
  
     
     public void onCreate(Bundle icicle)
     {        
      super.onCreate(icicle);
         setContentView(R.layout.activity_tab_host);
       
         Bitmap roundedImage = BitmapFactory.decodeResource(getResources(),R.drawable.ic_tab_background);
         roundedImage = getRoundedCornerBitmap(roundedImage,3);
        
         tabs = getTabHost();
    
         tabWidget = tabs.getTabWidget();
                       
      tabs.setOnTabChangedListener(this);
         
      for (int i = 0; i < TABS.length; i++)
         {
          TabHost.TabSpec tab = tabs.newTabSpec(TABS[i]);
          
          //Asociating Components
          ComponentName oneActivity = new ComponentName("com.manish.tabdemo", "com.manish.tabdemo." + TABS[i]);
          Intent intent = new Intent().setComponent(oneActivity);           
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          tab.setContent(intent);         
          //Setting the Indicator
          MyTabIndicator myTab = new MyTabIndicator(this, TAB_NAMES[i],(i+1), roundedImage); 
          tab.setIndicator(myTab); 
          tabs.addTab(tab);
         }
         
      checkTabsListener = true;
      
         for(int i=0;i<tabs.getTabWidget().getChildCount();i++)
         {
          tabs.getTabWidget().getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
         }
         
      
   tabs.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.ic_tab_background);   
   
   //Maintaining Clicks
   
   // Home Tab Click
   
   tabWidget.getChildAt(0).setOnClickListener(new OnClickListener()
         {   
    @Override
    public void onClick(View v)
    {     
      if(HomeGroupActivity.HomeGroupStack != null && HomeGroupActivity.HomeGroupStack.mIdList.size()>1)
      {      
       HomeGroupActivity.HomeGroupStack.getLocalActivityManager().removeAllActivities();
       HomeGroupActivity.HomeGroupStack.mIdList.clear();
       HomeGroupActivity.HomeGroupStack.mIntents.clear();
       HomeGroupActivity.HomeGroupStack.mAnimator.removeAllViews();
       HomeGroupActivity.HomeGroupStack.startChildActivity("CareGroupActivity", new Intent(HomeGroupActivity.HomeGroupStack, HomeActivity.class));
     
      }
      
      tabWidget.setCurrentTab(0);
      tabs.setCurrentTab(0);
      tabs.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.ic_tab_background);  
    }
         });
   
  
   // About tab Click
   
   tabWidget.getChildAt(1).setOnClickListener(new OnClickListener()
         {   
    public void onClick(View v)
    {     
     if(AboutGroupActivity.AboutGroupStack != null && AboutGroupActivity.AboutGroupStack.mIdList.size()>0)
     {
      AboutGroupActivity.AboutGroupStack.getLocalActivityManager().removeAllActivities();
      AboutGroupActivity.AboutGroupStack.mIdList.clear();      
      AboutGroupActivity.AboutGroupStack.mIntents.clear();
      AboutGroupActivity.AboutGroupStack.mAnimator.removeAllViews();            
      AboutGroupActivity.AboutGroupStack.startChildActivity("TrackingGroupActivity", new Intent(AboutGroupActivity.AboutGroupStack, AboutActivity.class));           
     }     
              
     tabWidget.setCurrentTab(1);
     tabs.setCurrentTab(1);
     tabs.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.ic_tab_background);       
    }
         });
   
   // Contact tab click
   
   tabWidget.getChildAt(2).setOnClickListener(new OnClickListener()
         {   
    public void onClick(View v)
    {     
     if(ContactGroupActivity.ContactGroupStack != null && ContactGroupActivity.ContactGroupStack.mIdList.size()>0)
     {
       
      ContactGroupActivity.ContactGroupStack.getLocalActivityManager().removeAllActivities();
      ContactGroupActivity.ContactGroupStack.mIdList.clear();      
      ContactGroupActivity.ContactGroupStack.mIntents.clear();
      ContactGroupActivity.ContactGroupStack.mAnimator.removeAllViews();            
      ContactGroupActivity.ContactGroupStack.startChildActivity("DashboardGroupActivity", new Intent(ContactGroupActivity.ContactGroupStack, ContactActivity.class));           
     }     
              
     tabWidget.setCurrentTab(2);
     tabs.setCurrentTab(2);
     tabs.getTabWidget().getChildAt(2).setBackgroundResource(R.drawable.ic_tab_background);       
    }
         });
   
 
     }
   
     
     public class MyTabIndicator extends LinearLayout 
     {
   public MyTabIndicator(Context context, String label, int tabId, Bitmap bgImg)
   {
    super(context);
    LinearLayout tab = null;
    TextView tv;
    this.setGravity(Gravity.CENTER);
    
    if(tabId == 1)
    {
     tab = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.home_tab, null);
     tv = (TextView)tab.findViewById(R.id.tab_label);
     tv.setText(label);
    }
    
    
    
    else if(tabId == 2)
    {
     tab = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.about_tab, null);
     tv = (TextView)tab.findViewById(R.id.tab_label);
     tv.setText(label);
    }
    else if(tabId == 3)
    {
     tab = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.contact_tab, null);
     tv = (TextView)tab.findViewById(R.id.tab_label);
     tv.setText(label);
    }
        
    this.addView(tab, new LinearLayout.LayoutParams(320/4,55));   
   }  
     }
 
     
     
  public void onTabChanged(String tabId) 
  {      
   InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(tabs.getApplicationWindowToken(), 0);
           
         for(int i=0; i<tabs.getTabWidget().getChildCount(); i++)
   {                             
          if(tabId.equalsIgnoreCase(TABS[i]))
    {            
     tabs.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.ic_tab_background);     
    }
    else
    {
     tabs.getTabWidget().getChildAt(i).setBackgroundColor((Color.TRANSPARENT));
    }     
     }  
  }
 
   public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPxRadius)
  {
         Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
         Canvas canvas = new Canvas(output);
         
         final int color = 0xff424242;
         final Paint paint = new Paint();
         final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
         final RectF rectF = new RectF(rect);
         final float roundPx =roundPxRadius;
      
         paint.setAntiAlias(true);
         canvas.drawARGB(0, 0, 0, 0);
         paint.setColor(color);
         canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
      
         paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
         canvas.drawBitmap(bitmap, rect, rect, paint);
      
         return output;
  }
  
  public void onResume()
  {
   super.onResume();
   
   
   //ReConstructing TabViews
   reDesignTabViews(); 
  }
  
  public void onPause()
  {
   super.onPause();     
  }
  
 
   
  
  /**
   * Method used to re constructing the Views at tab bar. This solves tabs disappearing issue.
   */
  public void reDesignTabViews()
  {
   MyTabIndicator myIndicator;
   
   
   //Construction of tab views....
   for(int i=0 ; i< tabWidget.getChildCount() ; i++)
   {
    myIndicator = (MyTabIndicator) tabWidget.getChildAt(i);
    myIndicator.removeAllViews();
    
    switch (i) 
    {
 
      case 0:
      myIndicator.addView((LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.home_tab, null));
      break;
     case 1:    
      myIndicator.addView((LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.about_tab, null));    
      break;
     case 2:    
      myIndicator.addView((LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.contact_tab, null));    
      break;
     
    }   
   }  
  }
  
 }
