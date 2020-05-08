 package com.tojc.logging.android.settings;
 
 import android.content.ComponentName;
 import android.content.Context;
 
 import com.tojc.logging.android.internal.Interface.LoggerModel;
 import com.tojc.logging.android.internal.Interface.LoggerSettingsPublicInterface;
 import com.tojc.logging.android.internal.model.LoggerModelType;
 import com.tojc.logging.android.provider.LoggerProvider;
 
 public class LoggerSettings implements LoggerSettingsPublicInterface
 {
 	private Context context;
     private boolean loggerEnabled;
     private boolean logcatEnabled;
 	private String packageName;
 	private LoggerModelType loggerModelType;
 	private LoggerModel targetLoggerModel = null;
 
     private ContentProviderSettings contentProviderSettings;
 
     public LoggerSettings()
     {
     	this.context = null;
     	this.loggerEnabled = true;
     	this.logcatEnabled = true;
     	this.packageName = "";
     	this.loggerModelType = LoggerModelType.OriginalModel;
     	this.targetLoggerModel = null;
     	this.contentProviderSettings = null;
     }
 
 	@Override
 	public void initialize(Context context)
 	{
 		if(this.context == null)
 		{
 			this.context = context;
 	    	this.contentProviderSettings = new ContentProviderSettings(context, new ComponentName(context, LoggerProvider.class));
 		}
	}
 
	@Override
	public void initialize(Context context, LoggerModelType loggerModelType)
	{
		this.initialize(context);
		this.setLoggerModelType(loggerModelType);
 	}
 
 	@Override
 	public void initialize(Context context, LoggerModel loggerModel)
 	{
 		this.initialize(context, LoggerModelType.OriginalModel);
 		this.setLoggerModel(loggerModel);
 	}
 
 	public Context getContext()
 	{
 		return this.context;
 	}
 
     @Override
 	public LoggerModelType getLoggerModelType()
 	{
 		return this.loggerModelType;
 	}
     @Override
 	public void setLoggerModelType(LoggerModelType loggerModelType)
 	{
     	this.loggerModelType = loggerModelType;

		for(LoggerModelType type : LoggerModelType.values())
		{
			this.setLoggerModel(type.getInstance());
		}
 	}
 
     @Override
     public LoggerModel getLoggerModel()
 	{
 		return this.targetLoggerModel;
 	}
     @Override
 	public void setLoggerModel(LoggerModel loggerModel)
 	{
 		if(loggerModel != null)
 		{
 			this.targetLoggerModel = loggerModel;
 	    	this.targetLoggerModel.initialize(this.context, this);
 		}
 	}
 
 	@Override
 	public boolean isLoggerEnabled()
 	{
 		return this.loggerEnabled;
 	}
 	@Override
 	public void setLoggerEnabled(boolean loggerEnabled)
 	{
 		this.loggerEnabled = loggerEnabled;
 	}
 
 	@Override
 	public boolean isLogcatEnabled()
 	{
 		return this.logcatEnabled;
 	}
 	@Override
 	public void setLogcatEnabled(boolean logcatEnabled)
 	{
 		this.logcatEnabled = logcatEnabled;
 	}
 
 	public String getPackageName()
 	{
 		return this.packageName;
 	}
 	@Override
     public void setPackageName(String packageName)
 	{
     	this.packageName = packageName;
 	}
 
 	@Override
 	public ContentProviderSettings getContentProviderSettings()
 	{
 		return this.contentProviderSettings;
 	}
 }
