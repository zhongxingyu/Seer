 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.connectors.microsoft;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.query.QueryResult;
 import net.sf.okapi.common.resource.TextFragment;
 
 import net.sf.okapi.connectors.microsoft.MicrosoftMTConnector;
 import net.sf.okapi.connectors.microsoft.Parameters;
 
 import org.junit.Test;
import static org.junit.Assert.*;
 
 public class MicrosoftBatchTokenConnectorTest {
 
 	@Test
	public void paramTest () {
		MicrosoftMTConnector mmtc = new MicrosoftMTConnector();
		Parameters params = (Parameters) mmtc.getParameters();
		params.setClientId("testID");
		params.setSecret("testSecret");
		assertEquals("testID", params.getClientId());
		assertEquals("testSecret", params.getSecret());
	}
	
	// To test manually: uncomment and add clientId and secret
	// Make sure to comment before pushing to public repository
	// @Test
	public void manualTest () {
 		int lenny;
 		int lynn;
 		QueryResult result;
 		TextFragment frag;
 		List<QueryResult> franz;
 		List<List<QueryResult>> liszt;
 		ArrayList<TextFragment> froggies = new ArrayList<TextFragment>();
 		String sTranslation="";
 		MicrosoftMTConnector mmtc = new MicrosoftMTConnector();
 		Parameters params = (Parameters) mmtc.getParameters();
 		// Add ClientId and Secret to test
 		params.setClientId("");
 		params.setSecret("");
 		params.setCategory("");
 		
 		// test query
 		mmtc.open();
 		mmtc.setLanguages(new LocaleId("en-US"), new LocaleId("es-ES"));
 		mmtc.setThreshold(0);
 		frag = new TextFragment("What a wonderful bird the frog are!");
 		mmtc.query(frag);
 		if (mmtc.hasNext()) {
 			result = mmtc.next();
 			frag = result.target;
 			sTranslation = frag.getText();
 		}
 		assertTrue(sTranslation.equals("Lo que un pájaro maravilloso son la rana!"));
 		
 		froggies.add(new TextFragment("When he stand he sit almost."));
 		froggies.add(new TextFragment("When he hop he fly almost."));
 		froggies.add(new TextFragment("He ain't got no sense hardly."));
 		liszt = mmtc.batchQuery(froggies);
 		lynn = liszt.size();
 		sTranslation = "";
 		for(int i=0; i<lynn; i++) {
 			franz = liszt.get(i);
 			lenny = franz.size();
 			for(int j=0; j<lenny; j++) {
 				sTranslation += "$" + franz.get(j).target.getText();				
 			}
 		}
 		assertTrue(sTranslation.equals("$Cuando él siente casi.$Cuando él hop volar casi.$No consiguió ningún sentido apenas."));
 //		assertTrue(sTranslation.equals("$Cuando él presentarse él sentarse casi.$Cuando él salto él volar casi.$No consiguió ningún sentido difícilmente."));
 	}
 }
