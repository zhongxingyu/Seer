 package net.skweez.sipgate.activity;
 
 import net.skweez.sipgate.R;
 import net.skweez.sipgate.model.AccountInfo;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 /**
  * @author Michael Kanis
  */
 public class AccountInfoAdapter extends BaseAdapter {
 
 	private static final int USER_NAME_INDEX = 0;
 	private static final int CUSTOMER_NUMBER_INDEX = 1;
 	private static final int PHONE_NUMBER_INDEX = 2;
 	private static final int BALANCE_INDEX = 3;
 
 	private final LayoutInflater inflater;
 
 	private AccountInfo accountInfo;
 
 	public AccountInfoAdapter(Activity context, AccountInfo accountInfo) {
 		this.accountInfo = accountInfo;
 		inflater = LayoutInflater.from(context);
 	}
 
 	public int getCount() {
 		if (accountInfo == null) {
 			return 0;
 		}
 
 		return 4;
 	}
 
 	public PairOfStrings getItem(int position) {
 		switch (position) {
 		case USER_NAME_INDEX:
 			return new PairOfStrings("Name", accountInfo.getUserName());
 		case CUSTOMER_NUMBER_INDEX:
 			return new PairOfStrings("Customer number",
 					accountInfo.getCustomerNumber());
 		case PHONE_NUMBER_INDEX:
 			return new PairOfStrings("Phone number",
 					accountInfo.getPhoneNumber());
 		case BALANCE_INDEX:
 			return new PairOfStrings("Balance", accountInfo.getBalance());
 		default:
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		View item;
 
 		if (convertView != null) {
 			item = convertView;
 		} else {
 			item = inflater.inflate(R.layout.account_info_item, null);
 		}
 
 		PairOfStrings strings = getItem(position);
 
 		TextView text1 = (TextView) item.findViewById(android.R.id.text1);
 		text1.setText(strings.getString1());
 
 		TextView text2 = (TextView) item.findViewById(android.R.id.text2);
 		text2.setText(strings.getString2());
 
 		return item;
 	}
 
 	private static class PairOfStrings {
 		private String string1;
 		private String string2;
 
 		public PairOfStrings(Object o1, Object o2) {
 			if (o1 != null) {
 				this.string1 = o1.toString();
 			}
 
 			if (o2 != null) {
 				this.string2 = o2.toString();
 			}
 		}
 
 		public String getString1() {
 			if (string1 != null) {
 				return string1;
 			} else {
 				return "";
 			}
 		}
 
 		public String getString2() {
 			if (string2 != null) {
 				return string2;
 			} else {
 				return "";
 			}
 		}
 	}
 
 	public void setAccountInfo(AccountInfo accountInfo) {
 		this.accountInfo = accountInfo;
 		notifyDataSetChanged();
 	}
 }
