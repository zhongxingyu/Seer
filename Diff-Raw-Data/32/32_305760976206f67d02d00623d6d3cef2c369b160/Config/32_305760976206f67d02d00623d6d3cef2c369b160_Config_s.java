 package ru.tulupov.nsuconnect.request;
 
 import android.net.Uri;
 
 public interface Config {
     Uri AJAX_ENDPOINT = Uri.parse("http://inctalk.net/ajax.php");
     Uri MSG_ENDPOINT = Uri.parse("http://msg.inctalk.net");
 
 }
