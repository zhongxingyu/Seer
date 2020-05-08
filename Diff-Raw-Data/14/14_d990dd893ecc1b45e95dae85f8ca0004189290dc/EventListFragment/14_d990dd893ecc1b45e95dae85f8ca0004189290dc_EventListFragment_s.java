 package com.nurun.activemtl.ui.fragment;
 
 import android.annotation.SuppressLint;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 
 import com.google.android.gms.location.LocationClient;
 import com.nurun.activemtl.ActiveMtlApplication;
 import com.nurun.activemtl.ActiveMtlConfiguration;
 import com.nurun.activemtl.R;
 import com.nurun.activemtl.model.EventType;
 
 public class EventListFragment extends Fragment {
 
     private static final String EXTRA_EVENT_TYPE = "EXTRA_EVENT_TYPE";
 
     @SuppressLint("SetJavaScriptEnabled")
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         WebView webview = (WebView) inflater.inflate(R.layout.event_fragment, container, false);
         WebSettings settings = webview.getSettings();
         settings.setJavaScriptEnabled(true);
         webview.setWebChromeClient(new WebChromeClient());
         webview.setWebViewClient(new WebViewClient());
         return webview;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         LocationClient locationClient = (LocationClient) getActivity().getApplicationContext().getSystemService(ActiveMtlApplication.LOCATION_CLIENT);
         Location lastLocation = locationClient.getLastLocation();
         if (lastLocation == null) {
             getView().loadUrl(ActiveMtlConfiguration.getInstance(getActivity()).getListUrl((EventType) getArguments().getSerializable(EXTRA_EVENT_TYPE)));
         } else {
             getView().loadUrl(
                     ActiveMtlConfiguration.getInstance(getActivity()).getListUrl((EventType) getArguments().getSerializable(EXTRA_EVENT_TYPE),
                             lastLocation.getLatitude(), lastLocation.getLongitude()));
         }
     }
 
     @Override
     public WebView getView() {
        FrameLayout  wrapper = (FrameLayout) super.getView();
         return (WebView) ((wrapper != null) ? wrapper.getChildAt(0) : null);
     }
 
     public static Fragment newFragment(EventType eventType) {
         EventListFragment fragment = new EventListFragment();
         Bundle bundle = new Bundle();
         bundle.putSerializable(EXTRA_EVENT_TYPE, eventType);
         fragment.setArguments(bundle);
         return fragment;
     }
 }
