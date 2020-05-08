 package coed.collab.client;
 
 import coed.base.comm.ICoedCommunicator;
 import coed.base.comm.ICoedVersioner;
 import coed.base.config.Config;
 import coed.base.data.exceptions.InvalidConfigFileException;
 import coed.base.data.exceptions.UnknownVersionerTypeException;
 import coed.versioning.client.StaticVersioner;
 
 public class CoedCommunicatorFactory {
 	
 	/**
 	 * 	Creates a Communicator with specified attributes
 	 * @param basePath the Path of the workspace (without "\" at the end!!!)
 	 * @return Communicator object
 	 * @throws UnknownVersionerTypeException
 	 * @throws InvalidConfigFileException
 	 */
 	public ICoedCommunicator create(String basePath) throws UnknownVersionerTypeException, InvalidConfigFileException
 	{
 		Config conf = new Config(basePath+"\\.coed\\config.ini");
 		String type = conf.getString("versioner.type");
 		
 		//must check for null, because even if the config file exists, it may be corrupt/incomplete
 		if(type!=null && type.equals(ICoedVersioner.STATIC)) {
			return new Communicator(new StaticVersioner(), new CollaboratorClient(conf,basePath), conf, basePath);
 		} else if(type!=null && type.equals(ICoedVersioner.GIT)) {
 			//return new Communicator(new GitVersioner(), new Collaborator());
 		}
 		throw new UnknownVersionerTypeException();
 	}
 }
