 package com.malcom.library.android.module.campaign;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.malcom.library.android.module.campaign.MCMCampaignDTO.CampaignPosition;
 import com.malcom.library.android.module.core.MCMCoreAdapter;
 import com.malcom.library.android.utils.MCMUtils;
 import com.malcom.library.android.utils.ToolBox;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * MCMCampaignAdapter.
  * Campaigns module. Shows a banner in 4 posible positions: bottom, top, full screen, and middle.
  * Redirects to other app in Play Store.
  *
  * @author Malcom
  * @author Pepe - code refactor, added middle banners, new logs.
  */
 public class MCMCampaignAdapter {
 
     public static int CAMPAIGN_DEFAULT_DURATION = -1;
 
     private static MCMCampaignAdapter instance = null;
     private ViewGroup bannerLayout;
 
     private String campaignResource = "";
 
     private String malcomAppId;
 
     private MCMCampaignDTO.CampaignType type;
 
     private Activity activity;
     private float density = 1;
     private int resBannerLayoutID;
     private int resImageLayoutID;
 
     private int duration;                                   //integer to set the duration of the banner
 
     private RequestCampaignReceiver receiver;               //callback to execute when the campaign request is done
 
     private MCMCampaignNotifiedDelegate delegate;
 
     public interface RequestCampaignReceiver{
         /**
          *
          * @param banners
          */
         public void onReceivedPromotions(List banners);
 
         public void onRequestFailed(String errorMessage);
     }
 
     private Handler mHandler = new Handler();
 
     // Exists only to defeat instantiation.
     protected MCMCampaignAdapter() {
     }
 
     /**
      * Gets the singleton instance of this class.
      *
      * @return instance of MCMCampaignAdapter.
      */
     public static MCMCampaignAdapter getInstance() {
 
         if (instance == null) {
             instance = new MCMCampaignAdapter();
         }
 
         return instance;
     }
 
     /**
      * Adds a campaign banner to an activity.
      *
      * @param activity     - your activity.
      * @param campaignType - the type of campaign requested
      * @param duration     - the duration to show the campaign banner in seconds
      * @param delegate     - This is the delegate.
      */
     public void addBanner(Activity activity, MCMCampaignDTO.CampaignType campaignType, int duration, MCMCampaignNotifiedDelegate delegate) {
 
         this.activity = activity;
         this.type = campaignType;
         setDuration(duration);
         this.delegate = delegate;
 
         mHandler.removeCallbacks(mRemoveCampaignBanner);
 
         makeRequest();
 
         this.density = this.activity.getResources().getDisplayMetrics().density;
 
     }
 
     public void requestBanner(Activity activity, MCMCampaignDTO.CampaignType campaignType, RequestCampaignReceiver receiver) {
 
         this.activity = activity;
         this.type = campaignType;
         this.receiver = receiver;
 
         makeRequest();
     }
 
     /**
      * Method that removes the banner and notifies it to delegate if delegate is set.
      *
      * @since 1.0.1
      */
     public void removeCurrentBanner(Activity activity) {
 
         // Get layout elements
         resBannerLayoutID = activity.getResources().getIdentifier(MCMCampaignDefines.RES_ID_LAYOUT, "id", activity.getPackageName());
         bannerLayout = (RelativeLayout) activity.findViewById(resBannerLayoutID);
         this.bannerLayout.setVisibility(View.GONE);
 
         notifyCampaignDidFinish();
     }
 
 
     /**
      * Method that removes the banner and notifies it to delegate if delegate is set when time is over (duration)
      *
      * @since 1.0.1
      */
     public void finishCampaign() {
 
         this.bannerLayout.setVisibility(View.GONE);
 
         notifyCampaignDidFinish();
     }
 
     private void makeRequest(){
 
         try {
             // Create URL to get campaigns of my app
             String malcomBaseUrl = MCMCoreAdapter.getInstance().coreGetProperty(MCMCoreAdapter.PROPERTIES_MALCOM_BASEURL);
 
             malcomAppId = URLEncoder.encode(MCMCoreAdapter.getInstance().coreGetProperty(MCMCoreAdapter.PROPERTIES_MALCOM_APPID), "UTF-8");
             String devideId = URLEncoder.encode(ToolBox.device_getId(this.activity), "UTF-8");
 
             campaignResource = MCMCampaignDefines.CAMPAIGN_URL.replace(MCMCampaignDefines.APP_ID_TAG, malcomAppId)
                     .replace(MCMCampaignDefines.UDID_TAG, devideId);
 
 			String urlCampaign = malcomBaseUrl + campaignResource;
            urlCampaign = "http://api.mymalcom.com/v2/campaigns/application/a8be468b-faca-4288-bf79-d4c00d4a8b35/udid/xvQ995mMZDd8cvRFNnTrpA%3D%3D";
 
             // Launch request to get campaigns data
             new MCMCampaignAsyncTasks.DownloadCampaignFile(type, this).execute(urlCampaign);
 
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             notifyCampaignDidFail(e.getMessage());
         }
     }
 
     /**
      * Method that filters the elements for current campaign type and calls the needed operation after the filtered
      * @param campaignsArray the campaigns collected from the server's response
      */
     protected void proccessResponse(ArrayList<MCMCampaignDTO> campaignsArray) {
 
         if (type == MCMCampaignDTO.CampaignType.IN_APP_CROSS_SELLING ||
                 type == MCMCampaignDTO.CampaignType.IN_APP_PROMOTION) {
 
             ArrayList<MCMCampaignDTO> selectionCampaignsArray = new ArrayList<MCMCampaignDTO>();
             if (type == MCMCampaignDTO.CampaignType.IN_APP_CROSS_SELLING) {
                 ArrayList<MCMCampaignDTO> filteredArray = MCMCampaignsUtils.getFilteredCampaigns(campaignsArray, MCMCampaignDTO.CampaignType.IN_APP_CROSS_SELLING);
                 MCMCampaignDTO selectedCampaign = MCMCampaignsUtils.getCampaignPerWeight(filteredArray);
                 selectionCampaignsArray.add(selectedCampaign);
             } else if (type == MCMCampaignDTO.CampaignType.IN_APP_PROMOTION) {
                 selectionCampaignsArray = MCMCampaignsUtils.getFilteredCampaigns(campaignsArray, MCMCampaignDTO.CampaignType.IN_APP_PROMOTION);
             }
 
             if (receiver == null) {
                 createBanner(selectionCampaignsArray);
             } else {
                 receiver.onReceivedPromotions(createBannersList(activity.getApplicationContext(),selectionCampaignsArray));
             }
         }
     }
 
     /**
      * Initiates banner layout depending on position.
      * Launches request to get remote image.
      *
      * @param campaignsArray - the campaigns' data to show.
      */
     protected void createBanner(ArrayList<MCMCampaignDTO> campaignsArray) {
 
         Log.d(MCMCampaignDefines.LOG_TAG, "createBanner - type: " + type + " campaigns: " + campaignsArray.size());
 
         if (campaignsArray == null) {
             return;
         }
 
         // Get layout elements
         try {
             resBannerLayoutID = activity.getResources().getIdentifier(MCMCampaignDefines.RES_ID_LAYOUT, "id", activity.getPackageName());
             bannerLayout = (RelativeLayout) activity.findViewById(resBannerLayoutID);
             resImageLayoutID = activity.getResources().getIdentifier(MCMCampaignDefines.RES_ID_IMAGE, "id", activity.getPackageName());
 
             if (type == MCMCampaignDTO.CampaignType.IN_APP_CROSS_SELLING) {
                 congigureCrossSellingCampaignLayout(campaignsArray.get(0), bannerLayout);
             } else if (type == MCMCampaignDTO.CampaignType.IN_APP_PROMOTION) {
                 bannerLayout = createPromotionsLayout(activity, (RelativeLayout) bannerLayout);
             }
 
             Iterator campaignsIterator = campaignsArray.iterator();
 
             while (campaignsIterator.hasNext()) {
                 // Launch request to get image bitmap and add it to banner layout
 //                new MCMCampaignAsyncTasks.DownloadCampaignImage(this).execute((MCMCampaignDTO) campaignsIterator.next());
 
                 MCMCampaignDTO campaign = (MCMCampaignDTO) campaignsIterator.next();
 
                 MCMCampaignBannerView bannerView = new MCMCampaignBannerView(activity.getApplicationContext(),campaign);
 //                bannerView.setDelegate(this);
                 bannerLayout.addView(bannerView);
 
                 // Config close button (if banner shows on full screen)
                 if (campaign.isFullScreen()) {
                     if (RelativeLayout.class.isInstance(bannerLayout))
                         addCloseButton((RelativeLayout) bannerLayout);
                 }
 
                 //if duration is not zero the banner will be removed automatically
                 if (duration != 0) {
                     mHandler.removeCallbacks(mRemoveCampaignBanner);
                     mHandler.postDelayed(mRemoveCampaignBanner, duration * 1000);
                 }
             }
 
         } catch (Exception e) {
 
             Log.d(MCMCampaignDefines.LOG_TAG, "Create banner error: Attends to load the layout " + MCMCampaignDefines.RES_ID_LAYOUT);
 
         }
 
     }
 
     protected static List<MCMCampaignBannerView> createBannersList(Context context, ArrayList<MCMCampaignDTO> campaignsArray) {
 
         ArrayList<MCMCampaignBannerView> bannersList = new ArrayList<MCMCampaignBannerView>();
 
         Iterator campaignsIterator = campaignsArray.iterator();
 
         while (campaignsIterator.hasNext()) {
             // Launch request to get image bitmap and add it to banner layout
 //                new MCMCampaignAsyncTasks.DownloadCampaignImage(this).execute((MCMCampaignDTO) campaignsIterator.next());
             MCMCampaignBannerView bannerView = new MCMCampaignBannerView(context, (MCMCampaignDTO) campaignsIterator.next());
 //                bannerView.setDelegate(this);
             bannersList.add(bannerView);
         }
 
         return bannersList;
     }
 
     /**
      * Method that notifies the delegate that the campaign was loaded
      */
     protected void notifyCampaignDidLoad(){
         if (delegate != null) {
             delegate.campaignDidLoad();
         }
     }
 
     /**
      * Method that notifies the delegate that the campaign finished
      */
     protected void notifyCampaignDidFinish(){
         if (delegate != null) {
             delegate.campaignDidFinish();
         }
     }
 
     /**
      * Method that notifies the delegate that the campaign failed
      * @param errorMessage the message of the error
      */
     protected void notifyCampaignDidFail(String errorMessage) {
         if (delegate != null) {
             delegate.campaignDidFail();
         }
 
         if (receiver != null) {
             receiver.onRequestFailed(errorMessage);
         }
     }
 
     private static LinearLayout createPromotionsLayout(Activity activity, RelativeLayout layout) {
 
         //Create the ScrollView to can add more banners
         ScrollView scroll = new ScrollView(activity);
         scroll.setBackgroundColor(android.R.color.transparent);
         scroll.setLayoutParams(layout.getLayoutParams());
 
         LinearLayout resultantLayout = new LinearLayout(activity);
         resultantLayout.setOrientation(LinearLayout.VERTICAL);
 
         //Add the views in hierarchy
         scroll.addView(resultantLayout);
         layout.addView(scroll);
 
         return resultantLayout;
     }
 
 
     private static void congigureCrossSellingCampaignLayout(MCMCampaignDTO campaign, ViewGroup layout) {
 
         // Config layout params depending on position
         RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
 //        bannerLayout.setGravity(Gravity.CENTER);
         if (campaign.getCampaignPosition() == CampaignPosition.BOTTOM) {
 
             params.height = MCMUtils.getDPI(layout.getContext(), MCMCampaignDefines.SIZE_BANNER);
             params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
         } else if (campaign.getCampaignPosition() == CampaignPosition.TOP) {
 
             params.height = MCMUtils.getDPI(layout.getContext(), MCMCampaignDefines.SIZE_BANNER);
             params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
         } else if (campaign.getCampaignPosition() == CampaignPosition.MIDDLE_LANDSCAPE ||
                 campaign.getCampaignPosition() == CampaignPosition.MIDDLE_PORTRAIT) {
 
             int margin = MCMUtils.getDPI(layout.getContext(), MCMCampaignDefines.MIDDLE_MARGIN);
             layout.setPadding(margin, margin, margin, margin);
         } else if (campaign.getCampaignPosition() == CampaignPosition.FULL_SCREEN) {
 
             // mantain params of full screen
         }
 
         // Apply params to banner layout
         layout.setLayoutParams(params);
     }
 
     /**
      * Shows banner image after download finish.
      * Configures close button if needed, and adds click events for banner and close button.
      *
      * @param bitmap   - the downloaded image.
      * @param campaign - the campaign object that is showed.
      */
     protected void setImageBanner(Bitmap bitmap, final MCMCampaignDTO campaign) {
 
         Log.d(MCMCampaignDefines.LOG_TAG, "setImageBanner campaign: " + campaign.getName());
 
         //Ya lo hago en el bannerView
 //        ImageView bannerImageView = new ImageView(activity.getApplicationContext());
 //        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
 //                ViewGroup.LayoutParams.WRAP_CONTENT);
 //        bannerImageView.setLayoutParams(layoutParams);
 //
 //        bannerLayout.addView(bannerImageView);
 
     }
 
     /**
      * Method that adds a close button to the layout and hide its when clicked
      *
      * @param layout RelativeLayout where the button will be added
      */
     private static void addCloseButton(final RelativeLayout layout) {
         Button closeButton = new Button(layout.getContext());
         closeButton.setText("X");
         RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                 RelativeLayout.LayoutParams.WRAP_CONTENT);
         params.addRule(RelativeLayout.ALIGN_RIGHT, layout.getId());
         params.addRule(RelativeLayout.ALIGN_TOP, layout.getId());
         closeButton.setLayoutParams(params);
         layout.addView(closeButton);
         layout.setBackgroundColor(Color.argb(MCMCampaignDefines.BACKGROUND_ALPHA, 0, 0, 0));
         closeButton.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
 
                 // Hide banner
                 layout.setVisibility(View.GONE);
             }
         });
     }
 
     private Runnable mRemoveCampaignBanner = new Runnable() {
         public void run() {
             mHandler.removeCallbacks(mRemoveCampaignBanner);
             finishCampaign();
         }
     };
 
     public int getDuration() {
         return duration;
     }
 
     public void setDuration(int duration) {
         if (duration < 0) {
             //sets the default time duration
             this.duration = MCMCampaignDefines.DEFAULT_CAMPAIGN_DURATION;
         } else {
             //set the duration parameter
             this.duration = duration;
         }
     }
 
     public MCMCampaignDTO.CampaignType getType() {
         return type;
     }
 
     public String getCampaignResource() {
         return campaignResource;
     }
 
     public String getMalcomAppId() {
         return malcomAppId;
     }
 
 }
