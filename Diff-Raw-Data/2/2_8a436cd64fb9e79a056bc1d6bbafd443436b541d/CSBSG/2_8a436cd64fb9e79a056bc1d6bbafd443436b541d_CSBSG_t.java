 package dataManager;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class CSBSG {
 	
 	private Double[] getSizeScale(Double size) {
 		Double projectSize = size / 1000;
 		if (projectSize >= 0 && projectSize <= 4)
 			return (new Double[] { 0.0, 4.0 });
 		else if (projectSize > 4 && projectSize <= 16)
 			return (new Double[] { 4.0, 16.0 });
 		else if (projectSize > 16 && projectSize <= 64)
 			return (new Double[] { 16.0, 64.0 });
 		else if (projectSize > 64 && projectSize <= 256)
 			return (new Double[] { 16.0, 256.0 });
 		else if (projectSize > 256 && projectSize <= 1024)
 			return (new Double[] { 256.0, 1024.0 });
 		else
			return (new Double[] { 1024.0, -1.0 });
 	}
 
 	public ArrayList<Double> getProductivity(Double size, String attribute,
 			String attributeValue) {
 		ArrayList<Double> array = new ArrayList<Double>();
 		String sql = null;
 
 		Double[] sizeScale = getSizeScale(size);
 		Double minSize = sizeScale[0];
 		Double maxSize = sizeScale[1];
 
 		if (minSize < 1024)
 			sql = "select productivity from csbsg where projectSize>="
 					+ minSize + " and " + "projectSize<" + maxSize + " and "
 					+ attribute + "='" + attributeValue
 					+ "' order by productivity asc";
 		else
 			sql = "select productivity from csbsg where projectSize>="
 					+ minSize + " and " + attribute + "='" + attributeValue
 					+ "' order by productivity asc";
 
 		
 		System.out.println(sql);
 		DataAccess dataAccess = new DataAccess();
 		ResultSet resultSet = dataAccess.query(sql);
 		try {
 			while (resultSet.next()){
 				array.add(resultSet.getDouble("productivity"));
 				System.out.println("pi: " + resultSet.getDouble("productivity"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		dataAccess.close();
 		return array;
 	}
 
 	//返回projectSize,effort数组
 	public ArrayList<Double[]> getEffort(Double size, double percent) {
 		ArrayList<Double[]> array = new ArrayList<Double[]>();
 		
 		Double minSize = size * (1 - percent)/1000;
 		Double maxSize = size * (1 + percent)/1000;
 
 		String sql = "select projectSize, effort from csbsg where projectSize>="
 				+ minSize + " and " + "projectSize<" + maxSize
 				+ " order by effort asc";
 
 		DataAccess dataAccess = new DataAccess();
 		ResultSet resultSet = dataAccess.query(sql);
 		try {
 			while (resultSet.next())
 				array.add(new Double[] { resultSet.getDouble("projectSize"),
 						resultSet.getDouble("effort") });
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		dataAccess.close();
 		return array;
 
 	}
 	
 	/*public static void main(String[] args)
 	{
 		CSBSG c = new CSBSG();
 		ArrayList<Double[]> l = c.getEffort(10000.0, 0.1);
 		for(int i=0; i<l.size(); i++)
 			System.out.println(l.get(i)[0]+":"+l.get(i)[1]);
 	}*/
 }
