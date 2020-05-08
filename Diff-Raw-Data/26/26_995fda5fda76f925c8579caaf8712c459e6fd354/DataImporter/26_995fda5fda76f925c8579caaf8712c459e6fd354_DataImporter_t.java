 package no.runsafe.essentialsimport;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.timer.Worker;
 
 public abstract class DataImporter extends Worker<String, Boolean> implements IConfigurationChanged
 {
 	public DataImporter(IScheduler scheduler, IConsole output)
 	{
 		super(scheduler);
 		console = output;
 	}
 
 	@Override
 	public void process(String key, Boolean value)
 	{
 		Import();
 	}
 
 	@Override
 	protected void onWorkerDone()
 	{
 		synchronized (sync)
 		{
 			configuration.setConfigValue("imported.".concat(getName()), true);
 			configuration.save();
 		}
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.configuration = configuration;
 		if (!configuration.getConfigValueAsBoolean("imported.".concat(getName())))
 			Push("import", true);
 	}
 
 	abstract String getName();
 
 	abstract void Import();
 
 	protected final IConsole console;
 	private IConfiguration configuration;
 
 	private static final Object sync = new Object();
 }
