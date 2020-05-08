 package com.rava.voting.ui;
 
 import java.math.BigInteger;
 import java.nio.charset.Charset;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.rau.evoting.ElGamal.BigIntegerTypeAdapter;
 import com.rau.evoting.ElGamal.ChaumPedersen;
 import com.rava.voting.R;
 
 public class ReceiptInfoFragment extends Fragment {
 
 	public static final String TAG = "ReceiptInfoFragment";
 
 	// public static final String KEY_A = "KEY_A";
 	// public static final String KEY_B = "KEY_B";
 	// public static final String KEY_MESSAGE = "KEY_MESSAGE";
 
	
	private TextView mTextViewP;
	private TextView mTextViewG;
	private TextView mTextViewY;
 	private TextView mTextViewA;
 	private TextView mTextViewB;
 	private TextView mTextViewMessage;
 	private TextView mTextViewMessageBigInt;
 	private TextView mTextViewY1;
 	private TextView mTextViewY2;
 	private TextView mTextViewA1;
 	private TextView mTextViewA2;
 	private TextView mTextViewC;
 	private TextView mTextViewS;
 	private TextView mTextViewRes1;
 	private TextView mTextViewRes2;
 	private TextView mTextViewNote;
 	private TextView mTextViewNextNote;
 	private Button mButtonNext;
 	private TableRow mTableRowA;
 	private TableRow mTableRowB;
 	private TableRow mTableRowMessage;
 	private TableRow mTableRowMessageBigInt;
 	private TableRow mTableRowStep2;
 	private TableRow mTableRowC;
 	private TableRow mTableRowS;
 	private TableRow mTableRowRes;
 
 	private int clicks = 0;
 
 	private BigInteger p ;
 	private BigInteger g ;
 	private BigInteger y;
 	//private BigInteger r = new BigInteger("43245626364");
 	private BigInteger a;
 	private BigInteger b;
 	private BigInteger y1;
 	private BigInteger y2;
 	private BigInteger a1;
 	private BigInteger a2;
 	private BigInteger k;
 	private BigInteger c;
 	private BigInteger s;
 	private BigInteger res1;
 	private BigInteger res2;
 	
 	private ChaumPedersen mChaumPedersen;
 
 	public static ReceiptInfoFragment newInstance(String content) {
 		ReceiptInfoFragment fragment = new ReceiptInfoFragment();
 		Bundle args = new Bundle();
 		args.putString("content", content);
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View root = inflater.inflate(R.layout.fragment_receipt_info, container,
 				false);
		mTextViewP = (TextView) root.findViewById(R.id.textview_p);
		mTextViewG = (TextView) root.findViewById(R.id.textview_g);
		mTextViewY = (TextView) root.findViewById(R.id.textview_y);
 		mTextViewA = (TextView) root.findViewById(R.id.textview_a);
 		mTextViewB = (TextView) root.findViewById(R.id.textview_b);
 		mTextViewMessage = (TextView) root.findViewById(R.id.textview_message);
 		mTextViewMessageBigInt = (TextView) root
 				.findViewById(R.id.textview_message_bigint);
 		mTextViewY1 = (TextView) root.findViewById(R.id.textview_y1);
 		mTextViewY2 = (TextView) root.findViewById(R.id.textview_y2);
 		mTextViewA1 = (TextView) root.findViewById(R.id.textview_a1);
 		mTextViewA2 = (TextView) root.findViewById(R.id.textview_a2);
 		mTextViewC = (TextView) root.findViewById(R.id.textview_c);
 		mTextViewS = (TextView) root.findViewById(R.id.textview_s);
 		mTextViewRes1 = (TextView) root.findViewById(R.id.textview_res1);
 		mTextViewRes2 = (TextView) root.findViewById(R.id.textview_res2);
 		mTextViewNote = (TextView) root.findViewById(R.id.textview_note);
 		mTextViewNextNote = (TextView) root
 				.findViewById(R.id.textview_next_note);
 		mButtonNext = (Button) root.findViewById(R.id.button_next);
 		mButtonNext.setOnClickListener(new NextClick());
 		mTableRowA = (TableRow) root.findViewById(R.id.tablerow_a);
 		mTableRowB = (TableRow) root.findViewById(R.id.tablerow_b);
 		mTableRowMessage = (TableRow) root.findViewById(R.id.tablerow_message);
 		mTableRowMessageBigInt = (TableRow) root
 				.findViewById(R.id.tablerow_message_bigint);
 		mTableRowStep2 = (TableRow) root.findViewById(R.id.tablerow_step2);
 		mTableRowC = (TableRow) root.findViewById(R.id.tablerow_c);
 		mTableRowS = (TableRow) root.findViewById(R.id.tablerow_s);
 		mTableRowRes = (TableRow) root.findViewById(R.id.tablerow_result);
 		return root;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		Bundle args = getArguments();
 		String content = args.getString("content");
 		
 		Gson gson = new GsonBuilder().registerTypeAdapter(BigInteger.class,
 				new BigIntegerTypeAdapter()).create();
 		mChaumPedersen = gson.fromJson(content, ChaumPedersen.class);
 		
 		a = mChaumPedersen.getA();
 		b = mChaumPedersen.getB();
 		String message = mChaumPedersen.getMessage();
 		
 		p = mChaumPedersen.getP();   
 		g = mChaumPedersen.getG();
 		y = mChaumPedersen.getY();
 		
 		Charset charset = Charset.forName("ISO-8859-1");
 		byte[] bytes = message.getBytes(charset);
 		BigInteger messageBigint = new BigInteger(bytes);
 		y1 = a;
 		y2 = b.divide(messageBigint).mod(p);
 
		mTextViewP.setText(p.toString());
		mTextViewG.setText(g.toString());
		mTextViewY.setText(y.toString());
 		mTextViewA.setText(a.toString());
 		mTextViewB.setText(b.toString());
 		mTextViewMessage.setText(message);
 		mTextViewMessageBigInt.setText(messageBigint.toString());
 		mTextViewY1.setText(y1.toString());
 		mTextViewY2.setText(y2.toString());
 
 	}
 
 	private class NextClick implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			clicks++;
 
 			switch (clicks) {
 			case 1:
 				mTableRowA.setVisibility(View.GONE);
 				mTableRowB.setVisibility(View.GONE);
 				mTableRowMessage.setVisibility(View.GONE);
 				mTableRowMessageBigInt.setVisibility(View.GONE);
 				mTableRowStep2.setVisibility(View.VISIBLE);
 
 				// change to random
 				//k = new BigInteger("1231365");
 				a1 = mChaumPedersen.getA1();// g.modPow(k, p);
 				a2 = mChaumPedersen.getA2(); //y.modPow(k, p);
 
 				mTextViewA1.setText(a1.toString());
 				mTextViewA2.setText(a2.toString());
 
 				mTextViewNote.setText(getResources().getString(R.string.note2));
 				mTextViewNextNote.setText(getResources().getString(
 						R.string.next_note2));
 
 				break;
 
 			case 2:
 				mTableRowC.setVisibility(View.VISIBLE);
 				String temp = y1.toString().concat(y2.toString());
 				long t2 = temp.hashCode();
 				c = BigInteger.valueOf(t2);
 				c = new BigInteger("111");
 
 				mTextViewC.setText(c.toString());
 
 				mTextViewNote.setVisibility(View.GONE);
 				mTextViewNextNote.setText(getResources().getString(
 						R.string.next_note3));
 
 				break;
 
 			case 3:
 				mTableRowS.setVisibility(View.VISIBLE);
 
 				//BigInteger temp2 = c.multiply(r).mod(p);
 				s = mChaumPedersen.getS(); //k.subtract(temp2).mod(p);
 
 				mTextViewS.setText(s.toString());
 
 				// mTextViewNote.setVisibility(View.GONE);
 				mTextViewNextNote.setText(getResources().getString(
 						R.string.next_note4));
 
 				break;
 			case 4:
 				mButtonNext.setVisibility(View.GONE);
 				mTableRowRes.setVisibility(View.VISIBLE);
 
 				res1 = g.modPow(s, p).multiply(y1.modPow(c, p)).mod(p);
 				res2 = y.modPow(s, p).multiply(y2.modPow(c, p)).mod(p);
 
 				mTextViewRes1.setText(res1.toString());
 				mTextViewRes2.setText(res2.toString());
 
 				if (a1.equals(res1) && a2.equals(res2)) {
 					mTextViewNextNote.setText(getResources().getString(
 							R.string.next_note5));
 				} else {
 					mTextViewNextNote.setText(getResources().getString(
 							R.string.next_error));
 					mTextViewNextNote.setTextColor(getResources().getColor(
 							android.R.color.holo_red_dark));
 				}
 
 				break;
 
 			}
 
 		}
 
 	}
 
 }
