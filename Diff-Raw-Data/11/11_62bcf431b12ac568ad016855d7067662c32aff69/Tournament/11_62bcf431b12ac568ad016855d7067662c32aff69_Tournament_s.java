 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model.game;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import util.ObjectState;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.TreeMap;
 import model.agent.Agent;
 import model.properties.game.GameProperties;
 import model.properties.game.TournamentProperties;
 import util.GenericFactory;
 import util.Subscriber;
 import java.lang.Thread.State;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * This represents the tournament and is used to start the tournament.
  * @author drew
  */
 public class Tournament implements Subscriber, Runnable, Comparable {
 
     /**
      * The state of the game is mapped to the respected game
      */
     private ExecutorService threadPool;
     private TreeMap<Game, ObjectState> runningGames;
     private ObjectState state;
     private TournamentProperties props;
     private String name;
     private ArrayList<Agent> remainingAgents, eliminatedAgents;
     private boolean paused;
     private GenericFactory fac;
     /**
      * Instantiates a properties object.
      */
     public Tournament() {
         setup();
     }
 
     /**
      * sets the tournprops to the arg
      * @param props 
      */
     public void setTournProps(TournamentProperties props) {
         this.props = props;
     }
 
     /**
      * Returns the tournament props
      * @return 
      */
     public TournamentProperties getTournProps() {
         return props;
     }
     
     public void setupTourn()
     {
         threadPool = Executors.newFixedThreadPool(props.getNumMaxThreads());
         remainingAgents = props.getAgents();
         // now remove any of the eliminated agents
         for (Agent ag : eliminatedAgents) {
             remainingAgents.remove(ag);
         }
         props.getAgentSelector().setPlayers(remainingAgents);
         props.getAgentSelector().setNumToSelect(props.getGameProps().getNumAgents());
         // first check if there are games that were started and saved and now need to be restarted
         for (Game g : runningGames.keySet()) {
 
             // start the game as a thread
             threadPool.submit(g);
         }
     }
 
     /**
      * This starts the tournament
      */
     public void startTourn() {
 
         
         
 
         // while there are agents to play game/not stopped/not paused
         // while the number of games in the queue is less than the max num threads then
 
        while (competitorsAvail() == true) {
 
             System.out.println("inside while loop for tourn");
 
             ArrayList<Agent> contestants = null;
             if (runningGames.size() < props.getNumMaxThreads() && (contestants = props.getAgentSelector().nextContestants()) != null && contestants.size() > 0) {
                 System.out.println("Starting first game");
                 // get the game properties from tournprops
                 GameProperties gameProps = props.getGameProps();
                 // get the name of the game toString
                 String gameName = gameProps.toString();
 
                 // get the game that matches the game props by providing the name of the game
                 Game game = (Game) fac.getObject(gameName);
                 // set the game's properties
                 game.setGameProperties(gameProps);
                 // set the agents to the game
                 game.setAgents(contestants);
                 // Create a new ObjectState set this as the subscriber (to know when game is done)
                 // set state to Runnable
                 ObjectState obState = new ObjectState(State.RUNNABLE, this, game);
                 // set the Agent's state to running
                 for (Agent ag : contestants)
                 {
                     // no one will be notified about the state of the agent
                     // this can change if neccessary...
                     ag.setState(new ObjectState(State.RUNNABLE));
                 }
                 // assign the Objectstate to the Game object
                 game.setObjectState(obState);
                 // add it to the running games
                 runningGames.put(game, obState);
                 // start the game as a thread
                 threadPool.submit(game);
             }
         }
         //threadPool.shutdown();
        // System.out.println(this.toString() + " Finished");
         // if the returned agents are null then I know that I am done so
         // set my state to finished and notify the subscribers
 
     }
 
     /**
      * pause all running games
      */
     public void pauseTournament() {
         for (Game g : runningGames.keySet()) {
             runningGames.get(g).setState(State.WAITING);
         }
     }
 
     /**
      * This will start all of the games that were paused
      */
     public void resumeTournament() {
         paused = false;
         // games are in seperate threads so they can call wait
         // so I must notify them
         for (Game g : runningGames.keySet()) {
             synchronized(g)
             {
                 g.notify();
             }
         }
         paused = false;
     }
 
     /**
      * set the tournaments state
      * @param st 
      */
     public void setState(ObjectState st) {
         state = st;
     }
 
     /**
      * Get the tournament's state
      * @return 
      */
     public ObjectState getState() {
         return state;
     }
 
     /**
      * This is where all of the variables are setup
      */
     private void setup() {
         props = new TournamentProperties();
         runningGames = new TreeMap<Game, ObjectState>();
         remainingAgents = new ArrayList<Agent>();
         eliminatedAgents = new ArrayList<Agent>();
         paused = false;
         // create a gamefactory object
         fac = new GenericFactory();
         fac.generateMaping("config/GameList.cfg");
     }
 
     /**
      * Whenever a game's state changes I get notified.
      * So I will remove the game from the map if the game is finished
      * @param pub
      * @param code
      * @throws RemoteException 
      */
     @Override
     public void update(Object pub, Object code) throws RemoteException {
         if (code instanceof Game && (State) ((ObjectState) pub).getState() == State.TERMINATED) {
             // eliminate an agent from the remaining list
             // new problem when I go to eliminate an agent the
             // score will be at where ever it is at when I call this
             // won't be fair maybe the other agent hasn't even played a whole
             // game yet...
             Agent elimAgent = props.getEliminator().eliminate(remainingAgents);
             // might not eliminate in that case it returns null
             if (elimAgent != null) {
                 remainingAgents.remove(elimAgent);
                 eliminatedAgents.add(elimAgent);
             }
             // So now I can set the state of the Agents in the game to Blocked
             // meaning that it is ready to be selected again
             for (Agent ag : ((Game) code).getAgents())
             {
                 // as long as it is not the eliminated agent
                 if (ag != elimAgent)
                     ag.getAgentObjectState().setState(State.BLOCKED);
             }
             
             // update the players to choose from for the AgentSelector
             props.getAgentSelector().setPlayers(remainingAgents);
             // remove the game from the running list
             runningGames.remove((Game) code);
             System.out.println(((Game) code).toString());
         }
         System.out.println((State) ((ObjectState) pub).getState());
 
 
         // move this to the update method and have Tournament subscribe to the state
         // so then it will be updated and I will need to check that the pub arg is
         // equal to state field then if it is WAITING then I go inside the if
         if (state.getState() == State.WAITING) {
             paused = true;
             // pause the games
             pauseTournament();
             // pause this thread then the batch will call notify on this thread.
             // I don't think I should call wait since I won't be in a seperate thread
            /* synchronized (this) {
                 try {
                     
                     //wait();
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }*/
             
             // done waiting so resume the games
            resumeTournament();
             
         }
 
         // once running games is empty and I am not paused and agentselector produces
         // no more competitors then Tournament is completed 
         // I will remove myself as a subscriber to my own object state
         // then I will set my state to Terminated and then batch will be
         // updated as to my status and I will return from update and will exit this part
         if (competitorsAvail() == false && runningGames.isEmpty() == true && paused == false )
         {
             state.removeSub(this);
             state.setState(State.TERMINATED);
         }
         else
         {
             // I still have games to play
             startTourn();
         }
     }
 
     @Override
     public void run() {
         setupTourn();
         startTourn();
     }
 
     @Override
     public String toString() {
         return name;
     }
 
     public void setString(String newName) {
         name = newName;
     }
 
     @Override
     public int compareTo(Object o) {
         return this.toString().compareTo(o.toString());
     }
 
     private boolean competitorsAvail() {
         // call getAgentSel to get the agent selector from the tournprops
         // set status to finished and notify observers and return false
         return props.getAgentSelector().hasContestants();
     }
 }
