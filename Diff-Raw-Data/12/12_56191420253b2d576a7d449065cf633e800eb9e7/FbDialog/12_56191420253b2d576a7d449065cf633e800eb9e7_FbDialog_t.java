 /*
  * Copyright 2010 Facebook, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.facebook.android;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Base64;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import com.facebook.android.Facebook.DialogListener;
 
 public class FbDialog extends Dialog {
 
     static final int FB_BLUE = 0xFF6D84B4;
     static final float[] DIMENSIONS_DIFF_LANDSCAPE = {20, 60};
     static final float[] DIMENSIONS_DIFF_PORTRAIT = {40, 60};
     static final FrameLayout.LayoutParams FILL =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
     static final int MARGIN = 4;
     static final int PADDING = 2;
     static final String DISPLAY_STRING = "touch";
     static final String FB_ICON = "icon.png";
 
     private String mUrl;
     private DialogListener mListener;
     private ProgressDialog mSpinner;
     private ImageView mCrossImage;
     private WebView mWebView;
     private FrameLayout mContent;
 
     public FbDialog(Context context, String url, DialogListener listener) {
         super(context, android.R.style.Theme_Translucent_NoTitleBar);
         mUrl = url;
         mListener = listener;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mSpinner = new ProgressDialog(getContext());
         mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
         mSpinner.setMessage("Loading...");
 
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         mContent = new FrameLayout(getContext());
 
         /* Create the 'x' image, but don't add to the mContent layout yet
          * at this point, we only need to know its drawable width and height 
          * to place the webview
          */
         createCrossImage();
         
         /* Now we know 'x' drawable width and height, 
          * layout the webivew and add it the mContent layout
          */
         int crossWidth = mCrossImage.getDrawable().getIntrinsicWidth();
         setUpWebView(crossWidth / 2);
         
         /* Finally add the 'x' image to the mContent layout and
          * add mContent to the Dialog view
          */
        mContent.addView(mCrossImage);
        addContentView(mContent, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
     }
 
     private void createCrossImage() {
         mCrossImage = new ImageView(getContext());
         // Dismiss the dialog when user click on the 'x'
         mCrossImage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 mListener.onCancel();
                 FbDialog.this.dismiss();
             }
         });
 
         int px30 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30,
             getContext().getResources().getDisplayMetrics());
         mCrossImage.setLayoutParams(new FrameLayout.LayoutParams(px30, px30));
         mCrossImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
 
         byte[] decodedString = Base64.decode(CLOSE_BT, Base64.DEFAULT);
         Bitmap cross = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
         mCrossImage.setImageBitmap(cross);
         /* 'x' should not be visible while webview is loading
          * make it visible only after webview has fully loaded
         */
         mCrossImage.setVisibility(View.INVISIBLE);
     }
 
     private void setUpWebView(int margin) {
         LinearLayout webViewContainer = new LinearLayout(getContext());
         mWebView = new WebView(getContext());
         mWebView.setVerticalScrollBarEnabled(false);
         mWebView.setHorizontalScrollBarEnabled(false);
         mWebView.setWebViewClient(new FbDialog.FbWebViewClient());
         mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.loadUrl(mUrl);
         mWebView.setLayoutParams(FILL);
         mWebView.setVisibility(View.INVISIBLE);
 
         webViewContainer.setPadding(margin, margin, margin, margin);
         webViewContainer.addView(mWebView);
         mContent.addView(webViewContainer);
     }
 
     private class FbWebViewClient extends WebViewClient {
 
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             Util.logd("Facebook-WebView", "Redirect URL: " + url);
             if (url.startsWith(Facebook.REDIRECT_URI)) {
                 Bundle values = Util.parseUrl(url);
 
                 String error = values.getString("error");
                 if (error == null) {
                     error = values.getString("error_type");
                 }
 
                 if (error == null) {
                     mListener.onComplete(values);
                 } else if (error.equals("access_denied") ||
                     error.equals("OAuthAccessDeniedException")) {
                     mListener.onCancel();
                 } else {
                     mListener.onFacebookError(new FacebookError(error));
                 }
 
                 FbDialog.this.dismiss();
                 return true;
             } else if (url.startsWith(Facebook.CANCEL_URI)) {
                 mListener.onCancel();
                 FbDialog.this.dismiss();
                 return true;
             } else if (url.contains(DISPLAY_STRING)) {
                 return false;
             }
             // launch non-dialog URLs in a full browser
             getContext().startActivity(
                 new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
             return true;
         }
 
         @Override
         public void onReceivedError(WebView view, int errorCode,
                                     String description, String failingUrl) {
             super.onReceivedError(view, errorCode, description, failingUrl);
             mListener.onError(
                 new DialogError(description, errorCode, failingUrl));
             FbDialog.this.dismiss();
         }
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             Util.logd("Facebook-WebView", "Webview loading URL: " + url);
             super.onPageStarted(view, url, favicon);
             mSpinner.show();
         }
 
         @Override
         public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
             mSpinner.dismiss();
             /* 
              * Once webview is fully loaded, set the mContent background to be transparent
              * and make visible the 'x' image. 
              */
             mContent.setBackgroundColor(0xcc000000);
             mWebView.setVisibility(View.VISIBLE);
             mCrossImage.setVisibility(View.VISIBLE);
         }
     }
 
     @Override
     public void onBackPressed() {
         mListener.onCancel();
         super.onBackPressed();
     }
 
     public static final String CLOSE_BT =
         "iVBORw0KGgoAAAANSUhEUgAAACsAAAArCAYAAADhXXHAAAAACXBIWXMAAAsTAAALEwEAmpwYAAAK\n" +
             "T2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHjanVNnVFPpFj333vRCS4iAlEtvUhUIIFJCi4AU\n" +
             "kSYqIQkQSoghodkVUcERRUUEG8igiAOOjoCMFVEsDIoK2AfkIaKOg6OIisr74Xuja9a89+bN/rXX\n" +
             "Pues852zzwfACAyWSDNRNYAMqUIeEeCDx8TG4eQuQIEKJHAAEAizZCFz/SMBAPh+PDwrIsAHvgAB\n" +
             "eNMLCADATZvAMByH/w/qQplcAYCEAcB0kThLCIAUAEB6jkKmAEBGAYCdmCZTAKAEAGDLY2LjAFAt\n" +
             "AGAnf+bTAICd+Jl7AQBblCEVAaCRACATZYhEAGg7AKzPVopFAFgwABRmS8Q5ANgtADBJV2ZIALC3\n" +
             "AMDOEAuyAAgMADBRiIUpAAR7AGDIIyN4AISZABRG8lc88SuuEOcqAAB4mbI8uSQ5RYFbCC1xB1dX\n" +
             "Lh4ozkkXKxQ2YQJhmkAuwnmZGTKBNA/g88wAAKCRFRHgg/P9eM4Ors7ONo62Dl8t6r8G/yJiYuP+\n" +
             "5c+rcEAAAOF0ftH+LC+zGoA7BoBt/qIl7gRoXgugdfeLZrIPQLUAoOnaV/Nw+H48PEWhkLnZ2eXk\n" +
             "5NhKxEJbYcpXff5nwl/AV/1s+X48/Pf14L7iJIEyXYFHBPjgwsz0TKUcz5IJhGLc5o9H/LcL//wd\n" +
             "0yLESWK5WCoU41EScY5EmozzMqUiiUKSKcUl0v9k4t8s+wM+3zUAsGo+AXuRLahdYwP2SycQWHTA\n" +
             "4vcAAPK7b8HUKAgDgGiD4c93/+8//UegJQCAZkmScQAAXkQkLlTKsz/HCAAARKCBKrBBG/TBGCzA\n" +
             "BhzBBdzBC/xgNoRCJMTCQhBCCmSAHHJgKayCQiiGzbAdKmAv1EAdNMBRaIaTcA4uwlW4Dj1wD/ph\n" +
             "CJ7BKLyBCQRByAgTYSHaiAFiilgjjggXmYX4IcFIBBKLJCDJiBRRIkuRNUgxUopUIFVIHfI9cgI5\n" +
             "h1xGupE7yAAygvyGvEcxlIGyUT3UDLVDuag3GoRGogvQZHQxmo8WoJvQcrQaPYw2oefQq2gP2o8+\n" +
             "Q8cwwOgYBzPEbDAuxsNCsTgsCZNjy7EirAyrxhqwVqwDu4n1Y8+xdwQSgUXACTYEd0IgYR5BSFhM\n" +
             "WE7YSKggHCQ0EdoJNwkDhFHCJyKTqEu0JroR+cQYYjIxh1hILCPWEo8TLxB7iEPENyQSiUMyJ7mQ\n" +
             "AkmxpFTSEtJG0m5SI+ksqZs0SBojk8naZGuyBzmULCAryIXkneTD5DPkG+Qh8lsKnWJAcaT4U+Io\n" +
             "UspqShnlEOU05QZlmDJBVaOaUt2ooVQRNY9aQq2htlKvUYeoEzR1mjnNgxZJS6WtopXTGmgXaPdp\n" +
             "r+h0uhHdlR5Ol9BX0svpR+iX6AP0dwwNhhWDx4hnKBmbGAcYZxl3GK+YTKYZ04sZx1QwNzHrmOeZ\n" +
             "D5lvVVgqtip8FZHKCpVKlSaVGyovVKmqpqreqgtV81XLVI+pXlN9rkZVM1PjqQnUlqtVqp1Q61Mb\n" +
             "U2epO6iHqmeob1Q/pH5Z/YkGWcNMw09DpFGgsV/jvMYgC2MZs3gsIWsNq4Z1gTXEJrHN2Xx2KruY\n" +
             "/R27iz2qqaE5QzNKM1ezUvOUZj8H45hx+Jx0TgnnKKeX836K3hTvKeIpG6Y0TLkxZVxrqpaXllir\n" +
             "SKtRq0frvTau7aedpr1Fu1n7gQ5Bx0onXCdHZ4/OBZ3nU9lT3acKpxZNPTr1ri6qa6UbobtEd79u\n" +
             "p+6Ynr5egJ5Mb6feeb3n+hx9L/1U/W36p/VHDFgGswwkBtsMzhg8xTVxbzwdL8fb8VFDXcNAQ6Vh\n" +
             "lWGX4YSRudE8o9VGjUYPjGnGXOMk423GbcajJgYmISZLTepN7ppSTbmmKaY7TDtMx83MzaLN1pk1\n" +
             "mz0x1zLnm+eb15vft2BaeFostqi2uGVJsuRaplnutrxuhVo5WaVYVVpds0atna0l1rutu6cRp7lO\n" +
             "k06rntZnw7Dxtsm2qbcZsOXYBtuutm22fWFnYhdnt8Wuw+6TvZN9un2N/T0HDYfZDqsdWh1+c7Ry\n" +
             "FDpWOt6azpzuP33F9JbpL2dYzxDP2DPjthPLKcRpnVOb00dnF2e5c4PziIuJS4LLLpc+Lpsbxt3I\n" +
             "veRKdPVxXeF60vWdm7Obwu2o26/uNu5p7ofcn8w0nymeWTNz0MPIQ+BR5dE/C5+VMGvfrH5PQ0+B\n" +
             "Z7XnIy9jL5FXrdewt6V3qvdh7xc+9j5yn+M+4zw33jLeWV/MN8C3yLfLT8Nvnl+F30N/I/9k/3r/\n" +
             "0QCngCUBZwOJgUGBWwL7+Hp8Ib+OPzrbZfay2e1BjKC5QRVBj4KtguXBrSFoyOyQrSH355jOkc5p\n" +
             "DoVQfujW0Adh5mGLw34MJ4WHhVeGP45wiFga0TGXNXfR3ENz30T6RJZE3ptnMU85ry1KNSo+qi5q\n" +
             "PNo3ujS6P8YuZlnM1VidWElsSxw5LiquNm5svt/87fOH4p3iC+N7F5gvyF1weaHOwvSFpxapLhIs\n" +
             "OpZATIhOOJTwQRAqqBaMJfITdyWOCnnCHcJnIi/RNtGI2ENcKh5O8kgqTXqS7JG8NXkkxTOlLOW5\n" +
             "hCepkLxMDUzdmzqeFpp2IG0yPTq9MYOSkZBxQqohTZO2Z+pn5mZ2y6xlhbL+xW6Lty8elQfJa7OQ\n" +
             "rAVZLQq2QqboVFoo1yoHsmdlV2a/zYnKOZarnivN7cyzytuQN5zvn//tEsIS4ZK2pYZLVy0dWOa9\n" +
             "rGo5sjxxedsK4xUFK4ZWBqw8uIq2Km3VT6vtV5eufr0mek1rgV7ByoLBtQFr6wtVCuWFfevc1+1d\n" +
             "T1gvWd+1YfqGnRs+FYmKrhTbF5cVf9go3HjlG4dvyr+Z3JS0qavEuWTPZtJm6ebeLZ5bDpaql+aX\n" +
             "Dm4N2dq0Dd9WtO319kXbL5fNKNu7g7ZDuaO/PLi8ZafJzs07P1SkVPRU+lQ27tLdtWHX+G7R7ht7\n" +
             "vPY07NXbW7z3/T7JvttVAVVN1WbVZftJ+7P3P66Jqun4lvttXa1ObXHtxwPSA/0HIw6217nU1R3S\n" +
             "PVRSj9Yr60cOxx++/p3vdy0NNg1VjZzG4iNwRHnk6fcJ3/ceDTradox7rOEH0x92HWcdL2pCmvKa\n" +
             "RptTmvtbYlu6T8w+0dbq3nr8R9sfD5w0PFl5SvNUyWna6YLTk2fyz4ydlZ19fi753GDborZ752PO\n" +
             "32oPb++6EHTh0kX/i+c7vDvOXPK4dPKy2+UTV7hXmq86X23qdOo8/pPTT8e7nLuarrlca7nuer21\n" +
             "e2b36RueN87d9L158Rb/1tWeOT3dvfN6b/fF9/XfFt1+cif9zsu72Xcn7q28T7xf9EDtQdlD3YfV\n" +
             "P1v+3Njv3H9qwHeg89HcR/cGhYPP/pH1jw9DBY+Zj8uGDYbrnjg+OTniP3L96fynQ89kzyaeF/6i\n" +
             "/suuFxYvfvjV69fO0ZjRoZfyl5O/bXyl/erA6xmv28bCxh6+yXgzMV70VvvtwXfcdx3vo98PT+R8\n" +
             "IH8o/2j5sfVT0Kf7kxmTk/8EA5jz/GMzLdsAAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADq\n" +
             "YAAAOpgAABdvkl/FRgAAB9hJREFUeNq8mUFMG2cWx//2GOw4bB2g0y0hIXjj1iosMSVqpSS7W5Sk\n" +
             "rQ9R0i0uEASHRBxAqzhC4hZV4uBVriSVIrHaFCUCJeHACpUDhyqKKmIWidSbIgXoGsxu7A1aFkxK\n" +
             "PQQ8+NvL+9A34zE2gd1Pehp5xt97v3nzzfvee2PC7oZJOJp05wCACUemO/faxnY6h4uZRKKjeI0J\n" +
             "kgKwSccUP88YYyaT6X8CyyE4mAWA5dSpU/tv3rz5G1mWj9ntdmdeXt4Bq9V6eH19/V/JZHJJUZTI\n" +
             "4uLiD36/f/Tx48cJACoJvwG2G28bQXI4K4ACAEWjo6Ofx+Pxb1Kp1BrLYaRSqbV4PP7N6Ojo5wCK\n" +
             "SI+V9Jpf8ymngUoA8gHYARQNDQ19nEgkvme7GIlE4vuhoaGPCdpO+qVswKYcPZoPYN/z58+vHTp0\n" +
             "yK//45MnT/Dw4UOEw2EsLy8jEomgrKwMsizD5XLh9OnTOH78eJqBaDR68/Dhw38E8ArAurA82Ot4\n" +
             "1AbAcfbs2V8tLS2NiN5RVZUFAgFWWlrKdC+ToZSWlrJAIMBUVdV4eWlpaaSuru4dAA6yJ+1kSYig\n" +
             "B1paWt5TFGVKNDAwMMAqKipygtRLRUUFGxgY0AArijLV0tLyHoADOwHmoFYAB0pKSo68fPnyr6Li\n" +
             "rq6u14LUS1dXlwZ4ZWVl/NixY04CtuYCbKb1+QaAg5FI5GtRYXNz856AcmlubtYARyKRrwEcJPv5\n" +
             "xJPRqxYA+wG81dPT81kqldrkivx+/56CcvH7/WJ42+zp6fkMwC+Jw5LJu2ZaL0UAnPF4/G9cSTAY\n" +
             "NDRUVFTEvF5vTlDnz59nsiwbXgsGg+Jy+AGAkzhsRt41AcijQH2wr6/vsvh4PB6PIWgoFGKqqjKf\n" +
             "z7ctaENDA1NVlYVCIUNgj8ejiRJ9fX2XaTkUEJdJ71Ur3Y0rGo1+yycGAoE05bIss1AopAljmYA5\n" +
             "KB+ZgAOBwNZ/otHotwBcxGPVe1eiNVJSWVn5gaqq63yiURwdHh5O25WMgPWgfAwPDxvGYUHXemVl\n" +
             "5QcASohL0i8BB4Dy27dv/4FPmpiYMPSWy+Vi0Wh0W+BMoNFolLlcLkO9ExMTW/+7c+fOFQDlFMq2\n" +
             "loKJXP0mgHfHxsb+xCdcv3494zrcDvjWrVs7BtUvhfHx8T8DeJe4rABMZmEjkADkFxcXl3OXz8zM\n" +
             "ZAzI4XAYtbW1iMVimvOSJKG9vR2SJGnOx2IxnDlzBuFweFudfBQWFpYJWZkkwvKEJc9msxXyCYuL\n" +
             "i9tud5mA9YODbnfzenvEYRETe7MQDSQAFovFYuUT1tbWsiYSHHhhYcHw+sLCQk6genvEIcJqkl4T\n" +
             "APOaMMPhcOSU+VRXV0OWZcNrsiyjuro6Jz2iPeIwC2Fry7NbtdPq6uqqaCjb8Pl8uH//ftoaFddw\n" +
             "f38/mpqasuoS7RGHpgg166pPFolE/sEnVFVV7QpUBL57925WYNEecYjRAmZdBZp69OjRNJ9w4sSJ\n" +
             "HYPGYjGcO3fOMEpkAxbtEYemGhZ3r1IA79vt9gvJZHKDxzujJNvn8xnG0RcvXjC32501Djc1NRkm\n" +
             "5Xwkk8kNu91+AcD7xLUfgGQWvKoCSCqK8ioUCo3xO2xvb0/zgNfrTfPowsICamtrt9767eKw1+tN\n" +
             "0ynaCYVCY4qivAKQ1NdlfAcrph3jo9bW1muiN2pqatI80dvba+jRbDtdf38/kyRJ85+amhqN91tb\n" +
             "W68B+Ih4ivkOJuYGbwA4AuBDAHWzs7M/ClufIUhvb++2oHpgI1AAbHx8fAt0dnb2RwB1xHGEuDRp\n" +
             "okT1+9sAfg3g0/r6+i9VYWF2dnZmrFpzrW6NQDs7O8X1rNbX138J4FPieJu4JH3yzfPZowBOAvhi\n" +
             "cHBQkwu2tbXtaUnT1tamefyDg4PDAL4g+0eFfNZkVH8VUA5ZBeATh8PROjU1NSMq7Ojo2BPQjo4O\n" +
             "DejU1NSMzWa7DOATsl9CPIZ1GK8WCqkG+hDABVmWr8zNzc2LioPBoGGpk4t4PB5NzcUYY3Nzc/Oy\n" +
             "LF8BcIHsOonDmqnC5d61A3gLgBvAbwH4nE5n5/T0dFgfM7u7u7O+XFzcbjfr7u5Oi7vT09Nhp9PZ\n" +
             "CcBH9txk3673qsnAu5puIQCZJsv37t37fX19/Wmz2ayZ9+zZM4yMjGBmZgYbGxuYnJxEVVUV8vPz\n" +
             "4Xa74fV6UVFRoTGUSqXYwMDAw4sXL/4FwCKAf9NxGcDPut5X1o6MA8Aheitr6c7bGhsbv5qcnJzd\n" +
             "TRdxcnJytrGx8SsAbaS3luyUkl3Djoxpm+5hHtXtBaSgiKQQgKOhocF99erV33k8nqN2u92WLaNS\n" +
             "FOXV06dPZ2/cuPHdgwcPZgC8BBAnTy4DWAGQoI5i0qibaMrS7syju7RTcHZQAeeg3wUA9l26dOmd\n" +
             "kydPljmdzjctFoulvLxcnp+fX1RVVY1EIv8JBoP/7O3t/TuANXrEPxHsCh1/AqDQo09manvuqD9L\n" +
             "CcUvSApI7HQtj0QM4JtkPEmgCsH+DGCVJEHXNrL1Z7O1FU1iyUNethEgh9xH5/IFWP4BhMNu0ONd\n" +
             "E6AVXRN5M1sjOZemrUkHnUdg+QRvFUAturi4lc0R8DrJBklSB8n2+muNWSjdOaBFADULnk0JwKoA\n" +
             "vrkTyN1+B4MAphf9R7tUBtnxB7zdfs75v35h/O8A3ZZHRXtYwqsAAAAASUVORK5CYII=";
 }
