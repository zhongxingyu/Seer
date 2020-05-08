 package swarm.client.view.sandbox;
 
 import swarm.client.view.tabs.code.smI_CodeLoadListener;
 import swarm.shared.debugging.smU_Debug;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.IFrameElement;
 
 public class smInlineFrameSandbox
 {
 	private final IFrameElement m_iframe;
 	private final String m_apiNamespace;
 	
 	public smInlineFrameSandbox(String apiNamespace)
 	{
 		m_iframe = Document.get().createIFrameElement();
 		
 		m_iframe.addClassName("sm_iframe_sandbox");
 		
 		m_apiNamespace = apiNamespace;
 	}
 	
 	private boolean registerLocalApi()
 	{
 		return registerLocalApi_native(m_iframe, m_apiNamespace);
 	}
 	
 	private native boolean registerLocalApi_native(IFrameElement iframe, String apiNamespace)
 	/*-{
 			function isSameDomain(iframe)
 			{
 			    var html = null;
 			    try { 
 			      // deal with older browsers
 			      var doc = iframe.contentDocument || iframe.contentWindow.document;
 			      html = doc.body.innerHTML;
 			    } catch(err){
 			      // do nothing
 			    }
 			
 			    return(html !== null);
 			}
 		
 			if( isSameDomain(iframe) )
 			{
 				iframe.contentWindow.bh = $wnd.bh;
 				iframe.contentWindow.alert = $wnd[apiNamespace+'_alert'];
 				
 				return true;
 			}
 			else
 			{
 				return false;
 			}			
 	}-*/;
 	
 	
 	private void start_helper(Element host)
 	{
 		if( m_iframe.getParentNode() != null )
 		{
 			smU_Debug.ASSERT(false, "Expected null parent for iframe.");
 			
 			m_iframe.removeFromParent();
 		}
 		
		m_iframe.removeAttribute("src");
		
 		host.appendChild(m_iframe);
 	}
 	
 	void start_local(Element host, String rawCode, smI_CodeLoadListener listener)
 	{
 		start_helper(host);
 		
 		this.start_local_private(m_iframe, rawCode);
 		
 		listener.onCodeLoad();
 	}
 	
 	private native void start_local_private(IFrameElement iframe, String rawCode)
 	/*-{
 			var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
 			
 			this.@swarm.client.view.sandbox.smInlineFrameSandbox::registerLocalApi()();
 			
 			iframeDoc.write(rawCode);
 	}-*/;
 	
 	void start_remote(Element host, String src, smI_CodeLoadListener listener)
 	{
 		start_helper(host);
 		
 		start_remote_private(m_iframe, src);
 		
 		listener.onCodeLoad();
 	}
 		
 	private native void start_remote_private(IFrameElement iframe, String src)
 	/*-{
 			var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
 
 			this.@swarm.client.view.sandbox.smInlineFrameSandbox::registerLocalApi()();
 			
 			iframeDoc.src = src;
 	}-*/;
 	
 	void stop(Element host)
 	{
 		if( m_iframe.getParentNode() != null )
 		{
 			if( m_iframe.getParentNode() != host )
 			{
 				smU_Debug.ASSERT(false, "Parent wasn't current host.");
 			}
 			
			m_iframe.setSrc("data:text/html, ");
 			m_iframe.removeFromParent();
 		}
 	}
 }
