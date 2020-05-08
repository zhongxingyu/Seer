 package fr.itinerennes.ui.preferences;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import fr.itinerennes.R;
import fr.itinerennes.utils.VersionUtils;
 
 /**
  * About dialog preference component.
  * 
  * @author Jérémie Huchet
  */
 public final class AboutDialogPreference extends DialogPreference {
 
     /** The event logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AboutDialogPreference.class);
 
     /**
      * Constructor.
      * 
      * @param context
      *            the context
      * @param attrs
      *            the xml attributes
      */
     public AboutDialogPreference(final Context context, final AttributeSet attrs) {
 
         super(context, attrs);
     }
 
     /**
      * Constructor.
      * 
      * @param context
      *            the context
      * @param attrs
      *            the xml attributes
      * @param defStyle
      *            the default style
      */
    public AboutDialogPreference(final Context context, final AttributeSet attrs, final int defStyle) {
 
         super(context, attrs, defStyle);
     }

    /**
     * {@inheritDoc}
     * 
     * @see android.preference.DialogPreference#onCreateDialogView()
     */
    @Override
    protected View onCreateDialogView() {

        final View aboutView = super.onCreateDialogView();
        final TextView versionText = (TextView) aboutView.findViewById(R.id.about_version_name);
        versionText.setText(getContext().getString(R.string.version_dots,
                VersionUtils.getCurrent(this.getContext())));

        return aboutView;
    }
 }
