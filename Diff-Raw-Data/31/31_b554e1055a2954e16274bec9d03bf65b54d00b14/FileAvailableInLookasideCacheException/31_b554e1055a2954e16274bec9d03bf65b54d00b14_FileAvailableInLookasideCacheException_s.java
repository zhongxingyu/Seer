 package org.fedoraproject.eclipse.packager.api.errors;
 
 /**
  * Thrown if a file with a specific MD5 is already available in
  * the lookaside cache.
  */
 public class FileAvailableInLookasideCacheException extends
 		FedoraPackagerAPIException {
 
 	private static final long serialVersionUID = -1682359355021803771L;
 
 	/**
	 * @param message
	 * @param cause
	 */
	public FileAvailableInLookasideCacheException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
 	 */
	public FileAvailableInLookasideCacheException(String message) {
		super(message);
 	}
 }
