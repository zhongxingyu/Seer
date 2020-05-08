 /*
  *  NeoPortal - New implementation of the INBio Species and Occurrences portal.
  *  
  *  Copyright (C) 2010 INBio - Instituto Nacional de Biodiversidad, Costa Rica
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.inbio.neoportal.image_crawler;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 import org.inbio.neoportal.core.dao.ImageDAO;
 import org.inbio.neoportal.core.dao.TaxonDAO;
 import org.inbio.neoportal.core.entity.Image;
 import org.inbio.neoportal.core.entity.Taxon;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * @author avargas
  *
  */
 @Service
 @Scope("prototype")
 @Transactional
 public class M3sIndexer implements Runnable {
 
 	private HashMap<String, Integer> csvHeaders;
 	private String[] csvLine;
 	
 	@Autowired
 	ImageDAO imageDAO;
 	
 	@Autowired
 	TaxonDAO taxonDAO;
 	
 	private final static Logger LOGGER = Logger.getLogger(M3sIndexer.class);
 	
	private HashMap<String, String> creativeCommons;
	
 	public M3sIndexer () {
 		
 	}
 	
 	public M3sIndexer (HashMap<String, Integer> csvHeaders, String[] csvLine) {
 		this.csvHeaders = csvHeaders;
 		this.csvLine = csvLine;
		
		creativeCommons = new HashMap<String, String>();
		creativeCommons.put("Uso institucional", "by-nc-sa");
		creativeCommons.put("Solo para uso de UBI", "");
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 
 		boolean update = false;
 		String imageId = csvLine[csvHeaders.get("imageId")];
 		String taxonId = csvLine[csvHeaders.get("taxonId")];
 		Image image = imageDAO.findM3sImage(new BigInteger(imageId), new BigDecimal(taxonId));
 		if (image == null)
 			image = new Image();
 		else
 			update = true;
 		
 		image.setSource("m3s");
 		image.setExternalImageId(new BigInteger(imageId));
 		image.setAuthor(csvLine[csvHeaders.get("author")]);
 		
 		Taxon taxon = taxonDAO.findById(Taxon.class, new BigDecimal(taxonId));
 		image.setTaxon(taxon);
 		
		// map rights with creative commons
 		String rights = csvLine[csvHeaders.get("rights")];
		String ccRights = this.creativeCommons.get(rights);
		image.setRights(ccRights);
 		
 		if (update) {
 			imageDAO.update(image);
 		}
 		else {
 			imageDAO.create(image);
 		}
 		
 //		LOGGER.info("Inserting image with id " + imageId);
 	}
 
 }
