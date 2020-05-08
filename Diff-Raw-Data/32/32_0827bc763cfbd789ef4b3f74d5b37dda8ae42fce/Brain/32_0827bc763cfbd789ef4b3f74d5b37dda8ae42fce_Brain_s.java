 package hello.presentation;
 
 import hello.domain.Thing;
 
 public abstract class Brain {
 	public Long key;
 	public Thing owner;
 	protected Game g1;
 	public void receiveCommand(Drop drop){}
 	public void receiveCommand(Empty empty){}
 	public void receiveCommand(Error error){}
 	public void receiveCommand(Get get){}
 	public void receiveCommand(Go go){}
 	public void receiveCommand(Look look){}
 	public void receiveCommand(Say say){}
 	public void dispatchCommand(Command command){
 		command.notifyBrain(this);
 	}
 	public void setGame(Game game) {
 		g1 = game;
 	}
 	public void setId() {
		key = Game.currentGame.nextId();
		Game.currentGame.brainId.put(key, this);
 	}
 	public long getId() {
 		return key;
 	}
 }
