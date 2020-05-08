 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.passerelle.actors.hardware;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import org.dawb.common.util.SubstituteUtils;
 import org.dawb.common.util.io.IFileUtils;
 import org.dawb.passerelle.actors.hardware.command.CommandBean;
 import org.dawb.passerelle.actors.hardware.command.CommandContainer;
 import org.dawb.passerelle.actors.hardware.command.CommandParameter;
 import org.dawb.passerelle.common.actors.AbstractDataMessageTransformer;
 import org.dawb.passerelle.common.message.DataMessageComponent;
 import org.dawb.passerelle.common.message.DataMessageException;
 import org.dawb.passerelle.common.message.ISubstitutionEditor;
 import org.dawb.passerelle.common.message.IVariable;
 import org.dawb.passerelle.common.message.MessageUtils;
 import org.dawb.passerelle.common.message.SubstitutionParticipant;
 import org.dawb.passerelle.common.parameter.ParameterUtils;
 import org.dawb.tango.extensions.TangoUtils;
 import org.dawb.tango.extensions.factory.TangoConnection;
 import org.dawb.tango.extensions.factory.TangoConnectionEvent;
 import org.dawb.tango.extensions.factory.TangoConnectionFactory;
 import org.dawb.tango.extensions.factory.TangoConnectionListener;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.gmf.runtime.common.core.util.StringUtil;
 import org.eclipse.ui.IWorkbenchPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ptolemy.kernel.CompositeEntity;
 import ptolemy.kernel.util.Settable;
 
 import com.isencia.passerelle.actor.ProcessingException;
 import com.isencia.passerelle.util.ptolemy.ResourceParameter;
 import com.isencia.passerelle.workbench.model.actor.IPartListenerActor;
 import com.isencia.passerelle.workbench.model.actor.IResourceActor;
 import com.isencia.passerelle.workbench.model.actor.ResourceObject;
 
 /**
  * This actor sets or gets a motor value, if set then value should be set
  * (expands allowed) and if get then motor name should be set only.
  * 
  * In  id11/spec/matt ::ExecuteCmd() ['eval(\'COLLECT_SEQ["kappaStart"] = -9999\')']
  * 
  * @author gerring
  *
  */
 public class TangoCommandTransformer extends AbstractDataMessageTransformer implements IResourceActor, SubstitutionParticipant, IPartListenerActor {
 	
 	private static final Logger logger = LoggerFactory.getLogger(TangoCommandTransformer.class);
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5335673681296652024L;
 
 	private CommandParameter    cmdParam;
 	private ResourceParameter   specFileParam;
 
 	public TangoCommandTransformer(CompositeEntity container, String name) throws Exception {
 		
 		super(container, name);
 		
 		cmdParam  = new CommandParameter(this,"Commands");
 		registerConfigurableParameter(cmdParam);
 		
 		specFileParam = new ResourceParameter(this, "Spec Macro", "Spec Macro", "*.mac", "*.txt");
 		registerConfigurableParameter(specFileParam);
 	
 		memoryManagementParam.setVisibility(Settable.NONE);
 		dataSetNaming.setVisibility(Settable.NONE);
 	}
 
 	protected int getMinimumCacheSize() {
 		return 0;
 	}
 	
 	@Override
 	protected DataMessageComponent getTransformedMessage(List<DataMessageComponent> cache) throws ProcessingException {
 		
 		try {
 			final CommandContainer cont = (CommandContainer)cmdParam.getBeanFromValue(CommandContainer.class);
 	    	final DataMessageComponent comp = MessageUtils.mergeAll(cache);
 		    
 	    	if (cont == null) {
 		    	comp.putScalar("error_messsage", "No motors configured, '"+getName()+"' did nothing.");
 		    	return comp;
 		    }
 	    	
 	    	runMacro(getResource(false), comp);
 		    
 		    final Map<String,String> values = runCommands(cont, comp);
 	    	if (values!=null) comp.addScalar(values);
 		
 	        return comp;
 	        
 		} catch (Exception e) {
 			throw createDataMessageException("Cannot run command!", e);
 		}
 	    
 	}
 
 	private boolean runMacro(final IResource resource, final DataMessageComponent comp) throws CoreException, Exception {
 		
         if (!resource.exists())           return false;
         if (!(resource instanceof IFile)) return false;
         final IFile file = (IFile)resource;
         
 		final String macroFile = SubstituteUtils.substitute(file.getContents(), comp.getScalar());
 		if (macroFile==null || "".equals(macroFile.trim())) return false;
 		
 		final String[] cmds = macroFile.split("\n");
 		if (cmds==null || cmds.length<1) return false;
 		
 		final List<String> commands  = new ArrayList<String>(cmds.length);
 		for (int i = 0; i < cmds.length; i++) { // Not ideal but # must be ignored
 			final String line = cmds[i].trim();
 			if (line.startsWith("#")) continue;
 			commands.add(line);
 		}
 
 		if (commands.isEmpty()) return false;
 		
 		logger.info("Running macro '"+resource.getLocation().toOSString()+"'");
 		final String     hardwareURI = TangoUtils.getSpecCommandAddress();
 		TangoConnection  connection	 = null;	
 		try {
 			connection  = TangoConnectionFactory.openCommandConnection(hardwareURI);
 			
 			String sleepTime = System.getProperty("org.dawb.passerelle.actors.hardware.command.sleep.interval");
 			for (String cmd : commands) {
 				
 				logger.info(cmd);
 				connection.executeCommand("ExecuteCmd", cmd, false);
 				if (sleepTime!=null) {
 					Thread.sleep(Long.parseLong(sleepTime));
 				}
 			}
 			
 			return true;
 			
 		} catch (Exception e) {
 			throw createDataMessageException("Cannot connect to "+hardwareURI, e);
 		} finally {
 			if (connection!=null) {
 				try {
 					connection.dispose();
 				} catch (Exception e) {
 					throw createDataMessageException("Cannot dispose tango connection "+connection.getUri(), e);
 				}
 			}
 		}
 	}
 
 	private Map<String, String> runCommands(final CommandContainer cont, final DataMessageComponent comp) throws DataMessageException {
 	
 		final Map<String,StringBuilder> data = new HashMap<String,StringBuilder>(cont.size());
 		
 		for (final CommandBean cb : cont.getBeans()) {
 			
 			final String cmd          = SubstituteUtils.substitute(cb.getCommand(), comp.getScalar());
 			final String attrbuteName = cb.getAttributeName()!=null && !"".equals(cb.getAttributeName())
 			                          ? cb.getAttributeName()
 			                          : "Output";
 			
 			final String            hardwareURI = TangoUtils.getSpecCommandAddress();
 			TangoConnection         connection  = null;
 			TangoConnectionListener listener    = null;
 			try {
 
 				connection = TangoConnectionFactory.openMonitoredCommandConnection(hardwareURI, attrbuteName);
 				
 				String cmdAttribute = "ExecuteCmd";
 				if (cb.getCommandAttributeName()!=null&&!"".equals(cb.getCommandAttributeName())) {
 					cmdAttribute = cb.getCommandAttributeName();
 				}
 				listener = createTangoConnectionListener(cmd, cb, data);
 				connection.addTangoConnectionListener(listener);
 				
 				connection.executeCommand(cmdAttribute, cmd, false);
 				
 			} catch (Exception e) {
 				if (isMockMode()) {
 					throw createDataMessageException(e.getMessage(), e);
 				} else {
 				    throw createDataMessageException("Cannot connect to "+hardwareURI, e);
 				}
 			} finally {
 				if (connection!=null) {
 					connection.removeTangoConnectionListener(listener);
 					try {
 						connection.dispose();
 					} catch (Exception e) {
 						throw createDataMessageException("Cannot dispose tango connection "+connection.getUri(), e);
 					}
 				}
 			}
 		}
 		
 		final Map<String,String> ret = new HashMap<String,String>(cont.size());
 		for (String key : data.keySet()) {
 			ret.put(key, data.get(key).toString());
 		}
 		return ret;
 	}
 
 	private TangoConnectionListener createTangoConnectionListener(final String                     cmd,
 			                                                      final CommandBean                cb,
 			                                                      final Map<String, StringBuilder> data) {
 		
 		return new TangoConnectionListener() {
 			@Override
 			public void tangoEventPerformed(final TangoConnectionEvent event) {
 				StringBuilder buf = data.get(cb.getVariableName());
 				if (buf==null) {
 					buf = new StringBuilder();
 					data.put(cb.getVariableName(), buf);
 				}
 				if (event.getErrorMessage()!=null) {
 					buf.append("\n"+event.getErrorMessage());
 				}
 				try {
 					final String value = event.getValue().extractString().trim();
 					logger.debug(value);
 					buf.append("\n"+value);
 				} catch (Exception ne) {
 				    buf.append("\n"+TangoUtils.getMessageFromException(cmd, ne));
 				}
 			}
 		};
 	}
 
 	@Override
 	protected String getOperationName() {
 		return "Get/Set Motor";
 	}
 
 	@Override
 	protected String getExtendedInfo() {
 		return "Actor to set and get motor";
 	}
 	
 	@Override
 	public List<IVariable> getOutputVariables() {
 		
 		try {
 		    final List<IVariable>    ret  = super.getOutputVariables();
 			// TODO Add variables created from bean
 			
 			return ret;
 			
 		} catch (Exception e) {
 			logger.error("Cannot read variables", e);
 			return null;
 		}
 
 	}
 
 	@Override
 	public String getDefaultSubstitution() {
 		return "# Please insert spec macro contents here, and use the view on the left to insert variables.\n";
 	}
 
 	@Override
 	public void setMomlResource(IResource momlFile) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getResourceCount() {
 		return 1;
 	}
 
 	protected IResource getResource(final boolean setParameterValue) throws Exception {
 		
 		String path = specFileParam.getExpression();
 		IFile  file = null;
 		if (path==null || "".equals(path)) {
 			final IProject          project= getProject();
 			final IContainer        src    = project.getFolder("src");
 			file = IFileUtils.getUniqueIFile(src, "macro", "mac");
 			path   = file.getFullPath().toOSString();
 			path   = StringUtil.replace(path, "/"+file.getProject().getName()+"/", "/${project_name}/", true);
 			if (setParameterValue) specFileParam.setExpression(path);
 		}
 		if (ResourcesPlugin.getWorkspace().getRoot().findMember(path) != null)
 			if (ResourcesPlugin.getWorkspace().getRoot().findMember(path).exists()) {
 				file = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(path);
 			}		
 		if (file==null) {
 			path = ParameterUtils.substitute(path, this);
 			final IProject project= getProject();
 			final String     srcP = IFileUtils.getPathWithoutProject(path.substring(0,path.lastIndexOf('/')));
 			IContainer specDir  = (IContainer)project.findMember(srcP);
 			if (specDir==null) {
 				try {
 					IFolder srcf = project.getFolder(srcP);
 					specDir = srcf;
 				} catch (Exception ne) {
 					logger.error("Cannot create folder "+srcP, ne);
 				}
 			}
 			final String  fileName= path.substring(path.lastIndexOf('/'));
 			file = (IFile)specDir.findMember(fileName);
 			if (file==null&&specDir instanceof IProject) {
 				file = ((IProject)specDir).getFile(fileName);
 			} if (file==null&&specDir instanceof IFolder) {
 				file = ((IFolder)specDir).getFile(fileName);
 			}
 		}
 				
 		return file;
 	}
 
 	@Override
 	public ResourceObject getResource(int iresource) throws Exception {
 		if (iresource==0) {
 			final ResourceObject ret = new ResourceObject();
 			ret.setResource(getResource(false));
 			ret.setResourceTypeName("Spec Macro");
 			ret.setEditorId("org.dawb.passerelle.editors.substitutionEditor");
 			return ret;
 		}
 	    return null;	
 	}
 
 
 	@Override
 	public void partPreopen(ResourceObject ob) {
 		try {
 			final IFile file = (IFile)getResource(true);
 			if (!file.exists()) {
 	        	final InputStream is = new ByteArrayInputStream(getDefaultSubstitution().getBytes("UTF-8"));
 	        	
 	        	if (!file.getParent().exists() && file.getParent() instanceof IFolder) {
 	        		IFolder par = (IFolder)file.getParent();
 	        		par.create(true, true, new NullProgressMonitor());
 	        	}
 	        	
 	        	file.create(is, true, new NullProgressMonitor());
 	        	file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
 			}
 		} catch (Exception ne) {
 			logger.error("Cannot create file "+ob.getResourceTypeName(), ne);
 		}
 	}
 
 	@Override
 	public void partOpened(IWorkbenchPart part, ResourceObject ob) {
 		final ISubstitutionEditor ed = (ISubstitutionEditor)part;
 		ed.setSubstitutionParticipant(this);
 		part.setFocus();
 	}
 	
 }
