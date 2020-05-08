 /*
  * Copyright (C) 2009 - 2012 SMVP.NET
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
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
 
 package net.smvp.factory.client.aop;
 
import com.google.gwt.user.client.Window;
 import net.smvp.aop.client.interceptor.HasInterceptor;
 import net.smvp.aop.client.interceptor.MethodInterceptor;
 import net.smvp.factory.client.creator.Creator;
 import net.smvp.reflection.client.clazz.ClassType;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The Class AopCreator.
  *
  * @author Nguyen Duc Dung
  * @since 12/13/11, 10:09 AM
  */
 public class AopCreator implements Creator {
 
     protected List<MethodInterceptor> interceptors = new ArrayList<MethodInterceptor>();
 
     @Override
     public <T> T create(Class<?> clazz) {
         //Don't do anything GWT generator will do it.
         return null;
     }
 
     @Override
     public boolean isFor(Class<?> classType) {
         return classType != ClassType.class;
     }
 
     protected void injectInterceptor(HasInterceptor hasInterceptor) {
         for (MethodInterceptor interceptor : interceptors) {
             hasInterceptor.addInterceptor(interceptor);
         }
     }
 }
