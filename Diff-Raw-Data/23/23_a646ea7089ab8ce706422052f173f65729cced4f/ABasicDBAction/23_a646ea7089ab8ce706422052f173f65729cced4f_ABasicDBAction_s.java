 package com.laivi.sic.action.basic;
 
 import java.lang.reflect.Field;
 
 import org.nutz.dao.Cnd;
 import org.nutz.dao.Condition;
 import org.nutz.dao.Dao;
 import org.nutz.dao.pager.Pager;
 import org.nutz.dao.util.cri.SqlExpressionGroup;
 import org.nutz.ioc.loader.annotation.Inject;
 import org.nutz.log.Log;
 import org.nutz.log.Logs;
 import org.nutz.mvc.annotation.At;
 import org.nutz.mvc.annotation.Param;
 
 import com.laivi.sic.model.exception.ErrorException;
 import com.laivi.sic.model.json.JsonItem;
 import com.laivi.sic.model.json.JsonList;
 import com.laivi.sic.model.po.basic.IBasicDBEntity;
 import com.laivi.sic.model.to.Response;
 import com.laivi.sic.model.type.IMOType;
 import com.laivi.sic.service.basic.BasicDBService;
 import com.laivi.sic.util.basic.DataUtil;
 
 
 public abstract class ABasicDBAction<T extends IBasicDBEntity> extends ABasicAction implements IBasicDBAction<T> {
 	protected static final Log log=Logs.get();
 	
 	@Inject
 	protected Dao dao;
 
 	protected Condition cnd=null;
 	
 	@Inject("refer:basicDBService")
 	protected BasicDBService basicService;
 	
 	@At
	public Object search(@Param("::page.")Pager page,String key,String value){
 		return list(page,Cnd.where(key, "LIKE", "%"+this.encodeString(value)+"%"));
 	}
 
 	@Override
 	@At
 	public Response delete(long id){
 		dao.delete(this.getEntityClass(), id);
 		return success();
 	}
 	
 	@At
 	public Response remove(long id)throws Exception{
 		T obj=dao.fetch(this.getEntityClass(), id);
 		obj.setDeleteIs(true);
 		dao.update(obj);
 		return success();
 	}
 
 	@Override
 	@At
 	public Response deletes(String ids) {
 		dao.clear(this.getEntityClass(), Cnd.wrap("id in ("+ids+")"));
 		return success();
 	}
 	
 	protected SqlExpressionGroup getBasicCnd(){
 		if(this.isSys()){
 			return Cnd.exps("deleteIs","=", false);
 		}else{
 			return Cnd.exps("deleteIs","=", false).and("userId","=",this.getUserId());
 		}
 	}
 	
 	@At
 	public Object getAll(@Param("::page.")Pager page){
 		return list(page,Cnd.where("deleteIs","=", false).desc("createDate"));
 	}
 
 
 	@Override
 	@At
 	public Object list(@Param("::page.")Pager page,boolean fold) {
 		return list(page,Cnd.where(getBasicCnd()).desc("createDate"),fold);
 	}
 	
 	protected JsonList list(Pager page,Condition cnd){
 		return list(page,cnd,false);
 	}
 	
 	protected JsonList list(Pager page,Condition cnd,boolean unFold){
 		return this.list(page, getEntityClass(), cnd, unFold);
 	}
 	
 	protected JsonList list(Pager page,String sql,String count) throws Exception{
 		return this.list(page, getEntityClass(), sql, count);
 	}
 	
 	
 
 	@Override
 	@At
 	public Object get(long id,boolean fold) {
 		return this.getJsonItem(dao.fetch(this.getEntityClass(), id), fold).toJsonForm();
 	}
 	
 	protected void updateValue(T srcObj,T destObj){
 		for(Field field:srcObj.getClass().getDeclaredFields()){
 			boolean accessFlag = field.isAccessible();
 			field.setAccessible(true);
 			Field dstField=null;
 			boolean dstAccessFlag=false;
 			try {
 				dstField=destObj.getClass().getDeclaredField(field.getName());
 				dstAccessFlag=dstField.isAccessible();
 				dstField.setAccessible(true);
 				if(field.get(srcObj)!=null){
 					dstField.set(destObj, field.get(srcObj));
 				}
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (NoSuchFieldException e) {
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			}finally{
 				dstField.setAccessible(dstAccessFlag);
 				field.setAccessible(accessFlag);
 			}
 		}
 	}
 	
 
 	@Override
 	public JsonItem getJsonItem(T obj,boolean fold) {
 		return getJsonItem(this.getEntityClass(),obj,fold);
 	}
 	
 	@Override
 	public JsonItem getJsonItem(Class<?> klass, Object obj, boolean fold) {
 		JsonItem item=new JsonItem();
 		Field[] fields=DataUtil.appendArray(klass.getSuperclass().getDeclaredFields(), klass.getSuperclass().getSuperclass().getDeclaredFields());
 		for(Field field:DataUtil.appendArray(klass.getDeclaredFields(), fields)){
 			boolean accessFlag = field.isAccessible();
 			field.setAccessible(true);
 			String fieldName=field.getName();
 			try {
 				if(fieldName.indexOf("Id")!=-1){
 					IMOType imo=IMOType.fromId(fieldName);
 					if(imo!=null){
 						item.add(fieldName.replaceAll("Id",""), dao.fetch(imo.getKlass(), (Long)field.get(obj)));
 					}else{
 						item.add(field.getName(),field.get(obj));
 					}
 					
 				}else if(fold){
 					boolean isMatch=false;
 					String[] keys={"content","answer"};
 					for(String key:keys){
 						if(key.equals(field.getName())){
 							isMatch=true;
 							item.add(field.getName(),DataUtil.getDefaultChar((String)field.get(obj)));
 							break;
 						}
 					}
 					if(!isMatch){
 						item.add(field.getName(),field.get(obj));
 					}
 				}else{
 					item.add(field.getName(),field.get(obj));
 				}
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (ErrorException e) {
 				e.printStackTrace();
 			}finally{
 				field.setAccessible(accessFlag);
 			}
 		}
 		return item;
 	}
 
 	@Override
 	public JsonList list(Pager page, Class<?> klass, Condition cnd, boolean unFold) {
 		JsonList jsonList=new JsonList();
 		for(Object obj:dao.query(this.getEntityClass(), cnd, page)){
 			jsonList.add(this.getJsonItem(klass,obj,!unFold));
 		}
 		jsonList.setTotalProperty(dao.count(this.getEntityClass(), cnd));
 		return jsonList;
 	}
 
 	@Override
 	public JsonList list(Pager page, Class<?> klass, String sql, String count)
 			throws Exception {
 		JsonList jsonList=new JsonList();
 		for(Object obj:basicService.list(klass, sql, page)){
 			jsonList.add(this.getJsonItem(klass,obj,true));
 		}
 		jsonList.setTotalProperty(basicService.getCount(klass, count));
 		return jsonList;
 	}
 	
 	
 }
