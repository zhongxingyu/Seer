 package org.opensixen.connection;
 
 import org.compiere.db.CConnection;
 import org.compiere.interfaces.Server;
 import org.compiere.session.ServerBean;
 
 public class DummyConnection extends CConnection{
 
 	private static final long serialVersionUID = 1L;
 	
 	private ServerBean m_server;
 	
 	public DummyConnection() {
 		super(null);
 		// TODO Auto-generated constructor stub
 	}
 
 	/* (non-Javadoc)
 	 * @see org.compiere.db.CConnection#isAppsServerOK(boolean)
 	 */
 	@Override
 	public boolean isAppsServerOK(boolean tryContactAgain) {
		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.compiere.db.CConnection#getServer()
 	 */
 	@Override
 	public Server getServer() {
 		if (m_server != null)	{
 			return m_server;
 		}
 
 		m_server = new ServerBean();
 		return m_server;
 	}
 	
 	@Override
 	public synchronized Exception testAppsServer() {
 		return null;
 	}			
 	
 }
