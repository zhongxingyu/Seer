 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package beans;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 import javax.inject.Inject;
 
 import models.ServerNode;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecuteResultHandler;
 import org.apache.commons.exec.ExecuteException;
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.i18n.Messages;
 import server.ApplicationContext;
 import server.DeployManager;
 import server.ProcExecutor;
 import server.exceptions.ServerException;
 import beans.api.ExecutorFactory;
 import beans.config.Conf;
 
 /**
  * This class deploys a recipe file vi cloudify non-interactive CLI. 
  * Each deploy forks a CLI process and stream the output.
  * 
  * @author Igor Goldenberg
  */
 public class DeployManagerImpl implements DeployManager
 {
 
     private static Logger logger = LoggerFactory.getLogger( DeployManagerImpl.class );
 
     @Inject
     private Conf conf;
     
     @Inject 
     private ExecutorFactory executorFactory;
     
    


 	static enum RecipeType
 	{
 		APPLICATION, SERVICE;
 		
 		static RecipeType getRecipeTypeByFileName( String fileName )
 		{
 			if ( fileName.endsWith(APPLICATION.getFileIdentifier()) )
 				return APPLICATION;
 			
 			if ( fileName.endsWith(SERVICE.getFileIdentifier()) )
 				return SERVICE;
 			
 			return null;
 		}
 		
 		public String getCmdParam()
 		{
 			switch( this )
 			{
 				case APPLICATION: return "install-application";
 				case SERVICE: return "install-service";
 				default: return null;
 			}
 		}
 		
 		public String getFileIdentifier()
 		{
 			switch( this )
 			{
 				case APPLICATION: return  "application.groovy";
 				case SERVICE: return "service.groovy";
 				default: return null;
 			}
 		}
 	}
 
 	public ProcExecutor fork(ServerNode server, File recipe)
 	{
 		RecipeType recipeType = getRecipeType( recipe );
 		logger.info( "Deploying: [ServerIP={}] [recipe={}] [type={}]", new Object[]{server.getPublicIP(), recipe, recipeType.name()} );
 		String recipePath = FilenameUtils.separatorsToSystem(recipe.getPath());
 		
 		CommandLine cmdLine = new CommandLine( conf.cloudify.deployScript );
 		cmdLine.addArgument(server.getPublicIP());
 		cmdLine.addArgument(recipePath);
 		cmdLine.addArgument(recipeType.getCmdParam());
 		
 		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
 
 		ProcExecutor executor = executorFactory.getDeployExecutor( server, recipe );
 
 		try
 		{
 			executor.execute(cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment(), resultHandler);
 
 			logger.info("The process instanceId: {}", executor.getId());
 			return executor;
 		} catch (ExecuteException e)
 		{
 			logger.error("Failed to execute process. Exit value: " + e.getExitValue(), e);
 
 			throw new ServerException("Failed to execute process. Exit value: " + e.getExitValue(), e);
 		} catch (IOException e)
 		{
 			logger.error("Failed to execute process", e);
 
 			throw new ServerException("Failed to execute process.", e);
 		}
 	}
 	
    /** 
 	* @return recipe type Application or Service by recipe directory.
 	* @throws ServerException if found a not valid recipe file.
 	**/
     protected RecipeType getRecipeType( File recipeDir )
 	{
 		String[] files = recipeDir.list( new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return RecipeType.getRecipeTypeByFileName( name ) != null;
 			}
 		} );
 		   
 		if ( files == null || files.length == 0 )
 			throw new ServerException( Messages.get( "recipe.not.valid.1",
                     RecipeType.APPLICATION.getFileIdentifier(), RecipeType.SERVICE.getFileIdentifier() ));
 		
 		if ( files.length > 1)
 			throw new ServerException( Messages.get( "recipe.not.valid.2",
                     RecipeType.APPLICATION.getFileIdentifier(), RecipeType.SERVICE.getFileIdentifier() ));
 
 		return RecipeType.getRecipeTypeByFileName(files[0]);
 	}
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 }
