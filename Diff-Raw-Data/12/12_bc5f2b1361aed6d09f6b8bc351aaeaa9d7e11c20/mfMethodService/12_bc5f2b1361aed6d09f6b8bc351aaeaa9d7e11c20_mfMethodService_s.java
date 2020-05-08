 package monbulk.MediaFlux.Services;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.xml.client.XMLParser;
 
 
 import arc.mf.client.Output;
 import arc.mf.client.ServiceContext;
 import arc.mf.client.ServiceRequest;
 import arc.mf.client.xml.XmlElement;
 import arc.mf.session.ServiceResponseHandler;
 import arc.mf.session.Session;
 
 
 import monbulk.shared.Architecture.IPresenter;
 import monbulk.shared.Architecture.iModel;
 import monbulk.shared.Form.iFormField;
 import monbulk.shared.Model.IPojo;
 import monbulk.shared.Model.pojo.pojoMethod;
 import monbulk.shared.Model.pojo.pojoMethodComplete;
 import monbulk.shared.Services.MethodService;
 
 
 import monbulk.shared.util.MonbulkEnums.ServiceActions;
 
 public class mfMethodService extends MethodService {
 
 	private class MethodResponseHandler implements ServiceResponseHandler
 	{
 		private MethodServiceHandler m_handler = null;
 		private MethodRequest m_request;
 		private MethodUpdateHandler mu_handler;
 		public MethodResponseHandler(MethodServiceHandler handler, MethodRequest request)
 		{
 			m_handler = handler;
 			m_request = request;
 		}
 		public MethodResponseHandler(MethodUpdateHandler handler, MethodRequest request)
 		{
 			mu_handler = handler;
 			m_request = request;
 		}
 		public void processResponse(XmlElement xe, List<Output> outputs) throws Throwable
 		{
 			ArrayList<pojoMethod> methods = new ArrayList<pojoMethod>();
 
 			switch (m_request)
 			{
 				case List:
 				{
 					/*
 					 * <response><method name="Animal-MRI-Simple" description="Animal MRI acquisition with simple method." version="1.1">727.1.1</method>
 					 * <method name="Animal-MRI-Simple-pvt" description="Animal MRI acquisition with simple method amd some meta in private fields" version="1.1">727.1.2</method>
 					 * <method name="Human-MRI-Simple-noRS" description="Human MRI acquisition with simple method and no R-Subject" version="1.1">727.1.3</method>
 					 * </response>
 					 */
 					//Window.alert(xe.elements().);
 					try
 					{
 					
 					List<XmlElement> elemMethods =  xe.elements("method");
 					Iterator<XmlElement> i = elemMethods.iterator();
 					while(i.hasNext())
 					{
 						XmlElement tmpItem = i.next();
 						String Name = tmpItem.value("@name");
 						String ID = tmpItem.value();
 						String Description = tmpItem.value("@description");
 						
 						pojoMethod tmpMethod = new pojoMethod();
 						
 						tmpMethod.setMethodID(ID);
 						
 						
 						tmpMethod.setFieldVale(pojoMethod.MethodDescriptionField, Description);
 						
 						tmpMethod.setMethodName(Name);
 						methods.add(tmpMethod);
 					}
 					//Window.alert(xe.values("method").toString());
 					m_handler.onReadMethodList(methods);
 					break;
 					}
 					catch(Exception ex)
 					{
 						GWT.log("Error occurs @ mfMethodService.processResponse-LIST" + ex.getMessage() + ex.getStackTrace().toString());
 					}
 				}
 				case Describe:
 					try
 						{
 						/*<response>
 						 * <method id="727.1.1" asset="92" version="1">
 						 *	<name>Animal-MRI-Simple</name>
 						 *	<description>Animal MRI acquisition with simple method.</description>
 						 *	<version>1.1</version>
 						 *		<step id="1">
 						 *			<name>MRI acquisition</name>
 						 *			<description>MRI acquisition of subject</description>
 						 *			<study><type>Magnetic Resonance Imaging</type><dicom><modality>MR</modality></dicom></study>
 						 *		</step>
 						 *		<subject>
 						 *			<project>
 						 *				<public>
 						 *					<metadata><definition requirement="mandatory">hfi.pssd.animal.disease</definition></metadata>
 						 *					<metadata><definition requirement="mandatory">hfi.pssd.identity</definition></metadata>
 						 *					<metadata><definition requirement="mandatory">hfi.pssd.subject</definition><value><type>constant(animal)</type></value></metadata>
 						 *					<metadata><definition requirement="mandatory">hfi.pssd.animal.subject</definition></metadata>
 						 *
 						 *				</public>
 						 *			</project>
 						 *		</subject>
 						 *	</method>
 						 * </response>
 						*/
 						//Window.alert(xe.toString());
 						//We need to create 
 						pojoMethodComplete pmc = new pojoMethodComplete(xe.elements().get(0).attributes().get(0).value());
 						pmc.readInput("XML", xe.toString());
 						m_handler.onReadMethod(pmc);
 					}
 					catch(Exception ex)
 					{
 						GWT.log("Exception caught at mfMethodService.onReadMethod" + ex.getMessage());
 					}
 					//pmc.getMethodDetails().readInput("XML", xe.stringValue("/method"));
 					//XMLParser.parse(contents)
 					//m_handler.onReadMethod(pmc);
 					break;
 				case Create:
 					try
 					{
						//Window.alert(xe.stringValue("id"));
						mu_handler.onUpdateMethod(xe.stringValue("id"));
 					}
 					catch(Exception ex)
 					{
 						GWT.log("Exception caught at mfMethodService.onReadMethod" + ex.getMessage());
 					}
 					break;
 				default:
 					break;
 			}
 		}
 	}
 	
 	
 	private String generateXML(String ID)
 	{
 		
 		return ID;
 		
 	}
 	
 
 	@Override
 	public void getMethodList(MethodServiceHandler handler) {
 		Session.execute(new ServiceContext("mfMethodService.getMethodList"), 
 						"om.pssd.method.list", 
 						"", 
 						null, 
 						0, 
 						new MethodResponseHandler(handler, MethodRequest.List), 
 						true);
 		
 	}
 
 	@Override
 	public void getMethod(String ID, MethodServiceHandler handler) {
 		Session.execute(
 						new ServiceContext("mfMethodService.getMethod"), 
 						"om.pssd.method.describe", 
 						"<id>" + ID + "</id>",
 						null,
 						0, 
 						new MethodResponseHandler(handler, MethodRequest.Describe), 
 						true);
 		
 	}
 
 
 	@Override
 	public void createOrUpdate(String xml, MethodUpdateHandler handler) {
 		Session.execute(
 						new ServiceContext("mfMethodService.update"),
 						"om.pssd.method.for.subject.update",
 						xml,
 						null,
 						0,
 						new MethodResponseHandler(handler, MethodRequest.Create), 
 						true);
 						
 						
 						
 		
 	}
 
 }
