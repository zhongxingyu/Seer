 package com.tomtom.amelinium.web.config;
 
 import org.apache.ibatis.session.SqlSessionFactory;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 import org.springframework.web.servlet.view.JstlView;
 
 import com.tomtom.amelinium.db.logic.BacklogDbLogic;
 import com.tomtom.amelinium.db.logic.ChartDbLogic;
 import com.tomtom.amelinium.db.logic.Initializer;
 import com.tomtom.amelinium.db.logic.ProjectDbLogic;
 import com.tomtom.amelinium.projectservice.corrector.ProjectCorrector;
 import com.tomtom.amelinium.projectservice.factory.ProjectServiceFactory;
 import com.tomtom.amelinium.web.standalone.controller.BacklogController;
 import com.tomtom.amelinium.web.standalone.controller.ChartController;
 import com.tomtom.amelinium.web.standalone.controller.NewProjectController;
 import com.tomtom.amelinium.web.standalone.controller.ProjectsController;
 import com.tomtom.amelinium.web.standalone.helper.HistoryHelper;
 
 @Configuration
 public class AppConfig {
 
 	@Bean
 	public ViewResolver viewResolver() {
 		final InternalResourceViewResolver result = new InternalResourceViewResolver();
 		result.setViewClass(JstlView.class);
 		result.setPrefix("/WEB-INF/jsp/");
 		result.setSuffix(".jsp");
 		return result;
 	}
 	
 	@Bean
 	public SqlSessionFactory sqlSessionFactory(){
 		Initializer initializer = new Initializer();
 		return initializer.initializeSqlMapper();
 	}
 
//	@Bean
//	public MainController uploadController() {
//		MainController mainController = new MainController();
//		return mainController;
//	}

 	@Bean
 	public ProjectsController projectsController() {
 		ProjectsController projectsController = new ProjectsController();
 		return projectsController;
 	}
 
 	@Bean
 	public NewProjectController newProjectController() {
 		NewProjectController newProjectController = new NewProjectController();
 		return newProjectController;
 	}
 
 	@Bean
 	public BacklogController backlogController() {
 		BacklogController backlogController = new BacklogController();
 		return backlogController;
 	}
 
 	@Bean
 	public ChartController chartController() {
 		ChartController chartController = new ChartController();
 		return chartController;
 	}
//
//	@Bean
//	public BacklogPageCorrector backlogPageCorrector() {
//		BacklogPageCorrector backlogPageCorrector = new BacklogPageCorrector();
//		return backlogPageCorrector;
//	}
//
//	@Bean
//	public ChartPageCorrector chartPageCorrector() {
//		ChartPageCorrector chartPageCorrector = new ChartPageCorrector();
//		return chartPageCorrector;
//	}
	
 	@Bean BacklogDbLogic backlogDbLogic(){
 		BacklogDbLogic backlogDbLogic = new BacklogDbLogic(sqlSessionFactory());
 		return backlogDbLogic;
 	}
 	
 	@Bean ChartDbLogic chartDbLogic(){
 		ChartDbLogic chartDbLogic = new ChartDbLogic(sqlSessionFactory());
 		return chartDbLogic;
 	}
 	
 	@Bean ProjectDbLogic projectDbLogic(){
 		ProjectDbLogic projectDbLogic = new ProjectDbLogic(sqlSessionFactory());
 		return projectDbLogic;
 	}
 	@Bean ProjectCorrector projectCorrector(){
 		ProjectCorrector projectCorrector = new ProjectCorrector(sqlSessionFactory());
 		return projectCorrector;
 	}
 	@Bean ProjectServiceFactory projectServiceFactory(){
 		ProjectServiceFactory projectServiceFactory = new ProjectServiceFactory(sqlSessionFactory());
 		return projectServiceFactory;
 	}
 	@Bean HistoryHelper historyHelper(){
 		HistoryHelper historyHelper = new HistoryHelper(sqlSessionFactory());
 		return historyHelper;
 	}
 }
