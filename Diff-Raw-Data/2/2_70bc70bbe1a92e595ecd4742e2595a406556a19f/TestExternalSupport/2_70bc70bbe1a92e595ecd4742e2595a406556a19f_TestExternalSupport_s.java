 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.support;
 
 import org.junit.Before;
 import org.junit.Rule;
 
 public abstract class TestExternalSupport {
 
   protected static final String ICT_MANAGERS_GROUP = "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:selenium-ict-managers";
   protected static final String ICT_MANAGERS_GROUP_2 = "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:selenium-ict-managers2";
 
   protected static final String BOD_PORT_ID_1 = "Mock_ETH10G-1-13-1";
   protected static final String BOD_PORT_ID_2 = "Mock_ETH10G-1-13-2";
   protected static final String BOD_PORT_ID_4 = "Mock_ETH-1-13-4";
 
   protected static final String USERS_GROUP = "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:selenium-users";
   protected static final String NMS_PORT_ID_1 = "00-21-E1-D6-D6-70_ETH10G-1-13-1";
   protected static final String NMS_PORT_ID_2 = "00-21-E1-D6-D6-70_ETH10G-1-13-2";
   protected static final String NMS_PORT_ID_3 = "00-21-E1-D6-D5-DC_ETH-1-13-5";
  protected static final String NMS_PORT_ID_4 = "00-21-E1-D6-D5-DC_ETH-1-13-4";
 
   private static BodWebDriver webDriver = new BodWebDriver();
 
   @Rule
   public Screenshotter screenshotter = new Screenshotter(webDriver);
 
   @Before
   public final void initialize() {
     webDriver.initializeOnce();
   }
 
   protected BodWebDriver getWebDriver() {
     return webDriver;
   }
 
   protected BodUserWebDriver getUserDriver() {
     return webDriver.getUserDriver();
   }
 
   protected BodManagerWebDriver getManagerDriver() {
     return webDriver.getManagerDriver();
   }
 
   protected BodNocWebDriver getNocDriver() {
     return webDriver.getNocDriver();
   }
 }
