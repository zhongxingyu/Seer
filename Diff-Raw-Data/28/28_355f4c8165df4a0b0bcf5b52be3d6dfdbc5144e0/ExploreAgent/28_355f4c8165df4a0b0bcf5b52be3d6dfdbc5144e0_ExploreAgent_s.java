 package agent;
 
 import jadex.bridge.Argument;
 import jadex.bridge.IArgument;
 import jadex.bridge.IComponentStep;
 import jadex.bridge.IInternalAccess;
 import jadex.micro.MicroAgent;
 import jadex.micro.MicroAgentMetaInfo;
 
 public class ExploreAgent extends MicroAgent
 {
   public void executeBody()
   {
     System.out.println(getArgument("welcome text"));
    waitFor(2000, new Runnable()
     {			
      public void run()
      {
        System.out.println("Good bye world.");
        killAgent();
      }
     });
   }
	
   public static MicroAgentMetaInfo getMetaInfo()
   {
    return new MicroAgentMetaInfo("This agent prints out a hello message.", 
      new String[]{"c1", "c2"}, 
      new IArgument[]
      {
        new Argument("welcome text", "This parameter is the text printed by the agent.", 
        "String", "Hello world, this is a Jadex micro agent."),	
      }, null, null);
   }
 }
