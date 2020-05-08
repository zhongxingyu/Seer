 /*
  * GUARD the Bridge
  * Copyright (C) 2012  Matthew Finkel <Matthew.Finkel@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package edu.uconn.guarddogs.guardthebridge;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLProtocolException;
 import javax.net.ssl.SSLSocket;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.google.protobuf.TextFormat;
 
 import edu.uconn.guarddogs.guardthebridge.Communication.Request;
 import edu.uconn.guarddogs.guardthebridge.Communication.Response;
 import edu.uconn.guarddogs.guardthebridge.Patron.PatronInfo;
 import edu.uconn.guarddogs.guardthebridge.Patron.PatronList;
 
 public class GuardtheBridge extends FragmentActivity {
 	private static final int PATRON_READ = 100;
 	private static final int PATRON_EDIT = 101;
 	private static final String TAG = "GTB";
 	private static final int OPENRIDES = 0;
 	private static final int CLOSEDRIDES = 1;
 	private static final int NUM_ITEMS = 2;
     private static GuardtheBridge self;
     
     private GtBDbAdapter mGDbHelper = null;
 	private GtBSSLSocketFactoryWrapper mSSLSF;
     
     private GTBAdapter m_GFPA = null; 
     private ViewPager m_aVP = null;
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         
         super.onCreate(savedInstanceState);
         self = this;
         setContentView(R.layout.rideslist);
         
         ViewPager aVp = (ViewPager)findViewById(R.id.ridelist_pageview);
         m_GFPA = new GTBAdapter(getSupportFragmentManager());
         aVp.setAdapter(m_GFPA);
         mSSLSF = new GtBSSLSocketFactoryWrapper(this);
         mGDbHelper = new GtBDbAdapter(this);
         mGDbHelper.open();
         //retrieveRides();
         //populateRides(OPENRIDES);
         mGDbHelper.close();
         //mDbHelper.close();
         //nGDbHelper.close();
         
         Button aRfrshBtn = (Button)findViewById(R.id.refresh);
         aRfrshBtn.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mGDbHelper.open();
 				retrieveRides();
 				mGDbHelper.close();
 			}
 		});
     }
     
     public boolean onOptionItemSelected(MenuItem menu){
 		return super.onOptionsItemSelected(menu);
     }
    
     public void retrieveRides() {
 		   ArrayList<Integer> vRides = mGDbHelper.fetchAllPid();
 		   Request aPBReq = Request.newBuilder().
 				   setNReqId(1).
 				   setSReqType("CURR").
 				   setNCarId(5).
 		   		   addAllNParams(vRides).
 		   		   build();
 		   
 		   Log.v(TAG, "Request type: " + aPBReq.getSReqType());
 		   Log.v(TAG, "Request ID: " + aPBReq.getNReqId());
 		   Log.v(TAG, "Request Size: " + aPBReq.isInitialized());
 		   Log.v(TAG, "SReqType = " + aPBReq.getSReqType() + " " + 
 				   aPBReq.getSerializedSize());
 		   SSLSocket aSock = mSSLSF.getSSLSocket();
 		   if (aSock.isClosed())
 			   aSock = mSSLSF.createSSLSocket(self);
 		   if (mSSLSF.getSession() == null)
 		   {
 			   mSSLSF = mSSLSF.getNewSSLSFW(self);
 			   aSock = mSSLSF.getSSLSocket();
 		   }
 		   try {
 			   OutputStream aOS = aSock.getOutputStream();
 			   try
 			   {
 				   aOS.write(aPBReq.getSerializedSize());
 			   } catch (SSLProtocolException ex)
 			   {
 				   Log.e(TAG, "SSLProtoclException Caught. On-write to Output Stream");
 					mSSLSF.forceReHandshake(this);
 					aSock = mSSLSF.getSSLSocket();
 					aOS = aSock.getOutputStream();
 					try
 					{
 						aOS.write(aPBReq.getSerializedSize());
 					} catch (SSLProtocolException exc)
 					{
 						mSSLSF = mSSLSF.getNewSSLSFW(this);
 						aSock = mSSLSF.getSSLSocket();
 						aOS = aSock.getOutputStream();
 						aOS.write(aPBReq.getSerializedSize());
 					}
 			   }
 			   byte[] vbuf = aPBReq.toByteArray();
 			   //aPBReq.writeTo(aOS);
 			   aOS.write(vbuf);
 			   InputStream aIS = aSock.getInputStream();
			   vbuf = new byte[9];
 			   aIS.read(vbuf);
 			   try
 			   {
 				   Response apbTmpSize = null;
 				   apbTmpSize = Response.parseFrom(vbuf);
 				   vbuf = new byte[apbTmpSize.getNRespId()];
 				   aIS.read(vbuf);
 				   Response apbRes;
 				   apbRes = Response.parseFrom(vbuf);
 			
 				   Log.v(TAG, "Response Buffer:");
 				   Log.v(TAG, TextFormat.shortDebugString(apbRes));
 				   Log.v(TAG, "PatronList Buffer: ");
 				   Log.v(TAG, TextFormat.shortDebugString(
 						   apbRes.getPlPatronList()));
 				   addToDb(apbRes.getPlPatronList());
 				   Log.v(TAG, "Added to DB");
 				} catch (InvalidProtocolBufferException e) {
 					// TODO Auto-generated catch block
 					String tmp = "";
 					for(int i = 0; i<vbuf.length; i++)
 						tmp = tmp + vbuf[i] + " ";
 					Log.w(TAG, "Buffer Received: " + vbuf.length + " bytes : " 
 						+ tmp);
 					e.printStackTrace();
 				}
 		   }catch (IOException e)
 		   {
 			   e.printStackTrace();
 		   }
 	   	}
 	   
 	   public void addToDb(PatronList list){
 		   for (PatronInfo patron : list.getPatronList())
 			   mGDbHelper.createPatron(patron.toByteArray(), patron.getPid());
 	   }
 	   
    private void setActions(ListView lv, Button dispatch, Button emerg){
 	   lv.setOnItemClickListener(new OnItemClickListener() { 
 		   @Override
 		   public void onItemClick(AdapterView<?> av, 
 				   View v, 
 				   int position, 
 				   long id){
 				Log.v(TAG, "Displaying Ride: " + id);
 				Log.v(TAG, "Car Number: " + position);
 				TextView tv = (TextView) v;
 				long row = 0;
 				try
 				{
 					row = Long.parseLong(tv.getText().
 							toString().
 							substring(0, 1));
 				} catch (NumberFormatException e)
 				{
 					return;
 				}
 				Intent intent = new Intent(self, ShowPatron.class);
 				intent.putExtra(GtBDbAdapter.KEY_ROWID, row);
 				startActivityForResult(intent, PATRON_READ);
 		   }
 	   });
 	   lv.setOnItemLongClickListener(new OnItemLongClickListener() { 
 		   @Override
 		   public boolean onItemLongClick(AdapterView<?> av, 
 				   View v, 
 				   int position, 
 				   long id){
 				Log.v(TAG, "Editing Ride: " + id);
 				Log.v(TAG, "Car Number: " + position);
 				TextView tv = (TextView) v;
 				long row = 0;
 				try
 				{
 					row = Long.parseLong(tv.getText().
 							toString().
 							substring(0, 1));
 				} catch (NumberFormatException e)
 				{
 					return false;
 				}
 				Intent intent = new Intent(self, EditPatron.class);
 				intent.putExtra(GtBDbAdapter.KEY_ROWID, row);
 				startActivityForResult(intent, PATRON_EDIT);
 				return true;
 		   }
 	   });
 	   
    }
    
    public static class GTBAdapter extends FragmentPagerAdapter
    {
 	   public GTBAdapter(FragmentManager fm)
 	   {
 		   super(fm);
 	   }
 	   public int getCount()
 	   {
 		   return NUM_ITEMS;
 	   }
 	   public Fragment getItem(int position)
 	   {
 		   return ArrayListFragment.newInstance(position);
 	   }
    }
    
    public static class ArrayListFragment extends ListFragment
    {
 	   private int mNum;
 	   private static GtBDbAdapter m_ALFGDbHelper = null;
 	   private CarsGtBDbAdapter mDbHelper = null;
 	   private TLSGtBDbAdapter nGDbHelper = null;
 	   private GtBSSLSocketFactoryWrapper m_sslSF;
 	    
 	   static ArrayListFragment newInstance(int num)
 	   {
 		   Log.v(TAG, "ArrayListFragment: newInstance: num=" + num);
 		   ArrayListFragment f = new ArrayListFragment();
 		   
 		   Bundle args = new Bundle();
 		   args.putInt("num", num);
 		   f.setArguments(args);
 		   return f;
 	   }
 	   
 	   public void onCreate(Bundle savedInstanceState)
 	   {
 		   super.onCreate(savedInstanceState);
 		   Log.v(TAG, "ArrayListFragment: onCreate");
 	       m_sslSF = new GtBSSLSocketFactoryWrapper(self);
 		   m_ALFGDbHelper = new GtBDbAdapter(self);
 		   mDbHelper = new CarsGtBDbAdapter(self);
 		   nGDbHelper = new TLSGtBDbAdapter(self);
 		   mNum = getArguments() != null ? getArguments().getInt("num") : 1;
 		   initializeDb();
 		   retrieveRides();
 	   }
 	   
 	   public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			   Bundle savedInstanceState)
 	   {
 		   Log.v(TAG, "ArrayListFragment: onCreateView");
 		   m_ALFGDbHelper.open();
 		   TextView aTV = null;
 		   View v = null;
 		   if (mNum == 0)
 		   {
 			   Log.v(TAG, "onCreateView: Current: mNum = " + mNum);
 			   v = inflater.inflate(R.layout.openrides, container, false);
 			   aTV = (TextView)v.findViewById(R.id.openrides_title);
 			   aTV.setText(R.string.openrides_title);
 		   }
 		   else/* if (mNum == 1)*/
 		   {
 			   Log.v(TAG, "onCreateView: Closed: mNum = " + mNum);
 			   v = inflater.inflate(R.layout.closedrides, container, false);
 			   aTV = (TextView)v.findViewById(R.id.closedrides_title);
 			   aTV.setText(R.string.closedrides_title);
 		   }
 		   ((RelativeLayout)aTV.getParent()).removeView(aTV);
 		   /*ListView aLV = (ListView) v.findViewById(android.R.id.list);
 		   aLV.setAdapter(new ArrayAdapter<PatronInfo>(getActivity(), R.layout.rides, GuardtheBridge.populateRides(OPENRIDES)));
 		   ((RelativeLayout)aLV.getParent()).removeView(aLV);*/
 		   Log.v(TAG, "onCreateView: returning");
 		   m_ALFGDbHelper.close();
 		   return v;
 	   }
 	   
 	   public void onActivityCreated(Bundle savedInstanceState)
 	   {
 		   super.onActivityCreated(savedInstanceState);
 		   Log.v(TAG, "ArrayListFragment: onActivityCreated");
 		   if (mNum == 0)
 			   setListAdapter(new ArrayAdapter<String>(getActivity(),
 				   R.layout.rides, populateRides(OPENRIDES)));
 		   else /* if (mNum == 1)*/
 			   setListAdapter(new ArrayAdapter<String>(getActivity(),
 				   R.layout.rides, populateRides(CLOSEDRIDES)));
 		   m_ALFGDbHelper.close();
 	   }
 	   
 	   @Override
 	   public void onListItemClick(ListView l, View v, int position, long id){
 			Log.v(TAG, "Displaying Ride: " + id);
 			Log.v(TAG, "Car Number: " + position);
 			TextView tv = (TextView) v;
 			long pid = 0;
 			try
 			{
 				Pattern pPidRegex = Pattern.compile("\\d*:");
 				String atmp = "";
 				try
 				{
 					atmp = tv.getText().
 							toString().split(pPidRegex.pattern())[0];
 					atmp = atmp.substring(0, atmp.length()-1);
 				} catch (NullPointerException ex)
 				{
 					Log.w(TAG, "NULL Pointer on pid read: " + atmp);
 					return;
 				}
 				pid = Long.parseLong(atmp);
 			} catch (NumberFormatException e)
 			{
 				return;
 			}
 			Intent intent = new Intent(self, ShowPatron.class);
 			intent.putExtra(GtBDbAdapter.KEY_ROWID, pid);
 			startActivity(intent);
 	   }
 	   
 	   public boolean onListItemLongClick(AdapterView<?> av, 
 			   View v, 
 			   int position, 
 			   long id){
 			Log.v(TAG, "Editing Ride: " + id);
 			Log.v(TAG, "Car Number: " + position);
 			TextView tv = (TextView) v;
 			long row = 0;
 			try
 			{
 				row = Long.parseLong(tv.getText().
 						toString().
 						substring(0, 1));
 			} catch (NumberFormatException e)
 			{
 				return false;
 			}
 			Intent intent = new Intent(self, EditPatron.class);
 			intent.putExtra(GtBDbAdapter.KEY_ROWID, row);
 			startActivityForResult(intent, PATRON_EDIT);
 			return true;
 	   }
 	   
 	   public void initializeDb()
 	   {
 	       mDbHelper.open();
 	       nGDbHelper.open();
 	   }
    
 	   public void retrieveRides() {
 		   m_ALFGDbHelper.open();
 		   ArrayList<Integer> vRides = m_ALFGDbHelper.fetchAllPid();
 		   Request aPBReq = Request.newBuilder().
 				   setNReqId(1).
 				   setSReqType("CURR").
 				   setNCarId(mDbHelper.getCar()).
 		   		   addAllNParams(vRides).
 		   		   build();
 		   
 		   Log.v(TAG, "Request type: " + aPBReq.getSReqType());
 		   Log.v(TAG, "Request ID: " + aPBReq.getNReqId());
 		   Log.v(TAG, "Request Size: " + aPBReq.isInitialized());
 		   Log.v(TAG, "SReqType = " + aPBReq.getSReqType() + " " + 
 				   aPBReq.getSerializedSize());
 		   SSLSocket aSock = m_sslSF.getSSLSocket();
 		   if (aSock.isClosed())
 			   aSock = m_sslSF.createSSLSocket(self);
 		   if (m_sslSF.getSession() == null)
 		   {
 			   m_sslSF = m_sslSF.getNewSSLSFW(self);
 			   aSock = m_sslSF.getSSLSocket();
 		   }
 		   try {
 			   OutputStream aOS = aSock.getOutputStream();
			   try
			   {
				   aOS.write(aPBReq.getSerializedSize());
			   } catch (SSLProtocolException ex)
			   {
				   Log.e(TAG, "SSLProtoclException Caught. On-write to Output Stream");
					m_sslSF.forceReHandshake(self);
					aSock = m_sslSF.getSSLSocket();
					aOS = aSock.getOutputStream();
					try
					{
						aOS.write(aPBReq.getSerializedSize());
					} catch (SSLProtocolException exc)
					{
						m_sslSF = m_sslSF.getNewSSLSFW(self);
						aSock = m_sslSF.getSSLSocket();
						aOS = aSock.getOutputStream();
						aOS.write(aPBReq.getSerializedSize());
					}
			   }
 			   byte[] vbuf = aPBReq.toByteArray();
 			   //aPBReq.writeTo(aOS);
 			   aOS.write(vbuf);
 			   InputStream aIS = aSock.getInputStream();
 			   vbuf = new byte[9];
 			   aIS.read(vbuf);
 			   try 
 			   {
 				   Response apbTmpSize = null;
 				   apbTmpSize = Response.parseFrom(vbuf);
 				   vbuf = new byte[apbTmpSize.getNRespId()];
 				   aIS.read(vbuf);
 				   Response apbRes;
 				   apbRes = Response.parseFrom(vbuf);
 			
 				   Log.v(TAG, "Response Buffer:");
 				   Log.v(TAG, TextFormat.shortDebugString(apbRes));
 				   Log.v(TAG, "PatronList Buffer: ");
 				   Log.v(TAG, TextFormat.shortDebugString(
 						   apbRes.getPlPatronList()));
 				   addToDb(apbRes.getPlPatronList());
 				   Log.v(TAG, "Added to DB");
 				} catch (InvalidProtocolBufferException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
					String tmp = "";
					for(int i = 0; i<vbuf.length; i++)
						tmp = tmp + vbuf[i] + " ";
					Log.w(TAG, "Buffer Received: " + vbuf.length + " bytes : " 
						+ tmp);
					e.printStackTrace();
 				}
 		   }catch (IOException e)
 		   {
 			   e.printStackTrace();
 		   }
 		   m_ALFGDbHelper.close();
 	   	}
 	   
 	   public void addToDb(PatronList list){
 		   for (PatronInfo patron : list.getPatronList())
 			   m_ALFGDbHelper.createPatron(patron.toByteArray(), patron.getPid());
 	   }
 	   
 	   public String[] populateRides(int ridetype){
 		   m_ALFGDbHelper.open();
 		   PatronInfo[] vPI = m_ALFGDbHelper.fetchAllPatrons(ridetype);
 		   if (vPI.length == 0)
 		   {
 			   String[] msg = new String[1];
 			   msg[0] = "No pending rides! Just chill";
 			   //ListView aLV = (ListView) findViewById(R.id.list);
 			   //aLV.setAdapter(new ArrayAdapter<String>(this, R.layout.activelist, R.id.nameVal, msg));
 			   //new ArrayAdapter<String>(this, R.layout.rideslist, msg);
 			   Log.w(TAG, "No rides received.");
 			   return msg;
 		   }
 		   else
 		   {
 			   /*int[] to = new int[]{R.string.nameVal, R.string.ttVal};
 			   String[] from = new String[]{vPI[i].getName(), vPI[i].getTimetaken()};
 			   ArrayList<Map<String, String>> listmap = new ArrayList<Map<String, String>>(vPI.length);
 			   TreeMap<String, String> map = new TreeMap<String, String>();
 			   for (int i = 0; i < vPI.length; i++){
 				   
 				   map.put(vPI[i].getTimetaken(), Integer.toString(i));
 				   listmap.add(map);
 			   }
 			   new SimpleAdapter(this, listmap, R.layout.activelist, from, to));*/
 			   String[] msg = new String[vPI.length];
 			   
 			   //ListView aLV = (ListView) findViewById(R.id.activelist_list);
 			   for(int i = 0; i<vPI.length; i++)
 			   {
 				   msg[i] = vPI[i].getPid() + " " + vPI[i].getTimeassigned() + 
 						   ": " + vPI[i].getName() + " - " + vPI[i].getPickup();
 			   }
 			   
 			   /*aLV.setAdapter(new ArrayAdapter<String>(this, R.layout.rides, msg));
 			   Log.v(TAG, "Finished compiling list of assigned rides");
 			   
 			   setActions(aLV, (Button)findViewById(R.id.dispatch), 
 					   (Button)findViewById(R.id.emergency));
 				*/
 			   m_ALFGDbHelper.close();
 			   return msg;
 		   }
 	   }
    }
 }
