package ACE;
 
 import java.io.BufferedInputStream;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.data.time.Minute;
 import org.jfree.data.time.TimeSeriesCollection;
 
 public class AceEpam extends Tplot {
 
 	private BufferedReader bufferedReader;
 
 	AceEpam() {
 		super(1);
 		//timeSeries[0].setKey("ACE Electron, Proton, and Alpha Monitor");
 	}
 
 	@Override
 	void readData(String arg0) {
 
 		String line;
 
 		try {
 			FileReader fileReader = new FileReader("/tmp" + arg0);
 			bufferedReader = new BufferedReader(fileReader);
 
 			while ((line = bufferedReader.readLine()) != null) {
 				if (!line.substring(0, 1).equals(":")
 						&& !line.substring(0, 1).equals("#")) { // skip header
 					int yyyy = Integer.parseInt(line.substring(0, 4));
 					int month = Integer.parseInt(line.substring(5, 7));
 					int day = Integer.parseInt(line.substring(8, 10));
 					int hour = Integer.parseInt(line.substring(12, 14));
 					int min = Integer.parseInt(line.substring(14, 16));
 
 					// Electron Differential Flux [particles/cm2-s-ster-MeV]
 					int stat_e = Integer.parseInt(line.substring(34, 35));
 					double electron1 = Double.parseDouble(line
 							.substring(35, 45)); // 38-53
 					double electron2 = Double.parseDouble(line
 							.substring(45, 55)); // 175-315
 
 					// Proton Differential Flux [particles/cm2-s-ster-MeV]
 					int stat_p = Integer.parseInt(line.substring(57, 58));
 					double proton1 = Double.parseDouble(line.substring(58, 68)); // 47-68
 					double proton2 = Double.parseDouble(line.substring(68, 78)); // 115-195
 					double proton3 = Double.parseDouble(line.substring(78, 88)); // 310-580
 					double proton4 = Double.parseDouble(line.substring(88, 98)); // 795-1193
 					double proton5 = Double
 							.parseDouble(line.substring(98, 108)); // 1060-1900
 
 					double anis = Double.parseDouble(line.substring(108, 115)); // 1060-1900
 
 					if (stat_e == 0) {
 						Minute minute = new Minute(min, hour, day, month, yyyy);
 						// this.add(minute, electron1);
 					}
 
 					if (stat_p == 0) {
 						Minute minute = new Minute(min, hour, day, month, yyyy);
 						this.add(minute, proton5, 0);
 					}
 
 				}
 			}
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	void file_http_copy(String arg0, String arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void file_http_copy(String arg0) {
 		URL url;
 		try {
 			url = new URL(arg0);
 
 			String[] strArray = url.getPath().split("/");
 			String strDir = "/tmp";
 			for (int i = 0; i < strArray.length - 1; i++) {
 				strDir = strDir + "/" + strArray[i];
 			}
 
 			File fileDir = new File(strDir);
 
 			if (fileDir.exists()) {
 				System.out.println(fileDir + "Directory exists.");
 			} else {
 				if (fileDir.mkdirs()) {
 					System.out.println(fileDir.getPath()
 							+ " Created directories to store data.");
 				} else {
 					System.out.println(fileDir.getPath()
 							+ " Couldn't created directories to store data.");
 				}
 			}
 
 			String charset = "UTF-8";
 
 			URLConnection conn = url.openConnection();
 			BufferedInputStream bis = new BufferedInputStream(
 					conn.getInputStream());
 			BufferedReader bufferedReader = new BufferedReader(
 					new InputStreamReader(bis, charset));
 			FileWriter fileWriter = new FileWriter("/tmp" + url.getPath());
 			String line;
 
 			while ((line = bufferedReader.readLine()) != null) {
 				fileWriter.write(line + "\n");
 			}
 
 			fileWriter.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public ChartPanel getChartPanel() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public JFreeChart getChart() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public TimeSeriesCollection loadData(String strUrl) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
