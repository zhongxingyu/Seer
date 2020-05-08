 /*******************************************************************************
  * Copyright (c) May 20, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Martin Taal - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.logic;
 
 import java.util.Date;
 import java.util.List;
 
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.Expression;
 import com.netxforge.netxstudio.library.ExpressionResult;
 import com.netxforge.netxstudio.scheduling.ExpressionFailure;
 import com.netxforge.netxstudio.scheduling.SchedulingFactory;
 import com.netxforge.netxstudio.server.CommonLogic;
 import com.netxforge.netxstudio.server.logic.expression.IExpressionEngine;
 
 /**
  * Common code for the engine implementations.
  * 
  * @author Martin Taal
  */
 public abstract class BaseEngine {
 
 	// on purpose no @Inject as we need the same instance
 	// as used in the job implementation
 	private IDataProvider dataProvider;
 
 	private Component component;
 
 	@Inject
 	private IExpressionEngine expressionEngine;
 
 	@Inject
 	private CommonLogic commonLogic;
 	
 	@Inject
 	private ModelUtils modelUtils;
 	
 	private DateTimeRange range;
 	private Date start;
 	private Date end;
 
 	private ExpressionFailure failure;
 
 	public abstract void execute();
 
 	protected void runForExpression(Expression expression) {
 		try {
 			if (expression == null) {
 				return;
 			}
 			expressionEngine.setExpression(expression);
 			expressionEngine.run();
 			if (expressionEngine.errorOccurred()) {
 				// stop here will be logged
 				throw new IllegalStateException(expressionEngine.getThrowable());
 			}
 			final List<ExpressionResult> result = expressionEngine
 					.getExpressionResult();
 
 			// process the result
 			commonLogic.processResult(result, start, end);
 
 		} catch (final Throwable t) {
 			t.printStackTrace(System.err);
 			failure = SchedulingFactory.eINSTANCE.createExpressionFailure();
 			failure.setExpressionRef(expression);
 			failure.setMessage(t.getMessage());
 			failure.setComponentRef(component);
 		}
 	}
 
 	public IDataProvider getDataProvider() {
 		return dataProvider;
 	}
 
 	public void setDataProvider(IDataProvider dataProvider) {
 		this.dataProvider = dataProvider;
 	}
 
 	public DateTimeRange getRange() {
 		return range;
 	}
 
 	public void setRange(DateTimeRange range) {
 		this.range = range;
 		start = modelUtils.fromXMLDate(range.getBegin());
 		end = modelUtils.fromXMLDate(range.getEnd());		
 	}
 
 	public Component getComponent() {
 		return component;
 	}
 
 	public void setComponent(Component component) {
 		this.component = component;
 	}
 
 	public ExpressionFailure getFailure() {
 		return failure;
 	}
 
 	public void setFailure(ExpressionFailure failure) {
 		this.failure = failure;
 	}
 
 	public IExpressionEngine getExpressionEngine() {
 		return expressionEngine;
 	}
 
 	public CommonLogic getCommonLogic() {
 		return commonLogic;
 	}
 
 	public ModelUtils getModelUtils() {
 		return modelUtils;
 	}
 
 	public Date getStart() {
 		return start;
 	}
 
 	public Date getEnd() {
 		return end;
 	}
 
 }
