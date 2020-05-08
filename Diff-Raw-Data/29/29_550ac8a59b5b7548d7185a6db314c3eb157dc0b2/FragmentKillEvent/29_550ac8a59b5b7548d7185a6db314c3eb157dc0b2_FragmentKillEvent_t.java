 package com.cesarandres.ps2link.fragments;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.android.volley.Response;
 import com.android.volley.Response.ErrorListener;
 import com.android.volley.Response.Listener;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.NetworkImageView;
 import com.cesarandres.ps2link.ApplicationPS2Link;
 import com.cesarandres.ps2link.R;
 import com.cesarandres.ps2link.R.drawable;
 import com.cesarandres.ps2link.R.id;
 import com.cesarandres.ps2link.R.layout;
 import com.cesarandres.ps2link.soe.SOECensus;
 import com.cesarandres.ps2link.soe.SOECensus.Game;
 import com.cesarandres.ps2link.soe.SOECensus.Verb;
 import com.cesarandres.ps2link.soe.content.CharacterEvent;
 import com.cesarandres.ps2link.soe.content.Faction;
 import com.cesarandres.ps2link.soe.content.Item;
 import com.cesarandres.ps2link.soe.content.Vehicle;
 import com.cesarandres.ps2link.soe.content.item.IContainDrawable;
 import com.cesarandres.ps2link.soe.content.response.Item_list_response;
 import com.cesarandres.ps2link.soe.util.Collections.PS2Collection;
 import com.cesarandres.ps2link.soe.volley.GsonRequest;
 
 /**
  * Created by cesar on 6/16/13.
  */
@SuppressLint("ValidFragment")
 public class FragmentKillEvent extends DialogFragment {
 
 	private CharacterEvent event;
 
 	public FragmentKillEvent() {
 	}
 
 	public FragmentKillEvent(CharacterEvent event) {
 		this.event = event;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 		View root = inflater.inflate(R.layout.fragment_kill_event, container, false);
 		((TextView) root.findViewById(R.id.TextViewKillEventAttackerName)).setText(event.getAttacker().getName().getFirst());
 		Bitmap faction_icon = null;
 		if (event.getAttacker().getFaction_id().equals(Faction.VS)) {
 			faction_icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_faction_vs);
 		} else if (event.getAttacker().getFaction_id().equals(Faction.NC)) {
 			faction_icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_faction_nc);
 		} else if (event.getAttacker().getFaction_id().equals(Faction.TR)) {
 			faction_icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_faction_tr);
 		}
 		if (faction_icon != null) {
 			((ImageView) root.findViewById(R.id.ImageViewKillEventAttackerFaction)).setImageBitmap(faction_icon);
 		}
 		((TextView) root.findViewById(R.id.TextViewKillEventAttackerBattleRank)).setText("BR: "
 				+ Integer.toString(event.getAttacker().getBattle_rank().getValue()));
 
 		((TextView) root.findViewById(R.id.textViewKillEventCharacterName)).setText(event.getCharacter().getName().getFirst());
 		faction_icon = null;
 		if (event.getCharacter().getFaction_id().equals(Faction.VS)) {
 			faction_icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_faction_vs);
 		} else if (event.getCharacter().getFaction_id().equals(Faction.NC)) {
 			faction_icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_faction_nc);
 		} else if (event.getCharacter().getFaction_id().equals(Faction.TR)) {
 			faction_icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_faction_tr);
 		}
 		if (faction_icon != null) {
 			((ImageView) root.findViewById(R.id.imageViewKillEventCharacterFaction)).setImageBitmap(faction_icon);
 		}
 		((TextView) root.findViewById(R.id.textViewKillEventCharacterBattleRank)).setText("BR: "
 				+ Integer.toString(event.getCharacter().getBattle_rank().getValue()));
 
 		if (!event.getAttacker_weapon_id().equals("0")) {
 			downloadPictures(event.getAttacker_weapon_id(), PS2Collection.ITEM, root);
 		}
 		if (!event.getAttacker_vehicle_id().equals("0")) {
 			downloadPictures(event.getAttacker_vehicle_id(), PS2Collection.VEHICLE, root);
 		}
 		return root;
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		Dialog dialog = super.onCreateDialog(savedInstanceState);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		return dialog;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setRetainInstance(true);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 		ApplicationPS2Link.volley.cancelAll(this);
 	}
 
 	public void downloadPictures(String resource_id, PS2Collection collection, View view) {
 
 		Class itemClass = null;
 
 		TextView name = null;
 		NetworkImageView image = null;
 
 		switch (collection) {
 		case ITEM:
 			itemClass = Item.class;
 			name = (TextView) view.findViewById(R.id.TextViewKillEventWeapon);
 			image = (NetworkImageView) view.findViewById(R.id.ImageViewKillEventWeaponImage);
 			break;
 		case VEHICLE:
 			itemClass = Vehicle.class;
 			name = (TextView) view.findViewById(R.id.TextViewKillEventVehicle);
 			image = (NetworkImageView) view.findViewById(R.id.ImageViewKillEventVehicleImage);
 			break;
 		}
 
 		final TextView nameView = name;
 		final NetworkImageView imageView = image;
 
 		URL url;
 		try {
 
 			url = SOECensus.generateGameDataRequest(Verb.GET, Game.PS2V2, collection, resource_id, null);
 			Listener<Item_list_response> success = new Response.Listener<Item_list_response>() {
 				@Override
 				public void onResponse(Item_list_response response) {
 					IContainDrawable item = null;
 
 					if (response.getItem_list() != null) {
 						item = response.getItem_list().get(0);
 					} else if (response.getVehicle_list() != null) {
 						item = response.getVehicle_list().get(0);
 					}
 
 					nameView.setText(item.getNameText());
 					imageView.setImageUrl(SOECensus.ENDPOINT_URL + "/" + item.getImagePath(), ApplicationPS2Link.mImageLoader);
 				}
 			};
 
 			ErrorListener error = new Response.ErrorListener() {
 				@Override
 				public void onErrorResponse(VolleyError error) {
 					error.equals(new Object());
 				}
 			};
 
 			GsonRequest<Item_list_response> gsonOject = new GsonRequest<Item_list_response>(url.toString(), Item_list_response.class, null, success, error);
 			gsonOject.setTag(this);
 			ApplicationPS2Link.volley.add(gsonOject);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
