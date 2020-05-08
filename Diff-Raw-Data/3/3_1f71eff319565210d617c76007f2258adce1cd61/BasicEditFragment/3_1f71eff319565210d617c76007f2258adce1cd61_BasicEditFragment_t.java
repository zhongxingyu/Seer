 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq.android;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.github.kanata3249.ffxieq.FFXICharacter;
 import com.github.kanata3249.ffxieq.R;
 
 public class BasicEditFragment extends FFXIEQFragment {
 	private View mView;
 	private boolean mUpdating;
     
     @Override
     public void onStart() {
     	super.onStart();
 
    		FFXICharacter charInfo = getFFXICharacter();
    		View v = getView();
 
         // setup controls
         {
 	        Spinner spin;
 	        AdapterView.OnItemSelectedListener listener;
 	
 	    	listener = new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					switch (arg0.getId()) {
 					case R.id.Race:
 						if (getFFXICharacter().getRace() != arg2) {
 							saveAndUpdateValues();
 						}
 						break;
 					case R.id.Job:
 						if (getFFXICharacter().getJob() != arg2) {
 							saveAndUpdateValues();
 						}
 						break;
 					case R.id.SubJob:
 						if (getFFXICharacter().getSubJob() != arg2) {
 							saveAndUpdateValues();
 						}
 						break;
 					case R.id.AbyssiteOfFurtherance:
 						if (getFFXICharacter().getAbyssiteOfFurtherance() != arg2) {
 							saveAndUpdateValues();
 						}
 						break;
 					case R.id.AbyssiteOfMerit:
 						if (getFFXICharacter().getAbyssiteOfMerit() != arg2) {
 							saveAndUpdateValues();
 						}
 						break;
 					}
 				}
 				public void onNothingSelected(AdapterView<?> arg0) {
 					saveAndUpdateValues();
 				}
 			};
 	    	spin = (Spinner)v.findViewById(R.id.Race);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(listener);
 	    	}
 	    	spin = (Spinner)v.findViewById(R.id.Job);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(listener);
 	    	}
 	    	spin = (Spinner)v.findViewById(R.id.SubJob);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(listener);
 	    	}
 	    	
 	    	spin = (Spinner)v.findViewById(R.id.AbyssiteOfFurtherance);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(listener);
 	    	}
 	    	spin = (Spinner)v.findViewById(R.id.AbyssiteOfMerit);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(listener);
 	    	}
 
 	    	spin = (Spinner)v.findViewById(R.id.AbyssiteOfFurtherance);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(listener);
 	    	}
 	    	spin = (Spinner)v.findViewById(R.id.AbyssiteOfMerit);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(listener);
 	    	}
 	    }
 
         {
 	        EditText et;
 	        OnEditorActionListener listener;
         
 	        listener = new OnEditorActionListener() {
 				public boolean onEditorAction(TextView v, int actionId,
 						KeyEvent event) {
 					saveAndUpdateValues();
 					return false;
 				}
 	        };
 	        et = (EditText)v.findViewById(R.id.JobLevel);
 	        if (et != null) {
 	        	et.setOnEditorActionListener(listener);
 	        }
 	        et = (EditText)v.findViewById(R.id.SubJobLevel);
 	        if (et != null) {
 	        	et.setOnEditorActionListener(listener);
 	        }
         }
         {
         	AtmaSetView as;
         	
         	as = (AtmaSetView)v.findViewById(R.id.Atmas);
         	if (as != null) {
         		as.bindFFXICharacter(charInfo);
             	as.setOnItemClickListener(new OnItemClickListener() {
     				public void onItemClick(AdapterView<?> arg0, View arg1,
     						int arg2, long arg3) {
     					AtmaSelector.startActivity(BasicEditFragment.this, 0, getFFXICharacter(), arg2, ((AtmaSetView)arg0).getItemId(arg2));
     				}
             	});
         	}
         }
         
         {
         	CheckBox cb;
         	
         	cb = (CheckBox)v.findViewById(R.id.InAbyssea);
         	if (cb != null) {
         		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
         		   		getFFXICharacter().setInAbysea(arg1);
         		   		saveAndUpdateValues();
 					}
         		});
         	}
         }
     }
 
     @Override
 	public void onStop() {
 		View v = getView();
 
         // reset listeners
         {
 	        Spinner spin;
 	
 	    	spin = (Spinner)v.findViewById(R.id.Race);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(null);
 	    	}
 	    	spin = (Spinner)v.findViewById(R.id.Job);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(null);
 	    	}
 	    	spin = (Spinner)v.findViewById(R.id.SubJob);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(null);
 	    	}
 	    	spin = (Spinner)v.findViewById(R.id.AbyssiteOfFurtherance);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(null);
 	    	}
 	    	spin = (Spinner)v.findViewById(R.id.AbyssiteOfMerit);
 	    	if (spin != null) {
 				spin.setOnItemSelectedListener(null);
 	    	}
 	    }
 
         {
 	        EditText et;
 	        et = (EditText)v.findViewById(R.id.JobLevel);
 	        if (et != null) {
 	        	et.setOnEditorActionListener(null);
 	        }
 	        et = (EditText)v.findViewById(R.id.SubJobLevel);
 	        if (et != null) {
 	        	et.setOnEditorActionListener(null);
 	        }
         }
         
         {
         	AtmaSetView as;
         	
         	as = (AtmaSetView)v.findViewById(R.id.Atmas);
         	if (as != null) {
             	as.setOnItemClickListener(null);
         	}
         }
         
         {
         	CheckBox cb;
         	
         	cb = (CheckBox)v.findViewById(R.id.InAbyssea);
         	if (cb != null) {
         		cb.setOnCheckedChangeListener(null);
         	}
         }
         
         super.onStop();
 	}
 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View result;
 
         result = mView = inflater.inflate(R.layout.basiceditfragment, container, false);
 
         return result;
     }
     
     @Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	FFXICharacter charInfo = getFFXICharacter();
 		if (resultCode == Activity.RESULT_OK) {
 			if (AtmaSelector.isComeFrom(data)) {
 				int index = AtmaSelector.getIndex(data);
 				long id = AtmaSelector.getAtmaId(data);
 				
 				if (index != -1) {
 					charInfo.setAtma(index, id);
 				}
 		        updateValues();
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
     private void saveAndUpdateValues() {
         Spinner spin;
         EditText edit;
         int v;
         
         FFXICharacter charInfo;
        if (mUpdating)
        	return;
         if (getActivity() == null) {
         	return;
         }
         charInfo = getFFXICharacter();
 
     	spin = (Spinner)mView.findViewById(R.id.Race);
     	if (spin != null) {
     		v = spin.getSelectedItemPosition();
     		charInfo.setRace(v);
     	}
     	spin = (Spinner)mView.findViewById(R.id.Job);
     	if (spin != null) {
     		v = spin.getSelectedItemPosition();
     		charInfo.setJob(v);
     	}
     	spin = (Spinner)mView.findViewById(R.id.SubJob);
     	if (spin != null) {
     		v = spin.getSelectedItemPosition();
     		charInfo.setSubJob(v);
     	}
 
     	edit = (EditText)mView.findViewById(R.id.JobLevel);
     	if (edit != null) {
     		String str = edit.getText().toString();
     		try {
     			v = Integer.valueOf(str);
     		} catch (NumberFormatException e) {
     			v = 0;
     		}
     		charInfo.setJobLevel(v);
     	}
     	edit = (EditText)mView.findViewById(R.id.SubJobLevel);
     	if (edit != null) {
     		String str = edit.getText().toString();
     		try {
     			v = Integer.valueOf(str);
     		} catch (NumberFormatException e) {
     			v = 0;
     		}
     		v = Math.min(v, charInfo.getJobLevel() / 2);
     		charInfo.setSubJobLevel(v);
     	}
     	spin = (Spinner)mView.findViewById(R.id.AbyssiteOfFurtherance);
     	if (spin != null) {
     		v = spin.getSelectedItemPosition();
     		charInfo.setAbyssiteOfFurtherance(v);
     	}
     	spin = (Spinner)mView.findViewById(R.id.AbyssiteOfMerit);
     	if (spin != null) {
     		v = spin.getSelectedItemPosition();
     		charInfo.setAbyssiteOfMerit(v);
     	}
 
     	updateValues();
 
     	if (mListener != null) {
     		mListener.notifyDatasetChanged();
     	}
     }
 
     public void updateValues() {
     	TextView tv;
     	Spinner spin;
         FFXICharacter charInfo = getFFXICharacter();
 
         if (mUpdating)
         	return;
         mUpdating = true;
     	spin = (Spinner)mView.findViewById(R.id.Race);
     	if (spin != null) {
     		spin.setSelection(charInfo.getRace());
     	}
     	spin = (Spinner)mView.findViewById(R.id.Job);
     	if (spin != null) {
     		spin.setSelection(charInfo.getJob());
     	}
     	spin = (Spinner)mView.findViewById(R.id.SubJob);
     	if (spin != null) {
     		spin.setSelection(charInfo.getSubJob());
     	}
     	tv = (TextView)mView.findViewById(R.id.JobLevel);
     	if (tv != null) {
     		tv.setText(((Integer)charInfo.getJobLevel()).toString());
     	}
     	tv = (TextView)mView.findViewById(R.id.SubJobLevel);
     	if (tv != null) {
     		tv.setText(((Integer)charInfo.getSubJobLevel()).toString());
     	}
 
     	CheckBox cb;
     	
     	cb = (CheckBox)mView.findViewById(R.id.InAbyssea);
     	if (cb != null) {
     		cb.setChecked(charInfo.isInAbbysea());
     	}
     		
     	spin = (Spinner)mView.findViewById(R.id.AbyssiteOfFurtherance);
     	if (spin != null) {
     		spin.setSelection(charInfo.getAbyssiteOfFurtherance());
     	}
     	spin = (Spinner)mView.findViewById(R.id.AbyssiteOfMerit);
     	if (spin != null) {
     		spin.setSelection(charInfo.getAbyssiteOfMerit());
     	}
 
     	AtmaSetView as;
     	
     	as = (AtmaSetView)mView.findViewById(R.id.Atmas);
     	if (as != null) {
     		as.bindFFXICharacter(charInfo);
     	}
 
     	mUpdating = false;
     }
 }
