 package edu.illinois.ncsa.versus.restlet;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.LogFactory;
 //import org.eclipse.jetty.util.log.Log;
 import org.apache.commons.logging.Log;
 
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import edu.illinois.ncsa.versus.adapter.Adapter;
 import edu.illinois.ncsa.versus.engine.impl.Job.ComparisonStatus;
 import edu.illinois.ncsa.versus.extract.Extractor;
 import edu.illinois.ncsa.versus.measure.Measure;
 import edu.illinois.ncsa.versus.restlet.DecisionSupport.DSInfo;
 import edu.illinois.ncsa.versus.restlet.DecisionSupport.DS_Status;
 import edu.illinois.ncsa.versus.restlet.MultiLabelDecisionSupport.MLDS_Status;
 import edu.illinois.ncsa.versus.store.ComparisonServiceImpl;
 import edu.illinois.ncsa.versus.store.RepositoryModule;
 
 @SuppressWarnings("serial")
 public class MultiLabelDecisionSupport implements Serializable {
 	
 	private String id;
 	private String adapterId;
 	private List<Extractor> availableExtractors = new ArrayList<Extractor>();
 	private List<Measure> availableMeasures 	= new ArrayList<Measure>();
 	private ArrayList<MLDSInfo> mldsData 		= new ArrayList<MLDSInfo>();
 	private ArrayList<ArrayList<String>> data   = new ArrayList<ArrayList<String>>();
 	private int k;
 	private MLDS_Status status                  = MLDS_Status.UNINITIALZED;
 	private String decidedMethod				= "Not Available";
 	private String method                       = "Not Set";
 	private boolean computationFinished         = false;
 	private String rankedResults                = "";
 		
 	private static Log log = LogFactory.getLog(MultiLabelDecisionSupport.class);
 	
 	public enum MLDS_Status {
 		UNINITIALZED, STARTED, RUNNING, DONE, FAILED
 	}
 	
 	public MultiLabelDecisionSupport(){};
 	
 	public MultiLabelDecisionSupport(ArrayList<ArrayList<String>> fileGroups, String adapterId, int k, String method) {
 		super();
 		this.adapterId = adapterId;	
 		this.k=k;
 		for( int i=0; i<fileGroups.size(); i++){
 			data.add(fileGroups.get(i));
 		}
 		this.method = method;
 	}
 	
 	public boolean checkIfComputationComplete(){
 		return computationFinished;
 	}
 	
 	public String getMethod(){
 		return method;
 	}
 	
 	public void setMethod(String method){
 		this.method = method;
 	}
 	
 	public String getDecidedMethod(){
 		return decidedMethod;
 	}
 	
 	public ArrayList<MLDSInfo> getMultiLabelDecisionSupportData(){
 		return mldsData;
 	}
 	
 	public void setMultiLabelDecisionSupportData(ArrayList<MLDSInfo> mlds){
 		this.mldsData = mlds;;
 	}
 	
 	public ArrayList<ArrayList<String>> getData(){
 		return data;
 	}
 	
 	public String getId(){
 		return this.id;
 	}
 	
 	public void setK(int k){
 		this.k=k;
 	}
 	
 	public int getK(){
 		return k;
 	}
 	
 	public void setId(String id){
 		this.id = id; 
 	}
 	
 	public void setData(ArrayList<ArrayList<String>> d){
 		this.data = d;
 	}
 	
 	public String getAdapterId(){
 		return this.adapterId;
 	}
 	
 	public void setAdapterId(String a){
 		this.adapterId = a;
 	}
 	
 	public void setAvailableExtractors(List<Extractor> e){
 		this.availableExtractors = e;
 	}
 	
 	public void setAvailableMeasures(List<Measure> m){
 		this.availableMeasures = m;
 	}
 	
 	public void calculateMeans(){
 		
 	}
 		
 	public void getSupportedMethods(){
 		
 		Iterator<Extractor> e_itr = this.availableExtractors.iterator();
 		
 		while( e_itr.hasNext() ){ //iterate through all available extractors	
 			
 			//extractor
 			Extractor e                              = e_itr.next();
 			Set<Class<? extends Adapter>> a          = e.supportedAdapters();			
 			Iterator<Class<? extends Adapter>> a_itr = a.iterator();
 			
 			while( a_itr.hasNext() ){ //iterate through all supported adapters for given extractor
 				
 				//adapter
 				String name = a_itr.next().getName();
 				
 				if( this.adapterId.equals(name) ){					
 									
 					String descriptorName   = (e.getFeatureType()).getName(); //get the descriptor supported by extractor
 					Iterator<Measure> m_itr = this.availableMeasures.iterator(); //all available measures 
 					
 					while( m_itr.hasNext() ){
 						
 						Measure m  = m_itr.next();	
 						
 						if( m.getFeatureType().equals(descriptorName) ){ //check if the measure supports the descriptor (provided by the extractor)
 							
 							if( !this.availableExtractors.contains( e.getClass().getName() ) ){// if there is an EM pair, create DSInfo and push to list
 								mldsData.add( new MLDSInfo(this.adapterId, e.getClass().getName(),m.getClass().getName()));
 							}
 						}	
 					}
 				}		
 			}//end adapters
 		}//end extractors
 	}
 	
 	public String availableExtractors2String(){
 		
 		String ae            = "";
 		Iterator<MLDSInfo> itr = mldsData.iterator();
 		
 		while( itr.hasNext() ){			
 			ae = ae + itr.next().extractorID + " ";
 		}		
 		return ae;
 	}
 	
 	public String availableMeasures2String(){
 		
 		String am            = "";
 		Iterator<MLDSInfo> itr = mldsData.iterator();
 		
 		while( itr.hasNext() ){			
 			am = am + itr.next().measureID + " ";	
 		}		
 		
 		return am;
 	}
 	
 	public void setStatus(MLDS_Status status) {
 		this.status = status;
 	}
 	public MLDS_Status getStatus(){
 		return status;
 	}
 	
 	private boolean checkIfComplete(){
 		
 		Injector injector                       = Guice.createInjector(new RepositoryModule());
 		ComparisonServiceImpl comparisonService = injector.getInstance(ComparisonServiceImpl.class);
 		
 		for( int i=0; i<mldsData.size(); i++){
 			
 			ArrayList<ArrayList<String>> comparisons = mldsData.get(i).getComparisons();
 			boolean cErrorFlag    					 = false;
 
 			//check all comparisons
 			for( int j=0; j < comparisons.size(); j++){
 				
 				for( int k=0; k < comparisons.get(j).size(); k++){
 				
 					Comparison c = comparisonService.getComparison(comparisons.get(j).get(k) );
 				
 					if( (c.getStatus() == ComparisonStatus.ABORTED) || (c.getStatus() == ComparisonStatus.FAILED) ){
 						cErrorFlag = true;
 						break;
 					}
 					else if( c.getStatus() != ComparisonStatus.DONE ){
 						log.debug("Befor:Status is not DONE");
 						try {
 							//Thread.sleep(5);
 					    	synchronized(c){
 					    	c.wait();
					    	if(c.getStatus()!=ComparisonStatus.DONE){
					    		c.wait();
					    	   }
 					    	}
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						log.debug("After:Status=DONE");
 						//return true;
 						
 						//return false;
 					}
 
 				}
 				if(cErrorFlag){
 					break;
 				}
 			}
 			if(cErrorFlag){
 				mldsData.remove(i);
 				
 				i--;
 			}
 		}	
 		return true;
 	}
 		
 	//method1
 	//calculate means -> add all k(means) for each E,M pair -> smallest is the best classifier (no need for normalization )
 	//because the smallest distances for each mean demonstrates smaller means, thus that is the best classifier. 
 	//thus return the best E,M pair that demonstrates this
 	//method2
 	//chen's method, but applying it towards multi-label data
 	public void getBestPair(){
 		
 		if(!computationFinished){
 			
 			log.debug("Entered getBestPair");
 			
 			this.status = MLDS_Status.RUNNING;
 			
 			Injector injector                       = Guice.createInjector(new RepositoryModule());
 			ComparisonServiceImpl comparisonService = injector.getInstance(ComparisonServiceImpl.class);
 			
 			boolean state        = checkIfComplete();
 			
 			
 			if(method.equals("inverseKmeans")){			
 			    log.debug("Inside inverseKmeans");
 				double minMeanSum = Double.MAX_VALUE;
 				int minMeanIndex = -1;
 				if(state){
 					log.debug("ALL COMPARISONS DONE");
 				for( int i=0; i<mldsData.size(); i++){ //each E-M pair
 				
 					ArrayList<ArrayList<String>> comparisons = mldsData.get(i).getComparisons();
 					
 					double[] mean = new double[comparisons.size()];//means for each label
 					double meanSum = 0;
 	
 					for( int j=0; j < comparisons.size(); j++){//each label
 					
 						double sum    = 0;
 						
 						for( int k=0; k<comparisons.get(j).size(); k++){ //distance values for each label
 						
 							String value = ((Comparison) comparisonService.getComparison( comparisons.get(j).get(k) )).getValue();
 							sum +=Double.parseDouble( value );
 						}
 						mean[j] = sum / k; // mean for label j
 						meanSum += sum / k;// Mean sum for each EM pair
 						
 						
 					}
 					mldsData.get(i).setMeans(mean);
 					mldsData.get(i).setMeanSum(meanSum);
 	
 					if(meanSum < minMeanSum){
 						minMeanSum   = meanSum;
 						minMeanIndex = i;
 						log.debug(" minMeanSum="+meanSum+" minMeanIndex="+minMeanIndex);
 					}
 				}// end i
 	
 				if( minMeanIndex != -1 ){
 					decidedMethod = mldsData.get(minMeanIndex).extractorID + " " + mldsData.get(minMeanIndex).measureID;
 					log.debug("decidedMethod="+decidedMethod);
 				}
 				
 				Collections.sort(mldsData,new Comparator<MLDSInfo>() {
 		            public int compare(MLDSInfo info1, MLDSInfo info2) {
 		                return info1.getMeanSum().compareTo(info2.getMeanSum());
 		            }
 		        });
 				
 				 //return top 5 or 25% of methods
 			       int numTopMethods = (int)(mldsData.size() * 0.30);
 			       
 			       if( numTopMethods > 5 ){
 			    	   numTopMethods = 5;
 			       }
 			       
 			       for( int i=0; i<=numTopMethods; i++){
 			    	   
 			    	   int exIn=mldsData.get(i).extractorID.lastIndexOf('.');
 			    	   String exName=mldsData.get(i).extractorID.substring(exIn+1);
 			    	   int meIn=mldsData.get(i).measureID.lastIndexOf('.');
 			    	   String meName=mldsData.get(i).measureID.substring(meIn+1);
 			    	   
 			    	  // rankedResults = rankedResults + (i+1) + ".) " + decisionSupportData.get(i).extractorID + " " + decisionSupportData.get(i).measureID + " ";
 			    	   rankedResults = rankedResults + (i+1) + ".) " + exName +(i+1)+"-" +meName+ (i+1)+":"+mldsData.get(i).getMeanSum() +" ";
 			    	   log.debug(rankedResults);
 			    	   //rankedResults = rankedResults + (i+1) + ".) " + mldsData.get(i).extractorID + " " + mldsData.get(i).measureID + " ";
 			       }
 				
 				status              = MLDS_Status.DONE;
 				computationFinished = true;
 				}
 			}
 			else if( method.equals("probabilistic") ){//chen et al's method
 								
 				//for each mldsData compute the PE value and get the best 5
 				double[] PE_vals       = new double[mldsData.size()];
 				Double smallestPE      = Double.MAX_VALUE;
 				Iterator<MLDSInfo> eid = mldsData.iterator();
 				String extractorID_d = "";
 				String measureID_d   = "";
 				//if(checkIfComplete()){
 				if(state){
 					log.debug("ALL COMPARISONS DONE");
 					log.debug("mldsData.size()="+mldsData.size());
 				while( eid.hasNext() ){
 					
 					MLDSInfo dsTuple = eid.next();		
 					log.debug("Extractor id="+dsTuple.getExtractorID()+" Measure ID="+dsTuple.getMeasureID());
 					Double val       = new Double( computeComparisonPEValue( dsTuple.getComparisons() ) );
 					log.debug("PEVal="+val);
 					dsTuple.setPEVal(val);
 					
 					if( val.doubleValue() < smallestPE.doubleValue()){
 						smallestPE    = val;
 						extractorID_d = dsTuple.getExtractorID();
 						measureID_d   = dsTuple.getMeasureID();
 					}
 				}
 				
 				decidedMethod = extractorID_d + " " + measureID_d;
 				
 		       Collections.sort(mldsData,new Comparator<MLDSInfo>() {
 		            public int compare(MLDSInfo info1, MLDSInfo info2) {
 		                return info1.getPEVal().compareTo( info2.getPEVal() );
 		            }
 		        });
 				
 		       //return top 5 or 25% of methods
 		       int numTopMethods = (int)(mldsData.size() * 0.30);
 		       
 		       if( numTopMethods > 5 ){
 		    	   numTopMethods = 5;
 		       }
 		       
 		       for( int i=0; i<=numTopMethods; i++){
 		    	   int exIn=mldsData.get(i).extractorID.lastIndexOf('.');
 		    	   String exName=mldsData.get(i).extractorID.substring(exIn+1);
 		    	   int meIn=mldsData.get(i).measureID.lastIndexOf('.');
 		    	   String meName=mldsData.get(i).measureID.substring(meIn+1);
 		    	   
 		    	  // rankedResults = rankedResults + (i+1) + ".) " + decisionSupportData.get(i).extractorID + " " + decisionSupportData.get(i).measureID + " ";
 		    	   rankedResults = rankedResults + (i+1) + ".) " + exName +(i+1)+"-" +meName+ (i+1)+":"+mldsData.get(i).getPEVal() +" ";
 		    	   log.debug(rankedResults);
 		    	   
 		    	   
 		    	   //rankedResults = rankedResults + (i+1) + ".) " + mldsData.get(i).extractorID + " " + mldsData.get(i).measureID + " ";
 		       }
 				status              = MLDS_Status.DONE;
 				computationFinished = true;
 				}
 			}
 		}//end if (for completion check)
 	}
 	
 	private double computeComparisonPEValue(ArrayList<ArrayList<String>> comparisons){//PE Val for each EM Pair
 		
 		Injector injector                       = Guice.createInjector(new RepositoryModule());
 		ComparisonServiceImpl comparisonService = injector.getInstance(ComparisonServiceImpl.class);
 
 		//get all the values (distances)
 		ArrayList<double[]> values = new ArrayList<double[]>();
 
 		double max = 0;
 		double min = Double.MAX_VALUE;
 		log.debug("EachPair: comparisons.size= "+comparisons.size());
 		for( int i=0; i<comparisons.size(); i++){
 			
 			ArrayList<String> kComps = comparisons.get(i);
 			double[] kVals           = new double[kComps.size()];
 			//Double[] kVals      =new Double[kComps.size()];
 			for( int k=0; k<kComps.size(); k++){
 				
 				String value =  ((Comparison) comparisonService.getComparison( kComps.get(k))).getValue();
 				log.debug("i="+i+" k="+k+" kComps.size="+kComps.size()+" value="+value);
 				kVals[k] = Double.parseDouble( value );
 				
 				if( kVals[k] > max){ 
 					max = kVals[k];
 				}
 				if( kVals[k] < min){ 
 					min = kVals[k];
 				}
 			}
 			values.add(kVals);
 			//values.addAll(kVals);
 		}
 
 		//integration using the gaussian kernels over the distance support 
 		//max+=2; //account for sigma in integration, do not understand the justification
 		//min+=-2;
 		double perrorComplement = 0;
 		double resolution       = 0.10;
 		int range               = (int)((max-min)/resolution);
 		double integral         = 0;
 		double hopt=0.10;
 		double x1=min;
 		
 		
 		for( int x=0; x<range; x++){
 			
 			double maxPt = 0;
 			
 			double [] fnvalue=new double[values.size()]; //added
 			Arrays.fill(fnvalue, 0);
 			for( int i=0; i<values.size(); i++){ //for each label
 				
 				double[] kVals = values.get(i); //all distance values for each label
 				
 				for(int s=0; s<kVals.length; s++){
 					fnvalue[i]= fnvalue[i] + Math.exp( -1*Math.pow( (double)x1 -kVals[s],2)/(2.0*hopt*hopt) );
 					//integral += integral + Math.exp( -1*Math.pow( (double)x -kVals[s],2) );
 					
 					//if(integral > maxPt){
 					//	maxPt = integral;
 					//}
 				}
 			}
 			double fnmin=Double.MAX_VALUE;
 			for(int i=0;i<values.size();i++){
 				fnvalue[i]=(1.0/(hopt*(values.get(i).length)*Math.sqrt(2.0*Math.PI)))*fnvalue[i];
 				if(fnmin>fnvalue[i]){
 					fnmin=fnvalue[i];
 				}
 			}
 			
 			perrorComplement += fnmin*resolution;
 			
 			x1=x1+hopt;
 			//perrorComplement += maxPt;
 		}//end x
 				
 		return 1-perrorComplement;
 	}
 		
 	public class MLDSInfo {
 		
 		private String            			 adapterID;
 		private String            			 extractorID;
 		private String            			 measureID;
 		private double[]          			 means;
 		private ArrayList<ArrayList<String>> comparisons;
 		private Double 				 		 meanSum;
 		private Double            			 PE_Value;
 		public MLDSInfo(){}
 		
 		public MLDSInfo(String a, String e, String m){
 			this.adapterID   = a;
 			this.extractorID = e;
 			this.measureID   = m;
 		}		
 		
 		public Double getPEVal(){
 			return PE_Value;
 		}
 		
 		public void setPEVal(double val){
 			this.PE_Value= val;
 		}
 		
 		/**
 		 * Setter function for adapterID member.
 		 * @param a ID of the Adapter
 		 */
 		public void setAdapterID(String a){
 			adapterID = a;
 		}
 		/**
 		 * Getter function for adapterID member.
 		 * @return The ID of the Adapter.
 		 */
 		public String getAdapterID(){
 			return adapterID;
 		}	
 		/**
 		 * Setter function for extractorID member.
 		 * @param The ID of the Extractor.
 		 */
 		public void setExtractorID(String e){
 			extractorID = e;
 		}
 		/**
 		 * Getter function for extractorID member.
 		 * @return The ID of the Extractor.
 		 */
 		public String getExtractorID(){
 			return extractorID;
 		}
 		/**
 		 * Setter function for measureID member.
 		 * @param The ID of the Measure.
 		 */
 		public void setMeasureID(String m){
 			measureID = m;
 		}
 		/**
 		 * Getter function for measureID member.
 		 * @return The ID of the Measure.
 		 */
 		public String getMeasureID(){
 			return measureID;
 		}
 		
 		/**
 		 * Setter function for the Comparison list.
 		 * @param The ArrayList of comparison IDs generated by the Comparison service resource.
 		 */
 		public void setComparisons(ArrayList<ArrayList<String>> c){
 			comparisons = c;
 		}
 		/**
 		 * Getter function for the Comparison list.
 		 * @param The ArrayList of comparison IDs generated by the Comparison service resource.
 		 */
 		public ArrayList<ArrayList<String>> getComparisons(){
 			return comparisons;
 		}
 		
 		public void setMeans(double[] means){
 			this.means = means;
 		}
 		
 		public double[] getMeans(){
 			return means;
 		}
 
 		public void setMeanSum(double meanSum) {
 			this.meanSum = meanSum;
 		}
 		public Double getMeanSum(){
 			return meanSum;
 		}
 		
 	}
 
 	public String getBestResultsList() {
 		
 		return rankedResults;
 	}
 
 }
