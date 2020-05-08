 package com.dreamtale.pintrestlike.activity;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 import com.dreamtale.pintrestlike.R;
 import com.dreamtale.pintrestlike.data.ImageInfo;
 import com.dreamtale.pintrestlike.data.ImageInfoProvider;
 import com.dreamtale.pintrestlike.fragment.ImageDetailFragment;
 import com.dreamtale.pintrestlike.share.BluetoothService;
 import com.dreamtale.pintrestlike.utils.IntentConstant;
 
 public class DetailActivity extends FragmentActivity
 {
     private static final String TAG = "DetailActivity";
     
     private static final int BLUETOOTH_INTENT_ENABLE_REQUEST_CODE = 0x01;
     private static final int BLUETOOTH_INTENT_DISCOVERABLE_REQUEST_CODE = 0x02;
     private static final int BLUETOOTH_INTENT_SCAN_REQUEST_CODE = 0x03;
     
     private BluetoothAdapter bluetoothAdapter = null;
     private BluetoothService bluetoothService = null;
     private ViewPager viewPager = null;
     private ImagePageAdapter adapter = null;
     
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.image_fragment);
         viewPager = (ViewPager)findViewById(R.id.pager);
         ArrayList<ImageInfo> dataList = ImageInfoProvider.getInstance().getDataList();
         adapter = new ImagePageAdapter(getSupportFragmentManager(), dataList);
         viewPager.setAdapter(adapter);
         viewPager.setOffscreenPageLimit(1);
         viewPager.setClickable(true);
         final GestureDetector detector = new GestureDetector(this, listener);
         viewPager.setOnTouchListener(new OnTouchListener()
         {
             @Override
             public boolean onTouch(View v, MotionEvent event)
             {
                 return detector.onTouchEvent(event);
             }
         });
         
         final int position = getIntent().getIntExtra(IntentConstant.EXTRA_IMAGE_DETAIL_FRAGMENT_POSITION, -1);
         if (-1 != position)
         {
             viewPager.setCurrentItem(position);
         }
         bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         bluetoothService = BluetoothService.getInstance();
     }
     
     private GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener()
     {
         public boolean onSingleTapUp(MotionEvent e)
         {
             int position = viewPager.getCurrentItem();
             ImageInfo info = ImageInfoProvider.getInstance().getDataList().get(position);
             String shareURL = info.getFullSizeUrl();
 //            String test = "hello from " + bluetoothAdapter.getName();
             bluetoothService.write(shareURL.getBytes());
             return true;
         };
     };
     
     @Override
     protected void onStart()
     {
         super.onStart();
         bluetoothService.addHandler(dataHandler);
         if (bluetoothAdapter.isEnabled())
         {
             bluetoothService.start();
         }
     }
     
     @Override
     protected void onStop()
     {
         super.onStop();
         bluetoothService.clearThread();
         bluetoothService.removeHandler(dataHandler);
     }
     
     @Override
     protected void onDestroy()
     {
         super.onDestroy();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         getMenuInflater().inflate(R.menu.detail_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         int id = item.getItemId();
         switch (id)
         {
         case R.id.action_share:
             if (!isBluetoothEnabled())
             {
                 enableBluetooth();
             }
             else
             {
                 Intent intent = new Intent();
                 intent.setClass(this, BluetoothDeviceListActivity.class);
                 startActivityForResult(intent, BLUETOOTH_INTENT_SCAN_REQUEST_CODE);
             }
             break;
         case R.id.action_discoverable:
             makeDeviceDiscoverable();
             break;
         default:
             break;
         }
         return true;
     }
     
     private Handler dataHandler = new Handler()
     {
         public void dispatchMessage(android.os.Message msg)
         {
             switch (msg.what)
             {
             case BluetoothService.MSG_BLUETOOTH_READ:
                 ByteBuffer data = (ByteBuffer)msg.obj;
                 if (null != data)
                 {
                     int length = msg.arg1;
                     String s = new String(data.array(), 0, length);
                     int position = viewPager.getCurrentItem();
                     ImageDetailFragment fragment = (ImageDetailFragment)adapter.instantiateItem(viewPager, position);
                     fragment.setShareURL(s);
                     Toast.makeText(DetailActivity.this, "start fetching the shared image...", Toast.LENGTH_SHORT).show();
                 }
                 break;
 
             default:
                 break;
             }
         };
     };
     
     static class ImagePageAdapter extends FragmentPagerAdapter
     {
         private ArrayList<ImageInfo> dataList = null;
         
         public ImagePageAdapter(android.support.v4.app.FragmentManager fm, ArrayList<ImageInfo> dataList)
         {
             super(fm);
             this.dataList = dataList;
         }
 
         @Override
         public Fragment getItem(int arg0)
         {
             return new ImageDetailFragment(dataList.get(arg0));
         }
 
         @Override
         public int getCount()
         {
             return dataList.size();
         }
         
         @Override
         public Object instantiateItem(ViewGroup container, int position)
         {
             return super.instantiateItem(container, position);
         }
 
         @Override
         public void destroyItem(ViewGroup container, int position, Object object)
         {
             // TODO Auto-generated method stub
             super.destroyItem(container, position, object);
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode)
         {
         case BLUETOOTH_INTENT_ENABLE_REQUEST_CODE:
             if (Activity.RESULT_OK == resultCode)
             {
                 bluetoothService.start();
                 
                 Intent intent = new Intent();
                 intent.setClass(this, BluetoothDeviceListActivity.class);
                 startActivityForResult(intent, BLUETOOTH_INTENT_SCAN_REQUEST_CODE);
                 Log.d(TAG, "enable bluetooth success!");
             }
             else
             {
                 Log.d(TAG, "enable bluetooth failed!");
             }
             break;
         case BLUETOOTH_INTENT_DISCOVERABLE_REQUEST_CODE:
             bluetoothService.start();
             break;
             
         case BLUETOOTH_INTENT_SCAN_REQUEST_CODE:
             if (Activity.RESULT_OK == resultCode)
             {
                 String macAddr = data.getStringExtra(IntentConstant.EXTRA_BLUETOOTH_ADDRESS);
                 if (!TextUtils.isEmpty(macAddr))
                 {
                     BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddr);
                     bluetoothService.connect(device);
                 }
             }
             break;
         default:
             break;
         }
     }
     
     /**
      * Whether the BlueTooth enabled.
      * 
      * @return  
      */
     private boolean isBluetoothEnabled()
     {
         BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
         if (null == adapter)
         {
             Toast.makeText(this, "No bluetooth support!", Toast.LENGTH_SHORT).show();
             return false;
         }
         else
         {
             return adapter.isEnabled();
         }
     }
     
     private void enableBluetooth()
     {
         Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
         startActivityForResult(intent, BLUETOOTH_INTENT_ENABLE_REQUEST_CODE);
     }
     
     private void makeDeviceDiscoverable()
     {
         if (BluetoothAdapter.getDefaultAdapter().getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
         {
             Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
             startActivityForResult(intent, BLUETOOTH_INTENT_DISCOVERABLE_REQUEST_CODE);
         }
     }
 }
