 // Copyright Hugh Perkins 2006, 2009
 // hughperkins@gmail.com http://manageddreams.com
 //
 // This program is free software; you can redistribute it and/or modify it
 // under the terms of the GNU General Public License as published by the
 // Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 // or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 //  more details.
 //
 // You should have received a copy of the GNU General Public License along
 // with this program in the file licence.txt; if not, write to the
 // Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-
 // 1307 USA
 // You can find the licence also on the web at:
 // http://www.opensource.org/licenses/gpl-license.php
 //
 // ======================================================================================
 //
 
 package hughai.packcoordinators;
 
 import java.util.*;
 import java.util.Map;
 
 import com.springrts.ai.*;
 import com.springrts.ai.oo.*;
 
 import hughai.*;
 import hughai.mapping.*;
 import hughai.packcoordinators.*;
 import hughai.unitdata.*;
 import hughai.utils.*;
 import hughai.basictypes.*;
 
 // this carries out a spread search: sends units to nearest
 // unseen los, otherwise sends all units around randomly
 // can be used by scoutcontroller, or by tankcontroller, for example
 public class SpreadSearchPackCoordinatorWithSearchGrid extends PackCoordinator
 {
    //	HashMap< Integer, UnitDef> UnitDefListByDeployedId;
    TerrainPos targetpos;
 
    HashSet<Unit> moveFailed = new HashSet<Unit>();
    HashMap<Unit, TerrainPos> lastPosByUnit = new HashMap<Unit, TerrainPos>(); // to check the thing is not stuck...
    HashMap<Unit, Integer> stuckCountByUnit = new HashMap<Unit, Integer>();
    Cache<Unit> unitsinselfdestruct = new Cache<Unit>( 180 );
 
    PlayerObjects playerObjects;
    UnitController unitController;
    Config config;
 
    int recentmeansnumframes = 5000;
 
    int mapwidth;
    int mapheight;
 
    // can pass in pointer to a hashtable in another class if we want
    // ie other class can directly modify our hashtable
    public SpreadSearchPackCoordinatorWithSearchGrid(
          PlayerObjects playerObjects )
    {
       super( playerObjects );
 
       this.playerObjects = playerObjects;
       this.unitController = playerObjects.getUnitController();
       config = playerObjects.getConfig();
 
       mapwidth = aicallback.getMap().getWidth();
       mapheight = aicallback.getMap().getHeight();
 
       csai.registerGameListener( new GameListenerHandler() );
    }
 
    // does NOT imply Activate()
    @Override
    public void SetTarget( TerrainPos newtarget )
    {
       this.targetpos = newtarget;
       //Activate();
    }
 
    @Override
    public void Activate()
    {
       if( !activated )
       {
          logfile.WriteLine( "SpreadSearchPackCoordinatorWithSearchGrid initiating spreadsearch" );
          activated = true;
          restartedfrompause = true;
          Recoordinate();
       }
    }
 
    @Override
    public void Disactivate()
    {
       if( activated )
       {
          activated = false;
          logfile.WriteLine( "SpreadSearchPackCoordinatorWithSearchGrid shutting down" );
 
          csai.UnregisterVoiceCommand("dumpsearchgrid" );
       }
    }
 
    boolean restartedfrompause = true;
    Float3 lasttargetpos = null;
 
    void Recoordinate()
    {
       if( !activated )
       {
          return;
       }
 //      logfile.WriteLine("SpreadSearchPackCoordinatorWithSearchGrid.Recoordinate() restartedfrompause " + restartedfrompause );
 
 //      int frame = aicallback.getGame().getCurrentFrame();
       int frame = playerObjects.getFrameController().getFrame();
       
       // just send each unit to random destination
       // in unit onidle, we send each unit to a new place
       for( Unit unit : unitsControlled )
       {
          boolean stuck = false;
          //         logfile.WriteLine( "unitsControlled unit " + unit.getUnitId() + " " + unit.hashCode() + " in movefailed? " + moveFailed.contains( unit ) );
          //         logfile.WriteLine( "Search Unit " + unit.getUnitId() 
          //               + " lastpos: " + lastPosByUnit.get( unit )  
          //               + " thispos: " + unitController.getPos( unit )
          //               + " same? " + unitController.getPos( unit ).equals( lastPosByUnit.get( unit ) ) ); 
          if( restartedfrompause  ) {
             logfile.WriteLine("SpreadSearchPackCoordinatorWithSearchGrid.Explorewith() reason: restartedfrompause " + unit.getUnitId() );
             ExploreWith( unit, false );
 //         } else if(  moveFailed.contains( unit ) ) {
 //            logfile.WriteLine("SpreadSearchPackCoordinatorWithSearchGrid.Explorewith() reason: movefailed " + unit.getUnitId() );
 //            ExploreWith( unit, false );
          } else if( unitController.getPos( unit ).equals( lastPosByUnit.get( unit ) ) ) {
             logfile.WriteLine("SpreadSearchPackCoordinatorWithSearchGrid.Explorewith() reason: hasn't moved " + unit.getUnitId() );
             stuck = true;
             if( !stuckCountByUnit.containsKey( unit ) || stuckCountByUnit.get( unit ) <= 10 ) {
                ExploreWith( unit, true );
             }
          }
          if( stuck ) {
             if( stuckCountByUnit.containsKey( unit ) ) {
                stuckCountByUnit.put( unit, stuckCountByUnit.get( unit ) + 1 );
                if( stuckCountByUnit.get( unit ) > 10 ){
                   logfile.WriteLine( "stuck count for " + unit.getUnitId() 
                         + " " + 
                         unitController.getUnitDef( unit ).getHumanName() + 
                   " too high: self-destructing." );
                   if( !unitsinselfdestruct.isInCache( frame, unit ) ) {
                      giveOrderWrapper.SelfDestruct( unit );
                      unitsinselfdestruct.cache( frame, unit );
                   }
                }
             } else {
                stuckCountByUnit.put( unit, 1 );
             }
          } else {
             stuckCountByUnit.remove( unit );
          }
          lastPosByUnit.put( unit, unitController.getPos( unit ) );
       }
       restartedfrompause = false;
       moveFailed.clear();
    }
 
    void HandleMoveFailed() {
       for( Unit unit : moveFailed ) {
          logfile.WriteLine( "Triggering new explore for move failed unit " + unit.getUnitId() + " " + 
                unitController.getUnitDef( unit ).getHumanName() );
          ExploreWith( unit, true );
       }
       moveFailed.clear();
    }
 
    HashMap< Unit, Integer > lastexploretime = new HashMap< Unit, Integer >();
    public void ExploreWith( Unit unit, boolean forcerandomdestination )
    {
 //      forcerandomdestination = true;
       if (lastexploretime.containsKey(unit) &&
             playerObjects.getFrameController().getFrame() - lastexploretime.get( unit ) < 30)
       {
          return;
       }
       boolean destinationfound = false;
       TerrainPos currentpos = unitController.getPos( unit );
       MovementMaps movementmaps = maps.getMovementMaps();
       UnitDef unitdef = unitController.getUnitDef( unit );
       int currentarea = movementmaps.GetArea( unitdef, currentpos );
       LosMap losmap = maps.getLosMap();
       if( csai.DebugOn )
       {
          logfile.WriteLine("SpreadSearchWithSearchGrid explorewith unit " 
                + unit.getUnitId() + " " 
                + unitdef.getHumanName() 
                + " area: " + currentarea );
       }
 
       /*
             int numtriesleft = 30; // just try a few times then give up
             // maybe there is a better way to do this?
             while( !destinationfound )
             {
                 Float3 destination = GetRandomDestination();
                // logfile.WriteLine( "SpreadSearchWithSearchGrid attempt " + destination.toString() );
                 int mapx = (int)( destination.x / 16 );
                 int mapy = (int)( destination.z / 16 );
                 if( ( movementmaps.GetArea( unitdef, destination ) == currentarea && 
                     losmap.LastSeenFrameCount[ mapx, mapy ] < recentmeansnumframes || numtriesleft <= 0 ) )
                 {
                     logfile.WriteLine( "Looks good. Go. " + numtriesleft + " retriesleft" );
                     if( csai.DebugOn )
                     {
                         aicallback.CreateLineFigure( currentpos, destination,10,true,400,0);
                         aicallback.DrawUnit( "ARMFAV", destination, 0.0f, 400, aicallback.GetMyAllyTeam(), true, true);
                     }
                     aicallback.GiveOrder( unitid, new Command( Command.CMD_MOVE, destination.ToDoubleArray() ) );
                     return;
                 }
                 numtriesleft--;
             }
        */
       // find nearest, area that hasnt had los recently
       //int maxradius = Math.Max( aicallback.getMap().getWidth()(), aicallback.getMap().getHeight()() ) / 2;
       //for( int radius = 
       TerrainPos nextpoint = null;
       if( !forcerandomdestination ) {
          nextpoint = losHelper.GetNearestUnseen(currentpos, unitdef, 12000 );
       }
       if (nextpoint == null)
       {
          nextpoint = GetRandomDestination(currentarea);
       }
      if (nextpoint == null ) {
         return;
      }
       lastexploretime.put(unit, playerObjects.getFrameController().getFrame());
       giveOrderWrapper.MoveTo(unit, nextpoint );
       if( config.isDebug() ) {
          drawingUtils.AddLine( currentpos, nextpoint );
          drawingUtils.DrawUnit( config.getSpreadsearchnextmovemarkerunitname(),
                nextpoint );
       }
    }
 
    // note: this needs to know whether we are a vehicle, or whatever
    TerrainPos GetRandomDestination( int currentarea )
    {
       int attempts = 20;
       while( attempts > 0 ) {
          TerrainPos destination = new TerrainPos();
          destination.x = random.nextFloat() * aicallback.getMap().getWidth() * maps.getMovementMaps().SQUARE_SIZE;
          destination.z = random.nextFloat() * aicallback.getMap().getHeight() * maps.getMovementMaps().SQUARE_SIZE;
          destination.y = aicallback.getMap().getElevationAt( destination.x, destination.y );
          int newarea = maps.getMovementMaps().GetVehicleArea( destination );
          if( newarea == currentarea ) {
             return destination;
          }
          attempts--;
       }
       return null;
    }
 
    class GameListenerHandler extends GameAdapter {
       int totalticks = 0;
       //		int ticks = 0;
       @Override
       public void Tick( int frame )
       {
          //			ticks++;
          totalticks++;
          //Reappraise();
          //HandleMoveFailed();
          Recoordinate();
          //			if( ticks >= 30 )
          //			{
          //Recoordinate();
 
          //				ticks = 0;
          //			}
       }
 
       @Override
       public void UnitIdle( Unit unit  )
       {
          if( activated )
          {
             if( unitsControlled.contains( unit ) )
             {
                ExploreWith( unit, false );
             }
          }
       }
 
       @Override
       public void UnitMoveFailed( Unit unit ) {
          if( activated )
          {
             if( unitsControlled.contains( unit ) )
             {
                if( !moveFailed.contains( unit )) {
                   moveFailed.add( unit );
                   csai.sendTextMessage( "Move failed for " + unit.getUnitId() + " " + 
                         unitController.getUnitDef( unit ).getHumanName() );
                }
             }
          }		   
       }
    }
 }
 
