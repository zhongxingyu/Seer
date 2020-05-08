 package com.pangratz.memorablepw.rest;
 
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.ResourceException;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 
 import com.pangratz.memorablepw.model.Password;
 
 public class TweetPasswordResource extends MemorablePwServerResource {
 
	public static final String STATUS_CODE_DUPLICATE = "b2b52c28-1c67fec4";
 	private Twitter mTwitter;
 
 	@Override
 	protected void doInit() throws ResourceException {
 		super.doInit();
 
 		mTwitter = TwitterFactory.getSingleton();
 
 		getVariants(Method.GET).add(new Variant(MediaType.APPLICATION_JSON));
 	}
 
 	@Override
 	protected Representation get(Variant variant) throws ResourceException {
 		int length = mModelUtils.getNextPasswordLength();
 		Password pw = mModelUtils.getNextPassword(length);
 		if (pw != null) {
			String tanga = pw.getText();
 			try {
 				mTwitter.updateStatus(tanga);
 				mModelUtils.removePassword(tanga);
 				mModelUtils.updateNextPasswordLength();
 
 				return createSuccessRepresentation("tweeted password: " + tanga);
 			} catch (TwitterException e) {
				// delete the password, if the status is a duplicate
				if (STATUS_CODE_DUPLICATE.equalsIgnoreCase(e.getExceptionCode())) {
					mModelUtils.removePassword(tanga);
				}
 				return createErrorRepresentation("could not tweet password " + tanga + " --> " + e.getErrorMessage());
 			}
 		} else {
 			return createErrorRepresentation("no password available! feed me :(");
 		}
 	}
 
 }
