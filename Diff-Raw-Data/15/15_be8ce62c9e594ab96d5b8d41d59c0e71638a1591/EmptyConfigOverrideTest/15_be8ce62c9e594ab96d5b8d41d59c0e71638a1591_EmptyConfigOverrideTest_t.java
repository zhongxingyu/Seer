/**
 * Copyright (C) 2012 Ness Computing, Inc.
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
 package com.nesscomputing.config;
 
 import java.util.Collections;
 
 import org.apache.commons.configuration.MapConfiguration;
 import org.junit.Test;
 
 public class EmptyConfigOverrideTest
 {
     @Test
     public void testEmptyConfigOverride()
     {
         // Fails in ness-cache <= 2.1.0
         Config.getOverriddenConfig(Config.getEmptyConfig(), new MapConfiguration(Collections.EMPTY_MAP));
     }
 }
