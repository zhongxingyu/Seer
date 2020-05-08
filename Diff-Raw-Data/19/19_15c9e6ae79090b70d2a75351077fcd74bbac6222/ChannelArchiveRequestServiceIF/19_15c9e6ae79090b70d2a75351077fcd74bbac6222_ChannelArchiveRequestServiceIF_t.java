 package dk.statsbiblioteket.mediaplatform.ingest.model.service;
 
 import dk.statsbiblioteket.mediaplatform.ingest.model.ChannelArchiveRequest;
 
 import java.nio.channels.Channel;
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  */
 public interface ChannelArchiveRequestServiceIF {
 
     /**
      * Inserts a new ChannelArchiveRequest in the object store
      * @param request the object to be inserted
      * @throws ServiceException if the request is not permitted because it violates a constraint.
      */
     void insert(ChannelArchiveRequest request) throws ServiceException;
 
     /**
      * Update the given request.
      * @param request the request
      * @throws ServiceException if the update is not permitted.
      */
     void update(ChannelArchiveRequest request) throws ServiceException;
 
     /**
      * Delete the given request
      * @param request the request
      * @throws ServiceException if the update is not permitted.
      */
     void delete(ChannelArchiveRequest request) throws ServiceException;
 
      /**
      * Returns a list of all ChannelArchiveRequest objects which are valid in at least part of the specified date range.
      * @param fromDate Start of date range.
      * @param toDate End of date range.
      * @return the list of vaild requests.
      */
     List<ChannelArchiveRequest> getValidRequests(Date fromDate, Date toDate) throws ServiceException;

    List<ChannelArchiveRequest> getAllRequests() throws ServiceException;
 }
