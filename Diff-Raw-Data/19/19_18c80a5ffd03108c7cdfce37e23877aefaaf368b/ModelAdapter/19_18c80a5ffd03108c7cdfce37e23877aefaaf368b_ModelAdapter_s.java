 package com.BattleBuilder.adapter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.BattleBuilder.R;
 import com.BattleBuilder.adapter.DamageGridAdapter.DamageGrid;
 import com.BattleBuilder.adapter.FactionAdapter.Faction;
 import com.BattleBuilder.widget.NumberPicker;
 import com.BattleBuilder.widget.NumberPicker.OnChangedListener;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.Resources.NotFoundException;
 import android.database.Cursor;
 import android.database.DataSetObserver;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Handler;
 import android.util.Log;
 import android.util.MonthDisplayHelper;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 
 public class ModelAdapter extends DefaultHandler 
 	implements ListAdapter, NumberPicker.OnChangedListener
 {
 	public interface OnFaUpdateListener{
 		public void updateFAs();
 	}
 	
 	private static final String TAG = "ModelAdapter";
 
 	private Context mContext;
 	private LayoutInflater mInflater;
 	private OnFaUpdateListener mListener;
 	private DamageGridAdapter mDamageGrids;
 
 	/* Buffer post information as we learn it in STATE_IN_ITEM. */
 	private ArrayList<Model> mModels;
 	private HashMap<String, Model> mNameIndex;
 	private Model mModelBuffer;
 	private HashMap<String, int[]> mFactionToModels;
 	private HashMap<String, int[]> mKeysToModels;
 	private HashSet<Model> mIncludedModels;
 	private String mFactionName;
 	private boolean mFactionIsMerc;
 	private int mMaxCasters;
 	private int mMaxPoints;
 	private int mCurrentCasters;
 	private int mCurrentTotems;
 	private int mCurrentPoints;
 	
 	/* Efficiency is the name of the game here... */
 	private int mState;
 	private static final int STATE_IN_MODEL = (1 << 2);
 	
 	public static final int ATTR_NAME=1;
 	public static final int ATTR_POINTS=2;
 	public static final int ATTR_ALLOWANCE=3;
 	public static final int ATTR_DAMAGE=4;
 	public static final int ATTR_UNIT_DAMAGE=5;
 	public static final int ATTR_GRID=6;
 	public static final int ATTR_SPIRAL=7;
 	public static final int ATTR_PAGE=8;
 	public static final int ATTR_PLAYS=9;
 	public static final int ATTR_CONFLICTS=10;
 	public static final int ATTR_ENABLES=11;
 	public static final int ATTR_ENABLED=12;
 	public static final int ATTR_NUM_MODELS=13;
 	public static final int ATTR_MERC=14;
 	public static final int ATTR_KEY=15;
 	
 	public static final int FA_UNLIMITED=-1;
 	public static final int FA_CHARACTER=-2;
 	public static final int FA_TOTEM_1=-3;
 	public static final int FA_TOTEM_2=-4;
 	public static final int FA_CASTER=-5;
 
 	public static final int FA_INDEX_MIN=0;
 	public static final int FA_INDEX_MAX=1;
 	public static final int FA_INDEX_WEAPON=2;
 	
 	private static HashMap<String, Integer> mStateMap;
 	private static HashMap<String, Integer> mAttrMap;
 	private static HashMap<String, Integer> mFaMap;
 
 	static
 	{
 		mStateMap = new HashMap<String, Integer>();
 		mStateMap.put("model", new Integer(STATE_IN_MODEL));
 
 		mAttrMap = new HashMap<String, Integer>();
 		mAttrMap.put("name", new Integer(ATTR_NAME));
 		mAttrMap.put("pc", new Integer(ATTR_POINTS));
 		mAttrMap.put("fa", new Integer(ATTR_ALLOWANCE));
 		mAttrMap.put("damage", new Integer(ATTR_DAMAGE));
 		mAttrMap.put("unit_damage", new Integer(ATTR_UNIT_DAMAGE));
 		mAttrMap.put("grid", new Integer(ATTR_GRID));
 		mAttrMap.put("spiral", new Integer(ATTR_SPIRAL));
 		mAttrMap.put("pn", new Integer(ATTR_PAGE));
 		mAttrMap.put("plays", new Integer(ATTR_PLAYS));
 		mAttrMap.put("conflicts", new Integer(ATTR_CONFLICTS));
 		mAttrMap.put("enables", new Integer(ATTR_ENABLES));
 		mAttrMap.put("enabled", new Integer(ATTR_ENABLED));
 		mAttrMap.put("num", new Integer(ATTR_NUM_MODELS));
 		mAttrMap.put("merc", new Integer(ATTR_MERC));
 		mAttrMap.put("key", new Integer(ATTR_KEY));
 		
 		mFaMap = new HashMap<String, Integer>();
 		mFaMap.put("caster", new Integer(FA_CASTER));
 		mFaMap.put("u", new Integer(FA_UNLIMITED));
 		mFaMap.put("c", new Integer(FA_CHARACTER));
 		mFaMap.put("t", new Integer(FA_TOTEM_1));
 		mFaMap.put("t2", new Integer(FA_TOTEM_2));
 	}
 	
 	private static ModelAdapter mSingle = null;
 	
 	public static ModelAdapter getAdapter(Context context){
 		if( mSingle == null){
 			mSingle = new ModelAdapter(context);
 		}
 		return mSingle;
 	}
 
 	private ModelAdapter(Context context)
 	{
 		super();
 		
 		mModels = new ArrayList<Model>();
 		mNameIndex = new HashMap<String, Model>();
 		mIncludedModels = new HashSet<Model>();
 		
 		mContext = context;
 		mInflater = LayoutInflater.from(context);
 
 		mDamageGrids = DamageGridAdapter.getAdapter(context);
 		
 		mFactionToModels = new HashMap<String, int[]>();
 		Object[] factions= FactionAdapter.getAdapter(context).toArray();
 		for( int i=0; i < factions.length; i++){
 			mFactionToModels.put(((Faction)factions[i]).name, new int[1]);
 		}
 		mKeysToModels = new HashMap<String, int[]>();
 
 		//now setup the sax parser for our static xml data
 		SAXParserFactory spf = SAXParserFactory.newInstance();
 		SAXParser sp;
 		XMLReader xr;
 		try {
 			sp = spf.newSAXParser();
 			xr = sp.getXMLReader();
 			xr.setContentHandler(this);
 			xr.setErrorHandler(this);
 			xr.parse(new InputSource(mContext.getResources().openRawResource(R.raw.models)));
 			replaceStrings();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 
 	public void startElement(String uri, String name, String qName,
 			Attributes attrs)	
 	/* These will only be used during init */
 	{
 		String use_name = name != null ? name : qName;
 		
 		Integer state = mStateMap.get(use_name);
 
 		if (state != null)
 		{
 			mState |= state.intValue();
 
 			if (state.intValue() == STATE_IN_MODEL){
 				mModelBuffer = new Model();
 				int local;
 				for( int i =0; i<attrs.getLength(); i++ ){
 					local = mAttrMap.get(attrs.getLocalName(i));
 					switch(local){
 					case( ATTR_NAME ):
 						String temp = attrs.getValue(i);
 						mModelBuffer.name = temp;
 						mNameIndex.put(temp, mModelBuffer);
 						break;
 					case( ATTR_POINTS ):
 						String[] points = attrs.getValue(i).split(",");
 						for( int j=0; j< points.length; j++){
 							mModelBuffer.cost[j] = Integer.parseInt(points[j]);
 						}
 						break;
 					case( ATTR_NUM_MODELS):
 						String[] models = attrs.getValue(i).split(",");
 						for( int j=0; j< models.length; j++){
 							mModelBuffer.num_models[j] = Integer.parseInt(models[j]);
 						}
 						break;
 					case( ATTR_ALLOWANCE):
 						String fa= attrs.getValue(i);
 						Integer faIndex = mFaMap.get(fa);
 						if( faIndex != null ){
 							mModelBuffer.fa = faIndex;
 						}else{
 							mModelBuffer.fa = Integer.parseInt(fa);
 						}
 						break;
 					case( ATTR_DAMAGE):
 						mModelBuffer.damage = mDamageGrids.getDamage(attrs.getValue(i));
 						break;
 					case( ATTR_UNIT_DAMAGE):
 						mModelBuffer.damage = mDamageGrids.getUnitDamage(attrs.getValue(i), mModelBuffer.num_models);
 						break;
 					case( ATTR_GRID):
 						mModelBuffer.damage = mDamageGrids.getGrid(attrs.getValue(i));
 						break;
 					case( ATTR_SPIRAL):
 						mModelBuffer.damage = mDamageGrids.getSpiral(attrs.getValue(i));
 						break;
 					case( ATTR_PAGE):
 						mModelBuffer.page_num = attrs.getValue(i);
 						break;
 					case( ATTR_PLAYS):
 						String[] facs = attrs.getValue(i).split(",");
 						for( int j = 0; j < facs.length; j++){
 							//count up the occurences of each model per faction
 							mFactionToModels.get(facs[j])[0]++;
 						}
 						mModelBuffer.plays = facs;
 						break;
 					case( ATTR_KEY):
 						String[] keys = attrs.getValue(i).split(",");
 						for( int j = 0; j < keys.length; j++){
 							int[] lKeys = mKeysToModels.get(keys[j]);
 							if( lKeys == null){
 								mKeysToModels.put(keys[j], new int[1]);
 							}
 							mKeysToModels.get(keys[j])[0]++;
 						}
 						mModelBuffer.keys = keys;	
 						break;
 					case( ATTR_CONFLICTS):
 						mModelBuffer.conflicts = attrs.getValue(i).split(",");
 						break;
 					case( ATTR_ENABLES):
 						mModelBuffer.enables = attrs.getValue(i).split(",");
 						break;
 					case( ATTR_ENABLED):
 						if(Boolean.parseBoolean(attrs.getValue(i))){
 							mModelBuffer.enabled = 0;
 						}else{
 							mModelBuffer.enabled = 1;
 						}
 						break;
 					case( ATTR_MERC):
 						mModelBuffer.isMerc = Boolean.parseBoolean(attrs.getValue(i));
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	public void endElement(String uri, String name, String qName)
 	{
 		String use_name = name != null ? name : qName;
 		
 		Integer state = mStateMap.get(use_name);
 
 		if (state != null)
 		{
 			if( state.intValue() == STATE_IN_MODEL ){
 				mModelBuffer.row_id = mModels.size();
 				mModels.add( mModelBuffer );
 //				Log.d(TAG, "Model added: " + mModelBuffer);
 			}
 			mState &= ~(state.intValue());
 		}
 	}
 
 	private void replaceStrings(){
 		//First replace our num count with arrays with a length equal to that count
 		Iterator<String> keyiter = mFactionToModels.keySet().iterator();
 		String key;
 		HashMap<String, Integer> indexes = new HashMap<String, Integer>();
 		while( keyiter.hasNext()){
 			key = keyiter.next();
 			int[] i = mFactionToModels.get(key);
 			mFactionToModels.put(key, new int[i[0]]);
 			indexes.put(key, 0);
 		}
 		
 		//Now go over the whole list of models and put an entry into 
 		//the appropriate faction list
 		for(int i=0; i<mModels.size(); i++){
 			Model model = mModels.get(i);
 			for(int j=0; j < model.plays.length; j++){
 				key = model.plays[j];
 				int index = indexes.get(key);
 				mFactionToModels.get(key)[indexes.put(key, ++index)] = i;
 			}
 		}
 		
 		//Now do the same thing only with keywords
 		keyiter = mKeysToModels.keySet().iterator();
 		indexes.clear();
 		while( keyiter.hasNext()){
 			key = keyiter.next();
 			int[] i = mKeysToModels.get(key);
 			mKeysToModels.put(key, new int[i[0]]);
 			indexes.put(key, 0);
 		}
 		for(int i=0; i<mModels.size(); i++){
 			Model model = mModels.get(i);
 			for(int j=0; j < model.keys.length; j++){
 				key = model.keys[j];
 				int index = indexes.get(key);
 				mKeysToModels.get(key)[indexes.put(key, ++index)] = i;
 			}
 		}
 	}
 	
 	public class Model
 	{		
 		public int row_id;
 		public String name;
 		public int fa;
 		public DamageGrid damage;//negative numbers for actual damage
 		public String page_num;
 		public int enabled = 0;
 		public String[] plays;
 		public String[] conflicts;
 		public String[] enables;
 		public String[] keys = new String[0];
 		public int[] num_models = {1,0,0}; //default 1 model
 		public int[] cost = {0,0,0};
 		public int[] num_used = {0,0,0};
 		public boolean isMerc=false;
 		
 		public Model(){
 			//Needed for replace strings logic otherwise more ifs
 			plays = new String[0];
 			conflicts = new String[0];
 			enables = new String[0];
 		}
 		
 		public String toString(){
 			return name + " fa:" + fa + " dam:" + damage + " page_num:" + 
 				page_num + " models:" + num_models + " cost:" + cost;
 		}
 		
 		public void unInclude(boolean remove){
 			if( remove){
 				mIncludedModels.remove(this);
 			}
 			
 			for(int i=0; i< enables.length; i++){
 				Model m = mNameIndex.get(enables[i]);
 				if( m != null){
 					m.clear(remove);
 					m.enabled++;
 				}else{
 					int [] models = mKeysToModels.get(enables[i]);
 					for( int j=0; j<models.length; j++){
 						m = mModels.get(models[j]);
 						m.clear(remove);
 						m.enabled++;
 					}
 				}
 			}
 
 			for(int i=0; i< conflicts.length; i++){
 				Model m = mNameIndex.get(conflicts[i]);
 				if( m != null){
 					m.enabled--;
 				}else{
 					int [] models = mKeysToModels.get(enables[i]);
 					for( int j=0; j<models.length; j++){
 						mModels.get(models[j]).enabled--;
 					}
 				}
 			}
 		}
 		
 		public void include(boolean remove){
 			mIncludedModels.add(this);
 			
 			for(int i=0; i< enables.length; i++){
 				Model m = mNameIndex.get(enables[i]);
 				if( m != null){
 					m.enabled--;
 				}else{
 					int [] models = mKeysToModels.get(enables[i]);
 					for( int j=0; j<models.length; j++){
 						mModels.get(models[j]).enabled--;
 					}
 				}
 			}
 
 			for(int i=0; i< conflicts.length; i++){
 				Model m = mNameIndex.get(conflicts[i]);
 				if( m != null){
 					m.clear(remove);
 					m.enabled++;
 				}else{
 					int [] models = mKeysToModels.get(conflicts[i]);
 					for( int j=0; j<models.length; j++){
 						m = mModels.get(models[j]);
 						m.clear(remove);
 						m.enabled++;
 					}
 				}
 			}
 
 		}
 		
 		public boolean isEnabled(){
 			return enabled <= 0;
 		}
 		
 		public boolean canPlus(int index){
 			if( getRemainingPoints() < cost[index]){
 				return false;
 			}
 			
 			if( !isEnabled()){
 				return false;
 			}
 			
 			int lCasters;
 			if( isMerc && mFactionIsMerc){
 				lCasters = mMaxCasters -1;
 			}else{
 				lCasters = mMaxCasters;				
 			}
 			
 			int total = getTotal();
 			switch(fa){
 			case FA_CASTER:
 				if( lCasters <= mCurrentCasters ){
 					return false;
 				}
 			case FA_CHARACTER:
 				return total<1;
 			case FA_UNLIMITED:
 				return true;
 			case FA_TOTEM_1:
 				return lCasters > mCurrentTotems;
 			case FA_TOTEM_2:
 				return (lCasters + 1) > mCurrentTotems;
 			default:
 				return lCasters * fa > total;
 			}
 		}
 		
 		public boolean canMinus(int index){
 			if( fa == FA_CASTER && getRemainingPoints() < cost[FA_INDEX_MIN]){
 				return false;
 			}
 			return num_used[index] > 0;
 		}
 		
 		public int getTotal(){
 			return  num_used[FA_INDEX_MIN]+
 			num_used[FA_INDEX_MAX]+
 			num_used[FA_INDEX_WEAPON];
 		}
 		
 		public void clear(boolean remove){
 			if( getTotal() > 0){ //to prevent recursion loops
 				if( fa == FA_CASTER){
 					mCurrentPoints += cost[FA_INDEX_MIN];
 					mCurrentCasters -= 1;
 					num_used[FA_INDEX_MIN] = 0;					
 				}else if(fa== FA_TOTEM_1 || fa==FA_TOTEM_2){
 					mCurrentPoints -= cost[FA_INDEX_MIN] * num_used[FA_INDEX_MIN];
 					mCurrentTotems -= num_used[FA_INDEX_MIN];
 					num_used[FA_INDEX_MIN] = 0;
 				}else{
 					for(int j=0; j< num_used.length; j++){
 						mCurrentPoints -= num_used[j] * cost[j];
 						num_used[j] = 0;
 					}
 				}
 				unInclude(remove);
 			}
 		}
 		
 		public void updateCount(int locationIndex, int newVal, int oldVal, boolean callListener){
 			if(newVal == oldVal){
 				return;
 			}
 			
 			switch(fa){
 				case FA_CASTER:
 					mCurrentCasters += (newVal - oldVal);
 					if( (newVal - oldVal) > 0){
 						mCurrentPoints -= cost[locationIndex];
 					}else{
 						mCurrentPoints += cost[locationIndex];
 					}
 					num_used[locationIndex]=newVal;
 					break;
 				case FA_TOTEM_1:
 				case FA_TOTEM_2:
 					mCurrentTotems += (newVal - oldVal);
 				default:
 					if( (newVal - oldVal) > 0){
 						mCurrentPoints += cost[locationIndex];
 					}else{
 						mCurrentPoints -= cost[locationIndex];
 					}
 					num_used[locationIndex]=newVal;
 			}
 			
 			int total = getTotal();
 			if( total == 0){
 				unInclude(true);
 			}else{
 				include(true);
 			}
 			
 			if( callListener){
 				//callback to ArmyEdit;
 				mListener.updateFAs();
 			}
 
 		}
 	}
 	
 	public void setOnFaUpdateListener( OnFaUpdateListener listener){
     	mListener = listener;
     }
 	
 	public void setFaction(String factionName){
 		if( !factionName.equalsIgnoreCase(mFactionName)){
 			mFactionName = factionName;
 			clearIncluded();
 			mFactionIsMerc = FactionAdapter.getAdapter(mContext).getMerc(factionName);
 		}
 	}
 	
 	public void setPointsLevel(int maxPoints, int maxCasters){
 		if( maxCasters != mMaxCasters || maxPoints != mMaxPoints){
 			mMaxCasters = maxCasters;
 			mMaxPoints = maxPoints;		
 			clearIncluded();
 		}
 	}
 	
 	public int getRemainingPoints(){
 		return  (this.mMaxPoints - this.mCurrentPoints);
 	}
 
 	private void clearIncluded(){
 		Iterator<Model> i = mIncludedModels.iterator();
 		while( i.hasNext() ){
 			i.next().clear(false);
 		}
 		mIncludedModels.clear();
 		this.mCurrentCasters = 0;
 		this.mCurrentPoints = 0;
 		this.mCurrentTotems = 0;
 	}
 	
 	/*
 	 * @param army Takes a comma deliminated list of model names and amounts
 	 * Format: name,#,#,#; and three numbers
 	 */
 	public void setUpArmy(String army){
 		clearIncluded();
 		String[] models = army.split(";");
 		Model m;
 		for( int i=0; i< models.length; i++){
 			String [] byComma= models[i].split(",");
 			m = mNameIndex.get(byComma[0]);
 			for( int j=1; j< byComma.length; j++){
 //				Log.d(TAG, m.toString() + "num: " + byComma[j]);
 				m.updateCount(j-1, Integer.parseInt(byComma[j]), 0, false);
 			}
 		}
 	}
 	
 	/*
 	 * @returns a comma deliminated list of model names and amounts
 	 */	
 	public String packUpArmy(){
 		Iterator<Model> iter= mIncludedModels.iterator();
 		Model m;
 		StringBuilder st = new StringBuilder();
 		while(iter.hasNext()){
 			m = iter.next();
 			st.append(m.name);
 			for(int i=0; i< m.num_used.length; i++){
 				st.append("," + m.num_used[i]);
 			}
 			st.append(";");
 		}
 		return st.toString();
 	}
 	
 	public String getNameAt(int position){
 		return mModels.get(mFactionToModels.get(mFactionName)[position]).name;
 	}
 	
 	public int getCount() {
 		return mFactionToModels.get(mFactionName).length;
 	}
 
 	public Object getItem(int position) {
 		return mModels.get(mFactionToModels.get(mFactionName)[position]);
 	}
 
 	public long getItemId(int position) {
 		return mFactionToModels.get(mFactionName)[position];
 	}
 
 	public int getItemViewType(int position) {
 		return R.layout.model_pick_row;
 	}
 	
 	public Model getItemByName(String name){
 		return mNameIndex.get(name);
 	}
 
 	public static class ViewHolder{
 		public TextView name;
 		public TextView infoMin;
 		public TextView infoMax;
 		public TextView infoWeapon;
 		public NumberPicker faMin;
 		public NumberPicker faMax;
 		public NumberPicker faWeapon;
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 		
 		if( convertView == null){
 			convertView	= mInflater.inflate(R.layout.model_pick_row, null);
 			
 			holder = new ViewHolder();
 			holder.name = (TextView) convertView.findViewById(R.id.model_name);
 			holder.infoMin = (TextView) convertView.findViewById(R.id.model_cost_min);
 			holder.infoMax = (TextView) convertView.findViewById(R.id.model_cost_max);
 			holder.infoWeapon = (TextView) convertView.findViewById(R.id.model_cost_weapon);
 			holder.faMin = (NumberPicker) convertView.findViewById(R.id.model_amount_min);
 			holder.faMin.setOnChangedListener(this);
 			holder.faMin.setLocationIndex(FA_INDEX_MIN);
 			holder.faMax = (NumberPicker) convertView.findViewById(R.id.model_amount_max);
 			holder.faMax.setOnChangedListener(this);
 			holder.faMax.setLocationIndex(FA_INDEX_MAX);
 			holder.faWeapon = (NumberPicker) convertView.findViewById(R.id.model_amount_weapon);
 			holder.faWeapon.setOnChangedListener(this);
 			holder.faWeapon.setLocationIndex(FA_INDEX_WEAPON);
 			
 			convertView.setTag(holder);	
 		}else{
 			holder = (ViewHolder) convertView.getTag();
 		}
 		
 		Model current = (Model)getItem(position);
 		holder.name.setText(current.name);
 		
 		holder.faMin.setDataIndex(position);
 		holder.faMin.setCountNoUpdate(current.num_used[FA_INDEX_MIN]);
		holder.infoMin.setText(current.cost[0] + "pc");
 		holder.faMin.setEnabled(current.canPlus(FA_INDEX_MIN), current.canMinus(FA_INDEX_MIN));
 		//This means it is a single model
 		if( current.cost[FA_INDEX_MAX] > 0 ){
			holder.infoMin.setText(current.num_models[FA_INDEX_MIN] + "@" + current.cost[FA_INDEX_MIN] + "pc");
			holder.infoMax.setText(current.num_models[FA_INDEX_MAX] + "@" + current.cost[FA_INDEX_MAX] + "pc");
 			holder.faMax.setDataIndex(position);
 			holder.faMax.setCountNoUpdate(current.num_used[FA_INDEX_MAX]);
 			holder.faMax.setEnabled(current.canPlus(FA_INDEX_MAX), current.canMinus(FA_INDEX_MAX));
 			holder.infoMax.setVisibility(View.VISIBLE);
 			holder.faMax.setVisibility(View.VISIBLE);
 
 			if( current.cost[FA_INDEX_WEAPON] > 0){
				holder.infoWeapon.setText(current.num_models[FA_INDEX_WEAPON] + "@" + current.cost[FA_INDEX_WEAPON] + "pc");
 				holder.faWeapon.setDataIndex(position);
 				holder.faWeapon.setCountNoUpdate(current.num_used[FA_INDEX_WEAPON]);
 				holder.faWeapon.setEnabled(current.canPlus(FA_INDEX_WEAPON), current.canMinus(FA_INDEX_WEAPON));
 				holder.infoWeapon.setVisibility(View.VISIBLE);
 				holder.faWeapon.setVisibility(View.VISIBLE);
 			}else{
 				holder.infoWeapon.setVisibility(View.GONE);
 				holder.faWeapon.setVisibility(View.GONE);				
 			}
 			
 		}else{
 			holder.infoMax.setVisibility(View.GONE);
 			holder.infoWeapon.setVisibility(View.GONE);
 			holder.faMax.setVisibility(View.GONE);
 			holder.faWeapon.setVisibility(View.GONE);
 		}
 
 		return convertView;
 	}
 
 	public int getViewTypeCount() {
 		return 1;
 	}
 
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	public boolean isEmpty() {
 		return mModels.isEmpty();
 	}
 
 	public void registerDataSetObserver(DataSetObserver observer) {
 		// TODO Auto-generated method stub
 	}
 
 	public void unregisterDataSetObserver(DataSetObserver observer) {
 		// TODO Auto-generated method stub
 	}
 
 	public boolean areAllItemsEnabled() {
 		return true;
 	}
 
 	public boolean isEnabled(int position) {
 		return true;
 	}
 
 	public void onChanged(NumberPicker picker, int newVal, int oldVal) {
 		Model current = (Model)getItem(picker.getDataIndex());
 		int locationIndex = picker.getLocationIndex();
 		current.updateCount(locationIndex, newVal, oldVal, true);
 	}
 }
