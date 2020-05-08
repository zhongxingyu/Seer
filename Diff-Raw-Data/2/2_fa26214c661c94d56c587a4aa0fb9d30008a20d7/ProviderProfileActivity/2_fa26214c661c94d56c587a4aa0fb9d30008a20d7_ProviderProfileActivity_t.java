 package edu.upenn.cis350;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import edu.upenn.cis350.entities.Provider;
 import edu.upenn.cis350.entities.Rating;
 import edu.upenn.cis350.util.InternetHelper;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.CheckBox;
 
 /**
  * Contains information about a provider
  * @author DXU
  *
  */
 public class ProviderProfileActivity extends Activity{
 
 	private static final String BASE_URL="https://fling.seas.upenn.edu/~xieyuhui/cgi-bin/ratings.php?mode=view&pid=";
 	private TextView m_provider_name;
 	private TextView m_provider_phone;
 	private TextView m_provider_address;
 	private TextView m_provider_rating;
 
 	private Dialog dialog;
 	private Dialog dialog2;
 
 	private EditText reviewText;
 	private EditText reviewSummaryText;
 	private Button reviewButton;
 	private RatingBar ratingbar;
 	private RatingBar rating_friendliness_bar;
 	private RatingBar rating_communication_bar;
 	private RatingBar rating_environment_bar;
 	//new features!!!!!
 	private RatingBar rating_professionalSkills_bar;
 	private RatingBar rating_costs_bar;
 	private RatingBar rating_availability_bar;
 
 	private RatingBar avg_rating_overall_bar;
 	private RatingBar avg_rating_friendliness_bar;
 	private RatingBar avg_rating_communication_bar;
 	private RatingBar avg_rating_environment_bar;
 	private RatingBar avg_rating_professionalskills_bar;
 	private RatingBar avg_rating_costs_bar;
 	private RatingBar avg_rating_availability_bar;
 
 	private Button m_button_map;
 	private Button m_button_review;
 	private Button m_button_avgfeature;
 	private final Context m_context = this;
 	private ArrayList<Rating> m_ratings;
 	private Provider m_provider;
 	private ListView m_comments;
 	private ImageView m_provider_star_rating;
 	private Button parking;
 	private Button creditcard;
 	private Button accepting;
 	private Button appointment; 
 	private Button PCP; 
 	private Button specialist;
 
 	//add for pros and cons
 	private Button m_button_pros;
 	private Button m_button_cons;
 	private Dialog dialog_pros;
 	private Dialog dialog_cons;
 	private CheckBox checkBox_pros1;
 	private CheckBox checkBox_pros2;
 	private CheckBox checkBox_pros3;
 	private CheckBox checkBox_pros4;
 	private CheckBox checkBox_pros5;
 	private CheckBox checkBox_cons1;
 	private CheckBox checkBox_cons2;
 	private CheckBox checkBox_cons3;
 	private CheckBox checkBox_cons4;
 	private CheckBox checkBox_cons5;
 	private Button button_pros_ok;
 	private Button button_cons_ok;
 
 
 	private RatingAdapter m_adapter;
 
 	/**
 	 * Create each provider's profile
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.provider_pf);
 		//layout elements
 		m_button_map = (Button)this.findViewById(R.id.button_providerpf_map);
 		m_button_review = (Button)this.findViewById(R.id.providerpf_rate_button);
 		m_comments = (ListView)this.findViewById(R.id.providerpf_comments);
 		m_adapter = new RatingAdapter(m_context);
 		m_comments.setAdapter(m_adapter);
 		m_button_avgfeature=(Button)this.findViewById(R.id.providerpf_all_previous_reviews_button);
 
 
 
 		//provider metadata
 		m_provider_name = (TextView)this.findViewById(R.id.provider_name);
 		m_provider_phone = (TextView)this.findViewById(R.id.provider_phone);
 		m_provider_address = (TextView)this.findViewById(R.id.provider_address);
 		m_provider_rating = (TextView)this.findViewById(R.id.providerpf_average_rating_text);
 		m_provider_star_rating = (ImageView) this
 				.findViewById(R.id.providerpf_average_stars);
 
 		//Initialize the icons of "has parking", "appointment only", etc
 		this.initializeIcons();	
 	}
 
 	/**
 	 * Set up icons for attributes. Add a listener to each button so that a
 	 * toast is shown to explain what the icon means once being clicked
 	 */
 	private void initializeIcons() {
 
 		parking = (Button) this.findViewById(R.id.provider_parking);
 		this.addDescriptionToast(parking, "The provider has parking");
 		creditcard = (Button) this.findViewById(R.id.provider_creditcard);
 		this.addDescriptionToast(creditcard, "The provider accepts credit card");
 		accepting = (Button) this.findViewById(R.id.provider_accepting);
 		this.addDescriptionToast(accepting, "This provider accepts new patient");
 		appointment = (Button) this.findViewById(R.id.provider_appointments);
 		this.addDescriptionToast(appointment, "Appointment Only");
 		PCP = (Button) this.findViewById(R.id.provider_PCP);
 		this.addDescriptionToast(PCP, "Primary Care Provider");
 		specialist = (Button) this.findViewById(R.id.provider_specialist);
 		this.addDescriptionToast(specialist, "Specialist");
 
 	}
 
 	/**
 	 * add a listen to the icon button so that a description is displayed when
 	 * clicked
 	 * 
 	 * @param icon
 	 * @param description
 	 */
 	private void addDescriptionToast(Button icon, final String description) {
 		icon.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Context context = getApplicationContext();
 				Toast toast = Toast.makeText(context, description,Toast.LENGTH_SHORT);
 				toast.show();
 			}
 		});
 
 	}
 
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// get existing Intent data (the ID of provider to be looked at), and
 		// initialize a list of ratings to be populated for the provider.
 		m_provider = (Provider) getIntent().getSerializableExtra("providers");
 		m_ratings = new ArrayList<Rating>();
 
 		//populate ratings, for RatingAdapter
 		populateRatings();
 
 		// initialize buttons, map, review, passing on the now-initialized provider
 		m_button_map.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (v == m_button_map) {
 
 				}
 				Intent intent = new Intent(m_context, MapProviderActivity.class);
 				intent.putExtra("providers", m_provider);
 				startActivity(intent);
 			}
 		});
 
 		m_button_avgfeature.setOnClickListener(new OnClickListener(){
 			//			public void onCreate(){
 			//				
 			//			}
 
 			// add feature button
 			public void onClick(View v) {
 				dialog2=new Dialog(m_context);
 				dialog2.setContentView(R.layout.newaddbutton);
 				dialog2.setTitle("Average Feature Rating");
 
 				Integer overallAvg = (int) m_provider.getAverageRating();
 				avg_rating_overall_bar = (RatingBar) dialog2.findViewById(R.id.average_overall_bar);
 				avg_rating_overall_bar.setRating(overallAvg);
 
 				Integer fAvg = (int) m_provider.getAverage_friendliness_rating();
 				avg_rating_friendliness_bar = (RatingBar) dialog2.findViewById(R.id.average_friendliness_bar);
 				avg_rating_friendliness_bar.setRating(fAvg);
 
 				Integer comAvg = (int) m_provider.getAverage_communication_rating();
 				avg_rating_communication_bar = (RatingBar) dialog2.findViewById(R.id.average_communication_bar);
 				avg_rating_communication_bar.setRating(comAvg);
 
 				Integer environAvg = (int) m_provider.getAverage_environment_rating();
 				avg_rating_environment_bar = (RatingBar) dialog2.findViewById(R.id.average_environment_bar);
 				avg_rating_environment_bar.setRating(environAvg);
 
 				Integer proAvg = (int) m_provider.getAverage_professional_rating();
 				avg_rating_professionalskills_bar = (RatingBar) dialog2.findViewById(R.id.average_professionalskills_bar);
 				avg_rating_professionalskills_bar.setRating(proAvg);
 
 				Integer costAvg = (int) m_provider.getAverage_costs_rating();
 				avg_rating_costs_bar = (RatingBar) dialog2.findViewById(R.id.average_costs_bar);
 				avg_rating_costs_bar.setRating(costAvg);
 
 				Integer avaiAvg = (int) m_provider.getAverage_availability_rating();
 				avg_rating_availability_bar = (RatingBar) dialog2.findViewById(R.id.average_availability_bar);
 				avg_rating_availability_bar.setRating(avaiAvg);
 
 				dialog2.show();
 
 			}});
 
 
 
 		//review dialog pops up.
 		m_button_review.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				dialog = new Dialog(m_context);
 
 				dialog.setContentView(R.layout.provider_pf_rate);
 				dialog.setTitle("Rate and Review this Provider!");
 
 				//				//add for pros and cons
 				m_button_pros = (Button)dialog.findViewById(R.id.button_pros);
 				m_button_cons = (Button)dialog.findViewById(R.id.button_cons);
 
 
 				//add for pros and cons
 				//pros checkbox pops up
 				m_button_pros.setOnClickListener(new OnClickListener() {
 					int countCheck = 0;
 
 					public void onClick(View v) {
 
 						dialog_pros = new Dialog(m_context);
 						dialog_pros.setContentView(R.layout.best_checkbox);
 						dialog_pros.setTitle("Best thing about this provider ");
 
 						//checkbox
 						checkBox_pros1 = (CheckBox)dialog_pros.findViewById(R.id.pros_checkbox1);
 						checkBox_pros2 = (CheckBox)dialog_pros.findViewById(R.id.pros_checkbox2);
 						checkBox_pros3 = (CheckBox)dialog_pros.findViewById(R.id.pros_checkbox3);
 						checkBox_pros4 = (CheckBox)dialog_pros.findViewById(R.id.pros_checkbox4);
 						checkBox_pros5 = (CheckBox)dialog_pros.findViewById(R.id.pros_checkbox5);
 						button_pros_ok = (Button)dialog_pros.findViewById(R.id.pros_ok);
 						button_pros_ok.setOnClickListener(new OnClickListener(){
 							public void onClick(View v) {
 								dialog_pros.hide();
 							}
 						});
 
 						checkBox_pros1.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_pros1);
 							}});
 
 						checkBox_pros2.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_pros2);
 							}});
 
 						checkBox_pros3.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_pros3);
 							}});						
 
 						checkBox_pros4.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_pros4);
 							}});
 
 						checkBox_pros5.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_pros5);
 							}});
 
 						dialog_pros.show();
 					}
 
 					public void onCheckboxClicked(View view) {
 						// Is the view now checked?
 						boolean checked = ((CheckBox) view).isChecked();
 
 						if(checked){
 							if(countCheck >= 3){// it will allow 3 checkboxes only
 								Toast.makeText(m_context, "Should not check more than 3!", Toast.LENGTH_LONG).show();
 								((CheckBox) view).setChecked(false);
 							}
 							else countCheck++;
 						}
 						else countCheck--;
 						// Check which checkbox was clicked
 						//					    switch(view.getId()) {
 						//					        case R.id.pros_checkbox1:
 						////					            if (checked) countCheck++;
 						//					            break;
 						//					        case R.id.pros_checkbox2:
 						////					            if (checked) countCheck++;
 						//					            break;
 						//					        // TODO: Veggie sandwich
 						//					    }
 					}
 
 
 				});
 
 				//cons checkbox pops up
 				m_button_cons.setOnClickListener(new OnClickListener() {
 					int countCheck = 0;
 					public void onClick(View v) {
 						dialog_cons = new Dialog(m_context);
 						dialog_cons.setContentView(R.layout.worst_checkbox);
 						dialog_cons.setTitle("Worst thing about this provider");
 						//checkbox
 						checkBox_cons1 = (CheckBox)dialog_cons.findViewById(R.id.cons_checkbox1);
 						checkBox_cons2 = (CheckBox)dialog_cons.findViewById(R.id.cons_checkbox2);
 						checkBox_cons3 = (CheckBox)dialog_cons.findViewById(R.id.cons_checkbox3);
 						checkBox_cons4 = (CheckBox)dialog_cons.findViewById(R.id.cons_checkbox4);
 						checkBox_cons5 = (CheckBox)dialog_cons.findViewById(R.id.cons_checkbox5);
 						button_cons_ok = (Button)dialog_cons.findViewById(R.id.cons_ok);
 
 						button_cons_ok.setOnClickListener(new OnClickListener(){
 							public void onClick(View v) {
 								dialog_cons.hide();
 							}
 						});
 
 						checkBox_cons1.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_cons1);
 							}});
 
 						checkBox_cons2.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_cons2);
 							}});
 
 						checkBox_cons3.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_cons3);
 							}});						
 
 						checkBox_cons4.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_cons4);
 							}});
 
 						checkBox_cons5.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onCheckboxClicked(checkBox_cons5);
 							}});
 
 						dialog_cons.show();
 					}
 
 					public void onCheckboxClicked(View view) {
 						// Is the view now checked?
 						boolean checked = ((CheckBox) view).isChecked();
 
 						if(checked) {
 							if(countCheck >= 3){// it will allow 3 checkboxes only
 								Toast.makeText(m_context, "Should not check more than 3!", Toast.LENGTH_LONG).show();
 								((CheckBox) view).setChecked(false);
 							}
 							else countCheck++;
 						}
 						else countCheck--;
 						// Check which checkbox was clicked
 						//					    switch(view.getId()) {
 						//					        case R.id.pros_checkbox1:
 						////					            if (checked) countCheck++;
 						//					            break;
 						//					        case R.id.pros_checkbox2:
 						////					            if (checked) countCheck++;
 						//					            break;
 						//					        // TODO: Veggie sandwich
 						//					    }
 					}
 				});
 
 
 				reviewText = (EditText) dialog.findViewById(R.id.providerpf_rate_review);
 				reviewSummaryText = (EditText) dialog.findViewById(R.id.providerpf_rate_review_summary);
 				reviewButton = (Button) dialog.findViewById(R.id.providerpf_rate_button_submit);
 				ratingbar = (RatingBar) dialog.findViewById(R.id.providerpf_rate_bar);
 				rating_communication_bar = (RatingBar) dialog.findViewById(R.id.providerpf_rate_communication_bar);
 				rating_environment_bar = (RatingBar) dialog.findViewById(R.id.providerpf_rate_environment_bar);
 				rating_friendliness_bar = (RatingBar) dialog.findViewById(R.id.providerpf_rate_friendly_bar);
 
 				//new features!!!!!!!!
 				rating_professionalSkills_bar = (RatingBar) dialog.findViewById(R.id.providerpf_rate_professsionalSkills_bar);
 				rating_costs_bar = (RatingBar) dialog.findViewById(R.id.providerpf_rate_costs_bar);
 				rating_availability_bar = (RatingBar) dialog.findViewById(R.id.providerpf_rate_availability_bar);
 
 				reviewButton.setOnClickListener(new OnClickListener(){
 
 					public void onClick(View arg0) {
 
 
 						String review = reviewText.getText().toString();
 						String review_summary = reviewSummaryText.getText().toString();
 
 						review = parseText(review);
 						review_summary = parseText(review_summary);
 
 
 						SharedPreferences settings = getSharedPreferences("UserData", 0);
 						System.out.println(settings);
 						String id = settings.getString("Id", null);
 						float rating = ratingbar.getRating();
 						float friendliness = rating_friendliness_bar.getRating();
 						float communication = rating_communication_bar.getRating();
 						float environment = rating_environment_bar.getRating();
 						//new features!!!!!!!!!
 						float professionalSkills = rating_professionalSkills_bar.getRating();
 						float costs = rating_costs_bar.getRating();
 						float availability = rating_availability_bar.getRating();
 						
 						//add 5 pros, 5 cons, see if it is checked and insert into database
 						ArrayList<Integer> proList=new ArrayList<Integer>();
 						ArrayList<Integer> conList=new ArrayList<Integer>();
 
 						proList.add(checkBox_pros1.isChecked()?1:0);
 						proList.add(checkBox_pros2.isChecked()?2:0);
 						proList.add(checkBox_pros3.isChecked()?3:0);
 						proList.add(checkBox_pros4.isChecked()?4:0);
 						proList.add(checkBox_pros5.isChecked()?5:0);
 						conList.add(checkBox_cons1.isChecked()?-1:0);
 						conList.add(checkBox_cons2.isChecked()?-2:0);
 						conList.add(checkBox_cons3.isChecked()?-3:0);
 						conList.add(checkBox_cons4.isChecked()?-4:0);
 						conList.add(checkBox_cons5.isChecked()?-5:0);
 
 						int[] pros=new int[3];
 						int[] cons=new int[3];
 
 						int pIndex=0;
 						int cIndex=0;
 						for(int i=0;i<proList.size();i++){
 							if(proList.get(i)>0) {pros[pIndex]=proList.get(i);
 							pIndex++;
 							}		
 							if(conList.get(i)<0){
 								cons[cIndex]=conList.get(i);
 								cIndex++;
 							}
 						}
 
 						int pro1=pros[0];
 						int pro2=pros[1];
 						int pro3=pros[2];
 						int con1=cons[0];
 						int con2=cons[1];
 						int con3=cons[2];
 
 
 						m_provider.getID();
 						String temp_base = "https://fling.seas.upenn.edu/~xieyuhui/cgi-bin/ratings.php?mode=insert";
 						String url = temp_base + "&uid=" + id + "&pid=" + m_provider.getID() + "&rating=" + 
 								(int)rating + "&review_summary=" + review_summary + "&review=" + review + "&friendliness=" + (int)friendliness + 
 								"&communication=" + (int)communication + "&office_environment=" + (int)environment +
 								"&professional=" + (int)professionalSkills + "&costs=" + (int)costs + 
 								"&availability=" + (int)availability + 
 								"&pro1=" + pro1 + "&pro2=" + pro2 + "&pro3=" + pro3 + "&con1=" + con1 + "&con2=" + con2 + "&con3=" + con3;
 						System.out.println(url);
 						InternetHelper.httpGetRequest(url);
 						Toast.makeText(m_context, "Review submitted!", Toast.LENGTH_LONG).show();
 						populateRatings();
 						dialog.hide();	
 					}
 
 					public String parseText(String review) {
 						//make sure the input for keyword search is correct
 						if (review.length()>0 && !review.matches("[A-Za-z0-9\\s\\.,'!?&&[^\\n]]+?")){
 							//tell user the input was invalid
 							Context context = getApplicationContext();
 							Toast toast = Toast.makeText(context, "The keyword for search should only contains" +
 									" English characters, numbers or white space",Toast.LENGTH_SHORT);
 							toast.show();
 							return null;
 						}else{
 							review = review.replace(" ", "%20");
 						}
 						return review;
 					}
 				});
 				dialog.show();
 
 
 			}
 		});
 
 		// set the provider info in TextViews
 		m_provider_name.setText(m_provider.getName());
 		m_provider_phone.setText(m_provider.getPhone());
 		m_provider_address.setText(m_provider.getAddress());
 		Double averageRating = m_provider.getAverageRating();
 		m_provider_rating.setText(averageRating.toString());
 		m_provider_address.setText(m_provider.getAddress() + ", "
 				+ m_provider.getCity() + ", " + m_provider.getState() + "  "
 				+ m_provider.getZip());
 
 		// show or hide icons as appropriate
 		toggleIconVisibility(parking,m_provider.getParking());
 		toggleIconVisibility(creditcard,m_provider.getCreditCards());
 		toggleIconVisibility(appointment,m_provider.getAppointment());
 		toggleIconVisibility(accepting,m_provider.getAccepting());
 		toggleIconVisibility(PCP,m_provider.getType());
 
 		setRatingImage();
 
 
 	}
 
 
 	private void populateRatings() {
 		// make the HttpRequest
 		String uri = BASE_URL + m_provider.getID();		
 		String ratingsJSON = InternetHelper.httpGetRequest(uri);
 
 		// parse the JSON and populate m_ratings from JSON for m_provider
 		try {
 			JSONObject json = new JSONObject(ratingsJSON);
 			JSONArray reviews = json.getJSONArray("reviews");
 			for (int i = 0; i < reviews.length(); i++) {
 				JSONObject current = reviews.getJSONObject(i);
 				long user_id = Long.parseLong(current.getString("uid"));
 				long provider_id = Long.parseLong(current.getString("pid"));
 				String time = current.getString("time");
 				String review_summary = current.getString("review_summary");
 				String review = current.getString("review");
 				float rating = Float.parseFloat(current.getString("rating"));
 				float friendliness = Float.parseFloat(current.getString("friendliness"));
 				float communication = Float.parseFloat(current.getString("communication"));
 				float environment = Float.parseFloat(current.getString("office_environment"));
 				float professional = Float.parseFloat(current.getString("professional"));
 				float costs = Float.parseFloat(current.getString("costs"));
 				float availability = Float.parseFloat(current.getString("availability"));
 				int pro1 = Integer.parseInt(current.getString("pro1"));
 				int pro2 = Integer.parseInt(current.getString("pro2"));
 				int pro3 = Integer.parseInt(current.getString("pro3"));
 				int con1 = Integer.parseInt(current.getString("con1"));
 				int con2 = Integer.parseInt(current.getString("con2"));
 				int con3 = Integer.parseInt(current.getString("con3"));
 
 				Rating currentRating = new Rating(user_id, provider_id, time, review_summary, review, (int)rating, 
 						(int)communication, (int)environment, (int)friendliness, (int)professional, (int)costs, 
 						(int)availability, pro1, pro2, pro3, con1, con2, con3);
 				m_ratings.add(currentRating);
 				m_adapter.notifyDataSetChanged();
 
 			}
 		} catch (Exception e) {
 			// for logging
 			System.out.println("Ratings error");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * get the user name by user id
 	 * @param uid
 	 * @return
 	 */
 	private String getUserNameByUserId(long uid) {
 		// make the HttpRequest
 		String temp_base = "https://fling.seas.upenn.edu/~xieyuhui/cgi-bin/history.php?uid=";
 		String uri = temp_base + uid;		
 		String usersJSON = InternetHelper.httpGetRequest(uri);
 		String user_name = "";
 		// parse the JSON and populate m_ratings from JSON for m_provider
 		try {
 			JSONObject json = new JSONObject(usersJSON);
 			JSONArray reviews = json.getJSONArray("reviews");
 			JSONObject current = reviews.getJSONObject(0);
 			user_name = current.getString("name");
 		} catch (Exception e) {
 			// for logging
 			System.out.println("Users error");
 			e.printStackTrace();
 		}
 		return user_name;
 	}
 
 	private void setRatingImage(){
 		Integer avg = (int) m_provider.getAverageRating();
 
 		m_provider_star_rating.setImageResource(R.drawable.onestar);
 		if (avg == 5) {
 			m_provider_star_rating.setImageResource(R.drawable.fivestars);
 		} else if (avg == 4) {
 			m_provider_star_rating.setImageResource(R.drawable.fourstars);
 		} else if (avg == 3) {
 			m_provider_star_rating.setImageResource(R.drawable.threestars);
 		} else if (avg == 2) {
 			m_provider_star_rating.setImageResource(R.drawable.twostars);
 		} else if (avg == 4) {
 			m_provider_star_rating.setImageResource(R.drawable.onestar);
 		}
 	}
 
 	private void toggleIconVisibility(Button button, String result){
 		if (result.equals("yes") || result.equals("PCP")) {
 			button.setVisibility(Button.VISIBLE);
 		}
 		else if(result.equals("PCP")){
 			specialist.setVisibility(Button.GONE);
 			PCP.setVisibility(Button.VISIBLE);
 		}
 		else if(result.equals("specialist")){
 			specialist.setVisibility(Button.VISIBLE);
 			PCP.setVisibility(Button.GONE);
 		}
 		else {
 			button.setVisibility(Button.GONE);
 		}
 	}
 
 	//inner class for rating adapter. Needs to reference m_ratings
 	class RatingAdapter extends BaseAdapter{
 		private Context m_context;
 		public RatingAdapter(Context c){
 			m_context = c;
 		}
 
 		public int getCount() {
 			if(m_ratings != null)
 				return m_ratings.size();
 			else
 				return 0;
 		}
 
 		public Object getItem(int position) {
 			if(m_ratings != null)
 				return m_ratings.get(position);
 			else
 				return 0;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 
 		/**
 		 * Convert the pros and cons integers into strings
 		 * @param label
 		 * @return
 		 */
 		public String getProAndConString(int label) {
 			String message;
 			switch(label) {
 			case 1:
 			    message = getString(R.string.pros1);
 			    break;
 			case 2:
 			    message = getString(R.string.pros2);
 			    break;
 			case 3:
 			    message = getString(R.string.pros3);
 			    break;
 			case 4:
 			    message = getString(R.string.pros4);
 			    break;
 			case 5:
 			    message = getString(R.string.pros5);
 			    break;
 			case -1:
 			    message = getString(R.string.cons1);
 			    break;
 			case -2:
 			    message = getString(R.string.cons2);
 			    break;
 			case -3:
 			    message = getString(R.string.cons3);
 			    break;
 			case -4:
 			    message = getString(R.string.cons4);
 			    break;
 			case -5:
 			    message = getString(R.string.cons5);
 			    break;
 			default:
 			    message = "";
 			}
 			return message;
 		}
 
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			//inflate the view
 			LinearLayout list_result;
 			if(convertView == null){
 				LayoutInflater inf = (LayoutInflater)m_context.getSystemService(
 						Context.LAYOUT_INFLATER_SERVICE);
 				list_result = (LinearLayout) inf.inflate(R.layout.provider_pf_comment, null);
 			}
 			else
 				list_result = (LinearLayout)convertView;
 
 			Rating currentRating = m_ratings.get(position);
 			String date = currentRating.getDate().substring(0, 11);  //only show the date
 			long user_id = currentRating.getUser();
 			String provider_user_name = "By " + getUserNameByUserId(user_id);
 			String review_summary = currentRating.getReview_summary();
 			String pro1 = getProAndConString(currentRating.getPro1());
 			String pro2 = getProAndConString(currentRating.getPro2());
 			String pro3 = getProAndConString(currentRating.getPro3());
 			String con1 = getProAndConString(currentRating.getCon1());
 			String con2 = getProAndConString(currentRating.getCon2());
 			String con3 = getProAndConString(currentRating.getCon3());
 			String review = currentRating.getReview();
 
 			RatingBar stars= (RatingBar)list_result.findViewById(R.id.providerpf_comment_stars);
			stars.setRating(currentRating.getRating());
 			TextView tv_provider_date = (TextView)list_result.findViewById(R.id.providerpf_comment_date);
 			tv_provider_date.setText(date);
 			TextView tv_provider_user_name = (TextView)list_result.findViewById(R.id.providerpf_user_name);
 			tv_provider_user_name.setText(provider_user_name);
 			TextView tv_provider_summary = (TextView)list_result.findViewById(R.id.providerpf_rate_review_summary);
 			tv_provider_summary.setText(review_summary);
 
 			TextView tv_pro1 = (TextView)list_result.findViewById(R.id.providerpf_review_pro1);
 			tv_pro1.setText(pro1);
 			TextView tv_pro2 = (TextView)list_result.findViewById(R.id.providerpf_review_pro2);
 			tv_pro2.setText(pro2);
 			TextView tv_pro3 = (TextView)list_result.findViewById(R.id.providerpf_review_pro3);
 			tv_pro3.setText(pro3);
 			TextView tv_con1 = (TextView)list_result.findViewById(R.id.providerpf_review_con1);
 			tv_con1.setText(con1);
 			TextView tv_con2 = (TextView)list_result.findViewById(R.id.providerpf_review_con2);
 			tv_con2.setText(con2);
 			TextView tv_con3 = (TextView)list_result.findViewById(R.id.providerpf_review_con3);
 			tv_con3.setText(con3);
 
 			TextView tv_provider_desc = (TextView)list_result.findViewById(R.id.providerpf_comment_review);
 			tv_provider_desc.setText(review);
 
 
 			//populate the new view
 			//TextView tv_rating = (TextView)list_result.findViewById(R.id.providerpf_comment_rating);
 			//			Integer rating = currentRating.getRating();
 
 			//			RatingBar friendliness = (RatingBar)list_result.findViewById(R.id.providerpf_comment_friendliness);
 			//			RatingBar environment = (RatingBar)list_result.findViewById(R.id.providerpf_comment_environment);
 			//			RatingBar communication = (RatingBar)list_result.findViewById(R.id.providerpf_comment_communication);
 			//			//wolaiwanyiwan
 			//			RatingBar professional = (RatingBar)list_result.findViewById(R.id.providerpf_comment_professionalskills);
 			//			RatingBar costs = (RatingBar)list_result.findViewById(R.id.providerpf_comment_costs);
 			//			RatingBar availability = (RatingBar)list_result.findViewById(R.id.providerpf_comment_availability);
 
 
 			//			if (rating==5) {
 			//				stars.setImageResource(R.drawable.fivestars);
 			//			} else if (rating==4) {
 			//				stars.setImageResource(R.drawable.fourstars);
 			//			} else if (rating==3) {
 			//				stars.setImageResource(R.drawable.threestars);
 			//			} else if (rating==2) {
 			//				stars.setImageResource(R.drawable.twostars);
 			//			} else if (rating==1) {
 			//				stars.setImageResource(R.drawable.onestar);
 			//			}
 			//
 			//			friendliness.setRating(currentRating.getFriendliness_rating());
 			//
 			//			environment.setRating(currentRating.getOffice_environment_rating());
 			//			communication.setRating(currentRating.getCommunication_rating());
 			//
 			//			//wolaiwanyiwan
 			//			professional.setRating(currentRating.getProfessional_rating());
 			//			costs.setRating(currentRating.getCosts_rating());
 			//			availability.setRating(currentRating.getAvailability_rating());
 
 
 
 			return list_result;
 		}
 	}
 }
