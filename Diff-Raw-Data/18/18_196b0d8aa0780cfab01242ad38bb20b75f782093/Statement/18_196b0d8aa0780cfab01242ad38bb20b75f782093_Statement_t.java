 /*
  * junitflood - An automatic junit test generator
  * Copyright 2011 MeBigFatGuy.com
  * Copyright 2011 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.junitflood.generator;
 
 public class Statement {
 	public enum StatementType {
 		CONSTRUCTOR, METHODCALL, ASSIGNMENT
 	}
 
 	private StatementType type;
 	private String className;
 	private String objectName;
 	private String methodName;
 	private Object[] methodArgs;
 
 	private Statement() {
 	}
 
 	public static Statement createConstructor(String clsName, Object... args) {
 		Statement statement = new Statement();
 		statement.type = StatementType.CONSTRUCTOR;
 		statement.className = clsName.replaceAll("/", ".");
 		statement.objectName = "o";
 		statement.methodArgs = new Object[args.length];
 		System.arraycopy(args, 0, statement.methodArgs, 0, args.length);
 		return statement;
 	}
 
	public static Statement createMethodCall(String objectName, String methodName, Object... args) {
 		Statement statement = new Statement();
 		statement.type = StatementType.METHODCALL;
 		statement.objectName = objectName;
		statement.methodName = methodName;
 		statement.methodArgs = new Object[args.length];
 		System.arraycopy(args, 0, statement.methodArgs, 0, args.length);
 		return statement;
 	}
 
 	public String getObjectName() {
 		return objectName;
 	}
 
 	@Override
 	public String toString() {
 
 		StringBuilder sb = new StringBuilder();
 
 		switch (type) {
 			case CONSTRUCTOR: {
 				sb.append(className).append(" ").append(objectName).append(" =  new ").append(className).append("(");
 				String sep = "";
 				for (Object o : methodArgs) {
 					sb.append(sep);
 					sb.append(o);
 					sep = ",";
 				}
 				sb.append(");");
 			}
 			break;
 
 			case METHODCALL: {
 				sb.append(objectName).append(".").append(methodName).append("(");
 				String sep = "";
 				for (Object o : methodArgs) {
 					sb.append(sep);
 					sb.append(o);
 					sep = ",";
 				}
 				sb.append(");");
 			}
 			break;
 
 			case ASSIGNMENT: {
 
 			}
 			break;
 		}
 
 		return sb.toString();
 	}
 }
