 /*******************************************************************************
  * Copyright (c) 2004 Eteration Bilisim A.S.
  * All rights reserved.  This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Gorkem Ercan - initial API and implementation
  *     Naci M. Dai
  * 
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Eteration Bilisim A.S.  For more
  * information on eteration, please see
  * <http://www.eteration.com/>.
  ***************************************************************************/
 package org.eclipse.jst.server.generic.ui.internal;
 
 import java.util.Map;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jst.server.generic.core.internal.GenericServerRuntime;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.TaskModel;
 import org.eclipse.wst.server.core.model.RuntimeDelegate;
 import org.eclipse.wst.server.ui.wizard.IWizardHandle;
 
 /**
  * A serverDefinitionType aware wizard for defining runtimes.
  * 
  * @author Gorkem Ercan
  */
 public class GenericServerRuntimeWizardFragment extends ServerDefinitionTypeAwareWizardFragment {
 	
 	private ServerTypeDefinitionGroup fServerPanel;
 	private RuntimeDelegate fRuntimeDelegate;
     private Map fServerRuntimeProperties;
 	
 	/**
 	 * Constructor
 	 */
 	public GenericServerRuntimeWizardFragment() {
 		super();
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.ui.wizard.IWizardFragment#isComplete()
 	 */
 	public boolean isComplete() {
 	  	RuntimeDelegate runtime = getRuntimeDelegate();
 		if (runtime == null)
 			return false;
 		IStatus status = runtime.validate();
 		return (status != null && status.isOK());
 	}
 	
 	public void createContent(Composite parent, IWizardHandle handle) {
 		createServerDefinitionTypeComposite(parent);
 	}
 
 	
 
 	/**
      * @param selected
      */
     private void createServerDefinitionTypeComposite(Composite parent) {     
         Map properties= null;
         if(getRuntimeDelegate()!=null)
             properties = getRuntimeDelegate().getAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES,(Map)null);
         ServerRuntime definition = getServerTypeDefinition(getServerDefinitionId(),properties);
         fServerPanel = new ServerTypeDefinitionGroup(this,definition,ServerTypeDefinitionGroup.CONTEXT_RUNTIME,properties,parent);
     }
 	
 	private String getServerDefinitionId()
 	{
 		String currentDefinition= null;
 		if(getRuntimeDelegate()!=null)
 			currentDefinition =  getRuntimeDelegate().getRuntime().getRuntimeType().getId();
 		if(currentDefinition!= null && currentDefinition.length()>0)
 		{	
 			return currentDefinition;
 		}
 		return null;
 	}
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.ui.wizard.IWizardFragment#enter()
 	 */
 	public void enter() {
 		
         String serverDefinition = getServerDefinitionId();
 		if(serverDefinition!=null && serverDefinition.length()>0)
 		{
 		    Map properties = null;
 	        if (getRuntimeDelegate() != null) {
 	            properties = getRuntimeDelegate().getAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES,(Map) null);
 	        }
 	        ServerRuntime definition = getServerTypeDefinition(serverDefinition,properties);
 //	        fServerPanel.reset(definition,ServerTypeDefinitionGroup.CONTEXT_RUNTIME, properties);
 		}
         else{
             populateRuntimeDelegateProperties();
         }
         validate();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.ui.wizard.IWizardFragment#exit()
 	 */
 	public void exit() {
 	    try {
 	        getRuntimeDelegate().getRuntimeWorkingCopy().save(true,new NullProgressMonitor());
 	    }
 	    catch(Exception e){//unhandled
 	    }
         fRuntimeDelegate=null;
    }
 
 	protected Map getServerRuntimeProperties(){
 	    return fServerRuntimeProperties; 
 	}
 
 	private String createName()
 	{
 	    RuntimeDelegate dl = getRuntimeDelegate();
 	    IRuntimeType runtimeType = dl.getRuntime().getRuntimeType();
 	    String name = GenericServerUIMessages.getFormattedString("runtimeName", new String[] {runtimeType.getName()});
 		IRuntime[] list = ServerCore.getRuntimes();
 		int suffix = 1;
 		String suffixName=name;
 		for(int i=0;i<list.length;i++)
 	    {
 	        if(list[i].getName().equals(name)|| list[i].getName().equals(suffixName))
 	            suffix++;
 	        suffixName= name+" "+suffix;
 	    }
 	    
 		if(suffix>1)
 		    return suffixName;
 	    return name;
 	}
 	
 	private RuntimeDelegate getRuntimeDelegate()
 	{
 		if(fRuntimeDelegate == null)
 		{	
 		    IRuntimeWorkingCopy wc = (IRuntimeWorkingCopy)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
 		    if(wc==null)
 		        return null;
 		    fRuntimeDelegate = (RuntimeDelegate)wc.getAdapter(RuntimeDelegate.class);
 		}
 		return fRuntimeDelegate;
 	}
     /* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.ui.ServerDefinitionTypeAwareWizardFragment#description()
      */
     public String description() {
         String rName = getRuntimeName();
         if(rName == null || rName.length()<1)
             rName="Generic";      
         return  GenericServerUIMessages.getFormattedString("runtimeWizardDescription",new String[] {rName});
     }
     /* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.ui.ServerDefinitionTypeAwareWizardFragment#title()
      */
     public String title() {
         String rName = getRuntimeName();
         if(rName == null || rName.length()<1)
             rName="Generic";
        return GenericServerUIMessages.getFormattedString("runtimeWizardTitle",new String[]{rName});
     }
     
     private String getRuntimeName()
     {
        if(getRuntimeDelegate()!=null && getRuntimeDelegate().getRuntime()!=null)
             return getRuntimeDelegate().getRuntime().getName();
         return null;
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.ui.ServerDefinitionTypeAwareWizardFragment#serverDefinitionTypePropertiesChanged()
      */
     public void serverDefinitionTypePropertiesChanged() {
 		populateRuntimeDelegateProperties();
 		validate();
    }
     /**
      * 
      */
     private void populateRuntimeDelegateProperties() {
        
         fServerRuntimeProperties = fServerPanel.getProperties();
         Map properties = getServerRuntimeProperties();
         RuntimeDelegate dl = getRuntimeDelegate();
         dl.setAttribute(GenericServerRuntime.SERVER_DEFINITION_ID, getRuntimeDelegate().getRuntime().getRuntimeType().getId());
         dl.setAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES,properties);
         dl.getRuntimeWorkingCopy().setName(createName());
     }
     
     private void validate()
     {
 		if (getRuntimeDelegate() == null) {
 			this.getWizard().setMessage("", IMessageProvider.ERROR);
 			return;
 		}
 		IStatus status = getRuntimeDelegate().validate();
 		if (status == null || status.isOK())
 			getWizard().setMessage(null, IMessageProvider.NONE);
 		else
 			getWizard().setMessage(status.getMessage(), IMessageProvider.ERROR);
 		getWizard().update();
     }
 }
