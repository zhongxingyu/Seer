 package net.kuwalab.android.icareader;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.kazzz.felica.FeliCaTag;
 import net.kazzz.felica.command.ReadResponse;
 import net.kazzz.felica.lib.FeliCaLib.ServiceCode;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.text.format.DateFormat;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		TextView messageText = (TextView) findViewById(R.id.messageText);
 		messageText.setText(R.string.ica_first_step);
 
 		onNewIntent(getIntent());
 	}
 
 	private void viewList(String nowRestMoney, List<Map<String, String>> list) {
 		TextView messageText = (TextView) findViewById(R.id.messageText);
 		messageText.setText(R.string.ica_rest);
 		TextView nowRestMoneyText = (TextView) findViewById(R.id.nowRestMoneyText);
 		nowRestMoneyText.setText(nowRestMoney);
 
 		LinearLayout firstStepLayout = (LinearLayout) findViewById(R.id.firstStepLayout);
 		firstStepLayout.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		messageText.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 		ListView historyListView = (ListView) findViewById(R.id.listView);
 		historyListView.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 
 		SimpleAdapter adapter = new ICaAdapter(this, list, R.layout.list,
 				new String[] { "restMoney", "addMoney", "useMoney", "date",
 						"rideTime", "dropTime" }, new int[] { R.id.restMoney,
 						R.id.addMoney, R.id.useMoney, R.id.date, R.id.rideTime,
 						R.id.dropTime });
 		historyListView.setAdapter(adapter);
 
 		SharedPreferences pref = getSharedPreferences(
 				ICaService.PREFERENCES_NAME, Context.MODE_PRIVATE);
 		Editor edit = pref.edit();
 		edit.putString(ICaService.PREFERENCES_CONF_DATE,
 				DateFormat.format("yyyy/MM/dd", Calendar.getInstance())
 						.toString());
		edit.putString(ICaService.PREFERENCES_REST_MONEY, "￥" + nowRestMoney);
 		edit.commit();
 
 		Intent intent = new Intent();
 		intent.setAction(ICaService.ACTION);
 		sendBroadcast(intent);
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		String action = intent.getAction();
 		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
 
 			Parcelable nfcTag = intent
 					.getParcelableExtra("android.nfc.extra.TAG");
 			read(nfcTag);
 		}
 	}
 
 	private void read(Parcelable nfcTag) {
 		try {
 			FeliCaTag f = new FeliCaTag(nfcTag);
 
 			// polling は IDm、PMmを取得するのに必要
 			f.polling(0x80EF);
 
 			// サービスコード読み取り
 			ServiceCode sc = new ServiceCode(0x898F);
 			byte addr = 0;
 			ReadResponse result = f.readWithoutEncryption(sc, addr);
 			if (result == null) {
				Toast.makeText(getBaseContext(), "ICaでないか、カードが読み込めません。",
 						Toast.LENGTH_LONG).show();
 				return;
 			}
 
 			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
 			String nowRestMoney = "0";
 
 			while (result != null && result.getStatusFlag1() == 0) {
 				IcaHistory icaHistory = new IcaHistory(result.getBlockData());
 
 				Map<String, String> map = new HashMap<String, String>();
 				map.put("restMoney", icaHistory.getDispRestMoney());
 				if (icaHistory.isUse()) {
 					map.put("useMoney",
 							String.valueOf(icaHistory.getDispUseMoney()));
 					map.put("addMoney", "");
 				} else {
 					map.put("useMoney", "");
 					map.put("addMoney",
 							String.valueOf(icaHistory.getDispAddMoney()));
 				}
 				map.put("date", getViewDate(icaHistory.getDate()));
 				map.put("rideTime", getViewTime(icaHistory.getRideTime()));
 				map.put("dropTime", getViewTime(icaHistory.getDropTime()));
 				list.add(map);
 
 				if (addr == 0) {
 					nowRestMoney = icaHistory.getDispRestMoney();
 				}
 				addr++;
 				result = f.readWithoutEncryption(sc, addr);
 			}
 
 			viewList(nowRestMoney, list);
 
 		} catch (Exception e) {
 			e.printStackTrace();
			Toast.makeText(getBaseContext(), "読み込めませんでした。再度試してください。",
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private String getViewDate(int[] date) {
 		if (date == null) {
			return "????/??/??";
 		}
 		return getString(R.string.ica_date, date[0], date[1], date[2]);
 	}
 
 	private String getViewTime(int[] time) {
 		if (time == null) {
			return "??:??";
 		}
 		return getString(R.string.ica_time, time[0], time[1]);
 	}
 }
