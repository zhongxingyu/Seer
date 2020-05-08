 package net.trajano.gasprices;
 
 import java.util.Date;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.os.Build;
 import android.text.format.Time;
 import android.util.Log;
 
 /**
  * This is an internal class to adapt the editor to add a few methods to hide
  * knowledge from clients what the keys are.
  * 
  * @author Archimedes Trajano (developer@trajano.net)
  * 
  */
 public class PreferenceAdaptorEditor implements
 		android.content.SharedPreferences.Editor {
 	private final android.content.SharedPreferences.Editor editor;
 
 	public PreferenceAdaptorEditor(
 			final android.content.SharedPreferences.Editor editor) {
 		this.editor = editor;
 	}
 
	/**
	 * {@inheritDoc}
	 * 
	 * However, if it detects that we are using an API level of less than 9
	 * which is when {@link #apply()} was first introduced it will default to
	 * commit.
	 */
 	@Override
 	public void apply() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
 	}
 
 	@Override
 	public android.content.SharedPreferences.Editor clear() {
 		return editor.clear();
 	}
 
 	@Override
 	public boolean commit() {
 		return editor.commit();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @deprecated should not be used as it allows access to modify the
 	 *             preferences directly.
 	 */
 	@Override
 	@Deprecated
 	public android.content.SharedPreferences.Editor putBoolean(
 			final String key, final boolean value) {
 		return editor.putBoolean(key, value);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @deprecated should not be used as it allows access to modify the
 	 *             preferences directly.
 	 */
 	@Override
 	@Deprecated
 	public android.content.SharedPreferences.Editor putFloat(final String key,
 			final float value) {
 		return editor.putFloat(key, value);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @deprecated should not be used as it allows access to modify the
 	 *             preferences directly.
 	 */
 	@Override
 	@Deprecated
 	public android.content.SharedPreferences.Editor putInt(final String key,
 			final int value) {
 		return editor.putInt(key, value);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @deprecated should not be used as it allows access to modify the
 	 *             preferences directly.
 	 */
 	@Override
 	@Deprecated
 	public android.content.SharedPreferences.Editor putLong(final String key,
 			final long value) {
 		return editor.putLong(key, value);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @deprecated should not be used as it allows access to modify the
 	 *             preferences directly.
 	 */
 	@Override
 	@Deprecated
 	public android.content.SharedPreferences.Editor putString(final String key,
 			final String value) {
 		return editor.putString(key, value);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @deprecated should not be used as it allows access to modify the
 	 *             preferences directly.
 	 */
 	@Override
 	@Deprecated
 	public android.content.SharedPreferences.Editor putStringSet(
 			final String arg0, final Set<String> arg1) {
 		return editor.putStringSet(arg0, arg1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @deprecated should not be used as it allows access to modify the
 	 *             preferences directly.
 	 */
 	@Override
 	@Deprecated
 	public android.content.SharedPreferences.Editor remove(final String key) {
 		return editor.remove(key);
 	}
 
 	/**
 	 * This removes the selected city id for the widgets.
 	 * 
 	 * @param appWidgetIds
 	 *            appwidget ID.
 	 * @return itself
 	 */
 	public PreferenceAdaptorEditor removeWidgetCityId(final int... appWidgetIds) {
 		for (final int appWidgetId : appWidgetIds) {
 			remove(PreferenceAdaptor.WIDGET_CITY_ID_PREFERENCE_KEY_PREFIX
 					+ appWidgetId);
 		}
 		return this;
 	}
 
 	/**
 	 * This updates the selected city id for the widget
 	 * 
 	 * @param appWidgetId
 	 *            appwidget ID.
 	 * @param cityId
 	 *            city id
 	 * @return itself
 	 */
 	public PreferenceAdaptorEditor saveWidgetCityId(final int appWidgetId,
 			final long cityId) {
 		putLong(PreferenceAdaptor.WIDGET_CITY_ID_PREFERENCE_KEY_PREFIX
 				+ appWidgetId, cityId);
 		return this;
 	}
 
 	/**
 	 * This will store the JSON data into the preferences as well as break it
 	 * apart for each city. Only the main JSON feed is formatted, the individual
 	 * city data is kept small.
 	 * 
 	 * @param gasPrices
 	 *            gas prices JSON data object.
 	 */
 	public void setJsonData(final JSONObject gasPrices) {
 		try {
 			editor.putString(PreferenceAdaptor.JSON_DATA_KEY,
 					gasPrices.toString(3));
 			final JSONArray gasPricesArray = gasPrices
 					.getJSONArray("gasprices");
 			for (int i = gasPricesArray.length() - 1; i >= 0; --i) {
 				final JSONObject cityData = gasPricesArray.getJSONObject(i);
 				editor.putString(PreferenceAdaptor.CITY_DATA_KEY_PREFIX
 						+ cityData.getLong("city_id"), cityData.toString());
 			}
 		} catch (final JSONException e) {
 			Log.e("GasPrices", e.getMessage());
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Sets the last updated time preference. Internally it will convert it to a
 	 * long before storing it in the shared preferences.
 	 * 
 	 * @param lastUpdated
 	 * @return itself
 	 */
 	public PreferenceAdaptorEditor setLastUpdated(final Date lastUpdated) {
 		putLong(PreferenceAdaptor.LAST_UPDATED_KEY, lastUpdated.getTime());
 		return this;
 	}
 
 	/**
 	 * Sets the last updated time preference to right now. Internally it will
 	 * convert it to a long before storing it in the shared preferences.
 	 * 
 	 * @return itself
 	 */
 	public PreferenceAdaptorEditor setLastUpdatedToNow() {
 		final Time time = new Time();
 		time.setToNow();
 		putLong(PreferenceAdaptor.LAST_UPDATED_KEY, time.normalize(false));
 		return this;
 	}
 
 	/**
 	 * This updates the selected city name.
 	 * 
 	 * @param cityId
 	 *            city id
 	 * @return
 	 */
 	public PreferenceAdaptorEditor setSelectedCityId(final long cityId) {
 		putLong(PreferenceAdaptor.SELECTED_CITY_ID_KEY, cityId);
 		return this;
 	}
 
 }
