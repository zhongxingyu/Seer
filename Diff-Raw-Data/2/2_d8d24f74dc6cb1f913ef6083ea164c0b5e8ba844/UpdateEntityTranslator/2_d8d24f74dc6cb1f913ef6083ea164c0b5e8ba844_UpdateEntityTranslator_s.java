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
 import org.nebulae2us.stardust.expr.domain.Expression;
 import org.nebulae2us.stardust.expr.domain.UpdateEntityExpression;
 import org.nebulae2us.stardust.internal.util.SQLUtils;
 import org.nebulae2us.stardust.my.domain.Attribute;
 import org.nebulae2us.stardust.my.domain.Entity;
 import org.nebulae2us.stardust.my.domain.EntityAttribute;
 import org.nebulae2us.stardust.my.domain.EntityDiscriminator;
 import org.nebulae2us.stardust.my.domain.EntityIdentifier;
 import org.nebulae2us.stardust.my.domain.ScalarAttribute;
 import org.nebulae2us.stardust.sql.domain.LinkedTableEntity;
 import org.nebulae2us.stardust.sql.domain.LinkedTableEntityBundle;
 
 /**
  * @author Trung Phan
  *
  */
 public class UpdateEntityTranslator implements Translator {
 
 	public boolean accept(Expression expression, ParamValues paramValues) {
 		return expression instanceof UpdateEntityExpression;
 	}
 
 	public SqlBundle translate(TranslatorContext context, Expression expression, ParamValues paramValues) {
 		
 		SqlBundle result = EmptySqlBundle.getInstance();
 		
 		Object entityToInsert = paramValues.getNextWildcardValue();
 		
 		LinkedTableEntityBundle linkedTableEntityBundle = context.getLinkedTableEntityBundle();
 		
 		UpdateEntityExpression updateExpression = (UpdateEntityExpression)expression;
 		
 		Entity entity = context.getLinkedEntityBundle().getRoot().getEntity();
 
 		for (int tableIndex = 0; tableIndex < linkedTableEntityBundle.getLinkedTableEntities().size(); tableIndex++) {
 		
 			LinkedTable linkedTable = entity.getLinkedTableBundle().getLinkedTables().get(tableIndex);
 			
 			LinkedTableEntity linkedTableEntity = linkedTableEntityBundle.findLinkedTableEntity(linkedTable.getTable());
 	
 			List<Object> values = new ArrayList<Object>();
 			
 			StringBuilder updateSql = new StringBuilder();
 			
 			updateSql.append("update ")
 				.append(SQLUtils.getFullTableName(updateExpression.getOverridingSchema(), context.getDefaultSchema(), linkedTableEntity.getTable()))
 				.append("\n  set ");
 			
 			EntityDiscriminator entityDiscriminator = entity.getEntityDiscriminator();
 			if (tableIndex == 0 && entityDiscriminator != null) {
 				updateSql.append(entityDiscriminator.getColumn().getName()).append(" = ?,\n");
 				Object value = entityDiscriminator.getValue();
 				values.add(value);
 			}
 			
 			for (Attribute attribute : linkedTableEntity.getOwningSideAttributes()) {
 				if (attribute instanceof ScalarAttribute) {
 					if (!attribute.isUpdatable()) {
 						continue;
 					}
 					
 					ScalarAttribute scalarAttribute = (ScalarAttribute)attribute;
 					
 					if (entity.getEntityIdentifier().containsColumn(scalarAttribute.getColumn())) {
 						continue;
 					}
 					
 					updateSql.append(scalarAttribute.getColumn().getName())
 						.append(" = ?,\n");
 					
					Object value = attribute.extractValueForPersistence(entityToInsert);
 					values.add(value);
 					
 				}
 				else if (attribute instanceof EntityAttribute) {
 					EntityAttribute entityAttribute = (EntityAttribute)attribute;
 					
 					Object friendObject = attribute.extractValueForPersistence(entityToInsert);
 					Entity friendEntity = entityAttribute.getEntity();
 					List<ScalarAttribute> friendIdentifierAttributes = friendEntity.getIdentifierScalarAttributes();
 					
 					for (int i = 0; i < entityAttribute.getLeftColumns().size(); i++) {
 						Column leftColumn = entityAttribute.getLeftColumns().get(i);
 						ScalarAttribute friendIdentifierAttribute = friendIdentifierAttributes.get(i);
 						
 						updateSql.append(leftColumn.getName()).append(" = ?,\n");
 						
 						if (friendObject == null) {
 							values.add(null);
 						}
 						else {
 							Object value = friendIdentifierAttribute.extractValueForPersistence(friendObject);
 							values.add(value);
 						}
 						
 					}
 				}
 			}
 	
 			updateSql.setCharAt(updateSql.length() - 2, ' ');
 			
 			updateSql.append("where ");
 	
 			EntityIdentifier identifier = entity.getEntityIdentifier();
 			List<ScalarAttribute> identifierScalarAttributes = identifier.getScalarAttributes();
 			for (int i = 0; i < identifierScalarAttributes.size(); i++) {
 				ScalarAttribute identifierScalarAttribute = identifierScalarAttributes.get(i);
 				Column idColumn = identifierScalarAttribute.getColumn();
 				if (!linkedTableEntity.isRoot()) {
 					idColumn = linkedTableEntity.getColumns().get(i);
 				}
 				updateSql.append(idColumn.getName())
 					.append(" = ? and ");
 		
 				Object value = identifierScalarAttribute.extractValueForPersistence(entityToInsert);
 				values.add(value);
 			}
 			
 			updateSql.replace(updateSql.length() - 5, updateSql.length(), "");
 	
 			result = result.join(new SingleStatementSqlBundle(updateSql.toString(), values));
 		}
 
 		return result;
 		
 	}
 
 }
