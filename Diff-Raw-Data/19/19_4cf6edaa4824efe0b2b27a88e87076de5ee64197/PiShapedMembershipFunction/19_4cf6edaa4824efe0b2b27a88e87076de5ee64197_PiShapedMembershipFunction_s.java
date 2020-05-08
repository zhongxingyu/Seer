 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an 
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language 
  * governing permissions and limitations under the License.
  */
 package fuzzy.mf;
 
 import java.io.Serializable;
 
 import org.apache.commons.math.util.FastMath;
 
 /**
  * PI Shaped Membership Function. Equivalent to Matlab 
  * <a href="http://www.mathworks.com/help/toolbox/fuzzy/pimf.html">pimf</a> 
  * function. 
  * 
  * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
  * @since 0.1
  */
 public class PiShapedMembershipFunction implements MembershipFunction, Serializable {
 
 	/**
 	 * serialVersionUID declaration.
 	 */
 	private static final long serialVersionUID = 6571079133599244632L;
 	
 	private final double a;
 	private final double b;
 	private final double c;
 	private final double d;
 	
 	public PiShapedMembershipFunction(double a, double b, double c, double d) {
 		this.a = a;
 		this.b = b;
 		this.c = c;
 		this.d = d;
 	}
 	
 	public Double evaluate(Double x) {
 		if(x <= a) {
 			return 0.0;
 		} else if (a <= x && x <= ((a+b)/2)) {
			return 2 * FastMath.pow(((x-a)/(b/a)), 2);
 		} else if(((a+b)/2) <= x && x <= b) {
 			return 1 - 2 * FastMath.pow(((x-b)/(b-a)), 2);
 		} else if(b <= x && x <= c) {
 			return 1.0;
 		} else if(c <= x && x <= ((c+d)/2)) {
 			return 1 - 2 * FastMath.pow(((x-c)/(d-c)), 2);
 		} else if((c+d)/2 <= x && x <= d) {
 			return 2 * FastMath.pow(((x-d)/(d-c)), 2);
 		} else if(x >= d) {
 			return 0.0;
 		}
 		return 0.0;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if(obj == null) {
 			return false;
 		}
 		if(obj == this) {
 			return true;
 		}
 		if(!(obj instanceof PiShapedMembershipFunction)) {
 			return false;
 		}
 		final PiShapedMembershipFunction that = (PiShapedMembershipFunction)obj;
 		return this.a == that.a && this.b == that.b && this.c == that.c && this.d == that.d;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		int hash = "PiShapedMembershipFunction".hashCode();
 		hash <<= 2;
 		hash ^= (int)this.a;
 		hash <<= 2;
 		hash ^= (int)this.b;
 		hash <<= 2;
 		hash ^= (int)this.c;
 		hash <<= 2;
 		hash ^= (int)this.d;
 		return hash;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "PI Shaped Membership Function ["+a+" "+b+" "+c+" "+d+"]";
 	}
 	
 }
