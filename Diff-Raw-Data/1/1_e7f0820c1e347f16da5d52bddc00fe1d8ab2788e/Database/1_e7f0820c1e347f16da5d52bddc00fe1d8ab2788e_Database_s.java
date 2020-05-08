 package darep.repos;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 
 /*representation of the physical database.
  *a database has two folders one for the datasets, located at 'filedb' 
  *and one for the metadatafiles, located at 'metadb'
  */
 public class Database {
 	File filedb;
 	File metadb;
 
 	/*
 	 * loads(/creates if 'reposPath' doesnt exits) a db in 'reposPath' doesnt
 	 * open any files/folders just loads the paths
 	 * 
 	 * @param reposPath the location of the repository (parent folder od
 	 * 'filedb' and 'metadb')
 	 */
 	public Database(String reposPath) {
 		filedb = new File(reposPath + "/datasets");
 		metadb = new File(reposPath + "/metadata");
 		if (!filedb.exists())
 			filedb.mkdirs();
 		if (!metadb.exists())
 			metadb.mkdirs();
 
 	}
 
 	/*
 	 * if 'copyMode'=true, dataset is copied, else dataset is moved into the db
 	 * does not check if 'name' is already used, this happens in
 	 * Repository.add()
 	 * 
 	 * @param dataset the (path to) the dataset to be added
 	 * 
 	 * @param meta the metada corresponig to 'dataset'
 	 * 
 	 * @param copyMode true if dataset should be copied to the db, else dataset
 	 * is moved into the db
 	 */
 	public void add(File dataset, Metadata meta, boolean copyMode) throws RepositoryException {
 		String datasetDest = filedb.getAbsolutePath() + "/" + meta.getName();
 		String metadataDest = metadb.getAbsolutePath() + "/" + meta.getName();
 		
 		setDatasetSize(dataset, meta);
 		
 		meta.saveAt(metadataDest);
 		if (copyMode) {
 			copyDataset(dataset, new File(datasetDest));
 		} else {
 			dataset.renameTo(new File(datasetDest));
 		}
 	}
 	
 	/*
 	 * recursively calculates and sets inplace the size of the dataset and the number of files in it
 	 */
 	private void setDatasetSize(File dataset, Metadata meta) {
 		if(dataset.isFile()) {
 			meta.addFileSize(dataset.length());
 			meta.incrementNumberOfFiles();
 		} else {
 			File[] subDatasets = dataset.listFiles();
 			
 			for(File subDataset:subDatasets) {
 				if(subDataset.isFile()) {
 					meta.addFileSize(subDataset.length());
 					meta.incrementNumberOfFiles();
 				} else {
 					setDatasetSize(subDataset, meta);
 				}
 			}
 		}
 	}
 	
 	private void copyDataset(File dataset, File datasetDest) throws RepositoryException {
 		if (dataset.isDirectory()) {
 				datasetDest.mkdir();
 			File[] content=dataset.listFiles();
 			for (int i=0;i<content.length;i++) {
 				copyDataset(content[i], new File(datasetDest.getAbsolutePath()+"/"+content[i].getName()));
 			}
 		} else {
 			FileChannel source = null;
 			FileChannel destination = null;
 			try {
 				source = new FileInputStream(dataset).getChannel();
 				destination = new FileOutputStream(datasetDest).getChannel();
 				try {
 					destination.transferFrom(source, 0, source.size());
 					source.close();
 					destination.close();
 				} finally {
 					source.close();
 					destination.close();
 				}
 			} catch (IOException e) {
 				throw new RepositoryException("could not copy file "
 						+ dataset.getAbsolutePath() + " to "
 						+ datasetDest.getAbsolutePath());
 			}
 		}
 	}
 
 	/*
 	 * return true if the repo/db contains a dataset named 'name', else false
 	 * 
 	 * @ param name name of the file you are searching for
 	 */
 	public boolean contains(String name) {
 		File file = getMetaFile(name);
 		if (file.exists())
 			return true;
 		else
 			return false;
 	}
 
 	private File getMetaFile(String name) {
 		return new File(metadb.getAbsolutePath() + "/" + name);
 	}
 
 	public boolean delete(String name) throws RepositoryException {
 		if (contains(name)) {
 			File meta=getMetaFile(name);
 			boolean success1=meta.delete();
 			boolean success2=deleteDataset(new File(filedb.getAbsolutePath()+"/"+name));
 			if(success1 && success2) {
 				return true;
 			} else {
 				throw new RepositoryException("could not delete dataset "+name);
 			}
 		}else
 			return false;
 	}
 
 	private boolean deleteDataset(File dataset) {
 		if (dataset.isDirectory()) {
 			File[] content=dataset.listFiles();
 			for (int i=0;i<content.length;i++) {
 				deleteDataset(content[i]);
 			}
 		}
 		return dataset.delete();
 	}
 
 }
