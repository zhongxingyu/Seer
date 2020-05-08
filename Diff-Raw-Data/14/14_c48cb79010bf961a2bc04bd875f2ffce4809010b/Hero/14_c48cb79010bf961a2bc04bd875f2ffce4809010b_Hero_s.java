 package logic;
 
 import model.Position;
 
 /**
  * The Class Hero.
  */
 public class Hero extends Unit
 {
     /** The Constant serialVersionUID. */
     private static final long serialVersionUID = 1L;
 
     /** Has sword?. */
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
      *
      * @param equip true to equip, false to unequip
      */
     public void EquipSword(boolean equip)
     {
         _armed = equip;
     }
 
     /* (non-Javadoc)
      * @see logic.WorldObject#GetSymbol()
      */
     @Override
     public char GetSymbol()
     {
         return _armed ? 'A' : 'H';
     }
 
     /* (non-Javadoc)
      * @see logic.Unit#HandleEvent(logic.Maze, logic.Event)
      */
     @Override
     public void HandleEvent(Maze maze, Event event)
     {
         if (event.IsRequestMovementEvent())
         {
             RequestMovementEvent ev = event.ToRequestMovementEvent();
 
             Position newPos = _position.clone();
             Direction.ApplyMovement(newPos, ev.Direction);
 
             _direction = ev.Direction;
 
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
                         ev.Actor.ToEagle().EquipSword(false);
                         this.EquipSword(true);
                         ev.Actor.ToEagle().SetState(Eagle.EagleState.FollowingHeroWithSword);
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
