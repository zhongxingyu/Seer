 package logic;
 
 import model.Position;
 
 /**
  * The Class Hero.
  */
 public class Hero extends Unit
 {
     /** Has sword? */
     private boolean _armed;
 
     /**
      * Instantiates a new hero.
      */
     public Hero()
     {
         super(UnitType.Hero);
     }
 
     /**
      * Checks if hero is armed.
      *
      * @return true, if hero has sword
      */
     public boolean IsArmed()
     {
         return _armed;
     }
 
     /**
      * Equip or unequip sword.
      */
     public void EquipSword(boolean equip)
     {
         _armed = equip;
     }
 
     @Override
     public char GetSymbol()
     {
         return _armed ? 'A' : 'H';
     }
 
     @Override
     public void Update(Maze maze){ }
 
     @Override
     public void OnCollision(Unit other)
     { }
 
     @Override
     public void HandleEvent(Maze maze, Event event) {
         if (event.IsRequestMovementEvent())
         {
             RequestMovementEvent ev = event.ToRequestMovementEvent();
 
             Position newPos = _position.clone();
             Direction.ApplyMovement(newPos, ev.Direction);
 
             if (maze.IsPathPosition(newPos) || (IsArmed() && maze.IsExitPosition(newPos)))
             {
                 _position = newPos;
                 maze.ForwardEventToUnits(new MovementEvent(this, ev.Direction));
             }
         }
         else if (event.IsMovementEvent())
         {
             MovementEvent ev = event.ToMovementEvent();
 
             if (ev.Actor.IsEagle())
             {
                 if (_position.equals(ev.Actor.GetPosition()))
                 {
                     if (ev.Actor.ToEagle().IsArmed())
                    {
         				ev.Actor.ToEagle().UnequipSword();
                         this.EquipSword(true);
         			}
 
                 }
             }
             else if (ev.Actor.IsDragon())
             {
                 if (_position.equals(ev.Actor.GetPosition()) || Position.IsAdjacent(_position, ev.Actor.GetPosition()))
                 {
                     if (this.IsArmed())
                         ev.Actor.Kill();
                     else
                         this.Kill();
                 }
             }
         }
 
     }
 }
