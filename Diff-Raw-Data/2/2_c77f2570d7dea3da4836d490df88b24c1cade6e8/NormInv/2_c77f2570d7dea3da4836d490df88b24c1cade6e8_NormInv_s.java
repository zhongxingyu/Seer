 /*
  * Copyright 2013 ENERKO Informatik GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
  * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
  * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
  * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
  * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package de.enerko.reports2.functions;
 
 import org.apache.poi.ss.formula.OperationEvaluationContext;
 import org.apache.poi.ss.formula.eval.EvaluationException;
 import org.apache.poi.ss.formula.eval.NumberEval;
 import org.apache.poi.ss.formula.eval.OperandResolver;
 import org.apache.poi.ss.formula.eval.ValueEval;
 import org.apache.poi.ss.formula.functions.FreeRefFunction;
 
 /**
  * Java Implementation of http://support.microsoft.com/kb/827358/en-us implementing 
  * Apache POIs {@link FreeRefFunction} to be used as a user defined function.
  * 
  * Mathematics found here: 
  * https://gist.github.com/kmpm/1211922/
  * https://gist.github.com/kmpm/1211922/raw/a11e0dfc9fab493bcdadc669f3213d11f1897ebf/norminv.js
  * 
  * Register for a given workbook with:
  * 
     <code>
 		final UDFFinder udfs = new DefaultUDFFinder(new String[]{ "MS_NormInv" }, new FreeRefFunction[]{ new NormInv() }) ;        
 		workbook.addToolPack(new AggregatingUDFFinder(new UDFFinder[] {udfs}));
     </code>
  * @author michael.simons, 2013-02-21
  */
 public class NormInv implements FreeRefFunction {
 	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {		
 		try {
 			final ValueEval p = OperandResolver.getSingleValue(args[0], ec.getRowIndex(), ec.getColumnIndex()) ;
 			final ValueEval mu = OperandResolver.getSingleValue(args[1], ec.getRowIndex(), ec.getColumnIndex()) ;
 			final ValueEval sigma = OperandResolver.getSingleValue(args[2], ec.getRowIndex(), ec.getColumnIndex()) ;			
 			return new NumberEval(this.compute(OperandResolver.coerceValueToDouble(p), OperandResolver.coerceValueToDouble(mu), OperandResolver.coerceValueToDouble(sigma)));
 		} catch (EvaluationException e) {
 			return e.getErrorEval();
 		}			 		
 	}
 	
 	/**
	 * Original C++ implementation found at http://www.wilmott.com/messageview.cfm?catid=10&threadid=38771
 	 * C# implementation found at http://weblogs.asp.net/esanchez/archive/2010/07/29/a-quick-and-dirty-implementation-of-excel-norminv-function-in-c.aspx
 	 *    Compute the quantile function for the normal distribution.
 	 *
 	 *    For small to moderate probabilities, algorithm referenced
 	 *    below is used to obtain an initial approximation which is
 	 *    polished with a final Newton step.
 	 *    For very large arguments, an algorithm of Wichura is used.
 	 *
 	 *  REFERENCE
 	 *
 	 *    Beasley, J. D. and S. G. Springer (1977).
 	 *    Algorithm AS 111: The percentage points of the normal distribution,
 	 *    Applied Statistics, 26, 118-121.
 	 *
 	 *     Wichura, M.J. (1988).
 	 *     Algorithm AS 241: The Percentage Points of the Normal Distribution.
 	 *     Applied Statistics, 37, 477-484.
 	 * @param p
 	 * @param mu
 	 * @param sigma
 	 * @return
 	 */
 	public double compute(double p, double mu, double sigma) {
 		if(p < 0 || p > 1) 
 			throw new RuntimeException("The probality p must be bigger than 0 and smaller than 1");		
 		if(sigma < 0)
 			throw new RuntimeException("The standard deviation sigma must be positive");
 		if(p == 0)
 			return Double.NEGATIVE_INFINITY;		
 		if(p == 1)
 			return Double.POSITIVE_INFINITY;		
 		if(sigma == 0)
 			return mu;		
 		double  q, r, val;
 
 		q = p - 0.5;
 
 		/* 0.075 <= p <= 0.925 */
 		if(Math.abs(q) <= .425) {
 			r = .180625 - q * q;
 			val =
 		         q * (((((((r * 2509.0809287301226727 +
 		                    33430.575583588128105) * r + 67265.770927008700853) * r +
 		                  45921.953931549871457) * r + 13731.693765509461125) * r +
 		                1971.5909503065514427) * r + 133.14166789178437745) * r +
 		              3.387132872796366608)
 		         / (((((((r * 5226.495278852854561 +
 		                  28729.085735721942674) * r + 39307.89580009271061) * r +
 		                21213.794301586595867) * r + 5394.1960214247511077) * r +
 		              687.1870074920579083) * r + 42.313330701600911252) * r + 1);
 		}
 		/* closer than 0.075 from {0,1} boundary */
 		else {
 	      /* r = min(p, 1-p) < 0.075 */
 	      if (q > 0)
 	          r = 1 - p;
 	      else
 	          r = p;
 		      r = Math.sqrt(-Math.log(r));
 		      /* r = sqrt(-log(r))  <==>  min(p, 1-p) = exp( - r^2 ) */
 
 		      if (r <= 5) { /* <==> min(p,1-p) >= exp(-25) ~= 1.3888e-11 */
 		    	  r += -1.6;
 		          val = (((((((r * 7.7454501427834140764e-4 +
 		                     .0227238449892691845833) * r + .24178072517745061177) *
 		                   r + 1.27045825245236838258) * r +
 		                  3.64784832476320460504) * r + 5.7694972214606914055) *
 		                r + 4.6303378461565452959) * r +
 		               1.42343711074968357734)
 		              / (((((((r *
 		                       1.05075007164441684324e-9 + 5.475938084995344946e-4) *
 		                      r + .0151986665636164571966) * r +
 		                     .14810397642748007459) * r + .68976733498510000455) *
 		                   r + 1.6763848301838038494) * r +
 		                  2.05319162663775882187) * r + 1);
 		      } else { /* very close to  0 or 1 */
 		          r += -5;
 		          val = (((((((r * 2.01033439929228813265e-7 +
 		                     2.71155556874348757815e-5) * r +
 		                    .0012426609473880784386) * r + .026532189526576123093) *
 		                  r + .29656057182850489123) * r +
 		                 1.7848265399172913358) * r + 5.4637849111641143699) *
 		               r + 6.6579046435011037772)
 		              / (((((((r *
 		                       2.04426310338993978564e-15 + 1.4215117583164458887e-7) *
 		                      r + 1.8463183175100546818e-5) * r +
 		                     7.868691311456132591e-4) * r + .0148753612908506148525)
 		                   * r + .13692988092273580531) * r +
 		                  .59983220655588793769) * r + 1);
 		      }
 
 		      if (q < 0.0) {
 		          val = -val;
 		      }
 		  }
 
 		  return mu + sigma * val;		
 	}
 }
