 package com.dozuki.ifixit.ui.guide.create;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.*;
 import com.dozuki.ifixit.MainApplication;
 import com.dozuki.ifixit.R;
 import com.dozuki.ifixit.model.guide.GuideInfo;
 import com.dozuki.ifixit.util.APIService;
 import com.marczych.androidimagemanager.ImageManager;
 import org.holoeverywhere.app.Activity;
 
 public class GuideListItem extends LinearLayout {
    private static final int ANIMATION_DURATION = 300;
 
    private static final boolean STATE_OPEN = true;
    private static final boolean STATE_CLOSED = false;
 
    private TextView mTitleView;
    private ImageView mThumbnail;
    private TextView mDeleteButton;
    private TextView mEditButton;
    private TextView mPublishText;
    private TextView mPublishButton;
    private ToggleButton mToggleEdit;
    private LinearLayout mEditBar;
    private Activity mActivity;
    private final RelativeLayout mUpperSection;
 
    private ImageManager mImageManager;
 
    private boolean mEditBarVisible = false;
    private GuideInfo mGuideInfo;
 
    private OnClickListener mEditClickListener = new OnClickListener() {
       @Override
       public void onClick(View v) {
          Intent intent = new Intent(mActivity, StepsActivity.class);
          intent.putExtra(StepsActivity.GUIDE_ID_KEY, mGuideInfo.mGuideid);
          mActivity.startActivityForResult(intent, GuideCreateActivity.GUIDE_STEP_LIST_REQUEST);
       }
    };
 
    private OnClickListener mPublishClickListener = new OnClickListener() {
       @Override
       public void onClick(View v) {
          if (!mGuideInfo.mPublic) {
             APIService.call(mActivity,
              APIService.getPublishGuideAPICall(mGuideInfo.mGuideid, mGuideInfo.mRevisionid));
          } else {
             APIService.call(mActivity,
              APIService.getUnPublishGuideAPICall(mGuideInfo.mGuideid, mGuideInfo.mRevisionid));
          }
       }
    };
 
    private OnClickListener mDeleteClickListener = new OnClickListener() {
       @Override
       public void onClick(View v) {
          ((GuideCreateActivity)mActivity).createDeleteDialog(mGuideInfo).show();
       }
    };
 
    private OnClickListener mUpperSectionListener = new OnClickListener() {
       @Override
       public void onClick(View v) {
          mToggleEdit.toggle();
       }
    };
 
 
    public GuideListItem(Context context, Activity activity) {
       super(context);
       mImageManager = MainApplication.get().getImageManager();
       mActivity = activity;
 
       LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       inflater.inflate(R.layout.guide_create_item, this, true);
 
       mTitleView = (TextView) findViewById(R.id.guide_create_item_title);
       mThumbnail = (ImageView) findViewById(R.id.guide_create_item_thumbnail);
       mToggleEdit = (ToggleButton) findViewById(R.id.guide_create_toggle_edit);
       mUpperSection = (RelativeLayout) findViewById(R.id.guide_create_upper_section);
       mDeleteButton = (TextView) findViewById(R.id.guide_create_item_delete);
       mEditBar = (LinearLayout) findViewById(R.id.guide_create_item_edit_section);
       mEditButton = (TextView) findViewById(R.id.guide_create_item_edit);
       mPublishText = (TextView) findViewById(R.id.guide_create_item_publish_status);
       mPublishButton = (TextView) findViewById(R.id.guide_create_item_publish);
 
       mToggleEdit.setChecked(STATE_CLOSED);
       mToggleEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             mGuideInfo.mEditMode = isChecked;
             ((GuideCreateActivity)mActivity).onItemSelected(mGuideInfo.mGuideid, isChecked);
             toggleListItem(isChecked, true, mToggleEdit, mEditBar);
          }
       });
 
 
       this.setOnClickListener(mUpperSectionListener);
 
       mUpperSection.setOnClickListener(mUpperSectionListener);
       mDeleteButton.setOnClickListener(mDeleteClickListener);
       mEditButton.setOnClickListener(mEditClickListener);
       mPublishButton.setOnClickListener(mPublishClickListener);
    }
 
    public void setRowData(GuideInfo guideInfo) {
       String image;
 
       mGuideInfo = guideInfo;
       setTag(mGuideInfo.mGuideid);
 
       mTitleView.setText(mGuideInfo.mTitle);
 
       if (mGuideInfo.mImage != null) {
          image = mGuideInfo.mImage.getSize(".thumbnail");
       } else {
          image = "";
       }
 
       if (mThumbnail != null) {
          mImageManager.displayImage(image, mActivity, mThumbnail);
       }
 
       setPublished(mGuideInfo.mPublic);
    }
 
    public void setPublished(boolean published) {
       if (published) {
          buildPublishView(R.drawable.ic_list_item_unpublish, Color.rgb(0, 191, 0),
           R.string.published, R.string.unpublish);
       } else {
          buildPublishView(R.drawable.ic_list_item_publish, Color.RED,
           R.string.unpublished, R.string.publish);
       }
    }
 
    private void buildPublishView(int drawable, int color, int textString, int buttonString) {
       Drawable img = getContext().getResources().getDrawable(R.drawable.ic_list_item_publish);
       img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
 
       mPublishText.setText(textString);
       mPublishText.setTextColor(color);
 
       mPublishButton.setCompoundDrawables(img, null, null, null);
       mPublishButton.setText(buttonString);
    }
 
    public void toggleListItem(boolean isChecked, boolean animate, final ToggleButton mToggleEdit,
     final LinearLayout mEditBar) {
       if (isChecked) {
          if (animate) {
             Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
             mToggleEdit.startAnimation(rotateAnimation);
             // Creating the expand animation for the item
             ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);
             // Start the animation on the toolbar
             mEditBar.startAnimation(expandAni);
          } else {
             mEditBar.setVisibility(View.VISIBLE);
             ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = 0;
          }
       } else {
          if (animate) {
             Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_counterclockwise);
             mToggleEdit.startAnimation(rotateAnimation);
             ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);
             mEditBar.startAnimation(expandAni);
          } else {
             mEditBar.setVisibility(View.GONE);
             ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = -50;
          }
       }
    }
 
    public void setChecked(boolean check) {
       mToggleEdit.setChecked(check);
    }
 
 }
