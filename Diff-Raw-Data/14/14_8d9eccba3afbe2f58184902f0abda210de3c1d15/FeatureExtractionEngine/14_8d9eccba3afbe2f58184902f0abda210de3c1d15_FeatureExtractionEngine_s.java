 package plugins.oeway.featureExtraction;
 
 import icy.gui.dialog.MessageDialog;
 import icy.gui.frame.progress.ProgressFrame;
 import icy.image.IcyBufferedImage;
 import icy.main.Icy;
 import icy.plugin.PluginDescriptor;
 import icy.plugin.PluginLoader;
 import icy.plugin.abstract_.Plugin;
 import icy.sequence.DimensionId;
 import icy.sequence.Sequence;
 import icy.sequence.SequenceUtil;
 import icy.type.DataType;
 import icy.util.ClassUtil;
 import icy.util.OMEUtil;
 import icy.util.StringUtil;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import plugins.adufour.blocks.lang.Block;
 import plugins.adufour.blocks.util.VarList;
 import plugins.adufour.ezplug.EzComponent;
 import plugins.adufour.ezplug.EzGroup;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzStoppable;
 import plugins.adufour.ezplug.EzVar;
 import plugins.adufour.ezplug.EzVarEnum;
 import plugins.adufour.ezplug.EzVarListener;
 import plugins.adufour.ezplug.EzVarSequence;
 import plugins.adufour.ezplug.EzVarText;
 import plugins.adufour.vars.lang.Var;
 import plugins.adufour.vars.util.VarListener;
 
 
 public class FeatureExtractionEngine extends EzPlug implements Block, EzStoppable
 {
 
 	protected final String INPUT_SEQUENCE_VAR = "Input(EzVarSequence)";
 	protected final String EXTRACT_AXIS = "ExtractAxis(EzVarEnum<DimensionId>)";
 	protected final String FEATURE_GROUPS = "FeatureGroups(String[])";
 	protected final String FEATURE_COUNT = "FeatureCount(int)";
 	protected final String FEATURE_DATA_TYPE = "FeatureDataType(Double)";
 	protected final String MAXIMUM_ERROR_COUNT = "MaximumErrorCount(int)";
 	protected final String OUTPUT_SEQUENCE_VAR = "Output(EzVarSequence)";
 	
     
     private final EzVarSequence                  input          = new EzVarSequence("Input Sequence");
     
    private final EzVarEnum<DimensionId> extractDir  = new EzVarEnum<DimensionId>("Extract Along", DimensionId.values(), DimensionId.Z);
     
    private final EzVarEnum<DimensionId> concatDir  = new EzVarEnum<DimensionId>("Output Concat Mode", DimensionId.values(), DimensionId.C);
     
     private final EzVarSequence                    outputVar         = new EzVarSequence("Output Sequence");
     
     private EzGroup featureFuncOptions = new EzGroup("Options");
     
     
     //private VarList  featureFuncOptionsVarList = new VarList();
     private VarList inputMap_ = null;
     
     LinkedHashMap<String,Object> optionDict = new LinkedHashMap<String,Object>();
     ArrayList<Object> guiList = new ArrayList<Object> ();
     
     private HashMap<String,Class<? extends FeatureExtractionFunction>> pluginList ;
     
     final EzVarText featureFuncVar= new EzVarText("Extraction Function", new String[]{}, 0, false);
     
     public FeatureExtractionFunction selectedExtractionFunc;
 	boolean						stopFlag = false;
 	int maxErrorCount = -1;
 	int featureCount = -1;
     DataType outputDataType = DataType.DOUBLE;
     String[] groupNames = new String[]{""};
     
     String lastfeatureFuncVar = "";
     public void createFeatureFunc() throws InstantiationException, IllegalAccessException{
     	
     	if(lastfeatureFuncVar.equals(featureFuncVar.getValue()))
     		return;
     	
 		selectedExtractionFunc = pluginList.get(featureFuncVar.getValue()).newInstance();
 		featureFuncOptions.components.clear();
 
 		for(Object o:guiList){
 			if(o instanceof EzVar<?>)
 			{
 				EzVar<?> v= (EzVar<?>)o;
 				if(inputMap_!=null)
 					if(inputMap_.contains( v.getVariable()))
 						inputMap_.remove(v.getVariable());
 			}
 		}
 			
 		optionDict.clear();
 		guiList.clear();
 		optionDict = createConfigurations();
 		try
 		{
 			selectedExtractionFunc.initialize(optionDict,guiList);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		//featureFuncOptions.addEzComponent(featureFuncOptions);
 		updateFromConfigurations();
 		
 		for(Object o:guiList){
 			if(o instanceof EzVar<?>)
 			{
 				EzVar<?> v= (EzVar<?>)o;
 				if(inputMap_!=null)
 					if(!inputMap_.contains( v.getVariable()))
 						inputMap_.add(( v).getVariable());	
 			}
 			if(o instanceof EzComponent)
 			{
 				EzComponent v2= (EzComponent)o;
 				//if(!mainGroup.components.contains(v))
 				featureFuncOptions.addEzComponent( v2);
 			}
 		}
 		lastfeatureFuncVar = featureFuncVar.getValue();
 		
 		featureFuncOptions.setVisible(featureFuncOptions.components.size()>0);
 			
     }
 
     public void buildFeatureFuncList()
     {
     	pluginList = getPluginList();
     	String tmp[] = new String[pluginList.size()];
     	int i=0;
     	for (Iterator<String> iter = pluginList.keySet().iterator(); iter.hasNext();) {
     		tmp[i++]=(String) iter.next();
     	}
     	featureFuncVar.setDefaultValues(tmp, 0, false);
     	//featureFuncVar = new EzVarText("Extraction Function", tmp, 0, false);
         featureFuncVar.addVarChangeListener(new EzVarListener<String>(){
 			@Override
 			public void variableChanged(EzVar<String> source, String newValue) {
 				try {
 					createFeatureFunc();	
 				} catch (Exception e1) {
 					e1.printStackTrace();
 					return ;
 				}
 			}
         });
     	
         featureFuncVar.getVariable().addListener(new VarListener<String>(){
 
 			@Override
 			public void valueChanged(Var<String> source, String oldValue,
 					String newValue) {
 				try {
 					createFeatureFunc();
 			    	
 				} catch (Exception e1) {
 					e1.printStackTrace();
 					return ;
 				}
 			}
 
 			@Override
 			public void referenceChanged(Var<String> source,
 					Var<? extends String> oldReference,
 					Var<? extends String> newReference) {
 				try {
 					createFeatureFunc();
 			    	
 				} catch (Exception e1) {
 					e1.printStackTrace();
 					return ;
 				}
 			}
         	
         });
         if(inputMap_!= null){
         	
         }
     }
 	
     
     @Override
     protected void initialize()
     {
     	getUI().setParametersIOVisible(false);
     	addEzComponent(input);
     	addEzComponent(extractDir);
     	addEzComponent(featureFuncVar); 
     	addEzComponent(featureFuncOptions);
     	addEzComponent(concatDir);
     	buildFeatureFuncList();
 
     }
     
     @Override
     protected void execute()
     {
     	stopFlag = false;
         if(selectedExtractionFunc==null)
         {
 			MessageDialog.showDialog("Extraction Function is not available",
 					MessageDialog.ERROR_MESSAGE);
 			return ;
         } 
         if(extractDir.getValue()==DimensionId.NULL)
         {
         	extractDir.setValue(DimensionId.Z);
 			MessageDialog.showDialog("Unsupported extraction direction.",
 					MessageDialog.ERROR_MESSAGE);
 			return ;
         } 
         
     	Sequence[] seqs = Extract(input.getValue(true), true);
     	if(concatDir.getValue() != DimensionId.NULL)
     		outputVar.setValue(mergeSequences(seqs,concatDir.getValue()));
     	else
     		outputVar.setValue(mergeSequences(seqs,DimensionId.C));
     	
         if (getUI() != null)
         {
         	if(concatDir.getValue() != DimensionId.NULL)
         		addSequence(outputVar.getValue());
         	else
         		for(Sequence seq:seqs)
         			addSequence(seq);
         }
         
     }
     
     @Override
     public void clean()
     {
         
     }
     
     public  HashMap<String,Class<? extends FeatureExtractionFunction>> getPluginList()
     {
     	 HashMap<String,Class<? extends FeatureExtractionFunction>> pl =  new HashMap<String,Class<? extends FeatureExtractionFunction>>();
     	 ArrayList<PluginDescriptor> plugins = PluginLoader.getPlugins(FeatureExtractionFunction.class, true, false, false);
          
          if (plugins.size() == 0)
          {
              return pl;
          }
          
          for (PluginDescriptor descriptor : plugins)
          {
              Class<? extends Plugin> clazz = descriptor.getPluginClass();
              try
              {
                  final Class<? extends FeatureExtractionFunction> funcClass = clazz.asSubclass(FeatureExtractionFunction.class);
 
                  if (ClassUtil.isAbstract(funcClass) || ClassUtil.isPrivate(funcClass)) continue;
                  String name = funcClass.getSimpleName();
                  name = descriptor.getName().equalsIgnoreCase(name) ? StringUtil.getFlattened(name) : descriptor.getName();
                  pl.put(name,funcClass);
 
              }
              catch (ClassCastException e1)
              {
              }
          }
          return pl;
          
     }
     
 	public  LinkedHashMap<String,Object>  createConfigurations(){
 		LinkedHashMap<String,Object> configurations = new LinkedHashMap<String,Object>();
 		
 		maxErrorCount = -1;
 		featureCount = -1;
 	    outputDataType = DataType.DOUBLE;
 	    groupNames = new String[]{""};
 	    
 	    configurations.put(INPUT_SEQUENCE_VAR, input);
 	    configurations.put(EXTRACT_AXIS, extractDir);
 		configurations.put(FEATURE_DATA_TYPE,outputDataType);
 		configurations.put(MAXIMUM_ERROR_COUNT, maxErrorCount );//new EzVarInteger("Maximum Error Count"));
 		configurations.put(FEATURE_GROUPS, groupNames);
 		configurations.put(FEATURE_COUNT,featureCount );
 		configurations.put(OUTPUT_SEQUENCE_VAR, outputVar);
 		return configurations;
 	}
 	public void  updateFromConfigurations(){
 
 	    outputDataType = (DataType) optionDict.get(FEATURE_DATA_TYPE);
 	    maxErrorCount = (int) optionDict.get(MAXIMUM_ERROR_COUNT);//, maxErrorCount );//new EzVarInteger("Maximum Error Count"));
 		groupNames = (String[]) optionDict.get(FEATURE_GROUPS);//, groupNames);
 		featureCount = (int) optionDict.get(FEATURE_COUNT);//,featureCount );
 
 	}
 
     /**
      * Performs a Z extraction of the input sequence using the specified algorithm. If the sequence
      * is already 2D, then a copy of the sequence is returned
      * 
      * @param sequence
      *            the sequence to extract
      * @param multiThread
      *            true if the process should be multi-threaded
      * @param restrictToROI
      *            <code>true</code> maps only data located within the sequence ROI,
      *            <code>false</code> maps the entire data set
      * @return the Extracted sequence
      */
 	public Sequence[] Extract(final Sequence in, boolean multiThread)
     {
        
         final FeatureExtractionFunction featureFunc = selectedExtractionFunc;
 
         try{
 			featureFunc.batchBegin();
 		}
 		catch(Exception e){
 			System.err.println("Error when excuting batchBegin().");
 			e.printStackTrace();
 		} 
         updateFromConfigurations();
         
    	 	int groupCount = groupNames.length;
   	 	
    	 	if(groupCount<1) groupCount = 1;
   	 	if(featureCount<1) featureCount = 1;
         
 		int error_exit_count = maxErrorCount;//((EzVarInteger) optionDict.get(MAXIMUM_ERROR_COUNT)).getValue();
 
     	SequenceAxisIterator input = new SequenceAxisIterator(in,extractDir.getValue());
     	//test output length
     	if(input.hasNext())
     	{
         	try{
         		double[] o=featureFunc.process(input.get(),input.getCursor());
                if(featureCount<1) featureCount = o.length/groupCount;
     		}
     		catch(Exception e){
     			System.err.println("Error when excuting process().");
     			e.printStackTrace();
     		}
     	}
     	
         int zz = in.getSizeZ();
         int xx = in.getSizeX();
         int yy = in.getSizeY();
         int tt = in.getSizeT();
         int cc = in.getSizeC();
         
         switch (extractDir.getValue())
         {
 	        case X:
 	        	xx = featureCount;
 	            break;
 	        case Y:
 	        	yy = featureCount;
 	            break;
             case T:
             	tt = featureCount;
 	            break;
             case Z:
             	zz = featureCount;
 	            break;
             case C:
             	cc = featureCount;
 	            break;
             default:
                 throw new UnsupportedOperationException("Direction not supported");
         }
         
 
         SequenceAxisIterator[] seqAIList = new SequenceAxisIterator[groupCount];
         
         for(int i=0;i<groupCount;i++)
         {
 	        final Sequence o = new Sequence(OMEUtil.createOMEMetadata(in.getMetadata()));
 	        o.setName(groupNames[i]);
 	        //Allocate output sequence 
 	    	for(int t=0; t<tt; ++t) {
 				for(int z=0; z<zz; ++z) {
 					o.setImage(t, z, new IcyBufferedImage(xx, yy, cc, outputDataType));
 					for(int c=0;c<cc;c++)
 		                o.setChannelName(c, in.getChannelName(c)+ "/"+groupNames[i]);
 	
 				}
 			}
 	    	SequenceAxisIterator sai = new SequenceAxisIterator(o,extractDir.getValue());
 	    	seqAIList[i] = sai;
         }
 
 		int cpu = Runtime.getRuntime().availableProcessors();
         // this plug-in processes each channel of each stack in a separate thread
         ExecutorService service = multiThread ? Executors.newFixedThreadPool(cpu) : Executors.newSingleThreadExecutor();
         ArrayList<Future<?>> futures = new ArrayList<Future<?>>(10);
         
 		for(SequenceAxisIterator sai:seqAIList)
 		{
 	        sai.getSequence().beginUpdate();
 		}
         
 		boolean errorExit = false;
 		final ProgressFrame pf = new ProgressFrame("Extracting features...");
     	while(input.hasNext() )
     	{
         	try{
     			double[] o = featureFunc.process(input.next(),input.getCursor());
     			int offset = 0;
         		for(SequenceAxisIterator sai:seqAIList)
         		{
         			if(sai.hasNext())
         				sai.setNext(o,offset);
         			offset+=featureCount;
         		}
     		}
     		catch(Exception e){
     			System.err.println("Error when excuting process().");
     			e.printStackTrace();
     			if(error_exit_count--<=0)
     			{
     				errorExit = true;
     				System.err.println("Maximum error count exceeded!");
     				break;
     			}
     		}
     	}
     	pf.close();
         try
         {
             for (Future<?> future : futures)
                 future.get();
         }
         catch (InterruptedException e)
         {
             e.printStackTrace();
         }
         catch (ExecutionException e)
         {
             e.printStackTrace();
         }
         
         service.shutdown();
     	try{
 			featureFunc.batchEnd();
 		}
 		catch(Exception e){
 			System.err.println("Error when excuting batchEnd().");
 			e.printStackTrace();
 			return null;
 		}
     	
     	if(errorExit){
     		MessageDialog.showDialog("Stoped because of error!",
 					MessageDialog.ERROR_MESSAGE);
     		return null;   
     	}
 
     	
     	Sequence[] seqList = new Sequence[seqAIList.length];
     	int i=0;
 		for(SequenceAxisIterator sai:seqAIList)
 		{
 			sai.getSequence().endUpdate();
 	        sai.getSequence().updateChannelsBounds(true);
 	        seqList[i++] = sai.getSequence();
 		}    	
 		
 		return seqList;
     }   
     
     public Sequence mergeSequences(final Sequence [] seqs, DimensionId dir)
     {
     	Sequence out;
         final ProgressFrame pf = new ProgressFrame("Merging sequences...");
         switch (dir)
         {
             default:
             case C:
                 out = SequenceUtil.concatC(seqs, true, true, pf);
                 break;
 
             case Z:
                 out = SequenceUtil.concatZ(seqs, true, true, true,pf);
                 break;
 
             case T:
                 out = SequenceUtil.concatT(seqs, true, true,true, pf);
                 break;
         }
         out.setName("Extraction of " + input.getValue().getName() +" along " + extractDir.getValue().toString());        
         pf.close();
         return out;
     }
   
     @Override
 	public void stopExecution()
 	{
 		stopFlag = true;
 	}
     @Override
     public String getName()
     {
         return "Feature Extraction Engine (1D)";
     }
     
     @Override
     public void declareInput(VarList inputMap)
     {   if(inputMap_==null)
         	inputMap_ = inputMap;
     	buildFeatureFuncList();
         inputMap.add("input", input.getVariable());
         inputMap.add("extract direction", extractDir.getVariable());
         inputMap.add("extraction function", featureFuncVar.getVariable());
     }
     
     @Override
     public void declareOutput(VarList outputMap)
     {
         outputMap.add("Output Sequence", outputVar.getVariable());
     }
     
 }
