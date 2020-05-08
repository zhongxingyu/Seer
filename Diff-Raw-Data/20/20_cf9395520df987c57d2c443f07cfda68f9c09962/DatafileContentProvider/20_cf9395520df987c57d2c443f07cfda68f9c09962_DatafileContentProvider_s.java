 package uk.ac.diamond.scisoft.icatexplorer.rcp.datafiles;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.icatexplorer.rcp.datasets.DatasetTreeData;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.icatclient.ICATClient;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.icatclient.ICATSessions;
 import uk.icat3.client.Datafile;
 import uk.icat3.client.Dataset;
 
 
 public class DatafileContentProvider implements ITreeContentProvider {
 	
 	private static final Logger logger = LoggerFactory.getLogger(DatafileContentProvider.class);
 	
 	private static final Object[] NO_CHILDREN = new Object[0];
 
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Object[] getElements(Object inputElement) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object[] getChildren(Object parentElement) {
 		Object[] children = null;
 		if (parentElement instanceof DatasetTreeData) { 
 			
 			return getDatafiles(((DatasetTreeData)parentElement).getIcatDataset().getId(), ((DatasetTreeData)parentElement).getParentProject());
 		} else if(parentElement instanceof IFolder) {
 			
 			long datasetid = ((Dataset)parentElement).getId();		
 			return getDatafiles(datasetid, null);
 			
 		}   
 		return children != null ? children : NO_CHILDREN;
 	}
 
 	/**
 	 * @param parentElement
 	 * @return
 	 */
 	private Object[] getDatafiles(long id, String projectName) {
 		ICATClient icatClient = ICATSessions.get(projectName);
 		List<Datafile> result = null;
 		result = icatClient.getDatafiles(id);
 		DatafileTreeData[] datafilesTree = new DatafileTreeData[result.size()];
		
 		for(int i=0; i< result.size(); i++){	
 			Datafile icatDatafile = result.get(i);
 			DatafileTreeData datafile = new DatafileTreeData(icatDatafile, projectName);
 			datafilesTree[i] = datafile;
 
 		}
 				
 		return datafilesTree;
 	}
 
 	@Override
 	public Object getParent(Object element) {
 		return null;
 	}
 
 	@Override
 	public boolean hasChildren(Object element) {
 		return false;
 	}
 
 }
