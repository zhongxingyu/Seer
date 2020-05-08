 package coed.collab.client;
 
 import coed.base.common.ICoedCommunicator;
 import coed.base.common.ICoedVersioner;
 import coed.base.data.exceptions.InvalidConfigFileException;
 import coed.base.data.exceptions.UnknownVersionerTypeException;
 import coed.collab.client.config.Config;
 import coed.versioning.client.NullVersioner;
 
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
 		if(type!=null && type.equals(ICoedVersioner.NULL)) {
 			return new Communicator(new NullVersioner(), new CollaboratorClient(conf), conf);
 		} else if(type!=null && type.equals(ICoedVersioner.GIT)) {
 			//return new Communicator(new GitVersioner(), new Collaborator());
 		}
 		throw new UnknownVersionerTypeException();
 	}
 }
