 package org.fedoraproject.eclipse.packager.api.errors;
 
import java.text.MessageFormat;

import org.fedoraproject.eclipse.packager.FedoraPackagerText;

 /**
  * Thrown if a file with a specific MD5 is already available in
  * the lookaside cache.
  */
 public class FileAvailableInLookasideCacheException extends
 		FedoraPackagerAPIException {
 
 	private static final long serialVersionUID = -1682359355021803771L;
 
 	/**
	 * @param fileName The available filename.
 	 */
	@SuppressWarnings("static-access")
	public FileAvailableInLookasideCacheException(String fileName) {
		super(
				MessageFormat
						.format(FedoraPackagerText.get().fileAvailableInLookasideCacheException_message,
								fileName));
 	}
 }
