 package com.xone.action.app.adbanner;
 
 import java.util.ArrayList;
import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.xone.action.base.Action;
 import com.xone.model.hibernate.entity.Adbanner;
 import com.xone.model.utils.MyDateUtils;
 import com.xone.service.app.AdbannerService;
 
 public class AdBannerAction extends Action {
 	
 	private static final long serialVersionUID = -2554675564068314137L;
 	
 	@Autowired
 	protected AdbannerService adbannerService;
 	
 	protected Adbanner adbanner = new Adbanner();
 	
 	protected List<Adbanner> list = new ArrayList<Adbanner>();
 
 	public String index() {
 		Map<String, String> params = getRequestMap();
		params.put("today", String.format("%1$tY-%1tm-%1$td %1$tH:%1$tM:%1$tS", new Date()));
 		List<Adbanner> l = adbannerService.findAllByMap(params);
 		if (null != l && !l.isEmpty()) {
 			getList().addAll(l);
 		}
 		return SUCCESS;
 	}
 	
 	public String list() {
 		return SUCCESS;
 	}
 	
 	public String items() {
 		Map<String, String> map = getRequestMap();
 		Map<String, String> params = new HashMap<String, String>();
 		if ("down".equals(map.get("itemaction"))) {
 			params.put("gtDateCreated", MyDateUtils.format(getAdbanner().getDateCreated()));
 		} else if ("up".equals(map.get("itemaction"))) {
 			params.put("ltDateCreated", MyDateUtils.format(getAdbanner().getDateCreated()));
 		}
 		params.put("userId", getUserId().toString());
 		setList(getAdbannerService().findItemsByMap(params));
 		return SUCCESS;
 	}
 	
 	public String item() {
 		setAdbanner(getAdbannerService().findById(getAdbanner().getId()));
 		return SUCCESS;
 	}
 
 	public List<Adbanner> getList() {
 		return list;
 	}
 
 	public void setList(List<Adbanner> list) {
 		this.list = list;
 	}
 
 	public Adbanner getAdbanner() {
 		return adbanner;
 	}
 
 	public void setAdbanner(Adbanner adbanner) {
 		this.adbanner = adbanner;
 	}
 
 	public AdbannerService getAdbannerService() {
 		return adbannerService;
 	}
 
 	public void setAdbannerService(AdbannerService adbannerService) {
 		this.adbannerService = adbannerService;
 	}
 
 }
