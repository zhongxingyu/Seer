 /*************************************************************************
  * 
  *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
  *  
  *  The Contents of this file are made available subject to
  *  the terms of European Union Public License (EUPL) version 1.1
  *  as published by the European Community.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the EUPL.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  EUPL for more details.
  *
  *  You should have received a copy of the EUPL along with this
  *  program.  If not, see:
  *  https://www.osor.eu/eupl, http://ec.europa.eu/idabc/eupl.
  *
  ************************************************************************/
 
 package it.plio.ext.oxsit.comp.security;
 
 import it.plio.ext.oxsit.Helpers;
 import it.plio.ext.oxsit.Utilities;
 import it.plio.ext.oxsit.logging.DynamicLogger;
 import it.plio.ext.oxsit.ooo.GlobConstant;
 import it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures;
 import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import com.sun.star.embed.XStorage;
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.XEventListener;
 import com.sun.star.lang.XInitialization;
 import com.sun.star.lang.XServiceInfo;
 import com.sun.star.lib.uno.helper.ComponentBase;
 import com.sun.star.uno.Exception;
 import com.sun.star.uno.XComponentContext;
 import com.sun.star.util.XChangesListener;
 import com.sun.star.util.XChangesNotifier;
 
 /**
  * THis is a specification, it may change!
  * This service implements the QualifiedCertificate service.
  * receives the doc information from the task  
  *  
  * This objects has properties, they are set by the calling UNO objects.
  * 
  * The service is initialized with URL and XStorage of the document under test
  * Information about the certificates, number of certificates, status of every signature
  * ca be retrieved through properties 
  * 
  * @author beppec56
  *
  */
 public class DocumentSignatures extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
 			implements 
 			XServiceInfo,
 			XChangesNotifier,
 			XComponent,
 			XInitialization,
 			XOX_DocumentSignatures
 			 {
 
 	// the name of the class implementing this object
 	public static final String			m_sImplementationName	= DocumentSignatures.class.getName();
 	// the Object name, used to instantiate it inside the OOo API
 	public static final String[]		m_sServiceNames			= { GlobConstant.m_sDOCUMENT_SIGNATURES_SERVICE };
 
 	protected DynamicLogger m_logger;
 
 	// these are the listeners on this document signatures changes
 	public HashMap<XChangesListener,XChangesListener> m_aListeners = new HashMap<XChangesListener, XChangesListener>(10);
 
 	protected XStorage		m_xDocumentStorage;
 	// this document signature state
 	protected int			m_nDocumentSignatureState;
 	protected String		m_sDocumentId;
 
 	protected Boolean		m_aMtx_setDocumentSignatureState = new Boolean(false);
 	/**
 	 * 
 	 * 
 	 * @param _ctx the UNO context
 	 */
 	public DocumentSignatures(XComponentContext _ctx) {
 		m_logger = new DynamicLogger(this, _ctx);
     	m_logger.enableLogging();
     	m_logger.ctor();
 	}
 
 	@Override
 	public String getImplementationName() {
 		// TODO Auto-generated method stub
 		m_logger.entering("getImplementationName");
 		return m_sImplementationName;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
 	 */
 	@Override
 	public String[] getSupportedServiceNames() {
 		// TODO Auto-generated method stub
 		m_logger.info("getSupportedServiceNames");
 		return m_sServiceNames;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
 	 */
 	@Override
 	public boolean supportsService(String _sService) {
 		int len = m_sServiceNames.length;
 
 		m_logger.info("supportsService",_sService);
 		for (int i = 0; i < len; i++) {
 			if (_sService.equals( m_sServiceNames[i] ))
 				return true;
 		}
 		return false;
 	}
 
 	// XChangesNotifier
 	/* (non-Javadoc)
 	 * @see com.sun.star.util.XChangesNotifier#addChangesListener(com.sun.star.util.XChangesListener)
 	 */
 	@Override
 	public void addChangesListener(XChangesListener _ChangesListener) {
 		// TODO Auto-generated method stub
 		
 		if(!m_aListeners.containsKey(_ChangesListener) &&
 				_ChangesListener != null ) {
 			m_aListeners.put(_ChangesListener, _ChangesListener);
 			m_logger.entering("addChangesListener "+Helpers.getHashHex(_ChangesListener));
 		}		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.util.XChangesNotifier#removeChangesListener(com.sun.star.util.XChangesListener)
 	 */
 	@Override
 	public void removeChangesListener(XChangesListener _ChangesListener) {
 		// TODO Auto-generated method stub
 		if(m_aListeners.containsKey(_ChangesListener) ) {
 			m_aListeners.remove(_ChangesListener);
 			m_logger.entering("removeChangesListener "+Helpers.getHashHex(_ChangesListener));		
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
 	 * when instantiated, 
 	 * 	_oObj[0] first argument document URL
 	 *  _oObj[1] corresponding XStorage object
 	 */
 	@Override
 	public void initialize(Object[] _oObj) throws Exception {
 		// TODO Auto-generated method stub
 		m_logger.entering("initialize");		
 	}	
 
 	/* (non-Javadoc)
 	 * @see it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures#getDocumentCertificates()
 	 */
 	@Override
 	public XOX_QualifiedCertificate[] getDocumentCertificates() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures#getDocumentStorage()
 	 * 
 	 * IMPORTANT the manipulation of storage variable is Sync Job only responsability!
 	 */
 	@Override
 	public XStorage getDocumentStorage() {
 		// TODO Auto-generated method stub
 		m_logger.info("getDocumentStorage");		
 		return m_xDocumentStorage;
 	}
 
 	/* (non-Javadoc)
 	 * @see it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures#setDocumentStorage(com.sun.star.embed.XStorage)
 	 * IMPORTANT the manipulation of storage variable is Sync Job only responsability!
 	 */
 	@Override
 	public void setDocumentStorage(XStorage _xStore) {
 		// TODO Auto-generated method stub
 		m_logger.info("setDocumentStorage");		
 		m_xDocumentStorage = _xStore;		
 	}
 
 	/* (non-Javadoc)
 	 * @see it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures#getDocumentSignatureState()
 	 */
 	@Override
 	public int getDocumentSignatureState() {
 		// TODO Auto-generated method stub
 		m_logger.log("getDocumentSignatureState");
 		synchronized (this) {
 			return m_nDocumentSignatureState;			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures#setDocumentSignatureState(int)
 	 * when this method is called, the signature state is notified to all the m_aListeners
 	 * 
 	 */
 	@Override
 	public void setDocumentSignatureState(int _nState) {
 		// TODO Auto-generated method stub
 		m_logger.entering("setDocumentSignatureState","_nState is: "+_nState);
 		synchronized (this) {
 			m_nDocumentSignatureState = _nState;
 		}
 		//call all the listeners, start a new thread for this
 		(new Thread(new Runnable() {
 			public void run() {
 				synchronized (m_aMtx_setDocumentSignatureState) {
 					m_logger.log("inter thread started");
 					Collection<XChangesListener> aColl = m_aListeners.values();
 					if(!aColl.isEmpty()) {
 						Iterator<XChangesListener> aIter = aColl.iterator();
 						// scan the array and for every one send the status
 						while (aIter.hasNext()) {
 							XChangesListener aThisOne =aIter.next();
 							aThisOne.changesOccurred(null);
 						}
 					}
 					m_logger.log("inter thread exits, there were", ((aColl.isEmpty()) ? " none " : " some ")+"listener");				
 				}
 			}
 		}
 		)).start();
 		m_logger.exiting("setDocumentSignatureState","");
 	}
 
 	/* (non-Javadoc)
 	 * @see it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures#getDocumentId()
 	 */
 	@Override
 	public String getDocumentId() {
 		// TODO Auto-generated method stub
 		m_logger.log("getDocumentId");
 		return m_sDocumentId;
 	}
 
 	/* (non-Javadoc)
 	 * @see it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures#setDocumentId(java.lang.String)
 	 */
 	@Override
 	public void setDocumentId(String arg0) {
 		// TODO Auto-generated method stub
 		m_sDocumentId = arg0;
 		m_logger.log("setDocumentId");
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
 	 */
 	@Override
 	public void addEventListener(XEventListener arg0) {
 		// TODO Auto-generated method stub
 		m_logger.log("addEventListener");
 		super.addEventListener(arg0);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#dispose()
 	 */
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		m_logger.log("dispose");
 		super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
 	 */
 	@Override
 	public void removeEventListener(XEventListener arg0) {
 		// TODO Auto-generated method stub
 		m_logger.log("removeEventListener");		
 		super.removeEventListener(arg0);
 	}
 }
