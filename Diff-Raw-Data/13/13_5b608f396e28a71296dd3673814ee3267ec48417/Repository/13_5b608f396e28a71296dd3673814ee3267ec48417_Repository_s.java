 package darep.repos;
 
 import java.io.File;
 
 import darep.Command;
 import darep.Command.ActionType;
 
 /*the repository provides methods to access the Database.class
  * and represents the physical folder located at 'location', which contains the db
  * the repository ensures he correctness of the contents of the db !!!!
  * 
  */
 public class Repository {
 	private static final String DEFAULT_LOCATION = System.getProperty("user.home") + "/.data-repository";
 	private File location;
 	private Database db;
 
 	/*
 	 * loads(/creates) the default (hidden) repo in user.home
 	 */
 	public Repository() {
 		location = new File(DEFAULT_LOCATION); 
 		if (!location.exists()) {
 			location.mkdir();
 			System.out.println("created new repository " + DEFAULT_LOCATION);
 		}
 		db = new Database(location.getAbsolutePath());
 	}
 
 	/*
 	 * loads(/creates) the repo located at path
 	 * 
 	 * @param path where to find/create repo
 	 */
 	public Repository(String path) {
 		location = new File(path);
 		if (!location.exists()) {
 			location.mkdir(); // XXX kevin: mkdir_s_() ?
 			System.out.println("created new repository " + path);
 		}
 		db = new Database(location.getAbsolutePath());
 	}
 
 	/*
 	 * adds a dataset to the db, as specified by the options in 'command'
 	 * 
 	 * @param command the command object which stores the options
 	 */
 	public boolean add(Command command) throws RepositoryExeption {
 		File dataset=null;
 		if (command.getAction()==ActionType.replace) { //sourcefile in param[0] or param[1] ?
 			dataset = new File(command.getParams()[1]);
 		} else {
 			dataset = new File(command.getParams()[0]);	
 		}
 		
 		if (!dataset.exists()) {
 				throw new RepositoryExeption(dataset.getAbsolutePath()+" does not exist.");
 		}
 		
 		Metadata meta = makeNewEntry(command);
 		
 		db.add(dataset, meta, !command.isSet("m"));
 		
 		System.out.println("The file/folder " + meta.getOriginalName()
 				+ " has been successfully added to the repository"
 				+ " as data set named " + meta.getName());
 		return true;
 	}
 	
 	public boolean delete(Command command) throws RepositoryExeption {
 		if(db.delete(command.getParams()[0])) {
 			System.out.println("The data set "+ command.getParams()[0] +"(original name: file/folder name) has been successfully removed from the repository.");
 			return true;
 		} else {
 				throw new RepositoryExeption("Unknown data set "+ command.getParams()[0]);
 		}
 	}
 
 	/* creates a new valid metadata for a new dataset. 
 	 * "valid" means the returned Metadata is safe to be entered into the db.
 	 *@ param command  
 	 */
 	private Metadata makeNewEntry(Command command) throws RepositoryExeption {
 		Metadata meta = new Metadata();
 		
 		if (command.getAction()==ActionType.replace) {
 			meta.setOriginalName(new File(command.getParams()[1]).getName());
 		}else {
 			meta.setOriginalName(new File(command.getParams()[0]).getName());			
 		}
 		
 		if (command.isSet("d"))
 			meta.setDescription(command.getOptionParam("d"));
 		
 		String name;
 		if (command.isSet("n")) { //name option set?
 			name = command.getOptionParam("n");
 			if (db.contains(name)) { //if name exists, exit
 				throw new RepositoryExeption("There is already a data set named "
 										+name+" name in the repository.");
 			}
 		} else { //no name provided, make a unique name from originalname
 			name = createUniqueName(meta.getOriginalName());
 		}
 		meta.setName(name);
 		
 		return meta;
 	}
 
 	private String createUniqueName(String name) {
 		name=name.toUpperCase();
		if (name.length() > 40)
			name = name.substring(0, 39);
 		if (db.contains(name)) { //if name exists append number 
 			int append = 1;
			// XXX kevin: ergibt das nicht 41 chars?
			// XXX aeneas: @kevin ja stimmt, sogar noch viel mehr jnd. merci, mach ich noch.
 			while (db.contains(name.concat(Integer.toString(append)))) { 
 				append++;
 			}
 			name = name.concat(Integer.toString(append));
 		}
 		return name;
 	}
 
 	public void replace(Command command) throws RepositoryExeption {
 		boolean success1=delete(command);
 		boolean success2=add(command);
 		if (success1 && success2)  {
 			System.out.println("The data set named "+command.getParams()[0]+" has been successfully replaced by the file/folder �file/folder name�.");
 	}
 	
 	}
 	
 
 	
 }
