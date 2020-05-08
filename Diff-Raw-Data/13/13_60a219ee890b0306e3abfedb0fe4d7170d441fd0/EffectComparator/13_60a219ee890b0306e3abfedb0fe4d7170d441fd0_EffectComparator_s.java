 package simumatch.datamanager;
 
 import java.util.Comparator;
 
 import simumatch.common.Effect;
 import simumatch.common.Operator;
 import simumatch.common.Scope;
 import simumatch.common.Target;
 
 public final class EffectComparator implements Comparator<Effect> {
 	
 	@Override
 	public int compare ( Effect effect1, Effect effect2 ) {
 		if ( effect1 == null || effect2 == null ) {
 			throw new NullPointerException();
 		}
 		
 		int aux = compareOperators( effect1.getOperator(), effect2.getOperator() );
 		if ( aux != 0 ) {
 			return aux;
 		}
 		
 		aux = compareScope( effect1.getScope(), effect2.getScope() );
 		if ( aux != 0 ) {
 			return aux;
 		}
 		
 		aux = compareTarget( effect1.getTarget(), effect2.getTarget() );
 		if ( aux != 0 ) {
 			return aux;
 		}
 		
 		aux = compareBonus( effect1.getBonus(), effect2.getBonus() );
 		if ( aux != 0 ) {
 			return aux;
 		}
 		
 		aux = comparePermanent( effect1.isPermanent(), effect2.isPermanent() );
 		return aux;
 		
 	}
 	
 	private static int compareOperators ( Operator op1, Operator op2 ) {
 		if ( op1 == Operator.PRODUCT && op2 != Operator.PRODUCT ) {
 			return -1;
 		} else if ( op1 != Operator.PRODUCT && op2 == Operator.PRODUCT ) {
 			return 1;
 		} else {
			return 0;
 		}
 	}
 	
 	private static int compareScope ( Scope sp1, Scope sp2 ) {
 		return sp1.compareTo( sp2 );
 	}
 	
 	private static int compareTarget ( Target tg1, Target tg2 ) {
 		return tg1.compareTo( tg2 );
 	}
 	
 	private static int compareBonus ( double db1, double db2 ) {
 		return db1 == db2 ? 0 : db1 < db2 ? -1 : +1;
 	}
 	
 	private static int comparePermanent ( boolean bl1, boolean bl2 ) {
 		// If bl1 and bl2 are the same => 0
 		// Else, if bl1 is true, then bl2 is false=> -1
 		// Else, bl2 is false and bl1 true => +1
		return bl1 == bl2 ? 0
		     : bl1 ? -1
		     : +1;
 	}
 }
