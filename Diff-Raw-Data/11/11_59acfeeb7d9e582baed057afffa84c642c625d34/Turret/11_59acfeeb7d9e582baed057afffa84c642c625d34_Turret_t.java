 package com.d4l3k.Link.Gate.Mechanic;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.util.Vector;
 
 import com.d4l3k.Link.BaseGate;
 import com.d4l3k.Link.Data;
 import com.d4l3k.Link.GateLocation;
 
 
 public class Turret extends BaseGate{
 	public Turret(SignChangeEvent event)
 	{
 		this.gateName = "Arrow Turret";
 		this.gateID = "[Turret]";
 		this.gatePerm = "danger";
 		this.gateBlock = event.getBlock();
 		//this.gateOutputNames = new String[1];
 		//this.gateOutputNames[0] = "Player";
 		//this.gateOutputTypes = new String[1];
 		//this.gateOutputTypes[0] = "string";
 		//this.gateOutputs = new Object[1];
 		
 		this.gateInputNames = new String[4];
 		this.gateInputNames[0] = "Target";
 		this.gateInputNames[1] = "Fire";
 		this.gateInputNames[2] = "Speed";
 		this.gateInputNames[3] = "Spread";
 		this.gateInputTypes = new String[4];
 		this.gateInputTypes[0] = "location";
 		this.gateInputTypes[1] = "double";
 		this.gateInputTypes[2] = "double";
 		this.gateInputTypes[3] = "double";
 		this.gateInputs = new Block[4];
 		this.gateInputIndexs = new int[4];
 	}
 	public Turret() {
 		// Auto-generated constructor stub
 	}
 	public void Execute()
 	{
 		if((Double)Data.getInput(this,1, 0.0)>=1.0)
 		{
			Location loc = ((GateLocation)Data.getInput(this,0, new GateLocation(gateBlock.getWorld()))).getLocation();
			Vector offset = loc.toVector().subtract(gateBlock.getLocation().toVector());
 			Vector vel = offset.normalize();
			double speed = (Double)Data.getInput(this, 2, 0.6);
			double spread = (Double)Data.getInput(this, 3, 12.0);
			gateBlock.getWorld().spawnArrow(gateBlock.getLocation().add(0.5, 0.5, 0.5), vel, (float)speed, (float)spread);
 			
 		}
 	}
 }
