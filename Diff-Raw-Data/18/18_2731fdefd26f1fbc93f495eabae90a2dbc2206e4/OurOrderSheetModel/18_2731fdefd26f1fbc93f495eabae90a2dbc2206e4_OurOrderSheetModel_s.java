 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.dto.OurOrder;
 import tk.c4se.halt.ih31.nimunimu.dto.OurOrderSheet;
 import tk.c4se.halt.ih31.nimunimu.dto.OurOrderSheetDetail;
 import tk.c4se.halt.ih31.nimunimu.dto.OurOrderStatus;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.OurOrderRepository;
 import tk.c4se.halt.ih31.nimunimu.repository.OurOrderSheetDetailRepository;
 import tk.c4se.halt.ih31.nimunimu.repository.OurOrderSheetRepository;
 
 /**
  * @author ne_Sachirou
  * 
  */
 public class OurOrderSheetModel implements DoPostModel {
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws DBAccessException
 	 */
 	public void postRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws DBAccessException {
 		val repo = new OurOrderRepository();
 		val sheetRepo = new OurOrderSheetRepository();
 		val detailRepo = new OurOrderSheetDetailRepository();
 		OurOrder order = new OurOrder();
 		setProperties(order, req);
 		try {
 			sheetRepo.insert(order.getOurOrderSheet());
 			for (val detail : order.getOurOrderSheet()
 					.getOurOrderSheetDetails()) {
 				detailRepo.insert(detail);
 			}
 			order.setOurOrderSheetId(order.getOurOrderSheet().getId());
 			repo.insert(order);
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			throw e;
 		}
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws DBAccessException
 	 */
 	public void putRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws DBAccessException {
 		val idStr = req.getParameter("id");
 		val repo = new OurOrderRepository();
 		val sheetRepo = new OurOrderSheetRepository();
 		val detailRepo = new OurOrderSheetDetailRepository();
 		OurOrder order = null;
 		try {
 			val sheet = sheetRepo.find(idStr);
 			if (sheet != null) {
 				order = repo.findByOurOrderSheetId(sheet.getId());
 			}
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			throw e;
 		}
 		if (order == null) {
 			throw new DBAccessException("OurOrderSheet " + idStr
 					+ " is not found in DB.");
 		}
 		setProperties(order, req);
 		try {
 			repo.update(order);
 			sheetRepo.update(order.getOurOrderSheet());
 			for (val detail : order.getOurOrderSheet()
 					.getOurOrderSheetDetails()) {
 				detailRepo.update(detail);
 			}
 		} catch (DBAccessException e1) {
 			e1.printStackTrace();
 			throw e1;
 		}
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws DBAccessException
 	 */
 	public void deleteRequest(HttpServletRequest req, HttpServletResponse resp)
 			throws DBAccessException {
 		val idStr = req.getParameter("id");
 		val repo = new OurOrderRepository();
 		val sheetRepo = new OurOrderSheetRepository();
 		OurOrder order = null;
 		try {
 			val sheet = sheetRepo.find(idStr);
 			if (sheet != null) {
 				order = repo.findByOurOrderSheetId(sheet.getId());
 			}
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			throw e;
 		}
 		if (order == null) {
 			throw new DBAccessException("OurOrderSheet " + idStr
 					+ " is not found in DB.");
 		}
 		try {
 			repo.delete(order);
 		} catch (DBAccessException e1) {
 			e1.printStackTrace();
 			return;
 		}
 	}
 
 	private void setProperties(OurOrder order, HttpServletRequest req) {
 		order.setSupplierId(Integer.parseInt(req
 				.getParameter("order_supplier_id")));
 		order.setMemberId(req.getParameter("order_member_id"));
 		order.setStatus(OurOrderStatus.valueOf(req.getParameter("order_status")));
 
 		OurOrderSheet sheet = new OurOrderSheet();
 		int amount = 0;
 		List<OurOrderSheetDetail> details = new ArrayList<>();
 		for (int i = 1; req.getParameter("detail_goods_id" + i) != null; ++i) {
 			val goodsId = req.getParameter("detail_goods_id" + i);
 			val price = Integer.parseInt(req.getParameter("detail_price" + i));
 			val goodsNumber = Integer.parseInt(req
 					.getParameter("detail_goods_number" + i));
 			OurOrderSheetDetail detail = new OurOrderSheetDetail();
 			detail.setOurOrderSheetId(sheet.getId());
 			detail.setGoodsId(goodsId);
 			detail.setPrice(price);
 			detail.setGoodsNumber(goodsNumber);
 			amount += price * goodsNumber;
 			details.add(detail);
 		}
 		sheet.setOurOrderSheetDetails(details);
 		sheet.setAmount(amount);
 		sheet.setTax((int) Math.floor(amount * 0.05));
 		order.setOurOrderSheet(sheet);
 	}
 }
