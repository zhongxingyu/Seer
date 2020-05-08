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
 
 import com.springrts.ai.*;
 import com.springrts.ai.oo.*;
 import com.springrts.ai.oo.Map;
 
 import hughai.*;
 import hughai.utils.*;
 import hughai.basictypes.*;
 import hughai.unitdata.*;
 
 // this manages an attack pack, eg group of tanks
 // specifically ,ensures they stay together, rather than streaming
 // in theory
 public class AttackPackCoordinator extends PackCoordinator
 {
    public float MaxPackDiameterOuterThreshold = 500;
    public float MaxPackDiameterInnerThreshold = 300;
    public float movetothreshold = 20;
    public int MaxPackToConsider = 5;
 
    public float AttackDistance = 50;
 
    //	HashMap< Integer,UnitDef> UnitDefListByDeployedId;
    TerrainPos targetpos;
 
    UnitController unitController;
 
    //public delegate void AttackPackDeadHandler();
 
    //public event AttackPackDeadHandler AttackPackDeadEvent;
 
    // can pass in pointer to a hashtable in another class if we want
    // ie other class can directly modify our hashtable
    public AttackPackCoordinator( PlayerObjects playerObjects )
    {
       super(playerObjects);
       this.unitController = playerObjects.getUnitController();
 
       csai.registerGameListener( new GameListenerHandler() );
       //		csai.TickEvent += new CSAI.TickHandler( this.Tick );
    }
 
    // does NOT imply Activate()
    @Override
    public void SetTarget( TerrainPos newtarget )
    {
       this.targetpos = newtarget;
       //Activate();
    }
 
    TerrainPos lasttargetpos = null;
 
    @Override
    void Recoordinate()
    {
       if( !activated )
       {
          return;
       }
 
       // first we scan the list to find the 5 closest units to target, in order
       // next we measure distance of first unit from 5th
       // if distance is less than MaxPackDiameter, we move in
       // otherwise 5th unit holds, and we move units to position of 5th unit
       // pack must have at least 5 units, otherwise we call AttackPackDead event
 
       if( !CheckIfPackAlive() )
       {
          return;
       }
 
       logfile.WriteLine( this.getClass().getSimpleName() + " recoordinate" );
       int packsize = Math.min(MaxPackToConsider, unitsControlled.size());
       UnitInfo[] closestunits = GetClosestUnits(targetpos, packsize);
      if( closestunits.length == 0 ) {
          return;
       }
       TerrainPos packheadpos = closestunits[0].pos;
       TerrainPos packtailpos = closestunits[packsize - 1].pos;
       double packsquareddiameter = packheadpos.GetSquaredDistance( packtailpos );
       csai.DebugSay("packsize: " + packsize + " packdiameter: " + Math.sqrt(packsquareddiameter));
       // logfile.WriteLine( "AttackPackCoordinator packheadpos " + packheadpos.toString() + " packtailpos " + packtailpos.toString() + " packsquareddiamter " + packsquareddiameter );
       if (( regrouping && ( packsquareddiameter < (MaxPackDiameterInnerThreshold * MaxPackDiameterInnerThreshold) ) ) ||
             (!regrouping && ( packsquareddiameter < (MaxPackDiameterOuterThreshold * MaxPackDiameterOuterThreshold)) ))
       {
          regrouping = false;
          csai.DebugSay("attacking");
          Attack( closestunits);
       }
       else
       {
          csai.DebugSay("regrouping");
          regrouping = true;
          Regroup(closestunits[(packsize / 2)].pos);
       }
    }
 
    //boolean attacking = false;
    boolean regrouping = false;
 
    // placeholder...
    void GetAttackDistance(UnitInfo[]closestunits)
    {
       // for( UnitInfo unitinfo : closestunits )
       //    {
       //       if( unitinfo.unitdef.
       //   }
    }
 
    void Attack(UnitInfo[] closestunits)
    {
       //logfile.WriteLine( "AttackPackCoordinator attacking" );
       //if( debugon )
       // {
       //      aicallback.SendTextMsg( "AttackPackCoordinator attacking", 0 );
       // }
 
       // get vector from head unit to target
       // pick point a bit behind target, backwards along vector
       TerrainPos vectortargettohead = 
          targetpos.subtract( closestunits[0].pos );
       vectortargettohead.Normalize();
       //vectortargettohead = vectortargettohead * AttackDistance;
       MoveTo( targetpos.add( vectortargettohead.multiply( AttackDistance ) ) );
    }
 
    void Regroup( TerrainPos regouppos )
    {
       logfile.WriteLine( "AttackPackCoordinator regrouping to " + regouppos );
       if( debugon )
       {
 //         csai.SendTextMsg( "AttackPackCoordinator regrouping to " + regouppos );
       }
       MoveTo( regouppos );
    }
 
    void MoveTo( TerrainPos pos )
    {
       // check whether we really need to do anything or if order is roughly same as last one
       if( csai.DebugOn )
       {
          drawingUtils.DrawUnit("ARMSOLAR", pos, 0.0f, 50, aicallback.getTeamId(), true, true);
       }
       if( restartedfrompause || 
             pos.GetSquaredDistance( lasttargetpos )
             > ( movetothreshold * movetothreshold ) )
       {
          for( Unit unit : unitsControlled )
          {
             //int deployedid = (int)de.Key;
             //UnitDef unitdef = de.Value as UnitDef;
             giveOrderWrapper.MoveTo(unit, pos );
             //aicallback.GiveOrder( deployedid, new Command( Command.CMD_MOVE, pos.ToDoubleArray() ) );
          }
          restartedfrompause = false;
          lasttargetpos = pos;
       }
    }
 
    boolean CheckIfPackAlive()
    {
       //if( UnitDefListByDeployedId.size() < MinPackSize )
       // {
       //     if( AttackPackDeadEvent != null )
       //   {
       //     AttackPackDeadEvent();
       // }
       //     return false;
       // }
       return true;
    }
 
    class UnitInfo
    {
       public Unit unit;
       public TerrainPos pos;
       public UnitDef unitdef;
       public double squareddistance;
       public UnitInfo(){}
       public UnitInfo( Unit unit, TerrainPos pos, UnitDef unitdef, double squareddistance )
       {
          this.unit = unit;
          this.pos = pos;
          this.unitdef = unitdef;
          this.squareddistance = squareddistance;
       }
    }
 
    UnitInfo[] GetClosestUnits( TerrainPos targetpos, int numclosestunits )
    {
       UnitInfo[] closestunits = new UnitInfo[ numclosestunits ];
       double worsttopfivesquareddistance = 0; // got to get better than this to enter the list
       int numclosestunitsfound = 0;
       for( Unit unit : unitsControlled ) {
          TerrainPos unitpos = unitController.getPos( unit );
          UnitDef unitdef = unit.getDef();
          double unitsquareddistance = unitpos.GetSquaredDistance( targetpos );
          if( numclosestunitsfound < numclosestunits )
          {
             UnitInfo unitinfo = new UnitInfo( unit, unitpos, unitdef, unitsquareddistance );
             InsertIntoArray( closestunits, unitinfo, numclosestunitsfound );
             numclosestunitsfound++;
             worsttopfivesquareddistance = closestunits[ numclosestunitsfound - 1 ].squareddistance;
          }
          else if( unitsquareddistance < worsttopfivesquareddistance )
          {
             UnitInfo unitinfo = new UnitInfo( unit, unitpos, unitdef, unitsquareddistance );
             InsertIntoArray( closestunits, unitinfo, numclosestunits );
             worsttopfivesquareddistance = closestunits[ numclosestunits - 1 ].squareddistance;
          }
       }
       return closestunits;
    }
 
    // we add it to the bottom, then bubble it up
    void InsertIntoArray( UnitInfo[] closestunits, UnitInfo newunit, int numexistingunits )
    {
       if( numexistingunits < closestunits.length )
       {
          closestunits[ numexistingunits ] = newunit;
          numexistingunits++;
       }
       else
       {
          closestunits[ numexistingunits - 1 ] = newunit;
       }
       // bubble new unit up
       for( int i = numexistingunits - 2; i >= 0; i-- )
       {
          if( closestunits[ i ].squareddistance > closestunits[ i + 1 ].squareddistance )
          {
             UnitInfo swap = closestunits[ i ];
             closestunits[ i ] = closestunits[ i + 1 ];
             closestunits[ i + 1 ] = swap;  
          }
       }
       // debug;
       //  logfile.WriteLine( "AttackPackCoordinator.InsertIntoArray");
       //  for( int i = 0; i < numexistingunits; i++ )
       //   {
       //       logfile.WriteLine(i + ": " + closestunits[ i ].squareddistance );
       //   }
    }
 
    class GameListenerHandler extends GameAdapter {
       //      int ticks = 0;
       @Override
       public void Tick( int frame )
       {
          //         ticks++;
          //         if( ticks >= 30 )
          //         {
          Recoordinate();
 
          //            ticks = 0;
          //         }
       }
    }
 }
 
