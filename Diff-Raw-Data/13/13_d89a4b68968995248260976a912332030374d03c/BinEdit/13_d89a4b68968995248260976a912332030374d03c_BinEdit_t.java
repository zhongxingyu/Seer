 package yoshikuni.jujo.binEdit;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.widget.TextView;
 import android.widget.Button;
 
 import android.view.View;
 
import android.util.Log;

 public class BinEdit extends Activity
 {
 	private TextView textview;
 	private Edit edit;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		textview = (TextView)findViewById(R.id.textview);
 
 		edit = new Edit();
 
 		setButtonAction();
 	}
 
 	private void setButtonAction()
 	{
 		Button[] btns = new Button[16];
 		Button bsbtn;
 
 		btns[0] = (Button)findViewById(R.id.btn0);
 		btns[1] = (Button)findViewById(R.id.btn1);
 		btns[2] = (Button)findViewById(R.id.btn2);
 		btns[3] = (Button)findViewById(R.id.btn3);
 		btns[4] = (Button)findViewById(R.id.btn4);
 		btns[5] = (Button)findViewById(R.id.btn5);
 		btns[6] = (Button)findViewById(R.id.btn6);
 		btns[7] = (Button)findViewById(R.id.btn7);
 		btns[8] = (Button)findViewById(R.id.btn8);
 		btns[9] = (Button)findViewById(R.id.btn9);
 		btns[10] = (Button)findViewById(R.id.btna);
 		btns[11] = (Button)findViewById(R.id.btnb);
 		btns[12] = (Button)findViewById(R.id.btnc);
 		btns[13] = (Button)findViewById(R.id.btnd);
 		btns[14] = (Button)findViewById(R.id.btne);
 		btns[15] = (Button)findViewById(R.id.btnf);
 		for (int i = 0; i < 16; i++) {
 			btns[i].setOnClickListener(
 				new ButtonClickListener(edit, textview, i));
 		}
 		bsbtn = (Button)findViewById(R.id.backspace);
 		bsbtn.setOnClickListener(
 			new ButtonClickListener(edit, textview, -1));
 	}
 }
 
 class Edit
 {
 	private String str = "";
 	private int num = -1;
 
 	public String get() {
 		if ( num < 0 ) {
 			return str;
 		} else {
 			return str + "^" + num;
 		}
 	}
 
 	public void push(int n) {
 		if (n < 0) {
			if (num > -1) {
				num = -1;
			} else {
				if (!str.equals("")) {
					Log.e("backspace", "before str = " + str);
					str = str.substring(0, str.length() - 1);
					Log.e("backspace", "after str = " + str);
				}
			}
 		} else if (num < 0) {
 			num = n;
 		} else {
 			str += (char)(num << 4 | n);
 			num = -1;
 		}
 	}
 }
 
 class ButtonClickListener implements View.OnClickListener
 {
 	TextView textview;
 	int key;
 	Edit edit;
 
 	ButtonClickListener(Edit ed, TextView tv, int i)
 	{
 		textview = tv;
 		key = i;
 		edit = ed;
 	}
 
 	@Override
 	public void onClick(View view)
 	{
 		edit.push(key);
 		textview.setText(edit.get());
 	}
 }
