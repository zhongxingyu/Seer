 /**
  * ========= CONFIDENTIAL =========
  *
  * Copyright (C) 2012 enStratus Networks Inc - ALL RIGHTS RESERVED
  *
  * ====================================================================
  *  NOTICE: All information contained herein is, and remains the
  *  property of enStratus Networks Inc. The intellectual and technical
  *  concepts contained herein are proprietary to enStratus Networks Inc
  *  and may be covered by U.S. and Foreign Patents, patents in process,
  *  and are protected by trade secret or copyright law. Dissemination
  *  of this information or reproduction of this material is strictly
  *  forbidden unless prior written permission is obtained from
  *  enStratus Networks Inc.
  * ====================================================================
  */
 package org.dasein.cloud.atmos;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AbstractCloud;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.atmos.storage.AtmosStorageServices;
 import org.dasein.cloud.compute.ComputeServices;
 import org.dasein.cloud.compute.VirtualMachineSupport;
 import org.dasein.cloud.storage.BlobStoreSupport;
 import org.dasein.cloud.storage.StorageServices;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 /**
  * Provider class for interacting with the EMC Atmos REST API.
  * <p>Created by George Reese: 10/5/12 8:48 AM</p>
  * @author George Reese
  * @version 2012.09 initial version
  * @since 2012.09
  */
 public class Atmos extends AbstractCloud {
     static private String getLastItem(String name) {
         int idx = name.lastIndexOf('.');
 
         if( idx < 0 ) {
             return name;
         }
         else if( idx == (name.length()-1) ) {
             return "";
         }
         return name.substring(idx+1);
     }
 
     static public Logger getLogger(Class<?> cls) {
         String pkg = getLastItem(cls.getPackage().getName());
 
         if( pkg.equals("atmos") ) {
             pkg = "";
         }
         else {
             pkg = pkg + ".";
         }
         return Logger.getLogger("dasein.cloud.atmos.std." + pkg + getLastItem(cls.getName()));
     }
 
     static public Logger getWireLogger(Class<?> cls) {
         return Logger.getLogger("dasein.cloud.atmos.wire." + getLastItem(cls.getPackage().getName()) + "." + getLastItem(cls.getName()));
     }
 
     public Atmos() { }
 
     public @Nonnull AtmosProvider getAtmosProvider() {
         if( getProviderName().equalsIgnoreCase("at&t") || getProviderName().equalsIgnoreCase("att") ) {
             return AtmosProvider.ATT;
         }
         return AtmosProvider.OTHER;
     }
 
     @Override
     public @Nonnull String getCloudName() {
         ProviderContext ctx = getContext();
         String cloudName = (ctx == null ? null : ctx.getCloudName());
 
         return (cloudName == null ? "Atmos" : cloudName);
     }
 
     @Override
     public @Nonnull AtmosGeography getDataCenterServices() {
         return new AtmosGeography(this);
     }
 
     @Override
     public @Nonnull String getProviderName() {
         ProviderContext ctx = getContext();
         String providerName = (ctx == null ? null : ctx.getProviderName());
 
         return (providerName == null ? "EMC/Atmos" : providerName);
     }
 
     @Override
     public @Nonnull AtmosStorageServices getStorageServices() {
         return new AtmosStorageServices(this);
     }
 
     public @Nonnull String formatTime(@Nonnegative long timestamp) {
         SimpleDateFormat fmt;
 
         fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
         fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
         return fmt.format(new Date(timestamp));
     }
 
     public @Nonnegative long parseTime(@Nonnull String timestamp) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2009-02-03T05:26:32.612278
 
         try {
             return df.parse(timestamp).getTime();
         }
         catch( ParseException e ) {
             df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy"); //Sun Jul 04 02:18:02 EST 2010
 
             try {
                 return df.parse(timestamp).getTime();
             }
             catch( ParseException another ) {
                 return 0L;
             }
         }
     }
 
     @Override
     public String testContext() {
         ProviderContext ctx = getContext();
 
         if( ctx == null ) {
             return null;
         }
         try {
             StorageServices storage = getStorageServices();
             BlobStoreSupport support = storage.getBlobStoreSupport();
 
             if( support == null || !support.isSubscribed() ) {
                 return null;
             }
         }
         catch( Throwable t ) {
             return null;
         }
         return ctx.getAccountNumber();
     }
 }
