 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-04-09
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis;
 
 import eu.serscis.sam.node.TStringLiteral;
 import java.util.List;
 import java.util.Arrays;
 import org.deri.iris.api.basics.IPredicate;
 import static org.deri.iris.factory.Factory.*;
 
 public class Constants {
 	static public IPredicate importP = BASIC.createPredicate("import", 2);
	static public IPredicate definedTypeP = BASIC.createPredicate("definedType", 1);
 	static public IPredicate isAP = BASIC.createPredicate("isA", 2);
 	static public IPredicate liveMethodP = BASIC.createPredicate("liveMethod", 3);
 	static public IPredicate hasCallSiteP = BASIC.createPredicate("hasCallSite", 2);
 	static public IPredicate didCallP = BASIC.createPredicate("didCall", 6);
 	static public IPredicate didGetP = BASIC.createPredicate("didGet", 4);
 	static public IPredicate didGetExceptionP = BASIC.createPredicate("didGetException", 4);
 	static public IPredicate didCreateP = BASIC.createPredicate("didCreate", 4);
 	static public IPredicate mayCallObjectP = BASIC.createPredicate("mayCallObject", 4);
 	static public IPredicate callsMethodP = BASIC.createPredicate("callsMethod", 2);
 	static public IPredicate callsAnyMethodP = BASIC.createPredicate("callsAnyMethod", 1);
 	static public IPredicate maySendP = BASIC.createPredicate("maySend", 5);
 	static public IPredicate mayCreateP = BASIC.createPredicate("mayCreate", 2);
 	static public IPredicate mayAcceptP = BASIC.createPredicate("mayAccept", 2);
 	static public IPredicate mayReturnP = BASIC.createPredicate("mayReturn", 4);
 	static public IPredicate mayThrowP = BASIC.createPredicate("mayThrow", 4);
 	static public IPredicate hasFieldP = BASIC.createPredicate("hasField", 2);
 	static public IPredicate hasConstructorP = BASIC.createPredicate("hasConstructor", 2);
 	static public IPredicate hasMethodP = BASIC.createPredicate("hasMethod", 2);
 	static public IPredicate methodNameP = BASIC.createPredicate("methodName", 2);	/* Class.method -> "method" */
 	static public IPredicate localP = BASIC.createPredicate("local", 4);
 	static public IPredicate fieldP = BASIC.createPredicate("field", 3);
 	static public IPredicate debugEdgeP = BASIC.createPredicate("debugEdge", 5);
 
 	static <X> List<X> makeList(X... items) {
 		return Arrays.asList(items);
 	}
 
 	static String getString(TStringLiteral literal) {
 		String str = literal.getText();
 		return str.substring(1, str.length() - 1);	// TODO: ignores escapes
 	}
 
 }
