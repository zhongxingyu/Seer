 package com.gentics.cr.rest.misc;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Collection;
 
 import com.gentics.cr.CRError;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.rest.ContentRepository;
 
 /**
  *
  * Implementaion of XML representation for a REST contentrepositroy.
  *
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *  
  */
 public class YoungestTimestampContentRepository extends ContentRepository {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3875250689693253390L;
 	/**
 	 * Key for the updatetimestamp.
 	 */
 	
 	public static final String UPDATE_TIMESTAMP_KEY = "updatetimestamp";
 	/**
 	 * Create instance.
 	 * sets response encoding to UTF-8
 	 * @param attr attributes
 	 */
 	public YoungestTimestampContentRepository(final String[] attr) {
 		
 		super(attr);
 
 		this.setResponseEncoding("UTF-8");
 		
 	}
 	
 	/**
 	 * Create instance.
 	 * @param attr attributes
 	 * @param encoding encoding
 	 */
 	public YoungestTimestampContentRepository(final String[] attr,
 			final String encoding) {
 		
 		super(attr);
 
 		this.setResponseEncoding(encoding);
 		
 	}
 	
 	/**
 	 * Create instance.
 	 * @param attr attributes
 	 * @param encoding encoding
 	 * @param options options
 	 */
 	public YoungestTimestampContentRepository(final String[] attr,
 				final String encoding, final String[] options) {
 		
 		super(attr, encoding, options);
 		
 	}
 	
 	/**
 	 * Returns "text/xml".
 	 * @return returns the contenttype
 	 */
 	public final String getContentType() {
 		return "text/plain";
 	}
 	
 	/**
 	 * Responds with Error.
 	 * 		Serialized CRError Class
 	 * @param stream 
 	 * @param ex 
 	 * @param isDebug 
 	 * 
 	 */
 	public final void respondWithError(final OutputStream stream,
 			final CRException ex, final boolean isDebug) {
  
 		CRError e = new CRError(ex);
 		if (!isDebug) {
 			e.setStringStackTrace(null);
 		}
 		PrintWriter pw = new PrintWriter(stream);
 	   
 		pw.write(e.getMessage() + " - " + e.getStringStackTrace());
 		pw.flush();
 		pw.close();
 	}
 	
 	/**
 	 * Finds the youngest element.
 	 * @param beanCollection collection to find the youngest of.
 	 * @return updatetimestamp of the youngest element
 	 */
 	private final long getYoungestFromCollection(
			final Collection<CRResolvableBean> beanCollection) {
 		long youngest = 0;
 		if (beanCollection != null) {
 			for (CRResolvableBean bean: beanCollection) {
 				long ts = bean.getLong(UPDATE_TIMESTAMP_KEY, 0);
 				if (ts > youngest) {
 					youngest = ts;
 				}
 				Collection<CRResolvableBean> children 
 					= bean.getChildRepository();
 				if (children != null && children.size() > 0) {
 					long youngestChild = getYoungestFromCollection(children);
 					if (youngestChild > youngest) {
 						youngest = youngestChild;
 					}
 				}
 			}
 		}
 		
 		return youngest;
 	}
 	
 	/**
 	 * Returns the update timestamp of the 
 	 * youngest element in the contentrepository.
 	 * @return timestamp
 	 */
 	public final long getYoungestTimestamp() {
 		long youngest = 0;
 		if (this.resolvableColl != null 
 				&& !this.resolvableColl.isEmpty()) {
 			youngest = getYoungestFromCollection(this.resolvableColl);
 		}
 		return youngest;
 	}
 	
 	/**
 	 * Writes Data to the specified stream.
 	 * @param stream 
 	 * @throws CRException 
 	 * 
 	 */
 	public final void toStream(final OutputStream stream) throws CRException {
 		long youngest =  getYoungestTimestamp();
 		PrintWriter pw = new PrintWriter(stream);
 		pw.write(Long.toString(youngest));
 		pw.flush();
 		pw.close();
 		
 	}
 
 }
