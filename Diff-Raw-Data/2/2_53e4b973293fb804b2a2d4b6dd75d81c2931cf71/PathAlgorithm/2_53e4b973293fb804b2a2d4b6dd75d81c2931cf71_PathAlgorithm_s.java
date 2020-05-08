 package fedora.server.storage.lowlevel;
 
 import java.io.File;
 
 import fedora.server.Server;
 import fedora.server.errors.LowlevelStorageException;
 import fedora.server.errors.MalformedPidException;
 
 /**
  *
  * <p><b>Title:</b> PathAlgorithm.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2005 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author wdn5e@virginia.edu
  * @version $Id$
  */
 abstract class PathAlgorithm implements IPathAlgorithm {
 
 	//protected static final Configuration configuration = Configuration.getInstance();
 
 	protected static final String sep = File.separator;
 
 	private final String storeBase;
 
 	protected final String getStoreBase() {
 		return storeBase;
 	}
 
 	protected PathAlgorithm (String storeBase) {
 		this.storeBase = storeBase;
 	}
 
 	private static final String encode(String unencoded) throws LowlevelStorageException {
         try {
             int i = unencoded.indexOf("+");
             if (i != -1) {
                return Server.getPID(unencoded.substring(0, i-1)).toFilename() 
                         + unencoded.substring(i);
             } else {
     		    return Server.getPID(unencoded).toFilename();
             }
         } catch (MalformedPidException e) {
             throw new LowlevelStorageException(true, e.getMessage(), e);
         }
 	}
 
 	public static final String decode(String encoded) throws LowlevelStorageException {
         try {
             int i = encoded.indexOf("+");
             if (i != -1) {
     		    return Server.pidFromFilename(encoded.substring(0, i-1)).toString() 
     		            + encoded.substring(i);
             } else {
     		    return Server.pidFromFilename(encoded).toString();
             }
         } catch (MalformedPidException e) {
             throw new LowlevelStorageException(true, e.getMessage(), e);
         }
 	}
 
 	abstract protected String format (String pid) throws LowlevelStorageException;
 
 	public final String get (String pid) throws LowlevelStorageException {
 		return format(encode(pid));
 	}
 
 }
