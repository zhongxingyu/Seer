 package dk.statsbiblioteket.mediaplatform.ingest.model.service;
 
 import dk.statsbiblioteket.mediaplatform.ingest.model.ChannelArchiveRequest;
 import dk.statsbiblioteket.mediaplatform.ingest.model.persistence.ChannelArchiveRequestDAO;
 import dk.statsbiblioteket.mediaplatform.ingest.model.persistence.ChannelArchiveRequestDAOIF;
 import dk.statsbiblioteket.mediaplatform.ingest.model.persistence.ChannelArchivingRequesterHibernateUtil;
 import dk.statsbiblioteket.mediaplatform.ingest.model.persistence.NotInitialiasedException;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  */
 public class ChannelArchiveRequestService implements ChannelArchiveRequestServiceIF {
 
     @Override
     public void insert(ChannelArchiveRequest request) throws ServiceException {
         if (request.getFromTime().after(request.getToTime())) {
             throw new ServiceException("fromTime (" + request.getFromTime() + ") must not be after toTime (" + request.getToTime() + ")");
         }
         try {
             getDao().create(request);
         } catch (NotInitialiasedException e) {
             throw new ServiceException(e);
         }
     }
 
     @Override
     public void update(ChannelArchiveRequest request) throws ServiceException {
         try {
             getDao().update(request);
         } catch (NotInitialiasedException e) {
             throw new ServiceException(e);
         }
     }
 
     @Override
     public void delete(ChannelArchiveRequest request) throws ServiceException {
         try {
             getDao().delete(request);
         } catch (NotInitialiasedException e) {
             throw new ServiceException(e);
         }
 
     }
 
     @Override
     public List<ChannelArchiveRequest> getValidRequests(Date fromDate, Date toDate) throws ServiceException {
         try {
             return getDao().getValidRequests(fromDate, toDate);
         } catch (NotInitialiasedException e) {
             throw new ServiceException(e);
         }
     }
 
    @Override
    public List<ChannelArchiveRequest> getAllRequests() throws ServiceException {
        try {
            return getDao().getAllRequests();
        } catch (NotInitialiasedException e) {
            throw new ServiceException(e);
        }

    }

    private ChannelArchiveRequestDAOIF getDao() throws NotInitialiasedException {
          return new ChannelArchiveRequestDAO(ChannelArchivingRequesterHibernateUtil.getInitialisedFactory());
    }
 
 }
