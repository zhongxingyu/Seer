 
 package org.projectvoodoo.gstandroid.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.projectvoodoo.gstandroid.R;
 import org.projectvoodoo.gstandroid.Shellcmd;
 import org.projectvoodoo.gstandroid.gstreamer.GstFeature;
 import org.projectvoodoo.gstandroid.gstreamer.GstPlugin;
 
 import android.os.AsyncTask;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.SectionIndexer;
 import android.widget.TextView;
 
 public class GstPluginsAdapter extends BaseExpandableListAdapter implements SectionIndexer {
 
     @SuppressWarnings("unused")
     private static final String TAG = "Gstreamer Android GstPluginsAdapter";
 
     private static GstPlugin[] sPlugins = new GstPlugin[0];
     private String[] mSections = new String[0];
     private ArrayList<Integer> mSectionsPositions = new ArrayList<Integer>();
 
     public GstPluginsAdapter() {
         new SetPluginsTask().execute();
     }
 
     private class SetPluginsTask extends AsyncTask<Void, Void, Void> {
         @Override
         protected void onPreExecute() {
             if (sPlugins.length > 0)
                 cancel(true);
         }
 
         @Override
         protected Void doInBackground(Void... params) {
             HashMap<String, GstPlugin> plugins = new HashMap<String, GstPlugin>();
 
             ArrayList<String> cmdout = Shellcmd.gstInspect("");
 
             Pattern p = Pattern.compile("(\\w*): (.*)");
             for (String line : cmdout) {
                 Matcher m = p.matcher(line);
                 if (!m.matches())
                     break;
 
                 String pluginName = m.group(1);
 
                 try {
                     String[] splittedFeature = m.group(2).split(": ");
                     String featureName = splittedFeature[0].trim();
                     String featureLongName = splittedFeature[1].trim();
 
                     if (!plugins.containsKey(pluginName))
                         plugins.put(pluginName, new GstPlugin(pluginName));
 
                     plugins.get(pluginName)
                             .addFeature(new GstFeature(featureName, featureLongName));
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
 
             GstPlugin[] pluginsArray = new GstPlugin[plugins.size()];
             plugins.values().toArray(pluginsArray);
             Arrays.sort(pluginsArray);
 
             sPlugins = pluginsArray;
             generateSectionAndIndexes();
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Void result) {
             super.onPostExecute(result);
             notifyDataSetChanged();
         }
     }
 
     @Override
     public Object getChild(int groupPosition, int childPosition) {
         return getFeature(getPlugin(groupPosition), childPosition);
     }
 
     @Override
     public long getChildId(int groupPosition, int childPosition) {
         return groupPosition + childPosition << 8;
     }
 
     @Override
     public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
             View convertView, ViewGroup parent) {
 
         if (convertView == null)
             convertView = View.inflate(parent.getContext(), R.layout.plugin_expanded, null);
 
         TextView featureName =
                 (TextView) convertView.findViewById(R.id.plugin_expanded_feature_name);
         TextView featureLongName =
                 (TextView) convertView.findViewById(R.id.plugin_expanded_feature_long_name);
 
         GstFeature feature = (GstFeature) getChild(groupPosition, childPosition);
         featureName.setText(feature.name);
         featureLongName.setText(feature.longName);
 
         return convertView;
     }
 
     @Override
     public int getChildrenCount(int groupPosition) {
         return getPlugin(groupPosition).getFeatures().size();
     }
 
     @Override
     public Object getGroup(int groupPosition) {
         return getPlugin(groupPosition);
     }
 
     @Override
     public int getGroupCount() {
         return sPlugins.length;
     }
 
     @Override
     public long getGroupId(int groupPosition) {
         return groupPosition;
     }
 
     @Override
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
             ViewGroup parent) {
 
         if (convertView == null)
             convertView = View.inflate(parent.getContext(), R.layout.plugin_group, null);
 
         TextView pluginName = (TextView) convertView.findViewById(R.id.plugin_group_name);
         GstPlugin plugin = getPlugin(groupPosition);
         pluginName.setText(plugin.name + " (" + getChildrenCount(groupPosition) + ")");
 
         return convertView;
     }
 
     @Override
     public boolean hasStableIds() {
         return true;
     }
 
     @Override
     public boolean isChildSelectable(int groupPosition, int childPosition) {
         return true;
     }
 
     private GstPlugin getPlugin(int groupPosition) {
         return sPlugins[groupPosition];
     }
 
     private GstFeature getFeature(GstPlugin plugin, int childPosition) {
         int i = 0;
         for (GstFeature feature : plugin.getFeatures()) {
             if (childPosition == i)
                 return feature;
             i++;
         }
 
         return null;
     }
 
     @Override
     public int getPositionForSection(int section) {
         if (section >= 0 && section < mSectionsPositions.size())
             return mSectionsPositions.get(section);
 
         return 0;
     }
 
     @Override
     public int getSectionForPosition(int position) {
        int indexMax = mSectionsPositions.size() > 0 ? mSectionsPositions.size() - 1 : 0;
 
         if (position >= 0 && position < mSectionsPositions.get(indexMax)) {
             int i;
             for (i = indexMax; i > 0; i--)
                 if (position >= mSectionsPositions.get(i))
                     break;
 
             for (; i < mSectionsPositions.size(); i++) {
                 if (position >= mSectionsPositions.get(i))
                     return i;
             }
         }
 
         return 0;
     }
 
     @Override
     public Object[] getSections() {
         return mSections;
     }
 
     private void generateSectionAndIndexes() {
         ArrayList<String> sections = new ArrayList<String>();
         ArrayList<Integer> sectionsPositions = new ArrayList<Integer>();
 
         String lastSection = "";
 
         for (int i = 0; i < sPlugins.length; i++) {
             String firstLetter = sPlugins[i].name.substring(0, 1).toUpperCase();
             if (!firstLetter.equals(lastSection)) {
                 sections.add(firstLetter);
                 sectionsPositions.add(i);
             }
             lastSection = firstLetter;
         }
 
         String[] sectionsArray = new String[sections.size()];
         sections.toArray(sectionsArray);
 
         mSectionsPositions = sectionsPositions;
 
         mSections = sectionsArray;
     }
 
 }
