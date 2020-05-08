 package com.smit.web.control.action;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang3.RandomStringUtils;
 import org.apache.http.HttpResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.MappingDispatchAction;
 
 import com.smit.service.ProductControlService;
 import com.smit.util.SmitPage;
 import com.smit.util.StringUtils;
 import com.smit.util.TripleDESHelper;
 import com.smit.vo.CertifiedProduct;
 import com.smit.vo.Device;
 import com.smit.vo.Order;
 
 public class AuthAction extends MappingDispatchAction{
 	
 	ProductControlService productService;
 	
 	public ProductControlService getProductService() {
 		return productService;
 	}
 
 	public void setProductService(ProductControlService productService) {
 		this.productService = productService;
 	}
 
 	//	登录鉴权
 	public ActionForward loginAuth(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException{
 		String machineID = request.getParameter("machineID");
 		String order_code = request.getParameter("order_code");
 		try{
 			if(!("".equals(machineID))&& null!= machineID){
 				machineID = StringUtils.formatMachineId(machineID,16);
 				List<Order> orderList = productService.loadOrder(order_code);
 				processOrder(response, machineID, order_code, orderList);
 			}else{
 				createResult(response, "", "fail");
 			}
 		}catch (Exception e){
 			sendErrorResult(response, "101");
 		}
 		
 		return null;
 	}
 
 	private void processOrder(HttpServletResponse response, String machineID,
 			String order_code, List<Order> orderList) throws ParseException,
 			IOException {
 		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
 		Date date = new Date();
 		Order order = orderList.get(0);
 		if(order != null && date.before(format.parse(order.getEnd_time()))){
 			String checkID = StringUtils.getCheckId(machineID);
 			insertDevice(order_code, format, date, checkID,machineID);
 			createResult(response, checkID,"success");
 		}else{
 			createResult(response, "", "fail");
 		}
 	}
 
 	/**
 	 * machineid = chipid(16位)+ mac地址(12位)
 	 * 生成checkid的时候我们只取最后的16位
 	 * @param machineID
 	 * @return
 	 */
 	
 
 	private void insertDevice(String order_code, SimpleDateFormat format,
 			Date date, String checkID,String machineID) {
 		Device device = new Device();
 		device.setCheck_id(checkID);
 		device.setOrder_code(order_code);
 		device.setCreate_time(format.format(date));
 		device.setMachineID(machineID);
 		List<Order> orders = productService.loadOrder(order_code);
 		if(orders.size()>0){
 			device.setOrder(orders.get(0));
 		}
 		productService.addDevice(device);
 	}
 
 	private void createResult(HttpServletResponse response, String checkID,String type)
 			throws IOException {
 		response.setContentType("text/xml;charset=utf-8");
 		response.setHeader("Cache-Control", "no-cache");
 		PrintWriter pw = response.getWriter();
 		StringBuffer sb = new StringBuffer();
 		TripleDESHelper helper = new TripleDESHelper("123456789ABCDEFG");
 		sb.append("<global>");
 		if(type.equals("success")){
 			sb.append("<checkID>"+helper.encode(checkID)+"</checkID>");
 		}else if(type.equals("fail")){
 			sb.append("<statusCode>101</statusCode>");
 		}
 		sb.append("</global>");
 		pw.println(sb.toString());
 	}
 
 	
 	//请求授权
 	public ActionForward reqAuth(ActionMapping mapping , ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws IOException{
 		String machineID = request.getParameter("machineID");
 		String checkID = request.getParameter("checkID");
 		try{
 			List<Device> list = productService.getDevice(checkID.trim());
 			sendAuthCode(response, machineID, list);
 		}catch (Exception e){
 			sendErrorResult(response,"105");
 		}
 		return null;
 	}
 
 	private void sendErrorResult(HttpServletResponse response,String code)
 			throws IOException {
 		response.setContentType("text/xml;charset=utf-8");
 		response.setCharacterEncoding("utf-8");
 		StringBuffer sb = new StringBuffer();
 		sb.append("<global>");
 		sb.append("<statusCode>"+code+"</statusCode>");
 		sb.append("</global>");
 		response.getWriter().println(sb.toString());
 	}
 
 	private void sendAuthCode(HttpServletResponse response, String machineID,
 			List<Device> list) throws IOException  {
 		response.setContentType("text/xml;charset=utf-8");
 		response.setCharacterEncoding("utf-8");
 		StringBuffer sb = new StringBuffer();
 		sb.append("<global>");
 		if(list.size()>0){
 			Order order = productService.loadOrder(list.get(0).getOrder().getId());
 			machineID = StringUtils.formatMachineId(machineID, 32);
 			String m_code = StringUtils.formatMachineId(order.getManufacturer_code(), 8);
 			String p_code = StringUtils.formatMachineId(order.getProduction_code(), 8);
 			String define = "0000000000000000";
 			String authCode = machineID + m_code + p_code + define;
 			System.out.println(authCode.length()+"auth_code is:"+authCode);
 			TripleDESHelper helper = new TripleDESHelper("123456789ABCDEFG");
 			sb.append("<authCode>"+helper.encode(authCode)+"</authCode>");
 			Device device = list.get(0);
 			device.setAuth_code(authCode);
 			productService.updateDevice(device);
 		}else{
 			sb.append("<statusCode>105</statusCode>");
 		}
 		sb.append("</global>");
 		response.getWriter().println(sb.toString());
 		
 	}
 	
 	//激活
 	public ActionForward active(ActionMapping mapping ,ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws IOException{
 		String checkID = request.getParameter("checkID");
 		try{
 			List<Device> list = productService.getDevice(checkID);
 			response.setContentType("text/xm" +
 					"l;charset=utf-8");
 			response.setCharacterEncoding("utf-8");
 			StringBuffer sb = new StringBuffer();
 			sb.append("<global>");
 			if(list.size()>0){
 				sb.append("<statusCode>200</statusCode>");
 				Device device = list.get(0);
 				device.setActive_status("200");
 				productService.updateDevice(device);
 			}else{
 				sb.append("<statusCode>106</statusCode>");
 			}
 			sb.append("</global>");
 			response.getWriter().println(sb.toString());
 		}catch (Exception e){
 			sendErrorResult(response, "106");
 		}
 		
 		return null;
 	}
 	
 	public ActionForward queryDevice(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request,HttpServletResponse response) throws Exception{
 		String orderCode = request.getParameter("orderCode");
 		String productCode = request.getParameter("productCode");
 		String manuCode = request.getParameter("manuCode");
 		Page page = new Page();
 		List<Device> list = productService.queryDevice(orderCode, productCode, manuCode);
 		int count = list.size();
 		if(count>page.size){
 			list = list.subList(0, 10);
 		}
 		page.setCurrentPage(1);
 		page.setTotalRecord(count);
 		Device device = new Device();
 		device.setOrder_code(orderCode);
 		device.setMachineID(productCode);
 		request.setAttribute("list", list);
 		request.getSession().setAttribute("page", page);
 		request.getSession().setAttribute("device", device);
 		return mapping.findForward("queryDevice");
 	}
 	public ActionForward getPageDevice(ActionMapping mapping,ActionForm form,
 			HttpServletRequest request,HttpServletResponse response) throws Exception{
		String orderCode = request.getParameter("orderCode");
		String productCode = request.getParameter("productCode");
 		String manuCode = request.getParameter("manuCode");
 		Page page = (Page) request.getSession().getAttribute("page");
 		String curStr = request.getParameter("currentPage");
 		String type = request.getParameter("type");
 		int currentPage = Integer.parseInt(curStr);
 		int start = 0;
 		if("pre".equals(type)){
 			if(currentPage>1){
 				//currentPage = currentPage-1;
 				page.setCurrentPage(currentPage-1);
 			}
 			int begin = (currentPage-2)*10;
 			start = begin>=0 ? begin : 0;
 		}else if("next".equals(type)){
 			if(currentPage<page.getCount()){
 				start = currentPage*(10);
 				//currentPage = currentPage+1;
 				page.setCurrentPage(currentPage+1);
 			}else{
 				//currentPage = page.getCount();
 				page.setCurrentPage(page.getCount());
 				start = (currentPage-1)*(10);
 			}
 		}
 		List<Device> list = productService.queryPageDevice(orderCode, productCode, manuCode, start, page.size);
 		request.setAttribute("list", list);
 		request.getSession().setAttribute("page", page);
 		return mapping.findForward("querypageDevice");
 	}
 	
 }
