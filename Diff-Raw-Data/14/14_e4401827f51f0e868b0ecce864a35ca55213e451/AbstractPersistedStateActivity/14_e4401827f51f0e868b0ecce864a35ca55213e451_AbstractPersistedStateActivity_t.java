 package woodsie.avalanche;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import android.app.Activity;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CompoundButton;
 
 public abstract class AbstractPersistedStateActivity extends Activity {
 
 	protected void saveStateToFile(View mainFormView) {
 
 		PrintWriter writer = null;
 		try {
 			Map<Integer, Boolean> stateMap = new HashMap<Integer, Boolean>();
 			recursiveStateReader(mainFormView, stateMap);
 
 			writer = new PrintWriter(openFileOutput(getStateFilename(), 0));
 
 			writer.println(getVersionCode());
 			for (Entry<Integer, Boolean> entry : stateMap.entrySet()) {
 				writer.println(entry.getKey() + "=" + entry.getValue());
 			}
 
 		} catch (Exception e) {
 			// Ignore - If we can't save the state, we can't save it
 		} finally {
 			if (writer != null)
 				writer.close();
 		}
 
 	}
 
 	private void recursiveStateReader(View view, Map<Integer, Boolean> stateMap) {
 
 		if (view instanceof ViewGroup) {
 			ViewGroup group = (ViewGroup) view;
 			for (int i = 0; i < group.getChildCount(); i++) {
 				recursiveStateReader(group.getChildAt(i), stateMap);
 			}
 
 		} else if (view instanceof CompoundButton) {
 			stateMap.put(view.getId(), ((CompoundButton) view).isChecked());
 		}
 
 	}
 
 	protected void restoreStateFromFile() {
 		BufferedReader reader = null;
 
 		try {
 			reader = new BufferedReader(new InputStreamReader(openFileInput(getStateFilename())));
 
			int versionCode = Integer.valueOf(reader.readLine());
 			if (versionCode != getVersionCode())
 				return;
 
 			String line;
 			while ((line = reader.readLine()) != null) {
 				String[] fields = line.split("=", 2);
				View view = findViewById(Integer.valueOf(fields[0]));
 				if (view != null && view instanceof CompoundButton) {
					((CompoundButton) view).setChecked(Boolean.valueOf(fields[1]));
 				}
 			}
 
 		} catch (Exception e) {
 			// Ignore - probably a corrupted state file we don't want to restore
 		} finally {
 			try {
 				if (reader != null)
 					reader.close();
 			} catch (IOException e) {
 				// Ignore
 			}
 		}
 	}
 
 	private int getVersionCode() throws NameNotFoundException {
 		return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
 	}
 
 	private String getStateFilename() {
 		return getClass().getCanonicalName() + "-State.properties";
 	}
 }
