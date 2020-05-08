 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.command;
 
 import java.io.File;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.URI;
import java.sql.Date;
 import java.util.Collection;
 import java.util.List;
 
 import javax.xml.datatype.Duration;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.jbasics.checker.ContractCheck;
 import org.jbasics.command.annotations.CommandParam;
 import org.jbasics.exception.DelegatedException;
 import org.jbasics.pattern.strategy.ExecuteStrategy;
 import org.jbasics.types.tuples.Triplet;
 
 public class CommandSpec implements ExecuteStrategy<Integer, CommandCall> {
 	private final String namespace;
 	private final String name;
 	private final Method commandMethod;
 	private final Triplet<String, Type, Boolean>[] commandParameterMapping;
 
 	public CommandSpec(final String namespace, final String name, final Method commandMethod) {
 		this.namespace = ContractCheck.mustNotBeNullOrTrimmedEmpty(namespace, "namespace"); //$NON-NLS-1$
 		this.name = ContractCheck.mustNotBeNullOrTrimmedEmpty(name, "name"); //$NON-NLS-1$
 		this.commandMethod = ContractCheck.mustNotBeNull(commandMethod, "commandMethod"); //$NON-NLS-1$
 		if (!Modifier.isStatic(commandMethod.getModifiers())) {
 			throw new IllegalArgumentException("Command methods must be public static methods"); //$NON-NLS-1$
 		}
 		this.commandParameterMapping = scanParameters(commandMethod);
 	}
 
 	private Triplet<String, Type, Boolean>[] scanParameters(final Method m) {
 		final Annotation[][] annotations = m.getParameterAnnotations();
 		final Type[] params = m.getGenericParameterTypes();
 		@SuppressWarnings("unchecked")
 		final Triplet<String, Type, Boolean>[] result = new Triplet[params.length];
 		for (int i = 0; i < params.length; i++) {
 			final Annotation[] paramAnnotations = annotations[i];
 			CommandParam cmdParam = null;
 			for (final Annotation paramAnnotation : paramAnnotations) {
 				if (paramAnnotation instanceof CommandParam) {
 					cmdParam = (CommandParam) paramAnnotation;
 					break;
 				}
 			}
 			if (cmdParam == null) {
 				throw new RuntimeException("Missing @CommandParam annotation on the parmeter"); //$NON-NLS-1$
 			}
 			final String parameterName = cmdParam.value().trim();
 			if (parameterName.length() == 0) {
 				throw new RuntimeException("@CommandParam value cannot be an empty string it defines the name of the parameter"); //$NON-NLS-1$
 			}
 			result[i] = new Triplet<String, Type, Boolean>(parameterName, params[i], Boolean.valueOf(cmdParam.optional()));
 		}
 		return result;
 	}
 
 	public Integer execute(final CommandCall request) {
 		final CommandCall cmdCall = ContractCheck.mustNotBeNull(request, "request"); //$NON-NLS-1$
 		final Object[] temp = new Object[this.commandParameterMapping.length];
 		for (int i = 0; i < temp.length; i++) {
 			final Triplet<String, Type, Boolean> paramSpec = this.commandParameterMapping[i];
 			final CommandParameter value = cmdCall.getParameters().get(paramSpec.first());
 			if (value != null) {
 				Class<?> paramType = null;
 				boolean isList = false;
 				final Type paramT = paramSpec.second();
 				if (paramT instanceof Class) {
 					paramType = (Class<?>) paramT;
 				} else if (paramT instanceof ParameterizedType) {
 					paramType = getListType((ParameterizedType) paramT);
 					isList = true;
 				}
 				if (CommandParameter.class.isAssignableFrom(paramType)) {
 					temp[i] = value;
 				} else if (String.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asStrings() : value.mustBeSingle().asString();
 				} else if (Integer.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asIntegers() : value.mustBeSingle().asInteger();
 				} else if (Double.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asDoubles() : value.mustBeSingle().asDouble();
 				} else if (BigInteger.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asBigIntegers() : value.mustBeSingle().asBigInteger();
 				} else if (BigDecimal.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asBigDecimals() : value.mustBeSingle().asBigDecimal();
 				} else if (URI.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asURIs() : value.mustBeSingle().asURI();
 				} else if (File.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asFiles() : value.mustBeSingle().asFile();
 				} else if (Date.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asDates() : value.mustBeSingle().asDate();
 				} else if (XMLGregorianCalendar.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asXmlDates() : value.mustBeSingle().asXmlDate();
 				} else if (Duration.class.isAssignableFrom(paramType)) {
 					temp[i] = isList ? value.asDurations() : value.mustBeSingle().asDuration();
 				} else {
 					throw new UnsupportedOperationException("Unsupported custom type " + paramType); //$NON-NLS-1$
 				}
 			} else if (Boolean.FALSE == paramSpec.third()) {
 				throw new RuntimeException("Missing mandatory argument " + paramSpec.first()); //$NON-NLS-1$
 			}
 		}
 		try {
 			return (Integer) this.commandMethod.invoke(null, temp);
 		} catch (final IllegalArgumentException e) {
 			throw DelegatedException.delegate(e);
 		} catch (final IllegalAccessException e) {
 			throw DelegatedException.delegate(e);
 		} catch (final InvocationTargetException e) {
 			throw DelegatedException.delegate(e);
 		} finally {
 			//
 		}
 	}
 
 	public String getNamespace() {
 		return this.namespace;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public String getFullname() {
 		return getNamespace() + "/" + getName(); //$NON-NLS-1$
 	}
 
 	@Override
 	@SuppressWarnings("nls")
 	public String toString() {
 		final StringBuilder temp = new StringBuilder().append(this.namespace).append("/").append(this.name);
 		for (final Triplet<String, Type, Boolean> paramSpec : this.commandParameterMapping) {
 			final Type t = paramSpec.second();
 			String typeName = null;
 			if (t instanceof Class) {
 				typeName = ((Class<?>) t).getSimpleName();
 			} else if (t instanceof ParameterizedType) {
 				final Class<?> tt = getListType((ParameterizedType) t);
 				if (tt != null) {
 					typeName = tt.getSimpleName() + "s"; //$NON-NLS-1$
 				}
 			}
 			if (paramSpec.third().booleanValue()) {
 				temp.append(" [").append(paramSpec.first()).append(":").append(typeName).append("]");
 			} else {
 				temp.append(" ").append(paramSpec.first()).append(":").append(typeName);
 			}
 		}
 		return temp.toString();
 	}
 
 	private final Class<?> getListType(final ParameterizedType type) {
 		final Type rawType = type.getRawType();
 		if (rawType == List.class || rawType == Collection.class) {
 			final Type implType = type.getActualTypeArguments()[0];
 			if (implType instanceof Class) {
 				return (Class<?>) implType;
 			}
 		}
 		return null;
 	}
 }
