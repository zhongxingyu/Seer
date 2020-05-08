 /* 
  * 
  * XFlow
  * _______
  * 
  *  
  *  (C) Copyright 2010, by Universidade Federal do Pará (UFPA), Francisco Santana, Jean Costa, Pedro Treccani and Cleidson de Souza.
  * 
  *  This file is part of XFlow.
  *
  *  XFlow is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  XFlow is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with XFlow.  If not, see <http://www.gnu.org/licenses/>.
  *
  *
  *  ==================
  *  DataExtractor.java
  *  ==================
  *  
  *  Original Author: Francisco Santana;
  *  Contributor(s):  -;
  *  
  */
 
 package br.ufpa.linc.xflow.cm;
 
 import java.util.List;
 
 import org.tmatesoft.svn.core.SVNException;
 
 import br.ufpa.linc.xflow.cm.connectivity.ConnectionHandler;
 import br.ufpa.linc.xflow.cm.connectivity.SVNAccess;
 import br.ufpa.linc.xflow.cm.info.Commit;
 import br.ufpa.linc.xflow.cm.transformations.EntriesTransformer;
 import br.ufpa.linc.xflow.data.dao.cm.ProjectDAO;
 import br.ufpa.linc.xflow.data.database.DatabaseManager;
 import br.ufpa.linc.xflow.data.entities.Project;
 import br.ufpa.linc.xflow.exception.cm.CMException;
 import br.ufpa.linc.xflow.exception.cm.svn.SVNProtocolNotSupportedException;
 import br.ufpa.linc.xflow.exception.persistence.DatabaseException;
 import br.ufpa.linc.xflow.util.Filter;
 
 public class DataExtractor {
 
 	private boolean processCanceled = false;
 
 	public void extractData(final Project project, final long startRevision, final long endRevision, final Filter filter) throws CMException, DatabaseException{
 		final ConnectionHandler connectionHandler = new ConnectionHandler(project);
 		connectionHandler.setDownloadCodeEnabled(project.isCodeDownloadEnabled());
 		connectionHandler.getAccess().setUrl(project.getUrl());
 		connectionHandler.getAccess().setUsername(project.getUsername());
 		connectionHandler.getAccess().setPassword(project.getPassword());
 		connectionHandler.getAccess().setFilter(filter);
 		project.setFirstRevision(startRevision);
 
 		final EntriesTransformer transformer = new EntriesTransformer();
 
 		final long revisionsInterval = 1 + endRevision - startRevision;
 
 		if(revisionsInterval < 100){
			final List<Commit> dataCollected = connectionHandler.gatherData(startRevision, endRevision);			
			transformer.transformData(project, dataCollected);
			project.setLastRevision(dataCollected.get(dataCollected.size()-1).getRevisionNbr());
 		}
 		else {
 			for (int i = 0; i < (int)(revisionsInterval/100); i++) {
 				if(!processCanceled){
 					final List<Commit> dataCollected = connectionHandler.gatherData(startRevision + (i*100), startRevision + ((i+1)*100) - 1);
					transformer.transformData(project, dataCollected);
					project.setLastRevision(dataCollected.get(dataCollected.size()-1).getRevisionNbr());
 				}
 				//FIXME:
 				//As we don't have an application layer yet, it is necessary 
 				//to frequently clear the persistence context to avoid memory issues
 				DatabaseManager.getDatabaseSession().clear();
 			}
 			if(((revisionsInterval%100) > 0) && (!processCanceled)){
 				final List<Commit> dataCollected = connectionHandler.gatherData(startRevision + ((int)(revisionsInterval/100)*100), endRevision);
				transformer.transformData(project, dataCollected);
				project.setLastRevision(dataCollected.get(dataCollected.size()-1).getRevisionNbr());
 			}
 		}
 		new ProjectDAO().update(project);
 	}
 	
 	public static boolean checkForSVNDatesInconsistency(final Project project) throws DatabaseException, SVNException, SVNProtocolNotSupportedException{
 		SVNAccess svnAccess = new SVNAccess();
 		svnAccess.setUrl(project.getUrl());
 		svnAccess.setUsername(project.getUsername());
 		svnAccess.setPassword(project.getPassword());
 		
 		return svnAccess.checkForDateInconsistencies(project);
 	}
 }
