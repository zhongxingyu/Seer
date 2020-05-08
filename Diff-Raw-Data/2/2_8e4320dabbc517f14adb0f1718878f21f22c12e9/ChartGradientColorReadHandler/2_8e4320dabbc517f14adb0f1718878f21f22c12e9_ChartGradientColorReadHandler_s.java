 /*
  * Copyright 2007 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * Created 3/25/2008 
  * @author Ravi Hasija 
  */
 
 package org.pentaho.chart.css.parser.stylehandler;
 
 import org.pentaho.reporting.libraries.css.model.StyleKey;
 import org.pentaho.reporting.libraries.css.parser.CSSValueFactory;
 import org.pentaho.reporting.libraries.css.parser.CSSValueReadHandler;
 import org.pentaho.reporting.libraries.css.parser.stylehandler.color.ColorReadHandler;
 import org.pentaho.reporting.libraries.css.values.CSSValue;
 import org.pentaho.reporting.libraries.css.values.CSSValuePair;
 import org.w3c.css.sac.LexicalUnit;
 
 /**
 * The style parser for the <code>-x-pentaho-chart-axis-type-dimension</code> style.
  *
  * @author Ravi Hasija
  */
 public class ChartGradientColorReadHandler implements CSSValueReadHandler
 {
   public ChartGradientColorReadHandler()
   {
   }
 
   public CSSValue createValue(final StyleKey name, final LexicalUnit unit)
   {
     final CSSValue firstValue = ColorReadHandler.createColorValue(unit);
     CSSValue secondValue = null;
 
     if (firstValue != null)
     {
       // Parse the comma and move to the next field
       final LexicalUnit secondUnit = CSSValueFactory.parseComma(unit);
       if (secondUnit != null)
       {
         secondValue = ColorReadHandler.createColorValue(secondUnit);
       }
     }
 
     if (firstValue != null && secondValue != null)
     {
       return new CSSValuePair(firstValue, secondValue);
     }
 
     return null;
   }
 }
