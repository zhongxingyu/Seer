 package com.awsec2.web.action.admin;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts2.ServletActionContext;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.awsec2.domain.Movement;
 import com.awsec2.domain.User;
 import com.awsec2.service.IBusinessUnitService;
 import com.awsec2.service.IMovementService;
 import com.awsec2.service.IProductService;
 import com.awsec2.service.IUserService;
 import com.awsec2.web.action.BaseAction;
 import com.opensymphony.xwork2.Preparable;
 
 public class MovementAction extends BaseAction implements Preparable {
 
 	private ArrayList list_ProdIds = null;
 	private ArrayList list_BUIds = null;
 	private List<Movement> movements;
 	private Movement movement;
 	private User user = null;
 
 	@Autowired
 	private IMovementService imovementService;
 	@Autowired
 	private IProductService iProdService;
 	@Autowired
 	private IBusinessUnitService iBUService;
 	@Autowired
 	private IUserService userService;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5850906816763804411L;
 
 	public List<Movement> getMovements() {
 		return movements;
 	}
 
 	public void setMovements(List<Movement> movements) {
 		this.movements = movements;
 	}
 
 	public Movement getMovement() {
 		return movement;
 	}
 
 	public void setMovement(Movement movement) {
 		this.movement = movement;
 	}
 
 	public List<Long> getList_ProdIds() {
 		return list_ProdIds;
 	}
 
 	public void setList_ProdIds(ArrayList<Long> list_ProdIds) {
 		this.list_ProdIds = list_ProdIds;
 	}
 
 	public List<Long> getList_BUIds() {
 		return list_BUIds;
 	}
 
 	public void setList_BUIds(ArrayList<Long> list_BUIds) {
 		this.list_BUIds = list_BUIds;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public String init() throws Exception {
 		return "success";
 	}
 
 	public String query() throws Exception {
 		System.out.println("abc");
 		setMovements(imovementService.findMovementsByMovement(movement));
 		System.out.println(movements);
 		return "success";
 	}
 
 	@Override
 	public void prepare() throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * This method is used to load products by a given user's id
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-04
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-04 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String getSessionUserName() throws Exception {
 		// products = productService.getProductsByUserId(1);
 		String username = null;
 		HttpServletRequest req = ServletActionContext.getRequest();
 		if (req != null) {
 			HttpSession session = req.getSession();
 			if (session.getAttribute("username") != null) {
 				username = (String) session.getAttribute("username");
 			}
 		}
 		return username;
 	}
 
 	/**
 	 * This method is used to load products by a given user's id
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-04
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-04 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public void getUserOrgId() throws Exception {
 		// products = productService.getProductsByUserId(1);
 		if (user == null) {
 			setUser(userService.getUserByUsername(getSessionUserName()));
 		}
 	}
 
 	/**
 	 * This method is used to load products by a given user's id
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-04
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-04 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public void getProdIdsAndBUIds() throws Exception {
 		getUserOrgId();
 		if (list_ProdIds == null) {
 			setList_ProdIds(iProdService.getProdIdsByOrgId(user
 					.getOrganization_id()));
 		}
 		if (list_BUIds == null) {
 			System.out.println("user.getOrganization_id())"+user.getOrganization_id());
 			setList_BUIds(iBUService.getBUIdsByOrgId(user.getOrganization_id()));
 		}
 	}
 
 	/**
 	 * This method is used to load products by a given user's id
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-04
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-04 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String loadMovms() throws Exception {
 //		String fmt = "yyyy-mm-dd";
 //		getUserOrgId();
 //		getProdIdsAndBUIds();
 //		HashMap<String, Object> searchParam = new HashMap<String, Object>();
 //		searchParam.put("buIds", getList_BUIds());
 //		Movement movm = new Movement();
 //		Date date = new Date();
 //		SimpleDateFormat formatter = new SimpleDateFormat(fmt);
 //		formatter.format(date);		
 //		movm.setOper_date(date);
 //		System.out.println(date);
 //		searchParam.put("objMovm", movm);
 //		movements = imovementService.searchMovements(searchParam);
 		return SUCCESS;
 	}
 
 	/**
 	 * This method is used to load product by a given Prod's id
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-04
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-04 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String loadMovmById() throws Exception {
 		movement = imovementService.getMovementsByMovmId(movement.getId());
 		return SUCCESS;
 	}
 
 	/**
 	 * This method is used to foward to products add page
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-05
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-05 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String addMovms() throws Exception {
 		getProdIdsAndBUIds();
 //		if(getList_ProdIds() != null) {
 //			for(int i= 0;i<getList_ProdIds().size();i++) {
 //				System.out.println(i);
 //				list_ProdId[i] = (long)list_ProdIds.get(i);
 //			}
 //		}
 
 
 		// products = productService.getProductsByUserId(1);
 		// if(products != null) {
 		// for(Product prod: products) {
 		// System.out.println(prod.getName());
 		// }
 		// }
 		return SUCCESS;
 	}
 
 	/**
 	 * This method is used to commit products just added
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-05
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-05 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String commitAddMovms() throws Exception {
 		if (movement != null) {
 			imovementService.insertMovement(movement);
 		}
 		if (movements != null) {
 			imovementService.insertMovements(movements);
 		} else
 			System.out.println("null");
 		return SUCCESS;
 	}
 
 	/**
 	 * This method is used to edit products info for a special product
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-05
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-05 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String modifyMovms() throws Exception {
 		loadMovmById();
 		getProdIdsAndBUIds();
 		return SUCCESS;
 	}
 
 	/**
 	 * This method is used to commit updates for a product
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-05
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-05 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String commitModifyMovms() throws Exception {
 		if (movement != null) {
 			imovementService.updateMovm(movement);
 
 			// System.out.println(product.getName());
 
 		}
 		return SUCCESS;
 
 	}
 
 	/**
 	 * This method is used to delete a product
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-05
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-05 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String deleteMovms() throws Exception {
 		imovementService.delMovmById(movement.getId());
 		return SUCCESS;
 	}
 
 	/**
 	 * This method is used to serach products based on given prod's name or\and
 	 * prod's barcode
 	 * 
 	 * @author Bin Yuan
 	 * @created 2012-09-05
 	 * 
 	 * 
 	 @return String "success" or "error"
 	 * 
 	 @changelog 2012-09-05 Bin Yuan <bin.yuan@itbconsult.com> - Created
 	 * 
 	 */
 	public String searchMovms() throws Exception {
 		System.out.println("ssssssssssss111");
 		getUserOrgId();
 		getProdIdsAndBUIds();
 		HashMap<String, Object> searchParam = new HashMap<String, Object>();
		System.out.println("ssssssssssss");
 		searchParam.put("buIds", getList_BUIds());
 		System.out.println("ssssssssssss");
 		if (movement != null) {
 			if (movement.getOper_date() != null) {
 				String fmt = "yyyy-mm-dd";
 				SimpleDateFormat sdf = new SimpleDateFormat(fmt);
 				sdf.format(movement.getOper_date());
 				System.out.println(movement.getOper_date());
 			}
 			searchParam.put("objMovm", movement);
 			movements = imovementService.searchMovements(searchParam);
 		} 
 		return SUCCESS;
 	}
 
 }
