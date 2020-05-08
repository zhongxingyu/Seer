 package svolkov.downloader.api;
 
 /**
  * Download manager facade, allows to add request, inspect request execution status, cancel request execution and get result.
  * @author vsa
  *
  */
 public interface DownloadManager {
 
 	/**
 	 * Adds request to download queue.
 	 * @param request request to download.
 	 * @return identifier of request in manager or <tt>null<tt> if manager is off.
 	 * @throws RequestNotSupported if request type is not supported.
 	 */
 	Long addDownloadRequest(DownloadRequest request) throws RequestNotSupported;
 	
 	/**
 	 * Returns status of request execution.
 	 * @param id identifier of request in manager.
 	 * @return status
 	 */
 	DownloadStatus getStatus(Long id);
 
 	/**
	 * Cancels request with specified id.
 	 * @param id identifier of request in manager.
	 * @return <tt>true</tt> if request was canceled, or
	 * <tt>false</tt> if request was not found or already in {@link DownloadStatus#DONE} or {@link DownloadStatus#CANCELLED} state.
 	 */
 	boolean cancelRequest(Long id);
 
 	/**
 	 * Returns response for request with specified id.
	 * @param id identifier of request in manager.
	 * @return protocol response, or <tt>null</tt> if corresponding request is not in {@link DownloadStatus#DONE} or was removed.
 	 */
 	DownloadResponse getResponse(Long id);
 
 	/**
 	 * Waits for response for request with specified id.
 	 * @param id
	 * @return response, or <tt>null</tt> if corresponding request is in {@link DownloadStatus#CANCELLED} or was removed.
 	 */
 	DownloadResponse waitResponse(Long id);
 
 	/**
	 * Removes request with specified id, cancels execution if necessary.
 	 * @param id
	 * @return response, or <tt>null</tt> if corresponding request is in {@link DownloadStatus#CANCELLED} or already removed.
 	 */
 	DownloadResponse removeResponse(Long id);
 
 	/**
 	 * Stop accepting requests. 
 	 */
 	void shutdown();
 
 }
