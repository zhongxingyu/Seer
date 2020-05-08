 package uk.org.downesward.dirtside;
 
 import java.util.ArrayList;
 
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.CheckBox;
 
 import uk.org.downesward.dirtside.R;
 
 import uk.org.downesward.dirtside.adapters.CampaignAdapter;
 import uk.org.downesward.dirtside.adapters.InfantryFirepowerAdapter;
 import uk.org.downesward.dirtside.adapters.InfantryMovementAdapter;
 import uk.org.downesward.dirtside.adapters.NationalityAdapter;
 import uk.org.downesward.dirtside.domain.Infantry;
 import uk.org.downesward.dirtside.domain.Campaign;
 import uk.org.downesward.dirtside.domain.InfantryFirepower;
 import uk.org.downesward.dirtside.domain.InfantryMovement;
 import uk.org.downesward.dirtside.domain.Nationality;
 
 /**
  * A fragment representing a single InfantryTeam detail screen. This fragment is
  * either contained in a {@link InfantryTeamListActivity} in two-pane mode (on
  * tablets) or a {@link InfantryTeamDetailActivity} on handsets.
  */
 public class InfantryTeamDetailFragment extends Fragment {
 	/**
 	 * The fragment argument representing the item ID that this fragment
 	 * represents.
 	 */
 	public static final String ARG_ITEM_ID = "item_id";
 
 	/**
 	 * The dummy content this fragment is presenting.
 	 */
 	private Infantry mItem;
 
 	private ArrayList<InfantryMovement> mMovementTypes;
 	private InfantryMovementAdapter infantryMovementAdapter;
 	private ArrayList<InfantryFirepower> mFirepowers;
 	private InfantryFirepowerAdapter infantryFirepowerAdapter;
 	
 	private ArrayList<Campaign> mCampaigns;
 	private CampaignAdapter campaignAdapter; 
 	
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public InfantryTeamDetailFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getArguments().containsKey(ARG_ITEM_ID)) {
 
 			Integer infantryId = Integer.parseInt(getArguments().getString(
 					ARG_ITEM_ID));
 			DatabaseHelper dbh = new DatabaseHelper(this.getActivity());
 			Cursor cur = dbh.getInfantry(infantryId);
 			if (cur.moveToNext()) {
 				Infantry inf = new Infantry(cur);
 				mItem = inf;
 			}
 		}
 		
 		DatabaseHelper dbh = new DatabaseHelper(this.getActivity());
 		
 		Cursor campaigns = dbh.getCampaigns();
 		mCampaigns = new ArrayList<Campaign>();
 				
 		while (campaigns.moveToNext()) {
 			Campaign campaign = new Campaign(campaigns);
 			mCampaigns.add(campaign);
 		}
 		campaignAdapter = new CampaignAdapter(this.getActivity(),
 				android.R.layout.simple_spinner_item, mCampaigns);	
 		
 		Cursor movementTypes = dbh.getInfantryMovements();
 		mMovementTypes = new ArrayList<InfantryMovement>();
 		
 		while (movementTypes.moveToNext()) {
 			InfantryMovement movementType = new InfantryMovement(movementTypes);
 			mMovementTypes.add(movementType);
 		}
 		infantryMovementAdapter = new InfantryMovementAdapter(this.getActivity(),
 				android.R.layout.simple_spinner_item, mMovementTypes);	
 		
 		// Firepower
 		Cursor firepowers = dbh.getInfantryFirepowers();
 		mFirepowers = new ArrayList<InfantryFirepower>();
 		while (firepowers.moveToNext()) {
 			InfantryFirepower firepower = new InfantryFirepower(firepowers);
 			mFirepowers.add(firepower);
 		}
 		infantryFirepowerAdapter = new InfantryFirepowerAdapter(this.getActivity(),
 				android.R.layout.simple_spinner_item, mFirepowers);			
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_infantryteam_detail,
 				container, false);
 
 		if (mItem != null) {
 			((TextView) rootView.findViewById(R.id.txtInfantryDescription))
 					.setText(mItem.getDescription());
 			
 			((TextView) rootView.findViewById(R.id.txtInfantryNotes))
 			.setText(mItem.getNotes());
 			
 			((TextView) rootView.findViewById(R.id.txtInfantryCost))
 			.setText(mItem.getCost().toString());
 			
 			((TextView) rootView.findViewById(R.id.txtInfantryPersonnelCount))
 			.setText(mItem.getPersonnelCount().toString());			
 			
 			// Binary Capabilities
 			((CheckBox) rootView.findViewById(R.id.chkIAVR)).setChecked(mItem.isIAVR());
 			((CheckBox) rootView.findViewById(R.id.chkFlying)).setChecked(mItem.isFlying());
 			((CheckBox) rootView.findViewById(R.id.chkBiological)).setChecked(mItem.isBiological());
 			((CheckBox) rootView.findViewById(R.id.chkLAD)).setChecked(mItem.isLAD());
 			((CheckBox) rootView.findViewById(R.id.chkEngineering)).setChecked(mItem.isEngineering());
 			((CheckBox) rootView.findViewById(R.id.chkTeleport)).setChecked(mItem.isTeleport());
 			
 			DatabaseHelper dbh = new DatabaseHelper(this.getActivity());
 			
 			// Campaigns
 			Cursor campaign = dbh.getCampaign(mItem.getCampaignId());
			Campaign thisCampaign = new Campaign(campaign);
 					
 			Spinner spinner = (Spinner) rootView.findViewById(R.id.spnCampaign);
 
 			spinner.setAdapter(campaignAdapter);
 			
 			int spinnerPosition = campaignAdapter.getPosition(thisCampaign);
 			spinner.setSelection(spinnerPosition);
 			
 			// Nationality
 			Cursor nationalities = dbh.getNationalitiesForCampaign(mItem.getCampaignId());
 			ArrayList<Nationality> mNationalities = new ArrayList<Nationality>();
 			Nationality thisNationality = null;		
 					
 			while (nationalities.moveToNext()) {
 				Nationality nationality = new Nationality(nationalities);
 				if (nationality.getNationalityId() == mItem.getNationalityId()) {
 					thisNationality = nationality;
 				}
 				mNationalities.add(nationality);
 			}
 			spinner = (Spinner) rootView.findViewById(R.id.spnNationality);
 			NationalityAdapter nationalityAdapter = new NationalityAdapter(this.getActivity(),
 					android.R.layout.simple_spinner_item, mNationalities);	
 			
 			spinner.setAdapter(nationalityAdapter);
 			
 			spinnerPosition = nationalityAdapter.getPosition(thisNationality);
 			spinner.setSelection(spinnerPosition);			
 			
 			// Movement
 			Cursor movement = dbh.getInfantryMovement(mItem.getInfantryMovementId());
			InfantryMovement thisInfantryMovement = new InfantryMovement(movement);				
 			spinner = (Spinner) rootView.findViewById(R.id.spnInfantryMovement);
 			spinner.setAdapter(infantryMovementAdapter);
 			
 			spinnerPosition = infantryMovementAdapter.getPosition(thisInfantryMovement);
 			spinner.setSelection(spinnerPosition);			
 			
 			// Firepower
 			Cursor firepowers = dbh.getInfantryFirepower(mItem.getInfantryFPId());
			InfantryFirepower thisInfantryFirepower = new InfantryFirepower(firepowers);
 					
 			spinner = (Spinner) rootView.findViewById(R.id.spnInfantryFirepower);		
 			spinner.setAdapter(infantryFirepowerAdapter);		
 			spinnerPosition = infantryFirepowerAdapter.getPosition(thisInfantryFirepower);
 			spinner.setSelection(spinnerPosition);				
 		}
 
 		return rootView;
 	}
 }
