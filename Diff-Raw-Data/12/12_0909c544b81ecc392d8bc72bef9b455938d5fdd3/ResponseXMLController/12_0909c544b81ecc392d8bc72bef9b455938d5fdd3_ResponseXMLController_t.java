 package com.towne.framework.springmvc.controller;
 
 import java.util.List;
 
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.towne.framework.common.service.IFacadeService;
 import com.towne.framework.common.model.Trader;
 import com.towne.framework.hibernate.model.Moment;
 import com.towne.framework.springmvc.model.Moments;
 
 /**
  * return xml format data
  * @author towne
  *
  */
 @Controller
 @RequestMapping(value="/xml",method={RequestMethod.GET})
 public class ResponseXMLController {
 	
 	@Autowired
 	IFacadeService ifacadeService;
 	
 	/**
 	 * query a xml date by id
 	 * @param id
 	 * @return
 	 */
 	@RequestMapping(value="/contact/{id}",produces=MediaType.APPLICATION_XML_VALUE)
 	public @ResponseBody Moment getContactInXML(@PathVariable(value="id")int id){
 		Trader trader = new Trader();
		Moment moment = ifacadeService.findById(trader, id);
 		return moment;
 	}
 	
 	
 	/**
 	 * query multiple xml data
 	 * @return
 	 */
 	@RequestMapping(value="/contacts",produces=MediaType.APPLICATION_XML_VALUE)
 	public @ResponseBody Moments getContactsInXML(){
 		Trader trader = new Trader();
		List<Moment> moment = ifacadeService.query(trader, "select t from Moment t");
 		Moments moments=new Moments();
 		moments.setMoments(moment);
 		return moments;
 	}
 }
