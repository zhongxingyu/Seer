 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.odkconnector.serialization.serializer;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.annotation.Handler;
 import org.openmrs.module.odkconnector.serialization.Serializer;
 import org.openmrs.util.HandlerUtil;
 
 @Handler(supports = List.class, order = 50)
 public class ListSerializer implements Serializer {
 
 	private static final Log log = LogFactory.getLog(ListSerializer.class);
 
 	/**
 	 * Write the data to the output stream.
 	 *
 	 * @param stream the output stream
 	 * @param data   the data that need to be written to the output stream
 	 * @throws java.io.IOException thrown when the writing process encounter is failing
 	 */
 	@Override
 	public void write(final OutputStream stream, final Object data) throws IOException {
 
 		DataOutputStream outputStream = new DataOutputStream(stream);
 
 		List list = null;
 		if (ClassUtils.isAssignable(data.getClass(), List.class))
 			list = (List) data;
 
 		if (list == null || CollectionUtils.isEmpty(list))
 			outputStream.writeInt(Serializer.ZERO);
 		else {
 			outputStream.writeInt(list.size());
 			for (Object object : list) {
				try {
					Serializer serializer = HandlerUtil.getPreferredHandler(Serializer.class, object.getClass());
					serializer.write(outputStream, object);
				} catch (Exception e) {
					log.error("Writing object to stream failed!", e);
				}
 			}
 		}
 	}
 }
