 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Main {
 
 	private class MinimumGas {
 		private class GasStation {
 			public int distance;
 			public int price;
 			
 			public GasStation(int distance, int price) {
 				this.distance = distance;
 				this.price = price;
 			}
 		}
 		
 		private final List<GasStation> stations = new ArrayList<GasStation>();
 		
 		private final int cost;
 		
 		public MinimumGas(BufferedReader in) throws IOException {
 			
 			int distance = Integer.parseInt(in.readLine());
 			while (true) {
 				String line = in.readLine();
 				if (line == null || line.length() == 0)
 					break;
 				
 				String[] tokens = line.split(" ");
 				int gasDist = Integer.parseInt(tokens[0]);
 				if (gasDist < distance) {
 					stations.add(new GasStation(gasDist,
 							Integer.parseInt(tokens[1])));
 				}
 			}
 			
 			// Add 100 to distance so that we have half a tank when arriving
 			distance += 100;
 			
 			cost = cost(0, distance, 100, 0, stations.size()-1);
 		}
 		
 		private int cost(int from, int to, int gas, int sLow, int sHigh) {
 			if (to - from <= gas)
 				return 0;
 			
 			GasStation minStation = null;
 			int minIndex = -1;
 			for (int i = sLow; i <= sHigh; i++) {
 				if (minStation == null || stations.get(i).price < minStation.price) {
 					minStation = stations.get(i);
 					minIndex = i;
 				}
 			}
 			if (minStation == null)
 				return -1;
 			
 			int lPrice = cost(from, minStation.distance, gas, sLow, minIndex - 1);
 			if (lPrice == -1)
 				return -1;
 			else if (lPrice == 0)
 				gas -= (minStation.distance - from);
 			else
 				gas = 0;
 			
 			int hPrice = cost(minStation.distance, to, 200, minIndex + 1, sHigh);
 			if (hPrice == -1)
 				return -1;
 			
 			int fill = Math.min(200 - gas, to - minStation.distance - gas);
 			return lPrice + fill*minStation.price + hPrice;
 		}
 		
 		public int getCost() {
 			return cost;
 		}
 	}
 	public Main(BufferedReader in) throws IOException {
 		int N = Integer.parseInt(in.readLine());
 		in.readLine();
 		for (int i = 0; i < N; i++) {
 			MinimumGas mg = new MinimumGas(in);
 			if (i > 0)
 				System.out.println();
 			if (mg.getCost() != -1)
 				System.out.println(mg.getCost());
 			else
 				System.out.println("Impossible");
 		}
 	}
 	
 	public static void main(String[] args) throws IOException {
 		InputStreamReader isr = new InputStreamReader(System.in);
 		BufferedReader in = new BufferedReader(isr);
 
 		new Main(in);
 	}
 
 }
