 /**
  * 
  */
 package com.yacme.ext.oxsit.cust_it.comp.security;
 
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.XEventListener;
 import com.sun.star.lang.XInitialization;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.lang.XServiceInfo;
 import com.sun.star.lib.uno.helper.ComponentBase;
 import com.sun.star.uno.Exception;
 import com.sun.star.uno.XComponentContext;
 import com.yacme.ext.oxsit.logging.DynamicLogger;
 import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
 import com.yacme.ext.oxsit.logging.IDynamicLogger;
 import com.yacme.ext.oxsit.ooo.GlobConstant;
 import com.yacme.ext.oxsit.security.SignatureState;
 import com.yacme.ext.oxsit.security.XOX_SignatureState;
 import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;
 
 /**
  * @author beppe
  *
  */
 public class SignatureState_IT  extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
 implements XServiceInfo, XComponent, XInitialization, XOX_SignatureState {
 
 	protected IDynamicLogger m_aLogger;
 	protected XComponentContext m_xCC;
 	private XMultiComponentFactory m_xMCF;
 
 	//XServiceInfo
 	// the name of the class implementing this object
	public static final String m_sImplementationName = DocumentSigner_IT.class.getName();
 	// the Object name, used to instantiate it inside the OOo API
 	public static final String[] m_sServiceNames = { GlobConstant.m_sSIGNATURE_STATE_SERVICE_IT };
 
 //service attributes:
 	//XOX_SignatureState:
 	private int m_nRelativeNumberInDocument = -1;
 	private SignatureState	m_eState;
 	private XOX_X509Certificate m_aCert;
 
 	/**
 	 * 
 	 * 
 	 * @param _ctx the UNO context
 	 */
 	public SignatureState_IT(XComponentContext _ctx) {
 		m_xCC = _ctx;
 		m_xMCF = _ctx.getServiceManager();
 		m_aLogger = new DynamicLogger(this, _ctx);
 		m_aLogger = new DynamicLoggerDialog(this, _ctx);
 		m_aLogger.enableLogging();
 		m_aLogger.ctor();
 		m_eState = SignatureState.NOT_YET_VERIFIED;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
 	 */
 	@Override
 	public String getImplementationName() {
 		m_aLogger.entering("getImplementationName");
 		return m_sImplementationName;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
 	 */
 	@Override
 	public String[] getSupportedServiceNames() {
 		m_aLogger.info("getSupportedServiceNames");
 		return m_sServiceNames;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
 	 */
 	@Override
 	public boolean supportsService(String _sService) {
 		int len = m_sServiceNames.length;
 
 		m_aLogger.info("supportsService", _sService);
 		for (int i = 0; i < len; i++) {
 			if (_sService.equals(m_sServiceNames[i]))
 				return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
 	 */
 	@Override
 	public void initialize(Object[] arg0) throws Exception {
 		m_aLogger.entering("initialize");
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
 	 */
 	@Override
 	public void addEventListener(XEventListener arg0) {
 		super.addEventListener(arg0);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#dispose()
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
 	 */
 	@Override
 	public void removeEventListener(XEventListener arg0) {
 		super.removeEventListener(arg0);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.XOX_SignatureState#getSignatureRelativeNumberInDocument()
 	 */
 	@Override
 	public int getSignatureRelativeNumberInDocument() {
 		return m_nRelativeNumberInDocument;		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.XOX_SignatureState#setSignatureRelativeNumberInDocument(int)
 	 */
 	@Override
 	public void setSignatureRelativeNumberInDocument(int _relPos) {
 		m_nRelativeNumberInDocument = _relPos;		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.XOX_SignatureState#getState()
 	 */
 	@Override
 	public SignatureState getState() {
 		return m_eState;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.XOX_SignatureState#setState(com.yacme.ext.oxsit.security.SignatureState)
 	 */
 	@Override
 	public void setState(SignatureState _sigState) {
 		m_eState = _sigState;		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.XOX_SignatureState#getSignersCerficate()
 	 */
 	@Override
 	public XOX_X509Certificate getSignersCerficate() {
 		return m_aCert;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.XOX_SignatureState#setSignersCerficate(com.yacme.ext.oxsit.security.cert.XOX_X509Certificate)
 	 */
 	@Override
 	public void setSignersCerficate(XOX_X509Certificate _aCert) {
 		m_aCert = _aCert;		
 	}
 	
 }
