 package com.smit.web.push.action;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang3.RandomStringUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 import org.apache.struts.upload.FormFile;
 
 import com.smit.service.PushService;
 import com.smit.service.push.IPushDataService;
 import com.smit.service.push.IPushManageService;
 import com.smit.util.Constants;
 import com.smit.vo.PushContent;
 import com.smit.vo.UserAccountResource;
 import com.smit.web.control.action.Page;
 
 public class PushAction extends DispatchAction{
 
 	private PushService service;
 	private IPushManageService pushManageService;
 	public PushService getService() {
 		return service;
 	}
 
 	public void setService(PushService service) {
 		this.service = service;
 	}
 
 	public IPushManageService getPushManageService() {
 		return pushManageService;
 	}
 
 	public void setPushManageService(IPushManageService pushManageService) {
 		this.pushManageService = pushManageService;
 	}
 
 	@Override
 	protected ActionForward unspecified(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		return new ActionForward("/home.do");
 	}
 	
 	public  ActionForward showMain(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response){
 		
 		List<PushContent> list = service.getContent();
 		int count = list.size();
 		if(count>10){
 			list = list.subList(0, 10);
 		}
 		setPhotos(list);
 		System.out.println(list.size());
 		int total = setTotal(count);
 		request.getSession().setAttribute("count", count);
 		request.setAttribute("total", total);
 		request.setAttribute("list", list);
 		request.setAttribute("currentpage", 1);
 		return new ActionForward("/main.do");
 	}
 
 	private int setTotal(int count) {
 		int total;
 		if(count%10==0){
 			total = count/10;
 		}else{
 			total = count/10+1;
 		}
 		return total;
 	}
 	public  ActionForward showLink(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response){
 		String type = request.getParameter("type");
 		List<UserAccountResource> list = pushManageService.listAllResource(
 				(String)request.getSession().getAttribute(Constants.CURUSERNAME));
 			request.setAttribute("list", list);
 		System.out.println(list.size());
 		
 		return new ActionForward("/"+type+".do");
 	}
 	public ActionForward text(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception{
 		String title = request.getParameter("title");
 		String content = request.getParameter("content");
 		System.out.println(title);
 		System.out.println(content);
 		PushContent pc = new PushContent();
 		pc.setTitle(title);
 		pc.setContent(content);
 		pc.setDes(content);
 		pc.setContent_type("TEXT");
 		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		pc.setCreate_time(formater.format(new Date()));
 		service.insertContent(pc);
 		pushData(request, pc);
 		
 		int count = 0;
 		int countStr  = (Integer) request.getSession().getAttribute("count");
 		count = countStr+1;
 		int total = setTotal(count);
 		List<PushContent> list = getContentList(0,10);
 		
 		request.getSession().setAttribute("count", count);
 		request.setAttribute("total", total);
 		request.getSession().setAttribute("list", list);
 		request.setAttribute("currentpage", 1);
 		return  new ActionForward("/main.do");
 	}
 
 	private List<PushContent> getContentList(int start, int num) {
 		List<PushContent> list = service.getContent(start,num);
 		setPhotos(list);
 		return list;
 	}
 
 	private void setPhotos(List<PushContent> list) {
 		if(list.size()>0){
 			for(PushContent p : list){
 				if("PICTURE".equals(p.getContent_type())){
 					String[] photos = p.getPath().split(";");
 					for(String s: photos){
 						p.getPhotos().add(s);
 					}	
 				}
 			}
 		}
 	}
 	
 	public ActionForward uploadPhoto(ActionMapping mapping,ActionForm form,
 		HttpServletRequest request, HttpServletResponse response) throws IOException{
 		PhotoForm photoForm = (PhotoForm) form;
 		System.out.println(photoForm.getFile().getFileName());
 		
 		String save_path = request.getSession().getServletContext().getRealPath("images");;
 		File file = new File(save_path);
 		if(!file.exists()){
 			file.mkdirs();
 		}
 		byte[] buffer = new byte[8192];
 		FormFile uploadFile = photoForm.getFile();
 		int count = 0;
 		if(null != uploadFile){
 			try {
 				InputStream in = uploadFile.getInputStream();
 				FileOutputStream out = new FileOutputStream(file + File.separator + uploadFile.getFileName());
 				while((count = in.read(buffer)) >0){
 					out.write(buffer, 0, count);
 				}
 				in.close();
 				out.close();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		//response.setContentType("text");
 		response.getWriter().println(uploadFile.getFileName());
 		return null;
 	}
 	
 	public ActionForward link(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception{
 			String title = request.getParameter("title");
 			String url = request.getParameter("url");
 			PushContent pc = new PushContent();
 			pc.setTitle(title);
 			pc.setUrl(url);
 			pc.setContent_type("URL");
 			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			pc.setCreate_time(formater.format(new Date()));
 			service.insertContent(pc);
 			pushData(request, pc);
 			
 			int count = 0;
 			int countStr  = (Integer) request.getSession().getAttribute("count");
 			count = countStr+1;
 			int total = setTotal(count);
 			List<PushContent> list = getContentList(0,10);
 			request.getSession().setAttribute("count", count);
 			request.setAttribute("total", total);
 			request.setAttribute("list", list);
 			request.setAttribute("currentpage", 1);
 			return new ActionForward("/main.do");
 	}
 	
 	public ActionForward photo(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception{
 		String desc = request.getParameter("desc");
 		String photoStr = request.getParameter("photos");
 		String[] paths = photoStr.split(";");
 		//List<String> list = new ArrayList<String>();
 		String path = "";
 		
 		for(String s : paths){
 			String[] p = s.replace("\\", "#").split("#");
 			String photo = p[p.length-1];
 			System.out.println(photo);
 			//list.add(photo);
 			path += photo+";";
 		}
 		PushContent pc = new PushContent();
 		pc.setDes(desc);
 		pc.setPath(path);
 		//注意这只保存最后一张图的url
 		String[] s = paths[paths.length-1].replace("\\", "#").split("#");
 		String loadurl = "http://"+request.getServerName()+
 		":"+request.getServerPort()+request.getContextPath()+"/images/"+s[s.length-1];
 		System.out.println(loadurl);
 		pc.setUrl(loadurl);
 		pc.setContent_type("PICTURE");
 		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		pc.setCreate_time(formater.format(new Date()));
 		service.insertContent(pc);
 		pushData(request, pc);
 		
 		int count = 0;
 		int countStr  = (Integer) request.getSession().getAttribute("count");
 		count = countStr+1;
 		int total = setTotal(count);
 		System.out.println("upload photos is:"+photoStr);
 		List<PushContent> list = getContentList(0,10);
 		request.getSession().setAttribute("count", count);
 		request.setAttribute("total", total);
 		request.setAttribute("list", list);
 		request.setAttribute("currentpage", 1);
 		return new ActionForward("/main.do");
 	}
 	
 	public ActionForward music(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception{
 		String url = request.getParameter("url");
 		String des = request.getParameter("des");
 		System.out.println("music url is:"+ url);
 		System.out.println(des);
 		PushContent pc = new PushContent();
 		pc.setUrl(url);
 		pc.setDes(des);
 		pc.setContent_type("AUDIO");
 		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		pc.setCreate_time(formater.format(new Date()));
 		service.insertContent(pc);
 		pushData(request, pc);
 		
 		int count = 0;
 		int countStr  = (Integer) request.getSession().getAttribute("count");
 		count = countStr+1;
 		int total = setTotal(count);
 		List<PushContent> list = getContentList(0,10);
 		request.getSession().setAttribute("count", count);
 		request.setAttribute("total", total);
 		request.setAttribute("list", list);
 		request.setAttribute("currentpage", 1);
 		return new ActionForward("/main.do");
 	}
 	
 	public ActionForward vedio(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception{
 		String url = request.getParameter("url");
 		String des = request.getParameter("des");
 		System.out.println("music url is:"+ url);
 		System.out.println(des);
 		PushContent pc = new PushContent();
 		pc.setUrl(url);
 		pc.setDes(des);
 		pc.setContent_type("VIDEO");
 		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		pc.setCreate_time(formater.format(new Date()));
 		service.insertContent(pc);
 		pushData(request, pc);
 		
 		int count = 0;
 		int countStr  = (Integer) request.getSession().getAttribute("count");
 		count = countStr+1;
 		int total = setTotal(count);
 		
 		List<PushContent> list = getContentList(0,10);
 		request.getSession().setAttribute("count", count);
 		request.setAttribute("total", total);
 		request.setAttribute("list", list);
 		request.setAttribute("currentpage", 1);
 		return new ActionForward("/main.do");
 	}
 	
 	public void pushData(HttpServletRequest request,PushContent pc) throws Exception{
 		HttpSession session = request.getSession();
		String[] deviceIds = request.getParameterValues("deviceIds");
 		IPushDataService ps = (IPushDataService)session.getAttribute(Constants.PUSH_CONNECTION);
 		List<String> userList = new ArrayList<String>();
		for(String s:deviceIds){
			userList.add(session.getAttribute(Constants.CURUSERNAME)
					+ "@smit/"+ s);
		}
 		ps.sendPushDataFromUser(userList, false, RandomStringUtils.randomNumeric(4), pc.getTitle(), "", pc.getUrl(), pc.getDes(), pc.getContent_type());
 	}
 	public  ActionForward content(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response){
 		String type = request.getParameter("type");
 		String currrentpage = request.getParameter("currentpage");
 		List<PushContent> p = service.getContent();
 		int currentPage = Integer.parseInt(currrentpage);
 		int start = 0;
 		int count = 0;
 		if(p.size()%10==0){
 			count = p.size()/10;
 		}else{
 			count = p.size()/10+1;
 		}
 		List<PushContent> list = null;
 		if("pre".equals(type)){
 			if(currentPage>1){
 				currentPage = currentPage-1;
 			}
 			int begin = (currentPage-2)*10;
 			start = begin>=0 ? begin : 0;
 		}else if("next".equals(type)){
 			
 			if(currentPage<count){
 				//page.setCurrentPage(currentPage+1);
 				start = currentPage*(10);
 				currentPage = currentPage+1;
 			}else{
 				currentPage = count;
 				start = (currentPage-1)*(10);
 			}
 		}
 		
 		list = service.getContent(start, 10);
 		setPhotos(list);
 		request.getSession().setAttribute("count", p.size());
 		request.setAttribute("total", count);
 		request.setAttribute("list", list);
 		request.setAttribute("currentpage", currentPage);
 		return new ActionForward("/main.do");
 	}
 	
 	public  ActionForward delete(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response){
 		String idStr = request.getParameter("id");
 		int id = 0;
 		if(!"".equals(idStr)){
 			id = Integer.parseInt(idStr);
 		}
 		PushContent content = service.getById(id);
 		service.deleteContent(content);
 		List<PushContent> list = service.getContent(0, 10);
 		setPhotos(list);
		int num = (Integer) request.getSession().getAttribute("count");
		int count = num-1;
		int total = setTotal(count);
		request.getSession().setAttribute("count", count);
		request.setAttribute("total", total);
 		request.setAttribute("list", list);
 		request.setAttribute("currentpage", 1);
 		return new ActionForward("/main.do");
 	}
 }
