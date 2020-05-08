 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.transformers;
 
 import org.mule.ibeans.api.application.Transformer;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * Some simple transformers commonly used by iBeans
  */
 public class CommonTransformers
 {
     @Transformer
     public URL transformStringToURL(String string) throws MalformedURLException
     {
        try
        {
            return new URL(string);
        }
        catch (MalformedURLException e)
        {
            //provide a more descriptive error message
            throw new MalformedURLException(e.getMessage() + " " + string);
        }
     }
 }
