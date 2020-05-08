 package com.java.notification.content;
 
 import android.app.PendingIntent;
 import android.graphics.Bitmap;
 import com.java.util.Utils;
 
 import java.util.UUID;
 
 public class NotificationContent
 {
     private String mTitle;
     private String mBody;
     private String mTickerText;
     private String mSummaryText;
     private String mContentInfo;
     private Bitmap mNotificationIcon;
     private String mFullyMessageText;
     private int mId;
     private NotificationStyle mStyle;
     private PendingIntent mContentPendingIntent;
     private PendingIntent mDeletePendingIntent;
     private NotificationAction mNotificationAction;
     private PendingIntent mFullScreenIntent;
     private boolean mFullScreenPriority;
     private int mIcon;
     private AlertPreference mAlertPreference;
 
    public NotificationContent(String title, String body, String tickerText)
     {
         mTitle = title;
         mBody = body;
         mFullyMessageText = body;
         mTickerText = new Utils().trimTickerText(tickerText);
         mSummaryText = "";
        mIcon = -1;
         mId = UUID.randomUUID().hashCode();
         mStyle = NotificationStyle.NORMAL;
         mAlertPreference = new AlertPreference();
     }
 
     public String getTitle()
     {
         return mTitle;
     }
 
     public NotificationStyle getStyle()
     {
         return mStyle;
     }
 
     public String getBody()
     {
         return mBody;
     }
 
     public String getTickerText()
     {
         return mTickerText;
     }
 
     public String getSummaryText()
     {
         return mSummaryText;
     }
 
     public String getContentInfo()
     {
         return mContentInfo;
     }
 
     public Bitmap getNotificationIcon()
     {
         return mNotificationIcon;
     }
 
     public String getFullMessageText()
     {
         return mFullyMessageText;
     }
 
     public void setSummaryText(String value)
     {
         mSummaryText = value;
     }
 
     public void setContactId(String value)
     {
         mContentInfo =value;
     }
 
     public void setNotificationIcon(Bitmap value)
     {
         mNotificationIcon = value;
     }
 
     public void setFullMessageText(String value)
     {
         mFullyMessageText = value;
     }
 
     public void setStyle(NotificationStyle style)
     {
         mStyle = style;
     }
 
     public int getId()
     {
         return mId;
     }
 
     public void setContentIntent(PendingIntent pendingIntent)
     {
         mContentPendingIntent = pendingIntent;
     }
 
     public PendingIntent getContentIntent()
     {
         return mContentPendingIntent;
     }
 
     public void setDeleteIntent(PendingIntent pendingIntent)
     {
         mDeletePendingIntent = pendingIntent;
     }
 
     public PendingIntent getDeleteIntent()
     {
         return mDeletePendingIntent;
     }
 
     public void setNotificationAction(NotificationAction action)
     {
         mNotificationAction = action;
     }
 
     public NotificationAction getNotificationAction()
     {
         return mNotificationAction;
     }
 
     public PendingIntent getFullScreenIntent()
     {
         return mFullScreenIntent;
     }
 
     public void setFullScreenIntentAndPriority(PendingIntent pendingIntent, boolean priority)
     {
         mFullScreenIntent = pendingIntent;
         mFullScreenPriority = priority;
     }
 
     public boolean isFullScreenPriorityHigh()
     {
         return mFullScreenPriority;
     }
 
     public int getIcon()
     {
         return mIcon;
     }
 
     public void setIcon(int icon)
     {
         mIcon = icon;
     }
 
     public void setAlertPreference(AlertPreference alertPreference)
     {
         mAlertPreference = alertPreference;
     }
 
     public AlertPreference getAlertPreference()
     {
         return mAlertPreference;
     }
 }
