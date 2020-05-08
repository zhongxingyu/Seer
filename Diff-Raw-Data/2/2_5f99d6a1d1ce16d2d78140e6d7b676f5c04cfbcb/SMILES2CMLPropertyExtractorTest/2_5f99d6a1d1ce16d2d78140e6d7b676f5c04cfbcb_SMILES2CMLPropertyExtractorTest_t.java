 /*******************************************************************************
  * Copyright (C) 2007 The University of Manchester   
  *               2009 Egon Willighagen <egon.willighagen@gmail.com>
  * 
  *  Modifications to the initial code base are copyright of their
  *  respective authors, or their employers as appropriate.
  * 
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2.1 of
  *  the License, or (at your option) any later version.
  *    
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *    
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  ******************************************************************************/
 package org.openscience.cdk.taverna.partition;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 import java.util.Map;
 
 import net.sf.taverna.t2.partition.PropertyExtractorSPI;
 import net.sf.taverna.t2.partition.PropertyExtractorSPIRegistry;
 
 import org.junit.Test;
 import org.openscience.cdk.taverna.query.SMILES2CMLActivityItem;
 
 public class SMILES2CMLPropertyExtractorTest {
 
 	@Test
 	public void testSPI() {
 		List<PropertyExtractorSPI> instances = PropertyExtractorSPIRegistry.getInstance().getInstances();
 		assertTrue("There should be more than one instance found",instances.size()>0);
 		boolean found = false;
 		for (PropertyExtractorSPI spi : instances) {
 			if (spi instanceof SMILES2CMLPropertyExtractor) {
 				found=true;
 				break;
 			}
 		}
 		assertTrue("A XMPPPropertyExtractor should have been found",found);
 	}
 	
 	@Test
 	public void testExtractProperties() {
 		SMILES2CMLActivityItem item = new SMILES2CMLActivityItem();
 		
 		Map<String,Object> props = new SMILES2CMLPropertyExtractor().extractProperties(item);
 
		assertEquals("missing or incorrect property","SMILES 2 CML Activity",props.get("type"));
 	}
 
 }
