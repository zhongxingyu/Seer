 package com.example.SubGameObj.Tests;
 
 import com.example.SubGameObj.ObjectDrawer;
 import com.example.SubGameObj.Utils.Position;
 
 public class ObjectDrawerTestImpl extends ObjectDrawer {
 
 	@Override
 	public void prepareDrawer() {
 		System.out.print("prepareDrawer\n");
 	}
 
 	@Override
 	public void postChanges() {
 		System.out.print("postChanges\n");
 	}
 
 	@Override
 	public void drawSubmarine(Position pos) {
 		System.out.print("drawSubmarine: " + pos.toString() + "\n");
 	}
 
 	@Override
 	public void drawEnemyShip(Position pos) {
 		System.out.print("drawEnemyShip: " + pos.toString() + "\n");
 	}
 
 	@Override
 	public void drawTorpedo(Position pos) {
 		System.out.print("drawTorpedo: " + pos.toString() + "\n");
 	}
 
 	@Override
 	public void drawDepthCharge(Position pos) {
 		System.out.print("drawDepthCharge: " + pos.toString() + "\n");
 	}
 
 	@Override
 	public void drawExplosion(Position pos) {
 		System.out.print("drawExplosion: " + pos.toString() + "\n");
 	}
 
 	@Override
 	public void drawSound(Position pos) {
 		System.out.print("drawSound: " + pos.toString() + "\n");
 	}
 
 	@Override
 	public void drawSonarPing(Position pos) {
 		System.out.print("drawSonarPing: " + pos.toString() + "\n");
 	}

	@Override
	public void drawMap() {
		// TODO Auto-generated method stub
		
	}
 	
 	
 }
