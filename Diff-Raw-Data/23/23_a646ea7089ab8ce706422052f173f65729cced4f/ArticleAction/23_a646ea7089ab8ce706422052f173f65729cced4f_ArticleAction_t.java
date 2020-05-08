 package com.laivi.sic.action.blog;
 
 import org.nutz.dao.Cnd;
 import org.nutz.dao.Condition;
 import org.nutz.dao.pager.Pager;
 import org.nutz.ioc.loader.annotation.Inject;
 import org.nutz.ioc.loader.annotation.IocBean;
 import org.nutz.mvc.annotation.At;
 import org.nutz.mvc.annotation.Param;
 
 import com.laivi.sic.action.basic.ABasicDBAction;
 import com.laivi.sic.model.annotation.CheckLogin;
 import com.laivi.sic.model.annotation.CheckValue;
 import com.laivi.sic.model.json.JsonItem;
 import com.laivi.sic.model.json.JsonList;
 import com.laivi.sic.model.po.blog.Article;
 import com.laivi.sic.model.po.common.FromOther;
 import com.laivi.sic.model.po.common.SimpleDegree;
 import com.laivi.sic.model.to.Response;
 import com.laivi.sic.model.type.CategoryType;
 import com.laivi.sic.service.blog.ArticleService;
 import com.laivi.sic.util.basic.DataUtil;
 
 @At("/blog/article")
 @IocBean
 public class ArticleAction extends ABasicDBAction<Article> {
 	
 	@Inject
 	private ArticleService articleService;
 	
 	@At
 	@CheckValue
 	@CheckLogin
 	public Response add(@Param("::article.")final Article article){
 		article.setUserId(this.getLoginUser().getUserId());
 		articleService.add(article);
 		return success();
 	}
 	
 	@At
 	public Response delete(long id){
 		dao.delete(FromOther.class, id);
 		return success();
 	}
 	
 	@At
 	@CheckValue
 	public Response update(@Param("::article.")Article article){
 		Article dArticle=dao.fetch(this.getEntityClass(), article.getId());
 		dArticle.setContent(article.getContent());
 		dArticle.setTitle(article.getTitle());
 		dArticle.setTagId(article.getTagId());
 		//this.updateValue(article, dArticle);
 		dao.update(dArticle);
 		return success();
 	}
 	
 	@At
 	public Response addViewCount(long id){
 		Article article=dao.fetch(this.getEntityClass(), id);
 		article.setViewCount(article.getViewCount()+1);
 		dao.update(article);
 		return success();
 	}
 	
 	
 	@At
 	public Object listAllTitle(){
 		JsonList jsonList=new JsonList();
 		this.cnd=this.getUserCnd();
 		for(Article article:dao.query(this.getEntityClass(), cnd)){
 			jsonList.add("\""+article.getTitle()+"\"");
 		}
 		return jsonList;
 	}
 	
 	@Override
 	@At
 	public Object getAll(@Param("::page.")Pager page){
 		return list(page,Cnd.where("deleteIs","=", false).and("type", "=",CategoryType.ARTICLE).and("selfIs", "=", true).desc("createDate"));
 	}
 	
 	@At
 	public Object getProposal(long id){
 		JsonList jsonList=new JsonList();
 		SimpleDegree simple=dao.fetch(SimpleDegree.class, Cnd.where("objId", "=", id).and("type", "=", CategoryType.ARTICLE));
 		if(simple!=null){
 			for(long aId:DataUtil.changeIdString(simple.getSimpleIds())){
 				if(aId!=0){
 					jsonList.add(this.getJsonItem(dao.fetch(Article.class, aId), true));
 				}
 			}
 		}
 		
 		jsonList.setSize();
 		return jsonList;
 	}
 	
 	@At
	public Object search(@Param("::page.")Pager page,String key,String value) throws Exception{
		value=this.encodeString(value);
		String where="where f.objId=a.id and f.type='ARTICLE' and a."+key+" like '%"+value+"%' order by f.id desc";
		String sql="select f.* from sic_fromother f,sic_article a "+where;
		JsonList jsonList=new JsonList();
		for(FromOther obj:basicService.list(FromOther.class, sql, page)){
			JsonItem item=this.getJsonItem(FromOther.class,obj,true);
			item.add("article", this.getJsonItem(dao.fetch(Article.class, obj.getObjId()), true));
			jsonList.add(item);
		}
		sql="select count(*) from sic_fromother f,sic_article a "+where;
		jsonList.setTotalProperty(basicService.getCount(FromOther.class, sql));
		return jsonList;
	}
	
	@At
 	public Object listByTag(@Param("::page.")Pager page,long tagId) throws Exception{
 		String sql="select f.* from sic_fromother f,sic_article a where f.objId=a.id and f.type='ARTICLE' and a.tagId="+tagId+" order by createDate desc";
 		JsonList jsonList=new JsonList();
 		for(FromOther obj:basicService.list(FromOther.class, sql, page)){
 			JsonItem item=this.getJsonItem(FromOther.class,obj,true);
 			item.add("article", this.getJsonItem(dao.fetch(Article.class, obj.getObjId()), true));
 			jsonList.add(item);
 		}
 		sql="select count(*) from sic_fromother f,sic_article a where f.objId=a.id and f.type='ARTICLE' and a.tagId="+tagId;
 		jsonList.setTotalProperty(basicService.getCount(FromOther.class, sql));
 		return jsonList;
 	}
 
 	@Override
 	@At
 	public Object list(@Param("::page.")Pager page,boolean fold) {
 		Cnd condition=Cnd.where(this.getBasicCnd()).and("type", "=",CategoryType.ARTICLE);
 		if(this.isSys()){
 			this.cnd= condition.and("selfIs", "=", true).desc("createDate");
 		}else{
 			this.cnd= condition.desc("createDate");
 		}
 		return list(page,cnd);
 	}
 
 	@Override
 	protected JsonList list(Pager page, Condition cnd) {
 		JsonList jsonList=new JsonList();
 		for(FromOther obj:dao.query(FromOther.class,cnd,page)){
 			JsonItem item=this.getJsonItem(FromOther.class,obj,true);
 			item.add("article", this.getJsonItem(dao.fetch(Article.class, obj.getObjId()), true));
 			jsonList.add(item);
 		}
 		jsonList.setTotalProperty(dao.count(FromOther.class, cnd));
 		return jsonList;
 	}
 
 	@Override
 	public Class<Article> getEntityClass() {
 		return Article.class;
 	}
 
 }
