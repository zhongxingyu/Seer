 package com.rfp.service;
 
import java.net.InetAddress;
import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import com.rfp.entity.RFP;
 import com.rfp.entity.RFPStatus;
 import com.rfp.to.RFPStatusTO;
 import com.rfp.to.RFPTO;
 
 public class RFPService {
 
 	private String url;
 	
 	public RFPService()
 	{
		try 
		{
			url = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/docsRFP/";
		} 
		catch (UnknownHostException e)  {} 
 	}
 	
 	public void registerRFP(RFPTO requestTO)
 	{
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("RFPs");
 		EntityManager em = null;
 		try
 		{
 			em = emf.createEntityManager();
 
 			RFP request = new RFP();
 			request.setName(requestTO.getName());
 			request.setStartDate(requestTO.getStartDate());
 			request.setDecitionDate(requestTO.getDecitionDate());
 			request.setCompany(requestTO.getCompany());
 			request.setFilename(requestTO.getFilename());
 			
 			em.getTransaction().begin();
 			em.persist(request);
 			em.getTransaction().commit();
 			requestTO.setRequestId(request.getRfpId());
 		}
 		catch (Exception e) 
 		{
 			requestTO.setRequestId(-1);
 		}
 		finally
 		{
 			if (em != null)
 			{
 				em.close();
 			}
 		}
 	}
 	
 	public List<RFPTO> getAllRFPs()
 	{
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("RFPs");
 		EntityManager em = null;
 		try
 		{
 			em = emf.createEntityManager();
 			Query query = em.createQuery("select r from RFP r");
 			List<RFP> rs = query.getResultList();
 			List<RFPTO> resutl = new ArrayList<RFPTO>();
 			for (RFP rfp : rs)
 			{
 				RFPTO to = new RFPTO();
 				to.setRequestId(rfp.getRfpId());
 				to.setName(rfp.getName());
 				to.setCompany(rfp.getCompany());
 				to.setStartDate(rfp.getStartDate());
 				to.setDecitionDate(rfp.getDecitionDate());
 				to.setFilename(url + rfp.getRfpId() + "/" + rfp.getFilename());
 				to.setComment(rfp.getComment());
 				to.setAverage(rfp.getAverage());
 				if(rfp.getStatus() != null)
 				{
 					RFPStatusTO status = new RFPStatusTO();
 					status.setStatusId(rfp.getStatus().getStatusId());
 					status.setName(rfp.getStatus().getName());
 					to.setStatus(status);
 				}
 				resutl.add(to);
 			}
 			return resutl;
 		}
 		finally
 		{
 			if (em != null)
 			{
 				em.close();
 			}
 		}
 	}
 	
 	public List<RFPTO> searchRFPs(String name, String company, int statusId)
 	{
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("RFPs");
 		EntityManager em = null;
 		try
 		{
 			em = emf.createEntityManager();
 			String queryStr = "select r from RFP r where r.name like :name and r.company like :company";
 			if (statusId != -1)
 			{
 				queryStr += " and r.status.statusId = :statusId";
 			}
 			Query query = em.createQuery(queryStr);
 			query.setParameter("name", name + "%");
 			query.setParameter("company", company + "%");
 			if (statusId != -1)
 			{
 				query.setParameter("statusId", statusId);
 			}
 			List<RFP> rs = query.getResultList();
 			List<RFPTO> resutl = new ArrayList<RFPTO>();
 			for (RFP rfp : rs)
 			{
 				RFPTO to = new RFPTO();
 				to.setRequestId(rfp.getRfpId());
 				to.setName(rfp.getName());
 				to.setCompany(rfp.getCompany());
 				to.setStartDate(rfp.getStartDate());
 				to.setDecitionDate(rfp.getDecitionDate());
 				to.setFilename(url + rfp.getRfpId() + "/" + rfp.getFilename());
 				to.setComment(rfp.getComment());
 				to.setAverage(rfp.getAverage());
 				if(rfp.getStatus() != null)
 				{
 					RFPStatusTO status = new RFPStatusTO();
 					status.setStatusId(rfp.getStatus().getStatusId());
 					status.setName(rfp.getStatus().getName());
 					to.setStatus(status);
 				}
 				resutl.add(to);
 			}
 			return resutl;
 		}
 		finally
 		{
 			if (em != null)
 			{
 				em.close();
 			}
 		} 
 	}
 	
 	public RFPTO getRFP(long rfpId)
 	{
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("RFPs");
 		EntityManager em = null;
 		try
 		{			
 			em = emf.createEntityManager();
 			RFP rfp = em.find(RFP.class, rfpId);
 			RFPTO to = null;
 			if (rfp != null)
 			{
 				to = new RFPTO();
 				to.setRequestId(rfp.getRfpId());
 				to.setName(rfp.getName());
 				to.setCompany(rfp.getCompany());
 				to.setStartDate(rfp.getStartDate());
 				to.setDecitionDate(rfp.getDecitionDate());
 				to.setFilename(url + rfp.getRfpId() + "/" + rfp.getFilename());
 				to.setComment(rfp.getComment());
 				to.setAverage(rfp.getAverage());
 				if(rfp.getStatus() != null)
 				{
 					RFPStatusTO status = new RFPStatusTO();
 					status.setStatusId(rfp.getStatus().getStatusId());
 					status.setName(rfp.getStatus().getName());
 					to.setStatus(status);
 				}
 			}
 			return to;
 		}
 		finally
 		{
 			if (em != null)
 			{
 				em.close();
 			}
 		}
 	}
 	
 	public boolean editRFP(RFPTO to)
 	{
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("RFPs");
 		EntityManager em = null;
 		try
 		{
 			em = emf.createEntityManager();
 			RFP rfp = em.find(RFP.class, to.getRequestId());
 			if (rfp != null)
 			{
 				em.getTransaction().begin();
 				rfp.setName(to.getName());
 				rfp.setCompany(to.getCompany());
 				rfp.setStartDate(to.getStartDate());
 				rfp.setDecitionDate(to.getDecitionDate());
 				rfp.setComment(to.getComment());
 				rfp.setAverage(to.getAverage());
 				if(to.getStatus() != null)
 				{
 					RFPStatus status = em.find(RFPStatus.class, to.getStatus().getStatusId());
 					rfp.setStatus(status);
 				}
 				else
 				{
 					rfp.setStatus(null);
 				}
 				em.getTransaction().commit();
 			}
 			return true;
 		}
 		catch (Exception e) {
 			return false;
 		}
 		finally
 		{
 			if (em != null)
 			{
 				em.close();
 			}
 		}
 	}
 	
 	public boolean rateRFP(long rfpId, double rate)
 	{
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("RFPs");
 		EntityManager em = null;
 		try
 		{
 			em = emf.createEntityManager();
 			RFP rfp = em.find(RFP.class, rfpId);
 			if (rfp != null)
 			{
 				em.getTransaction().begin();
 				rfp.setAverage(rate);
 				em.getTransaction().commit();
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		catch (Exception e) 
 		{
 			return false;
 		}
 		finally
 		{
 			if (em != null)
 			{
 				em.close();
 			}
 		}
 	}
 }
