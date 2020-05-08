 package com.hoos.around;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.hoos.around.ImageThreadLoader.ImageLoadedListener;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Fragment;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class ScheduleFragment extends Fragment {
 
 	static int SELECT_IMAGE = 666;
 	
 	private List<Class> ClassList = new ArrayList<Class>();
 	private List<com.hoos.around.Location> LocationList = new ArrayList<com.hoos.around.Location>();
 
 	private TextView detail_header;
 	private TextView detail_time;
 	private Button new_class;
 	private Button new_location;
 	private Button remove_class;
 	private ListView ClassListView;
 	private ImageView locationImage;
 	
 	private ProgressDialog dialog;
 
 	private ScheduleAdapter scheduleAdapter;
 
 	private ImageThreadLoader imageLoader = new ImageThreadLoader();
 
 	private Class selected_class;
 	
 	private String newLocationName = "";
 
 	private class ScheduleAdapter extends ArrayAdapter<Class> {
 
 		Context context;
 
 		public ScheduleAdapter(Context context, int layoutResourceId) {
 			super(context, layoutResourceId);
 			this.context = context;
 		}

 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			final Class tempClass = this.getItem(position);
 			if (convertView == null) {
 				convertView = ((LayoutInflater) context
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
 						.inflate(R.layout.schedule_fragment_classlist, parent,
 								false);
 			}
 			TextView label = (TextView) convertView.findViewById(R.id.name);
 
 			label.setText(tempClass.course_mnem);
 			return (convertView);
 		}
 
 	}
 
 	public void deleteClass(final int classID) {
 		
 	    AlertDialog confirm_dialog = new AlertDialog.Builder(this.getActivity()).create();
 	    confirm_dialog.setTitle("Are you sure?");
 	    confirm_dialog.setMessage("Do you want to remove " + selected_class.course_mnem + " From your schedule?");
 	    confirm_dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface d, int buttonId) {
 	        	
 	        	dialog = ProgressDialog.show(ScheduleFragment.this.getActivity(), "", "Deleting Class...", true);
 	    		RestClient.get("/schedules/delete/" + StaticUserInfo.getUserID() + "/" + classID, null, null, new JsonHttpResponseHandler() {
 	    					@Override
 	    					public void onSuccess(JSONArray classes) {
 	    						//Delete Schedule
 	    						try {
 	    							Schedule new_schedule = RestClient.parse_schedule(classes);
 	    							scheduleAdapter.clear();
 	    							scheduleAdapter.addAll(new_schedule.courses);
 	    							scheduleAdapter.notifyDataSetChanged();
 	    							Log.d("JSON", "MSG RECIEVED");
 	    							dialog.dismiss();
 	    							setDetail(null);
 
 	    						} catch (JSONException e) {
 	    							// TODO Auto-generated catch block
 	    							Log.d("JSON", e.getMessage());
 	    							dialog.dismiss();
 	    						}
 	    					}
 
 	    					@Override
 	    					public void onFailure(Throwable e,String response) {
 	    						Log.d("JSON", response);
 	    						dialog.dismiss();
 	    					}
 
 	    				});
 
 	        }
 	    });
 	    confirm_dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int buttonId) {
 	            
 	        }
 	    });
 	    confirm_dialog.setIcon(android.R.drawable.ic_dialog_alert);
 	    confirm_dialog.show();
 		
 		
 		
 
 	}
 	
 	public String formatDay(Class toFormat) {
 		String toReturn = "";
 		if(toFormat.monday) {toReturn = toReturn + " Monday"; }
 		if(toFormat.tuesday) {toReturn = toReturn + " Tuesday"; }
 		if(toFormat.wednesday) {toReturn = toReturn + " Wednesday"; }
 		if(toFormat.thursday) {toReturn = toReturn + " Thursday"; }
 		if(toFormat.friday) {toReturn = toReturn + " Friday"; }
 		return toReturn;
 	}
 	
 	public void setDetail(final Class class_Detail) {
 		if(class_Detail == null) {
 			selected_class = null;
 			detail_header.setText("");
 			locationImage.setImageBitmap(null);
 			detail_time.setText("<-- Select a Class");
 		} else {
 			selected_class = class_Detail;
 			detail_header.setText(class_Detail.course_mnem);
 			String end_time = "";
 			String start_time = "";
 			SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
 			try {
 				Date start = formatter.parse(class_Detail.course_start);
 				Date end = formatter.parse(class_Detail.course_end);
 				formatter.applyPattern("hh:mm aa");
 				start_time = formatter.format(start);
 				end_time = formatter.format(end);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	
 	        Bitmap cachedImage = null;
 	        try {
 	          cachedImage = imageLoader.loadImage("http://uva-cs4720-spinach.appspot.com/serve/" + class_Detail.location_id, new ImageLoadedListener() {
 	        	  public void imageLoaded(Bitmap imageBitmap) {
 	        		  locationImage.setImageBitmap(imageBitmap);
 	        		  Log.e("IMAGE", "GOOD remote image URL: " + "http://uva-cs4720-spinach.appspot.com/serve/" + class_Detail.location_id);             
 	          	  }
 	          });
 	
 	        } catch (MalformedURLException e) {
 	          Log.e("IMAGE", "Bad remote image URL: " + "http://uva-cs4720-spinach.appspot.com/serve/" + class_Detail.location_id, e);
 	        }
 	
 	        if( cachedImage != null ) {
 	        	locationImage.setImageBitmap(cachedImage);
 	        }
 			
 			
 			detail_time.setText(start_time + " till "+ end_time + " On " + formatDay(class_Detail));
 		}
 	}
 
 	public void addLocation(String name) {
 		if(name != "") {
 			newLocationName = name;
 			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 		    startActivityForResult(intent, SELECT_IMAGE);
 		}
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		  super.onActivityResult(requestCode, resultCode, data);
 		  if (requestCode == SELECT_IMAGE)
 		    if (resultCode == Activity.RESULT_OK) {
 				dialog = ProgressDialog.show(this.getActivity(), "", "Adding Location...", true);
 				try {
 					InputStream stream;
 					stream = this.getActivity().getContentResolver().openInputStream(data.getData());
 					final Bitmap bitmap = BitmapFactory.decodeStream(stream);
 					stream.close();
 		      
 					LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
 					Location current = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 					double latitude = 0;
 					double longitude = 0;
 					if (current != null) {
 						latitude = current.getLatitude();
 						longitude = current.getLongitude();
 					}
 					
 					RestClient.get("locations/add/" + URLEncoder.encode(newLocationName, "UTF8").replace("+", "%20") + "/" + latitude + "/" + longitude, null, null,
 							new JsonHttpResponseHandler() {
 								@Override
 								public void onSuccess(JSONArray locations) {
 									dialog.dismiss();
 									try {
 										JSONObject location = (JSONObject)locations.get(0);
 										
 										int new_ID = location.getJSONObject("Location").getInt("location_id");
 										Log.d("LOC", Integer.toString(new_ID));
 										//Upload the Bitmap with the new_ID.
 										dialog = ProgressDialog.show(ScheduleFragment.this.getActivity(), "", "Uploading Photo...", true);
 								        final HttpClient httpclient = new DefaultHttpClient();
 								        try {
 								            final HttpPost httppost = new HttpPost("http://uva-cs4720-spinach.appspot.com/upload");
 
 								            MultipartEntity reqEntity = new MultipartEntity();
 								            
 								            StringBody new_ID_string = new StringBody(Integer.toString(new_ID));
 								            
 								            ByteArrayOutputStream stream = new ByteArrayOutputStream();
 								            
 								            
 								            Bitmap.createScaledBitmap(bitmap, 300, 300, false).compress(Bitmap.CompressFormat.JPEG, 100, stream);
 								            byte[] byteArray = stream.toByteArray();
 								            
 								            Log.d("BITMAP", Integer.toString(byteArray.length));
 								            
 								            ByteArrayBody image = new ByteArrayBody(byteArray, "filename.jpg");
 								            
 								            reqEntity.addPart("location_name", new_ID_string);
 								            reqEntity.addPart("picture", image);
 
 								            httppost.setEntity(reqEntity);
 
 								            System.out.println("executing request " + httppost.getRequestLine());
 								            
 								            Thread network_thread = new Thread()
 								            {
 								                @Override
 								                public void run() {
 								                    try {
 														HttpResponse response = httpclient.execute(httppost);
 														
 											            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
 											            String sResponse;
 											            StringBuilder s = new StringBuilder();
 											            while ((sResponse = reader.readLine()) != null) {
 											                s = s.append(sResponse);
 											            }
 											            Log.d("POST", "Response: " + s);
 														dialog.dismiss();
 													} catch (ClientProtocolException e) {
 														// TODO Auto-generated catch block
 														e.printStackTrace();
 														dialog.dismiss();
 													} catch (IOException e) {
 														// TODO Auto-generated catch block
 														e.printStackTrace();
 														dialog.dismiss();
 													} finally {
 											            try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignore) {}
 											        }
 								                }
 								            };
 
 								            network_thread.start();
 								          
 
 								        } catch (UnsupportedEncodingException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										}
 										
 										
 										Log.d("JSON", "MSG RECIEVED");
 										dialog.dismiss();
 
 									} catch (JSONException e) {
 										// TODO Auto-generated catch block
 										Log.d("JSON", e.getMessage());
 										dialog.dismiss();
 									}
 								}
 
 								@Override
 								public void onFailure(Throwable e, String response) {
 									Log.d("JSON", response);
 									dialog.dismiss();
 								}
 
 							});
 					
 				} catch(Exception e) {
 					
 				}
 					
 		    } 
 		}
 	
 	public void LoadClasses() {
 		RestClient.get("courses/view", null, null,
 				new JsonHttpResponseHandler() {
 					@Override
 					public void onSuccess(JSONArray classes) {
 						// Populate Class List
 						try {
 
 							for (int x = 0; x < classes.length(); x++) {
 								Class temp = new Class();
 								JSONObject JSONClasses = (JSONObject) classes
 										.get(x);
 								temp.course_id = JSONClasses.getJSONObject("Course").getInt("course_id");
 								temp.course_start = JSONClasses.getJSONObject("Course").getString("course_start");
 								temp.course_end = JSONClasses.getJSONObject("Course").getString("course_end");
 								temp.course_mnem = JSONClasses.getJSONObject("Course").getString("course_mnem");
 								temp.location_id = JSONClasses.getJSONObject("Course").getInt("location_id");
 								temp.monday = JSONClasses.getJSONObject("Course").getBoolean("course_monday");
 								temp.tuesday = JSONClasses.getJSONObject("Course").getBoolean("course_tuesday");
 								temp.wednesday = JSONClasses.getJSONObject("Course").getBoolean("course_wednesday");
 								temp.thursday = JSONClasses.getJSONObject("Course").getBoolean("course_thursday");
 								temp.friday = JSONClasses.getJSONObject("Course").getBoolean("course_friday");
 								ClassList.add(temp);
 							}
 
 							// MAKE BUTTON VISISLBE
 							new_class.setClickable(true);
 
 							Log.d("JSON", "MSG RECIEVED");
 
 						} catch (JSONException e) {
 							// TODO Auto-generated catch block
 							Log.d("JSON", e.getMessage());
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable e, String response) {
 						Log.d("JSON", response);
 						Log.d("JSON",
 								RestClient.getAbsoluteUrl("courses/view/"));
 					}
 
 				});
 	}
 	
 	public void LoadLocations() {
 		RestClient.get("locations/view", null, null,
 				new JsonHttpResponseHandler() {
 					@Override
 					public void onSuccess(JSONArray locations) {
 						// Populate Location List
 						try {
 
 							for (int x = 0; x < locations.length(); x++) {
 								com.hoos.around.Location temp = new com.hoos.around.Location();
 								JSONObject JSONLocations = (JSONObject) locations.get(x);
 								temp.location_id = JSONLocations.getJSONObject("Location").getInt("location_id");
 								temp.location_name = JSONLocations.getJSONObject("Location").getString("location_name");
 								temp.location_lat = JSONLocations.getJSONObject("Location").getDouble("location_lat");
 								temp.location_long = JSONLocations.getJSONObject("Location").getDouble("location_long");
 								LocationList.add(temp);
 							}
 
 							Log.d("JSON", "LOCATION MSG RECIEVED");
 
 						} catch (JSONException e) {
 							// TODO Auto-generated catch block
 							Log.d("JSON", e.getMessage());
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable e, String response) {
 						Log.d("JSON", response);
 						Log.d("JSON", RestClient.getAbsoluteUrl("locations/view/"));
 					}
 
 				});
 	}
 
 	public void LoadSchedule(User user) {
 		dialog = ProgressDialog.show(this.getActivity(), "", 
                 "Loading Your Schedule...", true);
 		System.out.println("id "+user.user_id);
 		RestClient.get("schedules/id/" + user.user_id, null, null,
 				new JsonHttpResponseHandler() {
 					@Override
 					public void onSuccess(JSONArray classes) {
 						System.out.println(classes.toString());
 						// Grab A Schedule
 						try {
 							Schedule new_schedule = RestClient
 									.parse_schedule(classes);
 							for (int i=0; i<new_schedule.courses.size(); i++) System.out.println(new_schedule.courses.get(i));
 							scheduleAdapter.clear(); 
 							scheduleAdapter.addAll(new_schedule.courses);
 							scheduleAdapter.notifyDataSetChanged();
 							Log.d("JSON", "MSG RECIEVED");
 							dialog.dismiss();
 
 						} catch (JSONException e) {
 							// TODO Auto-generated catch block
 							Log.d("JSON", e.getMessage());
 							dialog.dismiss();
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable e, String response) {
 						Log.d("JSON", response);
 						Log.d("JSON",
 								RestClient.getAbsoluteUrl("courses/view/"));
 						dialog.dismiss();
 					}
 
 				});
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		scheduleAdapter = new ScheduleAdapter(getActivity(),
 				R.layout.friends_fragment_schedulelist);
 		
 		/*
         Toast toast = Toast.makeText(this.getActivity(), "User ID = " + StaticUserInfo.getUserID(), Toast.LENGTH_SHORT);
         toast.show();
 		 */
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if(StaticUserInfo.isLoggedIn()) {
 			View view = inflater.inflate(R.layout.schedule_fragment, container,
 					false);
 	
 			ClassListView = (ListView) view.findViewById(R.id.scheduleList);
 			ClassListView.setAdapter(scheduleAdapter);
 			ClassListView.setOnItemClickListener(new OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> adapter, View view,
 						int position, long arg) {
 					setDetail((Class) adapter.getItemAtPosition(position));
 					adapter.setSelection(position);
 				}
 			});
 	
 			new_location = (Button) view.findViewById(R.id.locationAdd);
 			new_location.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					AlertDialog.Builder editalert = new AlertDialog.Builder(ScheduleFragment.this.getActivity());
 
 					editalert.setTitle("New Location");
 					editalert.setMessage("Type a Name for You Current Location");
 
 
 					final EditText input = new EditText(ScheduleFragment.this.getActivity());
 					editalert.setView(input);
 
 					editalert.setPositiveButton("Take Photo", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							// TODO Auto-generated method stub
 							addLocation(input.getText().toString());
 						}
 					});
 
 
 					editalert.show();
 					
 				}
 			});
 			
 			new_class = (Button) view.findViewById(R.id.scheduleListAdd);
 			new_class.setClickable(false);
 			LoadClasses();
 			LoadLocations();
 			new_class.setOnClickListener(new OnClickListener() {
 	
 				@Override
 				public void onClick(View arg0) {
 	
 					//
 					// SET UP THE DIALOG THAT WILL POPUP WHEN THE ADD BUTTON IS
 					// CLICKED
 					//
 	
 					final Dialog addDialog = new Dialog(arg0.getContext(),
 							R.style.CustomDialogTheme);
 					addDialog.setContentView(R.layout.add_class_dialog);
 	
 					final Spinner class_spinner = (Spinner) addDialog.findViewById(R.id.class_spinner);
 					ArrayAdapter<Class> dataAdapter = new ArrayAdapter<Class>(arg0.getContext(), android.R.layout.simple_spinner_item, ClassList);
 					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					class_spinner.setAdapter(dataAdapter);
 					
 					final Spinner location_spinner = (Spinner)addDialog.findViewById(R.id.location_spinner);
 					ArrayAdapter<com.hoos.around.Location> dataAdapter2 = new ArrayAdapter<com.hoos.around.Location>(arg0.getContext(), android.R.layout.simple_spinner_item, LocationList);
 					dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					location_spinner.setAdapter(dataAdapter2);
 					
 					addDialog.show();
 	
 					//
 					// SET UP THE BUTTON WITHIN THE DIALOG
 					//
 	
 					Button close_btn = (Button) addDialog
 							.findViewById(R.id.submit_button);
 					close_btn.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							addDialog.dismiss();
 							dialog = ProgressDialog.show(ScheduleFragment.this.getActivity(), "", 
 					                "Adding Class...", true);
 							RestClient.get(
 									"schedules/add/"+ StaticUserInfo.getUserID() + "/" + ((Class) class_spinner.getSelectedItem()).course_id, null, null, new JsonHttpResponseHandler() {
 										@Override
 										public void onSuccess(JSONArray classes) {
 											// Grab A Schedule
 											try {
 												Schedule new_schedule = RestClient.parse_schedule(classes);
 												scheduleAdapter.clear();
 												scheduleAdapter.addAll(new_schedule.courses);
 												scheduleAdapter.notifyDataSetChanged();
 												Log.d("JSON", "MSG RECIEVED");
 												dialog.dismiss();
 	
 											} catch (JSONException e) {
 												// TODO Auto-generated catch block
 												Log.d("JSON", e.getMessage());
 												dialog.dismiss();
 											}
 										}
 	
 										@Override
 										public void onFailure(Throwable e,
 												String response) {
 											Log.d("JSON", response);
 											Log.d("JSON",RestClient.getAbsoluteUrl("courses/view/"));
 											dialog.dismiss();
 										}
 	
 									});
 	
 						}
 					});
 					
 					// ADD A NEW CLASS, AND ADD TO SCHEDULE
 					Button close_btn2 = (Button) addDialog.findViewById(R.id.submit_button2);
 					close_btn2.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							//POPULATE WITH ALL THE DATA
 							String mnem = ((EditText)addDialog.findViewById(R.id.class_name)).getText().toString();
 							int locationid = ((com.hoos.around.Location)location_spinner.getSelectedItem()).location_id;
 							TimePicker startTimePicker = (TimePicker)addDialog.findViewById(R.id.start_time);
 							TimePicker endTimePicker = (TimePicker)addDialog.findViewById(R.id.end_time);
 							String start = startTimePicker.getCurrentHour() + ":" + startTimePicker.getCurrentMinute() + ":00";
 							String end = endTimePicker.getCurrentHour() + ":" + endTimePicker.getCurrentMinute() + ":00";
 							String URL = "";
 							try {
 								URL = "courses/add/" + URLEncoder.encode(mnem, "UTF8").replace("+", "%20") + "/" + locationid + "/" + URLEncoder.encode(start, "UTF8") + "/" + URLEncoder.encode(end, "UTF8");
								URL = URL + "/" + ((CheckBox)addDialog.findViewById(R.id.monday)).isChecked() + "/" + ((CheckBox)addDialog.findViewById(R.id.tuesday)).isChecked() + "/" + ((CheckBox)addDialog.findViewById(R.id.wednesday)).isChecked() + "/" + ((CheckBox)addDialog.findViewById(R.id.thursday)).isChecked() + "/" + ((CheckBox)addDialog.findViewById(R.id.friday)).isChecked();
 								URL = URL + "/" + StaticUserInfo.getUserID();
 								Log.d("ADD", URL);
 							} catch (UnsupportedEncodingException e1) {
 								e1.printStackTrace();
 							}
 							addDialog.dismiss();
 							dialog = ProgressDialog.show(ScheduleFragment.this.getActivity(), "", 
 					                "Adding Class...", true);
 							RestClient.get(
 									URL, null, null, new JsonHttpResponseHandler() {
 										@Override
 										public void onSuccess(JSONArray classes) {
 											// Grab A Schedule
 											try {
 												Schedule new_schedule = RestClient.parse_schedule(classes);
 												scheduleAdapter.clear();
 												scheduleAdapter.addAll(new_schedule.courses);
 												scheduleAdapter.notifyDataSetChanged();
 												dialog.dismiss();
 												Log.d("JSON", "MSG RECIEVED");
 	
 											} catch (JSONException e) {
 												// TODO Auto-generated catch block
 												Log.d("JSON", e.getMessage());
 												dialog.dismiss();
 											}
 										}
 	
 										@Override
 										public void onFailure(Throwable e,
 												String response) {
 											Log.d("JSON", response);
 											dialog.dismiss();
 										}
 	
 									});
 	
 						}
 					});
 	
 				}
 			});
 	
 			
 			remove_class = (Button) view.findViewById(R.id.scheduleDetailRemove);
 			remove_class.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					if(selected_class == null || selected_class.course_id == 0) {
 				        Toast toast = Toast.makeText(ScheduleFragment.this.getActivity(), "Select a class!", Toast.LENGTH_SHORT);
 				        toast.show();
 					} else {
 						deleteClass(selected_class.course_id);
 					}
 				}
 			});
 			
 			detail_header = (TextView) view.findViewById(R.id.scheduleDetailHeader);
 			detail_time = (TextView) view.findViewById(R.id.scheduleDetailTime);
 			locationImage = (ImageView) view.findViewById(R.id.location_image);
 
 	
 			detail_time.setText("<-- Select a Class");
 			
 			User temp_user = new User();
 			temp_user.user_id = StaticUserInfo.getUserID();
 	
 			LoadSchedule(temp_user);
 			System.out.println(scheduleAdapter.getCount());
 			for (int i=0; i<scheduleAdapter.getCount(); i++) System.out.println(scheduleAdapter.getItem(i));
 			return view;
 		} else {
 			View view = inflater.inflate(R.layout.error_fragment, container, false);
 			return view;
 		}
 	}
 
 	public void setText(String item) {
 		TextView view = (TextView) getView().findViewById(R.id.header);
 		view.setText(item);
 	}
 
 }
