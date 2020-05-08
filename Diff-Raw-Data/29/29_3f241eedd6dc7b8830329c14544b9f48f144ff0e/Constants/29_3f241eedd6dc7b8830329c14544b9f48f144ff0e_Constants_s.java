 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.pubsubhubbub;
 
 import java.nio.charset.Charset;
 import java.util.concurrent.TimeUnit;
 
 public class Constants
 {
     public static final Charset UTF8_ENCODING = Charset.forName("UTF-8");
 
     // TODO make this configurable on the hub
     public static final long DEFAULT_LEASE_SECONDS = TimeUnit.DAYS.toSeconds(7);
     public static final long VERIFICATION_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(15L);
 
     public static final int MAXIMUM_SECRET_SIZE = 200;
 
     // Common Parameters
     // Mandatory
     public static final String HUB_MODE_PARAM = "hub.mode";
 
     // Subscription Parameters
     // Mandatory
     public static final String HUB_VERIFY_PARAM = "hub.verify";
     public static final String HUB_TOPIC_PARAM = "hub.topic";
     public static final String HUB_CALLBACK_PARAM = "hub.callback";
     public static final String HUB_CHALLENGE_PARAM = "hub.challenge";
     // Optional
     public static final String HUB_LEASE_SECONDS_PARAM = "hub.lease_seconds";
     public static final String HUB_SECRET_PARAM = "hub.secret";
     public static final String HUB_VERIFY_TOKEN_PARAM = "hub.verify_token";
 }
