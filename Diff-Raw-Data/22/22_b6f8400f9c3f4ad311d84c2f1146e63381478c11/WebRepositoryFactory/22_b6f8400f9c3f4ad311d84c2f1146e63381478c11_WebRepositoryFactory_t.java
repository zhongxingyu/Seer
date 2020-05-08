 /*
  *  StatCvs-XML - XML output for StatCvs.
  *
  *  Copyright by Steffen Pingel, Tammo van Lessen.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  version 2 as published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package de.berlios.statcvs.xml;
 
import java.util.logging.Logger;

 import net.sf.statcvs.output.ChoraIntegration;
 import net.sf.statcvs.output.CvswebIntegration;
 import net.sf.statcvs.output.ViewCvsIntegration;
 import net.sf.statcvs.output.WebRepositoryIntegration;
 
 
 /**
  * WebRepositoryFactory
  * 
  * @author Tammo van Lessen
  */
 public class WebRepositoryFactory {
 	
	private static final Logger logger = Logger.getLogger("de.berlios.statcvs.xml.WebRepositoryFactory");
	
 	public static WebRepositoryIntegration getInstance(String url) {
 		if (url.indexOf("cvs.php") != -1) {
 			// chora
 			return new ChoraIntegration(url);		
 		}
 		if (url.indexOf("cvsweb") != -1) {
 			// cvsweb
 			return new CvswebIntegration(url);		
 		}
 		if (url.indexOf("viewcvs") != -1) {
 			// viewcvs
 			return new ViewCvsIntegration(url);		
 		}
		
		logger.info("Could not recognize typo of web repository. Web repository integration disabled.");
 		return null;	
 	}
 }
