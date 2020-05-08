 package com.quanleimu.adapter;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.util.BXHanzi2Pinyin;
 //import com.quanleimu.widget.BXAlphabetListView.PinnedListViewAdapter;
 import net.sourceforge.pinyin4j.PinyinHelper;
 import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
 import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
 import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
 import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
 import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
 
 public class BXAlphabetSortableAdapter extends BaseAdapter implements Comparator<Object> {
 
 	private final static BXAlphabetSortableAdapter COMPARATOR = new BXAlphabetSortableAdapter();
 	protected Context context;
 
 	public class BXHeader extends Object {
 		public String text;
 
 		@Override
 		public String toString() {
 			return text;
 		}
 	}
 
 	static public class BXPinyinSortItem extends Object {
 		public String pinyin;
 		public String firstChars;
 		public Object obj;
 
 		@Override
 		public String toString() {
 			return obj.toString();
 		}
 	}
 
 	private BXAlphabetSortableAdapter() {
 
 	}
 	
 	public void doFilter(String text){
 		if(backupList == null){
 			backupList = new ArrayList<Object>();
 			backupList.addAll(list);
 		}
 		list.clear();
 		
 		for(int i = 0; i < backupList.size(); ++ i){
 			if(backupList.get(i) instanceof BXHeader){
 				list.add(backupList.get(i));
 				if(i > 0){
 					if(list.size() > 1 && list.get(list.size() - 2) instanceof BXHeader){
 						list.remove(list.size() - 2);
 					}
 				}
 				continue;
 			}
 			if(!(backupList.get(i) instanceof BXPinyinSortItem)) continue;
 			
 			BXPinyinSortItem item = (BXPinyinSortItem)backupList.get(i);
 			if(item.firstChars.contains(text) || item.pinyin.contains(text) || item.obj.toString().contains(text)){
 				list.add(item);
 			}
 			if(list.size() > 0 && list.get(list.size() - 1) instanceof BXHeader){
 				list.remove(list.size() - 1);
 			}
 		}
 		this.notifyDataSetChanged();
 	}
 	
 	protected View getHeaderIfItIs(int index,  View convertView){
 		if(this.getItem(index) instanceof BXHeader){
 			if(convertView != null && convertView.findViewById(R.id.headertext) != null){
 				TextView tv = (TextView)convertView.findViewById(R.id.headertext);
 				tv.setText(this.getItem(index).toString()); 
 				return convertView;
 			}else{
 				LayoutInflater inflater = LayoutInflater.from(context);
 				View v = inflater.inflate(R.layout.alphabetheader, null);
 				TextView tv = (TextView)v.findViewById(R.id.headertext);
 				tv.setText(this.getItem(index).toString()); 
 				return v;
 			}
 		}
 		return null;
 	}
 
 	protected List<Object> list = new ArrayList<Object>();
 	protected List<Object> backupList = null;
 
 	public BXAlphabetSortableAdapter(Context context, List<? extends Object> list, boolean sort) {
 		super();
 		if(list == null) return;
 		this.context = context;
 		if (!sort) {
 			this.list.addAll(list);
 		} else {
 			for (int i = 0; i < list.size(); ++i) {
 				BXPinyinSortItem item = new BXPinyinSortItem();
 				item.pinyin = "";
 				item.firstChars = "";
 				if(list.get(i).toString().equals("全部")){
 					item.pinyin = "#";
 				}
 				else{
 					 HanyuPinyinOutputFormat outFormat = new HanyuPinyinOutputFormat();  
 				        outFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);  
 				        outFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  
 					for(int j = 0; j < list.get(i).toString().length(); ++ j){
 //						String py = String.valueOf(list.get(i).toString().charAt(j));
 						try{
							String[] py = null;
							if(list.get(i).toString().charAt(j) == '长'){
								py = new String[]{"chang"};
							}
							else{
								py = PinyinHelper.toHanyuPinyinStringArray(list.get(i).toString().charAt(j), outFormat);
							}
 	//						Log.d("pinyin", list.get(i).toString() + ": " + py);
 	//						String py = BXHanzi2Pinyin.hanziToPinyin(String.valueOf(list.get(i).toString().charAt(j)));
 							if(py == null || py.length == 0){
 								char tp = list.get(i).toString().charAt(j) >= 'A' && list.get(i).toString().charAt(j) <= 'Z' ? 
 										(char)(list.get(i).toString().charAt(j) + 'a' - 'A') : list.get(i).toString().charAt(j);
 								item.pinyin += tp;//list.get(i).toString().charAt(j);
 								item.firstChars += tp;//list.get(i).toString().charAt(j);
 							}else{
 								item.pinyin += py[0];
 								item.firstChars += py[0].charAt(0);//py.charAt(0);							
 							}
						}catch(Exception e){
 							e.printStackTrace();
							item.pinyin += list.get(i).toString().charAt(j);
							item.firstChars += item.pinyin.charAt(0);
 						}
 					}
 				}
 				item.pinyin = item.pinyin.trim();
 				item.obj = list.get(i);
 				this.list.add(item);
 			}
 			Collections.sort(this.list, COMPARATOR);
 			int index = 0;
 			char prePinyin = 0;
 			int count = list.size();
 			while (true) {
 				BXPinyinSortItem item = (BXPinyinSortItem) this.list.get(index);
 				if (item.pinyin.charAt(0) != prePinyin && item.pinyin.charAt(0) != (char)(prePinyin - ('A' - 'a'))) {
 					prePinyin = item.pinyin.charAt(0);
 					prePinyin = (prePinyin >= 'a' && prePinyin <= 'z') ? (char)(prePinyin + 'A' - 'a') : prePinyin;
 					if((prePinyin >= 'A' && prePinyin <= 'Z') || prePinyin == '#'){
 						BXHeader header = new BXHeader();
 						header.text = String.valueOf(prePinyin);
 						this.list.add(index, header);
 						++count;
 					}
 				}
 				if (++index >= count)
 					break;
 			}
 		}
 	}
 
 	// @Override
 	// public int getPinnedHeaderState(int nextHeaderPos) {
 	// if(nextHeaderPos == -1) return PINNED_HEADER_VISIBLE;
 	// // final int childCount = getChildrenCount(groupPosition);
 	// // if (childPosition == childCount - 1) {
 	// // return PINNED_HEADER_PUSHED_UP;
 	// // } else if (childPosition == -1
 	// // && !BlockListView.this.isGroupExpanded(groupPosition)) {
 	// // return PINNED_HEADER_GONE;
 	// // } else {
 	// // return PINNED_HEADER_VISIBLE;
 	// // }
 	// return 0;
 	// }
 
 //	@Override
 //	public void configurePinnedHeader(View header, int nextIndex, int alpha) {
 //		TextView pinned = (TextView) header;
 //		pinned.setText((String) this.getItem(nextIndex).toString());
 //	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return list.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		// TODO Auto-generated method stub
 		return list.get(arg0);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = LayoutInflater.from(context);
 		View v = null;
 		if (convertView == null) {
 			v = inflater.inflate(R.layout.item_common, null);
 		} else {
 			v = (View) convertView;
 		}
 
 		TextView tvCateName = (TextView) v.findViewById(R.id.tvCateName);
 		tvCateName.setText(list.get(position).toString());
 
 		return v;
 	}
 
 	@Override
 	public int compare(Object lhs, Object rhs) {
 		return ((BXPinyinSortItem) lhs).pinyin
 				.compareTo(((BXPinyinSortItem) rhs).pinyin);
 	}
 }
