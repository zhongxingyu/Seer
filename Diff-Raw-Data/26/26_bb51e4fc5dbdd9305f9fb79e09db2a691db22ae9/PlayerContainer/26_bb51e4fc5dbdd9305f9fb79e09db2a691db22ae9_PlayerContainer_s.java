/**
 * Created by IntelliJ IDEA.
 * User: tibikolozsi
 * Date: 3/16/12
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerContainer {
     Player player;
     Position position;
     
    PlayerContainer(){
         this.player=new Player();
         this.position = new Position();
     }
    
    PlayerContainer(Player play, Position pos){
        this.player=play;
         this.position=pos;
     }


 }
