 /*
  * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
  * @author tags. See the copyright.txt file in the distribution for a full
  * listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  *
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this software; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
  * site: http://www.fsf.org.
  */
 
 package org.jboss.pressgang.ccms.util;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import lombok.extern.slf4j.Slf4j;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 @Slf4j
 public enum Constants {
     // Constants used by page and workflow objects
     propFile("setup.properties"),
    pressGangInstance("pressang.instance.url"),
     webDriverType("webdriver.type"),
     chrome, firefox, htmlUnit;
 
     public static final int FIFTY_SEC_IN_MS = 50000;
     public static final int THIRTY_SEC = 30;
     private String value;
 
     private Constants(String value) {
         this.value = value;
     }
 
     private Constants() {
         this(null);
         value = name();
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this).add("name", name()).add("value", value).toString();
     }
 
     public String value() {
         return value;
     }
 
     public static Function<WebElement, String> findDivButtonNamesByHtmlFaceText() {
         return new Function<WebElement, String>() {
             @Override
             public String apply(WebElement div) {
                 return (div.findElement(By.className("html-face")).getText());
             }
         };
     }
 }
