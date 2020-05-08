 /*
     Copyright (c) 2012,2013 Mirco Attocchi
 	
     This file is part of WebAppCommon.
 
     WebAppCommon is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     WebAppCommon is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with WebAppCommon.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package it.attocchi.jsf2;
 
 import it.attocchi.jpa2.IJpaListernes;
 
 import javax.persistence.EntityManagerFactory;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpSession;
 
 /**
  * Pagina Gestione Utenti Autenticati (Richiede filtro @AuthFilter)
  * 
  * @author Mirco
  * 
  */
 // public abstract class PageBaseNoAuth extends PageBaseJpa2Data {
 public abstract class PageBaseNoAuth extends PageBaseNoAuthNoJpa2 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
	/*
 	 * 
	 * MERGE DA PageBaseJpa2Data
 	 */
 
 	/**
 	 * Use With @JPASessionListener
 	 * 
 	 * @return
 	 */
 	private EntityManagerFactory getEmfSession() {
 		EntityManagerFactory emf = null;
 
 		HttpSession session = getSession();
 		if (session != null) {
 			emf = (EntityManagerFactory) session.getAttribute(IJpaListernes.SESSION_EMF);
 		} else {
 			logger.error("Session is null ");
 		}
 
 		return emf;
 	}
 
 	/**
 	 * Use With @JPAListener
 	 * 
 	 * @return
 	 */
 	private EntityManagerFactory getEmfContext() {
 		EntityManagerFactory emf = null;
 
 		ServletContext context = getServletContext();
 		if (context != null) {
 			emf = (EntityManagerFactory) context.getAttribute(IJpaListernes.SESSION_EMF);
 		} else {
 			logger.error("Context is null ");
 		}
 		return emf;
 	}
 
 	// protected abstract void init() throws Exception;
 
 	protected EntityManagerFactory getEmfShared() {
 		EntityManagerFactory emf = null;
 
 		EntityManagerFactory emfSession = getEmfSession();
 		EntityManagerFactory emfContext = getEmfContext();
 
 		if (emfSession != null) {
 			emf = emfSession;
 		} else if (emfContext != null) {
 			emf = emfContext;
 		} else {
 			logger.error("getEmfShared: EntityManagerFactory is not in Session or in Context ");
 		}
 
 		return emf;
 	}
 }
