 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.communicator.fragments.channels;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.SparseBooleanArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.communicator.R;
 import eu.trentorise.smartcampus.communicator.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.communicator.custom.data.CommunicatorHelper;
 import eu.trentorise.smartcampus.communicator.fragments.messages.LabelDialog;
 import eu.trentorise.smartcampus.communicator.model.Action;
 import eu.trentorise.smartcampus.communicator.model.Channel;
 import eu.trentorise.smartcampus.communicator.model.CommunicatorConstants;
 import eu.trentorise.smartcampus.communicator.model.LabelObject;
 import eu.trentorise.smartcampus.communicator.model.Preference;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 public class ChannelFormFragment extends SherlockFragment {
 
 	public static final String ARG_CHANNEL = "channel";
 	private static final String ARG_FEED = "feed";
 	private static final String FD_KEYWORDS = "keywords";
 
 	Channel channel = null;
 	
 	@Override
 	public void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		state.putSerializable(ARG_CHANNEL, channel);
 	}
 
 	public static Bundle prepareArgs(boolean feeds, Channel channel) {
 		Bundle b = new Bundle();
 		b.putSerializable(ARG_CHANNEL, channel);
 		b.putBoolean(ARG_FEED, feeds);
 		return b;
 	}
 
 	private boolean isFeed() {
 		return getArguments() != null && getArguments().getBoolean(ARG_FEED);
 	}
 	
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		if (bundle != null) {
 			channel = (Channel)bundle.getSerializable(ARG_CHANNEL);
 		}
 	}
 
 	private Channel getChannel() {
 		if (channel == null && getArguments() != null) {
 			channel = (Channel) getArguments().getSerializable(ARG_CHANNEL);
 			if (channel != null) channel = channel.copy();
 		}
 		
 		if (channel == null) {
 			channel = new Channel();
 			channel.setFeed(isFeed());
 			channel.setFilterData(new HashMap<String, Object>());
 		}
 		
 		return channel;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.channelform, container, false);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onStart() {
 		super.onStart();
 		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
 		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
 
 		Spinner spinner = (Spinner) getView().findViewById(R.id.channel_sourcetype);
 		final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
 		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(dataAdapter);
 
 		final String[][] sourceTypeLabels = CommunicatorConstants.getFeedLabels(getActivity());
 		String titleType = getResources().getString(isFeed() ? R.string.feed : R.string.channel);
 
 		for (String[] f : sourceTypeLabels) {
 			((ArrayAdapter<String>)spinner.getAdapter()).add(f[1]);
 		}
 
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 				getChannel().setSourceType(sourceTypeLabels[position][0]); 
 				EditText etTitle = (EditText) getView().findViewById(R.id.channel_name_et);
 				etTitle.setText(getChannel().getSourceType());
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {}
 		});
 		
 		if (getChannel().getId() != null) {
 			// title
 			TextView tv = (TextView) this.getView().findViewById(R.id.channel_name);
 			tv.setText(getChannel().getTitle());
 			
 			// source type
 			for (int i = 0; i < sourceTypeLabels.length; i++){
 				if (getChannel().getSourceType().equals(sourceTypeLabels[i][0])) {
 					spinner.setSelection(i);
 				}
 			}
 			//keywords
 			tv = (TextView) this.getView().findViewById(R.id.channel_keywords_et);
 			tv.setText((String)getChannel().getFilterData().get(FD_KEYWORDS));
 			// labels
 			updateLabels();
 			getSherlockActivity().setTitle("Edit "+titleType+": "+getChannel().getTitle());
 		} else {
 			getSherlockActivity().setTitle("New "+titleType);
 		}
 		
 		((Button)getView().findViewById(R.id.channel_cancel)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getSherlockActivity().getSupportFragmentManager().popBackStack();
 			}
 		}); 
 		
 		((Button)getView().findViewById(R.id.channel_ok)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				saveChannel();
 			}
 		}); 
 
 		((ImageButton)getView().findViewById(R.id.channel_add_labels)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				createLabelsDialog(getChannel());
 			}
 		});
 		
 		// destinations
 		setUpDestinations(getChannel());
 		
 //		// filters
 //		setUpFilters(getChannel());
 	}
 
 //	private void setUpFilters(final Channel channel) {
 //		ImageButton addFilterButton = (ImageButton)getView().findViewById(R.id.channel_add_filters);
 //		final ChannelFilterRenderer renderer = CommunicatorConstants.getChannelTypeRenderer(getActivity(), channel.getSourceType());
 //		final int layoutId = CommunicatorConstants.getChannelTypeLayout(getActivity(), channel.getSourceType());
 //		if (renderer != null && layoutId > 0) {
 //			updateFilters(renderer.getFilterDataDescription(channel.getFilterData()));
 //			addFilterButton.setEnabled(true);
 //			addFilterButton.setOnClickListener(new View.OnClickListener() {
 //				@Override
 //				public void onClick(View v) {
 //					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 //					builder.setInverseBackgroundForced(true);
 //					final View layout = getActivity().getLayoutInflater().inflate(layoutId, null);
 //					builder.setView(layout);
 //					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 //						@Override
 //						public void onClick(DialogInterface dialog, int which) {
 //							dialog.cancel();
 //						}
 //					});
 //					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 //						
 //						@Override
 //						public void onClick(DialogInterface dialog, int which) {
 //							Map<String,Object> data = renderer.validate(layout);
 //							if (data != null) {
 //								channel.setFilterData(data);
 //								updateFilters(renderer.getFilterDataDescription(data));
 //								dialog.dismiss();
 //							}
 //						}
 //					});
 //					AlertDialog dlg = builder.create();
 //					renderer.setUpView(dlg, getActivity(), layout, channel.getFilterData());
 //				}
 //			});
 //		} else {
 //			updateFilters("");
 //			addFilterButton.setEnabled(false);
 //		}
 //	}
 
 	private void setUpDestinations(Channel channel) {
 		Preference prefs = CommunicatorHelper.getPreferences();
 		ImageButton addDestButton = (ImageButton)getView().findViewById(R.id.channel_add_destinations);
 		
 		final LinearLayout layout = (LinearLayout)getView().findViewById(R.id.channel_fields); 
 		int idx = layout.indexOfChild(addDestButton);
 		int startIdx = layout.indexOfChild(getView().findViewById(R.id.channel_destinations));
 		if (idx - startIdx > 1) return;
 		
 		List<Action> defaultActions = CommunicatorConstants.getDefaultChannelActions(getActivity());
 		HashSet<String> added = new HashSet<String>();
 		for (Action a : defaultActions) {
 			CheckBox checkbox = new CheckBox(getActivity());
 			checkbox.setTag(a);
 			checkbox.setText(a.getLabel());
 			added.add(a.getLabel());
 			checkbox.setTextColor(getResources().getColor(R.color.sc_dark_gray));
 			if (channel != null && channel.asActionMap().containsKey(a.getLabel())) {
 				checkbox.setChecked(true);
 			}
 			layout.addView(checkbox,idx++);
 		}
 		
 		if (prefs != null && prefs.getActions() != null && prefs.getActions().size() > 0) {
 			for (Action a : prefs.getActions()) {
 				added.add(a.getLabel());
 				CheckBox checkbox = new CheckBox(getActivity());
 				checkbox.setTag(a);
 				checkbox.setText(a.getLabel());
 				checkbox.setTextColor(getResources().getColor(R.color.sc_dark_gray));
 				if (channel != null && channel.asActionMap().containsKey(a.getLabel())) {
 					checkbox.setChecked(true);
 				}
 				layout.addView(checkbox,idx++);
 			}
 		}
 		if (channel != null && channel.getActions() != null && channel.getActions().size() > 0) {
 			for (Action a : channel.getActions()) {
 				if (added.contains(a.getLabel())) continue;
 				
 				CheckBox checkbox = new CheckBox(getActivity());
 				checkbox.setTag(a);
 				checkbox.setText(a.getLabel());
 				checkbox.setTextColor(getResources().getColor(R.color.sc_dark_gray));
 				if (channel != null && channel.asActionMap().containsKey(a.getLabel())) {
 					checkbox.setChecked(true);
 				}
 				layout.addView(checkbox,idx++);
 			}
 		}
 		
 		addDestButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				int newIdx = layout.indexOfChild(v);
 				final View nv = getActivity().getLayoutInflater().inflate(R.layout.destination, layout, false);
 				layout.addView(nv,newIdx);
 				((EditText)nv.findViewById(R.id.destination_txt)).setOnFocusChangeListener(new OnFocusChangeListener() {
 					@Override
 					public void onFocusChange(View v, boolean hasFocus) {
 						if (hasFocus) {
 							((CheckBox)nv.findViewById(R.id.destination_chk)).setChecked(true);
 						}
 					}
 				});
 			}
 		});
 	}
 	
 	@Override
 	public void onPause() {
 		updateChannel();
 		super.onPause();
 	}
 
 	private void updateChannel() {
 		// title
 		String title = ((EditText)getView().findViewById(R.id.channel_name_et)).getText().toString();
 		if (title != null && title.trim().length() > 0) {
 			getChannel().setTitle(title.trim());
 		} else {
 			getChannel().setTitle(null);
 		}
 		// keywords stored in filter data
 		EditText keywords = (EditText)getView().findViewById(R.id.channel_keywords_et);
 		if (getChannel().getFilterData()==null) {
 			getChannel().setFilterData(new HashMap<String, Object>());
 		}
 		getChannel().getFilterData().put(FD_KEYWORDS, keywords == null || keywords.getText() == null || keywords.getText().toString().trim().length() == 0 ? null : keywords.getText().toString()); 
 		// channel actions: folders, mails
 		LinearLayout layout = (LinearLayout)getView().findViewById(R.id.channel_fields); 
 		TextView textView = (TextView)getView().findViewById(R.id.channel_destinations);
 		ImageButton addDestButton = (ImageButton)getView().findViewById(R.id.channel_add_destinations);
 		int positionFrom = layout.indexOfChild(textView)+1;
 		int positionTo = layout.indexOfChild(addDestButton);
 		List<Action> actionList = new ArrayList<Action>();
 		List<Action> userDefined = new ArrayList<Action>();
 		for (int i = positionFrom; i < positionTo; i++) {
 			View view = layout.getChildAt(i);
 			CheckBox check = null;
 			if (view.getTag() != null && view.getTag() instanceof Action && view instanceof CheckBox) {
 				check = (CheckBox) view;
 				if (check.isChecked()) actionList.add((Action)view.getTag());
 			} else if (view instanceof LinearLayout) {
 				check = (CheckBox)view.findViewById(R.id.destination_chk);
 				EditText edit = (EditText)view.findViewById(R.id.destination_txt);
 				if (check.isChecked() && edit.getText() != null && edit.getText().toString().trim().length() > 0) {
 					if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edit.getText()).matches()) {
 						Toast.makeText(getActivity(), R.string.email_format, Toast.LENGTH_LONG).show();
 						edit.requestFocus();
 						return;
 					}
 
 					Action a = new Action();
 					a.setLabel(edit.getText().toString().trim());
 					a.setValue(a.getLabel());
 					a.setType(Action.EMAIL_ACTION_TYPE);
 					userDefined.add(a);
 				}
 
 			}
 		}
 		actionList.addAll(userDefined);
 		getChannel().setActions(actionList);
 		getChannel().setFeed(isFeed());
 	}
 	
 	protected void saveChannel() {
 		updateChannel();
 		if (channel.getTitle() == null) {
			Toast.makeText(getActivity(), R.string.channel_name_empty, Toast.LENGTH_LONG).show();
			getView().findViewById(R.id.channel_name_et).requestFocus();
 			return;
 		}
 		new SCAsyncTask<Object, Void, Boolean>(getActivity(), new SaveChannelProcessor(getActivity())).execute(getChannel());
 	}
 
 	protected void updateLabels() {
 		TextView tv = null;
 		LinearLayout ll = (LinearLayout) getView().findViewById(R.id.channel_labels);
 		ll.removeAllViews();
 
 		if (getChannel().getLabelIds() != null && getChannel().getLabelIds().size() > 0) {
 			for (int i = 0; i < getChannel().getLabelIds().size(); i++) {
 				LabelObject lo = CommunicatorHelper.getLabel(getChannel().getLabelIds().get(i));
 				if (lo == null) continue;
 				tv = new TextView(getActivity());
 				tv.setText(lo.getName());
 				tv.setPadding(5, 0, 0, 0);
 				tv.setBackgroundColor(Long.decode(lo.getColor()).intValue());
 				ll.addView(tv);
 			}
 		}
 	}
 
 //	protected void updateFilters(CharSequence data) {
 //		((TextView)getView().findViewById(R.id.channel_filters)).setText(data);
 //	}
 
 	private void createLabelsDialog(final Channel content) {
 		final AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
 		final List<LabelObject> labels = CommunicatorHelper.getLabels();
 		final CharSequence[] items = new CharSequence[labels.size()];
 		final boolean[] checked = new boolean[items.length];
 		for (int i = 0; i < checked.length; i++) {
 			checked[i] = false;
 			items[i] = labels.get(i).getName();
 			if (content != null && content.getLabelIds() != null) {
 				for (String l : content.getLabelIds()) {
 					if (l.equals(labels.get(i).getId())) {
 						checked[i] = true;
 						break;
 					}
 				}
 			}
 		}
 		builder.setTitle(R.string.channel_add_labels_dlg_title);
 		builder.setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 				((AlertDialog) dialog).getListView().setItemChecked(which, isChecked);
 			}
 		});
 		builder.setPositiveButton(android.R.string.ok, null);
 		builder.setNeutralButton(R.string.btn_new_label,  new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(final DialogInterface dialog, int which) {
 				LabelDialog d = new LabelDialog(getActivity(), new LabelDialog.OnLabelCreatedListener() {
 					@Override
 					public void OnLabelCreated(LabelObject label) {
 						getChannel().setLabelIds(Collections.singletonList(label.getId()));
 						updateLabels();
 						dialog.dismiss();
 					}
 				});
 				d.setOwnerActivity(getActivity());
 				d.show();
 			}
 		});
 		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		
 		final AlertDialog dlg = builder.create();
 		dlg.show();
 		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				List<String> list = new ArrayList<String>();
 				SparseBooleanArray array = dlg.getListView()
 						.getCheckedItemPositions();
 				for (int i = 0; i < labels.size(); i++) {
 					if (array.get(i))
 						list.add(labels.get(i).getId());
 				}
 				getChannel().setLabelIds(list);
 				updateLabels();
 				dlg.dismiss();
 			}
 		});
 		
 	}
 
 	private class SaveChannelProcessor extends AbstractAsyncTaskProcessor<Object, Boolean> {
 		public SaveChannelProcessor(Activity activity) {
 			super(activity);
 		}
 		@Override
 		public Boolean performAction(Object... params) throws SecurityException, Exception {
 			return CommunicatorHelper.saveChannel((Channel)params[0]);
 		}
 
 		@Override
 		public void handleResult(Boolean result) {
 			if (result) {
 				getSherlockActivity().getSupportFragmentManager().popBackStack();
 			}
 		}
 		
 	}
 }
