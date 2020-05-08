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
 package eu.trentorise.smartcampus.jp;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.jp.custom.data.BasicItinerary;
 import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourney;
 import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourneyParameters;
 import eu.trentorise.smartcampus.jp.helper.JPHelper;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 public class ViewActivity extends BaseActivity {
 	
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		//setContentView(R.layout.)
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getSupportActionBar().setHomeButtonEnabled(false);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
 		initData();
 	}
 
 	@Override
 	protected void initData() {
 		try {
 			new LoadTask().execute();
 		} catch (Exception e1) {
 			JPHelper.endAppFailure(this, R.string.app_failure_setup);
 		}
 	}
 
 	private class LoadTask extends SCAsyncTask<Void, Void, Object> {
 
 		public LoadTask() {
 			super(ViewActivity.this, new AbstractAsyncTaskProcessor<Void, Object>(ViewActivity.this) {
 
 				@Override
 				public Object performAction(Void... params) throws SecurityException, Exception {
 					String objectId = getIntent().getStringExtra(getString(R.string.view_intent_arg_object_id));
 					if (objectId != null) {
 						return JPHelper.getItineraryObject(objectId);
 					}
 					return null;
 				}
 
 				@Override
 				public void handleResult(Object result) {
 					if (result != null) {
 						SherlockFragment fragment = null;
 						if (result instanceof BasicItinerary) {
 							fragment = MyItineraryFragment.newInstance((BasicItinerary)result);
 						} else {
 				
 							fragment = new MyRecurItineraryFragment();
 							Bundle b = new Bundle();
 							b.putSerializable(MyRecurItineraryFragment.PARAMS, (BasicRecurrentJourney)result);
 							b.putBoolean(MyRecurItineraryFragment.PARAM_EDITING,false );
 
 							fragment.setArguments(b);
 						}
 						FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
 						fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
 						fragmentTransaction.replace(Config.mainlayout, fragment);
 						fragmentTransaction.commit();
 					} else {
 						JPHelper.endAppFailure(ViewActivity.this, R.string.object_not_found);
 					}
 				}
 			});
 		}
 		
 	}
 }
