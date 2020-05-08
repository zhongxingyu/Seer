 package net.nan21.dnet.core.web.controller.data;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import net.nan21.dnet.core.api.action.IActionResultSave;
 import net.nan21.dnet.core.api.marshall.IDsMarshaller;
 import net.nan21.dnet.core.api.service.IDsService;
 import net.nan21.dnet.core.web.result.ActionResultSave;
 
 public class AbstractDsWriteController<M, P>
 	extends AbstractDsRpcController<M, P>{
 
 	/**
 	 * Default handler for insert action.
 	 * @param resourceName
 	 * @param dataformat
 	 * @param dataString
 	 * @param paramString
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(method=RequestMethod.POST , params="action=insert")
 	@ResponseBody
 	public String insert(
 				@PathVariable String resourceName,
 				@PathVariable String dataFormat,
 				@RequestParam(value="data", required=false, defaultValue="[]") String dataString,
 				@RequestParam(value="params", required=false, defaultValue="{}") String paramString,
 				HttpServletResponse response
 	) throws Exception {		 
 		try {
 			this.prepareRequest();
 			
 			this.resourceName = resourceName;		
 			this.dataFormat = dataFormat;
 			
 			if (!dataString.startsWith("[")) {
 				dataString = "[" + dataString + "]";
 			}
 			
 			IDsService<M, P> service = getDsService(this.resourceName);		
 			IDsMarshaller<M, P> marshaller = service.createMarshaller(dataFormat);
 			
 			List<M> list = marshaller.readListFromString(dataString);
 			P params = marshaller.readParamsFromString(paramString); 	
 			
 			service.insert(list);
 			
 			IActionResultSave result = this.packResult(list, params); 
 			return marshaller.writeResultToString(result);
 		} catch(Exception e) {
 			 return this.handleException(e, response);
 		} finally {
 			this.finishRequest();
 		}
 	}
 	
 	/**
 	 * Default handler for update action.
 	 * @param resourceName
 	 * @param dataformat
 	 * @param dataString
 	 * @param paramString
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(method=RequestMethod.POST , params="action=update")
 	@ResponseBody	
 	public String update(
 				@PathVariable String resourceName,
 				@PathVariable String dataFormat,
 				@RequestParam(value="data", required=false, defaultValue="[]") String dataString,
 				@RequestParam(value="params", required=false, defaultValue="{}") String paramString,	
 				HttpServletResponse response
 	) throws Exception {
 		
 		try {
 			this.prepareRequest();
 			this.resourceName = resourceName;		
 			this.dataFormat = dataFormat;
 			
 			if (!dataString.startsWith("[")) {
 				dataString = "[" + dataString + "]";
 			}
 			IDsService<M, P> service = getDsService(this.resourceName);
 			IDsMarshaller<M, P> marshaller = service.createMarshaller(dataFormat);
 			
 			List<M> list = marshaller.readListFromString(dataString);
 			P params = marshaller.readParamsFromString(paramString); 	
 			
 			service.update(list);
 
 			IActionResultSave result = this.packResult(list, params); 
 			return marshaller.writeResultToString(result);
 		} catch(Exception e) {
 			 this.handleException(e, response);
 			 return null;
 		} finally {
 			this.finishRequest();
 		}
 	}
 	
 	
 	/**
 	 * Default handler for delete action.
 	 * @param resourceName
 	 * @param dataformat
 	 * @param idsString
 	 * @param paramString
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(method=RequestMethod.POST , params="action=delete")
 	@ResponseBody	
 	public String delete(
 				@PathVariable String resourceName,
 				@PathVariable String dataFormat,
 				@RequestParam(value="data", required=false, defaultValue="[]") String idsString,
 				@RequestParam(value="params", required=false, defaultValue="{}") String paramString,	
 				HttpServletResponse response
 	) throws Exception {
 		
 		try {
 			this.prepareRequest();
 			this.resourceName = resourceName;		
 			this.dataFormat = dataFormat;
 			
 			if (!idsString.startsWith("[")) {
 				idsString = "[" + idsString + "]";
 			}
 			IDsService<M, P> service = getDsService(this.resourceName);
 			IDsMarshaller<M, P> marshaller = service.createMarshaller(dataFormat);
 			
 			List<Object> list = marshaller.readListFromString(idsString, Object.class );
 			P params = marshaller.readParamsFromString(paramString); 	
 			
 			service.deleteByIds(list);
 
 			//IActionResultSave result = this.packResult(list, params); 
			return "{'success':true}"; // marshaller.writeResultToString(result);
 		} catch(Exception e) {
 			 this.handleException(e, response);
 			 return null;
 		} finally {
 			this.finishRequest();
 		}
 	}
 	
 	
 	public IActionResultSave packResult(List<M> data, P params ) {
 		IActionResultSave pack = new ActionResultSave();
 		pack.setData(data);
 		//pack.setParams(params);
 		return pack;
 	}
 	
 	 
 }
