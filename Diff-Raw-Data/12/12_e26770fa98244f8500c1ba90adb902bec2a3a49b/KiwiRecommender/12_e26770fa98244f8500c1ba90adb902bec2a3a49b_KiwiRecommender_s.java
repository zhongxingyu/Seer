 package edu.ntnu.idi.goldfish.mahout;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import matlabcontrol.MatlabConnectionException;
 import matlabcontrol.MatlabInvocationException;
 import matlabcontrol.MatlabProxy;
 import matlabcontrol.MatlabProxyFactory;
 import matlabcontrol.MatlabProxyFactoryOptions.Builder;
 import matlabcontrol.extensions.MatlabTypeConverter;
 
 import org.apache.mahout.cf.taste.common.Refreshable;
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
 import org.apache.mahout.cf.taste.model.DataModel;
 import org.apache.mahout.cf.taste.recommender.IDRescorer;
 import org.apache.mahout.cf.taste.recommender.RecommendedItem;
 import org.apache.mahout.cf.taste.recommender.Recommender;
 import org.apache.mahout.math.Arrays;
 
 public class KiwiRecommender implements Recommender {
 	
 	private static MatlabProxyFactory factory = null;
 	private static MatlabProxy proxy = null;
 	private SMDataModel model;
 	
 	static MatlabProxy getProxy() {
 		
 		if(proxy == null) {
 			try {
 				Builder b = new Builder();
 				b.setUsePreviouslyControlledSession(true);
 				b.setMatlabStartingDirectory(new File("matlab"));
 				factory = new MatlabProxyFactory(b.build());
 				proxy = factory.getProxy();
 			} catch (MatlabConnectionException e) {
 				e.printStackTrace();
 			}
 		}
 		return proxy;
 	}
 	
 	public static void close() {
 		proxy.disconnect();	
 	}
 		
 	MatlabTypeConverter processor;
 
 	public KiwiRecommender(DataModel model, double[] weights, double[] latentFactors) {
 		this.model = (SMDataModel) model;
 		//Create a proxy, which we will use to control MATLAB
 		processor = new MatlabTypeConverter(getProxy());
 		
 		 try {
 			 // Build Matlab class
 			MatlabProxy p = getProxy();
 			File file = new File("/tmp/ratings-synthesized.csv");
 			this.model.writeDatasetToFile(file);
 			p.eval(String.format("kiwi = Kiwi('%s', %s, %s);", file.getAbsolutePath(), Arrays.toString(weights), Arrays.toString(latentFactors)));
 			
 			
 			
 		} catch (MatlabInvocationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	public void refresh(Collection<Refreshable> alreadyRefreshed) {	
 		try {
 			getProxy().eval("kiwi.refresh()");
 		} catch (MatlabInvocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public List<RecommendedItem> recommend(long userID, int howMany) {
 		try {
 			List<RecommendedItem> recommended = new ArrayList<RecommendedItem>(	);
 			getProxy().eval(String.format("results = kiwi.recommend(%d, %d)", userID, howMany));
 			double[][] arr = processor.getNumericArray("results").getRealArray2D();
			for(double val : arr[0]) {
				recommended.add(new GenericRecommendedItem((long) val, -10));
 			}
 			return recommended;
 		} catch (MatlabInvocationException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
 		return recommend(userID, howMany);
 	}
 
 	public float estimatePreference(long userID, long itemID) throws TasteException {
 		try {
 			getProxy().eval(String.format("result = kiwi.estimatePreference(%d, %d)", userID, itemID));
 			return ((float[]) proxy.getVariable("result"))[0];
 		} catch (MatlabInvocationException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public void setPreference(long userID, long itemID, float value) throws TasteException {
 		try {
 			getProxy().eval(String.format("kiwi.setPreference(%d, %d)", userID, itemID));
 		} catch (MatlabInvocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removePreference(long userID, long itemID) throws TasteException {
 		try {
 			getProxy().eval(String.format("kiwi.removePreference(%d, %d)", userID, itemID));
 		} catch (MatlabInvocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public DataModel getDataModel() { 
 		return null;
 	}
 }
