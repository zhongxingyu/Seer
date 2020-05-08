 
package examples.AgenteEnviador;

	
import java.io.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
 
 	public class agenteEnviador extends Agent{
 	    private byte[] fileContent;
 	    private String fileName;
 	    private Logger Log = Logger.getMyLogger(getClass().getName());
     protected void setup(){
 	    
 	// Obtener el nombre del archivo
 	Object[] args= getArguments();
 	if(args!=null && args.length>0){
 	    this.fileName= (String) args[0];
 	    addBehaviour(new SendFileBehaviour(this));
 	    System.out.println(fileName);
 	}else{
 	    System.out.println("No se especifico el nombre del archivo");
 	    doDelete();
 	} 
     }
     private class SendFileBehaviour extends Behaviour{
 	
 		public SendFileBehaviour(Agent a){
 		    super(a);
 		}
 
 		public boolean done(){
 		    return true;
 		}
 
 	public void action(){
 	    // Colocar aqui lo de buscar el archivo this.fileName
	  ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			  
	  try {
 		FileInputStream fstream = new FileInputStream(fileName);
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		String str;
 		StringBuffer strContent = new StringBuffer(""); 
 		while ((str = br.readLine()) != null) {
 		    fileContent = str.getBytes("UTF-16LE");
 		    strContent.append(str);	     
 		    System.out.println(str);
 		}
 		in.close();
 	    } catch (Exception e) {
 		System.err.println(e);
 	    }
 
 	    // Buscar un agente disponible a quien enviar el contenido
 	    DFAgentDescription temp = new DFAgentDescription();
 	    ServiceDescription sd = new ServiceDescription();
 	    sd.setType("agenteReceptor");
 	    template.addServices(sd);
 	    try {
 		DFAgentDescription[] result = DFService.search(myAgent, temp);
 		// Guardar los nombres de los agentes
 		AID receiverAgents[]= new AID[result.length];
 		for (int i = 0; i < result.length; ++i) {
 		    receiverAgents[i] = result[i].getName();
 		}
 	    }catch(FIPAException fe){
 		fe.printStackTrace();
 	    }
 	    msg.setByteSequenceContent(fileContent);
 	    msg.addUserDefinedParameter("file-name", fileName);
 	    send(msg);
 	    
 	}// fin de metodo action
 	
 	    }// FIN de clase privada SendFileBehaviour 
 
 	}
