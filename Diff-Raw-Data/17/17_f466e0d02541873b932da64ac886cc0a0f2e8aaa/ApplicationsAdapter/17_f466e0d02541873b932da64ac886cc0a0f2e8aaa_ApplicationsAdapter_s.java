 package info.evelio.whatsnew.adapter;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
 import info.evelio.whatsnew.R;
 import info.evelio.whatsnew.model.ApplicationEntry;
 
 import static android.text.Html.fromHtml;
 import static info.evelio.whatsnew.util.StringUtils.hexColor;
 
 /**
  * @author Evelio Tarazona Cáceres <evelio@evelio.info>
  */
 public class ApplicationsAdapter extends SingleTypeAdapter<ApplicationEntry> {
   private static final int[] CHILD_VIEW_IDS = { R.id.app_item_icon, R.id.app_item_label, R.id.app_version_label };
   private final Drawable mDefaultDrawable;
   private final Context mContext;
   private static final long NON_FRESH_TIME = 24 * 60 * 60 * 1000; // One nice day
 
   public ApplicationsAdapter(Context context) {
     super(context, R.layout.application_item);
     mContext = context;
     mDefaultDrawable = context.getResources().getDrawable(R.color.default_drawable);
   }
 
   @Override
   protected int[] getChildViewIds() {
     return CHILD_VIEW_IDS;
   }
 
   @Override
   protected void update(int position, ApplicationEntry item) {
     final Drawable icon = item.getIcon();
     imageView(0).setImageDrawable(icon != null ? item.getIcon() : mDefaultDrawable);
     setText(1, item.getDisplayableLabel());
     // Some packages just won't Set package version :\
     final CharSequence displayPackageVersion = item.getDisplayableVersion();
     CharSequence versionLabel;
    if (item.getPackageVersionCode() > item.getPackageVersionCode()) { // We got upgraded
       final int color = System.currentTimeMillis() - item.getLastUpdateTime() > NON_FRESH_TIME
           ? R.color.version_regular_text
           : R.color.version_upgraded_text;
       final CharSequence displayPreviousPackageVersion = item.getDisplayablePreviousVersion();
       final String computedVersionLabel = hexColor(mContext, R.string.version_update_template,
           color, displayPreviousPackageVersion, displayPackageVersion);
       versionLabel = fromHtml(computedVersionLabel);
     } else {
       versionLabel = displayPackageVersion;
     }
     setText(2, versionLabel);
   }
 }
