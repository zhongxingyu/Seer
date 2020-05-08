 package org.jessies.dalvikexplorer;
 
 import android.app.*;
 import android.content.*;
 import android.os.*;
 import android.widget.*;
 import android.view.*;
 import java.util.*;
 
 public class LocalesActivity extends ListActivity {
     private static class LocaleListItem {
         private final Locale locale;
         private LocaleListItem(Locale locale) {
             this.locale = locale;
         }
         @Override public String toString() {
             String result = locale.toString();
             if (locale.equals(Locale.getDefault())) {
                 result += " (default)";
             }
             return result;
         }
     }
     private static final List<LocaleListItem> LOCALES = gatherLocales();
     
     @Override public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setListAdapter(new ArrayAdapter<LocaleListItem>(this, android.R.layout.simple_list_item_1, LOCALES));
         setTitle("Locales (" + LOCALES.size() + ")");
         getListView().setTextFilterEnabled(true);
     }
     
     @Override protected void onListItemClick(ListView l, View v, int position, long id) {
        final Intent intent = new Intent(this, LocaleCountriesActivity.class);
         final LocaleListItem item = (LocaleListItem) l.getAdapter().getItem(position);
        intent.putExtra("org.jessies.dalvikexplorer.Language", item.locale.toString());
         startActivity(intent);
     }
     
     private static List<LocaleListItem> gatherLocales() {
         final Locale[] availableLocales = Locale.getAvailableLocales();
         final Locale defaultLocale = Locale.getDefault();
         // Put the default locale at the top of the list...
         final ArrayList<LocaleListItem> result = new ArrayList<LocaleListItem>(availableLocales.length);
         result.add(new LocaleListItem(defaultLocale));
         // ...followed by all the distinct languages...
         TreeSet<String> languages = new TreeSet<String>();
         for (Locale locale : availableLocales) {
             languages.add(locale.getLanguage());
         }
         for (String language : languages) {
             result.add(new LocaleListItem(new Locale(language)));
         }
         return result;
     }
     
     static String describeLocales() {
         StringBuilder result = new StringBuilder();
         for (LocaleListItem item : gatherLocales()) {
             if (Thread.currentThread().isInterrupted()) return null;
             result.append(LocaleActivity.describeLocale(item.locale.toString()));
             result.append('\n');
         }
         return result.toString();
     }
 }
