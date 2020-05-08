 package com.nbcedu.function.schoolmaster2.action;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.commons.lang.xwork.StringUtils;
 import org.apache.struts2.ServletActionContext;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.reflect.TypeToken;
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
 	
 	private SM2MasterSubBiz masterSubBiz;
 	
 	public void findLinshi(){
 		
		String uid = Utils.getAllSchoolMaster().
 			contains(this.getUserId())?null:getUserId();
 		//url,progress,title
 		final List<TSm2Subject> subList=
 			this.masterSubBiz.findByMasterAndCount(LINSHI_MODULEID, uid,6);
 		
 		SearchFunction linshi = new SearchFunction() {
 			@Override
 			public String search() {
 				return Utils.gson.toJson(
 						Lists.transform(subList, new Function<TSm2Subject, Linshi>() {
 							@Override
 							public Linshi apply(TSm2Subject input) {
 								Linshi result = new Linshi();
 								result.setTitle(input.getTitle());
 								result.setUrl("/scMaster2/detail_master.action?id=" + input.getId());
 								result.setProgress(String.valueOf(input.getProgress()));
 								return result;
 							}
 						}),
 						new TypeToken<List<Linshi>>() {}.getType()
 				);
 			}
 			@Override
 			public String getId() {
 				return LINSHI_MODULEID;
 			}
 		};
 		
 		Struts2Utils.renderJson(findData(linshi));
 	}
 	
 	class Linshi {
 		private String url;
 		private String progress;
 		private String title;
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
