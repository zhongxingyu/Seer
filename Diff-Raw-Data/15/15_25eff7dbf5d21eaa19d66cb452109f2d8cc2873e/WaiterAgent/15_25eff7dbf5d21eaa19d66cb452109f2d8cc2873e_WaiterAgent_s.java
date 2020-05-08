 package main.jadrin.waiter;
 
import java.awt.event.WindowEvent;

 import main.jadrin.ontology.DrinkOntology;
 import main.jadrin.ontology.QueryOntology;
 import jade.content.lang.Codec;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.core.Agent;
 import jade.core.AID;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.FIPAAgentManagement.Property;
 import jade.wrapper.ControllerException;
 
 public class WaiterAgent extends Agent { 
 
     /**
 	 * 
 	 */
 	private Codec codec = new SLCodec();
 	private Ontology queryOntology = QueryOntology.getInstance();
 	private Ontology drinkOntology = DrinkOntology.getInstance();
 	
 	private static final long serialVersionUID = 1378662441382554121L;
 	private WaiterAgentGui gui;
   
 	public String getCodecName()
 	{
 		return this.codec.getName();
 	}
 	
     protected void setup() 
     {
     	getContentManager().registerLanguage(codec);
 	    getContentManager().registerOntology(queryOntology);
 	    getContentManager().registerOntology(drinkOntology);
 		gui = new WaiterAgentGui(this);
 		gui.showGui();	
     }
 
     private AID[] getBartendersList()
     {
     	DFAgentDescription template = new DFAgentDescription();
 		ServiceDescription templateSd = new ServiceDescription();
 		templateSd.setType("drink-knowledge");
 		templateSd.addProperties(new Property("country", "Poland"));
 		template.addServices(templateSd);
 		AID aids[] = null;
      	try {
      		DFAgentDescription[] result = DFService.search(this, template);
         	aids = new AID[result.length];
      		for(int i=0; i<result.length; ++i)
     	    {
     	    	aids[i] = result[i].getName();
     	    }
      		return aids;
     	}
     	catch (FIPAException ex) {
     		ex.printStackTrace();
     	}
      	return aids;
     }
     
     public void close()
     {
 		try {
 			getContainerController().getPlatformController().kill();
 		} catch (ControllerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
 	public void analizeQuestion(String text) {
 		if(text.toLowerCase().equals("papa"))
 		{	
 			gui.done();
 			close();
 		}
 		AID bartenders[] = getBartendersList();
 		if (bartenders != null) {
 			addBehaviour(new BuildQueryBehaviour(this,text,bartenders,gui));
 	
 		}
 		else {
 			gui.setResponse("Nie udało mi się znaleźć żadnego Barmana - poczekaj chwilę, prawdapodobnie poszerzają swoją wiedzę.");
 		}
 	}
 }
