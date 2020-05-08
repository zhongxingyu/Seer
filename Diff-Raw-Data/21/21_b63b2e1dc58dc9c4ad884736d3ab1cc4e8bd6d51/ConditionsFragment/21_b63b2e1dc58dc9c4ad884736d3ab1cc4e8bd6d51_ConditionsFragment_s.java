 package com.dmtprogramming.pathfindercombat;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.dmtprogramming.pathfindercombat.models.Condition;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.ForeignCollection;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class ConditionsFragment extends FragmentBase {
 	
 	private static final String TAG = "PFCombat";
 	private ConditionArrayAdapter _listAdapter;
 	private ForeignCollection<Condition> conditions;
 	private LayoutInflater inflater;
 	private List<ConditionViewHolder> holders;
 	private List<Condition> checked;
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		this.inflater = inflater;
 		_view = inflater.inflate(R.layout.conditions_fragment, container, false);
 		
 		holders = new ArrayList<ConditionViewHolder>();
 		checked = new ArrayList<Condition>();
 		
 		setupIntentFilter();
 		
         setupView();
         populateStats("");
         
         hideConditionMenu();
 		return _view;
 	}
 	
 	private void hideConditionMenu() {
 		ListView lv = (ListView) findViewById(R.id.listConditions);
 		if (lv != null) {
 			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) lv.getLayoutParams();
 			params.setMargins(0, 0, 0, 0);
 		}
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutConditionsMenu);
 		layout.setVisibility(View.GONE);
 	}
 	
 	private void showConditionMenu() {
 		ListView lv = (ListView) findViewById(R.id.listConditions);
 		if (lv != null) {
 			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) lv.getLayoutParams();
 			params.setMargins(0, 0, 0, 50);
 		}
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutConditionsMenu);
 		layout.setVisibility(View.VISIBLE);	
 	}
 	
 	private void setupView() {
 		// TODO Auto-generated method stub
 		Log.d(TAG, "ConditionsFragment: test");
 		populateList();
 		
 		Button newCondition = (Button) findViewById(R.id.btnNewCondition);
 		newCondition.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				PFCombatApplication app = (PFCombatApplication) getActivity().getApplication();
 				
 				final Dialog dialog = new Dialog(ConditionsFragment.this.getActivity());
 				dialog.setContentView(R.layout.new_condition_dialog);
 				dialog.setTitle("New Condition");
 				dialog.setCancelable(true);
 				
 				EditText durationText = (EditText) dialog.findViewById(R.id.txtRounds);
 				durationText.setText("1");
 				
 				Spinner spinner = (Spinner) dialog.findViewById(R.id.spinConditions);
 				List<String> conditions = app.getSortedConditionNames();
 				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, conditions);
 				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				spinner.setAdapter(adapter); 			
 				
 				Button button = (Button)dialog.findViewById(R.id.btnAdd);
 				button.setOnClickListener(new View.OnClickListener() {	
 					@Override
 					public void onClick(View v) {
 						Spinner spinner = (Spinner) dialog.findViewById(R.id.spinConditions);
 						EditText text = (EditText) dialog.findViewById(R.id.txtRounds);
 						String name = spinner.getSelectedItem().toString();
 						String durationStr = text.getText().toString();
 						long duration = 1;
 						if (!durationStr.equals("")) {
 							duration = Integer.parseInt(durationStr);
 						}
 						Condition cond = new Condition();
 						cond.setName(name);
 						cond.setDuration(duration);
 						cond.setCharacter(getCharacter());
 	            		Dao<Condition, Integer> dao;
 						try {
 							dao = getHelper().getConditionDao();
 							dao.create(cond);
 							updateCharacter("");
 						} catch (SQLException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						populateList();
 						dialog.cancel();
 					}
 				});
 				
 				Button button2 = (Button)dialog.findViewById(R.id.btnCancel);
 				button2.setOnClickListener(new View.OnClickListener() {	
 					@Override
 					public void onClick(View v) {
 						// TODO Auto-generated method stub
 						dialog.cancel();
 					}
 				});
 				
 				dialog.getWindow().setLayout(380, 380);
 				
 				dialog.show();	
 			}
 		});
 		
 		Button nextRound = (Button) findViewById(R.id.btnNextRound);
 		nextRound.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 	            Iterator<Condition> i = conditions.iterator();
 	            while (i.hasNext()) {
 	            	Condition condition = i.next();
 					condition.setDuration(condition.getDuration() - 1);
 					Dao<Condition, Integer> dao;
 					try {
 						dao = getHelper().getConditionDao();
 						if (condition.getDuration() == 0) {
 							dao.delete(condition);
 						} else {
 							dao.update(condition);
 						}
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				
 				populateList();
 				updateCharacter("");
 			}
 		});
 		
 		Button removeConditions = (Button) findViewById(R.id.btnRemoveConditions);
 		removeConditions.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 		        ListView lv = (ListView) findViewById(R.id.listConditions);
 	        	List<Condition> deleters;
 		        if (lv == null) {
 		        	deleters = checked;
 		        } else {
 		        	deleters = _listAdapter.checked();
 		        }
 				for (int i = 0; i < deleters.size(); i++) {
 					Dao<Condition, Integer> dao;
 					try {
 						dao = getHelper().getConditionDao();
 						dao.delete(deleters.get(i));
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 				populateList();
 				hideConditionMenu();
 			}
 		});
 	}
 
 	@Override
 	public void onAfterUpdateCharacter(String field) {
 		populateList();
 	}
 
 	@Override
 	protected void populateStats(String field) {
 	}
 	
 	// hides or shows the condition menu based on check boxes checked
 	private void hideShowConditionMenu(boolean checked, ConditionViewHolder activeHolder) {
 		if (checked) {
 			this.checked.add(activeHolder.condition);
 			Log.d(TAG, "ConditionsFragment: list checkbox added, showing the menu");
 			showConditionMenu();
 		} else {
 			this.checked.remove(activeHolder.condition);
 			if (this.checked.size() == 0) {
 				hideConditionMenu();
 			}
 			Log.d(TAG, "ConditionsFragment: list checkbox removed");
 		}
 	}
 	
 	protected void populateList() {
 		if (_listAdapter != null) {
 			_listAdapter.notifyDataSetInvalidated();
 		}
 
 		holders.clear();
 		checked.clear();
 		
 		_listAdapter = null;
 		
         conditions = getCharacter().getConditions();
         if (conditions != null) { 
         	_listAdapter = new ConditionArrayAdapter(getActivity(), conditions, this);
         }
         
         ListView lv = (ListView) findViewById(R.id.listConditions);
         if (lv == null) {
         	LinearLayout list = (LinearLayout) findViewById(R.id.listConditionsList);
         	list.removeAllViews();
             Iterator<Condition> i = conditions.iterator();
             while (i.hasNext()) {
             	Condition condition = i.next();
         		View vi = inflater.inflate(R.layout.condition_row, null);
 				final ConditionViewHolder holder = new ConditionViewHolder();
 
 				holder.textView = (TextView) vi.findViewById(R.id.rowTextView);
 				holder.textName = (TextView) vi.findViewById(R.id.rowName);
 				holder.checkBox = (CheckBox) vi.findViewById(R.id.rowCheckBox);
 				holder.description = (TextView) vi.findViewById(R.id.rowTextDescription);
 				holder.row = (RelativeLayout) vi.findViewById(R.id.layoutConditionRow);
 				holder.condition = condition;
 				
 				holder.checkBox.setText("");
 				holder.textView.setText("(" + condition.getDuration() + ")");
 				PFCombatApplication app = (PFCombatApplication) getActivity().getApplication();
 				holder.description.setText(Html.fromHtml(app.getConditionShortDescription(condition.getName())));
 				holder.textName.setText(Html.fromHtml("<u>" + condition.getName() + "</u>"));
         		
 				holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						hideShowConditionMenu(isChecked, holder);
 					}
 					
 				});
 				
 				holder.row.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						// TODO Auto-generated method stub
 						if (holder.description.getVisibility() == View.GONE) {
 							holder.description.setVisibility(View.VISIBLE);
 						} else {
 							holder.description.setVisibility(View.GONE);
 						}
 					}
 				});
 				
 				list.addView(vi);
         	}
         } else {
         	if (_listAdapter != null) {
         		lv.setAdapter(_listAdapter);
     	        lv.forceLayout();
     	        _listAdapter.notifyDataSetChanged();
         	}
         }
         if (conditions != null) {
 					Log.d(TAG, "ConditionsFragment: populating list, count = " + conditions.size());
 	        Iterator<Condition> i = conditions.iterator();
 	        while (i.hasNext()) {
 	        	Condition cond = i.next();
 						Log.d(TAG, "ConditionsFragment:      id = " + cond.getId());
 	        }
         }
 	}
 	
     @Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 	
 	private static class ConditionViewHolder {
 		TextView textView;
 		TextView textName;
 		CheckBox checkBox;
 		Condition condition;
 		TextView description;
 		RelativeLayout row;
 	}
 	
 	private static class ConditionArrayAdapter extends ArrayAdapter<Condition> {
 		private static final String TAG = "PFCombat:ConditionArrayAdapter";
 		private LayoutInflater inflater;
 		private ConditionsFragment fragment;
 		private boolean menuShown;
 		private List<ConditionViewHolder> holders;
 		private List<Condition> checked;
 		
 		public ConditionArrayAdapter(Context context, ForeignCollection<Condition> conditions, ConditionsFragment frag) {
 			super(context, R.layout.condition_row, R.id.rowTextView, conditions.toArray(new Condition[0]));
 			holders = new ArrayList<ConditionViewHolder>();
 			checked = new ArrayList<Condition>();
 			fragment = frag;;
 			inflater = LayoutInflater.from(context);
 			menuShown = false;
 		}
 		
 		public List<Condition> checked() {
 			return this.checked;
 		}
 		
 		// hides or shows the condition menu based on check boxes checked
 		public void hideShowConditionMenu(boolean checked, ConditionViewHolder activeHolder) {
 			if (checked) {
 				this.checked.add(activeHolder.condition);
 				Log.d(TAG, "checkbox added, showing the menu");
 				if (!menuShown) {
 					fragment.showConditionMenu();
 				}
 			} else {
 				this.checked.remove(activeHolder.condition);
 				Log.d(TAG, "checkbox removed, test to see if we hide the menu");
 				boolean otherChecked = false;
 				for (int i = 0; i < holders.size(); i++) {
 					ConditionViewHolder holder = holders.get(i);
 					if (holder != activeHolder && holder.checkBox.isChecked()) {
 						otherChecked = true;
 					}
 				}
 				if (!otherChecked) {
 					fragment.hideConditionMenu();
 				}
 			}
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Condition condition = (Condition) this.getItem(position);
 			
 			TextView textView;
 			TextView textName;
 			CheckBox checkBox;
 			final TextView description;
 			RelativeLayout row;
 			
 			if (convertView == null) {
 				convertView = inflater.inflate(R.layout.condition_row, null, false);
 				
 				textView = (TextView) convertView.findViewById(R.id.rowTextView);
 				textName = (TextView) convertView.findViewById(R.id.rowName);
 				checkBox = (CheckBox) convertView.findViewById(R.id.rowCheckBox);
 				description = (TextView) convertView.findViewById(R.id.rowTextDescription);
 				row = (RelativeLayout) convertView.findViewById(R.id.layoutConditionRow);
 				
 				final ConditionViewHolder holder = new ConditionViewHolder();
 				holder.textView = textView;
 				holder.checkBox = checkBox;
 				holder.description = description;
 				holder.condition = condition;
 				holder.textName = textName;
 				holder.row = row;
 				
 				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						hideShowConditionMenu(isChecked, holder);
 					}
 					
 				});
 				
 				row.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						// TODO Auto-generated method stub
 						if (description.getVisibility() == View.GONE) {
 							description.setVisibility(View.VISIBLE);
 						} else {
 							description.setVisibility(View.GONE);
 						}
 					}
 				});
 				
 				holders.add(holder);
 				convertView.setTag(holder);
 			} else {
 				ConditionViewHolder viewHolder = (ConditionViewHolder) convertView.getTag();
 				textView = viewHolder.textView;
 				checkBox = viewHolder.checkBox;
 				description = viewHolder.description;
 				textName = viewHolder.textName;
 				row = viewHolder.row;
 			}
 			
 			checkBox.setText("");
 			textView.setText("(" + condition.getDuration() + ")");
 			PFCombatApplication app = (PFCombatApplication) fragment.getActivity().getApplication();
 			description.setText(Html.fromHtml(app.getConditionShortDescription(condition.getName())));
 			textName.setText(Html.fromHtml("<u>" + condition.getName() + "</u>"));
 			return convertView;
 		}
 	}
 }
 
