 /*
  * Copyright 2012 Barrie Treloar <barrie.treloar@gmail.com>
  *
  *  This file is part of USRPROPS Xtext Editor.
  *
  *  USRPROPS Xtext Editor is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  USRPROPS Xtext Editor is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with USRPROPS Xtext Editor.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.usrprops_xtext.services;
 
 import org.eclipse.xtext.conversion.IValueConverter;
 import org.eclipse.xtext.conversion.ValueConverter;
 import org.eclipse.xtext.conversion.impl.AbstractDeclarativeValueConverterService;
 
 import com.github.usrprops_xtext.conversion.STRINGValueConverter;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * Converters for Usrprops DSL.
  * <p>
 * Uses normal XText defaults except for Strings, which are not Java strings in the Usrprops DSL, but
 * dumb strings contained within double quotes, i.e. no escape handling.
  * </p>
  * <p>
 * Name rule is handled separately so that the STRING part get the quotes stripped.
  * </p>
  */
 @Singleton
 public class UsrpropsDslTerminalConverters extends AbstractDeclarativeValueConverterService {
 
     @Inject
     private STRINGValueConverter stringValueConverter;
 
     @ValueConverter(rule = "STRING")
     public IValueConverter<String> STRING() {
         return stringValueConverter;
     }
 
 }
