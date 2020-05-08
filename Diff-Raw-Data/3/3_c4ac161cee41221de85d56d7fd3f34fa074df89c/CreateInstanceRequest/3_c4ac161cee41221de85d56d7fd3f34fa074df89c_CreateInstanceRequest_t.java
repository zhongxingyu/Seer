 /*******************************************************************************
  * Copyright (c) 2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.deltacloud.core.client.request;
 
 import java.net.URL;
 
 import org.jboss.tools.deltacloud.core.client.HttpMethod;
 import org.jboss.tools.deltacloud.core.client.utils.UrlBuilder;
 
 /**
  * Creates a new instance
  * 
  * @author André Dietisheim
  */
 public class CreateInstanceRequest extends AbstractDeltaCloudRequest {
 
 	private String imageId;
 	private String profileId;
 	private String realmId;
 	private String name;
 	private String keyname;
 	private String memory;
 	private String storage;
 
 	public CreateInstanceRequest(URL baseUrl, String imageId) {
 		this(baseUrl, imageId, null, null, null, null, null, null);
 	}
 
 	public CreateInstanceRequest(URL baseUrl, String imageId, String profileId, String realmId, String name,
 			String keyId, String memory, String storage) {
 		super(baseUrl, HttpMethod.POST);
 		this.imageId = imageId;
 		this.profileId = profileId;
 		this.realmId = realmId;
 		this.name = name;
 		this.keyname = keyId;
 		this.memory = memory;
 		this.storage = storage;
 	}
 
 	@Override
 	protected String doCreateUrl(UrlBuilder urlBuilder) {
 		return urlBuilder.path("instances")
 				.parameter("keyname", keyname)
				// WORKAROUND for JBIDE-8005, STEAM-303
				.parameter("key_name", keyname)
				// WORKAROUND for JBIDE-8005, STEAM-303
 				.parameter("image_id", imageId)
 				.parameter("hwp_id", profileId)
 				.parameter("realm_id", realmId)
 				.parameter("name", name)
 				.parameter("hwp_memory", memory)
 				.parameter("hwp_storage", storage)
 				.parameter("commit", "create")
 				.toString();
 	}
 }
