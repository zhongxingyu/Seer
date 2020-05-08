 /*
  * Dec 7, 2005
  */
 package com.thinkparity.server.handler.artifact;
 
 import org.jivesoftware.messenger.auth.UnauthorizedException;
 import org.xmpp.packet.IQ;
 
 import com.thinkparity.server.handler.IQAction;
 import com.thinkparity.server.handler.IQHandler;
 import com.thinkparity.server.model.ParityServerModelException;
 import com.thinkparity.server.model.artifact.ArtifactModel;
 import com.thinkparity.server.model.session.Session;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class RequestArtifactKey extends IQHandler {
 
 	/**
 	 * Create a RequestArtifactKey.
 	 */
 	public RequestArtifactKey() { super(IQAction.REQUESTKEY); }
 
 	/**
 	 * @see com.thinkparity.server.handler.IQHandler#handleIQ(org.xmpp.packet.IQ,
 	 *      com.thinkparity.server.model.session.Session)
 	 */
	public IQ handleIQ(final IQ iq, final Session session)
			throws ParityServerModelException, UnauthorizedException {
 		logger.info("handleIQ(IQ)");
 		logger.debug(iq);
 		logger.debug(session);
 		final ArtifactModel artifactModel = getArtifactModel(session);
 		artifactModel.requestKey(extractUniqueId(iq));
 		return createResult(iq);
 	}
 }
