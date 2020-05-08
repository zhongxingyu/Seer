 public final class Figures {
 	private static final int[][] blinker = {{0,-1},{0,0},{0,1} };
 	private static final int[][] pulsar = {{1,-2},{1,-3},{1,-4},{2,-6},{3,-6},{4,-6},{2,-1},{3,-1},{4,-1},{6,-2},{6,-3},{6,-4},
		{2,1},{3,1},{4,1},{1,2},{1,3},{1,4},{2,6},{3,6},{4,6},{6,2},{6,3},{6,4},
		{-2,-1},{-3,-1},{-4,-1},{-1,-2},{-1,-3},{-1,-4},{-2,-6},{-3,-6},{-4,-6},{-6,-2},{-6,-3},{-6,-4},
		{-2,1},{-3,1},{-4,1},{-1,2},{-1,3},{-1,4},{-2,6},{-3,6},{-4,6},{-6,2},{-6,3},{-6,4}};
 	private static final int[][] glider = {{0,0},{0,-1},{0,-2},{-1,0},{-2,-1}};
 	private static final int[][] lwss = {{0,0},{1,0},{2,0},{3,0},{4,-1},{0,-1},{0,-2},{1,-3},{4,-3}};			

 	public static int[][] getCoordinates(String figureName){
		if(figureName.equals("blinker"))
			return blinker;
		else if(figureName.equals("pulsar"))
			return pulsar;
		else if(figureName.equals("glider"))
			return glider;
		
		return lwss;
	}
		/*
 		int[][] temp = null;
 		switch(figureName){
 		case "blinker": temp = blinker;break;
 		case "pulsar": temp = pulsar;break;
 		case "glider": temp = glider;break;
 		case "lwss": temp = lwss;break;
 		default:break;
 		}

		return temp;
		*/
	
 }
