 package genlab.algog.internal;
 
 import cern.jet.random.Uniform;
 
 public class AIntegerGene extends ANumericGene<Integer> {
 
 	public AIntegerGene(String name, double mutationProba, Integer min, Integer max) {
 		super(name, mutationProba, min, max);
 	}
 
 
 	@Override
 	public Integer crossoverArithmetic(Integer one, Integer other, double weight) {
 
 		Integer result = (int)Math.floor(weight*one) + (int)Math.ceil((1-weight)*other);
 		
 		if (result > max)
 			return max;
 		else if (result < min)
 			return min;
 		else
 			return result;
 	}
 
 	@Override
 	public Integer crossoverArithmetic(Object one, Object other, double weight) {
 		return crossoverArithmetic((Integer)one, (Integer)other, weight);
 	}
 
 	@Override
 	public Integer generateRandomnly(Uniform uniform) {
 		return uniform.nextIntFromTo(min, max);
 	}
 
 	@Override
 	public Integer mutate(Uniform uniform, Object previousValue) {
 		return uniform.nextIntFromTo(min, max);
 	}
 
 	@Override
 	public Integer[] crossoverSBX(Uniform U, Object genesA, Object genesB) {

		Double gA = ((Integer)genesA).doubleValue();
		Double gB = ((Integer)genesB).doubleValue();
 		
 		if( U.nextDoubleFromTo(0, 1)<0.5 ) {
			Double rand;
			Double y1, y2, yl, yu;
			Double c1, c2;
			Double alpha, beta, betaq;
 
             if( StrictMath.abs(gA-gB)>EPS ) {
                 if( gA<gB ) {
                     y1 = gA;
                     y2 = gB;
                 }else {
                     y1 = gB;
                     y2 = gA;
                 }
                 
                yl = min.doubleValue();
                yu = max.doubleValue();
                 rand = U.nextDoubleFromTo(0, 1);
                 beta = 1.0 + (2.0*(y1-yl)/(y2-y1));
                 alpha = 2.0 - StrictMath.pow( beta , -(eta_c+1.0) );
                 
                 if( rand<=(1.0/alpha) ) {
                     betaq = StrictMath.pow( (rand*alpha) , (1.0/(eta_c+1.0)) );
                 }else {
                     betaq = StrictMath.pow( (1.0/(2.0-rand*alpha)) , (1.0/(eta_c+1.0)) );
                 }
                 
                 c1 = 0.5*((y1+y2)-betaq*(y2-y1));
                 beta = 1.0 + (2.0*(yu-y2)/(y2-y1));
                 alpha = 2.0 - StrictMath.pow( beta , -(eta_c+1.0) );
                 
                 if( rand<=(1.0/alpha) ) {
                     betaq = StrictMath.pow( (rand*alpha) , (1.0/(eta_c+1.0)) );
                 }else {
                     betaq = StrictMath.pow( (1.0/(2.0-rand*alpha)) , (1.0/(eta_c+1.0)) );
                 }
                 
                 c2 = 0.5*((y1+y2)+betaq*(y2-y1));
                 
                 if( c1<yl ) c1 = yl;
                 if( c2<yl ) c2 = yl;
                 if( c1>yu ) c1 = yu;
                 if( c2>yu ) c2 = yu;
                 
                 if( U.nextDoubleFromTo(0, 1)<=0.5 ) {
                	return new Integer[]{c2.intValue(), c1.intValue()};
                 }else {
                	return new Integer[]{c1.intValue(), c2.intValue()};
                 }
             }else {
                 if( U.nextDoubleFromTo(0, 1)<=0.5 ) {
                 	return new Integer[]{(Integer)genesA, (Integer)genesB};
                 }else {
                	return new Integer[]{(Integer)genesB, (Integer)genesA};
                 }
             }
 		}else {
        	return new Integer[]{(Integer)genesA, (Integer)genesB};
 		}
 	}
 }
