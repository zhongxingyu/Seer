 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.reflect.plugins;
 
 /**
  * JBMICROCONT-119 issue
  * Support integer progression, e.g. float -> int or Float -> Integer and vice versa
  *
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public interface ProgressionConvertor
 {
 
    /**
     * Check if progression is supported.
     *
     * @param target
     * @param source
     * @return true, if we can progress source's value class type to target class
     */
    boolean canProgress(Class<? extends Object> target, Class<? extends Object> source);
 
    /**
     * Do the actual progression.
     *
     * @param target class type
     * @param value to progress
     * @return progressed value - it's class type now equals to target
     */
    Object doProgression(Class<? extends Object> target, Object value) throws Throwable;
 
 }
