 package com.example.digitallighterserver;
 
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 
 public class DeviceTracker implements DeviceLocatingStrategy {
 
 	HashMap<Point, ArrayList<Socket>> devices;
 	
 	public DeviceTracker(int tilesX, int tilesY,
 			HashMap<Point, ArrayList<Socket>> devices) {
		this.devices = devices;
 	}
 
 	@Override
 	public Boolean nextFrame(Mat image) {
		return false;
 	}
 
 	@Override
 	public HashMap<Point, ArrayList<Socket>> getDevices() {
		return devices;
 	}
 
 }
