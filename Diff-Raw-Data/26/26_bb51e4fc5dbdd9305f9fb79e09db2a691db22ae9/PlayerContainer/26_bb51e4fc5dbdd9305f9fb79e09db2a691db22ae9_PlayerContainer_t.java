package fearlesscode;

public class PlayerContainer
{
     Player player;
     Position position;
     
    PlayerContainer()
    {
         this.player=new Player();
         this.position = new Position();
     }
    PlayerContainer(Player player, Position pos)
    {
        this.player=player;
         this.position=pos;
     }
 }
