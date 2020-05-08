 package swarm.client.view.sandbox;
 
 import java.util.HashMap;
 
 import org.apache.commons.collections.map.HashedMap;
 
 import swarm.client.entities.smBufferCell;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.smViewContext;
 import swarm.client.view.sandbox.smSandboxManager.I_StartUpCallback;
 import swarm.client.view.tabs.code.smI_CodeLoadListener;
 import swarm.shared.code.smU_UriPolicy;
 import swarm.shared.code.smUriData;
 import swarm.shared.debugging.smU_Debug;
 import swarm.shared.memory.smObjectPool;
 import swarm.shared.reflection.smI_Class;
 import swarm.shared.statemachine.smA_Action;
 import swarm.shared.statemachine.smA_State;
 import swarm.shared.structs.smCellAddress;
 import swarm.shared.structs.smCode;
 import swarm.shared.structs.smCodePrivileges;
 import swarm.shared.structs.smE_CellAddressParseError;
 import swarm.shared.structs.smE_NetworkPrivilege;
 import swarm.shared.structs.smGridCoordinate;
 import swarm.shared.thirdparty.smS_Caja;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.dom.client.Node;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Window;
 
 
 public class smCajaSandboxManager
 {
 	private HashMap<Element, smI_CellSandbox> m_associations = new HashMap<Element, smI_CellSandbox>();
 	
 	private final I_StartUpCallback m_callback;
 	
 	private final String m_apiNamespace;
 	
 	private final smCellApi m_cellApi;
 	
 	private final smObjectPool<smStaticCajaSandbox> m_staticSandboxPool;
 	private final smObjectPool<smDynamicCajaSandbox> m_dynamicSandboxPool;
 	
 	smCajaSandboxManager(final smCellApi cellApi, I_StartUpCallback callback, final String apiNamespace)
 	{
 		m_cellApi = cellApi;
 		m_apiNamespace = apiNamespace;
 		m_callback = callback;
 		
 		m_staticSandboxPool = new smObjectPool<smStaticCajaSandbox>(new smI_Class<smStaticCajaSandbox>()
 		{
 			@Override
 			public smStaticCajaSandbox newInstance()
 			{
 				return new smStaticCajaSandbox();
 			}
 		});
 		
 		m_dynamicSandboxPool = new smObjectPool<smDynamicCajaSandbox>(new smI_Class<smDynamicCajaSandbox>()
 		{
 			@Override
 			public smDynamicCajaSandbox newInstance()
 			{
 				return new smDynamicCajaSandbox(cellApi, apiNamespace);
 			}
 		});
 		
 		initialize_native(this, apiNamespace);
 	}
 	
 	private void createApi(String apiNamespace)
 	{
 		tameApi(apiNamespace);
 	}
 	
 	private native void tameApi(String apiNamespace)
 	/*-{
 		{
 			$wnd.caja.markReadOnlyRecord($wnd[apiNamespace]);
 			
 			$wnd.caja.markFunction($wnd[apiNamespace].snap);
 			
 			$wnd.caja.markFunction($wnd[apiNamespace].getCoordinate);
 			$wnd.caja.markFunction($wnd[apiNamespace].getAddress);
 			$wnd.caja.markFunction($wnd[apiNamespace].getPosition);
 			$wnd.caja.markFunction($wnd[apiNamespace].getUsername);
 			$wnd.caja.markFunction($wnd[apiNamespace].getGridWidth);
 			$wnd.caja.markFunction($wnd[apiNamespace].getGridHeight);
 			
 			$wnd[apiNamespace+"_tamed"] = $wnd.caja.tame($wnd[apiNamespace]);
 		}
 
 		{// CONSOLE
 			var consoleWrapper;
 			if (!"console" in window || typeof console === "undefined")
 			{
 				//TODO: Define an alternate logging facility, maybe within swarm itself
 				consoleWrapper =
 				{
 					log:function(arg){}
 				};
 			}
 			else
 			{
 				consoleWrapper =
 				{
 					log:function(arg){console.log(arg);}
 				};
 			}
 			$wnd.caja.markReadOnlyRecord(consoleWrapper);
 			$wnd.caja.markFunction(consoleWrapper.log);
 			$wnd.console_tamed = $wnd.caja.tame(consoleWrapper);
 		}
 		
 		{// ALERT
 			
 			$wnd.caja.markFunction($wnd[apiNamespace+"_alert"]);
 			$wnd.alert_tamed = $wnd.caja.tame($wnd[apiNamespace+"_alert"]);
 		}
 	}-*/;
 	
 	private void caja_initialize_success()
 	{
 		m_callback.onStartUpComplete(true);
 	}
 	
 	private void caja_initialize_failure()
 	{
 		m_callback.onStartUpComplete(false);
 	}
 	
 	private native void initialize_native(smCajaSandboxManager thisArg, String apiNamespace)
 	/*-{
 			$wnd.caja.initFeralFrame(window);
 			
 			function onSuccess()
 			{
 				$wnd.caja.whenReady
 				(
 					function()
 					{
 						thisArg.@swarm.client.view.sandbox.smCajaSandboxManager::createApi(Ljava/lang/String;)(apiNamespace);
 						thisArg.@swarm.client.view.sandbox.smCajaSandboxManager::caja_initialize_success()();
 					}
 				);
 			}
 		
 			$wnd.caja.initialize
 			(
 				{
 					server:					'/r.js/caja/2/',
 					resources:				'/r.js/caja/2/',
 					es5Mode :				true,
 					debug:					false,
 					maxAcceptableSeverity:	"NO_KNOWN_EXPLOIT_SPEC_VIOLATION"
 				},
 				function() //on success
 				{
 					onSuccess();
 				},
 				function() // on failure
 				{
 					thisArg.@swarm.client.view.sandbox.smCajaSandboxManager::caja_initialize_failure()();
 				}
 			);
 	}-*/;
 	
 	private smI_CellSandbox getSandbox(Element host)
 	{
 		smI_CellSandbox sandbox = m_associations.get(host);
 		
 		return sandbox;
 	}
 	
 	public void allowScrolling(Element element, boolean yesOrNo)
 	{
 		//smStaticCajaSandbox cajaContainer = getContainer(element);
 		
 		//cajaContainer.allowScrolling(yesOrNo);
 	}
 	
 	void start_static(Element host, String rawCode, String cellNamespace, smI_CodeLoadListener listener)
 	{
 		smI_CellSandbox currentSandbox = this.getSandbox(host);
 		
 		if( currentSandbox != null )
 		{
 			currentSandbox.stop(host);
 		}
 		
 		cellNamespace += smS_Caja.CAJA_NAMESPACE_SUFFIX;
 		
 		if( currentSandbox instanceof smStaticCajaSandbox )
 		{
 			currentSandbox.start(host, rawCode, cellNamespace, listener);
 		}
 		else
 		{
 			if( currentSandbox != null )
 			{
 				this.deallocate(host, currentSandbox);
 			}
 			
 			currentSandbox = this.allocate(host, smStaticCajaSandbox.class);
 			currentSandbox.start(host, rawCode, cellNamespace, listener);
 		}
 	}
 	
 	private smI_CellSandbox allocate(Element host, Class<? extends smI_CellSandbox> T)
 	{
 		smI_CellSandbox toReturn = null;
 		
 		if( T == smStaticCajaSandbox.class )
 		{
 			toReturn = m_staticSandboxPool.allocate();
 		}
 		else
 		{
 			toReturn = m_dynamicSandboxPool.allocate();
 		}
 		
 		m_associations.put(host, toReturn);
 		
 		return toReturn;
 	}
 	
 	private void deallocate(Element host, smI_CellSandbox instance)
 	{
 		if( instance.getClass() == smStaticCajaSandbox.class )
 		{
 			m_staticSandboxPool.deallocate((smStaticCajaSandbox) instance);
 		}
 		else
 		{
 			m_dynamicSandboxPool.deallocate((smDynamicCajaSandbox) instance);
 		}
 		
 		m_associations.remove(host);
 	}
 	
 	void start_dynamic(Element host, String rawCode, String cellNamespace, smI_CodeLoadListener listener)
 	{
 		smI_CellSandbox currentSandbox = this.getSandbox(host);
 		
 		if( currentSandbox != null )
 		{
 			currentSandbox.stop(host);
 		}
 		
 		// HACK: namespacing dynamic content separately to avoid "leaked" setInterval calls, et al, being able to
 		//		modify static DOM elements that are namespaced normally without the prefix. This is pretty
 		//		much just a bandaid workaround...behind the scenes, user code may still try to access non-existent elements.
 		cellNamespace = "d_" + cellNamespace + smS_Caja.CAJA_NAMESPACE_SUFFIX;
 		
 		if( currentSandbox instanceof smDynamicCajaSandbox )
 		{
 			currentSandbox.start(host, rawCode, cellNamespace, listener);
 		}
 		else
 		{
 			if( currentSandbox != null )
 			{
 				this.deallocate(host, currentSandbox);
 			}
 			
 			currentSandbox = this.allocate(host, smDynamicCajaSandbox.class);
 			currentSandbox.start(host, rawCode, cellNamespace, listener);
 		}
 	}
 	
 	void stop(Element host)
 	{
 		smI_CellSandbox currentSandbox = this.getSandbox(host);
 		
 		if( currentSandbox != null )
 		{
 			currentSandbox.stop(host);
			
			this.deallocate(host, currentSandbox);
 		}
 	}
 }
