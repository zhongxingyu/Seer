 package com.suchangko.moneybook;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ExpandableListView.OnGroupClickListener;
 import android.widget.ExpandableListView.OnGroupCollapseListener;
 import android.widget.ExpandableListView.OnGroupExpandListener;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 /**
  * Created with IntelliJ IDEA.
  * User: SuChang_NT900X
  * Date: 13. 3. 27
  * Time: 오후 4:27
  * To change this template use File | Settings | File Template
 * 수입 수정	탭3 종료시 저장 탭4 눌렀을때 검색 탬3 4 월 이동 월별 연별
  */
 public class Tab_OneActivity extends Activity implements OnClickListener {	
	
 	ArrayList<HashMap<String,String>> list;
 	EditText edt_date; //Dialog
     EditText edt_time; //Dialog
 	Button bt_datepick;
 	Button bt_card;
 	Button bt_detail;
 	AlertDialog dialog_alert;
 	Button bt_middle;
 	Button bt_money;
 	Button Search_date_start;
 	Button Search_date_fin;
 	GregorianCalendar grecal;
 	BaseExpandableAdapter baseadapter;
 	Calendar c;
 	TextView tv_date;
 	FavorSpendDB favorSpendDB;
 	private static final int DIALOG_DATE = 0;
 	private static final int DIALOG_DATE_edt = 1;
 	private static final int DIALOG_TIME_edt = 2;
 	private static final int DIALOG_DATE_start = 3;
 	private static final int DIALOG_DATE_fin = 4;
 	
 	int tmp_moneyint =0;
 	private ArrayList<String> mGroupList = null;
 	private ArrayList<ArrayList<String>> mChildList = null;
 	private ArrayList<String> mChildListContent = null;
 	private ArrayList<String> mChildListContent1 = null;
 	MoneyBookDB mdb;
 	int LastDay=0;
 	ExpandableListView mListView;
 	boolean btmoneyispushed=false;
 	
 	int kindof=0;
 	String middleString="";
 	String detailString="";
 	AlertDialog favorlistdialog;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tab1);
         grecal=new GregorianCalendar();
         c=Calendar.getInstance();
         bt_datepick  = (Button)findViewById(R.id.bt_pickdate);
         bt_datepick.setOnClickListener(this);
         bt_datepick.setText(c.get(Calendar.YEAR)+"년 "+(c.get(Calendar.MONTH)+1)+"월");
         mListView = (ExpandableListView)findViewById(R.id.listview1);
         bt_card = (Button)findViewById(R.id.bt_card);
         bt_card.setOnClickListener(this);
         bt_money = (Button)findViewById(R.id.bt_money);
         bt_money.setOnClickListener(this);
         bt_middle=(Button)findViewById(R.id.bt_middle);
         bt_middle.setOnClickListener(this);
         bt_detail = (Button)findViewById(R.id.bt_detail);
         bt_detail.setOnClickListener(this);
         mdb =  new MoneyBookDB(this,MoneyBookDB.SQL_Create_Moneybook,MoneyBookDB.SQL_DBname);
         mdb.open();
         mGroupList = new ArrayList<String>();
         mChildList = new ArrayList<ArrayList<String>>();
         mChildListContent = new ArrayList<String>();
         mChildListContent1 = new ArrayList<String>();
         
         mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				/*
 				AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 				builder.setTitle("지출 세부 분류 선택");
 				
 					builder.setItems(util.fixdel, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	Toast.makeText(getApplicationContext(),util.fixdel[item],1000).show();
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 					*/
 					
 					
 					  if (mListView.getPackedPositionType(arg3) == mListView.PACKED_POSITION_TYPE_CHILD) {
 						 
 						                      final int groupPosition = ExpandableListView.getPackedPositionGroup(arg3);
 					
 						                      final int childPosition = ExpandableListView.getPackedPositionChild(arg3);
 						                      //Toast.makeText(getApplicationContext(),""+groupPosition+"."+childPosition ,1000).show();
 						                      //Log.d("groupPosition",""+groupPosition);
 						                      //Log.d("childPosition",""+childPosition);
 						                  	AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 						    				builder.setTitle("지출 세부 분류 선택");
 						    				
 						    					builder.setItems(util.fixdel, new DialogInterface.OnClickListener() {
 						    					    public void onClick(DialogInterface dialog, int item) {
 						    					    	//Toast.makeText(getApplicationContext(),util.fixdel[item],1000).show();
 						    					    	
 						    					    	if(util.fixdel[item].equals("삭제")){
 						    					    		//Toast.makeText(getApplicationContext(), baseadapter.getchildintid(groupPosition, childPosition)+"ID",1000).show();
 						    					    		mdb.datadel(""+baseadapter.getchildintid(groupPosition, childPosition));
 						    					    		Toast.makeText(getApplicationContext(),"삭제되었습니다.",Toast.LENGTH_SHORT).show();
 						    					    		madeAdapter();
 						    					    	}else if(util.fixdel[item].equals("수정")){
 						    					    		//수정기능 만들어야함.
 						    					    		AlertDialog aaa = dialog_editing(baseadapter.getchildintid(groupPosition, childPosition));
 						    					    		aaa.show();
 						    					    		
 						    					    	}
 						    					    }
 						    					});
 						    					AlertDialog alert = builder.create();
 						    					alert.show();
 					           return true;
 						  
 						                 }
 
 				return false;
 			}
         	
 		});
 		
         /*
          * http://www.androidpub.com/465319
          *  input : new java.util.Date().getTime()
          *  read :  
          *  1. 
          *  long datetime =  cursor.getLong(rowIndex);
          *  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( datetime); 
          * 2.
          * long _timeMillis = System.currentTimeMillis();
          * DateFormat.format("yyyy-MM-dd HH:mm:ss", _timeMillis).toString());
          * 3.
          * TimeZone timezone = TimeZone.getTimeZone("Etc/GMT-9");
          * TimeZone.setDefault(timezone);
          * */
         
         //ListItem
         //mChildListContent.add("1");
         //mChildListContent.add("2");
         //mChildListContent.add("3");
         madeAdapter();
        
         mListView.setAdapter(baseadapter);
         
         // 그룹 클릭 했을 경우 이벤트
         mListView.setOnGroupClickListener(new OnGroupClickListener() {
             @Override
             public boolean onGroupClick(ExpandableListView parent, View v,
                     int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(), "g click = " + groupPosition, 
                  //       Toast.LENGTH_SHORT).show();
                 return false;
             }
         });
         
         // 차일드 클릭 했을 경우 이벤트
         mListView.setOnChildClickListener(new OnChildClickListener() {
             @Override
             public boolean onChildClick(ExpandableListView parent, View v,
                     int groupPosition, int childPosition, long id) {
               //  Toast.makeText(getApplicationContext(), "c click = " + childPosition, 
                 //        Toast.LENGTH_SHORT).show();
                 return false;
             }
         });
         // 그룹이 닫힐 경우 이벤트
         mListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
             @Override
             public void onGroupCollapse(int groupPosition) {
                 //Toast.makeText(getApplicationContext(), "g Collapse = " + groupPosition, 
                   //      Toast.LENGTH_SHORT).show();
             }
         });
          
         // 그룹이 열릴 경우 이벤트
         mListView.setOnGroupExpandListener(new OnGroupExpandListener() {
             @Override
             public void onGroupExpand(int groupPosition) {
               //  Toast.makeText(getApplicationContext(), "g Expand = " + groupPosition, 
                     //    Toast.LENGTH_SHORT).show();
             }
         });
         
     }
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	// Inflate the menu; this adds items to the action bar if it is present.
 	menu.add(0,0,0,"지출입력").setIcon(R.drawable.ic_menu_add);
 	menu.add(0,1,0,"이전문자등록").setIcon(R.drawable.ic_menu_copy);
 	menu.add(0,2,0,"즐겨찾기편집").setIcon(R.drawable.btn_star_off_disabled_holo_light);
 	menu.add(1,3,0,"지출내역검색").setIcon(R.drawable.ic_menu_search);
 	menu.add(1,4,0,"회사소개").setIcon(R.drawable.ic_menu_notifications);
 	menu.add(1,5,0,"더보기").setIcon(R.drawable.ic_menu_more);	
 	return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		int itemid =item.getItemId();
 		switch (itemid) {
 		case 0:
 			//Toast.makeText(this,"지출입력",100).show();
 			AlertDialog a = dialog_add_spend();
 			a.show();
 			break;
 		case 1:
 			Toast.makeText(this,"이전문자등록",100).show();
 			break;
 		case 2:
 			/*
 			AlertDialog favorlistdialog = dialog_edit_favor();
 			favorlistdialog.show();
 			*/
 			favorlistdialog= dialog_list_favor();
 			favorlistdialog.show();
 			
 			break;
 		case 3:
 			AlertDialog a4 = dialog_search();
 			a4.show();
 			break;
 		case 4:
 			Intent i = new Intent(this,CompanyIntro.class);
 			startActivity(i);
 			break;
 		case 5:
 			Toast.makeText(this,"더보기",100).show();
 			break;
 				}
 		return super.onOptionsItemSelected(item);
 	}
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		if(v.getId()==R.id.bt_pickdate){
 			showDialog(DIALOG_DATE);	
 		}else if(v.getId()==R.id.bt_card){
 			AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 			builder.setTitle("결제 분류 선택");
 			builder.setItems(util.spendhow1, new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int item) {
 			    	if(util.spendhow1[item].equals("현금")){
 			    		bt_card.setText("현금");
 			    		kindof=1;
 			    		madeAdapter();
 			    		
 			    	}else if(util.spendhow1[item].equals("카드")){
 			    		bt_card.setText("카드");
 			    		kindof=2;
 			    		madeAdapter();
 			    	}else if(util.spendhow1[item].equals("전체")){
 			    		bt_card.setText("전체");
 			    		kindof=0;
 			    		madeAdapter();
 			    	}
 			    	
 			    }
 			    
 			    
 			});
 			AlertDialog alert = builder.create();
 
 			alert.show();
 		}else if(v.getId()==R.id.bt_detail){
 			AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 			builder.setTitle("지출 세부 분류 선택");
 			String tmp_middle = bt_middle.getText().toString();
 			if(tmp_middle.equals("식비")){
 				builder.setItems(util.detailitems1, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems1[item]);
 				    	detailString=(String) util.detailitems1[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("교통비")){
 				builder.setItems(util.detailitems2, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems2[item]);
 				    	detailString=(String) util.detailitems2[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("교육비")){
 				builder.setItems(util.detailitems3, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems3[item]);
 				    	detailString=(String) util.detailitems3[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("건강,의료비")){
 				builder.setItems(util.detailitems4, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems4[item]);
 				    	detailString=(String) util.detailitems4[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("통신비")){
 				builder.setItems(util.detailitems5, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems5[item]);
 				    	detailString=(String) util.detailitems5[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("가구집기")){
 				builder.setItems(util.detailitems6, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems6[item]);
 				    	detailString=(String) util.detailitems6[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("주거비")){
 				builder.setItems(util.detailitems7, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems7[item]);
 				    	detailString=(String) util.detailitems7[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("품위유지비")){
 				builder.setItems(util.detailitems8, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems8[item]);
 				    	detailString=(String) util.detailitems8[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("교양,오락비")){
 				builder.setItems(util.detailitems9, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems9[item]);
 				    	detailString=(String) util.detailitems9[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("보험,저축비")){
 				builder.setItems(util.detailitems10, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems10[item]);
 				    	detailString=(String) util.detailitems10[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("사업운영비")){
 				builder.setItems(util.detailitems11, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems11[item]);
 				    	detailString=(String) util.detailitems11[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("수수료,세금")){
 				builder.setItems(util.detailitems12, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems12[item]);
 				    	detailString=(String) util.detailitems12[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else if(tmp_middle.equals("기타")){
 				builder.setItems(util.detailitems13, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	bt_detail.setText(util.detailitems13[item]);
 				    	detailString=(String) util.detailitems13[item];
 				    	madeAdapter();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}else{
 				Toast.makeText(getApplicationContext(), "분류를 선택해주세요.",1000).show();
 			}
 			
 		}else if(v.getId()==R.id.bt_middle){
 			AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 			builder.setTitle("지출 분류 선택");
 			builder.setItems(util.Middleitems1, new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int item) {
 			    	bt_middle.setText(util.Middleitems1[item]);
 			    	middleString=(String) util.Middleitems1[item];
 			    	madeAdapter();
 			    }
 			});
 			AlertDialog alert = builder.create();
 			alert.show();
 		}else if(v.getId()==R.id.bt_money){
 			if(!btmoneyispushed){
 			int tmp_moneyperday=(int)tmp_moneyint/LastDay; 
 			bt_money.setText(tmp_moneyperday+"원/일");
 			btmoneyispushed=true;
 			}else{
 				bt_money.setText(tmp_moneyint+"원");
 				btmoneyispushed=false;
 			}
 		}
 	}
 	 private DatePickerDialog.OnDateSetListener dateListenerstart = 
 		        new DatePickerDialog.OnDateSetListener() {
 		         
 		        @Override
 		        public void onDateSet(DatePicker view, int year, int monthOfYear,
 		                int dayOfMonth) {
 		        	Search_date_start.setText(year+"-"+(monthOfYear+1)+"-"+dayOfMonth);
 		        	grecal = new GregorianCalendar(year,monthOfYear,dayOfMonth);
 		        	c.set(year, monthOfYear, dayOfMonth);
 		        	baseadapter=null;
 	            	
 	            	madeAdapter();
 	            	baseadapter.notifyDataSetChanged();
 	            	mListView.setAdapter(baseadapter);
 		        	
 		        }
 		    };
 	private DatePickerDialog.OnDateSetListener dateListenerfinish = 
 			        new DatePickerDialog.OnDateSetListener() {
 			         
 			        @Override
 			        public void onDateSet(DatePicker view, int year, int monthOfYear,
 			                int dayOfMonth) {
 			        	Search_date_fin.setText(year+"-"+(monthOfYear+1)+"-"+dayOfMonth);
 			        	grecal = new GregorianCalendar(year,monthOfYear,dayOfMonth);
 			        	c.set(year, monthOfYear, dayOfMonth);
 			        	baseadapter=null;
 		            	
 		            	madeAdapter();
 		            	baseadapter.notifyDataSetChanged();
 		            	mListView.setAdapter(baseadapter);
 			        	
 			        }
 			    };
 	 private DatePickerDialog.OnDateSetListener dateListener = 
 		        new DatePickerDialog.OnDateSetListener() {
 		         
 		        @Override
 		        public void onDateSet(DatePicker view, int year, int monthOfYear,
 		                int dayOfMonth) {
 		        	bt_datepick.setText(year+"년 "+(monthOfYear+1)+"월");
 		        	grecal = new GregorianCalendar(year,monthOfYear,dayOfMonth);
 		        	c.set(year, monthOfYear, dayOfMonth);
 		        	baseadapter=null;
 	            	
 	            	madeAdapter();
 	            	baseadapter.notifyDataSetChanged();
 	            	mListView.setAdapter(baseadapter);
 		        	
 		        }
 		    };
 		 private DatePickerDialog.OnDateSetListener edt_dateListener = 
 			        new DatePickerDialog.OnDateSetListener() {
 			         
 			        @Override
 			        public void onDateSet(DatePicker view, int year, int monthOfYear,
 			                int dayOfMonth) {
 			        	//bt_datepick.setText(year+"년 "+(monthOfYear+1)+"월");
 			        	Date tmp_date = new Date(year-1900,monthOfYear,dayOfMonth);
 			        	SimpleDateFormat dateformat = new SimpleDateFormat ( "yyyy-MM-dd", Locale.KOREA );
 			        	edt_date.setText(dateformat.format(tmp_date));
 			        }
 			    };
 	private TimePickerDialog.OnTimeSetListener edt_timeListener = 
 			new TimePickerDialog.OnTimeSetListener() {
 				
 				@Override
 				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 					// TODO Auto-generated method stub
 					SimpleDateFormat timeformat = new SimpleDateFormat ( "HH:mm", Locale.KOREA );
 			        Date tmp_time = new Date (2000,10,20,hourOfDay,minute);
 			        edt_time.setText(timeformat.format(tmp_time));
 				}
 			};
 			
 	 @Override
 	    protected Dialog onCreateDialog(int id){
 	        switch(id){
 	        case DIALOG_DATE:
 	        	Calendar c1 = Calendar.getInstance();
 	            return new DatePickerDialog(this, dateListener,c1.get(Calendar.YEAR),c1.get(Calendar.MONTH),c1.get(Calendar.DAY_OF_MONTH));
 	        case DIALOG_DATE_edt:
 	        	Calendar c2 = Calendar.getInstance();
 	        	return new DatePickerDialog(this, edt_dateListener,c2.get(Calendar.YEAR),c2.get(Calendar.MONTH),c2.get(Calendar.DAY_OF_MONTH));
 	        case DIALOG_TIME_edt:
 	        	Calendar c3 = Calendar.getInstance();
 	        	return new TimePickerDialog(this, edt_timeListener,c3.get(Calendar.HOUR_OF_DAY),c3.get(Calendar.MINUTE),false);
 	        case DIALOG_DATE_start:
 	        	Calendar c4 = Calendar.getInstance();
 	        	return new DatePickerDialog(this, dateListenerstart,c4.get(Calendar.YEAR),c4.get(Calendar.MONTH),c4.get(Calendar.DAY_OF_MONTH));
 	        case DIALOG_DATE_fin:
 	        	Calendar c5 = Calendar.getInstance();
 	        	return new DatePickerDialog(this, dateListenerfinish,c5.get(Calendar.YEAR),c5.get(Calendar.MONTH),c5.get(Calendar.DAY_OF_MONTH));
 	    }
 	        return null;
 	 }
 	 private AlertDialog dialog_list_favor(){
 		 final View innerView = getLayoutInflater().inflate(R.layout.dialog_list_favor, null);
 		 final AlertDialog.Builder ab = new AlertDialog.Builder(this);
 	        ab.setTitle("지출 즐겨찾기 편집");
 	        LinearLayout textView =(LinearLayout) innerView.findViewById(R.id.shortcutrow_nodata);
 	        ListView lview =(ListView)innerView.findViewById(R.id.dial_list);	        
 	        
 	        favorSpendDB = new FavorSpendDB(getApplicationContext(), FavorSpendDB.SQL_Create_favorspenddb,FavorSpendDB.SQL_DBname);
 	        favorSpendDB.open();
 	        
 	        
 	        String[] columns={"content","memo","money","kindof","moneykindof","_id"};
         	String selection="date=?";
         	
         	String selectionArgs="";
         	
 	        Cursor tmpc = favorSpendDB.selectTable(columns,null,null,null,null,null);
 	        if(tmpc.getCount()==0){
 	        	lview.setVisibility(View.GONE);
 	        	textView.setVisibility(View.VISIBLE);
 	        }else{
 	        	
 	        	textView.setVisibility(View.GONE);
 	        	list = new ArrayList<HashMap<String,String>>();
 	        	if(tmpc.moveToNext()){
 	        		do{
 	        			HashMap<String,String> map = new HashMap<String, String>();
 	        			map.put("0",tmpc.getString(0));
 	        			map.put("1",tmpc.getString(1));
 	        			map.put("2",tmpc.getString(2));
 	        			map.put("3",tmpc.getString(3));
 	        			map.put("4",tmpc.getString(4));
 	        			map.put("5",tmpc.getString(5));
 	        			Log.d("0",tmpc.getString(0));
 	        			Log.d("1",tmpc.getString(1));
 	        			Log.d("2",tmpc.getString(2));
 	        			Log.d("3",tmpc.getString(3));
 	        			Log.d("4",tmpc.getString(4));
 	        			Log.d("5",tmpc.getString(5));
 	        			list.add(map);
 	        		}while(tmpc.moveToNext());
 	        	}
 	        	dialogAdapter dAdapter = new dialogAdapter(getApplicationContext(), R.layout.listrow_shortcut,list);
 	        	lview.setAdapter(dAdapter);
 	        	lview.setOnItemClickListener(new OnItemClickListener() {
 	        		
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 						//Toast.makeText(getApplicationContext(), list.size() +" dsa"+ arg2,1000).show();
 						//AlertDialog dialog_ = dialog_edit_favor_edit(list.get(arg2));
 						
 						favorlistdialog.dismiss();
 						HashMap<String,String> h = list.get(arg2);
 						AlertDialog dialog_ = dialog_edit_favor_edit(h);
 						dialog_.show();
 						
 						
 					}
 				});
 	        	lview.setVisibility(View.VISIBLE);
 	        }
 	        
 	        
 	        
 	        ab.setView(innerView);
 	        ab.setPositiveButton("추가",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog.dismiss();
 					AlertDialog alertDialog = dialog_edit_favor();
 					alertDialog.show();
 				}
 			});
 	        ab.setNegativeButton("나가기",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					
 				}
 			});
 	        return ab.create();		 
 	 } 
 	 private AlertDialog dialog_editing(int idnum){
 		 final View innerView = getLayoutInflater().inflate(R.layout.dialog_add_spend, null);
      AlertDialog.Builder ab = new AlertDialog.Builder(this);
      ab.setTitle("지출내역");
      ab.setView(innerView);
      edt_date = (EditText)innerView.findViewById(R.id.dialog_edit_date);
      edt_time = (EditText)innerView.findViewById(R.id.dialog_edit_time);
      final EditText edt_money =(EditText)innerView.findViewById(R.id.dialog_edit_money);
      final EditText edt_content = (EditText)innerView.findViewById(R.id.dialog_edit_content);
      final EditText edt_memo = (EditText)innerView.findViewById(R.id.dialog_edit_memo);
      final EditText edt_middle = (EditText)innerView.findViewById(R.id.dialog_edit_middle);
      final EditText edt_detail =  (EditText)innerView.findViewById(R.id.dialog_edit_detail);
      final EditText edt_spendhow = (EditText)innerView.findViewById(R.id.dialog_edit_spendhow);
      final EditText edt_spend_detail = (EditText)innerView.findViewById(R.id.dialog_edit_spend_detail);
      
      edt_middle.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 					
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 				builder.setTitle("지출 분류 선택");
 				builder.setItems(util.Middleitems, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	edt_middle.setText(util.Middleitems[item]);
 				    	edt_detail.setText("상세선택");
 				    	//Toast.makeText(getApplicationContext(), util.Middleitems[item], Toast.LENGTH_SHORT).show();
 				    }
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		});
      edt_detail.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 				builder.setTitle("지출 세부 분류 선택");
 				String tmp_middle = edt_middle.getText().toString();
 				if(tmp_middle.equals("식비")){
 					builder.setItems(util.detailitems1, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems1[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("교통비")){
 					builder.setItems(util.detailitems2, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems2[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("교육비")){
 					builder.setItems(util.detailitems3, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems3[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("건강,의료비")){
 					builder.setItems(util.detailitems4, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems4[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("통신비")){
 					builder.setItems(util.detailitems5, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems5[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("가구집기")){
 					builder.setItems(util.detailitems6, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems6[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("주거비")){
 					builder.setItems(util.detailitems7, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems7[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("품위유지비")){
 					builder.setItems(util.detailitems8, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems8[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("교양,오락비")){
 					builder.setItems(util.detailitems9, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems9[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("보험,저축비")){
 					builder.setItems(util.detailitems10, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems10[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("사업운영비")){
 					builder.setItems(util.detailitems11, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems11[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("수수료,세금")){
 					builder.setItems(util.detailitems12, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems12[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else if(tmp_middle.equals("기타")){
 					builder.setItems(util.detailitems13, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_detail.setText(util.detailitems13[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}else{
 					Toast.makeText(getApplicationContext(), "분류를 선택해주세요.",1000).show();
 				}
 			}
 		});
      //Calendar cc = Calendar.getInstance();
      //String tmp_date = cc.get(Calendar.YEAR)+"-"+(cc.get(Calendar.MONTH)+1)+"-"+cc.get(Calendar.DAY_OF_MONTH);
      //String tmp_time =  cc.get(Calendar.HOUR_OF_DAY)+":"+cc.get(Calendar.MINUTE);
      edt_spendhow.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 				builder.setTitle("결제 분류 선택");
 				builder.setItems(util.spendhow, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	if(util.spendhow[item].equals("현금")){
 				    		edt_spend_detail.setText("현금");
 				    	}else{
 				    		edt_spend_detail.setText("-");
 				    	}
 				    	edt_spendhow.setText(util.spendhow[item]);
 				    }
 				});
 				AlertDialog alert = builder.create();
 
 				alert.show();
 			}
 		});
      edt_spend_detail.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(edt_spend_detail.getText().toString().equals("현금")){
 					Toast.makeText(getApplicationContext(), "현금으로 선택되었습니다.", Toast.LENGTH_SHORT).show();
 				}else{
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("결제 분류 선택");
 					builder.setItems(util.spendkindof, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_spend_detail.setText(util.spendkindof[item]);
 					    	
 					    	
 					    }
 					});
 					AlertDialog alert = builder.create();
 
 					alert.show();
 				}
 			}
 		});
      
      SimpleDateFormat formatter1 = new SimpleDateFormat ( "yyyy-MM-dd", Locale.KOREA );
      final Date currentDate = new Date ( );
      String dDate = formatter1.format ( currentDate );
      SimpleDateFormat formatter2 = new SimpleDateFormat ( "HH:mm", Locale.KOREA );
      Date currentTime = new Date ( );
      String dTime = formatter2.format ( currentTime );
      
      edt_date.setText(dDate);
      edt_time.setText(dTime);
      
      edt_date.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				showDialog(DIALOG_DATE_edt);
 			}
 		});
      edt_time.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				showDialog(DIALOG_TIME_edt);
 			}
 		});
      
      ////////////////////////////////*************************//
      String[] columns={"content","memo","money","kindof","date","minutetime","moneykindof","_id"};
      String selection="_id=?";
      String[] selectionArgs={String.valueOf(idnum)};
      
      
      
      Cursor cursor = mdb.selectTable(columns, selection, selectionArgs,null,null,null);
      cursor.moveToNext();
     
      Date dated = new Date(Long.parseLong(cursor.getString(4)));
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      edt_date.setText(format.format(dated));
      edt_time.setText(cursor.getString(5));
      edt_money.setText(cursor.getString(2));
      edt_content.setText(cursor.getString(0));
      edt_memo.setText(cursor.getString(1));
      
      String[] tmp_t=cursor.getString(3).split("\\+");
      edt_middle.setText(tmp_t[0]);
      edt_detail.setText(tmp_t[1]);
      String[] tmp_a=cursor.getString(6).split("/");
      edt_spendhow.setText(tmp_a[0]);
      edt_spend_detail.setText(tmp_a[1]);
      final String idnumber = cursor.getString(7);
      ab.setPositiveButton("수정", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface arg0, int arg1) {
          	Log.d("edt_date",edt_date.getText().toString());
          	String[] tmp_str_date = edt_date.getText().toString().split("-");
          	Log.d("Good",tmp_str_date[0]);
          	Log.d("Good",tmp_str_date[1]);
          	Log.d("Good",tmp_str_date[2]);
          	Log.d("Hello", "Good");
          	
          
          	Date tmp_date = new Date(Integer.parseInt(tmp_str_date[0])-1900,Integer.parseInt(tmp_str_date[1])-1,Integer.parseInt(tmp_str_date[2]));
          	String aaa = String.valueOf(currentDate.getTime());
          	aaa = ""+tmp_date.getTime();
          	Log.d("",aaa);
          	//Date tmp_date_sql = new Date(edt_date.getText().toString());
          	Log.d("", "1");
          	ContentValues val = new ContentValues();
          	val.put("content",edt_content.getText().toString());
          	val.put("memo",edt_memo.getText().toString());
          	val.put("money",Integer.parseInt(edt_money.getText().toString()));
          	//val.put("date",Integer.parseInt(""+tmp_date_sql.getTime()));
          	val.put("date",aaa);
          	val.put("kindof",edt_middle.getText().toString()+"+"+edt_detail.getText().toString());
          	val.put("moneykindof",edt_spendhow.getText().toString()+"/"+edt_spend_detail.getText().toString());
          	val.put("minutetime",edt_time.getText().toString());
          	mdb.datadel(idnumber);
          	mdb.insertTable(val);
          	
          	
          	Log.d("", "2");
          	baseadapter=null;
          	
          	madeAdapter();
          	baseadapter.notifyDataSetChanged();
          	mListView.setAdapter(baseadapter);
          }
      });/*
       ab.setNeutralButton("즐겨찾기",new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				dialog_alert = dialog_list_favor_input();
 				dialog_alert.show();
 			}
 		});
 		*/
      ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface arg0, int arg1) {
            //  setDismiss(mDialog);
          }
      });
        
      return ab.create();
 	 }
 	 private AlertDialog dialog_satus(int idnum){
 		 final View innerView = getLayoutInflater().inflate(R.layout.dialog_spstatus, null);
 		 final AlertDialog.Builder ab = new AlertDialog.Builder(this);
 	        ab.setTitle("지출 즐겨찾기 편집");
 	        TextView tv1 = (TextView)innerView.findViewById(R.id.dialog_spstatus_date);
 	        TextView tv2 = (TextView)innerView.findViewById(R.id.dialog_spstatus_price);
 	        TextView tv3 = (TextView)innerView.findViewById(R.id.dialog_spstatus_card);
 	        TextView tv4 = (TextView)innerView.findViewById(R.id.dialog_spstatus_cate);
 	        TextView tv5 = (TextView)innerView.findViewById(R.id.dialog_spstatus_memo);
 	        
 	        
 	        String[] columns={"content","memo","money","kindof","date","minutetime","moneykindof","_id"};
 	        String selection="_id=?";
 	        String[] selectionArgs={String.valueOf(idnum)};
 	        
 	        
 	        
 	        Cursor cursor = mdb.selectTable(columns, selection, selectionArgs,null,null,null);
 	        cursor.moveToNext();
 	       
 	        Date dated = new Date(Long.parseLong(cursor.getString(4)));
 	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 	        
 	        tv1.setText(format.format(dated));
 	        tv2.setText(cursor.getString(2));
 	        tv3.setText(cursor.getString(6));
 	        tv4.setText(cursor.getString(3));
 	        tv5.setText(cursor.getString(1));
 	        
 	        
 	        
 	        ab.setView(innerView);
 	        ab.setPositiveButton("추가",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					/*
 					dialog.dismiss();
 					AlertDialog alertDialog = dialog_edit_favor();
 					alertDialog.show();
 					*/
 				}
 			});
 	        ab.setNegativeButton("나가기",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					
 				}
 			});
 	        return ab.create();		 
 	 } 
 	 private AlertDialog dialog_list_favor_input(){
 		 final View innerView = getLayoutInflater().inflate(R.layout.dialog_list_favor, null);
 		 final AlertDialog.Builder ab = new AlertDialog.Builder(this);
 	        ab.setTitle("지출 즐겨찾기 편집");
 	        LinearLayout textView =(LinearLayout) innerView.findViewById(R.id.shortcutrow_nodata);
 	        ListView lview =(ListView)innerView.findViewById(R.id.dial_list);	        
 	        
 	        favorSpendDB = new FavorSpendDB(getApplicationContext(), FavorSpendDB.SQL_Create_favorspenddb,FavorSpendDB.SQL_DBname);
 	        favorSpendDB.open();
 	        
 	        
 	        String[] columns={"content","memo","money","kindof","moneykindof","_id"};
         	String selection="date=?";
         	
         	String selectionArgs="";
         	
 	        Cursor tmpc = favorSpendDB.selectTable(columns,null,null,null,null,null);
 	        if(tmpc.getCount()==0){
 	        	lview.setVisibility(View.GONE);
 	        	textView.setVisibility(View.VISIBLE);
 	        }else{
 	        	
 	        	textView.setVisibility(View.GONE);
 	        	list = new ArrayList<HashMap<String,String>>();
 	        	if(tmpc.moveToNext()){
 	        		do{
 	        			HashMap<String,String> map = new HashMap<String, String>();
 	        			map.put("0",tmpc.getString(0));
 	        			map.put("1",tmpc.getString(1));
 	        			map.put("2",tmpc.getString(2));
 	        			map.put("3",tmpc.getString(3));
 	        			map.put("4",tmpc.getString(4));
 	        			map.put("5",tmpc.getString(5));
 	        			Log.d("0",tmpc.getString(0));
 	        			Log.d("1",tmpc.getString(1));
 	        			Log.d("2",tmpc.getString(2));
 	        			Log.d("3",tmpc.getString(3));
 	        			Log.d("4",tmpc.getString(4));
 	        			Log.d("5",tmpc.getString(5));
 	        			list.add(map);
 	        		}while(tmpc.moveToNext());
 	        	}
 	        	dialogAdapter dAdapter = new dialogAdapter(getApplicationContext(), R.layout.listrow_shortcut,list);
 	        	lview.setAdapter(dAdapter);
 	        	lview.setOnItemClickListener(new OnItemClickListener() {
 	        		
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 						//Toast.makeText(getApplicationContext(), list.size() +" dsa"+ arg2,1000).show();
 						//AlertDialog dialog_ = dialog_edit_favor_edit(list.get(arg2));
 						
 						dialog_alert.dismiss();
 						HashMap<String,String> h = list.get(arg2);
 						AlertDialog dialog_ = dialog_add_spend(h);
 						dialog_.show();
 						
 						
 					}
 				});
 	        	lview.setVisibility(View.VISIBLE);
 	        }
 	        
 	        
 	        
 	        ab.setView(innerView);
 	    
 	        return ab.create();		 
 	 }
 	 private AlertDialog dialog_edit_favor_edit(final HashMap<String,String> map){
 		 final View innerView = getLayoutInflater().inflate(R.layout.dialog_favorite_edit, null);
 		 AlertDialog.Builder ab = new AlertDialog.Builder(this);
 		 LinearLayout linearLayout = (LinearLayout)innerView.findViewById(R.id.dial_layout_auto);
 		 linearLayout.setVisibility(View.GONE);
 		 
 		 final EditText edt_money =(EditText)innerView.findViewById(R.id.dialog_edit_money);
 	        final EditText edt_content = (EditText)innerView.findViewById(R.id.dialog_edit_content);
 	        final EditText edt_memo = (EditText)innerView.findViewById(R.id.dialog_edit_memo);
 	        final EditText edt_middle = (EditText)innerView.findViewById(R.id.dialog_edit_middle);
 	        final EditText edt_detail =  (EditText)innerView.findViewById(R.id.dialog_edit_detail);
 	        final EditText edt_spendhow = (EditText)innerView.findViewById(R.id.dialog_edit_spendhow);
 	        final EditText edt_spend_detail = (EditText)innerView.findViewById(R.id.dialog_edit_spend_detail);
 	        edt_money.setText(map.get("2"));
 	        edt_memo.setText(map.get("1"));
 	        edt_content.setText(map.get("0"));
 	        String tmp_String1 = map.get("3");
 	        String tmp_String2 = map.get("4");
 	        String[] tmp_arrStrings = tmp_String1.split("\\+");
 	        String[] tmp_arrStrings1 = tmp_String2.split("/");
 	        edt_middle.setText(tmp_arrStrings[0]);
 	        edt_detail.setText(tmp_arrStrings[1]);
 	        edt_spendhow.setText(tmp_arrStrings1[0]);
 	        edt_spend_detail.setText(tmp_arrStrings1[1]);
 	        
 	        edt_middle.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 						
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this); 
 					builder.setTitle("지출 분류 선택");
 					builder.setItems(util.Middleitems, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_middle.setText(util.Middleitems[item]);
 					    	edt_detail.setText("상세선택");
 					    	//Toast.makeText(getApplicationContext(), util.Middleitems[item], Toast.LENGTH_SHORT).show();
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			});
 	        edt_detail.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("지출 세부 분류 선택");
 					String tmp_middle = edt_middle.getText().toString();
 					if(tmp_middle.equals("식비")){
 						builder.setItems(util.detailitems1, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems1[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교통비")){
 						builder.setItems(util.detailitems2, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems2[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교육비")){
 						builder.setItems(util.detailitems3, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems3[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("건강,의료비")){
 						builder.setItems(util.detailitems4, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems4[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("통신비")){
 						builder.setItems(util.detailitems5, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems5[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("가구집기")){
 						builder.setItems(util.detailitems6, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems6[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("주거비")){
 						builder.setItems(util.detailitems7, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems7[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("품위유지비")){
 						builder.setItems(util.detailitems8, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems8[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교양,오락비")){
 						builder.setItems(util.detailitems9, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems9[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("보험,저축비")){
 						builder.setItems(util.detailitems10, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems10[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("사업운영비")){
 						builder.setItems(util.detailitems11, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems11[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("수수료,세금")){
 						builder.setItems(util.detailitems12, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems12[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("기타")){
 						builder.setItems(util.detailitems13, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems13[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else{
 						Toast.makeText(getApplicationContext(), "분류를 선택해주세요.",1000).show();
 					}
 				}
 			});
 	        //Calendar cc = Calendar.getInstance();
 	        //String tmp_date = cc.get(Calendar.YEAR)+"-"+(cc.get(Calendar.MONTH)+1)+"-"+cc.get(Calendar.DAY_OF_MONTH);
 	        //String tmp_time =  cc.get(Calendar.HOUR_OF_DAY)+":"+cc.get(Calendar.MINUTE);
 	        edt_spendhow.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("결제 분류 선택");
 					builder.setItems(util.spendhow, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	if(util.spendhow[item].equals("현금")){
 					    		edt_spend_detail.setText("현금");
 					    	}else{
 					    		edt_spend_detail.setText("-");
 					    	}
 					    	edt_spendhow.setText(util.spendhow[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 
 					alert.show();
 				}
 			});
 	        edt_spend_detail.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if(edt_spend_detail.getText().toString().equals("현금")){
 						Toast.makeText(getApplicationContext(), "현금으로 선택되었습니다.", Toast.LENGTH_SHORT).show();
 					}else{
 						AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 						builder.setTitle("결제 분류 선택");
 						builder.setItems(util.spendkindof, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_spend_detail.setText(util.spendkindof[item]);
 						    	
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 	
 						alert.show();
 					}
 				}
 			});
 		 
 	        ab.setTitle("지출 즐겨찾기 편집");
 	        ab.setView(innerView);
 	        ab.setPositiveButton("입력",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					ContentValues val = new ContentValues();
 					val.put("content",edt_content.getText().toString());
 	            	val.put("memo",edt_memo.getText().toString());
 	            	val.put("money",Integer.parseInt(edt_money.getText().toString()));
 	            	val.put("kindof",edt_middle.getText().toString()+"+"+edt_detail.getText().toString());
 	            	val.put("moneykindof",edt_spendhow.getText().toString()+"/"+edt_spend_detail.getText().toString());
 					favorSpendDB.datadel(map.get("5"));
 	            	favorSpendDB.insertTable(val);
 					Toast.makeText(getApplicationContext(), "입력되었습니다.",Toast.LENGTH_SHORT).show();
 					favorlistdialog = dialog_list_favor();
 					favorlistdialog.show();
 				}
 			});
 	        ab.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					favorSpendDB.datadel(map.get("5"));
 					favorlistdialog = dialog_list_favor();
 					favorlistdialog.show();
 				}
 			});
 	        ab.setNegativeButton("취소",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					favorlistdialog = dialog_list_favor();
 					favorlistdialog.show();
 				}
 			});
 	        return ab.create();
 	 }
 	 private AlertDialog dialog_edit_favor(){
 		 final View innerView = getLayoutInflater().inflate(R.layout.dialog_favorite_edit, null);
 		 AlertDialog.Builder ab = new AlertDialog.Builder(this);
 		 LinearLayout linearLayout = (LinearLayout)innerView.findViewById(R.id.dial_layout_auto);
 		 linearLayout.setVisibility(View.GONE);
 		 
 		 final EditText edt_money =(EditText)innerView.findViewById(R.id.dialog_edit_money);
 	        final EditText edt_content = (EditText)innerView.findViewById(R.id.dialog_edit_content);
 	        final EditText edt_memo = (EditText)innerView.findViewById(R.id.dialog_edit_memo);
 	        final EditText edt_middle = (EditText)innerView.findViewById(R.id.dialog_edit_middle);
 	        final EditText edt_detail =  (EditText)innerView.findViewById(R.id.dialog_edit_detail);
 	        final EditText edt_spendhow = (EditText)innerView.findViewById(R.id.dialog_edit_spendhow);
 	        final EditText edt_spend_detail = (EditText)innerView.findViewById(R.id.dialog_edit_spend_detail);
 	        
 	        edt_middle.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 						
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("지출 분류 선택");
 					builder.setItems(util.Middleitems, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_middle.setText(util.Middleitems[item]);
 					    	edt_detail.setText("상세선택");
 					    	//Toast.makeText(getApplicationContext(), util.Middleitems[item], Toast.LENGTH_SHORT).show();
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			});
 	        edt_detail.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("지출 세부 분류 선택");
 					String tmp_middle = edt_middle.getText().toString();
 					if(tmp_middle.equals("식비")){
 						builder.setItems(util.detailitems1, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems1[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교통비")){
 						builder.setItems(util.detailitems2, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems2[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교육비")){
 						builder.setItems(util.detailitems3, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems3[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("건강,의료비")){
 						builder.setItems(util.detailitems4, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems4[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("통신비")){
 						builder.setItems(util.detailitems5, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems5[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("가구집기")){
 						builder.setItems(util.detailitems6, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems6[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("주거비")){
 						builder.setItems(util.detailitems7, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems7[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("품위유지비")){
 						builder.setItems(util.detailitems8, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems8[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교양,오락비")){
 						builder.setItems(util.detailitems9, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems9[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("보험,저축비")){
 						builder.setItems(util.detailitems10, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems10[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("사업운영비")){
 						builder.setItems(util.detailitems11, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems11[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("수수료,세금")){
 						builder.setItems(util.detailitems12, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems12[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("기타")){
 						builder.setItems(util.detailitems13, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems13[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else{
 						Toast.makeText(getApplicationContext(), "분류를 선택해주세요.",1000).show();
 					}
 				}
 			});
 	        //Calendar cc = Calendar.getInstance();
 	        //String tmp_date = cc.get(Calendar.YEAR)+"-"+(cc.get(Calendar.MONTH)+1)+"-"+cc.get(Calendar.DAY_OF_MONTH);
 	        //String tmp_time =  cc.get(Calendar.HOUR_OF_DAY)+":"+cc.get(Calendar.MINUTE);
 	        edt_spendhow.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("결제 분류 선택");
 					builder.setItems(util.spendhow, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	if(util.spendhow[item].equals("현금")){
 					    		edt_spend_detail.setText("현금");
 					    	}else{
 					    		edt_spend_detail.setText("-");
 					    	}
 					    	edt_spendhow.setText(util.spendhow[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 
 					alert.show();
 				}
 			});
 	        edt_spend_detail.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if(edt_spend_detail.getText().toString().equals("현금")){
 						Toast.makeText(getApplicationContext(), "현금으로 선택되었습니다.", Toast.LENGTH_SHORT).show();
 					}else{
 						AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 						builder.setTitle("결제 분류 선택");
 						builder.setItems(util.spendkindof, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_spend_detail.setText(util.spendkindof[item]);
 						    	
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 	
 						alert.show();
 					}
 				}
 			});
 		 
 	        ab.setTitle("지출 즐겨찾기 편집");
 	        ab.setView(innerView);
 	        ab.setPositiveButton("입력",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					ContentValues val = new ContentValues();
 					val.put("content",edt_content.getText().toString());
 	            	val.put("memo",edt_memo.getText().toString());
 	            	val.put("money",Integer.parseInt(edt_money.getText().toString()));
 	            	val.put("kindof",edt_middle.getText().toString()+"+"+edt_detail.getText().toString());
 	            	val.put("moneykindof",edt_spendhow.getText().toString()+"/"+edt_spend_detail.getText().toString());
 					favorSpendDB.insertTable(val);
 					Toast.makeText(getApplicationContext(), "입력되었습니다.",Toast.LENGTH_SHORT).show();
 					favorlistdialog = dialog_list_favor();
 					favorlistdialog.show();
 				}
 			});
 	        ab.setNegativeButton("취소",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					favorlistdialog = dialog_list_favor();
 					favorlistdialog.show();
 				}
 			});
 	        return ab.create();
 	 }
 	 private AlertDialog dialog_search(){
 		 final View innerView = getLayoutInflater().inflate(R.layout.dialog_searchs, null);
 		 AlertDialog.Builder ab = new AlertDialog.Builder(this);
 	        ab.setTitle("지출 내역 검색");
 	        
 	        final EditText editText_1 = (EditText)innerView.findViewById(R.id.dialog_searchs_word);
 	        //editText_1.setHint("검색어를 입력해 주세요");
 	        
 	        Search_date_start = (Button)innerView.findViewById(R.id.dialog_searchs_startdate);
 	        Search_date_start.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("검색 시작일 설정");
 					builder.setItems(util.searchdate, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	if(util.searchdate[item].equals("전체 기간")){
 					    		
 					    	}else if(util.searchdate[item].equals("기간 설정")){
 					    		showDialog(DIALOG_DATE_start);
 					    	}				    	
 					    }
 					    
 					    
 					});
 					AlertDialog alert = builder.create();
 
 					alert.show();
 				}
 			});
 	        Search_date_fin = (Button)innerView.findViewById(R.id.dialog_searchs_enddate);
 	        Search_date_fin.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("검색 시작일 설정");
 					builder.setItems(util.searchdate, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	if(util.searchdate[item].equals("전체 기간")){
 					    		
 					    	}else if(util.searchdate[item].equals("기간 설정")){
 					    		showDialog(DIALOG_DATE_fin);
 					    	}				    	
 					    }
 					    
 					    
 					});
 					AlertDialog alert = builder.create();
 
 					alert.show();
 				}
 			});
 	        final Button b3 = (Button)innerView.findViewById(R.id.dialog_searchs_cate);
 	        final Button b4 = (Button)innerView.findViewById(R.id.dialog_searchs_subcate);
 	        b3.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("검색 분류 선택");
 					builder.setItems(util.Middleitems1, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	b3.setText(util.Middleitems1[item]);
 					    	if(b3.getText().toString().equals("전체")){
 					    		b4.setText("전체");	
 					    	}
 					    	
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			});
 	       
 	        b4.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("검색 세부 분류 선택");
 					String tmp_middle = b3.getText().toString();
 					if(tmp_middle.equals("식비")){
 						builder.setItems(util.detailitems1, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems1[item]);
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교통비")){
 						builder.setItems(util.detailitems2, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems2[item]);
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교육비")){
 						builder.setItems(util.detailitems3, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems3[item]);
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("건강,의료비")){
 						builder.setItems(util.detailitems4, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems4[item]);
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("통신비")){
 						builder.setItems(util.detailitems5, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems5[item]);
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("가구집기")){
 						builder.setItems(util.detailitems6, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems6[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("주거비")){
 						builder.setItems(util.detailitems7, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems7[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("품위유지비")){
 						builder.setItems(util.detailitems8, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems8[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교양,오락비")){
 						builder.setItems(util.detailitems9, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems9[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("보험,저축비")){
 						builder.setItems(util.detailitems10, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems10[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("사업운영비")){
 						builder.setItems(util.detailitems11, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems11[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("수수료,세금")){
 						builder.setItems(util.detailitems12, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems12[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("기타")){
 						builder.setItems(util.detailitems13, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	b4.setText(util.detailitems13[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("전체")){						
 						
 					}else{
 						Toast.makeText(getApplicationContext(), "분류를 선택해주세요.",1000).show();
 					}
 				}
 			});
 	        final Button b5 = (Button)innerView.findViewById(R.id.dialog_searchs_card);
 	        b5.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("검색 카드 선택");
 					builder.setItems(util.searchcard, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	b5.setText(util.searchcard[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			});
 	        ab.setView(innerView);
 	        ab.setPositiveButton("검색",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					if(editText_1.getText().toString().equals("")){
 						Toast.makeText(getApplicationContext(), "검색어를 입력해 주세요.",Toast.LENGTH_SHORT).show();
 					}else{
 						if(!Search_date_start.getText().toString().equals("") && !Search_date_fin.getText().toString().equals("")){
 							//날짜 검색
 							Calendar calendar1 = Calendar.getInstance();
 							Calendar calendar2 = Calendar.getInstance();
 							String[] strs1 = Search_date_start.getText().toString().split("\\-");
 							String[] strs2 = Search_date_fin.getText().toString().split("\\-");
 							
 							calendar1.set(Integer.parseInt(strs1[0]),Integer.parseInt(strs1[1])-1,Integer.parseInt(strs1[2]));
 							calendar2.set(Integer.parseInt(strs2[0]),Integer.parseInt(strs2[1])-1,Integer.parseInt(strs2[2]));
 							
 							
 							int[] cs = {calendar1.get(Calendar.YEAR),
 									(calendar1.get(Calendar.MONTH)+1),
 									calendar1.get(Calendar.DAY_OF_MONTH)};
 							int[] cf = {calendar2.get(Calendar.YEAR),
 									(calendar2.get(Calendar.MONTH)+1),
 									calendar2.get(Calendar.DAY_OF_MONTH)};
 							Date dates = new Date(cs[0]-1900, cs[1]-1, cs[2]);
 							Date datef = new Date(cf[0]-1900, cf[1]-1, cf[2]);
 							
 							if(dates.getTime() > datef.getTime()){
 								Toast.makeText(getApplicationContext(), "검색 기간을 확인해 주세요.",Toast.LENGTH_SHORT).show();
 							}else{
 							
 							
 							String searchstr = editText_1.getText().toString();
 							String kind1 = b3.getText().toString();
 							String kind2 = b4.getText().toString();
 							String card = b5.getText().toString();
 							
 							
 							Intent i = new Intent(getApplicationContext(),SearcheActivity.class);
 							i.putExtra("cs",cs);
 							i.putExtra("cf",cf);
 							i.putExtra("str",searchstr);
 							i.putExtra("kind",kind1+"+"+kind2);
 							i.putExtra("card",card);
 							startActivity(i);
 							}
 							
 							
 
 							
 							/*
 							calendar1.get(Calendar.YEAR)
 							(calendar1.get(Calendar.MONTH)+1)
 							calendar1.get(Calendar.DAY_OF_MONTH)
 							*/
 						}else if(Search_date_start.getText().toString().equals("") && Search_date_fin.getText().toString().equals("")){
 							//전체검색
 							
 							int[] cs = {0,0,0};
 							int[] cf = {0,0,0};
 							
 							String searchstr = editText_1.getText().toString();
 							String kind1 = b3.getText().toString();
 							String kind2 = b4.getText().toString();
 							String card = b5.getText().toString();
 							
 							
 							Intent i = new Intent(getApplicationContext(),SearcheActivity.class);
 							i.putExtra("cs",cs);
 							i.putExtra("cf",cf);
 							i.putExtra("str",searchstr);
 							i.putExtra("kind",kind1+"+"+kind2);
 							i.putExtra("card",card);
 							startActivity(i);
 							
 						}else{
 							Toast.makeText(getApplicationContext(), "검색 기간을 확인해 주세요.",Toast.LENGTH_SHORT).show();
 						}
 					}
 					
 				}
 			});
 	        ab.setNegativeButton("취소",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					
 				}
 			});
 	        return ab.create();
 	 }
 	 private AlertDialog dialog_add_spend() {
 	        final View innerView = getLayoutInflater().inflate(R.layout.dialog_add_spend, null);
 	        AlertDialog.Builder ab = new AlertDialog.Builder(this);
 	        ab.setTitle("지출내역");
 	        ab.setView(innerView);
 	        edt_date = (EditText)innerView.findViewById(R.id.dialog_edit_date);
 	        edt_time = (EditText)innerView.findViewById(R.id.dialog_edit_time);
 	        final EditText edt_money =(EditText)innerView.findViewById(R.id.dialog_edit_money);
 	        final EditText edt_content = (EditText)innerView.findViewById(R.id.dialog_edit_content);
 	        final EditText edt_memo = (EditText)innerView.findViewById(R.id.dialog_edit_memo);
 	        final EditText edt_middle = (EditText)innerView.findViewById(R.id.dialog_edit_middle);
 	        final EditText edt_detail =  (EditText)innerView.findViewById(R.id.dialog_edit_detail);
 	        final EditText edt_spendhow = (EditText)innerView.findViewById(R.id.dialog_edit_spendhow);
 	        final EditText edt_spend_detail = (EditText)innerView.findViewById(R.id.dialog_edit_spend_detail);
 	        
 	        edt_middle.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 						
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("지출 분류 선택");
 					builder.setItems(util.Middleitems, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_middle.setText(util.Middleitems[item]);
 					    	edt_detail.setText("상세선택");
 					    	//Toast.makeText(getApplicationContext(), util.Middleitems[item], Toast.LENGTH_SHORT).show();
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			});
 	        edt_detail.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("지출 세부 분류 선택");
 					String tmp_middle = edt_middle.getText().toString();
 					if(tmp_middle.equals("식비")){
 						builder.setItems(util.detailitems1, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems1[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교통비")){
 						builder.setItems(util.detailitems2, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems2[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교육비")){
 						builder.setItems(util.detailitems3, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems3[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("건강,의료비")){
 						builder.setItems(util.detailitems4, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems4[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("통신비")){
 						builder.setItems(util.detailitems5, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems5[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("가구집기")){
 						builder.setItems(util.detailitems6, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems6[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("주거비")){
 						builder.setItems(util.detailitems7, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems7[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("품위유지비")){
 						builder.setItems(util.detailitems8, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems8[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교양,오락비")){
 						builder.setItems(util.detailitems9, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems9[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("보험,저축비")){
 						builder.setItems(util.detailitems10, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems10[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("사업운영비")){
 						builder.setItems(util.detailitems11, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems11[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("수수료,세금")){
 						builder.setItems(util.detailitems12, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems12[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("기타")){
 						builder.setItems(util.detailitems13, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems13[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else{
 						Toast.makeText(getApplicationContext(), "분류를 선택해주세요.",1000).show();
 					}
 				}
 			});
 	        //Calendar cc = Calendar.getInstance();
 	        //String tmp_date = cc.get(Calendar.YEAR)+"-"+(cc.get(Calendar.MONTH)+1)+"-"+cc.get(Calendar.DAY_OF_MONTH);
 	        //String tmp_time =  cc.get(Calendar.HOUR_OF_DAY)+":"+cc.get(Calendar.MINUTE);
 	        edt_spendhow.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("결제 분류 선택");
 					builder.setItems(util.spendhow, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	if(util.spendhow[item].equals("현금")){
 					    		edt_spend_detail.setText("현금");
 					    	}else{
 					    		edt_spend_detail.setText("-");
 					    	}
 					    	edt_spendhow.setText(util.spendhow[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 
 					alert.show();
 				}
 			});
 	        edt_spend_detail.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if(edt_spend_detail.getText().toString().equals("현금")){
 						Toast.makeText(getApplicationContext(), "현금으로 선택되었습니다.", Toast.LENGTH_SHORT).show();
 					}else{
 						AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 						builder.setTitle("결제 분류 선택");
 						builder.setItems(util.spendkindof, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_spend_detail.setText(util.spendkindof[item]);
 						    	
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 	
 						alert.show();
 					}
 				}
 			});
 	        
 	        SimpleDateFormat formatter1 = new SimpleDateFormat ( "yyyy-MM-dd", Locale.KOREA );
 	        final Date currentDate = new Date ( );
 	        String dDate = formatter1.format ( currentDate );
 	        SimpleDateFormat formatter2 = new SimpleDateFormat ( "HH:mm", Locale.KOREA );
 	        Date currentTime = new Date ( );
 	        String dTime = formatter2.format ( currentTime );
 	        
 	        edt_date.setText(dDate);
 	        edt_time.setText(dTime);
 	        
 	        edt_date.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					showDialog(DIALOG_DATE_edt);
 				}
 			});
 	        edt_time.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					showDialog(DIALOG_TIME_edt);
 				}
 			});
 	        
 	        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
 	            @Override
 	            public void onClick(DialogInterface arg0, int arg1) {
 	            	Log.d("edt_date",edt_date.getText().toString());
 	            	String[] tmp_str_date = edt_date.getText().toString().split("-");
 	            	Log.d("Good",tmp_str_date[0]);
 	            	Log.d("Good",tmp_str_date[1]);
 	            	Log.d("Good",tmp_str_date[2]);
 	            	Log.d("Hello", "Good");
 	            	
 	            
 	            	Date tmp_date = new Date(Integer.parseInt(tmp_str_date[0])-1900,Integer.parseInt(tmp_str_date[1])-1,Integer.parseInt(tmp_str_date[2]));
 	            	String aaa = String.valueOf(currentDate.getTime());
 	            	aaa = ""+tmp_date.getTime();
 	            	Log.d("",aaa);
 	            	//Date tmp_date_sql = new Date(edt_date.getText().toString());
 	            	Log.d("", "1");
 	            	ContentValues val = new ContentValues();
 	            	val.put("content",edt_content.getText().toString());
 	            	val.put("memo",edt_memo.getText().toString());
 	            	val.put("money",Integer.parseInt(edt_money.getText().toString()));
 	            	//val.put("date",Integer.parseInt(""+tmp_date_sql.getTime()));
 	            	val.put("date",aaa);
 	            	val.put("kindof",edt_middle.getText().toString()+"+"+edt_detail.getText().toString());
 	            	val.put("moneykindof",edt_spendhow.getText().toString()+"/"+edt_spend_detail.getText().toString());
 	            	val.put("minutetime",edt_time.getText().toString());
 	            	mdb.insertTable(val);
 	            	Log.d("", "2");
 	            	baseadapter=null;
 	            	
 	            	madeAdapter();
 	            	baseadapter.notifyDataSetChanged();
 	            	mListView.setAdapter(baseadapter);
 	            }
 	        });
 	         ab.setNeutralButton("즐겨찾기",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog_alert = dialog_list_favor_input();
 					dialog_alert.show();
 				}
 			});
 	        ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
 	            @Override
 	            public void onClick(DialogInterface arg0, int arg1) {
 	              //  setDismiss(mDialog);
 	            }
 	        });
 	          
 	        return ab.create();
 	    }
 	 private AlertDialog dialog_add_spend(HashMap<String,String> map) {
 	        final View innerView = getLayoutInflater().inflate(R.layout.dialog_add_spend, null);
 	        AlertDialog.Builder ab = new AlertDialog.Builder(this);
 	        ab.setTitle("지출내역");
 	        ab.setView(innerView);
 	        edt_date = (EditText)innerView.findViewById(R.id.dialog_edit_date);
 	        edt_time = (EditText)innerView.findViewById(R.id.dialog_edit_time);
 	        final EditText edt_money =(EditText)innerView.findViewById(R.id.dialog_edit_money);
 	        final EditText edt_content = (EditText)innerView.findViewById(R.id.dialog_edit_content);
 	        final EditText edt_memo = (EditText)innerView.findViewById(R.id.dialog_edit_memo);
 	        final EditText edt_middle = (EditText)innerView.findViewById(R.id.dialog_edit_middle);
 	        final EditText edt_detail =  (EditText)innerView.findViewById(R.id.dialog_edit_detail);
 	        final EditText edt_spendhow = (EditText)innerView.findViewById(R.id.dialog_edit_spendhow);
 	        final EditText edt_spend_detail = (EditText)innerView.findViewById(R.id.dialog_edit_spend_detail);
 	        edt_money.setText(map.get("2"));
 	        edt_memo.setText(map.get("1"));
 	        edt_content.setText(map.get("0"));
 	        String tmp_String1 = map.get("3");
 	        String tmp_String2 = map.get("4");
 	        String[] tmp_arrStrings = tmp_String1.split("\\+");
 	        String[] tmp_arrStrings1 = tmp_String2.split("/");
 	        edt_middle.setText(tmp_arrStrings[0]);
 	        edt_detail.setText(tmp_arrStrings[1]);
 	        edt_spendhow.setText(tmp_arrStrings1[0]);
 	        edt_spend_detail.setText(tmp_arrStrings1[1]);
 	        edt_middle.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 						
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("지출 분류 선택");
 					builder.setItems(util.Middleitems, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	edt_middle.setText(util.Middleitems[item]);
 					    	edt_detail.setText("상세선택");
 					    	//Toast.makeText(getApplicationContext(), util.Middleitems[item], Toast.LENGTH_SHORT).show();
 					    }
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 			});
 	        edt_detail.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("지출 세부 분류 선택");
 					String tmp_middle = edt_middle.getText().toString();
 					if(tmp_middle.equals("식비")){
 						builder.setItems(util.detailitems1, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems1[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교통비")){
 						builder.setItems(util.detailitems2, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems2[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교육비")){
 						builder.setItems(util.detailitems3, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems3[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("건강,의료비")){
 						builder.setItems(util.detailitems4, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems4[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("통신비")){
 						builder.setItems(util.detailitems5, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems5[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("가구집기")){
 						builder.setItems(util.detailitems6, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems6[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("주거비")){
 						builder.setItems(util.detailitems7, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems7[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("품위유지비")){
 						builder.setItems(util.detailitems8, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems8[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("교양,오락비")){
 						builder.setItems(util.detailitems9, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems9[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("보험,저축비")){
 						builder.setItems(util.detailitems10, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems10[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("사업운영비")){
 						builder.setItems(util.detailitems11, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems11[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("수수료,세금")){
 						builder.setItems(util.detailitems12, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems12[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else if(tmp_middle.equals("기타")){
 						builder.setItems(util.detailitems13, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_detail.setText(util.detailitems13[item]);
 						    }
 						});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}else{
 						Toast.makeText(getApplicationContext(), "분류를 선택해주세요.",1000).show();
 					}
 				}
 			});
 	        //Calendar cc = Calendar.getInstance();
 	        //String tmp_date = cc.get(Calendar.YEAR)+"-"+(cc.get(Calendar.MONTH)+1)+"-"+cc.get(Calendar.DAY_OF_MONTH);
 	        //String tmp_time =  cc.get(Calendar.HOUR_OF_DAY)+":"+cc.get(Calendar.MINUTE);
 	        edt_spendhow.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 					builder.setTitle("결제 분류 선택");
 					builder.setItems(util.spendhow, new DialogInterface.OnClickListener() {
 					    public void onClick(DialogInterface dialog, int item) {
 					    	if(util.spendhow[item].equals("현금")){
 					    		edt_spend_detail.setText("현금");
 					    	}else{
 					    		edt_spend_detail.setText("-");
 					    	}
 					    	edt_spendhow.setText(util.spendhow[item]);
 					    }
 					});
 					AlertDialog alert = builder.create();
 
 					alert.show();
 				}
 			});
 	        edt_spend_detail.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if(edt_spend_detail.getText().toString().equals("현금")){
 						Toast.makeText(getApplicationContext(), "현금으로 선택되었습니다.", Toast.LENGTH_SHORT).show();
 					}else{
 						AlertDialog.Builder builder = new AlertDialog.Builder(Tab_OneActivity.this);
 						builder.setTitle("결제 분류 선택");
 						builder.setItems(util.spendkindof, new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int item) {
 						    	edt_spend_detail.setText(util.spendkindof[item]);
 						    	
 						    	
 						    }
 						});
 						AlertDialog alert = builder.create();
 	
 						alert.show();
 					}
 				}
 			});
 	        
 	        SimpleDateFormat formatter1 = new SimpleDateFormat ( "yyyy-MM-dd", Locale.KOREA );
 	        final Date currentDate = new Date ( );
 	        String dDate = formatter1.format ( currentDate );
 	        SimpleDateFormat formatter2 = new SimpleDateFormat ( "HH:mm", Locale.KOREA );
 	        Date currentTime = new Date ( );
 	        String dTime = formatter2.format ( currentTime );
 	        
 	        edt_date.setText(dDate);
 	        edt_time.setText(dTime);
 	        
 	        edt_date.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					showDialog(DIALOG_DATE_edt);
 				}
 			});
 	        edt_time.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					showDialog(DIALOG_TIME_edt);
 				}
 			});
 	        
 	        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
 	            @Override
 	            public void onClick(DialogInterface arg0, int arg1) {
 	            	Log.d("edt_date",edt_date.getText().toString());
 	            	String[] tmp_str_date = edt_date.getText().toString().split("-");
 	            	Log.d("Good",tmp_str_date[0]);
 	            	Log.d("Good",tmp_str_date[1]);
 	            	Log.d("Good",tmp_str_date[2]);
 	            	Log.d("Hello", "Good");
 	            	
 	            
 	            	Date tmp_date = new Date(Integer.parseInt(tmp_str_date[0])-1900,Integer.parseInt(tmp_str_date[1])-1,Integer.parseInt(tmp_str_date[2]));
 	            	String aaa = String.valueOf(currentDate.getTime());
 	            	aaa = ""+tmp_date.getTime();
 	            	Log.d("",aaa);
 	            	//Date tmp_date_sql = new Date(edt_date.getText().toString());
 	            	Log.d("", "1");
 	            	ContentValues val = new ContentValues();
 	            	val.put("content",edt_content.getText().toString());
 	            	val.put("memo",edt_memo.getText().toString());
 	            	val.put("money",Integer.parseInt(edt_money.getText().toString()));
 	            	//val.put("date",Integer.parseInt(""+tmp_date_sql.getTime()));
 	            	val.put("date",aaa);
 	            	val.put("kindof",edt_middle.getText().toString()+"+"+edt_detail.getText().toString());
 	            	val.put("moneykindof",edt_spendhow.getText().toString()+"/"+edt_spend_detail.getText().toString());
 	            	val.put("minutetime",edt_time.getText().toString());
 	            	mdb.insertTable(val);
 	            	Log.d("", "2");
 	            	baseadapter=null;
 	            	
 	            	madeAdapter();
 	            	baseadapter.notifyDataSetChanged();
 	            	mListView.setAdapter(baseadapter);
 	            }
 	        });
 	         ab.setNeutralButton("즐겨찾기",new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog_alert = dialog_list_favor_input();
 					dialog_alert.show();
 				}
 			});
 	        ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
 	            @Override
 	            public void onClick(DialogInterface arg0, int arg1) {
 	              //  setDismiss(mDialog);
 	            }
 	        });
 	          
 	        return ab.create();
 	    }
 	public void madeAdapter(){
 		mGroupList.clear();
 		mChildList.clear();
 		tmp_moneyint=0;
 		 
 	        LastDay = grecal.getActualMaximum(Calendar.DAY_OF_MONTH);
 	        int i=0;
 	        while(i<LastDay){
 	        	int a = LastDay;
 	        	int year_ = c.get(Calendar.YEAR);
 	        	int month_ = c.get(Calendar.MONTH)+1;
 	        	
 	          	Date tmp_date = new Date(year_-1900, month_-1, a-i);
 	        	
 	        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 	        	String str_tmp_date = formatter.format(tmp_date);
 	        	
 	        	
 	        	//mGroupList.add(year_+"."+month_+"."+(a-i));
 	        
 	        	//mChildListContent.clear();
 	        	String[] columns={"content","memo","money","kindof","date","minutetime","moneykindof","_id"};
 	        	String selection="date=?";
 	        	if(kindof==0){
 	        		
 	        	}else {
 	        		selection="date=? AND moneykindof LIKE ?";
 	        	}
 	        	
 	        	ArrayList<String> strings = new ArrayList<String>();
 	        	strings.add(String.valueOf(tmp_date.getTime()));
 	        	
 	        	if(kindof==1){
 	        		strings.add("현금%");
 	        	}else if(kindof==2){
 	        		strings.add("카드%");
 	        	}
 	        	
 	        	if(middleString.equals("전체")){
 	        		detailString="";
 	        	}else{
 	        		selection+=" AND kindof LIKE ?";
 	        		if(detailString.equals("")){
 	        			strings.add(middleString+"%");
 	        		}else{
 	        			strings.add(middleString+"+"+detailString);
 	        		}
 	        		
 	        	}
 	        	String[] selectionArgs=(String[])strings.toArray(new String[strings.size()]);
 	        	
 	        	//Log.d("Timestamp",String.valueOf(tmp_date.getTime()));
 	        	Cursor c = mdb.selectTable(columns, selection, selectionArgs, null,null,"minutetime ASC");
 	        	//        	c.getCount();
 	        	//Log.d("count",""+c.getCount());
 	        	//c.moveToFirst();
 	        	ArrayList<String> tmp_Content = new ArrayList<String>();
 	        	
 	        	
 	        	int tmp_count=c.getCount();
 	        	Log.d("count",""+tmp_count);
 	        	if(tmp_count>0){
 	        		int tmp_money = 0;
 	        		
 	        		if(c.moveToFirst()){
 	        			do{
 	        				Log.d("dddd",""+c.getInt(7));
 	        				tmp_money += Integer.parseInt(c.getString(2));
 	        				String tmp_content_String = c.getString(5)+"#"+c.getString(6)+"#"+c.getString(1)+"#"+c.getString(2)+"원"+"#"+c.getInt(7); 
 	    	        		tmp_Content.add(tmp_content_String);	    	  
 			        	}while(c.moveToNext());
 	        		}
 	        		str_tmp_date+="#"+tmp_money+"원";
 	        		tmp_moneyint+=tmp_money;
 	        	}else{
 	        		//Log.d("값 있음","");
 	        		//c.moveToNext();
 	        		str_tmp_date+="#0원";
 	        		//tmp_Content.add("값없음#값없음#값없음#값없음");	        		//mChildListContent.add(c.getString(0));
 	        		
 	        	}
 	        	mGroupList.add(str_tmp_date);
 	        	baseadapter = new BaseExpandableAdapter(this, mGroupList, mChildList);
 	        	
 	        	mChildList.add(tmp_Content);
 	        	i++;
 	        }
 
         	bt_money.setText(""+tmp_moneyint+"원");
         	
         	 mListView.setAdapter(baseadapter);
 	}
 	class dialogAdapter extends BaseAdapter{
 		private LayoutInflater Inflater;
 		private ArrayList<HashMap<String,String>> map;
 		private int _layout;
 		public dialogAdapter(Context c,int layout,ArrayList<HashMap<String,String>> map){
 			Inflater=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			_layout=layout;
 			this.map=map;
 		}
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return map.size();
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getView(int arg0, View arg1, ViewGroup arg2) {
 			// TODO Auto-generated method stub
 			if(arg1==null){
 				arg1=Inflater.inflate(_layout, arg2,false);
 			}
 			HashMap<String, String> hashMap = map.get(arg0);
 			
 			TextView t1 = (TextView)arg1.findViewById(R.id.shortcutrow_where);
 			TextView t2 = (TextView)arg1.findViewById(R.id.shortcutrow_cate);
 			TextView t3 = (TextView)arg1.findViewById(R.id.shortcutrow_price);
 			TextView t4 = (TextView)arg1.findViewById(R.id.shortcutrow_auto);
 			t1.setText(hashMap.get("0"));
 		//	String strings[] = hashMap.get("3").split("+");
 			t2.setText(hashMap.get("3"));
 			//t2.setText(strings[0]);
 			t3.setText(hashMap.get("2")+"원");
 			//t4.setText(hashMap.get("0"));
 			return arg1;
 		}
 		
 	}
 }
