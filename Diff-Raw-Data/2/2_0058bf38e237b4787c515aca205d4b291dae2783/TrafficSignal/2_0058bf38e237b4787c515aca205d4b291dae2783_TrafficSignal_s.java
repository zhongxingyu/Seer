 /*
  * Copyright(C) 2013 Yutaka Kato
  */
 package local.JPL.ch06.ex06_04;
 
 public enum TrafficSignal {
 	OFF(new Color(0x000000)),
 	RED(new Color(0xff0000)),
 	YELLOW(new Color(0xffff00)),
 	GREEN(new Color(0x008000)),
 	BLINK_RED(new Color(0xff0000)),
 	BLINK_YELLOW(new Color(0xffff00));
 	
	Color color;
 	TrafficSignal(Color color) {
 		this.color = color;
 	}
 	public Color getColor() {
 		return color;
 	}
 }
