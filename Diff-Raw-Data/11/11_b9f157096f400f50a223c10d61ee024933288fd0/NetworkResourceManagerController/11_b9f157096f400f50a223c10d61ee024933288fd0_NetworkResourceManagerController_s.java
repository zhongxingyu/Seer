 package org.jcwal.so.network.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.validation.Valid;
 
 import org.jcwal.so.network.domain.NetworkResource;
 import org.jcwal.so.network.domain.SegmentResource;
 import org.jcwal.so.network.repository.NetworkResourceRepository;
 import org.jcwal.so.network.repository.SegmentResourceRepository;
 import org.jcwal.so.network.service.NetworkResourceService;
 import org.jcwal.so.network.service.impl.NetworkSchemaRegistory;
 import org.macula.base.security.util.SecurityUtils;
 import org.macula.core.exception.FormBindException;
 import org.macula.core.mvc.annotation.FormBean;
 import org.macula.core.mvc.annotation.OpenApi;
 import org.macula.core.mvc.view.ExcelView;
 import org.macula.plugins.mda.finder.view.FinderView;
 import org.macula.plugins.mda.finder.vo.FinderSelection;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 
 @Controller
 public class NetworkResourceManagerController extends AdminNetworkController {
 
 	@Autowired
 	private NetworkResourceRepository networkResourceRepository;
 	@Autowired
 	private SegmentResourceRepository segmentResourceRepository;
 	@Autowired
 	private NetworkResourceService networkResourceService;
 
 	@RequestMapping(value = "/networkresource/list", method = RequestMethod.GET)
 	public View index() {
 		return new FinderView(NetworkSchemaRegistory.NETWORKRESOURCE_SCHEMA);
 	}
 
 	@RequestMapping(value = "/networkresource/delete", method = RequestMethod.POST)
 	@OpenApi
 	public boolean delete(@FormBean("selection") FinderSelection selection) {
 		for (String itemId : selection.getIterable(SecurityUtils.getUserContext(), true)) {
 			if (itemId != null) {
 				networkResourceRepository.delete(Long.parseLong(itemId));
 			}
 		}
 		return true;
 	}
 
 	@RequestMapping(value = "/networkresource/create", method = RequestMethod.POST)
 	public String create(Model model) {
 		return super.getRelativePath("/networkresource/edit");
 	}
 
 	@RequestMapping(value = "/networkresource/edit", method = RequestMethod.POST)
 	public String edit(@FormBean("selection") FinderSelection selection, Model model) {
 		String networkId = selection.getItems().get(0);
 		model.addAttribute("networkId", networkId);
 		return super.getRelativePath("/networkresource/edit");
 	}
 
 	@RequestMapping(value = "/networkresource/save", method = RequestMethod.POST)
 	@OpenApi
 	public Long save(@FormBean("network") @Valid NetworkResource network) {
 		if (hasErrors()) {
 			throw new FormBindException(getMergedBindingResults());
 		}
 		networkResourceRepository.save(network.updateRents());
 		return network.getId();
 	}
 
 	@RequestMapping(value = "/networkresource/export", method = RequestMethod.POST)
 	public ModelAndView export(@FormBean("selection") FinderSelection selection, Model model) {
 		List<NetworkResource> resources = new ArrayList<NetworkResource>();
 		List<Long> ids = new ArrayList<Long>();
 		for (String itemId : selection.getIterable(SecurityUtils.getUserContext(), false)) {
 			if (itemId != null) {
 				ids.add(Long.parseLong(itemId));
 				if (ids.size() > 900) {
 					resources.addAll(networkResourceRepository.findIds(ids));
 					ids.clear();
 				}
 			}
 		}
 		if (!ids.isEmpty()) {
 			resources.addAll(networkResourceRepository.findIds(ids));
 		}
 		model.addAttribute("ips", resources);
 		return new ModelAndView(new ExcelView(getRelativePath("/networkresource/IPExport")), model.asMap());
 	}
 
 	@RequestMapping(value = "/networkresource/get/{networkId}", method = RequestMethod.GET)
 	@OpenApi
 	public NetworkResource getNetworkResource(@PathVariable("networkId") Long networkId) {
 		return networkResourceRepository.findOne(networkId);
 	}
 
 	@RequestMapping(value = "/networkresource/detail", method = RequestMethod.GET)
 	public String viewDetail(Model model, @RequestParam("item") Long id) {
 		NetworkResource network = networkResourceRepository.findOne(id);
 		model.addAttribute("network", network);
 		return super.getRelativePath("/networkresource/detail");
 	}
 
 	@RequestMapping(value = "/networkresource/segdetail", method = RequestMethod.GET)
 	public String viewSegdetail(Model model, @RequestParam("item") Long id) {
 		NetworkResource network = networkResourceRepository.findOne(id);
		List<SegmentResource> segments = segmentResourceRepository.findIpBelongs(network.getIp());
 		model.addAttribute("segments", segments);
 		return super.getRelativePath("/networkresource/segdetail");
 	}
 
 }
