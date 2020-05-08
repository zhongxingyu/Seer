 /*
  * Copyright (C) VSPLF Software Foundation (VSF), the origin author or authors.
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
 package org.vsplf.i18n.impl;
 
import java.util.Locale;
 import java.util.Map;
 
 import org.vsplf.i18n.Translator;
 
 /**
  * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
  * @since 2/27/12
  */
 public class I18NTranslator implements Translator {
 
  @Override
   public String _(String messagePattern) {
     return null;  //To change body of implemented methods use File | Settings | File Templates.
   }
 
  @Override
  public String _(String messagePattern, Locale locale) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String _(String messagePattern, Object[] messageArguments, Locale locale) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
   public String _(String messagePattern, Object[] messageArguments) {
     return null;  //To change body of implemented methods use File | Settings | File Templates.
   }
 
  @Override
   public String _(String messagePattern, Map<String, Object> messageArguments) {
     return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

  @Override
  public String _(String messagePattern, Map<String, Object> messageArguments, Locale locale) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
 }
