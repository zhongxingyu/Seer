 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.jive.api.impl;
 
 import org.mule.modules.jive.api.EntityType;
 import org.mule.modules.jive.api.PayloadOperation;
 import org.mule.modules.jive.api.xml.XmlMapper;
 
import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Map;
 
import org.apache.commons.lang.StringUtils;

 public class StandardCreateOperation implements PayloadOperation
 {
     public static final PayloadOperation STANDARD = new StandardCreateOperation();
 
     /* (non-Javadoc)
      * @see org.mule.modules.jive.api.PayloadOperation#execute(com.sun.jersey.api.client.WebResource, org.mule.modules.jive.api.xml.XmlMapper, org.mule.modules.jive.api.EntityType, java.util.Map)
      */
     @Override
     public Map<String, Object> execute(WebResource resource,
                                        XmlMapper mapper,
                                        EntityType type,
                                        Map<String, Object> entityData)
     {
         final Writer writer = new StringWriter();
         mapper.map2xml("create" + type.getXmlRootElementName(), entityData, writer);
        ClientResponse result = resource.path(type.getBasePluralUri()).post(ClientResponse.class, writer.toString());
        if (result.getStatus() > 300)
        {
            throw new RuntimeException(StringUtils.substringAfterLast(
                StringUtils.substringBefore(result.getEntity(String.class), "</ns1:faultstring>"), ">"));
        }
        return mapper.xml2map(result.getEntity(String.class));
     }
 
 }
 
 
