 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.nebulae2us.stardust.translate.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.nebulae2us.stardust.dao.SqlBundle;
 import org.nebulae2us.stardust.db.domain.Column;
 import org.nebulae2us.stardust.db.domain.LinkedTable;
 import org.nebulae2us.stardust.expr.domain.DeleteEntityExpression;
 import org.nebulae2us.stardust.expr.domain.Expression;
 import org.nebulae2us.stardust.internal.util.SQLUtils;
 import org.nebulae2us.stardust.my.domain.Entity;
 import org.nebulae2us.stardust.my.domain.EntityIdentifier;
 import org.nebulae2us.stardust.my.domain.ScalarAttribute;
 import org.nebulae2us.stardust.sql.domain.LinkedTableEntity;
 import org.nebulae2us.stardust.sql.domain.LinkedTableEntityBundle;
 import static org.nebulae2us.stardust.internal.util.BaseAssert.*;
 
 /**
  * @author Trung Phan
  *
  */
 public class DeleteEntityTranslator implements Translator {
 
 	public boolean accept(Expression expression, ParamValues paramValues) {
 		return expression instanceof DeleteEntityExpression;
 	}
 
 	public SqlBundle translate(TranslatorContext context, Expression expression, ParamValues paramValues) {
 		
 		SqlBundle result = EmptySqlBundle.getInstance();
 		
 		Object entityToInsert = paramValues.getNextWildcardValue();
 		
 		LinkedTableEntityBundle linkedTableEntityBundle = context.getLinkedTableEntityBundle();
 		
 		DeleteEntityExpression deleteExpression = (DeleteEntityExpression)expression;
 		
 		Entity entity = context.getLinkedEntityBundle().getRoot().getEntity();
 
 		for (int tableIndex = 0; tableIndex < linkedTableEntityBundle.getLinkedTableEntities().size(); tableIndex++) {
 		
 			LinkedTable linkedTable = entity.getLinkedTableBundle().getLinkedTables().get(tableIndex);
 			
 			LinkedTableEntity linkedTableEntity = linkedTableEntityBundle.findLinkedTableEntity(linkedTable.getTable());
 	
 			List<Object> values = new ArrayList<Object>();
 			
 			StringBuilder deleteSql = new StringBuilder();
 			
			deleteSql.append("delete ")
 				.append(SQLUtils.getFullTableName(deleteExpression.getOverridingSchema(), context.getDefaultSchema(), linkedTableEntity.getTable()))
 				.append("\n  where ");
 			
 			EntityIdentifier identifier = entity.getEntityIdentifier();
 			
 			
 			List<ScalarAttribute> identifierScalarAttributes = identifier.getScalarAttributes();
 			AssertSyntax.isTrue(identifierScalarAttributes.size() > 0, "Cannot delete non-id entity");
 			
 			for (int i = 0; i < identifierScalarAttributes.size(); i++) {
 				ScalarAttribute identifierScalarAttribute = identifierScalarAttributes.get(i);
 				Column idColumn = identifierScalarAttribute.getColumn();
 				if (!linkedTableEntity.isRoot()) {
 					idColumn = linkedTableEntity.getColumns().get(i);
 				}
 				deleteSql.append(idColumn.getName())
 					.append(" = ? and ");
 		
 				Object value = identifierScalarAttribute.extractValueForPersistence(entityToInsert);
 				values.add(value);
 			}
 			
 			deleteSql.replace(deleteSql.length() - 5, deleteSql.length(), "");
 	
 			result = result.join(new SingleStatementSqlBundle(deleteSql.toString(), values));
 		}
 		
 		return result;
 	}
 
 }
