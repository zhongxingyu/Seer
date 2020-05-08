 package rpisdd.rpgme.activities;
 
 import java.util.ArrayList;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.items.Item;
 import rpisdd.rpgme.gamelogic.player.Player;
 import rpisdd.rpgme.popups.AnnoyingPopup;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.squareup.picasso.Picasso;
 
 public class InventoryMenu extends ListFragment implements OnClickListener {
 
 	ImageButton sell;
 	ImageButton use;
 	ImageButton details;
 	Item selectedItem;
 	View selectedItemSlot;
 	int selectedItemIndex;
 	TextView goldWidget;
 
 	ItemAdapter itemAdapter;
 
 	public InventoryMenu() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 
 		selectedItem = null;
 		selectedItemSlot = null;
 		selectedItemIndex = -1;
 
 		View v = inflater.inflate(R.layout.inventory_menu, container, false);
 
 		Log.i("inventory_gold", "about to set sell page");
 		sell = (ImageButton) v.findViewById(R.id.sellItem);
 		sell.setOnClickListener(this);
 
 		use = (ImageButton) v.findViewById(R.id.useItem);
 		use.setOnClickListener(this);
 
 		details = (ImageButton) v.findViewById(R.id.detailsItem);
 		details.setOnClickListener(this);
 
 		Log.i("inventory_gold", "about to set gold");
 		Player p = Player.getPlayer();
 		goldWidget = (TextView) v.findViewById(R.id.inventoryGoldDisplay);
 		goldWidget.setText("" + p.getGold());
 
 		Log.i("inventory_gold", " set gold");
 
 		updateButtons();
 
 		return v;
 	}
 
 	// Will enable or disable buttons, depending on the situation.
 	public void updateButtons() {
 		if (selectedItemSlot == null) {
 			sell.setEnabled(false);
 			sell.setVisibility(View.INVISIBLE);
 			use.setEnabled(false);
 			use.setVisibility(View.INVISIBLE);
 			details.setEnabled(false);
 			details.setVisibility(View.INVISIBLE);
 		} else {
 			sell.setEnabled(true);
 			sell.setVisibility(View.VISIBLE);
 			details.setEnabled(true);
 			details.setVisibility(View.VISIBLE);
 			if (selectedItem != null
 					&& selectedItem.isUsable(Player.getPlayer())) {
 				use.setEnabled(true);
 				use.setVisibility(View.VISIBLE);
 			} else {
 				use.setEnabled(false);
 			}
 		}
 
 		int armorIndex = Player.getPlayer().getInventory().getArmorIndex();
 		int weaponIndex = Player.getPlayer().getInventory().getWeaponIndex();
 
 		Item item = Player.getPlayer().getInventory()
 				.getItemAt(selectedItemIndex);
 
 		if (item != null) {
 			if (item.isEquipment()) {
 				// use.setText("Equip");
 			} else {
 				// use.setText("Use");
 			}
 		}
 
 		if (selectedItemIndex != -1 && selectedItemIndex == armorIndex
 				|| selectedItemIndex == weaponIndex) {
 			// use.setText("Unequip");
 		}
 
 	}
 
 	@Override
 	public void onResume() {
 		updateListView(false);
 		super.onResume();
 	}
 
 	// Update the list view adapter. Also can choose to unselect current
 	// selection
 	public void updateListView(boolean unselectAll) {
 
 		Player p = Player.getPlayer();
 
 		if (unselectAll) {
 			selectedItem = null;
 			selectedItemSlot = null;
 			selectedItemIndex = -1;
 			itemAdapter = new ItemAdapter(getActivity(), R.layout.shop_item, p
 					.getInventory().getItems());
 			setListAdapter(itemAdapter);
 		} else {
 			if (itemAdapter == null) {
 				itemAdapter = new ItemAdapter(getActivity(),
 						R.layout.shop_item, p.getInventory().getItems());
 				setListAdapter(itemAdapter);
 			} else {
 				itemAdapter.notifyDataSetChanged();
 			}
 
 		}
 
 		updateButtons();
 	}
 
 	private class ItemAdapter extends ArrayAdapter<Item> {
 
 		ArrayList<Item> items;
 
 		public ItemAdapter(Context context, int textViewResourceId,
 				ArrayList<Item> items) {
 			super(context, textViewResourceId, items);
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				v = LayoutInflater.from(getActivity()).inflate(
 						R.layout.inventory_item, null);
 			}
 			if (position == selectedItemIndex) {
 				v.setSelected(true);
 			}
 			Item i = items.get(position);
 			if (i != null) {
 				TextView name = (TextView) v
 						.findViewById(R.id.inventoryItemName);
 				name.setText(i.getName());
 
 				ImageView image = (ImageView) v
 						.findViewById(R.id.inventoryItemImage);
 				name.setText(i.getName());
 
 				name.setTextColor(Color.WHITE);
 
 				if (position == Player.getPlayer().getInventory()
 						.getWeaponIndex()
 						|| position == Player.getPlayer().getInventory()
 								.getArmorIndex()) {
 					name.setTextColor(Color.GREEN);
 					name.append(" (Equipped)");
 				}
 				Picasso.with(getActivity()).load(i.getImagePath()).into(image);
 			}
 			return v;
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
 		selectedItemIndex = position;
 
 		if (selectedItemSlot != null) {
 			selectedItemSlot.setBackgroundColor(Color.TRANSPARENT);
 		}
 
 		v.setBackgroundColor(Color.GRAY);
 
 		v.setSelected(true);
 		selectedItemSlot = v;
 		selectedItem = Player.getPlayer().getInventory().getItems()
 				.get(selectedItemIndex);
 
 		updateButtons();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 
 		case R.id.useItem:
 			useItem();
 			break;
 		case R.id.sellItem:
 			AnnoyingPopup.doDont(getActivity(),
 					"Sell this " + selectedItem.getName() + " for "
 							+ selectedItem.getRefundPrice() + " gold?", "Sell",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 							sellItemToShop();
 						}
 					});
 			break;
 		case R.id.detailsItem:
 			AnnoyingPopup.notice(getActivity(), selectedItem.getDescription()
 					+ "\n\n" + "Sell Value: " + selectedItem.getRefundPrice());
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void sellItemToShop() {
 		Player p = Player.getPlayer();
 		p.addGold(selectedItem.getRefundPrice());
 		p.getInventory().removeItemAt(selectedItemIndex);
 		updateListView(true);
 		goldWidget.setText("" + p.getGold());
 	}
 
 	public void useItem() {
 
 		Player p = Player.getPlayer();
 		selectedItem.useMe(p, selectedItemIndex);
 		Log.i("items", String.format("Player has %s and %s equipped", p
 				.getInventory().getWeapon(), p.getInventory().getArmor()));
 
 		if (!selectedItem.isEquipment()) {
 			updateListView(true);
 		} else {
 			updateListView(false);
 		}
 	}
 }
