 package com.isocraft.lib;
 
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Throwables;
 
 /**
  * ISOCraft
  * 
  * A libary containing general references for the mod
  * 
  * @author Turnermator13
  */
 
 public class Reference {
 
    public static final String VERSION = "${version}";
     public static final String MOD_ID = "isocraft";
     public static final String MOD_NAME = "ISOCraft";
     public static final String MOD_CHANNEL = MOD_ID;
     public static final String DEPENDENCIES = "required-after:Forge@[9.11.1.964,)";
 
     public static final String CLIENT_PROXY_CLASS = "com.isocraft.core.proxy.ClientProxy";
     public static final String COMMON_PROXY_CLASS = "com.isocraft.core.proxy.CommonProxy";
 }
