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
		// TODO Automaticky generovaný stub konstruktoru
 	}
 
 	@Override
 	public Boolean nextFrame(Mat image) {
		// TODO Automaticky generovaný stub metody
		return null;
 	}
 
 	@Override
 	public HashMap<Point, ArrayList<Socket>> getDevices() {
		// TODO Automaticky generovaný stub metody
		return null;
 	}
 
 }
