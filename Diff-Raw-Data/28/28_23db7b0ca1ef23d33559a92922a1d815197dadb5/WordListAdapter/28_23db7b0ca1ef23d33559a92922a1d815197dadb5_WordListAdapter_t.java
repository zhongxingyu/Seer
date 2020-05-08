 package com.ppp.wordplayadvlib.adapters;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import android.content.Context;
 import android.text.SpannableString;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.SectionIndexer;
 import android.widget.TextView;
 
 import com.ppp.wordplayadvlib.R;
 import com.ppp.wordplayadvlib.model.SearchObject;
 import com.ppp.wordplayadvlib.model.WordSortState;
 import com.ppp.wordplayadvlib.utils.Utils;
 
 public class WordListAdapter extends BaseAdapter implements SectionIndexer {
 
 	private ArrayList<String> items;
 	private SearchObject searchObject;
 
 	private LayoutInflater inflater;
 	private HashMap<String, Integer> indexer;
 	private String[] sections = new String[0];
 
 	public WordListAdapter(Context ctx,
 							int rowLayoutId,
 							ArrayList<String> items,
 							SearchObject searchObject)
 	{
 
 		this.items = items;
 		this.searchObject = searchObject;
 
 		this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		if (searchObject.wordSort == WordSortState.WORD_SORT_BY_ALPHA)  {
 
 			// Create a map of first letters to array positions
 			indexer = new HashMap<String, Integer>();
 			for (int i = items.size() - 1; i >= 0; i -= 1)  {
 				String word = items.get(i);
 				String firstChar = (word.charAt(0) + "").toUpperCase();
 				if ((firstChar.charAt(0) < 'A') || (firstChar.charAt(0) > 'Z'))
 					firstChar = "@";
 				indexer.put(firstChar, i);
 			}
 
 			// Now get all of the first letters we found and
 			// create an ordered array for section names
 			Set<String> keys = indexer.keySet();
 			Iterator<String> it = keys.iterator();
 			ArrayList<String> keyList = new ArrayList<String>();
 			while (it.hasNext())
 			    keyList.add(it.next());
 			Collections.sort(keyList);
 			sections = new String[keyList.size()];
 			keyList.toArray(sections);
 
 		}
 
 	}
 
 	@Override
 	public int getCount() { return items.size(); }
 
 	@Override
 	public Object getItem(int position) { return items.get(position); }
 
 	@Override
 	public long getItemId(int position) { return position; }
 
 	@Override
     public View getView(int position, View convertView, ViewGroup parent)
     {
 
		View v = convertView;
 
        if (v == null)
            v = (TextView) inflater.inflate(R.layout.word_list, parent, false);
 
        String word = (String) getItem(position);
         SpannableString ss =
         	Utils.convertToBoardString(word, word, searchObject.boardString, searchObject);
         if (ss != null)  {
        	TextView wordView = (TextView) v.findViewById(R.id.wl_word);
         	if (wordView != null)  {
 //        		Debug.e(ss.toString());
         		wordView.setText(ss);
         	}
         }
 
        return v;
         
     }
 
 	@Override
 	public int getPositionForSection(int section)
 	{
 		if (searchObject.wordSort != WordSortState.WORD_SORT_BY_ALPHA)
 			return 0;
 		if (section < 0)
 			section = 0;
 		else if (section >= sections.length)
 			section = sections.length - 1;
 		String letter = sections[section];
 		return indexer.get(letter);
 	}
 
 	@Override
 	public int getSectionForPosition(int position)
 	{
 		if (searchObject.wordSort != WordSortState.WORD_SORT_BY_ALPHA)
 			return 0;
 		int prevIndex = 0;
 		for (int i = 0; i < sections.length; i += 1)  {
 			if ((position < getPositionForSection(i)) && (position >= prevIndex))  {
 		        prevIndex = i;
 		        break;
 		    }
 		    prevIndex = i;
 		}
 		return prevIndex;
 	}
 
 	@Override
 	public Object[] getSections() { return sections; }
     
 }
