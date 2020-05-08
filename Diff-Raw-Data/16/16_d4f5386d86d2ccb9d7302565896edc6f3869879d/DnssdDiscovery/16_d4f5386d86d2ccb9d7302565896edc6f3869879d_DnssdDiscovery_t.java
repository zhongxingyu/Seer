// Copyright 2012 Rémi Emonet and Shaun Simpson.
// Thanks to Rémi Emonet for his work.
 
 package uk.co.immutablefix.ClockControl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceEvent;
 import javax.jmdns.ServiceInfo;
 import javax.jmdns.ServiceListener;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 public class DnssdDiscovery extends Object {
 
     android.net.wifi.WifiManager.MulticastLock lock;
     android.os.Handler handler = new android.os.Handler();
     Context context = null;
     List<String> hostnames, ipAdresses;
 
     DnssdDiscovery(Context myContext) {
     	context = myContext;
     	
     	hostnames = new ArrayList<String>();
     	ipAdresses = new ArrayList<String>();
     }
     
     public void init() {
         handler.postDelayed(new Runnable() {
             public void run() {
                 setUp();
             }
             }, 1);
 
     }    
 
     private String type = "_workstation._tcp.local.";
     private JmDNS jmdns = null;
     private ServiceListener listener = null;
     private ServiceInfo serviceInfo;
     private void setUp() {
         android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
         lock = wifi.createMulticastLock("clockcontrolmulticastlock");
         lock.setReferenceCounted(true);
         lock.acquire();
         try {
             jmdns = JmDNS.create();
             jmdns.addServiceListener(type, listener = new ServiceListener() {
 
                 @Override
                 public void serviceResolved(ServiceEvent ev) {
                 }
 
                 @Override
                 public void serviceRemoved(ServiceEvent ev) {
                 }
 
                 @Override
                 public void serviceAdded(ServiceEvent ev) {
                 	ServiceInfo info = jmdns.getServiceInfo(ev.getType(), ev.getName(), 1);
                 	
                 	hostnames.add(info.getName().split(" ")[0]);
                 	ipAdresses.add(info.getHostAddress());
                 }
             });
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
     }
 
     protected void deinit() {
     	if (jmdns != null) {
             if (listener != null) {
                 jmdns.removeServiceListener(type, listener);
                 listener = null;
             }
             jmdns.unregisterAllServices();
             try {
                 jmdns.close();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             jmdns = null;
     	}
     	//repo.stop();
         //s.stop();
         lock.release();
     }
     
     public synchronized String getHostAddress(String host) {
     	/*
     	// iterate over the array
     	for( int i = 0; i < hostnames.size(); i++) {
     	    Log.d(hostnames.get(i), ipAdresses.get(i));
     	}
     	*/
     	int i = hostnames.indexOf(host);
     	    	
    	    if (i >= 0) {
     	    return ipAdresses.get(i); 
     	}
    	    else return host;
     }
 }
