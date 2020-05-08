 package com.example.calculator;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	//Fixed the Force Close Bug
 	private int option = 0;
 	private boolean newdigital=true;
 	private double a=0,b=0;
 	private double c;
 	private Button button1;
 	private Button button2;
 	private Button button3;
 	private Button button4;
 	private Button button5;
 	private Button button6;
 	private Button button7;
 	private Button button8;
 	private Button button9;
 	private Button button0;
 	private Button buttonPlue;
 	private Button buttonMinus;
 	private Button buttonMultiply;
 	private Button buttonDivideBy;
 	private Button buttonSign;
 	private Button buttondenyu;
 	private Button buttonPoint;
 	private Button buttonClear;
 	private Button buttonSquereRoot;
 	private Button buttonPrime;
 	private Button buttonCos;
 	private Button buttonSin;
 	
 	
 	  public void onCreate(Bundle savedInstanceState)
 	  {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.activity_main);
 	        button0=(Button)findViewById(R.id.btn0);
 	        button1=(Button)findViewById(R.id.btn1);
 	        button2=(Button)findViewById(R.id.btn2);
 	        button3=(Button)findViewById(R.id.btn3);
 	        button4=(Button)findViewById(R.id.btn4);
 	        button5=(Button)findViewById(R.id.btn5);
 	        button6=(Button)findViewById(R.id.btn6);
 	        button7=(Button)findViewById(R.id.btn7);
 	        button8=(Button)findViewById(R.id.btn8);
 	        button9=(Button)findViewById(R.id.btn9);
 	        buttonPlue=(Button)findViewById(R.id.jia);
 	        buttonMinus=(Button)findViewById(R.id.jian);
 	        buttonMultiply=(Button)findViewById(R.id.chen);
 	        buttonDivideBy=(Button)findViewById(R.id.chu);
 	        buttonSign=(Button)findViewById(R.id.zhenfu);
 	        buttondenyu=(Button)findViewById(R.id.denyu);
 	        buttonClear=(Button)findViewById(R.id.qingchu);
 	        buttonPoint=(Button)findViewById(R.id.xiaoshudian);
 	        buttonSquereRoot=(Button)findViewById(R.id.kaifang);
 	    	buttonPrime=(Button)findViewById(R.id.zhishu);
 	        buttonCos=(Button)findViewById(R.id.cos);
 	    	buttonSin=(Button)findViewById(R.id.sin);
 	        
 	        
 	        button0.setOnClickListener(lisenter);
 	        button1.setOnClickListener(lisenter);
 	        button2.setOnClickListener(lisenter);
 	        button3.setOnClickListener(lisenter);
 	        button4.setOnClickListener(lisenter);
 	        button5.setOnClickListener(lisenter);
 	        button6.setOnClickListener(lisenter);
 	        button7.setOnClickListener(lisenter);
 	        button8.setOnClickListener(lisenter);
 	        button9.setOnClickListener(lisenter);
 	        buttonPlue.setOnClickListener(lisenter);
 	        buttonMinus.setOnClickListener(lisenter);
 	        buttonMultiply.setOnClickListener(lisenter);
 	        buttonDivideBy.setOnClickListener(lisenter);
 	        buttondenyu.setOnClickListener(lisenter);
 	        buttonSign.setOnClickListener(lisenter);
 	        buttonClear.setOnClickListener(lisenter);
 	        buttonPoint.setOnClickListener(lisenter);
 	        buttonSquereRoot.setOnClickListener(lisenter);
 	    	buttonPrime.setOnClickListener(lisenter);
 	        buttonCos.setOnClickListener(lisenter);
 	    	buttonSin.setOnClickListener(lisenter);
 	  }
 	  
 	  private OnClickListener lisenter=new OnClickListener()
 	  {
 	
 		public void onClick(View v) 
 		{
 		
 		TextView text = (TextView) findViewById(R.id.text);
 		String s = text.getText().toString();//ȡıʾַ
 		Button btn =(Button)v;
 		String t=(String) btn.getText();//ȡťַ
 		if(btn.getId()==R.id.btn0||btn.getId()==R.id.btn1||btn.getId()==R.id.btn2||btn.getId()==R.id.btn3
 			||btn.getId()==R.id.btn4||btn.getId()==R.id.btn5||btn.getId()==R.id.btn6||
 			btn.getId()==R.id.btn7||btn.getId()==R.id.btn8||btn.getId()==R.id.btn9)
 		{
 			if (newdigital) 
 				{
 					text.setText(s + t);
 				} 
 				else 
 				{
 					text.setText(s);
 					newdigital = false;
 				}return;
 		}
		if(!s.equals("")){
 			if(btn.getId()==R.id.zhenfu)//ı
 			{ 
 	//			c=Double.parseDouble(s);
 	//			text.setText(String.valueOf(-c));
 	//			return;
 				if(s.length()==0)
 				{
 					a=0;
 					b=0;
 					option=0;
 					newdigital=true;
 					text.setText("");
 					return;
 				}
 				else if(s!="")
 					{
 					c=Double.parseDouble(s);
 					text.setText(String.valueOf(-c));
 					newdigital=true;
 					return;
 					}	
 			}
 			
 			if(btn.getId()==R.id.jia)//
 			{ 
 				a=Double.parseDouble(s);
 				option=1;
 				text.setText("");
 				return;
 			}
 			
 			if(btn.getId()==R.id.jian)//
 			{ 
 				a=Double.parseDouble(s);
 				option=2;
 				text.setText("");
 				return;
 			}
 			
 			if(btn.getId()==R.id.chen)//
 			{ 
 				a=Double.parseDouble(s);
 				option=3;
 				text.setText("");
 				return;
 			}
 			
 			if(btn.getId()==R.id.chu)//
 			{ 
 				a=Double.parseDouble(s);
 				option=4;
 				text.setText("");
 				return;
 			}
 			
 			if(btn.getId()==R.id.qingchu)//
 			{ 
 				a=0;
 				b=0;
 				option=0;
 				newdigital=true;
 				text.setText("");
 				return;
 			}
 			if(btn.getId()==R.id.kaifang)//
 			{   
 				if(s.length()==0)
 				{
 					a=0;
 					b=0;
 					option=0;
 					newdigital=true;
 					text.setText("");
 					return;
 				}
 				else if(s!="")
 					{
 					double i=Double.parseDouble(s);
 					if(i>=0)
 					{
 						a=Math.sqrt(i);
 						text.setText(String.valueOf(a));
 						newdigital=true;
 						return;
 					}
 					else 
 					{
 						a=0;
 						b=0;
 						option=0;
 						newdigital=true;
 						text.setText("");
 						return;	
 					}
 					}
 			}
 			
 			if(btn.getId()==R.id.sin)//sin
 			{ 
 				if(s.length()==0)
 				{
 					a=0;
 					b=0;
 					option=0;
 					newdigital=true;
 					text.setText("");
 					return;
 				}
 				else if(s!="")
 					{
 					a=Math.sin(Double.parseDouble(s));
 					text.setText(String.valueOf(a));
 					newdigital=true;
 					return;
 					}			
 			}
 			
 			if(btn.getId()==R.id.cos)//cos
 			{ 
 				if(s.length()==0)
 				{
 					a=0;
 					b=0;
 					option=0;
 					newdigital=true;
 					text.setText("");
 					return;
 				}
 				else if(s!="")
 					{
 					a=Math.cos(Double.parseDouble(s));
 					text.setText(String.valueOf(a));
 					newdigital=true;
 					return;
 					}	
 			}
 			
 			if(btn.getId()==R.id.zhishu)//ָ
 			{ 
 					a = Double.parseDouble(s);
 					option = 5;
 					text.setText("");
 					return;
 			}
 			
 			if(btn.getId()==R.id.xiaoshudian)//С
 			{ 
 				if(s.indexOf(".")==-1)
 					if(s.trim().startsWith("0"))
 					{
 						text.setText("0.");
 						newdigital=true;
 					}
 					else
 					{
 						text.setText(s+".");
 						
 					}
 				return;
 			}
 			
 			if(btn.getId()==R.id.denyu)//ں
 			{ 
 				b=Double.parseDouble(s);
 				switch(option)
 				{
 				case 1:
 					text.setText(String.valueOf(a+b));break;
 				case 2:
 					text.setText(String.valueOf(a-b));break;
 				case 3:
 					text.setText(String.valueOf(a*b));break;
 				case 4:
 				{
 					if(b!=0)
 						{text.setText(String.valueOf(a/b));}
 					else
 						{
 						Toast.makeText(MainActivity.this, "Can not divided by 0", Toast.LENGTH_SHORT).show();
 						text.setText("");
 						a=0;
 						b=0;
 						option=0;
 						newdigital=true;
 						return;
 						}
 					break;
 				}
 				case 5:
 					text.setText(String.valueOf(Math.pow(a, b)));break;
 					
 				}
 				
 				return;
 			}
 		}	
 		}
 		  
 	  };
 	  
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 1, "Exit");
 		menu.add(0, 2, 2, "About");
 		//menu.add(0, 3, 3, "Help");
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getItemId()==1){finish();}
 		if(item.getItemId()==2){Toast.makeText(MainActivity.this, "Author: Haotian Li   li.haotian@yahoo.com", Toast.LENGTH_LONG).show();}
 		//if(item.getItemId()==3){Toast.makeText(MainActivity.this, "Not Done", Toast.LENGTH_LONG).show();}
 		return super.onOptionsItemSelected(item);
 	}
    
 	
 }
