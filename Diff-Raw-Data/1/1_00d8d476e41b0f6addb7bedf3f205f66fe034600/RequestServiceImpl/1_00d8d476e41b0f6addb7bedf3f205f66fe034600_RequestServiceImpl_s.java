 package org.accesointeligente.server.services;
 
 import org.accesointeligente.client.services.RequestService;
 import org.accesointeligente.model.*;
 import org.accesointeligente.server.HibernateUtil;
 import org.accesointeligente.server.SearchParamParseUtil;
 import org.accesointeligente.server.SessionUtil;
 import org.accesointeligente.shared.RequestSearchParams;
 import org.accesointeligente.shared.RequestStatus;
 import org.accesointeligente.shared.ServiceException;
 
 import net.sf.gilead.core.PersistentBeanManager;
 import net.sf.gilead.gwt.PersistentRemoteService;
 
 import org.hibernate.*;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.List;
 
 public class RequestServiceImpl extends PersistentRemoteService implements RequestService {
 	private static final long serialVersionUID = -8965250779021980788L;
 	private PersistentBeanManager persistentBeanManager;
 
 	public RequestServiceImpl() {
 		persistentBeanManager = HibernateUtil.getPersistentBeanManager();
 		setBeanManager(persistentBeanManager);
 	}
 
 	@Override
 	public Request saveRequest(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.saveOrUpdate(request);
 			hibernate.getTransaction().commit();
 			return request;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteRequest(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.delete(request);
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<RequestCategory> getCategories() throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(RequestCategory.class);
 			criteria.addOrder(Order.asc("id"));
 			List<RequestCategory> categories = (List<RequestCategory>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return categories;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request getRequest(Integer requestId) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("categories", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("id", requestId));
 			Request request = (Request) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request getRequest (String remoteIdentifier) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.eq("remoteIdentifier", remoteIdentifier));
 			Request request = (Request) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser();
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.addOrder(Order.asc("date"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			params = (RequestSearchParams) persistentBeanManager.merge(params);
 			User user = SessionUtil.getUser();
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.addOrder(Order.asc("date"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			if(params != null) {
 				SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 			}
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserFavoriteRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser();
 			String query = "select f.request from UserFavoriteRequest f join fetch f.request.institution join fetch f.request.favorites where f.user = :user";
 			Query hQuery = hibernate.createQuery(query);
 			hQuery.setParameter("user", user);
 			List<Request> requests = hQuery.list();
 			hibernate.getTransaction().commit();
 			return (List<Request>) persistentBeanManager.clone(requests);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getUserFavoriteRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser();
 			String query = "select f.request from UserFavoriteRequest f join fetch f.request.institution join fetch f.request.favorites where f.user = :user";
 			Query hQuery;
 			if(params != null) {
 				query += SearchParamParseUtil.queryAddSearchParams(params);
 				System.err.println(query);
 				hQuery = hibernate.createQuery(query);
 				hQuery.setParameter("user", user);
 				if (query.contains("minDate")) {
 					hQuery.setParameter("minDate", params.getMinDate());
 				}
 				if (query.contains("maxDate")) {
 					hQuery.setParameter("maxDate", params.getMaxDate());
 				}
 			} else {
 				hQuery = hibernate.createQuery(query);
 				hQuery.setParameter("user", user);
 			}
 			List<Request> requests = hQuery.list();
 			hibernate.getTransaction().commit();
 			return (List<Request>) persistentBeanManager.clone(requests);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
 			criteria.addOrder(Order.asc("date"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request> getRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFirstResult(offset);
 			criteria.setMaxResults(limit);
			criteria.add(Restrictions.ne("status", RequestStatus.NEW));
 			criteria.addOrder(Order.asc("date"));
 			criteria.addOrder(Order.asc("institution"));
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			if(params != null) {
 				SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 			}
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Attachment> getResponseAttachmentList(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Attachment.class);
 			criteria.add(Restrictions.eq("response", response));
 			criteria.addOrder(Order.asc("name"));
 			List<Attachment> attachments = (List<Attachment>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return attachments;
 		} catch (Exception ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserFavoriteRequest getFavoriteRequest(Request request, User user) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(UserFavoriteRequest.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("request", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.eq("request", request));
 			UserFavoriteRequest favorite = (UserFavoriteRequest) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			if (favorite != null) {
 				return (UserFavoriteRequest) persistentBeanManager.clone(favorite);
 			} else {
 				return null;
 			}
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserFavoriteRequest createFavoriteRequest(Request request, User user) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			UserFavoriteRequest favorite = new UserFavoriteRequest();
 			favorite.setRequest(request);
 			favorite.setUser(user);
 			hibernate.save(favorite);
 			hibernate.getTransaction().commit();
 			return favorite;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteFavoriteRequest(UserFavoriteRequest favorite) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			favorite = (UserFavoriteRequest) persistentBeanManager.merge(favorite);
 			hibernate.delete(favorite);
 			hibernate.getTransaction().commit();
 			return;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<RequestComment> getRequestComments(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(RequestComment.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("request", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("request", request));
 			criteria.addOrder(Order.desc("date"));
 			List<RequestComment> comments = (List<RequestComment>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return comments;
 		} catch (Exception ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public RequestComment createRequestComment(RequestComment comment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			comment = (RequestComment) persistentBeanManager.merge(comment);
 			hibernate.save(comment);
 			hibernate.getTransaction().commit();
 			return comment;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteRequestComment(RequestComment comment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			comment = (RequestComment) persistentBeanManager.merge(comment);
 			hibernate.delete(comment);
 			hibernate.getTransaction().commit();
 			return;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Response saveResponse(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			response = (Response) persistentBeanManager.merge(response);
 			hibernate.save(response);
 			hibernate.getTransaction().commit();
 			return response;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Attachment saveAttachment(Attachment attachment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			attachment = (Attachment) persistentBeanManager.merge(attachment);
 			hibernate.save(attachment);
 			hibernate.getTransaction().commit();
 			return attachment;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserRequestQualification saveUserRequestQualification(UserRequestQualification qualification) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			qualification = (UserRequestQualification) persistentBeanManager.merge(qualification);
 			hibernate.saveOrUpdate(qualification);
 			hibernate.getTransaction().commit();
 			qualification.setRequest(updateRequestQualification(qualification.getRequest()));
 			return (UserRequestQualification) persistentBeanManager.clone(qualification);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request updateRequestQualification(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			request = (Request) persistentBeanManager.merge(request);
 			Double averageQualification = (Double) hibernate.createQuery("select avg(qualification) from UserRequestQualification where Request = :request").setParameter("request", request).uniqueResult();
 			request.setQualification(averageQualification);
 			hibernate.update(request);
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 }
