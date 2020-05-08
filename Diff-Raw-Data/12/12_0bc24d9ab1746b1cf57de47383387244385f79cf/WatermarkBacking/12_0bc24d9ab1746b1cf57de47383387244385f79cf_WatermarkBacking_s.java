/*
 * Copyright 2008 - 2009 OC Tanner Company.  All Rights Reserved.
 *
 * This software is the property of OC Tanner Company.  Use of this software in whole or in
 * part without the express written consent of OC Tanner is strictly prohibited.
 *
 * $Id$
 */
 package org.example.icefaces.watermark;
 
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;
 
 import java.io.Serializable;
 
 @Name("watermark")
 @Scope(ScopeType.PAGE)
 public class WatermarkBacking implements Serializable
 {
     private String value;
 
     public String getValue()
     {
         return value;
     }
 
     public void setValue(String value)
     {
         this.value = value;
     }
 }
