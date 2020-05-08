 package com.experttag.util.impl;
 
import com.experttag.util.pool.core.ConnectionLeakEvent;
import com.experttag.util.pool.core.ConnectionLeakListener;

 
 /**
  * Created by IntelliJ IDEA.
  * User: kerneldebugger
  * Date: Oct 2, 2005
  * Time: 3:02:37 PM
  * To change this template use File | Settings | File Templates.
  */
 public class LeakDetectorImpl implements ConnectionLeakListener {
     public void connectionTimeOut(ConnectionLeakEvent cle) {
         System.out.println("Leak Detected for a user: " + cle.getOwner());
     }
 }
