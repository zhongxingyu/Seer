 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.core.invoke;
 
 import com.google.common.collect.Lists;
 import org.joor.Reflect;
 import org.springframework.util.ClassUtils;
 import org.springframework.util.ReflectionUtils;
 import org.springframework.util.StringUtils;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 /**
  * {@code InvokeUtils} is utility for calling standardized methods:
  * <ol>
  * <li>getters</li>
  * <li>setters [not yet implemented]</li>
  * </ol>
  *
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public class InvokeUtils {
 
     private static final String GETTER_PREFIX = "get%s";
 
     private InvokeUtils() {
     }
 
     /**
      * Invokes method described as property of the {@code target} bean.
      * Supports nested getter call, if passed property is {@link java.lang.String} delimited by <b>.[dot] character</b>
      *
      * @param target
      *         to call getter from
      * @param property
      *         either single property or nested properties' path delimited by by <b>.[dot] character</b>
      *
      * @return value as {@link java.lang.Object}
      */
     public static Object invokeGetter(Object target, final String property) {
         final List<String> props = InvokeUtils.splitProperty(property);
         Object value = null;
         for (final String prop : props) {
             final Method method = ReflectionUtils.findMethod(target.getClass(), InvokeUtils.getGetterName(prop));
             if (method != null) {
                 ReflectionUtils.makeAccessible(method);
                 value = ReflectionUtils.invokeMethod(method, target);
             }
             if (value != null) {
                 target = value;
             }
         }
         return value;
     }
 
     /**
      * Invokes method described as property of the {@code target} bean.
      * Supports nested getter call, if passed property is {@link java.lang.String} delimited by <b>.[dot] character</b>
      * <p/>
      * Runs
      *
      * @param target
      *         to call getter from
      * @param property
      *         either single property or nested properties' path delimited by by <b>.[dot] character</b>
      * @param targetClazz
      *         class that retrieved value will be cast to
      * @param <RR>
      *         generic type parameter
      *
      * @return value as <code>&lt;RR&gt;</code>
      */
     @SuppressWarnings("unchecked")
     public static <RR> RR invokeGetter(final Object target, final String property, final Class<RR> targetClazz) {
         final Object value = InvokeUtils.invokeGetter(target, property);
         if (ClassUtils.isAssignableValue(targetClazz, value)) {
             return (RR) value;
         } else {
             return Reflect.on(value.getClass()).create(value).get();
         }
     }
 
     private static String getGetterName(final String property) {
         return String.format(GETTER_PREFIX, StringUtils.capitalize(property));
     }
 
     private static List<String> splitProperty(final String property) {
         final List<String> props = Lists.newArrayListWithExpectedSize(1);
         if (property.contains(".")) {
             props.addAll(
                     Lists.newArrayList(
                            StringUtils.trimArrayElements(StringUtils.split(property.trim(), "."))
                     )
             );
         } else {
             props.add(property);
         }
         return props;
     }
 }
