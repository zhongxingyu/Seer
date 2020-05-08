 package Agents;
 
 import java.util.Random;
 import jade.content.lang.Codec.CodecException;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.OntologyException;
 import jade.content.onto.UngroundedException;
 import jade.content.onto.basic.Action;
 import jade.content.onto.basic.Result;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.Location;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.core.behaviours.TickerBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPANames;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.JADEAgentManagement.WhereIsAgentAction;
 import jade.domain.mobility.MobilityOntology;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.util.leap.Iterator;
 
 public class RobotAgent extends Agent {
 
    private static final long serialVersionUID = 1L;
    public Sandbox mapa;
    public Field OurMap[][];// = new Field[mapa.getWidth()][mapa.getHeight()];
    public int x=1;
    public int y=2;
    public boolean wyjscie=false; //oznacza że robot znalazł wyjście (gdy true)
    public boolean praca=true; //oznacza że robot pracuje (gdy true)
    public AID user;
    public AID[] tempAgents;   
    public String tmp;
    
    public boolean isDeadEnd(){
 	   try{
 		   if(OurMap[x+1][y].isObstacle() 
 				   && OurMap[x-1][y].isObstacle()
 				   && OurMap[x][y+1].isObstacle()
 				   && OurMap[x][y-1].isObstacle()
 				   ){
 			   	return true;
 			   }
 		   } catch(Exception e){
 			   return false;
 		   }
 	   return false;
    }
    
    public boolean canMove(int posX, int posY){
 	   try{
 		   if(mapa.map[posX][posY].isObstacle()){
 			   System.out.println("Obstacle @ " + posX + ":" + posY);
 			   return false;
 		   } else {
 			   System.out.println("Free @ " + posX + ":" + posY);
 			   return true;
 		   }
 	   } catch(Exception e) { // array out of bound etc
 		   //System.out.println("STH Gone wrong - while trying to move to " + posX + ":" + posY);
 		   return false;		   		   
 	   }
    }
    
    public void move(int posX, int posY){
 	   if(isDeadEnd())
 		   throw new Error("Dead end - app will close - i.e agent should die");
 	   if(canMove(posX, posY)){
		   try{		   
			   	OurMap[posX][posY].setObstacle(false);
		   	} catch(Exception e){ // create new field
		   		OurMap[posX][posY] = new Field();
		   	}
 		  x = posX;
 		  y = posY;
 	   } else { // can't move post action
 		   try{		   
 			   OurMap[posX][posY].setObstacle(true);
 			   System.out.println("Obstacle add " + posX + ":" + posY);
 		   } catch(Exception e){
 			   try{
 				   OurMap[posX][posY] = new Field(true);
 				   System.out.println("Obstacle init " + posX + ":" + posY);
 			   }catch(Exception e2){
 				   System.err.print(e2.getMessage());
 				   System.out.println("STH Gone terribly wrong - while moving - tried"  + posX + ":" + posY);
 			   }
 		   }
 		  	
 	   }
 	   System.out.println("Position " + x + ":" + y);	   
    }
 
    
    @Override
    protected void setup(){
        this.addBehaviour(new OneShotBehaviour(this) {
          private static final long serialVersionUID = 1L;
         
          /*
           * portal edukacyjny, wykład o komunikacji pod koniec rejestracja w DF kom miedzy robotami. ALBO
           * wspolny resource dla agentow... (blackboard).
          */
          
          @Override
          public void action() {
             getContentManager().registerLanguage(new SLCodec(),
                   FIPANames.ContentLanguage.FIPA_SL0);
             getContentManager().registerOntology(
                   MobilityOntology.getInstance());
 
             mapa = new Sandbox(true, 0, 0); // Tu siedzą smoki
             // initialize ourMap
             OurMap = new Field[mapa.getWidth()][];
             int i=0;
             while(i < mapa.getWidth()){
             	OurMap[i] = new Field[mapa.getHeight()];
             	i++;
             }
 
             Random rand = new Random();
 
            x = Math.abs(rand.nextInt())%mapa.getWidth();
            y = Math.abs(rand.nextInt())%mapa.getHeight();
 
            System.out.println(mapa.map[0][0].isObstacle());
            while(mapa.map[x][y].isObstacle()){
 
         	   x = Math.abs(rand.nextInt())%mapa.getWidth();
                y = Math.abs(rand.nextInt())%mapa.getHeight();	
            }
            
          }
       });
        
 
        this.addBehaviour(new CyclicBehaviour(this) {
            private static final long serialVersionUID = 1L;
 
            @Override
            public void action() {
         	   
         	   if(wyjscie && praca){
         		
         		   ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setConversationId("inform");
                    msg.addReceiver(new AID("SA", AID.ISLOCALNAME));
                    //convert tablicy na string
                    String str = mapa.getHeight()+" "+mapa.getWidth()+" ";
                    for(int i=0;i<mapa.getHeight();i++){
                 	   str+="R";
                 	   //lol
                 	   for(int j=0;j<mapa.getWidth();j++){
                		   try{
                			   if(OurMap[j][i].isObstacle())
                			   {
                				   str+=""+1;
                			   } else {
                				   str+=""+0;
                			   }
                		   } catch(Exception e){ // if field not initialised, return 0 - no obstacle - unknown
                 			   str+=""+0;
                		   }
                 	   }
                    }   
                    //koniec convertu tablicy
                    msg.setContent(str);
                    send(msg);
 
         	   }
     	   }
         });
        
        this.addBehaviour(new OneShotBehaviour(this){
 
 		@Override
 		public void action() {
 			
 			DFAgentDescription dfd = new DFAgentDescription();
 			dfd.setName(getAID());
 			ServiceDescription sd = new ServiceDescription();
 			sd.setType("Robot");
 			dfd.addServices(sd);
 			try {
 				DFService.register(myAgent, dfd);
 			}
 			catch (FIPAException fe) {
 			fe.printStackTrace();
 			}
 			
 		}
     	   
        });
        
        this.addBehaviour(new CyclicBehaviour(this) {
 
 		@Override
 		public void action() {
 			
 			//Określenie do kogo wysłać
 			// Update the list of agents
 			DFAgentDescription template = new DFAgentDescription();
 			ServiceDescription sd = new ServiceDescription();
 			sd.setType("Robot");
 			template.addServices(sd);
 			try {
 			DFAgentDescription[] result = DFService.search(myAgent, template);
 			tempAgents = new AID[result.length];
 			for (int i = 0; i < result.length; ++i) {
 			tempAgents[i] = result[i].getName();
 			    }
 			}
 			catch (FIPAException fe) {
 			fe.printStackTrace();
 			   }
 			
 			if(wyjscie && praca){
 				 praca = false;
 				 ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                  msg.setConversationId("inform");
                  for(int i=0;i<tempAgents.length; i++)
                  msg.addReceiver(tempAgents[i]);
                  String str = "koniec";
                  msg.setContent(str);
                  send(msg);
 			}
 			
 		}
     	   
        });
        
        this.addBehaviour(new CyclicBehaviour(this) {
            private static final long serialVersionUID = 1L;
 
            @Override
            public void action() {
               ACLMessage rec = receive(MessageTemplate
                     .MatchPerformative(ACLMessage.QUERY_IF));
               rec = receive();
               
               if (rec != null && rec.getSender() != getAMS()) {
              	if(rec.getPerformative()==ACLMessage.INFORM){
                      if (rec.getConversationId() != null && rec.getConversationId()=="spam"){
                     	 if(rec.getContent()=="koniec")
                     		 praca = false;
                      }
              		}
               	}
               }
        });
 
       this.addBehaviour(new TickerBehaviour(this, 500 * 1) {
 
           @Override
           public void onTick() {
         	  if(praca){
         	  Random go = new Random();
         	  int direction = go.nextInt()%4; // 0 top, 1 right, 2 bottom, 3 left    
         	  switch(direction){
         	  	case 0:
         	  		move(x,y+1);
         	  		break;
         	  	case 1:
         	  		move(x+1,y);
         	  		break;
         	  	case 2:
         	  		move(x,y-1);
         	  		break;
         	  	case 3:
         	  		move(x-1,y);
         	  		break;	
         	  }
         	  wyjscie = true;
         	  }
         	  
 
           }
        });
    }
 
 }
