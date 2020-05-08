 package com.gushuley.utils.scheduler.dom;
 
 import com.gushuley.utils.Tools;
 import com.gushuley.utils.orm.impl.GenericContext;
 
 public class SchedulerContext extends GenericContext {
 	public SchedulerContext(String scheduleDbJdni, String scheduler, String dbScheme) {
 		this.scheduleDbJdni = scheduleDbJdni;
 		this.scheduler = scheduler;
 		this.dbScheme = dbScheme;
 		
 		registerMapper(JobDone.class, JobDoneMapper.class);
 		registerMapper(JobDef.class, JobDefMapper.class);
 	}
 
 	private final String scheduleDbJdni;
 	private final String scheduler;
 	private final String dbScheme;
 
 	public String getDbScheme() {
 		if (Tools.isEmpty(dbScheme)) {
			return dbScheme;
 		} else if (!dbScheme.trim().endsWith(".")) {
 			return dbScheme + ".";
 		} else {
 			return dbScheme;
 		}
 	}
 
 	public String getScheduler() {
 		return scheduler;
 	}
 
 	public String getScheduleDbJdni() {
 		return scheduleDbJdni;
 	} 
 }
