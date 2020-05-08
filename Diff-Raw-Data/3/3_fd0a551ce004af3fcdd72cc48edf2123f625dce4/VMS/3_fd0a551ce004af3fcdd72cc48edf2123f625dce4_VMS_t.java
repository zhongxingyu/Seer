 // Manipulation of the vessel information
 // is made here
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 
 public class VMS {
 	private final int maxBoat = 100;
 	private double[][] distance;
 	private String[] risk;
 
 	VMS() {
 		distance = new double[maxBoat][maxBoat];
 		risk = new String[maxBoat];
 	}
 	void update(RadarSimulator rs) {
 		updateDistance(rs);
 	}
 		
 	private void updateDistance(RadarSimulator rs)  {
 		for (int i=0; i<rs.getCount(); i++)
 			for (int j=i+1; j<rs.getCount(); j++) {
 //				if (i == j)		distance[i][j] = 0;
 //				else {
 					double deltaX = rs.getXPosBoat(i) - rs.getXPosBoat(j);
 					double deltaY = rs.getYPosBoat(i) - rs.getYPosBoat(j);
 					double deltaD = Math.sqrt(Math.pow(deltaX, 2.0) + Math.pow(deltaY, 2.0));
 					
 					distance[i][j] = deltaD;
 					distance[j][i] = deltaD;
 					
 /*					if (deltaD < 50.0) {
 						highRisk[i] = true;
 						highRisk[j] = true;
 					}
 					else if (deltaD < 200.0) {
 						lowRisk[i] = true;
 						lowRisk[j] = true;
 						
 					}
 					else {
 						lowRisk[i] = false;
 						lowRisk[j] = false;
 						highRisk[i] = false;
 						highRisk[j] = false;
 					}
 				}// END OF ELSE
 */
 					if (deltaD <= 50) {
 						risk[i] = "high";
 						risk[j] = "high";
 					}
 					else if (deltaD <= 200) {
 						if (risk[i] != "high")
 							risk[i] = "low";
 						if (risk[j] != "high")
 							risk[j] = "low";
 					}
 					else {
 						if (risk[i] != "high" && risk[i] != "low")
 							risk[i] = "none";
 						if (risk[j] != "high" && risk[i] != "low")
 							risk[j] = "low";
 					}
 			
 			}// END OF FOR LOOP
 		
		rs.addRisk(risk);
 	}
 	void removeRow(int index, int count) {
 		for (int i = index; i<count; i++) {
 			risk[i] = risk[i+1];
 		}
 			
 	}
 	final Object[][] filterData(RadarSimulator rs, int type) {
 		if (type == 0)
 			return rs.getData();
 		
 		Object[][] filteredData;
 		int size = 0;
 		for (int i=0; i<rs.getCount(); i++)
 			if ((Integer)rs.getRow(i)[1] == type)
 				size++;
 		System.out.println("Size of new array is: " + size);
 
 		filteredData = new Object[size][9];
 		int row = 0;
 		for (int i=0; i<rs.getCount() && row<size; i++)
 			if ((Integer)rs.getRow(i)[1] == type) {
 				filteredData[row] = rs.getRow(i);
 				row++;
 			}
 		for (int i=0; i<filteredData.length; i++) {
 			for (int j=0; j<8; j++) {
 //				System.out.print(filteredData[i][j] + "\t");
 			}
 			System.out.println();
 		}
 		return filteredData;
 	}
 } // END IF ,java
 
 
