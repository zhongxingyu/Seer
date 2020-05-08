 package amaitjo.brains;
 
import amaitjo.Report;
import amaitjo.ReportingBeacon;
 import amaitjo.common.DirectionHelper;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EmptyStackException;
import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 import java.util.UUID;
 import org.linkedin.contest.ants.api.Action;
 import org.linkedin.contest.ants.api.Ant;
 import org.linkedin.contest.ants.api.Direction;
 import org.linkedin.contest.ants.api.DropFood;
 import org.linkedin.contest.ants.api.Environment;
 import org.linkedin.contest.ants.api.GetFood;
 import org.linkedin.contest.ants.api.Move;
 import org.linkedin.contest.ants.api.Pass;
 import org.linkedin.contest.ants.api.Square;
 import org.linkedin.contest.ants.api.WorldEvent;
 import org.linkedin.contest.ants.api.Write;
 
 
 public class RandomWalk implements Ant
 {
   Stack<Direction> _squares = new Stack<Direction>();
   Boolean _haveFood = false;
   Long _foodLeft = -1L;
   int _dropCount = 0;
   List<Direction> _directions = new ArrayList<Direction>()
   {{
       add(Direction.east);
       add(Direction.west);
       add(Direction.north);
       add(Direction.south);
       add(Direction.northeast);
       add(Direction.northwest);
       add(Direction.southeast);
       add(Direction.southwest);
     }};
 
   String _id = UUID.randomUUID().toString();
 
   @Override
   public void init()
   {
   }
 
   @Override
   public Action act(Environment environment, List<WorldEvent> worldEvents)
   {

    Report report = new Report();
    report.Action = _act(environment, worldEvents);
    report.Properties = new HashMap<String, String>();
    report.Properties.put("dropCount", Integer.toString(_dropCount));
//    ReportingBeacon.getInstance().report(_id, report);

    return report.Action;
  }

  public Action _act(Environment environment, List<WorldEvent> worldEvents)
  {
     try
     {
       Square curSquare = environment.getSquare(Direction.here);
 
       if (_squares.size() == 0)
       {
         if (_haveFood)
         {
           _haveFood = false;
           _foodLeft = 0L;
 
           if (curSquare.isNest())
           {
 //            System.out.println(_id.substring(0,2) + "_drop " + ++_dropCount);
             return new DropFood(Direction.here);
           }
           else
           {
             System.out.println("how did we get here?");
             return new Pass();
           }
         }
         else
         {
           if (!curSquare.isNest())
           {
             System.out.println("lost!");
             return null;
           }
         }
       }
       else
       {
         if (_haveFood)
         {
           if (_foodLeft > 0)
           {
             if (curSquare.hasWriting())
             {
               return new Move(_squares.pop());
             }
             return new Write(_foodLeft);
           }
           else
           {
             if (curSquare.hasWriting())
             {
               return new Write(null);
             }
             return new Move(_squares.pop());
           }
         }
       }
 
       if (curSquare.hasFood() && !curSquare.isNest())
       {
         _haveFood = true;
         _foodLeft = new Long(curSquare.getAmountOfFood());
         return new GetFood(Direction.here);
       }
 
       Collections.shuffle(_directions);
       for (Direction dir : _directions)
       {
         Square foo = environment.getSquare(dir);
 
         if (_squares.size() > 0 && dir.equals(_squares.peek())) continue;
 
         if ((foo.hasFood() && !foo.isNest())
             || foo.hasWriting()
             || foo.isPassable())
         {
           return storeMove(dir);
         }
       }
 
       if (_squares.size() > 0)
       {
         return new Move(_squares.pop());
       }
       return new Pass();
     }
     catch (EmptyStackException e)
     {
       e.printStackTrace();
     }
 
     return null;
   }
 
   private Move storeMove(Direction d)
   {
     _squares.push(DirectionHelper.getOppositeDirection(d));
     return new Move(d);
   }
 
   @Override
   public Action onDeath(WorldEvent worldEvent)
   {
     return null;
   }
 }
 
