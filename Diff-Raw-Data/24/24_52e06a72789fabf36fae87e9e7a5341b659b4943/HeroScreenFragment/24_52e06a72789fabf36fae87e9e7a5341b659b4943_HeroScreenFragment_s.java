 package net.hath.hondroid;
 
 import net.hath.hondroid.database.DatabaseAdapter;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class HeroScreenFragment extends Fragment {
 
 	private static final String TAG = "HeroFragment";
 	private Hero hero;
 	private DatabaseAdapter da;
 	
 	private ImageView icon;
 	private TextView name;
 	private TextView fac;
 	private TextView atr;
 	private ListView spellist;
 	private Spell[] spells;
 	
 	public HeroScreenFragment() {
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		da = new DatabaseAdapter(getActivity());
 		Bundle args = getArguments();
 		if (args == null && hero == null) {
 			hero = da.getHero(2);
 			return;
 		}
 		hero = da.getHero(args.getInt("heroid"));
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		View view = inflater.inflate(R.layout.hero_view, container, false);
 		if (hero == null) {
 			Log.i(TAG, "Hero is null");
 			return view;
 		}
 		icon = (ImageView) view.findViewById(R.id.hero_icon);
 		name = (TextView) view.findViewById(R.id.hero_heroname);
 		fac = (TextView) view.findViewById(R.id.hero_faction);
 		atr = (TextView) view.findViewById(R.id.hero_attribute);
 		spellist = (ListView) view.findViewById(R.id.spellist);
 		spellist.setClickable(false);
 		if(hero!=null){
 			updateView(hero);
 		}
 		return view;
 	}
 
 	public void updateView(Hero hero) {
 		this.hero = hero;
 		icon.setImageBitmap(hero.getIcon(getActivity()));
 		name.setText(hero.getName());
 		fac.setText(hero.getFaction().toString());
 		fac.setTextColor(hero.getFaction().getColor());
 		atr.setText(hero.getAttribute().toString());
 		atr.setTextColor(hero.getAttribute().getColor());
 		spells = da.getSpells(hero.getId());
 		spellist.setAdapter(new SpellListAdapter(spells));
 		
 	}
 
 	private class SpellListAdapter extends BaseAdapter {
 		private Spell[] spells;
 
 		public SpellListAdapter(Spell[] spells) {
 			this.spells = spells;
 		}
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return 4;
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			View view = convertView;
 			if (view == null) {
 				LayoutInflater la = getLayoutInflater(getArguments());
 				view = la.inflate(R.layout.spell_row, null);
 			}
 			Spell spell = spells[position];
 
 			if (spell == null) {
 				Log.w(TAG, "Spell " + hero.getName() + "_" + (position + 1)
 						+ " is null");
 				return view;
 			}
 
 			ImageView icon = (ImageView) view.findViewById(R.id.spell_icon);
 			icon.setImageBitmap(hero.getSpellIcon(getActivity(), position + 1));
 			TextView text_title = (TextView) view
 					.findViewById(R.id.spell_title);
 			text_title.setText(spell.getName());
 			TextView text_desc = (TextView) view.findViewById(R.id.spell_text);
 			text_desc.setText(spell.getDesc());
 
 			ListView.LayoutParams params = new ListView.LayoutParams(
 					ListView.LayoutParams.MATCH_PARENT,
 					ListView.LayoutParams.WRAP_CONTENT);
 			view.setLayoutParams(params);
 
 			return view;
 		}
 
 	}
 }
