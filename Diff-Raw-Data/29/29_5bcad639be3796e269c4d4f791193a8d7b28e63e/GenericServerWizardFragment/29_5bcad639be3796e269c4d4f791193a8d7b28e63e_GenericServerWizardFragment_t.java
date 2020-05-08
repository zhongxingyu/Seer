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
  * DISCLAIMED.  IN NO EVENT SHALL ETERATION A.S. OR
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
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jst.server.generic.core.internal.GenericServer;
 import org.eclipse.jst.server.generic.core.internal.GenericServerRuntime;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.TaskModel;
 import org.eclipse.wst.server.ui.wizard.IWizardHandle;
 /**
  * 
  *
  * @author Gorkem Ercan
  */
 public class GenericServerWizardFragment extends ServerDefinitionTypeAwareWizardFragment 
 {
 	private GenericServerCompositeDecorator[] fDecorators;
 
 	/**
 	 * 
 	 */
 	public boolean isComplete() {
 		for (int i = 0; fDecorators!=null && i < fDecorators.length; i++) {
 			if(fDecorators[i].validate())
 				return false;
 		}
 		ServerRuntime serverRuntime = getServerTypeDefinitionFor(getServer());
 		if(serverRuntime==null)
 		    return false;
 		return true;
 	}
 
 	public void createContent(Composite parent, IWizardHandle handle){
 		IServerWorkingCopy server = getServer();
 		GenericServer dl= (GenericServer)server.loadAdapter(GenericServer.class,null);
 		ServerRuntime definition = getServerTypeDefinitionFor(server);
 		fDecorators = new GenericServerCompositeDecorator[1];
 		fDecorators[0]=new ServerTypeDefinitionServerDecorator(definition,null,getWizard(),dl);
 		new GenericServerComposite(parent,fDecorators);
 		
 	}
 	/**
      * @param server
      * @return
      */
     private ServerRuntime getServerTypeDefinitionFor(IServerWorkingCopy server) {        
         GenericServerRuntime runtime = (GenericServerRuntime)server.getRuntime().getAdapter(GenericServerRuntime.class);
         if(runtime==null){
             IRuntime wc = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
             runtime= (GenericServerRuntime)wc.getAdapter(GenericServerRuntime.class);
             if(runtime==null)
             	runtime= (GenericServerRuntime)wc.loadAdapter(GenericServerRuntime.class,new NullProgressMonitor());
         }        
         String id = runtime.getRuntime().getRuntimeType().getId();
         if(id==null){   
             return null;
         }
         Map runtimeProperties = runtime.getServerInstanceProperties();
 		ServerRuntime definition = getServerTypeDefinition(id,runtimeProperties);
         return definition;
     }
 
     /**
      * @return
      */
     private IServerWorkingCopy getServer() {
         IServerWorkingCopy server = (IServerWorkingCopy)getTaskModel().getObject(TaskModel.TASK_SERVER);
         return server;
     }
 
    private boolean isNameInUse(String name){
       	IServer[] servers =ServerCore.getServers();   	
    	for (int i = 0; i < servers.length; i++) {
			if(!servers[i].equals(getServer().getOriginal()) && (servers[i].getName().equals(name) ))
				return true;
		}
    	return false;
     }
    
    private String createName(){
    	String name = GenericServerUIMessages.bind(GenericServerUIMessages.serverName,getServerTypeDefinitionFor(getServer()).getName());
    	int suffix=1;
    	String suffixName =name;
    	while(isNameInUse(suffixName)){
    		suffixName = name+" "+suffix;
    		suffix++;
    	}
    	return suffixName;
    }
    public void enter() {
    	getServer().setName(createName());
     }
 	
     /* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.ui.ServerDefinitionTypeAwareWizardFragment#description()
      */
     public String description() {
         String sName = getServerName();
         if(sName==null || sName.length()<1)
             sName="Generic";
         return  GenericServerUIMessages.bind(GenericServerUIMessages.serverWizardDescription,sName);
     }
 
     private String getServerName()
     {
         if(getServer()!=null && getServer().getRuntime()!=null)
            return getServer().getRuntime().getRuntimeType().getName();
         return null;
     }
     /* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.ui.ServerDefinitionTypeAwareWizardFragment#title()
      */
     public String title() {
         String sName= getServerName();
         if(sName==null || sName.length()<1)
             sName="Generic";
         return  GenericServerUIMessages.bind(GenericServerUIMessages.serverWizardTitle,sName);
     }
 }
