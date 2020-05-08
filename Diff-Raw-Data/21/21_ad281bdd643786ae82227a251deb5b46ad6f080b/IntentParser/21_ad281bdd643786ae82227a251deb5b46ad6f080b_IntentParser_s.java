 /*
  * Copyright (c) 2013 CaneData.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.canedata.provider.mongodb.entity;
 
 import org.bson.BSONObject;
 import org.canedata.core.intent.Step;
 import org.canedata.core.intent.Tracer;
 import org.canedata.core.logging.LoggerFactory;
 import org.canedata.core.util.Limiter;
 import org.canedata.core.util.StringUtils;
 import org.canedata.exception.AnalyzeBehaviourException;
 import org.canedata.logging.Logger;
 import org.canedata.provider.mongodb.expr.MongoExpression;
 import org.canedata.provider.mongodb.expr.MongoExpressionFactory;
 import org.canedata.provider.mongodb.intent.MongoIntent;
 import org.canedata.provider.mongodb.intent.MongoStep;
 
 import com.mongodb.BasicDBObject;
 
 /**
  * 
  * @author Sun Yat-ton
  * @version 1.00.000 2011-8-9
  */
 public class IntentParser {
 	protected static final Logger logger = LoggerFactory
 			.getLogger(IntentParser.class);
 
 	public static void parse(MongoIntent intent,
 			final MongoExpressionFactory expFactory,
 			final BasicDBObject fields, final BasicDBObject projection,
 			final Limiter limiter, final BasicDBObject sorter, final BasicDBObject options)
 			throws AnalyzeBehaviourException {
 		options.append(Options.RETAIN, false).append(Options.UPSERT, false);
 
		final BasicDBObject cmd_sets = new BasicDBObject();
 
 		intent.playback(new Tracer() {
 
 			public Tracer trace(Step step) throws AnalyzeBehaviourException {
 				switch (step.step()) {
 				case MongoStep.FILTER:
 					logger.debug("Putting filter ...");
 
 					if (null != step.getScalar())
 						expFactory.addExpression((MongoExpression) step
 								.getScalar()[0]);
 
 					break;
 				case MongoStep.PROJECTION:
 					if (null == projection)
 						break;
 
 					for (Object field : step.getScalar()) {
 						String f = (String) field;
 
 						logger.debug("Projected field {0} ...", f);
 
 						projection.append(f, 1);
 					}
 
 					break;
 				case MongoStep.LIMIT:
 					if (null == limiter)
 						break;
 
 					limiter.reset();
 
 					if (step.getScalar().length == 2)
 						limiter.limit((Integer) step.getScalar()[0],
 								(Integer) step.getScalar()[1]);
 					else
 						limiter.count((Integer) step.getScalar()[0]);
 
 					break;
 				case MongoStep.ORDER:
 					if (null == sorter)
 						break;
 
 					int ord = 1;
 					if (step.getPurpose() != null)
 						ord = -1;
 
 					String[] ordFs = (String[]) step.getScalar();
 					for (String f : ordFs) {
 						sorter.append(f, ord);
 					}
 					break;
 
 				case MongoStep.PUT:
 					if (null == fields
 							|| StringUtils.isBlank(step.getPurpose()))
 						break;
 
 					Object val = (step.getScalar() == null || step.getScalar().length == 0) ? null
 							: step.getScalar()[0];
 
					if (step.getPurpose().matches(MongoEntity.internalCmds)
							&& !step.getPurpose().equalsIgnoreCase("$set")) {
						fields.append(step.getPurpose(), val);
					} else {
						if (step.getPurpose().equalsIgnoreCase("$set"))
							cmd_sets.putAll((BSONObject) step.getScalar()[0]);
						else
							cmd_sets.append(step.getPurpose(), val);
					}
 
 					break;
 				case MongoStep.OPTION:
 					options.append(step.getPurpose(), step.getScalar()[0]);
 					break;
 				default:
                         logger.warn(
 							"Step {0} does not apply to activities query, this step will be ignored.",
 							step.getName());
 				}
 
 				return this;
 			}
 
 		});
 
		if(!cmd_sets.isEmpty())
			fields.putAll(cmd_sets.toMap());
 	}
 }
