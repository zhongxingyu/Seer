 package com.blogpost.hiro99ma.nfctest3;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.NfcA;
 import android.nfc.tech.NfcF;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private NfcAdapter mAdapter;
 	private PendingIntent mPendingIntent;
 	private IntentFilter[] mFilters;
 	private String[][] mTechLists;
 	private Tag mTag;
 	private byte[] mNfcId = null;
 	private static byte[] mBuf = new byte[265];
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// NFC
 		mAdapter = NfcAdapter.getDefaultAdapter(this);
 		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
 		IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
 		mFilters = new IntentFilter[] { tech };
 
 		mTechLists = new String[][] {
 						new String[] { NfcA.class.getName() },
 			// new String[] { NfcB.class.getName() },
 						new String[] { NfcF.class.getName() }
 		};
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (mAdapter != null) {
 			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (mAdapter != null) {
 			mAdapter.disableForegroundDispatch(this);
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 		mNfcId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
 	}
 
 	public void buttonWriteHdr(View v) {
 		if (mTag == null) {
 			Toast.makeText(this, "no card", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		NfcF f = NfcF.get(mTag);
 		//NfcA f = NfcA.get(mTag);
 		try {
 			f.connect();
 
 			byte[] poll = new byte[] { 0x06, 0x00, (byte)0xff, (byte)0xff, 0x00, 0x00 };
 			//byte[] poll = new byte[] { 0x30, 0x00 };
 			f.transceive(poll);
 
 			byte len = pushUrl("http://www.yahoo.co.jp/");
 			//byte len = readCard(0);
 
 			byte[] data = new byte[len];
 			System.arraycopy(mBuf, 0, data, 0, len);
 			f.transceive(data);
 
//			data = new byte[11];
//			data[0] = 11;
//			data[1] = (byte)0xa4;
//			System.arraycopy(mNfcId, 0, data, 2, mNfcId.length);
//			data[10] = 0x00;
//			f.transceive(data);

 			f.close();
 		} catch (Exception e) {
 			Toast.makeText(this, "try", Toast.LENGTH_SHORT).show();
 			Log.d("NFC", "mine", e);
 		}
 	}
 
 	public byte readCard(int i) {
 		mBuf[1] = (byte)0x06;
 		System.arraycopy(mNfcId, 0, mBuf, 2, mNfcId.length);
 		mBuf[10] = 0x01; // サービス数
		mBuf[11] = (byte)0x09;
		mBuf[12] = (byte)0x00;
 		mBuf[13] = 0x01; // ブロック数
 		mBuf[14] = (byte)0x80;
 		mBuf[15] = 0x01;
 
 		mBuf[0] = 16;
 
 		return mBuf[0];
 	}
 
 	/**
 	 * [FeliCa]PUSHコマンド
 	 * 
 	 * @param[in] data PUSHデータ
 	 * @param[in] dataLen dataの長さ
 	 * 
 	 * @retval true 成功
 	 * @retval false 失敗
 	 * 
 	 * @attention - dataはそのまま送信するため、上位層で加工しておくこと。
 	 */
 	public byte push(byte[] data, byte dataLen) {
 
 		mBuf[1] = (byte)0xb0;
 		System.arraycopy(mNfcId, 0, mBuf, 2, mNfcId.length);
 		mBuf[10] = dataLen;
 		System.arraycopy(data, 0, mBuf, 11, dataLen);
 
 		mBuf[0] = (byte)(11 + dataLen);
 
 		return mBuf[0];
 	}
 
 	public byte pushUrl(String str) {
 		byte[] data = null;
 		byte data_len = 0;
 
 		final byte[] str_byte = str.getBytes();
 		short str_len = (short)(str.length() + 2);
 		data = new byte[256];
 
 		int chksum = 0;
 		byte cnt = 0;
 
 		//
 		data[cnt] = 0x01;		//個別部数
 		chksum += data[cnt] & 0xff;
 		cnt++;
 
 		// header
 		data[cnt] = 0x02; // URL
 		chksum += data[cnt] & 0xff;
 		cnt++;
 		data[cnt] = (byte)(str_len & 0x00ff);
 		chksum += data[cnt] & 0xff;
 		cnt++;
 		data[cnt] = (byte)((str_len & 0xff00) >> 8);
 		chksum += data[cnt] & 0xff;
 		cnt++;
 
 		str_len -= 2;
 
 		// param
 		data[cnt] = (byte)(str_len & 0x00ff);
 		chksum += data[cnt] & 0xff;
 		cnt++;
 		data[cnt] = (byte)((str_len & 0xff00) >> 8);
 		chksum += data[cnt] & 0xff;
 		cnt++;
 		for (int i = 0; i < str_len; i++) {
 			data[cnt] = str_byte[i];
 			chksum += data[cnt] & 0xff;
 			cnt++;
 		}
 
 		// check sum
 		short sum = (short)-chksum;
 		data[cnt] = (byte)((sum & 0xff00) >> 8);
 		cnt++;
 		data[cnt] = (byte)(sum & 0x00ff);
 		cnt++;
 
 		data_len = push(data, cnt);
 
 		return data_len;
 	}
 }
