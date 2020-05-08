 package fr.univ_nantes.alma.archtool.dot;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import fr.univ_nantes.alma.archtool.architectureModel.Architecture;
 import fr.univ_nantes.alma.archtool.architectureModel.Component;
 import fr.univ_nantes.alma.archtool.architectureModel.Connector;
 import fr.univ_nantes.alma.archtool.architectureModel.Facade;
 import fr.univ_nantes.alma.archtool.architectureModel.Interface;
 import fr.univ_nantes.alma.archtool.sourceModel.ComplexType;
 import fr.univ_nantes.alma.archtool.sourceModel.File;
 import fr.univ_nantes.alma.archtool.sourceModel.Function;
 import fr.univ_nantes.alma.archtool.sourceModel.GlobalVariable;
 import fr.univ_nantes.alma.archtool.sourceModel.SourceCode;
 import fr.univ_nantes.alma.archtool.utils.Pair;
 
 public class DotGraph
 {
 	private StringBuffer graph = new StringBuffer();
 	
 	public String getGraph()
 	{
 		return this.graph.toString();
 	}
 	
 	public void createGraph(SourceCode sourceCode)
     {
         this.graph.setLength(0);        
         this.graph.append("digraph G{graph [style=\"rounded,"
                 + " filled\", color=black, fillcolor=\"#CCE5FF\", fontsize=11, "
                 + "fontname=Helvetica];node [shape=plaintext];");
         
         int fileIndex = 0;
                 
         for(File sourceFile : sourceCode.getSourceFiles())
         {           
             this.graph.append("subgraph ");
             this.graph.append("cluster" + fileIndex + "{");
             this.graph.append("label = \"" + sourceFile.getPath() + "\";");
                 
             for(Function f : sourceCode.getFileFunctions(sourceFile))
             {
                 this.graph.append(f.getName() + "[fontcolor=blue];");
             }
                 
             for(ComplexType t : sourceCode.getFileComplexTypes(sourceFile))
             {
                 this.graph.append(t.getName() + "[fontcolor=green];");
             }
                 
             for(GlobalVariable v : 
                 sourceCode.getFileGlobalVariables(sourceFile))
             {
                 this.graph.append(v.getName() + "[fontcolor=red];");
             }
 
             this.graph.append("}");
             fileIndex++;
         }
                         
         for(Function f : sourceCode.getCoreFunctions())
         {
             for(Entry<Function, Integer> fUse : 
                 f.getCoreCalledFunctions().entrySet())
             {
                 this.graph.append(f.getName() + " -> " +
                         fUse.getKey().getName() + "[label=\"" + fUse.getValue()
                         + "\"];");
             }
             
             for(Entry<ComplexType, Integer> tUse : 
                 f.getCoreComplexTypes().entrySet())
             {
                 this.graph.append(f.getName() + " -> " +
                         tUse.getKey().getName() + "[label=\"" + tUse.getValue()
                         + "\"];");
             }
             
             for(Entry<GlobalVariable, Integer> vUse : 
                 f.getCoreGlobalVariables().entrySet())
             {
                 this.graph.append(f.getName() + " -> " +
                         vUse.getKey().getName() + "[label=\"" + vUse.getValue()
                         + "\"];");
             }
         }
             
         for(GlobalVariable v : sourceCode.getCoreGlobalVariables())
         {
             if(v.getType().isComplex() &&
                     sourceCode.getCoreComplexTypes().contains(
                     (ComplexType) v.getType()))
             {
                 this.graph.append(v.getName() + " -> " +
                        v.getType().getName() + ";");
             }
         }
 
         this.graph.append("}");
     }
 	
 	public void createGraph(Architecture architecture)
 	{
 	    Map<Component, String> compToIds = new HashMap<Component, String>();
 	    Map<Connector, String> conToIds = new HashMap<Connector, String>();
 	    Map<Interface, String> itfToIds = new HashMap<Interface, String>();
         Map<Facade, String> facToIds = new HashMap<Facade, String>();
         Map<Function, String> compFctToIds = new HashMap<Function, String>();
         Map<ComplexType, String> compTpfToIds = new HashMap<ComplexType, String>();
         Map<GlobalVariable, String> compGvToIds =
                 new HashMap<GlobalVariable, String>();
         Map<Function, String> conFctToIds = new HashMap<Function, String>();
         Map<ComplexType, String> conTpfToIds = new HashMap<ComplexType, String>();
         Map<GlobalVariable, String> conGvToIds =
                 new HashMap<GlobalVariable, String>();
 	    
 	    this.graph.setLength(0);	    
 		this.graph.append("digraph G{compound=true;graph [style=\"rounded,"
 				+ " filled\", color=black, fillcolor=\"#CCE5FF\", fontsize=11, "
 				+ "fontname=Helvetica];node [shape=plaintext];");
 		
 		int nodeIndex = 0;
 		String nodeName;
 		int compIndex = 0;
 		int conIndex = 0;
 		String clusterName;
 		
 		for(Component c : architecture.getComponents())
 		{
 			Set<Function> functionsInInterfaces = new HashSet<Function>();
 			Set<ComplexType> typesInInterfaces = new HashSet<ComplexType>();
 			Set<GlobalVariable> globalsInInterfaces =
 					new HashSet<GlobalVariable>();
 			
 			this.graph.append("subgraph ");
 			
 			clusterName = "cluster_comp" + compIndex;
 			compToIds.put(c, clusterName);
 			
 			this.graph.append(clusterName + "{");
 			this.graph.append("label = \"Composant" + compIndex++ + "\";");
			
			int itfIndex = 0;
			
 			for(Interface provided : c.getProvidedInterfaces())
 			{
 				this.graph.append("subgraph ");
 				clusterName = "cluster_itf" + itfIndex++;
 				itfToIds.put(provided, clusterName);
 				this.graph.append(clusterName + "{");
 				this.graph.append("label = \"Fournit\";");
 				this.graph.append("fillcolor = \"#FFB266\";");
 				
 				for(Function f : provided.getFunctions())
 				{
 				    nodeName = "n" + nodeIndex++;
 				    compFctToIds.put(f, nodeName);
 					this.graph.append(nodeName+ "[label= " 
 					        + f.getName() + " ,fontcolor=blue];");
 				}
 				
 				for(ComplexType t : provided.getComplexTypes())
 				{
 				    nodeName = "n" + nodeIndex++;
 				    compTpfToIds.put(t, nodeName);
 				    this.graph.append(nodeName + "[label= " 
                             + t.getName() + " ,fontcolor=green];");
 				}
 				
 				for(GlobalVariable v : provided.getGlobalVariables())
 				{
 				    nodeName = "n" + nodeIndex++;
 				    compGvToIds.put(v, nodeName);
 				    this.graph.append(nodeName + "[label= " 
                             + v.getName() + " ,fontcolor=red];");
 				}
 				
 				functionsInInterfaces.addAll(provided.getFunctions());
 				typesInInterfaces.addAll(provided.getComplexTypes());
 				globalsInInterfaces.addAll(provided.getGlobalVariables());
 				
 				this.graph.append("}");
 			}
 			
 			// Create nodes for entities out of interfaces
 			Set<Function> functionsOutInterfaces = c.getFunctions();
 			Set<ComplexType> typesOutInterfaces = c.getComplexTypes();
 			Set<GlobalVariable> globalsOutInterfaces = c.getGlobalVariables();
 			
 			functionsOutInterfaces.removeAll(functionsInInterfaces);
 			typesOutInterfaces.removeAll(typesInInterfaces);
 			globalsOutInterfaces.removeAll(globalsInInterfaces);
 			
 			for(Function f : functionsOutInterfaces)
 			{
 			    nodeName = "n" + nodeIndex++;
 			    compFctToIds.put(f, nodeName);
 			    this.graph.append(nodeName + "[label= " 
                         + f.getName() + " ,fontcolor=blue];");
 			}
 			
 			for(ComplexType t : typesOutInterfaces)
 			{
 			    nodeName = "n" + nodeIndex++;
 			    compTpfToIds.put(t, nodeName);
 			    this.graph.append(nodeName + "[label= " 
                         + t.getName() + " ,fontcolor=green];");
 			}
 			
 			for(GlobalVariable v : globalsOutInterfaces)
 			{
 			    nodeName = "n" + nodeIndex++;
 			    compGvToIds.put(v, nodeName);
 			    this.graph.append(nodeName + "[label= " 
                         + v.getName() + " ,fontcolor=red];");
 			}
 			
 			this.graph.append("}");
 		}
 		
 		for(Connector con : architecture.getConnectors())
 		{
 		    this.graph.append("subgraph ");
 		    clusterName = "cluster_con" + conIndex;
 		    conToIds.put(con, clusterName);
             this.graph.append(clusterName + "{");
             this.graph.append("label = \"Connecteur" + conIndex++ + "\";");
            
            int facIndex = 0;
            
             for(Facade facade : con.getFacades())
             {
                 this.graph.append("subgraph ");
                 clusterName = "cluster_fac" + facIndex++;
                 facToIds.put(facade, clusterName);
                 this.graph.append(clusterName + "{");
                 this.graph.append("label = \"Façade\";");
                 this.graph.append("fillcolor = \"#FFB266\";");
                 
                 for(Function f : facade.getFunctions())
                 {
                     nodeName = "n" + nodeIndex++;
                     conFctToIds.put(f, nodeName);
                     this.graph.append(nodeName + "[label= " 
                             + f.getName() + " ,fontcolor=blue];");
                 }
                 
                 for(ComplexType t : facade.getComplexTypes())
                 {
                     nodeName = "n" + nodeIndex++;
                     conTpfToIds.put(t, nodeName);
                     this.graph.append(nodeName + "[label= " 
                             + t.getName() + " ,fontcolor=green];");
                 }
                 
                 for(GlobalVariable v : facade.getGlobalVariables())
                 {
                     nodeName = "n" + nodeIndex++;
                     conGvToIds.put(v, nodeName);
                     this.graph.append(nodeName + "[label= " 
                             + v.getName() + " ,fontcolor=red];");
                 }
                 
                 this.graph.append("}");
             }
             
             for(Function f : con.getFunctions())
             {
                 nodeName = "n" + nodeIndex++;
                 conFctToIds.put(f, nodeName);
                 this.graph.append(nodeName + "[label= " 
                         + f.getName() + " ,fontcolor=blue];");
             }
             
             for(ComplexType t : con.getComplexTypes())
             {
                 nodeName = "n" + nodeIndex++;
                 conTpfToIds.put(t, nodeName);
                 this.graph.append(nodeName + "[label= " 
                         + t.getName() + " ,fontcolor=green];");
             }
             
             for(GlobalVariable v : con.getGlobalVariables())
             {
                 nodeName = "n" + nodeIndex++;
                 conGvToIds.put(v, nodeName);
                 this.graph.append(nodeName + "[label= " 
                         + v.getName() + " ,fontcolor=red];");
             }
             
             this.graph.append("}");
 		}
 		
 		for(Pair<Connector, Component> connection : 
 		    architecture.getConfiguration().getConnections())
 		{
 		    String componentCluster = compToIds.get(connection.second);
 		    String connectorCluster = conToIds.get(connection.first);
 		    
 		    String componentNode;
 		    String connectorNode;
 		    
 		    if(!connection.second.getFunctions().isEmpty())
 		    {
 		        Iterator<Function> iter = 
 		                connection.second.getFunctions().iterator();
 		        
 		        Function f = iter.next();
 		        componentNode = compFctToIds.get(f);
 		    }
 		    else if(!connection.second.getComplexTypes().isEmpty())
 		    {
 		        Iterator<ComplexType> iter = 
                         connection.second.getComplexTypes().iterator();
                 
 		        ComplexType t = iter.next();
                 componentNode = compTpfToIds.get(t);
 		    }
 		    else
 		    {
 		        Iterator<GlobalVariable> iter = 
                         connection.second.getGlobalVariables().iterator();
                 
 		        GlobalVariable v = iter.next();
                 componentNode = compGvToIds.get(v);
 		    }
 		    
 		    Facade fac = connection.first.getFacades().iterator().next();
 		    
 		    
 		    if(!fac.getFunctions().isEmpty())
             {
                 Iterator<Function> iter = 
                         fac.getFunctions().iterator();
                 
                 Function f = iter.next();
                 connectorNode = conFctToIds.get(f);
             }
             else if(!fac.getComplexTypes().isEmpty())
             {
                 Iterator<ComplexType> iter = 
                         fac.getComplexTypes().iterator();
                 
                 ComplexType t = iter.next();
                 connectorNode = conTpfToIds.get(t);
             }
             else
             {
                 Iterator<GlobalVariable> iter = 
                         fac.getGlobalVariables().iterator();
                 
                 GlobalVariable v = iter.next();
                 connectorNode = conGvToIds.get(v);
             }
 		   
 		    System.out.println();
 		    
 		    this.graph.append(componentNode + "->" + connectorNode + "[ltail=" + componentCluster +", lhead=" + connectorCluster + "];");
 		}
 		
 		this.graph.append("}");
 	}	
 	
 	public void writeToFile(String filename)
 	{
 	    BufferedWriter writer = null;
 	    
 	    try
 	    {
 	        writer = new BufferedWriter(new FileWriter(filename));
 	        writer.write(this.graph.toString());
 	        writer.close();
 	    }
 	    catch(IOException e)
 	    {
 	        e.printStackTrace();
 	    }
 	    finally
 	    {
 	        if(writer != null)
 	        {
 	            try
 	            {
 	                writer.close();
 	            }
 	            catch(IOException e)
 	            {
 	            }
 	        }
 	    }
 	}
 }
