 package awful.objects;
 
 import awful.AEnvironment;
 import awful.exceptions.EvaluationException;
 
 public class AListPair extends AObject {
 	private AObject car;
 	private AObject cdr;
 	
 	public AListPair(AObject car, AObject cdr) {
 		this.setCar(car);
 		this.setCdr(cdr);
 	}
 
 	public AObject getCar() {
 		return car;
 	}
 
 	public void setCar(AObject car) {
 		this.car = car;
 	}
 
 	public AObject getCdr() {
 		return cdr;
 	}
 
 	public void setCdr(AObject cdr) {
 		this.cdr = cdr;
 	}
 	
 	public String toString(boolean inline) {
 		StringBuilder sb = new StringBuilder();
 		if (!inline)
 			sb.append("(");
		if (this.cdr instanceof AListPair)
			sb.append(String.format("%s %s", this.car.toString(), ((AListPair) this.cdr).toString(true)));
		else if (this.cdr instanceof AEmptyList)
 			sb.append(this.car.toString());
 		else
 			sb.append(String.format("%s . %s", this.car.toString(), this.cdr.toString()));
 		if (!inline)
 			sb.append(")");
 		return sb.toString();
 	}
 	
 	public String toString() {
 		return this.toString(false);
 		//return String.format("(%s . %s)", this.car.toString(), this.cdr.toString());
 	}
 	
 	public AObject evaluate(AEnvironment env, AListPair parameters) throws EvaluationException {
 		AObject car = this.car.evaluate(env, null);
 		if (car instanceof AOperator) {
 			if (!(this.cdr instanceof AListPair))
 				throw new EvaluationException("Expected AListPair");
 			return car.evaluate(env, (AListPair) this.cdr);
 		}
 		else
 		{
 			return new AListPair(car, this.cdr.evaluate(env, null));
 		}
 	}
 }
