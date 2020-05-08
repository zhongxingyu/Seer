 /*
  * Wifi Fixer for Android
  *     Copyright (C) 2010-2013  David Van de Ven
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see http://www.gnu.org/licenses
  */
 
 package org.wahtod.wififixer.ui;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import org.wahtod.wififixer.R;
 import org.wahtod.wififixer.WFMonitor;
 import org.wahtod.wififixer.prefs.PrefUtil;
 import org.wahtod.wififixer.utility.BroadcastHelper;
 import org.wahtod.wififixer.utility.NotifUtil;
 import org.wahtod.wififixer.utility.StringUtil;
 import org.wahtod.wififixer.utility.WFScanResult;
 
 import java.lang.reflect.Field;
 import java.util.List;
 
 public class ConnectFragment extends Fragment implements OnClickListener {
     public static final String TAG = "SFFJSHFTWFW";
     protected static final int CANCEL = 1;
     private static final String PROXY_CLASS = "android.net.wifi.WifiConfiguration$ProxySettings";
     private static final String BUGGED = "Proxy";
     private static final String DHCP_CONSTANT = "DHCP";
     private static final String NONE_CONSTANT = "NONE";
     private static final String IPASSIGNMENT_CLASS = "android.net.wifi.WifiConfiguration$IpAssignment";
     private static final String IP_ASSIGNMENT = "ipAssignment";
     private static final String PROXY_SETTINGS = "proxySettings";
     private static final String WPA = "WPA";
     private static final String WEP = "WEP";
     private static final String NETWORK_KEY = "WFSCANRESULT";
     private WFScanResult mNetwork;
 
     /*
      * Reflection magic ahead
      */
     private static WifiConfiguration addHiddenFields(WifiConfiguration w) {
         try {
             Field f = w.getClass().getField(IP_ASSIGNMENT);
             Field f2 = w.getClass().getField(PROXY_SETTINGS);
             Class<?> ipc = Class.forName(IPASSIGNMENT_CLASS);
             Class<?> proxy = Class.forName(PROXY_CLASS);
             Field dhcp = ipc.getField(DHCP_CONSTANT);
             Field none = proxy.getField(NONE_CONSTANT);
             Object v = dhcp.get(null);
             Object v2 = none.get(null);
             f.set(w, v);
             f2.set(w, v2);
         } catch (Exception e) {
             /*
              * Log
 			 */
             e.printStackTrace();
         }
         return w;
     }
 
     public static ConnectFragment newInstance(WFScanResult n) {
         ConnectFragment f = new ConnectFragment();
         f.mNetwork = n;
         return f;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.connect_fragment, null);
         return v;
     }
 
     private int addNetwork(final String password) {
         WifiConfiguration wf = getKeyAppropriateConfig(password);
         WifiManager wm = PrefUtil.getWifiManager(getActivity());
         int n = wm.addNetwork(wf);
         if (n != -1) {
             wm.enableNetwork(n, false);
             wm.saveConfiguration();
         }
         return n;
     }
 
     private WifiConfiguration getKeyAppropriateConfig(final String password) {
         WifiConfiguration wf = new WifiConfiguration();
         if (wf.toString().contains(BUGGED)) {
             /*
			 * Add hidden fields on bugged Android 3.1+ configs
 			 */
             wf = addHiddenFields(wf);
         }
         wf.SSID = StringUtil.addQuotes(mNetwork.SSID);
         if (mNetwork.capabilities.length() == 0) {
             wf.BSSID = mNetwork.BSSID;
             wf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
             wf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
             return wf;
         }
         wf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
         wf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
         wf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
         wf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
         wf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
         wf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
         if (mNetwork.capabilities.contains(WEP)) {
             wf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
             wf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
             wf.wepKeys[0] = StringUtil.addQuotes(password);
         } else if (mNetwork.capabilities.contains(WPA)) {
             wf.preSharedKey = StringUtil.addQuotes(password);
         }
         List<WifiConfiguration> configs = PrefUtil
                 .getWifiManager(getActivity()).getConfiguredNetworks();
         for (WifiConfiguration w : configs) {
             Log.i(w.SSID, w.toString());
         }
         Log.i(wf.SSID, wf.toString());
         return wf;
     }
 
     private void connectNetwork() {
         Intent intent = new Intent(WFMonitor.CONNECTINTENT);
         intent.putExtra(WFMonitor.NETWORKNAME, mNetwork.SSID);
         BroadcastHelper.sendBroadcast(getActivity(), intent, true);
     }
 
     private void notifyConnecting() {
         NotifUtil.showToast(getActivity(),
                 getActivity().getString(R.string.connecting_to_network)
                         + mNetwork.SSID);
     }
 
     public void onClick(View v) {
         View e = ((View) v.getParent()).findViewById(R.id.password);
         String password = null;
         try {
             password = String.valueOf(((EditText) e).getText());
         } catch (NullPointerException e1) {
         }
         if (password == null || password.length() == 0) {
             if (mNetwork.capabilities.length() == 0) {
                 addNetwork(null);
                 notifyConnecting();
                 connectNetwork();
             } else if (KnownNetworksFragment.getNetworks(getActivity())
                     .contains(mNetwork.SSID))
                 notifyConnecting();
             connectNetwork();
         } else
             addNetwork(password);
 
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
         FragmentTransaction f = this.getParentFragment()
                 .getChildFragmentManager().beginTransaction();
         f.remove(this);
         f.commit();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         if (savedInstanceState == null) {
             /*
              * Do nothing
              */
         } else {
             /*
              * Restore Network
              */
             mNetwork = WFScanResult.fromBundle(savedInstanceState.getBundle(NETWORK_KEY));
         }
         Button b = (Button) getActivity().findViewById(R.id.connect);
         View e = getActivity().findViewById(R.id.password);
         TextView summary = (TextView) getActivity().findViewById(R.id.password_summary);
         if (StringUtil.getCapabilitiesString(mNetwork.capabilities).equals(
                 StringUtil.OPEN)
                 || KnownNetworksFragment.getNetworks(getActivity()).contains(
                 mNetwork.SSID)) {
             e.setVisibility(View.INVISIBLE);
             b.setText(getString(R.string.connect));
             summary.setText(R.string.button_connect);
         }
         b.setOnClickListener(this);
         TextView s = (TextView) getActivity().findViewById(R.id.network_name);
         s.setText(mNetwork.SSID);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if (mNetwork != null)
             outState.putBundle(NETWORK_KEY, mNetwork.toBundle());
     }
 
     @Override
     public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
 		/*
 		 * Animate the view
 		 */
         if (enter) {
             ExpandViewAnimation ev = new ExpandViewAnimation(getView()
                     .findViewById(R.id.connect_fragment_layout), 300);
             return ev;
         } else
             return null;
     }
 }
