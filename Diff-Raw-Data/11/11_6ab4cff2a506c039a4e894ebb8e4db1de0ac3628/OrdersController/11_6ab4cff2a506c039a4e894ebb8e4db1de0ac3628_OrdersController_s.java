 package controllers;
 
 import org.nutz.dao.Cnd;
 import org.nutz.dao.Dao;
 import org.nutz.dao.Sqls;
 import org.nutz.dao.sql.Sql;
 import org.nutz.lang.Lang;
 import org.nutz.lang.Strings;
 import org.nutz.lang.util.Context;
 import org.nutz.mvc.annotation.Ok;
 import org.nutz.mvc.annotation.Param;
 
 import utils.CV;
 import utils.LocalMessage;
 import utils.form.PageForm;
 import domains.Orders;
 
 public class OrdersController {
 
 	@Ok(">>:/orders/list")
 	public void index(){
 	}
 	/**
 	 * @return
 	 */
 	public Object list(@Param("state") String state,@Param("offset")int offset , @Param("max")int max ) {
 		if(Strings.isEmpty(state)){
 			state = "未处理";
 		}
 		PageForm<Orders> pf = PageForm.getPaper(dao, Orders.class,Cnd.where("state", "=", state), offset, max);
 		Context result = Lang.context();
 		result.set("obj", pf);
 		result.set("state", state);
 		return result;
 	}
 
 	public void create() {
 	}
	@Ok(">>:/orders/list")
 	public Object save(@Param("..")Orders orders) {
 		String message = "";
 		if(orders != null){
 				orders.setState("未处理");
 				dao.insert(orders);
				message = "插入成功";
 		}else{
 			message = "校验不成功";
 		}
		return CV.redirect("/huodan.html", message);
 	}
 	public Object edit(@Param("id")long id) {
 		Orders orders = dao.fetch(Orders.class,id);
 		if(orders == null){
 			return CV.redirect("/orders/list", "该"+LocalMessage.get("Orders.listName")+"不存在");
 		}
 		return orders;
 	}
 	@Ok(">>:/orders/list")
 	public Object update(@Param("..")Orders orders) {
 		String message = "";
 		if(orders!=null){
 				dao.update(orders);
 				message = "更新成功";
 		}else{
 			message = "校验不成功,请重新输入";
 			return CV.redirect("/orders/list", message);
 		}
 		return  message;
 	}
 	@Ok(">>:/orders/list")
 	public Object delete(@Param("id")Long id) {
 		dao.delete(Orders.class, id);
 		return  "删除成功";
 	}	
 	@Ok(">>:/orders/list")
 	public Object deleteAll(@Param("ids")String ids) {
 		if(!Strings.isEmpty(ids)){
 			Sql sql = Sqls.create("delete from orders where id in ("+ids+")");
 			dao.execute(sql);
 		}
 		return "删除成功";
 	}
 	private Dao dao;
 	public void setDao(Dao dao){
 		this.dao = dao;
 	}
 }
 
