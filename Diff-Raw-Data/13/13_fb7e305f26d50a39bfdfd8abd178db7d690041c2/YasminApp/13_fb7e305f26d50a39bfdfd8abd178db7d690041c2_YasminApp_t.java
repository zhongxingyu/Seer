 /*
  * Copyright (C) 2011 YasminApp Team. All rights reserved.
  */
 
 package com.yasminapp.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class YasminApp implements EntryPoint {
 
   private static final int AES_BLOCK_SIZE = 16;
 
   private TextBox passphrase;
   private TextBox encrypted;
   private String message;
   private ListBox keylist_enc;
   private ListBox keylist_dec;
   private TextArea plaintext;
   private TextArea cipher;
  TextArea w;
   public YasminApp() {
   }
 
   public void onModuleLoad() {
     // Set up uncaught exception handler
     if (GWT.isScript()) {
       GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
         public void onUncaughtException(Throwable e) {
           alert("Uncaught exception: " + e);
         }
       });
     }
 
     Document $doc = Document.get();
 
     // temp variable used for null checking
     Element e = $doc.getElementById("passphrase").cast();
     assert (e != null);
     passphrase = TextBox.wrap(e);
 
    e = $doc.getElementById("encrypted").cast();
     assert (e != null);
     encrypted = TextBox.wrap(e);
 
     e = $doc.getElementById("encrypt").cast();
     assert (e != null);
     Anchor encryptButton = Anchor.wrap(e);
     encryptButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         encrypt();
       }
     });
    w = TextArea.wrap($doc.getElementById("not-encrypted"));
 
    e = $doc.getElementById("decrypted").cast();
     assert (e != null);
     plaintext = TextArea.wrap(e);
 
     e = $doc.getElementById("cipher").cast();
     assert (e != null);
     cipher = TextArea.wrap(e);
 
     e = $doc.getElementById("do-decrypt").cast();
     assert (e != null);
     Anchor decryptButton = Anchor.wrap(e);
     decryptButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent e) {
         decrypt();
       }
     });
 
     // e = $doc.getElementById("generate-key").cast();
     // assert (e != null);
     // Button generateKey = Button.wrap(e);
     // generateKey.addClickHandler(new ClickHandler() {
     // public void onClick(ClickEvent e) {
     // generate_key();
     // }
     // });
     //
     // String[] keys = load_keys();
     // e = $doc.getElementById("keylist-enc").cast();
     // assert (e != null);
     // ListBox keylist_enc = new ListBox();
     // for (String key : keys) {
     // keylist_enc.addItem(key);
     // }
     // keylist_enc.setVisibleItemCount(1);
     //
     // e = $doc.getElementById("keylist-dec").cast();
     // assert (e != null);
     // ListBox keylist_dec = new ListBox();
     // for (String key : keys) {
     // keylist_dec.addItem(key);
     // }
     // keylist_dec.setVisibleItemCount(1);
   }
 
   byte[] padToLength(byte[] in, int len) {
     byte[] out = in;
     if (in.length < len) {
       out = new byte[len];
       System.arraycopy(in, 0, out, 0, in.length);
     }
     return out;
   }
 
   public String generate_key() {
     return new String("DEADBEEFDEADBEEFDEADBEEFDEADBEEF");
   }
 
   public void store_keys(String[] keylist) {
     alert("Implement me, bitches!");
   }
 
   public String[] load_keys() {
     return new String[] { "This", "space", "intentionally", "left", "blank" };
   }
 
   public void encrypt() {
     // Hardcode value for demonstration purposes.
     String result = "☺ң▦▃ᒱ░✍ǯᖧᙁᒡÓΦᓵ8%ᘎLᓟ☺";
    message = w.getValue();
     encrypted.setValue(result);
     cipher.setValue(result);
   }
 
   public void decrypt() {
     String result = message; // retrieve hardcoded value for demonstration
                              // purposes.
     plaintext.setValue(result);
   }
 
   public native void alert(String msg) /*-{
 		$wnd.alert(msg);
   }-*/;
 }
