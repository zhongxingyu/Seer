 package group1.project.synthlab.cable;
 
 import group1.project.synthlab.exceptions.BadConnection;
 import group1.project.synthlab.exceptions.PortAlreadyUsed;
 import group1.project.synthlab.factory.Factory;
 import group1.project.synthlab.port.in.IInPort;
 import group1.project.synthlab.port.out.IOutPort;
 
 /**
  * Creation d'un cable
  * 
  * @author Groupe 1
  * 
  */
 
 public class Cable implements ICable {
 	
 	protected IInPort inPort; // Entree d'un module
 	protected IOutPort outPort; // Sortie d'un module
 	protected Factory factory;
 	protected boolean saturated;
 	protected boolean hasSignal;
 	
 	/**
 	 * Constructeur du cable.
 	 * @param factory
 	 */
 	public Cable(Factory factory) {
 		this.factory = factory;
 		this.hasSignal = false;
 		this.saturated = false;
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.cable.ICable#setOutPort(group1.project.synthlab.port.out.IOutPort)
 	 */
 	public void setOutPort(IOutPort outPort) throws BadConnection, PortAlreadyUsed {
 		if (outPort.isUsed())
 			throw new PortAlreadyUsed("Ce port " + outPort.getLabel() + " est deja utilise par un autre cable. Detachez le cable avant d'en ajouter un autre !");
 		this.outPort = outPort;
 		
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.cable.ICable#setInPort(group1.project.synthlab.port.in.IInPort)
 	 */
 	public void setInPort(IInPort inPort) throws BadConnection, PortAlreadyUsed {
 		if (inPort.isUsed())
 			throw new PortAlreadyUsed("Ce port " + inPort.getLabel() + " est deja utilise par un autre cable. Dettachez le cable avant d'en ajouter un autre !");
 		this.inPort = inPort;
 		if(outPort != null){
 			outPort.getJSynPort().connect(inPort.getJSynPort());
 			outPort.setCable(this);
 			inPort.setCable(this);
 			// On previent les observers qu'un cable connecte maintenant les 2 ports.
 			outPort.cableConnected();
 			inPort.cableConnected();
 		}
 		else
 			throw new BadConnection("Un cable doit partir d'une sortie de module et arriver a une entree d'un autre module.");
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.cable.ICable#getInPort()
 	 */
 	public IInPort getInPort() {
 		return inPort;
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.cable.ICable#getOutPort()
 	 */
 	public IOutPort getOutPort() {
 		return outPort;
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.cable.ICable#isConnected()
 	 */
 	public boolean isConnected() {
 		return inPort  != null && outPort != null;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.cable.ICable#disconnect()
 	 */
 	public void disconnect() {
 		this.outPort.getJSynPort().disconnect(inPort.getJSynPort());		
 		
 		// On previent les observers que dorenavant aucun cable ne connecte les 2 ports.
 		outPort.cableDisconnected();
 		inPort.cableDisconnected();
 		
 		//A faire impérativement en dernier
 		outPort.setCable(null);
 		inPort.setCable(null);
 	}
 	
 	@Override
 	public void finalize() throws Throwable{
 		disconnect();
 		super.finalize();		
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.cable.ICable#isSignalSaturated()
 	 */
 	public boolean isSignalSaturated() {
 		return outPort.detectSignalSaturated();
 	}
 
 
 	public boolean hasSignal() {
 		if (outPort == null)
 			return false;
		if (inPort == null)
			return false;
 		return outPort.getModule().isStarted() &&   inPort.getModule().isStarted() && outPort.detectSignal() ;
 	
 	}
 
 }
