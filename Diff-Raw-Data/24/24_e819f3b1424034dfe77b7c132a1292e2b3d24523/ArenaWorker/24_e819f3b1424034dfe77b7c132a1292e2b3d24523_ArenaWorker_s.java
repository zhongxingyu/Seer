 package game;
 
 import javax.swing.SwingWorker;
 
 public class ArenaWorker extends SwingWorker<String, Arena>{
 
 	
 	protected Arena a;
 	
 	public ArenaWorker(Arena a){
 		this.a = a;
 	}
 	
 	@Override
 	protected String doInBackground() throws Exception {
 		if(this.a.isHost()){
 			this.a.host(this.a.getNumPlayers(), this.a.getPort());
 		} else {
 			this.a.join(this.a.getIp(), this.a.getPort());
 		}
 		return " ";
 	}
 
 }
