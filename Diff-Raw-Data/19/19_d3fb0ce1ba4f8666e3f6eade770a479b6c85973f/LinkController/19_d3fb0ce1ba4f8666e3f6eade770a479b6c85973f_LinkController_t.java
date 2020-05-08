 package org.linkstorage.controller;
 
 import java.io.StringReader;
import java.sql.Date;
 import java.util.List;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.oxm.jaxb.Jaxb2Marshaller;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import org.linkstorage.bean.LinkBean;
 import org.linkstorage.model.Link;
 import org.linkstorage.repository.LinkBase;
 
 @Controller
 @RequestMapping("/integration/*")
 public class LinkController {
 
 	private LinkBase linksRepository;
 
 	private Jaxb2Marshaller jaxb2Marshaller;
 
 	/**
 	 * Name of the jsp view template
 	 */
 	private static final String XML_VIEW_NAME = "links";
 
 	/**
 	 *
 	 * @param LinkBase linksRepository
 	 */
 	@Autowired
 	public LinkController(LinkBase linksRepository) {
 		this.linksRepository = linksRepository;
 	}
 
 	public void setJaxb2Marshaller(Jaxb2Marshaller jaxb2Marshaller) {
 		this.jaxb2Marshaller = jaxb2Marshaller;
 	}
 
 	/**
 	 * Gets link by id
 	 *
 	 * @param String id
 	 * @return ModelAndView
 	 */
 	@RequestMapping(method=RequestMethod.GET, value="link/{id}")
 	public ModelAndView getLink(@PathVariable String id) {
 		Link link = linksRepository.getLink(Integer.parseInt(id));
 		LinkBean linkbean = new LinkBean(link);
 
 		ModelAndView modelAndView = new ModelAndView(XML_VIEW_NAME);
 	    modelAndView.addObject("link", linkbean);
 
 	    return modelAndView;
 	}
 
 	/**
 	 * Get links
 	 *
 	 * @return ModelAndView
 	 */
 	@RequestMapping(method=RequestMethod.GET, value="links")
 	public ModelAndView getLinks() {
 		List<Link> links = linksRepository.getLinks();
 		LinkBean linksList = new LinkBean(links);
 
 		return new ModelAndView(XML_VIEW_NAME, "links", linksList);
 	}
 
 	/**
 	 * Adds link
 	 *
 	 * @param body
 	 * @return
 	 */
 	@RequestMapping(method=RequestMethod.POST, value="/link")
 	public ModelAndView addLink(@RequestBody String body) {
 		Source source = new StreamSource(new StringReader(body));
 		Link link = (Link) jaxb2Marshaller.unmarshal(source);
		
		//TODO convert String date to Date date
		link.setUpdatedAt(new Date(1258466400));
		link.setCreatedAt(new Date(1258466400));
 		linksRepository.addLink(link);
		
 		LinkBean linkbean = new LinkBean(link);
 
 		ModelAndView modelAndView = new ModelAndView(XML_VIEW_NAME);
 		modelAndView.addObject("link", linkbean);
 
 		return modelAndView;
 	}
 }
