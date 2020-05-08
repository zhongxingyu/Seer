 package com.guotingchao;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import com.alibaba.druid.filter.stat.StatFilter;
 import com.alibaba.druid.wall.WallFilter;
 import com.guotingchao.MyPlugin.RedisPlugin;
 import com.guotingchao.handler.RenderTimeHandle;
 import com.guotingchao.model.impl.Branch;
 import com.guotingchao.model.impl.T_user_task;
 import com.guotingchao.model.impl.Task;
 import com.guotingchao.model.impl.User;
 import com.guotingchao.route.admin.AdminRoutes;
 import com.guotingchao.route.front.ForntRoutes;
 import com.jfinal.config.Constants;
 import com.jfinal.config.Handlers;
 import com.jfinal.config.Interceptors;
 import com.jfinal.config.JFinalConfig;
 import com.jfinal.config.Plugins;
 import com.jfinal.config.Routes;
 import com.jfinal.log.Log4jLogger;
 import com.jfinal.log.Logger;
 import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
 import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
 import com.jfinal.plugin.druid.DruidPlugin;
 import com.jfinal.render.JspRender;
 import com.jfinal.render.ViewType;
 
 public class BaseConfig extends JFinalConfig {
 	public static Logger log  = Log4jLogger.getLogger(BaseConfig.class);
 
 	static Properties prop = new Properties();
 	static{
 		InputStream in = BaseConfig.class.getClassLoader().getResourceAsStream("config.properties");
 		try{
 			prop.load(in);
 		}catch(IOException e){
 			log.error(e.getMessage());
 		}
 	}
 	@Override
 	public void configConstant(Constants me) {
 		me.setDevMode(true);
 		me.setViewType(ViewType.JSP);
 		me.setEncoding("UTF-8");
 		me.setError404View("/404.html");
 		me.setBaseViewPath("WEB-INF/fornt");
 		JspRender.setSupportActiveRecord(true);
 	}
 
 	@Override
 	public void configRoute(Routes me) {
 		me.add(new AdminRoutes());
 		me.add(new ForntRoutes());
 	}
 
 	@Override
 	public void configHandler(Handlers me) {
 		//		DruidStatViewHandler dvh = new DruidStatViewHandler("/druid");
 		//		me.add(dvh);
 		me.add(new RenderTimeHandle());
 	}
 	/**
 	 *  添加一个全局的拦截器
 	 */
 	@Override
 	public void configInterceptor(Interceptors me) {
 		
 	}
 
 	@Override
 	public void configPlugin(Plugins me) {
 		DruidPlugin dp = new DruidPlugin(
 				prop.getProperty("dataBase_address"), prop.getProperty("dataBase_user"), prop.getProperty("dataBase_pass"));
 		dp.addFilter(new StatFilter());
 		WallFilter wall = new WallFilter();
 		wall.setDbType("mysql");
 		dp.addFilter(wall);
 		me.add(dp);
 		// ActiveRecordPlugin
 		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
		//设置大小写   小写
		//arp.setContainerFactory(new CaseInsensitiveContainerFactory(true));
 		arp.addMapping("User", User.class);
 		arp.addMapping("Task", Task.class); 
 		arp.addMapping("T_user_task", T_user_task.class);
 		arp.addMapping("Branch", Branch.class);
 		me.add(arp);
 		
 		//RedisPlugin
 		RedisPlugin rp = new RedisPlugin(
 				prop.getProperty("redisHost"), Integer.parseInt(prop.getProperty("redisPort")), Integer.parseInt(prop.getProperty("dbIndex")));
 		me.add(rp);
 
 
 	}
 
 }
