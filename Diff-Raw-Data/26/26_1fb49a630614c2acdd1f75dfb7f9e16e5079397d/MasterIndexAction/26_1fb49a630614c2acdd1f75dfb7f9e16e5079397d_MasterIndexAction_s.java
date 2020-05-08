 package com.nbcedu.function.schoolmaster2.action;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.commons.lang.xwork.StringUtils;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.reflect.TypeToken;
 import com.google.gson.JsonObject;
 import com.nbcedu.function.schoolmaster2.biz.SM2MasterSubBiz;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
 import com.nbcedu.function.schoolmaster2.core.util.struts2.Struts2Utils;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Subject;
 import com.nbcedu.function.schoolmaster2.utils.Utils;
 
 
 @SuppressWarnings("serial")
 public class MasterIndexAction extends BaseAction{
 
 	private static volatile Map<String, String> CACHE = new HashMap<String, String>();
 	private static final Timer timer = new Timer();
 	private static final String LINSHI_MODULEID = "linshishixiang";
 	private static final String TONGJI_MODULEID = "tjfx" ;
 	
 	private SM2MasterSubBiz masterSubBiz;
 	
 	/**
 	 * 临时事项
 	 * @author xuechong
 	 */
 	public void findLinshi(){
 		
 		final String uid = this.getUserId();
 		
 		SearchFunction linshi = new SearchFunction() {
 			@Override
 			public String search() {
 				
 				List<TSm2Subject> subList=
 					masterSubBiz.findByMasterAndCount(LINSHI_MODULEID, uid,6);
 				
 				return Utils.gson.toJson(
 						Lists.transform(subList, new Function<TSm2Subject, Linshi>() {
 							@Override
 							public Linshi apply(TSm2Subject input) {
 								Linshi result = new Linshi();
 								result.setTitle(input.getTitle());
 								result.setUrl("/scMaster2/detail_master.action?id=" + input.getId());
 								result.setProgress(String.valueOf(input.getProgress()));
 								result.setStatu(input.getStatus());
 								return result;
 							}
 						}),
 						new TypeToken<List<Linshi>>() {}.getType()
 				);
 			}
 			@Override
 			public String getId() {return LINSHI_MODULEID + uid;}
 		};
 		
 		executeFind(linshi);
 	}
 
 	
 	/**
 	 * 学校动态
 	 * @author xuechong
 	 */
 	public void findDongtai(){
 		
 		final String uid = this.getUserId();
 		
 		SearchFunction dongtai = new SearchFunction(){
 			@Override
 			public String search() {
 				
 				Map<String, Integer> results = 
 					masterSubBiz.findNewCountByModule(uid);
 				
 				return countMapToJson(results);
 			}
 			@Override
 			public String getId() {
 				return "find_dongtai" + uid;
 			}
 		};
 		
 		executeFind(dongtai);
 	}
 
 	
 	/**
 	 * 重心工作
 	 * @author xuechong
 	 */
 	public void findZhongxin(){
 		
 		final String uid = this.getUserId();
 		
 		SearchFunction zhongxin = new SearchFunction(){
 			@Override
 			public String getId() {
 				return "find_zhongxin" + uid;
 			}
 			@Override
 			public String search() {
 				Map<String, Integer> results = 
 					masterSubBiz.findAttCountByModType("nianduzhongxin", uid);
 				return countMapToJson(results);
 			}
 		};
 		
 		executeFind(zhongxin);
 	}
 
 	
 	public void findTongjifenxi(){
 		
 		final String uid = this.getUserId();
 		
 		SearchFunction tongji = new SearchFunction() {
 			@Override
 			@SuppressWarnings("unchecked")
 			public String search() {
 				
 				List<TSm2Subject> subList=
 					masterSubBiz.findByMasterAndCount(TONGJI_MODULEID, uid,4);
 				
				test:{
					subList = new ArrayList<TSm2Subject>();
					for (int i = 0; i < 4; i++) {
						TSm2Subject sub = new TSm2Subject();
						sub.setTitle("title" + i);
						sub.setId("id" + i);
						subList.add(sub);
					}
					
				}
 				
 				final Iterator<String> color = shuffle(Arrays.asList("#F79263","#6FB3E0","#F76382","#846FE0")).iterator();
 				
 				final Iterator<String> icon = shuffle(Arrays.asList("tj01","tj02","tj03","tj04")).iterator();
 				
 				return Utils.gson.toJson(
 						Lists.transform(subList, new Function<TSm2Subject, Tongji>() {
 							@Override
 							public Tongji apply(TSm2Subject input) {
 								Tongji result = new Tongji();
 								result.setTitle(input.getTitle());
 								result.setUrl("/scMaster2/detail_master.action?id=" + input.getId());
 								result.setColor(color.next());
 								result.setIcon(icon.next());
 								return result;
 							}
 						}),
 						new TypeToken<List<Tongji>>() {}.getType()
 				);
 			}
 			@Override
 			public String getId() {return TONGJI_MODULEID + uid;}
 		};
 		
 		executeFind(tongji);
 		
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private List shuffle(List origin){
 		Collections.shuffle(origin);
 		return origin;
 	}
 	/**
 	 * 执行查询
 	 * @param function
 	 * @author xuechong
 	 */
 	private void executeFind(SearchFunction function){
 		Struts2Utils.renderJson(findData(function));
 	}
 	
 	/**
 	 * 统计分析B部分展示
 	 * @author xuechong
 	 */
 	class Tongji{
 		private String url;
 		private String title;
 		private String icon;
 		private String color;
 		///////////////////////
 		////getters&setters/////
 		////////////////////////
 		public String getUrl() {
 			return url;
 		}
 		public void setUrl(String url) {
 			this.url = url;
 		}
 		public String getTitle() {
 			return title;
 		}
 		public void setTitle(String title) {
 			this.title = title;
 		}
 		public String getIcon() {
 			return icon;
 		}
 		public void setIcon(String icon) {
 			this.icon = icon;
 		}
 		public String getColor() {
 			return color;
 		}
 		public void setColor(String color) {
 			this.color = color;
 		}
 		
 	}
 	
 	/**
 	 * 临时事项展示
 	 * @author xuechong
 	 */
 	class Linshi {
 		
 		private String url;
 		private String progress;
 		private String title;
 		private String statu;
 		///////////////////////////
 		////getters&setters////////
 		/////////////////////////////
 		public String getUrl() {
 			return url;
 		}
 		public void setUrl(String url) {
 			this.url = url;
 		}
 		public String getProgress() {
 			return progress;
 		}
 		public void setProgress(String progress) {
 			this.progress = progress;
 		}
 		public String getTitle() {
 			return title;
 		}
 		public void setTitle(String title) {
 			this.title = title;
 		}
 		public String getStatu() {
 			return statu;
 		}
 		public void setStatu(String statu) {
 			this.statu = statu;
 		}
 		
 	}
 	
 	
 	private static String countMapToJson(Map<String,Integer> input ){
 		JsonObject json = new JsonObject();
 		for (Map.Entry<String, Integer> entry : input.entrySet()) {
 			json.addProperty(entry.getKey(),entry.getValue());
 		}
 		return json.toString();
 	}
 	
 	
 	private String findData(SearchFunction function){
 		if(function!=null){
 			String result = CACHE.get(function.getId());
 			if(StringUtils.isBlank(result)){
 				result = function.doSearch();
 				CACHE.put(function.getId(), result);
 			}
 			return result;
 		}
 		return "";
 	}
 	
 	private abstract class SearchFunction{
 		/**
 		 * 返回前台需要的json数据
 		 * @return
 		 * @author xuechong
 		 */
 		public abstract String search();
 		
 		/**
 		 * 返回放在缓存中的KEY
 		 * @return
 		 * @author xuechong
 		 */
 		public abstract String getId();
 		
 		public final String doSearch(){
 			timeToDie(this.getId());
 			return this.search();
 		}
 	}
 	
 	private static void timeToDie(final String id){
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				CACHE.remove(id);
 			}
 		}, 1000L*60L*2L);
 	}
 
 	////////////////////////
 	////getters&setters/////
 	////////////////////////
 	public void setMasterSubBiz(SM2MasterSubBiz masterSubBiz) {
 		this.masterSubBiz = masterSubBiz;
 	}
 }
