 package com.kdcloud.server.engine.embedded;
 
import java.io.Serializable;
 import java.util.Set;
 
 import com.kdcloud.server.entity.ServerParameter;
 
public interface Node extends Serializable {
 	
 	public void setInput(PortObject input) throws WrongConnectionException;
 	
 	public PortObject getOutput();
 	
 	public Set<ServerParameter> getParameters();
 	
 	public void configure(WorkerConfiguration config) throws WrongConfigurationException;
 	
 	public void run() throws Exception;
 
 }
