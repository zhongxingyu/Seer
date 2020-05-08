  // Copyright (c) 2010 Cilogi All Rights Reserved.
 //
 // File:        ServeModule.java  (05-Oct-2010)
 // Author:      Cilogi
 
 //
 // Copyright in the whole and every part of this source file belongs to
 // Cilogi (the Author) and may not be used,
 // sold, licenced, transferred, copied or reproduced in whole or in
 // part in any manner or form or in or on any media to any person
 // other than in accordance with the terms of The Author's agreement
 // or otherwise without the prior written consent of The Author.  All
 // information contained in this source file is confidential information
 // belonging to The Author and as such may not be disclosed other
 // than in accordance with the terms of The Author's agreement, or
 // otherwise, without the prior written consent of The Author.  As
 // confidential information this source file must be kept fully and
 // effectively secure at all times.
 //
 
 
 package com.cilogi.qr.guice;
 
 import com.cilogi.qr.servlets.FreemarkerServlet;
 import com.cilogi.qr.servlets.QRServlet;
 import com.cilogi.qr.servlets.WakeServlet;
 import com.cilogi.qr.servlets.cart.BasketServlet;
 import com.cilogi.qr.servlets.cart.ItemServlet;
 import com.cilogi.qr.servlets.cart.StoreServlet;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import com.google.inject.servlet.ServletModule;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 public class ServeModule extends ServletModule {
     static final Logger LOG = Logger.getLogger(ServeModule.class.getName());
 
 
     public ServeModule() {
     }
 
     @Override
     protected void configureServlets() {
         serve("*.ftl").with(FreemarkerServlet.class, map(
                 "TemplatePath", "/WEB-INF/classes/ftl",
                 "NoCache", "true",
                 "ContentType", "text/html;charset=UTF-8",
                 "template_update_delay", "0",
                 "default_encoding", "UTF-8",
                 "number_format", "0.###"
         ));
 
 
         serve("/store").with(StoreServlet.class);
         serve("/store/item/*").with(ItemServlet.class);
         serve("/store/basket").with(BasketServlet.class);
 
        serve("*/qr").with(QRServlet.class);
        
         serve("/cron/wake").with(WakeServlet.class);
         // should serve / with servlet that delivers 404
 
     }
 
     private static Map<String,String> map(String... params) {
         Preconditions.checkArgument(params.length % 2 == 0, "You have to have a n even number of map params");
         Map<String,String> map = Maps.newHashMap();
         for (int i = 0; i < params.length; i+=2) {
             map.put(params[i], params[i+1]);
         }
         return map;
     }
 }
