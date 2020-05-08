 package com.pharmasynth.web;
 
import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import com.pharmasynth.dao.ClientDAO;
 import com.pharmasynth.model.Client;
 import com.pharmasynth.util.Country;
 import com.pharmasynth.util.Utils;
 
 @Controller
 public class ClientController extends MultiActionController
 {
 	private static Logger log = Logger.getLogger(ClientController.class);
 	
 	@Autowired
 	private ClientDAO clientDAO;
 	
 	/**
 	 * Forwards to index view
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "clients/index.do",method = RequestMethod.GET)
 	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception 
 	{
 		Map<String,Object> params = new HashMap<String, Object>();
 		params.put("clients",clientDAO.list());
 		return new ModelAndView("clients/index",params);
 	}
 	
 	/**
 	 * Forwards to new clients view
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "clients/new_clients.do",method = RequestMethod.GET)
 	public ModelAndView newClients(HttpServletRequest request, HttpServletResponse response) throws Exception 
 	{
 		Map<String,Object> params = new HashMap<String, Object>();
 		
 		String id = Utils.cleanString(request.getParameter("id"));
 		
 		if(id != null)
 		{
 			try
 			{
 				Client cl = clientDAO.get(Long.parseLong(id));
 				params.put("client",cl);
 			}
 			catch (Exception ex)
 			{
 				log.error("newClients: unable to retrieve user with the id " + id,ex);
 			}
 		}
 		
 		params.put("countries",Country.listCountries());
 		return new ModelAndView("clients/new_clients",params);
 	}
 	
 	/**
 	 * Forwards to view clients view
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "clients/view_clients.do",method = RequestMethod.GET)
 	public ModelAndView viewClients(HttpServletRequest request, HttpServletResponse response) throws Exception 
 	{
 		Map<String,Object> params = new HashMap<String, Object>();
 		
 		String id = Utils.cleanString(request.getParameter("id"));
 		if(id != null)
 		{
 			try
 			{
 				Client cl = clientDAO.get(Long.parseLong(id));
 				params.put("client",cl);
 			}
 			catch (Exception ex)
 			{
 				log.error("newClients: unable to retrieve user with the id " + id,ex);
 				params.put("error","Internal Error, please try again.");
 				return new ModelAndView("clients/index",params);
 			}
 		}
 		else
 		{
 			params.put("error","Internal Error, please try again.");
 			return new ModelAndView("clients/index",params);
 		}
 		
 		return new ModelAndView("clients/view_clients",params);
 	}
 	
 	/**
 	 * Saves the client
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "clients/save_clients.do",method = RequestMethod.POST)
 	public ModelAndView saveClient(HttpServletRequest request, HttpServletResponse response) throws Exception 
 	{
 		String id = Utils.cleanString(request.getParameter("id"));
 		String name = Utils.cleanString(request.getParameter("name"));
 		String country = Utils.cleanString(request.getParameter("country"));
 		String city = Utils.cleanString(request.getParameter("city"));
 		String address = Utils.cleanString(request.getParameter("address"));
 		String billingAddress = Utils.cleanString(request.getParameter("billingAddress"));
 		String telephone = Utils.cleanString(request.getParameter("telephone"));
 		String email = Utils.cleanString(request.getParameter("email"));
 		String acquisition = Utils.cleanString(request.getParameter("acquisition"));
 		String type =  Utils.cleanString(request.getParameter("type"));
 		String industry =  Utils.cleanString(request.getParameter("industry"));
 		String description =  Utils.cleanString(request.getParameter("description"));
 		
 		Map<String,Object> params = new HashMap<String, Object>();
 			
 		if(name == null)
 		{
 			params.put("error","Name field is mandatory. Please, provide a name.");
 			return new ModelAndView("clients/new_clients",params); 
 		}
 		
 		try
 		{
 			
 			Client client = new Client();
 			
 			if(id != null)
 			{
 				client.setId(Utils.getInteger(id).longValue());
 			}
			else
			{
				client.setCreatedDate(new Date());
			}
 			
 			client.setAcquisitionType(acquisition);
 			client.setAddress(address);
 			client.setBillingAddress(billingAddress);
 			client.setCity(city);
 			client.setClientType(type);
 			client.setCountry(country);
 			client.setDescription(description);
 			client.setEmail(email);
 			client.setIndustryType(industry);
 			client.setName(name);
 			client.setTelephone(telephone);
 			
 			client = clientDAO.save(client);
 			
 			if(client != null && client.getId() != null)
 			{
 				params.put("success","Client " + client.getName() + " registered successfully in the database.");
 				params.put("clients",clientDAO.list());
 				return new ModelAndView("clients/index",params);
 			}
 			else
 			{
 				params.put("error","Error saving the Client, please try again or contact the administrator.");
 				return new ModelAndView("clients/new_clients",params);
 			}
 			
 		}
 		catch (Exception ex)
 		{
 			log.error("saveClient: error saving the Client.",ex);
 			params.put("error","Error saving the Client, please try again or contact the administrator.");
 			return new ModelAndView("clients/new_clients",params); 
 		}
 	}
 	
 }
