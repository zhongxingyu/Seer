 /*
  * JGraLab - The Java graph laboratory
  * (c) 2006-2009 Institute for Software Technology
  *               University of Koblenz-Landau, Germany
  *
  *               ist@uni-koblenz.de
  *
  * Please report bugs to http://serres.uni-koblenz.de/bugzilla
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.uni_koblenz.jgralab.greql2.funlib;
 
 import java.util.ArrayList;
 
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
 import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
 import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
 import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
 import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
 import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
 
 /**
  * Calculates the arithmetic average of the given collection.
  * 
  * <dl>
  * <dt><b>GReQL-signature</b></dt>
  * <dd><code>DOUBLE avg(c:COLLECTION)</code></dd>
  * <dd>&nbsp;</dd>
  * </dl>
  * <dl>
  * <dt></dt>
  * <dd>
  * <dl>
  * <dt><b>Parameters:</b></dt>
  * <dd><code>c</code> - collection to calculate the average for</dd>
  * <dt><b>Returns:</b></dt>
  * <dd>the arithmetic average of the given collection</dd>
  * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
  * </dl>
  * </dd>
  * </dl>
  * 
  * @author ist@uni-koblenz.de
  * 
  */
 
 public class Avg extends Greql2Function {
 
 	{
 		JValueType[][] x = { { JValueType.COLLECTION, JValueType.NUMBER } };
 		signatures = x;
 
		description = "Arithmetic average of the given collection $c$ of numbers.\n"
 				+ "Returns null, if a null-Value is in $c$.";
 
 		Category[] c = { Category.COLLECTIONS_AND_MAPS };
 		categories = c;
 	}
 
 	@Override
 	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
 			JValue[] arguments) throws EvaluateException {
 		if (checkArguments(arguments) == -1) {
 			throw new WrongFunctionParameterException(this, arguments);
 		}
 
 		JValueCollection col = arguments[0].toCollection();
 		double sum = 0;
 		for (JValue curVal : col) {
 			if (curVal.isNumber()) {
 				sum += curVal.toNumber().doubleValue();
 			} else {
 				throw new WrongFunctionParameterException(this, arguments);
 			}
 		}
 		return new JValue(1.0 * sum / col.size());
 	}
 
 	@Override
 	public long getEstimatedCosts(ArrayList<Long> inElements) {
 		return inElements.get(0);
 	}
 
 	@Override
 	public double getSelectivity() {
 		return 1;
 	}
 
 	@Override
 	public long getEstimatedCardinality(int inElements) {
 		return 1;
 	}
 
 }
