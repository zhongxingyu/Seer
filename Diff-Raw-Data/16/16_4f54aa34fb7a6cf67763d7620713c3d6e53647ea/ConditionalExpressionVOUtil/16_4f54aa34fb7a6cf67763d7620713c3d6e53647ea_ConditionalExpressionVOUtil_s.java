 /**
  * Copyright (c) 2013 Christian Pelster.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Christian Pelster - initial API and implementation
  */
 package de.pellepelster.myadmin.db.daos;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.UUID;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import de.pellepelster.myadmin.client.base.jpql.IConditionalExpressionVO;
 import de.pellepelster.myadmin.client.base.jpql.IExpressionObjectVO;
 import de.pellepelster.myadmin.client.base.jpql.IntegerExpressionObjectVO;
 import de.pellepelster.myadmin.client.base.jpql.RelationalOperator;
 import de.pellepelster.myadmin.client.base.jpql.expressions.BooleanExpressionObjectVO;
 import de.pellepelster.myadmin.client.base.jpql.expressions.EntityExpressionObjectVO;
 import de.pellepelster.myadmin.client.base.jpql.expressions.LongExpressionObjectVO;
 import de.pellepelster.myadmin.client.base.jpql.expressions.NamedParameterExpressionObjectVO;
 import de.pellepelster.myadmin.client.base.jpql.expressions.NullExpressionObjectVO;
 import de.pellepelster.myadmin.client.base.jpql.expressions.StringExpressionObjectVO;
 import de.pellepelster.myadmin.client.base.jpql.expressions.TimestampExpressionObjectVO;
 import de.pellepelster.myadmin.db.jpql.expression.ConditionalExpression;
 import de.pellepelster.myadmin.db.jpql.expression.EntityExpressionObject;
 import de.pellepelster.myadmin.db.jpql.expression.IConditionalExpression;
 import de.pellepelster.myadmin.db.jpql.expression.IExpressionObject;
 import de.pellepelster.myadmin.db.jpql.expression.IntegerExpressionObject;
 import de.pellepelster.myadmin.db.jpql.expression.LongExpressionObject;
 import de.pellepelster.myadmin.db.jpql.expression.NamedParameterExpressionObject;
 import de.pellepelster.myadmin.db.jpql.expression.NullExpressionObject;
 import de.pellepelster.myadmin.db.jpql.expression.StringExpressionObject;
 import de.pellepelster.myadmin.db.jpql.expression.TimestampExpressionObject;
 import de.pellepelster.myadmin.db.util.CopyBean;
 import de.pellepelster.myadmin.db.util.EntityVOMapper;
 
 public final class ConditionalExpressionVOUtil
 {
 
 	@Autowired
 	private CopyBean copyBean;
 
 	public static List<IConditionalExpression> getConditionalExpressions(List<IConditionalExpressionVO> conditionalExpressionVOs)
 	{
 		List<IConditionalExpression> ces = new ArrayList<IConditionalExpression>();
 
 		for (IConditionalExpressionVO conditionalExpressionVO : conditionalExpressionVOs)
 		{
			IExpressionObject expressionObject1 = getExpressionObject(conditionalExpressionVO.getExpressionObject1());
			IExpressionObject expressionObject2 = getExpressionObject(conditionalExpressionVO.getExpressionObject2());
 
 			ces.add(new ConditionalExpression(conditionalExpressionVO.getLogicalOperator(), expressionObject1, RelationalOperator
 					.valueOf(conditionalExpressionVO.getRelationalOperator().name()), expressionObject2));
 		}
 
 		return ces;
 
 	}
 
	public static IExpressionObject getExpressionObject(IExpressionObjectVO expressionObjectVO)
 	{
 
 		if (expressionObjectVO instanceof EntityExpressionObjectVO)
 		{
 			EntityExpressionObjectVO voEO = (EntityExpressionObjectVO) expressionObjectVO;
 			return new EntityExpressionObject(voEO.getField());
 		}
 		if (expressionObjectVO instanceof NamedParameterExpressionObjectVO)
 		{
 			NamedParameterExpressionObjectVO voEO = (NamedParameterExpressionObjectVO) expressionObjectVO;
 
 			return new NamedParameterExpressionObject(voEO.getName(), CopyBean.getInstance().copyObject(voEO.getObject(),
 					EntityVOMapper.getInstance().getMappedClass(voEO.getObject().getClass())));
 		}
 
 		if (expressionObjectVO instanceof NullExpressionObjectVO)
 		{
 			return new NullExpressionObject();
 		}
 
 		if (expressionObjectVO instanceof StringExpressionObjectVO)
 		{
 			return new StringExpressionObject(((StringExpressionObjectVO) expressionObjectVO).getValue());
 		}
 
 		if (expressionObjectVO instanceof LongExpressionObjectVO)
 		{
 			return new LongExpressionObject(((LongExpressionObjectVO) expressionObjectVO).getValue());
 		}
 
 		if (expressionObjectVO instanceof BooleanExpressionObjectVO)
 		{
 			BooleanExpressionObjectVO booleanExpressionObjectVO = (BooleanExpressionObjectVO) expressionObjectVO;
			return new NamedParameterExpressionObject(UUID.randomUUID().toString(), booleanExpressionObjectVO.getValue());
 		}
 
 		if (expressionObjectVO instanceof IntegerExpressionObjectVO)
 		{
 			return new IntegerExpressionObject(((IntegerExpressionObjectVO) expressionObjectVO).getValue());
 		}
 
 		if (expressionObjectVO instanceof TimestampExpressionObjectVO)
 		{
 			return new TimestampExpressionObject(((TimestampExpressionObjectVO) expressionObjectVO).getValue());
 		}
 
 		throw new RuntimeException("unsupported expression object type");
 
 	}
 
 	private ConditionalExpressionVOUtil()
 	{
 	}
 
 	public CopyBean getCopyBean()
 	{
 		return this.copyBean;
 	}
 
 	public void setCopyBean(CopyBean copyBean)
 	{
 		this.copyBean = copyBean;
 	}
 }
