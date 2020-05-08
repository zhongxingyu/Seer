 package com.eyetribe;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.util.Log;
 
 public class EyeGaze implements UDPInput.Listener {
 	public final int width;
   public final int height;
 	UDPInput udpInput;
 	static int vertMargin =  12; // upper and lower margin values in %
 	static int horizMargin = 30; // right and left margin values in %
 	public final int leftMargin;
   public final int rightMargin;
   public final int upperMargin;
   public final int lowerMargin;
 	private List<GazeListener> listeners = new ArrayList<GazeListener>();
 	
 	public EyeGaze(int width, int height){
 		udpInput = new UDPInput();
     udpInput.addListener(this);
 		this.width = width;
 		this.height = height;
 		
 		leftMargin  = (width * horizMargin) / 100;
 		rightMargin = width - (width * horizMargin) / 100;
 		upperMargin = (height * vertMargin) / 100;
 		lowerMargin = height - (height * vertMargin) / 100;
 	}
 	
   public void read() {
     udpInput.read();
   }
 
   public void pauseReading() {
     udpInput.pauseReading();
   }
 
   public void close() {
     udpInput.close();
   }
 
   private Coord inputAsPortrait(Coord coord) {
     return new Coord(width - coord.y, coord.x);
   }
 
 	public void handleGazeDataSet(UDPInput udpInput) {
 		int x = Math.round(Float.parseFloat(udpInput.getX()));
 		int y = Math.round(Float.parseFloat(udpInput.getY()));
     Coord coord = inputAsPortrait(new Coord(x, y));
 
     Log.i(UDPInput.LOG_TAG, coord.toString());
		
		if (coord.x < leftMargin ) {
 			if(coord.y < upperMargin) {
 				fireNorthWest(coord);
 			} 
       else if(coord.y > lowerMargin) {
 				fireSouthWest(coord);
 			} 
       else {
 				fireWest(coord);
 			}
 		} 
     else if(coord.x > rightMargin) {
 			if(coord.y < upperMargin) {
 				fireNorthEast(coord);
 			} 
       else if(coord.y > lowerMargin) {
 				fireSouthEast(coord);
 			}
       else {
 				fireEast(coord);
 			}
 		} else {
 			if(coord.y < upperMargin) {
 				fireNorth(coord);
 			} 
       else if(coord.y > lowerMargin) {
 				fireSouth(coord);
 			} 
       else {
 				fireCenter();
 			}
 		}
 	}
 
 	private void fireEast(Coord coord) {
 		for (GazeListener listener : listeners) {
       listener.East(coord);
     }
 	}
 	
 	private void fireWest(Coord coord) {
 		for (GazeListener listener : listeners) {
       listener.West(coord);
     }
 	}
 	
 	private void fireNorth(Coord coord) {
 		for (GazeListener listener : listeners) {
       listener.North(coord);
     }
 	}
 	
 	private void fireSouth(Coord coord) {
 		for (GazeListener listener : listeners) {
       listener.South(coord);
     }
 	}
 	
 	private void fireSouthWest(Coord coord) {
 		for (GazeListener listener : listeners) {
       listener.SouthWest(coord);
     }
 	}
 	
 	private void fireNorthWest(Coord coord) {
 		for (GazeListener listener : listeners) {
       listener.NorthWest(coord);
     }
 	}
 
 	private void fireNorthEast(Coord coord) {
 		for (GazeListener listener : listeners) {
       listener.NorthEast(coord);
     }
 	}
 
 	private void fireSouthEast(Coord coord) {
 		for (GazeListener listener : listeners) {
       listener.SouthEast(coord);
     }
 	}
 
 	private void fireCenter() {
 		for (GazeListener listener : listeners) {
       listener.Center();
     }
 	}
 	
 	@SuppressWarnings("unused")
 	private void firePageUp() {
 		for (GazeListener listener : listeners) {
       listener.PageUp();
     }
 	}
 
 	@SuppressWarnings("unused")
 	private void firePageDown() {
 		for (GazeListener listener : listeners) {
       listener.PageDown();
     }
 	}
 	
 	@SuppressWarnings("unused")
 	private void fireBack() {
 		for (GazeListener listener : listeners) {
       listener.Back();
     }
 	}
 	
 	@SuppressWarnings("unused")
 	private void fireDwell() {
 		for (GazeListener listener : listeners) {
       listener.Dwell();
     }
 	}
 	
 	public interface GazeListener {
 		void East(Coord coord);
 		void West(Coord coord);
 		void North(Coord coord);
 		void South(Coord coord);
 		void NorthEast(Coord coord);
 		void SouthEast(Coord coord);
 		void NorthWest(Coord coord);
 		void SouthWest(Coord coord);
 		void Center();
 		void PageUp();
 		void PageDown();
 		void Back();
 		void Dwell();
   }
 
 	  public void addListener(GazeListener listener) {
 		 this.listeners.add(listener);
 	  }
 		 
 	  public void removeListener(GazeListener listener) {
 		 this.listeners.remove(listener);
 	  }
 }
