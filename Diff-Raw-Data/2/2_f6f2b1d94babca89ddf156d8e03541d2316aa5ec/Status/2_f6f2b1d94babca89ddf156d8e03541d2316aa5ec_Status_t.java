 package org.integratedmodelling.thinklab.client.commands;
 
 import java.util.Date;
 
 import org.integratedmodelling.thinklab.client.RemoteCommandHandler;
 import org.integratedmodelling.thinklab.client.Result;
 import org.integratedmodelling.thinklab.client.Session;
 import org.integratedmodelling.thinklab.client.annotations.Command;
 import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
 import org.integratedmodelling.thinklab.client.shell.CommandLine;
 
 import uk.co.flamingpenguin.jewel.cli.Option;
 
 @Command(id="status")
 public class Status extends RemoteCommandHandler {
 
 	public interface Args extends Arguments {
 		
 		@Option
 		public int getLog();
 		public boolean isLog();
 	}	
 	
 	@Override
 	public Class<? extends Arguments> getArgumentsClass() {
 		return Args.class;
 	}
 
 	@Override
 	public Result runRemote(Arguments arguments, Session session, CommandLine cl)
 			throws ThinklabClientException {
 		
 		Result ret = session.send("", false);
 		Args args = (Args)arguments;
 		
 		/*
 		 * report all vars
 		 */
 		Date up = new Date(Long.parseLong(ret.get("boot.time").toString()));
 		Date lt = new Date(Long.parseLong(ret.get("current.time").toString()));
 		
 		long mtot = Long.parseLong(ret.get("memory.total").toString())/(1024L*1024L);
 		long mfre = Long.parseLong(ret.get("memory.free").toString())/(1024L*1024L);
 		long mmax = Long.parseLong(ret.get("memory.max").toString())/(1024L*1024L);
 		
 		cl.say("   Server version " +
 				ret.get("thinklab.version") + 
 				" [" + ret.get("thinklab.branch") + "]");
 		
 		cl.say("   Server time: " + lt);
 		cl.say("   Up since: " + up);
 		
 		cl.say("   CPUs: " + ret.get("processors"));
 		
 		cl.say("   " + mfre + "M free memory out of " + mtot  + 
 				"M (max " + mmax + "M)");
 		
 		if (args.isLog()) {
 			
 			/*
 			 * ask for log and print it if successful
 			 */
 			 Result rlog = session.send("log", false, "lines", args.getLog()+"");
 			 
 			 if (rlog.getStatus() == Result.OK) {
				 cl.say(rlog.getResult().toString());
 			 }
 			 
 			 
 		}
 		
 		return ret;
 	}
 
 }
