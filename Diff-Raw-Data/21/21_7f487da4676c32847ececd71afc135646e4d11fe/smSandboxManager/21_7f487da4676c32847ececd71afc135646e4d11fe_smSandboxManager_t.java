 package swarm.client.view.sandbox;
 
 import swarm.client.view.smViewContext;
 import swarm.client.view.tabs.code.smI_CodeLoadListener;
 import swarm.shared.debugging.smU_Debug;
 import swarm.shared.entities.smE_CodeSafetyLevel;
 import swarm.shared.structs.smCode;
 
 import com.google.gwt.user.client.Element;
 
 public class smSandboxManager
 {
 	public interface I_StartUpCallback
 	{
 		void onStartUpComplete(boolean success);
 	}
 	
 	private final smCajaSandboxManager m_cajaSandboxMngr;
 	private final smInlineFrameSandboxManager m_iframeSandboxMngr;
 	
 	private final smCellApi m_cellApi;
 	
 	public smSandboxManager(smViewContext viewContext, I_StartUpCallback callback, String apiNamespace, boolean useVirtualSandbox)
 	{
 		m_cellApi = new smCellApi(viewContext);
 		m_cellApi.registerApi(apiNamespace);
 		
 		m_iframeSandboxMngr = new smInlineFrameSandboxManager(apiNamespace);
 		
 		if( useVirtualSandbox )
 		{
 			m_cajaSandboxMngr = new smCajaSandboxManager(m_cellApi, callback, apiNamespace);
 		}
 		else
 		{
 			m_cajaSandboxMngr = null;
 			
 			callback.onStartUpComplete(true);
 		}
 	}
 	
 	public void allowScrolling(Element element, boolean yesOrNo)
 	{
 		if( m_cajaSandboxMngr != null )
 		{
 			m_cajaSandboxMngr.allowScrolling(element, yesOrNo);
 		}
 	}
 	
 	public void start(Element host, smCode code, String cellNamespace, smI_CodeLoadListener listener)
 	{		
 		smE_CodeSafetyLevel codeLevel = code.getSafetyLevel();
		codeLevel = codeLevel == smE_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX ? smE_CodeSafetyLevel.LOCAL_SANDBOX : codeLevel;
 		
 		switch(codeLevel)
 		{
 			case NO_SANDBOX:
 			{
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.stop(host);
 				}
 				
 				m_iframeSandboxMngr.stop(host);
 				
 				host.setInnerHTML(code.getRawCode());
 				listener.onCodeLoad();
 				
 				break;
 			}
 			case LOCAL_SANDBOX:
 			{
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.stop(host);
 				}
 				
 				m_iframeSandboxMngr.start_local(host, code.getRawCode(), listener);  break;
 			}
 			case REMOTE_SANDBOX:
 			{
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.stop(host);
 				}
 				
 				m_iframeSandboxMngr.start_remote(host, code.getRawCode(), listener);  break;
 			}
 			case VIRTUAL_DYNAMIC_SANDBOX:
 			{
 				m_iframeSandboxMngr.stop(host);
 				
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.start_dynamic(host, code.getRawCode(), cellNamespace, listener);
 				}
 				
 				break;
 			}
 			case VIRTUAL_STATIC_SANDBOX:
 			{
 				m_iframeSandboxMngr.stop(host);
 				
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.start_static(host, code.getRawCode(), cellNamespace, listener);
 				}
 					
 				break;
 			}
 		}
 	}
 	
 	/*public boolean isRunning()
 	{
 		return m_cajaSandboxMngr.isRunning() || m_iframeSandboxMngr.isRunning();
 	}*/
 	
 	public void stop(Element host)
 	{
 		if( m_cajaSandboxMngr != null )
 		{
 			m_cajaSandboxMngr.stop(host);
 		}
 		
 		m_iframeSandboxMngr.stop(host);
 	}
 }
