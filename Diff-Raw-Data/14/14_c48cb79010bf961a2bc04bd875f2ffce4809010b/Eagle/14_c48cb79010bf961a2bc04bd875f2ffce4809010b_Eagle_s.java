 package logic;
 
 import java.util.Stack;
 
 import model.Position;
 
 /**
  * The Class Eagle.
  */
 public class Eagle extends Unit
 {
     /** The Constant serialVersionUID. */
     private static final long serialVersionUID = 1L;
 
     /**
      * The Eagle state.
      */
     public enum EagleState
     {
         /** Eagle is flying */
         OnFlight,
 
         /** Eagle laying on floor */
         OnFloor,
 
         /** Eagle is following hero */
         FollowingHero,
 
         /** Eagle reached sword */
         ReachedSword,
 
         /** Eagle if flying back to hero */
         OnFlightBack,
 
         /** Eagle is following hero and hero has sword */
         FollowingHeroWithSword
     }
 
     /** The eagle state. */
     private EagleState _state;
 
     /** The eagle speed. */
     private static final double _speed = 1;
 
     /** Eagle carries the sword. */
     private boolean _armed = false;
 
     /**
      * Instantiates a new eagle.
      */
     public Eagle()
     {
         super(UnitType.Eagle);
         _state = EagleState.FollowingHero;
     }
 
     /**
      * Sets the state.
      *
      * @param state the state
      */
     public void SetState(EagleState state)
     {
         _state = state;
     }
 
     /**
      * Gets the state.
      *
      * @return the eagle state
      */
     public EagleState GetState()
     {
         return _state;
     }
 
     /**
      * Equip sword.
      *
      * @param equip true if should equip, false to unequip
      */
     public void EquipSword(boolean equip)
     {
        _armed = true;
     }
 
     /**
      * Checks if Eagle carries sword.
      *
      * @return true, if successful
      */
     public boolean IsArmed()
     {
         return _armed;
     }
 
     /** The initial position. */
     private Position _initialPosition = null;
 
     /** The sword position. */
     private Position _swordPosition = null;
 
     /** The path eagle followed when flying to sword */
     private final Stack<Position> _wayPath = new Stack<Position>();
 
     /**
      * Sets the sword position (internal)
      *
      * @param pos the pos
      */
     public void SetSwordPosition(Position pos)
     {
         _swordPosition = pos;
     }
 
     /* (non-Javadoc)
      * @see logic.Unit#Update(logic.Maze)
      */
     @Override
     public void Update(Maze maze)
     {
         if (_state == EagleState.OnFlight)
         {
             if (_initialPosition == null) // takeoff
             {
                 _initialPosition = GetPosition();
                 _wayPath.push(_initialPosition);
             }
 
             double deltaX = _swordPosition.X - GetPosition().X;
             double deltaY = _swordPosition.Y - GetPosition().Y;
 
             double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
             if (dist > _speed)
             {
                 double ratio = _speed / dist;
                 double xMove = ratio * deltaX;
                 double yMove = ratio * deltaY;
                 double newXPos = xMove + GetPosition().X;
                 double newYPos = yMove + GetPosition().Y;
                 Position pos = GetPosition().clone();
                 SetPosition(new Position(Math.round((float)newXPos), Math.round((float)newYPos)));
                 _direction = GetPosition().GetDirectionFrom(pos);
                 maze.ForwardEventToUnits(new MovementEvent(this, null));
             }
             else
             {
                 Position pos = GetPosition().clone();
                 SetPosition(_swordPosition);
                 _direction = GetPosition().GetDirectionFrom(pos);
                 maze.ForwardEventToUnits(new MovementEvent(this, null));
             }
 
             if (GetPosition().equals(_swordPosition))
                 _state = EagleState.ReachedSword;
             else
                 _wayPath.push(GetPosition());
         }
         else if (_state == EagleState.OnFlightBack)
         {
             if (!_wayPath.isEmpty())
             {
                 Position pos = GetPosition().clone();
                 SetPosition(_wayPath.pop());
                 _direction = GetPosition().GetDirectionFrom(pos);
                 maze.ForwardEventToUnits(new MovementEvent(this, null));
             }
 
             if (_wayPath.isEmpty())
                 SetState(EagleState.OnFloor);
         }
         else if (_state == EagleState.ReachedSword)
             _state = EagleState.OnFlightBack;
     }
 
     /* (non-Javadoc)
      * @see logic.Unit#HandleEvent(logic.Maze, logic.Event)
      */
     @Override
     public void HandleEvent(Maze maze, Event event)
     {
         if (event.IsMovementEvent())
         {
             MovementEvent ev = event.ToMovementEvent();
             if (ev.Actor.IsHero())
             {
                 if (IsFollowingHero())
                 {
                     Position pos = this.GetPosition().clone();
                     _position = ev.Actor.GetPosition().clone();
                     _direction = _position.GetDirectionFrom(pos);
                     maze.ForwardEventToUnits(new MovementEvent(this, ev.Direction));
                 }
                else if (_position.equals(ev.Actor.GetPosition()))
                 {
                     this.EquipSword(false);
                     ev.Actor.ToHero().EquipSword(true);
                     SetState(EagleState.FollowingHeroWithSword);
                 }
                 else if (ev.Actor.IsDragon())
                 {
                     if (_position.equals(ev.Actor.GetPosition()) || Position.IsAdjacent(_position, ev.Actor.GetPosition()))
                         this.Kill();
                 }
             }
         }
         else if (event.IsSendEagleEvent())
         {
             SendEagleEvent ev = event.ToSendEagleEvent();
             if (!ev.Hero.IsArmed() && _state == EagleState.FollowingHero)
             {
                 _swordPosition = ev.Sword.GetPosition().clone();
                 SetState(EagleState.OnFlight);
             }
         }
     }
 
     /**
      * Can be killed by dragon.
      *
      * @return true, if successful
      */
     public boolean CanBeKilledByDragon()
     {
         return _state != EagleState.OnFlight && _state != EagleState.OnFlightBack && _state != EagleState.FollowingHeroWithSword;
     }
 
     /**
      * Checks if is flying.
      *
      * @return true, if successful
      */
     public boolean IsFlying()
     {
         return _state == EagleState.OnFlight || _state == EagleState.OnFlightBack;
     }
 
     /**
      * Checks if is following hero.
      *
      * @return true, if successful
      */
     public boolean IsFollowingHero()
     {
         return _state == EagleState.FollowingHero || _state == EagleState.FollowingHeroWithSword;
     }
 
     /* (non-Javadoc)
      * @see logic.WorldObject#GetSymbol()
      */
     @Override
     public char GetSymbol()
     {
         return IsArmed() ? '\u00A5' /* Yen */ : 'W';
     }
 }
