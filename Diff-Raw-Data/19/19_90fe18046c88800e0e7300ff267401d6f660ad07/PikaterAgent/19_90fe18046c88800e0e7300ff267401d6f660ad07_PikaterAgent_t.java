 package pikater.agents;
 
 import jade.content.lang.Codec;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.core.Agent;
 import jade.domain.DFService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.FIPAException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import pikater.configuration.Argument;
 import pikater.logging.Logger;
 import pikater.logging.Severity;
 import pikater.logging.Verbosity;
 import pikater.ontology.messages.MessagesOntology;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: Kuba
  * Date: 25.8.13
  * Time: 9:38
  */
 public abstract class PikaterAgent extends Agent {
     private final String DEFAULT_LOGGER_BEAN="logger";
     private final String LOGGER_BEAN_ARG="logger";
     private final String VERBOSITY_ARG="verbosity";
     protected Codec codec = new SLCodec();
     protected Ontology ontology = MessagesOntology.getInstance();
     protected String initBeansName = "Beans.xml";
     protected ApplicationContext context =  new ClassPathXmlApplicationContext(initBeansName);
     protected Verbosity verbosity=Verbosity.NORMAL;
     private Logger logger;
     protected Map<String,Argument> arguments;
 
     public Codec getCodec() {
         return codec;
     }
 
     public Ontology getOntology() {
         return ontology;
     }
 
     protected String getAgentType()
     {
         return  this.getClass().getName();
     }
 
 	protected boolean registerWithDF() {
 		return registerWithDF(new ArrayList<String>());
 	}
 
 	protected boolean registerWithDF(String service) {
 		List<String> st = new ArrayList<>();
 		st.add(service);
 		return registerWithDF(st);
 	}
 	
 	protected boolean registerWithDF(List<String> ServiceTypes) {
 		// register with the DF
 
 		DFAgentDescription description = new DFAgentDescription();
 		// the description is the root description for each agent
 		// and how we prefer to communicate.
 
 		description.setName(getAID());
 		// the service description describes a particular service we
 		// provide.
 		ServiceDescription serviceDescription = new ServiceDescription();
 
 		serviceDescription.setName(getLocalName());
 		String typeDesc = getAgentType();
 		serviceDescription.setType(typeDesc);
 		description.addServices(serviceDescription);
 
 		// add more agent service(s) (typicaly general service type, e.g. Search)
 		
 		for (String st : ServiceTypes) {	
 			ServiceDescription servicedesc_g = new ServiceDescription();
 			servicedesc_g.setName(getLocalName());
 			servicedesc_g.setType(st);
 			description.addServices(servicedesc_g);
 		}
 		
 		// register synchronously registers us with the DF, we may
 		// prefer to do this asynchronously using a behaviour.
 		try {
 			DFService.register(this, description);
 			
 			StringBuilder sb = new StringBuilder("Successfully registered with DF; service types: ");
 	        for (String st: ServiceTypes) {
 	            sb.append(st).append(" ");
 	        }
 	        log(sb.toString());
 			return true;
 			
 		} catch (FIPAException e) {
 			logError("Error registering with DF, :" + e);
 			return false;
 
 		}
 	} // end registerWithDF
 
 	
     public String GetArgumentValue(String argName)
     {
         return arguments.get(argName).getValue();
     }
 
     public Boolean isArgumentValueTrue(String argName)
     {
         String argValue=GetArgumentValue(argName);
         return argValue.equals("1") || argValue.equalsIgnoreCase("true");
     }
 
     public Boolean ContainsArgument(String argName)
     {
         if (arguments==null)
         {
             return false;
         }
         return arguments.containsKey(argName);
     }
 
     private void initLogging()
     {
         String loggerBean=DEFAULT_LOGGER_BEAN;
         if (ContainsArgument(LOGGER_BEAN_ARG))
         {
              loggerBean=GetArgumentValue(LOGGER_BEAN_ARG);
         }
         logger=(Logger)context.getBean(loggerBean);
         if (ContainsArgument(VERBOSITY_ARG))
         {
             String verbosityValue=GetArgumentValue(VERBOSITY_ARG);
             switch (verbosityValue)
             {
                 case "0":  verbosity = Verbosity.NO_OUTPUT;
                     break;
                 case "1":  verbosity = Verbosity.MINIMAL;
                 break;
                 case "2":  verbosity = Verbosity.NORMAL;
                     break;
                 case "3":  verbosity = Verbosity.DETAILED;
                     break;
             }
         }
     }
 
     public void initDefault()
     {
            Object[] args=getArguments();
             initDefault(args);
     }
 
     public void initDefault(Object[] args)
     {
         parseArguments(args);
         initLogging();
        log("is alive...", 1);
         getContentManager().registerLanguage(codec);
         getContentManager().registerOntology(ontology);
     }
 
     private void parseArguments(Object[] args)
     {
         if (args==null)
         {
             return;
         }
         arguments=new HashMap<>();
         for (Object arg:args)
         {
                if (arg instanceof Argument)
                {
                       Argument argumentToAdd=(Argument)arg;
                       arguments.put(argumentToAdd.getName(),argumentToAdd);
                }
             else {
                    throw new IllegalArgumentException();
                }
         }
     }
 
     public void log(String text)
     {
         log(text,Verbosity.NORMAL);
     }
 
     public void log(String text,Verbosity level)
     {
         log(text, level.ordinal());
     }
 
     public void log(String text, int level){
         if (verbosity.ordinal() >= level){
             logger.log(getLocalName(),text);
         }
     }
 
     public void logError(String errorDescription)
     {
         logger.logError(getLocalName(),errorDescription);
     }
 
     public void logError(String errorDescription,Severity severity)
     {
         logger.logError(getLocalName(),errorDescription,severity);
     }
 }
