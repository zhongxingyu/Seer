 package darep.repos;
 
 import java.io.File;
 import java.io.IOException;
 
 import darep.Command;
 import darep.Command.ActionType;
 import darep.Helper;
 import darep.repos.fileStorage.FileStorage;
 
 /*the repository provides methods to access the Database.class
  * and represents the physical folder located at 'location', which contains the db
  * the repository ensures he correctness of the contents of the db !!!!
  * 
  */
 public class Repository {
 	
 	public static final int prettyPrintColWidth = 20;
 	
 	private static final String DEFAULT_LOCATION = System
 			.getProperty("user.home") + "/.data-repository";
 	
 	/**
 	 * The CANONICAL File which contains the Repository
 	 */
 	private final File location;
 	private Storage db;
 
 	/*
 	 * loads(/creates) the default (hidden) repo in user.home
 	 */
 	public Repository() throws RepositoryException {
 		this(DEFAULT_LOCATION);
 	}
 
 	/*
 	 * loads(/creates) the repo located at path
 	 * 
 	 * @param path where to find/create repo
 	 */
 	public Repository(String path) throws RepositoryException {
 		try {
 			location = new File(path).getCanonicalFile();
 		} catch (IOException e) {
 			throw new RepositoryException("Could not get canonical " +
 					"File for " + path, e);
 		}
 		initRepository(path);
 	}
 
 	private void initRepository(String path) throws RepositoryException {
 		if (!location.exists()) {
 			if (!location.mkdirs()) {
 				throw new RepositoryException("Tried to create Repository " +
 						location.getPath() + " but failed." );
 			}
 			System.out.println("created new repository " + path);
 		}
 		db = new FileStorage(location.getPath());
 	}
 
 	/*
 	 * adds a dataset to the db, as specified by the options in 'command'
 	 * 
 	 * @param command the command object which stores the options
 	 */
 	public void add(Command command) throws RepositoryException {
 		
 		try {
 
 			File file = getInputFile(command);
 			if (!file.exists()) {
 				throw new RepositoryException(file.getCanonicalPath()
 						+ " does not exist.");
 			}
 			
 			String canonicalFilePath = file.getCanonicalPath();
 			String repoPath = location.getPath();
 			if (canonicalFilePath.startsWith(repoPath)
 					|| repoPath.startsWith(canonicalFilePath)) {
 				throw new RepositoryException(
 						"Dataset can not contain the repository itself.");
 			}
 	
 			Metadata meta = createMetaData(command);
 			
 			RepositoryDataSet ds = new RepositoryDataSet();
 			ds.setMetadata(meta);
 			ds.setFile(file);
 			ds.setCopyMode(!command.hasFlag("m"));
 			
 			String fileFolder = ds.getType().toString();
 			String msg = "The "+fileFolder+" '"+meta.getOriginalName()+"' has" +
 					" been successfully added to the repository as data set" +
 					" named " + meta.getName();
 			
 			try {
 				db.store(ds);
 				System.out.println(msg);
 			} catch (StorageException e) {
 				throw new RepositoryException("Could not store Dataset " +
 						meta.getName(), e);
 			}
 			
 		} catch (IOException e) {
 			throw new RepositoryException("There was an IOError while adding", e);
 		}
 	}
 
 	public void delete(Command command) throws RepositoryException {
 		try {
 			db.delete(command.getParams()[0]);
 			System.out.println("The data set " + command.getParams()[0] +
 							" (original name: file/folder name) has been" +
 							" successfully removed from the repository.");
 		} catch (StorageException e) {
 			throw new RepositoryException("Could not delete Dataset " +
 					command.getParams()[0], e);
 		}
 	}
 
 	/*
 	 * creates a new valid metadata for a new dataset. "valid" means the
 	 * returned Metadata is safe to be entered into the db.
 	 * 
 	 * @ param command
 	 */
 	private Metadata createMetaData(Command command) throws RepositoryException {
 		
 		File file;
 		try {
 			file = getInputFile(command);
 		} catch (IOException e) {
 			throw new RepositoryException("Could not get File", e); // TODO print filename?
 		}
 		
 		Metadata meta = new Metadata();
 		try {
 			meta.setOriginalName(getInputFile(command).getName());
 		} catch (IOException e) {
 			throw new RepositoryException("could not set meta data Name", e);
 		}
 
 		if (command.isSet("d")) {
 			meta.setDescription(command.getOptionParam("d"));
 		}
 
 		String name;
 		if (command.isSet("n")) {
 			name = command.getOptionParam("n");
 		} else {
 			name = createUniqueName(file.getName());
 		}
 		meta.setName(name);
 		
 		meta.setNumberOfFiles(countFiles(file));
 		
 		meta.setFileSize(calculateFileSize(file));
 
 		return meta;
 	}
 	
 	private long calculateFileSize(File file) {
 		if (file.isDirectory()) {
 			long currSize = 0L;
 			for (File subFile: file.listFiles()) {
 				currSize += calculateFileSize(subFile);
 			}
 			return currSize;
 		} else {
 			return file.length();
 		}
 	}
 
 	private int countFiles(File file) {
 		if (file.isDirectory()) {
 			int currNum = 0;
 			for (File subFile: file.listFiles()) {
 				currNum += countFiles(subFile);
 			}
 			return currNum;
 		} else {
 			return 1;
 		}
 	}
 
 
 	private File getInputFile(Command command) throws IOException {
 		// is the input file stored in the second or first parameter?
 		File file;
 		if (command.getAction() == ActionType.replace) {
 			file = new File(command.getParams()[1]);
 		} else {
 			file = new File(command.getParams()[0]);
 		}
 		
 		return file.getCanonicalFile();
 	}
 
 	private String createUniqueName(String name) throws RepositoryException {
 		name = name.toUpperCase();
 		int max = 40;
 		if (name.length() > max)
 			name = name.substring(0, max);
 		if (db.contains(name)) { // if name exists append number
 			int append = 1;
 			if (name.length() == max)
 				name = name.substring(0, max - 1);
 			while (db.contains(name + append)) {
 				append++;
 				if ((name + append).length() > max)
 					name = name.substring(0, name.length() - 1);
 			}
 			name = name + append;
 		}
 		return name;
 	}
 
 	public String getList(Command command) throws RepositoryException {
 		
 		if (command.hasFlag("p")) {
 			return getPrettyList();
 		} else {
 			return getTabList();
 		}
 		
 	}
 	
 	public DataSet getDataset(String name) throws RepositoryException {
 		try {
 			return db.getDataSet(name);
 		} catch (StorageException e) {
 			throw new RepositoryException("Could not get Dataset " + name, e);
 		}
 	}
 	
 	private String getPrettyList() throws RepositoryException {
 		
 		int totalFiles = 0;
 		long totalSize = 0;
 		
 		// Metadata.maxXXXLength is now set because all datasets are loaded
 		DataSet[] datasets;
 		try {
 			datasets = db.getAllDataSets();
 		} catch (StorageException e) {
 			throw new RepositoryException("Could not fetch Datasets", e);
 		}
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append(getHeaderline());
 		
 		for (DataSet dataset: datasets) {
 			sb.append(dataset.getMetadata().getPrettyString()); // TODO prettyStrings (renderer?)
 			totalFiles += dataset.getMetadata().getNumberOfFiles();
 			totalSize += dataset.getMetadata().getSize();
 		}
 		
 		sb.append("(" + totalFiles + " data sets, ");
 		sb.append(totalSize + " bytes in total)");
 		
 		return sb.toString();
 	}
 
 	private String getTabList() throws RepositoryException {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("Name\t");
 		sb.append("Original Name\t");
 		sb.append("Timestamp\t");
 		sb.append("Number of Files\t");
 		sb.append("Size\t");
 		sb.append("Description\t");
 		sb.append("\n");
 		
 		DataSet[] datasets;
 		try {
 			datasets = db.getAllDataSets();
 		} catch (StorageException e) {
 			throw new RepositoryException("Could not fetch Datasets", e);
 		}
 		
 		for (DataSet dataset : datasets) {
 			sb.append(dataset + "\n");
 		}
 		return sb.toString();
 	}
 
 	private String getHeaderline() {		
 		StringBuilder sb = new StringBuilder();
 		sb.append("|");
 		sb.append(Helper.stringToLength("Name", Metadata.getMaxNameLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Original Name", Metadata.getMaxOrigNameLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Timestamp", Metadata.getMaxTimestampLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Number of Files", Metadata.getMaxNumFilesLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Size", Metadata.getMaxSizeLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Description", Metadata.getMaxDescriptionLength()));
 		sb.append("|\n");
 		
 		int numDashes = (Metadata.getTotalMaxWidth()) + 7;
 		for (int i = 0; i < numDashes; i++) {
 			sb.append("-");
 		}
 		sb.append("\n");
 		
 		return sb.toString();
 	}
 
 	public void replace(Command command) throws RepositoryException {
 		delete(command);
 		
 		command.setOptionParam("n", command.getParams()[0]);
 		add(command);
 
 		System.out.println("The data set named " + command.getParams()[0]
 				+ " has been successfully"
 				+ " replaced by the file/folder.");
 	}
 
 	public void export(Command command) throws RepositoryException {
 		try {
 			String canonicalExportPath = new File(command.getParams()[1])
 				.getCanonicalPath();
 			if (canonicalExportPath.contains(location.getPath()))
 				throw new RepositoryException(
 						"It is not allowed to export something into the "
 								+ location.toString() + " repository folder");
 			
 			DataSet ds = db.getDataSet(command.getParams()[0]);
 			if (ds == null) {
 				throw new RepositoryException("Unknown data set "
 						+ command.getParams()[0]);
 			}
 			
 			File destination = new File(command.getParams()[1]).getCanonicalFile();
 			ds.copyFileTo(destination);
 			
 			System.out.println("The data set " + command.getParams()[0]
 							+ " (original name: " + ds.getMetadata().getName() + ")"
 							+ " has been successfully exported to "
 							+ command.getParams()[1]);
 		} catch (IOException e) {
 			throw new RepositoryException("Could not get Canonical Path for " +
 					command.getParams()[1], e);
 		} catch (StorageException e) {
 			throw new RepositoryException("Error in Database", e);
 		}
 	}
 
 	protected File getLocation() {
 		return this.location;
 	}
 
 	public static String getDefaultLocation() {
 		return DEFAULT_LOCATION;
 	}
 
 }
