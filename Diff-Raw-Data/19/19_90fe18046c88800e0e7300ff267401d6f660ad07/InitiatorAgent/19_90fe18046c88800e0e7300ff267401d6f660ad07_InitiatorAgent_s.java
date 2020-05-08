 package pikater.agents.management;
 
 import jade.core.behaviours.TickerBehaviour;
 import jade.wrapper.AgentController;
 import jade.wrapper.ControllerException;
 import jade.wrapper.PlatformController;
 import pikater.agents.PikaterAgent;
 import pikater.configuration.AgentConfiguration;
 import pikater.configuration.Argument;
 import pikater.configuration.Configuration;
 import pikater.configuration.ConfigurationProvider;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 public class InitiatorAgent extends PikaterAgent {
 	
 	private static final long serialVersionUID = -3908734088006529947L;
 
 	@Override
 	protected void setup() {
 		initDefault();
 		registerWithDF();
 		
 		// read agents from configuration
 		try {
 			/* Sets up a configuration provider via spring */
             ConfigurationProvider configProvider= (ConfigurationProvider) context.getBean("configuration");
             Configuration configuration=configProvider.getConfiguration();
             List<AgentConfiguration> agentConfigurations=configuration.getAgentConfigurations();
             for (AgentConfiguration agentConfiguration : agentConfigurations)
             {
                //Preimplemented ajde agents do not count with named arguments, convert to string if necessary
                 Object[] arguments=ProcessArgs(agentConfiguration.getArguments().toArray());
                 Boolean creationSuccessful=this.CreateAgent(agentConfiguration.getAgentType(),agentConfiguration.getAgentName(),arguments);
                 if (!creationSuccessful)
                 {
                     logError("Creation of agent "+agentConfiguration.getAgentName()+" failed.");
                 }
             }
 		}
         catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		addBehaviour(new TickerBehaviour(this, 60000) {
 		  Calendar cal;
 		  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			  
 		  protected void onTick() {
 			  cal = Calendar.getInstance();
 			  System.out.println(myAgent.getLocalName()+": tick="+getTickCount()+" time="+sdf.format(cal.getTime()));
 		  }
 		});
 	}
 
 	public Boolean CreateAgent(String type, String name, Object[] args) {
 		// get a container controller for creating new agents
 		PlatformController container = getContainerController();
 		try {
 			AgentController agent = container.createNewAgent(name, type, args);
 			agent.start();
 			// provide agent time to register with DF etc.
 			doWait(300);
 		} catch (ControllerException e) {
 			System.err.println("Exception while adding agent: " + e);
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
     public  Object[] ProcessArgs(Object[] args)
     {
          Object[] toReturn=new Object[args.length];
          for (int i=0;i<args.length;i++)
          {
                Argument arg=(Argument)args[i];
               if (arg.getSendOnlyValue())
               {
                   toReturn[i]=arg.getValue();
               }
              else
               {
                   toReturn[i]=args[i];
               }
          }
         return  toReturn;
     }
 }
